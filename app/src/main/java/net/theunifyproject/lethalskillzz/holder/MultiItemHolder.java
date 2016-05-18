package net.theunifyproject.lethalskillzz.holder;

import android.content.Intent;
import android.support.v7.widget.PopupMenu;
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
import net.theunifyproject.lethalskillzz.activity.HashActivity;
import net.theunifyproject.lethalskillzz.activity.PostFeedActivity;
import net.theunifyproject.lethalskillzz.activity.ProfileActivity;
import net.theunifyproject.lethalskillzz.activity.ProfilePicActivity;
import net.theunifyproject.lethalskillzz.activity.UserListActivity;
import net.theunifyproject.lethalskillzz.app.AppConfig;
import net.theunifyproject.lethalskillzz.app.PrefManager;
import net.theunifyproject.lethalskillzz.model.MultiItem;
import net.theunifyproject.lethalskillzz.service.HttpService;
import net.theunifyproject.lethalskillzz.widget.FeedImageView;

/**
 * Created by Ibrahim on 15/11/2015.
 */
public class MultiItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private  List<MultiItem> multiItems;
    private final int VIEW_ITEM_HASH = 3;
    private final int VIEW_ITEM_FEED = 2;
    private final int VIEW_ITEM_USER = 1;


    public TextView feed_name, feed_username, feed_timestamp, feed_likeCount, feed_commentCount;
    public EmojiconTextView feed_statusMsg;
    public ImageView feed_isVerify;
    public RoundNetworkImageView feed_profilePic;
    public FeedImageView feed_feedImageView;
    public ImageButton feed_btnLike, feed_btnOption;
    public LinearLayout feed_clickCommentCount, feed_clickLikeCount;

    public TextView user_name, user_username, user_info, user_btn_follow;
    public ImageView user_isVerify;
    public RoundNetworkImageView user_profilePic;

    public TextView hash_tag, hash_count;



    public MultiItemHolder(View convertView, List<MultiItem> multiItems, int viewType) {
        super(convertView);
        this.multiItems = multiItems;

        itemView.setOnClickListener(this);

        switch (viewType) {

            case VIEW_ITEM_USER: {

                user_name = (TextView) convertView
                        .findViewById(R.id.user_name);
                user_username = (TextView) convertView
                        .findViewById(R.id.user_username);
                user_info = (TextView) convertView
                        .findViewById(R.id.user_info);
                user_profilePic = (RoundNetworkImageView) convertView
                        .findViewById(R.id.user_profilePic);
                user_isVerify = (ImageView) convertView
                        .findViewById(R.id.user_isVerify);
                user_btn_follow = (TextView) convertView
                        .findViewById(R.id.user_follow_btn);

                user_name.setOnClickListener(this);
                user_profilePic.setOnClickListener(this);
                user_btn_follow.setOnClickListener(this);

            }
            break;

            case VIEW_ITEM_FEED: {

                feed_name = (TextView) convertView
                        .findViewById(R.id.feed_name);
                feed_username = (TextView) convertView
                        .findViewById(R.id.feed_username);
                feed_timestamp = (TextView) convertView
                        .findViewById(R.id.feed_timestamp);
                feed_statusMsg = (EmojiconTextView) convertView
                        .findViewById(R.id.feed_message);
                feed_profilePic = (RoundNetworkImageView) convertView
                        .findViewById(R.id.feed_profilePic);
                feed_isVerify = (ImageView) convertView
                        .findViewById(R.id.feed_isVerify);
                feed_feedImageView = (FeedImageView) convertView
                        .findViewById(R.id.feed_image);
                feed_btnLike = (ImageButton) convertView
                        .findViewById(R.id.feed_btnLike);
                feed_btnOption = (ImageButton) convertView
                        .findViewById(R.id.feed_btnOption);
                feed_likeCount = (TextView) convertView
                        .findViewById(R.id.feed_likeCount);
                feed_commentCount = (TextView) convertView
                        .findViewById(R.id.feed_commentCount);
                feed_clickLikeCount = (LinearLayout) convertView
                        .findViewById(R.id.feed_click_like_count);
                feed_clickCommentCount = (LinearLayout) convertView
                        .findViewById(R.id.feed_click_comment_count);

                feed_name.setOnClickListener(this);
                feed_profilePic.setOnClickListener(this);
                feed_btnLike.setOnClickListener(this);
                feed_btnOption.setOnClickListener(this);
                feed_clickLikeCount.setOnClickListener(this);
                feed_clickCommentCount.setOnClickListener(this);
                feed_feedImageView.setOnClickListener(this);

            }
            break;

            case VIEW_ITEM_HASH: {

                hash_tag = (TextView) convertView.findViewById(R.id.hash_tag);
                hash_count = (TextView) convertView
                        .findViewById(R.id.hash_count);

            }
            break;

        }

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


            case R.id.user_profilePic:
                clickUser(view);
                break;

            case R.id.user_name:
                clickUser(view);
                break;

            case R.id.user_follow_btn:
                clickFollow(view);
                break;

            default:
                clickItem(view);
                break;
        }
    }


    private void clickItem(View v) {
        final int position = getPosition();
        MultiItem item = multiItems.get(position);

        switch (item.getType()) {

            case VIEW_ITEM_USER:
                clickUser(v);
                break;

            case VIEW_ITEM_FEED: {

//                String feedId = String.valueOf(item.getId());
//
//                Intent intent = new Intent(v.getContext(), DisplayFeedActivity.class);
//                intent.putExtra("feedId", feedId);
//                v.getContext().startActivity(intent);
            }
            break;

            case VIEW_ITEM_HASH: {
                String hash = String.valueOf(item.getHash());

                Intent intent = new Intent(v.getContext(), HashActivity.class);
                intent.putExtra("hash", hash);
                v.getContext().startActivity(intent);
            }
            break;

        }

    }



    private void clickLikeCount(View v) {
        final int position = getPosition();
        MultiItem item = multiItems.get(position);
        String feedId = String.valueOf(item.getId());

        Intent intent = new Intent(v.getContext(), UserListActivity.class);
        intent.putExtra("list_type", AppConfig.listLike);
        intent.putExtra("feedId", feedId);
        v.getContext().startActivity(intent);
    }


    private void clickCommentCount(View v) {

        final int position = getPosition();
        MultiItem item = multiItems.get(position);
        String feedId = String.valueOf(item.getId());

        Intent intent = new Intent(v.getContext(), CommentActivity.class);
        intent.putExtra("feedId", feedId);
        v.getContext().startActivity(intent);
    }


    private void clickUser(View v) {

        final int position = getPosition();
        MultiItem item = multiItems.get(position);
        String username = String.valueOf(item.getUsername());

        Intent intent = new Intent(v.getContext(), ProfileActivity.class);
        intent.putExtra("username", username);
        v.getContext().startActivity(intent);
    }


    private void clickProfilePic(View v) {

        final int position = getPosition();
        MultiItem item = multiItems.get(position);
        String username = String.valueOf(item.getUsername());

        Intent intent = new Intent(v.getContext(), ProfilePicActivity.class);
        intent.putExtra("username", username);
        v.getContext().startActivity(intent);

    }


    private void clickFollow(View v) {

        final int position = getPosition();
        MultiItem item = multiItems.get(position);
        String username = String.valueOf(item.getUsername());

        Intent grapprIntent = new Intent(v.getContext(), HttpService.class);
        grapprIntent.putExtra("intent_type", AppConfig.httpIntentFollowUser);
        grapprIntent.putExtra("username", username);

        if(item.getIsFollow()) {
            user_btn_follow.setText(R.string.btn_follow);
            user_btn_follow.setBackgroundResource(R.drawable.btn_follow_green);
            grapprIntent.putExtra("follow_type", "unfollow");
            item.setIsFollow(false);
        }
        else {
            user_btn_follow.setText(R.string.btn_unfollow);
            user_btn_follow.setBackgroundResource(R.drawable.btn_unfollow_blue);
            grapprIntent.putExtra("follow_type", "follow");
            item.setIsFollow(true);
        }

        v.getContext().startService(grapprIntent);

    }


    private void clickLike(View v) {
        final int position = getPosition();
        MultiItem item = multiItems.get(position);

        String feedId = String.valueOf(item.getId());
        int count = Integer.parseInt(item.getLikeCount());

        Intent grapprIntent = new Intent(v.getContext(), HttpService.class);
        grapprIntent.putExtra("intent_type", AppConfig.httpIntentLikeFeed);
        grapprIntent.putExtra("feedId", feedId);

        if (item.getIsLike()) {
            feed_btnLike.setImageResource(R.mipmap.ic_heart_outline_grey);
            grapprIntent.putExtra("like_type", "unlike");
            item.setIsLike(false);
            count -= 1;
            item.setLikeCount(String.valueOf(count));
            feed_likeCount.setText(item.getLikeCount());
        } else {
            feed_btnLike.setImageResource(R.mipmap.ic_heart_red);
            grapprIntent.putExtra("like_type", "like");
            item.setIsLike(true);
            count += 1;
            item.setLikeCount(String.valueOf(count));
            feed_likeCount.setText(String.valueOf(item.getLikeCount()));
        }

        v.getContext().startService(grapprIntent);

    }


    private void clickImage(final View v) {

        final int position = getPosition();
        MultiItem item = multiItems.get(position);

        if(item.getIsExpand()) {
            feed_feedImageView.setAdjustViewBounds(false);
            feed_feedImageView.setScaleType(FeedImageView.ScaleType.CENTER_CROP);
            item.setIsExpand(false);
        } else {
            feed_feedImageView.setAdjustViewBounds(true);
            feed_feedImageView.setScaleType(FeedImageView.ScaleType.FIT_XY);
            item.setIsExpand(true);
        }
    }


    private void clickOption(final View v) {

        final int position = getPosition();
        MultiItem item = multiItems.get(position);
        final String feedId = String.valueOf(item.getId());


        PopupMenu.OnMenuItemClickListener onMenuClick = new PopupMenu.OnMenuItemClickListener() {

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


        PrefManager pref = new PrefManager(v.getContext());
        if (!pref.getUsername().equals(item.getUsername())) {

            PopupMenu popupMenu = new PopupMenu(v.getContext(), v);
            popupMenu.setOnMenuItemClickListener(onMenuClick);
            popupMenu.inflate(R.menu.menu_feed_popup);
            popupMenu.show();

        } else {
            PopupMenu popupMenu = new PopupMenu(v.getContext(), v);
            popupMenu.setOnMenuItemClickListener(onMenuClick);
            popupMenu.inflate(R.menu.menu_my_feed_popup);
            popupMenu.show();
        }
    }


}
