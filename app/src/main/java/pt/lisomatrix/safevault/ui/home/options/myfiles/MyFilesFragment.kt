package pt.lisomatrix.safevault.ui.home.options.myfiles

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract
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
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.PublishSubject
import kotlinx.android.synthetic.main.my_files_fragment.*
import pt.lisomatrix.safevault.R
import pt.lisomatrix.safevault.SafeVaultApplication.Companion.APPLICATION_NAME
import pt.lisomatrix.safevault.databinding.MyFilesFragmentBinding
import pt.lisomatrix.safevault.model.VaultFile
import pt.lisomatrix.safevault.other.ASFUriUtil.getPath
import pt.lisomatrix.safevault.other.MainClickListener
import pt.lisomatrix.safevault.other.SearchListener
import pt.lisomatrix.safevault.other.SelectedListener
import pt.lisomatrix.safevault.ui.component.SelectComponent.SelectedFragment
import pt.lisomatrix.safevault.ui.component.search.SearchToolbarFragment
import pt.lisomatrix.safevault.ui.home.HomeActivity.Companion.IS_KEY_GENERATED
import pt.lisomatrix.safevault.ui.home.options.myfiles.adapter.MyFilesAdapter
import java.util.concurrent.TimeUnit


@AndroidEntryPoint
class MyFilesFragment : Fragment(), MainClickListener, SearchListener, SelectedListener {

    companion object {
        const val READ_REQUEST_CODE: Int = 42
    }

    // List dependencies
    private lateinit var viewAdapter: MyFilesAdapter
    private lateinit var viewManager: LinearLayoutManager

    // Fragment objects
    private val viewModel: MyFilesViewModel by viewModels()
    private lateinit var binding: MyFilesFragmentBinding

    private val selectedItemsSize: MutableLiveData<Int> = MutableLiveData(0)

    private var isSearchMode = false

    // Get all files stream subscription
    private var getFilesSubscription: Disposable? = null

    // search files stream subscription
    private var searchFilesSubscription: Disposable? = null

    private var searchTextSubscription: Disposable? = null

    private val searchTextSubject = PublishSubject.create<String>()

    private var selectedItemId: Long = -1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Initialize bindings
        binding = DataBindingUtil.inflate(inflater, R.layout.my_files_fragment, container, false)

        // Initialize list object dependencies
        //viewManager = CustomLinearLayoutManager(requireContext())
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

        // I will need to cancel is stream later
        // In order to use a search stream
        // So I will be using RxJava instead
        // Of Live data but will keep Kotlin Coroutines
        // In order top be able to cancel diffing job
        getFilesStream()

        onSearchChanged()

        val sharedPref = requireContext()
            .getSharedPreferences(APPLICATION_NAME, Context.MODE_PRIVATE)

        if (!sharedPref.getBoolean(IS_KEY_GENERATED, false)) {
            viewModel.generateKey()

            with (sharedPref.edit()) {
                putBoolean(IS_KEY_GENERATED, true)
                commit()
            }
        }


        return binding.root
    }

    private fun disposeStreams() {
        searchFilesSubscription?.dispose()
        getFilesSubscription?.dispose()
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

        disposeStreams()
        searchTextSubscription?.dispose()
    }

    /**
     * Change toolbar component to selected
     */
    private fun showSelectedComponent() {
        val childFragment: Fragment = SelectedFragment.newInstance(this)
        val transaction = childFragmentManager.beginTransaction()
        transaction.replace(R.id.toolBarFragmentContainer, childFragment)

        transaction.commit()
    }

    /**
     * Change toolbar component to search
     */
    private fun showSearchComponent() {
        val childFragment: Fragment = SearchToolbarFragment.newInstance(this)
        val transaction = childFragmentManager.beginTransaction()
        transaction.replace(R.id.toolBarFragmentContainer, childFragment)

        transaction.commit()
    }

    /**
     * Get all files from database as a stream
     */
    private fun getFilesStream() {
        // Dispose previous streams
        disposeStreams()

        // Start new stream
        getFilesSubscription = viewModel.getVaultFiles()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { files ->
                // For some reason without this sometimes it crashes
                // I barely can replicate the error but sometimes happens
                myFilesList.stopScroll();
                viewAdapter.setList(files.toMutableList() as ArrayList<VaultFile>)
            }
    }

    /**
     * Get search results as a stream
     */
    private fun getSearchFilesStream(searchText: String) {
        // Dispose previous streams
        disposeStreams()

        searchFilesSubscription = viewModel.getVaultFilesByName(searchText)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { files ->
                // For some reason without this sometimes it crashes
                // I barely can replicate the error but sometimes happens
                myFilesList.stopScroll();
                viewAdapter.setList(files.toMutableList() as ArrayList<VaultFile>)
            }
    }

    /**
     * Give some time before sending research request to the database
     */
    private fun onSearchChanged() {
         searchTextSubscription = searchTextSubject
            .debounce(300, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(this::handleSearch)
    }

    /**
     * Check if files sources should change
     */
    private fun handleSearch(searchText: String) {
        if (!isSearchMode) {
            // Set flag on
            isSearchMode = true
            getSearchFilesStream(searchText)

        } else if (isSearchMode) {
            // Set flag off
            isSearchMode = false
            getFilesStream()
        }
    }

    /**
     * Receive text from search bar
     */
    override fun searchTextChanged(searchText: String) {
        if (!searchText.isNullOrBlank())
            searchTextSubject.onNext(searchText)
    }

    override fun addPressed() {
        // Request user to search for a file
        performFileSearch()
    }

    override fun onCancel() {
        viewAdapter.cancelSelectMode()
    }

    override fun onShare() {
        // TODO: IMPLEMENT SHARE
    }

    override fun onDelete() {
        viewModel.deleteVaultFiles(viewAdapter.selectedIds.toLongArray())
        viewAdapter.cancelSelectMode()

        // Reset search
        searchTextChanged("")
    }

    override fun selectedItemsSize(): LiveData<Int> {
        return selectedItemsSize
    }

    private fun performFileSearch() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = "*/*"
        }

        startActivityForResult(intent, READ_REQUEST_CODE)
    }

    private fun performFolderSearch() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        startActivityForResult(intent, READ_REQUEST_CODE + 1)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            resultData?.data?.also { uri ->
                viewModel.encryptFile(uri)
            }
        } else if (requestCode == READ_REQUEST_CODE + 1 && resultCode == Activity.RESULT_OK) {
            resultData?.data?.also { uri ->
                /*val docUri: Uri = DocumentsContract.buildDocumentUriUsingTree(
                    uri,
                    DocumentsContract.getTreeDocumentId(uri)
                )
                val path: String = getPath(requireContext(), docUri)*/
                viewModel.decryptFile(selectedItemId, uri)
                selectedItemId = -1
            }
        }
    }

    override fun itemPressed(index: Long) {
        selectedItemId = index
        performFolderSearch()
    }

}