package com.nextfaze.daggie.manup

import android.content.DialogInterface
import android.content.DialogInterface.BUTTON_NEGATIVE
import android.content.DialogInterface.BUTTON_POSITIVE
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.support.annotation.StringRes
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatDialogFragment
import android.view.View.GONE
import android.view.View.VISIBLE
import com.nextfaze.daggie.manup.Result.MAINTENANCE_MODE
import com.nextfaze.daggie.manup.Result.UPDATE_RECOMMENDED
import com.nextfaze.daggie.manup.Result.UPDATE_REQUIRED
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

private const val ARG_CONFIG = "config"
private val FRAGMENT_TAG = ManUpDialogFragment::class.java.name

/** This dialog is shown when an optional or mandatory update is available. */
internal class ManUpDialogFragment : AppCompatDialogFragment() {

    /** The ManUp configuration used by this dialog. */
    internal var config by parcelableArgument<Config>(ARG_CONFIG)

    /** Evaluates the update [Result] based on the [config] and current app version. */
    private val result get() = activity?.application?.versionCode?.let { config.check(it) }

    companion object {
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

        /** Returns an existing instance of the fragment, if present already in [fragmentManager]. */
        private fun find(fragmentManager: FragmentManager) =
            fragmentManager.findFragmentByTag(FRAGMENT_TAG) as? ManUpDialogFragment
    }

    override fun onCreateDialog(savedInstanceState: Bundle?) = AlertDialog.Builder(context!!).apply {
        setTitle(titleResource)
        setMessage(messageResource)
        positiveButtonClickAction?.let { setPositiveButton(positiveButtonResource) { _, _ -> it.invoke() } }
        negativeButtonClickAction?.let { setNegativeButton(negativeButtonResource) { _, _ -> it.invoke() } }
    }.create()!!

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
    private fun updateApp(updateUrl: String) = activity?.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(updateUrl)))

    private fun updateDialog() {
        dialog?.let {
            it.setTitle(titleResource)
            it.setMessage(getString(messageResource))
            it.updateButton(BUTTON_POSITIVE, positiveButtonClickAction, positiveButtonResource)
            it.updateButton(BUTTON_NEGATIVE, negativeButtonClickAction, negativeButtonResource)
        }
    }

    private fun AlertDialog.updateButton(which: Int, action: (() -> Unit?)?, @StringRes textResource: Int) {
        val button = getButton(which)
        button.visibility = if (action != null) VISIBLE else GONE
        button.text = getString(textResource)
        button.setOnClickListener { action?.invoke() }
    }

    private fun finishAffinityIfUpdateRequired() {
        // Finish all activities in task if this is a mandatory update or maintenance mode
        if (result == UPDATE_REQUIRED || result == MAINTENANCE_MODE) activity?.let { ActivityCompat.finishAffinity(it) }
    }

    private val titleResource: Int
        @StringRes get() = when (result) {
            MAINTENANCE_MODE -> R.string.daggie_manup_maintenance_mode_title
            UPDATE_REQUIRED -> R.string.daggie_manup_update_required_title
            UPDATE_RECOMMENDED -> R.string.daggie_manup_update_available_title
            else -> R.string.daggie_manup_update_available_title
        }

    private val messageResource: Int
        @StringRes get() = when (result) {
            MAINTENANCE_MODE -> R.string.daggie_manup_maintenance_mode_message
            UPDATE_REQUIRED -> R.string.daggie_manup_update_required_message
            UPDATE_RECOMMENDED -> R.string.daggie_manup_update_available_message
            else -> R.string.daggie_manup_update_available_message
        }

    private val positiveButtonClickAction: (() -> Unit?)?
        get() = when (result) {
            MAINTENANCE_MODE -> {
                { dismissAllowingStateLoss() }
            }
            UPDATE_REQUIRED, UPDATE_RECOMMENDED -> {
                config.updateUrl?.let { { updateApp(it) } }
            }
            else -> null
        }

    private val positiveButtonResource: Int
        @StringRes get() = when (result) {
            MAINTENANCE_MODE -> R.string.daggie_manup_ok
            UPDATE_REQUIRED, UPDATE_RECOMMENDED -> R.string.daggie_manup_update
            else -> R.string.daggie_manup_update
        }

    private val negativeButtonClickAction: (() -> Unit)?
        get() = when (result) {
            MAINTENANCE_MODE -> null
            else -> {
                { dismissAllowingStateLoss() }
            }
        }

    private val negativeButtonResource: Int
        @StringRes get() = R.string.daggie_manup_cancel

    override fun getDialog() = super.getDialog() as? AlertDialog
}

internal fun <P : Parcelable> parcelableArgument(key: String) = object : ReadWriteProperty<Fragment, P> {
    override fun getValue(thisRef: Fragment, property: KProperty<*>): P = thisRef.arguments!!.getParcelable(key)

    override fun setValue(thisRef: Fragment, property: KProperty<*>, value: P) {
        if (thisRef.arguments == null) thisRef.arguments = Bundle()
        thisRef.arguments!!.putParcelable(key, value)
    }
}
