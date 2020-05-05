package com.mobilespark.master.Pojos;

public class ClientStatData {

    public String clientName;
    public float powerConsumed;
    public long timeTaken;

    public ClientStatData(String clientName, float powerConsumed, long timeTaken) {
        this.clientName = clientName;
        this.powerConsumed = powerConsumed;
        this.timeTaken = timeTaken;
    }
}
