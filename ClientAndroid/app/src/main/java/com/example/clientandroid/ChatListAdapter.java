package com.example.clientandroid;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class ChatListAdapter extends BaseAdapter {
    Context context;
    LayoutInflater inflater;
    int layout;
    ArrayList<ChatMessageItem> src;

    public ChatListAdapter(Context context, int layout, ArrayList<ChatMessageItem> src) {
        this.context = context;
        this.layout = layout;
        this.src = src;

        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() { return this.src.size(); }

    @Override
    public ChatMessageItem getItem(int position) { return this.src.get(position); }

    @Override
    public long getItemId(int position) { return position; }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(layout, parent, false);
        }

        // 알림인 경우 (입장/퇴장)
        if (src.get(position).getState() == 0) {
            // 글씨체 기본설정
            TextView tv_text = (TextView) convertView.findViewById(R.id.c_text);
            tv_text.setTextColor(Color.BLACK);     // 검정 글씨
            tv_text.setTypeface(Typeface.DEFAULT); // 일반

            // 값 채우기 (알림 내용)
            TextView tv_Id = (TextView) convertView.findViewById(R.id.c_id);
            tv_Id.setText(src.get(position).getID());

            // 중앙 정렬
            LinearLayout tv_chatLout = (LinearLayout) convertView.findViewById(R.id.c_chat_layout);
            tv_chatLout.setGravity(Gravity.CENTER);

            // 채팅 메시지 내용을 위한 UI 없애기
            LinearLayout tv_textLout = (LinearLayout) convertView.findViewById(R.id.c_text_layout);
            tv_textLout.setPadding(0,0,0,0);
            tv_textLout.setVisibility(View.INVISIBLE);
        }
        // 채팅 메시지인 경우
        else if (src.get(position).getState() >= 1 && src.get(position).getState() <= 4) {
            // 글씨체 기본설정
            TextView tv_text = (TextView) convertView.findViewById(R.id.c_text);
            tv_text.setTextColor(Color.BLACK);     // 검정 글씨
            tv_text.setTypeface(Typeface.DEFAULT); // 일반

            // 채팅 메시지 내용을 위한 UI 보여주기
            LinearLayout tv_textLout = (LinearLayout) convertView.findViewById(R.id.c_text_layout);
            tv_textLout.setPadding(23,23,23,23);
            tv_textLout.setVisibility(View.VISIBLE);

            // 값 채우기 (유저 아이디, 메시지 내용)
            TextView tv_Id = (TextView) convertView.findViewById(R.id.c_id);
            tv_Id.setText(src.get(position).getID());
            tv_text.setText(src.get(position).getMessage());

            LinearLayout tv_chatLout = (LinearLayout) convertView.findViewById(R.id.c_chat_layout);
            // 나, 상대방에 따라 UI 변경 (나: 오른쪽, 주황색 / 상대방: 왼쪽, 하늘색)
            if (src.get(position).getState() == 1 || src.get(position).getState() == 3) {
                // 자신의 아이디 UI에서 없애기
                tv_Id.setText("");
                tv_Id.setPadding(0,0,0,0);

                tv_chatLout.setGravity(Gravity.RIGHT);  // 오른쪽 정렬
                tv_textLout.setBackgroundColor(Color.parseColor("#55FCAD3F"));
            } else if (src.get(position).getState() == 2 || src.get(position).getState() == 4) {
                // 자신의 아이디 UI에서 보여주기
                tv_Id.setPadding(15,0,15,15);

                tv_chatLout.setGravity(Gravity.LEFT);   // 왼쪽 정렬
                tv_textLout.setBackgroundColor(Color.parseColor("#443366FF"));
            }

            // 비밀 메시지인 경우
            if (src.get(position).getState() == 3 || src.get(position).getState() == 4) {
                tv_text.setTextColor(Color.RED);     // 빨간 글씨
                tv_text.setTypeface(Typeface.DEFAULT_BOLD); // 굵게
            }
        }
        return convertView;
    }
}
