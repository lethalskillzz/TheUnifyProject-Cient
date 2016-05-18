package net.theunifyproject.lethalskillzz.adapter;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.android.volley.toolbox.ImageLoader;
import net.theunifyproject.lethalskillzz.R;
import net.theunifyproject.lethalskillzz.app.AppConfig;
import net.theunifyproject.lethalskillzz.app.AppController;
import net.theunifyproject.lethalskillzz.app.PrefManager;
import net.theunifyproject.lethalskillzz.util.StripUnderline;
import net.theunifyproject.lethalskillzz.holder.MultiItemHolder;
import net.theunifyproject.lethalskillzz.holder.ProgressViewHolder;
import net.theunifyproject.lethalskillzz.model.MultiItem;
import net.theunifyproject.lethalskillzz.widget.FeedImageView;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Ibrahim on 12/11/2015.
 */
public class MultiItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>   {

    private Context context;
    private List<MultiItem> multiItems;
    ImageLoader imageLoader = AppController.getInstance().getImageLoader();


    private final int VIEW_ITEM_HASH = 3;
    private final int VIEW_ITEM_FEED = 2;
    private final int VIEW_ITEM_USER = 1;
    private final int VIEW_PROG = 0;
    
    private int visibleThreshold = 2;
    private int lastVisibleItem, totalItemCount;
    private boolean loading;
    private OnLoadMoreListener onLoadMoreListener;


    public MultiItemAdapter(Context context, RecyclerView recyclerView, List<MultiItem> multiItems) {
        this.multiItems = multiItems;
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
        //return multiItems.get(position) != null ? VIEW_ITEM : VIEW_PROG;
        int viewType = 0;

        if(multiItems.get(position) != null) {
            switch (multiItems.get(position).getType()) {
              
                case VIEW_ITEM_USER:
                    viewType = VIEW_ITEM_USER;
                    break;

                case VIEW_ITEM_FEED:
                    viewType= VIEW_ITEM_FEED;
                    break;

                case VIEW_ITEM_HASH:
                    viewType = VIEW_ITEM_HASH;
                    break;
            }
        }else {
            viewType = VIEW_PROG;
        }
        
        return viewType;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        RecyclerView.ViewHolder vh = null;

        switch (viewType) {

            case VIEW_ITEM_USER: {
                View v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_user, parent, false);

                vh = new MultiItemHolder(v,multiItems,VIEW_ITEM_USER);
            }
                break;

            case VIEW_ITEM_FEED: {
                View v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_feed, parent, false);

                vh = new MultiItemHolder(v,multiItems,VIEW_ITEM_FEED);
            }
                break;

            case VIEW_ITEM_HASH: {
                View v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_hash, parent, false);

