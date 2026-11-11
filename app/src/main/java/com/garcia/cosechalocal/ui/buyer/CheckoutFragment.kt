package com.garcia.cosechalocal.ui.buyer

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.garcia.cosechalocal.R
import com.garcia.cosechalocal.databinding.FragmentCheckoutBinding
import com.garcia.cosechalocal.data.repository.AuthRepository
import com.garcia.cosechalocal.data.repository.OrderRepository
import com.garcia.cosechalocal.utils.FormatUtils
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class CheckoutFragment : Fragment() {

    private var _binding: FragmentCheckoutBinding? = null
    private val binding get() = _binding!!

    private val cartViewModel: CartViewModel by activityViewModels()

    private var capturedLocation: Location? = null
    private var geocodeThread: Thread? = null
    private val geocodeHandler = Handler(Looper.getMainLooper())
    private val geocodeRunnable = Runnable { geocodeAddress() }

    private val fusedLocationClient by lazy {
        LocationServices.getFusedLocationProviderClient(requireActivity())
    }

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) fetchLocation()
        else Toast.makeText(requireContext(), R.string.location_permission_denied, Toast.LENGTH_LONG).show()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCheckoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupTotals()
        cartViewModel.connect()
        setupListeners()
    }

    private fun setupTotals() {
        cartViewModel.subtotal.observe(viewLifecycleOwner) { subtotal ->
            binding.tvSubtotal.text = FormatUtils.formatPrice(subtotal)
        }

        cartViewModel.total.observe(viewLifecycleOwner) { total ->
            binding.tvTotal.text = FormatUtils.formatPrice(total)
        }
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnUseLocation.setOnClickListener {
            if (hasLocationPermission()) fetchLocation()
            else locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        binding.btnPay.setOnClickListener {
            processPayment()
        }

        binding.etAddress.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
            override fun afterTextChanged(s: Editable?) {
                geocodeHandler.removeCallbacks(geocodeRunnable)
                val text = s?.toString()?.trim().orEmpty()
                if (text.length >= 8) geocodeHandler.postDelayed(geocodeRunnable, 800)
            }
        })
    }

    private fun geocodeAddress() {
        val address = binding.etAddress.text?.toString()?.trim().orEmpty()
        if (address.length < 8 || !Geocoder.isPresent()) return
        geocodeThread?.interrupt()
        geocodeThread = Thread {
            val point = try {
                Geocoder(requireContext()).getFromLocationName(address, 1)?.firstOrNull()
            } catch (_: Exception) {
                null
            }
            view?.post {
                if (_binding == null || point == null) return@post
                capturedLocation = Location("").apply {
                    latitude = point.latitude
                    longitude = point.longitude
                }
                binding.tvLocationStatus.visibility = View.VISIBLE
                binding.tvLocationStatus.text = getString(
                    R.string.address_located, point.latitude, point.longitude
                )
            }
        }.also { it.start() }
    }

    private fun hasLocationPermission(): Boolean =
        ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED

    private fun fetchLocation() {
        fusedLocationClient
            .getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, CancellationTokenSource().token)
            .addOnSuccessListener { location ->
                if (_binding == null) return@addOnSuccessListener
                if (location != null) {
                    capturedLocation = location
                    binding.tvLocationStatus.visibility = View.VISIBLE
                    binding.tvLocationStatus.text = getString(
                        R.string.location_captured, location.latitude, location.longitude
                    )
                } else {
                    Toast.makeText(requireContext(), R.string.location_unavailable, Toast.LENGTH_LONG).show()
                }
            }
            .addOnFailureListener {
                if (_binding != null) {
                    Toast.makeText(requireContext(), R.string.location_unavailable, Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun processPayment() {
        val address = binding.etAddress.text?.toString()?.trim().orEmpty()
        if (address.isEmpty()) {
            binding.tilAddress.error = getString(R.string.delivery_address_hint)
            return
        } else {
            binding.tilAddress.error = null
        }

        val uid = AuthRepository.currentUser?.uid ?: return
        val location = capturedLocation?.let {
            mapOf("latitude" to it.latitude, "longitude" to it.longitude)
        }
        binding.btnPay.isEnabled = false
        OrderRepository.checkout(uid, address, location, { id ->
            if (_binding != null) {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.order_created_title)
                    .setMessage(getString(R.string.order_created_message, id))
                    .setPositiveButton(R.string.btn_accept) { _, _ ->
                        findNavController().navigate(R.id.navigation_buyer_orders)
                    }.show()
            }
        }, { error ->
            if (_binding != null) {
                binding.btnPay.isEnabled = true
                Toast.makeText(requireContext(), error.localizedMessage, Toast.LENGTH_LONG).show()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        geocodeHandler.removeCallbacks(geocodeRunnable)
        geocodeThread?.interrupt()
        _binding = null
    }
}
