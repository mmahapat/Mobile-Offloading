package com.mobilespark.master;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
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
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                }
            });
            TextView clientName = convertView.findViewById(R.id.clientName);
            clientName.setText(clientData.clientName);
            TextView battery = convertView.findViewById(R.id.battery);
            battery.setText(clientData.batteryPercentage);
            TextView clientIp = convertView.findViewById(R.id.clientIp);
            clientIp.setText(clientData.clientIp);
        }
        return convertView;
    }
}
