package com.example.joseph.myapplication;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Iterator;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Helper class used by the StockRepository that abstracts the details of retrieving the StockData
 * from the cloud. Ideal implementations of this object would likely include some abstraction to
 * increase modularity for differnt types of StockProviders (Local database, different cloud sources,
 * custom stock libraries, etc.)
 */

public class CloudStockProvider {
    private static final String TAG = CloudStockProvider.class.getSimpleName();

    private static final String PROVIDER_URL = "https://www.alphavantage.co/";
    private static final String PROVIDER_QUERY_ENDPOINT = "query";

    private static final String QUERY_PARAM_FUNCTION = "function";
    private static final String FUNCTION_TYPE_DAILY = "TIME_SERIES_DAILY";
    private static final String QUERY_PARAM_SYMBOL = "symbol";
    private static final String QUERY_PARAM_DATATYPE = "datatype";
    private static final String DATA_TYPE_JSON = "json";
    private static final String QUERY_PARAM_APIKEY = "apikey";
    private static final String API_KEY = "GJLF3CX2MZ6ATP09";

    private static final String PARSER_STOCK_DATE = "Time Series (Daily)";
    private static final String PARSER_META_DATA = "Meta Data";

    private static final String JSON_PARAM_LAST_REFRESHED = "3. Last Refreshed";
    private static final String JSON_PARAM_SYMBOL = "2. Symbol";
    private static final String JSON_PARAM_OPEN = "1. open";
    private static final String JSON_PARAM_HIGH = "2. high";
    private static final String JSON_PARAM_LOW = "3. low";
    private static final String JSON_PARAM_CLOSE = "4. close";
    private static final String JSON_PARAM_VOLUME = "5. volume";

    private final OkHttpClient mClient;

    public CloudStockProvider() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.hostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        });
        mClient = builder.build();
    }

    public LiveData<StockData> getStockForSymbol(String symbol, final MutableLiveData<StockData> stockData) {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(PROVIDER_URL + PROVIDER_QUERY_ENDPOINT).newBuilder();
        urlBuilder.addQueryParameter(QUERY_PARAM_FUNCTION, FUNCTION_TYPE_DAILY);
        urlBuilder.addQueryParameter(QUERY_PARAM_SYMBOL, symbol);
        urlBuilder.addQueryParameter(QUERY_PARAM_DATATYPE, DATA_TYPE_JSON);
        urlBuilder.addQueryParameter(QUERY_PARAM_APIKEY, API_KEY);
        String url = urlBuilder.build().toString();
        Log.d(TAG, "url: " + url);

        Request request = new Request.Builder()
                .url(url)
                .build();

        // This call is asynchronous
        mClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                if (!response.isSuccessful()) {
                    Log.e(TAG, "Query failed!");
                    // TODO real error handling
                } else {
                    String jsonData = response.body().string();
                    StockData data = getStockDataForResponse(jsonData);

                    // Queues up the livedata object to be updated.
                    stockData.postValue(data);
                }
            }
        });

        return stockData;
    }

    private StockData getStockDataForResponse(String jsonBody) {
        StockData result = null;
        try {
            JSONObject stockDataObject = new JSONObject(jsonBody);

            result = new StockData();
            JSONObject metadataObject = stockDataObject.getJSONObject(PARSER_META_DATA);
            Log.e("Joey", metadataObject.toString());
            result.mLastRefreshed = metadataObject.getString(JSON_PARAM_LAST_REFRESHED);
            result.mSymbol = metadataObject.getString(JSON_PARAM_SYMBOL);

            JSONObject stockDateObjects = stockDataObject.getJSONObject(PARSER_STOCK_DATE);
            Iterator<String> iter = stockDateObjects.keys();
            while (iter.hasNext()) {
                String dateString = iter.next();

                StockDate date = new StockDate();
                date.mDate = dateString;
                try {
                    JSONObject stockDateObject = stockDateObjects.getJSONObject(dateString);

                    date.mOpen = Double.parseDouble(stockDateObject.getString(JSON_PARAM_OPEN));
                    date.mHigh = Double.parseDouble(stockDataObject.getString(JSON_PARAM_HIGH));
                    date.mLow = Double.parseDouble(stockDataObject.getString(JSON_PARAM_LOW));
                    date.mClose = Double.parseDouble(stockDataObject.getString(JSON_PARAM_CLOSE));
                    date.mVolume = Integer.parseInt(stockDataObject.getString(JSON_PARAM_VOLUME));
                } catch (JSONException e) {
                    // Something went wrong! TODO
                }

                result.mStockDates.add(date);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return result;
    }
}
