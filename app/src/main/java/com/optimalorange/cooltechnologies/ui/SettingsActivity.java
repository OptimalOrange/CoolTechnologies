package com.optimalorange.cooltechnologies.ui;

import com.optimalorange.cooltechnologies.ui.fragment.SettingsFragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class SettingsActivity extends BaseActivity {

    public static void start(Context context) {
        context.startActivity(new Intent(context, SettingsActivity.class));
    }

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
