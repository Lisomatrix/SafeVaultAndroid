package pt.lisomatrix.safevault.ui.register

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.runBlocking
import pt.lisomatrix.safevault.R
import pt.lisomatrix.safevault.databinding.RegisterFragmentBinding
import pt.lisomatrix.safevault.extensions.hideKeyboard

@AndroidEntryPoint
class RegisterFragment : Fragment() {

    private val viewModel: RegisterViewModel by viewModels()

    private lateinit var binding: RegisterFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Initialize binding
        binding = DataBindingUtil.inflate(inflater, R.layout.register_fragment, container, false)
        binding.viewModel = viewModel

        setUpListeners()

        return binding.root
    }

    private fun setUpListeners() {
        // On register press
        binding.registerBtn.setOnClickListener { onRegisterPress() }

        // On arrow press go back
        binding.backBtn.setOnClickListener { findNavController().popBackStack() }

        // On register complete
        viewModel.onRegister.observe(this.viewLifecycleOwner, Observer { isRegistered ->
            if (isRegistered) {
                // Navigate to welcome
                findNavController().navigate(RegisterFragmentDirections.actionRegisterFragmentToWelcomeFragment())
            }
        })
    }

    private fun onRegisterPress() {
        // Close keyboard
        // So it won't show on next page
        binding.root.hideKeyboard()

        // Check if fields are correctly filled
        val password = binding.passwordText.text.toString()
        val repassword = binding.repasswordText.text.toString()

        if (
            !password.isNullOrBlank() && password.isNotEmpty() &&
            !repassword.isNullOrBlank() && repassword.isNotEmpty() &&
            password == repassword
        )
            viewModel.register(password)
    }
}