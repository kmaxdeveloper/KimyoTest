package uz.kmax.kimyotest.presentation.ui.fragment.main.arcade

import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import uz.kmax.base.fragment.BaseFragmentWC
import uz.kmax.kimyotest.data.ads.AdsManager
import uz.kmax.kimyotest.databinding.FragmentGamesListBinding
import uz.kmax.kimyotest.presentation.ui.adapter.GamesAdapter
import javax.inject.Inject

@AndroidEntryPoint
class GamesListFragment : BaseFragmentWC<FragmentGamesListBinding>(FragmentGamesListBinding::inflate) {

    @Inject
    lateinit var adsManager: AdsManager

    override fun onViewCreated() {
        val adapter = GamesAdapter()
        binding.gamesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.gamesRecyclerView.adapter = adapter

        adapter.setOnGameClickListener {
            val currentActivity = activity ?: return@setOnGameClickListener
            adsManager.setOnAdDismissListener {
                if (isAdded && !isStateSaved) {
                    startMainFragment(Chemistry2048Fragment())
                }
            }
            adsManager.showAds(currentActivity, false) { showed ->
                if (!showed) {
                    if (isAdded && !isStateSaved) {
                        startMainFragment(Chemistry2048Fragment())
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        adsManager.setOnAdDismissListener {}
        adsManager.setOnAdClickListener {}
        super.onDestroyView()
    }
}
