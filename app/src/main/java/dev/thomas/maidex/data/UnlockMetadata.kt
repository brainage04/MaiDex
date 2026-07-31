package dev.thomas.maidex.data

internal object UnlockMetadata {
    private const val CIRCLE_ASIA_SOURCE = "https://silentblue.remywiki.com/maimai_DX:CiRCLE_(Asia)"
    private const val PERFECT_CHALLENGE_SOURCE = "https://silentblue.remywiki.com/maimai_DX:Perfect_Challenge"
    private const val FRIEND_MATCHING_SOURCE = "https://silentblue.remywiki.com/maimai_DX:Friend_Matching"
    private const val DAREDEVIL_SOURCE = "https://silentblue.remywiki.com/Daredevil_Glaive"
    private const val SEVEN_WONDERS_SOURCE = "https://silentblue.remywiki.com/7_Wonders"

    private data class AreaGroup(
        val id: String,
        val name: String,
        val nameRomanized: String,
        val songs: List<UnlockGuideSong>,
    )

    private data class ClassBattleGroup(
        val id: String,
        val version: String,
        val songs: List<UnlockGuideSong>,
    )

    private val areaGroups = listOf(
        AreaGroup(
            "chiho-paradigm-reboot",
            "Paradigm: Rebootちほー",
            "Paradigm: Reboot Area",
            songs("零號車輛"),
        ),
        AreaGroup(
            "chiho-tricoro",
            "トリコロちほー",
            "Tricolo Area",
            songs("Magical Paradox", "殿ッ！？ご乱心！？"),
        ),
        AreaGroup(
            "chiho-feat-contest",
            "FEAT CONTESTちほー",
            "FEAT CONTEST Area",
            songs("拝啓、最高の思い出たち", "おべんきょうたいむ", "るろうらんる"),
        ),
        AreaGroup(
            "chiho-kawaii-2",
            "kawaiiちほー2",
            "kawaii Area 2",
            songs("真空都市", "Eternal Return", "ぱぱぱらビーチ", "Get U ♭ack"),
        ),
        AreaGroup(
            "chiho-cosmo-2",
            "cosMo＠暴走Pちほー2",
            "cosMo@Bousou-P Area 2",
            songs("ラストピースに祝福と栄光を"),
        ),
        AreaGroup(
            "chiho-takamagahara-2",
            "高天原ちほー2",
            "Takamagahara Area 2",
            songs("ミクマリ", "雲外蒼電 -Dreaming Voltage-", "鬼女紅妖", "華天月兎"),
        ),
        AreaGroup(
            "chiho-sky-street-7",
            "スカイストリートちほー7",
            "Sky Street Area 7",
            songs("スローグロー", "ECHO,", "Phase: Theatre", "Sky Trails"),
        ),
        AreaGroup(
            "chiho-ongeki-9",
            "オンゲキちほー9",
            "ONGEKI Area 9",
            listOf(
                UnlockGuideSong("ICEBURN", "Progress through the area"),
                UnlockGuideSong("Daredevil Glaive", "Reach 525 km"),
            ),
        ),
        AreaGroup(
            "chiho-tricoro-2",
            "トリコロちほー2",
            "Tricolo Area 2",
            listOf(UnlockGuideSong("ソテリア", "Final-track Perfect Challenge")),
        ),
    )

