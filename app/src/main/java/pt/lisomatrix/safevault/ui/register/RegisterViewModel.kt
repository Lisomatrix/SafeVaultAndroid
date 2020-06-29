package pt.lisomatrix.safevault.ui.register

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pt.lisomatrix.safevault.crypto.AuthHandler

class RegisterViewModel @ViewModelInject
        constructor(private val authHandler: AuthHandler) : ViewModel() {



    private val _onRegister = MutableLiveData<Boolean>()
    val onRegister: LiveData<Boolean>
        get() = _onRegister

    fun register(password: String) {
        viewModelScope.launch(Dispatchers.IO) {
            authHandler.register(password)
            _onRegister.postValue(true)
        }
    }
}