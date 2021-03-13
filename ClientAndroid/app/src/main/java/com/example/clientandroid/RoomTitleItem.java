package com.example.clientandroid;

public class RoomTitleItem implements Comparable<RoomTitleItem> {
    private String title;       // 채팅방 이름
    private String last_msg;    // 마지막 메시지
    private String last_time;   // 마지막 채팅 시간
    private int count;          // 채팅방 인원수
    private int color;          // 고유색깔

    public RoomTitleItem() { }

    public RoomTitleItem(String title, String last_msg, String last_time, int count) {
        super();
        this.title = title;
        this.last_msg = last_msg;
        this.last_time = last_time;
        this.count = count;
    }

    @Override
    // 시간 기준으로 채팅방 정렬
    public int compareTo(RoomTitleItem r) {
        return this.last_time.compareTo(r.last_time);
    }

    public String getTitle() { return title; }

    public void setTitle(String title) { this.title = title; }

    public String getLmsg() { return last_msg; }

    public void setLmsg(String last_msg) { this.last_msg = last_msg; }

    public String getLtime() { return last_time; }

    public void setLtime(String last_time) { this.last_time = last_time; }

    public int getCount() { return count; }

    public void setCount(int count) { this.count = count; }

    public int getColor() { return color; }

    public void setColor(int color) { this.color = color; }
}
