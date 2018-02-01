package eu.kudan.test;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class CreateAccountActivity extends AppCompatActivity {
    ImageButton buttonCancle;
    ImageButton buttonCreate;
    EditText idInput;
    EditText pwInput;
    EditText pwCheckInput;
    EditText nameInput;
    EditText emailInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);
        idInput = (EditText)findViewById(R.id.idInput);
        pwInput = (EditText)findViewById(R.id.pwInput);
        pwCheckInput = (EditText)findViewById(R.id.pwCheckInput);
        nameInput = (EditText)findViewById(R.id.nameInput);
        emailInput = (EditText)findViewById(R.id.emailInput);
        buttonCancle = (ImageButton)findViewById(R.id.buttonCancle);
        buttonCreate = (ImageButton)findViewById(R.id.buttonCreate);
    }

    public void btnClickCancle(View v){
        this.finish();
    }
    public void btnClickCreate(View v){
        String id = idInput.getText().toString();
        String pw = pwInput.getText().toString();
        String pwCheck = pwCheckInput.getText().toString();
        String name = nameInput.getText().toString();
        String email = emailInput.getText().toString();

        if(!id.equals("") && pw.equals(pwCheck) && !pw.equals("") && !name.equals("") && !email.equals("")){
            InsertData task = new InsertData();
            task.execute(id,pw,name,email);

            idInput.setText("");
            pwInput.setText("");
            pwCheckInput.setText("");
            nameInput.setText("");
            emailInput.setText("");


            this.finish();
        }
        else{
            Toast.makeText(getApplicationContext(), "빈칸이나 비밀번호를 확인해주세요", Toast.LENGTH_SHORT).show();
        }
    }


    class InsertData extends AsyncTask<String, Void, String> {
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = ProgressDialog.show(CreateAccountActivity.this,
                    "Please Wait", null, true, true);
        }


        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            progressDialog.dismiss();
        }


        @Override
        protected String doInBackground(String... params) {


            String id = (String)params[0];
            String pw = (String)params[1];
            String name = (String)params[2];
            String email = (String)params[3];

            String serverURL = "http://112.173.202.189:10/insert.php";    //root's 자취방 고정ip
            //String serverURL = "http://175.202.158.167:10/insert.php";  //찬씨's 자취방
            String postParameters = "id=" + id + "&pw=" + pw + "&name=" + name + "&email=" + email;


            try {

                URL url = new URL(serverURL);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();


                httpURLConnection.setReadTimeout(5000);
                httpURLConnection.setConnectTimeout(5000);
                httpURLConnection.setRequestMethod("POST");
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


                return sb.toString();


            } catch (Exception e) {
                return new String("Error: " + e.getMessage());
            }

        }
    }

}