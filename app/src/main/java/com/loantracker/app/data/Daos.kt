package com.loantracker.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ClienteDao {
    @Insert
    suspend fun inserir(cliente: Cliente): Long

    @Update
    suspend fun atualizar(cliente: Cliente)

    @Delete
    suspend fun excluir(cliente: Cliente)

    @Query("SELECT * FROM clientes ORDER BY nome ASC")
    fun observarTodos(): Flow<List<Cliente>>

    @Query("SELECT * FROM clientes WHERE id = :id")
    fun observarPorId(id: Long): Flow<Cliente?>

    @Query("SELECT * FROM clientes WHERE id = :id")
    suspend fun buscarPorId(id: Long): Cliente?
}

@Dao
interface EmprestimoDao {
    @Insert
    suspend fun inserir(emprestimo: Emprestimo): Long

    @Query("SELECT * FROM emprestimos WHERE clienteId = :clienteId ORDER BY dataEmprestimo DESC")
    fun observarPorCliente(clienteId: Long): Flow<List<Emprestimo>>

    @Query("SELECT * FROM emprestimos ORDER BY dataEmprestimo DESC")
    fun observarTodos(): Flow<List<Emprestimo>>

    @Query("SELECT * FROM emprestimos WHERE id = :id")
    fun observarPorId(id: Long): Flow<Emprestimo?>

    @Query("SELECT * FROM emprestimos WHERE id = :id")
    suspend fun buscarPorId(id: Long): Emprestimo?
}

@Dao
interface ParcelaDao {
    @Insert
    suspend fun inserirTodas(parcelas: List<Parcela>)

    @Update
    suspend fun atualizar(parcela: Parcela)

    @Query("SELECT * FROM parcelas WHERE emprestimoId = :emprestimoId ORDER BY numero ASC")
    fun observarPorEmprestimo(emprestimoId: Long): Flow<List<Parcela>>

    @Query(
        """
        SELECT parcelas.* FROM parcelas
        INNER JOIN emprestimos ON parcelas.emprestimoId = emprestimos.id
        WHERE emprestimos.clienteId = :clienteId AND parcelas.valorPagoCents < parcelas.valorCents
        ORDER BY parcelas.vencimento ASC
        """
    )
    fun observarPendentesPorCliente(clienteId: Long): Flow<List<Parcela>>

    @Query("SELECT * FROM parcelas")
    fun observarTodas(): Flow<List<Parcela>>

    @Query("SELECT * FROM parcelas WHERE id = :id")
    suspend fun buscarPorId(id: Long): Parcela?
}

@Dao
interface PagamentoDao {
    @Insert
    suspend fun inserir(pagamento: Pagamento): Long

    @Query("SELECT * FROM pagamentos WHERE parcelaId = :parcelaId ORDER BY dataPagamento DESC")
    fun observarPorParcela(parcelaId: Long): Flow<List<Pagamento>>

    @Query("SELECT * FROM pagamentos ORDER BY dataPagamento DESC")
    fun observarTodos(): Flow<List<Pagamento>>
}
