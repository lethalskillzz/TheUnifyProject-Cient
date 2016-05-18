package net.theunifyproject.lethalskillzz.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import net.theunifyproject.lethalskillzz.R;
import net.theunifyproject.lethalskillzz.model.CustomSpinnerItem;

/**
 * Created by Ibrahim on 18/12/2015.
 */
public class CustomSpinnerAdapter extends ArrayAdapter<CustomSpinnerItem> {

    private Context context;
    private List<CustomSpinnerItem> customSpinnerItems;

    public CustomSpinnerAdapter( Context context, int textViewResourceId,
                                 List<CustomSpinnerItem> customSpinnerItems) {

        super(context, textViewResourceId, customSpinnerItems);

        /********** Take passed values **********/
        this.context = context;
        this.customSpinnerItems = customSpinnerItems;

    }

    @Override
    public View getDropDownView(int position, View convertView,ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }


    // This funtion called for each row ( Called data.size() times )
    public View getCustomView(int position, View convertView, ViewGroup parent) {

        /********** Inflate spinner_rows.xml file for each row ( Defined below ) ************/
        View row = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_custom_spinner, parent, false);

        /***** Get each Model object from Arraylist ********/
        CustomSpinnerItem item = (CustomSpinnerItem) customSpinnerItems.get(position);

        ImageView icon = (ImageView)row.findViewById(R.id.custom_spinner_icon);
        TextView title = (TextView)row.findViewById(R.id.custom_spinner_title);

        icon.setImageResource(item.getIcon());
        title.setText(item.getTitle());


        return row;
    }
}
