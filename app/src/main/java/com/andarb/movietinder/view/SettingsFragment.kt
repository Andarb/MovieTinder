package com.andarb.movietinder.view

import android.os.Bundle
import android.widget.Toast
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.andarb.movietinder.R


/**
 * Allows user to change app settings.
 */
class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        val namePreference: Preference? =
            findPreference(getString(R.string.preferences_device_name_key))

        namePreference?.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { preference, newValue ->
                val stringValue: String = newValue.toString()

                if (stringValue.length > 12 || stringValue.contains("\n")) {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.toast_device_name_limitations),
                        Toast.LENGTH_LONG
                    ).show()
                    false
                } else
                    true
            }
    }
}