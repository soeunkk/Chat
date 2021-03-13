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
	
	// for ����
	int 	port = 11692;
	Socket 	sock;
	boolean stop = false;			// ���� ���� ���ɿ���
	BufferedReader 	receiveRead;	// ������ ������ ���ſ� 
	OutputStream 	ostream;		// �������� ������ �۽ſ� ��Ʈ��
	
	// for UI
	ChattingFrame chatPane;
	ChatHomeFrame homePane;
	
	String selectedRoomName;
	
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
	public ChatClient() {		
		chatPane = new ChattingFrame();
		homePane = new ChatHomeFrame();
	    
		// ������ ���� �õ�
		connect();
		// �������� ������ ������ �б�(������)
		new Thread(this).start();
		
		// ä�����α׷� ����
		sendMsg("9100|");
		
		// ���̵� �Է¹���
		String user_ID = JOptionPane.showInputDialog(homePane, "���̵�");
		sendMsg("9105|"+ user_ID);
		
		// �̺�Ʈ ����
		registerEvent();
		
		// ������ 'X'��ư ����ó��
		closeWindowEvent();
	}
	
	// �������� ���� ��û
	public void connect() {
		try {
			// �ش� (IP, ��Ʈ)�� ���� �õ�
			sock = new Socket("222.111.4.158", port);
			
			// ���ϰ� I/O��Ʈ�� ����
			receiveRead = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			ostream = sock.getOutputStream();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// �������� ������ �۽�
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
			// â�� ���� ������ 
			while(!stop) {
				// �������� ������ ������ �б�
				String msg = receiveRead.readLine();
				
				// Ŭ���̾�Ʈ�� �ܼ�â�� ���ų��� ���
				System.out.println("from Server: "+msg+";"+sock.getInetAddress().getHostAddress());
				
				// ������ ����
				String msgs[] = msg.split("\\|");	// msgs[0]: ��������, msgs[1]: �ΰ����� 
				String protocol = msgs[0];
				
				switch(protocol) {
				// ä�ù� ��� ����
				// "0110|ä�ù�1--6,ä�ù�2--3,ä�ù�3--8"
				case "0110":
					String rooms[] = {""};
					// ä�ù� ������ �ɰ���
					if(msgs.length > 1) rooms = msgs[1].split(",");
					// ����Ʈ ������Ʈ
					homePane.roomInfo.setListData(rooms);
					break;
					
					
				// ���õ� ä�ù� �ο����� ���
				// "0170|�����,��Ҹ�,�����"
				case "0170":
					String members[] = {""};
					// ��� ������ �ɰ���
					if (msgs.length > 1) members = msgs[1].split(",");
					// ����Ʈ ������Ʈ
					homePane.roomMember.setListData(members);
					break;
					
					
				// ��� �ο����� ����
				// "0180|�ƺ�,����"
				case "0180":
					String waitUsers[] = {""};
					// ��� ������ �ɰ���
					if (msgs.length > 1) waitUsers = msgs[1].split(",");
					// ����Ʈ ������Ʈ
					homePane.waitUser.setListData(waitUsers);
					break;
					
					
				// ä�ù� �ʱ�ȭ
				// "0200|"
				case "0200":
					chatPane.tpChat.setText("");	// ä�ó��� ����
					chatPane.tpChat.setCaretPosition(0);
					chatPane.lbTitle.setText("");	// ä�ù� �̸� ����
					String empty_elm[] = {""}; 
					chatPane.userList.setListData(empty_elm);	// ä�ù� �Ҽ��ο� ����
					chatPane.userListClose();					// ��� ����Ʈâ �ݱ�
					break;
					
					
				// ä�ù� ���� ���
				// "0205|�����"
				case "0205":
					chatPane.tpChat.append(new Color(140,140,140), "["+msgs[1]+"]���� �����ϼ̽��ϴ�.\n");
					break;
					
					
				// ä�ø޽��� ���
				// "0220|[�����]:�ȳ��ϼ���!"
				case "0220":
					chatPane.tpChat.append(Color.BLACK, msgs[1]+"\n");
					break;
					
				// ��ũ���޽��� ���
				// "0230|[�����]:�ȳ��ϼ���!"
				case "0230":
					chatPane.tpChat.append(Color.RED, msgs[1]+"\n");
					break;	
					
				// �ڽ��� ä�ù� �ο����� ����
				// "0270|�����,��Ҹ�,�����"
				case "0270":
					String Mymembers[] = {""};
					// ��� ������ �ɰ���
					if (msgs.length > 1) Mymembers = msgs[1].split(",");
					// ����Ʈ ������Ʈ
					chatPane.userList.setListData(Mymembers);
					break;
					
				// ä�ù� ���� ���
				// "0295|�����"
				case "0295":
					chatPane.tpChat.append(new Color(140,140,140), "["+msgs[1]+"]���� �����ϼ̽��ϴ�.\n");
					break;
				}
			}
		} catch (IOException e) {
			// stop ���θ� �ѹ� �� üũ (stop=true�̸� ����)
			if (stop != true) e.printStackTrace();
		} 
	}
	
	// �̺�Ʈ ����
	private void registerEvent() {
		//ä�ø��(ChatHome)
		homePane.btnCreate.addActionListener(this);
		homePane.btnEnter.addActionListener(this);
		homePane.btnExit.addActionListener(this);
		homePane.btnUpdate.addActionListener(this);
			    
		//��ȭ��(ChatClient)
		chatPane.btnSend.addActionListener(this);
		
		/*-- Ȩ-ä�ù� ����� �׸� Ŭ�� --*/ 
		homePane.roomInfo.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				String room_data = homePane.roomInfo.getSelectedValue();
				if (room_data == null) return;
				
				// �������� �ش� ä�ù� �ο����� ��û
				selectedRoomName = room_data.substring(0, room_data.indexOf("-"));	// ä�ù� �̸��� ����
				sendMsg("9170|"+selectedRoomName);
			}
		});
		
		/*-- ä�ù�-ENTER �Է� --*/
		chatPane.message.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					// ����ڰ� �ۼ��� �޽���
					String msg = chatPane.message.getText();
					if (msg.length() == 0) return;
					
					// ä�ù� �Ҽ��ο����� �޽��� ���� ��û
					sendMsg("9220|"+msg);
					
					// ������ �ؽ�Ʈ ����
					chatPane.message.setText("");
					chatPane.message.setCaretPosition(0);
				}
			}
		});
		
		/*-- ä�ù�-Ư����� ����Ŭ�� --*/ 
		chatPane.userList.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					String selectedUser = chatPane.userList.getSelectedValue();
					
					// ����ڰ� �ۼ��� �޽���
					String msg = chatPane.message.getText();	
					if (msg.length() == 0) return;
						
					// ��ũ���޽��� ���� ��û
					sendMsg("9230|"+ selectedUser + "," +msg);
					
					// ������ �ؽ�Ʈ ����
					chatPane.message.setText("");
					chatPane.message.setCaretPosition(0);
					
					// ��ũ������ ��� ���� ���� 
					chatPane.userList.clearSelection();
				} 

			}
		});
		
		/*-- ä�ù�-[�ڷΰ���] �� --*/
		chatPane.lbMoveHome.addMouseListener(new MouseAdapter() { 
			@Override
			public void mouseClicked(MouseEvent arg0) { 
				// ä�ù� ���� �˸�
				sendMsg("9299|");
				
				// ä��â -> Ȩ ȭ����ȯ
				chatPane.setVisible(false);
				homePane.setVisible(true);
			}
		}); 
	}
	
	// ��ư�� ���� �̺�Ʈ ó��
	@Override
	public void actionPerformed(ActionEvent e) {
		Object ob = e.getSource();
		/*-- Ȩ-[�� �����] --*/
		if (ob == homePane.btnCreate) {
			String title = JOptionPane.showInputDialog(homePane, "ä�ù� �̸�: ");	
			if (title == null) return;			
			// ä�ù� ���� ��û
			sendMsg("9110|"+title);
			
			// �ڽ��� ���� ä�ù� �ο����� ���� ��û
			sendMsg("9270|");
			
			// ä�ù� �̸� ����
			chatPane.lbTitle.setText(title);
			// �޽��� �Է¶����� ��Ŀ�� ���߱�
			chatPane.message.requestFocus();
			// Ȩ -> ä��â ȭ����ȯ
			homePane.setVisible(false);
			chatPane.setVisible(true);
		}
		
		
		/*-- Ȩ-[�� ����] --*/
		if (ob == homePane.btnEnter) {
			// ���õ� ���� ���� ���
			if(selectedRoomName == null) {
				JOptionPane.showMessageDialog(homePane, "�� ���� �������ּ���.");
				return;
			}
			
			// ä�ù� ���� ��û
			sendMsg("9200|"+selectedRoomName);
			
			// ä�ù� �̸� ����
			chatPane.lbTitle.setText(selectedRoomName);
			selectedRoomName = null;
			// �޽��� �Է¶����� ��Ŀ�� ���߱�
			chatPane.message.requestFocus();
			// Ȩ -> ä��â ȭ����ȯ
			homePane.setVisible(false);
			chatPane.setVisible(true);
		} 
		
		/*-- Ȩ-[���ΰ�ħ] --*/
		if (ob == homePane.btnUpdate) {
			sendMsg("9180|");
			
			// ä�ø���� �ο������� ����
			String roomMembers_delete[] = {""};
			homePane.roomMember.setListData(roomMembers_delete);
			
			// ä�ù� ���� ���� 
			homePane.roomInfo.clearSelection();
			selectedRoomName = null;
		}
		
		
		/*-- Ȩ-[������] --*/
		if (ob == homePane.btnExit) {
			exit(0);
		}
		
		
		/*-- ä��â-[����] --*/
		if (ob == chatPane.btnSend) {
			// ����ڰ� �ۼ��� �޽���
			String msg = chatPane.message.getText();
			if (msg.length() == 0) return;
			
			// ä�ù� �Ҽ��ο����� �޽��� ���� ��û
			sendMsg("9220|"+msg);
			
			// ������ �ؽ�Ʈ ����
			chatPane.message.setText("");
			chatPane.message.setCaretPosition(0);
		}
	}
	
	// ������ 'X'��ư ����ó��
	public void closeWindowEvent() {
		// êȨ���� 'X'��ư�� ���� ��� �����Լ� ��û
		homePane.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				exit(0);
			}
		});
		
		// ä��â���� 'X'��ư�� ���� ��� �����Լ� ��û
		chatPane.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				exit(1);
			}
		});
	}
		
	// �����Լ�
	public void exit(int window) {
		// 1: ä��â, 2: êȨ
		if (window == 1) sendMsg("9299|");	// ä�ù� ���� �˸�
		sendMsg("9199|");					// ä�����α׷� ���� �˸�
		try {
			this.stop = true;		// ���� ������ �����ϴ� ������ ����
			this.sock.close();		// ���� ����
			System.exit(0);			// ���α׷� ����
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// main function
	public static void main(String[] args) {
		new ChatClient();
	}
}
