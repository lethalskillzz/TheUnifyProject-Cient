package net.theunifyproject.lethalskillzz.holder;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

import net.theunifyproject.lethalskillzz.R;
import net.theunifyproject.lethalskillzz.activity.ChangeNumberActivity;
import net.theunifyproject.lethalskillzz.activity.ChangePasswordActivity;
import net.theunifyproject.lethalskillzz.app.PrefManager;
import net.theunifyproject.lethalskillzz.model.ListItem;

/**
 * Created by Ibrahim on 28/12/2015.
 */
public class ListHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private final int VIEW_ITEM_LIST_CHECKBOX = 2;
    private final int VIEW_ITEM_LIST = 1;

    public List<ListItem> listItems;
    public TextView title;
    private RelativeLayout layout;
    public CheckBox chkbox;

    public ListHolder(View convertView, List<ListItem> listItems, int viewType) {
        super(convertView);
        this.listItems = listItems;
        itemView.setOnClickListener(this);

        if(viewType==VIEW_ITEM_LIST) {

            title = (TextView) convertView
                    .findViewById(R.id.list_label);

            layout = (RelativeLayout) convertView
                    .findViewById(R.id.list_layout);

            layout.setOnClickListener(this);

        } else if(viewType==VIEW_ITEM_LIST_CHECKBOX) {

            title = (TextView) convertView
                    .findViewById(R.id.list_checkbox_label);

            layout = (RelativeLayout) convertView
                    .findViewById(R.id.list_checkbox_layout);

            chkbox = (CheckBox) convertView
                    .findViewById(R.id.list_checkbox);

            chkbox.setOnClickListener(this);
            layout.setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.list_layout:
                clickItem(view);
                break;

            case R.id.list_checkbox:
                clickCheckBox(view);
                break;

            default:
                break;
        }
    }

    private void clickCheckBox(View v) {
        PrefManager pref = new PrefManager(v.getContext());
        final int position = getPosition();
        ListItem item = listItems.get(position);
        String title = item.getTitle();

        switch (title) {

            case "Show prompt": {
                if(chkbox.isChecked())
                    pref.setPrompt(true);
                else
                    pref.setPrompt(false);
            }
            break;

            case "Sound": {
                if(chkbox.isChecked())
                    pref.setSound(true);
                else
                    pref.setSound(false);
            }
            break;

            case "Vibrate": {
                if(chkbox.isChecked())
                    pref.setVibrate(true);
                else
                    pref.setVibrate(false);
            }
            break;

        }
    }

    private void clickItem(View v) {
        final int position = getPosition();
        ListItem item = listItems.get(position);
        String title = item.getTitle();

        switch (title) {

            case "Change phone number": {
                Intent intent = new Intent(v.getContext(), ChangeNumberActivity.class);
                v.getContext().startActivity(intent);
            }
            break;

            case "Privacy": {

            }
            break;

            case "Change password": {
                Intent intent = new Intent(v.getContext(), ChangePasswordActivity.class);
                v.getContext().startActivity(intent);
            }
            break;
        }
    }
}
