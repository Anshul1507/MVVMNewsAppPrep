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
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.codinginflow.mvvmnewsapp.MainActivity
import com.codinginflow.mvvmnewsapp.R
import com.codinginflow.mvvmnewsapp.databinding.FragmentSearchNewsBinding
import com.codinginflow.mvvmnewsapp.util.onQueryTextSubmit
import com.codinginflow.mvvmnewsapp.util.showSnackbar
import com.codinginflow.mvvmnewsapp.util.viewBinding
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class SearchNewsFragment : Fragment(R.layout.fragment_search_news),
    MainActivity.OnBottomNavigationFragmentReselected {
    private val viewModel: SearchNewsViewModel by viewModels()

    private lateinit var newsPagingAdapter: NewsPagingAdapter

    private val binding by viewBinding(FragmentSearchNewsBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = binding

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

            // TODO: 19.01.2021 This is not right yet. I have to play around until this is correct
            newsPagingAdapter.addLoadStateListener { loadState ->
                Timber.d("source = ${loadState.source}")
                Timber.d("mediator = ${loadState.mediator}")
                progressBar.isVisible = loadState.refresh is LoadState.Loading && newsPagingAdapter.itemCount < 1
                buttonRetry.isVisible = loadState.refresh is LoadState.Error && loadState.source.refresh is LoadState.NotLoading && newsPagingAdapter.itemCount < 1
                textViewError.isVisible = loadState.refresh is LoadState.Error && loadState.source.refresh is LoadState.NotLoading && newsPagingAdapter.itemCount < 1

                val errorState = /*loadState.append as? LoadState.Error
                    ?: loadState.prepend as? LoadState.Error
                    ?:*/ loadState.refresh as? LoadState.Error

                errorState?.let {
                    val errorMessage = it.error.localizedMessage ?: "An unknown error occurred"
                    textViewError.text = errorMessage
//                    showSnackbar(errorMessage) // TODO: 19.01.2021 Ideally this would be a one-off event
                }

                if (loadState.refresh is LoadState.NotLoading &&
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
