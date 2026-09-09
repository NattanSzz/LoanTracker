package com.loantracker.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.loantracker.app.LoanTrackerApp
import com.loantracker.app.data.LoanRepository

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
inline fun <reified T : ViewModel> rememberViewModel(
    noinline factory: (LoanRepository) -> T
): T {
    val context = LocalContext.current
    val repository =
        (context.applicationContext as LoanTrackerApp).repository

    return viewModel(
        modelClass = T::class.java,
        factory = RepositoryViewModelFactory(repository, factory)
    )
}
