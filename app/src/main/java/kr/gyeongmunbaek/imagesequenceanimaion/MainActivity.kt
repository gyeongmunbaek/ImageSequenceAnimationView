package kr.gyeongmunbaek.imagesequenceanimaion

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

import kr.gyeongmunbaek.imagesequenceanimaion.view.ImageSequenceAnimationView
import kr.gyeongmunbaek.imagesequenceanimaion.view.image.SampleBitmapCreator

class MainActivity : AppCompatActivity() {
    private var animationView: ImageSequenceAnimationView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        animationView = findViewById(R.id.image_sequence_animation_view)

        try {
            animationView!!.setAnimationClazz(SampleBitmapCreator()).setCacheCount(10).setDelay(10)
        } catch (e: NullPointerException) {
            animationView!!.destroy()
            animationView = null
        }

    }

    override fun onResume() {
        super.onResume()
        if (animationView != null) {
            animationView!!.startAnimation()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (animationView != null) {
            animationView!!.destroy()
        }
    }
}
