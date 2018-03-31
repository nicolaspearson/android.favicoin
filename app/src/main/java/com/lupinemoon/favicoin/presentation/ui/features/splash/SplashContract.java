package com.lupinemoon.favicoin.presentation.ui.features.splash;

import com.lupinemoon.favicoin.presentation.ui.base.BaseContract;
import com.lupinemoon.favicoin.presentation.ui.base.IBasePresenter;
import com.lupinemoon.favicoin.presentation.ui.base.IBaseView;

class SplashContract extends BaseContract {

    interface View extends IBaseView {
        Presenter getPresenter();
        void setPresenter(IBasePresenter presenter);
        void startLandingActivity();
        void startLoginActivity();
    }

    interface Presenter extends IBasePresenter {
        void performAutoLogin();
    }

}
