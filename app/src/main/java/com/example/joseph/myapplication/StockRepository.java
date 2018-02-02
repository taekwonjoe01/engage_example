package com.example.joseph.myapplication;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Repository class responsible for caching StockData. This object is a singleton and exists at the
 * Application layer. It will exist as a data cache as long as the application exists.
 */
public class StockRepository {
    private static final List<String> STOCK_SYMBOLS = Collections.unmodifiableList(
            new ArrayList<String>() {{
                add("UNH");
                add("AMZ");
                add("MA");
                add("ADP");
                add("BBVA");
            }});
    private static StockRepository sInstance;

    // TODO - this provider might exist in many different forms, so perhaps create some abstraction?
    private final CloudStockProvider mProvider;

    // simple in memory cache, details omitted for brevity. Maps symbols to LiveData.
    private Map<String, MutableLiveData<StockData>> mStockCache;

    private StockRepository() {
        // Initialize our cache.
        mStockCache = new HashMap<>();
        mProvider = new CloudStockProvider();

        for (String symbol : STOCK_SYMBOLS) {
            mStockCache.put(symbol, new MutableLiveData<StockData>());
        }
    }

    public static StockRepository getInstance() {
        if (sInstance == null) {
            sInstance = new StockRepository();
        }
        return sInstance;
    }

    public List<String> getStockSymbols() {
        return STOCK_SYMBOLS;
    }

    /**
     * @param symbol
     * @return a LiveData pointer to the StockData. If the value is null, the data is loading.
     */
    public LiveData<StockData> getCachedStockDataForSymbol(String symbol) {
        // We cast the MutableLiveData to a LiveData here so that clients cannot change the value
        // inside. Only this repository should hold that permission.
        return mStockCache.get(symbol);
    }

    public void refreshStockData() {
        // We are going to refresh all of our data, therefore post a null value here. Any observers
        // will be updated and notified the old data is invalid.
        for (String symbol : STOCK_SYMBOLS) {
            MutableLiveData<StockData> data = mStockCache.get(symbol);

            // Set to null so observers know it's loading. As pointed out in the getCachedStockData
            // documentation.
            data.postValue(null);

            // This launches an asynchronous operation to grab the updated StockData.
            mProvider.getStockForSymbol(symbol, data);
        }
    }
}
