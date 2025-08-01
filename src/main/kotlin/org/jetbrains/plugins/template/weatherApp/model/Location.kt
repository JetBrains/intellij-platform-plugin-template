package org.jetbrains.plugins.template.weatherApp.model

import androidx.compose.runtime.Immutable

/**
 * Represents a location with a name and associated country.
 *
 * @property name The name of the location.
 * @property country The associated country of the location.
 * @property id A derived unique identifier for the location in the format `name, country`.
 */
@Immutable
data class Location(val name: String, val country: String) : PreviewableItem, Searchable {
    val id: String = "$name, $country"

    override fun isSearchApplicable(query: String): Boolean {
        val applicableCandidates = listOf(
            id,
            name,
            country,
            name.split(" ").map { it.first() }.joinToString(""),
            "${name.first()}${country.first()}",
            "${country.first()}${name.first()}"
        )

        return applicableCandidates.any { it.contains(query, ignoreCase = true) }
    }

    override val label: String
        get() = id
}

@Immutable
data class SelectableLocation(val location: Location, val isSelected: Boolean)