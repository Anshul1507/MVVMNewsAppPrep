package com.codinginflow.mvvmnewsapp.features.searchnews

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.codinginflow.mvvmnewsapp.MainActivity
import com.codinginflow.mvvmnewsapp.R
import com.codinginflow.mvvmnewsapp.databinding.FragmentSearchNewsBinding
import com.codinginflow.mvvmnewsapp.util.onQueryTextSubmit
import com.codinginflow.mvvmnewsapp.util.showSnackbar
import com.codinginflow.mvvmnewsapp.util.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
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
                itemAnimator?.changeDuration = 0 // get rid of bookmark click flash
            }

            viewModel.newsArticles.observe(viewLifecycleOwner) { result ->
                newsPagingAdapter.submitData(viewLifecycleOwner.lifecycle, result)
            }

            swipeRefreshLayout.setOnRefreshListener {
                // TODO: 23.01.2021 If refresh fails we normally need to call retry, not refresh. Also, not
                //  having a retry button visible somewhere could make the user think we are at the end of
                //  the data (Although we show an error snackbar)
                newsPagingAdapter.refresh()
            }

            viewModel.hasCurrentQuery.observe(viewLifecycleOwner) { hasCurrentQuery ->
                swipeRefreshLayout.isEnabled = hasCurrentQuery
                textViewInstructions.isVisible = !hasCurrentQuery
            }

            // TODO: 19.01.2021 This is not right yet. I have to play around until this is correct
            newsPagingAdapter.addLoadStateListener { loadState ->
                when (loadState.mediator?.refresh) {
                    is LoadState.NotLoading -> {
//                        Timber.d("mediator refresh NotLoading, endOfPaginationReached = ${loadState.mediator?.refresh?.endOfPaginationReached}")
                    }
                    is LoadState.Loading -> {
//                        Timber.d("mediator refresh Loading, endOfPaginationReached = ${loadState.mediator?.refresh?.endOfPaginationReached}")
                    }
                    is LoadState.Error -> {
//                        Timber.d("mediator refresh Error, endOfPaginationReached = ${loadState.mediator?.refresh?.endOfPaginationReached}")
                    }
                }

                when (loadState.source.refresh) {
                    is LoadState.NotLoading -> {
//                        Timber.d("source refresh NotLoading, endOfPaginationReached = ${loadState.source.refresh.endOfPaginationReached}")
                    }
                    is LoadState.Loading -> {
//                        Timber.d("source refresh Loading, endOfPaginationReached = ${loadState.source.refresh.endOfPaginationReached}")
                    }
                    is LoadState.Error -> {
//                        Timber.d("source refresh Error, endOfPaginationReached = ${loadState.source.refresh.endOfPaginationReached}")
                    }
                }

                when (loadState.mediator?.append) {
                    is LoadState.NotLoading -> {
//                        Timber.d("mediator append NotLoading, endOfPaginationReached = ${loadState.mediator?.append?.endOfPaginationReached}")
                    }
                    is LoadState.Loading -> {
//                        Timber.d("mediator append Loading, endOfPaginationReached = ${loadState.mediator?.append?.endOfPaginationReached}")
                    }
                    is LoadState.Error -> {
//                        Timber.d("mediator append Error, endOfPaginationReached = ${loadState.mediator?.append?.endOfPaginationReached}")
                    }
                }

                when (loadState.source.append) {
                    is LoadState.NotLoading -> {
//                        Timber.d("source append NotLoading, endOfPaginationReached = ${loadState.source.append.endOfPaginationReached}")
                    }
                    is LoadState.Loading -> {
//                        Timber.d("source append Loading, endOfPaginationReached = ${loadState.source.append.endOfPaginationReached}")
                    }
                    is LoadState.Error -> {
//                        Timber.d("source append Error, endOfPaginationReached = ${loadState.source.append.endOfPaginationReached}")
                    }
                }

                when (loadState.mediator?.prepend) {
                    is LoadState.NotLoading -> {
//                        Timber.d("mediator prepend NotLoading, endOfPaginationReached = ${loadState.mediator?.prepend?.endOfPaginationReached}")
                    }
                    is LoadState.Loading -> {
//                        Timber.d("mediator prepend Loading, endOfPaginationReached = ${loadState.mediator?.prepend?.endOfPaginationReached}")
                    }
                    is LoadState.Error -> {
//                        Timber.d("mediator prepend Error, endOfPaginationReached = ${loadState.mediator?.prepend?.endOfPaginationReached}")
                    }
                }

                when (loadState.source.prepend) {
                    is LoadState.NotLoading -> {
//                        Timber.d("source prepend NotLoading, endOfPaginationReached = ${loadState.source.append.endOfPaginationReached}")
                    }
                    is LoadState.Loading -> {
//                        Timber.d("source prepend Loading, endOfPaginationReached = ${loadState.source.append.endOfPaginationReached}")
                    }
                    is LoadState.Error -> {
//                        Timber.d("source prepend Error, endOfPaginationReached = ${loadState.source.append.endOfPaginationReached}")
                    }
                }

                when (val mediatorRefresh = loadState.mediator?.refresh) {
                    is LoadState.Loading -> {
//                        Timber.d("refresh = LoadState.Loading")
                        viewModel.refreshInProgress = true
                    }
                    is LoadState.NotLoading -> {
                        if (viewModel.refreshInProgress) {
//                            Timber.d("mediator.refresh = NotLoading -> scroll to 0")
//                            recyclerView.scrollToPosition(0) // this does not work if there are new items at the top
                            viewModel.refreshInProgress = false
                        }
                    }
                    is LoadState.Error -> {
//                        Timber.d("refresh = LoadState.Error")
                        val errorMessage =
                            "Could not refresh:\n${mediatorRefresh.error.localizedMessage ?: "An unknown error occurred"}"
                        textViewError.text = errorMessage
                        if (viewModel.refreshInProgress) {
                            showSnackbar(errorMessage)
                            viewModel.refreshInProgress = false
                        }
                    }
                }

                when (val sourceRefresh = loadState.source.refresh) {
                    is LoadState.NotLoading -> {
                        if (viewModel.refreshInProgress) {
//                            Timber.d("scroll to 0 because of source")
//                            recyclerView.scrollToPosition(0)
                        }
                    }
                }

                recyclerView.isVisible = newsPagingAdapter.itemCount > 0
                swipeRefreshLayout.isRefreshing = loadState.refresh is LoadState.Loading
                buttonRetry.isVisible =
                    loadState.refresh is LoadState.Error && loadState.source.refresh is LoadState.NotLoading && newsPagingAdapter.itemCount < 1
                textViewError.isVisible =
                    loadState.refresh is LoadState.Error && loadState.source.refresh is LoadState.NotLoading && newsPagingAdapter.itemCount < 1



                textViewNoResults.isVisible = loadState.refresh is LoadState.NotLoading &&
                        loadState.refresh.endOfPaginationReached
                newsPagingAdapter.itemCount < 1
            }

            newsPagingAdapter.registerAdapterDataObserver(object :
                RecyclerView.AdapterDataObserver() {

                override fun onChanged() {
                        Timber.d("onChanged")
                }

                override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
                        Timber.d("onItemRangeChanged start = $positionStart, count: $itemCount")
                }

                override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                    val firstVisibleItemPosition =
                        (recyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                    Timber.d("onItemRangeInserted start = $positionStart, count: $itemCount, firstVisibleItemPosition: $firstVisibleItemPosition")
                    if (positionStart < firstVisibleItemPosition) {
//                        Timber.d("SCROLL UP - Insert")
//                            recyclerView.scrollToPosition(0)
                    }
                }

                override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
                    val start = positionStart
                    val end = positionStart + itemCount - 1
                    val firstVisibleItemPosition =
                        (recyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                     Timber.d("onItemRangeRemoved start: $start count: $itemCount end: $end firstVisible: $firstVisibleItemPosition adapter-last: ${newsPagingAdapter.itemCount}")
//                        Handler().postDelayed({
//                            Timber.d("delayed current: ${(recyclerView.layoutManager as LinearLayoutManager).findLastCompletelyVisibleItemPosition()}")
//                        }, 500)
                    if (firstVisibleItemPosition >= newsPagingAdapter.itemCount) {
//                            Timber.d("SCROLL UP - last item")
//                            recyclerView.scrollToPosition(0)
                    }
                }

                override fun onItemRangeMoved(
                    fromPosition: Int,
                    toPosition: Int,
                    itemCount: Int
                ) {
                    val firstVisibleItemPosition =
                        (recyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                    Timber.d("onItemRangeMoved from: $fromPosition to: $toPosition firstVisibleItemPosition: $firstVisibleItemPosition")
                    if (toPosition < firstVisibleItemPosition) {
//                            recyclerView.scrollToPosition(0)
//                            Timber.d("SCROLL UP - MOVED")
                    }
                }
            })

            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                newsPagingAdapter.loadStateFlow
                    // Only emit when REFRESH LoadState for RemoteMediator changes.
                    .distinctUntilChangedBy { it.refresh }
                    // Only react to cases where Remote REFRESH completes i.e., NotLoading.
                    .filter { it.refresh is LoadState.NotLoading }
                    .collect {
                        delay(300)
                        recyclerView.scrollToPosition(0) }
            }

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
            viewModel.searchArticles(query)
            searchView.clearFocus()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.refresh -> {
                newsPagingAdapter.refresh()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBottomNavigationFragmentReselected() {
        binding.recyclerView.scrollToPosition(0) // TODO: 16.01.2021 This doesn't scroll all the way up if we are far enough down
    }
}