    private val classBattleGroups = listOf(
        ClassBattleGroup(
            "class-circle-plus",
            "CiRCLE PLUS",
            listOf(UnlockGuideSong("Break The Speakers", "Japan")),
        ),
        ClassBattleGroup(
            "class-circle",
            "CiRCLE",
            listOf(
                UnlockGuideSong("Customized Justice", "Japan"),
                UnlockGuideSong("7 Wonders", "International"),
            ),
        ),
        ClassBattleGroup("class-prism-plus", "PRiSM PLUS", songs("ATLAS RUSH")),
        ClassBattleGroup("class-prism", "PRiSM", songs("Cryptarithm")),
        ClassBattleGroup("class-buddies-plus", "BUDDiES PLUS", songs("IF:U")),
        ClassBattleGroup("class-buddies", "BUDDiES", songs("Latent Kingdom")),
        ClassBattleGroup("class-festival-plus", "FESTiVAL PLUS", songs("VeRForTe αRtE:VEiN")),
        ClassBattleGroup("class-festival", "FESTiVAL", songs("mystique as iris")),
        ClassBattleGroup("class-universe-plus", "UNiVERSE PLUS", songs("sølips")),
        ClassBattleGroup("class-universe", "UNiVERSE", songs("Lia=Fail")),
        ClassBattleGroup("class-splash-plus", "Splash PLUS", songs("Heavenly Blast")),
        ClassBattleGroup(
            "class-splash",
            "Splash",
            listOf(
                UnlockGuideSong("BATTLE NO.1", "Japan"),
                UnlockGuideSong("≠彡\"/了→", "International"),
            ),
        ),
    )

    val guideEntries: List<UnlockGuideEntry> =
        areaGroups.map { area ->
            UnlockGuideEntry(
                id = area.id,
                section = UnlockGuideSection.CHIHOS,
                title = area.name,
                titleRomanized = area.nameRomanized,
                subtitle = "${area.songs.size} unlock ${if (area.songs.size == 1) "song" else "songs"}",
                details = "International area progression. Final tracks may use a life-mode Perfect Challenge whose Life allowance relaxes over time.",
                songs = area.songs,
                sourceUrl = CIRCLE_ASIA_SOURCE,
            )
        } + classBattleGroups.map { battle ->
            UnlockGuideEntry(
                id = battle.id,
                section = UnlockGuideSection.CLASS_BATTLES,
                title = battle.version,
                titleRomanized = battle.version,
                subtitle = "Conduction / Gift ${if (battle.songs.size == 1) "Song" else "Songs"}",
                details = "Defeat the final Friend Matching class boss and reach LEGEND, or play with a LEGEND-class conductor. A previous version's Gift Song becomes available by default after the next version update.",
                songs = battle.songs,
                sourceUrl = FRIEND_MATCHING_SOURCE,
            )
        }

