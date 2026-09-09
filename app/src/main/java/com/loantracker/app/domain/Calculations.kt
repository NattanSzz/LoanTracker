package com.loantracker.app.domain

import com.loantracker.app.data.Cents
import com.loantracker.app.data.FrequenciaParcela
import com.loantracker.app.data.Parcela
import com.loantracker.app.data.SituacaoParcela
import com.loantracker.app.data.TipoJuros
import com.loantracker.app.data.applyPercentage
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Resultado do cálculo de um empréstimo antes de ser salvo, usado para exibir
 * o resumo de conferência para o usuário.
 */
data class ResumoEmprestimo(
    val valorEmprestadoCents: Cents,
    val totalJurosCents: Cents,
    val totalAReceberCents: Cents,
    val valorParcelaCents: Cents,
    val datasParcelas: List<LocalDate>
)

object InterestCalculator {

    /**
     * A taxa informada (ex: 10% "ao mês") é aplicada uma vez por parcela, já que
     * cada parcela representa um período do empréstimo.
     *
     * Juros simples:  totalJuros = principal * taxa * quantidadeParcelas
     * Juros composto:  totalAReceber = principal * (1 + taxa)^quantidadeParcelas
     */
    fun calcular(
        valorEmprestadoCents: Cents,
        taxaPercentual: Double,
        tipo: TipoJuros,
        quantidadeParcelas: Int,
        dataEmprestimo: LocalDate,
        primeiroVencimento: LocalDate,
        frequencia: FrequenciaParcela
    ): ResumoEmprestimo {
        val totalAReceberCents: Cents = when (tipo) {
            TipoJuros.SIMPLES -> {
                val juros = valorEmprestadoCents.applyPercentage(taxaPercentual) * quantidadeParcelas
                valorEmprestadoCents + juros
            }
            TipoJuros.COMPOSTO -> {
                var valor = BigDecimal(valorEmprestadoCents)
                val rate = BigDecimal.ONE.add(BigDecimal.valueOf(taxaPercentual).movePointLeft(2))
                repeat(quantidadeParcelas) { valor = valor.multiply(rate) }
                valor.setScale(0, RoundingMode.HALF_UP).toLong()
            }
        }
        val totalJurosCents = totalAReceberCents - valorEmprestadoCents
        val valorParcelaCents = distribuirIgualmente(totalAReceberCents, quantidadeParcelas).first()
        val datas = InstallmentGenerator.gerarDatas(primeiroVencimento, quantidadeParcelas, frequencia)

        return ResumoEmprestimo(
            valorEmprestadoCents = valorEmprestadoCents,
            totalJurosCents = totalJurosCents,
            totalAReceberCents = totalAReceberCents,
            valorParcelaCents = valorParcelaCents,
            datasParcelas = datas
        )
    }

    /**
     * Divide um valor total em N parcelas iguais, colocando a diferença de
     * arredondamento (poucos centavos) na última parcela, para que a soma
     * das parcelas bata exatamente com o total a receber.
     */
    fun distribuirIgualmente(totalCents: Cents, quantidade: Int): List<Cents> {
        if (quantidade <= 0) return emptyList()
        val base = totalCents / quantidade
        val resto = totalCents - (base * quantidade)
        return List(quantidade) { index ->
            if (index == quantidade - 1) base + resto else base
        }
    }
}

object InstallmentGenerator {
    fun gerarDatas(primeiroVencimento: LocalDate, quantidade: Int, frequencia: FrequenciaParcela): List<LocalDate> {
        return (0 until quantidade).map { index ->
            when (frequencia) {
                FrequenciaParcela.DIARIA -> primeiroVencimento.plusDays(index.toLong())
                FrequenciaParcela.SEMANAL -> primeiroVencimento.plusWeeks(index.toLong())
                FrequenciaParcela.QUINZENAL -> primeiroVencimento.plusDays(index.toLong() * 15)
                FrequenciaParcela.MENSAL -> primeiroVencimento.plusMonths(index.toLong())
            }
        }
    }
}

/** Calcula a situação atual de uma parcela com base no que já foi pago e na data de hoje. */
fun Parcela.situacao(hoje: LocalDate = LocalDate.now()): SituacaoParcela {
    return when {
        valorPagoCents >= valorCents -> SituacaoParcela.PAGA
        vencimento.isBefore(hoje) -> SituacaoParcela.VENCIDA
        valorPagoCents > 0 -> SituacaoParcela.PARCIALMENTE_PAGA
        else -> SituacaoParcela.PENDENTE
    }
}

fun Parcela.restanteCents(): Cents = (valorCents - valorPagoCents).coerceAtLeast(0)

fun Parcela.diasEmAtraso(hoje: LocalDate = LocalDate.now()): Long =
    if (situacao(hoje) == SituacaoParcela.VENCIDA) ChronoUnit.DAYS.between(vencimento, hoje) else 0
