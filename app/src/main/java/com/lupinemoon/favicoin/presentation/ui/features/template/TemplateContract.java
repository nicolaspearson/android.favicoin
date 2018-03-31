package com.lupinemoon.favicoin.presentation.ui.features.template;

import com.lupinemoon.favicoin.databinding.ActivityTemplateBinding;
import com.lupinemoon.favicoin.presentation.ui.base.IBasePresenter;
import com.lupinemoon.favicoin.presentation.ui.base.IBaseView;
import com.lupinemoon.favicoin.presentation.ui.base.IBaseViewModel;

class TemplateContract {

    interface View extends IBaseView {
        Presenter getPresenter();
        ViewModel getViewModel();
        ActivityTemplateBinding getBinding();
    }

    interface Presenter extends IBasePresenter {
        void performAction(String actionText);
    }

    interface ViewModel extends IBaseViewModel {

    }

}
