package net.theunifyproject.lethalskillzz.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import net.theunifyproject.lethalskillzz.R;
import net.theunifyproject.lethalskillzz.model.ListPopupItem;

/**
 * Created by Ibrahim on 07/01/2016.
 */
public class ListPopupAdapter extends ArrayAdapter<ListPopupItem> {

    private Context context;
    private List<ListPopupItem> listPopupItems;

    private final int TAG_MENTION = 1;
    private final int TAG_HASH = 2;

    public ListPopupAdapter(Context context,int textViewResourceId, List<ListPopupItem> listPopupItems) {
        super(context, textViewResourceId, listPopupItems);

        this.listPopupItems = listPopupItems;
        this.context = context;

    }

    public int getCount() {
        return listPopupItems.size();
    }

    public long getItemId(int position) {
        return position;
    }

    public ListPopupItem getCurrentPerson(int position) {
        return listPopupItems.get(position);
    }

    public void removeItem(int position) {
        listPopupItems.remove(position);
    }

    public static class ViewHolder {
        public TextView title, subTitle;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View vi = convertView;
        ViewHolder holder;
        if (convertView == null) {
            vi = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_popup, null);

            holder = new ViewHolder();
            holder.title = (TextView) vi.findViewById(R.id.list_popup_title);
            holder.subTitle = (TextView) vi.findViewById(R.id.list_popup_subTitle);

            vi.setTag(holder);
        } else {
            holder = (ViewHolder) vi.getTag();
        }

        ListPopupItem item = listPopupItems.get(position);


        if (item.getTitle() != null) {
            holder.title.setText(item.getTitle());
        }

        if (item.getSubTitle() != null) {
            if(item.getType()==TAG_MENTION)
                holder.subTitle.setText("@"+item.getSubTitle());
            else
                holder.subTitle.setText(item.getSubTitle()+" posts");
        }

        return vi;
    }

}
