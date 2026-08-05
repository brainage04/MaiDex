package io.github.brainage04.maidex.data

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

internal object ClassBattleMetadata {
    private const val SOURCE_URL =
        "https://silentblue.remywiki.com/maimai_DX:Friend_Matching/Bosses"
    private const val SPLASH_SOURCE_URL =
        "https://silentblue.remywiki.com/maimai_DX:Friend_Matching/Splash"

    fun sourceUrlForVersion(version: String): String =
        if (version == "Splash") SPLASH_SOURCE_URL else SOURCE_URL

    val byVersion: Map<String, List<ClassBattleMilestone>> = DATA
        .trimIndent()
        .lineSequence()
        .filter(String::isNotBlank)
        .map { row ->
            val fields = row.split('|')
            fields[0] to ClassBattleMilestone(
                className = fields[1],
                songTitle = fields[2],
                level = fields[3],
                opponentStrength = fields[4],
            )
        }
        .groupBy(
            keySelector = { (version, _) -> version },
            valueTransform = { (_, milestone) -> milestone },
        )

    private const val DATA = """
        CiRCLE PLUS|A5|明星ロケット|10|S+
        CiRCLE PLUS|A4|地獄|10+|S+
        CiRCLE PLUS|A3|Halcyon|11|S+
        CiRCLE PLUS|A2|マトリョシカ|11+|SS
        CiRCLE PLUS|A1|The Great Journey|11+|SS
        CiRCLE PLUS|S5|Unwelcome School|12|SS
        CiRCLE PLUS|S4|METEOR|12|SS+
        CiRCLE PLUS|S3|UTAKATA|12+|SS+
        CiRCLE PLUS|S2|キャットラビング|12+|SS+
        CiRCLE PLUS|S1|BANG!|13|SSS
        CiRCLE PLUS|SS5|不思議の国のクリスマス|13|SSS
        CiRCLE PLUS|SS4|Rush-More|13|SSS+
        CiRCLE PLUS|SS3|にゃーにゃー冒険譚|13+|SSS+
        CiRCLE PLUS|SS2|Baqeela|13+|SSS+
        CiRCLE PLUS|SS1|れっつ！みらくる☆はーどこあっ！|13+|SSS+
        CiRCLE PLUS|SSS5|麒麟|14|MAX
        CiRCLE PLUS|SSS4|GEOMETRIC DANCE|14|MAX
        CiRCLE PLUS|SSS3|VERTeX (rintaro soma deconstructed remix)|14+|MAX
        CiRCLE PLUS|SSS2|Re:Unknown X|14+|MAX
        CiRCLE PLUS|SSS1|Break The Speakers|14+|MAX
        CiRCLE|A5|過去を喰らう|10|S+
        CiRCLE|A4|Y.Y.Y.計画!!!!|10+|S+
        CiRCLE|A3|前衛的Landscape|11|S+
        CiRCLE|A2|メランコリック|11+|SS
        CiRCLE|A1|WARNING×WARNING×WARNING|11+|SS
        CiRCLE|S5|熱異常|12|SS
        CiRCLE|S4|maimaiちゃんのテーマ|12|SS+
        CiRCLE|S3|さくゆいたいそう|12+|SS+
        CiRCLE|S2|Spring of Dreams|12+|SS+
        CiRCLE|S1|転生林檎|13|SSS
        CiRCLE|SS5|橙の幻想郷音頭|13|SSS
        CiRCLE|SS4|ULTRA POWER|13|SSS+
        CiRCLE|SS3|MORNINGLOOM|13+|SSS+
        CiRCLE|SS2|YKWTD|13+|SSS+
        CiRCLE|SS1|LiftOff|13+|SSS+
        CiRCLE|SSS5|Ragnarok|14|MAX
        CiRCLE|SSS4|Abstruse Dilemma|14|MAX
        CiRCLE|SSS3|渦状銀河のシンフォニエッタ|14+|MAX
        CiRCLE|SSS2|Metamorphosism|14+|MAX
        CiRCLE|SSS1|Customized Justice|14+|MAX
        PRiSM PLUS|A5|トンデモワンダーズ|10|S+
        PRiSM PLUS|A4|Bad Apple!! feat.nomico ～五十嵐 撫子 Ver.～|10+|S+
        PRiSM PLUS|A3|アイドル|11|S+
        PRiSM PLUS|A2|Love or Lies|11+|SS
        PRiSM PLUS|A1|STARTLINER|11+|SS
        PRiSM PLUS|S5|再会|12|SS
        PRiSM PLUS|S4|Link|12|SS+
        PRiSM PLUS|S3|Strobe♡Girl|12+|SS+
        PRiSM PLUS|S2|『んっあっあっ。』|12+|SS+
        PRiSM PLUS|S1|アルカンシエル|12+|SSS
        PRiSM PLUS|SS5|妖精村の月誕祭 ～Lunate Elf|13|SSS
        PRiSM PLUS|SS4|魔法少女とチョコレゐト|13|SSS+
        PRiSM PLUS|SS3|PinqPiq (xovevox Remix)|13+|SSS+
        PRiSM PLUS|SS2|Cyaegha|13+|SSS+
        PRiSM PLUS|SS1|華の集落、秋のお届け|13+|SSS+
        PRiSM PLUS|SSS5|インパアフェクシオン・ホワイトガアル|14|MAX
        PRiSM PLUS|SSS4|VERTeX (rintaro soma deconstructed remix)|14+|MAX
        PRiSM PLUS|SSS3|Prophesy One|14+|MAX
        PRiSM PLUS|SSS2|チューリングの跡|14+|MAX
        PRiSM PLUS|SSS1|ATLAS RUSH|14+|MAX
        PRiSM|A5|阿修羅ちゃん|10|S+
        PRiSM|A4|モンダイナイトリッパー！|10+|S+
        PRiSM|A3|Crush On You|11|S+
        PRiSM|A2|39みゅーじっく！|11+|SS
        PRiSM|A1|INTERNET YAMERO|11+|SS
        PRiSM|S5|Virtual to LIVE|12|SS
        PRiSM|S4|STEREOSCAPE|12|SS+
        PRiSM|S3|猫猫的宇宙論|12+|SS+
        PRiSM|S2|物凄い勢いでけーねが物凄いうた|12+|SS+
        PRiSM|S1|ずんだもんの朝食 〜目覚ましずんラップ〜|13|SSS
        PRiSM|SS5|Y.Y.Y.計画!!!!|13|SSS
        PRiSM|SS4|スカーレット警察のゲットーパトロール24時|13|SSS+
        PRiSM|SS3|ネトゲ廃人シュプレヒコール|13+|SSS+
        PRiSM|SS2|D✪N’T ST✪P R✪CKIN’|13+|SSS+
        PRiSM|SS1|ozma|13+|SSS+
        PRiSM|SSS5|エンドマークに希望と涙を添えて|14|MAX
        PRiSM|SSS4|Sage|14|MAX
        PRiSM|SSS3|WiPE OUT MEMORIES|14+|MAX
        PRiSM|SSS2|Straight into the lights|14+|MAX
        PRiSM|SSS1|Cryptarithm|14+|MAX
        BUDDiES PLUS|A5|エイリアンエイリアン|10|S+
        BUDDiES PLUS|A4|Alice's Suitcase|10+|S+
        BUDDiES PLUS|A3|りばーぶ|11|S+
        BUDDiES PLUS|A2|バグ|11+|SS
        BUDDiES PLUS|A1|WARNING×WARNING×WARNING(DX)|11+|SS
        BUDDiES PLUS|S5|Virtual to LIVE|12|SS
        BUDDiES PLUS|S4|Counselor|12|SS+
        BUDDiES PLUS|S3|フラグメンツ -T.V. maimai edit-|12+|SS+
        BUDDiES PLUS|S2|Ututu|12+|SS+
        BUDDiES PLUS|S1|私のドッペルゲンガー|13|SSS
        BUDDiES PLUS|SS5|Grip & Break down !! (STD)|13|SSS
        BUDDiES PLUS|SS4|ツクヨミステップ|13|SSS+
        BUDDiES PLUS|SS3|エゴロック|13+|SSS+
        BUDDiES PLUS|SS2|See The Light|13+|SSS+
        BUDDiES PLUS|SS1|First Dance|13+|SSS+
        BUDDiES PLUS|SSS5|Party☆People☆Princess|14|MAX
        BUDDiES PLUS|SSS4|otorii INNOVATED -[i]3-|14|MAX
        BUDDiES PLUS|SSS3|AMAZING MIGHTYYYY!!!!|14+|MAX
        BUDDiES PLUS|SSS2|怒槌|14+|MAX
        BUDDiES PLUS|SSS1|IF:U|14+|MAX
        BUDDiES|A5|MAGENTA POTION|10|S+
        BUDDiES|A4|ナイト・オブ・ナイツ|11+|S+
        BUDDiES|A3|ZIGG-ZAGG|11|S+
        BUDDiES|A2|マトリョシカ|11+|SS
        BUDDiES|A1|テレキャスタービーボーイ|11+|SS
        BUDDiES|S5|Crazy Circle|12|SS
        BUDDiES|S4|INFINITE WORLD|12|SS
        BUDDiES|S3|クレイジー・ビート|12+|SS+
        BUDDiES|S2|不機嫌なスリーカード|12+|SS+
        BUDDiES|S1|アカツキアライヴァル|13|SSS
        BUDDiES|SS5|ちがう!!!|13|SSS
        BUDDiES|SS4|クレイジークレイジーダンサーズ|13|SSS+
        BUDDiES|SS3|なるとなぎのパーフェクトロックンロール教室|13+|SSS+
        BUDDiES|SS2|にゃーにゃー冒険譚|13+|SSS+
        BUDDiES|SS1|DADDY MULK -Groove remix-|13+|SSS+
        BUDDiES|SSS5|VSpook!|14|MAX
        BUDDiES|SSS4|Swift Swing|14|MAX
        BUDDiES|SSS3|Yorugao|14+|MAX
        BUDDiES|SSS2|封焔の135秒|14+|MAX
        BUDDiES|SSS1|Latent Kingdom|14+|MAX
        FESTiVAL PLUS|A5|エイリアンエイリアン|10|S+
        FESTiVAL PLUS|A4|You Mean the World to Me|11|S+
        FESTiVAL PLUS|A3|りばーぶ|11|S+
        FESTiVAL PLUS|A2|クレイジークレイジーダンサーズ|11+|SS
        FESTiVAL PLUS|A1|だれかの心臓になれたなら|11+|SS
        FESTiVAL PLUS|S5|Counselor|12|SS
        FESTiVAL PLUS|S4|MSSPlanet|12|SS
        FESTiVAL PLUS|S3|悪戯センセーション|12+|SS+
        FESTiVAL PLUS|S2|おねがいダーリン|12+|SS+
        FESTiVAL PLUS|S1|二息歩行|13|SSS
        FESTiVAL PLUS|SS5|Falling|13|SSS
        FESTiVAL PLUS|SS4|踊|13|SSS+
        FESTiVAL PLUS|SS3|エテルニタス・ルドロジー|13+|SSS+
        FESTiVAL PLUS|SS2|OTOGEMA|13+|SSS+
        FESTiVAL PLUS|SS1|パラマウント☆ショータイム！！|13+|SSS+
        FESTiVAL PLUS|SSS5|#狂った民族２ PRAVARGYAZOOQA|14|MAX
        FESTiVAL PLUS|SSS4|Credits|14|MAX
        FESTiVAL PLUS|SSS3|Dingle Bell|14+|MAX
        FESTiVAL PLUS|SSS2|the EmpErroR|14+|MAX
        FESTiVAL PLUS|SSS1|VeRForTe αRtE:VEiN|14+|MAX
        FESTiVAL|A5|アルティメットセンパイ|9|S+
        FESTiVAL|A4|Limits|10|S+
        FESTiVAL|A3|JACKY [Remix]|11|S+
        FESTiVAL|A2|ロキ|11+|SS
        FESTiVAL|A1|バラライカ|11+|SS
        FESTiVAL|S5|アルカリレットウセイ|12|SS
        FESTiVAL|S4|バレリーコ|12|SS
        FESTiVAL|S3|源平大戦絵巻テーマソング|12+|SS+
        FESTiVAL|S2|Paranoia|12+|SS+
        FESTiVAL|S1|ぱくぱく☆がーる|13|SSS
        FESTiVAL|SS5|四次元跳躍機関|13|SSS
        FESTiVAL|SS4|ヴァンパイア|13|SSS+
        FESTiVAL|SS3|スーパーシンメトリー|13+|SSS+
        FESTiVAL|SS2|LANCE|13+|SSS+
        FESTiVAL|SS1|METATRON|13+|SSS+
        FESTiVAL|SSS5|Good bye, Merry-Go-Round.|14|MAX
        FESTiVAL|SSS4|脳天直撃|14|MAX
        FESTiVAL|SSS3|ARAIS|14|MAX
        FESTiVAL|SSS2|躯樹の墓守|14+|MAX
        FESTiVAL|SSS1|mystique as iris|14+|MAX
        UNiVERSE PLUS|A5|STARTLINER|9|S+
        UNiVERSE PLUS|A4|テレキャスタービーボーイ|9+|S+
        UNiVERSE PLUS|A3|美しく燃える森|10+|S+
        UNiVERSE PLUS|A2|ブリキノダンス|12|SS
        UNiVERSE PLUS|A1|StargazeR|11+|SS
        UNiVERSE PLUS|S5|Backyun! -悪い女-|12+|SS
        UNiVERSE PLUS|S4|TRUST|12+|SS
        UNiVERSE PLUS|S3|ダンスロボットダンス|12+|SS+
        UNiVERSE PLUS|S2|ミラクルペイント|13|SS+
        UNiVERSE PLUS|S1|トランスダンスアナーキー|13|SSS
        UNiVERSE PLUS|SS5|L9|13|SSS
        UNiVERSE PLUS|SS4|TwisteD! XD|13|SSS
        UNiVERSE PLUS|SS3|Sound Chimera|13+|SSS+
        UNiVERSE PLUS|SS2|マツヨイナイトバグ|13+|SSS+
        UNiVERSE PLUS|SS1|華の集落、秋のお届け|13+|SSS+
        UNiVERSE PLUS|SSS5|ワンダーシャッフェンの法則|14|SSS+
        UNiVERSE PLUS|SSS4|BLACK SWAN|14|SSS+
        UNiVERSE PLUS|SSS3|怒槌|14+|SSS+
        UNiVERSE PLUS|SSS2|封焔の135秒|14+|MAX
        UNiVERSE PLUS|SSS1|sølips|14+|MAX
        UNiVERSE|A5|六兆年と一夜物語|9|S
        UNiVERSE|A4|絡めトリック利己ライザー|10+|S+
        UNiVERSE|A3|りばーぶ|10+|S+
        UNiVERSE|A2|Tic Tac DREAMIN’|11|SS
        UNiVERSE|A1|バレリーコ|11+|SS
        UNiVERSE|S5|今、誰が為のかがり火へ|12|SS
        UNiVERSE|S4|welcome to maimai!! with マイマイマー|12|SS
        UNiVERSE|S3|Rooftop Run：Act1|12+|SS+
        UNiVERSE|S2|スローアライズ|12+|SS+
        UNiVERSE|S1|単一指向性オーバーブルーム|13|SS+
        UNiVERSE|SS5|トルコ行進曲 - オワタ＼(^o^)／|13|SSS
        UNiVERSE|SS4|Burning Hearts ～炎のANGEL～|13+|SSS
        UNiVERSE|SS3|Nerverakes|13+|SSS+
        UNiVERSE|SS2|Jack-the-Ripper◆|13+|SSS+
        UNiVERSE|SS1|MEGATON KICK|14|SSS+
        UNiVERSE|SSS5|Desperado Waltz|14|SSS+
        UNiVERSE|SSS4|AMAZING MIGHTYYYY!!!!|14|SSS+
        UNiVERSE|SSS3|Alea jacta est!|14+|SSS+
        UNiVERSE|SSS2|In Chaos|14+|MAX
        UNiVERSE|SSS1|Lia=Fail|14+|MAX
        Splash PLUS|A5|アディショナルメモリー|9|S+
        Splash PLUS|A4|ブレインジャックシンドローム|10+|S+
        Splash PLUS|A3|えれくとりっく・えんじぇぅ|10+|S+
        Splash PLUS|A2|Virtual to LIVE|11|SS
        Splash PLUS|A1|泡沫、哀のまほろば|11+|SS
        Splash PLUS|S5|MIRACLE RUSH|12|SS
        Splash PLUS|S4|キミノヨゾラ哨戒班|12|SS
        Splash PLUS|S3|炉心融解|12+|SS+
        Splash PLUS|S2|REAL VOICE|12+|SS+
        Splash PLUS|S1|KISS CANDY FLAVOR|13|SS+
        Splash PLUS|SS5|Signature|13|SSS
        Splash PLUS|SS4|閃鋼のブリューナク|13+|SSS
        Splash PLUS|SS3|ULTRA B+K|13+|SSS+
        Splash PLUS|SS2|Selector|13+|SSS+
        Splash PLUS|SS1|初音ミクの激唱|14|SSS+
        Splash PLUS|SSS5|雷切-RAIKIRI-|14|SSS+
        Splash PLUS|SSS4|Our Wrenally|14+|SSS+
        Splash PLUS|SSS3|larva|14+|SSS+
        Splash PLUS|SSS2|Regulus|14+|MAX
        Splash PLUS|SSS1|Heavenly Blast|14+|MAX
        Splash|初段|UTAKATA|9|S+
        Splash|二段|美しく燃える森|10|S+
        Splash|三段|むかしむかしのきょうのぼく / Living Universe|11|S+
        Splash|四段|ラブチーノ|11|S+
        Splash|五段|フォルテシモBELL|11+|S+
        Splash|六段|フリィダム ロリィタ|11+|S+
        Splash|七段|アカリがやってきたぞっ|12+|SS
        Splash|八段|ソーラン☆節|13|SS
        Splash|九段|Imitation:Loud Lounge|12+|SS+
        Splash|十段|Stardust Memories|12+|SS+
        Splash|皆伝|六兆年と一夜物語 (DX)|13|SSS
        Splash|壱皆伝|アトロポスと最果の探究者|13|SSS+
        Splash|弐皆伝|♡マイマイマイラブ♡|13|SSS+
        Splash|参皆伝|コネクト (DX)|12+|MAX
        Splash|肆皆伝|Entrance|13+|SSS+
        Splash|伍皆伝|NULCTRL|13+|MAX
        Splash|陸皆伝|アリサのテーマ / Rodeo Machine|13|MAX
        Splash|漆皆伝|Maboroshi|14|MAX
        Splash|捌皆伝|渦状銀河のシンフォニエッタ|14|MAX
        Splash|玖皆伝|Valsqotch|14+|MAX
        Splash|拾皆伝|BATTLE NO.1 / ≠彡"/了→|14+|MAX
    """
}
