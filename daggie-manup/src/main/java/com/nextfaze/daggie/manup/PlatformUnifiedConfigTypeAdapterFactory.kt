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
internal class PlatformUnifiedConfigTypeAdapterFactory : TypeAdapterFactory {

    override fun <T> create(gson: Gson, type: TypeToken<T>): TypeAdapter<T>? {
        if (!Config::class.java.isAssignableFrom(type.rawType)) {
            return null
        }

        val payloadAdapter = gson.getDelegateAdapter(this, type)
        val unifiedAdapter = gson.getDelegateAdapter(this, typeToken<UnifiedConfig>())

        return object : TypeAdapter<T>() {
            override fun write(writer: JsonWriter, value: T?) = payloadAdapter.write(writer, value)

            override fun read(reader: JsonReader): T? {
                val jsonObject = gson.fromJson<JsonObject>(reader, JsonObject::class.java)

                return if (jsonObject["android"] != null) {
                    // Is unified, so extract inner config
                    @Suppress("UNCHECKED_CAST")
                    unifiedAdapter.fromJsonTree(jsonObject)?.config as T?
                } else {
                    payloadAdapter.fromJsonTree(jsonObject)
                }
            }
        }
    }
}

private inline fun <reified T : Any> typeToken() = object : TypeToken<T>() {}
