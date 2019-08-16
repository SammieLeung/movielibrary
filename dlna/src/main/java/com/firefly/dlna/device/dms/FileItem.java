package com.firefly.dlna.device.dms;

import android.support.annotation.IntDef;

import com.firefly.dlna.DlnaManager;
import com.firefly.dlna.httpserver.Registration;

import org.fourthline.cling.support.model.DIDLContent;
import org.fourthline.cling.support.model.DIDLObject;
import org.fourthline.cling.support.model.Res;
import org.fourthline.cling.support.model.container.Container;
import org.fourthline.cling.support.model.item.AudioItem;
import org.fourthline.cling.support.model.item.ImageItem;
import org.fourthline.cling.support.model.item.Item;
import org.fourthline.cling.support.model.item.VideoItem;

import java.util.HashMap;
import java.util.Map;

public class FileItem {
    @IntDef({
            DIRECTORY,
            FILE_VIDEO,
            FILE_AUDIO,
            FILE_IMAGE
    })
    @interface Type {}
    public final static int DIRECTORY = 0;
    public final static int FILE_VIDEO = 1;
    public final static int FILE_AUDIO = 2;
    public final static int FILE_IMAGE = 3;

    private DIDLContent mRoot = new DIDLContent();
    private Map<String, DIDLObject> mItems = new HashMap<>();
    private String mId;
    private DlnaManager mDlnaManager;

    /**
     * 构建{@link FileItem}
     * @param id 用户请求objectId
     */
    public FileItem(String id) {
        mId = id;
        mDlnaManager = DlnaManager.getInstance();
    }

    public DIDLContent getRoot() {
        return mRoot;
    }

    /**
     * 创建{@link Container}，在DLNA中表现为文件夹
     * @param id container的id
     * @param parent container的父id
     * @param title container显示名字
     * @param creator container创建者
     * @param childCount container包含文件数
     * @return 新建的Container
     */
    public Container newContainer(String id,
                                  String parent,
                                  String title,
                                  String creator,
                                  int childCount) {
        Container container =  new Container(id, parent, title, creator,
                new DIDLObject.Class("object.container"), childCount);

        mRoot.addContainer(container);
        mItems.put(id, container);

        return container;
    }

    /**
     * 创建{@link Container}，在DLNA中表现为文件夹
     * @param id container的id
     * @param parent container的父Container
     * @param title container显示名字
     * @param creator container创建者
     * @param childCount container包含文件数
     * @return 新建的Container
     */
    public Container newContainer(String id,
                                  Container parent,
                                  String title,
                                  String creator,
                                  int childCount) {
        return newContainer(id, parent.getId(), title, creator, childCount);
    }

    /**
     * 创建{@link Container}，在DLNA中表现为文件夹
     * 父亲节点默认为{@link FileItem#mId mId}
     * @param id container的id
     * @param title container显示名字
     * @param creator container创建者
     * @param childCount container包含文件数
     * @return 新建的Container
     */
    public Container newContainer(String id,
                                  String title,
                                  String creator,
                                  int childCount) {
        return newContainer(id, mId, title, creator, childCount);
    }

    /**
     * 通过id获取Container
     * @param id container id
     * @return 如果Container存在则返回container实例，否则返回null
     */
    public Container getContainer(String id) {
        DIDLObject object = mItems.get(id);
        if (object instanceof Container) {
            return (Container) object;
        }

        return null;
    }

    /**
     * 添加item
     * @param item {@link Item Item}及其子类的实例
     */
    public void addItem(Item item) {
        mItems.put(item.getId(), item);
    }

    /**
     * 通过id获取Item实例
     * @param id item id
     * @return 如果item存在则返回item实例，否则返回null
     */
    public Item getItem(String id) {
        DIDLObject object = mItems.get(id);
        if (object instanceof Item) {
            return (Item) object;
        }

        return null;
    }

    /**
     * 新建视频Item
     * @param id item id保持一个DMS中唯一
     * @param parent 所在的Container的id
     * @param title item显示名
     * @param uri 获取文件的uri
     * @param mimeType item的MIME type
     * @param size 文件大小
     * @param creator 文件创建人
     * @return {@link VideoItem}, 可通过返回值添加更多的信息
     */
    public VideoItem newVideoItem(String id,
                                  String parent,
                                  String title,
                                  String uri,
                                  String mimeType,
                                  Long size,
                                  String creator) {
        VideoItem item = new VideoItem(id, parent, title, creator);
        Registration[] registrations = mDlnaManager.getServer().registerFile(uri, mimeType);
        for (Registration registration : registrations) {
            item.addResource(new Res(registration.getProtocolInfo(), size, registration.getUri()));
        }
        mRoot.addItem(item);
        mItems.put(id, item);

        return item;
    }

    /**
     * 新建视频Item
     * 默认使用{@link FileItem#mId}作为父Container的id
     * @param id item id保持一个DMS中唯一
     * @param title item显示名
     * @param uri 获取文件的uri
     * @param mimeType item的MIME type
     * @param size 文件大小
     * @param creator 文件创建人
     * @return {@link VideoItem}, 可通过返回值添加更多的信息
     */
    public VideoItem newVideoItem(String id,
                                  String title,
                                  String uri,
                                  String mimeType,
                                  Long size,
                                  String creator) {
        return newVideoItem(id, mId, title, uri, mimeType, size, creator);
    }

