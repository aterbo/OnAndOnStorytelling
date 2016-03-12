package com.aterbo.tellme.classes;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by ATerbo on 2/13/16.
 */
public class Prompt implements Parcelable{

    private String promptText;
    private int idNumber;
    private String tagText;

    public Prompt(String promptText, String tagText){
        this.promptText = promptText;
        this.tagText = tagText;
        this.idNumber = 1111;
    }

    public String getPromptText() {
        return promptText;
    }

    public String getTagText() {
        return tagText;
    }

    public int getIdNumber(){
        return idNumber;
    }

    protected Prompt(Parcel in) {
        promptText = in.readString();
        tagText = in.readString();
        idNumber = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(promptText);
        dest.writeString(tagText);
        dest.writeInt(idNumber);
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