                vh = new MultiItemHolder(v,multiItems,VIEW_ITEM_HASH);
            }
                break;

            case VIEW_PROG: {
                View v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.loading_footer_item, parent, false);

                vh = new ProgressViewHolder(v);
            }
                break;

        }

        return vh;
    }



    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof MultiItemHolder) {
            final MultiItem item = multiItems.get(position);
            switch (item.getType()) {

                case VIEW_ITEM_USER: {

                    if (imageLoader == null)
                        imageLoader = AppController.getInstance().getImageLoader();

                    // user profile pic
                    //if(item.getProfilePic().length()>0) {
                        ((MultiItemHolder) holder).user_profilePic.setImageUrl(
                                AppConfig.URL_PROFILE_PIC + item.getUsername()+".png"+AppConfig.AUTO_REF_HACK(), imageLoader);
                    //} else
                         ((MultiItemHolder) holder).user_profilePic.setDefaultImageResId(R.drawable.ic_user);

                    ((MultiItemHolder) holder).user_name.setText(item.getName());
                    ((MultiItemHolder) holder).user_username.setText("@"+item.getUsername());

                    ((MultiItemHolder) holder).user_info.setText(item.getInfo());

                    PrefManager pref = new PrefManager(context);
                    if(!pref.getUsername().equals(item.getUsername())) {

                        ((MultiItemHolder) holder).user_btn_follow.setVisibility(View.VISIBLE);
                        if(item.getIsFollow()) {
                            ((MultiItemHolder) holder).user_btn_follow.setText(R.string.btn_unfollow);
                            ((MultiItemHolder) holder).user_btn_follow.setBackgroundResource(R.drawable.btn_unfollow_blue);
                        }
                        else {
                            ((MultiItemHolder) holder).user_btn_follow.setText(R.string.btn_follow);
                            ((MultiItemHolder) holder).user_btn_follow.setBackgroundResource(R.drawable.btn_follow_green);
                        }
                    }else ((MultiItemHolder) holder).user_btn_follow.setVisibility(View.GONE);


                    if(item.getIsVerify())
                        ((MultiItemHolder) holder).user_isVerify.setVisibility(View.VISIBLE);
                    else ((MultiItemHolder) holder).user_isVerify.setVisibility(View.GONE);


                }
                break;

                case VIEW_ITEM_FEED: {
                    // user profile pic
                    //if(item.getProfilePic().length()>0) {
                        ((MultiItemHolder) holder).feed_profilePic.setImageUrl(
                                AppConfig.URL_PROFILE_PIC + item.getUsername()+".png"+AppConfig.AUTO_REF_HACK(), imageLoader);
                   // } else
                        ((MultiItemHolder) holder).feed_profilePic.setDefaultImageResId(R.drawable.ic_user);

                    ((MultiItemHolder) holder).feed_name.setText(item.getName());
                    ((MultiItemHolder) holder).feed_username.setText("@"+item.getUsername());


                    ((MultiItemHolder) holder).feed_timestamp.setText(item.getTimeStamp());


                    // Check for empty status message
                    if (!TextUtils.isEmpty(item.getStatus())) {

                        Pattern atMentionPattern = Pattern.compile("@([A-Za-z0-9_]+)");
                        String atMentionScheme = "mention://";

                        Pattern HashPattern = Pattern.compile("#([A-Za-z0-9_]+)");
                        String HashScheme = "hash://";

                        Linkify.TransformFilter transformFilter = new Linkify.TransformFilter() {
                            //skip the first character to filter out '@'
                            public String transformUrl(final Matcher match, String url) {
                                return match.group(1);
                            }
                        };

                        ((MultiItemHolder) holder).feed_statusMsg.setText(item.getStatus());
                        ((MultiItemHolder) holder).feed_statusMsg.setVisibility(View.VISIBLE);

                        Linkify.addLinks(((MultiItemHolder) holder).feed_statusMsg, Linkify.ALL);
                        Linkify.addLinks(((MultiItemHolder) holder).feed_statusMsg, atMentionPattern, atMentionScheme, null, transformFilter);
                        Linkify.addLinks(((MultiItemHolder) holder).feed_statusMsg, HashPattern, HashScheme, null, transformFilter);

                        //StripUnderline.stripUnderlines(((MultiItemHolder) holder).feed_statusMsg);
                    } else {
                        // status is empty, remove from view
                        ((MultiItemHolder) holder).feed_statusMsg.setVisibility(View.GONE);
                    }


                    // Feed image
                    if (item.getImage().length()>0) {
                        ((MultiItemHolder) holder).feed_feedImageView.setImageUrl(item.getImage()+AppConfig.AUTO_REF_HACK(), imageLoader);
                        ((MultiItemHolder) holder).feed_feedImageView.setVisibility(View.VISIBLE);
                        ((MultiItemHolder) holder).feed_feedImageView
                                .setResponseObserver(new FeedImageView.ResponseObserver() {
                                    @Override
                                    public void onError() {
                                        ((MultiItemHolder) holder).feed_feedImageView.setImageResource(R.drawable.ic_image);
                                    }

                                    @Override
                                    public void onSuccess() {
                                        if(item.getIsExpand()) {
                                            ((MultiItemHolder) holder).feed_feedImageView.setAdjustViewBounds(true);
                                            ((MultiItemHolder) holder).feed_feedImageView.setScaleType(FeedImageView.ScaleType.FIT_XY);
                                        }else {
                                            ((MultiItemHolder) holder).feed_feedImageView.setAdjustViewBounds(false);
                                            ((MultiItemHolder) holder).feed_feedImageView.setScaleType(FeedImageView.ScaleType.CENTER_CROP);
                                        }
                                    }
                                });
                    } else {
                        ((MultiItemHolder) holder).feed_feedImageView.setVisibility(View.GONE);
                    }

                    if(item.getIsLike()) {
                        ((MultiItemHolder) holder).feed_btnLike.setImageResource(R.mipmap.ic_heart_red);
                    }else {
                        ((MultiItemHolder) holder).feed_btnLike.setImageResource(R.mipmap.ic_heart_outline_grey);
                    }

                    ((MultiItemHolder) holder).feed_likeCount.setText(item.getLikeCount());
                    ((MultiItemHolder) holder).feed_commentCount.setText(item.getCommentCount());


                    if(item.getIsVerify())
                        ((MultiItemHolder) holder).feed_isVerify.setVisibility(View.VISIBLE);
                    else ((MultiItemHolder) holder).feed_isVerify.setVisibility(View.GONE);

                }

                break;

                case VIEW_ITEM_HASH: {

                    Pattern HashPattern = Pattern.compile("#([A-Za-z0-9_]+)");
                    String HashScheme = "hash://";

                    Linkify.TransformFilter transformFilter = new Linkify.TransformFilter() {
                        //skip the first character to filter out '@'
                        public String transformUrl(final Matcher match, String url) {
                            return match.group(1);
                        }
                    };

                    ((MultiItemHolder) holder).hash_tag.setText(item.getHash());

                    Linkify.addLinks(((MultiItemHolder) holder).hash_tag, HashPattern, HashScheme, null, transformFilter);
                    StripUnderline.stripUnderlines(((MultiItemHolder) holder).hash_tag);

                    if(Integer.parseInt(item.getCount())>1) {
                        ((MultiItemHolder) holder).hash_count.setText(item.getCount() + " people talking about this");
                    }else
                        ((MultiItemHolder) holder).hash_count.setText(item.getCount() + " person talking about this");

                }
                break;

            }
        }
    }

    public void setLoaded() {
        loading = false;
    }

    @Override
    public int getItemCount() {
        return this.multiItems.size();
    }

    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
        this.onLoadMoreListener = onLoadMoreListener;
    }

    public interface OnLoadMoreListener {
        void onLoadMore();
    }




}
