package hu.ponte.mobile.twoaf.interfaces;

public interface Twoaf {

    void onTokenChangedListener(String token, long remainingTimeInSeconds);
    void onTokenChangedUIListener(String token, long remainingTimeInSeconds);

}
