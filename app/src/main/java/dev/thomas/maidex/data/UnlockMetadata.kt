package dev.thomas.maidex.data

internal object UnlockMetadata {
    private const val CIRCLE_ASIA_SOURCE = "https://silentblue.remywiki.com/maimai_DX:CiRCLE_(Asia)"
    private const val PERFECT_CHALLENGE_SOURCE = "https://silentblue.remywiki.com/maimai_DX:Perfect_Challenge"
    private const val DAREDEVIL_SOURCE = "https://silentblue.remywiki.com/Daredevil_Glaive"
    private const val SEVEN_WONDERS_SOURCE = "https://silentblue.remywiki.com/7_Wonders"

    private data class AreaGroup(val name: String, val songs: List<String>)

    private val areaGroups = listOf(
        AreaGroup("Paradigm: Reboot Chiho", listOf("零號車輛")),
        AreaGroup("Tricoro Chiho", listOf("Magical Paradox", "殿ッ！？ご乱心！？")),
        AreaGroup("FEAT CONTEST Chiho", listOf("拝啓、最高の思い出たち", "おべんきょうたいむ", "るろうらんる")),
        AreaGroup("kawaii Chiho 2", listOf("真空都市", "Eternal Return", "ぱぱぱらビーチ", "Get U ♭ack")),
        AreaGroup("cosMo@Bousou-P Chiho 2", listOf("ラストピースに祝福と栄光を")),
        AreaGroup("Takamagahara Chiho 2", listOf("ミクマリ", "雲外蒼電 -Dreaming Voltage-", "鬼女紅妖", "華天月兎")),
        AreaGroup("Sky Street Chiho 7", listOf("スローグロー", "ECHO,", "Phase: Theatre", "Sky Trails")),
        AreaGroup("ONGEKI Area 9", listOf("ICEBURN", "Daredevil Glaive")),
    )

    private val bySourceSongId: Map<String, List<SongUnlockInfo>> = buildMap {
        areaGroups.forEach { area ->
            val info = SongUnlockInfo(
                label = "Chiho unlock",
                summary = area.name,
                details = "International CiRCLE: progress through ${area.name} to unlock this song for regular play.",
                sourceUrl = CIRCLE_ASIA_SOURCE,
            )
            area.songs.forEach { put(it, listOf(info)) }
        }

        perfectChallenge(
            song = "殿ッ！？ご乱心！？",
            area = "Tricoro Chiho",
            internationalDate = "2026-01-22",
        )
        perfectChallenge(
            song = "るろうらんる",
            area = "FEAT CONTEST Chiho",
            internationalDate = "2026-02-13",
        )
        perfectChallenge(
            song = "Get U ♭ack",
            area = "kawaii Chiho 2",
            internationalDate = "2026-03-19",
        )
        perfectChallenge(
            song = "華天月兎",
            area = "Takamagahara Chiho 2",
            internationalDate = "2026-05-01",
        )
        perfectChallenge(
            song = "Sky Trails",
            area = "Sky Street Chiho 7",
            internationalDate = "2026-06-12",
        )
        put(
            "ソテリア",
            listOf(
                SongUnlockInfo(
                    label = "Perfect Challenge",
                    summary = "Tricoro Chiho 2 final track",
                    details = "International CiRCLE PLUS: available from 2026-07-23. Clear the life-mode Perfect Challenge to unlock regular play. Each non-Perfect judgement costs 1 Life; the allowance relaxes over time.",
                    sourceUrl = PERFECT_CHALLENGE_SOURCE,
                ),
            ),
        )

        put(
            "Daredevil Glaive",
            listOf(
                SongUnlockInfo(
                    label = "Chiho unlock",
                    summary = "ONGEKI Area 9 · 525 km",
                    details = "International CiRCLE: available from 2026-07-10. Reach a total distance of 525 km in ONGEKI Area 9 to unlock it permanently. Daredevil Glaive is not the area's Perfect Challenge track.",
                    sourceUrl = DAREDEVIL_SOURCE,
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
                ),
            ),
        )
    }

    fun forSong(sourceSongId: String): List<SongUnlockInfo> = bySourceSongId[sourceSongId].orEmpty()

    private fun MutableMap<String, List<SongUnlockInfo>>.perfectChallenge(
        song: String,
        area: String,
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
                ),
            ),
        )
    }
}
