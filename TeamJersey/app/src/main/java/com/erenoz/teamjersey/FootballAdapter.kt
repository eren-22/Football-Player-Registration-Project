package com.erenoz.teamjersey

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.erenoz.teamjersey.databinding.RecyclerRowBinding

class FootballAdapter(val footballList : ArrayList<Football>) : RecyclerView.Adapter<FootballAdapter.FootballHolder>() {

    class FootballHolder(val binding : RecyclerRowBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FootballHolder {
        val binding = RecyclerRowBinding.inflate(LayoutInflater.from(parent.context) , parent , false)
        return FootballHolder(binding)
    }

    override fun onBindViewHolder(holder: FootballHolder, position: Int) {
        holder.binding.recyclerViewTextView.text = footballList.get(position).name
        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context , JerseyActivity::class.java)
            intent.putExtra("info" , "old")  //kayıtlı bir futbolcuyu göstermek istiyor.
            intent.putExtra("id" , footballList.get(position).id)
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return footballList.size
    }
}