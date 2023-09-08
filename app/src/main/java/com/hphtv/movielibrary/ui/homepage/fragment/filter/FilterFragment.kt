package com.hphtv.movielibrary.ui.homepage.fragment.filter

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.hphtv.movielibrary.R
import com.hphtv.movielibrary.adapter.NewMovieItemListAdapter
import com.hphtv.movielibrary.data.Constants
import com.hphtv.movielibrary.data.Constants.REQUEST_CODE_FROM_BASE_FILTER
import com.hphtv.movielibrary.data.Constants.VideoType
import com.hphtv.movielibrary.databinding.ActivityNewHomepageBinding
import com.hphtv.movielibrary.databinding.FragmentFilterBinding
import com.hphtv.movielibrary.effect.FilterGridLayoutManager
import com.hphtv.movielibrary.effect.GridSpacingItemDecorationVertical2
import com.hphtv.movielibrary.listener.OnMovieLoadListener
import com.hphtv.movielibrary.roomdb.entity.Shortcut
import com.hphtv.movielibrary.roomdb.entity.VideoTag
import com.hphtv.movielibrary.roomdb.entity.dataview.MovieDataView
import com.hphtv.movielibrary.ui.detail.MovieDetailActivity
import com.hphtv.movielibrary.ui.homepage.BaseAutofitHeightFragment
import com.hphtv.movielibrary.ui.shortcutmanager.ShortcutManagerActivity
import com.hphtv.movielibrary.ui.view.NoScrollAutofitHeightViewPager
import com.hphtv.movielibrary.ui.view.TvRecyclerView
import com.orhanobut.logger.Logger
import com.station.kit.util.DensityUtil

class FilterFragment constructor(val videoType: VideoType) :
    BaseAutofitHeightFragment<FilterViewModel, FragmentFilterBinding>() {
    private val adapter: NewMovieItemListAdapter by lazy {
        NewMovieItemListAdapter(mViewModel.getApplication(), mutableListOf())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding.bindState()
        mViewModel.setVideoType(videoType)
        mViewModel.setOnRefresh(object : FilterViewModel.OnRefresh {
            override fun beforeLoad() {
            }

            override fun newSearch(newMovieDataView: List<MovieDataView?>?) {
                mBinding.isEmpty = newMovieDataView?.isEmpty() ?: true
                newMovieDataView?.let{
                    adapter.addAll(it)
                }
            }

            override fun appendMovieDataViews(movieDataViews: List<MovieDataView?>?) {
                adapter.appendAll(movieDataViews)
            }
        })
    }

    private fun FragmentFilterBinding.bindState() {
        initRecyclerView(movieList)
        btnQuickAddShortcut.setOnClickListener {
            startActivityForResult(
                Intent(
                    this@FilterFragment.requireContext(),
                    ShortcutManagerActivity::class.java
                )
            )
        }
    }

    /*
     |                      ------1920------                     |
     |-----1-----|-----2-----|-----3-----|-----4-----|-----5-----|
     |80] 340  [8|7]  340  [8|7]  340  [8|7]  340  [8|7]  340 [80|
     |-----------|-----------|-----------|-----------|-----------|
     |    429    |    354    |    354    |    354    |    429    |
   */
    private fun initRecyclerView(movieList: TvRecyclerView) {
        adapter.setZoomRatio(1.208888f)
        movieList.adapter = adapter
        movieList.layoutManager =
            FilterGridLayoutManager(context, 1920, GridLayoutManager.VERTICAL, false)
        movieList.addItemDecoration(
            GridSpacingItemDecorationVertical2(
                /* itemWidth = */ R.dimen.poster_item_1_w.dimen,
                /* firstRowSpacing = */ 81.dp,
                /* edgeSpacing = */ 80,
                /* rowSpacing = */ 65.dp,
                /* columnSpacing = */ 16,
                /* spanCount = */ 5
            )
        )
        movieList.setOnBackPressListener {
            val binding: ActivityNewHomepageBinding = baseActivity.binding
            val pos: Int = binding.tabLayout.selectedTabPosition
            binding.tabLayout.getTabAt(pos)?.view?.requestFocus()
        }
        (movieList.layoutManager as GridLayoutManager).spanSizeLookup =
            object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return if (position % 5 == 0) {
                        428
                    } else if (position % 5 == 4) {
                        428
                    } else {
                        354
                    }
                }
            }
        movieList.addOnScrollListener(object : OnMovieLoadListener() {
            override fun onLoading(countItem: Int, lastItem: Int) {
                mViewModel.loadMoreMovies()
            }
        })
        adapter.setOnItemClickListener { view, position, data ->
            val intent = Intent(
                activity,
                MovieDetailActivity::class.java
            )
            val bundle = Bundle()
            bundle.putLong(Constants.Extras.MOVIE_ID, data.id)
            bundle.putInt(Constants.Extras.SEASON, data.season)
            bundle.putInt(Constants.Extras.REQUEST_CODE, REQUEST_CODE_FROM_BASE_FILTER)
            intent.putExtras(bundle)
            startActivityForResult(intent)
        }
    }

    override fun createViewModel(): FilterViewModel {
        return ViewModelProvider(this)[FilterViewModel::class.java]
    }

    override fun forceRefresh() {
        mViewModel.setVideoType(videoType)
        mViewModel.forceReloadMovies()
    }

    fun setFilter(
        shortcut: Shortcut?,
        genre: String?,
        startYear: String?,
        endYear: String?
    ) {
        mViewModel.setFilter(shortcut, genre, startYear, endYear)
        mViewModel.reloadMovies()
    }

    fun setOrder(order: Int, isDesc: Boolean) {
        mViewModel.setOrder(order, isDesc)
        mViewModel.reloadMovies()
    }

    private val Int.dimen
        get() = resources.getDimensionPixelSize(this)
    private val Int.dp
        get() = DensityUtil.dip2px(context, this.toFloat())
    private val Float.dp
        get() = DensityUtil.dip2px(context, this)
    private val Double.dp
        get() = this.toFloat().dp

    companion object {
        @JvmStatic
        fun newInstance(
            videoType: VideoType,
            viewPager: NoScrollAutofitHeightViewPager,
            pos: Int
        ): FilterFragment {
            val args = Bundle()
            val fragment = FilterFragment(videoType)
            args.putInt(POSITION, pos)
            fragment.arguments = args
            fragment.setAutoFitHeightViewPager(viewPager)
            return fragment
        }
    }
}