package hu.ponte.mobile.twoaf.utils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class Connection {
    private String url = "http://www.google.com";

    public void getNetworkTime(NetworkTimeListener networkTimeListener) {
        Thread t = new Thread(() -> getTimeFromResponse(networkTimeListener));
        t.start();
    }

    private void getTimeFromResponse(NetworkTimeListener networkTimeListener) {
        try {
            HttpURLConnection.setFollowRedirects(false);
            URL host = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) host.openConnection();
            connection.setRequestMethod("HEAD");
            connection.setConnectTimeout(2000);
            connection.setReadTimeout(2000);
            connection.setInstanceFollowRedirects(false);
            connection.connect();

            networkTimeListener.onDateTimeChanged(connection.getHeaderField("Date"));
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
