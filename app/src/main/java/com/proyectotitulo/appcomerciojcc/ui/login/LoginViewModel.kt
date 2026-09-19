package com.proyectotitulo.appcomerciojcc.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proyectotitulo.appcomerciojcc.data.local.SessionManager
import com.proyectotitulo.appcomerciojcc.data.remote.KtorApiClient
import com.proyectotitulo.appcomerciojcc.data.repository.LoginRepository
import com.proyectotitulo.appcomerciojcc.domain.models.LoginUiState
import com.proyectotitulo.appcomerciojcc.domain.models.RegisterUiState
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
        _loginEnable.value = email.isNotEmpty() && password.isNotEmpty()

        if (_uiState.value is LoginUiState.Error) {
            _uiState.value = LoginUiState.Idle
        }
    }

    fun onLoginSelected(sessionManager: SessionManager) {
        val currentEmail = email.value.orEmpty()
        val currentPassword = password.value.orEmpty()

        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading

            val result = repository.doLogin(currentEmail, currentPassword)

            result.onSuccess { response ->
                if (response.status == "success") {
                    val token = response.data?.token.orEmpty()
                    val username = response.data?.user?.username.orEmpty()

                    sessionManager.saveToken(token)
                    KtorApiClient.token = token
                    _uiState.value = LoginUiState.Success(
                        username = username,
                        message = response.message
                    )
                } else {
                    _uiState.value = LoginUiState.Error(
                        errors = response.errors ?: listOf(response.message)
                    )
                }
            }.onFailure { error ->
                _uiState.value = LoginUiState.Error(
                    errors = listOf(error.message ?: "Error de red inesperado")
                )
            }
        }
    }
}