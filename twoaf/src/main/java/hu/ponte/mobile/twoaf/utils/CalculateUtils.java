package hu.ponte.mobile.twoaf.utils;

import java.util.Date;

public class CalculateUtils {

    public static long getRemainingTime(long time, long timeWindow, long timeStepSize){
        return timeStepSize - (time - (timeWindow * timeStepSize));
    }

    public static long getCorrectedTime(long offset){
        return new Date().getTime() + offset;
    }

    public static long getTimeWindow(long time, long timeStepSize){
        return time / timeStepSize;
    }
}
