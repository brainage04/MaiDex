package io.github.brainage04.maidex.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.time.Instant
import java.time.ZoneId

class UserDataRepository(context: Context) {
    private val helper = UserDatabase(context)
    private val preferences = context.getSharedPreferences("player-profile", Context.MODE_PRIVATE)
    private val filterPreferences =
        context.getSharedPreferences("filter-presets", Context.MODE_PRIVATE)
    private val profileAssetCache = ProfileAssetCache(File(context.filesDir, "profile-assets"))

    fun loadScores(): Map<String, UserScore> = helper.readableDatabase.rawQuery(
        """SELECT chart_key, achievement, grade, combo_medal, sync_medal,
            dx_score, max_dx_score, imported_at FROM scores""",
        null,
    ).use { cursor ->
        buildMap(cursor.count) {
            while (cursor.moveToNext()) {
                val score = UserScore(
                    chartKey = cursor.getString(0),
                    achievement = cursor.getDouble(1),
                    grade = enumValueOr(cursor.getString(2), Grade.D),
                    comboMedal = enumValueOr(cursor.getString(3), ComboMedal.NONE),
                    syncMedal = enumValueOr(cursor.getString(4), SyncMedal.NONE),
                    dxScore = cursor.getInt(5),
                    maxDxScore = cursor.getInt(6),
                    importedAt = cursor.getLong(7),
                )
                put(score.chartKey, score)
            }
        }
    }

    fun loadPlayDetails(): Map<String, PlayDetail> = helper.readableDatabase.rawQuery(
        "SELECT chart_key, played_at, achievement, fast, late, judgments FROM play_details",
        null,
    ).use { cursor ->
        buildMap(cursor.count) {
            while (cursor.moveToNext()) {
                val detail = PlayDetail(
                    chartKey = cursor.getString(0),
                    playedAt = cursor.getString(1),
                    achievement = cursor.getDouble(2),
                    fast = cursor.getInt(3),
                    late = cursor.getInt(4),
                    judgments = decodeJudgments(cursor.getString(5)),
                )
                put(detail.chartKey, detail)
            }
        }
    }

    fun loadProfile(): PlayerProfile? {
        val regionName = preferences.getString("region", null) ?: return null
        return DxNetAssets.decorate(PlayerProfile(
            name = preferences.getString("name", "Player").orEmpty(),
            officialRating = preferences.getInt("rating", 0),
            region = enumValueOr(regionName, AccountRegion.INTERNATIONAL),
            title = preferences.getString("title", "").orEmpty(),
            titleRarity = preferences.getString("title_rarity", "").orEmpty(),
            starCount = preferences.getInt("star_count", -1).takeIf { it >= 0 },
            avatarUrl = preferences.getString("avatar_url", "").orEmpty(),
            courseRankUrl = preferences.getString("course_rank_url", "").orEmpty(),
            titleBackgroundUrl = preferences.getString("title_background_url", "").orEmpty(),
            ratingBaseUrl = preferences.getString("rating_base_url", "").orEmpty(),
            starIconUrl = preferences.getString("star_icon_url", "").orEmpty(),
            classRankUrl = preferences.getString("class_rank_url", "").orEmpty(),
            completedChihoNames = preferences
                .getStringSet("completed_chiho_names", emptySet())
                .orEmpty()
                .toSet(),
            friendClass = preferences.getString("friend_class", "").orEmpty(),
            currentVersionPlayCount = preferences.getInt("current_version_play_count", -1).takeIf { it >= 0 },
            totalPlayCount = preferences.getInt("total_play_count", -1).takeIf { it >= 0 },
            importedAt = preferences.getLong("imported_at", 0L),
        ))
    }

    fun loadCircleHistory(): List<CircleData> = helper.readableDatabase.rawQuery(
        "SELECT data_json FROM circle_months ORDER BY month DESC, captured_at DESC",
        null,
    ).use { cursor ->
        buildList(cursor.count) {
            while (cursor.moveToNext()) add(decodeCircle(cursor.getString(0)))
        }
    }

