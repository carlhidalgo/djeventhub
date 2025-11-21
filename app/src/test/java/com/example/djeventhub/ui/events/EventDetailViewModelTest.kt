package com.example.djeventhub.ui.events

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.djeventhub.Event
import com.example.djeventhub.EventRepository
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class EventDetailViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: EventDetailViewModel
    private lateinit var eventRepository: EventRepository
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        eventRepository = mockk()
        viewModel = EventDetailViewModel(eventRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        clearAllMocks()
    }

    @Test
    fun `loadEvent success should update state to Success`() = runTest {
        // Given
        val eventId = "event-123"
        val mockEvent = Event(
            id = eventId,
            name = "Test Event",
            description = "Test Description",
            date = System.currentTimeMillis(),
            endDate = null,
            locationName = "Test Location",
            latitude = 0.0,
            longitude = 0.0,
            imageUrl = null,
            musicGenre = "Electronic",
            createdBy = "user-123",
            applicants = emptyList(),
            selectedDJ = null
        )
        coEvery { eventRepository.getEventById(eventId) } returns mockEvent

        // When
        viewModel.loadEvent(eventId)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertTrue(state is EventDetailUiState.Success)
        assertEquals(mockEvent, (state as EventDetailUiState.Success).event)
    }

    @Test
    fun `loadEvent with null event should update state to Error`() = runTest {
        // Given
        val eventId = "non-existent-event"
        coEvery { eventRepository.getEventById(eventId) } returns null

        // When
        viewModel.loadEvent(eventId)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertTrue(state is EventDetailUiState.Error)
        assertTrue((state as EventDetailUiState.Error).message.contains("no encontrado"))
    }

    @Test
    fun `loadEvent failure should update state to Error`() = runTest {
        // Given
        val eventId = "event-123"
        val errorMessage = "Network error"
        coEvery { eventRepository.getEventById(eventId) } throws Exception(errorMessage)

        // When
        viewModel.loadEvent(eventId)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertTrue(state is EventDetailUiState.Error)
        assertEquals(errorMessage, (state as EventDetailUiState.Error).message)
    }

    @Test
    fun `applyToEvent success should reload event and call onSuccess`() = runTest {
        // Given
        val eventId = "event-123"
        val mockEvent = Event(
            id = eventId,
            name = "Test Event",
            description = "Test Description",
            date = System.currentTimeMillis(),
            endDate = null,
            locationName = "Test Location",
            latitude = 0.0,
            longitude = 0.0,
            imageUrl = null,
            musicGenre = "Electronic",
            createdBy = "user-123",
            applicants = listOf("dj-user-123"),
            selectedDJ = null
        )
        coEvery { eventRepository.applyToEvent(eventId) } returns Result.success(Unit)
        coEvery { eventRepository.getEventById(eventId) } returns mockEvent

        var onSuccessCalled = false
        var onErrorCalled = false

        // When
        viewModel.applyToEvent(
            eventId = eventId,
            onSuccess = { onSuccessCalled = true },
            onError = { onErrorCalled = true }
        )
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertTrue(onSuccessCalled)
        assertFalse(onErrorCalled)
        coVerify(exactly = 1) { eventRepository.applyToEvent(eventId) }
        coVerify(exactly = 1) { eventRepository.getEventById(eventId) }
    }

    @Test
    fun `applyToEvent failure should call onError`() = runTest {
        // Given
        val eventId = "event-123"
        val errorMessage = "Application failed"
        coEvery { eventRepository.applyToEvent(eventId) } returns Result.failure(Exception(errorMessage))

        var onSuccessCalled = false
        var receivedError: String? = null

        // When
        viewModel.applyToEvent(
            eventId = eventId,
            onSuccess = { onSuccessCalled = true },
            onError = { receivedError = it }
        )
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertFalse(onSuccessCalled)
        assertEquals(errorMessage, receivedError)
    }

    @Test
    fun `removeApplication success should reload event and call onSuccess`() = runTest {
        // Given
        val eventId = "event-123"
        val mockEvent = Event(
            id = eventId,
            name = "Test Event",
            description = "Test Description",
            date = System.currentTimeMillis(),
            endDate = null,
            locationName = "Test Location",
            latitude = 0.0,
            longitude = 0.0,
            imageUrl = null,
            musicGenre = "Electronic",
            createdBy = "user-123",
            applicants = emptyList(),
            selectedDJ = null
        )
        coEvery { eventRepository.removeApplication(eventId) } returns Result.success(Unit)
        coEvery { eventRepository.getEventById(eventId) } returns mockEvent

        var onSuccessCalled = false
        var onErrorCalled = false

        // When
        viewModel.removeApplication(
            eventId = eventId,
            onSuccess = { onSuccessCalled = true },
            onError = { onErrorCalled = true }
        )
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertTrue(onSuccessCalled)
        assertFalse(onErrorCalled)
        coVerify(exactly = 1) { eventRepository.removeApplication(eventId) }
        coVerify(exactly = 1) { eventRepository.getEventById(eventId) }
    }
}
