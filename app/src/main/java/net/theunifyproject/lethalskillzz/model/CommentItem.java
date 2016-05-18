package net.theunifyproject.lethalskillzz.model;

/**
 * Created by Ibrahim on 29/11/2015.
 */
public class CommentItem {

    private int id;
    private String name, username, comment, timeStamp;
    private boolean isVerify;

   /* public CommentItem() {
    }

    public CommentItem(int id, String uid, String name, String comment, boolean isVerify, String username, String profilePic, String timeStamp) {

        super();
        this.id = id;
        this.uid = uid;
        this.name = name;
        this.comment = comment;
        this.isVerify = isVerify;
        this.username = username;
        this.profilePic = profilePic;
        this.timeStamp = timeStamp;

      }*/

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

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


    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }


    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }


}