    fun loadCircleSnapshots(): List<CircleDailySnapshot> = helper.readableDatabase.rawQuery(
        "SELECT day, captured_at, data_json FROM circle_snapshots ORDER BY day ASC",
        null,
    ).use { cursor ->
        buildList(cursor.count) {
            while (cursor.moveToNext()) {
                add(
                    CircleDailySnapshot(
                        day = cursor.getString(0),
                        capturedAt = cursor.getLong(1),
                        circle = decodeCircle(cursor.getString(2)),
                    ),
                )
            }
        }
    }

    fun loadPlayCountSnapshots(): List<PlayCountSnapshot> = helper.readableDatabase.rawQuery(
        """SELECT day, captured_at, current_version_count, total_count
            FROM play_count_snapshots ORDER BY day ASC""",
        null,
    ).use { cursor ->
        buildList(cursor.count) {
            while (cursor.moveToNext()) {
                add(
                    PlayCountSnapshot(
                        day = cursor.getString(0),
                        capturedAt = cursor.getLong(1),
                        currentVersionPlayCount = cursor.getInt(2),
                        totalPlayCount = cursor.getInt(3),
                    ),
                )
            }
        }
    }


    fun loadFilterPresets(): List<FilterPreset> = runCatching {
        JSONArray(filterPreferences.getString("presets", "[]").orEmpty())
            .objects()
            .mapNotNull(::decodeFilterPreset)
    }.getOrDefault(emptyList())

    fun saveFilterPreset(preset: FilterPreset) {
        val updated = loadFilterPresets().filterNot { it.id == preset.id } + preset.copy(isBuiltIn = false)
        filterPreferences.edit()
            .putString("presets", JSONArray().apply {
                updated.forEach { put(encodeFilterPreset(it)) }
            }.toString())
            .apply()
    }

    fun deleteFilterPreset(id: String) {
        val updated = loadFilterPresets().filterNot { it.id == id }
        filterPreferences.edit()
            .putString("presets", JSONArray().apply {
                updated.forEach { put(encodeFilterPreset(it)) }
            }.toString())
            .apply()
    }
    fun loadTrackingSettings(): TrackingSettings = TrackingSettings(
        lastAttemptAt = preferences.getLong("tracking_last_attempt_at", 0L),
        lastSuccessAt = preferences.getLong("tracking_last_success_at", 0L),
        lastError = preferences.getString("tracking_last_error", "").orEmpty(),
    )

    fun saveImport(result: ImportResult): PlayerProfile {
        val profile = profileAssetCache.cache(mergeProgress(result.profile))
        helper.writableDatabase.transaction {
            delete("scores", null, null)
            result.scores.forEach { score ->
                insertWithOnConflict(
                    "scores",
                    null,
                    ContentValues().apply {
                        put("chart_key", score.chartKey)
                        put("achievement", score.achievement)
                        put("grade", score.grade.name)
                        put("combo_medal", score.comboMedal.name)
                        put("sync_medal", score.syncMedal.name)
                        put("dx_score", score.dxScore)
                        put("max_dx_score", score.maxDxScore)
                        put("imported_at", score.importedAt)
                    },
                    SQLiteDatabase.CONFLICT_REPLACE,
                )
            }
            result.playDetails.forEach { detail ->
                insertWithOnConflict(
                    "play_details",
                    null,
                    ContentValues().apply {
                        put("chart_key", detail.chartKey)
                        put("played_at", detail.playedAt)
                        put("achievement", detail.achievement)
                        put("fast", detail.fast)
                        put("late", detail.late)
                        put("judgments", encodeJudgments(detail.judgments))
                    },
                    SQLiteDatabase.CONFLICT_REPLACE,
                )
            }
            saveTrackingRows(profile, result.circle)
        }
        saveProfile(profile)
        recordTrackingSuccess()
        return profile
    }

    fun saveTrackingSnapshot(profile: PlayerProfile, circle: CircleData?): PlayerProfile {
        val cachedProfile = profileAssetCache.cache(mergeProgress(profile))
        helper.writableDatabase.transaction {
            saveTrackingRows(cachedProfile, circle)
        }
        saveProfile(cachedProfile)
        return cachedProfile
    }

    private fun mergeProgress(profile: PlayerProfile): PlayerProfile {
        val existing = loadProfile()
        return profile.copy(
            completedChihoNames = existing
                ?.completedChihoNames
                .orEmpty() + profile.completedChihoNames,
            friendClass = profile.friendClass.ifBlank { existing?.friendClass.orEmpty() },
        )
    }


