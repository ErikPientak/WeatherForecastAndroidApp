package com.example.weatherforecastandroidapp.data.repository

import com.example.weatherforecastandroidapp.data.local.SavedPlaceDao
import com.example.weatherforecastandroidapp.data.local.SavedPlaceEntity
import com.example.weatherforecastandroidapp.data.model.PlaceSearchResult
import com.example.weatherforecastandroidapp.data.remote.GeocodingApiService
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Reference example for testing a Repository with MockK + kotlinx-coroutines-test.
 *
 * The pattern used throughout this class, reusable for any suspend-function repository/service:
 * 1. mockk<Interface>() creates a fake implementation of an interface/class - every function on
 *    it does nothing and returns nothing until you explicitly stub it.
 * 2. coEvery { fake.someSuspendFun(args) } returns value stubs what a suspend function should
 *    return when called with those args. (Use plain `every` instead of `coEvery` for non-suspend
 *    functions.) If a stubbed function is called with different args than any coEvery you set up,
 *    or isn't stubbed at all, MockK throws - which is a feature, not an annoyance: it means an
 *    untested code path was hit "for real" instead of silently passing.
 * 3. runTest { ... } (from kotlinx-coroutines-test) is required to call any suspend function from
 *    a JUnit test, since @Test methods themselves can't be `suspend`. It also runs coroutines
 *    "instantly" (skipping real delays), so tests involving delay()/timeouts stay fast.
 * 4. coVerify(exactly = n) { fake.someSuspendFun(args) } asserts a suspend function WAS (or, with
 *    exactly = 0, was NOT) called - useful when the thing under test is "did we call X", not just
 *    "what did the function return".
 * 5. slot<T>() + capture(slot) lets you grab the actual argument passed into a stubbed call, so
 *    you can assert on its contents afterward via slot.captured. Use this when "was insert()
 *    called" isn't enough and you need to check *what* was passed to it.
 */
class PlacesRepositoryImplTest {

    // GeocodingApiService is a constructor dependency of PlacesRepositoryImpl but addPlace()
    // never touches it. We still have to pass *something* to satisfy the constructor, so it's
    // mocked here with zero stubbing - if addPlace() ever started calling it unexpectedly, MockK
    // would throw immediately and the test would fail loudly instead of silently doing the wrong
    // thing. That's a deliberate safety net, not an oversight.
    private val geocodingApiService = mockk<GeocodingApiService>()

    // This is the dependency addPlace() actually talks to, so every test below stubs it deliberately.
    private val savedPlaceDao = mockk<SavedPlaceDao>()

    private lateinit var repository: PlacesRepositoryImpl

    // Shared test fixture: one realistic PlaceSearchResult reused by both tests below, so each
    // test method only needs to set up the one thing it's actually varying (whether exists()
    // returns true or false), not re-declare the whole input every time.
    private val searchResult = PlaceSearchResult(
        name = "Prague",
        admin1 = "Prague",
        country = "Czechia",
        latitude = 50.0755,
        longitude = 14.4378,
    )

    // @Before runs before every single @Test method, giving each test a fresh repository instance
    // wired to the same fakes declared above. This matters because MockK mocks accumulate stubs/
    // verifications across calls - a fresh `repository` per test isn't strictly required here since
    // the mocks themselves aren't reset, but it's the standard shape to reach for.
    @Before
    fun setUp() {
        repository = PlacesRepositoryImpl(geocodingApiService, savedPlaceDao)
    }

    @Test
    fun `addPlace returns false and never inserts when the place already exists`() = runTest {
        // Arrange: tell the fake DAO to report this exact lat/lng as already saved.
        coEvery { savedPlaceDao.exists(searchResult.latitude, searchResult.longitude) } returns true

        // Act: call the real repository method under test - only savedPlaceDao is fake, the
        // addPlace() logic itself (the if-check, the early return) is the real production code.
        val result = repository.addPlace(searchResult)

        // Assert: both the return value...
        assertFalse(result)
        // ...and the *side effect that should NOT have happened*. This second assertion is the
        // important one - without it, a buggy addPlace() that inserts AND (incorrectly) returns
        // false would still pass the assertFalse check above.
        coVerify(exactly = 0) { savedPlaceDao.insert(any()) }
    }

    @Test
    fun `addPlace inserts the place and returns true when it does not exist yet`() = runTest {
        coEvery { savedPlaceDao.exists(searchResult.latitude, searchResult.longitude) } returns false

        // slot + capture: instead of stubbing insert() to just return a value, we intercept
        // whatever SavedPlaceEntity gets passed into it so we can inspect it after the fact.
        val insertedEntity = slot<SavedPlaceEntity>()
        coEvery { savedPlaceDao.insert(capture(insertedEntity)) } returns 1L // 1L = fake generated row id

        val result = repository.addPlace(searchResult)

        assertTrue(result)
        coVerify(exactly = 1) { savedPlaceDao.insert(any()) }

        // This is the part a simpler "was insert() called" test would miss: proving the mapping
        // from PlaceSearchResult -> SavedPlaceEntity inside addPlace() is field-correct, not just
        // that *some* entity got inserted. insertedEntity.captured is the exact object the real
        // addPlace() code constructed and passed to the (fake) DAO.
        assertEquals(searchResult.name, insertedEntity.captured.name)
        assertEquals(searchResult.admin1, insertedEntity.captured.admin1)
        assertEquals(searchResult.country, insertedEntity.captured.country)
        // Double comparisons need an explicit delta - the third argument to assertEquals here
        // means "equal to within 0.0", i.e. exactly equal. Required because assertEquals(Double,
        // Double) without a delta doesn't compile/resolve correctly for primitive doubles.
        assertEquals(searchResult.latitude, insertedEntity.captured.latitude, 0.0)
        assertEquals(searchResult.longitude, insertedEntity.captured.longitude, 0.0)
        // Note: addedAt (System.currentTimeMillis(), set inside addPlace()) is deliberately not
        // asserted here - it's a real timestamp we can't predict exactly. If you needed to test
        // it, you'd assert it falls within a reasonable time window instead of an exact value.
    }
}
