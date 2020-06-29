package pt.lisomatrix.safevault.ui.home.options.myfiles

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StableIdKeyProvider
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.my_files_fragment.*
import pt.lisomatrix.safevault.R
import pt.lisomatrix.safevault.databinding.MyFilesFragmentBinding
import pt.lisomatrix.safevault.model.VaultFile
import pt.lisomatrix.safevault.other.MainClickListener
import pt.lisomatrix.safevault.ui.home.options.myfiles.adapter.MyFilesAdapter

@AndroidEntryPoint
class MyFilesFragment : Fragment(), MainClickListener {

    private lateinit var viewAdapter: MyFilesAdapter
    private lateinit var viewManager: LinearLayoutManager

    private val viewModel: MyFilesViewModel by viewModels()
    private lateinit var binding: MyFilesFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.my_files_fragment, container, false)

        val test = ArrayList<VaultFile>()

        for (i in 1..2) {
            test.add(VaultFile(i.toLong(), "My cool file $i", ".enc","dunno"))
        }

        viewManager = LinearLayoutManager(requireContext())
        viewAdapter = MyFilesAdapter(test, this, viewManager)

        viewAdapter.isSelectMode.observe(this.viewLifecycleOwner) { isMultiSelectOn ->
            viewModel.isMultiSelectOn = isMultiSelectOn

        }

        binding.myFilesList.apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }

        viewAdapter.recoverSelectState(viewModel.isMultiSelectOn, viewModel.selectedIds)

        return binding.root
    }

    override fun mainInterface(size: Int) {
        viewModel.selectedIds = viewAdapter.selectedIds
    }

    override fun onDestroy() {
        super.onDestroy()

        viewModel.selectedIds = viewAdapter.selectedIds
        viewModel.isMultiSelectOn = viewAdapter.selectedIds.size > 0
    }
}