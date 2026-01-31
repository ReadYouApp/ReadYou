<div align="center">
    <img width="200" height="200" style="display: block; border: 1px solid #f5f5f5; border-radius: 9999px;" src="https://raw.githubusercontent.com/ReadYouApp/ReadYou/main/fastlane/metadata/android/en-US/images/icon.png">
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
    <h1>Read You AI</h1>
    <p>An Android RSS reader presented in <a target="_blank" href="https://m3.material.io/">Material You</a> style.</p>
    <p>English&nbsp;&nbsp;|&nbsp;&nbsp;
    <a target="_blank" href="https://github.com/ReadYouApp/ReadYou/blob/main/README-de.md">Deutsch</a>&nbsp;&nbsp;|&nbsp;&nbsp;
    <a target="_blank" href="https://github.com/ReadYouApp/ReadYou/blob/main/README-zh-CN.md">简体中文</a>&nbsp;&nbsp;|&nbsp;&nbsp;
    <a target="_blank" href="https://github.com/ReadYouApp/ReadYou/blob/main/README-zh-TW.md">繁體中文 (Outdated)</a>&nbsp;&nbsp;|&nbsp;&nbsp;
    <a target="_blank" href="https://github.com/ReadYouApp/ReadYou/blob/main/README-fa.md">فارسی (Outdated)</a></p>
    <br/>
    <br/>
    <img src="https://raw.githubusercontent.com/ReadYouApp/ReadYou/main/fastlane/metadata/android/en-US/images/phoneScreenshots/startup.png" width="19.2%" alt="startup" />
    <img src="https://raw.githubusercontent.com/ReadYouApp/ReadYou/main/fastlane/metadata/android/en-US/images/phoneScreenshots/feeds.png" width="19.2%" alt="feeds" />
    <img src="https://raw.githubusercontent.com/ReadYouApp/ReadYou/main/fastlane/metadata/android/en-US/images/phoneScreenshots/flow.png" width="19.2%" alt="flow" />
    <img src="https://raw.githubusercontent.com/ReadYouApp/ReadYou/main/fastlane/metadata/android/en-US/images/phoneScreenshots/read.png" width="19.2%" alt="read" />
    <img src="https://raw.githubusercontent.com/ReadYouApp/ReadYou/main/fastlane/metadata/android/en-US/images/phoneScreenshots/settings.png" width="19.2%" alt="settings" />
    <br/>
    <br/>
</div>

## Features

