package client.Frame;

import ect.RoundedButton;

import java.awt.Color;

import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.TitledBorder;

public class ChatHomeFrame extends JFrame {
	public JList<String> 	roomInfo, roomMember, waitUser;
	public JScrollPane 		spRoomInfo, sproomMember, spwaitUser;
	public RoundedButton 	btnCreate, btnEnter, btnExit, btnUpdate;
	
	// ���� ����
	Color deepBlue = new Color(142, 168, 219);
		
	// ������
	public ChatHomeFrame() {
		setTitle("Chat Program");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		//panel
		JPanel mainPanel = new JPanel();
		placeHomePanel(mainPanel);
		mainPanel.setLayout(null);
		mainPanel.setBackground(deepBlue);
		add(mainPanel);
		
		setBounds(300,200, 500, 540);
		setVisible(true);
	}
	
	protected void placeHomePanel(JPanel p) {
		roomInfo = new JList<String>();
		roomInfo.setBorder(new TitledBorder("ä�ù� ���"));
	    spRoomInfo = new JScrollPane(roomInfo);
		spRoomInfo.setBounds(10, 10, 300, 270);
		p.add(spRoomInfo);
		
		roomMember = new JList<String>();
		roomMember.setBorder(new TitledBorder("ä�ø��"));
		sproomMember = new JScrollPane(roomMember);
	    sproomMember.setBounds(10, 330, 460, 160);
	    p.add(sproomMember);
	    
	    waitUser = new JList<String>();
		waitUser.setBorder(new TitledBorder("����ο�"));
	    spwaitUser = new JScrollPane(waitUser);
	    spwaitUser.setBounds(320, 50, 150, 230);
	    p.add(spwaitUser);
	    
	    btnCreate = new RoundedButton("ä�ù� ����");
	    btnCreate.setBounds(10,290,145,30);
	    p.add(btnCreate);
	    
	    btnEnter = new RoundedButton("ä�ù� ����");
	    btnEnter.setBounds(165,290,145,30);
	    p.add(btnEnter);

		btnUpdate = new RoundedButton("���ΰ�ħ");
	    btnUpdate.setBounds(320,290,150,30);
	    p.add(btnUpdate);
	    
	    btnExit = new RoundedButton("������");
	    btnExit.setBounds(320,10,150,30);
	    p.add(btnExit);
	}
}
