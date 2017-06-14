package com.bosong.largeimagegallerylib;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Android Studio.
 * User: Moki
 * Email: mosicou@gmail.com
 * Date: 2017/6/14
 * Time: 10:47
 * Desc:
 */

public class ImageUri implements Parcelable {
    private Uri lowUri;
    private Uri highUri;

    public ImageUri(Uri lowUri, Uri highUri) {
        this.lowUri = lowUri;
        this.highUri = highUri;
    }

    protected ImageUri(Parcel in) {
        lowUri = in.readParcelable(Uri.class.getClassLoader());
        highUri = in.readParcelable(Uri.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(lowUri, flags);
        dest.writeParcelable(highUri, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ImageUri> CREATOR = new Creator<ImageUri>() {
        @Override
        public ImageUri createFromParcel(Parcel in) {
            return new ImageUri(in);
        }

        @Override
        public ImageUri[] newArray(int size) {
            return new ImageUri[size];
        }
    };

    public Uri getLowUri() {
        return lowUri;
    }

    public void setLowUri(Uri lowUri) {
        this.lowUri = lowUri;
    }

    public Uri getHighUri() {
        return highUri;
    }

    public void setHighUri(Uri highUri) {
        this.highUri = highUri;
    }
}
