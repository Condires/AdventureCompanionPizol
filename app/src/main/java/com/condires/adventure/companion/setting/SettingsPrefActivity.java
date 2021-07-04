package com.condires.adventure.companion.setting;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;

import com.condires.adventure.companion.R;
import com.condires.adventure.companion.audio.CompanionAudioService;


public class SettingsPrefActivity extends AppCompatPreferenceActivity {
    private static final String TAG = SettingsPrefActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // load settings fragment
        getFragmentManager().beginTransaction().replace(android.R.id.content, new MainPreferenceFragment()).commit();
    }

    public static class MainPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref);

            // gallery EditText change listener
            bindPreferenceSummaryToValue(findPreference("key_volume"));
            bindPreferenceSummaryToValue(findPreference("key_sms_signature"));
            bindPreferenceSummaryToValue(findPreference("key_caller"));
            bindPreferenceSummaryToValue(findPreference("key_notfall_nummer"));

            bindPreferenceSummaryToValue(findPreference("key_default_anlage"));

            bindPreferenceSummaryToValue(findPreference("key_time_to_notstop"));
            bindPreferenceSummaryToValue(findPreference("key_battery_level"));
            bindPreferenceSummaryToValue(findPreference("key_battery_temp"));
            bindPreferenceSummaryToValue(findPreference("key_internet_timeout"));
            bindPreferenceSummaryToValue(findPreference("key_min_verweilzeit_sec"));
            bindPreferenceSummaryToValue(findPreference("key_restart_delay_sec"));
            bindPreferenceSummaryToValue(findPreference("key_dist_log_m"));
            bindPreferenceSummaryToValue(findPreference("key_restart_distance_m"));
            bindPreferenceSummaryToValue(findPreference("key_wlan_distance_m"));
            bindPreferenceSummaryToValue(findPreference("key_gps_min_speed"));
            bindPreferenceSummaryToValue(findPreference("key_wlan_master_i"));
            bindPreferenceSummaryToValue(findPreference("key_apikey"));
            bindPreferenceSummaryToValue(findPreference("key_base_name"));




        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    private static void bindPreferenceSummaryToValue(Preference preference) {
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            String stringValue = newValue.toString();

            if (preference instanceof EditTextPreference) {

                if (preference.getKey().equals("key_caller")) {
                    // update the changed gallery name to summary filed
                    preference.setSummary(stringValue);
                }
                if (preference.getKey().equals("key_volume")) {
                    // update the changed gallery name to summary filed
                    preference.setSummary(stringValue);
                    if (stringValue != null) {
                        int volume = Integer.parseInt(stringValue);

                        // CompanionAudioService ist ein Singleton und muss in der aufrufenden Activity gesetzt sein
                        CompanionAudioService as = CompanionAudioService.getInstance();
                        if (as != null) {
                            as.setVolume(volume);
                        } else {
                            Log.d(TAG, "onPreferenceChange: CompanionAudioService ist nicht intialisiert, Volume kann nicht gesetzt werden");
                        }
                    }
                }
                if (preference.getKey().equals("key_notfall_nummer")) {
                    // update the changed gallery name to summary filed
                    preference.setSummary(stringValue);
                }


                if (preference.getKey().equals("key_time_to_notstop")) {
                    // update the changed gallery name to summary filed
                    preference.setSummary(stringValue);
                }
                if (preference.getKey().equals("key_battery_level")) {
                    // update the changed gallery name to summary filed
                    preference.setSummary(stringValue);
                }
                if (preference.getKey().equals("key_battery_temp")) {
                    // update the changed gallery name to summary filed
                    preference.setSummary(stringValue);
                }
                if (preference.getKey().equals("key_internet_timeout")) {
                    // update the changed gallery name to summary filed
                    preference.setSummary(stringValue);
                }

                if (preference.getKey().equals("key_apikey")) {
                    // update the changed gallery name to summary filed
                    preference.setSummary(stringValue);
                }
                if (preference.getKey().equals("key_base_name")) {
                    // update the changed gallery name to summary filed
                    preference.setSummary(stringValue);
                }
                if (preference.getKey().equals("key_default_anlage")) {
                    // update the changed gallery name to summary filed
                    preference.setSummary(stringValue);

                } else {
                    preference.setSummary(stringValue);
                }
            }
            return true;
        }
    };

    /**
     * Email client intent to send support mail
     * Appends the necessary device information to email body
     * useful when providing support
     */
    public static void sendFeedback(Context context) {
        String body = null;
        try {
            body = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
            body = "\n\n-----------------------------\nPlease don't remove this information\n Device OS: Android \n Device OS version: " +
                    Build.VERSION.RELEASE + "\n App Version: " + body + "\n Device Brand: " + Build.BRAND +
                    "\n Device Model: " + Build.MODEL + "\n Device Manufacturer: " + Build.MANUFACTURER;
        } catch (PackageManager.NameNotFoundException e) {
        }
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("message/rfc822");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"contact@androidhive.info"});
        intent.putExtra(Intent.EXTRA_SUBJECT, "Query from android app");
        intent.putExtra(Intent.EXTRA_TEXT, body);
        //context.startActivity(Intent.createChooser(intent, context.getString(choose_email_client));
    }
}
