package com.codinginflow.mvvmnewsapp.features.bookmarks

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.codinginflow.mvvmnewsapp.MainActivity
import com.codinginflow.mvvmnewsapp.R
import com.codinginflow.mvvmnewsapp.databinding.FragmentBookmarksBinding
import com.codinginflow.mvvmnewsapp.core.shared.NewsListAdapter
import com.codinginflow.mvvmnewsapp.util.viewBinding
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class BookmarksFragment : Fragment(R.layout.fragment_bookmarks), MainActivity.OnBottomNavigationFragmentReselected {
    private val viewModel: BookmarksViewModel by viewModels()

    private val binding by viewBinding(FragmentBookmarksBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bookmarksAdapter = NewsListAdapter(
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
                adapter = bookmarksAdapter
                layoutManager = LinearLayoutManager(requireContext())
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

    override fun onBottomNavigationFragmentReselected() {
        binding.recyclerView.scrollToPosition(0)
    }
}