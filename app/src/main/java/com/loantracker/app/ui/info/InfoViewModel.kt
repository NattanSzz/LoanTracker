package com.loantracker.app.ui.info

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.loantracker.app.data.InfoGeral
import com.loantracker.app.data.LoanRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class InfoViewModel(repository: LoanRepository) : ViewModel() {
    val info: StateFlow<InfoGeral?> =
        repository.observarInfoGeral().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
}
