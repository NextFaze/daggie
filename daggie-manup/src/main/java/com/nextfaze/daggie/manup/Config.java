package com.nextfaze.daggie.manup;

import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;
import com.ryanharter.auto.value.parcel.ParcelAdapter;

import okhttp3.HttpUrl;

/** Represents the mandatory update configuration. */
@AutoValue
abstract class Config implements Parcelable {

    /** Default instance to use before any has been saved from the server. */
    @NonNull
    static final Config DEFAULT = create(0, 0, null);

    /** Version code of the current published Play Store app. */
    @SerializedName("manUpAppVersionCurrent")
    abstract int getCurrentVersion();

    /** Minimum version code required to run the app. Earlier versions will show the update screen. */
    @SerializedName("manUpAppVersionMin")
    abstract int getMinimumVersion();

    /** URL to open in order to update the app. */
    @SerializedName("manUpAppUpdateURLMin")
    @ParcelAdapter(HttpUrlTypeAdapter.class)
    @Nullable
    abstract HttpUrl getUpdateUrl();

    @NonNull
    public static Config create(int currentVersion, int minimumVersion, @Nullable HttpUrl updateUrl) {
        return new AutoValue_Config(currentVersion, minimumVersion, updateUrl);
    }

    @NonNull
    @SuppressWarnings("WeakerAccess")
    public static TypeAdapter<Config> typeAdapter(@NonNull Gson gson) {
        return new AutoValue_Config.GsonTypeAdapter(gson);
    }
}
