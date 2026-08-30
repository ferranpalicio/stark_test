package com.pal.starktest.data.local.datastore

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.pal.starktest.data.local.datastore.StarkPreferencesSerializer.defaultValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

/**
 * Reads/writes [StarkPreferences] as JSON. A first launch has no file at all, in which case
 * DataStore hands back [defaultValue] — the all-null instance, i.e. "nothing saved yet".
 *
 * A [CorruptionException] is what DataStore expects for unreadable content; throwing it (rather
 * than letting the [SerializationException] escape) is what lets a corruption handler recover.
 */
object StarkPreferencesSerializer : Serializer<StarkPreferences> {

    override val defaultValue = StarkPreferences()

    override suspend fun readFrom(input: InputStream): StarkPreferences = try {
        Json.decodeFromString(
            StarkPreferences.serializer(),
            withContext(Dispatchers.IO) { input.readBytes() }.decodeToString(),
        )
    } catch (e: SerializationException) {
        throw CorruptionException("Unable to read StarkPreferences", e)
    }

    override suspend fun writeTo(t: StarkPreferences, output: OutputStream) {
        val bytes = Json.encodeToString(StarkPreferences.serializer(), t).encodeToByteArray()
        withContext(Dispatchers.IO) { output.write(bytes) }
    }

    /** File name under the app's `datastore/` dir. */
    const val FILE_NAME = "stark_preferences.json"
}
