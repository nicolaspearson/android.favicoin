package com.lupinemoon.favicoin.presentation.ui.features.landing.home;

import android.support.v4.app.Fragment;

import com.lupinemoon.favicoin.databinding.FragmentHomeBinding;
import com.lupinemoon.favicoin.presentation.ui.base.IBasePresenter;
import com.lupinemoon.favicoin.presentation.ui.base.IBaseView;
import com.lupinemoon.favicoin.presentation.ui.base.IBaseViewModel;
import com.lupinemoon.favicoin.presentation.widgets.interfaces.GenericCallback;

public class HomeContract {

    public interface View extends IBaseView {
        Presenter getPresenter();
        ViewModel getViewModel();
        FragmentHomeBinding getBinding();
        Fragment getFragment();
        void showNetworkErrorLayout(final GenericCallback genericCallback);
    }

    public interface Presenter extends IBasePresenter {

    }

    interface ViewModel extends IBaseViewModel {

    }

}
