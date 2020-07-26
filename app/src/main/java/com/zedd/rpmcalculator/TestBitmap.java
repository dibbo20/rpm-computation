package com.zedd.rpmcalculator;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;



import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

public class TestBitmap extends AppCompatActivity {



    private FFmpeg ffmpeg;
    private static final String TAG = "zedd";
    private static final int REQUEST_TAKE_GALLERY_VIDEO = 100;
    private Uri selectedVideoUri;
    private String filePath;
    private int duration;
    private int num_of_frames;
    ProgressBar progressBar;
    Button aiobtn;
    double gg;
    Button textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_bitmap);

        Button uploadVid = findViewById(R.id.uploadVideoTv);
        final Button extractImages = findViewById(R.id.extractImagesTv);
        Button createHistograms = findViewById(R.id.CreateHistImagesTv);
        Button calculateResult = findViewById(R.id.calculateResult);
          textView = findViewById(R.id.testImView);


        loadFFMpegBinary();
        uploadVid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= 23)
                    getPermission();
                else {

                    uploadVideo();

                }
            }
        });

        extractImages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(selectedVideoUri!= null)
                {
                    String path = selectedVideoUri.getPath();
                    MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
                    mediaMetadataRetriever.setDataSource(TestBitmap.this,selectedVideoUri);
                    duration = Integer.parseInt(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
                    int duration_mili = 1000*duration;
                    extractImagesVideo(0,duration_mili);

                    //Integer.parseInt(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)

                }
                else
                {
                    Toast.makeText(TestBitmap.this, "Upload Video", Toast.LENGTH_SHORT).show();

                }
            }
        });

        createHistograms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<ArrayList<Integer>> lastlist = new ArrayList<>();
                lastlist = getListOFGHistograms();

                ArrayList<Integer> selectList = new ArrayList<>();
                selectList = arrayselection(75000,lastlist);
                Toast.makeText(TestBitmap.this, "Histograms Generated", Toast.LENGTH_SHORT).show();
                Log.d(TAG,"Array :  -->>"+ TextUtils.join(", ",selectList));

                //ArrayList<Integer> test = new ArrayList<>();
               // test = getHistogram("/sdcard/Pictures/VideoEditor/extract_picture001.jpg");
                //test = getHistogram("/sdcard/Pictures/VideoEditor3/extract_picture009.jpg");

                for (int i=0 ;i<lastlist.size();i++)
                {

                    Log.d(TAG,"Array : "+ i + "-->>"+ TextUtils.join(", ",lastlist.get(i)));
                }

                gg = getResult(selectList);

               // textView.setText(Double.toString(gg));




            }
        });

        calculateResult.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                textView.setText(Double.toString(gg));
            }
        });



       /* aiobtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                MyAsyncTasks myAsyncTasks = new MyAsyncTasks();
                myAsyncTasks.execute();


            }
        });*/




}

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_TAKE_GALLERY_VIDEO) {
                selectedVideoUri = data.getData();

            }
        }
    }

    private ArrayList< Integer > arrayselection(int threshold,ArrayList<ArrayList<Integer>>listOfHistograms){

        ArrayList< Integer > selectedArray= new ArrayList<>();

        for (int i=0;i<listOfHistograms.size();i++)
        {
            int count =0;



            for (int j=250 ;j < 256 ; j++)
            {
                if(listOfHistograms.get(i).get(j)>threshold)
                {
                    count++;
                    Log.d("array num : "+i+"","intensity : "+j+"");

                }
            }

            if(count ==1)
            {
                selectedArray.add(i);
            }
            else if(count>1){
                selectedArray.add(i);

                //Log.d("Count",Integer.toString(count));
            }


        }

        return  selectedArray;














    }

 /*   public  class MyAsyncTasks extends AsyncTask<String,String,String>{



        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);



        }


        @Override
        protected String doInBackground(String... strings) {

            String res="";

            try{
                loadFFMpegBinary();

                if (Build.VERSION.SDK_INT >= 23)
                    getPermission();
                else {

                    uploadVideo();
                }
                if(selectedVideoUri!= null)
                {
                    String path = selectedVideoUri.getPath();
                    MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
                    mediaMetadataRetriever.setDataSource(TestBitmap.this,selectedVideoUri);
                    duration = Integer.parseInt(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
                    int duration_mili = 1000*duration;
                    extractImagesVideo(0,duration_mili);

                    //Integer.parseInt(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)

                }
                else
                {
                    Toast.makeText(TestBitmap.this, "Upload Video", Toast.LENGTH_SHORT).show();

                }
                ArrayList<ArrayList<Integer>> lastlist = new ArrayList<>();
                lastlist = getListOFGHistograms();

                ArrayList<Integer> selectList = new ArrayList<>();
                selectList = arrayselection(75000,lastlist);
                Toast.makeText(TestBitmap.this, "Histograms Generated", Toast.LENGTH_SHORT).show();
                Log.d(TAG,"Array :  -->>"+ TextUtils.join(", ",selectList));

                //ArrayList<Integer> test = new ArrayList<>();
                // test = getHistogram("/sdcard/Pictures/VideoEditor/extract_picture001.jpg");
                //test = getHistogram("/sdcard/Pictures/VideoEditor3/extract_picture009.jpg");

                for (int i=0 ;i<lastlist.size();i++)
                {

                    Log.d(TAG,"Array : "+ i + "-->>"+ TextUtils.join(", ",lastlist.get(i)));
                }

                double gg = getResult(selectList);

                res = Double.toString(gg);






            } catch (Exception e) {
                e.printStackTrace();
            }
            return  res;





        }




        @Override
        protected void onPostExecute(String s) {
            progressBar.setVisibility(View.INVISIBLE);
            textView.setText(s);
        }


    }*/


    private double getResult(ArrayList<Integer> selectedArray){

        double result=0;
        if(selectedArray.size()==1)
        {

            Log.d("frames : "+num_of_frames+"",selectedArray.get(0).toString());
            result = num_of_frames/selectedArray.get(0);
            result = result / (duration / 60.00000);
        }
        else if(selectedArray.size()>1)
        {
            Log.d("ff : "+num_of_frames+"",(selectedArray.get(1)-selectedArray.get(0))+"");
            result = num_of_frames/(selectedArray.get(1)-selectedArray.get(0));
            Log.d("Duration : ", duration+"");
            double dur = (double)duration/1000.00;
            result =  result/ (dur / 60.00000);
        }


        return result;


    }

    private void getPermission() {
        String[] params = null;
        String writeExternalStorage = Manifest.permission.WRITE_EXTERNAL_STORAGE;
        String readExternalStorage = Manifest.permission.READ_EXTERNAL_STORAGE;

        int hasWriteExternalStoragePermission = ActivityCompat.checkSelfPermission(this, writeExternalStorage);
        int hasReadExternalStoragePermission = ActivityCompat.checkSelfPermission(this, readExternalStorage);
        List<String> permissions = new ArrayList<String>();

        if (hasWriteExternalStoragePermission != PackageManager.PERMISSION_GRANTED)
            permissions.add(writeExternalStorage);
        if (hasReadExternalStoragePermission != PackageManager.PERMISSION_GRANTED)
            permissions.add(readExternalStorage);

        if (!permissions.isEmpty()) {
            params = permissions.toArray(new String[permissions.size()]);
        }
        if (params != null && params.length > 0) {
            ActivityCompat.requestPermissions(TestBitmap.this,
                    params,
                    100);
        } else
            uploadVideo();
    }

    private void uploadVideo() {
        try {
            Intent intent = new Intent();
            intent.setType("video/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Video"), REQUEST_TAKE_GALLERY_VIDEO);
        } catch (Exception e) {

        }
    }


    private void loadFFMpegBinary() {
        try {
            if (ffmpeg == null) {
                Log.d(TAG, "ffmpeg : era nulo");
                ffmpeg = FFmpeg.getInstance(this);
            }
            ffmpeg.loadBinary(new LoadBinaryResponseHandler() {
                @Override
                public void onFailure() {
                    //showUnsupportedExceptionDialog();
                }

                @Override
                public void onSuccess() {
                    Log.d(TAG, "ffmpeg : correct Loaded");
                }
            });
        } catch (FFmpegNotSupportedException e) {
            //showUnsupportedExceptionDialog();
        } catch (Exception e) {
            Log.d(TAG, "EXception no controlada : " + e);
        }
    }


    private void extractImagesVideo(int startMs, int endMs) {
        File moviesDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES
        );

        String filePrefix = "extract_picture";
        String fileExtn = ".jpg";
        String yourRealPath = getPath(TestBitmap.this, selectedVideoUri);

        File dir = new File(moviesDir, "VideoEditor");
        int fileNo = 0;
        while (dir.exists()) {
            fileNo++;
            dir = new File(moviesDir, "VideoEditor" + fileNo);

        }
        dir.mkdir();
        filePath = dir.getAbsolutePath();
        File dest = new File(dir, filePrefix + "%03d" + fileExtn);


        Log.d(TAG, "startTrim: src: " + yourRealPath);
        Log.d(TAG, "startTrim: dest: " + dest.getAbsolutePath());


        String[] complexCommand = {"-y", "-i", yourRealPath, "-an", "-ss", "" + startMs / 1000, "-t", "" + (endMs - startMs) / 1000, dest.getAbsolutePath()};
        //Using FFMPEG decoder to extract frames

        execFFmpegBinary(complexCommand);

    }




    private void execFFmpegBinary(final String[] command) {


        try{


            ffmpeg.execute(command,new ExecuteBinaryResponseHandler(){

                @Override
                public void onSuccess(String message) {
                    Log.d(TAG,"Success "+message);
                }

                @Override
                public void onProgress(String message) {
                    Log.d(TAG,"On Progress "+message);
                }

                @Override
                public void onFailure(String message) {
                    Log.d(TAG,"On Failure "+message);
                }

                @Override
                public void onStart() {
                    Log.d(TAG,"Started Command "+command);
                }

                @Override
                public void onFinish() {
                    Log.d(TAG,"Finished " + command);
                    Toast.makeText(TestBitmap.this, "Images Generated", Toast.LENGTH_SHORT).show();
                }
            });



        }
        catch (FFmpegCommandAlreadyRunningException e){


        }


    }
    private String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    private boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }
    private boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }
    private boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }
    private String getDataColumn(Context context, Uri uri, String selection,
                                 String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    // ei function.... ami run kore dekhi ni, but most probably kaj kore.
    private ArrayList<Integer> getHistogram(String imgPath) {


        ArrayList<Integer> histogram = new ArrayList<>();

       // File f = new File("/storage/emulated/0/Download/extract_picture001.jpg");
        File f = new File(imgPath);
        try {
           Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f));
           // Bitmap b = BitmapFactory.decodeStream(getContentResolver().openInputStream(Uri.fromFile(new File(imgPath))));
            //Bitmap b = BitmapFactory.decodeFile(f.getAbsolutePath());
            int height = b.getHeight();
            int width = b.getWidth();
            //int[] intArray = new int[width * height];
            //b.getPixels(intArray, 0, width, 0, 0, width, height);
           // Log.d("Bitmap Contents :",Arrays.toString(intArray));
            b = toGrayScale(b);
           // Log.d("Bitmap Contentsgray :",Arrays.toString(intArray));

            boolean isGrayScale = true;
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    int pixel = b.getPixel(j, i);
                    if (!isGrayScalePixel(pixel)) {
                        isGrayScale = false;
                    }
                }
            }
           /* Log.d("error__", "asche");
            if (!isGrayScale) {
                Log.d("error__", "image is not grayscale");
            } else {
                Log.d("error__", "image is grayscale");
            }*/




            for (int i = 0; i < 256; i++)
            {
                histogram.add(0);
            }


            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    int pixel = b.getPixel(j, i);

                    int grayValue = getGrayScalePixel(pixel);
                   // Log.d("zedd","index : "+ grayValue);
                    //Log.d("zedd","index : "+ pixel);
                    histogram.set(grayValue, histogram.get(grayValue) + 1);

                    //histogram.set(pixel, histogram.get(pixel) + 1);
                }
            }

        } catch (Exception e) {
            //Log.d("error__", "exception asche");
            e.printStackTrace();
        }
        return histogram;

    }


    private ArrayList<ArrayList<Integer>> getListOFGHistograms(){


        ArrayList<ArrayList<Integer>>listHistogram = new ArrayList<>();

        File moviesDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES
        );

        String filePrefix = "extract_picture";
        String fileExtn = ".jpg";

        boolean flag = false;
        File dir = new File(moviesDir, "VideoEditor");
        int fileNo = 0;
        while (dir.exists()) {
            fileNo++;
            dir = new File(moviesDir, "VideoEditor" + fileNo);
            flag = true;

        }
        if(flag)
        {
            fileNo--;


            if(fileNo==0)
            {
                dir = new File(moviesDir, "VideoEditor");
            }
            else{
                dir = new File(moviesDir, "VideoEditor" + fileNo);
            }



        }


        filePath = dir.getAbsolutePath();

        Log.d(TAG,dir.toString());

        num_of_frames = dir.listFiles().length;

        int scaled = num_of_frames/5;

        for (int i=1;i<=scaled;i++)
        {
            String pad = String.format("%03d",i);
            String  filename = filePrefix + pad + fileExtn;
            String fullpath = dir.toString()+ "/"+filename;

            if (new File(fullpath).exists()){
                listHistogram.add(getHistogram(fullpath));

            }




        }

        return  listHistogram;










    }

    private Bitmap toGrayScale(Bitmap src){

        //Custom color matrix to convert to GrayScale
        float[] matrix = new float[]{
                0.3f, 0.59f, 0.11f, 0, 0,
                0.3f, 0.59f, 0.11f, 0, 0,
                0.3f, 0.59f, 0.11f, 0, 0,
                0, 0, 0, 1, 0,};

        Bitmap dest = Bitmap.createBitmap(
                src.getWidth(),
                src.getHeight(),
                src.getConfig());

        Canvas canvas = new Canvas(dest);
        Paint paint = new Paint();
        ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
        paint.setColorFilter(filter);
        canvas.drawBitmap(src, 0, 0, paint);

        return dest;
    }

    boolean isGrayScalePixel(int pixel){
        int alpha = (pixel & 0xFF000000) >> 24;
        int red   = (pixel & 0x00FF0000) >> 16;
        int green = (pixel & 0x0000FF00) >> 8;
        int blue  = (pixel & 0x000000FF);
        //Log.d("error__", new Integer(red).toString() + " " + new Integer(green).toString() + " " + new Integer(blue).toString());
        red   = Color.red(pixel);
        blue   = Color.blue(pixel);
        green   = Color.green(pixel);
        //Log.d("error__",new Integer(red).toString() + " " + new Integer(green).toString() + " " + new Integer(blue).toString());

        if( red == green && green == blue ) return true;
        else return false;

    }
    int getGrayScalePixel(int pixel){
        int alpha = (pixel & 0xFF000000) >> 24;
        int red   = (pixel & 0x00FF0000) >> 16;
        int green = (pixel & 0x0000FF00) >> 8;
        int blue  = (pixel & 0x000000FF);

        return red;

    }

}





