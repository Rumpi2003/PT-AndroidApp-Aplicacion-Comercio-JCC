package com.proyectotitulo.appcomerciojcc.ui.login

import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proyectotitulo.appcomerciojcc.data.repository.LoginRepository
import com.proyectotitulo.appcomerciojcc.domain.models.LoginUiState
import kotlinx.coroutines.launch
class LoginViewModel : ViewModel() {

    private val repository = LoginRepository()

    private val _uiState = MutableLiveData<LoginUiState>(LoginUiState.Idle)
    val uiState: LiveData<LoginUiState> = _uiState

    private val _email = MutableLiveData<String>()
    val email : LiveData<String> = _email

    private val _password = MutableLiveData<String>()
    val password : LiveData<String> = _password

    private val _loginEnable = MutableLiveData<Boolean>()
    val loginEnable : LiveData<Boolean> = _loginEnable

    fun onTextFieldChanged(email: String, password: String) {
        _email.value = email
        _password.value = password
        _loginEnable.value = isValidEmail(email) && isValidPassword(password)

        if (_uiState.value is LoginUiState.Error) {
            _uiState.value = LoginUiState.Idle
        }
    }

    private fun isValidEmail(email: String): Boolean = Patterns.EMAIL_ADDRESS.matcher(email).matches()
    private fun isValidPassword(password: String): Boolean = password.length >= 8

    fun onLoginSelected() {
        val currentEmail = email.value.orEmpty()
        val currentPassword = password.value.orEmpty()

        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading

            val result = repository.doLogin(currentEmail, currentPassword)

            result.onSuccess { response ->
                if (response.status == "success") {
                    val username = response.data?.user?.username.orEmpty()
                    _uiState.value = LoginUiState.Succes(username, response.message)
                } else {
                    _uiState.value = LoginUiState.Error(response.errors)
                }
            }.onFailure { error ->
                val errors = listOf<String>(error.message?: "Error de red inesperado")
                _uiState.value = LoginUiState.Error(errors)
            }
        }
    }
}