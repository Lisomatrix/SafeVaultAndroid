package pt.lisomatrix.safevault.ui.account

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import pt.lisomatrix.safevault.R
import pt.lisomatrix.safevault.databinding.AccountFragmentBinding
import pt.lisomatrix.safevault.ui.home.HomeActivity

@AndroidEntryPoint
class AccountFragment : Fragment() {

    private val viewModel: AccountViewModel by viewModels()

    private lateinit var binding: AccountFragmentBinding
    private lateinit var accountID: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Initialize bindings
        binding = DataBindingUtil.inflate(inflater, R.layout.account_fragment, container, false)

        // Get account ID
        viewModel.account.observe(this.viewLifecycleOwner) { account ->
            binding.accountIDText.text = account.accountID
            accountID = account.accountID
        }

        binding.btnCopy.setOnClickListener {
            // Get clipboard manager and set the code
            val clipboard = getSystemService(requireContext(), ClipboardManager::class.java)
            val data = ClipData.newPlainText("AccountID", accountID)
            clipboard?.setPrimaryClip(data)

            // Notify user of the action
            Toast
                .makeText(
                    requireContext(),
                    getString(R.string.clipboard_copy),
                    Toast.LENGTH_SHORT
                )
                .show()
        }

        // Navigate to home page
        binding.continueTxt.setOnClickListener {
            val intent = Intent(requireContext(), HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
            startActivity(intent)
            activity?.finish()
        }

        return binding.root
    }

}