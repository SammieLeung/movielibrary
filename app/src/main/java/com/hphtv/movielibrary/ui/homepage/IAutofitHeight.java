package com.hphtv.movielibrary.ui.homepage;

import com.hphtv.movielibrary.ui.view.NoScrollAutofitHeightViewPager;

/**
 * author: Sam Leung
 * date:  2022/1/14
 * 用于实现当NestedScrollView嵌套Viewpager，viewpager实现自适应高度的接口
 */
public interface IAutofitHeight {
    NoScrollAutofitHeightViewPager getAutofitHeightViewPager();
}
