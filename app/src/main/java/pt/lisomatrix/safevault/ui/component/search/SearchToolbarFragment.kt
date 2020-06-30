package pt.lisomatrix.safevault.ui.component.search

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import pt.lisomatrix.safevault.R
import pt.lisomatrix.safevault.databinding.SearchToolbarFragmentBinding
import pt.lisomatrix.safevault.other.SearchListener

class SearchToolbarFragment(private val searchListener: SearchListener, private val showAddBtn: Boolean) : Fragment() {

    companion object {
        fun newInstance(searchListener: SearchListener, showAddBtn: Boolean = true)
                = SearchToolbarFragment(searchListener, showAddBtn)
    }

    private val viewModel: SearchToolbarViewModel by viewModels()
    private lateinit var binding: SearchToolbarFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.search_toolbar_fragment, container, false)

        binding.searchTxt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                searchListener.searchTextChanged(p0.toString())
            }
        })

        binding.addBtn.setOnClickListener { searchListener.addPressed() }

        if (!showAddBtn)
            binding.addBtn.visibility = View.INVISIBLE

        return binding.root
    }

}