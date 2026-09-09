package com.loantracker.app.data

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.Locale

/**
 * Todo valor monetário no aplicativo é armazenado como Long representando CENTAVOS.
 * Isso evita os problemas de arredondamento de ponto flutuante ao trabalhar com dinheiro.
 */
typealias Cents = Long

private val brFormat: NumberFormat = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))

fun Cents.toBRL(): String = brFormat.format(BigDecimal(this).movePointLeft(2))

/** Texto simples "1234,56" (sem símbolo de moeda), útil para pré-preencher campos editáveis. */
fun Cents.toPlainDecimalString(): String =
    BigDecimal(this).movePointLeft(2).setScale(2, RoundingMode.HALF_UP).toPlainString().replace('.', ',')

/** Converte um texto digitado pelo usuário (ex: "1.000,50" ou "1000.50") para centavos. */
fun parseToCents(input: String): Cents {
    if (input.isBlank()) return 0L
    val cleaned = input
        .trim()
        .replace("R$", "")
        .replace(Regex("\\s"), "")
    // Se tiver vírgula, assumimos formato brasileiro (1.000,50)
    val normalized = if (cleaned.contains(",")) {
        cleaned.replace(".", "").replace(",", ".")
    } else {
        cleaned
    }
    val decimal = normalized.toBigDecimalOrNull() ?: BigDecimal.ZERO
    return decimal.movePointRight(2).setScale(0, RoundingMode.HALF_UP).toLong()
}

private fun String.toBigDecimalOrNull(): BigDecimal? = try {
    BigDecimal(this)
} catch (e: NumberFormatException) {
    null
}

/** Retorna o valor de um percentual (ex: 10.0 = 10%) sobre um valor em centavos, arredondado. */
fun Cents.applyPercentage(percentage: Double): Cents {
    val value = BigDecimal(this)
    val rate = BigDecimal.valueOf(percentage).movePointLeft(2)
    return value.multiply(rate).setScale(0, RoundingMode.HALF_UP).toLong()
}
