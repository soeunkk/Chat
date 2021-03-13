package com.example.clientandroid;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.util.ArrayList;

import static com.example.clientandroid.MainActivity.ostream;
import static com.example.clientandroid.MainActivity.server_msg01;
import static com.example.clientandroid.MainActivity.server_msg02;
import static com.example.clientandroid.MainActivity.server_msg03;
import static com.example.clientandroid.MainActivity.server_msg04;
import static com.example.clientandroid.MainActivity.server_msg05;
import static com.example.clientandroid.MainActivity.user_ID;

public class ChatRoomActivity extends AppCompatActivity {
    TextView tv_msg;
    ArrayList<ChatMessageItem> chatList;
    ChatListAdapter chatListAdapter;
    ArrayList<String> memberList;
    ArrayAdapter<String> memberListAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chatting_layout);
        String title = getIntent().getStringExtra("title");

        // MainActivity에서 메시지를 받기 위한 BroadcastReceiver
        MyBroadcastReceiver myBroadcastReceiver = new MyBroadcastReceiver();
        IntentFilter myIntentFilter = new IntentFilter();
        myIntentFilter.addAction(getString(R.string.broadcast_01));
        myIntentFilter.addAction(getString(R.string.broadcast_02));
        myIntentFilter.addAction(getString(R.string.broadcast_03));
        myIntentFilter.addAction(getString(R.string.broadcast_04));
        myIntentFilter.addAction(getString(R.string.broadcast_05));
        registerReceiver(myBroadcastReceiver, myIntentFilter);

        // 채팅방 제목 UI 변경
        TextView tv_title = (TextView) findViewById(R.id.title);
        tv_title.setText(title);

        // 채팅 내용 UI
        chatList = new ArrayList<ChatMessageItem>();
        chatListAdapter = new ChatListAdapter(this, R.layout.chatrow, chatList);
        ListView lv1 = (ListView) findViewById(R.id.chatlist);
        lv1.setAdapter(chatListAdapter);

        // 멤버 UI
        memberList = new ArrayList<String>();
        memberListAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, memberList);
        ListView lv2 = (ListView) findViewById(R.id.memberlist);
        lv2.setAdapter(memberListAdapter);

        tv_msg = (TextView) findViewById(R.id.message);

        // 이벤트 처리
        registerEvent();
    }

    // 서버에게 데이터 송신
    public void sendMsg(String msg) {
        final String message = msg;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ostream.write((message+'\n').getBytes("EUC_KR"));
                    ostream.flush();    //
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    // MainActivity에서 보내온 Broadcast 수신 (소켓통신 내용전달)
    public class MyBroadcastReceiver extends BroadcastReceiver {

        public MyBroadcastReceiver() {
            super();
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            // 채팅방 입장 출력
            // "0205|김소은"
            if (intent.getAction() != null && intent.getAction().equals(context.getString(R.string.broadcast_01))) {
                if (!server_msg01.equals(" ")) {
                    ChatMessageItem c = new ChatMessageItem();
                    String alarm_msg = server_msg01.split("\\|")[1] + " 님이 입장하셨습니다.";
                    c.setID(alarm_msg);
                    c.setState(0);      // 공지사항

                    // 채팅 항목 추가
                    chatList.add(c);
                    chatListAdapter.notifyDataSetChanged(); // 채팅 내용 갱신
                }
            }

            // 채팅방 퇴장 출력
            // "0295|김소은"
            else if (intent.getAction() != null && intent.getAction().equals(context.getString(R.string.broadcast_02))) {
                if (!server_msg02.equals(" ")) {
                    ChatMessageItem c = new ChatMessageItem();
                    String alarm_msg = server_msg02.split("\\|")[1] + " 님이 퇴장하셨습니다.";
                    c.setID(alarm_msg);
                    c.setState(0);      // 공지사항

                    // 채팅 항목 추가
                    chatList.add(c);
                    chatListAdapter.notifyDataSetChanged(); // 채팅 내용 갱신
                }
            }

            // 일반 메시지 출력
            // "0220|[김소은]: 안녕하세요!"
            else if (intent.getAction() != null && intent.getAction().equals(context.getString(R.string.broadcast_03))) {
                if (!server_msg03.equals(" ")) {
                    ChatMessageItem c = new ChatMessageItem();
                    // 상대방 ID, 메시지 저장
                    String user = server_msg03.split("]")[0].substring(6);
                    c.setID(user);
                    String message = server_msg03.split("]", 2)[1].substring(2);
                    c.setMessage(message);

                    // ChatMessageItem의 상태 설정
                    if (user.equals(user_ID)) // 자신이 보낸 메시지
                        c.setState(1);
                    else                      // 상대가 보낸 메시지
                        c.setState(2);

                    // 채팅 항목 추가
                    chatList.add(c);
                    chatListAdapter.notifyDataSetChanged(); // 채팅 내용 갱신
                }
            }

            // 시크릿 메시지 출력
            // "0230|[김소은]: 안녕하세요!"
            else if (intent.getAction() != null && intent.getAction().equals(context.getString(R.string.broadcast_04))) {
                if (!server_msg04.equals(" ")) {
                    ChatMessageItem c = new ChatMessageItem();
                    // 상대방 ID, 메시지 저장
                    String user = server_msg04.split("]")[0].substring(6);
                    c.setID(user);
                    String message = server_msg04.split("]", 2)[1].substring(2);
                    c.setMessage(message);

                    // ChatMessageItem의 상태 설정
                    if (user.equals(user_ID)) // 자신이 보낸 메시지
                        c.setState(3);
                    else                      // 상대가 보낸 메시지
                        c.setState(4);

                    // 채팅 항목 추가
                    chatList.add(c);
                    chatListAdapter.notifyDataSetChanged(); // 채팅 내용 갱신
                }
            }

            // 채팅방 멤버정보 갱신
            // "0270|김소은,김소명,김소준"
            else if (intent.getAction() != null && intent.getAction().equals(context.getString(R.string.broadcast_05))) {
                if (!server_msg05.equals(" ")) {
                    String members[] = {""};
                    // 채팅방 단위로 쪼개기
                    if (server_msg05.split("\\|")[1].length() > 1)
                        members = server_msg05.split("\\|")[1].split(",");
                    memberList.clear();   // 채팅방 목록 UI 초기화

                    // (방제목, 인원)으로 쪼개기
                    if (members.length > 0) {
                        // 채팅방 목록 항목 채우기
                        int i;
                        for (i = 0; i < members.length; i++) {
                            memberList.add(members[i]);
                        }
                        memberListAdapter.notifyDataSetChanged(); // 채팅 내용 갱신

                        // 채팅방 제목 UI 변경
                        TextView tv_title = (TextView) findViewById(R.id.title);
                        tv_title.setText(String.valueOf(tv_title.getText()).split("\\(")[0] + "(" + String.valueOf(i) + ")");

                    }
                }
            }
        }
    }

    // 이벤트 등록
    private void registerEvent() {
        // 메시지 보내기
        Button send = (Button) findViewById(R.id.sendMsg);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (tv_msg.getText() != null && !String.valueOf(tv_msg.getText()).equals("")) {
                    String msg = String.valueOf(tv_msg.getText());
                    sendMsg("9220|"+msg);   // 채팅원 멤버에게 메시지 전송 요청

                    // 전송한 텍스트 삭제
                    tv_msg.setText("");
                }
            }
        });

        //채팅방 멤버 보기
        final TextView member_btn = (TextView) findViewById(R.id.show_member);
        member_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ListView lv = (ListView) findViewById(R.id.memberlist);
                String tag = String.valueOf(lv.getTag());

                // Visible -> Invisible
                if (tag.equals("1")) {
                    lv.setTag("2");
                    lv.setVisibility(View.INVISIBLE);
                }
                // Invisible -> Visible
                else if (tag.equals("2")) {
                    lv.setTag("1");
                    lv.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    @Override
    // 채팅방 나갈경우
    public void onBackPressed() {
        super.onBackPressed();
        sendMsg("9299|");   // 채팅방 퇴장 알림
        sendMsg("9180|");   // 채팅홈 새로고침
    }
}