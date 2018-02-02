package com.example.joseph.myapplication;

/**
 * POJO object caching the Stock data relevant to the application per date.
 *
 * TODO
 */
public class StockDate {
    public double mOpen;
    public double mHigh;
    public double mLow;
    public double mClose;

    // TODO perhaps this should be a long instead?
    public int mVolume;

    // TODO perhaps turn this into a unix timestamp?
    public String mDate;
}
