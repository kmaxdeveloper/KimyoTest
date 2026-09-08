package uz.kmax.kimyotest.presentation.ui.fragment.welcome

import android.view.View
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import uz.kmax.base.fragment.BaseFragmentWC
import uz.kmax.kimyotest.R
import uz.kmax.kimyotest.data.adapter.WelcomeAdapter
import uz.kmax.kimyotest.data.tools.tools.SharedPref
import uz.kmax.kimyotest.databinding.FragmentWelcomeBinding
import javax.inject.Inject

@AndroidEntryPoint
class WelcomeFragment: BaseFragmentWC<FragmentWelcomeBinding>(FragmentWelcomeBinding::inflate) {
    private lateinit var adapter: WelcomeAdapter

    @Inject
    lateinit var shared: SharedPref

    override fun onViewCreated() {
        shared = SharedPref(requireContext())
        adapter = WelcomeAdapter(requireContext())
        binding.viewPager.adapter = adapter
        TabLayoutMediator(binding.indicator, binding.viewPager) { tab, position -> }.attach()
        
        binding.start.setOnClickListener {
            shared.setWelcomeStatus(resume = false)
            startMainFragment(SplashFragment())
        }

        binding.nextButton.setOnClickListener {
            binding.viewPager.currentItem += 1
        }

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (position == 4) {
                    binding.indicator.visibility = View.INVISIBLE
                    binding.nextButton.visibility = View.INVISIBLE
                    binding.start.visibility = View.VISIBLE
                } else {
                    binding.indicator.visibility = View.VISIBLE
                    binding.nextButton.visibility = View.VISIBLE
                    binding.start.visibility = View.GONE
                }
            }
        })
    }
}