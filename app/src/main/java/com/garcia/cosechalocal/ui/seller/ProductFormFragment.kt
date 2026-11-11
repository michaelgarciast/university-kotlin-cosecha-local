package com.garcia.cosechalocal.ui.seller

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.garcia.cosechalocal.R
import com.garcia.cosechalocal.data.model.Product
import com.garcia.cosechalocal.databinding.FragmentProductFormBinding
import com.garcia.cosechalocal.utils.ProductImageStore
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

class ProductFormFragment : Fragment() {

    private var _binding: FragmentProductFormBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SellerViewModel by activityViewModels()

    private var editing: Product? = null
    private var imageBytes: ByteArray? = null
    private var imageRemoved = false
    private var cameraUri: Uri? = null

    private val pickImage =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let(::onImageSelected)
        }

    private val takePicture =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { captured ->
            val uri = cameraUri
            if (captured && uri != null) onImageSelected(uri)
            cameraUri = null
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProductFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val categories = resources.getStringArray(R.array.product_categories)
        binding.actvCategory.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, categories))

        val productId = arguments?.getString("productId")
        if (productId == null) {
            binding.tvFormTitle.setText(R.string.form_title_new)
        } else {
            binding.tvFormTitle.setText(R.string.form_title_edit)
            loadEditingProduct(productId, fillForm = savedInstanceState == null)
        }

        binding.btnBack.setOnClickListener { findNavController().navigateUp() }
        binding.btnTakePhoto.setOnClickListener { launchCamera() }
        binding.btnPickImage.setOnClickListener { pickImage.launch("image/*") }
        binding.btnRemovePhoto.setOnClickListener { removeImage() }
        binding.btnUseLocation.setOnClickListener {
            Snackbar.make(binding.root, R.string.location_simulated, Snackbar.LENGTH_SHORT).show()
        }
        binding.btnSave.setOnClickListener { save() }
    }

    private fun loadEditingProduct(productId: String, fillForm: Boolean) {
        setFormEnabled(false)
        viewModel.loadProduct(
            productId = productId,
            onResult = { product ->
                if (!isAdded) return@loadProduct
                if (product == null) {
                    Snackbar.make(binding.root, R.string.error_product_not_found, Snackbar.LENGTH_LONG).show()
                    findNavController().navigateUp()
                    return@loadProduct
                }
                editing = product
                if (fillForm) fillForm(product)
                setFormEnabled(true)
            },
            onError = {
                if (!isAdded) return@loadProduct
                Snackbar.make(binding.root, R.string.error_product_load, Snackbar.LENGTH_LONG).show()
                findNavController().navigateUp()
            }
        )
    }

    private fun fillForm(product: Product) {
        binding.etName.setText(product.name)
        binding.etPrice.setText(product.price.toString())
        binding.etStock.setText(product.stock.toString())
        binding.actvCategory.setText(product.category, false)
        binding.etDescription.setText(product.description)
        if (product.hasLocalImage) {
            ProductImageStore.bind(binding.ivFormImage, product.id, true)
            binding.btnRemovePhoto.isVisible = true
        }
    }

    private fun launchCamera() {
        val file = File(requireContext().cacheDir, "camera/${UUID.randomUUID()}.jpg")
            .apply { parentFile?.mkdirs() }
        val uri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.fileprovider",
            file
        )
        cameraUri = uri
        takePicture.launch(uri)
    }

    private fun onImageSelected(uri: Uri) {
        val context = context ?: return
        viewLifecycleOwner.lifecycleScope.launch {
            val bytes = withContext(Dispatchers.IO) {
                val compressed = ProductImageStore.compress(context, uri)
                // La foto de cámara vive en caché solo hasta comprimirla.
                if (uri.scheme == "file") File(uri.path.orEmpty()).delete()
                compressed
            }
            if (!isAdded) return@launch
            if (bytes == null) {
                Snackbar.make(binding.root, R.string.error_image_processing, Snackbar.LENGTH_SHORT).show()
                return@launch
            }
            imageBytes = bytes
            imageRemoved = false
            ProductImageStore.decode(bytes)?.let { ProductImageStore.showBitmap(binding.ivFormImage, it) }
            binding.btnRemovePhoto.isVisible = true
        }
    }

    private fun removeImage() {
        imageBytes = null
        imageRemoved = true
        ProductImageStore.showPlaceholder(binding.ivFormImage)
        binding.btnRemovePhoto.isVisible = false
    }

    private fun save() {
        val name = binding.etName.text.toString().trim()
        val price = binding.etPrice.text.toString().trim().toIntOrNull()
        val stock = binding.etStock.text.toString().trim().toIntOrNull()
        val category = binding.actvCategory.text.toString().trim()

        binding.tilName.error = if (name.isEmpty()) getString(R.string.error_name_required) else null
        binding.tilPrice.error = if (price == null || price <= 0) getString(R.string.error_price_invalid) else null
        binding.tilStock.error = if (stock == null || stock < 0) getString(R.string.error_stock_invalid) else null
        binding.tilCategory.error = if (category.isEmpty()) getString(R.string.error_category_required) else null

        if (listOf(binding.tilName, binding.tilPrice, binding.tilStock, binding.tilCategory).any { it.error != null }) return

        setFormEnabled(false)
        viewModel.saveProduct(
            editing = editing,
            name = name,
            price = price!!,
            stock = stock!!,
            category = category,
            description = binding.etDescription.text.toString().trim(),
            imageBytes = imageBytes,
            imageRemoved = imageRemoved,
            onSaved = {
                if (isAdded) findNavController().navigateUp()
            },
            onError = {
                if (!isAdded) return@saveProduct
                setFormEnabled(true)
                Snackbar.make(binding.root, R.string.error_product_save, Snackbar.LENGTH_LONG).show()
            }
        )
    }

    private fun setFormEnabled(enabled: Boolean) {
        binding.btnSave.isEnabled = enabled
        binding.btnTakePhoto.isEnabled = enabled
        binding.btnPickImage.isEnabled = enabled
        binding.btnUseLocation.isEnabled = enabled
        binding.btnSave.alpha = if (enabled) 1f else 0.6f
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
