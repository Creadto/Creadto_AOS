package com.creadto.creadto_aos.gallery

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.creadto.creadto_aos.databinding.ItemLayoutDetailBinding

class DetailAdapter(private val files : MutableList<String>) : RecyclerView.Adapter<DetailAdapter.ViewHolder>() {

    private lateinit var listener: GalleryAdapter.FileClickListener

    inner class ViewHolder(val itemBinding : ItemLayoutDetailBinding, val listener: GalleryAdapter.FileClickListener)
        : RecyclerView.ViewHolder(itemBinding.root) {
        fun bind(file : String) {
            val pos = adapterPosition
            if(pos != RecyclerView.NO_POSITION) {
                itemBinding.tvTitle.text = file

                itemView.setOnClickListener {
                    listener?.onItemClickListener(file)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetailAdapter.ViewHolder {
        val v = ItemLayoutDetailBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(v, listener)
    }

    override fun onBindViewHolder(holder: DetailAdapter.ViewHolder, position: Int) = holder.bind(files[position])

    override fun getItemCount(): Int = files.size

    fun update(file : List<String>) {
        files.clear()
        files.addAll(file)
        notifyDataSetChanged()
    }

    fun setOnItemClickListener(listener: GalleryAdapter.FileClickListener) {
        this.listener = listener
    }
}