    fun recordTrackingAttempt(at: Long = System.currentTimeMillis()) {
        preferences.edit().putLong("tracking_last_attempt_at", at).apply()
    }

    fun recordTrackingSuccess(at: Long = System.currentTimeMillis()) {
        preferences.edit()
            .putLong("tracking_last_success_at", at)
            .putString("tracking_last_error", "")
            .apply()
    }

    fun recordTrackingFailure(message: String) {
        preferences.edit().putString("tracking_last_error", message).apply()
    }

    private fun saveProfile(profile: PlayerProfile) {
        val editor = preferences.edit()
            .putString("name", profile.name)
            .putInt("rating", profile.officialRating)
            .putString("region", profile.region.name)
            .putString("title", profile.title)
            .putString("title_rarity", profile.titleRarity)
            .putString("avatar_url", profile.avatarUrl)
            .putString("course_rank_url", profile.courseRankUrl)
            .putString("class_rank_url", profile.classRankUrl)
            .putString("title_background_url", profile.titleBackgroundUrl)
            .putString("rating_base_url", profile.ratingBaseUrl)
            .putString("star_icon_url", profile.starIconUrl)
            .putStringSet("completed_chiho_names", profile.completedChihoNames)
            .putString("friend_class", profile.friendClass)
            .putLong("imported_at", profile.importedAt)
        profile.starCount?.let { editor.putInt("star_count", it) }
            ?: editor.remove("star_count")
        profile.currentVersionPlayCount?.let { editor.putInt("current_version_play_count", it) }
            ?: editor.remove("current_version_play_count")
        profile.totalPlayCount?.let { editor.putInt("total_play_count", it) }
            ?: editor.remove("total_play_count")
        editor.apply()
    }

    fun clear() {
        helper.writableDatabase.transaction {
            delete("scores", null, null)
            delete("play_details", null, null)
            delete("circle_months", null, null)
            delete("circle_snapshots", null, null)
            delete("play_count_snapshots", null, null)
        }
        preferences.edit().clear().apply()
        profileAssetCache.clear()
    }
}

private class UserDatabase(context: Context) : SQLiteOpenHelper(context, "user-data.db", null, 2) {
    override fun onCreate(database: SQLiteDatabase) {
        database.execSQL(
            """CREATE TABLE scores(
                chart_key TEXT PRIMARY KEY,
                achievement REAL NOT NULL,
                grade TEXT NOT NULL,
                combo_medal TEXT NOT NULL,
                sync_medal TEXT NOT NULL,
                dx_score INTEGER NOT NULL,
                max_dx_score INTEGER NOT NULL,
                imported_at INTEGER NOT NULL
            )""",
        )
        database.execSQL(
            """CREATE TABLE play_details(
                chart_key TEXT PRIMARY KEY,
                played_at TEXT NOT NULL,
                achievement REAL NOT NULL,
                fast INTEGER NOT NULL,
                late INTEGER NOT NULL,
                judgments TEXT NOT NULL
            )""",
        )
        createTrackingTables(database)
    }

    override fun onUpgrade(database: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) createTrackingTables(database)
    }
}

private fun createTrackingTables(database: SQLiteDatabase) {
    database.execSQL(
        """CREATE TABLE circle_months(
            month TEXT NOT NULL,
            circle_key TEXT NOT NULL,
            captured_at INTEGER NOT NULL,
            data_json TEXT NOT NULL,
            PRIMARY KEY(month, circle_key)
        )""",
    )
    database.execSQL(
        """CREATE TABLE circle_snapshots(
            day TEXT NOT NULL,
            circle_key TEXT NOT NULL,
            captured_at INTEGER NOT NULL,
            data_json TEXT NOT NULL,
            PRIMARY KEY(day, circle_key)
        )""",
    )
    database.execSQL(
        """CREATE TABLE play_count_snapshots(
            day TEXT PRIMARY KEY,
            captured_at INTEGER NOT NULL,
            current_version_count INTEGER NOT NULL,
            total_count INTEGER NOT NULL
        )""",
    )
}

private inline fun SQLiteDatabase.transaction(block: SQLiteDatabase.() -> Unit) {
    beginTransaction()
    try {
        block()
        setTransactionSuccessful()
    } finally {
        endTransaction()
    }
}

