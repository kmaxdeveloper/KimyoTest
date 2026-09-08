package uz.kmax.kimyotest.presentation.ui.fragment.main.content

import android.os.Bundle
import android.widget.Toast
import uz.kmax.base.fragment.BaseFragmentWC
import uz.kmax.kimyotest.R
import uz.kmax.kimyotest.data.tools.tools.onFragmentBackPressed
import uz.kmax.kimyotest.databinding.FragmentBookBinding
import uz.kmax.kimyotest.presentation.ui.fragment.main.content.list.BookListFragment
import java.io.File

class BookFragment : BaseFragmentWC<FragmentBookBinding>(FragmentBookBinding::inflate) {
    
    private var bookPath: String = ""
    private var bookName: String = ""

    companion object {
        private const val ARG_BOOK_PATH = "book_path"
        private const val ARG_BOOK_NAME = "book_name"

        fun newInstance(bookPath: String, bookName: String): BookFragment {
            val fragment = BookFragment()
            val args = Bundle()
            args.putString(ARG_BOOK_PATH, bookPath)
            args.putString(ARG_BOOK_NAME, bookName)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onViewCreated() {
        bookPath = arguments?.getString(ARG_BOOK_PATH) ?: ""
        bookName = arguments?.getString(ARG_BOOK_NAME) ?: ""
        
        val window = requireActivity().window
        window.statusBarColor = this.resources.getColor(R.color.appTheme)
        binding.toolbarTitle.text = bookName
        
        val file = File(bookPath)
        if (file.exists()) {
            binding.pdfViewer.initWithFile(file)
        } else {
            Toast.makeText(requireContext(), "Fayl topilmadi yoki yuklanmagan", Toast.LENGTH_SHORT).show()
            startMainFragment(BookListFragment())
        }

        binding.back.setOnClickListener {
            startMainFragment(BookListFragment())
        }

        onFragmentBackPressed {
            startMainFragment(BookListFragment())
        }
    }
}