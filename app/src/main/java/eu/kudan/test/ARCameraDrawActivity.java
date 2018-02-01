package eu.kudan.test;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Calendar;

import eu.kudan.kudan.ARActivity;

public class ARCameraDrawActivity extends ARActivity {


    DrawLine drawLine = null;
    RelativeLayout layoutCanvas;
    View view;
    String userid = "";
    String currentDateTime="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_arcamera_draw);

        layoutCanvas = (RelativeLayout) findViewById(R.id.layoutCanvas);

        Intent intent = getIntent();
        userid = intent.getStringExtra("userid");

        view = new DrawLine(ARCameraDrawActivity.this);


        layoutCanvas.addView(view, new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT));

    }


    public void onWindowFocusChanged(boolean hasFocus)
    {
        //hasFocus : 앱이 화면에 보여졌을때 true로 설정되어 호출됨.
        //만약 그리기 뷰 전역변수에 값이 없을경우 전역변수를 초기화 시킴.
        if(hasFocus && drawLine == null)
        {
            //그리기 뷰가 보여질(나타날) 레이아웃 찾기
            if(layoutCanvas != null) //그리기 뷰가 보여질 레이아웃이 있으면...
            {
                //그리기 뷰 초기화
                drawLine = new DrawLine(this);

                //그리기 뷰를 그리기 뷰 레이아웃에 넣기 -이렇게 하면 그리기 뷰가 화면에 보여지게 됨.
                layoutCanvas.addView(drawLine);
            }

            //상단 메뉴(RED, BLUE)버튼 설정
            //일단 초기값은 0번(WHITE)
            resetCurrentMode(0);
        }

        super.onWindowFocusChanged(hasFocus);
    }

    //코딩 하기 쉽게 하기 위해서 사용할 상단 메뉴 버튼들의 아이디를 배열에 넣는다
    private int[] btns = {R.id.btnWHITE, R.id.btnBLUE, R.id.btnGREEN, R.id.btnRED, R.id.btnBLACK};
    //코딩 하기 쉽게 하기 위해서 상단 메뉴 버튼의 배열과 똑같이 실제 색상값을 배열로 만든다.
    private int[] colors = {Color.WHITE, Color.parseColor("#02b6f6"), Color.parseColor("#01d5d5"), Color.parseColor("#ffa3c1"), Color.BLACK};

    //선택한 색상에 맞도록 버튼의 배경색과 글자색을 바꾸고, 그리기 뷰에도 전달.
    private void resetCurrentMode(int curMode)
    {
        for(int i=0;i<btns.length;i++)
        {
            //배열을 돌면서 버튼이 있는지 확인
            ImageButton btn = (ImageButton)findViewById(btns[i]);
            if(btn != null)
            {
                //버튼 있으면 배경색과 글자색 변경
                //만약 선택한 버튼값과 찾은 버튼이 동일하면 회색배경에 흰색글자 버튼으로 변경.
                //동일하지 않으면 흰색배경에 회색글자 버튼으로 변경.
//                btn.setTextColor(i==curMode?0xffffffff:0xff555555);
            }
        }

        //만약 그리기 뷰가 초기화 되었으면, 그리기 뷰에 글자색을 전달
        if(drawLine != null) drawLine.setLineColor(colors[curMode]);
    }

    //버튼을 클릭했을때 호출 되는 함수.
    //이 함수가 호출될때 어떤 버튼(뷰)에서 호출했는지를 같이 알려준다.
    //버튼 클릭시 이 함수를 호출 하게 하기 위해
    //main.xml에서
    //<Button ~~~~ android:onClick="btnClick" ~~~~ />
    //와 같이 btnClick이라는 함수명을 넣어 줌.
    public void btnClick(View view)
    {
        if(view == null) return;

        for(int i=0;i<btns.length;i++)
        {
            //배열을 돌면서 클릭한 버튼이 있는지 확인
            if(btns[i] == view.getId())
            {
                //만약 선택한 버튼이 있으면 버튼모양 및 그리기 뷰 설정을 하기 위해서 함수 호출
                resetCurrentMode(i);

                //더이상 처리를 할 필요가 없으므로 for문을 빠져 나옴
                break;
            }
        }
    }


    //캡쳐버튼클릭
    public void mOnCaptureClick(View v) throws MalformedURLException {
        Calendar cal = Calendar.getInstance();
        GPSInfo gps = new GPSInfo(ARCameraDrawActivity.this);
        //현재 날짜, 시간을 받아옴
        currentDateTime=String.format("%04d%02d%02d%02d%02d%02d", cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND));
        String writer = userid;
        String createdAt = currentDateTime;
        String latitude = "";
        String longitude = "";
        String alttitude = "";
        String coordinate = "";
        //이미 받아놓은 gps값이 없으면 위도,경도,고도로 분리하여 저장하고, coordinate라는 변수로 묶어서 다시저장
        if (gps.isGetLocation()) {
            latitude = String.format("%f", gps.getLatitude());
            longitude = String.format("%f", gps.getLongitude());
            alttitude = String.format("%f", gps.getAltitude());
            coordinate = String.format("%f,%f,%f", gps.getLatitude(), gps.getLongitude(), gps.getAltitude());
        }

        File screenShot = ScreenShot(layoutCanvas);
        if(screenShot!=null){
            //갤러리에 추가
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(screenShot)));
        }

        FileSystem fileSystem = new FileSystem();
        //파일을 서버에 올림
        fileSystem.fileUploader(writer, createdAt, latitude, longitude, alttitude, coordinate);
    }

    String filename = "";
    //화면 캡쳐하기
    public File ScreenShot(RelativeLayout relativeLayout){
        relativeLayout.setDrawingCacheEnabled(true);  //화면에 뿌릴때 캐시를 사용하게 한다

        Bitmap screenBitmap = relativeLayout.getDrawingCache();   //캐시를 비트맵으로 변환

        // 가로 0.3배, 세로 0.3배인 사이즈 Matrix
        Matrix matrix = new Matrix();
        matrix.preScale(0.3f, 0.3f);
        // 이미지 사이즈 변경
        screenBitmap = Bitmap.createBitmap(screenBitmap, 0, 0, screenBitmap.getWidth(), screenBitmap.getHeight(), matrix, false);

        filename = userid+currentDateTime+".png";
        File file = new File(Environment.getExternalStorageDirectory()+"/Pictures", filename);  //Pictures폴더 screenshot.png 파일
        FileOutputStream os = null;
        try{
            os = new FileOutputStream(file);
            screenBitmap.compress(Bitmap.CompressFormat.PNG, 90, os);   //비트맵을 PNG파일로 변환
            os.close();
        }catch (IOException e){
            e.printStackTrace();
            return null;
        }
        this.finish();
        view.setDrawingCacheEnabled(false);
        return file;
    }


}
