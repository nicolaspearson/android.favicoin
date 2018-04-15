package com.lupinemoon.favicoin.presentation.ui.features.dev;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;

import com.lupinemoon.favicoin.R;
import com.lupinemoon.favicoin.data.storage.AppRepository;
import com.lupinemoon.favicoin.presentation.ui.base.BasePresenter;
import com.lupinemoon.favicoin.presentation.utils.ActivityUtils;

import java.io.File;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

class DevPresenter extends BasePresenter implements DevContract.Presenter {

    private DevContract.View devView;

    private AppRepository appRepository = AppRepository.getInstance();

    DevPresenter(@NonNull DevContract.View view, @NonNull Bundle args) {
        // Set the view locally
        devView = ActivityUtils.checkNotNull(view, "view cannot be null!");
    }

    @Override
    public void performExportDatabase() {
        devView.showSnackbarMsg(
                devView.getActivity().getString(R.string.export_database),
                Snackbar.LENGTH_SHORT);

        nonViewDisposables.add(
                appRepository.getRealmDatabaseFile(
                        devView.getActivity().getExternalCacheDir())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(file -> {
                            if (devView.isAttached()) {
                                devView.emailFile(file);
                            }
                        }, throwable -> {
                            Timber.w(throwable);
                            if (devView.isAttached()) {
                                devView.showSnackbarMsg(
                                        devView.getActivity().getString(R.string.export_failed),
                                        Snackbar.LENGTH_SHORT);
                            }
                        }));
    }

    @Override
    public void clearRealmDatabase() {
        appRepository.clearRealmDatabase();
    }
}
