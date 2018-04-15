package com.lupinemoon.favicoin.presentation.ui.features.landing;

import com.lupinemoon.favicoin.presentation.ui.base.IBasePresenter;
import com.lupinemoon.favicoin.presentation.ui.base.IBaseView;

interface LandingContract {

    interface View extends IBaseView {
        Presenter getPresenter();
    }

    interface Presenter extends IBasePresenter {
        void retryQueuedRequests();

        void onLogout();
    }

}
