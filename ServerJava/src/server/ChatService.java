package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

public class ChatService extends Thread{
	// for ����
	Socket 		sock;
	boolean 	stop = false;		// ���� ���� ���ɿ���
	BufferedReader 	receiveRead;	// ������ ������ ���ſ� 
	OutputStream 	ostream;		// �������� ������ �۽ſ� ��Ʈ��
	
	private String 		userId;		// �ڽ��� Client ���̵�
	ChattingRoom 		myRoom;		// �ڽ��� Client�� ���� ä�ù�
	ChatServer			server;		// �ڽ��� ȣ���� ChatServer
	
	/*
	����->Ŭ���̾�Ʈ ��������
		0110: ä�ù� ��� ����
		0170: ���õ� ä�ù� �ο����� ���
		0180: ��� �ο����� ����

		0200: ä�ù� ���� �ʱ�ȭ
		0205: ä�ù� ���� ���
		0220: ä�ø޽��� ���
		0230: ��ũ���޽��� ���
		0270: �ڽ��� ä�ù� �ο����� ����
		0295: ä�ù� ���� �˸�
		
		0999: Ŭ�󸮾�Ʈ ������ ���������� üũ��


	Ŭ���̾�Ʈ->���� ��������
		9100: ä�����α׷� ����
		9105: ���̵� ��� ��û
		9110: ä�ù� ���� ��û
		9170: Ȩ���� ä�ù� �ο����� ��û
		9180: Ȩ ���ΰ�ħ ��û
		9199: ä�����α׷� ���� �˸�

		9200: ä�ù� ���� ��û
		9220: ä�ù� �Ҽ��ο����� �޽��� ���� ��û
		9230: ��ũ���޽��� ���� ��û
		9270: �ڽ��� ���� ä�ù� �ο����� ���� ��û
		9299: ä�ù� ���� �˸�
	*/
	
