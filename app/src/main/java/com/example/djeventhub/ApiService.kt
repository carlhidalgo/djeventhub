package com.example.djeventhub

import com.example.djeventhub.models.Event

// Retrofit annotations are intentionally omitted here to avoid static analysis errors in the editor.
// When wiring Retrofit, annotate the methods with @GET("/events") and @GET("/events/{id}") and add @Path where needed.
interface ApiService {
    // @GET("/events")
    suspend fun getEvents(): List<Event>

    // @GET("/events/{id}")
    // suspend fun getEvent(@Path("id") id: String): Event
    suspend fun getEvent(id: String): Event
}
