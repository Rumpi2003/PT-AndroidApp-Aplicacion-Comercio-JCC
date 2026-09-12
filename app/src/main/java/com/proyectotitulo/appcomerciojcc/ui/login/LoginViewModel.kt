package com.proyectotitulo.appcomerciojcc.ui.login

import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proyectotitulo.appcomerciojcc.data.repository.LoginRepository
import kotlinx.coroutines.launch
class LoginViewModel : ViewModel() {

    private val repository = LoginRepository()

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading : LiveData<Boolean> = _isLoading

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
    }

    private fun isValidEmail(email: String): Boolean = Patterns.EMAIL_ADDRESS.matcher(email).matches()
    private fun isValidPassword(password: String): Boolean = password.length >= 8

    fun onLoginSelected() {
        val currentEmail = email.value.orEmpty()
        val currentPassword = password.value.orEmpty()

        viewModelScope.launch {
            _isLoading.value = true

            val result = repository.doLogin(currentEmail, currentPassword)

            result.onSuccess { response ->
                if (response.status == "success") {
                    println("Login exitoso: ${response.message}")
                    println("Nombre de usuario: ${response.data?.user?.username}")
                    println("Token: ${response.data?.token}")
                } else {
                    println("Error de API: ${response.message}: ${response.errors}")
                }
            }.onFailure { error ->
                println("Error de red: ${error.message}")
            }

            _isLoading.value = false
        }
    }
}