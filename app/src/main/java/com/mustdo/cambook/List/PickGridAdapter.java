package com.mustdo.cambook.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.mustdo.cambook.Ui.PickPhotoActivity;
import com.bumptech.glide.Glide;
import com.werb.pickphotoview.model.PickData;
import com.werb.pickphotoview.model.PickHolder;
import com.werb.pickphotoview.util.PickConfig;
import com.werb.pickphotoview.util.PickUtils;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Created by jieun on 2017-08-19.
 */

public class PickGridAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<String> imagePaths;
    private boolean isShowCamera;
    private boolean isClickSelectable;
    private int spanCount;
    private int maxSelectSize;
    private int selectIconColor;
    private ArrayList<String> selectPath;
    private PickPhotoActivity context;
    private View.OnClickListener imgClick;
    private int scaleSize;
    private Context mContext;

    public PickGridAdapter(Context context, ArrayList<String> imagePaths, PickData pickData, View.OnClickListener imgClick) {
        this.mContext = context;
        this.context = (PickPhotoActivity) context;
        this.imagePaths = imagePaths;
        this.isShowCamera = pickData.isShowCamera();
        this.spanCount = pickData.getSpanCount();
        this.maxSelectSize = pickData.getPickPhotoSize();
        this.isClickSelectable = pickData.isClickSelectable();
        this.selectIconColor = pickData.getSelectIconColor();
        this.imgClick = imgClick;
        selectPath = PickHolder.getStringPaths();
        buildScaleSize();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == PickConfig.CAMERA_TYPE) {
            return new CameraViewHolder(LayoutInflater.from(context).inflate(com.werb.pickphotoview.R.layout.pick_item_camera_layout, parent, false));
        } else {
            return new GridImageViewHolder(LayoutInflater.from(context).inflate(com.werb.pickphotoview.R.layout.pick_item_grid_layout, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof GridImageViewHolder) {
            String path;
            if (isShowCamera) {
                path = imagePaths.get(position - 1);
            } else {
                path = imagePaths.get(position);
            }
            GridImageViewHolder gridImageViewHolder = (GridImageViewHolder) holder;
            gridImageViewHolder.bindItem(path);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (isShowCamera) {
            if (position == 0) {
                return PickConfig.CAMERA_TYPE;
            } else {
                return position;
            }
        } else {
            return position;
        }
    }

    @Override
    public int getItemCount() {
        if (isShowCamera) {
            return imagePaths.size() + 1;
        } else {
            return imagePaths.size();
        }
    }

    @Override
    public void onViewRecycled(RecyclerView.ViewHolder holder) {
        if(holder instanceof GridImageViewHolder) {
            GridImageViewHolder gridImageViewHolder = (GridImageViewHolder) holder;
            Glide.with(context).clear(gridImageViewHolder.weekImage);
        }
        super.onViewRecycled(holder);
    }

    public void updateData(ArrayList<String> paths) {
        imagePaths = paths;
        notifyDataSetChanged();
    }

    // ViewHolder
    private class GridImageViewHolder extends RecyclerView.ViewHolder {

        private ImageView selectImage, weekImage;
        private FrameLayout selectLayout;

        GridImageViewHolder(View itemView) {
            super(itemView);
            ImageView gridImage = (ImageView) itemView.findViewById(com.werb.pickphotoview.R.id.iv_grid);
            selectImage = (ImageView) itemView.findViewById(com.werb.pickphotoview.R.id.iv_select);
            selectLayout = (FrameLayout) itemView.findViewById(com.werb.pickphotoview.R.id.frame_select_layout);
            selectLayout.setVisibility(View.GONE);
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) gridImage.getLayoutParams();
            params.width = scaleSize;
            params.height = scaleSize;

            final WeakReference<ImageView> imageViewWeakReference = new WeakReference<>(gridImage);
            weekImage = imageViewWeakReference.get();
        }

        void bindItem(final String path) {
            if (selectPath.contains(path)) {
                select();
            } else {
                unSelect();
            }
            if (weekImage != null) {
                Glide.with(context)
                        .load(Uri.parse("file://" + path))
                        .into(weekImage);
                selectLayout.setTag(com.werb.pickphotoview.R.id.pick_image_path,path);
                if(maxSelectSize == 1){
                    selectLayout.setOnClickListener(singleClick);
                }else {
                    selectLayout.setOnClickListener(moreClick);
                }
                weekImage.setTag(com.werb.pickphotoview.R.id.pick_image_path, path);
                if (isClickSelectable)
                {
                    if(maxSelectSize == 1){
                        weekImage.setOnClickListener(singleClick);
                    }else {
                        weekImage.setOnClickListener(moreClick);
                    }
                }
                else
                {
                    weekImage.setOnClickListener(imgClick);
                }
            }
        }

        void select() {
            Drawable drawable = ContextCompat.getDrawable(mContext, com.werb.pickphotoview.R.mipmap.pick_ic_select);
            drawable.clearColorFilter();
            drawable.setColorFilter(selectIconColor, PorterDuff.Mode.SRC_ATOP);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                selectImage.setBackground(ContextCompat.getDrawable(mContext, com.werb.pickphotoview.R.mipmap.pick_ic_select));
            }
            else
            {
                //noinspection deprecation
                selectImage.setBackgroundDrawable(ContextCompat.getDrawable(mContext, com.werb.pickphotoview.R.mipmap.pick_ic_select));
            }
            selectImage.setTag(com.werb.pickphotoview.R.id.pick_is_select, true);
        }

        void unSelect() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                selectImage.setBackground(ContextCompat.getDrawable(mContext, com.werb.pickphotoview.R.mipmap.pick_ic_un_select));
            }
            else
            {
                //noinspection deprecation
                selectImage.setBackgroundDrawable(ContextCompat.getDrawable(mContext, com.werb.pickphotoview.R.mipmap.pick_ic_un_select));
            }
            selectImage.setTag(com.werb.pickphotoview.R.id.pick_is_select, false);
        }

        void addPath(String path) {
            selectPath.add(path);
            PickHolder.setStringPaths(selectPath);
            context.updateSelectText(String.valueOf(selectPath.size()));
        }

        void removePath(String path) {
            selectPath.remove(path);
            PickHolder.setStringPaths(selectPath);
            context.updateSelectText(String.valueOf(selectPath.size()));
        }

        View.OnClickListener moreClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String path = (String) v.getTag(com.werb.pickphotoview.R.id.pick_image_path);
                boolean isSelect = (boolean) selectImage.getTag(com.werb.pickphotoview.R.id.pick_is_select);
                if (isSelect) {
                    if (selectPath.contains(path)) {
                        unSelect();
                        removePath(path);
                    }
                } else {
                    if (selectPath.size() < maxSelectSize) {
                        if (!selectPath.contains(path)) {
                            select();
                            addPath(path);
                        }
                    } else {
                        Toast.makeText(context, String.format(context.getString(com.werb.pickphotoview.R.string.pick_photo_size_limit), String.valueOf(maxSelectSize)), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        };

        View.OnClickListener singleClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(selectPath.size() == maxSelectSize){
                    Toast.makeText(context, String.format(context.getString(com.werb.pickphotoview.R.string.pick_photo_size_limit), String.valueOf(maxSelectSize)), Toast.LENGTH_SHORT).show();
                    return;
                }
                String path = (String) v.getTag(com.werb.pickphotoview.R.id.pick_image_path);
                select();
                addPath(path);
                context.select();
            }
        };

    }

    private class CameraViewHolder extends RecyclerView.ViewHolder {

        CameraViewHolder(View itemView) {
            super(itemView);

            ViewGroup.LayoutParams params = itemView.getLayoutParams();
            params.width = scaleSize;
            params.height = scaleSize;
            itemView.setOnClickListener(cameraClick);
        }

        View.OnClickListener cameraClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    File photoFile = PickUtils.getInstance(context).getPhotoFile(v.getContext());
                    photoFile.delete();
                    if (photoFile.createNewFile()) {
                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, PickUtils.getInstance(context).getUri(photoFile));
                        context.startActivityForResult(intent, PickConfig.CAMERA_PHOTO_DATA);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
    }

    private void buildScaleSize() {
        int screenWidth = PickUtils.getInstance(context).getWidthPixels();
        int space = PickUtils.getInstance(context).dp2px(PickConfig.ITEM_SPACE);
        scaleSize = (screenWidth - (spanCount + 1) * space) / spanCount;
    }

    public ArrayList<String> getSelectPath() {
        return selectPath;
    }

    public void setSelectPath(ArrayList<String> selectPath){
        this.selectPath = selectPath;
    }

    private Handler handler = new Handler(Looper.getMainLooper());

}
