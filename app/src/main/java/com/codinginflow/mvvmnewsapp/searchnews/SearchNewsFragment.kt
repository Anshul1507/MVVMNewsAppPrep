package com.codinginflow.mvvmnewsapp.searchnews

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.codinginflow.mvvmnewsapp.R
import com.codinginflow.mvvmnewsapp.databinding.FragmentSearchNewsBinding
import dagger.hilt.android.AndroidEntryPoint

// TODO: 18.01.2021 Implement swipe-to-refresh so that we can get unstuck after loading searches while offline

@AndroidEntryPoint
class SearchNewsFragment : Fragment(R.layout.fragment_search_news) {
    private val viewModel: SearchNewsViewModel by viewModels()

    private lateinit var newsPagingAdapter: NewsPagingAdapter

    private var _binding: FragmentSearchNewsBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentSearchNewsBinding.bind(view)

        newsPagingAdapter = NewsPagingAdapter(
            onItemClick = { article ->
                val uri = Uri.parse(article.url)
                val intent = Intent(Intent.ACTION_VIEW, uri)
                binding.root.context.startActivity(intent)
            },
            onBookmarkClick = { article ->
                viewModel.onBookmarkClick(article)
            }
        )

        binding.apply {
            recyclerView.apply {
                setHasFixedSize(true)
                adapter = newsPagingAdapter.withLoadStateHeaderAndFooter(
                    header = NewsLoadStateAdapter(newsPagingAdapter::retry),
                    footer = NewsLoadStateAdapter(newsPagingAdapter::retry)
                )
                layoutManager = LinearLayoutManager(requireContext())
                itemAnimator?.changeDuration = 0
            }

            viewModel.newsArticles.observe(viewLifecycleOwner) { result ->
                newsPagingAdapter.submitData(viewLifecycleOwner.lifecycle, result)
            }
        }
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

        inflater.inflate(R.menu.menu_search_news, menu)

        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem?.actionView as SearchView

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrBlank()) {
                    viewModel.searchArticles(query)
                    searchView.clearFocus()
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })
    }

    fun scrollUp() {
        binding.recyclerView.scrollToPosition(0) // TODO: 16.01.2021 This doesn't scroll all the way up if we are far enough down
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
