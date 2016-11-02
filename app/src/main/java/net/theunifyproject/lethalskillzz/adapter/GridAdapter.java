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
import net.theunifyproject.lethalskillzz.holder.GridHolder;
import net.theunifyproject.lethalskillzz.holder.ProgressViewHolder;
import net.theunifyproject.lethalskillzz.model.GridItem;
import net.theunifyproject.lethalskillzz.widget.FeedImageView;

/**
 * Created by Ibrahim on 12/12/2015.
 */
public class GridAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<GridItem> gridItems;
    private Context context;
    ImageLoader imageLoader = AppController.getInstance().getImageLoader();
    
    private final int VIEW_ITEM_STORE = 4;
    private final int VIEW_ITEM_SHOP = 3;
    private final int VIEW_ITEM_DIGEST = 2;
    private final int VIEW_ITEM_REPO = 1;
    private final int VIEW_PROG = 0;

    private int visibleThreshold = 2;
    private int lastVisibleItem, totalItemCount;
    private boolean loading;
    private OnLoadMoreListener onLoadMoreListener;


    public GridAdapter(Context context, RecyclerView recyclerView, List<GridItem> gridItems) {

        this.gridItems = gridItems;
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

        if(gridItems.get(position) != null) {
            switch (gridItems.get(position).getType()) {

                case VIEW_ITEM_REPO:
                    viewType = VIEW_ITEM_REPO;
                    break;

                case VIEW_ITEM_DIGEST:
                    viewType= VIEW_ITEM_DIGEST;
                    break;

                case VIEW_ITEM_SHOP:
                    viewType = VIEW_ITEM_SHOP;
                    break;

                case VIEW_ITEM_STORE:
                    viewType = VIEW_ITEM_STORE;
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

            case VIEW_ITEM_REPO: {
                View v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_grid, parent, false);

                vh = new GridHolder(v,gridItems,VIEW_ITEM_REPO);
            }
            break;

            case VIEW_ITEM_DIGEST: {
                View v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_grid, parent, false);

                vh = new GridHolder(v,gridItems,VIEW_ITEM_DIGEST);
            }
            break;

            case VIEW_ITEM_SHOP: {
                View v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_shop, parent, false);

                vh = new GridHolder(v,gridItems,VIEW_ITEM_SHOP);
            }
            break;

            case VIEW_ITEM_STORE: {
                View v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_store, parent, false);

                vh = new GridHolder(v,gridItems,VIEW_ITEM_STORE);
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

        if (holder instanceof GridHolder) {

            if (imageLoader == null)
                imageLoader = AppController.getInstance().getImageLoader();

            GridItem item = gridItems.get(position);

            if (item.getType() == VIEW_ITEM_SHOP) {

                ((GridHolder) holder).shop_title.setText(item.getTitle());
                ((GridHolder) holder).shop_price.setText(item.getPrice());

                    ((GridHolder) holder).shop_image.setDefaultImageResId(R.drawable.ic_image);
                //if (item.getImage().length()>0) {
                    ((GridHolder) holder).shop_image.setImageUrl(item.getImage() + AppConfig.AUTO_REF_HACK(), imageLoader);
                    ((GridHolder) holder).shop_image
                            .setResponseObserver(new FeedImageView.ResponseObserver() {
                                @Override
                                public void onError() {
                                }

                                @Override
                                public void onSuccess() {
                                }
                            });
               // } else
                //    ((GridHolder) holder).shop_image.setDefaultImageResId(R.drawable.ic_image);

            } else if (item.getType() == VIEW_ITEM_STORE) {

                ((GridHolder) holder).store_title.setText(item.getTitle());
                ((GridHolder) holder).store_price.setText(item.getPrice());


                ((GridHolder) holder).store_image.setDefaultImageResId(R.drawable.ic_image);
                //if (item.getImage().length()>0) {
                    ((GridHolder) holder).store_image.setImageUrl(item.getImage() + AppConfig.AUTO_REF_HACK(), imageLoader);
                    ((GridHolder) holder).store_image
                            .setResponseObserver(new FeedImageView.ResponseObserver() {
                                @Override
                                public void onError() {
                                }

                                @Override
                                public void onSuccess() {
                                }
                            });
                //} else
                    //((GridHolder) holder).store_image.setDefaultImageResId(R.drawable.ic_image);


            } else if (item.getType() == VIEW_ITEM_REPO) {

                ((GridHolder) holder).grid_title.setText(item.getTitle());

                ((GridHolder) holder).grid_image.setDefaultImageResId(R.drawable.ic_pdf);
                //if (item.getImage().length()>0) {
                    ((GridHolder) holder).grid_image.setImageUrl(item.getImage() + AppConfig.AUTO_REF_HACK(), imageLoader);
                    ((GridHolder) holder).grid_image
                            .setResponseObserver(new FeedImageView.ResponseObserver() {
                                @Override
                                public void onError() {
                                    // ((GridHolder) holder).grid_image.setImageResource(R.drawable.ic_image);
                                }

                                @Override
                                public void onSuccess() {
                                }
                            });
                } else {

                ((GridHolder) holder).grid_title.setText(item.getTitle());

                ((GridHolder) holder).grid_image.setDefaultImageResId(R.drawable.ic_image);
                //if (item.getImage().length()>0) {
                ((GridHolder) holder).grid_image.setImageUrl(item.getImage() + AppConfig.AUTO_REF_HACK(), imageLoader);
                ((GridHolder) holder).grid_image
                        .setResponseObserver(new FeedImageView.ResponseObserver() {
                            @Override
                            public void onError() {
                                // ((GridHolder) holder).grid_image.setImageResource(R.drawable.ic_image);
                            }

                            @Override
                            public void onSuccess() {
                            }
                        });
             }
        }

    }

    public void setLoaded() {
        loading = false;
    }

    @Override
    public int getItemCount() {
        return this.gridItems.size();
    }

    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
        this.onLoadMoreListener = onLoadMoreListener;
    }

    public interface OnLoadMoreListener {
        void onLoadMore();
    }

}
