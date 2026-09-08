package uz.kmax.kimyotest

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.firebase.FirebaseApp
import dagger.hilt.android.AndroidEntryPoint
import uz.kmax.base.fragmentcontroller.FragmentController
import uz.kmax.kimyotest.data.tools.tools.SharedPref
import uz.kmax.kimyotest.databinding.ActivityMainBinding
import uz.kmax.kimyotest.presentation.ui.fragment.tool.LanguageFragment
import uz.kmax.kimyotest.presentation.ui.fragment.welcome.SplashFragment
import uz.kmax.kimyotest.presentation.ui.fragment.welcome.WelcomeFragment
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    @Inject
    lateinit var shared: SharedPref

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _: Boolean -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(shared.getThemeMode())
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            // Paddingni olib tashladik, shunda kontent ekranga to'liq yoyiladi
            insets
        }
        
        checkNotificationPermission()
        
        FirebaseApp.initializeApp(this)
        FragmentController.init(R.id.container, supportFragmentManager)
        if (!shared.getLangStatus()) {
            if (shared.getWelcomeStatus()) {
                FragmentController.controller?.startMainFragment(WelcomeFragment())
            } else {
                FragmentController.controller?.startMainFragment(SplashFragment())
            }
        }else{
            FragmentController.controller?.startMainFragment(LanguageFragment())
        }

        WindowCompat.setDecorFitsSystemWindows(window, false)
        hideSystemUI()
    }

    private fun hideSystemUI() {
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.systemBars()) // status + nav bar yashiradi
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }

    override fun onResume() {
        super.onResume()
        hideSystemUI()
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}