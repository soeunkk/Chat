package server;

import java.util.Vector;

// ä�ù� ���� ����
public class ChattingRoom {
	private String 	title;			// ä�ù� �̸�
	private int 	count;			// ä�ù� �ο���
	private String 	boss = "none";	// ����(�氳����)
	private Vector<ChatService> memberV;	// ä�ù� ���� ������ Client�� ����(������ ����)
	private String lastMsg = " ";	// ������ �޽���
	private String lastDate = " ";	// ������ �޽��� timestamp
	
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
