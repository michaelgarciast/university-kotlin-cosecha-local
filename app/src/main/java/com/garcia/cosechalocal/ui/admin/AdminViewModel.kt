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
import com.garcia.cosechalocal.data.model.User
import com.garcia.cosechalocal.data.repository.AdminRepository
import com.garcia.cosechalocal.ui.common.UserRole

class AdminViewModel : ViewModel() {

    private val _users = MutableLiveData(AdminRepository.getUsers())
    private val _products = MutableLiveData(AdminRepository.getProducts())
    private val _query = MutableLiveData("")
    private val _roleFilter = MutableLiveData<UserRole?>(null)
    private val _statusFilter = MutableLiveData<ModerationStatus?>(null)
    private val _period = MutableLiveData(SalesPeriod.MES)
    private var nextUserId = 100

    val query: String get() = _query.value.orEmpty()
    val roleFilter: UserRole? get() = _roleFilter.value
    val statusFilter: ModerationStatus? get() = _statusFilter.value
    val period: SalesPeriod get() = _period.value ?: SalesPeriod.MES

    val filteredUsers: LiveData<List<User>> = MediatorLiveData<List<User>>().apply {
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

    fun getUser(id: Int): User? = _users.value.orEmpty().firstOrNull { it.id == id }

    fun emailExists(email: String, excludeId: Int?): Boolean =
        _users.value.orEmpty().any { it.id != excludeId && it.email.equals(email, true) }

    fun createUser(name: String, email: String, role: UserRole, active: Boolean) {
        _users.value = _users.value.orEmpty() + User(nextUserId++, name, email, role, active)
    }

    fun updateUser(user: User) {
        _users.value = _users.value.orEmpty().map { if (it.id == user.id) user else it }
    }

    fun toggleBlock(id: Int) {
        _users.value = _users.value.orEmpty().map { if (it.id == id) it.copy(active = !it.active) else it }
    }

    fun deleteUser(id: Int) {
        _users.value = _users.value.orEmpty().filterNot { it.id == id }
    }

    fun setProductStatus(productId: String, status: ModerationStatus) {
        _products.value = _products.value.orEmpty().map {
            if (it.product.id == productId) it.copy(status = status) else it
        }
    }

    fun deleteProduct(productId: String) {
        _products.value = _products.value.orEmpty().filterNot { it.product.id == productId }
    }
}
