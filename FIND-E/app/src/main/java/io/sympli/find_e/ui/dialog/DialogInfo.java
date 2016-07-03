package io.sympli.find_e.ui.dialog;

import android.os.Parcel;
import android.os.Parcelable;

public final class DialogInfo implements Parcelable {
    protected String title;
    protected String body;
    protected String btnPositiveText;
    protected String btnNegativeText;
    protected int listResID;
    protected int listSelectedItem;

    public DialogInfo() {

    }

    public DialogInfo setListSelectedItem(int listSelectedItem) {
        this.listSelectedItem = listSelectedItem;
        return this;
    }

    public DialogInfo setListResID(int listResID) {
        this.listResID = listResID;
        return this;
    }

    public DialogInfo setTitle(String title) {
        this.title = title;
        return this;
    }

    public DialogInfo setBody(String body) {
        this.body = body;
        return this;
    }

    public DialogInfo setBtnPositiveText(String btnPositiveText) {
        this.btnPositiveText = btnPositiveText;
        return this;
    }

    public DialogInfo setBtnNegativeText(String btnNegativeText) {
        this.btnNegativeText = btnNegativeText;
        return this;
    }

    protected DialogInfo(Parcel in) {
        title = in.readString();
        body = in.readString();
        btnPositiveText = in.readString();
        btnNegativeText = in.readString();
        listResID = in.readInt();
    }

    public static final Creator<DialogInfo> CREATOR = new Creator<DialogInfo>() {
        @Override
        public DialogInfo createFromParcel(Parcel in) {
            return new DialogInfo(in);
        }

        @Override
        public DialogInfo[] newArray(int size) {
            return new DialogInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(body);
        dest.writeString(btnPositiveText);
        dest.writeString(btnNegativeText);
        dest.writeInt(listResID);
    }
}