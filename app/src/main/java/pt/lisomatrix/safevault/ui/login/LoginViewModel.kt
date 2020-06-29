package pt.lisomatrix.safevault.ui.login

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pt.lisomatrix.safevault.crypto.AuthHandler

class LoginViewModel @ViewModelInject
        constructor(private val authHandler: AuthHandler) : ViewModel() {

    private val _onLogin = MutableLiveData<Boolean>()
    val onLogin: LiveData<Boolean>
        get() = _onLogin

    fun login(accountId: String, password: String) {
        // Call suspended functions on IO Context
        viewModelScope.launch(Dispatchers.IO) {
            _onLogin.postValue(authHandler.login(accountId, password))
        }
    }
}