private fun SQLiteDatabase.saveTrackingRows(profile: PlayerProfile, circle: CircleData?) {
    circle?.let { value ->
        val json = encodeCircle(value)
        insertWithOnConflict(
            "circle_months",
            null,
            ContentValues().apply {
                put("month", value.month)
                put("circle_key", value.key)
                put("captured_at", value.importedAt)
                put("data_json", json)
            },
            SQLiteDatabase.CONFLICT_REPLACE,
        )
        insertWithOnConflict(
            "circle_snapshots",
            null,
            ContentValues().apply {
                put("day", dayOf(value.importedAt))
                put("circle_key", value.key)
                put("captured_at", value.importedAt)
                put("data_json", json)
            },
            SQLiteDatabase.CONFLICT_REPLACE,
        )
    }
    val currentVersionCount = profile.currentVersionPlayCount
    val totalCount = profile.totalPlayCount
    if (currentVersionCount != null && totalCount != null) {
        insertWithOnConflict(
            "play_count_snapshots",
            null,
            ContentValues().apply {
                put("day", dayOf(profile.importedAt))
                put("captured_at", profile.importedAt)
                put("current_version_count", currentVersionCount)
                put("total_count", totalCount)
            },
            SQLiteDatabase.CONFLICT_REPLACE,
        )
    }
}

private fun dayOf(timestamp: Long): String =
    Instant.ofEpochMilli(timestamp)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
        .toString()

private fun encodeCircle(circle: CircleData): String = JSONObject().apply {
    put("month", circle.month)
    put("name", circle.name)
    put("code", circle.code)
    put("leader", circle.leader)
    put("comment", circle.comment)
    put("tags", JSONArray(circle.tags))
    put("circleClass", circle.circleClass)
    put("totalPoints", circle.totalPoints)
    put("regionalRank", circle.regionalRank ?: JSONObject.NULL)
    put("rankingLabel", circle.rankingLabel)
    put("memberCount", circle.memberCount ?: JSONObject.NULL)
    put("daysUntilReset", circle.daysUntilReset ?: JSONObject.NULL)
    put("nextRewardPoints", circle.nextRewardPoints ?: JSONObject.NULL)
    put("updatedAt", circle.updatedAt)
    put("characterUrl", circle.characterUrl)
    put("backgroundUrl", circle.backgroundUrl)
    put("profileImageUrl", circle.profileImageUrl)
    put("members", JSONArray().apply {
        circle.members.forEach { member ->
            put(JSONObject().apply {
                put("key", member.key)
                put("name", member.name)
                put("points", member.points)
                put("role", member.role)
                put("avatarUrl", member.avatarUrl)
                put("isCurrentUser", member.isCurrentUser)
            })
        }
    })
    put("rewards", JSONArray().apply {
        circle.rewards.forEach { reward ->
            put(JSONObject().apply {
                put("pointsRequired", reward.pointsRequired)
                put("name", reward.name)
                put("imageUrl", reward.imageUrl)
                put("earned", reward.earned)
            })
        }
    })
    put("information", JSONArray().apply {
        circle.information.forEach { item ->
            put(JSONObject().apply {
                put("label", item.label)
                put("value", item.value)
            })
        }
    })
    put("pages", JSONArray().apply {
        circle.pages.forEach { page ->
            put(JSONObject().apply {
                put("title", page.title)
                put("text", page.text)
                put("type", page.type.name)
                put("items", JSONArray().apply {
                    page.items.forEach { item ->
                        put(JSONObject().apply {
                            put("label", item.label)
                            put("value", item.value)
                            put("imageUrl", item.imageUrl)
                        })
                    }
                })
                put("imageUrls", JSONArray(page.imageUrls))
            })
        }
    })
    put("importedAt", circle.importedAt)
}.toString()

