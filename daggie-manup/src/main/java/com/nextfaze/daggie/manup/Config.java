package com.nextfaze.daggie.manup;

import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.auto.value.AutoValue;
import com.ryanharter.auto.value.parcel.ParcelAdapter;

import okhttp3.HttpUrl;

/** Represents the mandatory update configuration. */
@AutoValue
abstract class Config implements Parcelable {

    /** Default instance to use before any has been saved from the server. */
    @NonNull
    static final Config DEFAULT = create(false, 0, 0, null);

    /** Whether app is currently down due to maintenance. */
    abstract boolean getMaintenanceMode();

    /** Version code of the current published Play Store app. */
    abstract int getCurrentVersion();

    /** Minimum version code required to run the app. Earlier versions will show the update screen. */
    abstract int getMinimumVersion();

    /** URL to open in order to update the app. */
    @ParcelAdapter(HttpUrlTypeAdapter.class)
    @Nullable
    abstract HttpUrl getUpdateUrl();

    @NonNull
    public static Config create(boolean maintenanceMode, int currentVersion, int minimumVersion, @Nullable HttpUrl updateUrl) {
        return new AutoValue_Config(maintenanceMode, currentVersion, minimumVersion, updateUrl);
    }
}
