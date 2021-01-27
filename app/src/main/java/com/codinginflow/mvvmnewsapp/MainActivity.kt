package com.codinginflow.mvvmnewsapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import com.codinginflow.mvvmnewsapp.features.bookmarks.BookmarksFragment
import com.codinginflow.mvvmnewsapp.features.worldnews.WorldNewsFragment
import com.codinginflow.mvvmnewsapp.databinding.ActivityMainBinding
import com.codinginflow.mvvmnewsapp.features.searchnews.SearchNewsFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    interface OnBottomNavigationFragmentReselected {
        fun onBottomNavigationFragmentReselected()
    }

    private lateinit var navController: NavController
    private lateinit var binding: ActivityMainBinding

    private lateinit var worldNewsFragment: WorldNewsFragment
    private lateinit var searchNewsFragment: SearchNewsFragment
    private lateinit var bookmarksFragment: BookmarksFragment

    private val fragments: Array<out Fragment>
        get() = arrayOf(
            worldNewsFragment,
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

        title = when (selectedFragment) {
            is WorldNewsFragment ->  "World News"
            is SearchNewsFragment -> "Search News"
            is BookmarksFragment -> "Bookmarks"
            else -> ""
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            worldNewsFragment = WorldNewsFragment()
            searchNewsFragment = SearchNewsFragment()
            bookmarksFragment = BookmarksFragment()

            supportFragmentManager.beginTransaction()
                .add(R.id.fragment_container, worldNewsFragment, "breaking")
                .add(R.id.fragment_container, searchNewsFragment, "search")
                .add(R.id.fragment_container, bookmarksFragment, "bookmarks")
                .commitNow()
        } else {
            selectedIndex = savedInstanceState.getInt("selectedIndex", 0)

            worldNewsFragment =
                supportFragmentManager.findFragmentByTag("breaking") as WorldNewsFragment
            searchNewsFragment =
                supportFragmentManager.findFragmentByTag("search") as SearchNewsFragment
            bookmarksFragment =
                supportFragmentManager.findFragmentByTag("bookmarks") as BookmarksFragment
        }

        selectFragment(selectedFragment)

        binding.bottomNav.setOnNavigationItemSelectedListener { item ->
            val currentFragment = selectedFragment

            val fragment = when (item.itemId) {
                R.id.worldNewsFragment -> worldNewsFragment
                R.id.searchNewsFragment -> searchNewsFragment
                R.id.bookmarksFragment -> bookmarksFragment
                else -> null
            }

            if (fragment == null) {
                return@setOnNavigationItemSelectedListener false
            }

            if (currentFragment === fragment) {
                @Suppress("USELESS_IS_CHECK")
                if (fragment is OnBottomNavigationFragmentReselected) {
                    fragment.onBottomNavigationFragmentReselected()
                }
            } else {
                selectFragment(fragment)
            }

            true
        }
    }

    override fun onBackPressed() {
        if (selectedIndex != 0) {
            binding.bottomNav.selectedItemId = R.id.worldNewsFragment
        } else {
            super.onBackPressed()
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