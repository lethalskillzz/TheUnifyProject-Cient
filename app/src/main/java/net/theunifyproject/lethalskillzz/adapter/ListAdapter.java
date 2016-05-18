package net.theunifyproject.lethalskillzz.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import net.theunifyproject.lethalskillzz.R;
import net.theunifyproject.lethalskillzz.app.PrefManager;
import net.theunifyproject.lethalskillzz.holder.ListHolder;
import net.theunifyproject.lethalskillzz.model.ListItem;

/**
 * Created by Ibrahim on 28/12/2015.
 */
public class ListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<ListItem> listItems;
    private Context context;

    private final int VIEW_ITEM_LIST_CHECKBOX = 2;
    private final int VIEW_ITEM_LIST = 1;

    public ListAdapter(Context context, List<ListItem> listItems) {

        this.listItems = listItems;
        this.context = context;

    }

    @Override
    public int getItemViewType(int position) {
        if(listItems.get(position).getType()==VIEW_ITEM_LIST)
            return VIEW_ITEM_LIST;
        else
            return VIEW_ITEM_LIST_CHECKBOX;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = null;

        if(viewType==VIEW_ITEM_LIST) {
             v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_list, parent, false);
        } else if (viewType==VIEW_ITEM_LIST_CHECKBOX) {
            v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_list_checkbox, parent, false);
        }

        return new ListHolder(v, listItems,viewType);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        ListItem item = listItems.get(position);
        PrefManager pref = new PrefManager(context);

        if(item.getType()==VIEW_ITEM_LIST_CHECKBOX) {
            switch (item.getTitle()) {

                case "Show prompt": {
                    if(pref.getPrompt())
                        ((ListHolder) holder).chkbox.setChecked(true);
                    else
                        ((ListHolder) holder).chkbox.setChecked(false);
                }
                break;

                case "Sound": {
                    if(pref.getSound())
                        ((ListHolder) holder).chkbox.setChecked(true);
                    else
                        ((ListHolder) holder).chkbox.setChecked(false);
                }
                break;

                case "Vibrate": {
                    if(pref.getVibrate())
                        ((ListHolder) holder).chkbox.setChecked(true);
                    else
                        ((ListHolder) holder).chkbox.setChecked(false);
                }
                break;
            }
        }

        ((ListHolder) holder).title.setText(item.getTitle());
    }

    @Override
    public int getItemCount() {
        return listItems.size();
    }


}
