package com.sosvital

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sosvital.databinding.ItemContactBinding
import com.sosvital.model.EmergencyContact

class ContactsAdapter(
    private val onDelete: (EmergencyContact) -> Unit
) : RecyclerView.Adapter<ContactsAdapter.VH>() {

    private val items = mutableListOf<EmergencyContact>()

    fun submitList(list: List<EmergencyContact>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemContactBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(items[position])

    override fun getItemCount() = items.size

    inner class VH(private val b: ItemContactBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(contact: EmergencyContact) {
            b.tvName.text = contact.name
            b.tvPhone.text = contact.phone
            b.btnDelete.setOnClickListener { onDelete(contact) }
        }
    }
}
