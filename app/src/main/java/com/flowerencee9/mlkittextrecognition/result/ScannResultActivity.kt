package com.flowerencee9.mlkittextrecognition.result

import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Menu
import android.view.MenuItem
import android.view.Window
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.flowerencee9.mlkittextrecognition.R
import com.flowerencee9.mlkittextrecognition.databinding.ActivityScannResultBinding
import com.flowerencee9.mlkittextrecognition.databinding.LayoutStoredListDataBinding
import com.flowerencee9.mlkittextrecognition.support.showLoadingDialog
import kotlinx.coroutines.launch

class ScannResultActivity : AppCompatActivity() {
    private lateinit var binding: ActivityScannResultBinding
    private lateinit var viewModel: ScannResultViewModel
    private lateinit var storedDialog: Dialog
    private lateinit var loadingDialog: Dialog

    private val resultText: String? by lazy {
        intent.getStringExtra(EXTRA_RESULT)
    }

    private val adapterStoredText: AdapterStoredText by lazy {
        AdapterStoredText { value ->
            storedDialog.dismiss()
            replaceValue(value)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScannResultBinding.inflate(layoutInflater)
        viewModel = ViewModelProvider(this)[ScannResultViewModel::class.java]
        setContentView(binding.root)
        setupView()
        dataObserver()
    }

    private fun dataObserver() {
        lifecycleScope.launch {
            viewModel.retrieveStoredText()
        }
        viewModel.storedText.observe(this) {
            adapterStoredText.setData(it)
        }
        viewModel.success.observe(this) {
            Toast.makeText(this, "saving text success $it", Toast.LENGTH_SHORT).show()
            viewModel.retrieveStoredText()
        }
        viewModel.progress.observe(this) {
            if (it && !loadingDialog.isShowing) loadingDialog.show()
            else {
                Handler(Looper.getMainLooper()).postDelayed({
                    loadingDialog.dismiss()
                }, 500)
            }
        }
    }

    private fun setupView() {
        with(binding) {
            loadingDialog = showLoadingDialog(root)
            etResult.apply {
                resultText?.let { setText(it) }
            }
        }
        setupDialog()
    }

    private fun replaceValue(text: String) {
        binding.etResult.setText(text)
    }

    private fun setupDialog() {
        storedDialog = Dialog(this, R.style.DialogSlideAnimFullWidth)
        val popupBinding = LayoutStoredListDataBinding.bind(
            layoutInflater.inflate(
                R.layout.layout_stored_list_data,
                binding.root,
                false
            )
        )
        storedDialog.apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setContentView(popupBinding.root)
            setCancelable(false)
        }
        with(popupBinding) {
            btnClose.setOnClickListener { storedDialog.dismiss() }
            rvPopup.apply {
                adapter = adapterStoredText
                layoutManager = LinearLayoutManager(this@ScannResultActivity)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.result_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_copy -> {
                copyAllText()
                true
            }
            R.id.menu_paste -> {
                val textOnKeyboard = getSavedText()
                if (textOnKeyboard != null) {
                    binding.etResult.apply {
                        setText(textOnKeyboard)
                    }
                } else Toast.makeText(this, "No Text To Be Pasted", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.menu_sotred -> {
                if (!isFinishing) storedDialog.show()
                true
            }
            else -> false
        }
    }

    private fun getSavedText(): String? {
        val clipboard: ClipboardManager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        return try {
            clipboard.primaryClip?.getItemAt(0)?.text.toString()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun copyAllText() {
        val clipboard =
            this.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Copied Text", binding.etResult.text.toString())
        clipboard.setPrimaryClip(clip)

        Toast.makeText(this, "Copied", Toast.LENGTH_SHORT).show()
    }

    companion object {
        private val TAG = ScannResultActivity::class.java.simpleName
        private const val EXTRA_RESULT = "EXTRA_RESULT"
        fun myIntent(context: Context, result: String) =
            Intent(context, ScannResultActivity::class.java).apply {
                putExtra(EXTRA_RESULT, result)
            }
    }
}