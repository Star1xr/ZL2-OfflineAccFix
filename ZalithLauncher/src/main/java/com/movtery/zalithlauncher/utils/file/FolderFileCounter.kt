package com.movtery.zalithlauncher.utils.file

import java.io.File

/**
 * 记录文件夹内文件数量
 */
class FolderFileCounter(
    private val dir: File
) {
    private var counts: Int? = null

    /**
     * 记录目录内的文件数量，并检查是否有变化
     * @return 当前目录
     */
    fun checkDir(): Boolean {
        val tempCount = if (dir.isFile) {
            0
        } else {
            dir.list()?.size ?: 0
        }
        val result = getRecordedCount() != tempCount
        counts = tempCount
        return result
    }

    /**
     * 获取上一次记录的目录内的文件数量
     */
    fun getRecordedCount(): Int = counts ?: 0

    /**
     * 当前是否从未检查过文件数量
     */
    fun isUnchecked(): Boolean = counts == null
}