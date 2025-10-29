package uz.kmax.kimyotest.presentation.ui.fragment.main.content

import uz.kmax.base.fragment.BaseFragmentWC
import uz.kmax.kimyotest.R
import uz.kmax.kimyotest.data.tools.tools.onFragmentBackPressed
import uz.kmax.kimyotest.databinding.FragmentBookBinding
import uz.kmax.kimyotest.presentation.ui.fragment.main.content.list.BookListFragment
import java.io.File

class BookFragment(private var bookPath : String,private var bookName : String) : BaseFragmentWC<FragmentBookBinding>(FragmentBookBinding::inflate) {
    override fun onViewCreated() {
        val window = requireActivity().window
        window.statusBarColor = this.resources.getColor(R.color.appTheme)
        binding.toolbar.title = bookName
        binding.pdfViewer.initWithFile(File(bookPath))

        binding.back.setOnClickListener {
            startMainFragment(BookListFragment())
        }

        onFragmentBackPressed {
            startMainFragment(BookListFragment())
        }
    }
}