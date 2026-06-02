package com.mssde.mobilelantern.data.local

import android.content.Context
import com.mssde.mobilelantern.ui.components.LanternType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LanternPreferenceManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs = context.getSharedPreferences("lantern_prefs", Context.MODE_PRIVATE)

    private val _selectedLanternType = MutableStateFlow(loadSavedType())
    val selectedLanternType: StateFlow<LanternType> = _selectedLanternType.asStateFlow()

    fun saveSelectedType(type: LanternType) {
        prefs.edit().putString(KEY_LANTERN_TYPE, type.name).apply()
        _selectedLanternType.value = type
    }

    private fun loadSavedType(): LanternType {
        val saved = prefs.getString(KEY_LANTERN_TYPE, null)
        return saved?.let { runCatching { LanternType.valueOf(it) }.getOrNull() } ?: LanternType.NONE
    }

    companion object {
        private const val KEY_LANTERN_TYPE = "selected_lantern_type"
    }
}
