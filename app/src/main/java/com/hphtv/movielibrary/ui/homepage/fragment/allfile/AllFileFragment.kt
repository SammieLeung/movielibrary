package com.hphtv.movielibrary.ui.homepage.fragment.allfile

import android.content.IntentFilter
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver.OnPreDrawListener
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.hphtv.movielibrary.R
import com.hphtv.movielibrary.databinding.ActivityNewHomepageBinding
import com.hphtv.movielibrary.databinding.FragmentAllfileBinding
import com.hphtv.movielibrary.effect.FilterGridLayoutManager
import com.hphtv.movielibrary.effect.GridSpacingItemDecorationVertical2
import com.hphtv.movielibrary.listener.OnMovieLoadListener
import com.hphtv.movielibrary.ui.ILoadingState
import com.hphtv.movielibrary.ui.homepage.BaseAutofitHeightFragment
import com.hphtv.movielibrary.ui.homepage.HomePageActivity
import com.hphtv.movielibrary.ui.homepage.PlayVideoReceiver
import com.hphtv.movielibrary.ui.homepage.fragment.BaseHomeFragment
import com.hphtv.movielibrary.ui.view.NoScrollAutofitHeightViewPager
import com.hphtv.movielibrary.util.DLogger
import com.orhanobut.logger.Logger
import com.station.kit.util.DensityUtil
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicInteger

class AllFileFragment : BaseAutofitHeightFragment<AllFileViewModel, FragmentAllfileBinding>(),
    ILoadingState, DLogger {
    var atomicState = AtomicInteger()
    lateinit var fileTreeAdapter: FileTreeAdapter
    private var mPlayVideoReceiver: PlayVideoReceiver? = null
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding.bindState(
            mViewModel.uiState,mViewModel.loadingState, mViewModel.accept
        )
        lifecycleScope.launch {
            mViewModel.accept(UiAction.GoToRoot)
        }
    }

    fun FragmentAllfileBinding.bindState(
        uiStateFlow: StateFlow<UiState>, loadingStateFlow: StateFlow<LoadingState>,accept: (UiAction) -> Unit
    ) {
        isEmpty = true
        isLoading = false
        fileTreeAdapter = FileTreeAdapter(requireContext(), mutableListOf())
        rvAllFilesView.layoutManager = FilterGridLayoutManager(
            context, 1440, GridLayoutManager.VERTICAL, false
        )

        rvAllFilesView.addItemDecoration(
            GridSpacingItemDecorationVertical2(
                R.dimen.unknown_root_width.dimen, 60.dp, 68.dp, 35.dp, 45.dp, 6
            )
        );

        (rvAllFilesView.layoutManager as GridLayoutManager).spanSizeLookup =
            object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return if (position % 6 == 0) {
                        270
                    } else if (position % 6 == 5) {
                        270
                    } else {
                        225
                    }
                }
            }

        rvAllFilesView.adapter = fileTreeAdapter

        rvAllFilesView.setOnBackPressListener {
            if (uiStateFlow.value.isRoot) {
                val binding = baseActivity.binding
                val pos = binding.tabLayout.selectedTabPosition
                binding.tabLayout.getTabAt(pos)?.view?.requestFocus()
            } else {
                accept(UiAction.ClickItem(0, uiStateFlow.value.rootList[0]))
            }
        }

        rvAllFilesView.addOnScrollListener(object : OnMovieLoadListener() {
            override fun onLoading(countItem: Int, lastItem: Int) {
                accept(UiAction.LoadMore)
            }
        })

        fileTreeAdapter.setOnItemClickListener { _, position, data ->
            data?.let {
                accept(UiAction.ClickItem(position, it))
            }
        }


        lifecycleScope.launch {
            uiStateFlow.collect { uiState ->
            logger("collect uiStateFlow $uiState")
                if (uiState.rootList.isNotEmpty()) {
                    isEmpty = false
                    val firstItem = uiState.rootList[0]
                    if (firstItem.type == FolderType.BACK
                        || firstItem.type == FolderType.DEVICE
                        || firstItem.type == FolderType.SMB
                        || firstItem.type == FolderType.DLNA
                    ) {
                        rvAllFilesView.viewTreeObserver.addOnPreDrawListener(object :
                            OnPreDrawListener {
                            override fun onPreDraw(): Boolean {
                                rvAllFilesView.viewTreeObserver.removeOnPreDrawListener(this)
                                rvAllFilesView.requestFocus()
                                rvAllFilesView.scrollToPosition(uiState.focusPosition)
                                rvAllFilesView.layoutManager?.findViewByPosition(uiState.focusPosition)
                                    ?.requestFocus()
                                return true
                            }

                        })
                    }
                    if(uiState.isAppend){
                        fileTreeAdapter.appendAll(uiState.rootList)
                    }else{
                        fileTreeAdapter.addAll(uiState.rootList)
                    }

                }
            }

        }

        lifecycleScope.launch {
            loadingStateFlow.map { it.isLoading }.distinctUntilChanged().collect {
                isLoading = it
                if (isLoading) {
                    registerPlayReceiver()
                }
            }
        }
    }

    private fun registerPlayReceiver() {
        try {
            unregisterPlayReceiver()
            if (mPlayVideoReceiver == null) mPlayVideoReceiver = PlayVideoReceiver(
                context = requireContext(), refreshAction = {
                    (baseActivity as HomePageActivity).updateHistory()
                }, unregisterAction = this::unregisterPlayReceiver
            )
            val intentFilter = IntentFilter()
            intentFilter.addAction("com.firefly.video.player")
            requireContext().registerReceiver(mPlayVideoReceiver, intentFilter)
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
    }

    private fun unregisterPlayReceiver() {
        try {
            if (mPlayVideoReceiver != null) {
                requireContext().unregisterReceiver(mPlayVideoReceiver)
                mPlayVideoReceiver = null
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun forceRefresh() {
        lifecycleScope.launch {
            mViewModel.accept(UiAction.GoToRoot)
        }
    }

    override fun startLoading() {
        val i = atomicState.incrementAndGet()
        mBinding.isLoading = true
    }

    override fun finishLoading() {
        val i = atomicState.decrementAndGet()
        if (i <= 0) {
            mBinding.isLoading = false
            atomicState.set(0)
        }
    }

    fun OnBackPress(): Boolean {
        if (!mViewModel.uiState.value.isRoot && mBinding.rvAllFilesView.findFocus() != null) {
            return true
        }
        return false
    }

    override fun DLogger.tag(): String {
        return "AllFileFragment"
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
            viewPager: NoScrollAutofitHeightViewPager, position: Int
        ): AllFileFragment {
            val args = Bundle()
            val fragment = AllFileFragment()
            args.putInt(POSITION, position)
            fragment.arguments = args
            fragment.setAutoFitHeightViewPager(viewPager)
            return fragment
        }
    }

}