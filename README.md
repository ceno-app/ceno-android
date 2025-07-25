<img src="https://ceno.app/static/img/logos/ceno-logo.png" width=250px alt="Ceno Browser">


[![pipeline status](https://gitlab.com/ceno-app/ceno-android/badges/main/pipeline.svg)](https://gitlab.com/ceno-app/ceno-android/commits/main)
[![Gitlab release (latest by date)](https://img.shields.io/gitlab/v/release/ceno-app/ceno-android)](https://gitlab.com/ceno-app/ceno-android/-/releases)
[![License: MPL 2.0](https://img.shields.io/badge/License-MPL%202.0-brightgreen.svg)](./LICENSE)
[![Weblate](https://hosted.weblate.org/widget/censorship-no/svg-badge.svg)](https://hosted.weblate.org/projects/censorship-no/)

Ceno is a next-generation mobile web browser that uses peer-to-peer technology to deliver websites to your phone and caches popular content with cooperating peers. Ceno can be used to bypass Internet censorship and help others retrieve blocked pages.

Built from [Mozilla Android Components](https://github.com/mozilla-mobile/firefox-android/blob/main/android-components/README.md), it includes the Mozilla Web Platform via GeckoView and a [Ouinet](https://ouinet.work) Client for sharing web content peer-to-peer.

## ▶️ Access

[<img src="https://ceno.app/static/img/index/google-play.png"
      alt="Get it on Play Store"
      height="80">](https://play.google.com/store/apps/details?id=ie.equalit.ceno)
[<img src="https://ceno.app/static/img/index/paskoocheh.png"
      alt="Get it on Paskoocheh" 
      height="80">](https://paskoocheh.com/tools/124/android.html?utm_source=UpdatePage)

## 🚀 Features

🌴 **Browse freely, anytime.**  
Ceno is designed with internet shutdown scenarios in mind. Websites are shared by a global network of peers, and stored in a distributed cache for availability when traditional networks are blocked or go down.

🔓 **Unlock the web.**  
Access any website. Frequently requested content is cached on the network and cannot be forcibly removed.

💲 **Reduce Data Costs.**  
By routing user traffic through peer-to-peer networks, Ceno Browser incurs less data costs while still providing users with circumvention capability.

🌐 **Grow the Network, Fight Censorship.**  
Fight censorship by becoming a bridge! Install and run Ceno Browser to instantly join the network and expand the availability of blocked websites to those in censored countries.

👐 **Free and open source.**  
Ceno Browser is powered by [Ouinet](https://ouinet.work), an open source library enabling third party developers to incorporate the Ceno network into their apps for peer-to-peer connectivity.

## ❗ Important Notice:
Ceno has two modes of operation - **Public** and **Personal**. You can easily toggle between them. Public mode offers the best connectivity but the least privacy - websites that you visit or share are recorded in a publicly accessible registry (BitTorrent). Private mode eliminates this record but may be slower and less efficient at retrieving content. See the [FAQ](https://ceno.app/en/support.html) or [User Manual](https://ceno.app/user-manual/en/) for more details on Ceno usage.

By default, Ceno collects anonymized application and network metrics to help us understand issues and improve application performance and user experience. The metrics records are encrypted-at-rest on your device and in-transit to eQualitie's self-hosted metrics endpoint. Metrics records are submitted at least once per-hour and are deleted from your device after submission. The collected data is aggregated on a weekly-basis and then deleted from our servers. No personal information is collected and the data is never shared with third-parties. Users can opt-out of metrics collection at any time via the `Settings > Background metrics > Ceno Metrics` toggle.

Read more about the technical details of what we collect in the Ouinet repo's [record_format.md](https://gitlab.com/equalitie/ouinet/-/blob/main/rust/record_format.md).

## ❣️ Contributing
From testing to translations to bug reporting and merge requests, there are lots of ways to contribute to this project! Please see [the contributing guidelines](CONTRIBUTING.md) for more information.

## 🔧 Building
### Developer Build
To build debug versions of Ceno Browser, enter the checkout directory and execute the following commands,
```
cp local.propeties.sample local.properties
ANDROID_HOME=/path/to/Android/Sdk ./gradlew assembleDebug
```
The resulting apks will be copied to the `output/debug/` directory.

The Ouinet client configuration is currently hardcoded at build time and cannot be changed at run time. You may customize the `local.properties` file with your own values and rebuild as needed.

By default, the latest versions of the [Ouinet library](https://gitlab.com/equalitie/ouinet/-/releases) and our forks of [Mozilla's GeckoView and Android-Components](https://github.com/mozilla-mobile/firefox-android/releases) (built with our [Mozilla Build Scripts](https://gitlab.com/ceno-app/mozilla-build-scripts)) are automatically downloaded from the [Maven Central repository](https://repo.maven.apache.org/maven2/ie/equalit/ouinet/) and used for building both the debug and release variants of Ceno Browser.

# Accessibility

If your code has user-facing changes, follow [Android accessibility best practices](https://github.com/mozilla-mobile/shared-docs/blob/main/android/accessibility_guide.md).

# Testing  

This project is tested with [BrowserStack](https://www.browserstack.com/).

# License

This Source Code Form is subject to the terms of [the Mozilla Public License, v. 2.0](LICENSE). 
