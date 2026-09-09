package com.loantracker.app.data

import androidx.room.withTransaction
import com.loantracker.app.domain.InterestCalculator
import com.loantracker.app.domain.diasEmAtraso
import com.loantracker.app.domain.restanteCents
import com.loantracker.app.domain.situacao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate

/** Dados de um cliente já combinados com o que aparece na tela inicial. */
data class ClienteResumo(
    val cliente: Cliente,
    val totalEmAbertoCents: Cents,
    val proximoVencimento: LocalDate?,
    val proximoVencimentoValorCents: Cents,
    val parcelasVencidas: Int,
    val situacao: SituacaoCliente
)

enum class SituacaoCliente { EM_DIA, VENCE_HOJE, VENCE_AMANHA, EM_ATRASO }

data class EmprestimoResumo(
    val emprestimo: Emprestimo,
    val totalPagoCents: Cents,
    val totalRestanteCents: Cents,
    val quitado: Boolean,
    val parcelasEmAtraso: Int
)

data class InfoGeral(
    val totalEmprestadoAnoCents: Cents,
    val totalRecebidoAnoCents: Cents,
    val jurosRecebidosAnoCents: Cents,
    val totalEmAbertoCents: Cents,
    val parcelasVencidasQtd: Int,
    val parcelasVencidasValorCents: Cents,
    val venceHojeQtd: Int,
    val venceHojeValorCents: Cents,
    val venceAmanhaQtd: Int,
    val venceAmanhaValorCents: Cents,
    val proximos7DiasQtd: Int,
    val proximos7DiasValorCents: Cents,
    val clientesEmAtraso: List<ClienteEmAtraso>
)

data class ClienteEmAtraso(
    val cliente: Cliente,
    val parcelasVencidasQtd: Int,
    val valorVencidoCents: Cents
)

/** Uma parcela pendente já enriquecida com dados do cliente, usada para montar notificações. */
data class ParcelaParaNotificar(
    val parcelaId: Long,
    val clienteId: Long,
    val clienteNome: String,
    val emprestimoId: Long,
    val valorRestanteCents: Cents,
    val vencimento: LocalDate,
    val diasAtraso: Long
)

data class DadosNotificacao(
    val vencemAmanha: List<ParcelaParaNotificar>,
    val vencemHoje: List<ParcelaParaNotificar>,
    val vencidas: List<ParcelaParaNotificar>
)

