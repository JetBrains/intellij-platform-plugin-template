package org.jetbrains.plugins.template

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.jetbrains.plugins.template.weatherApp.model.Location
import org.jetbrains.plugins.template.weatherApp.ui.LocationsUIState
import org.jetbrains.plugins.template.weatherApp.ui.MyLocationsViewModelApi
import org.jetbrains.plugins.template.weatherApp.ui.MyLocationsListWithEmptyListPlaceholder
import org.junit.Test

internal class MyLocationListTest : ComposeBasedTestCase() {
    private val noLocations = emptyList<Location>()
    private val myLocationsViewModelApi = FakeMyLocationsViewModel(locations = noLocations)

    override val contentUnderTest: @Composable () -> Unit = {
        MyLocationsListWithEmptyListPlaceholder(
            modifier = Modifier.fillMaxWidth(),
            myLocationsViewModelApi = myLocationsViewModelApi
        )
    }

    @Test
    fun `verify placeholder is shown when no locations is added`() = runComposeTest {
        val myLocationsRobot = MyLocationListRobot(this)

        myLocationsRobot
            .verifyNoLocationsPlaceHolderVisible()
    }

    @Test
    fun `verify location is selected when user adds location`() = runComposeTest {
        val myLocationsRobot = MyLocationListRobot(this)

        myLocationsViewModelApi.onAddLocation(Location("Munich", "Germany"))

        myLocationsRobot
            .verifyListItemWithTextIsSelected("Munich, Germany")
    }

    @Test
    fun `verify item selection when multiple items are present`() = runComposeTest {
        val myLocationsRobot = MyLocationListRobot(this)

        // Add multiple locations
        myLocationsViewModelApi.onAddLocation(Location("Munich", "Germany"))
        myLocationsViewModelApi.onAddLocation(Location("Berlin", "Germany"))
        myLocationsViewModelApi.onAddLocation(Location("Paris", "France"))

        // Initially, the last added location (Paris) should be selected
        myLocationsRobot.verifyListItemWithTextIsSelected("Paris, France")

        // Select a different location
        myLocationsRobot.clickOnItemWithText("Berlin, Germany")

        // Verify the clicked location is now selected
        myLocationsRobot.verifyListItemWithTextIsSelected("Berlin, Germany")
    }

    @Test
    fun `verify item deletion when multiple items are present`() = runComposeTest {
        val myLocationsRobot = MyLocationListRobot(this)

        // Add multiple locations
        val munich = Location("Munich", "Germany")
        val berlin = Location("Berlin", "Germany")
        val paris = Location("Paris", "France")

        myLocationsViewModelApi.onAddLocation(munich)
        myLocationsViewModelApi.onAddLocation(berlin)
        myLocationsViewModelApi.onAddLocation(paris)

        // Initially, the last added location (Paris) should be selected
        myLocationsRobot.verifyListItemWithTextIsSelected("Paris, France")

        // Delete the selected location (Paris)
        myLocationsViewModelApi.onDeleteLocation(paris)

        // Verify Paris is no longer in the list and Berlin is now selected
        // (as it's the last item in the list after deletion)
        myLocationsRobot.verifyItemDoesNotExist("Paris, France")
        myLocationsRobot.verifyListItemWithTextIsSelected("Berlin, Germany")
    }

    @Test
    fun `verify middle item deletion when three items are present`() = runComposeTest {
        val myLocationsRobot = MyLocationListRobot(this)

        // Add three locations
        val munich = Location("Munich", "Germany")
        val berlin = Location("Berlin", "Germany")
        val paris = Location("Paris", "France")

        myLocationsViewModelApi.onAddLocation(munich)
        myLocationsViewModelApi.onAddLocation(berlin)
        myLocationsViewModelApi.onAddLocation(paris)

        // Initially, the last added location (Paris) should be selected
        myLocationsRobot.verifyListItemWithTextIsSelected("Paris, France")

        // Delete the middle location (Berlin)
        myLocationsViewModelApi.onDeleteLocation(berlin)

        // Verify Berlin is no longer in the list
        myLocationsRobot.verifyItemDoesNotExist("Berlin, Germany")

        // Verify Munich and Paris still exist
        myLocationsRobot.verifyItemExists("Munich, Germany")
        myLocationsRobot.verifyItemExists("Paris, France")

        // Paris should still be selected as it was the selected item before deletion
        myLocationsRobot.verifyListItemWithTextIsSelected("Paris, France")
    }

    @Test
    fun `verify deletion of the only item in list`() = runComposeTest {
        val myLocationsRobot = MyLocationListRobot(this)

        // Add one location
        val munich = Location("Munich", "Germany")
        myLocationsViewModelApi.onAddLocation(munich)

        // Verify the location is selected
        myLocationsRobot.verifyListItemWithTextIsSelected("Munich, Germany")

        // Delete the only location
        myLocationsViewModelApi.onDeleteLocation(munich)

        // Verify the location is no longer in the list
        myLocationsRobot.verifyItemDoesNotExist("Munich, Germany")

        // Verify the empty list placeholder is shown
        myLocationsRobot.verifyNoLocationsPlaceHolderVisible()
    }

    private class FakeMyLocationsViewModel(
        locations: List<Location> = emptyList()
    ) : MyLocationsViewModelApi {

        private val _myLocationsUIStateFlow: MutableStateFlow<LocationsUIState> =
            MutableStateFlow(LocationsUIState.initial(locations))


        override fun onAddLocation(locationToAdd: Location) {
            _myLocationsUIStateFlow.value = _myLocationsUIStateFlow.value.withLocationAdded(locationToAdd)
        }

        override fun onDeleteLocation(locationToDelete: Location) {
            _myLocationsUIStateFlow.value = _myLocationsUIStateFlow.value.withLocationDeleted(locationToDelete)

        }

        override fun onLocationSelected(selectedLocationIndex: Int) {
            _myLocationsUIStateFlow.value = _myLocationsUIStateFlow.value.withItemAtIndexSelected(selectedLocationIndex)
        }

        override val myLocationsUIStateFlow: Flow<LocationsUIState>
            get() = _myLocationsUIStateFlow.asStateFlow()

        override fun dispose() {
        }
    }

    private class MyLocationListRobot(private val composableRule: ComposeTestRule) {

        fun clickOnItemWithText(text: String) {
            composableRule
                .onNodeWithText(text)
                .performClick()
        }

        fun verifyNoLocationsPlaceHolderVisible() {
            composableRule
                .onNodeWithText("No locations added yet. Go and add the first location.")
                .assertExists()

            composableRule
                .onNodeWithContentDescription("Empty list icon.")
                .assertExists()
        }

        fun verifyListItemWithTextIsSelected(text: String) {
            composableRule
                .onNodeWithText(text)
                .assertExists()
                .assertIsSelected()
        }

        fun verifyItemDoesNotExist(text: String) {
            composableRule
                .onNodeWithText(text)
                .assertDoesNotExist()
        }

        fun verifyItemExists(text: String) {
            composableRule
                .onNodeWithText(text)
                .assertExists()
        }
    }
}
