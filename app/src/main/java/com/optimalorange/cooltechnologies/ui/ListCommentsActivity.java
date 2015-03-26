package com.optimalorange.cooltechnologies.ui;

import com.optimalorange.cooltechnologies.ui.fragment.ListCommentsFragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;

public class ListCommentsActivity extends LoginableBaseActivity {

    /**
     * 应当显示的评论所属的Video的ID<br/>
     * Type: String
     */
    public static final String EXTRA_KEY_VIDEO_ID =
            ListCommentsActivity.class.getName() + ".extra.KEY_VIDEO_ID";

    @NonNull
    public static Intent buildIntent(Context context, @NonNull String videoId) {
        return new Intent(context, ListCommentsActivity.class)
                .putExtra(EXTRA_KEY_VIDEO_ID, videoId);
    }

    public static void startMe(Context context, @NonNull String videoId) {
        context.startActivity(buildIntent(context, videoId));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String videoId = getIntent().getStringExtra(EXTRA_KEY_VIDEO_ID);
        if (videoId == null) {
            throw new IllegalStateException(
                    "Please do intent.putExtra(EXTRA_KEY_VIDEO_ID, vid)",
                    new NullPointerException("EXTRA_KEY_VIDEO_ID is null"));
        }

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(android.R.id.content, ListCommentsFragment.newInstance(videoId))
                    .commit();
        }
    }
}