    /**
     * 新建视频Item
     * 默认使用{@link FileItem#mId}作为父Container的id
     * @param id item id保持一个DMS中唯一
     * @param title item显示名
     * @param uri 获取文件的uri
     * @param mimeType item的MIME type
     * @param creator 文件创建人
     * @return {@link VideoItem}, 可通过返回值添加更多的信息
     */
    public VideoItem newVideoItem(String id,
                                  String title,
                                  String uri,
                                  String mimeType,
                                  String creator) {
        return newVideoItem(id, title, uri, mimeType, null, creator);
    }

    /**
     * 新建音频Item
     * @param id item id保持一个DMS中唯一
     * @param parent 所在的Container的id
     * @param title item显示名
     * @param uri 获取文件的uri
     * @param mimeType item的MIME type
     * @param size 文件大小
     * @param creator 文件创建人
     * @return {@link AudioItem}, 可通过返回值添加更多的信息
     */
    public AudioItem newAudioItem(String id,
                                  String parent,
                                  String title,
                                  String uri,
                                  String mimeType,
                                  Long size,
                                  String creator) {
        AudioItem item = new AudioItem(id, parent, title, creator);
        Registration[] registrations = mDlnaManager.getServer().registerFile(uri, mimeType);
        for (Registration registration : registrations) {
            item.addResource(new Res(registration.getProtocolInfo(), size, registration.getUri()));
        }
        mRoot.addItem(item);
        mItems.put(id, item);

        return item;
    }

    /**
     * 新建音频Item
     * 默认使用{@link FileItem#mId}作为父Container的id
     * @param id item id保持一个DMS中唯一
     * @param title item显示名
     * @param uri 获取文件的uri
     * @param mimeType item的MIME type
     * @param size 文件大小
     * @param creator 文件创建人
     * @return {@link AudioItem}, 可通过返回值添加更多的信息
     */
    public AudioItem newAudioItem(String id,
                                  String title,
                                  String uri,
                                  String mimeType,
                                  Long size,
                                  String creator) {
        return newAudioItem(id, mId, title, uri, mimeType, size, creator);
    }

    /**
     * 新建音频Item
     * 默认使用{@link FileItem#mId}作为父Container的id
     * @param id item id保持一个DMS中唯一
     * @param title item显示名
     * @param uri 获取文件的uri
     * @param mimeType item的MIME type
     * @param creator 文件创建人
     * @return {@link AudioItem}, 可通过返回值添加更多的信息
     */
    public AudioItem newAudioItem(String id,
                                  String title,
                                  String uri,
                                  String mimeType,
                                  String creator) {
        return newAudioItem(id, title, uri, mimeType, null, creator);
    }

    /**
     * 新建图片Item
     * @param id item id保持一个DMS中唯一
     * @param parent 所在的Container的id
     * @param title item显示名
     * @param uri 获取文件的uri
     * @param mimeType item的MIME type
     * @param size 文件大小
     * @param creator 文件创建人
     * @return {@link ImageItem}, 可通过返回值添加更多的信息
     */
    public ImageItem newImageItem(String id,
                                  String parent,
                                  String title,
                                  String uri,
                                  String mimeType,
                                  Long size,
                                  String creator) {

        ImageItem item = new ImageItem(id, parent, title, creator);
        Registration[] registrations = mDlnaManager.getServer().registerFile(uri, mimeType);
        for (Registration registration : registrations) {
            item.addResource(new Res(registration.getProtocolInfo(), size, registration.getUri()));
        }
        mRoot.addItem(item);
        mItems.put(id, item);

        return item;
    }

    /**
     * 新建图片Item
     * 默认使用{@link FileItem#mId}作为父Container的id
     * @param id item id保持一个DMS中唯一
     * @param title item显示名
     * @param uri 获取文件的uri
     * @param mimeType item的MIME type
     * @param size 文件大小
     * @param creator 文件创建人
     * @return {@link ImageItem}, 可通过返回值添加更多的信息
     */
    public ImageItem newImageItem(String id,
                                  String title,
                                  String uri,
                                  String mimeType,
                                  Long size,
                                  String creator) {
        return newImageItem(id, mId, title, uri, mimeType, size, creator);
    }

    /**
     * 新建图片Item
     * 默认使用{@link FileItem#mId}作为父Container的id
     * @param id item id保持一个DMS中唯一
     * @param title item显示名
     * @param uri 获取文件的uri
     * @param mimeType item的MIME type
     * @param creator 文件创建人
     * @return {@link ImageItem}, 可通过返回值添加更多的信息
     */
    public ImageItem newImageItem(String id,
                                  String title,
                                  String uri,
                                  String mimeType,
                                  String creator) {
        return newImageItem(id, title, uri, mimeType, null, creator);
    }
}
