package com.proyectotitulo.appcomerciojcc.ui.register

import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proyectotitulo.appcomerciojcc.data.repository.ProfileRepository
import com.proyectotitulo.appcomerciojcc.domain.models.CommuneResponse.Commune
import com.proyectotitulo.appcomerciojcc.domain.models.LoginUiState
import kotlinx.coroutines.launch
import java.io.Console
import kotlin.math.log

class RegisterViewModel : ViewModel() {
    private val profileRepository = ProfileRepository()

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

    private val _commune = MutableLiveData<Int>()
    val commune : LiveData<Int> = _commune

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
                }
            }.onFailure { }
        }
    }

    fun onCommuneSelected(selectedCommune: Commune) {
        _commune.value = selectedCommune.communeId
        _selectedCommuneName.value = selectedCommune.communeName

        validateForm()
    }

    fun onFieldChanged(email: String, username: String, password: String, confirmPassword: String,
                       contact: String, commune: Int, profileDescription: String) {
        _email.value = email
        _username.value = username
        _password.value = password
        _confirmPassword.value = confirmPassword
        _contact.value = contact
        _commune.value = commune
        _profileDescription.value = profileDescription

        validateForm()
    }

    private fun validateForm() {
        _registerEnable.value = !email.value.isNullOrEmpty() &&
                !username.value.isNullOrEmpty() &&
                validPassword(password.value.orEmpty(), confirmPassword.value.orEmpty()) &&
                (commune.value ?: 0) != 0
    }

    fun validPassword(password: String, confirmPassword: String): Boolean = password.isNotEmpty()
            && confirmPassword.isNotEmpty()
            && password == confirmPassword
}