package com.codinginflow.mvvmnewsapp.features.worldnews

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.codinginflow.mvvmnewsapp.MainActivity
import com.codinginflow.mvvmnewsapp.R
import com.codinginflow.mvvmnewsapp.shared.NewsListAdapter
import com.codinginflow.mvvmnewsapp.databinding.FragmentWorldNewsBinding
import com.codinginflow.mvvmnewsapp.util.Resource
import com.codinginflow.mvvmnewsapp.util.showSnackbar
import com.codinginflow.mvvmnewsapp.util.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import timber.log.Timber

@AndroidEntryPoint
class WorldNewsFragment : Fragment(R.layout.fragment_world_news),
    MainActivity.OnBottomNavigationFragmentReselected {

    private val viewModel: WorldNewsViewModel by viewModels()

    private lateinit var newsAdapter: NewsListAdapter

    private val binding by viewBinding(FragmentWorldNewsBinding::bind)

    private var firstInsertCompleted = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firstInsertCompleted = false

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
                itemAnimator =
                    null // we don't need animations and this gets us rid of ugly DiffUtil changes
//                itemAnimator?.changeDuration = 0 // get rid of bookmark click flash
            }

            viewModel.breakingNews.observe(viewLifecycleOwner) { result ->
                Timber.d("BREAKING observe with result $result")

                swipeRefreshLayout.isRefreshing = result is Resource.Loading
                recyclerView.isVisible = !result.data.isNullOrEmpty()
                textViewError.isVisible = result.error != null && result.data.isNullOrEmpty()
                buttonRetry.isVisible = result.error != null && result.data.isNullOrEmpty()
                textViewError.text = resources.getString(
                    R.string.could_not_refresh,
                    result.error?.localizedMessage ?: "An unknown error occurred"
                )

                newsAdapter.submitList(result.data)
            }

            newsAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
                override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
                    Timber.d("onItemRangeMoved count: $itemCount")
                    recyclerView.scrollToPosition(0)
                }

                override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                    Timber.d("onItemRangeInserted start $positionStart count: $itemCount")
                    // opening the fragment causes an insert for all items -> we want to ignore that
                    if (firstInsertCompleted) {
                        recyclerView.scrollToPosition(0)
                    } else {
                        firstInsertCompleted = true
                    }
                }
            })

            swipeRefreshLayout.setOnRefreshListener {
                viewModel.onManualRefresh()
            }

            buttonRetry.setOnClickListener {
                viewModel.onManualRefresh()
            }

            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                viewModel.events.collect { event ->
                    when (event) {
                        is WorldNewsViewModel.Event.ShowErrorMessage -> {
                            showSnackbar(
                                resources.getString(
                                    R.string.could_not_refresh,
                                    event.error.localizedMessage ?: "An unknown error occurred"
                                )
                            )
                        }
                        is WorldNewsViewModel.Event.ScrollToTop -> recyclerView.scrollToPosition(0)
                    }
                }
            }
        }

        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_breaking_news, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.refresh -> {
                viewModel.onManualRefresh()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBottomNavigationFragmentReselected() {
        binding.recyclerView.scrollToPosition(0)
//        viewModel.onManualRefresh()
    }
}