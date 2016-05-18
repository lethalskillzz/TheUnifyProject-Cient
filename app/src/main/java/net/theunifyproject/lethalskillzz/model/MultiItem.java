package net.theunifyproject.lethalskillzz.model;

/**
 * Created by Ibrahim on 12/11/2015.
 */
public class MultiItem {

    private int id, type;
    private String name, username, status, image, timeStamp, info, hash, count, likeCount, commentCount;
    private boolean isVerify, isLike, isFollow, isExpand;

  /*  public MultiItem() {
    }

    public MultiItem(int id, int type, String uid, String name, boolean isVerify, String username, String image, String status,
                     String profilePic, String timeStamp, String info, String hash, String count, String likeCount,
                     String commentCount, boolean isLike, boolean isFollow, boolean isExpand) {

        super();
        this.id = id;
        this.type = type;
        this.uid = uid;
        this.name = name;
        this.isVerify = isVerify;
        this.username = username;
        this.image = image;
        this.status = status;
        this.profilePic = profilePic;
        this.timeStamp = timeStamp;
        this.info = info;
        this.hash = hash;
        this.count = count;
        this.likeCount = likeCount;
        this.commentCount = commentCount;
        this.isLike = isLike;
        this.isFollow = isFollow;
        this.isExpand = isExpand;

    }*/


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
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



    public String getImage() {

        return image;
    }

    public void setImage(String image) {

        this.image = image;
    }


    public String getStatus() {

        return status;
    }

    public void setStatus(String status) {

        this.status = status;
    }



    public String getTimeStamp() {

        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {

        this.timeStamp = timeStamp;
    }


    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {

        this.info = info;
    }


    public String getHash() {

        return hash;
    }

    public void setHash(String hash) {

        this.hash = hash;
    }


    public String getCount() {
        return count;
    }

    public void setCount(String count) {
        this.count = count;
    }


    public String getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(String likeCount) {
        this.likeCount = likeCount;
    }

    public String getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(String commentCount) {
        this.commentCount = commentCount;
    }

    public boolean getIsLike() {
        return isLike;
    }

    public void setIsLike(boolean isLike) {
        this.isLike = isLike;
    }


    public boolean getIsFollow() {
        return isFollow;
    }

    public void setIsFollow(boolean isFollow) { this.isFollow = isFollow; }


    public boolean getIsExpand() {
        return isExpand;
    }

    public void setIsExpand(boolean isExpand) {
        this.isExpand = isExpand;
    }



}
