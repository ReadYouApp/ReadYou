<div align="center">
    <img width="200" height="200" style="display: block; border: 1px solid #f5f5f5; border-radius: 9999px;" src="https://raw.githubusercontent.com/ReadYouApp/ReadYou/main/fastlane/metadata/android/zh-TW/images/icon.png">
</div>

<br>
<br>
<br>

<div align="center">
    <img alt="GitHub" src="https://img.shields.io/github/license/ReadYouApp/ReadYou?color=c3e7ff&style=flat-square">
    <a target="_blank" href="https://github.com/ReadYouApp/ReadYou/releases">
        <img alt="Version" src="https://img.shields.io/github/v/release/ReadYouApp/ReadYou?color=c3e7ff&label=version&style=flat-square">
    </a>
    <img alt="GitHub last commit" src="https://img.shields.io/github/last-commit/ReadYouApp/ReadYou?color=c3e7ff&style=flat-square">
    <br>
    <a target="_blank" href="https://t.me/ReadYouApp">
        <img alt="Telegram" src="https://img.shields.io/badge/Telegram-ReadYouApp-c3e7ff?logo=telegram&style=flat-square">
    </a>
    <a target="_blank" href="https://www.figma.com/file/ViBW8GbUgkTMmK6a80h8X1/Read-You?node-id=7028%3A23673">
        <img alt="Figma" src="https://img.shields.io/badge/Figma-ReadYou-c3e7ff?logo=figma&style=flat-square">
    </a>
</div>

<div align="center">
    <h1>Read You</h1>
    <p>這是一個在 Android 上的  <a href="https://reederapp.com/">Reeder</a> 仿製品，為了讓 Android 擁有一個與 Reeder 相似的 RSS 閱讀器。</p>
    <p><a target="_blank" href="https://github.com/ReadYouApp/ReadYou/blob/main/README.md">English by DeepL</a>&nbsp;&nbsp;|&nbsp;&nbsp;
    <a target="_blank" href="https://github.com/ReadYouApp/ReadYou/blob/main/README-de.md">Deutsch</a>&nbsp;&nbsp;|&nbsp;&nbsp;
    <a target="_blank" href="https://github.com/ReadYouApp/ReadYou/blob/main/README-zh-CN.md">简体中文</a>&nbsp;&nbsp;|&nbsp;&nbsp;
    <a target="_blank" href="https://github.com/ReadYouApp/ReadYou/blob/main/README-fa.md">فارسی</a></p>
    繁體中文</p>
    <br/>
    <br/>
    <img src="https://raw.githubusercontent.com/ReadYouApp/ReadYou/main/fastlane/metadata/android/zh-TW/images/phoneScreenshots/startup.png" width="19.2%"alt="startup" />
    <img src="https://raw.githubusercontent.com/ReadYouApp/ReadYou/main/fastlane/metadata/android/zh-TW/images/phoneScreenshots/feeds.png" width="19.2%" alt="feeds" />
    <img src="https://raw.githubusercontent.com/ReadYouApp/ReadYou/main/fastlane/metadata/android/zh-TW/images/phoneScreenshots/flow.png" width="19.2%" alt="flow" />
    <img src="https://raw.githubusercontent.com/ReadYouApp/ReadYou/main/fastlane/metadata/android/zh-TW/images/phoneScreenshots/read.png" width="19.2%" alt="read" />
    <img src="https://raw.githubusercontent.com/ReadYouApp/ReadYou/main/fastlane/metadata/android/zh-TW/images/phoneScreenshots/settings.png" width="19.2%" alt="settings" />
    <br/>
    <br/>
</div>

## 特性

**Read You** 結合了 Reeder 的交互邏輯與 [Material Design 3 (You)](https://m3.material.io/) 的設計風格。

以下是目前取得的進展和近期將要努力的目標：

-   [x] 本地

    -   [x] 訂閱 RSS 連結
    -   [x] 匯入 OPML 文件
    -   [x] 文章同步
    -   [x] 文章更新通知
    -   [x] 全文分析
    -   [x] 過濾未讀、星標
    -   [x] 訂閱源分組
    -   [x] 本地化
    -   [x] 匯出 OPML 文件
    -   [x] 文章搜尋
    -   [ ] 偏好設定
    -   [ ] 發布 APK
    -   [ ] 小工具
    -   [ ] ...

-   [ ] Fever API 支持
-   [ ] Google Reader API 支持
-   [ ] Inoreader API 支持
-   [ ] ...

> 以上特性僅是初步實現，可能存在某些未知 BUG，仍需進行測試和最佳化。

## 下载

[<img src="https://fdroid.gitlab.io/artwork/badge/get-it-on.png"
     alt="Get it on F-Droid"
     height="80">](https://f-droid.org/packages/me.ash.reader/)

或者从 [GitHub release](https://github.com/ReadYouApp/ReadYou/releases) 获取 APK 文件。

## 翻译

<a target="_blank" href="https://hosted.weblate.org/engage/readyou/">
<img src="https://hosted.weblate.org/widgets/readyou/-/287x66-white.png" alt="" />
</a>

## 構建

> 如果你想要預覽 Read You 應用，可以在 [Telegram 頻道](https://t.me/ReadYouApp) 中獲取 **預覽版本** 的 APK 文件。

**Read You** 基於 Android 原生的 [Jetpack Compose](https://developer.android.com/jetpack/compose) 架構實現。

1. 首先需要獲取 **Read You** 的原始碼：

    ```shell
    git clone https://github.com/ReadYouApp/ReadYou.git
    ```

2. 然後通過 [Android Studio (最新版本)](https://developer.android.com/studio) 打開。

3. 點擊 `▶ 運行（Run）` 按鈕後將會自動構建並運行。

    > 如遇卡頓現象，請選擇 Release 版本構建。

## 感謝開源

-   [MusicYou](https://github.com/Kyant0/MusicYou)
-   [ParseRSS](https://github.com/muhrifqii/ParseRSS): [MIT](https://github.com/muhrifqii/ParseRSS/blob/master/LICENSE)
-   [Readability4J](https://github.com/dankito/Readability4J): [Apache License 2.0](https://github.com/dankito/Readability4J/blob/master/LICENSE)
-   [opml-parser](https://github.com/mdewilde/opml-parser): [Apache License 2.0](https://github.com/mdewilde/opml-parser/blob/master/LICENSE)
-   [compose-html](https://github.com/ireward/compose-html): [Apache License 2.0](https://github.com/ireward/compose-html/blob/main/LICENSE.txt)
-   [Rome](https://github.com/rometools/rome): [Apache License 2.0](https://github.com/rometools/rome/blob/master/LICENSE)
-   [Feeder](https://gitlab.com/spacecowboy/Feeder): [GPL v3.0](https://gitlab.com/spacecowboy/Feeder/-/blob/master/LICENSE)

## 許可證

[GNU GPL v3.0](https://github.com/ReadYouApp/ReadYou/blob/main/LICENSE)
