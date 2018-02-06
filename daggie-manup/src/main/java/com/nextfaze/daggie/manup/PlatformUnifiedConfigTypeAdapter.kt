package com.nextfaze.daggie.manup

import com.google.gson.Gson
import com.google.gson.TypeAdapter
import com.google.gson.TypeAdapterFactory
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import com.ryanharter.auto.value.gson.GsonTypeAdapterFactory
import okhttp3.HttpUrl

/** [TypeAdapter] for [Config] that supports platform unified format as well as legacy. */
internal class PlatformUnifiedConfigTypeAdapter(gson: Gson) : com.google.gson.TypeAdapter<Config?>() {

    private val httpUrlAdapter = gson.getAdapter(HttpUrl::class.java)

    override fun write(writer: JsonWriter, config: Config?) {
        if (config == null) {
            writer.nullValue()
            return
        }

        writer.beginObject()
        writer.name("manUpAppMaintenanceMode")
        writer.value(config.maintenanceMode)
        writer.name("manUpAppVersionCurrent")
        writer.value(config.currentVersion)
        writer.name("manUpAppVersionMin")
        writer.value(config.minimumVersion)
        writer.name("manUpAppUpdateURLMin")
        httpUrlAdapter.write(writer, config.updateUrl)
        writer.endObject()
    }

    override fun read(reader: JsonReader): Config? {
        if (reader.peek() == JsonToken.NULL) {
            reader.nextNull()
            return null
        }
        reader.beginObject()

        var isPlatformUnified = false

        var maintenanceMode = false
        var currentVersion = 0
        var minimumVersion = 0
        var updateUrl: HttpUrl? = null
        while (reader.hasNext()) {
            val name = reader.nextName()
            if (reader.peek() == JsonToken.NULL) {
                reader.nextNull()
                continue
            }

            when (name) {
                "android" -> {
                    isPlatformUnified = true
                    // Begin platform unified object
                    reader.beginObject()
                }
                "manUpAppMaintenanceMode" -> { maintenanceMode = reader.nextBoolean() }
                "manUpAppVersionCurrent" -> { currentVersion = reader.nextInt() }
                "manUpAppVersionMin" -> { minimumVersion = reader.nextInt() }
                "manUpAppUpdateURLMin" -> { updateUrl = httpUrlAdapter.read(reader) }
                else -> { reader.skipValue() }
            }
        }

        if (isPlatformUnified) {
            // End platform unified object
            reader.endObject()
        }

        reader.endObject()
        return AutoValue_Config(maintenanceMode, currentVersion, minimumVersion, updateUrl)
    }
}

@GsonTypeAdapterFactory internal abstract class PlatformUnifiedConfigTypeAdapterFactory : TypeAdapterFactory {
    companion object {
        @JvmStatic fun create() = object : TypeAdapterFactory {
            @Suppress("UNCHECKED_CAST")
            override fun <T> create(gson: Gson, type: TypeToken<T>): TypeAdapter<T>? {
                val rawType = type.rawType as Class<T>
                return if (Config::class.java.isAssignableFrom(rawType)) {
                    PlatformUnifiedConfigTypeAdapter(gson) as TypeAdapter<T>
                } else {
                    null
                }
            }
        }
    }
}
