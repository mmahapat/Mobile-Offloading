package com.mobilespark.master.Pojos;

import java.util.ArrayList;

public class ClientListData {
    public String clientName;
    public String batteryPercentage;
    public String clientIp;

    public ClientListData(String clientName, String batteryPercentage, String clientIp) {
        this.clientIp = clientIp;
        this.clientName = clientName;
        this.batteryPercentage = batteryPercentage;
    }
}
