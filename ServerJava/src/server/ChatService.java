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
	// for 소켓
	Socket 		sock;
	boolean 	stop = false;		// 소켓 수신 가능여부
	BufferedReader 	receiveRead;	// 서버의 데이터 수신용 
	OutputStream 	ostream;		// 서버에게 데이터 송신용 스트림
	
	private String 		userId;		// 자신의 Client 아이디
	ChattingRoom 		myRoom;		// 자신의 Client가 속한 채팅방
	ChatServer			server;		// 자신을 호출한 ChatServer
	
	/*
	서버->클라이언트 프로토콜
		0110: 채팅방 목록 갱신
		0170: 선택된 채팅방 인원정보 출력
		0180: 대기 인원정보 갱신

		0200: 채팅방 내용 초기화
		0205: 채팅방 입장 출력
		0220: 채팅메시지 출력
		0230: 시크릿메시지 출력
		0270: 자신의 채팅방 인원정보 갱신
		0295: 채팅방 퇴장 알림
		
		0999: 클라리언트 소켓이 끊어졌는지 체크용


	클라이언트->서버 프로토콜
		9100: 채팅프로그램 시작
		9105: 아이디 등록 요청
		9110: 채팅방 생성 요청
		9170: 홈에서 채팅방 인원정보 요청
		9180: 홈 새로고침 요청
		9199: 채팅프로그램 종료 알림

		9200: 채팅방 입장 요청
		9220: 채팅방 소속인원에게 메시지 전송 요청
		9230: 시크릿메시지 전송 요청
		9270: 자신이 속한 채팅방 인원정보 갱신 요청
		9299: 채팅방 퇴장 알림
	*/
	
	// 생성자
	public ChatService(Socket sock, ChatServer server) {
		this.sock = sock;	// 클라이언트와 대응된 서버측 소켓
		this.server = server;
	
		// 소켓과 I/O스트림 연결
		try {
			receiveRead = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			ostream = sock.getOutputStream();
			start();	// 클라이언트에서 수신한 데이터 읽기(쓰레드)
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		try {
			while(!stop) {
				// 클라이언트에서 수신한 데이터 읽기
				String msg = receiveRead.readLine();
				
				if(msg != null && msg.trim().length() > 0) {
					// 서버측 콘솔창에 수신내용 출력
					System.out.println("from Client: "+msg+";"+sock.getInetAddress().getHostAddress());
					
					String msgs[] = msg.split("\\|");	// msgs[0]: 프로토콜, msgs[1]: 부가정보 
					String protocol = msgs[0];
					
					switch(protocol) {
					// 채팅프로그램 시작
					// "9100|"
					case "9100":
						// 서버에 정보등록
						server.addUserV(this);		// 전체 사용자 리스트에 등록
						break;
						
					// 아이디 등록 요청
					// "9105|김소은"
					case "9105":
						userId = msgs[1];	// 자신의 Client 아이디 설정
						
						// 서버에 정보등록
						server.addWaitV(this);		// 대기 사용자 리스트에 등록
						
						// 정보갱신
						// for 자신의 Client
						messageTo("0110|"+getRoomInfo());	// 채팅방 목록정보(자바용)
						messageTo("0111|"+getRoomInfo_M());	// 채팅방 목록정보(모바일용)
						// for 대기중인 사용자
						messageWait("0180|"+getWaitUser());	// 대기중인 사용자정보
						break;
						
					// 채팅방 생성 요청
					// "9110|채팅방1"
					case "9110":	
						// ChattingRoom 생성
						myRoom = new ChattingRoom();
						myRoom.setTitle(msgs[1]);
						myRoom.increaseCount(this);
						myRoom.setBoss(userId);
						
						// server의 채팅방 리스트에 추가
						server.addRoomV(myRoom);
						// 대기중인 사용자 리스트에서 삭제 
						server.removeWaitV(this);
						
						// 정보갱신
						// for 채팅방 멤버
						messageMembers("0205|"+userId);		// 채팅방 입장알림
						messageMembers("0270|"+getRoomMember());// 채팅방 인원정보
						// for 대기중인 사용자
						messageWait("0110|"+getRoomInfo());	// 채팅방 목록정보(자바용)
						messageWait("0111|"+getRoomInfo_M());// 채팅방 목록정보(모바일용)
						messageWait("0180|"+getWaitUser());	// 대기중인 사용자정보
						break;
						
					// 홈에서 채팅방 인원정보 요청
					// "9170|채팅방1"
					case "9170":
						// for 자신의 Client
						messageTo("0170|"+getRoomMember(msgs[1]));	// 채팅방 인원정보
						break;
						
					// 챗홈 새로고침 요청
					// "9180|"
					case "9180":
						// for 대기중인 사용자
						messageWait("0110|"+getRoomInfo());	// 채팅방 목록정보(자바용)
						messageWait("0111|"+getRoomInfo_M());// 채팅방 목록정보(모바일용)
						messageWait("0180|"+getWaitUser());	// 대기중인 사용자정보
						break;
						
					// 채팅프로그램 종료 알림
					// "9199|"
					case "9199":
						stop = true;	// 클라이언트 데이터 수신하는 쓰레드 종료
						
						// 채팅방에 들어갔을 때 종료한 경우(case 9299와 동일)
						for (int i=0; (myRoom!=null)&&(myRoom.getMemberV()!=null)&&(i<myRoom.getMemberV().size()); i++) {
							Vector<ChatService> mem = myRoom.getMemberV();
							if (this == mem.get(i)) {
								// 채팅방에서 자신의 정보 삭제
								myRoom.decreaseCount(this);
								
								// 자신이 방장이었을 경우 후처리
								if (myRoom.getCount() == 0) myRoom.setBoss("none");
								else {
									if (myRoom.getBoss() == userId) 
										myRoom.setBoss(mem.get(0).getUserId());
								}
								
								// 정보갱신 for 채팅방 멤버
								messageMembers("0295|"+userId);				// 채팅방 퇴장알림
								messageMembers("0270|"+getRoomMember());	// 채팅방 인원정보
								
								// 자신의 채팅방 정보 삭제 
								myRoom = null;
								// 대기중인 사용자 리스트에 자신 추가
								server.addWaitV(this);
								
								// 정보갱신 for 대기중인 사용자
								messageWait("0110|"+getRoomInfo());	// 채팅방 목록정보(자바용)
								messageWait("0111|"+getRoomInfo_M());// 채팅방 목록정보(모바일용)
								messageWait("0180|"+getWaitUser());	// 대기중인 사용자정보
							}
						}
						// 서버에서 정보삭제 
						server.removeWaitV(this);
						server.removeUserV(this);
						
						// 정보갱신 for 대기중인 사용자
						messageWait("0180|"+getWaitUser());	// 대기중인 사용자정보
						// for 자신의 Client
						messageTo("0200|");	// 채팅창 초기화
						
						// 클라리언트의 소켓이 끊어지기를 기다렸다가 자신도 종료
						closeClass();
						break;
					
					// 채팅방 입장 요청
					// "9200|채팅방1"
					case "9200":	
						Vector<ChattingRoom> roomV = server.getRoomV();
						for (int i=0; i < roomV.size(); i++) {
							ChattingRoom r = roomV.get(i);
							
							// 일치하는 방 찾으면 자신의 채팅방으로 설정
							if (r.getTitle().equals(msgs[1])) {
								myRoom = r;
								// 0명인 채팅방에 입장한 경우 방장 위임
								if (myRoom.getBoss()=="none") myRoom.setBoss(userId);
								// 채팅방에 자신의 정보 추가
								myRoom.increaseCount(this);
								break;
							}
						}
						
						// 대기중인 사용자 리스트에서 삭제 
						server.removeWaitV(this);
						
						// 정보갱신
						// for 채팅방 멤버
						messageMembers("0205|"+userId);		// 채팅방 입장알림
						messageMembers("0270|"+getRoomMember());	// 자신의 채팅방 인원정보
						// for 대기중인 사용자
						messageWait("0110|"+getRoomInfo());	// 채팅방 목록정보(자바용)
						messageWait("0111|"+getRoomInfo_M());// 채팅방 목록정보(모바일용)
						messageWait("0180|"+getWaitUser());	// 대기중인 사용자정보
						break;
						
					// 채팅방 소속인원에게 메시지 전송 요청
					// "9220|안녕하세요~"
					case "9220":
						if (msgs.length > 1) {
							myRoom.setLMsg(msgs[1]);						// 최근 메시지 갱신
							myRoom.setLDate(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));
							// for 채팅방 멤버
							messageMembers("0220|["+userId+"]: "+msgs[1]);	// 채팅 메시지
							// for 모바일 대기중인 사용자
							messageWait("0111|"+getRoomInfo_M());			// 채팅방 목록정보(모바일용)
						}

						break;
					
					// 시크릿메시지 전송 요청
					// "9230|김소은,하이하이"
					case "9230":
						String[] data = msgs[1].split(",", 2);	// data[0]: 타겟 아이디, data[1]: 시크릿메시지 내용
						Vector<ChatService> memberV = myRoom.getMemberV();
						
						// 해당 아이디의 멤버 ChatService의 Client에게 메시지 송신
						for (int i=0 ; i < memberV.size(); i++) {
							ChatService mem = memberV.get(i);
							if (data[0].equals(memberV.get(i).getUserId())) {
								mem.messageTo("0230|["+userId+"]: "+data[1]);	// 시크릿메시지 to 상대방
								messageTo("0230|["+userId+"]: "+data[1]);		// 시크릿메시지 to 자신
								break;
							}
						}
						break;
						
					// 자신이 속한 채팅방 인원정보 갱신 요청
					// "9270|"
					case "9270":
						// for 채팅방 멤버
						messageMembers("0270|"+getRoomMember());	// 자신의 채팅방 인원정보
						break;
						
					// 채팅방 퇴장 알림
					// "9299|"
					case "9299":
						// 채팅방에서 자신의 정보 삭제
						myRoom.decreaseCount(this);
						
						// 자신이 방장이었을 경우 후처리
						if (myRoom.getCount() == 0) myRoom.setBoss("none");
						else {
							if (myRoom.getBoss() == userId) 
								myRoom.setBoss(myRoom.getMemberV().get(0).getUserId());
						}
						
						// 정보갱신 for 채팅방 멤버
						messageMembers("0295|"+userId);				// 채팅방 퇴장알림
						messageMembers("0270|"+getRoomMember());	// 채팅방 인원정보
						
						// 자신의 채팅방 정보 삭제 
						myRoom = null;
						// 대기중인 사용자 리스트에 자신 추가
						server.addWaitV(this);
						
						// 정보갱신 for 대기중인 사용자
						messageWait("0110|"+getRoomInfo());	// 채팅방 목록정보(자바용)
						messageWait("0111|"+getRoomInfo_M());// 채팅방 목록정보(모바일용)
						messageWait("0180|"+getWaitUser());	// 대기중인 사용자정보
						// for 자신의 Client
						messageTo("0200|");	// 채팅창 초기화
						
						break;
					}
				}
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// 전체 채팅방 정보
	public String getRoomInfo() {
		String str = "";
		Vector<ChattingRoom> roomV = server.getRoomV();
		
		// 전체 채팅방의 이름과 인원수
		for (int i=0; i < roomV.size(); i++) {
			ChattingRoom r = roomV.get(i);
			str += r.getTitle()+"--"+r.getCount();
			if (i < roomV.size()-1) str += ",";
		}
		
		// "채팅방1--6,채팅방2--3,채팅방3--8"
		return str;
	}
	
	// 전체 채팅방 정보 (모바일용)
	public String getRoomInfo_M() {
		String str = "";
		Vector<ChattingRoom> roomV = server.getRoomV();
		
		// 전체 채팅방의 이름과 인원수
		for (int i=0; i < roomV.size(); i++) {
			ChattingRoom r = roomV.get(i);
			str += r.getTitle()+"--"+r.getCount()+"--"+r.getLMsg()+"--"+r.getLDate(); 
			if (i < roomV.size()-1) str += ",";
		}
		
		// "채팅방1--6--야야야--2020/11/30 11:24:33,채팅방2--3--안녕안녕--2020/11/30 11:48:32,채팅방3--8--헐진짜??--2020/11/30 12:43:94"
		return str;
	}
	
	// 채팅방의 인원정보 (매개변수 X: Client가 현재 속한 채팅방)
	public String getRoomMember() {
		String str = "";
		Vector<ChatService> memberV = myRoom.getMemberV();
		
		// 채팅방 멤버의 아이디
		for (int i=0 ; i < memberV.size(); i++) {
			ChatService mem = memberV.get(i);
			str += mem.getUserId();
			if (i < memberV.size()-1) str += ",";
		}
		
		// "김소은,김소명,김소준"
		return str;
	}
	
	// 채팅방의 인원정보 (매개변수 O: 챗룸에서 선택된 채팅방)
	public String getRoomMember(String title) {
		String str = "";
		Vector<ChattingRoom> roomV = server.getRoomV();
		Vector<ChatService> memberV;
		
		// 전체 채팅방의 이름과 인원수
		for (int i=0; i < roomV.size(); i++) {
			ChattingRoom r = roomV.get(i);
			
			// 채팅방 이름과 일치하는 ChattingRoom 찾기
			if(r.getTitle().equals(title) ) {
				memberV = r.getMemberV();
				
				// 채팅방 멤버의 아이디
				for (int j=0; j < memberV.size(); j++) {
					ChatService mem = memberV.get(j);
					str += mem.getUserId();
					if (j < memberV.size()-1) str += ",";
				}
				break;
			}
		}
		
		// "김소은,김소명,김소준"
		return str;
	}
	
	// 대기중인 사용자 정보
	public String getWaitUser() {
		String str = "";
		Vector<ChatService> waitUserV = server.getWaitV();
		
		// 대기중인 사용자의 아이디
		for (int i=0; i < waitUserV.size(); i++) {
			ChatService user = waitUserV.get(i);
			str += user.getUserId();
			if (i < waitUserV.size()-1) str += ",";
		}
		
		// "김소은,김소명,김소준"
		return str;
	}
	
	// 전체 Client에게 메시지 전송
	public void messageAll(String msg) {
		Vector<ChatService> userV = server.getUserV();
		
		// 해당하는 ChatService 객체를 통해 메시지 송신 
		for (int i=0; i < userV.size(); i++) {
			ChatService user = userV.get(i);
			
			try {
				user.messageTo(msg);
			} catch (IOException e) {		// 클라이언트 접속 끊김 오류
				server.removeUserV(i--);	// 접속 끊긴 클라이언트 삭제
				System.out.println("클라이언트 접속 끊음");
			}	
		}
	}

	// 대기중인 Client에게 메시지 전송
	public void messageWait(String msg) {
		Vector<ChatService> waitUserV = server.getWaitV();
		
		// 해당하는 ChatService 객체를 통해 메시지 송신 
		for (int i=0; i < waitUserV.size(); i++) {
			ChatService waitUser = waitUserV.get(i);
			
			try {
				waitUser.messageTo(msg);
			} catch (IOException e) {		// 클라이언트 접속 끊김 오류
				server.removeWaitV(i--);	// 접속 끊긴 클라이언트 삭제
				System.out.println("클라이언트 접속 끊음");
			}	
		}	
	}
	
	// 채팅방 멤버들에게 메시지 전송
	public void messageMembers(String msg) {
		Vector<ChatService> memberV = myRoom.getMemberV();
		
		// 해당하는 ChatService 객체를 통해 메시지 송신
		for (int i=0; i < memberV.size(); i++) {
			ChatService mem = memberV.get(i);
			
			try {
				mem.messageTo(msg);
			} catch (IOException e) {		// 클라이언트 접속 끊김 오류
				System.out.println("클라이언트 접속 끊음");
			}	
		}
	}
	
	
	// 자신의 Client에게 메시지 전달 (소켓을 통한 송신)
	public void messageTo(String msg) throws IOException {
		ostream.write((msg+"\n").getBytes());
	}
	
	public String getUserId() { return userId; }
	
	// 클라이언트의 연결이 끊기기를 기다렸다가 자신도 종료
	private void closeClass() {
		// 클라이언트의 소켓 연결이 끊어지기를 기다렸다가 자신의 소켓도 끊음
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
