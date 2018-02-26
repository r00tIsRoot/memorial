package eu.kudan.test;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutionException;

public class FileSystem {
    String serverIP = "http://112.149.131.249:80/"; //root's ip
    //String serverIP = "http://175.202.158.167:10/"; //찬씨's ip
    String inServerfilePath = "C:\\wamp64\\www\\uploads/";
    String mostCurrentFileName="";
    String myFileName="";

    final String inClientFilePath = "storage/emulated/0/Pictures/";//클라이언트에 파일을 저장할 경로

    public FileSystem() throws MalformedURLException {
    }

    //인자로 쓸  파일이름, 낙서저장경로(서버)를 만들고, 작성자, 파일저장경로(클라이언트), 좌표값들을 함께 서브스레드로 보내 파일 업로드를 시작
    public void fileUploader(String writer, String createdAt, String latitude, String longitude, String alttitude, String coordinate){
        String fileName = writer + createdAt+".png";
        Log.e("이런", "업로더파일네임2"+fileName);
        String memoryUri = serverIP+"uploads/"+fileName;
        FileUploader uploader = new FileUploader();
        uploader.execute(inClientFilePath, fileName, memoryUri, writer, createdAt, latitude, longitude, alttitude, coordinate);
    }



    Bitmap mSaveBm = null;
    public void fileDownloader(String memoryName){
        BitmapFactory.Options bmOptions;
        bmOptions = new BitmapFactory.Options();
        bmOptions.inSampleSize = 1;
        String myFileUrl = serverIP+"uploads/" + memoryName;
        OpenHttpConnection opHttpCon = new OpenHttpConnection();
        opHttpCon.execute(myFileUrl, memoryName);
        Log.e("이런", "execute"+myFileUrl);
    }
    public String mostCurrentFileFind(String latitude, String longitude, String altitude) throws ExecutionException, InterruptedException {
        MostCurrentFileFinder mostCurrentFileFinder = new MostCurrentFileFinder();
        synchronized (mostCurrentFileName) {
            return mostCurrentFileFinder.execute(latitude, longitude, altitude).get();
        }
    }
    public String myFileFind(String userId, String latitude, String longitude, String altitude) throws ExecutionException, InterruptedException {
        MyFileFinder myFileFinder = new MyFileFinder();
        synchronized (myFileName) {
            return myFileFinder.execute(userId, latitude, longitude, altitude).get();
        }
    }

