package com.lupinemoon.favicoin.presentation.ui.features.dev;

import com.lupinemoon.favicoin.databinding.ActivityDevBinding;
import com.lupinemoon.favicoin.presentation.ui.base.IBasePresenter;
import com.lupinemoon.favicoin.presentation.ui.base.IBaseView;
import com.lupinemoon.favicoin.presentation.ui.base.IBaseViewModel;

import java.io.File;

class DevContract {

    interface View extends IBaseView {
        Presenter getPresenter();
        ViewModel getViewModel();
        ActivityDevBinding getBinding();
        void emailFile(File file);
    }

    interface Presenter extends IBasePresenter {
        void performExportDatabase();
        void clearRealmDatabase();
    }

    interface ViewModel extends IBaseViewModel {

    }

}
