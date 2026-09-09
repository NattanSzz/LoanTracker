package com.loantracker.app.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

enum class TipoJuros { SIMPLES, COMPOSTO }

enum class FrequenciaParcela { DIARIA, SEMANAL, QUINZENAL, MENSAL }

enum class SituacaoParcela { PENDENTE, PAGA, PARCIALMENTE_PAGA, VENCIDA }

@Entity(tableName = "clientes")
data class Cliente(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nome: String,
    val telefone: String? = null,
    val cpf: String? = null,
    val endereco: String? = null,
    val observacao: String? = null,
    val criadoEm: LocalDate = LocalDate.now()
)

@Entity(
    tableName = "emprestimos",
    foreignKeys = [
        ForeignKey(
            entity = Cliente::class,
            parentColumns = ["id"],
            childColumns = ["clienteId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("clienteId")]
)
data class Emprestimo(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val clienteId: Long,
    val valorEmprestadoCents: Cents,
    val taxaJurosPercentual: Double, // ex: 10.0 representa 10%
    val tipoJuros: TipoJuros,
    val quantidadeParcelas: Int,
    val frequencia: FrequenciaParcela,
    val dataEmprestimo: LocalDate,
    val primeiroVencimento: LocalDate,
    // valores calculados no momento do cadastro, mantidos fixos mesmo que
    // empréstimos futuros usem taxas diferentes
    val totalJurosCents: Cents,
    val totalAReceberCents: Cents,
    val valorParcelaCents: Cents
)

@Entity(
    tableName = "parcelas",
    foreignKeys = [
        ForeignKey(
            entity = Emprestimo::class,
            parentColumns = ["id"],
            childColumns = ["emprestimoId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("emprestimoId")]
)
data class Parcela(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val emprestimoId: Long,
    val numero: Int,
    val vencimento: LocalDate,
    val valorCents: Cents,
    val valorPagoCents: Cents = 0
)

@Entity(
    tableName = "pagamentos",
    foreignKeys = [
        ForeignKey(
            entity = Parcela::class,
            parentColumns = ["id"],
            childColumns = ["parcelaId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("parcelaId")]
)
data class Pagamento(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val parcelaId: Long,
    val valorPagoCents: Cents,
    val dataPagamento: LocalDate
)
