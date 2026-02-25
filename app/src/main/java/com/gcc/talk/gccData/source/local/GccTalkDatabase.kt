/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2023-2025 Marcel Hibbe <dev@mhibbe.de>
 * SPDX-FileCopyrightText: 2022 Andy Scherzinger <info@andy-scherzinger.de>
 * SPDX-FileCopyrightText: 2017-2020 Mario Danic <mario@lovelyhq.com>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.gcc.talk.gccData.source.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.gcc.talk.R
import com.gcc.talk.gccData.database.dao.GccChatBlocksDao
import com.gcc.talk.gccData.database.dao.GccChatMessagesDao
import com.gcc.talk.gccData.database.dao.GccConversationsDao
import com.gcc.talk.gccData.database.model.GccChatBlockEntity
import com.gcc.talk.gccData.database.model.GccChatMessageEntity
import com.gcc.talk.gccData.database.model.GccConversationEntity
import com.gcc.talk.gccData.source.local.converters.GccArrayListConverter
import com.gcc.talk.gccData.source.local.converters.GccCapabilitiesConverter
import com.gcc.talk.gccData.source.local.converters.GccExternalSignalingServerConverter
import com.gcc.talk.gccData.source.local.converters.GccHashMapHashMapConverter
import com.gcc.talk.gccData.source.local.converters.GccLinkedHashMapConverter
import com.gcc.talk.gccData.source.local.converters.GccPushConfigurationConverter
import com.gcc.talk.gccData.source.local.converters.GccSendStatusConverter
import com.gcc.talk.gccData.source.local.converters.GccServerVersionConverter
import com.gcc.talk.gccData.source.local.converters.GccSignalingSettingsConverter
import com.gcc.talk.gccData.storage.GccArbitraryStoragesDao
import com.gcc.talk.gccData.storage.model.GccArbitraryStorageEntity
import com.gcc.talk.gccData.user.GccUsersDao
import com.gcc.talk.gccData.user.model.GccUserEntity
import com.gcc.talk.gccModels.MessageDraftConverter
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import java.util.Locale

@Database(
    entities = [
        GccUserEntity::class,
        GccArbitraryStorageEntity::class,
        GccConversationEntity::class,
        GccChatMessageEntity::class,
        GccChatBlockEntity::class
    ],
    version = 23,
 /*   autoMigrations = [
        AutoMigration(from = 9, to = 10),
        AutoMigration(from = 16, to = 17, spec = AutoMigration16To17::class),
        AutoMigration(from = 19, to = 20),
        AutoMigration(from = 20, to = 21),
        AutoMigration(from = 21, to = 22),
        AutoMigration(from = 22, to = 23)
    ],*/
    exportSchema = true
)
@TypeConverters(
    GccPushConfigurationConverter::class,
    GccCapabilitiesConverter::class,
    GccServerVersionConverter::class,
    GccExternalSignalingServerConverter::class,
    GccSignalingSettingsConverter::class,
    GccHashMapHashMapConverter::class,
    GccLinkedHashMapConverter::class,
    GccArrayListConverter::class,
    GccSendStatusConverter::class,
    MessageDraftConverter::class
)
@Suppress("MagicNumber")
abstract class GccTalkDatabase : RoomDatabase() {
    abstract fun usersDao(): GccUsersDao
    abstract fun conversationsDao(): GccConversationsDao
    abstract fun chatMessagesDao(): GccChatMessagesDao
    abstract fun chatBlocksDao(): GccChatBlocksDao
    abstract fun arbitraryStoragesDao(): GccArbitraryStoragesDao

    companion object {
        const val TAG = "GccTalkDatabase"
        const val SQL_CIPHER_LIBRARY = "sqlcipher"

        @Volatile
        private var instance: GccTalkDatabase? = null

        @JvmStatic
        fun getInstance(context: Context): GccTalkDatabase =
            instance ?: synchronized(this) {
                instance ?: build(context).also { instance = it }
            }

        // If editing the migrations, please add a test case in MigrationsTest under androidTest/data
        val MIGRATIONS = arrayOf(
            Migrations.MIGRATION_6_8,
            Migrations.MIGRATION_7_8,
            Migrations.MIGRATION_8_9,
            Migrations.MIGRATION_10_11,
            Migrations.MIGRATION_11_12,
            Migrations.MIGRATION_12_13,
            Migrations.MIGRATION_13_14,
            Migrations.MIGRATION_14_15,
            Migrations.MIGRATION_15_16,
            Migrations.MIGRATION_17_19
        )

        @Suppress("SpreadOperator")
        private fun build(context: Context): GccTalkDatabase {
            val passCharArray = context.getString(R.string.nc_talk_database_encryption_key).toCharArray()
            val passphrase: ByteArray = getBytesFromChars(passCharArray)
            val factory = SupportOpenHelperFactory(passphrase)

            val dbName = context
                .resources
                .getString(R.string.nc_app_product_name)
                .lowercase(Locale.getDefault())
                .replace(" ", "_")
                .trim() +
                ".sqlite"

            System.loadLibrary(SQL_CIPHER_LIBRARY)

            return Room
                .databaseBuilder(context.applicationContext, GccTalkDatabase::class.java, dbName)
                // comment out openHelperFactory to view the database entries in Android Studio for debugging
                .openHelperFactory(factory)
                .fallbackToDestructiveMigrationFrom(true,18)
                .addMigrations(*MIGRATIONS) // * converts migrations to vararg
                .allowMainThreadQueries()
                .addCallback(
                    object : Callback() {
                        override fun onOpen(db: SupportSQLiteDatabase) {
                            super.onOpen(db)
                            db.execSQL("PRAGMA defer_foreign_keys = 1")
                        }
                    }
                )
                .build()
        }

        private fun getBytesFromChars(chars: CharArray): ByteArray = String(chars).toByteArray(Charsets.UTF_8)
    }
}
