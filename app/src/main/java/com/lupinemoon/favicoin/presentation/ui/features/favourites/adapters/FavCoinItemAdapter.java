package com.lupinemoon.favicoin.presentation.ui.features.favourites.adapters;

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
import com.lupinemoon.favicoin.databinding.ListItemFavCoinBinding;
import com.lupinemoon.favicoin.presentation.ui.features.favourites.FavouritesContract;

import java.util.ArrayList;
import java.util.List;

public class FavCoinItemAdapter extends RecyclerView.Adapter<FavCoinItemAdapter.CoinItemViewHolder> {

    private FavouritesContract.View favouritesView;

    private List<CoinItem> coinItems;

    private RequestBuilder requestBuilder;

    public FavCoinItemAdapter(FavouritesContract.View view, List<CoinItem> newCoinItems) {
        favouritesView = view;
        coinItems = new ArrayList<>();
        setCoinItems(newCoinItems);

        RequestOptions options = new RequestOptions()
                .placeholder(R.drawable.ic_placeholder)
                .dontAnimate()
                .encodeFormat(Bitmap.CompressFormat.PNG)
                .encodeQuality(100)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .priority(Priority.HIGH);

        requestBuilder = Glide.with(favouritesView.getActivity().getApplicationContext())
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

    @NonNull
    @Override
    public CoinItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ListItemFavCoinBinding binding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.getContext()),
                R.layout.list_item_fav_coin,
                parent,
                false);
        return new CoinItemViewHolder(binding, favouritesView);
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

        private ListItemFavCoinBinding binding;
        private FavCoinItemViewModel favCoinItemViewModel;

        CoinItemViewHolder(ListItemFavCoinBinding binding, FavouritesContract.View view) {
            super(binding.getRoot());
            this.binding = binding;
            favCoinItemViewModel = new FavCoinItemViewModel(view, requestBuilder, this.binding);
        }

        void bindCoinItem(CoinItem coinItem) {
            favCoinItemViewModel.setCoinItem(coinItem);
            this.binding.coinItemImageView.setImageDrawable(null);
            this.binding.setViewModel(favCoinItemViewModel);
            this.binding.executePendingBindings();
        }

        void clean() {
            Glide.with(favouritesView.getActivity().getApplicationContext()).clear(binding.coinItemImageView);
        }
    }
}

