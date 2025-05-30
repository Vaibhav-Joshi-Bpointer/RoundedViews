package com.bpointer.roundedviews

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bpointer.roundedviews.databinding.ActivitySplashBinding

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private var _binding: ActivitySplashBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivitySplashBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        init()
    }

    private fun init(){
        binding.roundedCornerDemoBtn.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
        binding.roundedImageViewDemoBtn.setOnClickListener {
            startActivity(Intent(this, ExampleActivity::class.java))
        }
    }
}