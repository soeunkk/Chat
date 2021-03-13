package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

// ���� ����
public class ChatServer implements Runnable  {
	int port = 11692;
	ServerSocket sersock;
	
	private Vector<ChatService> 	userV;	// ��� �����
	private Vector<ChatService> 	waitV;	// êȨ�� �ִ� ����� (ä�ù� ����X)
	private Vector<ChattingRoom> 	roomV;	// ä�ù� (ä�ù� ����O ����� �ľ� ����)
			
	public ChatServer() {
		userV = new Vector<>();
		waitV = new Vector<>();
		roomV = new Vector<>();
		
		// �������� ���� ���� ����
		new Thread(this).start();
	}
	
	@Override
	public void run() {
		try {
			sersock = new ServerSocket(port);
			System.out.println("Start Server.......");
			
			while(true) {
				// Ŭ���̾�Ʈ �����ϱ⸦ ���
				Socket sock = sersock.accept();
				
				new ChatService(sock, this);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
            try{
                if( sersock != null && !sersock.isClosed() ){
                	sersock.close();
                }
            }
            catch(IOException e){
                e.printStackTrace();
            }
        }
	}
	
	
	public Vector<ChatService> getUserV() { return userV; }
	
	public void addUserV(ChatService s) { userV.add(s); }
	
	public void removeUserV(int i) { userV.remove(i); }
	
	public void removeUserV(ChatService s) { userV.remove(s); }

	public Vector<ChatService> getWaitV() { return waitV; }
	
	public void addWaitV(ChatService s) { waitV.add(s); }
	
	public void removeWaitV(int i) { waitV.remove(i); }
	
	public void removeWaitV(ChatService s) { waitV.remove(s); }
	
	public void addRoomV(ChattingRoom r) { roomV.add(r); }
	
	public Vector<ChattingRoom> getRoomV() { return roomV; }
	
	
	// main function
	public static void main(String[] args) {
		new ChatServer();
	}
}
