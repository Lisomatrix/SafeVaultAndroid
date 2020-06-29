package pt.lisomatrix.safevault

import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import dagger.hilt.android.AndroidEntryPoint
import pt.lisomatrix.safevault.databinding.ActivityMainBinding

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize binding
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        window.statusBarColor = ContextCompat.getColor(this, R.color.colorPrimaryLight)
    }
}