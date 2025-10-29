package uz.kmax.kimyotest

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
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
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.systemBars()) // status + nav bar yashiradi
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
}