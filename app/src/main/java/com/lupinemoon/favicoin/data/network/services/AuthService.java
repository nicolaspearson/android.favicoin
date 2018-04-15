package com.lupinemoon.favicoin.data.network.services;

import android.content.Context;

import com.lupinemoon.favicoin.MainApplication;
import com.lupinemoon.favicoin.data.models.AuthToken;
import com.lupinemoon.favicoin.data.network.interfaces.AuthApi;
import com.lupinemoon.favicoin.data.network.rest.ServiceGenerator;
import com.lupinemoon.favicoin.presentation.utils.Constants;
import com.lupinemoon.favicoin.presentation.utils.DateTimeUtils;

import io.reactivex.Flowable;
import io.reactivex.functions.Function;

public class AuthService {

    private static AuthService authService;

    private AuthApi authApi;

    private AuthService() {
        // Empty Constructor
    }

    public static synchronized AuthService getInstance() {
        if (authService == null) {
            authService = new AuthService();
        }
        return authService;
    }

    private synchronized AuthApi getAuthApi(Context context) {
        // We want to reuse the same instance
        if (authApi == null) {
            authApi = ServiceGenerator.getInstance().createService(AuthApi.class, context);
        }

        return authApi;
    }

    public Flowable<AuthToken> doLogin(
            final Context context,
            String username,
            String password) {
        return getAuthApi(context).postLogin(username, password)
                .map(token -> {
                    // Add the created_at date to the token
                    if (token != null) {
                        token.setCreatedAt(DateTimeUtils.getCurrentTimeStamp());
                    }

                    MainApplication.getStorage(context).putObject(
                            Constants.KEY_AUTH_TOKEN,
                            token);
                    MainApplication.setLoggedIn(true);
                    return token;
                });
    }
}
