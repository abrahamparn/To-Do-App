package com.dicoding.todoapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.dicoding.todoapp.R
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

//TODO 3 : Define room database class and prepopulate database using JSON
@Database(entities = [Task::class], version = 1)
abstract class TaskDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao

    companion object {

        @Volatile
        private var INSTANCE: TaskDatabase? = null

        fun getInstance(context: Context): TaskDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TaskDatabase::class.java,
                    "task.db"
                )
                    .allowMainThreadQueries()
                    .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)

                        //https://www.dicoding.com/academies/14/tutorials/159
                        //https://www.dicoding.com/academies/352/tutorials/22502

                        val executor: ExecutorService = Executors.newCachedThreadPool()

                        //THIS IS THE THING I CHOOSE newCachedThreadPool instead of newSingleThreadExecutor
                        executor.execute {
                            fillWithStartingData(context, getInstance(context).taskDao())
                        }
                    }
                }).build()
                INSTANCE = instance
                instance
            }
        }

        private fun fillWithStartingData(context: Context, dao: TaskDao) {
            val task = loadJsonArray(context)
            try {
                if (task != null) {
                    for (i in 0 until task.length()) {
                        val item = task.getJSONObject(i)
                        dao.insertAll(
                            Task(
                                item.getInt("id"),
                                item.getString("title"),
                                item.getString("description"),
                                item.getLong("dueDate"),
                                item.getBoolean("completed")
                            )
                        )
                    }
                }
            } catch (exception: JSONException) {
                exception.printStackTrace()
            }
        }

        private fun loadJsonArray(context: Context): JSONArray? {
            val builder = StringBuilder()
            val `in` = context.resources.openRawResource(R.raw.task)
            val reader = BufferedReader(InputStreamReader(`in`))
            var line: String?
            try {
                while (reader.readLine().also { line = it } != null) {
                    builder.append(line)
                }
                val json = JSONObject(builder.toString())
                return json.getJSONArray("tasks")
            } catch (exception: IOException) {
                exception.printStackTrace()
            } catch (exception: JSONException) {
                exception.printStackTrace()
            }
            return null
        }

    }
}
