package net.theunifyproject.lethalskillzz.adapter;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.util.Linkify;
import android.text.util.Linkify.TransformFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.android.volley.toolbox.ImageLoader;
import net.theunifyproject.lethalskillzz.R;
import net.theunifyproject.lethalskillzz.app.AppConfig;
import net.theunifyproject.lethalskillzz.app.AppController;
import net.theunifyproject.lethalskillzz.holder.FeedHolder;
import net.theunifyproject.lethalskillzz.holder.ProgressViewHolder;
import net.theunifyproject.lethalskillzz.model.FeedItem;
import net.theunifyproject.lethalskillzz.widget.FeedImageView;

/**
 * Created by Ibrahim on 31/10/2015.
 */
public class FeedAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<FeedItem> feedItems;
    private Context context;

    private final int VIEW_ITEM = 1;
    private final int VIEW_PROG = 0;

    private int visibleThreshold = 2;
    private int lastVisibleItem, totalItemCount;
    private boolean loading;
    private OnLoadMoreListener onLoadMoreListener;

    ImageLoader imageLoader = AppController.getInstance().getImageLoader();


    public FeedAdapter(Context context, RecyclerView recyclerView, List<FeedItem> feedItems) {

        this.feedItems = feedItems;
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
        return feedItems.get(position) != null ? VIEW_ITEM : VIEW_PROG;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        if (viewType == VIEW_ITEM) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_feed, parent, false);

            vh = new FeedHolder(v, feedItems);

        } else {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.loading_footer_item, parent, false);

            vh = new ProgressViewHolder(v);
        }
        return vh;
    }



    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof FeedHolder) {

            if (imageLoader == null)
                imageLoader = AppController.getInstance().getImageLoader();


            final FeedItem item = feedItems.get(position);

            ((FeedHolder) holder).name.setText(item.getName());
            ((FeedHolder) holder).username.setText("@"+item.getUsername());

            ((FeedHolder) holder).timestamp.setText(item.getTimeStamp());

            // Chcek for empty status message
            if (!TextUtils.isEmpty(item.getStatus())) {

                Pattern atMentionPattern = Pattern.compile("@([A-Za-z0-9_]+)");
                String atMentionScheme = "mention://";

                Pattern HashPattern = Pattern.compile("#([A-Za-z0-9_]+)");
                String HashScheme = "hash://";

                TransformFilter transformFilter = new TransformFilter() {
                    //skip the first character to filter out '@'
                    public String transformUrl(final Matcher match, String url) {
                        return match.group(1);
                    }
                };

                ((FeedHolder) holder).statusMsg.setText(item.getStatus());
                ((FeedHolder) holder).statusMsg.setVisibility(View.VISIBLE);

                Linkify.addLinks(((FeedHolder) holder).statusMsg, Linkify.ALL);
                Linkify.addLinks(((FeedHolder) holder).statusMsg, atMentionPattern, atMentionScheme, null, transformFilter);
                Linkify.addLinks(((FeedHolder) holder).statusMsg, HashPattern, HashScheme, null, transformFilter);

                //StripUnderline.stripUnderlines(((FeedHolder) holder).statusMsg);
            } else {
                // status is empty, remove from view
                ((FeedHolder) holder).statusMsg.setVisibility(View.GONE);
            }

            // user profile pic
            ((FeedHolder) holder).profilePic.setDefaultImageResId(R.drawable.ic_user);
            //if(item.getProfilePic().length()>0) {
                ((FeedHolder) holder).profilePic.setImageUrl(AppConfig.URL_PROFILE_PIC + item.getUsername()+".png"+AppConfig.AUTO_REF_HACK(), imageLoader);
            //}

            // Feed image
            if (item.getImage().length()>0) {
                ((FeedHolder) holder).feedImageView.setImageUrl(item.getImage()+AppConfig.AUTO_REF_HACK(), imageLoader);
                ((FeedHolder) holder).feedImageView.setVisibility(View.VISIBLE);
                ((FeedHolder) holder).feedImageView
                        .setResponseObserver(new FeedImageView.ResponseObserver() {
                            @Override
                            public void onError() {
                                ((FeedHolder) holder).feedImageView.setImageResource(R.drawable.ic_image);
                            }

                            @Override
                            public void onSuccess() {
                                if(item.getIsExpand()) {
                                    ((FeedHolder) holder).feedImageView.setAdjustViewBounds(true);
                                    ((FeedHolder) holder).feedImageView.setScaleType(FeedImageView.ScaleType.FIT_XY);
                                }else {
                                    ((FeedHolder) holder).feedImageView.setAdjustViewBounds(false);
                                    ((FeedHolder) holder).feedImageView.setScaleType(FeedImageView.ScaleType.CENTER_CROP);
                                }

                            }
                        });


            } else {
                ((FeedHolder) holder).feedImageView.setVisibility(View.GONE);
            }

            if(item.getIsLike()) {
                ((FeedHolder) holder).btnLike.setImageResource(R.mipmap.ic_heart_red);
            }else {
                ((FeedHolder) holder).btnLike.setImageResource(R.mipmap.ic_heart_outline_grey);
            }



            ((FeedHolder) holder).likeCount.setText(item.getLikeCount());
            ((FeedHolder) holder).commentCount.setText(item.getCommentCount());


            if(item.getIsVerify())
                ((FeedHolder) holder).isVerify.setVisibility(View.VISIBLE);
            else ((FeedHolder) holder).isVerify.setVisibility(View.GONE);


        } else {
            ((ProgressViewHolder) holder).progressBar.setIndeterminate(true);
        }
    }

    public void setLoaded() {
        loading = false;
    }

    @Override
    public int getItemCount() {
        return this.feedItems.size();
    }

    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
        this.onLoadMoreListener = onLoadMoreListener;
    }

    public interface OnLoadMoreListener {
        void onLoadMore();
    }

}
