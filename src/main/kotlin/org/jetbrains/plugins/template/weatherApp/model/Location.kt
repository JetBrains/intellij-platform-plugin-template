package org.jetbrains.plugins.template.weatherApp.model

import androidx.compose.runtime.Immutable

/**
 * Represents a location with a name and associated country.
 *
 * @property name The name of the location.
 * @property country The associated country of the location.
 * @property label A textual representation of the location.
 */
@Immutable
data class Location(val name: String, val country: String) : PreviewableItem, Searchable {

    override val label: String
        get() {
            if (country.isBlank()) {
                return name.takeIf { it.isNotBlank() } ?: "-"
            }

            if (name.isBlank()) {
                return country.takeIf { it.isNotBlank() } ?: "-"
            }

            return "$name, $country"
        }

    override fun matches(query: String): Boolean {
        val applicableCandidates = listOf(
            label,
            name,
            country,
            name.split(" ").map { it.first() }.joinToString(""),
            "${name.first()}${country.first()}",
            "${country.first()}${name.first()}"
        )

        return applicableCandidates.any { it.contains(query, ignoreCase = true) }
    }
}

@Immutable
data class SelectableLocation(val location: Location, val isSelected: Boolean)