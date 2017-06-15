package com.bosong.largeimagegallerylib;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.DrawableRes;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bosong.frescozoomablelib.zoomable.DefaultZoomableController;
import com.bosong.frescozoomablelib.zoomable.DoubleTapGestureListener;
import com.bosong.frescozoomablelib.zoomable.ZoomableDraweeView;
import com.facebook.common.executors.CallerThreadExecutor;
import com.facebook.common.internal.Closeables;
import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.BaseDataSubscriber;
import com.facebook.datasource.DataSource;
import com.facebook.datasource.DataSubscriber;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.controller.ControllerListener;
import com.facebook.drawee.drawable.ProgressBarDrawable;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.imageformat.ImageFormat;
import com.facebook.imageformat.ImageFormatChecker;
import com.facebook.imagepipeline.common.ImageDecodeOptions;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.common.RotationOptions;
import com.facebook.imagepipeline.image.ImageInfo;
import com.facebook.imagepipeline.memory.PooledByteBuffer;
import com.facebook.imagepipeline.memory.PooledByteBufferInputStream;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * Created by boson on 2016/12/21.
 */

public class LargeImageGalleryAdapter extends PagerAdapter {
    List<ImageUri> mData;
    ZoomableDraweeView[] mImageViewList;

    private int mPlaceholderImageResId;
    private int mFailureImageResId;

    private View.OnClickListener mItemClickListener;
    private View.OnLongClickListener mItemLongClickListener;

    public LargeImageGalleryAdapter(){
        this(null);
    }

    public LargeImageGalleryAdapter(List<ImageUri> data){
        setData(data);
    }

    public void setData(List<ImageUri> imageUris){
        setData(imageUris, 0, 0);
    }

    public void setData(List<ImageUri> imageUris, @DrawableRes int placeholderImageResId, @DrawableRes int failureImageResId){
        if(imageUris != null){
            mData = imageUris;
            mImageViewList = new ZoomableDraweeView[mData.size()];
        }
        mPlaceholderImageResId = placeholderImageResId;
        mFailureImageResId = failureImageResId;
    }

    public void setOnItemClickListener(View.OnClickListener listener){
        this.mItemClickListener = listener;
    }

    public void setOnItemLongClickListener(View.OnLongClickListener longClickListener) {
        mItemLongClickListener = longClickListener;
    }

    @Override
    public int getCount() {
        return mData != null ? mData.size() : 0;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        if(mData != null && mData.size() > position){
            ZoomableDraweeView zoomableDraweeView = null;
            if(mImageViewList != null && mImageViewList.length > position && mImageViewList[position] != null){
                zoomableDraweeView = mImageViewList[position];

            }else{

                final ImageRequest request = ImageRequestBuilder.newBuilderWithSource(mData.get(position % mData.size()).getHighUri())
                        .setResizeOptions(ResizeOptions.forDimensions(1080, 1080))
                        .setRotationOptions(RotationOptions.autoRotate())
                        .build();

                zoomableDraweeView = new ZoomableDraweeView(container.getContext());
                zoomableDraweeView.setAllowTouchInterceptionWhileZoomed(true);
                zoomableDraweeView.setIsLongpressEnabled(true);

                final ZoomableDraweeView finalZoomableDraweeView = zoomableDraweeView;
                zoomableDraweeView.setTapListener(new DoubleTapGestureListener(finalZoomableDraweeView){
                    @Override
                    public boolean onSingleTapConfirmed(MotionEvent e) {
                        if(mItemClickListener != null){
                            mItemClickListener.onClick(finalZoomableDraweeView);
                        }
                        return super.onSingleTapConfirmed(e);
                    }

                    @Override
                    public void onLongPress(MotionEvent e) {
                        Context context = finalZoomableDraweeView.getContext();
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        CharSequence[] items = new String[]{
                                context.getString(R.string.save_origin_image)
                        };

                        builder.setItems(items, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case 0:
                                        saveImage(finalZoomableDraweeView, request);
                                        dialog.dismiss();
                                        break;
                                }
                            }
                        }).create().show();
                    }
                });

