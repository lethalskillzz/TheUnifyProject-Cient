package net.theunifyproject.lethalskillzz.holder;

import android.content.Intent;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.List;

import net.theunifyproject.lethalskillzz.R;
import net.theunifyproject.lethalskillzz.activity.DisplayShopActivity;
import net.theunifyproject.lethalskillzz.activity.EditShopActivity;
import net.theunifyproject.lethalskillzz.activity.ProfileActivity;
import net.theunifyproject.lethalskillzz.activity.StoreActivity;
import net.theunifyproject.lethalskillzz.activity.PDFActivity;
import net.theunifyproject.lethalskillzz.app.AppConfig;
import net.theunifyproject.lethalskillzz.app.PrefManager;
import net.theunifyproject.lethalskillzz.model.GridItem;
import net.theunifyproject.lethalskillzz.service.HttpService;
import net.theunifyproject.lethalskillzz.widget.FeedImageView;

/**
 * Created by Ibrahim on 12/12/2015.
 */
public class GridHolder  extends RecyclerView.ViewHolder implements View.OnClickListener {

    private List<GridItem> gridItems;
    private final int VIEW_ITEM_STORE = 4;
    private final int VIEW_ITEM_SHOP = 3;
    private final int VIEW_ITEM_DIGEST = 2;
    private final int VIEW_ITEM_REPO = 1;

    public TextView grid_title;
    public FeedImageView grid_image;

    public TextView shop_title, shop_price;
    public ImageButton shop_option;
    public FeedImageView shop_image;

    public TextView store_title, store_price;
    public ImageButton store_option;
    public FeedImageView store_image;


    public GridHolder(View convertView, List<GridItem> gridItems, int viewType) {
        super(convertView);
        this.gridItems = gridItems;

        itemView.setOnClickListener(this);

        if (viewType == VIEW_ITEM_SHOP) {

            shop_title = (TextView) convertView
                    .findViewById(R.id.shop_title);
            shop_price = (TextView) convertView
                    .findViewById(R.id.shop_price);
            shop_option = (ImageButton) convertView
                    .findViewById(R.id.shop_btnOption);
            shop_image = (FeedImageView) convertView
                    .findViewById(R.id.shop_img);

            shop_option.setOnClickListener(this);

        } else if (viewType == VIEW_ITEM_STORE) {

            store_title = (TextView) convertView
                    .findViewById(R.id.store_title);
            store_price = (TextView) convertView
                    .findViewById(R.id.store_price);
            store_option = (ImageButton) convertView
                    .findViewById(R.id.store_btnOption);
            store_image = (FeedImageView) convertView
                    .findViewById(R.id.store_img);

            store_option.setOnClickListener(this);

        } else {

            grid_title = (TextView) convertView
                    .findViewById(R.id.grid_label);
            grid_image = (FeedImageView) convertView
                    .findViewById(R.id.grid_img);

        }

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.shop_btnOption:
                clickOption(view);
                break;

            case R.id.store_btnOption:
                clickOption(view);
                break;

            default:
            clickItem(view);
                break;
        }
    }


    private void clickItem(View v) {
        final int position = getPosition();
        GridItem item = gridItems.get(position);

       if (item.getType() == VIEW_ITEM_SHOP || item.getType() == VIEW_ITEM_STORE) {

           String shopId = String.valueOf(item.getId());

           Intent intent = new Intent(v.getContext(), DisplayShopActivity.class);
           intent.putExtra("shopId", shopId);
           v.getContext().startActivity(intent);

       } else  {

           String type = String.valueOf(item.getType());
           String title = item.getTitle();
           String url = item.getUrl();

           Intent intent = new Intent(v.getContext(), PDFActivity.class);
           intent.putExtra("type", type);
           intent.putExtra("title", title);
           intent.putExtra("url", url);
           v.getContext().startActivity(intent);

       }
    }

    private void clickOption(final View v) {

        final int position = getPosition();
        GridItem item = gridItems.get(position);
        final String shopId = String.valueOf(item.getId());
        final String username = String.valueOf(item.getUsername());


        PopupMenu.OnMenuItemClickListener onMenuClick = new PopupMenu.OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.shop_popup_edit: {
                        Intent intent = new Intent(v.getContext(), EditShopActivity.class);
                        intent.putExtra("shopId", shopId);
                        v.getContext().startActivity(intent);
                    }
                    return true;

                    case R.id.shop_popup_delete: {
                        Intent intent = new Intent(v.getContext(), HttpService.class);
                        intent.putExtra("intent_type", AppConfig.httpIntentDeleteShop);
                        intent.putExtra("shopId", shopId);
                        v.getContext().startService(intent);
                    }
                    return true;

                    case R.id.shop_popup_goto_store: {
                        Intent intent = new Intent(v.getContext(), StoreActivity.class);
                        intent.putExtra("username", username);
                        v.getContext().startActivity(intent);
                    }
                    return true;

                    case R.id.shop_popup_goto_profile: {
                        Intent intent = new Intent(v.getContext(), ProfileActivity.class);
                        intent.putExtra("username", username);
                        v.getContext().startActivity(intent);
                    }
                    return true;

                    case R.id.shop_popup_report: {
                        Intent intent = new Intent(v.getContext(), HttpService.class);
                        intent.putExtra("intent_type", AppConfig.httpIntentReportShop);
                        intent.putExtra("shopId", shopId);
                        v.getContext().startService(intent);
                    }
                    return true;

                    default:
                        return false;
                }
            }
        };

        PopupMenu popupMenu = new PopupMenu(v.getContext(), v);
        popupMenu.setOnMenuItemClickListener(onMenuClick);

        PrefManager pref = new PrefManager(v.getContext());
        if (pref.getUsername().equals(item.getUsername()))
            popupMenu.inflate(R.menu.menu_my_shop_popup);
        else
            popupMenu.inflate(R.menu.menu_shop_popup);

        popupMenu.show();

    }

}
