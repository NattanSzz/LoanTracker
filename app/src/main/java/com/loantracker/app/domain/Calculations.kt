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
 * o resumo de conferência para o usuário e para gerar as parcelas reais.
 */
data class ResumoEmprestimo(
    val valorEmprestadoCents: Cents,
    val totalJurosCents: Cents,
    val totalAReceberCents: Cents,
    val valorParcelaCents: Cents,
    val valoresParcelas: List<Cents>,
    val datasParcelas: List<LocalDate>
)

object InterestCalculator {

    /**
     * Juros simples (fórmula do usuário — juros incide uma única vez sobre o
     * principal, depois divide igualmente pelas parcelas, subtraindo o
     * desconto por parcela de cada uma):
     *   valorParcela = ((principal + principal*taxa) / quantidadeParcelas) - desconto
     * Ex.: R$ 1.000,00 a 20% em 4 parcelas, sem desconto → (1000 + 200) / 4 = R$ 300,00.
     *
     * Aluguel: o cliente só paga os juros periodicamente, sem prazo fixo pra
     * quitar o principal — cada "parcela" é o próprio valor do juros do
     * período, menos o desconto. Não existe quantidade de parcelas fixa;
     * aqui é gerada só a primeira data de vencimento.
     *
     * Juros composto: mantido apenas por compatibilidade com empréstimos e
     * backups já existentes — não é mais oferecido no formulário de cadastro.
     */
    fun calcular(
        valorEmprestadoCents: Cents,
        taxaPercentual: Double,
        tipo: TipoJuros,
        quantidadeParcelas: Int,
        descontoPorParcelaCents: Cents,
        dataEmprestimo: LocalDate,
        primeiroVencimento: LocalDate,
        frequencia: FrequenciaParcela
    ): ResumoEmprestimo {
        if (tipo == TipoJuros.ALUGUEL) {
            val jurosPeriodo = valorEmprestadoCents.applyPercentage(taxaPercentual)
            val valorParcela = (jurosPeriodo - descontoPorParcelaCents).coerceAtLeast(0)
            return ResumoEmprestimo(
                valorEmprestadoCents = valorEmprestadoCents,
                totalJurosCents = valorParcela,
                totalAReceberCents = valorEmprestadoCents + valorParcela,
                valorParcelaCents = valorParcela,
                valoresParcelas = listOf(valorParcela),
                datasParcelas = listOf(primeiroVencimento)
            )
        }

        val totalSemDesconto: Cents = when (tipo) {
            TipoJuros.SIMPLES -> valorEmprestadoCents + valorEmprestadoCents.applyPercentage(taxaPercentual)
            else -> { // COMPOSTO — legado
                var valor = BigDecimal(valorEmprestadoCents)
                val rate = BigDecimal.ONE.add(BigDecimal.valueOf(taxaPercentual).movePointLeft(2))
                repeat(quantidadeParcelas) { valor = valor.multiply(rate) }
                valor.setScale(0, RoundingMode.HALF_UP).toLong()
            }
        }

        val valoresBase = distribuirIgualmente(totalSemDesconto, quantidadeParcelas)
        val valoresComDesconto = valoresBase.map { (it - descontoPorParcelaCents).coerceAtLeast(0) }
        val totalAReceberCents = valoresComDesconto.sum()
        val totalJurosCents = totalAReceberCents - valorEmprestadoCents
        val datas = InstallmentGenerator.gerarDatas(primeiroVencimento, quantidadeParcelas, frequencia)

        return ResumoEmprestimo(
            valorEmprestadoCents = valorEmprestadoCents,
            totalJurosCents = totalJurosCents,
            totalAReceberCents = totalAReceberCents,
            valorParcelaCents = valoresComDesconto.first(),
            valoresParcelas = valoresComDesconto,
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
        return (0 until quantidade).map { index -> avancar(primeiroVencimento, frequencia, index.toLong()) }
    }

    /** Próxima data de vencimento a partir de uma data base, conforme a frequência. */
    fun proximaData(dataBase: LocalDate, frequencia: FrequenciaParcela): LocalDate =
        avancar(dataBase, frequencia, 1L)

    private fun avancar(data: LocalDate, frequencia: FrequenciaParcela, periodos: Long): LocalDate {
        return when (frequencia) {
            FrequenciaParcela.DIARIA -> data.plusDays(periodos)
            FrequenciaParcela.SEMANAL -> data.plusWeeks(periodos)
            FrequenciaParcela.QUINZENAL -> data.plusDays(periodos * 15)
            FrequenciaParcela.MENSAL -> data.plusMonths(periodos)
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
