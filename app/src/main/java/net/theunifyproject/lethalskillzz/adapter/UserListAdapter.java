package net.theunifyproject.lethalskillzz.adapter;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.android.volley.toolbox.ImageLoader;
import net.theunifyproject.lethalskillzz.R;
import net.theunifyproject.lethalskillzz.app.AppConfig;
import net.theunifyproject.lethalskillzz.app.AppController;
import net.theunifyproject.lethalskillzz.app.PrefManager;
import net.theunifyproject.lethalskillzz.holder.ProgressViewHolder;
import net.theunifyproject.lethalskillzz.holder.UserListHolder;
import net.theunifyproject.lethalskillzz.model.UserListItem;

import java.util.List;

/**
 * Created by Ibrahim on 14/11/2015.
 */
public class UserListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int VIEW_ITEM = 1;
    private final int VIEW_PROG = 0;

    private int visibleThreshold = 2;
    private int lastVisibleItem, totalItemCount;
    private boolean loading;
    private OnLoadMoreListener onLoadMoreListener;

    private Context context;
    private List<UserListItem> userListItems;
    ImageLoader imageLoader = AppController.getInstance().getImageLoader();

    public UserListAdapter(Context context, RecyclerView recyclerView, List<UserListItem> userListItems) {
        this.userListItems = userListItems;
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
        return userListItems.get(position) != null ? VIEW_ITEM : VIEW_PROG;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        if (viewType == VIEW_ITEM) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_user_list, parent, false);

            vh = new UserListHolder(v, userListItems);
        } else {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.loading_footer_item, parent, false);

            vh = new ProgressViewHolder(v);
        }
        return vh;
    }



    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof UserListHolder) {

            if (imageLoader == null)
                imageLoader = AppController.getInstance().getImageLoader();

            UserListItem item = userListItems.get(position);

            ((UserListHolder) holder).name.setText(item.getName());
            ((UserListHolder) holder).username.setText("@"+item.getUsername());

            ((UserListHolder) holder).info.setText(item.getInfo());


            // user profile pic
            //if(item.getProfilePic().length()!=0) {
                ((UserListHolder) holder).profilePic.setImageUrl(
                        AppConfig.URL_PROFILE_PIC + item.getUsername()+".png"+AppConfig.AUTO_REF_HACK(), imageLoader);
            //} else
                ((UserListHolder) holder).profilePic.setDefaultImageResId(R.drawable.ic_user);

            PrefManager pref = new PrefManager(context);
            if(!pref.getUsername().equals(item.getUsername())) {
                ((UserListHolder) holder).btn_follow.setVisibility(View.VISIBLE);
                if(item.getIsFollow()) {
                    ((UserListHolder) holder).btn_follow.setText(R.string.btn_unfollow);
                    ((UserListHolder) holder).btn_follow.setBackgroundResource(R.drawable.btn_unfollow_blue);
                }
                else {
                    ((UserListHolder) holder).btn_follow.setText(R.string.btn_follow);
                    ((UserListHolder) holder).btn_follow.setBackgroundResource(R.drawable.btn_follow_green);
                }
            }else ((UserListHolder) holder).btn_follow.setVisibility(View.GONE);


            if(item.getIsVerify())
                ((UserListHolder) holder).isVerify.setVisibility(View.VISIBLE);
            else ((UserListHolder) holder).isVerify.setVisibility(View.GONE);
        }
    }


    public void setLoaded() {
        loading = false;
    }

    @Override
    public int getItemCount() {
        return this.userListItems.size();
    }

    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
        this.onLoadMoreListener = onLoadMoreListener;
    }

    public interface OnLoadMoreListener {
        void onLoadMore();
    }


}

