package org.jetbrains.plugins.template.weatherApp.services

import org.jetbrains.plugins.template.weatherApp.model.Searchable

/**
 * Defines a provider interface for auto-completion items based on a search query.
 *
 * Implementations of this interface are responsible for supplying a filtered list of
 * items that match the given search term. These items must conform to the [Searchable] interface,
 * which ensures that they can be evaluated against the query.
 *
 * @param T The type of items provided by this interface, which must extend [Searchable].
 */
interface SearchAutoCompletionItemProvider<T : Searchable> {
    fun provideSearchableItems(searchTerm: String): List<T>
}