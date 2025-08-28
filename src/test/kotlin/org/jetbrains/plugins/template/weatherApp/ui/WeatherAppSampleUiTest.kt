package org.jetbrains.plugins.template.weatherApp.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.ComposeTestRule
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.jetbrains.plugins.template.ComposeBasedTestCase
import org.jetbrains.plugins.template.weatherApp.model.Location
import org.jetbrains.plugins.template.weatherApp.services.SearchAutoCompletionItemProvider
import org.junit.Test

internal class WeatherAppSampleUiTest : ComposeBasedTestCase() {

    private val fakeSearchProvider = FakeSearchProvider()
    private val fakeMyLocationsViewModel = FakeMyLocationsViewModel()
    private val fakeWeatherViewModel = FakeWeatherViewModel()

    override val contentUnderTest: @Composable () -> Unit = {
        WeatherAppSample(
            myLocationViewModel = fakeMyLocationsViewModel,
            weatherViewModelApi = fakeWeatherViewModel,
            searchAutoCompletionItemProvider = fakeSearchProvider
        )
    }

    @Test
    fun `add location via search UI then remove it`() = runComposeTest {
        val robot = WeatherSampleRobot(this)

        // Add a location via UI: type, select autocomplete, click Add
        robot.focusAndTypeInSearchField("Mun")
        robot.waitForAutocomplete("Munich, Germany")
        robot.clickOnAutocompleteItem("Munich, Germany")
        robot.clickAddButton()

        // Verify the item appears selected in My Locations
        robot.verifyListItemWithTextIsSelected("Munich, Germany")

        // Remove the item via UI: open context menu with primary click (test mode) and click Delete
        robot.rightClickOnListItem("Munich, Germany")
        robot.clickDeleteInContextMenu()

        // Verify empty placeholder is shown again
        robot.verifyNoLocationsPlaceHolderVisible()
    }

    private class WeatherSampleRobot(private val rule: ComposeTestRule) {
        fun idle() = rule.waitForIdle()

        fun focusAndTypeInSearchField(text: String) {
            val field = rule.onNode(hasSetTextAction())
            field.performClick()
            field.performTextInput(text)
        }

        fun waitForAutocomplete(itemLabel: String) {
            rule.waitUntil(timeoutMillis = 100) {
                rule.onAllNodesWithText(itemLabel).fetchSemanticsNodes().isNotEmpty()
            }
        }

        fun clickOnAutocompleteItem(itemLabel: String) {
            rule.onNodeWithText(itemLabel).performClick()
        }

        fun clickAddButton() {
            rule.onNodeWithText("Add").performClick()
        }

        fun rightClickOnListItem(text: String) {
            rule.onNodeWithText(text)
                .assertExists("No node found with text: $text")
                .performMouseInput { rightClick() }
        }

        fun clickDeleteInContextMenu() {
            rule.onNodeWithText("Delete").performClick()
        }

        fun verifyListItemWithTextIsSelected(text: String) {
            rule.onNodeWithText(text).assertIsSelected()
        }

        fun verifyNoLocationsPlaceHolderVisible() {
            rule.onNodeWithText("No locations added yet. Go and add the first location.").assertExists()
        }
    }

    private class FakeSearchProvider : SearchAutoCompletionItemProvider<Location> {
        override fun provideSearchableItems(searchTerm: String): List<Location> {
            if (searchTerm.isBlank()) return emptyList()
            // Provide a small fixed set that includes Munich and others regardless of query for simplicity
            return listOf(
                Location("Munich", "Germany"),
                Location("Berlin", "Germany"),
                Location("Paris", "France"),
            ).filter { it.label.contains(searchTerm, ignoreCase = true) }
        }
    }

    private class FakeMyLocationsViewModel : MyLocationsViewModelApi {
        private val _state = MutableStateFlow(LocationsUIState.empty())

        override val myLocationsUIStateFlow: Flow<LocationsUIState>
            get() = _state.asStateFlow()

        override fun onAddLocation(locationToAdd: Location) {
            _state.value = _state.value.withLocationAdded(locationToAdd)
        }

        override fun onDeleteLocation(locationToDelete: Location) {
            _state.value = _state.value.withLocationDeleted(locationToDelete)
        }

        override fun onLocationSelected(selectedLocationIndex: Int) {
            _state.value = _state.value.withItemAtIndexSelected(selectedLocationIndex)
        }

        override fun dispose() {
            // no-op for tests
        }
    }

    private class FakeWeatherViewModel : WeatherViewModelApi {
        private val _weatherState = MutableStateFlow<WeatherForecastUIState>(WeatherForecastUIState.Empty)

        override val weatherForecastUIState: Flow<WeatherForecastUIState>
            get() = _weatherState.asStateFlow()

        override fun onLoadWeatherForecast(location: Location) {
            // no-op
        }

        override fun onReloadWeatherForecast() {
            // no-op
        }

        override fun dispose() {
            // no-op
        }
    }
}
