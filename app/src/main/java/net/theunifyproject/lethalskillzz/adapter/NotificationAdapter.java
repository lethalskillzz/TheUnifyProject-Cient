package net.theunifyproject.lethalskillzz.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import net.android.volley.toolbox.ImageLoader;
import net.theunifyproject.lethalskillzz.R;
import net.theunifyproject.lethalskillzz.activity.ProfileActivity;
import net.theunifyproject.lethalskillzz.app.AppConfig;
import net.theunifyproject.lethalskillzz.app.AppController;
import net.theunifyproject.lethalskillzz.holder.NotificationHolder;
import net.theunifyproject.lethalskillzz.holder.ProgressViewHolder;
import net.theunifyproject.lethalskillzz.model.NotificationItem;

/**
 * Created by Ibrahim on 02/12/2015.
 */
public class NotificationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>  {

    private List<NotificationItem> notificationItems;
    private Context context;
    ImageLoader imageLoader = AppController.getInstance().getImageLoader();

    private final int VIEW_ITEM = 1;
    private final int VIEW_PROG = 0;

    private int visibleThreshold = 2;
    private int lastVisibleItem, totalItemCount;
    private boolean loading;
    private OnLoadMoreListener onLoadMoreListener;

    private final int NOTIFICATION_FOLLOW = 0;
    private final int NOTIFICATION_MENTION = 1;
    private final int  NOTIFICATION_COMMENT = 2;
    private final int NOTIFICATION_LIKE = 3;


    public NotificationAdapter(Context context, RecyclerView recyclerView, List<NotificationItem> notificationItems) {

        this.notificationItems = notificationItems;
        this.context = context;

        if (recyclerView.getLayoutManager() instanceof LinearLayoutManager) {

            final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
            recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    totalItemCount = linearLayoutManager.getItemCount();
                    lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();
                    if (!loading && totalItemCount <= (lastVisibleItem + visibleThreshold)) {
                        // End has been reached
                        // Do something
                        if (onLoadMoreListener != null) {
                            onLoadMoreListener.onLoadMore();
                        }
                        loading = true;
                    }
                }
            });

        }

    }


    @Override
    public int getItemViewType(int position) {
        return notificationItems.get(position) != null ? VIEW_ITEM : VIEW_PROG;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        if (viewType == VIEW_ITEM) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_notification, parent, false);

            vh = new NotificationHolder(v, notificationItems);

        } else {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.loading_footer_item, parent, false);

            vh = new ProgressViewHolder(v);
        }
        return vh;
    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof NotificationHolder) {

            NotificationItem item = notificationItems.get(position);

            if(item.getIsSeen())
              ((NotificationHolder) holder).layout.setBackgroundColor(Color.TRANSPARENT);
            else
                ((NotificationHolder) holder).layout.setBackgroundColor(Color.parseColor("#DEDCFF"));

            if (imageLoader == null)
                imageLoader = AppController.getInstance().getImageLoader();

            // user profile pic
            final String data[] = item.getData().split(AppConfig.OTP_DELIMITER);
            ((NotificationHolder) holder).profilePic.setImageUrl(
                    AppConfig.URL_PROFILE_PIC+data[0]+".png"+AppConfig.AUTO_REF_HACK(), imageLoader);
            ((NotificationHolder) holder).profilePic.setDefaultImageResId(R.drawable.ic_user);


            ((NotificationHolder) holder).timestamp.setText(item.getTimeStamp());

           /* Pattern atMentionPattern = Pattern.compile("@([A-Za-z0-9_]+)");
            String atMentionScheme = "mention://";
            TransformFilter transformFilter = new TransformFilter() {
                //skip the first character to filter out '@'
                public String transformUrl(final Matcher match, String url) {
                    return match.group(1);
                }
            };*/

            int i1;
            int i2;
            String nameLink;
            if(item.getType()==NOTIFICATION_FOLLOW) {
                nameLink = data[1];
                i1 = item.getMsg().indexOf(nameLink);
                i2 = i1 + nameLink.length();
            }
            else {
                nameLink = data[2];
                i1 = item.getMsg().indexOf(nameLink);
                i2 = i1 + nameLink.length();
            }


            ((NotificationHolder) holder).msg.setMovementMethod(LinkMovementMethod.getInstance());
            ((NotificationHolder) holder).msg.setText(item.getMsg(), TextView.BufferType.SPANNABLE);

            Spannable mySpannable = (Spannable)((NotificationHolder) holder).msg.getText();
            ClickableSpan myClickableSpan = new ClickableSpan()
            {
                @Override
                public void onClick(View widget) {
                    Intent intent = new Intent(context, ProfileActivity.class);
                    intent.putExtra("username", data[0]);
                    context.startActivity(intent);
                }
            };
            mySpannable.setSpan(myClickableSpan, i1, i2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            //Linkify.addLinks(((NotificationHolder) holder).msg, atMentionPattern, atMentionScheme, null, transformFilter);
        }
    }


    public void setLoaded() {
        loading = false;
    }

    @Override
    public int getItemCount() {
        return this.notificationItems.size();
    }

    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
        this.onLoadMoreListener = onLoadMoreListener;
    }

    public interface OnLoadMoreListener {
        void onLoadMore();
    }

}
