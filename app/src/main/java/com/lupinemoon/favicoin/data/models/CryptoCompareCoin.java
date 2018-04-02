package com.lupinemoon.favicoin.data.models;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

import io.realm.CryptoCompareCoinRealmProxy;
import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;
import lombok.ToString;

@ToString
@Parcel(implementations = {CryptoCompareCoinRealmProxy.class},
        value = Parcel.Serialization.BEAN,
        analyze = {CryptoCompareCoin.class})
@SuppressWarnings("WeakerAccess, unused")
public class CryptoCompareCoin extends RealmObject {

    // Fields must be public
    @PrimaryKey
    @Index
    @SerializedName("Id")
    String id;

    @SerializedName("Url")
    String url;

    @SerializedName("ImageUrl")
    String imageUrl;

    @SerializedName("Name")
    String name;

    @Index
    @SerializedName("Symbol")
    String symbol;

    @Index
    @SerializedName("CoinName")
    String coinName;

    @SerializedName("FullName")
    String fullName;

    @SerializedName("Algorithm")
    String algorithm;

    @SerializedName("ProofType")
    String proofType;

    @SerializedName("FullyPremined")
    String fullyPremined;

    @SerializedName("TotalCoinSupply")
    String totalCoinSupply;

    @SerializedName("PreMinedValue")
    String preMinedValue;

    @SerializedName("TotalCoinsFreeFloat")
    String totalCoinsFreeFloat;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getCoinName() {
        return coinName;
    }

    public void setCoinName(String coinName) {
        this.coinName = coinName;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public String getProofType() {
        return proofType;
    }

    public void setProofType(String proofType) {
        this.proofType = proofType;
    }

    public String getFullyPremined() {
        return fullyPremined;
    }

    public void setFullyPremined(String fullyPremined) {
        this.fullyPremined = fullyPremined;
    }

    public String getTotalCoinSupply() {
        return totalCoinSupply;
    }

    public void setTotalCoinSupply(String totalCoinSupply) {
        this.totalCoinSupply = totalCoinSupply;
    }

    public String getPreMinedValue() {
        return preMinedValue;
    }

    public void setPreMinedValue(String preMinedValue) {
        this.preMinedValue = preMinedValue;
    }

    public String getTotalCoinsFreeFloat() {
        return totalCoinsFreeFloat;
    }

    public void setTotalCoinsFreeFloat(String totalCoinsFreeFloat) {
        this.totalCoinsFreeFloat = totalCoinsFreeFloat;
    }
}
