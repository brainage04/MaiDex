package io.github.brainage04.maidex.data

enum class DanCourseGroup(val label: String) {
    NORMAL("Normal"),
    TRUE("True"),
    URA("Ura"),
}

data class DanLifeRule(
    val maximum: Int,
    val greatDamage: Int,
    val goodDamage: Int,
    val missDamage: Int,
    val trackBonus: Int,
)

data class DanTrack(
    val title: String,
    val type: String,
    val difficulty: String,
)

data class DanCourse(
    val id: String,
    val name: String,
    val nameEnglish: String,
    val group: DanCourseGroup,
    val life: DanLifeRule,
    val tracks: List<DanTrack>,
    private val internationalOverrides: Map<Int, DanTrack> = emptyMap(),
) {
    fun track(index: Int, region: AccountRegion): DanTrack =
        if (region == AccountRegion.INTERNATIONAL) internationalOverrides[index] ?: tracks[index] else tracks[index]
}

object DanCourseMetadata {
    const val version = "CiRCLE PLUS"

    val courses: List<DanCourse> = listOf(
        course(
            id = "first",
            name = "初段",
            nameEnglish = "First Dan",
            group = DanCourseGroup.NORMAL,
            life = life(350, 0, 2, 5, 20),
            tracks = listOf(
                track("＊ハロー、プラネット。", "dx", "basic"),
                track("シャルル", "dx", "basic"),
                track("ゆけむり魂温泉 II", "dx", "basic"),
                track("オトヒメモリー☆ウタゲーション", "std", "basic"),
            ),
            internationalOverrides = mapOf(
                0 to track("いーあるふぁんくらぶ", "dx", "basic"),
            ),
        ),
        course(
            "second", "二段", "Second Dan", DanCourseGroup.NORMAL, life(350, 0, 2, 5, 20),
            track("もういいよ", "dx", "advanced"),
            track("Witches night", "dx", "advanced"),
            track("Worlders", "dx", "advanced"),
            track("コンティニュー！ feat. 藍月なくる", "dx", "advanced"),
        ),
        course(
            "third", "三段", "Third Dan", DanCourseGroup.NORMAL, life(600, 1, 2, 5, 50),
            track("蒼空に舞え、墨染の桜", "std", "advanced"),
            track("死ぬな！", "dx", "advanced"),
            track("色は匂へど散りぬるを", "std", "advanced"),
            track("エイリアンエイリアン", "std", "advanced"),
        ),
        course(
            "fourth", "四段", "Fourth Dan", DanCourseGroup.NORMAL, life(700, 2, 2, 5, 50),
            track("独りんぼエンヴィー", "dx", "expert"),
            track("シアワセうさぎ", "std", "expert"),
            track("HOT LIMIT", "dx", "expert"),
            track("空回りライブラリ", "dx", "expert"),
        ),
        course(
            "fifth", "五段", "Fifth Dan", DanCourseGroup.NORMAL, life(700, 2, 2, 5, 50),
            track("フリィダム ロリィタ", "std", "expert"),
            track("でらっくmaimai♪てんてこまい!", "dx", "expert"),
            track("まっすぐ→→→ストリーム！", "dx", "expert"),
            track("蒼空に舞え、墨染の桜", "std", "expert"),
        ),
        course(
            "sixth", "六段", "Sixth Dan", DanCourseGroup.NORMAL, life(700, 2, 2, 5, 50),
            track("ULTRA SYNERGY MATRIX", "dx", "expert"),
            track("終点", "std", "expert"),
            track("超最終鬼畜妹フランドール・S", "dx", "expert"),
            track("約束", "dx", "master"),
        ),
        course(
            "seventh", "七段", "Seventh Dan", DanCourseGroup.NORMAL, life(700, 2, 2, 5, 50),
            track("再会", "dx", "master"),
            track("アマツキツネ", "dx", "master"),
            track("エイリアンエイリアン", "std", "master"),
            track("チルノのパーフェクトさんすう教室", "dx", "master"),
        ),
        course(
            id = "eighth",
            name = "八段",
            nameEnglish = "Eighth Dan",
            group = DanCourseGroup.NORMAL,
            life = life(700, 2, 2, 5, 20),
            tracks = listOf(
                track("アンビバレンス", "dx", "master"),
                track("ミラクルペイント", "std", "master"),
                track("パラドクスイヴ", "dx", "master"),
                track("進め！イッスン軍団 -Rebellion of the Dwarfs-", "std", "master"),
            ),
            internationalOverrides = mapOf(
                1 to track("Catch Me If You Can", "dx", "master"),
            ),
        ),
        course(
            "ninth", "九段", "Ninth Dan", DanCourseGroup.NORMAL, life(800, 2, 2, 5, 30),
            track("ウルトラトレーラー", "dx", "master"),
            track("snooze", "dx", "master"),
            track("Little \"Sister\" Bitch", "dx", "master"),
            track("Comet Panto Men!", "dx", "master"),
        ),
        course(
            "tenth", "十段", "Tenth Dan", DanCourseGroup.NORMAL, life(900, 2, 2, 5, 30),
            track("頓珍漢の宴", "std", "master"),
            track("LiftOff", "dx", "master"),
            track("Yakumo >>JOINT STRUGGLE (2019 Update)", "dx", "master"),
            track("Caliburne ～Story of the Legendary sword～", "std", "master"),
        ),
        course(
            "true-first", "真初段", "True First Dan", DanCourseGroup.TRUE, life(50, 2, 3, 5, 10),
            track("STARTLINER", "dx", "master"),
            track("乙女解剖", "dx", "master"),
            track("華鳥風月", "std", "master"),
            track("Streak", "std", "expert"),
        ),
        course(
            "true-second", "真二段", "True Second Dan", DanCourseGroup.TRUE, life(50, 2, 3, 5, 10),
            track("さくゆいたいそう", "dx", "master"),
            track("にっこり^^調査隊のテーマ", "dx", "master"),
            track("脳漿炸裂ガール", "std", "expert"),
            track("宿題が終わらないっ！", "std", "master"),
        ),
        course(
            "true-third", "真三段", "True Third Dan", DanCourseGroup.TRUE, life(50, 2, 3, 5, 10),
            track("ネガティブ進化論", "dx", "master"),
            track("うぇいびー", "dx", "master"),
            track("System “Z”", "std", "expert"),
            track("橙の幻想郷音頭", "std", "master"),
        ),
        course(
            id = "true-fourth",
            name = "真四段",
            nameEnglish = "True Fourth Dan",
            group = DanCourseGroup.TRUE,
            life = life(50, 2, 3, 5, 10),
            tracks = listOf(
                track("フィクサー", "std", "master"),
                track("Bad Apple!! feat.nomico (Tetsuya Komuro Remix)", "dx", "master"),
                track("怒槌", "std", "expert"),
                track("Garden Of The Dragon", "std", "master"),
            ),
            internationalOverrides = mapOf(
                1 to track("紅星ミゼラブル～廃憶編", "dx", "master"),
            ),
        ),
        course(
            "true-fifth", "真五段", "True Fifth Dan", DanCourseGroup.TRUE, life(50, 2, 3, 5, 10),
            track("Splash Dance!!", "dx", "master"),
            track("ULTRA POWER", "dx", "master"),
            track("ありふれたせかいせいふく", "std", "master"),
            track("HANIPAGANDA", "dx", "master"),
        ),
        course(
            "true-sixth", "真六段", "True Sixth Dan", DanCourseGroup.TRUE, life(50, 2, 3, 5, 10),
            track("The Cursed Doll", "dx", "master"),
            track("M@GICAL☆CURE! LOVE ♥ SHOT!", "dx", "master"),
            track("レーイレーイ", "dx", "remaster"),
            track("疾走あんさんぶる", "std", "master"),
        ),
        course(
            "true-seventh", "真七段", "True Seventh Dan", DanCourseGroup.TRUE, life(50, 2, 3, 5, 10),
            track("Never Give Up!", "dx", "remaster"),
            track("六兆年と一夜物語", "std", "master"),
            track("星見草", "dx", "master"),
            track("Armageddon", "dx", "master"),
        ),
        course(
            "true-eighth", "真八段", "True Eighth Dan", DanCourseGroup.TRUE, life(50, 2, 3, 5, 10),
            track("Magical Paradox", "dx", "master"),
            track("Sakura Fubuki", "std", "master"),
            track("Sun Dance", "std", "remaster"),
            track("砂の函", "dx", "master"),
        ),
        course(
            "true-ninth", "真九段", "True Ninth Dan", DanCourseGroup.TRUE, life(50, 2, 3, 5, 10),
            track("NAGAREBOSHI☆ROCKET", "dx", "master"),
            track("Xevel", "std", "master"),
            track("有明/Ariake", "dx", "master"),
            track("FFT", "std", "remaster"),
        ),
        course(
            "true-tenth", "真十段", "True Tenth Dan", DanCourseGroup.TRUE, life(50, 2, 3, 5, 10),
            track("AMABIE", "dx", "master"),
            track("ガラテアの螺旋", "std", "remaster"),
            track("Apollo", "dx", "master"),
            track("Divide et impera!", "dx", "master"),
        ),
        course(
            "true-kaiden", "真皆伝", "True Kaiden", DanCourseGroup.TRUE, life(50, 2, 3, 5, 5),
            track("Customized Justice", "dx", "master"),
            track("the EmpErroR", "std", "master"),
            track("躯樹の墓守", "dx", "master"),
            track("PANDORA PARADOXXX", "std", "master"),
        ),
        course(
            "ura-kaiden", "裏皆伝", "Ura Kaiden", DanCourseGroup.URA, life(10, 1, 3, 10, 0),
            track("PANDORA PARADOXXX", "std", "remaster"),
            track("World's end BLACKBOX", "dx", "master"),
            track("raputa", "dx", "master"),
            track("系ぎて", "dx", "remaster"),
        ),
    )

