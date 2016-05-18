package net.theunifyproject.lethalskillzz.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import net.theunifyproject.lethalskillzz.R;
import net.theunifyproject.lethalskillzz.model.NavDrawerItem;
import net.theunifyproject.lethalskillzz.holder.NavDrawerHolder;

import java.util.Collections;
import java.util.List;

/**
 * Created by Ibrahim on 21/11/2015.
 */
public class NavDrawerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    List<NavDrawerItem> data = Collections.emptyList();
    private LayoutInflater inflater;
    private Context context;

    private final int VIEW_ROW = 1;
    private final int VIEW_HEADER = 0;

    public NavDrawerAdapter(Context context, List<NavDrawerItem> data) {
        this.context = context;
        inflater = LayoutInflater.from(context);
        this.data = data;
    }


    @Override
    public int getItemViewType(int position) {
        return data.get(position).getIsHeader()  ? VIEW_HEADER : VIEW_ROW;
    }

    public void delete(int position) {
        data.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        NavDrawerHolder holder;

        if (viewType == VIEW_ROW)
            holder = new NavDrawerHolder(inflater.inflate(R.layout.nav_drawer_row, parent, false), data, VIEW_ROW);
        else
            holder = new NavDrawerHolder(inflater.inflate(R.layout.nav_drawer_header, parent, false), data, VIEW_HEADER);

        return holder;
    }


    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        NavDrawerItem current = data.get(position);
        if (!current.getIsHeader()) {
            switch (current.getTitle()) {
                case "Phone contacts":
                    ((NavDrawerHolder) holder).icon.setImageResource(R.drawable.ic_search_mobile);
                    break;

                case "Profile":
                    ((NavDrawerHolder) holder).icon.setImageResource(R.drawable.ic_profile);
                    break;

                case "Account":
                    ((NavDrawerHolder) holder).icon.setImageResource(R.drawable.ic_privacy);
                    break;

                case "Store":
                    ((NavDrawerHolder) holder).icon.setImageResource(R.drawable.ic_mstore);
                    break;

                case "Notification":
                    ((NavDrawerHolder) holder).icon.setImageResource(R.drawable.ic_bell);
                    break;

                case "About":
                    ((NavDrawerHolder) holder).icon.setImageResource(R.drawable.ic_info);
                    break;

                case "FAQ":
                    ((NavDrawerHolder) holder).icon.setImageResource(R.drawable.ic_faq);
                    break;

                case "Logout":
                    ((NavDrawerHolder) holder).icon.setImageResource(R.drawable.ic_switch);
                    break;

            }
            ((NavDrawerHolder) holder).title.setText(current.getTitle());
        }else
            ((NavDrawerHolder) holder).header.setText(current.getTitle());
    }

    @Override
    public int getItemCount() {
        return data.size();
    }


}
