package com.nextfaze.daggie.manup

import android.annotation.SuppressLint
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
import kotlin.reflect.KClass

@Parcelize
@SuppressLint("ParcelCreator")
internal data class Config(
        @SerializedName("manUpAppMaintenanceMode")
        val maintenanceMode: Boolean = false,

        @SerializedName("manUpAppVersionCurrent")
        val currentVersion: Int = 0,

        @SerializedName("manUpAppVersionMin")
        val minimumVersion: Int = 0,

        @SerializedName("manUpAppUpdateURLMin")
        val updateUrl: String? = null
) : Parcelable

private data class UnifiedConfig(
        @SerializedName("android")
        val config: Config? = null
)

/** [TypeAdapterFactory] for [Config] that supports platform unified format as well as legacy. */
internal abstract class PlatformUnifiedConfigTypeAdapterFactory : TypeAdapterFactory {
    companion object {
        @JvmStatic fun create() = object : TypeAdapterFactory {
            @Suppress("UNCHECKED_CAST")
            override fun <T> create(gson: Gson, type: TypeToken<T>): TypeAdapter<T>? {
                val rawType = type.rawType as Class<T>
                if (!Config::class.java.isAssignableFrom(rawType)) {
                    return null
                }

                val configAdapter = gson.getDelegateAdapter(this, Config::class.asTypeToken())
                val unifiedAdapter = gson.getDelegateAdapter(this, UnifiedConfig::class.asTypeToken())

                return object : TypeAdapter<Config>() {
                    override fun write(writer: JsonWriter, value: Config?) = configAdapter.write(writer, value)

                    override fun read(reader: JsonReader): Config? {
                        val jsonObject = gson.fromJson<JsonObject>(reader, JsonObject::class.java)

                        if (jsonObject.get("android") != null) {
                            // Is unified, so extract inner config
                            return unifiedAdapter.fromJsonTree(jsonObject)?.config
                        } else {
                            return configAdapter.fromJsonTree(jsonObject)
                        }
                    }

                } as TypeAdapter<T>
            }
        }
    }
}

fun <T : Any> KClass<T>.asTypeToken(): TypeToken<T> = TypeToken.get(this.java)
