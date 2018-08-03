package hu.ponte.mobile.twoaf.utils;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Connection {
    private OkHttpClient client = new OkHttpClient();
    private String url = "http://www.google.com";

    public void getNetworkTime(NetworkTimeListener networkTimeListener) {
        Thread t = new Thread(() -> getTimeFromResponse(networkTimeListener));
        t.start();
    }

    private void getTimeFromResponse(NetworkTimeListener networkTimeListener) {
        try {
            Request request = new Request.Builder()
                    .url(url)
                    .build();

            Response response = client.newCall(request).execute();
            networkTimeListener.onDateTimeChanged(response.header("Date"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setUrl(String url) {
        this.url = url;
    }

    // listener ------------------------------------------------------------------------------------
    public interface NetworkTimeListener {
        void onDateTimeChanged(String dateTime);
    }
}
