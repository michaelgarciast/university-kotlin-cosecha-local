package com.garcia.cosechalocal.ui.admin

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.garcia.cosechalocal.data.model.ModerationStatus
import com.garcia.cosechalocal.data.model.ProductModeration
import com.garcia.cosechalocal.data.model.SalesPeriod
import com.garcia.cosechalocal.data.model.SalesReport
import com.garcia.cosechalocal.data.model.UserProfile
import com.garcia.cosechalocal.data.repository.AdminRepository
import com.garcia.cosechalocal.data.repository.AuthRepository
import com.garcia.cosechalocal.data.repository.UserRepository
import com.garcia.cosechalocal.ui.common.UserRole
import com.google.firebase.firestore.ListenerRegistration

class AdminViewModel : ViewModel() {

    private val _users = MutableLiveData<List<UserProfile>>(emptyList())
    private val _usersLoading = MutableLiveData(true)
    private val _usersError = MutableLiveData<Exception?>(null)
    private var usersRegistration: ListenerRegistration? = null

    private val _products = MutableLiveData(AdminRepository.getProducts())
    private val _query = MutableLiveData("")
    private val _roleFilter = MutableLiveData<UserRole?>(null)
    private val _statusFilter = MutableLiveData<ModerationStatus?>(null)
    private val _period = MutableLiveData(SalesPeriod.MES)

    val query: String get() = _query.value.orEmpty()
    val roleFilter: UserRole? get() = _roleFilter.value
    val statusFilter: ModerationStatus? get() = _statusFilter.value
    val period: SalesPeriod get() = _period.value ?: SalesPeriod.MES

    val users: LiveData<List<UserProfile>> = _users
    val usersLoading: LiveData<Boolean> = _usersLoading
    val usersError: LiveData<Exception?> = _usersError

    init {
        usersRegistration = UserRepository.observeUsers(
            onChange = { list ->
                _users.value = list
                _usersLoading.value = false
                _usersError.value = null
            },
            onError = { error ->
                _usersLoading.value = false
                _usersError.value = error
            }
        )
    }

    override fun onCleared() {
        usersRegistration?.remove()
        super.onCleared()
    }

    val filteredUsers: LiveData<List<UserProfile>> = MediatorLiveData<List<UserProfile>>().apply {
        fun update() {
            val q = query.trim()
            val role = roleFilter
            value = _users.value.orEmpty().filter {
                (role == null || it.role == role) &&
                    (q.isEmpty() || it.name.contains(q, true) || it.email.contains(q, true))
            }
        }
        addSource(_users) { update() }
        addSource(_query) { update() }
        addSource(_roleFilter) { update() }
    }

    val filteredProducts: LiveData<List<ProductModeration>> = MediatorLiveData<List<ProductModeration>>().apply {
        fun update() {
            val s = statusFilter
            value = _products.value.orEmpty().filter { s == null || it.status == s }
        }
        addSource(_products) { update() }
        addSource(_statusFilter) { update() }
    }

    val pendingApprovals: LiveData<Int> = _products.map { list ->
        list.count { it.status == ModerationStatus.POR_APROBAR }
    }

    val report: LiveData<SalesReport> = _period.map { AdminRepository.getReport(it) }

    fun setQuery(text: String) { _query.value = text }
    fun setRoleFilter(role: UserRole?) { _roleFilter.value = role }
    fun setStatusFilter(status: ModerationStatus?) { _statusFilter.value = status }
    fun setPeriod(period: SalesPeriod) { _period.value = period }
    fun clearUsersError() { _usersError.value = null }

    fun isSelf(uid: String): Boolean = uid == AuthRepository.currentUser?.uid

    fun getUser(uid: String?): UserProfile? =
        _users.value.orEmpty().firstOrNull { it.uid == uid }

    fun emailExists(email: String, excludeUid: String?): Boolean =
        _users.value.orEmpty().any { it.uid != excludeUid && it.email.equals(email, true) }

    fun createUser(
        name: String,
        email: String,
        role: UserRole,
        active: Boolean,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) = UserRepository.createUser(name, email, role, active, onSuccess, onError)

    fun updateUser(
        uid: String,
        name: String,
        role: UserRole,
        active: Boolean,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) = UserRepository.updateUser(uid, name, role, active, onSuccess, onError)

    fun setUserActive(
        uid: String,
        active: Boolean,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) = UserRepository.setActive(uid, active, onSuccess, onError)

    fun deleteUser(
        uid: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) = UserRepository.deleteUser(uid, onSuccess, onError)

    fun setProductStatus(productId: String, status: ModerationStatus) {
        _products.value = _products.value.orEmpty().map {
            if (it.product.id == productId) it.copy(status = status) else it
        }
    }

    fun deleteProduct(productId: String) {
        _products.value = _products.value.orEmpty().filterNot { it.product.id == productId }
    }
}
