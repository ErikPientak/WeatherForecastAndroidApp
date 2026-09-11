package com.example.weatherforecastandroidapp

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * Every ViewModel in this app launches coroutines via `viewModelScope`, which internally runs on
 * `Dispatchers.Main`. In a plain JVM unit test (no Android framework, no Looper), `Dispatchers.Main`
 * isn't set up at all - calling `viewModelScope.launch { }` without this rule throws
 * "Module with the Main dispatcher had failed to initialize".
 *
 * This JUnit Rule points `Dispatchers.Main` at a `TestDispatcher` before each test and restores it
 * after, so ViewModel code under test runs against a dispatcher the test can control (via
 * `runTest`'s `advanceUntilIdle()` etc.) instead of a real background thread.
 *
 * Usage in a ViewModel test class:
 * ```
 * @get:Rule
 * val mainDispatcherRule = MainDispatcherRule()
 * ```
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    // Exposed (not private) so test classes can pass it into runTest(testDispatcher) { ... }.
    // That's important, not just convenient: runTest normally creates its OWN TestDispatcher
    // with its own scheduler, separate from whatever Dispatchers.Main was set to below. If those
    // two schedulers are different objects, calling advanceUntilIdle() inside runTest would only
    // advance runTest's scheduler - it would never run the coroutines your ViewModel launched via
    // viewModelScope (which dispatches through Dispatchers.Main, i.e. THIS dispatcher). Passing
    // this same instance into runTest(...) makes them share one scheduler, so one
    // advanceUntilIdle() call advances both.
    val testDispatcher: TestDispatcher = StandardTestDispatcher(),
) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}
