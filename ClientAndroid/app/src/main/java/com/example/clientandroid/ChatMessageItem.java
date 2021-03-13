package com.example.clientandroid;

public class ChatMessageItem {
    private String user_ID; // 메시지 보낸 사용자 이름
    private String message; // 메시지 내용
    private int state;      // 메시지 상태값

    /*
    state 값
        0: 공지사항용
        1: 일반 메시지용-자신
        2: 일반 메시지용-상대방
        3: 비밀 메시지용-자신
        4: 비밀 메시지용-상대방
     */

    public ChatMessageItem() { }

    public String getID() { return user_ID; }

    public void setID(String user_ID) { this.user_ID = user_ID; }

    public String getMessage() { return message; }

    public void setMessage(String message) { this.message = message; }

    public int getState() { return state; }

    public void setState(int state) { this.state = state; }
}
