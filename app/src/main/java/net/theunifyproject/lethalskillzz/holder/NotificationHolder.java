package net.theunifyproject.lethalskillzz.holder;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import net.android.volley.toolbox.RoundNetworkImageView;
import net.theunifyproject.lethalskillzz.R;
import net.theunifyproject.lethalskillzz.activity.DisplayFeedActivity;
import net.theunifyproject.lethalskillzz.activity.ProfileActivity;
import net.theunifyproject.lethalskillzz.activity.ProfilePicActivity;
import net.theunifyproject.lethalskillzz.app.AppConfig;
import net.theunifyproject.lethalskillzz.model.NotificationItem;
import net.theunifyproject.lethalskillzz.service.HttpService;

/**
 * Created by Ibrahim on 02/12/2015.
 */
public class NotificationHolder extends RecyclerView.ViewHolder implements View.OnClickListener  {

    private List<NotificationItem> notificationItems;
    private final int NOTIFICATION_FOLLOW = 0;
    private final int NOTIFICATION_MENTION = 1;
    private final int  NOTIFICATION_COMMENT = 2;
    private final int NOTIFICATION_LIKE = 3;

    public TextView msg, timestamp;
    public RoundNetworkImageView profilePic;
    public LinearLayout layout;

    public NotificationHolder(View convertView, List<NotificationItem> notificationItems) {
        super(convertView);
        this.notificationItems = notificationItems;

        itemView.setOnClickListener(this);

        msg = (TextView) convertView
                .findViewById(R.id.notify_msg);
        timestamp = (TextView) convertView
                .findViewById(R.id.notify_stamp);
        profilePic = (RoundNetworkImageView) convertView
                .findViewById(R.id.notify_profilePic);
        layout = (LinearLayout) convertView
                .findViewById(R.id.notify_layout);

    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.notify_profilePic:
                clickProfilePic(view);
                break;

            default:
                clickItem(view);
                break;
        }

    }

    private void clickItem(View v) {
        final int position = getPosition();
        NotificationItem item = notificationItems.get(position);

        //MainActivity.tabLayout.getTabAt(1).setIcon(R.drawable.ic_tab_feed);
        if(!item.getIsSeen()) {
            String notifyId = String.valueOf(item.getId());

            Intent intent = new Intent(v.getContext(), HttpService.class);
            intent.putExtra("intent_type", AppConfig.httpIntentSeenNotification);
            intent.putExtra("notifyId", notifyId);
            v.getContext().startService(intent);

            layout.setBackgroundColor(Color.TRANSPARENT);
            item.setIsSeen(true);
        }


        switch (item.getType()) {

            case NOTIFICATION_MENTION: {
                String data[] = item.getData().split(AppConfig.OTP_DELIMITER);
                String feedId = data[1];

                Intent intent = new Intent(v.getContext(), DisplayFeedActivity.class);
                intent.putExtra("feedId", feedId);
                v.getContext().startActivity(intent);
            }
            break;

            case NOTIFICATION_LIKE: {
                String data[] = item.getData().split(AppConfig.OTP_DELIMITER);
                String feedId = data[1];

                Intent intent = new Intent(v.getContext(), DisplayFeedActivity.class);
                intent.putExtra("feedId", feedId);
                v.getContext().startActivity(intent);
            }
            break;

            case NOTIFICATION_COMMENT: {
                String data[] = item.getData().split(AppConfig.OTP_DELIMITER);
                String feedId = data[1];

                Intent intent = new Intent(v.getContext(), DisplayFeedActivity.class);
                intent.putExtra("feedId", feedId);
                v.getContext().startActivity(intent);
            }
            break;

            case NOTIFICATION_FOLLOW: {
                clickUser(v);
            }
            break;
        }

    }

    private void clickUser(View v) {

        final int position = getPosition();
        NotificationItem item = notificationItems.get(position);
        String data[] = item.getData().split(AppConfig.OTP_DELIMITER);
        String username = data[0];

        Intent intent = new Intent(v.getContext(), ProfileActivity.class);
        intent.putExtra("username", username);
        v.getContext().startActivity(intent);
    }

    private void clickProfilePic(View v) {

        final int position = getPosition();
        NotificationItem item = notificationItems.get(position);
        String data[] = item.getData().split(AppConfig.OTP_DELIMITER);
        String username = data[0];

        Intent intent = new Intent(v.getContext(), ProfilePicActivity.class);
        intent.putExtra("username", username);
        v.getContext().startActivity(intent);

    }
}
