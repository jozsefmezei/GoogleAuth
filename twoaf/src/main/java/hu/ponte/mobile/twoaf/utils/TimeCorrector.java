package hu.ponte.mobile.twoaf.utils;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class TimeCorrector {

    private OkHttpClient client = new OkHttpClient();
    private String url = "http://www.google.com";

    public void getNetworkTime() {

    }

    private void getTimeFromResponse(){
        try {
            Request request = new Request.Builder()
                    .url(url)
                    .build();

            Response response = client.newCall(request).execute();
            String header = response.header("Date");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
