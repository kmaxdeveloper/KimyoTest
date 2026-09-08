package uz.kmax.kimyotest.presentation.ui.fragment.tool

import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import uz.kmax.base.fragment.BaseFragmentWC
import uz.kmax.kimyotest.MainActivity
import uz.kmax.kimyotest.R
import uz.kmax.kimyotest.data.tools.tools.SharedPref
import uz.kmax.kimyotest.databinding.FragmentLanguageBinding
import javax.inject.Inject

@AndroidEntryPoint
class LanguageFragment : BaseFragmentWC<FragmentLanguageBinding>(FragmentLanguageBinding::inflate) {

    @Inject
    lateinit var sharedPref: SharedPref

    override fun onViewCreated() {
        binding.selectLangEn.setOnClickListener {
            context?.let { ctx ->
                sharedPref.setLanguage(getString(R.string.lang_en), ctx)
                sharedPref.setLangStatus(false)
                val intent = Intent(ctx, MainActivity::class.java)
                startActivity(intent)
                activity?.finish()
            }
        }

        binding.selectLangUz.setOnClickListener {
            context?.let { ctx ->
                sharedPref.setLanguage(getString(R.string.lang_uz), ctx)
                sharedPref.setLangStatus(false)
                val intent = Intent(ctx, MainActivity::class.java)
                startActivity(intent)
                activity?.finish()
            }
        }
    }
}