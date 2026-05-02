# 自定义主页使用指南！

欢迎使用 Zalith Launcher2 的自定义主页！你可以使用 Markdown 语法来编写主页！  
就像这个主页一样，你可以参考它的写法，相信你能看懂~  

当然，除了标准 Markdown 语法外，我们还提供了一些**扩展组件**来增强界面交互！


---


### 注释
我们可以使用简单的注释，对主页某些内容进行解释说明 ~~（防止自己看不懂XD）~~  
注释用于在源码中添加备注，渲染时会被完全忽略  
在行首添加`//`，那么该行会被视为注释行  


**正确示例：**  
// 这是一个单行注释，不会显示在界面上
  // 缩进后的注释同样有效
随便写点内容，注释被忽略了~  


**错误示例：**  
这行不是注释 // 这后面的文字会被当作普通文本渲染，因为 // 必须在行首


---


### 卡片
卡片是将内容分组的容器，其内部支持完整的 Markdown 语法

**下面展示了一个基础的卡片**  
...card-start title="最简单的卡片"
这是一个最简单的卡片，仅包含标题和一段文本 ~~（废话）~~
...card-end

**支持属性**：title, contentPadding, shape  
你可以使用属性控制这个卡片的外观：  

> title="自定义样式卡片"
> shape=large
> contentPadding=(10)

...card-start title="自定义样式卡片" shape=large contentPadding=(10)
这里是卡片内部，你可以使用 Markdown 的语法 **加粗**、*斜体*  
接下来是卡片属性的详细介绍~  

- **title**: 卡片的标题。留空 `""` 则不显示标题栏
    - ```text
      title="这是标题！"
      ```
- **contentPadding**: 边距。支持 `(全边距)`、`(水平, 垂直)` 或 `(左, 上, 右, 下)`。
    - ```text
      12的内边距
      contentPadding=(12)
      水平0内边距，垂直12内边距
      contentPadding=(0, 12)
      左0内边距，上12内边距，右12内边距，下0内边距
      contentPadding=(0, 12, 12, 0)
      ```
- **shape**: 圆角。支持 `extraSmall`, `small`, `medium`, `large`, `extraLarge` 或具体数值如 `16dp`
    - ```text
      使用一个非常小的圆角，由MaterialTheme提供！
      shape=extraSmall
      使用一个比较小的圆角，由MaterialTheme提供！
      shape=small
      使用一个中等的圆角，由MaterialTheme提供！
      shape=medium
      使用一个比较大的圆角，由MaterialTheme提供！
      shape=large
      使用一个非常大的圆角，由MaterialTheme提供！
      shape=extraLarge
      Jetpack Compose使用dp作为测量单位，这里可以指定你想要多大的圆角
      shape=16dp
      若不指定dp为单位，我们将视为你想使用百分比圆角，例如50，是一个完美的圆形
      shape=50
      ```
...card-end

...card-start title="注意事项" shape=large contentPadding=(10)
使用卡片时，我们需要注意这些事情：
1. 卡片是一个允许嵌套子组件的组件，为了框定内容，我们必须使用开始标签和结束标签来闭合内容，不然启动器没法知道你这个卡片里头到底有哪些组件
2. 卡片内部是不允许进行嵌套的！如果硬要嵌套，则会：

...card-start title="嵌套卡片"
嵌套卡片内部的内容
...card-end
...card-end


---


### 按钮与布局
按钮用于触发交互事件，当然也可以什么都不触发！

**基础按钮与事件**  
按钮必须包含 `text`，可不包含 `event`：  
...button text="纯展示按钮"
...button text="访问哔哩哔哩" event="url=https://www.bilibili.com/"
...button text="启动器功能" event="launcher=check_update"

- **url**: 在系统浏览器中打开指定链接  
- **launcher**: 触发启动器内部定义的事件标签  

**按钮外观样式**
我们提供四种 Material Design 3 按钮样式：
...button text="默认样式"
...button-outlined text="轮廓样式"
...button-filled-tonal text="色调填充样式"
...button-text text="文本样式"

**横向布局控制，Row组件**  
这是一个比较重要的布局组件，你也看到了，上面的示例中，按钮都是竖向排列的，这非常坏！  
这个时候，就可以使用 `Row` 来控制了！  
使用 `Row` 可以将内部的组件横向放置，目前 `Row` 内部仅支持放置按钮！ ~~废话，当前只有按钮这一种组件~~

...row-start
...button text="按钮1"
...button-outlined text="按钮2"
...row-end

**Row 详细配置项：**  
当然这些属性都是与 Jetpack Compose 原生的 Row 组件对齐的  
- **horizontalArrangement**: 控制水平分布
    - 常规：`Arrangement.Start`, `Center`, `End`, `SpaceBetween`, `SpaceAround`, `SpaceEvenly`
      - ```text
        horizontalArrangement=Arrangement.Start
        horizontalArrangement=Arrangement.Center
        horizontalArrangement=Arrangement.End
        horizontalArrangement=Arrangement.SpaceBetween
        horizontalArrangement=Arrangement.SpaceAround
        horizontalArrangement=Arrangement.SpaceEvenly
        ```
    - 间距：`Arrangement.spacedBy(12)`
      - ```text
        使每两个相邻的子组件间隔固定的12的距离
        以防剩余宽度为空，距离参数可以是负数，在这种情况下子组件将重叠在一起！
        可以指定alignment以将间隔开的子项在父项内水平对齐
        horizontalArrangement=Arrangement.spacedBy(12)
        
        可以指定横向的 Alignment 以将间隔开的子项在父项内水平对齐
        spacedBy中，只支持使用横向的 Alignment：Alignment.Start、Alignment.End、Alignment.End
        horizontalArrangement=Arrangement.spacedBy(12, Alignment.Start)
        ```
- **verticalAlignment**: 控制垂直对齐  
    - `Alignment.Top`, `Alignment.CenterVertically`, `Alignment.Bottom`


---


### 注意事项
在编写扩展组件时，请务必遵守以下规则以确保渲染正常：

**严格的禁止嵌套规定**：  
- **禁止卡片套娃**：为了界面简洁，卡片内部不能再定义另一个卡片
- **禁止Row套娃**：Row 内部暂时只能包含按钮系列组件  
- **允许组合**：你可以在卡片内部放置 `Row` 组件和按钮组件  

**语法细节**：  
- 扩展的语法无法直接嵌入 Markdown 语法内，比如卡片没法放进 Markdown 的列表、引用块等语法内
- 可包含子组件的组件的标签必须成对出现  

**你至少要会 Markdown 基础**：  
- 这毕竟是 Markdown 的扩展，基础的内容依然遵循标准 Markdown 语法  
- 如果不会，没关系，这个真的很好学！https://www.runoob.com/markdown/md-tutorial.html
