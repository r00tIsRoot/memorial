package eu.kudan.test;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class ListActivity extends AppCompatActivity {
    TabHost tabs;
    ListView listRecent;
    ListView listUser;
    String userid;
    //선택되어있는 탭을 구분할 변수
    String tabNum="0";

    //파일이름 배열들 초기화
    String mFiles[] = {null, null, null, null, null};
    String mcFiles[] = {null, null, null, null, null};
    String myFiles[] = {null, null, null, null, null};

    GPSInfo gps;
    FileSystem fileSystem = new FileSystem();

    //파일정보 배열들 초기화
    String mcFileWriter[] = {null, null, null, null, null};
    String mcFileCreatedAt[] = {null, null, null, null, null};
    String myFileWriter[] = {null, null, null, null, null};
    String myFileCreatedAt[] = {null, null, null, null, null};

    public ListActivity() throws MalformedURLException {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        Intent intent = getIntent();
        //LoginActivity로 부터 받은 userid값 설정
        userid = intent.getStringExtra("userid");

        //gps값 설정
        gps = new GPSInfo(ListActivity.this);
        double cLatitude =gps.getLatitude();
        double cLongitude =gps.getLongitude();
        double cAltitude =gps.getAltitude();

        String mcFileInfoArr[] = new String[100];
        String myFileInfoArr[] = new String[100];
        //근처에 있는 모든 유저의 최근파일 찾아서 다운
        synchronized (mcFileInfoArr){
            try {
                mcFileInfoArr = fileSystem.mostCurrentFileFind(String.valueOf(cLatitude), String.valueOf(cLongitude), String.valueOf(cAltitude)).split("<br>");
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            for(int arrIndex=0; arrIndex<mcFileInfoArr.length; arrIndex++){
                if(mcFileInfoArr[arrIndex] != null) {
                    mcFileWriter[arrIndex] = mcFileInfoArr[arrIndex].split("/")[0];
                    mcFileCreatedAt[arrIndex] = mcFileInfoArr[arrIndex].split("/")[1];
                    mcFiles[arrIndex] = mcFileWriter[arrIndex] + mcFileCreatedAt[arrIndex] + ".png";
                    Log.e("이런", "파일목록" + mcFiles[arrIndex]);
                }
                else if(mcFileInfoArr[arrIndex] == null){
                    break;
                }
            }
            for(String mcFileName : mcFiles){
                if(mcFileName!=null) {
                    fileSystem.fileDownloader(mcFileName);
                    Log.e("이런", "다운근처파일이름" + mcFileName);
                }
            }  //최근파일들 다운로드
        }
        //근처에 있는 내최근파일 찾아서 다운
        synchronized (myFileInfoArr){
            try {
                myFileInfoArr = fileSystem.myFileFind(userid, String.valueOf(cLatitude), String.valueOf(cLongitude), String.valueOf(cAltitude)).split("<br>");
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


            for(int arrIndex=0; arrIndex<myFileInfoArr.length; arrIndex++){
                if(myFileInfoArr[arrIndex]!=null) {
                    Log.e("이런", "파일배열" + myFileInfoArr[arrIndex]);
                    myFileWriter[arrIndex] = myFileInfoArr[arrIndex].split("/")[0];
                    myFileCreatedAt[arrIndex] = myFileInfoArr[arrIndex].split("/")[1];
                    myFiles[arrIndex] = myFileWriter[arrIndex] + myFileCreatedAt[arrIndex] + ".png";
                    Log.e("이런", "파일배열" + myFiles[arrIndex]);
                }
                else if(myFileInfoArr[arrIndex] == null){
                    break;
                }
            }
            for(String myFileName : myFiles){
                if(myFileName!=null) {
                    fileSystem.fileDownloader(myFileName);
                    Log.e("이런", "다운내파일이름" + myFileName);
                }
            }  //내파일들 다운로드

        }


        tabs = (TabHost)findViewById(R.id.tabHost);
        tabs.setup();

        TabHost.TabSpec spec1 = tabs.newTabSpec("Recent");
        spec1.setIndicator("Recent");
        spec1.setContent(R.id.layout1);

        TabHost.TabSpec spec2 = tabs.newTabSpec("User");
        spec2.setIndicator("User");
        spec2.setContent(R.id.layout2);


        tabs.addTab(spec1);
        tabs.addTab(spec2);

        listRecent = (ListView)findViewById(R.id.listRecent);
        listUser = (ListView)findViewById(R.id.listUser);


        ArrayList<String> items_user = new ArrayList<>();
        ArrayList<String> items_user2 = new ArrayList<>();

        for(int writerIndex=0; writerIndex<mcFileWriter.length; writerIndex++){
            items_user.add(mcFileWriter[writerIndex]);
            Log.e("이런", "mcFileWriter["+writerIndex+"]"+mcFileWriter[writerIndex]);
        }
        for(int writerIndex=0; writerIndex<myFileWriter.length; writerIndex++){
            items_user2.add(myFileWriter[writerIndex]);
            Log.e("이런", "myFileWriter["+writerIndex+"]"+myFileWriter[writerIndex]);
        }

        ArrayList<String> items_createdAt = new ArrayList<>();
        ArrayList<String> items_createdAt2 = new ArrayList<>();

        //낙서 시간을 년/월/일 시:분:초로 바꿈
        for(int createdAtIndex=0; createdAtIndex<mcFileCreatedAt.length; createdAtIndex++){
            if(mcFileCreatedAt[createdAtIndex] !=null) {
                String tmpDate = mcFileCreatedAt[createdAtIndex].substring(0, 4) + "/" + mcFileCreatedAt[createdAtIndex].substring(4, 6) + "/" + mcFileCreatedAt[createdAtIndex].substring(6, 8) + " " + mcFileCreatedAt[createdAtIndex].substring(8, 10) + ":" + mcFileCreatedAt[createdAtIndex].substring(10, 12) + ":" + mcFileCreatedAt[createdAtIndex].substring(12, 14);
                items_createdAt.add(tmpDate);
            }else{
                break;
            }
        }
        for(int createdAtIndex=0; createdAtIndex<myFileCreatedAt.length; createdAtIndex++){
            if(myFileCreatedAt[createdAtIndex]!=null) {
                Log.e("이런", "createdAtIndex: "+createdAtIndex);
                String tmpDate =myFileCreatedAt[createdAtIndex].substring(0, 4) + "/" + myFileCreatedAt[createdAtIndex].substring(4, 6) + "/" + myFileCreatedAt[createdAtIndex].substring(6, 8) + " " + myFileCreatedAt[createdAtIndex].substring(8, 10) + ":" + myFileCreatedAt[createdAtIndex].substring(10, 12) + ":" + myFileCreatedAt[createdAtIndex].substring(12, 14);
                items_createdAt2.add(tmpDate);
            }else{
                break;
            }
        }

        //리스트들의 내용을 채움
        mFiles = mcFiles;
        CustomAdapter adapter1 = new CustomAdapter(this, 0, items_user, items_createdAt);
        mFiles = myFiles;
        CustomAdapter adapter2 = new CustomAdapter(this, 0, items_user2, items_createdAt2);
        listRecent.setAdapter(adapter1);
        listUser.setAdapter(adapter2);

        //현재 선택되어 있는 탭을 구분
        tabs.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String s) {
                switch (s) {
                    case "Recent":
                        tabNum = "0";
                        break;
                    case "User":
                        tabNum = "1";
                        break;

                }

            }
        });
    }


    public void btnArStart(View view){
        Intent intent = new Intent(ListActivity.this, ARCameraViewActivity.class);
        //현재보고있는 탭과 불러온 파일목록들을 ar view activity로 전달
        intent.putExtra("userid",userid);
        intent.putExtra("mcFiles",mcFiles);
        intent.putExtra("myFiles",myFiles);
        intent.putExtra("tabNum",String.valueOf(tabNum));

        startActivity(intent);
    }


    private class CustomAdapter extends ArrayAdapter<String> {
        private ArrayList<String> items_user;
        private ArrayList<String> items_createdAt;

        public CustomAdapter(Context context, int textViewResourceId, ArrayList<String> objects1, ArrayList<String> objects2) {
            super(context, textViewResourceId, objects1);
            this.items_user = objects1;
            this.items_createdAt = objects2;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.layout_list, null);
            }

            // ImageView 초기화
            ImageView imageView = (ImageView)v.findViewById(R.id.imageView);

            // 리스트뷰의 아이템에 이미지를 변경한다.
            Log.e("이런", "getView mcFiles : " + mcFiles[position] + "items_user.get(position) : " + items_user.get(position));
            if(mcFileWriter[position] != null){
                if(mcFileWriter[position].equals(items_user.get(position))) {
                    String filePath = "storage/emulated/0/Pictures/" + mcFiles[position];
                    Bitmap bmImg = BitmapFactory.decodeFile(filePath);
                    imageView.setImageBitmap(bmImg);
                }
            }

            if(myFileWriter[position] != null) {
                if(myFileWriter[position].equals(items_user.get(position))) {
                    String filePath = "storage/emulated/0/Pictures/" + myFiles[position];
                    Bitmap bmImg = BitmapFactory.decodeFile(filePath);
                    imageView.setImageBitmap(bmImg);
                }
            }

            TextView textView1 = (TextView)v.findViewById(R.id.textView1);
            TextView textView2 = (TextView)v.findViewById(R.id.textView2);

            if(mFiles[position]!=null){
                textView1.setText(items_user.get(position));
                textView2.setText(items_createdAt.get(position));
            }

            return v;
        }
    }

}
