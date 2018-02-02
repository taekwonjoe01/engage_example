package com.example.joseph.myapplication;

import java.util.ArrayList;
import java.util.List;

/**
 * POJO object caching the Stock data relevant to the application.
 *
 * TODO:
 * -Store this on device? Perhaps a Room database?
 */
public class StockData {

    public String mSymbol;

    // TODO perhaps turn this into a unix timestamp?
    public String mLastRefreshed;

    public List<StockDate> mStockDates = new ArrayList<>();
}
