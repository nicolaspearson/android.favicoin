package com.lupinemoon.favicoin.presentation.ui.features.home.adapters;

import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.lupinemoon.favicoin.R;
import com.lupinemoon.favicoin.data.models.CoinItem;
import com.lupinemoon.favicoin.databinding.ListItemCoinBinding;
import com.lupinemoon.favicoin.presentation.ui.features.home.HomeContract;

import java.util.ArrayList;
import java.util.List;

public class CoinItemAdapter extends RecyclerView.Adapter<CoinItemAdapter.CoinItemViewHolder> {

    private HomeContract.View homeView;

    private List<CoinItem> coinItems;

    private RequestBuilder requestBuilder;

    public CoinItemAdapter(HomeContract.View view, List<CoinItem> newCoinItems) {
        homeView = view;
        coinItems = new ArrayList<>();
        setCoinItems(newCoinItems);

        RequestOptions options = new RequestOptions()
                .placeholder(R.drawable.ic_placeholder)
                .dontAnimate()
                .encodeFormat(Bitmap.CompressFormat.PNG)
                .encodeQuality(100)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .priority(Priority.HIGH);

        requestBuilder = Glide.with(homeView.getActivity().getApplicationContext())
                .applyDefaultRequestOptions(options)
                .asBitmap();
    }

    public List<CoinItem> getCoinItems() {
        return coinItems;
    }

    public void setCoinItems(List<CoinItem> newCoinItems) {
        notifyItemRangeRemoved(0, this.coinItems.size());
        this.coinItems.clear();
        this.coinItems.addAll(newCoinItems);
        notifyItemRangeInserted(0, this.coinItems.size());
    }

    public void addCoinItems(List<CoinItem> newCoinItems) {
        int currentSize = this.coinItems.size();
        this.coinItems.addAll(newCoinItems);
        notifyItemRangeInserted(currentSize, newCoinItems.size());
    }

    public void updateCoinItem(CoinItem coinItem) {
        if (coinItem != null) {
            int position = -1;
            for (int i = 0; i < coinItems.size(); i++) {
                CoinItem item = coinItems.get(i);
                if (coinItem.getId().equals(item.getId())) {
                    position = i;
                    break;
                }
            }
            if (position >= 0) {
                coinItems.remove(position);
                coinItems.add(position, coinItem);
                notifyItemChanged(position);
            }
        }
    }

    @NonNull
    @Override
    public CoinItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ListItemCoinBinding binding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.getContext()),
                R.layout.list_item_coin,
                parent,
                false);
        return new CoinItemViewHolder(binding, homeView);
    }

    @Override
    public void onBindViewHolder(@NonNull CoinItemViewHolder coinItemViewHolder, int position) {
        CoinItem coinItem = getCoinItems().get(position);
        coinItemViewHolder.bindCoinItem(coinItem);
    }

    @Override
    public int getItemCount() {
        return getCoinItems().size();
    }

    @Override
    public long getItemId(int position) {
        return Integer.parseInt(getCoinItems().get(position).getRank());
    }

    @Override
    public void onViewRecycled(@NonNull CoinItemViewHolder coinItemViewHolder) {
        coinItemViewHolder.clean();
        super.onViewRecycled(coinItemViewHolder);
    }

    class CoinItemViewHolder extends RecyclerView.ViewHolder {

        private ListItemCoinBinding binding;
        private CoinItemViewModel coinItemViewModel;

        CoinItemViewHolder(ListItemCoinBinding binding, HomeContract.View view) {
            super(binding.getRoot());
            this.binding = binding;
            coinItemViewModel = new CoinItemViewModel(view, requestBuilder, this.binding);
        }

        void bindCoinItem(CoinItem coinItem) {
            coinItemViewModel.setCoinItem(coinItem);
            this.binding.coinItemImageView.setImageDrawable(null);
            this.binding.setViewModel(coinItemViewModel);
            this.binding.executePendingBindings();
        }

        void clean() {
            Glide.with(homeView.getActivity().getApplicationContext()).clear(binding.coinItemImageView);
        }
    }
}

