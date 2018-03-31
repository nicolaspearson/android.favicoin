package com.lupinemoon.favicoin.presentation.ui.features.offline;

import com.lupinemoon.favicoin.databinding.ActivityOfflineBinding;
import com.lupinemoon.favicoin.presentation.ui.base.IBasePresenter;
import com.lupinemoon.favicoin.presentation.ui.base.IBaseView;
import com.lupinemoon.favicoin.presentation.ui.base.IBaseViewModel;

class OfflineContract {

    interface View extends IBaseView {
        Presenter getPresenter();
        ViewModel getViewModel();
        ActivityOfflineBinding getBinding();
        void closeOfflineActivity();
    }

    interface Presenter extends IBasePresenter {

    }

    interface ViewModel extends IBaseViewModel {

    }

}
