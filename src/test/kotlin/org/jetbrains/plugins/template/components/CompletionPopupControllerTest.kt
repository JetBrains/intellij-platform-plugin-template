package org.jetbrains.plugins.template.components

import org.jetbrains.plugins.template.weatherApp.model.PreviewableItem
import org.jetbrains.plugins.template.weatherApp.model.Searchable
import org.jetbrains.plugins.template.weatherApp.services.SearchAutoCompletionItemProvider
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

internal class CompletionPopupControllerTest {

    private lateinit var mockProvider: MockSearchProvider
    private lateinit var controller: CompletionPopupController<TestItem>

    // Autocompleted items
    private val selectedItems = mutableListOf<CompletionItem<TestItem>>()

    @Before
    fun setUp() {
        val testItems = listOf(
            TestItem("Paris, France"),
            TestItem("Berlin, Germany"),
            TestItem("Chicago, USA"),
            TestItem("Rome, Italy")
        )
        mockProvider = MockSearchProvider(testItems)

        controller = CompletionPopupController(mockProvider) { item ->
            selectedItems.add(item)
        }
    }

    @After
    fun tearDown() {
        selectedItems.clear()
    }

    @Test
    fun `test query changes updates completion items`() {
        // When
        controller.onQueryChanged("a")

        // Then
        assertTrue(controller.isVisible)
        assertItemCount(4)
        assertOnlyItemAtIndexIsSelected(0)
    }

    @Test
    fun `test keyboard selection with onSelectMovedDown`() {
        // Given
        controller.onQueryChanged("a")
        assertEquals(4, controller.completionItems.size)
        assertOnlyItemAtIndexIsSelected(0) // First item is initially selected

        // When
        moveSelectionDown(1)

        // Then
        assertOnlyItemAtIndexIsSelected(1) // Second item should now be selected

        // When moving down again
        moveSelectionDown(1)

        // Then
        assertOnlyItemAtIndexIsSelected(2) // Third item should now be selected
    }

    @Test
    fun `test keyboard selection with onSelectMovedUp`() {
        // Given - Initialize with query and move to the last item
        controller.onQueryChanged("a")
        assertOnlyItemAtIndexIsSelected(0)
        assertItemCount(4)
        moveSelectionDown(3) // Move to the last item (index 3)

        // Test case 1: Move up from last item (index 3) to third item (index 2)
        moveSelectionUp()
        assertOnlyItemAtIndexIsSelected(2)

        // Test case 2: Move up from third item (index 2) to second item (index 1)
        moveSelectionUp()
        assertOnlyItemAtIndexIsSelected(1)

        // Test case 3: Move up from second item (index 1) to first item (index 0)
        moveSelectionUp()
        assertOnlyItemAtIndexIsSelected(0)
    }

    @Test
    fun `test keyboard selection stays within boundaries`() {
        // Given
        controller.onQueryChanged("a")
        assertEquals(4, controller.completionItems.size)
        assertOnlyItemAtIndexIsSelected(0) // First item is initially selected

        // When moving up from the first item
        moveSelectionUp(1)

        // Then it should stay at the first item (no wrapping)
        assertOnlyItemAtIndexIsSelected(0) // First item should still be selected

        // Move to the last item
        moveSelectionDown(3)
        assertOnlyItemAtIndexIsSelected(3) // Last item is selected

        // When moving down from the last item
        moveSelectionDown(1)

        // Then it should stay at the last item (no wrapping)
        assertOnlyItemAtIndexIsSelected(3) // Last item should still be selected
    }

    @Test
    fun `test selection confirmation with keyboard navigation`() {
        // Given
        controller.onQueryChanged("a")
        moveSelectionDown(1)

        // When
        controller.onSelectionConfirmed()

        // Then
        assertItemSelected("Berlin, Germany")
    }

    @Test
    fun `test selection confirmation with explicit item (mouse click)`() {
        // Given
        controller.onQueryChanged("a")
        val itemToSelect = controller.completionItems[2] // "Chicago, USA"

        controller.onItemClicked(itemToSelect)

        // Then
        assertItemSelected("Chicago, USA")
    }

    /**
     * Asserts that the given selected item label matches the expected label and verifies the state
     * of the selected items list and popup visibility after a selection operation.
     *
     * @param selectedItemLabel The expected label of the selected item.
     */
    private fun assertItemSelected(selectedItemLabel: String) {
        assertEquals(1, selectedItems.size)
        assertEquals(selectedItemLabel, selectedItems[0].item.label)
        assertFalse(controller.isVisible) // Popup should be hidden after selection
    }

    /**
     * Helper method to move selection down by the specified number of steps
     *
     * @param step Number of steps to move down
     */
    private fun moveSelectionDown(step: Int) {
        repeat(step) {
            controller.onSelectionMovedDown()
        }
    }

    /**
     * Helper method to move selection up by the specified number of steps
     *
     * @param step Number of steps to move up
     */
    private fun moveSelectionUp(step: Int = 1) {
        repeat(step) {
            controller.onSelectionMovedUp()
        }
    }

    /**
     * Asserts that the current number of completion items matches the expected count.
     *
     * @param count The expected number of completion items.
     */
    private fun assertItemCount(count: Int) {
        assertEquals(count, controller.completionItems.size)
    }

    /**
     * Helper method to assert that only the item at the specified index is selected
     */
    private fun assertOnlyItemAtIndexIsSelected(selectedIndex: Int) {
        for (i in controller.completionItems.indices) {
            if (i == selectedIndex) {
                assertTrue(
                    "Item at index $selectedIndex should be selected",
                    controller.completionItems[i].isSelected
                )
            } else {
                assertFalse(
                    "Item at index $i should not be selected",
                    controller.completionItems[i].isSelected
                )
            }
        }
    }

    private data class TestItem(override val label: String) : Searchable, PreviewableItem {
        override fun matches(query: String): Boolean = label.contains(query, ignoreCase = true)
    }

    private class MockSearchProvider(private val items: List<TestItem>) : SearchAutoCompletionItemProvider<TestItem> {
        override fun provideSearchableItems(searchTerm: String): List<TestItem> {
            return items.filter { it.matches(searchTerm) }
        }
    }
}