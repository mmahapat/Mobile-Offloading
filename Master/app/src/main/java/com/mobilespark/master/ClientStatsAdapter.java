package com.mobilespark.master;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.mobilespark.master.Pojos.ClientStatData;

import java.util.List;

public class ClientStatsAdapter extends ArrayAdapter {

    List<ClientStatData> list;
    Context context;

    public ClientStatsAdapter(Context context, List<ClientStatData> list) {
        super(context, 0, list);
        this.list = list;
        this.context = context;
    }

    @Override
    public View getView(int position, @Nullable View convertView, ViewGroup parent) {

        ClientStatData statData = list.get(position);
        if (convertView == null) {
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            convertView = layoutInflater.inflate(R.layout.stat_list_row, null);


            TextView clientName = convertView.findViewById(R.id.clientName);
            clientName.setText(statData.clientName);
            TextView time = (TextView) convertView.findViewById(R.id.timeValue);
            time.setText(Long.toString(statData.timeTaken));
            TextView power = (TextView) convertView.findViewById(R.id.powerValue);
            power.setText(Float.toString(statData.powerConsumed));
            LinearLayout showStatusLayout = convertView.findViewById(R.id.showStatus);
            TextView clientStatus = convertView.findViewById(R.id.clientStatus);
            clientStatus.setText(statData.status);
            if (statData.status.equalsIgnoreCase("Success ✓")) {
                clientStatus.setTextColor(Color.parseColor("#10b542"));
            }
            else if (statData.status.equalsIgnoreCase("Failed ✗")) {
                clientStatus.setTextColor(Color.parseColor("#bf1f1f"));
            } else {
                clientStatus.setTextColor(Color.parseColor("#FFCC00"));
            }

        }
        return convertView;
    }
}
