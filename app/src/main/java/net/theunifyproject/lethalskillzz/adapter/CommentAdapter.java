package net.theunifyproject.lethalskillzz.adapter;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import android.text.util.Linkify;
import android.text.util.Linkify.TransformFilter;


import net.android.volley.toolbox.ImageLoader;
import net.theunifyproject.lethalskillzz.R;
import net.theunifyproject.lethalskillzz.app.AppConfig;
import net.theunifyproject.lethalskillzz.app.AppController;
import net.theunifyproject.lethalskillzz.holder.CommentHolder;
import net.theunifyproject.lethalskillzz.holder.ProgressViewHolder;
import net.theunifyproject.lethalskillzz.model.CommentItem;

/**
 * Created by Ibrahim on 29/11/2015.
 */
public class CommentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>  {

    private List<CommentItem> commentItems;
    private Context context;
    ImageLoader imageLoader = AppController.getInstance().getImageLoader();

    private final int VIEW_ITEM = 1;
    private final int VIEW_PROG = 0;

    private int visibleThreshold = 2;
    private int lastVisibleItem, totalItemCount;
    private boolean loading;
    private OnLoadMoreListener onLoadMoreListener;

    public CommentAdapter(Context context, RecyclerView recyclerView, List<CommentItem> commentItems) {

        this.commentItems = commentItems;
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
        return commentItems.get(position) != null ? VIEW_ITEM : VIEW_PROG;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        if (viewType == VIEW_ITEM) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_comment, parent, false);

            vh = new CommentHolder(v,commentItems);

        } else {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.loading_footer_item, parent, false);

            vh = new ProgressViewHolder(v);
        }
        return vh;
    }



    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof CommentHolder) {

            if (imageLoader == null)
                imageLoader = AppController.getInstance().getImageLoader();

            CommentItem item = commentItems.get(position);

            ((CommentHolder) holder).name.setText(item.getName());
            ((CommentHolder) holder).username.setText("@"+item.getUsername());

            ((CommentHolder) holder).timestamp.setText(item.getTimeStamp());

            // Chcek for empty status message
            if (!TextUtils.isEmpty(item.getComment())) {

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

                ((CommentHolder) holder).commentMsg.setText(item.getComment());
                ((CommentHolder) holder).commentMsg.setVisibility(View.VISIBLE);

                Linkify.addLinks(((CommentHolder) holder).commentMsg, Linkify.ALL);
                Linkify.addLinks(((CommentHolder) holder).commentMsg, atMentionPattern, atMentionScheme, null, transformFilter);
                Linkify.addLinks(((CommentHolder) holder).commentMsg, HashPattern, HashScheme, null, transformFilter);

                //StripUnderline.stripUnderlines(((FeedHolder) holder).statusMsg);
            } else {
                // status is empty, remove from view
                ((CommentHolder) holder).commentMsg.setVisibility(View.GONE);
            }

            // user profile pic
            //if(item.getProfilePic().length()>0) {
                ((CommentHolder) holder).profilePic.setImageUrl(AppConfig.URL_PROFILE_PIC + item.getUsername()+".png"+AppConfig.AUTO_REF_HACK(), imageLoader);
            //} else
                ((CommentHolder) holder).profilePic.setDefaultImageResId(R.drawable.ic_user);


            if(item.getIsVerify())
                ((CommentHolder) holder).isVerify.setVisibility(View.VISIBLE);
            else ((CommentHolder) holder).isVerify.setVisibility(View.GONE);



        } else {
            ((ProgressViewHolder) holder).progressBar.setIndeterminate(true);
        }
    }

    public void setLoaded() {
        loading = false;
    }

    @Override
    public int getItemCount() {
        return this.commentItems.size();
    }

    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
        this.onLoadMoreListener = onLoadMoreListener;
    }

    public interface OnLoadMoreListener {
        void onLoadMore();
    }



}
