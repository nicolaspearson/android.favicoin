package com.lupinemoon.favicoin.presentation.ui.features.login;

import android.databinding.Bindable;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import com.lupinemoon.favicoin.BR;
import com.lupinemoon.favicoin.BuildConfig;
import com.lupinemoon.favicoin.R;
import com.lupinemoon.favicoin.presentation.ui.base.BaseViewModel;
import com.lupinemoon.favicoin.presentation.utils.ActivityUtils;
import com.lupinemoon.favicoin.presentation.widgets.Toasty;

import timber.log.Timber;

public class LoginViewModel extends BaseViewModel implements LoginContract.ViewModel {

    private LoginContract.View loginView;

    private String username;
    private String password;

    private boolean loginClicked = false;

    LoginViewModel(@NonNull LoginContract.View view, @Nullable State savedInstanceState) {
        loginView = ActivityUtils.checkNotNull(view, "view cannot be null!");

        if (savedInstanceState != null && savedInstanceState instanceof LoginState) {
            LoginState loginState = (LoginState) savedInstanceState;
            username = loginState.username;
            password = loginState.password;
        }
    }

    @Override
    public State getInstanceState() {
        return new LoginState(this);
    }

    public void onLoginClick() {
        loginClicked = true;
        notifyChange();
        if (validate()) {
            loginView.getPresenter().performLogin(username, password);
        }
    }

    public void onSkipClick() {
        // Skip login
        loginView.getPresenter().performSkip();
    }

    public void onRegisterClick() {
        reportAnalyticsJoinNowTap();
    }

    public void onForgotPasswordClick() {
        loginView.showToastMsg(
                loginView.getActivity().getApplicationContext().getString(R.string.forgot_password),
                Toasty.ToastType.INFO);
    }

    @Bindable
    public boolean getAllowSkip() {
        return BuildConfig.DEBUG;
    }

    @Bindable
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
        notifyPropertyChanged(BR.username);
    }

    @Bindable
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
        notifyPropertyChanged(BR.password);
    }

    public String getUsernameError() {
        if (!loginClicked) {
            return "";
        }

        int usernameMinLength = loginView.getActivity().getResources().getInteger(R.integer.username_min_length);
        if (TextUtils.isEmpty(username)) {
            return loginView.getActivity().getString(R.string.error_please_fill_in_username);
        } else if (username.length() < usernameMinLength) {
            return String.format(
                    loginView.getActivity().getString(R.string.error_username_invalid_length),
                    usernameMinLength);
        }

        return "";
    }

    public String getPasswordError() {
        if (!loginClicked) {
            return "";
        }

        int passwordMinLength = loginView.getActivity().getResources().getInteger(R.integer.password_min_length);
        if (TextUtils.isEmpty(password)) {
            return loginView.getActivity().getString(R.string.error_please_fill_in_password);
        } else if (password.length() < passwordMinLength) {
            return String.format(
                    loginView.getActivity().getString(R.string.error_password_invalid_length),
                    passwordMinLength);
        }

        return "";
    }

    private boolean validate() {
        boolean valid;
        valid = TextUtils.isEmpty(getUsernameError());

        if (valid) {
            valid = TextUtils.isEmpty(getPasswordError());
        }

        Timber.d("Valid: %b", valid);

        return valid;
    }

    private static class LoginState extends State {

        final String username;
        final String password;

        LoginState(LoginViewModel viewModel) {
            super(viewModel);
            username = viewModel.username;
            password = viewModel.password;
        }

        LoginState(Parcel in) {
            super(in);
            username = in.readString();
            password = in.readString();
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(username);
            dest.writeString(password);
        }

        @SuppressWarnings("unused")
        public static final Parcelable.Creator<LoginState> CREATOR = new Parcelable.Creator<LoginState>() {
            @Override
            public LoginState createFromParcel(Parcel in) {
                return new LoginState(in);
            }

            @Override
            public LoginState[] newArray(int size) {
                return new LoginState[size];
            }
        };
    }

    private void reportAnalyticsJoinNowTap() {

    }

}
