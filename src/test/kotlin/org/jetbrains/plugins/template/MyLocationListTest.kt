package org.jetbrains.plugins.template

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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

    @Test
    fun `show placeholder when no locations is added`() = runTest {
        val myLocationsRobot = MyLocationListRobot(composeRule)

        composeRule.setContentWrappedInTheme {
            val noLocations = emptyList<Location>()
            val myLocationsViewModel = FakeMyLocationsViewModel(locations = noLocations)
            MyLocationsListWithEmptyListPlaceholder(
                modifier = Modifier.fillMaxWidth(),
                myLocationsViewModelApi = myLocationsViewModel
            )
        }

        myLocationsRobot
            .verifyNoLocationsPlaceHolderVisible()
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
        }

        override fun onDeleteLocation(locationToDelete: Location) {
            val currentLocations = locationsFlow.value
            currentLocations.remove(locationToDelete)
            locationsFlow.value = currentLocations
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
}