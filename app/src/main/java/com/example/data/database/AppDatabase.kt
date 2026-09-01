package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.database.dao.*
import com.example.data.database.entity.*
import com.example.data.model.UserRole
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserEntity::class,
        GoldRateEntity::class,
        PriceHistoryEntity::class,
        ProcessingBatchEntity::class,
        CapitalTransactionEntity::class,
        GoldIntakeEntity::class,
        GoldInventoryEntity::class,
        GoldSalesEntity::class,
        ExpenseEntity::class,
        ActivityLogEntity::class,
        AuditLogEntity::class,
        BusinessSettingEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun goldRateDao(): GoldRateDao
    abstract fun processingBatchDao(): ProcessingBatchDao
    abstract fun capitalTransactionDao(): CapitalTransactionDao
    abstract fun goldIntakeDao(): GoldIntakeDao
    abstract fun goldInventoryDao(): GoldInventoryDao
    abstract fun goldSalesDao(): GoldSalesDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun activityLogDao(): ActivityLogDao
    abstract fun auditLogDao(): AuditLogDao
    abstract fun businessSettingDao(): BusinessSettingDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope = CoroutineScope(Dispatchers.IO)): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "catatan_modal_emas_database"
                )
                    .addCallback(AppDatabaseCallback(scope))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class AppDatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database)
                    }
                }
            }
        }

        suspend fun populateInitialData(database: AppDatabase) {
            val userDao = database.userDao()
            if (userDao.getUserCount() == 0) {
                userDao.insertUser(
                    UserEntity(
                        username = "ferrynani",
                        fullName = "Ferry Nani (Pemilik & Pengelola)",
                        email = "ferry.nani@gmail.com",
                        phoneWhatsapp = "08124180909",
                        password = "833273",
                        role = UserRole.OWNER,
                        securityType = "PIN",
                        securityPinOrPattern = "833273"
                    )
                )
                userDao.insertUser(
                    UserEntity(
                        username = "owner",
                        fullName = "H. Sudirman (Pemilik)",
                        email = "owner@emasjaya.com",
                        phoneWhatsapp = "0812-3456-7890",
                        password = "123",
                        role = UserRole.OWNER
                    )
                )
                userDao.insertUser(
                    UserEntity(
                        username = "admin",
                        fullName = "Ahmad Rifai (Admin)",
                        email = "admin@emasjaya.com",
                        phoneWhatsapp = "0813-8899-1122",
                        password = "123",
                        role = UserRole.ADMIN
                    )
                )
                userDao.insertUser(
                    UserEntity(
                        username = "operator",
                        fullName = "Budi Santoso (Operator)",
                        email = "operator@emasjaya.com",
                        phoneWhatsapp = "0815-4433-2211",
                        password = "123",
                        role = UserRole.OPERATOR
                    )
                )
            }

            val goldRateDao = database.goldRateDao()
            val initialRates = listOf(
                GoldRateEntity(kadarName = "24K", buyPricePerGram = 1500000.0, sellPricePerGram = 1550000.0, defaultDeductionPercent = 0.0),
                GoldRateEntity(kadarName = "23K", buyPricePerGram = 1420000.0, sellPricePerGram = 1470000.0, defaultDeductionPercent = 1.0),
                GoldRateEntity(kadarName = "22K", buyPricePerGram = 1350000.0, sellPricePerGram = 1400000.0, defaultDeductionPercent = 2.0),
                GoldRateEntity(kadarName = "21K", buyPricePerGram = 1280000.0, sellPricePerGram = 1330000.0, defaultDeductionPercent = 2.0),
                GoldRateEntity(kadarName = "20K", buyPricePerGram = 1200000.0, sellPricePerGram = 1250000.0, defaultDeductionPercent = 3.0),
                GoldRateEntity(kadarName = "19K", buyPricePerGram = 1130000.0, sellPricePerGram = 1180000.0, defaultDeductionPercent = 3.0),
                GoldRateEntity(kadarName = "18K", buyPricePerGram = 1050000.0, sellPricePerGram = 1100000.0, defaultDeductionPercent = 4.0),
                GoldRateEntity(kadarName = "17K", buyPricePerGram = 980000.0, sellPricePerGram = 1030000.0, defaultDeductionPercent = 4.0),
                GoldRateEntity(kadarName = "16K", buyPricePerGram = 900000.0, sellPricePerGram = 950000.0, defaultDeductionPercent = 5.0),
                GoldRateEntity(kadarName = "14K", buyPricePerGram = 780000.0, sellPricePerGram = 830000.0, defaultDeductionPercent = 5.0),
                GoldRateEntity(kadarName = "12K", buyPricePerGram = 650000.0, sellPricePerGram = 700000.0, defaultDeductionPercent = 6.0),
                GoldRateEntity(kadarName = "10K", buyPricePerGram = 520000.0, sellPricePerGram = 570000.0, defaultDeductionPercent = 7.0)
            )
            for (rate in initialRates) {
                goldRateDao.insertRate(rate)
            }

            val settingDao = database.businessSettingDao()
            settingDao.insertSetting(BusinessSettingEntity("BUSINESS_NAME", "Catatan Modal & Pengolahan Emas"))
            settingDao.insertSetting(BusinessSettingEntity("BUSINESS_ADDRESS", "Jl. Tambang Emas No. 88, Pinogaluman"))
            settingDao.insertSetting(BusinessSettingEntity("BUSINESS_PHONE", "0812-3456-7890"))
            settingDao.insertSetting(BusinessSettingEntity("DEFAULT_DEDUCTION_METHOD", "PERCENT"))
        }
    }
}
