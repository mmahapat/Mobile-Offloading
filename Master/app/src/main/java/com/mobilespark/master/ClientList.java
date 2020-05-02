package com.mobilespark.master;

import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.format.Formatter;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import com.mobilespark.master.Pojos.ClientListData;

import java.net.InetAddress;
import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;

public class ClientList extends AppCompatActivity {
    private static final String TAG = "ClientList";
    private String localIp = "";
    private ListView list;
    private ArrayList<ClientListData> clientData = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_list);
        clientData = new ArrayList<>();
        list = findViewById(R.id.nodelist);

        ClientListAdapter adapter = new ClientListAdapter(this, clientData);
        list.setAdapter(adapter);
        WifiManager wm = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        localIp = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());

        try {
            new NetworkDiscovery(localIp, list, clientData).execute();
        } catch (Exception e) {
            Log.e(TAG, "onCreate: " + e.getMessage());
        }

    }

    @Override
    public void onBackPressed() {
        Toast.makeText(this, "Cannot go back when the server is running, Please stop the server",
                Toast.LENGTH_LONG).show();
        Log.e(TAG, "onBackPressed: " + "Back button pressed when server is running");

    }


    static class NetworkDiscovery extends AsyncTask<Void, ClientListData, Void> {

        private static final String TAG = "NetworkDiscovery";

        @Override
        protected void onProgressUpdate(ClientListData... values) {
//            super.onProgressUpdate(values);
            clientData.add(values[0]);
            ((ClientListAdapter) (listView.getAdapter())).notifyDataSetChanged();
        }

        private String localIp;
        ArrayList<ClientListData> clientData = new ArrayList<>();
        ListView listView;

        NetworkDiscovery(String localIp, ListView list, ArrayList<ClientListData> clientData) {
            this.localIp = localIp;
            this.listView = list;
            this.clientData = clientData;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Log.d(TAG, "Let's sniff the network");

            try {
                Log.d(TAG, "ipString: " + localIp);

                String prefix = localIp.substring(0, localIp.lastIndexOf(".") + 1);
                Log.d(TAG, "prefix: " + prefix);

                for (int i = 0; i < 255; i++) {
                    String testIp = prefix + i;
                    InetAddress address = InetAddress.getByName(testIp);
                    boolean reachable = address.isReachable(256);
                    String hostName = address.getCanonicalHostName();

                    if (reachable) {
                        ClientListData clientListData = new ClientListData("Mobile 1", "20%", testIp);
                        Log.i(TAG, "Host: " + hostName + "(" + testIp + ") is reachable!");
                        publishProgress(clientListData);
                    } else
                        Log.e(TAG, "Host: " + hostName + "(" + testIp + ") is not reachable!");
                }
            } catch (Throwable t) {
                Log.e(TAG, "Well that's not good.", t);
            }

            return null;
        }
    }
}