**Read You** is an Android RSS reader presented in [Material You](https://m3.material.io/) style.

The following are the progress made so far and the goals to be worked on in the near future:

- [x] Subscribe to RSS links
- [x] Import or export OPML files
- [x] Notification of new articles
- [x] Article readability optimization
- [x] Full content parse for original articles
- [x] Multi-account
- [x] Read aloud
- [x] AI Article Summarization
- [ ] Android widget
- [ ] ...

## Integration

**Read You** integrates with some of third-party service APIs to support you in using your existing cloud accounts as data sources.

- [x] Fever
- [x] Google Reader
- [x] FreshRSS
- [x] Miniflux
- [ ] Tiny Tiny RSS
- [ ] Inoreader
- [ ] Feedly
- [ ] Feedbin
- [ ] ...

## AI Summarization

**Read You** supports AI-powered article summarization using OpenAI-compatible APIs. You can configure:
- API Base URL (default: OpenAI)
- API Key
- LLM Model (fetched dynamically)
- Custom Summarization Prompt

Access these settings in **Settings > AI**.

## Download

[<img src="https://s1.ax1x.com/2023/01/12/pSu1a36.png" alt="Get it on GitHub" height="80">](https://github.com/ReadYouApp/ReadYou/releases)
[<img src="https://s1.ax1x.com/2023/01/12/pSnTZ0f.png"
     alt="Get it on Telegram Channel"
     height="80">](https://t.me/ReadYouApp)
[<img src="https://fdroid.gitlab.io/artwork/badge/get-it-on.png"
     alt="Get it on F-Droid"
     height="80">](https://f-droid.org/packages/me.ash.reader/)

F-Droid releases can not upgrade to other releases and it is compiled, signed and distributed uniformly by [F-Droid](https://f-droid.org/docs/FAQ_-_General/).

## Nightly

We offer Nightly versions for testing purposes. These versions include the latest features and improvements but may not be as stable as our regular releases. Please note that Nightly builds might have bugs and are not recommended for everyday use.

You can download the Nightly version from the following link:

<a target="_blank" href="https://github.com/ReadYouApp/ReadYou/actions/workflows/build_commit.yaml">Check Nightly Builds</a>

Choose the latest workflow from the GitHub Actions page and download the artifacts from the workflow's summary page (You may require logging in to GitHub).

**Remember to back up your data before trying the Nightly version in case of any issues.**

## Sponsorship

**Read You** is a free open source software that benefits from the open source community and every user can enjoy it's full functionality for free, so if you appreciate my current work, you can buy me a cup of coffee.☕️

[<img src="https://s1.ax1x.com/2023/01/12/pSnHqpQ.png" alt="donate" height="80">](https://ash7.io/sponsor)

Thanks for all the love and support.❤️

## Localization

Thank you to each of the **Read You** translators, and if you would like to contribute, please submit a translation via [Weblate](https://hosted.weblate.org/engage/readyou/).

[<img src="https://hosted.weblate.org/widgets/readyou/-/horizontal-auto.svg" alt="" />](https://hosted.weblate.org/engage/readyou/)

## Build

> Welcome to open a [pull request](https://github.com/ReadYouApp/ReadYou/pulls). [GitHub Actions](https://github.com/ReadYouApp/ReadYou/actions) automatically packages all flavors of apk files for each commit.

**Read You** is based on [Jetpack Compose](https://developer.android.com/jetpack/compose) toolkit for building Android's native UI.

1. First you need to get the source code of **Read You**.

   ```shell
   git clone https://github.com/ReadYouApp/ReadYou.git
   ```

2. Then open it via [Android Studio (latest version)](https://developer.android.com/studio).

3. When you click the `▶ Run` button, it will be built and run automatically.

    > In case of lag, please select Release version build.

## Credits

### Open Source Projects

- [MusicYou](https://github.com/Kyant0/MusicYou)
- [ParseRSS](https://github.com/muhrifqii/ParseRSS)
- [Readability4J](https://github.com/dankito/Readability4J)
- [opml-parser](https://github.com/mdewilde/opml-parser)
- [compose-html](https://github.com/ireward/compose-html)
- [Rome](https://github.com/rometools/rome)
- [Feeder](https://gitlab.com/spacecowboy/Feeder)
- [Seal](https://github.com/JunkFood02/Seal)
- [news-flash](https://gitlab.com/news-flash)
- [besticon](https://github.com/mat/besticon)
- [Jiffy Reader](https://github.com/ansh/jiffyreader.com)
- ...

### Special Thanks

[<img src="https://avatars.githubusercontent.com/u/76829190?v=4" width="180" height="180" style="display: block; border: 1px solid #f5f5f5; border-radius: 9999px;"/>](https://github.com/Kyant0)

Thanks to **@Kyant0** for the design inspiration and Monet engine implementation for **Read You**.

[<img src="https://resources.jetbrains.com/storage/products/company/brand/logos/jb_beam.png" width="200" alt="Copyright © 2000-2023 JetBrains s.r.o. JetBrains and the JetBrains logo are registered trademarks of JetBrains s.r.o."/>](https://www.jetbrains.com/)

Thanks to **JetBrains** for allocating free open-source licences for IDEs for **Read You**.

[<img src="https://hosted.weblate.org/widgets/readyou/-/287x66-white.png"  width="200"/>](https://hosted.weblate.org/engage/readyou/)

Thanks to **Weblate** for providing free hosting of open source projects for **Read You**.

## License

GNU GPL v3.0 © [Read You](https://github.com/ReadYouApp/ReadYou/blob/main/LICENSE)
