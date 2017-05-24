package com.nextfaze.daggie.manup

import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.support.annotation.StringRes
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatDialogFragment
import com.nextfaze.daggie.manup.Result.UPDATE_RECOMMENDED
import com.nextfaze.daggie.manup.Result.UPDATE_REQUIRED
import okhttp3.HttpUrl
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

private const val ARG_CONFIG= "config"
private val FRAGMENT_TAG = ManUpDialogFragment::class.java.name

/** This dialog is shown when an optional or mandatory update is available. */
internal class ManUpDialogFragment : AppCompatDialogFragment() {

    /** The ManUp configuration used by this dialog. */
    internal var config by parcelableArgument<Config>(ARG_CONFIG)

    /** Evaluates the update [Result] based on the [config] and current app version. */
    private val result get() = config.check(activity.application.versionCode)

    companion object {
        /** Returns an existing instance of the fragment, if present already in [fragmentManager]. */
        internal fun find(fragmentManager: FragmentManager) =
                fragmentManager.findFragmentByTag(FRAGMENT_TAG) as? ManUpDialogFragment

        /** Shows the fragment in [fragmentManager], or updates an existing one, with the specified [Config]. */
        internal fun show(fragmentManager: FragmentManager, config: Config) {
            val existing = find(fragmentManager)
            if (existing != null) {
                existing.config = config
                existing.updateDialog()
            } else {
                val new = ManUpDialogFragment()
                new.config = config
                new.show(fragmentManager, FRAGMENT_TAG)
            }
        }

        /** Updates an existing fragment, if present, with the specified [Config]. */
        internal fun update(fragmentManager: FragmentManager, config: Config) {
            val existing = find(fragmentManager)
            if (existing != null) {
                existing.config = config
                existing.updateDialog()
            }
        }

        /** Dismisses an existing fragment, if present. */
        internal fun dismiss(fragmentManager: FragmentManager) = find(fragmentManager)?.dismissAllowingStateLoss()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?) = AlertDialog.Builder(context)
            .setTitle(titleResource())
            .setMessage(messageResource())
            .setPositiveButton(R.string.daggie_manup_update) { _, _ -> updateApp() }
            .setNegativeButton(R.string.daggie_manup_cancel) { _, _ -> dismissAllowingStateLoss() }
            .create()!!

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        finishAffinityIfUpdateRequired()
    }

    override fun onDismiss(dialog: DialogInterface?) {
        super.onDismiss(dialog)
        finishAffinityIfUpdateRequired()
    }

    override fun onResume() {
        super.onResume()
        updateDialog()
    }

    /** Opens the update URL. */
    private fun updateApp() = activity?.startActivity(Intent(Intent.ACTION_VIEW, config.updateUrl.toUri()))

    private fun updateDialog() {
        dialog?.setTitle(titleResource())
        dialog?.setMessage(getString(messageResource()))
    }

    private fun finishAffinityIfUpdateRequired() {
        // Finish all activities in task if this is a mandatory update
        if (result == UPDATE_REQUIRED) activity?.finishAffinity()
    }

    @StringRes private fun titleResource() = when (result) {
        UPDATE_REQUIRED -> R.string.daggie_manup_update_required_title
        UPDATE_RECOMMENDED -> R.string.daggie_manup_update_available_title
        else -> R.string.daggie_manup_update_available_title
    }

    @StringRes private fun messageResource() = when (result) {
        UPDATE_REQUIRED -> R.string.daggie_manup_update_required_message
        UPDATE_RECOMMENDED -> R.string.daggie_manup_update_available_message
        else -> R.string.daggie_manup_update_available_message
    }

    override fun getDialog() = super.getDialog() as? AlertDialog
}

private fun HttpUrl.toUri() = Uri.parse(toString())!!

internal fun <P : Parcelable> parcelableArgument(key: String) = object : ReadWriteProperty<Fragment, P> {
    override fun getValue(thisRef: Fragment, property: KProperty<*>): P = thisRef.arguments.getParcelable(key)

    override fun setValue(thisRef: Fragment, property: KProperty<*>, value: P) {
        if (thisRef.arguments == null) thisRef.arguments = Bundle()
        thisRef.arguments.putParcelable(key, value)
    }
}