    private val bySourceSongId: Map<String, List<SongUnlockInfo>> = buildMap {
        areaGroups.forEach { area ->
            val info = SongUnlockInfo(
                label = "Chiho unlock",
                summary = area.displayName,
                details = "International: progress through ${area.displayName} to unlock this song for regular play.",
                sourceUrl = CIRCLE_ASIA_SOURCE,
                guideEntryId = area.id,
            )
            area.songs.forEach { put(it.title, listOf(info)) }
        }
        classBattleGroups.forEach { battle ->
            battle.songs.forEach { song ->
                put(
                    song.title,
                    listOf(
                        SongUnlockInfo(
                            label = "Class battle unlock",
                            summary = "${battle.version} Gift Song" +
                                song.requirement.takeIf(String::isNotBlank)?.let { " · $it" }.orEmpty(),
                            details = "Defeat the final Friend Matching class boss and reach LEGEND, or receive the song from a LEGEND-class conductor. Previous-version Gift Songs become available by default after a version update.",
                            sourceUrl = FRIEND_MATCHING_SOURCE,
                            guideEntryId = battle.id,
                        ),
                    ),
                )
            }
        }

        perfectChallenge(
            song = "殿ッ！？ご乱心！？",
            area = "トリコロちほー (Tricoro Chiho)",
            guideEntryId = "chiho-tricoro",
            internationalDate = "2026-01-22",
        )
        perfectChallenge(
            song = "るろうらんる",
            area = "FEAT CONTESTちほー (FEAT CONTEST Chiho)",
            guideEntryId = "chiho-feat-contest",
            internationalDate = "2026-02-13",
        )
        perfectChallenge(
            song = "Get U ♭ack",
            area = "kawaiiちほー2 (kawaii Chiho 2)",
            guideEntryId = "chiho-kawaii-2",
            internationalDate = "2026-03-19",
        )
        perfectChallenge(
            song = "華天月兎",
            area = "高天原ちほー2 (Takamagahara Chiho 2)",
            guideEntryId = "chiho-takamagahara-2",
            internationalDate = "2026-05-01",
        )
        perfectChallenge(
            song = "Sky Trails",
            area = "スカイストリートちほー7 (Sky Street Chiho 7)",
            guideEntryId = "chiho-sky-street-7",
            internationalDate = "2026-06-12",
        )
        put(
            "ソテリア",
            listOf(
                SongUnlockInfo(
                    label = "Perfect Challenge",
                    summary = "トリコロちほー2 (Tricoro Chiho 2) final track",
                    details = "International CiRCLE PLUS: available from 2026-07-23. Clear the life-mode Perfect Challenge to unlock regular play. Each non-Perfect judgement costs 1 Life; the allowance relaxes over time.",
                    sourceUrl = PERFECT_CHALLENGE_SOURCE,
                    guideEntryId = "chiho-tricoro-2",
                ),
            ),
        )
        put(
            "Daredevil Glaive",
            listOf(
                SongUnlockInfo(
                    label = "Chiho unlock",
                    summary = "オンゲキちほー9 (ONGEKI Chiho 9) · 525 km",
                    details = "International CiRCLE: available from 2026-07-10. Reach a total distance of 525 km in オンゲキちほー9 (ONGEKI Chiho 9) to unlock it permanently. Daredevil Glaive is not the area's Perfect Challenge track.",
                    sourceUrl = DAREDEVIL_SOURCE,
                    guideEntryId = "chiho-ongeki-9",
                ),
            ),
        )
        put(
            "7 Wonders",
            listOf(
                SongUnlockInfo(
                    label = "Former class battle unlock",
                    summary = "Friend Matching · now available by default",
                    details = "International history: initially unlocked by defeating the SSS1 boss friend and reaching LEGEND, or by playing with a LEGEND-class conductor. From 2026-04-24 it became an SSS5+ special boss requiring rank SSS; from 2026-05-29 the class requirement dropped to SS5+. It is available by default from CiRCLE PLUS.",
                    sourceUrl = SEVEN_WONDERS_SOURCE,
                    guideEntryId = "class-circle",
                ),
            ),
        )
        put(
            "Latent Kingdom",
            listOf(
                SongUnlockInfo(
                    label = "Former class battle unlock",
                    summary = "BUDDiES Gift Song · now available by default",
                    details = "Originally unlocked through the BUDDiES Friend Matching class battle. It became available by default after the BUDDiES PLUS version update and no longer requires a class-battle unlock.",
                    sourceUrl = FRIEND_MATCHING_SOURCE,
                    guideEntryId = "class-buddies",
                ),
            ),
        )
    }

    fun forSong(sourceSongId: String): List<SongUnlockInfo> = bySourceSongId[sourceSongId].orEmpty()

    fun guideEntry(id: String): UnlockGuideEntry? = guideEntries.firstOrNull { it.id == id }

    private fun MutableMap<String, List<SongUnlockInfo>>.perfectChallenge(
        song: String,
        area: String,
        guideEntryId: String,
        internationalDate: String,
    ) {
        put(
            song,
            listOf(
                SongUnlockInfo(
                    label = "Perfect Challenge",
                    summary = "$area final track",
                    details = "International CiRCLE: available from $internationalDate. Clear the life-mode Perfect Challenge to unlock regular play. Each non-Perfect judgement costs 1 Life; the allowance relaxes over time.",
                    sourceUrl = PERFECT_CHALLENGE_SOURCE,
                    guideEntryId = guideEntryId,
                ),
            ),
        )
    }
    private val AreaGroup.displayName: String
        get() = "$name ($nameRomanized)"


    private fun songs(vararg titles: String): List<UnlockGuideSong> =
        titles.map(::UnlockGuideSong)
}
