package client;

import client.Frame.ChattingFrame;
import client.Frame.ChatHomeFrame;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.JOptionPane;

public class ChatClient implements ActionListener, Runnable{
	
	// for 소켓
	int 	port = 11692;
	Socket 	sock;
	boolean stop = false;			// 소켓 수신 가능여부
	BufferedReader 	receiveRead;	// 서버의 데이터 수신용 
	OutputStream 	ostream;		// 서버에게 데이터 송신용 스트림
	
	// for UI
	ChattingFrame chatPane;
	ChatHomeFrame homePane;
	
	String selectedRoomName;
	
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
	public ChatClient() {		
		chatPane = new ChattingFrame();
		homePane = new ChatHomeFrame();
	    
		// 서버와 연결 시도
		connect();
		// 서버에서 수신한 데이터 읽기(쓰레드)
		new Thread(this).start();
		
		// 채팅프로그램 접속
		sendMsg("9100|");
		
		// 아이디 입력받음
		String user_ID = JOptionPane.showInputDialog(homePane, "아이디");
		sendMsg("9105|"+ user_ID);
		
		// 이벤트 연결
		registerEvent();
		
		// 윈도우 'X'버튼 종료처리
		closeWindowEvent();
	}
	
	// 서버에게 연결 요청
	public void connect() {
		try {
			// 해당 (IP, 포트)로 연결 시도
			sock = new Socket("222.111.4.158", port);
			
			// 소켓과 I/O스트림 연결
			receiveRead = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			ostream = sock.getOutputStream();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// 서버에게 데이터 송신
	public void sendMsg(String msg) {
		try {
			ostream.write((msg+'\n').getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		try {
			// 창을 끄지 전까지 
			while(!stop) {
				// 서버에서 수신한 데이터 읽기
				String msg = receiveRead.readLine();
				
				// 클라이언트측 콘솔창에 수신내용 출력
				System.out.println("from Server: "+msg+";"+sock.getInetAddress().getHostAddress());
				
				// 데이터 분할
				String msgs[] = msg.split("\\|");	// msgs[0]: 프로토콜, msgs[1]: 부가정보 
				String protocol = msgs[0];
				
				switch(protocol) {
				// 채팅방 목록 갱신
				// "0110|채팅방1--6,채팅방2--3,채팅방3--8"
				case "0110":
					String rooms[] = {""};
					// 채팅방 단위로 쪼개기
					if(msgs.length > 1) rooms = msgs[1].split(",");
					// 리스트 업데이트
					homePane.roomInfo.setListData(rooms);
					break;
					
					
				// 선택된 채팅방 인원정보 출력
				// "0170|김소은,김소명,김소준"
				case "0170":
					String members[] = {""};
					// 멤버 단위로 쪼개기
					if (msgs.length > 1) members = msgs[1].split(",");
					// 리스트 업데이트
					homePane.roomMember.setListData(members);
					break;
					
					
				// 대기 인원정보 갱신
				// "0180|아빠,엄마"
				case "0180":
					String waitUsers[] = {""};
					// 멤버 단위로 쪼개기
					if (msgs.length > 1) waitUsers = msgs[1].split(",");
					// 리스트 업데이트
					homePane.waitUser.setListData(waitUsers);
					break;
					
					
				// 채팅방 초기화
				// "0200|"
				case "0200":
					chatPane.tpChat.setText("");	// 채팅내용 삭제
					chatPane.tpChat.setCaretPosition(0);
					chatPane.lbTitle.setText("");	// 채팅방 이름 삭제
					String empty_elm[] = {""}; 
					chatPane.userList.setListData(empty_elm);	// 채팅방 소속인원 삭제
					chatPane.userListClose();					// 멤버 리스트창 닫기
					break;
					
					
				// 채팅방 입장 출력
				// "0205|김소은"
				case "0205":
					chatPane.tpChat.append(new Color(140,140,140), "["+msgs[1]+"]님이 입장하셨습니다.\n");
					break;
					
					
				// 채팅메시지 출력
				// "0220|[김소은]:안녕하세요!"
				case "0220":
					chatPane.tpChat.append(Color.BLACK, msgs[1]+"\n");
					break;
					
				// 시크릿메시지 출력
				// "0230|[김소은]:안녕하세요!"
				case "0230":
					chatPane.tpChat.append(Color.RED, msgs[1]+"\n");
					break;	
					
				// 자신의 채팅방 인원정보 갱신
				// "0270|김소은,김소명,김소준"
				case "0270":
					String Mymembers[] = {""};
					// 멤버 단위로 쪼개기
					if (msgs.length > 1) Mymembers = msgs[1].split(",");
					// 리스트 업데이트
					chatPane.userList.setListData(Mymembers);
					break;
					
				// 채팅방 퇴장 출력
				// "0295|김소은"
				case "0295":
					chatPane.tpChat.append(new Color(140,140,140), "["+msgs[1]+"]님이 퇴장하셨습니다.\n");
					break;
				}
			}
		} catch (IOException e) {
			// stop 여부를 한번 더 체크 (stop=true이면 정상)
			if (stop != true) e.printStackTrace();
		} 
	}
	
	// 이벤트 연결
	private void registerEvent() {
		//채팅목록(ChatHome)
		homePane.btnCreate.addActionListener(this);
		homePane.btnEnter.addActionListener(this);
		homePane.btnExit.addActionListener(this);
		homePane.btnUpdate.addActionListener(this);
			    
		//대화방(ChatClient)
		chatPane.btnSend.addActionListener(this);
		
		/*-- 홈-채팅방 목록의 항목 클릭 --*/ 
		homePane.roomInfo.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				String room_data = homePane.roomInfo.getSelectedValue();
				if (room_data == null) return;
				
				// 서버에게 해당 채팅방 인원정보 요청
				selectedRoomName = room_data.substring(0, room_data.indexOf("-"));	// 채팅방 이름만 추출
				sendMsg("9170|"+selectedRoomName);
			}
		});
		
		/*-- 채팅방-ENTER 입력 --*/
		chatPane.message.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					// 사용자가 작성한 메시지
					String msg = chatPane.message.getText();
					if (msg.length() == 0) return;
					
					// 채팅방 소속인원에게 메시지 전송 요청
					sendMsg("9220|"+msg);
					
					// 전송한 텍스트 삭제
					chatPane.message.setText("");
					chatPane.message.setCaretPosition(0);
				}
			}
		});
		
		/*-- 채팅방-특정멤버 더블클릭 --*/ 
		chatPane.userList.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					String selectedUser = chatPane.userList.getSelectedValue();
					
					// 사용자가 작성한 메시지
					String msg = chatPane.message.getText();	
					if (msg.length() == 0) return;
						
					// 시크릿메시지 전송 요청
					sendMsg("9230|"+ selectedUser + "," +msg);
					
					// 전송한 텍스트 삭제
					chatPane.message.setText("");
					chatPane.message.setCaretPosition(0);
					
					// 시크릿전송 대상 선택 해제 
					chatPane.userList.clearSelection();
				} 

			}
		});
		
		/*-- 채팅방-[뒤로가기] 라벨 --*/
		chatPane.lbMoveHome.addMouseListener(new MouseAdapter() { 
			@Override
			public void mouseClicked(MouseEvent arg0) { 
				// 채팅방 퇴장 알림
				sendMsg("9299|");
				
				// 채팅창 -> 홈 화면전환
				chatPane.setVisible(false);
				homePane.setVisible(true);
			}
		}); 
	}
	
	// 버튼에 대한 이벤트 처리
	@Override
	public void actionPerformed(ActionEvent e) {
		Object ob = e.getSource();
		/*-- 홈-[방 만들기] --*/
		if (ob == homePane.btnCreate) {
			String title = JOptionPane.showInputDialog(homePane, "채팅방 이름: ");	
			if (title == null) return;			
			// 채팅방 생성 요청
			sendMsg("9110|"+title);
			
			// 자신이 속한 채팅방 인원정보 갱신 요청
			sendMsg("9270|");
			
			// 채팅방 이름 변경
			chatPane.lbTitle.setText(title);
			// 메시지 입력란으로 포커스 맞추기
			chatPane.message.requestFocus();
			// 홈 -> 채팅창 화면전환
			homePane.setVisible(false);
			chatPane.setVisible(true);
		}
		
		
		/*-- 홈-[방 들어가기] --*/
		if (ob == homePane.btnEnter) {
			// 선택된 방이 없을 경우
			if(selectedRoomName == null) {
				JOptionPane.showMessageDialog(homePane, "들어갈 방을 선택해주세요.");
				return;
			}
			
			// 채팅방 입장 요청
			sendMsg("9200|"+selectedRoomName);
			
			// 채팅방 이름 변경
			chatPane.lbTitle.setText(selectedRoomName);
			selectedRoomName = null;
			// 메시지 입력란으로 포커스 맞추기
			chatPane.message.requestFocus();
			// 홈 -> 채팅창 화면전환
			homePane.setVisible(false);
			chatPane.setVisible(true);
		} 
		
		/*-- 홈-[새로고침] --*/
		if (ob == homePane.btnUpdate) {
			sendMsg("9180|");
			
			// 채팅목록의 인원정보를 없앰
			String roomMembers_delete[] = {""};
			homePane.roomMember.setListData(roomMembers_delete);
			
			// 채팅방 선택 해제 
			homePane.roomInfo.clearSelection();
			selectedRoomName = null;
		}
		
		
		/*-- 홈-[나가기] --*/
		if (ob == homePane.btnExit) {
			exit(0);
		}
		
		
		/*-- 채팅창-[전송] --*/
		if (ob == chatPane.btnSend) {
			// 사용자가 작성한 메시지
			String msg = chatPane.message.getText();
			if (msg.length() == 0) return;
			
			// 채팅방 소속인원에게 메시지 전송 요청
			sendMsg("9220|"+msg);
			
			// 전송한 텍스트 삭제
			chatPane.message.setText("");
			chatPane.message.setCaretPosition(0);
		}
	}
	
	// 윈도우 'X'버튼 종료처리
	public void closeWindowEvent() {
		// 챗홈에서 'X'버튼을 누를 경우 종료함수 요청
		homePane.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				exit(0);
			}
		});
		
		// 채팅창에서 'X'버튼을 누를 경우 종료함수 요청
		chatPane.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				exit(1);
			}
		});
	}
		
	// 종료함수
	public void exit(int window) {
		// 1: 채팅창, 2: 챗홈
		if (window == 1) sendMsg("9299|");	// 채팅방 퇴장 알림
		sendMsg("9199|");					// 채팅프로그램 종료 알림
		try {
			this.stop = true;		// 서버 데이터 수신하는 쓰레드 종료
			this.sock.close();		// 소켓 종료
			System.exit(0);			// 프로그램 종료
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// main function
	public static void main(String[] args) {
		new ChatClient();
	}
}
