package com.flowerencee9.mlkittextrecognition.support

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.provider.Settings
import android.view.View
import android.view.ViewGroup
import android.view.Window
import com.flowerencee9.mlkittextrecognition.R
import com.flowerencee9.mlkittextrecognition.databinding.LayoutLoadingBinding
import com.flowerencee9.mlkittextrecognition.databinding.LayoutPopupFailedBinding
import java.text.SimpleDateFormat
import java.util.*

fun getCurrentTime(format: String = "dd-MMM-yyy | HH:mm"): String {
    val calendar = Calendar.getInstance().time
    val value = SimpleDateFormat(format, Locale.getDefault())
    return value.format(calendar)
}

fun View.setVisible(visible: Boolean) {
    when (visible) {
        true -> this.visibility = View.VISIBLE
        false -> this.visibility = View.GONE
    }
}

fun Activity.showPopupAction(title: String, body: String, positiveClick: () -> Unit) {
    val dialog = Dialog(this, R.style.DialogSlideAnimFullWidth)
    val popupBinding = LayoutPopupFailedBinding.bind(
        layoutInflater.inflate(
            R.layout.layout_popup_failed,
            null
        )
    )
    dialog.apply {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setContentView(popupBinding.root)
        setCancelable(false)
    }

    with(popupBinding) {
        lblTitle.text = title
        lblDesc.text = body
        btnCancel.setOnClickListener {
            dialog.dismiss()
        }
        btnOk.setOnClickListener {
            positiveClick()
            dialog.dismiss()
        }
    }
    dialog.show()
}

fun Activity.showLoadingDialog(root: ViewGroup): Dialog {
    val dialog = Dialog(this, R.style.DialogSlideAnimFullWidth)
    val binding = LayoutLoadingBinding.bind(
        layoutInflater.inflate(
            R.layout.layout_loading,
            root,
            false
        )
    )
    dialog.apply {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setContentView(binding.root)
        setCancelable(false)
    }
    return dialog
}

