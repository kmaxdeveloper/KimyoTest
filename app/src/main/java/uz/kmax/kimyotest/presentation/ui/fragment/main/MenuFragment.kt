package uz.kmax.kimyotest.presentation.ui.fragment.main

import android.view.View
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import com.google.android.material.navigation.NavigationView
import com.google.android.play.core.review.ReviewException
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.android.play.core.review.model.ReviewErrorCode
import dagger.hilt.android.AndroidEntryPoint
import uz.kmax.base.fragment.BaseFragmentWC
import uz.kmax.base.fragmentcontroller.InnerFragmentController
import uz.kmax.kimyotest.R
import uz.kmax.kimyotest.data.tools.tools.SharedPref
import uz.kmax.kimyotest.databinding.FragmentMenuBinding
import uz.kmax.kimyotest.presentation.ui.fragment.other.AdminFragment
import uz.kmax.kimyotest.presentation.ui.fragment.other.PrivacyFragment
import uz.kmax.kimyotest.presentation.ui.fragment.tool.SettingsFragment
import uz.kmax.kimyotest.presentation.ui.fragment.main.arcade.GamesListFragment
import uz.kmax.kimyotest.data.ads.AdsManager
import javax.inject.Inject

@AndroidEntryPoint
class MenuFragment : BaseFragmentWC<FragmentMenuBinding>(FragmentMenuBinding::inflate) {
    private lateinit var toggleBar: ActionBarDrawerToggle

    @Inject
    lateinit var adsManager: AdsManager

    @Inject
    lateinit var shared: SharedPref

    override fun onViewCreated() {
        val window = requireActivity().window
        window.statusBarColor = this.resources.getColor(R.color.appTheme)

        InnerFragmentController.init(R.id.innerContainer, childFragmentManager)
        replaceInnerFragment(TestListFragment())
        
        updateNavigationVisibility(adsManager.isAppOpenAdShowing())
        
        adsManager.setOnAppOpenAdStatusListener { isShowing ->
            updateNavigationVisibility(isShowing)
        }

        toggleBar = ActionBarDrawerToggle(
            requireActivity(),
            binding.drawerLayout,
            binding.toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        binding.drawerLayout.addDrawerListener(toggleBar)
        toggleBar.syncState()

        binding.bottomNavigation.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.appTheme))
        binding.bottomNavigation.itemIconTintList = null

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when(item.itemId) {
                R.id.action_home -> {
                    // Respond to navigation item 1 click
                    replaceInnerFragment(TestListFragment())
                    true
                }
                R.id.action_content -> {
                    // Respond to navigation item 2 click
                    replaceInnerFragment(ContentFragment())
                    true
                }
                R.id.action_arcade -> {
                    replaceInnerFragment(GamesListFragment())
                    true
                }
                R.id.action_settings ->{
                    // Sozlamalar fragmentiga o'tish
                    replaceInnerFragment(SettingsFragment())
                    true
                }
                else -> false
            }
        }

        binding.navigationMenu.setNavigationItemSelectedListener(NavigationView.OnNavigationItemSelectedListener { item ->
            when (item.itemId) {

                R.id.homePage -> {
                    replaceInnerFragment(TestListFragment())
                    closeDrawer()
                    binding.drawerLayout.isSelected = false
                }

                R.id.ratingApp -> {
                    context?.let { ctx ->
                        val manager = ReviewManagerFactory.create(ctx)
                        val request = manager.requestReviewFlow()
                        request.addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val reviewInfo = task.result
                                activity?.let { act ->
                                    val flow = manager.launchReviewFlow(act, reviewInfo)
                                    flow.addOnCompleteListener { result ->
                                        if (!isAdded) return@addOnCompleteListener
                                        if (result.isCanceled) {
                                            toast("Dasturni baholash bekor qilindi !")
                                        } else if (result.isSuccessful) {
                                            toast("Dastur baholandi !!!")
                                        } else if (result.isComplete) {
                                            toast("Baholash tugatildi !")
                                        }
                                    }
                                }
                            } else {
                                @ReviewErrorCode val reviewErrorCode =
                                    (task.exception as ReviewException).errorCode
                            }
                        }
                    }
                    closeDrawer()
                    binding.drawerLayout.isSelected = false
                }

                R.id.devConnection -> {
                    replaceInnerFragment(AdminFragment())
                    closeDrawer()
                }

                R.id.privacyPolicy -> {
                    replaceInnerFragment(PrivacyFragment())
                    closeDrawer()
                }

                else -> return@OnNavigationItemSelectedListener true
            }
            true
        })

        // TODO: Crashlytics testi uchun (uncomment qiling va ilovani ishga tushiring)
        //throw RuntimeException("Test Crash for Crashlytics")
    }

    private fun updateNavigationVisibility(isAdShowing: Boolean) {
        if (isAdShowing) {
            binding.bottomNavigation.visibility = View.GONE
            binding.toolbar.visibility = View.GONE
        } else {
            binding.bottomNavigation.visibility = View.VISIBLE
            binding.toolbar.visibility = View.VISIBLE
        }
    }

    private fun closeDrawer() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START, true)
        }
    }

    private fun toast(message : String){
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun replaceInnerFragment(fragment : Fragment){
        InnerFragmentController.innerController?.startInnerMainFragment(fragment)
    }
}