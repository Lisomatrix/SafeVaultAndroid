package pt.lisomatrix.safevault.ui.home

import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import dagger.hilt.android.AndroidEntryPoint
import pt.lisomatrix.safevault.R
import pt.lisomatrix.safevault.databinding.ActivityHomeBinding

@AndroidEntryPoint
class HomeActivity : AppCompatActivity() {

    //private val viewModel: HomeViewModel by viewModels()
    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize binding
        binding = DataBindingUtil.setContentView(this, R.layout.activity_home)

        // Initialize navigation
        setupViews()

        window.statusBarColor = ContextCompat.getColor(this, R.color.colorAccent)
        window.statusBarColor = ContextCompat.getColor(this, R.color.colorPrimaryLight)
    }

    private fun setupViews()
    {
        // Finding the Navigation Controller
        var navController = findNavController(R.id.nav_home_host_fragment)

        // Setting Navigation Controller with the BottomNavigationView
        binding.bottomNav.setupWithNavController(navController)
    }
}