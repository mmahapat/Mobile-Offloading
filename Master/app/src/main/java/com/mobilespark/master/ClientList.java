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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.appcompat.app.AppCompatActivity;

public class ClientList extends AppCompatActivity {
    private static final String TAG = "ClientList";
    private String localIp = "";
    private ListView clientList;
    private ListView _clientsWithConsentList;
    public static ArrayList<ClientListData> clientData = new ArrayList<>();
    public static ArrayList<ClientListData> clientWithConsentData = new ArrayList<>();
    public static Map<String, String> clientMap = new HashMap<>();
    private Button _startTaskButton;
    private Button _getConsentButton;
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
        clientList = findViewById(R.id.nodelist);
        _clientsWithConsentList = findViewById(R.id.clientsWithConsent);

        _startTaskButton = findViewById(R.id.startTask);
        _rescanButton = findViewById(R.id.rescan);
        pb = findViewById(R.id.progress_horizontal);
        scanStatus = findViewById(R.id.percent);
        _stopServer = findViewById(R.id.stopServer);
        _clientNumbers = findViewById(R.id.clientNumber);
        _clientNumberText = findViewById(R.id.clientNumberText);
        _getConsentButton = findViewById(R.id.getConsent);

        ClientListAdapter adapter = new ClientListAdapter(this, clientData);
        clientList.setAdapter(adapter);

        ClientListAdapter consent = new ClientListAdapter(this, clientWithConsentData);
        _clientsWithConsentList.setAdapter(consent);

        WifiManager wm = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        localIp = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());

        _rescanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _rescanButton.setVisibility(View.GONE);
                _getConsentButton.setVisibility(View.GONE);

                clientData.clear();
                ((ClientListAdapter) (clientList.getAdapter())).notifyDataSetChanged();

                clientWithConsentData.clear();
                ((ClientListAdapter) (_clientsWithConsentList.getAdapter())).notifyDataSetChanged();

                _clientNumbers.setVisibility(View.GONE);
                _clientNumberText.setVisibility(View.GONE);
                _startTaskButton.setVisibility(View.GONE);
                _getConsentButton.setVisibility(View.GONE);
                pb.setProgress(0);
                pb.setVisibility(View.VISIBLE);
                scanStatus.setVisibility(View.VISIBLE);
                scanStatus.setText("Scanning : 0%");
                execute = new NetworkDiscovery(localIp, clientList).execute();
            }
        });
