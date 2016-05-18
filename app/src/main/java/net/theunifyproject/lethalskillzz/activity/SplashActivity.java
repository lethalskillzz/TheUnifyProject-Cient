package net.theunifyproject.lethalskillzz.activity;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import net.theunifyproject.lethalskillzz.R;
import net.theunifyproject.lethalskillzz.app.AppConfig;
import net.theunifyproject.lethalskillzz.app.PrefManager;

public class SplashActivity extends AppCompatActivity {

    // Splash screen timer
    private static int SPLASH_TIME_OUT = 3000;
    private PrefManager pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        pref = new PrefManager(this);

        new Handler().postDelayed(new Runnable() {

            //Showing splash screen with a timer

            @Override
            public void run() {
                // This method will be executed once the timer is over

                // Checking for user session

                switch (pref.getRegStage()) {
                    case AppConfig.REG_STAGE_ZERO: {

                        if (pref.isLoggedIn()) {
                            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);

                            finish();
                        } else {

                            Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);

                            finish();
                        }
                    }
                    break;

                    case AppConfig.REG_STAGE_ONE: {

                        Intent intent = new Intent(SplashActivity.this, RegisterAccountActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);

                        finish();
                    }
                    break;

                    case AppConfig.REG_STAGE_TWO: {

                        Intent intent = new Intent(SplashActivity.this, OTPActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);

                        finish();
                    }
                    break;

                    case AppConfig.REG_STAGE_THREE: {

                        Intent intent = new Intent(SplashActivity.this, IntroDiscoverActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);

                        finish();
                    }
                    break;

                }

            }




        }, SPLASH_TIME_OUT);
    }
}
