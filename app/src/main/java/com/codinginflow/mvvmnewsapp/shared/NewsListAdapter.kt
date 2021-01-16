package com.codinginflow.mvvmnewsapp.shared

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.codinginflow.mvvmnewsapp.R
import com.codinginflow.mvvmnewsapp.data.NewsArticle
import com.codinginflow.mvvmnewsapp.databinding.ItemNewsArticleBinding

class NewsListAdapter(private val onBookmarkClick: (NewsArticle) -> Unit) :
    ListAdapter<NewsArticle, NewsListAdapter.NewsViewHolder>(ARTICLE_COMPARATOR) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val binding =
            ItemNewsArticleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NewsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.bind(currentItem)
    }

    inner class NewsViewHolder(private val binding: ItemNewsArticleBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(article: NewsArticle) {
            binding.apply {
                Glide.with(itemView)
                    .load(article.urlToImage)
                    .error(R.drawable.image_placeholder)
                    .into(imageView)

                textViewTitle.text = article.title

                if (article.isBookmarked) {
                    imageViewBookmark.setImageResource(R.drawable.ic_bookmark_selected)
                } else {
                    imageViewBookmark.setImageResource(R.drawable.ic_bookmark_unselected)
                }
            }
        }

        init {
            binding.apply {
                root.setOnClickListener {
                    // TODO: 14.01.2021 I let the adapter open the browser to avoid duplication.
                    //  But this is beyond the responsibility of the adapter
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        val article = getItem(position)
                        val uri = Uri.parse(article.url)
                        val intent = Intent(Intent.ACTION_VIEW, uri)
                        binding.root.context.startActivity(intent)
                    }
                }
                imageViewBookmark.setOnClickListener {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        val article = getItem(position)
                        onBookmarkClick(article)
                    }
                }
            }
        }
    }

    companion object {
        private val ARTICLE_COMPARATOR =
            object : DiffUtil.ItemCallback<NewsArticle>() {
                override fun areItemsTheSame(
                    oldItem: NewsArticle,
                    newItem: NewsArticle
                ) = oldItem.url == newItem.url // TODO: 12.01.2021 better identifier?

                override fun areContentsTheSame(
                    oldItem: NewsArticle,
                    newItem: NewsArticle
                ) = oldItem == newItem
            }
    }
}