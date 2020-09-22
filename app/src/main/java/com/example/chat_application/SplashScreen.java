package com.example.chat_application;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

public class SplashScreen extends AppCompatActivity {

    private ImageView imageView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        imageView = findViewById(R.id.logo);

        Animation animation = AnimationUtils.loadAnimation(this,R.anim.transition);

        imageView.startAnimation(animation);



        final Intent intent = new Intent(SplashScreen.this,MainActivity.class);





        Thread time = new Thread()
        {
            public void run()
            {
                try
                {
                    sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                finally {


                        startActivity(intent);
                        finish();



                }
            }
        };
        time.start();
    }
}

