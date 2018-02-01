package eu.kudan.test;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class AccountFindActivity extends AppCompatActivity {
    ImageButton buttonCancle;
    ImageButton buttonFind;
    EditText idInput;
    EditText nameInput;
    EditText emailInput;
    TextView textViewID2;
    TextView textViewPW2;

    String id;
    String name;
    String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_find);
        buttonCancle = (ImageButton)findViewById(R.id.buttonCancle);
        buttonFind = (ImageButton)findViewById(R.id.buttonCreate);

        idInput = (EditText)findViewById(R.id.idInput);
        nameInput = (EditText)findViewById(R.id.nameInput);
        emailInput = (EditText)findViewById(R.id.emailInput);

        textViewID2 = (TextView)findViewById(R.id.textViewID2);
        textViewPW2 = (TextView)findViewById(R.id.textViewPW2);


    }

    public void btnClickCancle(View v){
        Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
        startActivity(intent);
    }
    public void btnClickFind(View v){
        id = idInput.getText().toString();
        name = nameInput.getText().toString();
        email = emailInput.getText().toString();

        findDB fDB = new findDB();
        fDB.execute();
    }
    public class findDB extends AsyncTask<Void, Integer, String> {

        String data = "";
        URL url;
        @Override
        protected String doInBackground(Void... unused) {

/* 인풋 파라메터값 생성 */
            String param = "id=" + id + "&name=" + name + "&email=" + email + "";
            Log.e("POST",param);
            try {
/* 서버연결 */
                String serverIp = "http://112.173.202.189:10/";//root's ip
                //String serverIp = "http://175.202.158.167:10/"; //찬씨's ip
                //URL url = new URL("https://175.202.158.167:10"); //찬씨's ip
                if(!id.equals("") && !name.equals("") && !email.equals("")){
                    url = new URL(serverIp + "findPW.php");
                }
                else if(id.equals("") && !name.equals("") && !email.equals("")){
                    url = new URL(serverIp + "findID.php");
                }
                else{
                    return "값을 입력해주세요.";
                }
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
//                String data = "";

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
        protected void onPostExecute(String str) {
            super.onPostExecute(str);
            if(str != "값을 입력해주세요."){
                if(data.equals("Can not find Account")) {
                    Log.e("RESULT","해당하는 계정을 찾지못하였습니다.");
                    Toast.makeText(getApplicationContext(), "아이디 혹은 비밀번호를 다시 확인해주세요.", Toast.LENGTH_LONG).show();
                }
                else if(!data.equals(""))
                {
                    if (!id.equals("") && !name.equals("") && !email.equals("")) {
                        textViewPW2.setText("찾은 PW : " + data);
                    } else if (id.equals("") && !name.equals("") && !email.equals("")) {
                        textViewID2.setText("찾은 ID : " + data);
                    } else {
                        Toast.makeText(getApplicationContext(), "값을 입력해주세요.", Toast.LENGTH_SHORT).show();
                    }
                }
                else
                {
                    Toast.makeText(getApplicationContext(), "알수없는 에러입니다.", Toast.LENGTH_LONG).show();
                }
            }
            else{
                Toast.makeText(getApplicationContext(), str, Toast.LENGTH_LONG).show();
            }

        }

    }

}
