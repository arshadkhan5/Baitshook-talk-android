/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2025 Julius Linus <juliuslinus1@email.com>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.gcc.talk.gccData.database.migrations

import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.gcc.talk.gccData.source.local.Migrations
import com.gcc.talk.gccData.source.local.GccTalkDatabase
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class MigrationsTest {
    companion object {
        private const val TEST_DB = "migration-test"
        private const val INIT_VERSION = 10 // last version before update to offline first
        private val TAG = MigrationsTest::class.java.simpleName
    }

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        GccTalkDatabase::class.java
    )

    @Test
    @Throws(IOException::class)
    @Suppress("SpreadOperator")
    fun migrateAll() {
        helper.createDatabase(TEST_DB, INIT_VERSION).apply {
            close()
        }

        Room.databaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext,
            GccTalkDatabase::class.java,
            TEST_DB
        ).addMigrations(*GccTalkDatabase.MIGRATIONS).build().apply {
            openHelper.writableDatabase.close()
        }
    }

    @Test
    @Throws(IOException::class)
    fun migrate10To11() {
        helper.createDatabase(TEST_DB, 10).apply {
            close()
        }
        helper.runMigrationsAndValidate(TEST_DB, 11, true, Migrations.MIGRATION_10_11)
    }

    @Test
    @Throws(IOException::class)
    fun migrate11To12() {
        helper.createDatabase(TEST_DB, 11).apply {
            close()
        }
        helper.runMigrationsAndValidate(TEST_DB, 12, true, Migrations.MIGRATION_11_12)
    }

    @Test
    @Throws(IOException::class)
    fun migrate12To13() {
        helper.createDatabase(TEST_DB, 12).apply {
            close()
        }
        helper.runMigrationsAndValidate(TEST_DB, 13, true, Migrations.MIGRATION_12_13)
    }

    @Test
    @Throws(IOException::class)
    fun migrate13To14() {
        helper.createDatabase(TEST_DB, 13).apply {
            close()
        }
        helper.runMigrationsAndValidate(TEST_DB, 14, true, Migrations.MIGRATION_13_14)
    }

    @Test
    @Throws(IOException::class)
    fun migrate14To15() {
        helper.createDatabase(TEST_DB, 14).apply {
            close()
        }
        helper.runMigrationsAndValidate(TEST_DB, 15, true, Migrations.MIGRATION_14_15)
    }

    @Test
    @Throws(IOException::class)
    fun migrate15To16() {
        helper.createDatabase(TEST_DB, 15).apply {
            close()
        }
        helper.runMigrationsAndValidate(TEST_DB, 16, true, Migrations.MIGRATION_15_16)
    }

    @Test
    @Throws(IOException::class)
    fun migrate17To19() {
        helper.createDatabase(TEST_DB, 17).apply {
            close()
        }
        helper.runMigrationsAndValidate(TEST_DB, 19, true, Migrations.MIGRATION_17_19)
    }
}
