package pt.lisomatrix.safevault.ui.home.options.sharedfiles

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import pt.lisomatrix.safevault.R

class SharedFilesFragment : Fragment() {

    companion object {
        fun newInstance() = SharedFilesFragment()
    }

    private lateinit var viewModel: SharedFilesViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.shared_files_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(SharedFilesViewModel::class.java)
        // TODO: Use the ViewModel
    }

}