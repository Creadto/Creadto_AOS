package com.creadto.creadto_aos.convert

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.creadto.creadto_aos.databinding.ItemLayoutConvertBinding

class ConvertAdapter(private val files : MutableList<String>) : RecyclerView.Adapter<ConvertAdapter.ViewHolder>() {
    private lateinit var listener: FileClickListener

    inner class ViewHolder(val itemBinding : ItemLayoutConvertBinding, val listener: FileClickListener)
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConvertAdapter.ViewHolder {
        val v = ItemLayoutConvertBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(v, listener)
    }

    override fun onBindViewHolder(holder: ConvertAdapter.ViewHolder, position: Int) = holder.bind(files[position])

    override fun getItemCount(): Int = files.size

    fun update(file : List<String>) {
        files.clear()
        files.addAll(file)
        notifyDataSetChanged()
    }

    interface FileClickListener {
        fun onItemClickListener(file : String)
    }

    fun setOnItemClickListener(listener: FileClickListener) {
        this.listener = listener
    }
}