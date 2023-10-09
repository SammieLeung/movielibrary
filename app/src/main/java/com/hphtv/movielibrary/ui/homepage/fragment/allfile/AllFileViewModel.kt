package com.hphtv.movielibrary.ui.homepage.fragment.allfile

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.sqlite.db.SimpleSQLiteQuery
import com.hphtv.movielibrary.R
import com.hphtv.movielibrary.data.Constants.DeviceType
import com.hphtv.movielibrary.data.pagination.PaginatedDataLoader
import com.hphtv.movielibrary.roomdb.MovieLibraryRoomDatabase
import com.hphtv.movielibrary.roomdb.dao.VideoFileDao.QUERY_FILES_SQL
import com.hphtv.movielibrary.roomdb.dao.VideoFileDao.QUERY_FOLDERS_SQL
import com.hphtv.movielibrary.util.MovieHelper
import com.hphtv.movielibrary.util.StringTools
import com.hphtv.movielibrary.util.rxjava.SimpleObserver
import com.orhanobut.logger.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AllFileViewModel(application: Application) : AndroidViewModel(application) {

    private val deviceDao = MovieLibraryRoomDatabase.getDatabase(application).deviceDao
    private val shortcutDao = MovieLibraryRoomDatabase.getDatabase(application).shortcutDao
    private val videoFileDao = MovieLibraryRoomDatabase.getDatabase(application).videoFileDao
    private val _uiState: MutableStateFlow<UiState> = MutableStateFlow(UiState())
    private val _loadingState: MutableStateFlow<LoadingState> = MutableStateFlow(LoadingState())


    val uiState: StateFlow<UiState> = _uiState.asStateFlow()
    val loadingState: StateFlow<LoadingState> = _loadingState.asStateFlow()
    var accept: (UiAction) -> Unit

    var lastParentFolder: FolderItem? = null


    var folderItemPagerLoader = AllFilePaginatedLoader()
    var dlnaPagerLoader = DLNAPaginatedLoader()

    init {
        accept = initAcceptAction()
    }

    fun initAcceptAction(): (UiAction) -> Unit {
        val actionStateFlow: MutableSharedFlow<UiAction> = MutableSharedFlow()
        val gotoRootAction = actionStateFlow.filterIsInstance<UiAction.GoToRoot>()
        val clickItemAction = actionStateFlow.filterIsInstance<UiAction.ClickItem>()
        val loadNextAction = actionStateFlow.filterIsInstance<UiAction.LoadNext>()
        val backAction = actionStateFlow.filterIsInstance<UiAction.BackAction>()
        val loadPreAction = actionStateFlow.filterIsInstance<UiAction.LoadPre>()

        handleGotoRootAction(gotoRootAction)
        handleItemClickAction(clickItemAction)
        handleLoadNextAction(loadNextAction)
        handleLoadPreAction(loadPreAction)
        handleBackAction(backAction)

        return { action ->
            viewModelScope.launch {
                actionStateFlow.emit(action)
            }
        }
    }


    private fun handleGotoRootAction(
        gotoRootAction: Flow<UiAction.GoToRoot>,
        parentFolderType: FolderType? = null
    ) =
        viewModelScope.launch(Dispatchers.Default) {
            val rootList = mutableListOf<FolderItem>()

            val shortcutList = shortcutDao.queryAllConnectShortcuts()
            val deviceTypes = mutableSetOf<Int>()
            shortcutList.forEach {
                if (it.deviceType <= DeviceType.DEVICE_TYPE_HARD_DISK) {
                    deviceTypes.add(DeviceType.DEVICE_TYPE_LOCAL)
                } else {
                    deviceTypes.add(it.deviceType)
                }
            }
            deviceTypes.forEachIndexed { index, type ->
                when (type) {
                    DeviceType.DEVICE_TYPE_LOCAL -> {
                        rootList.add(
                            FolderItem(
                                name = getString(R.string.filter_box_local_device),
                                icon = R.mipmap.icon_folder,
                                path = "",
                                friendlyPath = "/${getString(R.string.filter_box_local_device)}/",
                                pos = index,
                                type = FolderType.DEVICE,
                            )
                        )
                    }

                    DeviceType.DEVICE_TYPE_SMB -> {
                        rootList.add(
                            FolderItem(
                                name = getString(R.string.filter_box_smb_device),
                                icon = R.mipmap.icon_samba,
                                path = "",
                                friendlyPath = "/${getString(R.string.filter_box_smb_device)}/",
                                pos = index,
                                type = FolderType.SMB,
                            )
                        )
                    }

                    DeviceType.DEVICE_TYPE_DLNA -> {
                        rootList.add(
                            FolderItem(
                                name = getString(R.string.filter_box_dlna_device),
                                icon = R.mipmap.icon_dlna,
                                path = "",
                                friendlyPath = "/${getString(R.string.filter_box_dlna_device)}/",
                                pos = index,
                                type = FolderType.DLNA
                            )
                        )
                    }
                }
            }
            var lastFocusPosition = 0
            parentFolderType?.let { folderType ->
                rootList.forEach {
                    if (it.type == folderType) {
                        lastFocusPosition = it.pos
                    }
                }
            }
            gotoRootAction.collect {
                _uiState.update {
                    it.copy(
                        isRoot = true,
                        isReload = true,
                        isAppend = false,
                        isAddInFront = false,
                        currentPath = "",
                        friendlyPath = "/",
                        rootList = rootList.sortedBy { it.type.ordinal },
                        focusPosition = lastFocusPosition
                    )
                }
            }
        }

    private fun handleItemClickAction(clickItemAction: Flow<UiAction.ClickItem>) =
        viewModelScope.launch(Dispatchers.Default) {
            clickItemAction.collect {
                it.folderItem.let {
                    when (it.type) {
                        FolderType.DEVICE -> {
                            lastParentFolder = it
                            updateLocalDevices(it)
                        }

                        FolderType.SMB -> {
                            lastParentFolder = it
                            updateSmbDevices(it)
                        }

                        FolderType.DLNA -> {
                            lastParentFolder = it
                            updateDLNADevices(it)
                        }

                        FolderType.DLNA_GROUP, FolderType.DLNA_SHARE -> {
                            lastParentFolder = it
                            dlnaPagerLoader.reload(it)
                        }

                        FolderType.FOLDER -> {
                            lastParentFolder = it
                            folderItemPagerLoader.reload(it)
                        }

                        FolderType.FILE -> {
                            _loadingState.update {
                                it.copy(isLoading = true)
                            }
                            MovieHelper.playingMovie(it.path, it.name)
                                .flatMap(MovieHelper::updateHistory)
                                .subscribe(object : SimpleObserver<String>() {
                                    override fun onAction(t: String?) {
                                        _loadingState.update {
                                            it.copy(isLoading = false)
                                        }
                                    }
                                })
                        }
                    }
                }
            }
        }

    private fun handleBackAction(backAction: Flow<UiAction.BackAction>) =
        viewModelScope.launch(Dispatchers.Default) {
            backAction.collect {
                if (lastParentFolder?.type == FolderType.SMB
                    || lastParentFolder?.type == FolderType.DEVICE
                    || lastParentFolder?.type == FolderType.DLNA
                ) {
                    handleGotoRootAction(
                        gotoRootAction = flowOf(UiAction.GoToRoot),
                        lastParentFolder?.type
                    )
                } else {
                    if (lastParentFolder?.type != FolderType.FOLDER)
                        dlnaPagerLoader.back()
                    else
                        folderItemPagerLoader.back()
                }
                lastParentFolder = lastParentFolder?.parent
            }
        }

    private fun handleLoadNextAction(loadMoreAction: Flow<UiAction.LoadNext>) =
        viewModelScope.launch(Dispatchers.Default) {

            loadMoreAction.collect {
                lastParentFolder?.let {
                    when (it.type) {
                        FolderType.DLNA_SHARE -> {
                            dlnaPagerLoader.loadNext()
                        }

                        FolderType.FOLDER -> {
                            folderItemPagerLoader.loadNext()
                        }

                        else -> {}
                    }
                }
            }
        }


    private fun handleLoadPreAction(loadPreAction: Flow<UiAction.LoadPre>) = viewModelScope.launch {
        loadPreAction.collect {
            lastParentFolder?.let {
                when (it.type) {
                    FolderType.DLNA_SHARE -> {
                        dlnaPagerLoader.loadPre()
                    }

                    FolderType.FOLDER -> {
                        folderItemPagerLoader.loadPre()
                    }

                    else -> {}
                }
            }
        }
    }

    private fun updateLocalDevices(parentFolder: FolderItem, defaultPosition: Int = 0) {
        val deviceList = deviceDao.qureyAll()
        val folderItemList = deviceList.mapIndexed { index, device ->
            if (device.type == DeviceType.DEVICE_TYPE_INTERNAL_STORAGE) {
                FolderItem(
                    name = getString(R.string.device_internal_storage),
                    icon = R.mipmap.icon_folder,
                    path = device.path.withoutPathSeparator(),
                    friendlyPath = "${parentFolder.friendlyPath}${getString(R.string.device_internal_storage)}/",
                    type = FolderType.FOLDER,
                    pos = index,
                    parent = parentFolder
                )
            } else {
                FolderItem(
                    name = device.name,
                    icon = when (device.type) {
                        DeviceType.DEVICE_TYPE_USB,
                        DeviceType.DEVICE_TYPE_HARD_DISK,
                        DeviceType.DEVICE_TYPE_PCIE -> {
                            R.mipmap.icon_cloud
                        }

                        DeviceType.DEVICE_TYPE_SDCARDS -> {
                            R.mipmap.icon_sdcard
                        }

                        else -> {
                            R.mipmap.icon_folder
                        }
                    },
                    path = device.path.withoutPathSeparator(),
                    friendlyPath = "${parentFolder.friendlyPath}${device.name}/",
                    type = FolderType.FOLDER,
                    pos = index,
                    parent = parentFolder
                )
            }
        }
        _uiState.update {
            it.copy(
                isRoot = false,
                isReload = true,
                isAppend = false,
                isAddInFront = false,
                currentPath = "",
                friendlyPath = parentFolder.friendlyPath,
                rootList = folderItemList,
                focusPosition = if (folderItemList.size > defaultPosition) defaultPosition else folderItemList.size - 1
            )
        }
    }

    private fun updateSmbDevices(parentFolder: FolderItem, defaultPosition: Int = 0) {
        val shortcutList =
            shortcutDao.queryAllShortcutsByDevcietype(DeviceType.DEVICE_TYPE_SMB)
        val folderItemList = shortcutList.mapIndexed { index, shortcut ->
            val noAuthInfoUri = StringTools.hideSmbAuthInfo(shortcut.uri)
            val ip = Uri.parse(noAuthInfoUri).host

            FolderItem(
                name = ip ?: noAuthInfoUri,
                icon = R.mipmap.icon_samba,
                path = shortcut.uri.withoutPathSeparator(),
                friendlyPath = "${parentFolder.friendlyPath}${ip ?: noAuthInfoUri}/",
                type = FolderType.FOLDER,
                pos = index,
                parent = parentFolder
            )
        }
        _uiState.update {
            it.copy(
                isRoot = false,
                isReload = true,
                isAppend = false,
                isAddInFront = false,
                currentPath = "",
                friendlyPath = parentFolder.friendlyPath,
                rootList = folderItemList,
                focusPosition = if (folderItemList.size > defaultPosition) defaultPosition else folderItemList.size - 1
            )
        }
    }

    private fun updateDLNADevices(parentFolder: FolderItem, defaultPosition: Int = 0) {
        val shortcutList =
            shortcutDao.queryAllShortcutsByDevcietype(DeviceType.DEVICE_TYPE_DLNA)
        val folderList = shortcutList.let { it ->
            val dlnaDeviceSet = mutableSetOf<Pair<String, String>>()
            for (shortcut in it) {
                val splits = shortcut.uri.split(":")
                if (splits.size != 4)
                    continue
                dlnaDeviceSet.add(splits[0] to splits[2])
            }
            dlnaDeviceSet.toList().mapIndexed { index, pair ->
                FolderItem(
                    name = pair.second,
                    icon = R.mipmap.icon_dlna,
                    path = pair.first,
                    friendlyPath = "${parentFolder.friendlyPath}${pair.second}/",
                    type = FolderType.DLNA_GROUP,
                    pos = index,
                    parent = parentFolder
                )
            }
        }
        _uiState.update {
            it.copy(
                isRoot = false,
                isReload = true,
                isAppend = false,
                isAddInFront = false,
                currentPath = "",
                friendlyPath = parentFolder.friendlyPath,
                rootList = folderList,
                focusPosition = defaultPosition
            )
        }
    }

    private fun getString(res: Int): String {
        return getApplication<Application>().getString(res)
    }

    inner class DLNAPaginatedLoader : PaginatedDataLoader<FolderItem>() {
        var currentFolder: FolderItem? = null
        var focusPosition: Int = 0
        override fun getLimit(): Int {
            return PAGE_LIMIT
        }

        override fun getFirstLimit(): Int {
            return FIRST_PAGE_LIMIT
        }

        fun back() {
            currentFolder?.let { current ->
                current.parent?.let {
                    when (it.type) {
                        FolderType.DLNA -> {
                            updateDLNADevices(it,current.pos)
                        }

                        else -> {
                            backReload(it, current.pos)
                        }
                    }
                }
            }

        }


        fun reload(parentFolder: FolderItem) {
            this.currentFolder = parentFolder
            focusPosition=0
            super.reload()
        }

        fun backReload(parentFolder: FolderItem, position: Int) {
            this.currentFolder = parentFolder
            focusPosition = position
            super.reload(position)
        }

        override fun loadDataFromDB(offset: Int, limit: Int): List<FolderItem> {
            currentFolder?.let { folderItem ->
                when (folderItem.type) {
                    FolderType.DLNA_GROUP -> {
                        val shortcutList =
                            shortcutDao.queryAllShortcutsByDevcietype(DeviceType.DEVICE_TYPE_DLNA)
                        val folderList = shortcutList.filter {
                            val splits = it.uri.split(":")
                            if (splits.size != 4)
                                return@filter false
                            splits[0] == folderItem.path
                        }.let {
                            val dlnaDeviceSet = mutableSetOf<Pair<String, String>>()
                            for (shortcut in it) {
                                val splits = shortcut.uri.split(":")
                                if (splits.size != 4)
                                    continue
                                dlnaDeviceSet.add(shortcut.uri to splits[3])
                            }
                            dlnaDeviceSet.toList().mapIndexed { index, pair ->
                                FolderItem(
                                    name = pair.second,
                                    icon = R.mipmap.icon_folder,
                                    path = pair.first,
                                    friendlyPath = currentFolder?.friendlyPath.plus("${pair.second}/"),
                                    type = FolderType.DLNA_SHARE,
                                    pos = index,
                                    parent = folderItem
                                )
                            }
                        }
                        return folderList
                    }

                    FolderType.DLNA_SHARE -> {
                        val videoFileList =
                            videoFileDao.queryVideoFilesOnShortcut(folderItem.path, offset, limit)
                        return videoFileList.mapIndexed { index, it ->
                            FolderItem(
                                name = it.filename,
                                icon = R.mipmap.icon_mini_file,
                                path = it.path,
                                friendlyPath = currentFolder?.friendlyPath.plus("${it.filename}/"),
                                type = FolderType.FILE,
                                pos = offset + index,
                                parent = folderItem
                            )
                        }

                    }

                    else -> {}
                }
            }
            return emptyList()
        }

        override fun OnReloadResult(result: MutableList<FolderItem>?) {
            _uiState.update {
                var lastFocusPosition = 0
                result?.find {
                    it.pos == focusPosition
                }?.let {
                    lastFocusPosition = result.indexOf(it)
                }
                it.copy(
                    isRoot = false,
                    isReload = true,
                    isAppend = false,
                    isAddInFront = false,
                    currentPath = currentFolder?.path,
                    friendlyPath = currentFolder?.friendlyPath,
                    rootList = result ?: emptyList(),
                    focusPosition = result?.let { if (result.size > lastFocusPosition) lastFocusPosition else result.size - 1 }
                        ?: 0
                )
            }
        }

        override fun OnLoadNextResult(result: MutableList<FolderItem>?) {
            _uiState.update {
                it.copy(
                    isRoot = false,
                    isReload = false,
                    isAppend = true,
                    isAddInFront = false,
                    currentPath = currentFolder?.path,
                    rootList = result ?: emptyList(),
                )
            }
        }

    }

    inner class AllFilePaginatedLoader : PaginatedDataLoader<FolderItem>() {
        var currentFolder: FolderItem? = null
        var focusPosition: Int = 0
        override fun getLimit(): Int {
            return PAGE_LIMIT
        }

        override fun getFirstLimit(): Int {
            return FIRST_PAGE_LIMIT
        }

        fun reload(parentFolder: FolderItem) {
            this.currentFolder = parentFolder
            focusPosition = 0
            super.reload()
        }

        fun backReload(parentFolder: FolderItem, position: Int) {
            this.currentFolder = parentFolder
            focusPosition = position
            super.reload(position)
        }

        fun back() {
            currentFolder?.let { current ->
                current.parent?.let {
                    when (it.type) {
                        FolderType.DEVICE -> {
                            updateLocalDevices(it, current.pos)
                        }

                        FolderType.SMB -> {
                            updateSmbDevices(it, current.pos)
                        }

                        FolderType.DLNA -> {
                            updateDLNADevices(it, current.pos)
                        }

                        FolderType.FOLDER -> {
                            backReload(it, current.pos)
                        }

                        else -> {
                            Logger.d(it)
                        }
                    }
                }
            }
        }


        override fun loadDataFromDB(offset: Int, limit: Int): List<FolderItem> {
            currentFolder?.let {
                val parentFolder = it.path.withPathSeparator()
                val folderItemList = mutableListOf<FolderItem>()

                val queryFolderRegex = "${parentFolder.regexEscape()}[^/]+/.*"
                val queryFolders = SimpleSQLiteQuery(
                    QUERY_FOLDERS_SQL,
                    arrayOf(parentFolder, parentFolder, queryFolderRegex, offset, limit)
                )
                val folderList = videoFileDao.querySubFiles(
                    queryFolders
                )
                val remainingCapacity = limit - folderList.size

                folderList.forEachIndexed { index, it ->
                    folderItemList.add(
                        FolderItem(
                            name = "(${offset+index},$it)",
                            icon = R.mipmap.icon_folder,
                            path = "$parentFolder$it",
                            friendlyPath = currentFolder?.friendlyPath.plus("$it/"),
                            type = FolderType.FOLDER,
                            pos = offset + index,
                            parent = currentFolder
                        )
                    )
                }


                if (remainingCapacity > 0) {
                    val currentOffset = if (remainingCapacity == limit) offset else 0
                    val queryFilesRegex = "${parentFolder.regexEscape()}[^/]+\$"
                    val queryFiles = SimpleSQLiteQuery(
                        QUERY_FILES_SQL,
                        arrayOf(parentFolder, queryFilesRegex, currentOffset, remainingCapacity)
                    )
                    val filesList = videoFileDao.querySubFiles(queryFiles)
                    filesList.forEachIndexed { index, it ->
                        folderItemList.add(
                            FolderItem(
                                name = it,
                                icon = R.mipmap.icon_mini_file,
                                path = "$parentFolder$it",
                                friendlyPath = currentFolder?.friendlyPath.plus("$it/"),
                                type = FolderType.FILE,
                                pos = if (remainingCapacity == limit) offset + index else offset + (limit - remainingCapacity) + index,
                                parent = currentFolder
                            )
                        )
                    }
                }

                return folderItemList
            }
            return emptyList()
        }

        override fun OnReloadResult(result: MutableList<FolderItem>?) {
            _uiState.update {
                var lastFocusPosition = 0
                result?.find {
                    it.pos == focusPosition
                }?.let {
                    lastFocusPosition = result.indexOf(it)
                }
                it.copy(
                    isRoot = false,
                    isReload = true,
                    isAppend = false,
                    isAddInFront = false,
                    currentPath = currentFolder?.path,
                    friendlyPath = currentFolder?.friendlyPath,
                    rootList = result ?: emptyList(),
                    focusPosition = result?.let { if (result.size > lastFocusPosition) lastFocusPosition else result.size - 1 }
                        ?: 0
                )
            }
        }

        override fun OnLoadNextResult(result: MutableList<FolderItem>?) {
            _uiState.update {
                it.copy(
                    isRoot = false,
                    isReload = false,
                    isAppend = true,
                    isAddInFront = false,
                    currentPath = currentFolder?.path,
                    rootList = result ?: emptyList(),
                )
            }
        }

        override fun OnLoadPreResult(result: MutableList<FolderItem>?) {
            _uiState.update {
                it.copy(
                    isRoot = false,
                    isReload = false,
                    isAppend = false,
                    isAddInFront = true,
                    currentPath = currentFolder?.path,
                    rootList = result ?: emptyList(),
                )
            }
        }
    }

    private fun String.withoutPathSeparator(): String {
        if (this.endsWith("/")) {
            return this.substringBeforeLast("/")
        }
        return this
    }

    private fun String.withPathSeparator(): String {
        if (!this.endsWith("/")) {
            return this.plus("/")
        }
        return this
    }

    private fun String.regexEscape() = Regex.escape(this)

    companion object {
        const val PAGE_LIMIT = 18
        const val FIRST_PAGE_LIMIT = 24
    }
}

data class UiState(
    val isRoot: Boolean = true,
    val isReload: Boolean = true,
    val isAppend: Boolean = false,
    val isAddInFront: Boolean = false,
    val currentPath: String? = "",
    val friendlyPath: String? = "/",
    val rootList: List<FolderItem> = emptyList(),
    val isEmpty: Boolean = false,
    val focusPosition: Int = 0,
)

data class LoadingState(
    val isLoading: Boolean = false,
)


data class FolderItem(
    val name: String,
    val icon: Int = R.mipmap.icon_mini_file,
    val path: String,
    val friendlyPath: String,
    val type: FolderType,
    val pos: Int = -1,
    val parent: FolderItem? = null,
)

sealed class UiAction {
    object GoToRoot : UiAction()
    object BackAction : UiAction()
    data class ClickItem(val itemPosition: Int, val folderItem: FolderItem) : UiAction()
    object LoadNext : UiAction()
    object LoadPre : UiAction()
}


enum class FolderType {
    DEVICE,
    SMB,
    DLNA,
    DLNA_GROUP,
    DLNA_SHARE,
    FOLDER,
    FILE
}