class LoanRepository(
    private val db: AppDatabase,
    private val clienteDao: ClienteDao,
    private val emprestimoDao: EmprestimoDao,
    private val parcelaDao: ParcelaDao,
    private val pagamentoDao: PagamentoDao
) {
    fun observarClientes(): Flow<List<Cliente>> = clienteDao.observarTodos()

    suspend fun cadastrarCliente(cliente: Cliente): Long = clienteDao.inserir(cliente)

    suspend fun buscarCliente(id: Long): Cliente? = clienteDao.buscarPorId(id)

    fun observarCliente(id: Long): Flow<Cliente?> = clienteDao.observarPorId(id)

    fun observarEmprestimosDoCliente(clienteId: Long): Flow<List<Emprestimo>> =
        emprestimoDao.observarPorCliente(clienteId)

    fun observarEmprestimo(id: Long): Flow<Emprestimo?> = emprestimoDao.observarPorId(id)

    fun observarParcelasDoEmprestimo(emprestimoId: Long): Flow<List<Parcela>> =
        parcelaDao.observarPorEmprestimo(emprestimoId)

    fun observarParcelasPendentesDoCliente(clienteId: Long): Flow<List<Parcela>> =
        parcelaDao.observarPendentesPorCliente(clienteId)

    /** Cadastra um novo empréstimo já calculando e criando todas as parcelas. */
    suspend fun cadastrarEmprestimo(
        clienteId: Long,
        valorEmprestadoCents: Cents,
        taxaPercentual: Double,
        tipo: TipoJuros,
        quantidadeParcelas: Int,
        frequencia: FrequenciaParcela,
        dataEmprestimo: LocalDate,
        primeiroVencimento: LocalDate
    ): Long {
        val resumo = InterestCalculator.calcular(
            valorEmprestadoCents = valorEmprestadoCents,
            taxaPercentual = taxaPercentual,
            tipo = tipo,
            quantidadeParcelas = quantidadeParcelas,
            dataEmprestimo = dataEmprestimo,
            primeiroVencimento = primeiroVencimento,
            frequencia = frequencia
        )
        val valoresParcelas = InterestCalculator.distribuirIgualmente(resumo.totalAReceberCents, quantidadeParcelas)

        val emprestimo = Emprestimo(
            clienteId = clienteId,
            valorEmprestadoCents = valorEmprestadoCents,
            taxaJurosPercentual = taxaPercentual,
            tipoJuros = tipo,
            quantidadeParcelas = quantidadeParcelas,
            frequencia = frequencia,
            dataEmprestimo = dataEmprestimo,
            primeiroVencimento = primeiroVencimento,
            totalJurosCents = resumo.totalJurosCents,
            totalAReceberCents = resumo.totalAReceberCents,
            valorParcelaCents = resumo.valorParcelaCents
        )
        val emprestimoId = emprestimoDao.inserir(emprestimo)

        val parcelas = resumo.datasParcelas.mapIndexed { index, data ->
            Parcela(
                emprestimoId = emprestimoId,
                numero = index + 1,
                vencimento = data,
                valorCents = valoresParcelas[index]
            )
        }
        parcelaDao.inserirTodas(parcelas)
        return emprestimoId
    }

    /** Registra um pagamento (total ou parcial) em uma parcela. */
    suspend fun registrarPagamento(parcelaId: Long, valorPagoCents: Cents, data: LocalDate) {
        val parcela = parcelaDao.buscarPorId(parcelaId) ?: return
        pagamentoDao.inserir(Pagamento(parcelaId = parcelaId, valorPagoCents = valorPagoCents, dataPagamento = data))
        val novoValorPago = (parcela.valorPagoCents + valorPagoCents).coerceAtMost(parcela.valorCents)
        parcelaDao.atualizar(parcela.copy(valorPagoCents = novoValorPago))
    }

    fun observarResumosClientes(): Flow<List<ClienteResumo>> =
        combine(clienteDao.observarTodos(), parcelaDao.observarTodas(), emprestimoDao.observarTodos()) { clientes, parcelas, emprestimos ->
            val hoje = LocalDate.now()
            clientes.map { cliente ->
                montarResumoCliente(cliente, emprestimos, parcelas, hoje)
            }
        }

    private fun montarResumoCliente(
        cliente: Cliente,
        todosEmprestimos: List<Emprestimo>,
        todasParcelas: List<Parcela>,
        hoje: LocalDate
    ): ClienteResumo {
        val emprestimosIds = todosEmprestimos.filter { it.clienteId == cliente.id }.map { it.id }.toSet()
        val parcelasDoCliente = todasParcelas.filter { it.emprestimoId in emprestimosIds }

        val totalEmAberto = parcelasDoCliente.sumOf { it.restanteCents() }
        val pendentes = parcelasDoCliente.filter { it.restanteCents() > 0 }
        val proxima = pendentes.minByOrNull { it.vencimento }
        val vencidas = pendentes.count { it.situacao(hoje) == SituacaoParcela.VENCIDA }

        val situacao = when {
            vencidas > 0 -> SituacaoCliente.EM_ATRASO
            proxima?.vencimento == hoje -> SituacaoCliente.VENCE_HOJE
            proxima?.vencimento == hoje.plusDays(1) -> SituacaoCliente.VENCE_AMANHA
            else -> SituacaoCliente.EM_DIA
        }

        return ClienteResumo(
            cliente = cliente,
            totalEmAbertoCents = totalEmAberto,
            proximoVencimento = proxima?.vencimento,
            proximoVencimentoValorCents = proxima?.restanteCents() ?: 0,
            parcelasVencidas = vencidas,
            situacao = situacao
        )
    }

    fun observarResumosEmprestimos(clienteId: Long): Flow<List<EmprestimoResumo>> =
        combine(
            emprestimoDao.observarPorCliente(clienteId),
            parcelaDao.observarTodas()
        ) { emprestimos, todasParcelas ->
            val hoje = LocalDate.now()
            emprestimos.map { emprestimo ->
                val parcelas = todasParcelas.filter { it.emprestimoId == emprestimo.id }
                val totalPago = parcelas.sumOf { it.valorPagoCents }
                val totalRestante = parcelas.sumOf { it.restanteCents() }
                EmprestimoResumo(
                    emprestimo = emprestimo,
                    totalPagoCents = totalPago,
                    totalRestanteCents = totalRestante,
                    quitado = totalRestante <= 0,
                    parcelasEmAtraso = parcelas.count { it.situacao(hoje) == SituacaoParcela.VENCIDA }
                )
            }
        }

    fun observarInfoGeral(): Flow<InfoGeral> =
        combine(
            clienteDao.observarTodos(),
            emprestimoDao.observarTodos(),
            parcelaDao.observarTodas()
        ) { clientes, emprestimos, parcelas ->
            val hoje = LocalDate.now()
            val anoAtual = hoje.year

            val emprestimosDoAno = emprestimos.filter { it.dataEmprestimo.year == anoAtual }
            val totalEmprestadoAno = emprestimosDoAno.sumOf { it.valorEmprestadoCents }

            val totalRecebidoAno = parcelas.sumOf { parcela ->
                // aproximação: contabiliza o valor pago proporcional às parcelas cujo empréstimo é do ano atual
                if (emprestimos.firstOrNull { it.id == parcela.emprestimoId }?.dataEmprestimo?.year == anoAtual) {
                    parcela.valorPagoCents
                } else 0
            }

            val jurosRecebidosAno = emprestimosDoAno.sumOf { emprestimo ->
                val parcelasDoEmprestimo = parcelas.filter { it.emprestimoId == emprestimo.id }
                val totalPago = parcelasDoEmprestimo.sumOf { it.valorPagoCents }
                val proporcaoJuros = if (emprestimo.totalAReceberCents > 0) {
                    emprestimo.totalJurosCents.toDouble() / emprestimo.totalAReceberCents.toDouble()
                } else 0.0
                (totalPago * proporcaoJuros).toLong()
            }

            val totalEmAberto = parcelas.sumOf { it.restanteCents() }

            val pendentes = parcelas.filter { it.restanteCents() > 0 }
            val vencidas = pendentes.filter { it.situacao(hoje) == SituacaoParcela.VENCIDA }
            val venceHoje = pendentes.filter { it.vencimento == hoje }
            val venceAmanha = pendentes.filter { it.vencimento == hoje.plusDays(1) }
            val proximos7 = pendentes.filter { it.vencimento in hoje..hoje.plusDays(7) }

            val clientesEmAtraso = clientes.mapNotNull { cliente ->
                val emprestimosIds = emprestimos.filter { it.clienteId == cliente.id }.map { it.id }.toSet()
                val vencidasDoCliente = vencidas.filter { it.emprestimoId in emprestimosIds }
                if (vencidasDoCliente.isEmpty()) null
                else ClienteEmAtraso(
                    cliente = cliente,
                    parcelasVencidasQtd = vencidasDoCliente.size,
                    valorVencidoCents = vencidasDoCliente.sumOf { it.restanteCents() }
                )
            }

            InfoGeral(
                totalEmprestadoAnoCents = totalEmprestadoAno,
                totalRecebidoAnoCents = totalRecebidoAno,
                jurosRecebidosAnoCents = jurosRecebidosAno,
                totalEmAbertoCents = totalEmAberto,
                parcelasVencidasQtd = vencidas.size,
                parcelasVencidasValorCents = vencidas.sumOf { it.restanteCents() },
                venceHojeQtd = venceHoje.size,
                venceHojeValorCents = venceHoje.sumOf { it.restanteCents() },
                venceAmanhaQtd = venceAmanha.size,
                venceAmanhaValorCents = venceAmanha.sumOf { it.restanteCents() },
                proximos7DiasQtd = proximos7.size,
                proximos7DiasValorCents = proximos7.sumOf { it.restanteCents() },
                clientesEmAtraso = clientesEmAtraso
            )
        }

    /**
     * Busca (uma única vez, não reativo) os dados necessários para montar as
     * notificações diárias de vencimento — usado pelo worker em segundo plano.
     */
    suspend fun buscarDadosParaNotificacao(): DadosNotificacao {
        val hoje = LocalDate.now()
        val clientes = clienteDao.observarTodos().first()
        val emprestimos = emprestimoDao.observarTodos().first()
        val parcelas = parcelaDao.observarTodas().first()

        val clientesPorId = clientes.associateBy { it.id }
        val clienteIdPorEmprestimo = emprestimos.associateBy({ it.id }, { it.clienteId })

        val pendentes = parcelas.mapNotNull { parcela ->
            val restante = parcela.restanteCents()
            if (restante <= 0) return@mapNotNull null
            val clienteId = clienteIdPorEmprestimo[parcela.emprestimoId] ?: return@mapNotNull null
            val clienteNome = clientesPorId[clienteId]?.nome ?: return@mapNotNull null
            ParcelaParaNotificar(
                parcelaId = parcela.id,
                clienteId = clienteId,
                clienteNome = clienteNome,
                emprestimoId = parcela.emprestimoId,
                valorRestanteCents = restante,
                vencimento = parcela.vencimento,
                diasAtraso = parcela.diasEmAtraso(hoje)
            )
        }

        return DadosNotificacao(
            vencemAmanha = pendentes.filter { it.vencimento == hoje.plusDays(1) },
            vencemHoje = pendentes.filter { it.vencimento == hoje },
            vencidas = pendentes.filter { it.vencimento.isBefore(hoje) }
        )
    }

    /** Gera um snapshot em JSON de todos os dados locais, para backup manual. */
    suspend fun exportarParaJson(): String {
        val clientes = clienteDao.observarTodos().first()
        val emprestimos = emprestimoDao.observarTodos().first()
        val parcelas = parcelaDao.observarTodas().first()
        val pagamentos = pagamentoDao.observarTodos().first()

        val raiz = JSONObject()
        raiz.put("versao", 1)
        raiz.put("exportadoEm", LocalDate.now().toString())

        raiz.put("clientes", JSONArray().apply {
            clientes.forEach { c ->
                put(JSONObject().apply {
                    put("id", c.id)
                    put("nome", c.nome)
                    put("telefone", c.telefone ?: JSONObject.NULL)
                    put("cpf", c.cpf ?: JSONObject.NULL)
                    put("endereco", c.endereco ?: JSONObject.NULL)
                    put("observacao", c.observacao ?: JSONObject.NULL)
                    put("criadoEm", c.criadoEm.toEpochDay())
                })
            }
        })

        raiz.put("emprestimos", JSONArray().apply {
            emprestimos.forEach { e ->
                put(JSONObject().apply {
                    put("id", e.id)
                    put("clienteId", e.clienteId)
                    put("valorEmprestadoCents", e.valorEmprestadoCents)
                    put("taxaJurosPercentual", e.taxaJurosPercentual)
                    put("tipoJuros", e.tipoJuros.name)
                    put("quantidadeParcelas", e.quantidadeParcelas)
                    put("frequencia", e.frequencia.name)
                    put("dataEmprestimo", e.dataEmprestimo.toEpochDay())
                    put("primeiroVencimento", e.primeiroVencimento.toEpochDay())
                    put("totalJurosCents", e.totalJurosCents)
                    put("totalAReceberCents", e.totalAReceberCents)
                    put("valorParcelaCents", e.valorParcelaCents)
                })
            }
        })

        raiz.put("parcelas", JSONArray().apply {
            parcelas.forEach { p ->
                put(JSONObject().apply {
                    put("id", p.id)
                    put("emprestimoId", p.emprestimoId)
                    put("numero", p.numero)
                    put("vencimento", p.vencimento.toEpochDay())
                    put("valorCents", p.valorCents)
                    put("valorPagoCents", p.valorPagoCents)
                })
            }
        })

        raiz.put("pagamentos", JSONArray().apply {
            pagamentos.forEach { pg ->
                put(JSONObject().apply {
                    put("id", pg.id)
                    put("parcelaId", pg.parcelaId)
                    put("valorPagoCents", pg.valorPagoCents)
                    put("dataPagamento", pg.dataPagamento.toEpochDay())
                })
            }
        })

        return raiz.toString(2)
    }

    /**
     * Restaura um backup gerado por [exportarParaJson], substituindo todos os
     * dados locais atuais. Os IDs originais são preservados para manter os
     * relacionamentos entre clientes, empréstimos, parcelas e pagamentos.
     */
    suspend fun importarDeJson(json: String) {
        val raiz = JSONObject(json)

        val clientes = raiz.getJSONArray("clientes").toObjectList { o ->
            Cliente(
                id = o.getLong("id"),
                nome = o.getString("nome"),
                telefone = o.optNullableString("telefone"),
                cpf = o.optNullableString("cpf"),
                endereco = o.optNullableString("endereco"),
                observacao = o.optNullableString("observacao"),
                criadoEm = LocalDate.ofEpochDay(o.getLong("criadoEm"))
            )
        }

        val emprestimos = raiz.getJSONArray("emprestimos").toObjectList { o ->
            Emprestimo(
                id = o.getLong("id"),
                clienteId = o.getLong("clienteId"),
                valorEmprestadoCents = o.getLong("valorEmprestadoCents"),
                taxaJurosPercentual = o.getDouble("taxaJurosPercentual"),
                tipoJuros = TipoJuros.valueOf(o.getString("tipoJuros")),
                quantidadeParcelas = o.getInt("quantidadeParcelas"),
                frequencia = FrequenciaParcela.valueOf(o.getString("frequencia")),
                dataEmprestimo = LocalDate.ofEpochDay(o.getLong("dataEmprestimo")),
                primeiroVencimento = LocalDate.ofEpochDay(o.getLong("primeiroVencimento")),
                totalJurosCents = o.getLong("totalJurosCents"),
                totalAReceberCents = o.getLong("totalAReceberCents"),
                valorParcelaCents = o.getLong("valorParcelaCents")
            )
        }

        val parcelas = raiz.getJSONArray("parcelas").toObjectList { o ->
            Parcela(
                id = o.getLong("id"),
                emprestimoId = o.getLong("emprestimoId"),
                numero = o.getInt("numero"),
                vencimento = LocalDate.ofEpochDay(o.getLong("vencimento")),
                valorCents = o.getLong("valorCents"),
                valorPagoCents = o.getLong("valorPagoCents")
            )
        }

        val pagamentos = raiz.getJSONArray("pagamentos").toObjectList { o ->
            Pagamento(
                id = o.getLong("id"),
                parcelaId = o.getLong("parcelaId"),
                valorPagoCents = o.getLong("valorPagoCents"),
                dataPagamento = LocalDate.ofEpochDay(o.getLong("dataPagamento"))
            )
        }

        db.withTransaction {
            clienteDao.excluirTodos()
            clienteDao.inserirTodos(clientes)
            emprestimoDao.inserirTodos(emprestimos)
            parcelaDao.inserirTodas(parcelas)
            pagamentoDao.inserirTodos(pagamentos)
        }
    }
}

private inline fun <T> JSONArray.toObjectList(transform: (JSONObject) -> T): List<T> =
    (0 until length()).map { index -> transform(getJSONObject(index)) }

private fun JSONObject.optNullableString(key: String): String? =
    if (isNull(key)) null else getString(key)
