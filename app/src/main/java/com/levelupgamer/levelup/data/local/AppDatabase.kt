package com.levelupgamer.levelup.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.levelupgamer.levelup.data.InitialData
import com.levelupgamer.levelup.data.local.dao.*
import com.levelupgamer.levelup.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

@Database(
    entities = [
        Event::class, 
        Review::class, 
        Reward::class, 
        User::class, 
        Product::class, 
        CartItem::class, 
        Order::class, 
        OrderItem::class, 
        UserAddress::class, 
        UserReward::class, 
        UserEvent::class
    ], 
    version = 4, // Aumentado de 3 a 4 por el cambio en Product
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun eventDao(): EventDao
    abstract fun reviewDao(): ReviewDao
    abstract fun rewardDao(): RewardDao
    abstract fun userDao(): UserDao
    abstract fun productDao(): ProductDao
    abstract fun cartDao(): CartDao
    abstract fun orderDao(): OrderDao
    abstract fun addressDao(): AddressDao
    abstract fun userRewardDao(): UserRewardDao
    abstract fun userEventDao(): UserEventDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "levelupgamer_database"
                )
                .fallbackToDestructiveMigration()
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        Executors.newSingleThreadExecutor().execute {
                            INSTANCE?.let { database ->
                                CoroutineScope(Dispatchers.IO).launch {
                                    database.productDao().insertAll(InitialData.getInitialProducts())
                                    database.rewardDao().insertAll(InitialData.getInitialRewards())
                                    database.eventDao().insertAll(InitialData.getInitialEvents())
                                    database.reviewDao().insertAll(InitialData.getInitialReviews())
                                }
                            }
                        }
                    }
                })
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
