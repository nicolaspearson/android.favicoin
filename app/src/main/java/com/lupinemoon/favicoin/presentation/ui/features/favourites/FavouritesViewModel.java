package com.lupinemoon.favicoin.presentation.ui.features.favourites;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.lupinemoon.favicoin.presentation.ui.base.BaseViewModel;
import com.lupinemoon.favicoin.presentation.utils.ActivityUtils;

public class FavouritesViewModel extends BaseViewModel implements FavouritesContract.ViewModel {

    private FavouritesContract.View favouritesView;

    FavouritesViewModel(@NonNull FavouritesContract.View view, @Nullable State savedInstanceState) {
        // Set the view locally
        favouritesView = ActivityUtils.checkNotNull(view, "view cannot be null!");
    }

    @Override
    public State getInstanceState() {
        // Not required
        return null;
    }

}
