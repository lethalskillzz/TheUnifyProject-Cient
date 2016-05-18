package net.theunifyproject.lethalskillzz.adapter;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import net.android.volley.toolbox.ImageLoader;
import net.theunifyproject.lethalskillzz.R;
import net.theunifyproject.lethalskillzz.app.AppConfig;
import net.theunifyproject.lethalskillzz.app.AppController;
import net.theunifyproject.lethalskillzz.holder.ProgressViewHolder;
import net.theunifyproject.lethalskillzz.holder.UserSelectHolder;
import net.theunifyproject.lethalskillzz.model.UserSelectItem;

/**
 * Created by Ibrahim on 21/12/2015.
 */
public class UserSelectAdapter  extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int VIEW_ITEM = 1;
    private final int VIEW_PROG = 0;

    private int visibleThreshold = 2;
    private int lastVisibleItem, totalItemCount;
    private boolean loading;
    private OnLoadMoreListener onLoadMoreListener;

    private Context context;
    private List<UserSelectItem> userSelectItems;
    ImageLoader imageLoader = AppController.getInstance().getImageLoader();

    public UserSelectAdapter(Context context, RecyclerView recyclerView, List<UserSelectItem> userListItems) {
        this.userSelectItems = userListItems;
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
        return userSelectItems.get(position) != null ? VIEW_ITEM : VIEW_PROG;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        if (viewType == VIEW_ITEM) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_user_select, parent, false);

            vh = new UserSelectHolder(v, userSelectItems);
        } else {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.loading_footer_item, parent, false);

            vh = new ProgressViewHolder(v);
        }
        return vh;
    }



    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof UserSelectHolder) {

            if (imageLoader == null)
                imageLoader = AppController.getInstance().getImageLoader();

            UserSelectItem item = userSelectItems.get(position);

            ((UserSelectHolder) holder).name.setText(item.getName());
            ((UserSelectHolder) holder).username.setText("@"+item.getUsername());

            ((UserSelectHolder) holder).info.setText(item.getInfo());


            // user profile pic
            //if(item.getProfilePic().length()!=0) {
                ((UserSelectHolder) holder).profilePic.setImageUrl(
                        AppConfig.URL_PROFILE_PIC + item.getUsername()+".png"+AppConfig.AUTO_REF_HACK(), imageLoader);
            //} else
                ((UserSelectHolder) holder).profilePic.setDefaultImageResId(R.drawable.ic_user);


                if(item.getIsSelect()) {
                    ((UserSelectHolder) holder).chkBox.setChecked(true);
                }
                else {
                    ((UserSelectHolder) holder).chkBox.setChecked(false);
                }


            if(item.getIsVerify())
                ((UserSelectHolder) holder).isVerify.setVisibility(View.VISIBLE);
            else ((UserSelectHolder) holder).isVerify.setVisibility(View.GONE);
        }
    }


    public void setLoaded() {
        loading = false;
    }

    @Override
    public int getItemCount() {
        return this.userSelectItems.size();
    }

    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
        this.onLoadMoreListener = onLoadMoreListener;
    }

    public interface OnLoadMoreListener {
        void onLoadMore();
    }
}
