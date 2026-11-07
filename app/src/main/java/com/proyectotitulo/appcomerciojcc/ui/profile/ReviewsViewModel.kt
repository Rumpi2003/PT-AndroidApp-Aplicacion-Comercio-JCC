package com.proyectotitulo.appcomerciojcc.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proyectotitulo.appcomerciojcc.data.repository.ProfileRepository
import com.proyectotitulo.appcomerciojcc.domain.models.GetReviewsResponse
import kotlinx.coroutines.launch

data class ReviewsUiState(
    val reviewsList: List<GetReviewsResponse.Data.Review> = emptyList(),
    val totalReviews: Int = 0,
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val isRefreshing: Boolean = false,
    val errors: List<String>? = null
)

class ReviewsViewModel : ViewModel() {

    private val repository = ProfileRepository()

    private val _uiState = MutableLiveData<ReviewsUiState>(ReviewsUiState())
    val uiState: LiveData<ReviewsUiState> = _uiState

    fun loadReviews(userId: Int, forceRefresh: Boolean = false) {
        val currentState = _uiState.value ?: ReviewsUiState()

        val offset = if (forceRefresh) 0 else currentState.reviewsList.size

        viewModelScope.launch {
            if (forceRefresh) {
                _uiState.value = currentState.copy(
                    isLoading = true,
                    errors = null,
                    reviewsList = emptyList()
                )
            } else {
                _uiState.value = currentState.copy(
                    isLoadingMore = true,
                    errors = null
                )
            }

            val result = repository.getReviews(userId, limit = 10, offset = offset)
            val latest = _uiState.value ?: ReviewsUiState()

            result.onSuccess { response ->
                if (response.status == "success") {
                    val newItems = response.data?.results ?: emptyList()
                    val updated = if (forceRefresh) newItems else (latest.reviewsList + newItems)

                    _uiState.value = latest.copy(
                        reviewsList = updated,
                        totalReviews = response.data?.totalReviews ?: updated.size,
                        isLoading = false,
                        isLoadingMore = false,
                        isRefreshing = false
                    )
                } else {
                    _uiState.value = latest.copy(
                        isLoading = false,
                        isLoadingMore = false,
                        isRefreshing = false,
                        errors = response.errors ?: listOf(response.message)
                    )
                }
            }.onFailure { error ->
                _uiState.value = latest.copy(
                    isLoading = false,
                    isLoadingMore = false,
                    isRefreshing = false,
                    errors = listOf(error.message ?: "Error al cargar reseñas")
                )
            }
        }
    }
}
