package net.theunifyproject.lethalskillzz.holder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import net.theunifyproject.lethalskillzz.R;
import net.theunifyproject.lethalskillzz.model.NavDrawerItem;

/**
 * Created by Ibrahim on 17/12/2015.
 */
public class NavDrawerHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private List<NavDrawerItem> navDrawerItems;
    private final int VIEW_ROW = 1;
    private final int VIEW_HEADER = 0;

    public ImageView icon;
    public TextView title;
    public TextView header;

    public NavDrawerHolder(View itemView, List<NavDrawerItem> navDrawerItems, int viewType) {
        super(itemView);
        this.navDrawerItems = navDrawerItems;

        if(viewType == VIEW_ROW) {
            icon = (ImageView) itemView.findViewById(R.id.nav_icon);
            title = (TextView) itemView.findViewById(R.id.nav_title);
        }else {
            header = (TextView) itemView.findViewById(R.id.nav_header);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            default:
                break;
        }
    }


}
