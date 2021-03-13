package server;

import java.util.Vector;

// 채팅방 개별 정보
public class ChattingRoom {
	private String 	title;			// 채팅방 이름
	private int 	count;			// 채팅방 인원수
	private String 	boss = "none";	// 방장(방개설자)
	private Vector<ChatService> memberV;	// 채팅방 내에 접속한 Client들 정보(서버측 정보)
	private String lastMsg = " ";	// 마지막 메시지
	private String lastDate = " ";	// 마지막 메시지 timestamp
	
	public ChattingRoom() {
		memberV = new Vector<>();
		count = 0;
	}
	
	public String getTitle() { return title; }

	public void setTitle(String title) { this.title = title; }
	
	public int getCount() { return count; }
	
	public Vector<ChatService> getMemberV() { return memberV; }
	
	public void increaseCount(ChatService s) { 
		count++;
		memberV.add(s);
	}
	
	public void decreaseCount(ChatService s) {
		count--;
		memberV.remove(s);
	}
	
	public String getBoss() { return boss; }
	
	public void setBoss(String id) { boss = id; }
	
	public String getLMsg() { return lastMsg; }
	
	public void setLMsg(String lastMsg) { this.lastMsg = lastMsg; }
	
	public String getLDate() { return lastDate; }
	
	public void setLDate(String lastDate) { this.lastDate = lastDate; }

}
