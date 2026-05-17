/*
 * Zalith Launcher 2
 * Copyright (C) 2025 MovTery <movtery228@qq.com> and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.star1xr.treelauncher.ui.control

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.star1xr.treelauncher.R
import com.star1xr.treelauncher.bridge.ZLBridgeStates
import com.star1xr.treelauncher.game.keycodes.HOTBAR_1
import com.star1xr.treelauncher.game.keycodes.HOTBAR_1_VALUE
import com.star1xr.treelauncher.game.keycodes.HOTBAR_2
import com.star1xr.treelauncher.game.keycodes.HOTBAR_2_VALUE
import com.star1xr.treelauncher.game.keycodes.HOTBAR_3
import com.star1xr.treelauncher.game.keycodes.HOTBAR_3_VALUE
import com.star1xr.treelauncher.game.keycodes.HOTBAR_4
import com.star1xr.treelauncher.game.keycodes.HOTBAR_4_VALUE
import com.star1xr.treelauncher.game.keycodes.HOTBAR_5
import com.star1xr.treelauncher.game.keycodes.HOTBAR_5_VALUE
import com.star1xr.treelauncher.game.keycodes.HOTBAR_6
import com.star1xr.treelauncher.game.keycodes.HOTBAR_6_VALUE
import com.star1xr.treelauncher.game.keycodes.HOTBAR_7
import com.star1xr.treelauncher.game.keycodes.HOTBAR_7_VALUE
import com.star1xr.treelauncher.game.keycodes.HOTBAR_8
import com.star1xr.treelauncher.game.keycodes.HOTBAR_8_VALUE
import com.star1xr.treelauncher.game.keycodes.HOTBAR_9
import com.star1xr.treelauncher.game.keycodes.HOTBAR_9_VALUE
import com.star1xr.treelauncher.game.keycodes.LwjglGlfwKeycode
import com.star1xr.treelauncher.game.keycodes.mapToKeycode
import com.star1xr.treelauncher.game.launch.MCOptions
import kotlinx.coroutines.delay

enum class HotbarRule(val nameRes: Int) {
    /**
     * 自动计算(一些情况下并不精准)
     */
    Auto(R.string.game_menu_option_hotbar_rule_auto),

    /**
     * 完全自定义大小
     */
    Custom(R.string.game_menu_option_hotbar_rule_custom)
}

/**
 * 自定义大小：0~1000比例下，计算百分比值
 */
fun Int.hotbarPercentage() = this / 1000f

/**
 * 快捷栏按键绑定键
 */
private val hotbarList = listOf(
    HOTBAR_1 to HOTBAR_1_VALUE,
    HOTBAR_2 to HOTBAR_2_VALUE,
    HOTBAR_3 to HOTBAR_3_VALUE,
    HOTBAR_4 to HOTBAR_4_VALUE,
    HOTBAR_5 to HOTBAR_5_VALUE,
    HOTBAR_6 to HOTBAR_6_VALUE,
    HOTBAR_7 to HOTBAR_7_VALUE,
    HOTBAR_8 to HOTBAR_8_VALUE,
    HOTBAR_9 to HOTBAR_9_VALUE,
)

private val keyList = listOf(
    LwjglGlfwKeycode.GLFW_KEY_1,
    LwjglGlfwKeycode.GLFW_KEY_2,
    LwjglGlfwKeycode.GLFW_KEY_3,
    LwjglGlfwKeycode.GLFW_KEY_4,
    LwjglGlfwKeycode.GLFW_KEY_5,
    LwjglGlfwKeycode.GLFW_KEY_6,
    LwjglGlfwKeycode.GLFW_KEY_7,
    LwjglGlfwKeycode.GLFW_KEY_8,
    LwjglGlfwKeycode.GLFW_KEY_9
)

/**
 * Minecraft 快捷栏判定箱
 * 根据屏幕分辨率定位 MC 的快捷栏位置
 * 点击、滑动快捷栏，会计算指针处于哪个槽位中，并触发 [onClickSlot] 回调
 *
 * @param isGrabbing 处于鼠标抓获模式下，才会开启判定箱
 * @param resolutionRatio 当前分辨率缩放
 */
