package com.mobilespark.master;

import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.mobilespark.master.Pojos.ClientListData;

import java.net.InetAddress;
import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;

public class ClientList extends AppCompatActivity {
    private static final String TAG = "ClientList";
    private String localIp = "";
    private ListView list;
    private ArrayList<ClientListData> clientData = new ArrayList<>();
    private Button _startTaskButton;
    private ImageButton _rescanButton;
    ProgressBar pb;
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_list);
        list = findViewById(R.id.nodelist);
        _startTaskButton = findViewById(R.id.startTask);
        _rescanButton = findViewById(R.id.rescan);
        pb = findViewById(R.id.progress_horizontal);
        textView = findViewById(R.id.percent);

        ClientListAdapter adapter = new ClientListAdapter(this, clientData);
        list.setAdapter(adapter);
        WifiManager wm = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        localIp = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());

        new NetworkDiscovery(localIp, list).execute();

        Log.e(TAG, "onCreate: " + clientData.size());

        _rescanButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                _rescanButton.setVisibility(View.GONE);
                clientData.clear();
                ((ClientListAdapter) (list.getAdapter())).notifyDataSetChanged();
                _startTaskButton.setEnabled(false);
                pb.setProgress(0);
                pb.setVisibility(View.VISIBLE);
                textView.setVisibility(View.VISIBLE);
                textView.setText("Scanning : 0%");
                new NetworkDiscovery(localIp, list).execute();
            }
        });

    }

    @Override
    public void onBackPressed() {
        Toast.makeText(this, "Cannot go back when the server is running, Please stop the server",
                Toast.LENGTH_LONG).show();
        Log.e(TAG, "onBackPressed: " + "Back button pressed when server is running");

    }


    class NetworkDiscovery extends AsyncTask<Void, Object, Void> {
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            textView.setVisibility(View.GONE);
            pb.setVisibility(View.GONE);
            Toast.makeText(ClientList.this, "Scan Complete",
                    Toast.LENGTH_LONG).show();
            if (clientData.size() < 1) {
                Toast.makeText(ClientList.this, "No Clients found, Please Rescan",
                        Toast.LENGTH_LONG).show();
            } else {
                _startTaskButton.setEnabled(true);
            }
            _rescanButton.setVisibility(View.VISIBLE);
        }

        private static final String TAG = "NetworkDiscovery";


        @Override
        protected void onProgressUpdate(Object... values) {
//            super.onProgressUpdate(values);
            if (values[0] != null) {
                clientData.add((ClientListData) values[0]);
                ((ClientListAdapter) (listView.getAdapter())).notifyDataSetChanged();
            } else {
                int percent = (int) values[1];
                pb.setProgress(percent);
                textView.setText("Scanning : " + percent + "%");
            }
        }

        private String localIp;
        ListView listView;

        NetworkDiscovery(String localIp, ListView list) {
            this.localIp = localIp;
            this.listView = list;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                String prefix = localIp.substring(0, localIp.lastIndexOf(".") + 1);
                Log.d(TAG, "prefix: " + prefix);

                for (int i = 2; i < 255; i++) {
                    String testIp = prefix + i;
                    InetAddress address = InetAddress.getByName(testIp);
                    boolean reachable = address.isReachable(256);
                    String hostName = address.getCanonicalHostName();
                    if (testIp.equalsIgnoreCase(localIp)) continue;
                    publishProgress(null, (int) (i * 100 / 255));
                    if (reachable) {
                        ClientListData clientListData = new ClientListData("Mobile 1", "20%", testIp);
                        Log.i(TAG, "Host: " + hostName + "(" + testIp + ") is reachable!");
                        publishProgress(clientListData, (i * 100 / 255));
                    }
                }
            } catch (Throwable t) {
                Log.e(TAG, "Well that's not good.", t);
            }

            return null;
        }
    }
}