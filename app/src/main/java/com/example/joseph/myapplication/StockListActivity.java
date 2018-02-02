package com.example.joseph.myapplication;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * An activity representing a list of Stocks. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link StockDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class StockListActivity extends AppCompatActivity {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;

    private SimpleItemRecyclerViewAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StockListViewModel model = ViewModelProviders.of(StockListActivity.this).get(StockListViewModel.class);
                model.refreshUsers();
            }
        });

        if (findViewById(R.id.item_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }

        View recyclerView = findViewById(R.id.item_list);
        assert recyclerView != null;
        setupRecyclerView((RecyclerView) recyclerView);

        // Create a ViewModel the first time the system calls an activity's onCreate() method.
        // Re-created activities receive the same MyViewModel instance created by the first activity.
        StockListViewModel model = ViewModelProviders.of(this).get(StockListViewModel.class);
        model.getStockData().observe(this, new Observer<List<LiveData<StockData>>>() {
            @Override
            public void onChanged(@Nullable List<LiveData<StockData>> liveData) {
                mAdapter.setItems(liveData);

                // Right now, this LIST does not ever update, so this next logic is safe, however
                // if we change this behavior (defined in the ListViewModel), we should unregister and
                // reregister Observers.
                int index = 0;
                for (LiveData<StockData> stockData : liveData) {
                    final int ldIndex = index;
                    stockData.observe(StockListActivity.this, new Observer<StockData>() {
                        @Override
                        public void onChanged(@Nullable StockData stockData) {
                            mAdapter.notifyItemChanged(ldIndex);
                        }
                    });
                    index++;
                }
            }
        });
        model.refreshUsers();
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        mAdapter = new SimpleItemRecyclerViewAdapter(this, mTwoPane);
        recyclerView.setAdapter(mAdapter);
    }

    public static class SimpleItemRecyclerViewAdapter
            extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

        private final StockListActivity mParentActivity;
        private List<LiveData<StockData>> mValues;
        private final boolean mTwoPane;
        private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LiveData<StockData> liveDataItem = (LiveData<StockData>) view.getTag();

                StockData item = liveDataItem.getValue();
                if (item != null) {
                    if (mTwoPane) {
                        Bundle arguments = new Bundle();
                        arguments.putString(StockDetailFragment.ARG_ITEM_ID, item.mSymbol);
                        StockDetailFragment fragment = new StockDetailFragment();
                        fragment.setArguments(arguments);
                        mParentActivity.getSupportFragmentManager().beginTransaction()
                                .replace(R.id.item_detail_container, fragment)
                                .commit();
                    } else {
                        Context context = view.getContext();
                        Intent intent = new Intent(context, StockDetailActivity.class);
                        intent.putExtra(StockDetailFragment.ARG_ITEM_ID, item.mSymbol);

                        context.startActivity(intent);
                    }
                }
            }
        };

        SimpleItemRecyclerViewAdapter(StockListActivity parent,
                                      boolean twoPane) {
            mValues = new ArrayList<>();
            mParentActivity = parent;
            mTwoPane = twoPane;
        }

        public void setItems(List<LiveData<StockData>> items) {
            mValues = items;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_list_content, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            LiveData<StockData> liveDataItem = mValues.get(position);

            StockData item = liveDataItem.getValue();

            if (item != null) {
                holder.mIdView.setText(item.mSymbol);
                StockDate latestDate = item.mStockDates.get(0);
                if (latestDate != null) {
                    holder.mContentView.setText(Double.toString(latestDate.mClose));
                } else {
                    // TODO make a string asset.
                    holder.mContentView.setText("Something went wrong!");
                }
            } else {
                // TODO make a string asset.
                holder.mIdView.setText("Loading");
                holder.mContentView.setText("Loading");
            }

            holder.itemView.setTag(mValues.get(position));
            holder.itemView.setOnClickListener(mOnClickListener);
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            final TextView mIdView;
            final TextView mContentView;

            ViewHolder(View view) {
                super(view);
                mIdView = (TextView) view.findViewById(R.id.id_text);
                mContentView = (TextView) view.findViewById(R.id.content);
            }
        }
    }
}
