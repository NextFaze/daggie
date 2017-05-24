package com.nextfaze.daggie.manup

import android.os.Parcel
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import okhttp3.HttpUrl

internal class HttpUrlTypeAdapter : com.google.gson.TypeAdapter<HttpUrl?>(),
        com.ryanharter.auto.value.parcel.TypeAdapter<HttpUrl?> {
    override fun write(writer: JsonWriter, url: HttpUrl?) {
        writer.value(url.toString())
    }

    override fun read(reader: JsonReader) = reader.nextString().let { HttpUrl.parse(it) }

    override fun toParcel(url: HttpUrl?, parcel: Parcel) = parcel.writeString(url.toString())

    override fun fromParcel(parcel: Parcel): HttpUrl? = HttpUrl.parse(parcel.readString())
}