private fun decodeCircle(value: String): CircleData {
    val root = JSONObject(value)
    return CircleData(
        month = root.optString("month"),
        name = root.optString("name"),
        code = root.optString("code"),
        leader = root.optString("leader"),
        comment = root.optString("comment"),
        tags = root.optJSONArray("tags").strings(),
        circleClass = root.optString("circleClass"),
        totalPoints = root.optInt("totalPoints"),
        regionalRank = root.optionalInt("regionalRank"),
        rankingLabel = root.optString("rankingLabel"),
        memberCount = root.optionalInt("memberCount"),
        daysUntilReset = root.optionalInt("daysUntilReset"),
        nextRewardPoints = root.optionalInt("nextRewardPoints"),
        updatedAt = root.optString("updatedAt"),
        characterUrl = root.optString("characterUrl"),
        backgroundUrl = root.optString("backgroundUrl"),
        profileImageUrl = root.optString("profileImageUrl"),
        members = root.optJSONArray("members").objects().map { member ->
            CircleMember(
                key = member.optString("key"),
                name = member.optString("name"),
                points = member.optInt("points"),
                role = member.optString("role"),
                avatarUrl = member.optString("avatarUrl"),
                isCurrentUser = member.optBoolean("isCurrentUser"),
            )
        },
        rewards = root.optJSONArray("rewards").objects().map { reward ->
            CircleReward(
                pointsRequired = reward.optInt("pointsRequired"),
                name = reward.optString("name"),
                imageUrl = reward.optString("imageUrl"),
                earned = reward.optBoolean("earned"),
            )
        },
        information = root.optJSONArray("information").objects().map { item ->
            CircleInfoItem(
                label = item.optString("label"),
                value = item.optString("value"),
            )
        },
        pages = root.optJSONArray("pages").objects().map { page ->
            val title = page.optString("title")
            CirclePageInfo(
                title = title,
                text = page.optString("text"),
                type = runCatching {
                    CirclePageType.valueOf(page.optString("type"))
                }.getOrElse {
                    circlePageTypeFromLegacyTitle(title)
                },
                items = page.optJSONArray("items").objects().map { item ->
                    CirclePageItem(

                        label = item.optString("label"),
                        value = item.optString("value"),
                        imageUrl = item.optString("imageUrl"),
                    )
                },
                imageUrls = page.optJSONArray("imageUrls").strings(),
            )
        },
        importedAt = root.optLong("importedAt"),
    )
}
private fun encodeFilterPreset(preset: FilterPreset): JSONObject = JSONObject().apply {
    put("id", preset.id)
    put("name", preset.name)
    put("sort", preset.sort.name)
    put("sortOrder", preset.sortOrder.name)
    put("filters", JSONObject().apply {
        val filters = preset.filters
        put("artist", filters.artist)
        put("noteDesigner", filters.noteDesigner)
        put("categories", JSONArray(filters.categories.toList()))
        put("difficulties", JSONArray(filters.difficulties.toList()))
        put("versions", JSONArray(filters.versions.toList()))
        put("types", JSONArray(filters.types.toList()))
        put("showUtage", filters.showUtage)
        put("regions", JSONArray(filters.regions.toList()))
        filters.minLevel?.let { put("minLevel", it) }
        filters.maxLevel?.let { put("maxLevel", it) }
        filters.minBpm?.let { put("minBpm", it) }
        filters.maxBpm?.let { put("maxBpm", it) }
        put("constantAvailability", filters.constantAvailability.name)
        put("grades", JSONArray(filters.grades.map(Grade::name)))
        put("comboMedals", JSONArray(filters.comboMedals.map(ComboMedal::name)))
        put("syncMedals", JSONArray(filters.syncMedals.map(SyncMedal::name)))
        put("scoredOnly", filters.scoredOnly)
    })
}

private fun decodeFilterPreset(root: JSONObject): FilterPreset? {
    val id = root.optString("id").takeIf(String::isNotBlank) ?: return null
    val name = root.optString("name").takeIf(String::isNotBlank) ?: return null
    val filters = root.optJSONObject("filters") ?: return null
    return FilterPreset(
        id = id,
        name = name,
        filters = ChartFilters(
            artist = filters.optString("artist"),
            noteDesigner = filters.optString("noteDesigner"),
            categories = filters.optJSONArray("categories").strings().toSet(),
            difficulties = filters.optJSONArray("difficulties").strings().toSet(),
            versions = filters.optJSONArray("versions").strings().toSet(),
            types = filters.optJSONArray("types").strings().toSet(),
            showUtage = filters.optBoolean("showUtage", true),
            regions = filters.optJSONArray("regions").strings().toSet(),
            minLevel = filters.optionalDouble("minLevel"),
            maxLevel = filters.optionalDouble("maxLevel"),
            minBpm = filters.optionalInt("minBpm"),
            maxBpm = filters.optionalInt("maxBpm"),
            constantAvailability = enumValueOr(
                filters.optString("constantAvailability"),
                ConstantAvailability.BOTH,
            ),
            grades = filters.optJSONArray("grades").enumSet<Grade>(),
            comboMedals = filters.optJSONArray("comboMedals").enumSet<ComboMedal>(),
            syncMedals = filters.optJSONArray("syncMedals").enumSet<SyncMedal>(),
            scoredOnly = filters.optBoolean("scoredOnly"),
        ),
        sort = enumValueOr(root.optString("sort"), ChartSort.LEVEL),
        sortOrder = enumValueOr(root.optString("sortOrder"), SortOrder.DESCENDING),
    )
}

