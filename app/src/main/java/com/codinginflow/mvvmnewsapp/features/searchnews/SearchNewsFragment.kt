package com.codinginflow.mvvmnewsapp.features.searchnews

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.codinginflow.mvvmnewsapp.MainActivity
import com.codinginflow.mvvmnewsapp.R
import com.codinginflow.mvvmnewsapp.databinding.FragmentSearchNewsBinding
import com.codinginflow.mvvmnewsapp.util.onQueryTextSubmit
import com.codinginflow.mvvmnewsapp.util.showSnackbar
import com.codinginflow.mvvmnewsapp.util.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.distinctUntilChangedBy
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
                itemAnimator?.changeDuration = 0 // get rid of bookmark click flash
            }

            viewModel.newsArticles.observe(viewLifecycleOwner) { result ->
                newsPagingAdapter.submitData(viewLifecycleOwner.lifecycle, result)
            }

            // TODO: 20.01.2021 Disable swipe refresh if we don't have a query running yet
            swipeRefreshLayout.setOnRefreshListener {
                viewModel.onManualRefresh()
            }

            // TODO: 19.01.2021 This is not right yet. I have to play around until this is correct
            newsPagingAdapter.addLoadStateListener { loadState ->
                when (loadState.mediator?.refresh) {
                    is LoadState.NotLoading -> {
                        Timber.d("mediator refresh NotLoading, endOfPaginationReached = ${loadState.mediator?.refresh?.endOfPaginationReached}")
                    }
                    is LoadState.Loading -> {
                        Timber.d("mediator refresh Loading, endOfPaginationReached = ${loadState.mediator?.refresh?.endOfPaginationReached}")
                    }
                    is LoadState.Error -> {
                        Timber.d("mediator refresh Error, endOfPaginationReached = ${loadState.mediator?.refresh?.endOfPaginationReached}")
                    }
                }

                when (loadState.source.refresh) {
                    is LoadState.NotLoading -> {
                        Timber.d("source refresh NotLoading, endOfPaginationReached = ${loadState.source.refresh.endOfPaginationReached}")
                    }
                    is LoadState.Loading -> {
                        Timber.d("source refresh Loading, endOfPaginationReached = ${loadState.source.refresh.endOfPaginationReached}")
                    }
                    is LoadState.Error -> {
                        Timber.d("source refresh Error, endOfPaginationReached = ${loadState.source.refresh.endOfPaginationReached}")
                    }
                }

                when (loadState.mediator?.append) {
                    is LoadState.NotLoading -> {
                        Timber.d("mediator append NotLoading, endOfPaginationReached = ${loadState.mediator?.append?.endOfPaginationReached}")
                    }
                    is LoadState.Loading -> {
                        Timber.d("mediator append Loading, endOfPaginationReached = ${loadState.mediator?.append?.endOfPaginationReached}")
                    }
                    is LoadState.Error -> {
                        Timber.d("mediator append Error, endOfPaginationReached = ${loadState.mediator?.append?.endOfPaginationReached}")
                    }
                }

                when (loadState.source.append) {
                    is LoadState.NotLoading -> {
                        Timber.d("source append NotLoading, endOfPaginationReached = ${loadState.source.append.endOfPaginationReached}")
                    }
                    is LoadState.Loading -> {
                        Timber.d("source append Loading, endOfPaginationReached = ${loadState.source.append.endOfPaginationReached}")
                    }
                    is LoadState.Error -> {
                        Timber.d("source append Error, endOfPaginationReached = ${loadState.source.append.endOfPaginationReached}")
                    }
                }

                when (loadState.mediator?.prepend) {
                    is LoadState.NotLoading -> {
                        Timber.d("mediator prepend NotLoading, endOfPaginationReached = ${loadState.mediator?.prepend?.endOfPaginationReached}")
                    }
                    is LoadState.Loading -> {
                        Timber.d("mediator prepend Loading, endOfPaginationReached = ${loadState.mediator?.prepend?.endOfPaginationReached}")
                    }
                    is LoadState.Error -> {
                        Timber.d("mediator prepend Error, endOfPaginationReached = ${loadState.mediator?.prepend?.endOfPaginationReached}")
                    }
                }

                when (loadState.source.prepend) {
                    is LoadState.NotLoading -> {
                        Timber.d("source prepend NotLoading, endOfPaginationReached = ${loadState.source.append.endOfPaginationReached}")
                    }
                    is LoadState.Loading -> {
                        Timber.d("source prepend Loading, endOfPaginationReached = ${loadState.source.append.endOfPaginationReached}")
                    }
                    is LoadState.Error -> {
                        Timber.d("source prepend Error, endOfPaginationReached = ${loadState.source.append.endOfPaginationReached}")
                    }
                }

                swipeRefreshLayout.isRefreshing = loadState.refresh is LoadState.Loading
                buttonRetry.isVisible =
                    loadState.refresh is LoadState.Error && loadState.source.refresh is LoadState.NotLoading && newsPagingAdapter.itemCount < 1
                textViewError.isVisible =
                    loadState.refresh is LoadState.Error && loadState.source.refresh is LoadState.NotLoading && newsPagingAdapter.itemCount < 1



                if (loadState.source.refresh is LoadState.NotLoading &&
                    loadState.append.endOfPaginationReached &&
                    newsPagingAdapter.itemCount < 1
                ) {
                    recyclerView.isVisible = false
                    textViewEmpty.isVisible = true
                } else {
                    textViewEmpty.isVisible = false
                    recyclerView.isVisible = true
                }
            }

            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                newsPagingAdapter.loadStateFlow.distinctUntilChangedBy {
                    it.mediator?.refresh // avoid showing the same error when we click a bookmark
                }.collect { loadState ->
                    val errorState = loadState.mediator?.refresh as? LoadState.Error
                    /*  ?: loadState.append as? LoadState.Error
                      ?: loadState.prepend as? LoadState.Error*/

                    errorState?.let {
                        val errorMessage = it.error.localizedMessage ?: "An unknown error occurred"
                        textViewError.text = errorMessage
                            showSnackbar(errorMessage) // TODO: 19.01.2021 Ideally this would be a one-off event
                    }
                }
            }

            buttonRetry.setOnClickListener {
                newsPagingAdapter.retry()
            }
        }
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

        inflater.inflate(R.menu.menu_search_news, menu)

        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem?.actionView as SearchView

        searchView.onQueryTextSubmit { query ->
            viewModel.searchArticles(query)
            searchView.clearFocus()
        }
    }

    override fun onBottomNavigationFragmentReselected() {
        binding.recyclerView.scrollToPosition(0) // TODO: 16.01.2021 This doesn't scroll all the way up if we are far enough down
    }
}
