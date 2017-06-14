package com.bosong.commentgallery;

import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.bosong.largeimagegallerylib.ImageUri;
import com.bosong.largeimagegallerylib.LargeImageGallery;

import java.util.ArrayList;
import java.util.List;

public class Main2Activity extends AppCompatActivity {

    private static final String[] SAMPLE_URIS = {
            "https://images-cn.ssl-images-amazon.com/images/I/81ghN3jk6AL._SL1000_.jpg",
            "https://images-cn.ssl-images-amazon.com/images/I/61YK4KgVWLL._SL1000_.jpg",
            "https://images-cn.ssl-images-amazon.com/images/I/81v6YVUdLnL._SL1000_.jpg",
            "https://images-cn.ssl-images-amazon.com/images/I/61y10jAltmL._SL1000_.jpg",
            "https://images-cn.ssl-images-amazon.com/images/I/71owNXqWERL._SL1000_.jpg",
            "https://images-cn.ssl-images-amazon.com/images/G/28/aplus_rbs/iPhone6PC_170223.jpg",
            "https://images-cn.ssl-images-amazon.com/images/G/28/kindle/2016/zhangr/DPfeature_img/voyag_featureimg._CB532421748_.jpg"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        LargeImageGallery gallery = (LargeImageGallery) findViewById(R.id.image_gallery);

        List<ImageUri> imageUris = new ArrayList<>();
        for (String url: SAMPLE_URIS) {
            imageUris.add(new ImageUri(Uri.EMPTY, Uri.parse(url)));
        }

        gallery.setData(imageUris);
    }
}