	// ������
	public ChatService(Socket sock, ChatServer server) {
		this.sock = sock;	// Ŭ���̾�Ʈ�� ������ ������ ����
		this.server = server;
	
		// ���ϰ� I/O��Ʈ�� ����
		try {
			receiveRead = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			ostream = sock.getOutputStream();
			start();	// Ŭ���̾�Ʈ���� ������ ������ �б�(������)
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		try {
			while(!stop) {
				// Ŭ���̾�Ʈ���� ������ ������ �б�
				String msg = receiveRead.readLine();
				
				if(msg != null && msg.trim().length() > 0) {
					// ������ �ܼ�â�� ���ų��� ���
					System.out.println("from Client: "+msg+";"+sock.getInetAddress().getHostAddress());
					
					String msgs[] = msg.split("\\|");	// msgs[0]: ��������, msgs[1]: �ΰ����� 
					String protocol = msgs[0];
					
					switch(protocol) {
					// ä�����α׷� ����
					// "9100|"
					case "9100":
						// ������ �������
						server.addUserV(this);		// ��ü ����� ����Ʈ�� ���
						break;
						
					// ���̵� ��� ��û
					// "9105|�����"
					case "9105":
						userId = msgs[1];	// �ڽ��� Client ���̵� ����
						
						// ������ �������
						server.addWaitV(this);		// ��� ����� ����Ʈ�� ���
						
						// ��������
						// for �ڽ��� Client
						messageTo("0110|"+getRoomInfo());	// ä�ù� �������(�ڹٿ�)
						messageTo("0111|"+getRoomInfo_M());	// ä�ù� �������(����Ͽ�)
						// for ������� �����
						messageWait("0180|"+getWaitUser());	// ������� ���������
						break;
						
					// ä�ù� ���� ��û
					// "9110|ä�ù�1"
					case "9110":	
						// ChattingRoom ����
						myRoom = new ChattingRoom();
						myRoom.setTitle(msgs[1]);
						myRoom.increaseCount(this);
						myRoom.setBoss(userId);
						
						// server�� ä�ù� ����Ʈ�� �߰�
						server.addRoomV(myRoom);
						// ������� ����� ����Ʈ���� ���� 
						server.removeWaitV(this);
						
						// ��������
						// for ä�ù� ���
						messageMembers("0205|"+userId);		// ä�ù� ����˸�
						messageMembers("0270|"+getRoomMember());// ä�ù� �ο�����
						// for ������� �����
						messageWait("0110|"+getRoomInfo());	// ä�ù� �������(�ڹٿ�)
						messageWait("0111|"+getRoomInfo_M());// ä�ù� �������(����Ͽ�)
						messageWait("0180|"+getWaitUser());	// ������� ���������
						break;
						
					// Ȩ���� ä�ù� �ο����� ��û
					// "9170|ä�ù�1"
					case "9170":
						// for �ڽ��� Client
						messageTo("0170|"+getRoomMember(msgs[1]));	// ä�ù� �ο�����
						break;
						
					// êȨ ���ΰ�ħ ��û
					// "9180|"
					case "9180":
						// for ������� �����
						messageWait("0110|"+getRoomInfo());	// ä�ù� �������(�ڹٿ�)
						messageWait("0111|"+getRoomInfo_M());// ä�ù� �������(����Ͽ�)
						messageWait("0180|"+getWaitUser());	// ������� ���������
						break;
						
					// ä�����α׷� ���� �˸�
					// "9199|"
					case "9199":
						stop = true;	// Ŭ���̾�Ʈ ������ �����ϴ� ������ ����
						
						// ä�ù濡 ���� �� ������ ���(case 9299�� ����)
						for (int i=0; (myRoom!=null)&&(myRoom.getMemberV()!=null)&&(i<myRoom.getMemberV().size()); i++) {
							Vector<ChatService> mem = myRoom.getMemberV();
							if (this == mem.get(i)) {
								// ä�ù濡�� �ڽ��� ���� ����
								myRoom.decreaseCount(this);
								
								// �ڽ��� �����̾��� ��� ��ó��
								if (myRoom.getCount() == 0) myRoom.setBoss("none");
								else {
									if (myRoom.getBoss() == userId) 
										myRoom.setBoss(mem.get(0).getUserId());
								}
								
								// �������� for ä�ù� ���
								messageMembers("0295|"+userId);				// ä�ù� ����˸�
								messageMembers("0270|"+getRoomMember());	// ä�ù� �ο�����
								
								// �ڽ��� ä�ù� ���� ���� 
								myRoom = null;
								// ������� ����� ����Ʈ�� �ڽ� �߰�
								server.addWaitV(this);
								
								// �������� for ������� �����
								messageWait("0110|"+getRoomInfo());	// ä�ù� �������(�ڹٿ�)
								messageWait("0111|"+getRoomInfo_M());// ä�ù� �������(����Ͽ�)
								messageWait("0180|"+getWaitUser());	// ������� ���������
							}
						}
						// �������� �������� 
						server.removeWaitV(this);
						server.removeUserV(this);
						
						// �������� for ������� �����
						messageWait("0180|"+getWaitUser());	// ������� ���������
						// for �ڽ��� Client
						messageTo("0200|");	// ä��â �ʱ�ȭ
						
						// Ŭ�󸮾�Ʈ�� ������ �������⸦ ��ٷȴٰ� �ڽŵ� ����
						closeClass();
						break;
					
					// ä�ù� ���� ��û
					// "9200|ä�ù�1"
					case "9200":	
						Vector<ChattingRoom> roomV = server.getRoomV();
						for (int i=0; i < roomV.size(); i++) {
							ChattingRoom r = roomV.get(i);
							
							// ��ġ�ϴ� �� ã���� �ڽ��� ä�ù����� ����
							if (r.getTitle().equals(msgs[1])) {
								myRoom = r;
								// 0���� ä�ù濡 ������ ��� ���� ����
								if (myRoom.getBoss()=="none") myRoom.setBoss(userId);
								// ä�ù濡 �ڽ��� ���� �߰�
								myRoom.increaseCount(this);
								break;
							}
						}
						
						// ������� ����� ����Ʈ���� ���� 
						server.removeWaitV(this);
						
						// ��������
						// for ä�ù� ���
						messageMembers("0205|"+userId);		// ä�ù� ����˸�
						messageMembers("0270|"+getRoomMember());	// �ڽ��� ä�ù� �ο�����
						// for ������� �����
						messageWait("0110|"+getRoomInfo());	// ä�ù� �������(�ڹٿ�)
						messageWait("0111|"+getRoomInfo_M());// ä�ù� �������(����Ͽ�)
						messageWait("0180|"+getWaitUser());	// ������� ���������
						break;
						
					// ä�ù� �Ҽ��ο����� �޽��� ���� ��û
					// "9220|�ȳ��ϼ���~"
					case "9220":
						if (msgs.length > 1) {
							myRoom.setLMsg(msgs[1]);						// �ֱ� �޽��� ����
							myRoom.setLDate(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));
							// for ä�ù� ���
							messageMembers("0220|["+userId+"]: "+msgs[1]);	// ä�� �޽���
							// for ����� ������� �����
							messageWait("0111|"+getRoomInfo_M());			// ä�ù� �������(����Ͽ�)
						}

						break;
					
					// ��ũ���޽��� ���� ��û
					// "9230|�����,��������"
					case "9230":
						String[] data = msgs[1].split(",", 2);	// data[0]: Ÿ�� ���̵�, data[1]: ��ũ���޽��� ����
						Vector<ChatService> memberV = myRoom.getMemberV();
						
						// �ش� ���̵��� ��� ChatService�� Client���� �޽��� �۽�
						for (int i=0 ; i < memberV.size(); i++) {
							ChatService mem = memberV.get(i);
							if (data[0].equals(memberV.get(i).getUserId())) {
								mem.messageTo("0230|["+userId+"]: "+data[1]);	// ��ũ���޽��� to ����
								messageTo("0230|["+userId+"]: "+data[1]);		// ��ũ���޽��� to �ڽ�
								break;
							}
						}
						break;
						
					// �ڽ��� ���� ä�ù� �ο����� ���� ��û
					// "9270|"
					case "9270":
						// for ä�ù� ���
						messageMembers("0270|"+getRoomMember());	// �ڽ��� ä�ù� �ο�����
						break;
						
					// ä�ù� ���� �˸�
					// "9299|"
					case "9299":
						// ä�ù濡�� �ڽ��� ���� ����
						myRoom.decreaseCount(this);
						
						// �ڽ��� �����̾��� ��� ��ó��
						if (myRoom.getCount() == 0) myRoom.setBoss("none");
						else {
							if (myRoom.getBoss() == userId) 
								myRoom.setBoss(myRoom.getMemberV().get(0).getUserId());
						}
						
						// �������� for ä�ù� ���
						messageMembers("0295|"+userId);				// ä�ù� ����˸�
						messageMembers("0270|"+getRoomMember());	// ä�ù� �ο�����
						
						// �ڽ��� ä�ù� ���� ���� 
						myRoom = null;
						// ������� ����� ����Ʈ�� �ڽ� �߰�
						server.addWaitV(this);
						
						// �������� for ������� �����
						messageWait("0110|"+getRoomInfo());	// ä�ù� �������(�ڹٿ�)
						messageWait("0111|"+getRoomInfo_M());// ä�ù� �������(����Ͽ�)
						messageWait("0180|"+getWaitUser());	// ������� ���������
						// for �ڽ��� Client
						messageTo("0200|");	// ä��â �ʱ�ȭ
						
						break;
					}
				}
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// ��ü ä�ù� ����
	public String getRoomInfo() {
		String str = "";
		Vector<ChattingRoom> roomV = server.getRoomV();
		
		// ��ü ä�ù��� �̸��� �ο���
		for (int i=0; i < roomV.size(); i++) {
			ChattingRoom r = roomV.get(i);
			str += r.getTitle()+"--"+r.getCount();
			if (i < roomV.size()-1) str += ",";
		}
		
		// "ä�ù�1--6,ä�ù�2--3,ä�ù�3--8"
		return str;
	}
	
	// ��ü ä�ù� ���� (����Ͽ�)
	public String getRoomInfo_M() {
		String str = "";
		Vector<ChattingRoom> roomV = server.getRoomV();
		
		// ��ü ä�ù��� �̸��� �ο���
		for (int i=0; i < roomV.size(); i++) {
			ChattingRoom r = roomV.get(i);
			str += r.getTitle()+"--"+r.getCount()+"--"+r.getLMsg()+"--"+r.getLDate(); 
			if (i < roomV.size()-1) str += ",";
		}
		
		// "ä�ù�1--6--�߾߾�--2020/11/30 11:24:33,ä�ù�2--3--�ȳ�ȳ�--2020/11/30 11:48:32,ä�ù�3--8--����¥??--2020/11/30 12:43:94"
		return str;
	}
	
	// ä�ù��� �ο����� (�Ű����� X: Client�� ���� ���� ä�ù�)
	public String getRoomMember() {
		String str = "";
		Vector<ChatService> memberV = myRoom.getMemberV();
		
		// ä�ù� ����� ���̵�
		for (int i=0 ; i < memberV.size(); i++) {
			ChatService mem = memberV.get(i);
			str += mem.getUserId();
			if (i < memberV.size()-1) str += ",";
		}
		
		// "�����,��Ҹ�,�����"
		return str;
	}
	
	// ä�ù��� �ο����� (�Ű����� O: ê�뿡�� ���õ� ä�ù�)
	public String getRoomMember(String title) {
		String str = "";
		Vector<ChattingRoom> roomV = server.getRoomV();
		Vector<ChatService> memberV;
		
		// ��ü ä�ù��� �̸��� �ο���
		for (int i=0; i < roomV.size(); i++) {
			ChattingRoom r = roomV.get(i);
			
			// ä�ù� �̸��� ��ġ�ϴ� ChattingRoom ã��
			if(r.getTitle().equals(title) ) {
				memberV = r.getMemberV();
				
				// ä�ù� ����� ���̵�
				for (int j=0; j < memberV.size(); j++) {
					ChatService mem = memberV.get(j);
					str += mem.getUserId();
					if (j < memberV.size()-1) str += ",";
				}
				break;
			}
		}
		
		// "�����,��Ҹ�,�����"
		return str;
	}
	
	// ������� ����� ����
	public String getWaitUser() {
		String str = "";
		Vector<ChatService> waitUserV = server.getWaitV();
		
		// ������� ������� ���̵�
		for (int i=0; i < waitUserV.size(); i++) {
			ChatService user = waitUserV.get(i);
			str += user.getUserId();
			if (i < waitUserV.size()-1) str += ",";
		}
		
		// "�����,��Ҹ�,�����"
		return str;
	}
	
	// ��ü Client���� �޽��� ����
	public void messageAll(String msg) {
		Vector<ChatService> userV = server.getUserV();
		
		// �ش��ϴ� ChatService ��ü�� ���� �޽��� �۽� 
		for (int i=0; i < userV.size(); i++) {
			ChatService user = userV.get(i);
			
			try {
				user.messageTo(msg);
			} catch (IOException e) {		// Ŭ���̾�Ʈ ���� ���� ����
				server.removeUserV(i--);	// ���� ���� Ŭ���̾�Ʈ ����
				System.out.println("Ŭ���̾�Ʈ ���� ����");
			}	
		}
	}

	// ������� Client���� �޽��� ����
	public void messageWait(String msg) {
		Vector<ChatService> waitUserV = server.getWaitV();
		
		// �ش��ϴ� ChatService ��ü�� ���� �޽��� �۽� 
		for (int i=0; i < waitUserV.size(); i++) {
			ChatService waitUser = waitUserV.get(i);
			
			try {
				waitUser.messageTo(msg);
			} catch (IOException e) {		// Ŭ���̾�Ʈ ���� ���� ����
				server.removeWaitV(i--);	// ���� ���� Ŭ���̾�Ʈ ����
				System.out.println("Ŭ���̾�Ʈ ���� ����");
			}	
		}	
	}
	
	// ä�ù� ����鿡�� �޽��� ����
	public void messageMembers(String msg) {
		Vector<ChatService> memberV = myRoom.getMemberV();
		
		// �ش��ϴ� ChatService ��ü�� ���� �޽��� �۽�
		for (int i=0; i < memberV.size(); i++) {
			ChatService mem = memberV.get(i);
			
			try {
				mem.messageTo(msg);
			} catch (IOException e) {		// Ŭ���̾�Ʈ ���� ���� ����
				System.out.println("Ŭ���̾�Ʈ ���� ����");
			}	
		}
	}
	
	
	// �ڽ��� Client���� �޽��� ���� (������ ���� �۽�)
	public void messageTo(String msg) throws IOException {
		ostream.write((msg+"\n").getBytes());
	}
	
	public String getUserId() { return userId; }
	
	// Ŭ���̾�Ʈ�� ������ ����⸦ ��ٷȴٰ� �ڽŵ� ����
	private void closeClass() {
		// Ŭ���̾�Ʈ�� ���� ������ �������⸦ ��ٷȴٰ� �ڽ��� ���ϵ� ����
//		while (true) {
//			try {
//				ostream.write(("0999|").getBytes());
//			} catch (IOException e) {
//				try {
//					sock.close();
//					System.exit(0);
//				}
//				catch (IOException e1) {
//					e1.printStackTrace();
//				}
//			}
//		}
	}
}
