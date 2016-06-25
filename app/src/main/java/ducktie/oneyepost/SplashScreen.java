
package ducktie.oneyepost;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

import ducktie.oneyepost.R;

public class SplashScreen extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // remove title bar.


        setContentView(R.layout.activity_splash_screen);

        // our timer task.
        TimerTask task = new TimerTask() {

            @Override
            public void run() {

                // we have to run it on UI thread so we won't get view error
                SplashScreen.this.runOnUiThread(new Runnable() {
                    public void run() {
                        try {
                            // get the splash image
                            ImageView splashImage = (ImageView) SplashScreen.this
                                    .findViewById(R.id.imageView1);

                            TextView titleEngTextView = (TextView) SplashScreen.this.findViewById(R.id.titleEngTextView);
                            TextView titleKzTextView = (TextView) SplashScreen.this.findViewById(R.id.titleKzTextView);
                            // make the splash image invisible
                            splashImage.setVisibility(View.GONE);
                            titleEngTextView.setVisibility(View.GONE);
                            titleKzTextView.setVisibility(View.GONE);
                            // specify animation
                            Animation animFadeOut = AnimationUtils.loadAnimation(SplashScreen.this,
                                    R.anim.splash_screen_fadeout);

                            // apply the animattion
                            splashImage.startAnimation(animFadeOut);
                            titleEngTextView.startAnimation(animFadeOut);
                            titleKzTextView.startAnimation(animFadeOut);
                        }
                        finally{
                            Intent intent = new Intent(SplashScreen.this, MainActivity.class);
                            startActivity(intent);
                        }
                    }
                });

            }

        };

        // Schedule a task for single execution after a specified delay.
        // Show splash screen for 4 seconds
        new Timer().schedule(task, 4000);

    }

}