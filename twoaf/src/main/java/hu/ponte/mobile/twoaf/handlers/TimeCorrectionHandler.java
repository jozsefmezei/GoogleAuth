package hu.ponte.mobile.twoaf.handlers;

import android.content.Context;

import hu.ponte.mobile.twoaf.interfaces.TimeSync;
import hu.ponte.mobile.twoaf.utils.CalculateUtils;
import hu.ponte.mobile.twoaf.utils.Connection;
import hu.ponte.mobile.twoaf.utils.DateFormatter;

public class TimeCorrectionHandler {

    private Connection connection = new Connection();
    private StorageHandler storageHandler = new StorageHandler();

    public void syncTime(Context context, TimeSync timeSync) {
        connection.getNetworkTime(dateTime -> {
            long time = DateFormatter.getDateInMillis(dateTime);
            syncTime(context, time, timeSync);
        });
    }

    public void syncTime(Context context, long trustedDateInMillis, TimeSync timeSync) {
        long systemTime = System.currentTimeMillis();
        storageHandler.storeCorrectionTime(context, trustedDateInMillis - systemTime);
        timeSync.onTimeSyncronisedListener();
    }

    public long getCorrectedTime(Context context){
        long offset = storageHandler.getCorrectionTime(context);
        return CalculateUtils.getCorrectedTime(offset);
    }

    public void setUrl(String url){
        connection.setUrl(url);
    }
}