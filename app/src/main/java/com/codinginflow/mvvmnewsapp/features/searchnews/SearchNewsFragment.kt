package com.codinginflow.mvvmnewsapp.features.searchnews

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.codinginflow.mvvmnewsapp.MainActivity
import com.codinginflow.mvvmnewsapp.R
import com.codinginflow.mvvmnewsapp.databinding.FragmentSearchNewsBinding
import com.codinginflow.mvvmnewsapp.util.onQueryTextSubmit
import com.codinginflow.mvvmnewsapp.util.showSnackbar
import com.codinginflow.mvvmnewsapp.util.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filter
import timber.log.Timber

@AndroidEntryPoint
class SearchNewsFragment : Fragment(R.layout.fragment_search_news),
    MainActivity.OnBottomNavigationFragmentReselected {
    private val viewModel: SearchNewsViewModel by viewModels()

    private lateinit var newsPagingAdapter: NewsPagingAdapter

    private val binding by viewBinding(FragmentSearchNewsBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
                itemAnimator = null // we don't need animations and this gets us rid of ugly DiffUtil changes
//                itemAnimator?.changeDuration = 0 // get rid of bookmark click flash
            }

            recyclerView.isVisible = false
            swipeRefreshLayout.isEnabled = false

            viewModel.newsArticles.observe(viewLifecycleOwner) { result ->
                Timber.d("SEARCH: observe with result $result")
                textViewInstructions.isVisible = false
                swipeRefreshLayout.isEnabled = true
                newsPagingAdapter.submitData(viewLifecycleOwner.lifecycle, result)
            }

            swipeRefreshLayout.setOnRefreshListener {
                // TODO: 26.01.2021 Not yet sure yet if retry refresh and refresh equivalent
                newsPagingAdapter.refresh()
            }

            newsPagingAdapter.addLoadStateListener { loadState ->

                when (val mediatorRefresh = loadState.mediator?.refresh) {
                    is LoadState.Loading -> {
//                        Timber.d("refresh = LoadState.Loading")
                        viewModel.refreshInProgress = true
                        recyclerView.isVisible = !viewModel.newQueryInProgress
                    }
                    is LoadState.NotLoading -> {
                        recyclerView.isVisible = newsPagingAdapter.itemCount > 0
                        if (viewModel.refreshInProgress) {
//                            Timber.d("mediator.refresh = NotLoading -> scroll to 0")
                            recyclerView.scrollToPosition(0)
                            viewModel.refreshInProgress = false
                            viewModel.newQueryInProgress = false
                            viewModel.pendingScrollToTopAfterRefresh = true
                        }
                    }
                    is LoadState.Error -> {
                        recyclerView.isVisible = newsPagingAdapter.itemCount > 0
//                        Timber.d("refresh = LoadState.Error")
                        val errorMessage =
                            "Could not load search results:\n${mediatorRefresh.error.localizedMessage ?: "An unknown error occurred"}"
                        textViewError.text = errorMessage
                        if (viewModel.refreshInProgress) {
                            viewModel.refreshInProgress = false
                            viewModel.newQueryInProgress = false
                            showSnackbar(errorMessage)
                        }
                    }
                }

//                Timber.d("loadState.refresh is ${loadState.refresh}")
                swipeRefreshLayout.isRefreshing = loadState.refresh is LoadState.Loading
                val showErrorViews =
                    loadState.refresh is LoadState.Error && loadState.source.refresh is LoadState.NotLoading && newsPagingAdapter.itemCount < 1
                buttonRetry.isVisible = showErrorViews
                textViewError.isVisible = showErrorViews

                textViewNoResults.isVisible = loadState.refresh is LoadState.NotLoading &&
                        loadState.refresh.endOfPaginationReached &&
                        newsPagingAdapter.itemCount < 1
            }

            newsPagingAdapter.registerAdapterDataObserver(object :
                RecyclerView.AdapterDataObserver() {

                override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
                    if (viewModel.pendingScrollToTopAfterRefresh) {
                        Timber.d("SCROLL UP pendingRefreshDiffing Changed")
                        recyclerView.scrollToPosition(0)
                    }
                }

                override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                    if (viewModel.pendingScrollToTopAfterRefresh) {
                        Timber.d("SCROLL UP pendingRefreshDiffing Inserted")
                        recyclerView.scrollToPosition(0)
                    }
                }
            })

            recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                        Timber.d("resetting pendingRefreshDiffing because of scroll")
                        viewModel.pendingScrollToTopAfterRefresh = false
                    }
                }
            })

            buttonRetry.setOnClickListener {
                newsPagingAdapter.retry()
            }
        }
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_search_news, menu)

        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem?.actionView as SearchView

        searchView.onQueryTextSubmit { query ->
            // make cached data invisible because we will jump back to the top after refresh finished
            // PagingData.empty() avoids that the old list flashes up for a moment if we are offline
            newsPagingAdapter.submitData(viewLifecycleOwner.lifecycle, PagingData.empty())
//            binding.recyclerView.scrollToPosition(0) // shouldn't be necessary because a new search triggers refresh
            viewModel.searchArticles(query)
            searchView.clearFocus()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.refresh -> {
                // clicking retry on the footer after this failed causes it to retry refresh. I reported
                // this to dlam and he said they will probably provide an argument in the future
                newsPagingAdapter.refresh()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBottomNavigationFragmentReselected() {
        binding.recyclerView.scrollToPosition(0)
    }
}
