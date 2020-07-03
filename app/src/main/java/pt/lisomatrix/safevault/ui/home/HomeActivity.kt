package pt.lisomatrix.safevault.ui.home

import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import dagger.hilt.android.AndroidEntryPoint
import pt.lisomatrix.safevault.R
import pt.lisomatrix.safevault.databinding.ActivityHomeBinding
import pt.lisomatrix.safevault.ui.auth.AuthActivity
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions
import java.util.concurrent.Executor


@AndroidEntryPoint
class HomeActivity : AppCompatActivity() {

    companion object {
        const val IS_KEY_GENERATED = "KEY_GENERATED"
        var isSecure = false
        var isFirstRequest = true
    }

    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize binding
        binding = DataBindingUtil.setContentView(this, R.layout.activity_home)
        // When in background we want to prevent screen shots
        // Over even peek at files
        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)

        val keyguardManager = application.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager

        if (keyguardManager.isDeviceSecure && !isSecure) {
            startActivityForResult(Intent(applicationContext, AuthActivity::class.java), 2)
        }

        // Initialize navigation
        setupViews()

        window.statusBarColor = ContextCompat.getColor(this, R.color.colorAccent)
        window.statusBarColor = ContextCompat.getColor(this, R.color.colorPrimaryLight)


        methodRequiresTwoPermission()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    override fun onResume() {
        super.onResume()

        if (isFirstRequest) {
            isFirstRequest = false
            return
        }

        val keyguardManager = application.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager

        if (keyguardManager.isDeviceSecure && !isSecure) {
            startActivityForResult(Intent(applicationContext, AuthActivity::class.java), 2)
        }
    }

    override fun onPause() {
        super.onPause()
        isSecure = false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 2 && resultCode == Activity.RESULT_OK) {
            if (data?.getBooleanExtra(AuthActivity.AUTH_OK, false)!!) {
                isSecure = true
            }
        }
    }

    @AfterPermissionGranted(15)
    private fun methodRequiresTwoPermission() {
        val perms = arrayOf(
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        )

        if (!EasyPermissions.hasPermissions(this, *perms)) {
            EasyPermissions.requestPermissions(this, "", 15, perms[0], perms[1])
        }
    }

    private fun setupViews()
    {
        // Finding the Navigation Controller
        var navController = findNavController(R.id.nav_home_host_fragment)

        // Setting Navigation Controller with the BottomNavigationView
        binding.bottomNav.setupWithNavController(navController)
    }
}