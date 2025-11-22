package com.example.djeventhub.di

import android.content.Context
import com.example.djeventhub.EventRepository
import com.example.djeventhub.data.ChatRepository
import com.example.djeventhub.data.StorageRepository
import com.example.djeventhub.data.UserRepository
import com.example.djeventhub.location.LocationManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module that provides app-wide dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage {
        return FirebaseStorage.getInstance()
    }

    @Provides
    @Singleton
    fun provideEventRepository(
        firestore: FirebaseFirestore
    ): EventRepository {
        return EventRepository(firestore)
    }

    @Provides
    @Singleton
    fun provideUserRepository(
        firestore: FirebaseFirestore,
        auth: FirebaseAuth,
        storage: FirebaseStorage
    ): UserRepository {
        return UserRepository(firestore, auth, storage)
    }

    @Provides
    @Singleton
    fun provideChatRepository(
        firestore: FirebaseFirestore,
        auth: FirebaseAuth,
        userRepository: UserRepository
    ): ChatRepository {
        return ChatRepository(firestore, auth, userRepository)
    }

    @Provides
    @Singleton
    fun provideReviewRepository(
        firestore: FirebaseFirestore,
        auth: FirebaseAuth,
        userRepository: UserRepository,
        eventRepository: EventRepository
    ): com.example.djeventhub.data.ReviewRepository {
        return com.example.djeventhub.data.ReviewRepository(firestore, auth, userRepository, eventRepository)
    }

    @Provides
    @Singleton
    fun provideStorageRepository(
        storage: FirebaseStorage,
        auth: FirebaseAuth
    ): StorageRepository {
        return StorageRepository(storage, auth)
    }

    @Provides
    @Singleton
    fun provideLocationManager(
        @ApplicationContext context: Context
    ): LocationManager {
        return LocationManager(context)
    }
}
