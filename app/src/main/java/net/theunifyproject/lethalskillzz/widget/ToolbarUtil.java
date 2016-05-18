package net.theunifyproject.lethalskillzz.widget;

import android.content.Context;
import android.content.res.TypedArray;

import net.theunifyproject.lethalskillzz.R;

/**
 * Created by Ibrahim on 09/12/2015.
 */

public class ToolbarUtil {
    public static int getToolbarHeight(Context context) {
        final TypedArray styledAttributes = context.getTheme().obtainStyledAttributes(
                new int[]{R.attr.actionBarSize});
        int toolbarHeight = (int) styledAttributes.getDimension(0, 0);
        styledAttributes.recycle();
        return toolbarHeight;
    }
    /*public static int getTabsHeight(Context context) {
        return (int) context.getResources().getDimension(R.dimen.tabsHeight);
    }*/
}
