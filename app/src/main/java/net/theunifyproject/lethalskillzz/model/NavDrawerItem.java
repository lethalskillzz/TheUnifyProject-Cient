package net.theunifyproject.lethalskillzz.model;

/**
 * Created by Ibrahim on 15/10/2015.
 */
public class NavDrawerItem {
    private boolean isHeader;
    private String title;


    /*public NavDrawerItem() {

    }

    public NavDrawerItem(boolean showNotify, String title) {
        this.showNotify = showNotify;
        this.title = title;
    }*/

    public boolean getIsHeader() {
        return isHeader;
    }

    public void setIsHeader(boolean isHeader) {
        this.isHeader = isHeader;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
