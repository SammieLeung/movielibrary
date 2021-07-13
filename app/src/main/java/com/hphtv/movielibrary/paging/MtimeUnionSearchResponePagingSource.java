package com.hphtv.movielibrary.paging;

import androidx.paging.PagingState;
import androidx.paging.rxjava3.RxPagingSource;

import com.hphtv.movielibrary.roomdb.entity.Movie;
import com.hphtv.movielibrary.scraper.mtime.MtimeApi2;
import com.hphtv.movielibrary.scraper.mtime.respone.MtimeUnionSearchRespone;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.schedulers.Schedulers;

import static com.hphtv.movielibrary.scraper.mtime.MtimeApi2.UNIONSEARCH_PAGE_SIZE;


/**
 * author: Sam Leung
 * date:  2021/7/6
 */
public class MtimeUnionSearchResponePagingSource extends RxPagingSource<Integer, Movie> {

    private String mKeyword;

    public MtimeUnionSearchResponePagingSource(String keyword) {
        mKeyword = keyword;
    }

    @Override
    public @NotNull Single<LoadResult<Integer, Movie>> loadSingle(@NotNull LoadParams<Integer> loadParams) {
        Integer nextPageNumber = loadParams.getKey();
        if (nextPageNumber == null)
            nextPageNumber = 1;
        Integer finalNextPageNumber = nextPageNumber;
        return MtimeApi2.singleUnionSearch(mKeyword, nextPageNumber,loadParams.getLoadSize())
                .subscribeOn(Schedulers.io())
                .map(mtimeUnionSearchRespone -> {
                    Object nextKey;
                    Object prevKey;

                    if(mtimeUnionSearchRespone.toEntity().size()==0){
                        nextKey=null;
                    }else{
                        //初始加载数据长度为 3 * NETWORK_PAGE_SIZE;
                        nextKey= finalNextPageNumber +(loadParams.getLoadSize()/UNIONSEARCH_PAGE_SIZE);
                    }

                    if (finalNextPageNumber == 1)
                        prevKey=null;
                    else
                        prevKey =finalNextPageNumber - 1;
                    return MtimeUnionSearchResponePagingSource.this.toLoadResult(mtimeUnionSearchRespone,prevKey, nextKey);
                })
                .onErrorReturn(LoadResult.Error::new);
    }

    private LoadResult<Integer, Movie> toLoadResult(MtimeUnionSearchRespone response,Object prevKey,Object nextPageNumber) {
        return new LoadResult.Page(
                response.toEntity(),
                prevKey,
                nextPageNumber
              );
    }


    @Override
    public @Nullable Integer getRefreshKey(@NotNull PagingState<Integer, Movie> pagingState) {
        Integer anchorPosition = pagingState.getAnchorPosition();
        if (anchorPosition == null) {
            return null;
        }

        LoadResult.Page<Integer, Movie> anchorPage = pagingState.closestPageToPosition(anchorPosition);
        if (anchorPage == null) {
            return null;
        }

        Integer prevKey = anchorPage.getPrevKey();
        if (prevKey != null) {
            return prevKey + 1;
        }

        Integer nextKey = anchorPage.getNextKey();
        if (nextKey != null) {
            return nextKey - 1;
        }

        return null;
    }
}
