package com.lupinemoon.favicoin.presentation.ui.features.landing.home;

import android.graphics.Bitmap;

import com.lupinemoon.favicoin.data.models.CoinItem;
import com.lupinemoon.favicoin.data.models.Coins;
import com.lupinemoon.favicoin.databinding.FragmentHomeBinding;
import com.lupinemoon.favicoin.presentation.ui.base.IBasePresenter;
import com.lupinemoon.favicoin.presentation.ui.base.IBaseView;
import com.lupinemoon.favicoin.presentation.ui.base.IBaseViewModel;
import com.lupinemoon.favicoin.presentation.widgets.interfaces.GenericCallback;

import java.util.List;

public class HomeContract {

    public interface View extends IBaseView {
        Presenter getPresenter();

        ViewModel getViewModel();

        FragmentHomeBinding getBinding();

        List<CoinItem> getCoinItems();

        void setCoinItems(List<CoinItem> coinItems, boolean refresh);

        void addCoinItems(List<CoinItem> coinItems);

        void updateCoinItem(CoinItem coinItem);

        void showNetworkErrorLayout(final GenericCallback genericCallback);
    }

    public interface Presenter extends IBasePresenter {
        void fetchCoinItems(boolean refresh, long delay);

        void updateCoinItems(Coins coins);

        void loadMore(int itemCount);

        void showCoinDetailView(CoinItem coinItem, android.view.View transitionView, Bitmap imageBitmap);
    }

    interface ViewModel extends IBaseViewModel {

    }

}