//        _rescanButton.performClick();
        _getConsentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clientWithConsentData.clear();
                clientMap.clear();
                ((ClientListAdapter) (_clientsWithConsentList.getAdapter())).notifyDataSetChanged();

                _clientNumbers.setVisibility(View.GONE);
                _clientNumberText.setVisibility(View.GONE);
                _startTaskButton.setVisibility(View.GONE);


                new GetConsentNetworkCall().execute();
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
                                    closeServerAndDeregister();
                                    dialog.cancel();
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
                    closeServerAndDeregister();
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

    private void closeServerAndDeregister() {
        if (MainActivity.server != null)
            MainActivity.server.stop();
        new DeregisterClients().execute();
        MainActivity.serverRunning = false;
        ClientList.super.onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();
        _rescanButton.performClick();
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
                _startTaskButton.setVisibility(View.GONE);
                _getConsentButton.setVisibility(View.GONE);
            } else {
                _getConsentButton.setVisibility(View.VISIBLE);
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
                ((ClientListAdapter) (listView.getAdapter())).notifyDataSetChanged();
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
                    volleyController.makeRequest(url, body, NetworkDiscovery.this, null);
                }
            } catch (Throwable t) {
                Log.e(TAG, "Well that's not good.", t);
            }

            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            clearClientData();
        }

        @Override
        public void onSuccess(JSONObject jsonObject, String identifier) {
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
            clientMap.put(clientIp, deviceName);
            Log.i(TAG, jsonObject.toString());
            publishProgress(clientListData, 30);
        }

        @Override
        public void onFailure(VolleyError error, String identifier) {
            Log.e(TAG, "onFailure: " + "Client not online");
        }
    }

    private void clearClientData() {
        clientMap.clear();
        clientData.clear();
        clientWithConsentData.clear();
        ((ClientListAdapter) (_clientsWithConsentList.getAdapter())).notifyDataSetChanged();
        ((ClientListAdapter) (clientList.getAdapter())).notifyDataSetChanged();
    }


    class GetConsentNetworkCall extends AsyncTask<Void, Object, Void> implements ClientResponse {
        private static final String TAG = "GetConsentNetworkCall";

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                Log.e(TAG, "doInBackground: " + clientData.size());
                for (ClientListData client : clientData) {
                    if (isCancelled()) break;
                    String clientIp = client.clientIp;
                    String url = "http://" + clientIp + ":8080/register";
                    Log.e(TAG, "doInBackground: " + url);
                    VolleyController volleyController = new VolleyController(getApplicationContext());
                    JSONObject body = new JSONObject();
                    body.put("ip", localIp);
                    volleyController.makeRequest(url, body, GetConsentNetworkCall.this, clientIp);
                }
                Thread.sleep(7000);
            } catch (Throwable t) {
                Log.e(TAG, "Well that's not good.", t);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Log.e(TAG, "onPostExecute: " + clientWithConsentData.size());
            if (clientWithConsentData.size() < 1) {
                Toast.makeText(ClientList.this, "No Clients Gave Consent. please Refresh",
                        Toast.LENGTH_LONG).show();
                _clientNumbers.setVisibility(View.GONE);
                _clientNumberText.setVisibility(View.GONE);
                _startTaskButton.setVisibility(View.GONE);

            } else {
                _clientNumbers.setVisibility(View.VISIBLE);
                _clientNumberText.setVisibility(View.VISIBLE);
                _startTaskButton.setVisibility(View.VISIBLE);
                List<Integer> list = new ArrayList<>();
                for (int i = 1; i <= clientWithConsentData.size(); i++) {
                    list.add(i);
                }
                ArrayAdapter<Integer> dataAdapter = new ArrayAdapter<Integer>(ClientList.this, android.R.layout.simple_spinner_item, list);
                dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                _clientNumbers.setAdapter(dataAdapter);
            }
        }

        @Override
        protected void onProgressUpdate(Object... values) {
            super.onProgressUpdate(values);
            if (values[0] != null) {
                clientWithConsentData.add((ClientListData) values[0]);
                //Dummy data
                ((ClientListAdapter) (_clientsWithConsentList.getAdapter())).notifyDataSetChanged();
            }
        }

        @Override
        public void onSuccess(JSONObject jsonObject, String identifier) {
            String gaveConsent = "";
            try {
                gaveConsent = (String) jsonObject.get("consent");
            } catch (JSONException e) {
                Log.e(TAG, "onSuccess: " + "Could not pass JSON");
            }
            if (gaveConsent.equalsIgnoreCase("YES")) {
                for (ClientListData clientListData : clientData) {
                    if (clientListData.clientIp == identifier) {
                        publishProgress(clientListData, 30);
                        clientMap.put(identifier, clientListData.clientName);
                    }
                }
                Log.i(TAG, jsonObject.toString());
            } else {
                Log.i(TAG, "Client rejected the consent");
            }
        }

        @Override
        public void onFailure(VolleyError error, String identifier) {
            Log.e(TAG, "onFailure: " + "Client not online");
        }
    }


    class DeregisterClients extends AsyncTask<Void, Object, Void> implements ClientResponse {

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                Log.e(TAG, "doInBackground: " + clientWithConsentData.size());
                for (ClientListData client : clientWithConsentData) {
                    if (isCancelled()) break;
                    String clientIp = client.clientIp;
                    String url = "http://" + clientIp + ":8080/unregister";
                    Log.e(TAG, "doInBackground: " + url);
                    VolleyController volleyController = new VolleyController(getApplicationContext());
                    JSONObject body = new JSONObject();
                    body.put("ip", localIp);
                    volleyController.makeRequest(url, body, DeregisterClients.this, clientIp);
                }
            } catch (Throwable t) {
                Log.e(TAG, "Well that's not good.", t);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            clearClientData();
        }

        @Override
        public void onSuccess(JSONObject jsonObject, String identifier) {
            Log.d(TAG, "onSuccess: Unregister : " + identifier);
        }

        @Override
        public void onFailure(VolleyError error, String identifier) {

        }
    }
}