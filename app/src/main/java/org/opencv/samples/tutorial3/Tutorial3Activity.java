package org.opencv.samples.tutorial3;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;

public class Tutorial3Activity extends Activity implements CvCameraViewListener2, OnTouchListener {
    private static final String TAG = "Tutorial3Activity";

    private Tutorial3View mOpenCvCameraView;
    private List<Size> mResolutionList;
    private MenuItem[] mEffectMenuItems;
    private SubMenu mColorEffectsMenu;
    private MenuItem[] mResolutionMenuItems;
    private SubMenu mResolutionMenu;
    private ImageView imageView;
    private List<Rect> objects;

    Mat currImage = null;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                    mOpenCvCameraView.setOnTouchListener(Tutorial3Activity.this);
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    public Tutorial3Activity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tutorial3_surface_view);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        initComponents();
    }

    private void initComponents() {
        mOpenCvCameraView = (Tutorial3View) findViewById(R.id.tutorial3_activity_java_surface_view);
        imageView = (ImageView) findViewById(R.id.img);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume() {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
    }

    public void onCameraViewStopped() {
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        Mat img = inputFrame.rgba();
        currImage = img.clone();
        return img;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        List<String> effects = mOpenCvCameraView.getEffectList();

        if (effects == null) {
            Log.e(TAG, "Color effects are not supported by device!");
            return true;
        }

        mColorEffectsMenu = menu.addSubMenu("Color Effect");
        mEffectMenuItems = new MenuItem[effects.size()];

        int idx = 0;
        ListIterator<String> effectItr = effects.listIterator();
        while(effectItr.hasNext()) {
            String element = effectItr.next();
            mEffectMenuItems[idx] = mColorEffectsMenu.add(1, idx, Menu.NONE, element);
            idx++;
        }

        mResolutionMenu = menu.addSubMenu("Resolution");
        mResolutionList = mOpenCvCameraView.getResolutionList();
        mResolutionMenuItems = new MenuItem[mResolutionList.size()];

        ListIterator<Size> resolutionItr = mResolutionList.listIterator();
        idx = 0;
        while(resolutionItr.hasNext()) {
            Size element = resolutionItr.next();
            mResolutionMenuItems[idx] = mResolutionMenu.add(2, idx, Menu.NONE,
                    Integer.valueOf(element.width).toString() + "x" + Integer.valueOf(element.height).toString());
            idx++;
        }

        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(TAG, "called onOptionsItemSelected; selected item: " + item);
        if (item.getGroupId() == 1)
        {
            mOpenCvCameraView.setEffect((String) item.getTitle());
            Toast.makeText(this, mOpenCvCameraView.getEffect(), Toast.LENGTH_SHORT).show();
        }
        else if (item.getGroupId() == 2)
        {
            int id = item.getItemId();
            Size resolution = mResolutionList.get(id);
            mOpenCvCameraView.setResolution(resolution);
            resolution = mOpenCvCameraView.getResolution();
            String caption = Integer.valueOf(resolution.width).toString() + "x" + Integer.valueOf(resolution.height).toString();
            Toast.makeText(this, caption, Toast.LENGTH_SHORT).show();
        }

        return true;
    }

    @SuppressLint("SimpleDateFormat")
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        this.onPause();
        mOpenCvCameraView.setVisibility(View.GONE);
        Mat img = detectBorder(currImage);
        process(img);

        //imageView.setVisibility(View.VISIBLE);
        //Mat mat = process(currImage);
        /*Bitmap bm = Bitmap.createBitmap(mat.cols(), mat.rows(),Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mat, bm);
        imageView.setImageBitmap(bm);*/
        return false;
    }

    private Mat detectBorder(Mat input) {

        Mat bin = input.clone();
        Imgproc.resize(bin, bin, new org.opencv.core.Size(640, 360));
        Imgproc.cvtColor(bin, bin, Imgproc.COLOR_RGB2GRAY);
        Imgproc.GaussianBlur(bin, bin, new org.opencv.core.Size(3, 3), 0);
        Imgproc.adaptiveThreshold(bin, bin, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 31, 2);

        int dilation_size = 2;
        Mat element1 = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new org.opencv.core.Size(2 * dilation_size + 1, 2 * dilation_size + 1));
        Imgproc.dilate(bin, bin, element1);
        int erosion_size = 3;
        Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new org.opencv.core.Size(2 * erosion_size + 1, 2 * erosion_size + 1));
        Imgproc.erode(bin, bin, element);

        Imgproc.GaussianBlur(bin, bin, new org.opencv.core.Size(3, 3), 0);

        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(bin, contours, hierarchy, Imgproc.RETR_TREE,Imgproc.CHAIN_APPROX_SIMPLE);

        Collections.sort(contours, new Comparator<MatOfPoint>() {
            @Override
            public int compare(MatOfPoint a, MatOfPoint b) {
                if (Imgproc.boundingRect(a).area() < Imgproc.boundingRect(b).area())
                    return 1;

                return -1;
            }
        });




        Rect border = Imgproc.boundingRect(contours.get(2));
        border.x = border.x*input.width()/640 + 10;
        border.y = border.y*input.height()/360;

        border.width = border.width*input.width()/640 - 10;
        border.height = border.height*input.height()/360 - 40 ;

        return new Mat(input, border);

    }

    private Mat process(Mat input) {
        Mat img = input;
        Imgproc.resize(img, img, new org.opencv.core.Size(360, 240));

        Imgproc.cvtColor(img, img, Imgproc.COLOR_BGR2GRAY);

        Imgproc.GaussianBlur(img, img, new org.opencv.core.Size(9, 9), 2, 2);
        Imgproc.adaptiveThreshold(img, img, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 21, 2);
        int dilation_size = 1;
        Mat element1 = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new org.opencv.core.Size(2 * dilation_size + 1, 2 * dilation_size + 1));
        Imgproc.dilate(img, img, element1);

        int erosion_size = 4;
        Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new org.opencv.core.Size(2 * erosion_size + 1, 2 * erosion_size + 1));
        Imgproc.erode(img, img, element);

        int n = img.height();
        int m = img.width();


        int mark[][] = new int[n][m];

        objects = new ArrayList<>();
        for (int i = 0; i < n; ++i) {
            for (int j = 0; j < m; ++j) {

                Rect rect = findBound(img, mark, i, j);
                if (rect != null && rect.area() > 600)
                    objects.add(rect);
            }
        }

        extractDigits(img);
        return img;
    }

    private void extractDigits(Mat img) {
        ArrayList<Rect> line1 = new ArrayList<>();
        ArrayList<Rect> line2 = new ArrayList<>();
        ArrayList<Rect> line3 = new ArrayList<>();
        ArrayList<ArrayList<Rect>> lines = new ArrayList<>();
        lines.add(line1);
        lines.add(line2);
        lines.add(line3);

        Rect stdObject = Collections.min(objects, new Comparator<Rect>() {
            @Override
            public int compare(Rect lhs, Rect rhs) {
                if (lhs.x < rhs.x) return -1;
                if (lhs.x > rhs.x) return 1;
                return 0;
            }
        });

        int stdX = stdObject.x;
        int width = stdObject.width;
        int len = objects.size();
        for (int i = 0; i < len; i++) {
            if (Math.abs(objects.get(i).x - stdX) / width >= 2)
                line3.add(objects.get(i));
            else if (Math.abs(objects.get(i).x - stdX) / width >= 1)
                line2.add(objects.get(i));
            else line1.add(objects.get(i));
        }

        Comparator<Rect> comparator = new Comparator<Rect>() {
            @Override
            public int compare(Rect lhs, Rect rhs) {
                if (lhs.y < rhs.y) return 1;
                if (lhs.y > rhs.y) return -1;
                return 0;
            }
        };

        Collections.sort(line1, comparator);
        Collections.sort(line2, comparator);
        Collections.sort(line3, comparator);

        objects.clear();
        objects.addAll(line1);
        objects.addAll(line2);
        objects.addAll(line3);

        String res = "";
        Imgproc.cvtColor(img, img, Imgproc.COLOR_GRAY2RGB);
        for (Rect rect:objects)
        {
            Mat tmp = new Mat(img, rect);
            String txt = getText(tmp);
            res += txt;
            Core.rectangle(img, rect.tl(), rect.br(), new Scalar(255, 0, 0), 1);
            Core.putText(img, txt,
                    new Point(rect.x + rect.width/2, rect.y + rect.height/2),
                    Core.FONT_ITALIC,
                    1,
                    new Scalar(0, 0, 255));
        }

        try {
            String txt1 = res.substring(0, line1.size());
            String txt2 = res.substring(line1.size(), line1.size() + line2.size());
            String txt3 = res.substring(line1.size() + line2.size(), line1.size() + line2.size() + line3.size());
            Log.d(TAG, txt1 + " : " + txt2 + " : " + txt3);

            Intent intent = new Intent();
            intent.putExtra("maxbp", txt1);
            intent.putExtra("minbp", txt2);
            intent.putExtra("pulse", txt3);
            setResult(RESULT_OK, intent);
            finish();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Rect findBound(Mat img, int[][] mark, int i, int j) {
        if (mark[i][j] == 1 || img.get(i, j)[0] > 127)
            return null;

        final int vi[] = {1, 0, 0, -1};
        final int vj[] = {0, 1, -1, 0};
        int n = img.height();
        int m = img.width();

        int qi[] = new int[n*m];
        int qj[] = new int[n*m];

        int maxi = 0, maxj = 0;
        int mini = n, minj = m;

        qi[0] = i;
        qj[0] = j;

        Rect bound = null;
        int front = 0, rear = 1;
        while (front < rear) {
            int _i = qi[front];
            int _j = qj[front];
            front++;
            for (int k = 0; k < 4; ++k) {
                int ii = _i+vi[k];
                int jj = _j+vj[k];

                if (0 <= ii && ii < n && 0 <= jj && jj < m) {
                    if (mark[ii][jj] == 0 && img.get(ii, jj)[0] == 0.0) {
                        mark[ii][jj] = 1;
                        if (mini > ii) mini = ii;
                        if (minj > jj) minj = jj;
                        if (maxi < ii) maxi = ii;
                        if (maxj < jj) maxj = jj;

                        rear++;
                        qi[rear] = ii;
                        qj[rear] = jj;
                    }
                }
            }
        }

        if (maxj - minj > m/6)
            bound = new Rect(minj, mini, maxj-minj, maxi-mini);

        return bound;
    }

    private String getText(Mat input) {
       int n = input.width();
       int m = input.height();
       Rect r[] = new Rect[7];

        r[0] = new Rect(new Point(0, m/6), new Point(n/5, 5*m/6));
        r[1] = new Rect(new Point(n/10, 2*m/3), new Point(4*n/10, m));
        r[2] = new Rect(new Point(6*n/10, 2*m/3), new Point(9*n/10, m));
        r[3] = new Rect(new Point(4*n/5, m/6), new Point(n, 5*m/6));
        r[4] = new Rect(new Point(6*n/10, 0), new Point(9*n/10, m/3));
        r[5] = new Rect(new Point(n/10, 0), new Point(4*n/10, m/3));
        r[6] = new Rect(new Point(2*n/5, m/6), new Point(3*n/5, 5*m/6));

       if (n / 2.5 > m)
           return "1";

       double c[] = new double[7];
       for (int k = 0; k < 7; k++) {
           Mat tmp = new Mat(input, r[k]);
           int cnt = 0;
           for (int i = 0; i < tmp.height(); ++i) {
               for (int j = 0; j < tmp.width(); ++j)
                   if (tmp.get(i, j)[0] == 0) cnt++;
           }

           c[k] = cnt / r[k].area();
       }

       final int[][] const_led = new int[][]{
               {1, 1, 1, 1, 1, 1, 0},
               {0, 0, 0, 0, 0, 0, 0},
               {1, 0, 1, 1, 0, 1, 1},
               {1, 0, 0, 1, 1, 1, 1},
               {0, 1, 0, 0, 1, 1, 1},
               {1, 1, 0, 1, 1, 0, 1},
               {1, 1, 1, 1, 1, 0, 1},
               {1, 0, 0, 0, 1, 1, 0},
               {1, 1, 1, 1, 1, 1, 1},
               {1, 1, 0, 1, 1, 1, 1},
       };

       int digit = -1;
       double max = -1;
       for (int i = 0; i < 10; ++i)
           if (i != 1) {
               double p = 1;
               for (int j = 0; j < 7; ++j)
                   if (const_led[i][j] == 1) p *= c[j];
                   else p *= (1 - c[j]);

               if (max < p) {
                   max = p;
                   digit = i;
               }
           }
       return Integer.toString(digit);
   }
}
