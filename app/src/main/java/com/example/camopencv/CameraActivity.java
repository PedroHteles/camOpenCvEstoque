package com.example.camopencv;


import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.LuminanceSource;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Reader;
import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.multi.qrcode.QRCodeMultiReader;

import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.QRCodeDetector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class CameraActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener2{
    private static final String TAG="MainActivity";

    private Mat mRgba;
    private Mat mGray;
    private CameraBridgeViewBase mOpenCvCameraView;
    private BaseLoaderCallback mLoaderCallback =new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status){
                case LoaderCallbackInterface
                        .SUCCESS:{
                    Log.i(TAG,"OpenCv Is loaded");
                    mOpenCvCameraView.enableView();
                }
                default:
                {
                    super.onManagerConnected(status);

                }
                break;
            }
        }
    };

    public CameraActivity(){
        Log.i(TAG,"Instantiated new "+this.getClass());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        int MY_PERMISSIONS_REQUEST_CAMERA=0;
        // if camera permission is not given it will ask for it on device
        if (ContextCompat.checkSelfPermission(CameraActivity.this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(CameraActivity.this, new String[] {Manifest.permission.CAMERA}, MY_PERMISSIONS_REQUEST_CAMERA);
        }

        setContentView(R.layout.activity_camera);

        mOpenCvCameraView=(CameraBridgeViewBase) findViewById(R.id.frame_Surface);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (OpenCVLoader.initDebug()){
            //if load success
            Log.d(TAG,"Opencv initialization is done");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
        else{
            //if not loaded
            Log.d(TAG,"Opencv is not loaded. try again");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0,this,mLoaderCallback);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mOpenCvCameraView !=null){
            mOpenCvCameraView.disableView();
        }
    }

    public void onDestroy(){
        super.onDestroy();
        if(mOpenCvCameraView !=null){
            mOpenCvCameraView.disableView();
        }

    }

    public void onCameraViewStarted(int width ,int height){
        mRgba=new Mat(height,width, CvType.CV_8UC4);
        mGray =new Mat(height,width,CvType.CV_8UC1);
    }
    public void onCameraViewStopped(){
        mRgba.release();
    }
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame){
        mRgba=inputFrame.rgba();
        mGray=inputFrame.gray();



        Bitmap bMap = Bitmap.createBitmap(mRgba.width(), mRgba.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mRgba,bMap);
        int[] intArray = new int[bMap.getWidth()*bMap.getHeight()];
        bMap.getPixels(intArray, 0, bMap.getWidth(), 0, 0, bMap.getWidth(), bMap.getHeight());
        LuminanceSource source = new RGBLuminanceSource(bMap.getWidth(), bMap.getHeight(),intArray);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

        List<Qr> listaProduto = new ArrayList<>();
        List<Qr> listaEndereco = new ArrayList<>();


        try {
            Map<DecodeHintType, String> hints = new HashMap<>();
            hints.put(DecodeHintType.CHARACTER_SET, "utf-8");
            Result[]  result = new QRCodeMultiReader().decodeMultiple(bitmap, hints);
            for(Result kp : result)
            {
                ResultPoint[] points = kp.getResultPoints();
                Imgproc.putText(mRgba, kp.getText(),new Point(points[1].getX(),points[1].getY()-50), Core.FONT_HERSHEY_COMPLEX, 1.0, new Scalar(0, 255, 0), 3, Imgproc.LINE_AA, false);
                Imgproc.rectangle(mRgba,new Point(points[0].getX(),points[0].getY()),new Point(points[2].getX(),points[2].getY()), new Scalar(0,0,0,0),15);


                if(kp.getText().contains("QR01")){
                    Float x = points[0].getX();
                    Float y = points[0].getY();
                    listaProduto.add(new Qr(x,y,kp.getText()));

                } else if (kp.getText().contains("QR02")){
                    Float x = points[0].getX();
                    Float y = points[0].getY();
                    listaEndereco.add(new Qr(x,y,kp.getText()));
                }

                for (int i = 0 ;  i < listaProduto.size(); i++) {
                    Double xp = listaProduto.get(i).getX();
                    Double yp = listaProduto.get(i).getY();

                    String produtoTemp = listaProduto.get(i).getResultado();
                    Double distanciaTemp = null;
                    String enderecoTemp = null;
                    Integer indexTemp = null;

                    for(int j = 0 ;  j < listaEndereco.size(); j++){
                        if (j == 0){
                            Double xe = listaEndereco.get(j).getX();
                            Double ye = listaEndereco.get(j).getY();
                            enderecoTemp = listaEndereco.get(j).getResultado();
                            distanciaTemp = Math.pow(((xe - xp)*(xe - xp) + (ye - yp )*(ye - yp )),0.5);
                            indexTemp = j;
                            Log.d(TAG, distanciaTemp.toString());

                        }else{
                            Double xe = listaEndereco.get(j).getX();
                            Double ye = listaEndereco.get(j).getY();
                            double pow = Math.pow(((xe - xp) * (xe - xp) + (ye - yp) * (ye - yp)), 0.5);
                            if(pow < distanciaTemp){
                                enderecoTemp = listaEndereco.get(j).getResultado();
                                distanciaTemp = pow;
                                indexTemp = j;
                            }
                        }
                    }
                    System.out.println(produtoTemp);
                    System.out.println(enderecoTemp);
                    System.out.println(distanciaTemp);
                    System.out.println(indexTemp);
                }
            }


        }
        catch (NotFoundException e) {
            e.printStackTrace();
        }

        return mRgba;

    }


}