package uz.kmax.kimyotest.presentation.ui.fragment.main.content

import dagger.hilt.android.AndroidEntryPoint
import uz.kmax.base.fragment.BaseFragmentWC
import uz.kmax.kimyotest.data.tools.firebase.FirebaseManager
import uz.kmax.kimyotest.data.tools.tools.SharedPref
import uz.kmax.kimyotest.databinding.FragmentPeriodicTableBinding
import javax.inject.Inject

@AndroidEntryPoint
class PeriodicTableFragment : BaseFragmentWC<FragmentPeriodicTableBinding>(
    FragmentPeriodicTableBinding::inflate
) {

    private lateinit var firebaseManager: FirebaseManager

    @Inject
    lateinit var shared: SharedPref

    override fun onViewCreated() {

    }
}