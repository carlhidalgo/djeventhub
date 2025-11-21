package com.example.djeventhub.ui.auth

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.djeventhub.auth.AuthRepository
import com.example.djeventhub.data.UserRepository
import com.example.djeventhub.models.UserType
import com.google.firebase.auth.FirebaseUser
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
class AuthViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: AuthViewModel
    private lateinit var authRepository: AuthRepository
    private lateinit var userRepository: UserRepository
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        authRepository = mockk()
        userRepository = mockk()
        viewModel = AuthViewModel(authRepository, userRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        clearAllMocks()
    }

    @Test
    fun `signInWithEmail success should update state to Authenticated`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password123"
        val mockUser = mockk<FirebaseUser> {
            every { uid } returns "test-uid"
        }
        coEvery { authRepository.signInWithEmail(email, password) } returns mockUser

        // When
        viewModel.signInWithEmail(email, password)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertTrue(state is AuthUiState.Authenticated)
        assertEquals("test-uid", (state as AuthUiState.Authenticated).uid)
    }

    @Test
    fun `signInWithEmail failure should update state to Error`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "wrong-password"
        val errorMessage = "Invalid credentials"
        coEvery { authRepository.signInWithEmail(email, password) } throws Exception(errorMessage)

        // When
        viewModel.signInWithEmail(email, password)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertTrue(state is AuthUiState.Error)
        assertEquals(errorMessage, (state as AuthUiState.Error).message)
    }

    @Test
    fun `signUpWithEmail success should create user profile and update state`() = runTest {
        // Given
        val email = "newuser@example.com"
        val password = "password123"
        val userType = UserType.DJ
        val mockUser = mockk<FirebaseUser> {
            every { uid } returns "new-uid"
            every { this@mockk.email } returns email
        }
        coEvery { authRepository.signUpWithEmail(email, password) } returns mockUser
        coEvery { userRepository.createUserProfile(any(), any(), any(), any()) } returns Result.success(Unit)

        // When
        viewModel.signUpWithEmail(email, password, userType)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify {
            userRepository.createUserProfile(
                uid = "new-uid",
                email = email,
                userType = userType,
                displayName = "newuser"
            )
        }
        val state = viewModel.uiState.value
        assertTrue(state is AuthUiState.Authenticated)
        assertEquals("new-uid", (state as AuthUiState.Authenticated).uid)
    }

    @Test
    fun `signOut should update state to Idle`() = runTest {
        // Given
        every { authRepository.signOut() } just Runs

        // When
        viewModel.signOut()

        // Then
        verify { authRepository.signOut() }
        val state = viewModel.uiState.value
        assertTrue(state is AuthUiState.Idle)
    }

    @Test
    fun `setError should update state to Error with message`() = runTest {
        // Given
        val errorMessage = "Test error"

        // When
        viewModel.setError(errorMessage)

        // Then
        val state = viewModel.uiState.value
        assertTrue(state is AuthUiState.Error)
        assertEquals(errorMessage, (state as AuthUiState.Error).message)
    }
}