@Composable
fun BoxScope.MinecraftHotbar(
    screenSize: IntSize,
    rule: HotbarRule,
    widthPercentage: Float,
    heightPercentage: Float,
    onClickSlot: (key: Int) -> Unit,
    isGrabbing: Boolean = false,
    resolutionRatio: Int,
    onOccupiedPointer: (PointerId) -> Unit,
    onReleasePointer: (PointerId) -> Unit
) {
    val density = LocalDensity.current

    var hotbarSize by remember { mutableStateOf(DpSize(0.dp, 0.dp)) }
    val hotbarUpdateAnim = remember { Animatable(0f) }

    when (rule) {
        HotbarRule.Auto -> {
            val optionsChangeKey by MCOptions.refreshKey.collectAsStateWithLifecycle()
            val windowChangeKey by ZLBridgeStates.windowChangeKey.collectAsStateWithLifecycle()
            LaunchedEffect(
                isGrabbing, optionsChangeKey, screenSize, density,
                resolutionRatio, windowChangeKey
            ) {
                val guiScale = getMCGuiScale(
                    width = (screenSize.width * resolutionRatio / 100f).toInt(),
                    height = (screenSize.height * resolutionRatio / 100f).toInt()
                )
                val slotSize = guiScale * 20

                with(density) {
                    hotbarSize = DpSize((slotSize * hotbarList.size).toDp(), slotSize.toDp())
                }
            }
        }
        HotbarRule.Custom -> {
            var isInitialized by remember { mutableStateOf(false) }

            LaunchedEffect(
                widthPercentage, heightPercentage
            ) {
                val width = (screenSize.width * widthPercentage).toInt()
                val height = (screenSize.height * heightPercentage).toInt()

                with(density) {
                    hotbarSize = DpSize(width.toDp(), height.toDp())
                }

                if (isInitialized) {
                    hotbarUpdateAnim.snapTo(0.5f)
                    delay(1000)
                    hotbarUpdateAnim.animateTo(0f, tween(800))
                } else {
                    isInitialized = true
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .size(hotbarSize)
            .align(Alignment.BottomCenter)
            .then(
                if (rule == HotbarRule.Custom) Modifier.background(Color.Red.copy(alpha = hotbarUpdateAnim.value))
                else Modifier
            )
            .then(
                if (isGrabbing) Modifier
                    .mainTouchLogic(
                        slotCount = hotbarList.size,
                        hotbarSize = hotbarSize,
                        density = density,
                        onClickSlot = { index: Int ->
                            val pair = hotbarList[index]
                            val keyCode = mapToKeycode(pair.first, pair.second)
                                ?: keyList[index].toInt()

                            onClickSlot(keyCode)
                        },
                        onOccupiedPointer = onOccupiedPointer,
                        onReleasePointer = onReleasePointer
                    )
                else Modifier
            )
    )
}

private fun Modifier.mainTouchLogic(
    slotCount: Int,
    hotbarSize: DpSize,
    density: Density,
    onClickSlot: (index: Int) -> Unit,
    onOccupiedPointer: (PointerId) -> Unit,
    onReleasePointer: (PointerId) -> Unit
) = this.pointerInput(slotCount, hotbarSize, density) {
    awaitPointerEventScope {
        /** 所有被占用的指针 */
        val occupiedPointers = mutableSetOf<PointerId>()
        var lastSlotIndex = -1

        while (true) {
            val event = awaitPointerEvent(PointerEventPass.Initial)

            event.changes.forEach { change ->
                if (!change.isConsumed) {
                    val pointerId = change.id
                    if (pointerId !in occupiedPointers) {
                        onOccupiedPointer(pointerId)
                        occupiedPointers.add(pointerId)
                    }

                    change.consume()

                    when {
                        //手指刚按下
                        change.pressed && !change.previousPressed -> {
                            val x = change.position.x
                            val currentSlotIndex = calculateSlotIndex(x, hotbarSize, slotCount, density)

                            if (currentSlotIndex != -1) {
                                onClickSlot(currentSlotIndex)
                                lastSlotIndex = currentSlotIndex
                            }
                        }
                        //按下、滑动
                        change.pressed && change.previousPressed -> {
                            val x = change.position.x
                            val currentSlotIndex = calculateSlotIndex(x, hotbarSize, slotCount, density)

                            if (currentSlotIndex != -1 && currentSlotIndex != lastSlotIndex) {
                                onClickSlot(currentSlotIndex)
                                lastSlotIndex = currentSlotIndex
                            }
                        }
                        //松开手指
                        !change.pressed && change.previousPressed -> {
                            lastSlotIndex = -1
                            if (pointerId in occupiedPointers) {
                                occupiedPointers.remove(pointerId)
                                onReleasePointer(pointerId)
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun getMCGuiScale(width: Int, height: Int): Int {
    val guiScale = MCOptions.get("guiScale")?.toIntOrNull() ?: 4
    val scale = minOf(width / 320, height / 240).coerceAtLeast(1)
    return if (scale < guiScale || guiScale == 0) scale else guiScale
}

private fun calculateSlotIndex(
    x: Float,
    hotbarSize: DpSize,
    slotCount: Int,
    density: Density
): Int {
    val totalWidth = with(density) { hotbarSize.width.toPx() }
    val slotWidth = totalWidth / slotCount
    return (x / slotWidth).toInt().coerceIn(0, slotCount - 1)
}