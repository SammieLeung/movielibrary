package com.hphtv.movielibrary.util;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.hphtv.movielibrary.sqlite.bean.VideoFile;
import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.data.ConstData;
import com.hphtv.movielibrary.sqlite.dao.VideoFileDao;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;


/**
 * Created by tchip on 18-6-2.
 */

public class FilmCreator {
    public static final String TAG = FilmCreator.class.getSimpleName();

    /**
     * 大图 倾斜视图
     *
     * @param context
     * @param soure
     * @return
     */
    public static Bitmap CreateFlimBitmap(Context context, Bitmap soure) {
        if (soure != null) {
            Bitmap baseBitmap = Bitmap.createBitmap(DensityUtil.dip2px(context, 300), DensityUtil.dip2px(context, 400), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(baseBitmap);//初始化画布
            Paint paint = new Paint();//初始化画笔
            paint.setAntiAlias(true);//设置抗锯齿

            Resources res = context.getResources();
            Bitmap sourceBmp = soure;//目标图片

            //胶片底图
            Bitmap filmFrame = BitmapFactory.decodeResource(res, R.drawable.film_bg_16_9);

            Matrix matrix1 = new Matrix();
            Matrix matrix2 = new Matrix();

            int filmWidth = filmFrame.getWidth();
            int filmHeight = filmFrame.getHeight();

            int sourceWidth = sourceBmp.getWidth();
            int sourceHeight = sourceBmp.getHeight();

            float scaleX = 0.9f;
            float scaleY = 0.9f;


//            float sourceScaleX = ((filmWidth - 27) * scaleX) / sourceWidth;
//            float sourceScaleY = ((filmHeight - 170) * scaleY) / sourceHeight;
            float sourceScaleX = ((filmWidth - DensityUtil.dip2px(context,18)) * scaleX) / sourceWidth;
            float sourceScaleY = ((filmHeight - DensityUtil.dip2px(context,114)) * scaleY) / sourceHeight;
            matrix1.postScale(scaleX, scaleY);//将胶片边框缩小到0.9倍

            float curFilmWidth = filmWidth * scaleX;//当前实际胶片尺寸
            float curFilmHeight = filmHeight * scaleY;

            float curSourceHeight = sourceHeight * sourceScaleY;

            float sy = (curFilmHeight - curSourceHeight) / 2;


            matrix2.postScale(sourceScaleX, sourceScaleY);//封面缩放到适合大小
            matrix2.postTranslate(12, sy);  //封面图片居中


            Matrix matrix3 = new Matrix(matrix1);
            Matrix matrix4 = new Matrix(matrix2);//复制上面的效果
            matrix3.postTranslate(curFilmWidth - 2, 0);
            matrix4.postTranslate(curFilmWidth - 2, 0);//往x轴水平偏移，调整位置
            matrix1.postTranslate(0, (-curFilmHeight + 50) / 2);
            matrix2.postTranslate(0, (-curFilmHeight + 50) / 2);
            matrix3.postTranslate(0, (-curFilmHeight + 50) / 2);
            matrix4.postTranslate(0, (-curFilmHeight + 50) / 2);//全体往y轴垂直偏移，调整位置
            matrix1.postRotate(45);
            matrix2.postRotate(45);
            matrix3.postRotate(45);
            matrix4.postRotate(45);//全体旋转
            canvas.drawColor(res.getColor(R.color.c14171b));//背景颜色
            canvas.drawBitmap(sourceBmp, matrix2, paint);
            canvas.drawBitmap(sourceBmp, matrix4, paint);//绘制原始图片

            canvas.drawBitmap(filmFrame, matrix1, paint);
            canvas.drawBitmap(filmFrame, matrix3, paint);//绘制边框
            return baseBitmap;
        }
        return null;
    }

    /**
     * 小图 水平全覆盖
     *
     * @param context
     * @param soure
     * @return
     */
    public static Bitmap CreateFlimBitmap2(Context context, Bitmap soure) {
        if (soure != null) {
            int width = DensityUtil.dip2px(context, 300);
            int height = DensityUtil.dip2px(context, 400);

            Bitmap baseBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(baseBitmap);//初始化画布
            Paint paint = new Paint();//初始化画笔
            paint.setAntiAlias(true);//设置抗锯齿

            Resources res = context.getResources();
            Bitmap sourceBmp = soure;

            Bitmap filmFrame = BitmapFactory.decodeResource(res, R.drawable.film_bg_4_3);

            Matrix matrix1 = new Matrix();
            Matrix matrix2 = new Matrix();

            int flimWidth = filmFrame.getWidth();
            int filmHeight = filmFrame.getHeight();

            int sourceWidth = sourceBmp.getWidth();
            int sourceHeight = sourceBmp.getHeight();

            float scaleX = width * 1.0f / flimWidth;
            float scaleY = height * 1.0f / filmHeight;

            float sourceScale = (filmHeight * scaleY) / sourceHeight * 0.7f;

            matrix1.postScale(scaleX, scaleY);//拉伸胶片边框

            float curFilmWidth = flimWidth * scaleX;
            float curFilmHeight = filmHeight * scaleY;

            float curSourceHeight = sourceHeight * sourceScale;
            float curSourceWidth = sourceWidth * sourceScale;

            float sy = (curFilmHeight - curSourceHeight) / 2;
            float sx = (curFilmWidth - curSourceWidth) / 2;

            matrix2.postScale(sourceScale, sourceScale);
            matrix2.postTranslate(sx, sy);  //将原始图片缩放到合适大小并移动到边框内

            canvas.drawColor(res.getColor(R.color.c14171b));//背景颜色
            canvas.drawBitmap(sourceBmp, matrix2, paint);

            canvas.drawBitmap(filmFrame, matrix1, paint);
            return baseBitmap;
        }
        return null;
    }

    public static String saveBitmap(Context context, Bitmap bitmap) {
        // 首先保存图片
        File appDir = new File(context.getExternalCacheDir(), "cache_image");
        if (!appDir.exists()) {
            appDir.mkdir();
        }
        String fileName = "poster_" + System.currentTimeMillis() + ".png";
        File file = new File(appDir, fileName);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 把文件插入到系统图库
        try {
            MediaStore.Images.Media.insertImage(context.getContentResolver(), file.getAbsolutePath(), fileName, null);
            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse(file.getAbsolutePath())));
            return file.getAbsolutePath();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        // 通知图库更新

    }

