package com.loantracker.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [Cliente::class, Emprestimo::class, Parcela::class, Pagamento::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun clienteDao(): ClienteDao
    abstract fun emprestimoDao(): EmprestimoDao
    abstract fun parcelaDao(): ParcelaDao
    abstract fun pagamentoDao(): PagamentoDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * v1 -> v2: adiciona desconto por parcela, título automático do
         * empréstimo e a flag de quitação manual (usada pelos empréstimos do
         * tipo Aluguel). Os defaults preservam o comportamento anterior para
         * todo empréstimo já cadastrado.
         */
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE emprestimos ADD COLUMN descontoPorParcelaCents INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE emprestimos ADD COLUMN titulo TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE emprestimos ADD COLUMN quitadoManualmente INTEGER NOT NULL DEFAULT 0")
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "loan_tracker.db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
