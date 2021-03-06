package com.mobilespark.master;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mobilespark.master.Pojos.ClientListData;

import java.util.ArrayList;

public class ClientListAdapter extends ArrayAdapter {

    ArrayList<ClientListData> list;
    Context context;

    public ClientListAdapter(Context context, ArrayList<ClientListData> list) {
        super(context, 0, list);
        this.list = list;
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        ClientListData clientData = list.get(position);
        if (convertView == null) {
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            convertView = layoutInflater.inflate(R.layout.list_row, null);
        }
        TextView clientName = convertView.findViewById(R.id.clientName);
        LinearLayout showStatusLayout = convertView.findViewById(R.id.showStatus);
        TextView battery = convertView.findViewById(R.id.battery);
        TextView clientStatus = convertView.findViewById(R.id.clientStatus);

        clientName.setText(clientData.clientName);
        double ba = Double.parseDouble(clientData.batteryPercentage);
        if (ba < 20) {
            battery.setTextColor(Color.parseColor("#bf1f1f"));
        } else if (ba < 70 && ba >= 21) {
            battery.setTextColor(Color.parseColor("#FFCC00"));
        } else {
            battery.setTextColor(Color.parseColor("#10b542"));
        }
        if (clientData.showStatus) {
            showStatusLayout.setVisibility(View.VISIBLE);
        }
        clientStatus.setText(clientData.status);
        if (clientData.status.equalsIgnoreCase("CONNECTED")
                || clientData.status.equalsIgnoreCase("COMPLETED ✓")
                || clientData.status.equalsIgnoreCase("AGREED")
                || clientData.status.equalsIgnoreCase("ASSIGNED")) {
            clientStatus.setTextColor(Color.parseColor("#10b542"));
        } else if (clientData.status.equalsIgnoreCase("ASSIGNED")
                || clientData.status.equalsIgnoreCase("FALLBACK")) {
            clientStatus.setTextColor(Color.parseColor("#fadc64"));
        } else if (clientData.status.equalsIgnoreCase("DISCONNECTED ✗")) {
            clientStatus.setTextColor(Color.parseColor("#bf1f1f"));
        } else {
            clientStatus.setTextColor(Color.parseColor("#fadc64"));
        }
        battery.setText(clientData.batteryPercentage + "%");
        TextView clientIp = convertView.findViewById(R.id.clientIp);
        clientIp.setText(clientData.clientIp);
        return convertView;
    }
}
