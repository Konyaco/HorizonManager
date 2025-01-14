package org.wvt.horizonmgr.ui.downloadlist

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class DownloadListViewModel @Inject constructor() : ViewModel() {
    private val _downloadItems = MutableStateFlow(emptyList<String>())
    val downloadItems: StateFlow<List<String>> = _downloadItems
}