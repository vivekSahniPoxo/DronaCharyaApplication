package com.example.lms.BookDetails

import android.text.method.TextKeyListener.clear
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.lms.BookDetails.model.BookDetailsModel
import com.example.lms.databinding.BookDetailsBinding


class BookDetailsAdapter(private val mList: List<BookDetailsModel>) : RecyclerView.Adapter<BookDetailsAdapter.ViewHOlder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHOlder {
        val itemBinding = BookDetailsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHOlder(itemBinding)
    }

    override fun onBindViewHolder(holder: ViewHOlder, position: Int) {
        val items = mList[position]
        holder.bind(items)

    }
    override fun getItemCount(): Int = mList.size
    class ViewHOlder(private val itemBinding: BookDetailsBinding) : RecyclerView.ViewHolder(itemBinding.root) {

        fun bind(items: BookDetailsModel) {
            itemBinding.tvBookTitle.text = items.title
            itemBinding.tvAccessNo.text = items.accessNo
        }
    }



    fun clear() {
        val size: Int = mList.size
       // mList.clear()
        notifyItemRangeRemoved(0, size)
    }

}