                DraweeController controller = Fresco.newDraweeControllerBuilder()
                        .setLowResImageRequest(ImageRequest.fromUri(mData.get(position % mData.size()).getLowUri()))
                        .setImageRequest(request)
                        .setAutoPlayAnimations(true)
                        .build();
                zoomableDraweeView.setController(controller);
                GenericDraweeHierarchyBuilder builder =
                        new GenericDraweeHierarchyBuilder(container.getResources());
                GenericDraweeHierarchy hierarchy = builder
                        .setFadeDuration(300)
                        .setProgressBarImage(new CircleProgress.Builder()
                                .setTextShow(false)
                                .setStyle(CircleProgress.CircleStyle.FAN)
                                .setGradientType(CircleProgress.GradientType.SWEEP)
                                .build())
                        .setActualImageScaleType(ScalingUtils.ScaleType.FIT_CENTER)
                        .build();

                if(mPlaceholderImageResId > 0){
                    hierarchy.setPlaceholderImage(mPlaceholderImageResId, ScalingUtils.ScaleType.FIT_CENTER);
                }
                if(mFailureImageResId > 0){
                    hierarchy.setFailureImage(mFailureImageResId, ScalingUtils.ScaleType.FIT_CENTER);
                }
                zoomableDraweeView.setHierarchy(hierarchy);

                mImageViewList[position] = zoomableDraweeView;
            }
            container.addView(zoomableDraweeView);
            container.requestDisallowInterceptTouchEvent(true);

            return zoomableDraweeView;
        }

        return null;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        if(mImageViewList != null && mImageViewList.length > position && mImageViewList[position] != null){
            ZoomableDraweeView imageView = mImageViewList[position];
            container.removeView(imageView);
        }
    }


    public ZoomableDraweeView getItem(int position){
        if(position > -1 && mImageViewList != null && mImageViewList.length > position){
            return mImageViewList[position];
        }
        return null;
    }

    private void saveImage(final View v, final ImageRequest request) {
        DataSource<CloseableReference<PooledByteBuffer>> dataSource =
                Fresco.getImagePipeline().fetchEncodedImage(request, v.getContext());

        DataSubscriber<CloseableReference<PooledByteBuffer>> dataSubscriber =
                new BaseDataSubscriber<CloseableReference<PooledByteBuffer>>() {
                    @Override
                    protected void onNewResultImpl(
                            DataSource<CloseableReference<PooledByteBuffer>> dataSource) {
                        if (!dataSource.isFinished()) {
                            return;
                        }
                        CloseableReference<PooledByteBuffer> ref = dataSource.getResult();

                        if (ref != null) {
                            try {
                                Context context = v.getContext();
                                PooledByteBuffer result = ref.get();
                                InputStream is = new PooledByteBufferInputStream(result);

                                File pictureDir = new File(Environment.getExternalStorageDirectory(), "worktile/pictures");
                                if (!pictureDir.exists()) {
                                    pictureDir.mkdirs();
                                }
                                String fileName = request.getSourceFile().getName();
                                File imageFile = new File(pictureDir, fileName);

                                OutputStream out = new FileOutputStream(imageFile);
                                try {
                                    byte[] buf = new byte[1024];
                                    int len;
                                    while ((len = is.read(buf)) > 0) {
                                        out.write(buf, 0, len);
                                    }
                                    v.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(v.getContext(), R.string.save_image_success, Toast.LENGTH_SHORT).show();
                                        }
                                    });

                                    try {
                                        MediaStore.Images.Media.insertImage(context.getContentResolver(),
                                                imageFile.getAbsolutePath(), fileName, null);
                                    } catch (FileNotFoundException e) {
                                        e.printStackTrace();
                                    }
                                    context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(imageFile)));

                                } finally {
                                    out.close();
                                    is.close();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                v.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(v.getContext(), R.string.save_image_failed, Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } finally {
                                CloseableReference.closeSafely(ref);
                            }
                        }
                    }

                    @Override
                    protected void onFailureImpl(DataSource<CloseableReference<PooledByteBuffer>> dataSource) {
                        Throwable t = dataSource.getFailureCause();
                        // handle failure
                    }
                };

        dataSource.subscribe(dataSubscriber, CallerThreadExecutor.getInstance());
    }
}
