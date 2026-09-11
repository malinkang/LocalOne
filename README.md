# LocalOne (AuraJournal)

一款基于 **Jetpack Compose + 现代化 Android 原生架构** 打造的、**完全本地（Local-Only & Zero-Knowledge）** 的 Day One 风格离线日记应用。

## 🌟 核心设计理念

1. **绝对隐私（Local-First / Zero-Knowledge）**
   - 彻底摆脱第三方云端同步服务器，数据与多媒体文件只保存在设备本地私有沙盒中。
   - 本地数据库采用 **Room + SQLCipher (AES-256)** 全盘透明加密。
2. **现代 Android 原生体验**
   - 100% 采用 **Jetpack Compose + Material 3**，深度还原 Day One 标志性的天空蓝 (`#44C0FF`) 与高质感时间线排版。
   - 单向数据流架构（MVI + Kotlin 协程与 Flow）。
3. **1:1 对齐 Day One 经典功能**
   - **时间线与那年今天（On This Day）**：多维回忆检索，自动跨年聚合。
   - **上下文元数据自动感知**：日记自动附带拍摄地点、温度、气象状态与月相（Moon Phase）。
   - **打卡与日历热力图**：集成 `kizitonwose/Calendar`。
   - **画册级导出与排版**：参考 Day One 官方排版 CSS 规则，支持日记渲染与防断裂 PDF 打印。

## 🛠 技术选型

- **UI**: Jetpack Compose, Material 3, Navigation Compose
- **Markdown 渲染**: `com.mikepenz:multiplatform-markdown-renderer-m3`
- **日历组件**: `com.kizitonwose.calendar:compose`
- **图像加载**: Coil 3 (`io.coil-kt:coil-compose`)
- **数据持久化**: Room Database (`androidx.room`) + SQLCipher (`net.zetetic:android-database-sqlcipher`)
- **并发与响应式**: Kotlin Coroutines, StateFlow, Channel

## 📂 项目结构

```
app/src/main/java/com/localone/journal/
├── core/               # 基础设计系统、加密工具
├── data/               # Room 实体、Dao、仓库实现
├── domain/             # 核心领域模型（JournalEntry, Metadata）与用例
└── ui/                 # Compose 界面
    ├── theme/          # Day One 色彩令牌、排版与主题
    ├── timeline/       # 时间线列表、日记卡片与那年今天
    ├── editor/         # 混合富文本编辑器
    └── components/     # 通用小组件与徽标
```
