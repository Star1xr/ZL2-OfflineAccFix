# 自定义主页使用指南！

欢迎使用 Zalith Launcher2 的自定义主页！你可以使用 **Markdown** 语法来编写主页！  
就像这个主页一样，你可以参考它的写法，相信你能看懂~

除了标准 Markdown 语法外，我们还提供了一系列**扩展组件**，让界面更丰富、布局更灵活！

---

## 注释

在源码中添加注释，可以解释某些内容的用途 ~~（防止自己看不懂XD）~~  
注释在渲染时会被**完全忽略**，不会显示在界面上

**正确示例：**  
// 这是一个单行注释，不会显示在界面上
  // 缩进后的注释同样有效
随便写点内容，注释被忽略了~  


**错误示例：**  
这行不是注释 // 这后面的文字会被当作普通文本渲染，因为 // 必须在行首


---

## 卡片
卡片是**内容分组容器**，内部支持完整的 Markdown 语法


### 基础卡片

...card-start title="最简单的卡片"
这是一个最简单的卡片，仅包含标题和一段文本 ~~（废话）~~
...card-end

### 支持的属性

| 属性               | 说明                                                                                                                | 示例                                                                                 |
|------------------|-------------------------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------|
| `title`          | 卡片标题。留空 `""` 则不显示标题栏                                                                                              | `title="欢迎页"`                                                                      |
| `contentPadding` | 内边距（单位 dp）<br>支持 1、2 或 4 个值，分别代表：(所有方向的边距)、(水平内边距, 垂直内边距)、(左内边距, 上内边距, 右内边距, 下内边距)                                | `contentPadding=(12)`<br>`contentPadding=(8, 16)`<br>`contentPadding=(4, 8, 4, 8)` |
| `shape`          | 圆角大小<br>可选：`extraSmall`, `small`, `medium`, `large`, `extraLarge`<br>或直接指定 `12dp`<br>或百分比（如 `50` 表示 50% 圆角，可形成圆形） | `shape=large`<br>`shape=16dp`<br>`shape=50`                                        |

...card-start title="示例卡片" shape=large contentPadding=(10)
这里是卡片内部，你可以使用 **加粗**、*斜体* 等 Markdown 语法

- **title**：卡片标题，留空则不显示标题块
- **contentPadding**：内边距
- **shape**：圆角样式，支持 Material 预定义值、dp 数值或百分比
...card-end

> **注意**：卡片内部不能嵌套卡片，否则内部卡片会被当作普通文本渲染

---

## 按钮
按钮用于触发交互事件，也可以只做展示

### 基础语法

按钮必须包含 `text`，`event` 可选：
...button text="纯展示按钮"
...button text="访问哔哩哔哩" event="url=https://www.bilibili.com/"
...button text="检查更新" event="launcher=check_update"

- **url**：在系统浏览器中打开指定链接  
- **launcher**：触发启动器内部定义的事件标签  

### 按钮样式

我们提供四种 Material Design 3 按钮样式：
...button text="默认样式"
...button-outlined text="轮廓样式"
...button-filled-tonal text="色调填充样式"
...button-text text="文本样式"

样式通过 `...button` 的后缀指定：
- 无后缀 → 填充样式（Filled）
- `-outlined` → 轮廓样式（Outlined）
- `-filled-tonal` → 色调填充样式（FilledTonal）
- `-text` → 纯文本样式（Text）

---

## 横向布局（Row）
使用 `Row` 组件可以将内部的**按钮和图片**水平排列，避免默认的竖向布局

### 基础用法

...row-start
    //对的我们可以对子组件进行行首缩进！
    //不缩进也没关系，只是护眼
    //以及，想缩进多少空格都可以
    ...button text="按钮1"
    ...button-outlined text="按钮2"
...row-end

### Row 详细配置
Row 支持两个主要属性，与 Jetpack Compose 原生的 Row 组件对齐：

#### 1. `horizontal`（水平分布）

**常规值：**
- `Start`（默认）
- `Center`
- `End`
- `SpaceBetween`
- `SpaceAround`
- `SpaceEvenly`

...row-start horizontal=SpaceBetween
    ...button text="左"
    ...button text="中"
    ...button text="右"
...row-end

**带间距的分布：**
...row-start horizontal=spacedBy(12)
    ...button text="按钮A"
    ...button text="按钮B"
...row-end

你还可以指定水平对齐方式（`Start`、`Center`、`End`）：
...row-start horizontal=spacedBy(12, End)
    ...button text="位于末尾，且间距12"
    ...image url="https://www.baidu.com/img/flexible/logo/pc/result.png" width=20%
...row-end

#### 2. `vertical`（垂直对齐）

- `Top`（默认）
- `Center`
- `Bottom`

...row-start vertical=Center
    ...button text="垂直居中"
    ...image url="https://www.baidu.com/img/flexible/logo/pc/result.png" width=10%
...row-end

### 权重（Weight）
让子元素按比例占满剩余空间  
在 Row 组件内部，你可以为每个按钮或图片指定 `weight` 参数，控制它们在剩余空间中的占比

- 语法：`weight=(权重值)` 或 `weight=(权重值, noFill)`
- `权重值`：一个小数或者整数，例如 `1`、`2.5`
- `noFill`（可选）：加上 `noFill` 后该元素不会强制填满分配的空间，由自身内容决定宽度

...row-start
    ...button weight=(1) text="占1份"
    ...button weight=(2) text="占2份"
    ...button weight=(3, noFill) text="占3份，但不填充"
...row-end

> 如果希望某个元素不占权重、保持原始宽度，就不要写 `weight` 参数

---

## 图片组件
增强的图片组件，支持**百分比宽度**和**固定 DP 宽度**，还可以自定义圆角

### 支持的属性

| 属性      | 说明                  | 示例                                                            |
|---------|---------------------|---------------------------------------------------------------|
| `url`   | 图片链接，这个属性是必填项       | `url="https://www.baidu.com/img/flexible/logo/pc/result.png"` |
| `width` | 宽度：数字（单位 dp）或百分比字符串 | `width=150`<br>`width=50%`                                    |
| `shape` | 圆角，用法同卡片            | `shape=medium`<br>`shape=20dp`<br>`shape=50`                  |

### 示例

宽度：50% 占主页整体宽度的 50%  
形状：medium 中等的圆角  
...image url="https://picsum.photos/300/200" width=50% shape=medium
宽度：48 大小为 48dp    
形状：50 百分比圆角，圆形  
...image url="https://picsum.photos/300/200" width=48 shape=50


---

## 注意事项
- 卡片禁止嵌套，否则内部卡片会被当作普通文本  
- Row 内部目前只支持按钮和图片
- 权重（weight）仅对 Row 内的子元素有效，并且必须写在对应子组件的同一行参数中  
- 标签必须成对出现：`...card-start` 与 `...card-end`、`...row-start` 与 `...row-end` 必须配对  
- 扩展组件不能嵌入标准 Markdown 容器：扩展组件相对独立，并没有彻底融入 Markdown 语法，例如无法将 `...card-start` 写在 Markdown 的列表项或引用块内部  
- 图片加载依赖网络，请确保图片链接可访问  

虽然我们提供了丰富的扩展组件，但整个主页仍然基于 **Markdown**  
如果你还不熟悉标准 Markdown 语法，建议先花几分钟学习一下，真的很好学！  
[Markdown 菜鸟教程](https://www.runoob.com/markdown/md-tutorial.html)
