package com.optimalorange.cooltechnologies.ui.viewholder;

import com.optimalorange.cooltechnologies.R;
import com.optimalorange.cooltechnologies.entity.Comment;

import android.view.View;
import android.widget.TextView;

public class CommentView {

    public static class Holder {

        public View mRootView;

        public TextView mUserName;

        public TextView mContent;

        public TextView mDate;

        public Holder(final View rootView) {
            mRootView = rootView;
            mUserName = (TextView) rootView.findViewById(R.id.user_name);
            mContent = (TextView) rootView.findViewById(R.id.content);
            mDate = (TextView) rootView.findViewById(R.id.date);
        }

        public void updateData(Comment comment) {
            if (comment != null) {
                mUserName.setText(comment.getUser().getName());
                mContent.setText(comment.getContent());
                mDate.setText(comment.getPublished());
            } else {
                mUserName.setText("");
                mContent.setText("");
                mDate.setText("");
            }
        }
    }

}
