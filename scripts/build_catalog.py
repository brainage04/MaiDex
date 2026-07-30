#!/usr/bin/env python3
"""Build the bundled maimai catalog from arcade-songs' published dataset."""

from __future__ import annotations

import argparse
import json
import re
import sqlite3
import unicodedata
import urllib.request
from pathlib import Path

from pykakasi import kakasi

SOURCE_URL = "https://dp4p6x0xfi5o9.cloudfront.net/maimai/data.json"
IMAGE_BASE_URL = "https://dp4p6x0xfi5o9.cloudfront.net/maimai/img/cover"
ALIASES_SOURCE_URL = "https://github.com/lomotos10/GCM-bot/blob/master/data/aliases/en/maimai.tsv"



def romanizer():
    converter = kakasi()

    def romanize(value: str | None) -> str:
        if not value:
            return ""
        converted = " ".join(
            part["hepburn"] if part["orig"] != part["hepburn"] else part["orig"]
            for part in converter.convert(value)
        )
        converted = unicodedata.normalize("NFKC", converted)
        converted = re.sub(r"\s+([,.:;!?'\"\]\)])", r"\1", converted)
        converted = re.sub(r"([\[\(])\s+", r"\1", converted)
        return re.sub(r"\s+", " ", converted).strip()

    return romanize


def fetch_catalog(source: str) -> dict:
    if source.startswith(("http://", "https://")):
        request = urllib.request.Request(source, headers={"User-Agent": "MaiDex catalog builder"})
        with urllib.request.urlopen(request, timeout=60) as response:
            return json.load(response)
    with open(source, encoding="utf-8") as source_file:
        return json.load(source_file)


def load_aliases(path: Path) -> dict[str, list[str]]:
    aliases: dict[str, list[str]] = {}
    with path.open(encoding="utf-8") as alias_file:
        for line in alias_file:
            values = [value.strip() for value in line.rstrip("\n").split("\t")]
            if len(values) > 1:
                aliases[values[0]] = [value for value in values[1:] if value]
    return aliases


