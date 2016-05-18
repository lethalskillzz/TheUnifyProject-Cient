package net.theunifyproject.lethalskillzz.model;

/**
 * Created by Ibrahim on 14/11/2015.
 */
public class UserListItem {

    private String name, username, info;
    private boolean isVerify, isFollow;

    /*public UserListItem() {
    }

    public UserListItem(String uid, String name, boolean isVerify, String username, String profilePic,
                        String info, boolean isFollow) {
        super();
        this.uid = uid;
        this.name = name;
        this.isVerify = isVerify;
        this.username = username;
        this.profilePic = profilePic;
        this.info = info;
        this.isFollow = isFollow;
    }*/


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getIsVerify() {
        return isVerify;
    }

    public void setIsVerify(Boolean isVerify) {
        this.isVerify = isVerify;
    }


    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }


    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public boolean getIsFollow() {
        return isFollow;
    }

    public void setIsFollow(boolean isFollow) {
        this.isFollow = isFollow;
    }


}