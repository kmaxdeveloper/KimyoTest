package uz.kmax.kimyotest.presentation.ui.fragment.tool

import android.content.Intent
import android.graphics.Color
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import uz.kmax.base.fragment.BaseFragmentWC
import uz.kmax.kimyotest.MainActivity
import uz.kmax.kimyotest.R
import uz.kmax.kimyotest.data.tools.tools.SharedPref
import uz.kmax.kimyotest.databinding.FragmentSettingsBinding
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment : BaseFragmentWC<FragmentSettingsBinding>(FragmentSettingsBinding::inflate) {

    @Inject
    lateinit var sharedPref: SharedPref

    override fun onViewCreated() {
        updateLanguageUI()
        updateTestModeUI()

        binding.modeHeart.setOnClickListener {
            sharedPref.setTestType(3)
            updateTestModeUI()
            showSnackBar(it)
        }

        binding.modeInfinity.setOnClickListener {
            sharedPref.setTestType(1)
            updateTestModeUI()
            showSnackBar(it)
        }

        binding.langUz.setOnClickListener {
            context?.let { ctx ->
                sharedPref.setLanguage(getString(R.string.lang_uz), ctx)
                updateLanguageUI()
                val intent = Intent(ctx, MainActivity::class.java)
                startActivity(intent)
                activity?.finish()
            }
        }

        binding.langEng.setOnClickListener {
            context?.let { ctx ->
                sharedPref.setLanguage(getString(R.string.lang_en), ctx)
                updateLanguageUI()
                val intent = Intent(ctx, MainActivity::class.java)
                startActivity(intent)
                activity?.finish()
            }
        }

        // Initialize Theme Switches
        val currentMode = sharedPref.getThemeMode()
        binding.systemThemeSwitch.isChecked = currentMode == AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM || currentMode == -1
        binding.nightModeSwitch.isChecked = currentMode == AppCompatDelegate.MODE_NIGHT_YES
        binding.nightModeSwitch.isEnabled = !binding.systemThemeSwitch.isChecked

        binding.systemThemeSwitch.setOnCheckedChangeListener { _, isChecked ->
            binding.nightModeSwitch.isEnabled = !isChecked
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                sharedPref.setThemeMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            } else {
                // If system theme is off, revert to whatever nightModeSwitch says
                val mode = if (binding.nightModeSwitch.isChecked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
                AppCompatDelegate.setDefaultNightMode(mode)
                sharedPref.setThemeMode(mode)
            }
        }

        binding.nightModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (!binding.systemThemeSwitch.isChecked) {
                val mode = if (isChecked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
                AppCompatDelegate.setDefaultNightMode(mode)
                sharedPref.setThemeMode(mode)
            }
        }
    }

    private fun updateLanguageUI() {
        val language = sharedPref.getLanguage().toString()
        if (language == "uz") {
            binding.langUzSelected.visibility = View.VISIBLE
            binding.langEnSelected.visibility = View.GONE
        } else {
            binding.langEnSelected.visibility = View.VISIBLE
            binding.langUzSelected.visibility = View.GONE
        }
    }

    private fun updateTestModeUI() {
        val testType = sharedPref.getTestType()
        if (testType == 3) {
            binding.modeHeartSelected.visibility = View.VISIBLE
            binding.modeInfinitySelected.visibility = View.GONE
        } else {
            binding.modeInfinitySelected.visibility = View.VISIBLE
            binding.modeHeartSelected.visibility = View.GONE
        }
    }

    private fun showSnackBar(view: View){
        context?.let { ctx ->
            Snackbar.make(view, "Test rejimi muvaffaqiyatli o'zgartirildi!", Snackbar.LENGTH_SHORT)
                .setBackgroundTint(ctx.getColor(R.color.chem_primary_light))
                .setTextColor(Color.WHITE)
                .show()
        }
    }


}