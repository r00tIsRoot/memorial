package eu.kudan.test;

import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import eu.kudan.kudan.ARActivity;
import eu.kudan.kudan.ARArbiTrack;
import eu.kudan.kudan.ARGyroPlaceManager;
import eu.kudan.kudan.ARImageNode;

import android.content.Intent;
import android.os.Environment;
import android.widget.ImageButton;

import java.net.MalformedURLException;
import java.util.concurrent.ExecutionException;

public class ARCameraViewActivity extends ARActivity {

    ARImageNode memImageNode[]={null, null, null, null, null};
    private ARBITRACK_STATE arbitrack_state;
    GPSInfo gps;
    String userid="";
    String[] mFiles = {null, null, null, null, null};

    public ARCameraViewActivity() throws MalformedURLException {
    }

    //Tracking enum
    enum ARBITRACK_STATE {
        ARBI_PLACEMENT,
        ARBI_TRACKING
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_arcamera_view);

        Intent intent = getIntent();
        userid = intent.getStringExtra("userid");
        Log.e("이런", "forNum"+intent.getStringExtra("forNum"));
        int tabNum = Integer.valueOf(intent.getStringExtra("tabNum"));
        if(tabNum == 0){
            mFiles = intent.getStringArrayExtra("mcFiles");
        }else if(tabNum == 1){
            mFiles = intent.getStringArrayExtra("myFiles");
        }
        else{
            Log.e("이런","탭구분에러");
        }
        Log.e("이런", "뷰파일들"+mFiles);


        gps = new GPSInfo(ARCameraViewActivity.this);
        double cLatitude =gps.getLatitude();
        double cLongitude =gps.getLongitude();
        double cAltitude =gps.getAltitude();

        arbitrack_state  = ARBITRACK_STATE.ARBI_PLACEMENT;

    }

    public void setup() {
        int changeNum = -1000;

        for(String mFileName : mFiles){
            try {
                addArImageNode(mFileName, changeNum);
                Log.e("이런", "노드추가"+mFileName+changeNum);
                changeNum += 500;
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
        try {
            setupArbiTrack();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    //낙서모델  imageNode로
    public void addArImageNode(String mFileName, int changeNum) throws ExecutionException, InterruptedException {
        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getPath();
        path += "/"+mFileName;
        Log.e("이런", "추가된이미지노드path"+path);

        ARImageNode mImageNode = new ARImageNode();
        mImageNode = mImageNode.initWithPath(path);

        mImageNode.scaleByUniform(0.5f);
        mImageNode.rotateByDegrees(90, 0, 1, 0);

        Log.e("이런", "changeNum : "+changeNum);
        memImageNode[(changeNum+1000)/500] = mImageNode;

    }

    //Sets up arbi track
    public void setupArbiTrack() throws ExecutionException, InterruptedException {

        //타겟 노드로 쓰일 이미지노드 생성
        ARImageNode targetImageNode = new ARImageNode("target.png");

        // 이미지노드의 크기와 각도등을 알맞게 조정
        targetImageNode.scaleByUniform(0.5f);
        targetImageNode.rotateByDegrees(90, 0, 1, 0);

        // gyro 센서를 이용해 가상의 기준 지점을 설정
        ARGyroPlaceManager gyroPlaceManager = ARGyroPlaceManager.getInstance();
        gyroPlaceManager.initialise();

        // gyro센서로 만든 공간에 타겟 이미지 노드를 추가
        gyroPlaceManager.getWorld().addChild(targetImageNode);

        // arbiTracker 초기화
        ARArbiTrack arbiTrack = ARArbiTrack.getInstance();
        arbiTrack.initialise();

        // arviTracker를 기준으로 타겟 이미지노드를 설정
        arbiTrack.setTargetNode(targetImageNode);

        // 만들어진 가상의 공간에 낙서노드들을 추가
        int changeNum = -1000;
        for(ARImageNode mImageNode : memImageNode) {
            arbiTrack.getWorld().addChild(mImageNode);
            mImageNode.setPosition(0,0,changeNum);
            Log.e("이런", "노드추가완료"+((changeNum+1000)/500));
            changeNum += 500;
        }
    }

    //gyro센서를 이용해 잡은 기준위치를 확정하고 가상의 ar세상을 활성화
    public void lockPosition(View view) {

        ImageButton b = (ImageButton)findViewById(R.id.lockButton);
        ARArbiTrack arbiTrack = ARArbiTrack.getInstance();

        // If in placement mode start arbi track, hide target node and alter label
        if(arbitrack_state.equals(ARCameraViewActivity.ARBITRACK_STATE.ARBI_PLACEMENT)) {

            //Start Arbi Track
            arbiTrack.start();

            //Hide target node
            arbiTrack.getTargetNode().setVisible(false);

            //Change enum and label to reflect Arbi Track state
            arbitrack_state = ARCameraViewActivity.ARBITRACK_STATE.ARBI_TRACKING;
            b.setImageResource(R.drawable.stop_ar);
            Log.e("이런", "ar표현끝");


        }

        // If tracking stop tracking, show target node and alter label
        else {

            // Stop Arbi Track
            arbiTrack.stop();

            // Display target node
            arbiTrack.getTargetNode().setVisible(true);

            //Change enum and label to reflect Arbi Track state
            arbitrack_state = ARCameraViewActivity  .ARBITRACK_STATE.ARBI_PLACEMENT;
            b.setImageResource(R.drawable.view_ar);
        }
    }


}
