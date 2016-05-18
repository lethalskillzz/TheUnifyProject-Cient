package net.theunifyproject.lethalskillzz.holder;

import android.content.Intent;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.PopupMenu.OnMenuItemClickListener;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.rockerhieu.emojicon.EmojiconTextView;

import java.util.List;

import net.android.volley.toolbox.RoundNetworkImageView;
import net.theunifyproject.lethalskillzz.R;
import net.theunifyproject.lethalskillzz.activity.CommentActivity;
import net.theunifyproject.lethalskillzz.activity.DisplayFeedActivity;
import net.theunifyproject.lethalskillzz.activity.PostFeedActivity;
import net.theunifyproject.lethalskillzz.activity.ProfileActivity;
import net.theunifyproject.lethalskillzz.activity.ProfilePicActivity;
import net.theunifyproject.lethalskillzz.activity.UserListActivity;
import net.theunifyproject.lethalskillzz.app.AppConfig;
import net.theunifyproject.lethalskillzz.app.PrefManager;
import net.theunifyproject.lethalskillzz.model.FeedItem;
import net.theunifyproject.lethalskillzz.service.HttpService;
import net.theunifyproject.lethalskillzz.widget.FeedImageView;

/**
 * Created by Ibrahim on 31/10/2015.
 */

public class FeedHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

    private List<FeedItem> feedItems;

    public TextView name, username, timestamp, likeCount, commentCount;
    public EmojiconTextView statusMsg;
    public ImageView isVerify;
    public RoundNetworkImageView profilePic;
    public FeedImageView feedImageView;
    public ImageButton btnLike, btnOption;
    public LinearLayout clickCommentCount, clickLikeCount;

    public FeedHolder(View convertView, List<FeedItem> feedItems) {
        super(convertView);
        this.feedItems = feedItems;

        itemView.setOnClickListener(this);
        name = (TextView) convertView
                .findViewById(R.id.feed_name);
        username = (TextView) convertView
                .findViewById(R.id.feed_username);
        timestamp = (TextView) convertView
                .findViewById(R.id.feed_timestamp);
        statusMsg = (EmojiconTextView) convertView
                .findViewById(R.id.feed_message);
        profilePic = (RoundNetworkImageView) convertView
                .findViewById(R.id.feed_profilePic);
        isVerify = (ImageView) convertView
                .findViewById(R.id.feed_isVerify);
        feedImageView = (FeedImageView) convertView
                .findViewById(R.id.feed_image);
        btnLike = (ImageButton) convertView
                .findViewById(R.id.feed_btnLike);
        btnOption = (ImageButton) convertView
                .findViewById(R.id.feed_btnOption);
        likeCount = (TextView) convertView
                .findViewById(R.id.feed_likeCount);
        commentCount = (TextView) convertView
                .findViewById(R.id.feed_commentCount);

        clickLikeCount = (LinearLayout) convertView
                .findViewById(R.id.feed_click_like_count);
        clickCommentCount = (LinearLayout) convertView
                .findViewById(R.id.feed_click_comment_count);


        name.setOnClickListener(this);
        profilePic.setOnClickListener(this);
        feedImageView.setOnClickListener(this);
        btnLike.setOnClickListener(this);
        btnOption.setOnClickListener(this);
        clickLikeCount.setOnClickListener(this);
        clickCommentCount.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {

            case R.id.feed_btnLike:
                clickLike(view);
                break;

            case R.id.feed_btnOption:
                clickOption(view);
                break;

            case R.id.feed_click_like_count:
                clickLikeCount(view);
                break;

            case R.id.feed_click_comment_count:
                clickCommentCount(view);
                break;

            case R.id.feed_profilePic:
                clickProfilePic(view);
                break;

            case R.id.feed_name:
                clickUser(view);
                break;

            case R.id.feed_image:
                clickImage(view);
                break;

            default:
                //clickItem(view);
                break;
        }
        //Toast.makeText(view.getContext(), view.toString() + getPosition(), Toast.LENGTH_SHORT).show();
    }


    private void clickItem(View v) {

        final int position = getPosition();
        FeedItem item = feedItems.get(position);
        String feedId = String.valueOf(item.getId());

        Intent intent = new Intent(v.getContext(), DisplayFeedActivity.class);
        intent.putExtra("feedId", feedId);
        v.getContext().startActivity(intent);
    }


    private void clickLike(View v) {
        final int position = getPosition();
        FeedItem item = feedItems.get(position);

        String feedId = String.valueOf(item.getId());
        int count = Integer.parseInt(item.getLikeCount());

        Intent grapprIntent = new Intent(v.getContext(), HttpService.class);
        grapprIntent.putExtra("intent_type", AppConfig.httpIntentLikeFeed);
        grapprIntent.putExtra("feedId", feedId);

        if (item.getIsLike()) {
            btnLike.setImageResource(R.mipmap.ic_heart_outline_grey);
            grapprIntent.putExtra("like_type", "unlike");
            item.setIsLike(false);
            count -= 1;
            item.setLikeCount(String.valueOf(count));
            likeCount.setText(item.getLikeCount());
        } else {
            btnLike.setImageResource(R.mipmap.ic_heart_red);
            grapprIntent.putExtra("like_type", "like");
            item.setIsLike(true);
            count += 1;
            item.setLikeCount(String.valueOf(count));
            likeCount.setText(String.valueOf(item.getLikeCount()));
        }

        v.getContext().startService(grapprIntent);

    }



    private void clickLikeCount(View v) {
        final int position = getPosition();
        FeedItem item = feedItems.get(position);
        String feedId = String.valueOf(item.getId());

        Intent intent = new Intent(v.getContext(), UserListActivity.class);
        intent.putExtra("list_type", AppConfig.listLike);
        intent.putExtra("feedId", feedId);
        v.getContext().startActivity(intent);
    }


    private void clickCommentCount(View v) {

        final int position = getPosition();
        FeedItem item = feedItems.get(position);
        String feedId = String.valueOf(item.getId());

        Intent intent = new Intent(v.getContext(), CommentActivity.class);
        intent.putExtra("feedId", feedId);
        v.getContext().startActivity(intent);
    }


    private void clickUser(View v) {

        final int position = getPosition();
        FeedItem item = feedItems.get(position);
        String username = String.valueOf(item.getUsername());

        Intent intent = new Intent(v.getContext(), ProfileActivity.class);
        intent.putExtra("username", username);
        v.getContext().startActivity(intent);
    }

    private void clickProfilePic(View v) {

        final int position = getPosition();
        FeedItem item = feedItems.get(position);
        String username = String.valueOf(item.getUsername());

        Intent intent = new Intent(v.getContext(), ProfilePicActivity.class);
        intent.putExtra("username", username);
        v.getContext().startActivity(intent);

    }


    private void clickImage(final View v) {

        final int position = getPosition();
        FeedItem item = feedItems.get(position);

        if(item.getIsExpand()) {
            feedImageView.setAdjustViewBounds(false);
            feedImageView.setScaleType(FeedImageView.ScaleType.CENTER_CROP);
            item.setIsExpand(false);
        } else {
            feedImageView.setAdjustViewBounds(true);
            feedImageView.setScaleType(FeedImageView.ScaleType.FIT_XY);
            item.setIsExpand(true);
        }
    }

    private void clickOption(final View v) {

        final int position = getPosition();
        FeedItem item = feedItems.get(position);
        final String feedId = String.valueOf(item.getId());


        OnMenuItemClickListener onMenuClick = new OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.feed_popup_edit: {
                        Intent intent = new Intent(v.getContext(), PostFeedActivity.class);
                        intent.putExtra("intent_type", "edit");
                        intent.putExtra("feedId", feedId);
                        v.getContext().startActivity(intent);
                    }
                    return true;

                    case R.id.feed_popup_delete: {
                        Intent intent = new Intent(v.getContext(), HttpService.class);
                        intent.putExtra("intent_type", AppConfig.httpIntentDeleteFeed);
                        intent.putExtra("feedId", feedId);
                        v.getContext().startService(intent);
                    }
                    return true;

                    case R.id.feed_popup_report: {
                        Intent intent = new Intent(v.getContext(), HttpService.class);
                        intent.putExtra("intent_type", AppConfig.httpIntentReportFeed);
                        intent.putExtra("feedId", feedId);
                        v.getContext().startService(intent);
                    }
                    return true;

                    case R.id.feed_popup_cancel:
                        return true;

                    default:
                        return false;
                }
            }
        };


        PopupMenu popupMenu = new PopupMenu(v.getContext(), v);
        popupMenu.setOnMenuItemClickListener(onMenuClick);

        PrefManager pref = new PrefManager(v.getContext());
        if (pref.getUsername().equals(item.getUsername())) {

           popupMenu.inflate(R.menu.menu_my_feed_popup);

        } else {

            popupMenu.inflate(R.menu.menu_feed_popup);
        }

        popupMenu.show();
    }
}