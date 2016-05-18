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
import net.theunifyproject.lethalskillzz.app.AppConfig;
import net.theunifyproject.lethalskillzz.model.UserListItem;
import net.theunifyproject.lethalskillzz.service.HttpService;

/**
 * Created by Ibrahim on 15/11/2015.
 */
public class UserListHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private List<UserListItem> userListItems;

    public TextView name, username, info, btn_follow;
    public ImageView isVerify;
    public RoundNetworkImageView profilePic;

    public UserListHolder(View convertView,  List<UserListItem> userListItems) {
        super(convertView);
        this.userListItems = userListItems;

        itemView.setOnClickListener(this);

        name = (TextView) convertView
                .findViewById(R.id.user_list_name);
        username = (TextView) convertView
                .findViewById(R.id.user_list_username);
        info = (TextView) convertView
                .findViewById(R.id.user_list_info);
        profilePic = (RoundNetworkImageView) convertView
                .findViewById(R.id.user_list_profilePic);
        isVerify = (ImageView) convertView
                .findViewById(R.id.user_list_isVerify);
        btn_follow = (TextView) convertView
                .findViewById(R.id.user_list_follow_btn);

        name.setOnClickListener(this);
        profilePic.setOnClickListener(this);
        btn_follow.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {

            case R.id.user_list_name:
                clickUser(view);
                break;

            case R.id.user_list_profilePic:
                clickProfilePic(view);
                break;

            case R.id.user_list_follow_btn:
                clickFollow(view);
                break;

            default:
                clickItem(view);
                break;
        }
        //Toast.makeText(view.getContext(), "Clicked Country Position = " + getPosition(), Toast.LENGTH_SHORT).show();
    }


    private void clickItem(View v) {
        clickUser(v);
    }

    private void clickUser(View v) {

        final int position = getPosition();
        UserListItem item = userListItems.get(position);
        String username = String.valueOf(item.getUsername());

        Intent intent = new Intent(v.getContext(), ProfileActivity.class);
        intent.putExtra("username", username);
        v.getContext().startActivity(intent);
    }

    private void clickProfilePic(View v) {

        final int position = getPosition();
        UserListItem item = userListItems.get(position);
        String username = String.valueOf(item.getUsername());

        Intent intent = new Intent(v.getContext(), ProfilePicActivity.class);
        intent.putExtra("username", username);
        v.getContext().startActivity(intent);

    }

    private void clickFollow(View v) {

        final int position = getPosition();
        UserListItem item = userListItems.get(position);
        String username = String.valueOf(item.getUsername());

        Intent grapprIntent = new Intent(v.getContext(), HttpService.class);
        grapprIntent.putExtra("intent_type", AppConfig.httpIntentFollowUser);
        grapprIntent.putExtra("username", username);

        if(item.getIsFollow()) {
            btn_follow.setText(R.string.btn_follow);
            btn_follow.setBackgroundResource(R.drawable.btn_follow_green);
            grapprIntent.putExtra("follow_type", "unfollow");
            item.setIsFollow(false);
        }
        else {
            btn_follow.setText(R.string.btn_unfollow);
            btn_follow.setBackgroundResource(R.drawable.btn_unfollow_blue);
            grapprIntent.putExtra("follow_type", "follow");
            item.setIsFollow(true);
        }

        v.getContext().startService(grapprIntent);

    }



}
