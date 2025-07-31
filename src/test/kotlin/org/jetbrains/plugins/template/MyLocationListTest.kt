package org.jetbrains.plugins.template

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.test.runTest
import org.jetbrains.jewel.intui.standalone.theme.IntUiTheme
import org.jetbrains.plugins.template.weatherApp.model.Location
import org.jetbrains.plugins.template.weatherApp.model.SelectableLocation
import org.jetbrains.plugins.template.weatherApp.services.MyLocationsViewModelApi
import org.jetbrains.plugins.template.weatherApp.ui.MyLocationsListWithEmptyListPlaceholder
import org.junit.Rule
import org.junit.Test

internal class MyLocationListTest {
    @get:Rule
    val composeRule = createComposeRule()
    private val noLocations = emptyList<Location>()
    private val myLocationsViewModelApi = FakeMyLocationsViewModel(locations = noLocations)

    @Test
    fun `verify placeholder is shown when no locations is added`() = composeRule.runComposeTest {
        val myLocationsRobot = MyLocationListRobot(this)

        myLocationsRobot
            .verifyNoLocationsPlaceHolderVisible()
    }

    @Test
    fun `verify location is selected when user adds location`() = composeRule.runComposeTest { locationsViewModelApi ->
        val myLocationsRobot = MyLocationListRobot(this)

        locationsViewModelApi.onAddLocation(Location("Munich", "Germany"))

        myLocationsRobot
            .verifyListItemWithTextIsSelected("Munich, Germany")
    }

    @Test
    fun `verify item selection when multiple items are present`() = composeRule.runComposeTest { locationsViewModelApi ->
        val myLocationsRobot = MyLocationListRobot(this)
        
        // Add multiple locations
        locationsViewModelApi.onAddLocation(Location("Munich", "Germany"))
        locationsViewModelApi.onAddLocation(Location("Berlin", "Germany"))
        locationsViewModelApi.onAddLocation(Location("Paris", "France"))
        
        // Initially, the last added location (Paris) should be selected
        myLocationsRobot.verifyListItemWithTextIsSelected("Paris, France")
        
        // Select a different location
        myLocationsRobot.clickOnItemWithText("Berlin, Germany")
        
        // Verify the clicked location is now selected
        myLocationsRobot.verifyListItemWithTextIsSelected("Berlin, Germany")
    }
    
    @Test
    fun `verify item deletion when multiple items are present`() = composeRule.runComposeTest { locationsViewModelApi ->
        val myLocationsRobot = MyLocationListRobot(this)
        
        // Add multiple locations
        val munich = Location("Munich", "Germany")
        val berlin = Location("Berlin", "Germany")
        val paris = Location("Paris", "France")
        
        locationsViewModelApi.onAddLocation(munich)
        locationsViewModelApi.onAddLocation(berlin)
        locationsViewModelApi.onAddLocation(paris)
        
        // Initially, the last added location (Paris) should be selected
        myLocationsRobot.verifyListItemWithTextIsSelected("Paris, France")
        
        // Delete the selected location (Paris)
        locationsViewModelApi.onDeleteLocation(paris)
        
        // Verify Paris is no longer in the list and Berlin is now selected
        // (as it's the last item in the list after deletion)
        myLocationsRobot.verifyItemDoesNotExist("Paris, France")
        myLocationsRobot.verifyListItemWithTextIsSelected("Berlin, Germany")
    }
    
    @Test
    fun `verify middle item deletion when three items are present`() = composeRule.runComposeTest { locationsViewModelApi ->
        val myLocationsRobot = MyLocationListRobot(this)
        
        // Add three locations
        val munich = Location("Munich", "Germany")
        val berlin = Location("Berlin", "Germany")
        val paris = Location("Paris", "France")
        
        locationsViewModelApi.onAddLocation(munich)
        locationsViewModelApi.onAddLocation(berlin)
        locationsViewModelApi.onAddLocation(paris)
        
        // Initially, the last added location (Paris) should be selected
        myLocationsRobot.verifyListItemWithTextIsSelected("Paris, France")
        
        // Delete the middle location (Berlin)
        locationsViewModelApi.onDeleteLocation(berlin)
        
        // Verify Berlin is no longer in the list
        myLocationsRobot.verifyItemDoesNotExist("Berlin, Germany")
        
        // Verify Munich and Paris still exist
        myLocationsRobot.verifyItemExists("Munich, Germany")
        myLocationsRobot.verifyItemExists("Paris, France")
        
        // Paris should still be selected as it was the selected item before deletion
        myLocationsRobot.verifyListItemWithTextIsSelected("Paris, France")
    }
    
    @Test
    fun `verify deletion of the only item in list`() = composeRule.runComposeTest { locationsViewModelApi ->
        val myLocationsRobot = MyLocationListRobot(this)
        
        // Add one location
        val munich = Location("Munich", "Germany")
        locationsViewModelApi.onAddLocation(munich)
        
        // Verify the location is selected
        myLocationsRobot.verifyListItemWithTextIsSelected("Munich, Germany")
        
        // Delete the only location
        locationsViewModelApi.onDeleteLocation(munich)
        
        // Verify the location is no longer in the list
        myLocationsRobot.verifyItemDoesNotExist("Munich, Germany")
        
        // Verify the empty list placeholder is shown
        myLocationsRobot.verifyNoLocationsPlaceHolderVisible()
    }

    private fun ComposeContentTestRule.runComposeTest(
        myLocationsViewModelApi: MyLocationsViewModelApi = this@MyLocationListTest.myLocationsViewModelApi,
        block: suspend ComposeContentTestRule.(MyLocationsViewModelApi) -> Unit
    ) = runTest {
        this@runComposeTest.setContentWrappedInTheme {
            MyLocationsListWithEmptyListPlaceholder(
                modifier = Modifier.fillMaxWidth(),
                myLocationsViewModelApi = myLocationsViewModelApi
            )
        }

        this@runComposeTest.block(myLocationsViewModelApi)
    }

    private class FakeMyLocationsViewModel(
        locations: List<Location> = emptyList()
    ) : MyLocationsViewModelApi {

        private val locationsFlow = MutableStateFlow(locations.toMutableList())

        private val selectedItemIndex = MutableStateFlow(if (locations.isNotEmpty()) 0 else -1)

        private val _myLocations = locationsFlow
            .combine(selectedItemIndex) { locations, selectedIndex ->
                locations.mapIndexed { index, location ->
                    SelectableLocation(location, index == selectedIndex)
                }
            }
        override val myLocationsFlow: Flow<List<SelectableLocation>> = _myLocations

        override fun onAddLocation(locationToAdd: Location) {
            val currentLocations = locationsFlow.value
            currentLocations.add(locationToAdd)

            locationsFlow.value = currentLocations
            selectedItemIndex.value = currentLocations.lastIndex
        }

        override fun onDeleteLocation(locationToDelete: Location) {
            val currentLocations = locationsFlow.value
            currentLocations.remove(locationToDelete)

            locationsFlow.value = currentLocations
            selectedItemIndex.value = currentLocations.lastIndex
        }

        override fun onLocationSelected(selectedLocationIndex: Int) {
            selectedItemIndex.value = selectedLocationIndex
        }
    }

    private fun ComposeContentTestRule.setContentWrappedInTheme(content: @Composable () -> Unit) {
        setContent {
            IntUiTheme {
                content()
            }
        }
    }
}

private class MyLocationListRobot(private val composableRule: ComposeContentTestRule) {

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