package com.loantracker.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.loantracker.app.LoanTrackerApp
import com.loantracker.app.data.LoanRepository

/**
 * Factory genérica que injeta o [LoanRepository] da Application em qualquer
 * ViewModel do aplicativo, sem precisar de uma biblioteca de injeção de
 * dependência (desnecessária para um app pessoal deste tamanho).
 */
class RepositoryViewModelFactory(
    private val repository: LoanRepository,
    private val factory: (LoanRepository) -> ViewModel
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return factory(repository) as T
    }
}

@Composable
fun <T : ViewModel> rememberViewModel(factory: (LoanRepository) -> T): T {
    val context = LocalContext.current
    val repository = (context.applicationContext as LoanTrackerApp).repository
    return viewModel(factory = RepositoryViewModelFactory(repository, factory))
}
