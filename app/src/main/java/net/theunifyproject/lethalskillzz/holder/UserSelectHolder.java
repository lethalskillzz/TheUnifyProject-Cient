package net.theunifyproject.lethalskillzz.holder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import net.android.volley.toolbox.RoundNetworkImageView;
import net.theunifyproject.lethalskillzz.R;
import net.theunifyproject.lethalskillzz.activity.IntroDiscoverActivity;
import net.theunifyproject.lethalskillzz.model.UserSelectItem;

/**
 * Created by Ibrahim on 21/12/2015.
 */
public class UserSelectHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private List<UserSelectItem> userSelectItems;

    public TextView name, username, info;
    public CheckBox chkBox;
    public ImageView isVerify;
    public RoundNetworkImageView profilePic;

    public UserSelectHolder(View convertView, List<UserSelectItem> userSelectItems) {
        super(convertView);
        this.userSelectItems = userSelectItems;

        itemView.setOnClickListener(this);

        name = (TextView) convertView
                .findViewById(R.id.user_select_name);
        username = (TextView) convertView
                .findViewById(R.id.user_select_username);
        info = (TextView) convertView
                .findViewById(R.id.user_select_info);
        profilePic = (RoundNetworkImageView) convertView
                .findViewById(R.id.user_select_profilePic);
        isVerify = (ImageView) convertView
                .findViewById(R.id.user_select_isVerify);
        chkBox = (CheckBox) convertView
                .findViewById(R.id.user_select_checkBox);

        name.setOnClickListener(this);
        profilePic.setOnClickListener(this);
        chkBox.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.user_select_checkBox:
                clickSelect(view);
                break;
        }
    }


    private void clickSelect(View v) {

        final int position = getPosition();
        UserSelectItem item = userSelectItems.get(position);

        if(item.getIsSelect()) {
            IntroDiscoverActivity.selectCount -=1;
            item.setIsSelect(false);
            chkBox.setChecked(false);
        }
        else {
            IntroDiscoverActivity.selectCount +=1;
            item.setIsSelect(true);
            chkBox.setChecked(true);
        }

        IntroDiscoverActivity.btn_follow.setText("Follow("+String.valueOf(IntroDiscoverActivity.selectCount)+")");
    }

}
