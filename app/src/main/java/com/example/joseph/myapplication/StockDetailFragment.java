package com.example.joseph.myapplication;

import android.app.Activity;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * A fragment representing a single Item detail screen.
 * This fragment is either contained in a {@link StockListActivity}
 * in two-pane mode (on tablets) or a {@link StockDetailActivity}
 * on handsets.
 */
public class StockDetailFragment extends Fragment {
    public static final String ARG_ITEM_ID = "item_id";

    private String mSymbol;

    public StockDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            mSymbol = getArguments().getString(ARG_ITEM_ID);

            Activity activity = this.getActivity();
            CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
            if (appBarLayout != null) {
                appBarLayout.setTitle(mSymbol);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.item_detail, container, false);

        if (mSymbol != null) {
            ((TextView) rootView.findViewById(R.id.item_detail)).setText(mSymbol);

            StockDetailViewModel model = ViewModelProviders.of(getActivity()).get(StockDetailViewModel.class);
            model.getStockData(mSymbol).observe(this, new Observer<StockData>() {
                @Override
                public void onChanged(@Nullable StockData stockData) {
                    if (stockData != null) {
                        StockDate latestDate = stockData.mStockDates.get(0);

                        // TODO handle if the date IS null!?
                        if (latestDate != null) {
                            ((TextView) rootView.findViewById(R.id.stock_open)).setText(Double.toString(latestDate.mOpen));
                            ((TextView) rootView.findViewById(R.id.stock_high)).setText(Double.toString(latestDate.mHigh));
                            ((TextView) rootView.findViewById(R.id.stock_low)).setText(Double.toString(latestDate.mLow));
                            ((TextView) rootView.findViewById(R.id.stock_close)).setText(Double.toString(latestDate.mClose));
                            ((TextView) rootView.findViewById(R.id.stock_volume)).setText(Integer.toString(latestDate.mVolume));
                        }
                    }
                }
            });
        }

        return rootView;
    }
}
