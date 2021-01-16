package com.codinginflow.mvvmnewsapp.breakingnews

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.codinginflow.mvvmnewsapp.R
import com.codinginflow.mvvmnewsapp.databinding.FragmentBreakingNewsBinding
import com.codinginflow.mvvmnewsapp.shared.NewsListAdapter
import com.codinginflow.mvvmnewsapp.util.Resource
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import timber.log.Timber

@AndroidEntryPoint
class BreakingNewsFragment : Fragment(R.layout.fragment_breaking_news) {
    private val viewModel: BreakingNewsViewModel by viewModels()

    private lateinit var newsAdapter: NewsListAdapter

    private var _binding: FragmentBreakingNewsBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentBreakingNewsBinding.bind(view)

        newsAdapter = NewsListAdapter(
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
                adapter = newsAdapter
                layoutManager = LinearLayoutManager(requireContext())
                itemAnimator?.changeDuration = 0
            }

            viewModel.breakingNews.observe(viewLifecycleOwner) { result ->
                swipeRefreshLayout.isRefreshing = result is Resource.Loading
                recyclerView.isVisible = !result.data.isNullOrEmpty()
                textViewError.isVisible = result.throwable != null && result.data.isNullOrEmpty()
                textViewError.text = result.throwable?.localizedMessage ?: "An unknown error occurred"

                newsAdapter.submitList(result.data)
            }

            swipeRefreshLayout.setOnRefreshListener {
                viewModel.onManualRefresh()
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.events.collect { event ->
                when (event) {
                    is BreakingNewsViewModel.Event.ShowErrorMessage ->
                        Snackbar.make(
                            requireView(),
                            event.throwable.localizedMessage ?: "An unknown error occurred",
                            Snackbar.LENGTH_LONG
                        ).show()
                }
            }
        }

        setHasOptionsMenu(true)
    }

    fun scrollUpAndRefresh() {
        binding.recyclerView.scrollToPosition(0)
        viewModel.onManualRefresh()
    }

    override fun onStart() {
        super.onStart()
        viewModel.onStart()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}