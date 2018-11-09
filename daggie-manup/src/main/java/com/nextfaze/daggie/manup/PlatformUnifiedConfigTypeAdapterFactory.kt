package com.nextfaze.daggie.manup

import android.os.Parcelable
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.TypeAdapter
import com.google.gson.TypeAdapterFactory
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import kotlinx.android.parcel.Parcelize

/** These keys should not be used for future projects, here for maintaining backwards compatibility */
internal data class LegacyConfig(
    @SerializedName("manUpAppMaintenanceMode")
    val maintenanceMode: Boolean = false,

    @SerializedName("manUpAppVersionCurrent")
    val currentVersion: Int = 0,

    @SerializedName("manUpAppVersionMin")
    val minimumVersion: Int = 0,

    @SerializedName("manUpAppUpdateURLMin")
    val updateUrl: String? = null
)

/** New shorter, concise keys to be used for new configurations */
@Parcelize
internal data class Config(
    @SerializedName("enabled")
    val enabled: Boolean = true,

    @SerializedName("current")
    val currentVersion: Int = 0,

    @SerializedName("minimum")
    val minimumVersion: Int = 0,

    @SerializedName("url")
    val updateUrl: String? = null
) : Parcelable

/** [TypeAdapterFactory] for [Config] that supports platform unified format as well as legacy. */
internal class PlatformUnifiedConfigTypeAdapterFactory : TypeAdapterFactory {

    override fun <T> create(gson: Gson, type: TypeToken<T>): TypeAdapter<T>? {
        if (!Config::class.java.isAssignableFrom(type.rawType)) {
            return null
        }

        val legacyConfigAdapter = gson.getDelegateAdapter(this, typeToken<LegacyConfig>())
        val configAdapter = gson.getDelegateAdapter(this, type)

        @Suppress("UNCHECKED_CAST")
        return object : TypeAdapter<T>() {
            override fun write(writer: JsonWriter, value: T?) = legacyConfigAdapter.write(writer, value?.toLegacyConfig())

            override fun read(reader: JsonReader): T? {
                val jsonObject = gson.fromJson<JsonObject>(reader, JsonObject::class.java)

                val configModel = jsonObject["android"]
                return if (configModel != null) {
                    // Is unified, so extract inner config
                    val config = configAdapter.fromJsonTree(configModel)

                    if (config != Config()) {
                        // Using the new config keys
                        config
                    } else {
                        // No new config found so default to legacy
                        legacyConfigAdapter.fromJsonTree(configModel).toConfig()
                    }
                } else {
                    // Legacy, non-unified model
                    legacyConfigAdapter.fromJsonTree(jsonObject).toConfig()
                }
            }

            private fun LegacyConfig.toConfig() = Config(
                enabled = !maintenanceMode,
                currentVersion = currentVersion,
                minimumVersion = minimumVersion,
                updateUrl = updateUrl
            ) as T?

            private fun T.toLegacyConfig(): LegacyConfig {
                val config = this as Config
                return LegacyConfig(
                    maintenanceMode = !config.enabled,
                    currentVersion = config.currentVersion,
                    minimumVersion = config.minimumVersion,
                    updateUrl = config.updateUrl
                )
            }
        }
    }
}

private inline fun <reified T : Any> typeToken() = object : TypeToken<T>() {}
