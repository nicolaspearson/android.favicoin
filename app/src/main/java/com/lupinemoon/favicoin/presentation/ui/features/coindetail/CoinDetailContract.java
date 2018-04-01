package com.lupinemoon.favicoin.presentation.ui.features.coindetail;

import com.lupinemoon.favicoin.databinding.ActivityCoinDetailBinding;
import com.lupinemoon.favicoin.presentation.ui.base.IBasePresenter;
import com.lupinemoon.favicoin.presentation.ui.base.IBaseView;
import com.lupinemoon.favicoin.presentation.ui.base.IBaseViewModel;

class CoinDetailContract {

    interface View extends IBaseView {
        Presenter getPresenter();
        ViewModel getViewModel();
        ActivityCoinDetailBinding getBinding();
    }

    interface Presenter extends IBasePresenter {
        void performAction(String actionText);
    }

    interface ViewModel extends IBaseViewModel {

    }

}
