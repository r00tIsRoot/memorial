package eu.kudan.test;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


public class LoginActivity extends AppCompatActivity {
    ImageButton buttonCreate;
    ImageButton buttonFind;
    ImageButton buttonLogin;

    EditText idText;
    EditText pwText;

    String id;
    String pw;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        buttonCreate = (ImageButton)findViewById(R.id.buttonCreate);
        buttonFind = (ImageButton)findViewById(R.id.buttonFind);
        buttonLogin = (ImageButton)findViewById(R.id.buttonLogin);
        idText = (EditText)findViewById(R.id.editTextID);
        pwText = (EditText)findViewById(R.id.editTextPW);
    }

    public void btnClickNew(View v){
        Intent intent = new Intent(LoginActivity.this,CreateAccountActivity.class);
        startActivity(intent);
    }
    public void btnClickFind(View v){
        Intent intent = new Intent(LoginActivity.this,AccountFindActivity.class);
        startActivity(intent);
    }
    public void btnClickLogin(View v){
        id = idText.getText().toString();
        pw = pwText.getText().toString();

        if(id=="master" && pw=="0000")
        {
            Toast.makeText(getApplicationContext(), "환영합니다.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
        else {
            loginDB lDB = new loginDB();
            lDB.execute();
        }
    }

    public class loginDB extends AsyncTask<Void, Integer, Void> {

        String data = "";

        @Override
        protected Void doInBackground(Void... unused) {

/* 인풋 파라메터값 생성 */
            String param = "id=" + id + "&pw=" + pw + "";
            Log.e("POST",param);
            try {
/* 서버연결 */
                String serverIp = "http://112.173.202.189:10/"; //root's ip
                //String serverIP = "http://175.202.158.167:10/"; //찬씨's ip
                URL url = new URL(serverIp + "login.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.connect();

/* 안드로이드 -> 서버 파라메터값 전달 */
                OutputStream outs = conn.getOutputStream();
                outs.write(param.getBytes("UTF-8"));
                outs.flush();
                outs.close();

/* 서버 -> 안드로이드 파라메터값 전달 */
                InputStream is = null;
                BufferedReader in = null;

                is = conn.getInputStream();
                in = new BufferedReader(new InputStreamReader(is), 8 * 1024);
                String line = null;
                StringBuffer buff = new StringBuffer();
                while ( ( line = in.readLine() ) != null )
                {
                    buff.append(line + "\n");
                }
                data = buff.toString().trim();

/* 서버에서 응답 */
                Log.e("RECV DATA",data);

                if(data.equals("0"))
                {
                    Log.e("RESULT","성공적으로 처리되었습니다!");
                }
                else
                {
                    Log.e("RESULT","에러 발생! ERRCODE = " + data);
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if(data.equals("1"))
            {
                Toast.makeText(getApplicationContext(), id+"님 환영합니다.", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                intent.putExtra("userid",id);
                startActivity(intent);
                finish();
            }
            else if(data.equals("0"))
            {
                Log.e("RESULT","비밀번호가 일치하지 않습니다.");
                Toast.makeText(getApplicationContext(), "아이디 혹은 비밀번호를 다시 확인해주세요.", Toast.LENGTH_LONG).show();
            }
            else
            {
                Toast.makeText(getApplicationContext(), "다시 로그인해주세요.", Toast.LENGTH_LONG).show();
            }
        }

    }
}
