package com.example.joseph.myapplication;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * ViewModel class for maintaining Stock data as a cache. This allows the data to survive some basic
 * lifecycle events like orientation changes and pausing/stopping.
 */
public class StockListViewModel extends ViewModel {
    private MutableLiveData<List<LiveData<StockData>>> mStockList;

    /**
     * @return A LiveData reference for where the StockData will be when it's loaded.
     */
    public LiveData<List<LiveData<StockData>>> getStockData() {
        if (mStockList == null) {
            loadStocks();
        }
        return mStockList;
    }

    public void refreshUsers() {
        StockRepository.getInstance().refreshStockData();
    }

    private void loadStocks() {
        mStockList = new MutableLiveData<>();

        StockRepository repository = StockRepository.getInstance();
        List<String> stockSymbols = repository.getStockSymbols();

        List<LiveData<StockData>> stockData = new ArrayList<>();
        for (String symbol : stockSymbols) {
            stockData.add(repository.getCachedStockDataForSymbol(symbol));
        }

        mStockList.setValue(stockData);
    }

}
