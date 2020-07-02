package pt.lisomatrix.safevault.ui.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.runBlocking
import pt.lisomatrix.safevault.R
import pt.lisomatrix.safevault.databinding.LoginFragmentBinding
import pt.lisomatrix.safevault.extensions.hideKeyboard
import pt.lisomatrix.safevault.ui.account.AccountFragmentDirections
import pt.lisomatrix.safevault.ui.home.HomeActivity

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private lateinit var binding: LoginFragmentBinding

    private lateinit var viewModel: LoginViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Initialize view model
        viewModel = ViewModelProvider(this).get(LoginViewModel::class.java)
        // Initialize binding
        binding = DataBindingUtil
            .inflate(inflater, R.layout.login_fragment, container, false)

        binding.viewModel = viewModel

        setUpListeners()

        return binding.root
    }

    private fun setUpListeners() {
        // Listen for login click
        binding.loginBtn.setOnClickListener { onLoginClick() }

        // Listen for register click
        binding.registerTv.setOnClickListener {
            findNavController()
                .navigate(LoginFragmentDirections.actionLoginFragmentToRegisterFragment())
        }

        // Listen for login click result
        viewModel.onLogin.observe(this.viewLifecycleOwner, Observer { isLoggedIn ->
            if (isLoggedIn) {
                // Navigate to home
                findNavController()
                    .navigate(LoginFragmentDirections.actionLoginFragmentToHomeActivity())
                requireActivity().finish()
            } else {
                // Show error message
                // I don't show any attempts warning so the files are deleted
                // Without the malicious user knowing
                binding.errorLabel.visibility = View.VISIBLE
                setEnabled(true)
            }
        })
    }

    private fun onLoginClick() {
        // Close keyboard
        // So it won't show on next page
        binding.root.hideKeyboard()

        // Check if all fields are filled
        val accountID = binding.accountIDText.text.toString()
        val password = binding.passwordText.text.toString()

        if (
            accountID != "" && accountID.isNotEmpty() &&
            password != "" && password.isNotEmpty()
        ) {
            setEnabled(false)
            viewModel.login(accountID, password)
        }

    }

    private fun setEnabled(enabled: Boolean) {
        binding.accountIDText.isEnabled = enabled
        binding.passwordText.isEnabled = enabled
        binding.loginBtn.isEnabled = enabled
    }


}