package org.wvt.horizonmgr.ui.donate

import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.wvt.horizonmgr.DependenciesContainer
import kotlin.math.log

class DonateViewModel(
    dependencies: DependenciesContainer
) : ViewModel() {
    private val webApi = dependencies.webapi

    data class DonateItem(
        val name: String,
        val size: TextUnit
    )

    private val _donates = MutableStateFlow(emptySet<DonateItem>())
    val donates: StateFlow<Set<DonateItem>> = _donates

    fun refresh() {
        viewModelScope.launch {
            val result = mutableSetOf<DonateItem>()
            webApi.getDonates().forEach {
                result.add(
                    DonateItem(
                        name = it.name,
                        size = it.money.toFloatOrNull()?.let {
                            TextUnit.Sp(log(it + 1f, 1.5f) * 2)
                        } ?: 2.sp
                    )
                )
            }
            _donates.value = result
        }
    }
}