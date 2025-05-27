package ie.equalit.ceno.ui.robots

import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import ie.equalit.ceno.R
import ie.equalit.ceno.helpers.click
import ie.equalit.ceno.helpers.hasCousin
import org.hamcrest.CoreMatchers.allOf

/**
 * Implementation of Robot Pattern for the settings metrics menu.
 */
class SettingsViewMetricsRobot {

    fun verifyMetricsHeading(): ViewInteraction = assertMetricsHeading()
    fun verifyMetricsSubHeading(): ViewInteraction = assertMetricsSubHeading()
    fun verifyMetricsOptionalMetrics(): ViewInteraction = assertMetricsOptionalMetrics()
    fun verifyMetricsToggle1(): ViewInteraction = assertMetricsToggle1()
    fun verifyMetricsToggle2(): ViewInteraction = assertMetricsToggle2()
    fun verifyMetricsExplainer(): ViewInteraction = assertMetricsExplainer()
    fun verifyMetricsLearnMore(): ViewInteraction = assertMetricsLearnMore()
    fun verifyMetricsNegativeButton(): ViewInteraction = assertMetricsNegativeButton()
    fun verifyMetricsPositiveButton(): ViewInteraction = assertMetricsPositiveButton()
    fun verifyCrashReportingButton() = assertCrashReportingButton()
    fun verifyMetricsButton() = assertMetricsButton()


    class Transition {
        fun settingsViewMetrics(interact: SettingsViewMetricsRobot.() -> Unit): Transition {
            return Transition()
        }

        fun clickNegative(interact: SettingsViewRobot.() -> Unit): SettingsViewRobot.Transition {
            metricsNegativeButton().click()
            SettingsViewRobot().interact()
            return SettingsViewRobot.Transition()
        }
    }
}

private fun crashReportingButton() = onView(allOf(withId(R.id.campaignCrashReporting), hasDescendant(withText(R.string.preferences_allow_crash_reporting))))
private fun metricsButton() = onView(allOf(withId(R.id.campaignOuinetMetrics), hasDescendant(withText(R.string.metrics_ouinet_title))))

private fun metricsHeading() = Espresso.onView(ViewMatchers.withText(R.string.clean_insights_header))
private fun metricsSubHeading() = Espresso.onView(ViewMatchers.withText(R.string.clean_insights_sub_header))
private fun metricsOptionalMetrics() = Espresso.onView(ViewMatchers.withText(R.string.optional_metrics))
private fun metricsToggle1() = Espresso.onView(ViewMatchers.withText(R.string.device_type))
private fun metricsToggle2() = Espresso.onView(ViewMatchers.withText(R.string.device_locale))
private fun metricsExplainer() = Espresso.onView(ViewMatchers.withText(R.string.clean_insights_explainer))
private fun metricsLearnMore() = Espresso.onView(ViewMatchers.withText(R.string.learn_more_title))
private fun metricsNegativeButton() = Espresso.onView(ViewMatchers.withText(R.string.clean_insights_maybe_later))
private fun metricsPositiveButton() = Espresso.onView(ViewMatchers.withText(R.string.clean_insights_opt_in))
private fun assertMetricsHeading() = metricsHeading()
    .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertMetricsSubHeading() = metricsSubHeading()
    .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertMetricsOptionalMetrics() = metricsOptionalMetrics()
    .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertMetricsToggle1() = metricsToggle1()
    .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertMetricsToggle2() = metricsToggle2()
    .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertMetricsExplainer() = metricsExplainer()
    .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertMetricsLearnMore() = metricsLearnMore()
    .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertMetricsNegativeButton() = metricsNegativeButton()
    .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertMetricsPositiveButton() = metricsPositiveButton()
    .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertCrashReportingButton() = crashReportingButton()
    .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertMetricsButton() = metricsButton()
    .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
