package com.mobilespark.master.Pojos;

public class ClientListData {
    public String clientName;
    public String batteryPercentage;
    public String clientIp;
    public boolean showStatus;
    public String status;

    public ClientListData(String clientName, String batteryPercentage, String clientIp) {
        this.clientIp = clientIp;
        this.clientName = clientName;
        this.batteryPercentage = batteryPercentage;
        this.showStatus = false;
        this.status = "";
    }
}
