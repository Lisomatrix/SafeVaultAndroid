package pt.lisomatrix.safevault.ui.home.options.myfiles

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import pt.lisomatrix.safevault.R
import pt.lisomatrix.safevault.databinding.MyFilesFragmentBinding
import pt.lisomatrix.safevault.model.VaultFile
import pt.lisomatrix.safevault.other.MainClickListener
import pt.lisomatrix.safevault.other.SearchListener
import pt.lisomatrix.safevault.other.SelectedListener
import pt.lisomatrix.safevault.ui.component.SelectComponent.SelectedFragment
import pt.lisomatrix.safevault.ui.component.search.SearchToolbarFragment
import pt.lisomatrix.safevault.ui.home.options.myfiles.adapter.MyFilesAdapter


@AndroidEntryPoint
class MyFilesFragment : Fragment(), MainClickListener, SearchListener, SelectedListener {

    // List dependencies
    private lateinit var viewAdapter: MyFilesAdapter
    private lateinit var viewManager: LinearLayoutManager

    // Fragment objects
    private val viewModel: MyFilesViewModel by viewModels()
    private lateinit var binding: MyFilesFragmentBinding

    private val selectedItemsSize: MutableLiveData<Int> = MutableLiveData(0)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Initialize bindings
        binding = DataBindingUtil.inflate(inflater, R.layout.my_files_fragment, container, false)

        // Initialize list object dependencies
        viewManager = LinearLayoutManager(requireContext())
        viewAdapter = MyFilesAdapter(ArrayList(), this)

        // Observe for multi selection flag change
        // in order to save it on the view model
        // and restore it on screen rotate
        viewAdapter.isSelectMode.observe(this.viewLifecycleOwner) { isMultiSelectOn ->
            viewModel.isMultiSelectOn = isMultiSelectOn

            // If select flag is one then show select component
            if (isMultiSelectOn)
                showSelectedComponent()
            else
                showSearchComponent()
        }

        // Initialize recycler list
        binding.myFilesList.apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }

        // Attempt to recover previous state
        viewAdapter.recoverSelectState(viewModel.isMultiSelectOn, viewModel.selectedIds)

        // Get files from database
        viewModel.getVaultFiles().observe(this.viewLifecycleOwner) { files ->
            runBlocking { viewAdapter.setList(files.toMutableList() as ArrayList<VaultFile>) }
        }

        return binding.root
    }

    /**
     * Called when an element selected status changes
     *
     * @param [size] of selected elements
     */
    override fun mainInterface(size: Int) {
        viewModel.selectedIds = viewAdapter.selectedIds
        selectedItemsSize.value = viewModel.selectedIds.size
    }

    override fun onDestroy() {
        super.onDestroy()

        // We want to save the current adapter stater in order
        // to restore it later
        viewModel.selectedIds = viewAdapter.selectedIds
        viewModel.isMultiSelectOn = viewAdapter.selectedIds.size > 0
    }

    private fun showSelectedComponent() {
        val childFragment: Fragment = SelectedFragment.newInstance(this)
        val transaction = childFragmentManager.beginTransaction()
        transaction.replace(R.id.toolBarFragmentContainer, childFragment)

        transaction.commit()
    }

    private fun showSearchComponent() {
        val childFragment: Fragment = SearchToolbarFragment.newInstance(this)
        val transaction = childFragmentManager.beginTransaction()
        transaction.replace(R.id.toolBarFragmentContainer, childFragment)

        transaction.commit()

    }

    override fun searchTextChanged(searchText: String) {
        // TODO: IMPLEMENT SEARCH
    }

    override fun addPressed() {
        // TODO: IMPLEMENT ADD

        val file = VaultFile(null, "test", ".enc", "dunno")

        /*file.apply {
            extension = ".enc"
            name = "test"
            path = "dunno"
        }*/

        // TODO: VIEW MODEL ADD FILE
        viewModel.addFile(file)

        viewAdapter.itemAdded()
    }

    override fun onCancel() {
        viewAdapter.cancelSelectMode()
    }

    override fun onShare() {
        // TODO: IMPLEMENT SHARE
    }

    override fun onDelete() {
        // TODO: TEST NEEDED
        viewModel.deleteVaultFiles(viewAdapter.selectedIds.toLongArray())

        viewAdapter.cancelSelectMode()
    }

    override fun selectedItemsSize(): LiveData<Int> {
        return selectedItemsSize
    }
}