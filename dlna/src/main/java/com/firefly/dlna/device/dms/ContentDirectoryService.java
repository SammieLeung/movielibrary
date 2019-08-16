package com.firefly.dlna.device.dms;

import org.fourthline.cling.model.types.ErrorCode;
import org.fourthline.cling.support.contentdirectory.AbstractContentDirectoryService;
import org.fourthline.cling.support.contentdirectory.ContentDirectoryException;
import org.fourthline.cling.support.contentdirectory.DIDLParser;
import org.fourthline.cling.support.model.BrowseFlag;
import org.fourthline.cling.support.model.BrowseResult;
import org.fourthline.cling.support.model.DIDLContent;
import org.fourthline.cling.support.model.SortCriterion;

class ContentDirectoryService extends AbstractContentDirectoryService {
    private static final String TAG = ContentDirectoryService.class.getSimpleName();

    private DmsDevice mDmsDevice;

    public ContentDirectoryService(DmsDevice dmsDevice) {
        mDmsDevice = dmsDevice;
    }

    @Override
    public BrowseResult browse(String objectID,
                               BrowseFlag browseFlag,
                               String filter,
                               long firstResult,
                               long maxResults,
                               SortCriterion[] orderby) throws ContentDirectoryException {
        FileItem fileItem = mDmsDevice.getFileStore()
                .browse(objectID, browseFlag, filter, firstResult, maxResults, orderby);

        if (fileItem != null) {
            DIDLContent didlContent = fileItem.getRoot();
            try {
                return new BrowseResult(new DIDLParser().generate(didlContent),
                        didlContent.getCount(), didlContent.getCount());
            } catch (Exception e) {
                throw new ContentDirectoryException(ErrorCode.ACTION_FAILED, e.getLocalizedMessage());
            }
        }

        return new BrowseResult("", 0, 0);
    }

    @Override
    public BrowseResult search(String containerId,
                               String searchCriteria,
                               String filter,
                               long firstResult,
                               long maxResults,
                               SortCriterion[] orderBy) throws ContentDirectoryException {
        FileItem fileItem = mDmsDevice.getFileStore()
                .search(containerId, searchCriteria, filter, firstResult, maxResults, orderBy);

        if (fileItem != null) {
            DIDLContent didlContent = fileItem.getRoot();
            try {
                return new BrowseResult(new DIDLParser().generate(didlContent),
                        didlContent.getCount(), didlContent.getCount());
            } catch (Exception e) {
                throw new ContentDirectoryException(ErrorCode.ACTION_FAILED, e.getLocalizedMessage());
            }
        }

        return new BrowseResult("", 0, 0);
    }
}