private fun JSONObject.optionalDouble(key: String): Double? =
    if (has(key) && !isNull(key)) optDouble(key) else null

private inline fun <reified T : Enum<T>> JSONArray?.enumSet(): Set<T> =
    strings().mapNotNull { value ->
        runCatching { enumValueOf<T>(value) }.getOrNull()
    }.toSet()

private fun circlePageTypeFromLegacyTitle(title: String): CirclePageType {
    val key = title.replace(Regex("""[^a-z]""", RegexOption.IGNORE_CASE), "").lowercase()
    return when {
        "circlechallenge" in key && "ranking" in key -> CirclePageType.CHALLENGE_RANKING
        "rankingrules" in key -> CirclePageType.RANKING_RULE
        "pointreward" in key -> CirclePageType.POINT_REWARD
        "pastcirclefesta" in key -> CirclePageType.FESTA_HISTORY
        "circlefestaranking" in key -> CirclePageType.FESTA_RANKING
        "festa" in key -> CirclePageType.FESTA
        "searchcircle" in key -> CirclePageType.SEARCH
        "recruitingcircles" in key -> CirclePageType.SEARCH_RESULTS
        "inviteaccept" in key || "invitation" in key -> CirclePageType.INVITE_ACCEPT
        "leavecircle" in key -> CirclePageType.LEAVE
        "profile" in key -> CirclePageType.PROFILE
        "member" in key -> CirclePageType.MEMBER
        "ranking" in key -> CirclePageType.RANKING
        else -> CirclePageType.OTHER
    }
}

private fun JSONObject.optionalInt(key: String): Int? =
    if (has(key) && !isNull(key)) optInt(key) else null

private fun JSONArray?.strings(): List<String> =
    if (this == null) emptyList() else (0 until length()).mapNotNull { index ->
        optString(index).takeIf(String::isNotBlank)
    }

private fun JSONArray?.objects(): List<JSONObject> =
    if (this == null) emptyList() else (0 until length()).mapNotNull(::optJSONObject)

private inline fun <reified T : Enum<T>> enumValueOr(value: String, fallback: T): T =
    runCatching { enumValueOf<T>(value) }.getOrDefault(fallback)

private fun encodeJudgments(table: JudgmentTable): String = JSONObject().apply {
    put("tap", table.tap.toJson())
    put("hold", table.hold.toJson())
    put("slide", table.slide.toJson())
    put("touch", table.touch.toJson())
    put("break", table.breakNotes.toJson())
}.toString()

private fun JudgeCounts.toJson(): JSONObject = JSONObject().apply {
    put("cp", criticalPerfect)
    put("p", perfect)
    put("great", great)
    put("good", good)
    put("miss", miss)
}

private fun decodeJudgments(value: String): JudgmentTable {
    val root = JSONObject(value)
    return JudgmentTable(
        tap = root.optJSONObject("tap").toJudgeCounts(),
        hold = root.optJSONObject("hold").toJudgeCounts(),
        slide = root.optJSONObject("slide").toJudgeCounts(),
        touch = root.optJSONObject("touch").toJudgeCounts(),
        breakNotes = root.optJSONObject("break").toJudgeCounts(),
    )
}

private fun JSONObject?.toJudgeCounts(): JudgeCounts = JudgeCounts(
    criticalPerfect = this?.optInt("cp") ?: 0,
    perfect = this?.optInt("p") ?: 0,
    great = this?.optInt("great") ?: 0,
    good = this?.optInt("good") ?: 0,
    miss = this?.optInt("miss") ?: 0,
)
