package org.jetbrains.plugins.template.weatherApp.services

import com.intellij.openapi.components.Service
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.jetbrains.plugins.template.weatherApp.model.Location

@Service
class LocationsProvider : SearchAutoCompletionItemProvider<Location> {
    private val locationStateFlow = MutableStateFlow(
        listOf(
            Location("Munich", "Germany"),
            Location("Belgrade", "Serbia"),
            Location("Berlin", "Germany"),
            Location("Rome", "Italy"),
            Location("Paris", "France"),
            Location("Sydney", "Australia"),
            Location("Moscow", "Russia"),
            Location("Tokyo", "Japan"),
            Location("New York", "USA"),
        )
    )

    fun provideLocations(): StateFlow<List<Location>> = locationStateFlow.asStateFlow()

    override fun provideSearchableItems(searchTerm: String): List<Location> {
        return locationStateFlow
            .value
            .filter { it.matches(searchTerm) }
    }
}