package com.example.joseph.myapplication;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

/**
 * ViewModel class for maintaining Stock data as a cache. This allows the data to survive some basic
 * lifecycle events like orientation changes and pausing/stopping.
 */
public class StockDetailViewModel extends ViewModel {
    private LiveData<StockData> mStock;

    /**
     * @return A LiveData reference for where the StockData will be when it's loaded.
     */
    public LiveData<StockData> getStockData(String symbol) {
        // This could be nicer (what is symbol changes?)
        if (mStock == null) {
            loadStock(symbol);
        }
        return mStock;
    }

    private void loadStock(String symbol) {
        StockRepository repository = StockRepository.getInstance();
        mStock = repository.getCachedStockDataForSymbol(symbol);
    }
}
