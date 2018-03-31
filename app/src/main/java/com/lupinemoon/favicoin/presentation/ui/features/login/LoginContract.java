package com.lupinemoon.favicoin.presentation.ui.features.login;

import com.lupinemoon.favicoin.databinding.ActivityLoginBinding;
import com.lupinemoon.favicoin.presentation.ui.base.IBasePresenter;
import com.lupinemoon.favicoin.presentation.ui.base.IBaseView;
import com.lupinemoon.favicoin.presentation.ui.base.IBaseViewModel;


class LoginContract {

    interface View extends IBaseView {
        Presenter getPresenter();

        ViewModel getViewModel();

        ActivityLoginBinding getBinding();
    }

    interface Presenter extends IBasePresenter {
        void performLogin(String mobileNumber, String password);

        void performSkip();
    }

    interface ViewModel extends IBaseViewModel {

    }

}
