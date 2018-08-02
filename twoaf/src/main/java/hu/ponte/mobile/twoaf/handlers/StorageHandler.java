package hu.ponte.mobile.twoaf.handlers;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class StorageHandler {
    private static String TIME_KEY = "timeOffset";
    private final Object mLock = new Object();
    private long mCachedCorrectionMillis = -1;

    public void storeCorrectionTime(Context context, long offset) {
        synchronized (mLock) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putLong(TIME_KEY, offset);
            editor.apply();
            mCachedCorrectionMillis = offset;
        }
    }

    public long getCorrectionTime(Context context) {
        synchronized (mLock) {
            if (mCachedCorrectionMillis == -1) {
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
                mCachedCorrectionMillis = preferences.getLong(TIME_KEY, -1);
            }
        }
        return mCachedCorrectionMillis;
    }
}