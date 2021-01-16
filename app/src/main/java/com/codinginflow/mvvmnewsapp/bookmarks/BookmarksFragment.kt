package com.codinginflow.mvvmnewsapp.bookmarks

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.codinginflow.mvvmnewsapp.R
import com.codinginflow.mvvmnewsapp.databinding.FragmentBookmarksBinding
import com.codinginflow.mvvmnewsapp.shared.NewsListAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BookmarksFragment : Fragment(R.layout.fragment_bookmarks) {
    private val viewModel: BookmarksViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentBookmarksBinding.bind(view)

        val bookmarksAdapter = NewsListAdapter(
            onBookmarkClick = { article ->
                viewModel.onBookmarkClick(article)
            }
        )

        binding.apply {
            recyclerView.apply {
                setHasFixedSize(true)
                adapter = bookmarksAdapter
                layoutManager = LinearLayoutManager(requireContext())
                itemAnimator?.changeDuration = 0
            }

            viewModel.bookmarks.observe(viewLifecycleOwner) { bookmarks ->
                bookmarksAdapter.submitList(bookmarks)
                textViewEmpty.isVisible = bookmarks.isEmpty()
                recyclerView.isVisible = bookmarks.isNotEmpty()
            }
        }

        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_bookmarks, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) =
        when (item.itemId) {
            R.id.action_delete_all_bookmarks -> {
                viewModel.onDeleteAllBookmarks()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
}