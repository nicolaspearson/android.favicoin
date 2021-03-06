package com.lupinemoon.favicoin.data.network.interfaces;

import com.lupinemoon.favicoin.data.models.AuthToken;

import io.reactivex.Flowable;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface AuthApi {

    @FormUrlEncoded
    @POST("login")
    Flowable<AuthToken> postLogin(
            @Field("username") String username,
            @Field("password") String password);

}
