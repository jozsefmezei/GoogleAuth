package hu.ponte.mobile.twoaf.handlers;

import android.content.Context;

import hu.ponte.mobile.twoaf.utils.Connection;
import hu.ponte.mobile.twoaf.utils.DateFormatter;

public class TimeCorrectionHandler {

    private Connection connection = new Connection();
    private StorageHandler storageHandler = new StorageHandler();

    public void syncTime(Context context) {
        connection.getNetworkTime(dateTime -> {
            long time = DateFormatter.getDateInMillis(dateTime);
            syncTime(context, time);
        });
    }

    public void syncTime(Context context, long trustedDateInMillis) {
        long systemTime = System.currentTimeMillis();
        storageHandler.storeCorrectionTime(context, trustedDateInMillis - systemTime);
        System.out.println();
    }
}
