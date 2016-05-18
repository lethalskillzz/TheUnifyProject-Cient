package net.theunifyproject.lethalskillzz.holder;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import net.android.volley.toolbox.RoundNetworkImageView;
import net.theunifyproject.lethalskillzz.R;
import net.theunifyproject.lethalskillzz.activity.ProfileActivity;
import net.theunifyproject.lethalskillzz.activity.ProfilePicActivity;
import net.theunifyproject.lethalskillzz.model.CommentItem;

/**
 * Created by Ibrahim on 29/11/2015.
 */
public class CommentHolder  extends RecyclerView.ViewHolder implements View.OnClickListener {

    private List<CommentItem> commentItems;

    public TextView name, username, timestamp, commentMsg, likeCount, commentCount;
    public ImageView isVerify;
    public RoundNetworkImageView profilePic;

    public CommentHolder(View convertView,List<CommentItem> commentItems) {
        super(convertView);
        this.commentItems = commentItems;

        itemView.setOnClickListener(this);

        name = (TextView) convertView
                .findViewById(R.id.comment_name);
        username = (TextView) convertView
                .findViewById(R.id.comment_username);
        timestamp = (TextView) convertView
                .findViewById(R.id.comment_timestamp);
        commentMsg = (TextView) convertView
                .findViewById(R.id.comment_message);
        profilePic = (RoundNetworkImageView) convertView
                .findViewById(R.id.comment_profilePic);
        isVerify = (ImageView) convertView
                .findViewById(R.id.comment_isVerify);

        name.setOnClickListener(this);
        profilePic.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {

            case R.id.comment_name:
                clickUser(view);
                break;

            case R.id.comment_profilePic:
                clickProfilePic(view);
                break;

            default:
                clickItem(view);
                break;
        }
        //Toast.makeText(view.getContext(), "Clicked Country Position = " + getPosition(), Toast.LENGTH_SHORT).show();
    }


    private void clickItem(View v) {

    }


    private void clickUser(View v) {

        final int position = getPosition();
        CommentItem item = commentItems.get(position);
        String username = String.valueOf(item.getUsername());

        Intent intent = new Intent(v.getContext(), ProfileActivity.class);
        intent.putExtra("username", username);
        v.getContext().startActivity(intent);
    }

    private void clickProfilePic(View v) {

        final int position = getPosition();
        CommentItem item = commentItems.get(position);
        String username = String.valueOf(item.getUsername());

        Intent intent = new Intent(v.getContext(), ProfilePicActivity.class);
        intent.putExtra("username", username);
        v.getContext().startActivity(intent);

    }

}
