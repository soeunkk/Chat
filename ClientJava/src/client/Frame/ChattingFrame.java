package client.Frame;

import ect.RoundedButton;
import ect.ColorPane;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

public class ChattingFrame extends JFrame {
	public JList<String>	userList;
	public JLabel			lbTitle, lbMoveHome;
	public RoundedButton 	btnSend;
	public ColorPane 		tpChat;
	public JTextArea 		message;

	private boolean 		isOpenList = false;
	
	// »ö»ó Á¾·ù
	Color buttonColor = new Color(163, 175, 201);
	Color deepBlue = new Color(142, 168, 219);
	Color lightBlue = new Color(217, 229, 255);
	
	// »ý¼ºÀÚ
	public ChattingFrame() {
		setTitle("Chat Program");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		//panel
		JPanel mainPanel = new JPanel();
		placeChattingPanel(mainPanel);
		mainPanel.setLayout(null);
		add(mainPanel);
		
		setBounds(100,100,355,570);
		setVisible(false);
	}
	
	protected void placeChattingPanel(JPanel p) {
		JPanel boardPane = new JPanel();
		boardPane.setBackground(deepBlue);
		boardPane.setBounds(0,0,350,440);
		p.add(boardPane);
		boardPane.setLayout(null);
		
		lbTitle = new JLabel("");
		lbTitle.setBounds(0,0,350,45);
		lbTitle.setHorizontalAlignment(SwingConstants.CENTER);
		lbTitle.setFont(new Font("¸¼Àº °íµñ", Font.BOLD, 15));
		lbTitle.setForeground(Color.WHITE);
		boardPane.add(lbTitle);
		
		JScrollPane spChat = new JScrollPane();
		spChat.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		spChat.setBounds(0,45,350,395);
		boardPane.add(spChat);
		
		tpChat = new ColorPane();
		tpChat.setFont(new Font("¸¼Àº °íµñ", Font.PLAIN, 12));
		tpChat.setBackground(lightBlue);
		spChat.setViewportView(tpChat);
		tpChat.setText("");
		
		userList = new JList<String>();
		userList.setBorder(new TitledBorder("ÇöÀç ¸â¹ö"));
		userList.setBackground(Color.WHITE);
		userList.setFont(new Font("¸¼Àº °íµñ", Font.PLAIN, 12));
		spChat.setColumnHeaderView(userList);
		userList.setVisible(false);
		userList.setVisibleRowCount(0);
		userList.setAutoscrolls(true);
		
		JLabel lbUserList = new JLabel("¡Õ"); 
		lbUserList.addMouseListener(new MouseAdapter() { 
			@Override 
			public void mouseClicked(MouseEvent arg0) { 
				userListControl(); 
			} 
		}); 
		lbUserList.setFont(new Font("¸¼Àº °íµñ", Font.BOLD, 36));
		lbUserList.setHorizontalAlignment(SwingConstants.CENTER);
		lbUserList.setBounds(12, 0, 40, 40);
		boardPane.add(lbUserList);
		
		lbMoveHome = new JLabel("¡ç"); 
		lbMoveHome.setFont(new Font("¸¼Àº °íµñ", Font.BOLD, 32));
		lbMoveHome.setHorizontalAlignment(SwingConstants.CENTER);
		lbMoveHome.setBounds(294, 0, 32, 40);
		boardPane.add(lbMoveHome);
		
		JScrollPane spMessage = new JScrollPane();
		spMessage.setBounds(10, 450, 259, 70);
		p.add(spMessage);
		
		message = new JTextArea();
		message.setLineWrap(true);
		message.setWrapStyleWord(true);
		spMessage.setViewportView(message);
		
		btnSend = new RoundedButton("Àü¼Û");
		btnSend.setFont(new Font("¸¼Àº °íµñ", Font.BOLD, 12));
		btnSend.setForeground(Color.WHITE);
		btnSend.setBackground(deepBlue);
		btnSend.setBounds(275, 450, 55, 30);
		p.add(btnSend);
	}
	
	
	private void userListControl() {
		if (isOpenList) {
			userListClose();
		} else {
			userListOpen();
		}
	}
	
	public void userListOpen() {
		userList.setVisible(true);
		userList.setVisibleRowCount(8);
		isOpenList = true;
	}
	
	public void userListClose() {
		userList.setVisible(false);
		userList.setVisibleRowCount(0);
		isOpenList = false;
	}
}
