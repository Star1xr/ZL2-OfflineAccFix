package com.movtery.zalithlauncher.viewmodel

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.context.copyAssetFile
import com.movtery.zalithlauncher.path.PathManager
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.setting.enums.HomePageType
import com.movtery.zalithlauncher.ui.components.SimpleAlertDialog
import com.movtery.zalithlauncher.ui.screens.main.custom_home.MarkdownBlock
import com.movtery.zalithlauncher.ui.screens.main.custom_home.parseMarkdownBlocks
import com.movtery.zalithlauncher.utils.isInGreaterChina
import com.movtery.zalithlauncher.utils.logging.Logger.lWarning
import com.movtery.zalithlauncher.utils.network.fetchStringFromUrl
import com.movtery.zalithlauncher.utils.string.isEmptyOrBlank
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.commons.io.FileUtils
import java.io.File

class HomePageViewModel : ViewModel() {
    private val _pageState = MutableStateFlow<HomePageState>(HomePageState.Loading)
    /** 启动器主页状态 */
    val pageState = _pageState.asStateFlow()

    private val _pageOp = MutableStateFlow<HomePageOperation>(HomePageOperation.None)
    /** 启动器主页操作流 */
    val pageOp = _pageOp.asStateFlow()

    fun updateOperation(
        operation: HomePageOperation
    ) {
        _pageOp.update { operation }
    }

    private var reloadJob: Job? = null
    /**
     * 重载主页
     */
    fun reloadPage() {
        reloadJob?.cancel()
        reloadJob = viewModelScope.launch {
            _pageState.update { HomePageState.Loading }
            val type = AllSettings.homePageType.getValue()
            when (type) {
                HomePageType.Blank -> {
                    _pageState.update { HomePageState.Blank }
                }
                HomePageType.FromLocal -> {
                    val page = reloadPageFromLocal()
                    _pageState.update { HomePageState.None(page) }
                }
                HomePageType.FromURL -> {
                    val page = reloadPageFromURL()
                    _pageState.update { HomePageState.None(page) }
                }
            }
            reloadJob = null
        }
    }

    private val localPageFile: File get() = File(PathManager.DIR_FILES_EXTERNAL, "home_page.md")
    /** 本地主页文件是否存在 */
    fun isLocalExists(): Boolean = localPageFile.exists()

    /**
     * 从本地文件加载主页内容
     */
    private suspend fun reloadPageFromLocal(): List<MarkdownBlock> {
        val file = localPageFile
        return if (file.exists() && file.isFile) {
            withContext(Dispatchers.IO) {
                runCatching {
                    val content = file.readText()
                    parseMarkdownBlocks(content)
                }.onFailure { e ->
                    if (e is CancellationException) return@onFailure
                    lWarning("Failed to load the homepage from the local device!", e)
                }.getOrDefault(emptyList())
            }
        } else {
            emptyList()
        }
    }

    private var genJob: Job? = null
    /** 生成示例文档主页 */
    fun genDocPage(
        context: Context
    ) {
        genJob?.cancel()
        genJob = viewModelScope.launch(Dispatchers.IO) {
            _pageState.update { HomePageState.Loading }
            runCatching {
                if (localPageFile.exists()) {
                    //删除本地的主页文件后，再解压
                    FileUtils.deleteQuietly(localPageFile)
                }
                val isChinese = isInGreaterChina()
                context.copyAssetFile(
                    fileName = if (isChinese) {
                        "home_page/doc_page_zh.md"
                    } else {
                        "home_page/doc_page_en.md"
                    },
                    output = localPageFile,
                    overwrite = true //以防解压失败
                )
            }.onFailure { e ->
                lWarning("Failed to extract the document homepage from Assets!", e)
            }
            genJob = null
            reloadPage()
        }
    }

    /**
     * 从网络地址加载启动器主页
     */
    private suspend fun reloadPageFromURL(): List<MarkdownBlock> = withContext(Dispatchers.IO) {
        val url = AllSettings.homePageURL.getValue()
        if (url.isEmptyOrBlank()) {
            emptyList()
        } else {
            runCatching {
                val content = fetchStringFromUrl(url)
                parseMarkdownBlocks(content)
            }.onFailure { e ->
                if (e is CancellationException) return@onFailure
                lWarning("Failed to retrieve the homepage from the network!", e)
            }.getOrDefault(emptyList())
        }
    }

    init {
        reloadPage()
    }

    override fun onCleared() {
        _pageState.value = HomePageState.Blank
        reloadJob?.cancel()
        reloadJob = null
        genJob?.cancel()
        genJob = null
    }
}

/** 启动器主页状态 */
sealed interface HomePageState {
    /** 加载中 */
    data object Loading : HomePageState
    /** 加载完成，展示主页 */
    data class None(val page: List<MarkdownBlock>) : HomePageState
    /** 空白主页 */
    data object Blank : HomePageState
}

/** 启动器主页操作状态 */
sealed interface HomePageOperation {
    data object None : HomePageOperation
    /** 警告用户将要覆盖本地已有的主页文件 */
    data object WarningOverwrite : HomePageOperation
}

/**
 * 启动器主页操作流程
 * @param onGenDocPage 用户确定要覆盖本地主页
 */
@Composable
fun HomePageOperation(
    operation: HomePageOperation,
    onChange: (HomePageOperation) -> Unit,
    onGenDocPage: () -> Unit
) {
    when (operation) {
        HomePageOperation.None -> {}
        HomePageOperation.WarningOverwrite -> {
            SimpleAlertDialog(
                title = stringResource(R.string.generic_warning),
                text = stringResource(R.string.settings_launcher_home_page_type_local_gen_doc_exists),
                onDismiss = {
                    onChange(HomePageOperation.None)
                },
                onConfirm = {
                    onGenDocPage()
                    onChange(HomePageOperation.None)
                }
            )
        }
    }
}

val LocalHomePageViewModel = compositionLocalOf<HomePageViewModel> {
    error("No HomePageViewModel provided")
}