    public static void CreatePoster(final Context context, final ImageView imgView, final int ViewType, final VideoFile videoFile, final Handler uiThreadHanlder) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                //Log.v(TAG, "videofile=" + videoFile.getFilename() + "============>");
                try {
                    boolean isThumbnailExsit = false;

                    if (!TextUtils.isEmpty(videoFile.getThumbnail()) && new File(videoFile.getThumbnail()).exists()) {
                        isThumbnailExsit = true;
                        String tmpPath = null;
                        //Log.v(TAG, videoFile.getFilename() + " isThumbnailExsit true and thumb " + videoFile.getThumbnail());

                        if (ViewType == ConstData.CardViewType.VIEWTYPE_SIMPLE) {
                            if (imgView.getId() == R.id.pos_1) {
                                tmpPath = videoFile.getThumbnail();
                            } else {
                                tmpPath = videoFile.getThumbnail_s();
                            }
                        } else if (ViewType == ConstData.CardViewType.VIEWTYPE_EXPEND) {
                            if (imgView.getId() == R.id.pos_1 || imgView.getId() == R.id.pos_2) {
                                tmpPath = videoFile.getThumbnail();
                            } else {
                                tmpPath = videoFile.getThumbnail_s();
                            }
                        } else if (ViewType == ConstData.CardViewType.VIEWTYPE_SMALL) {
                            tmpPath = videoFile.getThumbnail_s();
                            //Log.v(TAG, videoFile.getFilename() + " pic exsit tmpPath=" + tmpPath);
                        }else {
                            tmpPath=videoFile.getThumbnail();
                        }
                        final String path = tmpPath;
                        uiThreadHanlder.post(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Glide.with(context).load(path).apply(RequestOptions.placeholderOf(R.mipmap.ic_poster_default).centerCrop()).into(imgView);
                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                            }
                        });
                    }

                    if (!isThumbnailExsit) {
                        Bitmap bmp2 = ThumbnailUtils.createVideoThumbnail(videoFile.getUri(), MediaStore.Video.Thumbnails.FULL_SCREEN_KIND);
                        //Log.v(TAG, videoFile.getFilename() + " isThumbnailExsit none but bmp2 exsit is " + (bmp2 == null));
                        if (bmp2 != null) {
                            Bitmap baseBitmap = null;
                            Bitmap baseBitmap_s = null;
                            baseBitmap = FilmCreator.CreateFlimBitmap(context, bmp2);
                            baseBitmap_s = FilmCreator.CreateFlimBitmap2(context, bmp2);

                            ByteArrayOutputStream bao = new ByteArrayOutputStream();
                            baseBitmap.compress(Bitmap.CompressFormat.PNG, 100, bao);

                            ByteArrayOutputStream bao_s = new ByteArrayOutputStream();
                            baseBitmap_s.compress(Bitmap.CompressFormat.PNG, 100, bao_s);

                            String filepath = "";
                            String filepath_s = "";
                            try {
                                filepath = FilmCreator.saveBitmap(context, baseBitmap);
                                filepath_s = FilmCreator.saveBitmap(context, baseBitmap_s);
                                VideoFileDao videoFileDao = new VideoFileDao(context);
                                ContentValues contentValues = new ContentValues();
                                contentValues.put("thumbnail", filepath);
                                contentValues.put("thumbnail_s", filepath_s);
                                videoFileDao.update(contentValues, "id=?", new String[]{String.valueOf(videoFile.getId())});
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            String tmpFilepath = null;

                            if (ViewType == ConstData.CardViewType.VIEWTYPE_SIMPLE) {
                                if (imgView.getId() == R.id.pos_1) {
                                    tmpFilepath = filepath;
                                } else {
                                    tmpFilepath = filepath_s;
                                }
                            } else if (ViewType == ConstData.CardViewType.VIEWTYPE_EXPEND) {
                                if (imgView.getId() == R.id.pos_1 || imgView.getId() == R.id.pos_2) {
                                    tmpFilepath = filepath;
                                } else {
                                    tmpFilepath = filepath_s;
                                }
                            } else if (ViewType == ConstData.CardViewType.VIEWTYPE_SMALL) {
                                tmpFilepath = filepath_s;
                            }

                            final String path = tmpFilepath;
                            //Log.v(TAG, videoFile.getFilename() + " make film ");
                            uiThreadHanlder.post(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        Glide.with(context).load(path).apply(RequestOptions.placeholderOf(R.mipmap.ic_poster_default).centerCrop()).into(imgView);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        } else {
                            //Log.v(TAG, videoFile.getFilename() + "+ make ic_poster_default ");
                            uiThreadHanlder.post(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        Glide.with(context).load(R.mipmap.ic_poster_default).apply(RequestOptions.placeholderOf(R.mipmap.ic_poster_default)).into(imgView);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        }

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                //Log.v(TAG, videoFile.getFilename() + " ----------------end------>");

            }
        }).start();
    }

