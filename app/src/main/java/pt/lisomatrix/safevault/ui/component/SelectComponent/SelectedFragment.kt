package pt.lisomatrix.safevault.ui.component.SelectComponent

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import pt.lisomatrix.safevault.R
import pt.lisomatrix.safevault.databinding.SelectedFragmentBinding
import pt.lisomatrix.safevault.other.SelectedListener

class SelectedFragment(private val selectedListener: SelectedListener) : Fragment() {

    companion object {
        fun newInstance(selectedListener: SelectedListener)
                = SelectedFragment(selectedListener)
    }

    private val viewModel: SelectedViewModel by viewModels()
    private lateinit var binding: SelectedFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.selected_fragment, container, false)

        binding.cancelBtn.setOnClickListener { selectedListener.onCancel() }
        binding.deleteBtn.setOnClickListener { selectedListener.onDelete() }
        binding.shareBtn.setOnClickListener { selectedListener.onShare() }

        selectedListener.selectedItemsSize().observe(this.viewLifecycleOwner) { size ->
            binding.selectedNumberTxt.text = "$size Selected"
        }

        return binding.root
    }


}