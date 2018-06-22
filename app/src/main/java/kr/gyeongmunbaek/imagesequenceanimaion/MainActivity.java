package kr.gyeongmunbaek.imagesequenceanimaion;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import kr.gyeongmunbaek.imagesequenceanimaion.view.ImageSequenceAnimationView;
import kr.gyeongmunbaek.imagesequenceanimaion.view.image.SampleBitmapCreator;

public class MainActivity extends AppCompatActivity {
    private ImageSequenceAnimationView mAnimationView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAnimationView = findViewById(R.id.image_sequence_animation_view);

        try {
            mAnimationView.setAnimationClazz(new SampleBitmapCreator()).setCacheCount(10).setDelay(10);
        } catch (NullPointerException e) {
            mAnimationView.destroy();
            mAnimationView = null;
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mAnimationView != null) {
            mAnimationView.startAnimation();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mAnimationView != null) {
            mAnimationView.destroy();
        }
    }
}