    //서브쓰레드에서 실행되는 파일업로더
    class FileUploader extends AsyncTask<String, Void, String> {
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }


        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

        }


        @Override
        protected String doInBackground(String... params) {
            int serverResponseCode = 0;
            String fileUri = (String)params[0];
            Log.e("이런", "업로더파일네임"+fileUri);
            String fileName = (String)params[1];
            Log.e("이런", "업로더파일네임"+fileName);
            String memoryUri = (String)params[2];
            String writer = (String)params[3];
            String createdAt = (String)params[4];
            String latitude = (String)params[5];
            String longitude = (String)params[6];
            String altitude = (String)params[7];
            String coordinate = (String)params[8];


            String uploadServerUri = serverIP + "uploadToServer.php";
            String uploadDbUri = serverIP + "uploadToDb.php";
            String postParameters = "memoryName="+fileName+"&memoryUri="+memoryUri+"&writer="+writer+"&createdAt="+createdAt+"&latitude="+latitude+"&longitude="+longitude+"&altitude="+altitude+"&coordinate="+coordinate;

            HttpURLConnection conn = null;
            DataOutputStream dos = null;
            String lineEnd = "\r\n";
            String twoHyphens = "--";
            String boundary = "*****";
            int bytesRead, bytesAvailable, bufferSize;
            byte[] buffer;
            int maxBufferSize = 1 * 1024 * 1024;
            File sourceFile = new File(fileUri+""+fileName);

            if (!sourceFile.isFile()) {

                Log.e("uploadFile", "Source File not exist :"
                        +fileUri);


                return "File not exist";
            }
            else{
                try {
                    //file upload시작
                    // URL과의 연결 열기(http://taetanee.tistory.com/entry/%EC%95%88%EB%93%9C%EB%A1%9C%EC%9D%B4%EB%93%9C-php-%ED%8C%8C%EC%9D%BC-%EC%A0%84%EC%86%A1-%EC%98%88%EC%A0%9C 참고)
                    FileInputStream fileInputStream = new FileInputStream(sourceFile);
                    URL url = new URL(uploadServerUri);

                    // URL의 HTTP연결 열기
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setDoInput(true); // Input 허용
                    conn.setDoOutput(true); // Output 허용
                    conn.setUseCaches(false); // Cached Copy 사용 불가
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Connection", "Keep-Alive");
                    conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                    conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                    conn.setRequestProperty("uploaded_file", fileName);

                    dos = new DataOutputStream(conn.getOutputStream());

                    dos.writeBytes(twoHyphens + boundary + lineEnd);
                    dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""
                            + fileName + "\"" + lineEnd);

                    dos.writeBytes(lineEnd);

                    // 가장큰 크기의 버퍼를 생성
                    bytesAvailable = fileInputStream.available();

                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    buffer = new byte[bufferSize];

                    // 파일을 읽고 양식에 맞게 다시씀
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                    while (bytesRead > 0) {

                        dos.write(buffer, 0, bufferSize);
                        bytesAvailable = fileInputStream.available();
                        bufferSize = Math.min(bytesAvailable, maxBufferSize);
                        bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                    }

                    // send multipart form data necesssary after file data...
                    dos.writeBytes(lineEnd);
                    dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                    // Responses from the server (code and message)
                    serverResponseCode = conn.getResponseCode();
                    String serverResponseMessage = conn.getResponseMessage();

                    Log.i("uploadFile", "HTTP Response is : "
                            + serverResponseMessage + ": " + serverResponseCode);


                    //close the streams //
                    fileInputStream.close();
                    dos.flush();
                    dos.close();
                    //file upload 끝

                    //memoryDB에 추가
                    URL DBurl = new URL(uploadDbUri);
                    HttpURLConnection httpURLConnection = (HttpURLConnection) DBurl.openConnection();


                    httpURLConnection.setReadTimeout(5000);
                    httpURLConnection.setConnectTimeout(5000);
                    httpURLConnection.setRequestMethod("POST");
                    //httpURLConnection.setRequestProperty("content-type", "application/json");
                    httpURLConnection.setDoInput(true);
                    httpURLConnection.connect();


                    OutputStream outputStream = httpURLConnection.getOutputStream();
                    outputStream.write(postParameters.getBytes("UTF-8"));
                    outputStream.flush();
                    outputStream.close();


                    int responseStatusCode = httpURLConnection.getResponseCode();

                    InputStream inputStream;
                    if(responseStatusCode == HttpURLConnection.HTTP_OK) {
                        inputStream = httpURLConnection.getInputStream();
                    }
                    else{
                        inputStream = httpURLConnection.getErrorStream();
                    }


                    InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                    StringBuilder sb = new StringBuilder();
                    String line = null;

                    while((line = bufferedReader.readLine()) != null){
                        sb.append(line);
                    }


                    bufferedReader.close();

                } catch (MalformedURLException ex) {

                    ex.printStackTrace();

                    Log.e("Upload file to server", "error: " + ex.getMessage(), ex);
                } catch (Exception e) {

                    e.printStackTrace();

                    Log.e("Upload to server Except", "Exception : " + e.getMessage(), e);
                }
                return String.valueOf(serverResponseCode);

            } // End else block

        }
    }

    private class FileDownloader extends AsyncTask<String, Integer,Bitmap>{
        Bitmap bmImg;
        String fileName;



        @Override
        protected Bitmap doInBackground(String... parms) {
            fileName = parms[0];


            // TODO Auto-generated method stub
            try{
                URL myFileUrl = new URL(serverIP+"uploads/" + fileName);
                Log.e("이런", "myFileUri :"+myFileUrl);
                HttpURLConnection conn = (HttpURLConnection)myFileUrl.openConnection();
                conn.setDoInput(true);
                conn.connect();

                InputStream is = conn.getInputStream();

                bmImg = BitmapFactory.decodeStream(is);


            }catch(IOException e){
                e.printStackTrace();
            }
            return bmImg;
        }

        //background에서 받아온 bitmap 배열을 파일로 저장함
        protected void onPostExecute(Bitmap img){
            File file = new File(inClientFilePath, fileName);  //Pictures폴더 screenshot.png 파일
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            FileOutputStream os = null;
            try{
                os = new FileOutputStream(file);
                bmImg.compress(Bitmap.CompressFormat.PNG, 100, os);   //비트맵을 PNG파일로 변환
                os.flush();
                os.close();
            }catch (IOException e){
                e.printStackTrace();
            }
        }



    }
    private class OpenHttpConnection extends AsyncTask<Object, Void, Bitmap> {


        @Override
        protected Bitmap doInBackground(Object... params) {
            Bitmap mBitmap = null;
            String url = (String) params[0];
            Log.e("이런", (String)params[0]);
            InputStream in = null;
            try {
                in = new java.net.URL(url).openStream();
                mBitmap = BitmapFactory.decodeStream(in);
                in.close();

            } catch (Exception ex) {
                ex.printStackTrace();
            }

            OutputStream outStream = null;
            String extStorageDirectory = inClientFilePath;
            String memoryName = (String)params[1];

            File file = new File(extStorageDirectory, memoryName);
            try {
                outStream = new FileOutputStream(file);
                mBitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream);
                outStream.flush();
                outStream.close();

                Log.e("이런", "saved"+extStorageDirectory);
                Log.e("이런", "saved");

            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Log.e("이런", e.toString());
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("이런", e.toString());
            }

            return mBitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bm) {
            super.onPostExecute(bm);
            mSaveBm = bm;
        }
    }

    //모든 유저들의 근처에 있는 낙서들을 찾음
    private class MostCurrentFileFinder extends AsyncTask<String, Integer,String>{

        @Override
        protected String doInBackground(String... parms) {
            String mResults = "";
            String latitude = parms[0];
            String longitude = parms[1];
            String altitude = parms[2];

            String postParameters = "latitude=" + latitude + "&longitude=" + longitude + "&altitude=" + altitude;


            // TODO Auto-generated method stub
            try{
                URL mcFileFinderUrl = new URL(serverIP+"mostCurrentFileFinder.php");
                HttpURLConnection conn = (HttpURLConnection)mcFileFinderUrl.openConnection();

                conn.setReadTimeout(5000);
                conn.setConnectTimeout(5000);
                conn.setRequestMethod("POST");
                //httpURLConnection.setRequestProperty("content-type", "application/json");
                conn.setDoInput(true);
                conn.connect();


                OutputStream outputStream = conn.getOutputStream();
                outputStream.write(postParameters.getBytes("UTF-8"));
                outputStream.flush();
                outputStream.close();

                InputStream is = conn.getInputStream();
                BufferedReader in = null;

                in = new BufferedReader(new InputStreamReader(is), 8 * 1024);
                String line = null;
                StringBuffer buff = new StringBuffer();
                while ( ( line = in.readLine() ) != null )
                {
                    buff.append(line + "\n");
                }

                mResults = buff.toString().trim();


            }catch(IOException e){
                e.printStackTrace();
            }
            Log.e("이런", "최근파일"+mResults);
            return mResults;
        }

        protected void onPostExecute(String mcFileName){
            mostCurrentFileName=mcFileName;
            Log.e("이런", "onpost"+mostCurrentFileName);

        }

    }

    //근처에 있는 내 낙서들을 찾음
    private class MyFileFinder extends AsyncTask<String, Integer,String>{
        @Override
        protected String doInBackground(String... parms) {
            String mResults = "";
            String userid = parms[0];
            String latitude = parms[1];
            String longitude = parms[2];
            String altitude = parms[3];

            String postParameters = "writer=" + userid + "&latitude=" + latitude + "&longitude=" + longitude + "&altitude=" + altitude;


            // TODO Auto-generated method stub
            try{
                URL mcFileFinderUrl = new URL(serverIP+"myFileFinder.php");
                HttpURLConnection conn = (HttpURLConnection)mcFileFinderUrl.openConnection();

                conn.setReadTimeout(5000);
                conn.setConnectTimeout(5000);
                conn.setRequestMethod("POST");
                //httpURLConnection.setRequestProperty("content-type", "application/json");
                conn.setDoInput(true);
                conn.connect();


                OutputStream outputStream = conn.getOutputStream();
                outputStream.write(postParameters.getBytes("UTF-8"));
                outputStream.flush();
                outputStream.close();

                InputStream is = conn.getInputStream();
                BufferedReader in = null;

                in = new BufferedReader(new InputStreamReader(is), 8 * 1024);
                String line = null;
                StringBuffer buff = new StringBuffer();
                while ( ( line = in.readLine() ) != null )
                {
                    buff.append(line + "\n");
                }

                mResults = buff.toString().trim();


            }catch(IOException e){
                e.printStackTrace();
            }
            Log.e("이런", "내파일"+mResults);
            return mResults;
        }

        protected void onPostExecute(String mcFileName){
            mostCurrentFileName=mcFileName;
            Log.e("이런", "onpost"+mostCurrentFileName);

        }

    }

}