//    public static String CreatePoster2(final Context context, final ImageView imgView, final int ViewType, final VideoFile videoFile) {
//        Log.v(TAG, "videofile=" + videoFile.getFilename() + "============>");
//        try {
//            boolean isThumbnailExsit = false;
//
//            if (!TextUtils.isEmpty(videoFile.getThumbnail()) && new File(videoFile.getThumbnail()).exists()) {
//                isThumbnailExsit = true;
//                String tmpPath = null;
//                Log.v(TAG, videoFile.getFilename() + " isThumbnailExsit true and thumb " + videoFile.getThumbnail());
//
//                if (ViewType == VIEWTYPE_SIMPLE) {
//                    if (imgView.getId() == R.id.pos_1) {
//                        tmpPath = videoFile.getThumbnail();
//                    } else {
//                        tmpPath = videoFile.getThumbnail_s();
//                    }
//                } else if (ViewType == VIEWTYPE_EXPEND) {
//                    if (imgView.getId() == R.id.pos_1 || imgView.getId() == R.id.pos_2) {
//                        tmpPath = videoFile.getThumbnail();
//                    } else {
//                        tmpPath = videoFile.getThumbnail_s();
//                    }
//                } else if (ViewType == VIEWTYPE_SMALL) {
//                    tmpPath = videoFile.getThumbnail_s();
//                    Log.v(TAG, videoFile.getFilename() + " pic exsit tmpPath=" + tmpPath);
//                }
//                final String path = tmpPath;
//                return path;
//            }
//
//            if (!isThumbnailExsit) {
//                Bitmap bmp2 = ThumbnailUtils.createVideoThumbnail(videoFile.getUri(), MediaStore.Video.Thumbnails.FULL_SCREEN_KIND);
//                Log.v(TAG, videoFile.getFilename() + " isThumbnailExsit none but bmp2 exsit is " + (bmp2 == null));
//                if (bmp2 != null) {
//                    Bitmap baseBitmap = null;
//                    Bitmap baseBitmap_s = null;
//                    baseBitmap = FilmCreator.CreateFlimBitmap(context, bmp2);
//                    baseBitmap_s = FilmCreator.CreateFlimBitmap2(context, bmp2);
//
//                    ByteArrayOutputStream bao = new ByteArrayOutputStream();
//                    baseBitmap.compress(Bitmap.CompressFormat.PNG, 100, bao);
//
//                    ByteArrayOutputStream bao_s = new ByteArrayOutputStream();
//                    baseBitmap_s.compress(Bitmap.CompressFormat.PNG, 100, bao_s);
//
//                    String filepath = "";
//                    String filepath_s = "";
//                    try {
//                        filepath = FilmCreator.saveBitmap(context, baseBitmap);
//                        filepath_s = FilmCreator.saveBitmap(context, baseBitmap_s);
//                        VideoFileDao videoFileDao = new VideoFileDao(context);
//                        ContentValues contentValues = new ContentValues();
//                        contentValues.put("thumbnail", filepath);
//                        contentValues.put("thumbnail_s", filepath_s);
//                        videoFileDao.update(contentValues, "id=?", new String[]{String.valueOf(videoFile.getId())});
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                    String tmpFilepath = null;
//
//                    if (ViewType == VIEWTYPE_SIMPLE) {
//                        if (imgView.getId() == R.id.pos_1) {
//                            tmpFilepath = filepath;
//                        } else {
//                            tmpFilepath = filepath_s;
//                        }
//                    } else if (ViewType == VIEWTYPE_EXPEND) {
//                        if (imgView.getId() == R.id.pos_1 || imgView.getId() == R.id.pos_2) {
//                            tmpFilepath = filepath;
//                        } else {
//                            tmpFilepath = filepath_s;
//                        }
//                    } else if (ViewType == VIEWTYPE_SMALL) {
//                        tmpFilepath = filepath_s;
//                    }
//
//                    final String path = tmpFilepath;
//                    Log.v(TAG, videoFile.getFilename() + " make film ");
//                    return path;
//                } else {
//                    Log.v(TAG, videoFile.getFilename() + "+ make ic_poster_default ");
//                    return null;
//                }
//
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        Log.v(TAG, videoFile.getFilename() + " ----------------end------>");
//        return null;
//    }
//
//
//    public static Runnable CreatePosterThread(final Context context, final ImageView imgView, final int ViewType, final VideoFile videoFile, final Handler uiThreadHanlder) {
//
//        Runnable runnable = new Runnable() {
//            @Override
//            public void run() {
//                //Log.v(TAG, "videofile=" + videoFile.getFilename() + "============>");
//                try {
//                    boolean isThumbnailExsit = false;
//
//                    if (!TextUtils.isEmpty(videoFile.getThumbnail()) && new File(videoFile.getThumbnail()).exists()) {
//                        isThumbnailExsit = true;
//                        String tmpPath = null;
//                        //Log.v(TAG, videoFile.getFilename() + " isThumbnailExsit true       and thumb " + videoFile.getThumbnail());
//
//                        if (ViewType == VIEWTYPE_SIMPLE) {
//                            if (imgView.getId() == R.id.pos_1) {
//                                tmpPath = videoFile.getThumbnail();
//                            } else {
//                                tmpPath = videoFile.getThumbnail_s();
//                            }
//                        } else if (ViewType == VIEWTYPE_EXPEND) {
//                            if (imgView.getId() == R.id.pos_1 || imgView.getId() == R.id.pos_2) {
//                                tmpPath = videoFile.getThumbnail();
//                            } else {
//                                tmpPath = videoFile.getThumbnail_s();
//                            }
//                        } else if (ViewType == VIEWTYPE_SMALL) {
//                            tmpPath = videoFile.getThumbnail_s();
//                            //Log.v(TAG, videoFile.getFilename() + " pic exsit tmpPath=" + tmpPath);
//                        }
//                        final String path = tmpPath;
//                        uiThreadHanlder.post(new Runnable() {
//                            @Override
//                            public void run() {
//                                Glide.with(context).load(path).centerCrop().placeholder(R.mipmap.ic_poster_default)
//                                        .error(R.mipmap.ic_poster_default).into(imgView);
//
//                            }
//                        });
//                    }
//
//                    if (!isThumbnailExsit) {
//                        Bitmap bmp2 = ThumbnailUtils.createVideoThumbnail(videoFile.getUri(), MediaStore.Video.Thumbnails.FULL_SCREEN_KIND);
//                        //Log.v(TAG, videoFile.getFilename() + " isThumbnailExsit none but bmp2 exsit is " + (bmp2 == null));
//                        if (bmp2 != null) {
//                            Bitmap baseBitmap = null;
//                            Bitmap baseBitmap_s = null;
//                            baseBitmap = FilmCreator.CreateFlimBitmap(context, bmp2);
//                            baseBitmap_s = FilmCreator.CreateFlimBitmap2(context, bmp2);
//
//                            ByteArrayOutputStream bao = new ByteArrayOutputStream();
//                            baseBitmap.compress(Bitmap.CompressFormat.PNG, 100, bao);
//
//                            ByteArrayOutputStream bao_s = new ByteArrayOutputStream();
//                            baseBitmap_s.compress(Bitmap.CompressFormat.PNG, 100, bao_s);
//
//                            String filepath = "";
//                            String filepath_s = "";
//                            try {
//                                filepath = FilmCreator.saveBitmap(context, baseBitmap);
//                                filepath_s = FilmCreator.saveBitmap(context, baseBitmap_s);
//                                VideoFileDao videoFileDao = new VideoFileDao(context);
//                                ContentValues contentValues = new ContentValues();
//                                contentValues.put("thumbnail", filepath);
//                                contentValues.put("thumbnail_s", filepath_s);
//                                videoFileDao.update(contentValues, "id=?", new String[]{String.valueOf(videoFile.getId())});
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                            }
//                            String tmpFilepath = null;
//
//                            if (ViewType == VIEWTYPE_SIMPLE) {
//                                if (imgView.getId() == R.id.pos_1) {
//                                    tmpFilepath = filepath;
//                                } else {
//                                    tmpFilepath = filepath_s;
//                                }
//                            } else if (ViewType == VIEWTYPE_EXPEND) {
//                                if (imgView.getId() == R.id.pos_1 || imgView.getId() == R.id.pos_2) {
//                                    tmpFilepath = filepath;
//                                } else {
//                                    tmpFilepath = filepath_s;
//                                }
//                            } else if (ViewType == VIEWTYPE_SMALL) {
//                                tmpFilepath = filepath_s;
//                            }
//
//                            final String path = tmpFilepath;
//                            //Log.v(TAG, videoFile.getFilename() + " make film ");
//                            uiThreadHanlder.post(new Runnable() {
//                                @Override
//                                public void run() {
//                                    Glide.with(context).load(path).centerCrop().placeholder(R.mipmap.ic_poster_default)
//                                            .error(R.mipmap.ic_poster_default).into(imgView);
//
//                                }
//                            });
//                        } else {
//                            //Log.v(TAG, videoFile.getFilename() + "+ make ic_poster_default ");
//                            uiThreadHanlder.post(new Runnable() {
//                                @Override
//                                public void run() {
//                                    Glide.with(context).load(R.mipmap.ic_poster_default).placeholder(R.mipmap.ic_poster_default)
//                                            .error(R.mipmap.ic_poster_default).into(imgView);
//
//                                }
//                            });
//                        }
//
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//
//                //Log.v(TAG, videoFile.getFilename() + " ----------------end------>");
//
//            }
//        };
//        return runnable;
//    }
}
