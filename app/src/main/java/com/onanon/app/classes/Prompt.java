package com.onanon.app.classes;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by ATerbo on 2/13/16.
 */
public class Prompt implements Parcelable{

    private String text;
    private String tag;

    public Prompt(){}

    public Prompt(String promptText, String tagText){
        this.text = promptText;
        this.text = tagText;
    }

    public String getText() {
        return text;
    }

    public String getTag() {
        return tag;
    }


    protected Prompt(Parcel in) {
        text = in.readString();
        tag = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(text);
        dest.writeString(tag);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Prompt> CREATOR = new Parcelable.Creator<Prompt>() {
        @Override
        public Prompt createFromParcel(Parcel in) {
            return new Prompt(in);
        }

        @Override
        public Prompt[] newArray(int size) {
            return new Prompt[size];
        }
    };
}
