package com.lupinemoon.favicoin.presentation.ui.base;

public interface IBaseViewModel {
    boolean validate(String data);

    String getError(String data, String error);

    BaseViewModel.State getInstanceState();
}
