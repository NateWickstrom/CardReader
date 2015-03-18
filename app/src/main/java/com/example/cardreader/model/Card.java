package com.example.cardreader.model;

import com.google.gson.annotations.SerializedName;

/**
 * Card PoJo.
 */
public abstract class Card {

    @SerializedName("type")
    private String mType;
    @SerializedName("title")
    private String mTitle;
    @SerializedName("imageURL")
    private String mImageUrl;

    public String getType() {
        return mType;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getImageUrl() {
        return mImageUrl;
    }
}
