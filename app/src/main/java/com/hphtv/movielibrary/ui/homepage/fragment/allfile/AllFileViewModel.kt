package com.hphtv.movielibrary.ui.homepage.fragment.allfile

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hphtv.movielibrary.R
import com.hphtv.movielibrary.data.Constants.DeviceType
import com.hphtv.movielibrary.data.pagination.PaginatedDataLoader
import com.hphtv.movielibrary.roomdb.MovieLibraryRoomDatabase
import com.hphtv.movielibrary.ui.homepage.fragment.SimpleLoadingObserver
import com.hphtv.movielibrary.util.MovieHelper
import com.hphtv.movielibrary.util.StringTools
import com.hphtv.movielibrary.util.rxjava.SimpleObserver
import com.orhanobut.logger.Logger
import io.reactivex.rxjava3.core.ObservableSource
import io.reactivex.rxjava3.functions.Function
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AllFileViewModel(application: Application) : AndroidViewModel(application) {

    private val deviceDao = MovieLibraryRoomDatabase.getDatabase(application).deviceDao
    private val shortcutDao = MovieLibraryRoomDatabase.getDatabase(application).shortcutDao
    private val videoFileDao = MovieLibraryRoomDatabase.getDatabase(application).videoFileDao
    private val _uiState: MutableStateFlow<UiState> = MutableStateFlow(UiState())

    private var lastFocusPosition = 0

    val uiState: StateFlow<UiState> = _uiState.asStateFlow()
    var accept: (UiAction) -> Unit

    var folderItemPagerLoader = AllFilePaginatedLoader()

    init {
        accept = initAcceptAction()
    }

    fun initAcceptAction(): (UiAction) -> Unit {
        val actionStateFlow: MutableSharedFlow<UiAction> = MutableSharedFlow()
        val gotoRootAction = actionStateFlow.filterIsInstance<UiAction.GoToRoot>()
        val clickItemAction = actionStateFlow.filterIsInstance<UiAction.ClickItem>()

        handleGotoRootAction(gotoRootAction)
        handleItemClickAction(clickItemAction)

        return { action ->
            viewModelScope.launch {
                actionStateFlow.emit(action)
            }
        }
    }


    private fun handleGotoRootAction(gotoRootAction: Flow<UiAction.GoToRoot>) =
        viewModelScope.launch(Dispatchers.IO) {
            lastFocusPosition = 0
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
            deviceTypes.forEach {
                when (it) {
                    DeviceType.DEVICE_TYPE_LOCAL -> {
                        rootList.add(
                            FolderItem(
                                getString(R.string.filter_box_local_device),
                                R.mipmap.icon_folder,
                                "",
                                FolderType.DEVICE,
                            )
                        )
                    }

                    DeviceType.DEVICE_TYPE_SMB -> {
                        rootList.add(
                            FolderItem(
                                getString(R.string.filter_box_smb_device),
                                R.mipmap.icon_samba,
                                "",
                                FolderType.SMB,
                            )
                        )
                    }

                    DeviceType.DEVICE_TYPE_DLNA -> {
                        rootList.add(
                            FolderItem(
                                getString(R.string.filter_box_dlna_device),
                                R.mipmap.icon_dlna,
                                "",
                                FolderType.DLNA
                            )
                        )
                    }
                }
            }

            gotoRootAction.collect {
                _uiState.update {
                    it.copy(
                        isRoot = true,
                        currentPath = "",
                        rootList = rootList,
                        focusPosition = lastFocusPosition
                    )
                }
            }
        }

    private fun handleItemClickAction(clickItemAction: Flow<UiAction.ClickItem>) =
        viewModelScope.launch(Dispatchers.IO) {
            clickItemAction.collect {
                lastFocusPosition = it.itemPosition
                it.folderItem.let {
                    when (it.type) {
                        FolderType.DEVICE -> {
                            updateLocalDevices(it)
                        }

                        FolderType.SMB -> {
                            updateSmbDevices(it)
                        }

                        FolderType.DLNA -> {

                        }

                        FolderType.FOLDER -> {
                            folderItemPagerLoader.reload(it)
                        }

                        FolderType.FILE -> {
                            _uiState.update {
                                it.copy(isLoading = true)
                            }
                            MovieHelper.playingMovie(it.path, it.name)
                                .flatMap(MovieHelper::updateHistory)
                                .subscribe(object : SimpleObserver<String>() {
                                    override fun onAction(t: String?) {
                                        _uiState.update {
                                            it.copy(isLoading = false)
                                        }
                                    }
                                })
                        }

                        FolderType.BACK -> {
                            if (uiState.value.currentPath?.isEmpty() == true) {
                                accept(UiAction.GoToRoot)
                            } else {
                                folderItemPagerLoader.back()
                            }
                        }


                    }
                }


            }
        }

    private fun updateLocalDevices(parentFolder: FolderItem) {
        val deviceList = deviceDao.qureyAll()
        val folderItemList = deviceList.map { device ->
            if (device.type == DeviceType.DEVICE_TYPE_INTERNAL_STORAGE) {
                FolderItem(
                    getString(R.string.device_internal_storage),
                    R.mipmap.icon_folder,
                    device.path.withoutPathSeparator(),
                    FolderType.FOLDER,
                    parentFolder
                )
            } else {
                FolderItem(
                    device.name,
                    when (device.type) {
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
                    device.path.withoutPathSeparator(),
                    FolderType.FOLDER,
                    parentFolder
                )
            }
        }.withAppendBackBtn()
        _uiState.update {
            it.copy(
                isRoot = false,
                currentPath = "",
                rootList = folderItemList,
                focusPosition = if (folderItemList.size > lastFocusPosition) lastFocusPosition else folderItemList.size - 1
            )
        }
    }

    private fun updateSmbDevices(parentFolder: FolderItem) {
        val shortcutList =
            shortcutDao.queryAllShortcutsByDevcietype(DeviceType.DEVICE_TYPE_SMB)
        val folderItemList = shortcutList.map { shortcut ->
            val noAuthInfoUri = StringTools.hideSmbAuthInfo(shortcut.uri)
            val ip = Uri.parse(noAuthInfoUri).host

            FolderItem(
                ip ?: noAuthInfoUri,
                R.mipmap.icon_samba,
                shortcut.uri.withoutPathSeparator(),
                FolderType.FOLDER,
                parentFolder
            )
        }.withAppendBackBtn()
        _uiState.update {
            it.copy(
                isRoot = false,
                currentPath = "",
                rootList = folderItemList,
                focusPosition = if (folderItemList.size > lastFocusPosition) lastFocusPosition else folderItemList.size - 1
            )
        }
    }

    private fun getString(res: Int): String {
        return getApplication<Application>().getString(res)
    }

    private fun List<FolderItem>.withAppendBackBtn(): List<FolderItem> {
        return this.toMutableList().apply {
            add(
                0, FolderItem(
                    getString(R.string.goback),
                    R.mipmap.icon_mini_back,
                    "",
                    FolderType.BACK
                )
            )
        }
    }


    inner class AllFilePaginatedLoader : PaginatedDataLoader<FolderItem>() {
        var currentFolder: FolderItem? = null

        override fun getLimit(): Int {
            return PAGE_LIMIT
        }

        override fun getFirstLimit(): Int {
            return FIRST_PAGE_LIMIT
        }

        fun reload(parentFolder: FolderItem) {
            this.currentFolder = parentFolder
            super.reload()
        }

        fun back() {
            Logger.d(currentFolder?.path)
            currentFolder?.let { it ->
                it.parent?.let {
                    when (it.type) {
                        FolderType.DEVICE -> {
                            updateLocalDevices(it)
                        }

                        FolderType.SMB -> {
                            updateSmbDevices(it)
                        }

                        FolderType.DLNA -> {}
                        FolderType.FOLDER -> {
                            reload(it)
                        }

                        else -> {
                            Logger.d(it)
                        }
                    }
                }
            }
        }

        override fun reloadDataFromDB(offset: Int, limit: Int): List<FolderItem> {
            return super.reloadDataFromDB(offset, limit).withAppendBackBtn()
        }

        override fun loadDataFromDB(offset: Int, limit: Int): List<FolderItem> {
            currentFolder?.let {
                val filePathList = videoFileDao.queryFilePathList(it.path.withPathSeparator() + "%")
                val folderSet = mutableSetOf<String>()
                val fileNameList = mutableListOf<String>()
                filePathList.map { filePath ->
                    val pos = filePath.substringAfter(it.path.withPathSeparator()).indexOf("/")
                    if (pos >= 0) {
                        val folderName =
                            filePath.substringAfter(it.path.withPathSeparator()).substring(0, pos)
                        folderSet.add(folderName)
                    } else {
                        val fileName = filePath.substringAfter(it.path.withPathSeparator())
                        fileNameList.add(fileName)
                    }
                }
                val folderItemList = folderSet.map {
                    FolderItem(
                        it,
                        R.mipmap.icon_folder,
                        "${currentFolder?.path}/$it",
                        FolderType.FOLDER,
                        currentFolder
                    )
                }

                val fileItemList = fileNameList.map {
                    FolderItem(
                        it,
                        R.mipmap.icon_mini_file,
                        "${currentFolder?.path}/$it",
                        FolderType.FILE,
                        currentFolder
                    )
                }

                return folderItemList.toMutableList().apply { addAll(fileItemList) }
            }
            return emptyList()
        }

        override fun OnReloadResult(result: MutableList<FolderItem>?) {
            _uiState.update {
                it.copy(
                    isRoot = false,
                    isAppend = false,
                    currentPath = currentFolder?.path,
                    rootList = result ?: emptyList(),
                    focusPosition = result?.let { if (result.size > lastFocusPosition) lastFocusPosition else result.size - 1 }
                        ?: 0
                )
            }
        }

        override fun OnLoadResult(result: MutableList<FolderItem>?) {
            _uiState.update {
                it.copy(
                    isRoot = false,
                    isAppend = true,
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

    companion object {
        const val PAGE_LIMIT = 12
        const val FIRST_PAGE_LIMIT = 18
    }
}

data class UiState(
    val isRoot: Boolean = true,
    val isAppend: Boolean = false,
    val currentPath: String? = "",
    val rootList: List<FolderItem> = emptyList(),
    val isLoading: Boolean = false,
    val isEmpty: Boolean = false,
    val focusPosition: Int = 0
)

data class FolderItem(
    val name: String,
    val icon: Int = R.mipmap.icon_mini_file,
    val path: String,
    val type: FolderType,
    val parent: FolderItem? = null,
)

sealed class UiAction {
    object GoToRoot : UiAction()
    data class ClickItem(val itemPosition: Int, val folderItem: FolderItem) : UiAction()
}


enum class FolderType {
    BACK,
    DEVICE,
    SMB,
    DLNA,
    FOLDER,
    FILE
}