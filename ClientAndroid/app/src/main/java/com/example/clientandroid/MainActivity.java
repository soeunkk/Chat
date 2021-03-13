package com.example.clientandroid;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements Runnable {
    // for Socket
    int port = 11692;
    static Socket sock;
    static boolean stop = false;
    BufferedReader receiveRead;
    static OutputStream ostream;

    // CharListAdpater에게 소켓 내용 공유 (시간차에 의한 겹침문제 해결)
    static String server_msg01 = " ";
    static String server_msg02 = " ";
    static String server_msg03 = " ";
    static String server_msg04 = " ";
    static String server_msg05 = " ";

    // for Activity
    static String user_ID;
    ArrayList<RoomTitleItem> roomList;
    RoomListAdapter roomListAdapter;
    Intent intent = new Intent();

    int[] style = { R.drawable.chat_1, R.drawable.chat_2, R.drawable.chat_3, R.drawable.chat_4, R.drawable.chat_5, R.drawable.chat_6, R.drawable.chat_7, R.drawable.chat_8, R.drawable.chat_9 };

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkDangerousPermissions();

        // 채팅방 목록 UI
        roomList = new ArrayList<RoomTitleItem>();
        roomListAdapter = new RoomListAdapter(this, R.layout.roomrow, roomList);
        ListView lv1 = (ListView) findViewById(R.id.roomlist);
        lv1.setAdapter(roomListAdapter);

        /* 연결 요청 */
        Thread connectThread = new Thread() {
            public void run() {
                try {
                    // 해당 (IP, 포트)로 연결 시도
                    sock = new Socket("222.111.4.158", port);

                    // 소켓과 I/O스트림 연결
                    receiveRead = new BufferedReader(new InputStreamReader(sock.getInputStream(),"EUC_KR"));
                    ostream = sock.getOutputStream();
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        connectThread.start();
        try {
            connectThread.join();   // 서버와 연결되기까지 기다림
            sendMsg("9100|");       // 채팅프로그램 접속
        } catch (InterruptedException ex) {}

        // 아이디 입력 및 서버에게 알림
        enterID();

        // 이벤트 처리
        registerEvent();

        // 서버와 통신 (서버의 메시지 수신 및 처리)
        new Thread(this).start();
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

    @Override
    public void run() {
        try {
            // 창을 끄기 전까지
            while(!stop) {
                // 서버에서 수신한 데이터 읽기
                final String msg = receiveRead.readLine();

                // 클라이언트측 콘솔창에 수신내용 출력
                Log.i("Socket", "from Server: "+msg+";");

                // 데이터 분할
                String msgs[] = msg.split("\\|");	// msgs[0]: 프로토콜, msgs[1]: 부가정보
                String protocol = msgs[0];

                switch(protocol) {
                    // 채팅방 목록 갱신
                    // "채팅방1--6--야야야--2020/11/30 11:24:33,채팅방2--3--안녕안녕--2020/11/30 11:48:32,채팅방3--8--헐진짜??--2020/11/30 12:43:94"
                    case "0111":
                        String rooms[] = {""};
                        // 채팅방 단위로 쪼개기
                        if (msgs.length > 1) rooms = msgs[1].split(",");
                        roomList.clear();   // 채팅방 목록 UI 초기화

                        // (방제목, 인원, 최근 내용, 시각)으로 쪼개기
                        if (rooms.length > 0) {
                            String room_data[] = {""};

                            // 채팅방 목록 항목 채우기
                            for (int i = 0; i < rooms.length; i++) {
                                if (!rooms[i].equals("")) {
                                    RoomTitleItem r = new RoomTitleItem();
                                    room_data = rooms[i].split("--");
                                    r.setTitle(room_data[0]);
                                    r.setCount(Integer.valueOf(room_data[1]));
                                    r.setLmsg(room_data[2]);
                                    r.setLtime(room_data[3]);
                                    r.setColor(style[i % style.length]);
                                    roomList.add(r);
                                }
                            }
                            // 채팅 목록을 날짜 순으로 내림차순
                            Collections.sort(roomList, new AscendingInteger());
                            // 메인 쓰레드에서 View 갱신
                            MainActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    // 채팅목록 갱신
                                    roomListAdapter.notifyDataSetChanged();
                                }
                            });
                        }
                        break;

                    // 채팅방 입장 출력
                    // "0205|김소은"
                    case "0205":
                        // 동기화를 맞춰주기 위해 시간 지연
                        // (자신이 입장하는 경우 = Activity가 만들어짐)
                        if (msgs[1].equals(user_ID)) {
                            try {
                                TimeUnit.SECONDS.sleep(1);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        server_msg01 = msg;
                        intent.setAction(getString(R.string.broadcast_01));
                        sendBroadcast(intent);
                        break;

                    // 채팅방 퇴장 출력
                    // "0295|김소은"
                    case "0295":
                        server_msg02 = msg;
                        intent.setAction(getString(R.string.broadcast_02));
                        sendBroadcast(intent);
                        break;

                    // 일반 메시지 출력
                    // "0220|[김소은]: 안녕하세요!"
                    case "0220":
                        server_msg03 = msg;
                        intent.setAction(getString(R.string.broadcast_03));
                        sendBroadcast(intent);
                        break;

                    // 시크릿 메시지 출력
                    // "0230|[김소은]: 안녕하세요"
                    case "0230":
                        server_msg04 = msg;
                        intent.setAction(getString(R.string.broadcast_04));
                        sendBroadcast(intent);
                        break;

                    // 채팅방 멤버정보 갱신
                    // "0270|김소은,김소명,김소준"
                    case "0270":
                        server_msg05 = msg;
                        intent.setAction(getString(R.string.broadcast_05));
                        sendBroadcast(intent);
                        break;

                }
            }
        } catch (IOException e) {
            // stop 여부를 한번 더 체크 (stop=true이면 정상)
            if (stop != true) e.printStackTrace();
        }
    }

    // 아이디 입력받기
    private void enterID() {
        // 입력 다이얼로그 생성
        AlertDialog.Builder ad = new AlertDialog.Builder(MainActivity.this);
        ad.setTitle("아이디 입력");
        ad.setMessage("아이디를 입력해주세요.");
        final EditText et = new EditText(MainActivity.this);
        ad.setView(et);

        // 확인 버튼 설정
        ad.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                user_ID = et.getText().toString();
                dialog.dismiss();
                sendMsg("9105|"+ user_ID);  // 아이디 등록 요청
            }
        });
        ad.show();
    }

    // 이벤트 등록
    private void registerEvent() {
        // 채팅방 만들기
        ImageButton createRoom = (ImageButton) findViewById(R.id.createRoom);
        createRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 입력 다이얼로그 생성
                AlertDialog.Builder ad = new AlertDialog.Builder(MainActivity.this);
                ad.setTitle("방제목 입력");
                ad.setMessage("채팅방 제목을 입력해주세요.");
                final EditText et = new EditText(MainActivity.this);
                ad.setView(et);

                // 확인 버튼 설정
                ad.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        String title = et.getText().toString();
                        if (title == null) {}
                        else if (title.equals("")) {
                            Toast.makeText(MainActivity.this, "방 제목이 입력되지 않았습니다.", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            Intent intent = new Intent(MainActivity.this, ChatRoomActivity.class);
                            intent.putExtra("title", title);
                            startActivity(intent);

                            sendMsg("9110|" + title); // 채팅방 생성 요청
                        }
                    }
                });
                ad.show();
            }
        });

        // 방 들어가기
        ListView lv = (ListView) findViewById(R.id.roomlist);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                String title = roomList.get(position).getTitle();

                Intent intent = new Intent(MainActivity.this, ChatRoomActivity.class);
                intent.putExtra("title", title);
                startActivity(intent);

                sendMsg("9200|" + title);
            }
        });

        // 프로그램 종료
        ImageButton close = (ImageButton) findViewById(R.id.close);
        close.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Thread closeThread = new Thread() {
                    public void run() {
                        try {
                            ostream.write(("9199|").getBytes("EUC_KR"));
                            ostream.flush();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                };
                closeThread.start();
                try {
                    closeThread.join();             // 서버에게 종료요청을 보내고 나면 자신도 종료  
                    MainActivity.stop = true;		// 서버 데이터 수신하는 쓰레드 종료
                    MainActivity.sock.close();		// 소켓 종료
                } catch (IOException e1) {
                    e1.printStackTrace();
                } catch (InterruptedException e2) {
                    e2.printStackTrace();
                }
                finishAffinity();
                System.exit(0);
            }
        });
    }

    // RoomTitleItem 내림차순 정렬
    class AscendingInteger implements Comparator<RoomTitleItem> {
        @Override
        public int compare(RoomTitleItem a, RoomTitleItem b) {
            return b.compareTo(a); }
    }

    // Permission 체크
    private void checkDangerousPermissions() {
        // 사용해야 할 퍼미션
        String[] permissions = {
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.ACCESS_WIFI_STATE
        };

        int permissionCheck = PackageManager.PERMISSION_GRANTED;
        for (int i = 0 ; i < permissions.length ; i++) {
            permissionCheck = ContextCompat.checkSelfPermission(this, permissions[i]);
            if (permissionCheck == PackageManager.PERMISSION_DENIED) break;
        }

        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "권한 있음", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "권한 없음", Toast.LENGTH_LONG).show();

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[0])) {
                Toast.makeText(this, "권한 설명 필요함", Toast.LENGTH_LONG).show();
            } else {
                ActivityCompat.requestPermissions(this, permissions, 1);
            }
        }
    }
}