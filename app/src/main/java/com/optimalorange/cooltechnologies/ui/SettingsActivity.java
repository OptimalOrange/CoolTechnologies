package com.optimalorange.cooltechnologies.ui;

import com.optimalorange.cooltechnologies.ui.fragment.SettingsFragment;

import android.os.Bundle;

public class SettingsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showSettingsMenuItem(false);
        // Display the fragment as the main content.
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .replace(android.R.id.content, new SettingsFragment())
                    .commit();
        }
    }

}
