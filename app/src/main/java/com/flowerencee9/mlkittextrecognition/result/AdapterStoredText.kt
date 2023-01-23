package com.flowerencee9.mlkittextrecognition.result

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.flowerencee9.mlkittextrecognition.R
import com.flowerencee9.mlkittextrecognition.databinding.LayoutStoredItemDataBinding

class AdapterStoredText(
    private val clickListener: (String) -> Unit
) : RecyclerView.Adapter<AdapterStoredText.ViewHolder>() {
    private var storedList = mapOf<String, String>()

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(storedDate: String, value: String) = with(itemView) {
            val binding = LayoutStoredItemDataBinding.bind(itemView)
            val showedItem = "$storedDate - $value"
            binding.tvStoredItem.text = showedItem
            binding.root.setOnClickListener {
                clickListener(value)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(
        (
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.layout_stored_item_data, parent, false)
                )
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val extractMap = storedList.entries.toTypedArray()[position]
        holder.bind(extractMap.key, extractMap.value)
    }

    override fun getItemCount(): Int = storedList.size

    fun setData(list: Map<String, String>) {
        if (list.isEmpty()) return
        storedList = list
    }
}