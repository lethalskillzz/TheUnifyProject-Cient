package net.theunifyproject.lethalskillzz.model;

/**
 * Created by Ibrahim on 02/12/2015.
 */
public class NotificationItem {
    private int id, type;
    private String  msg, data, timeStamp;
    private boolean isSeen;

   /* public NotificationItem() {
    }

    public NotificationItem(int id, int type, String msg, String data, boolean isSeen, String timeStamp) {

        super();
        this.id = id;
        this.type = type;
        this.msg = msg;
        this.data = data;
        this.isSeen = isSeen;
        this.timeStamp = timeStamp;
    }*/


    public int getId() { return id; }

    public void setId(int id) {this.id = id;}


    public int getType() { return type; }

    public void setType(int type) { this.type = type; }


    public String getMsg() { return msg; }

    public void setMsg(String msg) { this.msg = msg; }


    public String getData() { return data;}

    public void setData(String data) { this.data = data; }


    public Boolean getIsSeen() { return isSeen; }

    public void setIsSeen(Boolean isSeen) { this.isSeen = isSeen; }


    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }



}
