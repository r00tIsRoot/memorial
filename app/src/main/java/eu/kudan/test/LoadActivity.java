package eu.kudan.test;


import android.content.Intent;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class LoadActivity extends AppCompatActivity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(2000);
                }catch (Throwable ex){
                    ex.printStackTrace();
                }
                Intent intent = new Intent(LoadActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        }).start();
    }
}