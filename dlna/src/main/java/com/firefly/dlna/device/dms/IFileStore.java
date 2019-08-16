package com.firefly.dlna.device.dms;

import org.fourthline.cling.support.contentdirectory.ContentDirectoryException;
import org.fourthline.cling.support.model.BrowseFlag;
import org.fourthline.cling.support.model.SortCriterion;

public interface IFileStore {
    /**
     * 在DMC浏览文件时被调用
     * @param objectId 文件或目录的id，在一个DMS中应唯一
     * @param browseFlag {@link BrowseFlag}
     * @param filter 过滤返回的文件属性
     * @param firstResult 获取第一个结果的位置
     * @param maxResults 获取最大的结果数
     * @param orderBy 结果的排列方式
     * @return {@link FileItem}
     * @exception ContentDirectoryException 所有错误一个包裹在此类中
     */
    FileItem browse(String objectId,
                        BrowseFlag browseFlag,
                        String filter,
                        long firstResult,
                        long maxResults,
                        SortCriterion[] orderBy) throws ContentDirectoryException;

    /**
     * 通过DMC搜索文件时调用
     * @param containerId 搜索的起点目录的id，在一个DMS中应唯一
     * @param searchCriteria
     * @param filter 过滤返回的文件属性
     * @param firstResult 获取第一个结果的位置
     * @param maxResults 获取最大的结果数
     * @param orderBy 结果的排列方式
     * @return {@link FileItem}
     * @exception ContentDirectoryException 所有错误一个包裹在此类中
     */
    FileItem search(String containerId,
                        String searchCriteria,
                        String filter,
                        long firstResult,
                        long maxResults,
                        SortCriterion[] orderBy) throws ContentDirectoryException;
}
