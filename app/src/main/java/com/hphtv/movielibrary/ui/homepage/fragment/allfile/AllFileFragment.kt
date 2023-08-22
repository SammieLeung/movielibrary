package com.hphtv.movielibrary.ui.homepage.fragment.allfile

import com.hphtv.movielibrary.databinding.FragmentUnknowfileBinding
import com.hphtv.movielibrary.ui.ILoadingState
import com.hphtv.movielibrary.ui.homepage.BaseAutofitHeightFragment
import com.hphtv.movielibrary.ui.homepage.fragment.unknown.UnknownFileViewModel
import java.util.concurrent.atomic.AtomicInteger

class AllFileFragment :
    BaseAutofitHeightFragment<UnknownFileViewModel, FragmentUnknowfileBinding>(), ILoadingState {
    var atomicState = AtomicInteger()
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
}