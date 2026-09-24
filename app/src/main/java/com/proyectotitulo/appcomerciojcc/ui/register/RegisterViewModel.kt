package com.proyectotitulo.appcomerciojcc.ui.register

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proyectotitulo.appcomerciojcc.data.repository.ProfileRepository
import com.proyectotitulo.appcomerciojcc.data.repository.RegisterRepository
import com.proyectotitulo.appcomerciojcc.domain.models.GetCommuneResponse.Commune
import com.proyectotitulo.appcomerciojcc.domain.models.RegisterUiState
import kotlinx.coroutines.launch

class RegisterViewModel : ViewModel() {
    private val profileRepository = ProfileRepository()
    private val repository = RegisterRepository()

    private val _uiState = MutableLiveData<RegisterUiState>(RegisterUiState.Idle)
    val uiState: LiveData<RegisterUiState> = _uiState

    private val _email = MutableLiveData<String>()
    val email : LiveData<String> = _email

    private val _username = MutableLiveData<String>()
    val username : LiveData<String> = _username

    private val _password = MutableLiveData<String>()
    val password : LiveData<String> = _password

    private val _confirmPassword = MutableLiveData<String>()
    val confirmPassword : LiveData<String> = _confirmPassword

    private val _contact = MutableLiveData<String>()
    val contact : LiveData<String> = _contact

    private val _communeId = MutableLiveData<Int>()
    val communeId : LiveData<Int> = _communeId

    private val _communesList = MutableLiveData<List<Commune>>(emptyList())
    val communesList: LiveData<List<Commune>> = _communesList

    private val _selectedCommuneName = MutableLiveData<String>("")
    val selectedCommuneName: LiveData<String> = _selectedCommuneName

    private val _profileDescription = MutableLiveData<String>()
    val profileDescription : LiveData<String> = _profileDescription

    private val _registerEnable = MutableLiveData<Boolean>()
    val registerEnable : LiveData<Boolean> = _registerEnable

    init {
        loadCommunes()
    }

    private fun loadCommunes() {
        viewModelScope.launch {
            val result = profileRepository.getCommunes()
            result.onSuccess { response ->
                if (response.status == "success") {
                    _communesList.value = response.data ?: emptyList()
                } else {
                    _uiState.value = RegisterUiState.Error(
                        errors = response.errors ?: listOf(response.message)
                    )
                }
            }.onFailure {error ->
                _uiState.value = RegisterUiState.Error(
                    errors = listOf(error.message ?: "Error de red inesperado")
                )
            }
        }
    }

    fun onCommuneSelected(selectedCommune: Commune) {
        _communeId.value = selectedCommune.communeId
        _selectedCommuneName.value = selectedCommune.communeName

        validateForm()

        if (_uiState.value is RegisterUiState.Error) {
            _uiState.value = RegisterUiState.Idle
        }
    }

    fun onFieldChanged(email: String, username: String, password: String, confirmPassword: String,
                       contact: String, commune: Int, profileDescription: String) {
        _email.value = email
        _username.value = username
        _password.value = password
        _confirmPassword.value = confirmPassword
        _contact.value = contact
        _communeId.value = commune
        _profileDescription.value = profileDescription

        validateForm()

        if (_uiState.value is RegisterUiState.Error) {
            _uiState.value = RegisterUiState.Idle
        }
    }

    private fun validateForm() {
        _registerEnable.value = !email.value.isNullOrEmpty() &&
                !username.value.isNullOrEmpty() &&
                validPassword(password.value.orEmpty(), confirmPassword.value.orEmpty()) &&
                (communeId.value ?: 0) != 0
    }

    fun validPassword(password: String, confirmPassword: String): Boolean = password.isNotEmpty()
            && confirmPassword.isNotEmpty()
            && password == confirmPassword

    fun onRegisterSelected() {
        val currentEmail = email.value.orEmpty()
        val currentPassword = password.value.orEmpty()
        val currentUsername = username.value.orEmpty()
        val currentContact = contact.value.orEmpty()
        val currentDescription = profileDescription.value.orEmpty()
        val currentCommuneId = communeId.value ?: 0

        viewModelScope.launch {
            _uiState.value = RegisterUiState.Loading

            val result = repository.doRegister(
                currentEmail,
                currentPassword,
                currentUsername,
                currentContact,
                currentDescription,
                currentCommuneId
            )

            result.onSuccess { response ->
                if (response.status == "success") {
                    val registeredName = response.data?.user?.username.orEmpty()
                    _uiState.value = RegisterUiState.Success(
                        username = registeredName,
                        message = response.message
                    )
                } else {
                    _uiState.value = RegisterUiState.Error(
                        errors = response.errors ?: listOf(response.message)
                    )
                }
            }.onFailure { error ->
                _uiState.value = RegisterUiState.Error(
                    errors = listOf(error.message ?: "Error de red inesperado")
                )
            }
        }


    }
}