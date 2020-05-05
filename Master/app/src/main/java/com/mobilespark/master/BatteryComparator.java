package com.mobilespark.master;

import com.mobilespark.master.Pojos.ClientListData;

import java.util.Comparator;

public class BatteryComparator implements Comparator<ClientListData>{

        @Override
        public int compare(ClientListData t1, ClientListData t2) {
            return (int) (Float.parseFloat(t2.batteryPercentage) - Float.parseFloat(t1.batteryPercentage));
        }

}
