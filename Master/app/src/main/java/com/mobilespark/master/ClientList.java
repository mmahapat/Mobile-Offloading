package com.mobilespark.master;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.mobilespark.master.Pojos.ClientListData;
import com.mobilespark.master.WebUtils.VolleyController;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;

public class ClientList extends AppCompatActivity {
    private static final String TAG = "ClientList";
    private String localIp = "";
    private ListView list;
    public static ArrayList<ClientListData> clientData = new ArrayList<>();
    private Button _startTaskButton;
    private ImageButton _rescanButton;
    private ImageButton _stopServer;
    private Spinner _clientNumbers;
    private TextView _clientNumberText;
    ProgressBar pb;
    TextView scanStatus;
    AsyncTask<Void, Object, Void> execute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_list);
        list = findViewById(R.id.nodelist);
        _startTaskButton = findViewById(R.id.startTask);
        _rescanButton = findViewById(R.id.rescan);
        pb = findViewById(R.id.progress_horizontal);
        scanStatus = findViewById(R.id.percent);
        _stopServer = findViewById(R.id.stopServer);
        _clientNumbers = findViewById(R.id.clientNumber);
        _clientNumberText = findViewById(R.id.clientNumberText);
        ClientListAdapter adapter = new ClientListAdapter(this, clientData);
        list.setAdapter(adapter);
        WifiManager wm = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        localIp = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());

        execute = new NetworkDiscovery(localIp, list).execute();

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
                scanStatus.setVisibility(View.VISIBLE);
                scanStatus.setText("Scanning : 0%");
                execute = new NetworkDiscovery(localIp, list).execute();
            }
        });
        _stopServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (execute.getStatus() == AsyncTask.Status.RUNNING) {
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(ClientList.this);
                    builder1.setMessage("Scanning is on going, still shutdown the server?");
                    builder1.setCancelable(true);

                    builder1.setPositiveButton(
                            "Yes",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    execute.cancel(true);
                                    if (MainActivity.server != null)
                                        MainActivity.server.stop();
                                    dialog.cancel();
                                    MainActivity.serverRunning = false;
                                    ClientList.super.onBackPressed();
                                }
                            });

                    builder1.setNegativeButton(
                            "No",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });

                    AlertDialog alert11 = builder1.create();
                    alert11.show();
                } else {
                    if (MainActivity.server != null)
                        MainActivity.server.stop();
                    MainActivity.serverRunning = false;
                    ClientList.super.onBackPressed();
                }
            }
        });

        _startTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent taskMonitorScreen = new Intent(ClientList.this, TaskMonitor.class);
                String slaveNumber = _clientNumbers.getSelectedItem().toString();
                taskMonitorScreen.putExtra("Slaves", Integer.parseInt(slaveNumber));
                //taskMonitorScreen.putExtra("ClientList",clientData);
                startActivity(taskMonitorScreen);
            }
        });
    }

    @Override
    public void onBackPressed() {
        Toast.makeText(this, "Cannot go back when the server is running, Please stop the server",
                Toast.LENGTH_LONG).show();
        Log.e(TAG, "onBackPressed: " + "Back button pressed when server is running");

    }


    class NetworkDiscovery extends AsyncTask<Void, Object, Void> implements ClientResponse {
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Log.d(TAG, "onPostExecute: Finished calls");
            scanStatus.setVisibility(View.GONE);
            pb.setVisibility(View.GONE);
            Toast.makeText(ClientList.this, "Scan Complete",
                    Toast.LENGTH_LONG).show();
            if (clientData.size() < 1) {
                Toast.makeText(ClientList.this, "No Clients found, Please Rescan",
                        Toast.LENGTH_LONG).show();
                _clientNumbers.setVisibility(View.GONE);
                _clientNumberText.setVisibility(View.GONE);
            } else {
                _clientNumbers.setVisibility(View.VISIBLE);
                _clientNumberText.setVisibility(View.VISIBLE);
                _startTaskButton.setEnabled(true);
                List<Integer> list = new ArrayList<>();
                for (int i = 1; i <= clientData.size(); i++) {
                    list.add(i);
                }
                ArrayAdapter<Integer> dataAdapter = new ArrayAdapter<Integer>(ClientList.this, android.R.layout.simple_spinner_item, list);
                dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                _clientNumbers.setAdapter(dataAdapter);
            }
            _rescanButton.setVisibility(View.VISIBLE);
        }

        private static final String TAG = "NetworkDiscovery";


        @Override
        protected void onProgressUpdate(Object... values) {
//            super.onProgressUpdate(values);
            if (values[0] != null) {
                clientData.add((ClientListData) values[0]);
                //Dummy data
//                clientData.add((ClientListData) values[0]);
//                clientData.add((ClientListData) values[0]);
//                clientData.add((ClientListData) values[0]);
//                clientData.add((ClientListData) values[0]);
//                clientData.add((ClientListData) values[0]);
                ((ClientListAdapter) (listView.getAdapter())).notifyDataSetChanged();
            } else {
                int percent = (int) values[1];
                pb.setProgress(percent);
                scanStatus.setText("Scanning : " + percent + "%");
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
                String prefix = "192.168.0.";
                Log.d(TAG, "prefix: " + prefix);

                for (int i = 2; i < 255; i++) {
                    if (isCancelled()) break;
                    String testIp = prefix + i;
                    if (testIp.equals(localIp)) continue;
                    String url = "http://" + testIp + ":8080/status";
                    VolleyController volleyController = new VolleyController(getApplicationContext());
                    JSONObject body = new JSONObject();
                    body.put("ip", localIp);
                    publishProgress(null, (i * 100) / 255);
                    volleyController.makeRequest(url, body, NetworkDiscovery.this);
                }
            } catch (Throwable t) {
                Log.e(TAG, "Well that's not good.", t);
            }

            return null;
        }

        @Override
        public void onSuccess(JSONObject jsonObject) {
            String clientIp = "Empty";
            String battery = "Empty";
            String deviceName = "Empty";
            try {
                clientIp = (String) jsonObject.get("ip");
                battery = (String) jsonObject.get("battery");
                deviceName = (String) jsonObject.get("deviceName");
            } catch (JSONException e) {
                Log.e(TAG, "onSuccess: " + "Could not pass JSON");
            }
            ClientListData clientListData = new ClientListData(deviceName, battery, clientIp);
            Log.i(TAG, jsonObject.toString());
            publishProgress(clientListData, 30);
        }

        @Override
        public void onFailure(VolleyError error) {
            Log.e(TAG, "onFailure: " + "Client not online");
        }
    }
}