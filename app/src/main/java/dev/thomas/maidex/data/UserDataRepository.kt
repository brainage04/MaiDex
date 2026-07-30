package dev.thomas.maidex.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import org.json.JSONObject

class UserDataRepository(context: Context) {
    private val helper = UserDatabase(context)
    private val preferences = context.getSharedPreferences("player-profile", Context.MODE_PRIVATE)

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
        return PlayerProfile(
            name = preferences.getString("name", "Player").orEmpty(),
            officialRating = preferences.getInt("rating", 0),
            region = enumValueOr(regionName, AccountRegion.INTERNATIONAL),
            importedAt = preferences.getLong("imported_at", 0L),
        )
    }

    fun saveImport(result: ImportResult) {
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
        }
        preferences.edit()
            .putString("name", result.profile.name)
            .putInt("rating", result.profile.officialRating)
            .putString("region", result.profile.region.name)
            .putLong("imported_at", result.profile.importedAt)
            .apply()
    }

    fun clear() {
        helper.writableDatabase.transaction {
            delete("scores", null, null)
            delete("play_details", null, null)
        }
        preferences.edit().clear().apply()
    }
}

private class UserDatabase(context: Context) : SQLiteOpenHelper(context, "user-data.db", null, 1) {
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
    }

    override fun onUpgrade(database: SQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
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
