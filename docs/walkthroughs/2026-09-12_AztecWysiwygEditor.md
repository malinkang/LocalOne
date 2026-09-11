# 工作总结：1:1 还原 Day One 原生 Aztec 富文本编辑器

- **日期**：2026-09-12
- **任务目标**：1:1 还原 Day One 原生富文本所见即所得编辑器（WYSIWYG），彻底告别纯文本 Markdown 标签源码展示，实现真正的图文与富文本即时排版。
- **真机环境**：小米 13（Android 16 / HyperOS，无线调试已连接）

---

## 一、技术背景与原版架构对齐

在 Day One 原版应用中，日记编辑器并非单纯的 Markdown 纯文本输入框，而是基于 Automattic 开源的 **Aztec 原生富文本引擎 (`org.wordpress:aztec`)**。
通过反编译 Day One 官方 APK 证实其核心编辑器类 `com.dayoneapp.dayone.main.editor.aztec.AztecEditor` 直接继承自 Aztec 的 `AztecText`。

为了达成严格的 1:1 还原目标，本项目弃用了原先在输入框中显示 Markdown 语法字符的方案，全面引入并深度封装了 Aztec 富文本内核。

---

## 二、本次核心变更

### 1. 依赖与仓库配置
- 在 `settings.gradle.kts` 中添加 Automattic 官方 Maven 存储库：`https://a8c-libs.s3.amazonaws.com/android`。
- 在 `app/build.gradle.kts` 中引入 `org.wordpress:aztec:v1.6.2` 及配套兼容库 `androidx.appcompat:appcompat:1.7.0`。

### 2. 深度封装 `AztecRichEditor`
- 创建 `AztecRichEditor.kt`，使用 Compose 的 `AndroidView` 桥接 AztecText。
- 配置 Day One 经典的衬线排版（Serif 字体）、自适应文本颜色、行间距与无边框沉浸背景。
- 提供撤销（Undo）、重做（Redo）、加粗（Bold）、斜体（Italic）、下划线（Underline）、标题（Heading）以及待办清单（Task List）原生 API 调用接口。

### 3. 编辑器界面 1:1 像素级复刻
- **顶部操作栏 (`EditorHeaderBar.kt`)**：
  - 左侧 1:1 实现 Day One 特有的**青蓝色圆形打勾完成按钮（✔）**，支持一键保存并优雅返回时间线。
  - 右侧提供 **Undo（撤销）** 和 **Redo（重做）** 原生动作按钮。
- **底部格式工具栏 (`EditorFormatBar.kt`)**：
  - 快捷触发粗体、斜体、下划线、H1/H2 标题、待办复选清单等，所有格式实时所见即所得渲染。

### 4. 列表与卡片解析器 (`RichTextConverter.kt`)
- 解决富文本日记在时间线（Timeline）与日历（Calendar）中卡片展示时可能暴露 `<p>`、`<b>` 等 HTML 标签的问题。
- 采用 `HtmlCompat` 与自定义解析器，将 HTML 富文本转换为 Compose 的 `AnnotatedString`，完美提取干净的段落、加粗/斜体样式，并将待办清单转换为统一美观的 Unicode 待办标识（☐ / ☑）。

---

## 三、修改与新增文件清单

1. `settings.gradle.kts`：添加 Automattic S3 Maven 仓库。
2. `app/build.gradle.kts`：引入 Aztec 和 AppCompat 依赖。
3. `app/src/main/java/com/localone/journal/ui/editor/components/AztecRichEditor.kt` *(新增)*：封装 Aztec 原生富文本输入引擎。
4. `app/src/main/java/com/localone/journal/ui/common/RichTextConverter.kt` *(新增)*：HTML 转 Compose AnnotatedString 工具类。
5. `app/src/main/java/com/localone/journal/ui/editor/EditorScreen.kt`：切换为 AztecRichEditor，集成双向绑定。
6. `app/src/main/java/com/localone/journal/ui/editor/EditorViewModel.kt`：适配 HTML 富文本数据流持久化。
7. `app/src/main/java/com/localone/journal/ui/editor/EditorState.kt`：状态实体支持 HTML 内容。
8. `app/src/main/java/com/localone/journal/ui/editor/components/EditorHeaderBar.kt`：1:1 青蓝色勾选按钮与撤销重做。
9. `app/src/main/java/com/localone/journal/ui/editor/components/EditorFormatBar.kt`：格式化快捷按钮绑定。
10. `app/src/main/java/com/localone/journal/ui/timeline/TimelineScreen.kt`：卡片预览支持富文本解析展示。

---

## 四、真机验证结果

在小米 13（HyperOS / Android 16）真机测试：
1. **输入与编辑**：实时输入汉字、英文，加粗、斜体即时呈现，输入流畅无卡顿。
2. **保存与持久化**：点击左上角青蓝色打勾按钮，成功保存并写入本地 Room 数据库。
3. **列表呈现**：时间线卡片中正文段落自然排版，样式清晰优雅，完全无裸露 HTML/Markdown 源码符号。
