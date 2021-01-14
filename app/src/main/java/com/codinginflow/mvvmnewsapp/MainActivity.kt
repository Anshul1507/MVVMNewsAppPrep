package com.codinginflow.mvvmnewsapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import com.codinginflow.mvvmnewsapp.bookmarks.BookmarksFragment
import com.codinginflow.mvvmnewsapp.breakingnews.BreakingNewsFragment
import com.codinginflow.mvvmnewsapp.databinding.ActivityMainBinding
import com.codinginflow.mvvmnewsapp.searchnews.SearchNewsFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var navController: NavController

    private lateinit var breakingNewsFragment: BreakingNewsFragment
    private lateinit var searchNewsFragment: SearchNewsFragment
    private lateinit var bookmarksFragment: BookmarksFragment

    private val fragments: Array<out Fragment>
        get() = arrayOf(
            breakingNewsFragment,
            searchNewsFragment,
            bookmarksFragment
        )

    private var selectedIndex = 0

    private val selectedFragment get() = fragments[selectedIndex]

    private fun selectFragment(selectedFragment: Fragment) {
        var transaction = supportFragmentManager.beginTransaction()
        fragments.forEachIndexed { index, fragment ->
            if (selectedFragment == fragment) {
                transaction = transaction.attach(fragment)
                selectedIndex = index
            } else {
                transaction = transaction.detach(fragment)
            }
        }
        transaction.commit()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            breakingNewsFragment = BreakingNewsFragment()
            searchNewsFragment = SearchNewsFragment()
            bookmarksFragment = BookmarksFragment()

            supportFragmentManager.beginTransaction()
                .add(R.id.fragment_container, breakingNewsFragment, "breaking")
                .add(R.id.fragment_container, searchNewsFragment, "search")
                .add(R.id.fragment_container, bookmarksFragment, "bookmarks")
                .commitNow()
        } else {
            selectedIndex = savedInstanceState.getInt("selectedIndex", 0)

            breakingNewsFragment =
                supportFragmentManager.findFragmentByTag("breaking") as BreakingNewsFragment
            searchNewsFragment =
                supportFragmentManager.findFragmentByTag("search") as SearchNewsFragment
            bookmarksFragment =
                supportFragmentManager.findFragmentByTag("bookmarks") as BookmarksFragment
        }

        selectFragment(selectedFragment)

        // TODO: 14.01.2021 Let Zhuinden check this scroll-up logic
        binding.bottomNav.setOnNavigationItemSelectedListener { item ->
            val currentFragment = selectedFragment
            when (item.itemId) {
                R.id.breakingNewsFragment -> {
                    if (currentFragment is BreakingNewsFragment) {
                        currentFragment.scrollUpAndRefresh()
                    } else {
                        selectFragment(breakingNewsFragment)
                        title = "Breaking News"
                    }
                    true
                }
                R.id.searchNewsFragment -> {
                    selectFragment(searchNewsFragment)
                    title = "Search News"
                    true
                }
                R.id.bookmarksFragment -> {
                    selectFragment(bookmarksFragment)
                    title = "Bookmarks"
                    true
                }
                else -> false
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("selectedIndex", selectedIndex)
    }
}