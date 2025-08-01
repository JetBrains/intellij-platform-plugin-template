package org.jetbrains.plugins.template.weatherApp.model

/**
 * Represents an entity that can be filtered by a search query.
 */
interface Searchable {
    fun isSearchApplicable(query: String): Boolean
}