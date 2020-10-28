package org.wvt.horizonmgr.ui.joingroup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.wvt.horizonmgr.DependenciesContainer
import org.wvt.horizonmgr.service.WebAPI

class JoinGroupViewModel(
    private val dependencies: DependenciesContainer
) : ViewModel() {
    private val _groups = MutableStateFlow<List<WebAPI.QQGroupEntry>>(emptyList())
    val groups: StateFlow<List<WebAPI.QQGroupEntry>> = _groups

    init {
        viewModelScope.launch {
            _groups.value = dependencies.webapi.getQQGroupList()
        }
    }
}