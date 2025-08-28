package org.jetbrains.plugins.template.weatherApp.services

import org.jetbrains.plugins.template.weatherApp.model.Location
import org.jetbrains.plugins.template.weatherApp.ui.LocationsUIState
import org.junit.Assert.*
import org.junit.Test

internal class LocationsUIStateTest {

    @Test
    fun `test initialization with empty locations`() {
        val state = LocationsUIState.initial(emptyList())

        assertTrue(state.locations.isEmpty())
        assertEquals(-1, state.selectedIndex)
        assertNull(state.selectedLocation)
    }

    @Test
    fun `test initialization with locations and valid selection`() {
        val locations = listOf(
            Location("Berlin", "Germany"),
            Location("Paris", "France")
        )
        val state = LocationsUIState.initial(locations).withItemAtIndexSelected(1)

        assertEquals(2, state.locations.size)
        assertEquals(1, state.selectedIndex)
        assertEquals(locations[1], state.selectedLocation)
    }

    @Test
    fun `test selecting item out of range throws exception`() {
        val locations = listOf(
            Location("Berlin", "Germany"),
            Location("Paris", "France")
        )

        assertThrows(IllegalArgumentException::class.java) {
            LocationsUIState.initial(locations).withItemAtIndexSelected(5)
        }
    }

    @Test
    fun `test toSelectableLocations with selection`() {
        val locations = listOf(
            Location("Berlin", "Germany"),
            Location("Paris", "France"),
            Location("Rome", "Italy")
        )
        val state = LocationsUIState
            .initial(locations)
            .withItemAtIndexSelected(1)

        val selectableLocations = state.toSelectableLocations()

        assertEquals(3, selectableLocations.size)
        assertFalse(selectableLocations[0].isSelected)
        assertTrue(selectableLocations[1].isSelected)
        assertFalse(selectableLocations[2].isSelected)

        assertEquals("Berlin, Germany", selectableLocations[0].location.label)
        assertEquals("Paris, France", selectableLocations[1].location.label)
        assertEquals("Rome, Italy", selectableLocations[2].location.label)
    }

    @Test
    fun `test withLocationAdded adds location when location doesn't exist`() {
        val locations = listOf(
            Location("Berlin", "Germany"),
            Location("Paris", "France")
        )
        val state = LocationsUIState.initial(locations).withItemAtIndexSelected(0)

        val newLocation = Location("Rome", "Italy")
        val newState = state.withLocationAdded(newLocation)

        assertEquals(3, newState.locations.size)
        assertEquals(2, newState.selectedIndex) // New location should be selected
        assertEquals(newLocation, newState.selectedLocation)
    }

    @Test
    fun `test withLocationAdded selects existing location when location already exists in list`() {
        val berlin = Location("Berlin", "Germany")
        val paris = Location("Paris", "France")
        val locations = listOf(berlin, paris)
        val state = LocationsUIState.initial(locations).withItemAtIndexSelected(0)

        // Add Berlin again (already exists)
        val newState = state.withLocationAdded(berlin)

        assertEquals(2, newState.locations.size) // No new location added
        assertEquals(0, newState.selectedIndex) // Berlin is selected
        assertEquals(berlin, newState.selectedLocation)
    }

    @Test
    fun `test withLocationRemoved removes location when location exists`() {
        val berlin = Location("Berlin", "Germany")
        val paris = Location("Paris", "France")
        val rome = Location("Rome", "Italy")
        val locations = listOf(berlin, paris, rome)
        val state = LocationsUIState.initial(locations).withItemAtIndexSelected(1) // Paris selected

        // Remove Paris (the selected location)
        val newState = state.withLocationDeleted(paris)

        assertEquals(2, newState.locations.size)
        assertEquals(0, newState.selectedIndex) // Selection should move to Berlin
        assertEquals(berlin, newState.selectedLocation)
    }

    @Test
    fun `test withLocationRemoved does nothing when location to remove doesn't exist in list`() {
        val berlin = Location("Berlin", "Germany")
        val paris = Location("Paris", "France")
        val locations = listOf(berlin, paris)
        val state = LocationsUIState.initial(locations).withItemAtIndexSelected(1) // Paris selected

        // Try to remove a location that doesn't exist
        val newState = state.withLocationDeleted(Location("Rome", "Italy"))

        // State should remain unchanged
        assertEquals(2, newState.locations.size)
        assertEquals(1, newState.selectedIndex)
        assertEquals(paris, newState.selectedLocation)
    }

    @Test
    fun `test withLocationRemoved when location to remove is currently selected location`() {
        val berlin = Location("Berlin", "Germany")
        val paris = Location("Paris", "France")
        val locations = listOf(berlin, paris)
        val state = LocationsUIState.initial(locations).withItemAtIndexSelected(1) // Paris selected

        // Remove Paris (the selected and last location)
        val newState = state.withLocationDeleted(paris)

        assertEquals(1, newState.locations.size)
        assertEquals(0, newState.selectedIndex) // Selection should move to Berlin
        assertEquals(berlin, newState.selectedLocation)
    }

    @Test
    fun `test withLocationRemoved when removing the only location`() {
        val berlin = Location("Berlin", "Germany")
        val locations = listOf(berlin)
        val state = LocationsUIState.initial(locations).withItemAtIndexSelected(0) // Berlin selected

        // Remove Berlin (the only location)
        val newState = state.withLocationDeleted(berlin)

        assertTrue(newState.locations.isEmpty())
        assertEquals(-1, newState.selectedIndex) // No selection
        assertNull(newState.selectedLocation)
    }

    @Test
    fun `test withLocationRemoved moves selection when location to remove is between first location and selected location`() {
        val berlin = Location("Berlin", "Germany")
        val paris = Location("Paris", "France")
        val rome = Location("Rome", "Italy")
        val madrid = Location("Madrid", "Spain")
        val locations = listOf(berlin, paris, rome, madrid)
        val state = LocationsUIState.initial(locations).withItemAtIndexSelected(2) // Rome selected (index 3)

        // Remove Paris (index 1) which is between 0 and selectedIndex (3)
        val newState = state.withLocationDeleted(paris)

        assertEquals(3, newState.locations.size)
        assertEquals(1, newState.selectedIndex) // Selection should be decremented by 1
        assertEquals(rome, newState.selectedLocation) // Still Rome, but at index 2 now
    }

    @Test
    fun `test withItemAtIndexSelected with valid index`() {
        val locations = listOf(
            Location("Berlin", "Germany"),
            Location("Paris", "France"),
            Location("Rome", "Italy")
        )
        val state = LocationsUIState.initial(locations).withItemAtIndexSelected(0) // Berlin selected

        // Select Rome
        val newState = state.withItemAtIndexSelected(2)

        assertEquals(3, newState.locations.size)
        assertEquals(2, newState.selectedIndex)
        assertEquals(locations[2], newState.selectedLocation)
    }

    @Test
    fun `test withLocationRemoved when removed location is after the selected location in list`() {
        val berlin = Location("Berlin", "Germany")
        val paris = Location("Paris", "France")
        val rome = Location("Rome", "Italy")
        val madrid = Location("Madrid", "Spain")
        val locations = listOf(berlin, paris, rome, madrid)
        val state = LocationsUIState.initial(locations).withItemAtIndexSelected(1) // Paris selected (index 1)

        // Remove Madrid (index 3) which is greater than selectedIndex (1)
        val newState = state.withLocationDeleted(madrid)

        assertEquals(3, newState.locations.size)
        assertEquals(1, newState.selectedIndex) // Selection should remain unchanged
        assertEquals(paris, newState.selectedLocation) // Still Paris at index 1
    }
}