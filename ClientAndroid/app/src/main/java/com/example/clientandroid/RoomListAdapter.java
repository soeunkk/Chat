package com.example.clientandroid;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class RoomListAdapter extends BaseAdapter {
    Context context;
    LayoutInflater inflater;
    int layout;
    ArrayList<RoomTitleItem> src;

    public RoomListAdapter(Context context, int layout, ArrayList<RoomTitleItem> src) {
        this.context = context;
        this.layout = layout;
        this.src = src;

        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() { return this.src.size(); }

    @Override
    public RoomTitleItem getItem(int position) { return this.src.get(position); }

    @Override
    public long getItemId(int position) { return position; }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(layout, parent, false);
        }

        // 값 채우기 (채팅방 제목, 인원, 최근 메시지, 날짜)
        TextView tv_title = (TextView) convertView.findViewById(R.id.r_title);
        tv_title.setText(src.get(position).getTitle());

        TextView tv_count = (TextView) convertView.findViewById(R.id.r_count);
        tv_count.setText("("+String.valueOf(src.get(position).getCount())+")");

        TextView tv_Lmsg = (TextView) convertView.findViewById(R.id.r_lastmsg);
        tv_Lmsg.setText(src.get(position).getLmsg());

        TextView tv_Ltime = (TextView) convertView.findViewById(R.id.r_lastdate);
        String lastdate = src.get(position).getLtime();
        // 유효한 날짜 값이 들어있으면 초 단위 제거
        if (!lastdate.equals(" ")) tv_Ltime.setText(lastdate.substring(0, lastdate.length()-3));
        else tv_Ltime.setText(lastdate);

        // 색상 설정
        ImageView v_style = (ImageView) convertView.findViewById(R.id.r_circle);
        v_style.setBackgroundResource(src.get(position).getColor());

        return convertView;
    }
}
