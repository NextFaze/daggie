package com.nextfaze.daggie.manup;

import android.os.Parcelable;
import android.support.annotation.NonNull;
import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;
import com.ryanharter.auto.value.parcel.ParcelAdapter;
import okhttp3.HttpUrl;

/** Represents the mandatory update configuration. */
@AutoValue abstract class Config implements Parcelable {

    /** Version code of the current published Play Store app. */
    @SerializedName("manUpAppVersionCurrent")
    abstract int getCurrentVersion();

    /** Minimum version code required to run the app. Earlier versions will show the update screen. */
    @SerializedName("manUpAppVersionMin")
    abstract int getMinimumVersion();

    /** URL to open in order to update the app. */
    @SerializedName("manUpAppUpdateURLMin")
    @ParcelAdapter(HttpUrlTypeAdapter.class)
    @NonNull
    abstract HttpUrl getUpdateUrl();

    @NonNull
    @SuppressWarnings("WeakerAccess")
    public static TypeAdapter<Config> typeAdapter(@NonNull Gson gson) {
        return new AutoValue_Config.GsonTypeAdapter(gson);
    }
}