def create_database(catalog: dict, aliases: dict[str, list[str]], destination: Path) -> None:
    destination.parent.mkdir(parents=True, exist_ok=True)
    destination.unlink(missing_ok=True)
    romanize = romanizer()

    connection = sqlite3.connect(destination)
    connection.executescript(
        """
        PRAGMA journal_mode = OFF;
        PRAGMA synchronous = OFF;
        PRAGMA foreign_keys = ON;
        CREATE TABLE metadata (
            key TEXT PRIMARY KEY,
            value TEXT NOT NULL
        );
        CREATE TABLE songs (
            id INTEGER PRIMARY KEY,
            source_id TEXT NOT NULL UNIQUE,
            category TEXT NOT NULL,
            title TEXT NOT NULL,
            title_romanized TEXT NOT NULL,
            title_aliases TEXT NOT NULL,
            artist TEXT NOT NULL,
            artist_romanized TEXT NOT NULL,
            bpm INTEGER,
            image_url TEXT NOT NULL,
            version TEXT NOT NULL,
            release_date TEXT,
            is_new INTEGER NOT NULL,
            is_locked INTEGER NOT NULL,
            comment TEXT
        );
        CREATE TABLE charts (
            id INTEGER PRIMARY KEY,
            chart_key TEXT NOT NULL UNIQUE,
            song_id INTEGER NOT NULL REFERENCES songs(id),
            type TEXT NOT NULL,
            difficulty TEXT NOT NULL,
            level TEXT,
            level_value REAL,
            internal_level TEXT,
            chart_constant REAL,
            note_designer TEXT,
            note_designer_romanized TEXT NOT NULL,
            tap_count INTEGER,
            hold_count INTEGER,
            slide_count INTEGER,
            touch_count INTEGER,
            break_count INTEGER,
            total_count INTEGER,
            region_jp INTEGER NOT NULL,
            region_intl INTEGER NOT NULL,
            region_usa INTEGER NOT NULL,
            region_cn INTEGER NOT NULL,
            region_overrides TEXT NOT NULL,
            is_special INTEGER NOT NULL,
            version TEXT
        );
        CREATE INDEX songs_title_idx ON songs(title COLLATE NOCASE);
        CREATE INDEX songs_artist_idx ON songs(artist COLLATE NOCASE);
        CREATE INDEX songs_category_idx ON songs(category);
        CREATE INDEX charts_song_idx ON charts(song_id);
        CREATE INDEX charts_level_idx ON charts(chart_constant, level_value);
        CREATE INDEX charts_filter_idx ON charts(difficulty, type, version);
        """
    )

    connection.executemany(
        "INSERT INTO metadata(key, value) VALUES (?, ?)",
        [
            ("source_url", SOURCE_URL),
            ("aliases_source_url", ALIASES_SOURCE_URL),
            ("catalog_update_time", catalog.get("updateTime", "")),
            ("song_count", str(len(catalog["songs"]))),
            ("chart_count", str(sum(len(song["sheets"]) for song in catalog["songs"]))),
            ("categories", json.dumps(catalog.get("categories", []), ensure_ascii=False)),
            ("versions", json.dumps(catalog.get("versions", []), ensure_ascii=False)),
            ("types", json.dumps(catalog.get("types", []), ensure_ascii=False)),
            ("difficulties", json.dumps(catalog.get("difficulties", []), ensure_ascii=False)),
            ("regions", json.dumps(catalog.get("regions", []), ensure_ascii=False)),
        ],
    )

    song_rows = []
    chart_rows = []
    for song_id, song in enumerate(catalog["songs"], start=1):
        title = song.get("title") or song["songId"]
        artist = song.get("artist") or ""
        title_aliases = aliases.get(title, [])
        song_rows.append(
            (
                song_id,
                song["songId"],
                song.get("category") or "",
                title,
                title_aliases[0] if title_aliases else romanize(title),
                "\u001e".join(title_aliases),
                artist,
                romanize(artist),
                song.get("bpm"),
                f"{IMAGE_BASE_URL}/{song['imageName']}",
                song.get("version") or "",
                song.get("releaseDate"),
                int(bool(song.get("isNew"))),
                int(bool(song.get("isLocked"))),
                song.get("comment"),
            )
        )
        for sheet in song["sheets"]:
            chart_key = "\u001f".join(
                [song["songId"], sheet.get("type") or "", sheet.get("difficulty") or ""]
            )
            counts = sheet.get("noteCounts") or {}
            regions = sheet.get("regions") or {}
            designer = sheet.get("noteDesigner") or ""
            chart_rows.append(
                (
                    chart_key,
                    song_id,
                    sheet.get("type") or "",
                    sheet.get("difficulty") or "",
                    sheet.get("level"),
                    sheet.get("levelValue"),
                    sheet.get("internalLevel"),
                    sheet.get("internalLevelValue"),
                    designer,
                    romanize(designer),
                    counts.get("tap"),
                    counts.get("hold"),
                    counts.get("slide"),
                    counts.get("touch"),
                    counts.get("break"),
                    counts.get("total"),
                    int(bool(regions.get("jp"))),
                    int(bool(regions.get("intl"))),
                    int(bool(regions.get("usa"))),
                    int(bool(regions.get("cn"))),
                    json.dumps(sheet.get("regionOverrides") or {}, ensure_ascii=False, separators=(",", ":")),
                    int(bool(sheet.get("isSpecial"))),
                    sheet.get("version"),
                )
            )

    connection.executemany(
        """INSERT INTO songs(
            id, source_id, category, title, title_romanized, title_aliases, artist,
            artist_romanized, bpm, image_url, version, release_date, is_new, is_locked, comment
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)""",
        song_rows,
    )
    connection.executemany(
        """INSERT INTO charts(
            chart_key, song_id, type, difficulty, level, level_value, internal_level,
            chart_constant, note_designer, note_designer_romanized, tap_count, hold_count,
            slide_count, touch_count, break_count, total_count, region_jp, region_intl,
            region_usa, region_cn, region_overrides, is_special, version
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)""",
        chart_rows,
    )
    connection.commit()
    connection.execute("VACUUM")
    connection.close()
    print(f"Wrote {len(song_rows)} songs and {len(chart_rows)} charts to {destination}")


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--source", default=SOURCE_URL)
    parser.add_argument(
        "--output",
        type=Path,
        default=Path("app/src/main/assets/catalog.db"),
    )
    parser.add_argument(
        "--aliases",
        type=Path,
        default=Path("data/maimai_aliases.tsv"),
    )
    args = parser.parse_args()
    create_database(fetch_catalog(args.source), load_aliases(args.aliases), args.output)


if __name__ == "__main__":
    main()
