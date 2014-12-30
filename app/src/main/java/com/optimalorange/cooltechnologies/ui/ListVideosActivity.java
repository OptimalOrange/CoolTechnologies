package com.optimalorange.cooltechnologies.ui;

import com.optimalorange.cooltechnologies.R;
import com.optimalorange.cooltechnologies.ui.fragment.ListVideosFragment;

import android.os.Bundle;

//TODO back in ActionBar
public class ListVideosActivity extends BaseActivity {

    /**
     * 应当显示的Video的genre（类型，示例：手机）<br/>
     * Type: String
     */
    public static final String EXTRA_KEY_GENRE =
            ListVideosActivity.class.getName() + ".extra.KEY_GENRE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_videos);

        ListVideosFragment videosFragment = new ListVideosFragment();
        // 如果Intent有Extras，应用之
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            Bundle arguments = new Bundle();
            if (extras.containsKey(EXTRA_KEY_GENRE)) {
                String genre = extras.getString(EXTRA_KEY_GENRE);
                setTitle(genre);
                arguments.putString(ListVideosFragment.ARGUMENT_KEY_GENRE, genre);
            }
            videosFragment.setArguments(arguments);
        }
        // 添加videosFragment
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, videosFragment)
                    .commit();
        }
    }

}
