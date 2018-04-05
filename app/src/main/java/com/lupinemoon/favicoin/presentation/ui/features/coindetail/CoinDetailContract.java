package com.lupinemoon.favicoin.presentation.ui.features.coindetail;

import com.lupinemoon.favicoin.data.models.CoinItem;
import com.lupinemoon.favicoin.databinding.ActivityCoinDetailBinding;
import com.lupinemoon.favicoin.presentation.ui.base.IBasePresenter;
import com.lupinemoon.favicoin.presentation.ui.base.IBaseView;
import com.lupinemoon.favicoin.presentation.ui.base.IBaseViewModel;

class CoinDetailContract {

    interface View extends IBaseView {
        Presenter getPresenter();

        ViewModel getViewModel();

        ActivityCoinDetailBinding getBinding();

        void showNoCoinItemDialog();

        void favouriteToggleSuccess(CoinItem coinItem);
    }

    interface Presenter extends IBasePresenter {
        void toggleFavourite(CoinItem coinItem);
    }

    interface ViewModel extends IBaseViewModel {

    }

}