    fun sourceUrl(region: AccountRegion): String =
        "https://arcade-songs.zetaraku.dev/maimai/gallery/?id=" +
            if (region == AccountRegion.JAPAN) "circle-plus-dan" else "circle-plus-dan-intl"

    fun chartKey(track: DanTrack): String = listOf(
        normalizeSearch(track.title),
        track.type,
        track.difficulty,
    ).joinToString("\u001f")

    fun chartLookup(charts: List<SongChart>): Map<String, SongChart> = charts.associateBy { chart ->
        chartKey(DanTrack(chart.title, chart.type, chart.difficulty))
    }

    private fun life(maximum: Int, great: Int, good: Int, miss: Int, bonus: Int) =
        DanLifeRule(maximum, great, good, miss, bonus)

    private fun track(title: String, type: String, difficulty: String) = DanTrack(title, type, difficulty)

    private fun course(
        id: String,
        name: String,
        nameEnglish: String,
        group: DanCourseGroup,
        life: DanLifeRule,
        vararg tracks: DanTrack,
    ) = DanCourse(id, name, nameEnglish, group, life, tracks.toList())

    private fun course(
        id: String,
        name: String,
        nameEnglish: String,
        group: DanCourseGroup,
        life: DanLifeRule,
        tracks: List<DanTrack>,
        internationalOverrides: Map<Int, DanTrack>,
    ) = DanCourse(id, name, nameEnglish, group, life, tracks, internationalOverrides)
}
