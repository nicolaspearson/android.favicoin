package com.lupinemoon.favicoin.presentation.ui.features.favourites;

import android.graphics.Bitmap;
import android.support.v4.app.Fragment;

import com.lupinemoon.favicoin.data.models.CoinItem;
import com.lupinemoon.favicoin.databinding.FragmentFavouritesBinding;
import com.lupinemoon.favicoin.presentation.ui.base.IBasePresenter;
import com.lupinemoon.favicoin.presentation.ui.base.IBaseView;
import com.lupinemoon.favicoin.presentation.ui.base.IBaseViewModel;
import com.lupinemoon.favicoin.presentation.widgets.interfaces.GenericCallback;

import java.util.List;

public class FavouritesContract {

    public interface View extends IBaseView {
        Fragment getFragment();

        Presenter getPresenter();

        ViewModel getViewModel();

        FragmentFavouritesBinding getBinding();

        List<CoinItem> getCoinItems();

        void setCoinItems(List<CoinItem> coinItems, boolean refresh);

        void addCoinItems(List<CoinItem> coinItems);

        void showNetworkErrorLayout(final GenericCallback genericCallback);
    }

    public interface Presenter extends IBasePresenter {
        void removeCoinItem(CoinItem coinItem);

        void fetchCoinItems(boolean refresh, long delay);

        void performEmptyButtonAction();

        void showCoinDetailView(
                CoinItem coinItem,
                android.view.View transitionView,
                Bitmap imageBitmap);
    }

    interface ViewModel extends IBaseViewModel {

    }

}
