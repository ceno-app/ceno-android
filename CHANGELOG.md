# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## v2.10.0 - Unreleased

### Added

- Radio buttons for theme and DoH settings options

### Fixed

- 300+ warnings in source code were addressed, remaining warnings added to detekt-baseline.xml

### Changed

- Applied consistent code style formatting to source code

## [v2.9.3](https://gitlab.com/ceno-app/ceno-android/-/releases/v2.9.3) - 2026-06-11

### Fixed

- Bug that enabled debug logging by default at silly log level
- Link to website on About page was broken

### Changed

- Update Android-Components to v151.0.4
- Updates Sentry to 8.35.0 and enables tombstones
- Update translations for various locales

## [v2.9.2](https://gitlab.com/ceno-app/ceno-android/-/releases/v2.9.2) - 2026-05-20

### Changed

- Update Android-Components to v150.0.3
- Update to permissions launcher for downloadsPermissions, promptsPermissions, and sitePermissions
- Set ignoreUnknownKeys to true and explicitNulls to false for JSON decoder
- Update Ouinet to v1.6.7
- Update translations for various locales
- Remove READ_MEDIA_IMAGES and READ_MEDIA_VIDEO permissions

## [v2.9.1](https://gitlab.com/ceno-app/ceno-android/-/releases/v2.9.1) - 2026-04-17

### Changed

- Update Android-Components to v149.0.2

## [v2.9.0](https://gitlab.com/ceno-app/ceno-android/-/releases/v2.9.0) - 2026-04-02

### Added

- Prompt to be shown when external app requests to load a URL
- Secure screen feature to hide view when app is in background and prevent screenshots
- Warnings to be shown depending on which website sources are unchecked

### Fixed

- Search engine selection displayed incorrectly after change

### Changed

- Default list of shortcuts updated for RU and UA locales
- Update Android-Components to v148.0.2
- Update Ouinet to v1.6.4
- Increase on-device retention of metrics records to a maximum of 30 days
- Update version code generation of nightly builds to use Kotlin plugin

## [v2.8.3](https://gitlab.com/ceno-app/ceno-android/-/releases/v2.8.3) - 2026-02-27

### Fixed

- Log export not working when app set to RTL locale
- Crash when free media has less than 5 items
- Crash when navigating back in Setting after language change

### Changed

- Default name of log file is now ceno_log
- Update Android-Components to v147.0.4
- Update Android Gradle Plugin to 8.13.2

## [v2.8.2](https://gitlab.com/ceno-app/ceno-android/-/releases/v2.8.2) - 2026-01-29

### Added

- Metrics for app version and bridge opt in

### Fixed

- Handle JsonDecodingException in CenoSettings
- Fix IllegalStateException in CenoTooltip.addButtons
- Fix progress bar 'freeze' when exporting logs

### Changed

- Filter free media feed based on full locale detection
- Update Android-Components to v147.0.2
- Update Ouinet to v1.6.2
- Update translations for various locales

## [v2.8.1](https://gitlab.com/ceno-app/ceno-android/-/releases/v2.8.1) - 2025-12-31

This release:
- Updates random number generation to use SecureRandom
- Fixes bug with tabs tray not scrolling to selected tab
- Updates Ceno extension to v1.12.1
- Implements proxy authorization for local Ouinet client
- Fixes missing context crash in HomeFragment
- Fix null exception in search engine settings page

## [v2.8.0](https://gitlab.com/ceno-app/ceno-android/-/releases/v2.8.0) - 2025-12-24

This release:
- Refactors custom back button navigation to use OnBackPressedDispatcher
- Update BrowserActivity to launch as singleInstance
- Replace FLAG_ACTIVITY_NEW_TASK with FLAG_ACTIVITY_CLEAR_TASK
- Updates Android Gradle Plugin to 8.13.0
- Introduces option to pin websites to cache
- Reduces amount of default logging in release builds
- Removes standby page
- Updates Android-Components to v146.0.1
- Adds customization option to hide free media feed
- Updates Ouinet to v1.6.1
- Adds option to toggle DNS-over-HTTPS as default resolver

## [v2.7.1](https://gitlab.com/ceno-app/ceno-android/-/releases/v2.7.1) - 2025-11-15

This release:
- Updates Ouinet to v1.4.2
- Updates Android-Components to v145.0
- Update cacert.pem with bundle generated on Tue Nov  4 04:12:02 2025 GMT
- Minor updates to various translations

## [v2.7.0](https://gitlab.com/ceno-app/ceno-android/-/releases/v2.7.0) - 2025-10-31

This release:
- Updates Ouinet to v1.4.1
- Updates Android-Components to v144.0
- Updates minSdk to 26
- Fixes bug with metrics collection

## [v2.6.3](https://gitlab.com/ceno-app/ceno-android/-/releases/v2.6.3) - 2025-10-22

This release:
- Updates compile SDK to version 36
- Updates Sentry to 8.20.0
- Updates AndroidX Core to 1.17
- Applies workaround for edge-to-edge display support
- Updates Ouinet to 1.4.0
- Enables dynamic port selection for frontend and proxy endpoints
- Adds option to follow system theme
- Fixes bug with export log option
- Fixes bug with notifications and error page
- Updates Android-Components to v143.0.3
- Update cacert.pem with bundle generated on Tue Sep 9 03:12:01 2025 GMT
- Updates Ceno extension to 1.10.0

## [v2.6.2](https://gitlab.com/ceno-app/ceno-android/-/releases/v2.6.2) - 2025-09-04

This release:
- Updates Android-Components to 142.0.1
- Updates Sentry to 8.18.0
- Updates Ouinet to v1.3.1
- Removes remaining uses of OuinetNotification
- Fixes app not responding error due to network metrics
- Updates cacert.pem with bundle generated on Tue Aug 12 2025
- Updates Ceno logo
- Minor updates to various translations

## [v2.6.1](https://gitlab.com/ceno-app/ceno-android/-/releases/v2.6.1) - 2025-08-06

This release:
- Updates Android-Components to 141.0.1
- Fixes minor bugs
- Updates announcement handling to prevent crashes
- Major copy edits to RU translations

## [v2.6.0](https://gitlab.com/ceno-app/ceno-android/-/releases/v2.6.0) - 2025-07-24

This release:
- Updates Android-Components to 141.0
- Adds notification on Android 15+ to nudge user to reopen Ceno
- Updates default suggested site for RU
- Updates links to eQualitie website on About page
- Introduces homepage section to display crawled sites
- Re-enable swiping away homepage announcements
- Introduces privacy-preserving metrics collection
- Updates Ouinet to v1.3.0

## [v2.5.1](https://gitlab.com/ceno-app/ceno-android/-/releases/v2.5.1) - 2025-06-24

This release:
- Fixes foreground service timeout crash
- Makes homepage announcements persistent
- Updates Ouinet to v1.2.1

## [v2.5.0](https://gitlab.com/ceno-app/ceno-android/-/releases/v2.5.0) - 2025-06-17
This release:
- Updates Ouinet to v1.2.0
- Adds bookmarking feature
- Uses new version code scheme for release builds
- Updates Android-Components to v139.0.4
- Updater Sentry to 8.13.2

## [v2.4.7](https://gitlab.com/ceno-app/ceno-android/-/releases/v2.4.7) - 2025-05-22

This release:
- Updates Android-Components to v138.0.4
- Update cacert.pem with bundle generated on Tue May 20 03:12:02 2025 GMT
- Updates translations for various locales

## [v2.4.6](https://gitlab.com/ceno-app/ceno-android/-/releases/v2.4.6) - 2025-05-20

This release:
- Updates Android-Components to v138.0.3
- Updates Gradle to 8.11.1 and Kotlin to 2.0.0
- Updates Sentry to 8.11.1
- Updates Gson to 2.11.0

## [v2.4.5](https://gitlab.com/ceno-app/ceno-android/-/releases/v2.4.5) - 2025-05-14

This release:
- Updates Android-Components to v137.0.2
- Fixes issue with pre-installed extensions not localizing
- Updates Ouinet to v1.1.1
- Update cacert.pem to bundle generate on Feb 25, 2025
- Update translations for various locales

## [v2.4.4](https://gitlab.com/ceno-app/ceno-android/-/releases/v2.4.4) - 2025-04-18

This release:
- Updates Android-Components to v137.0
- Updates Ceno extension to 1.9.0
- Updates uBlock Origin extension to 1.63.2
- Refactors cached groups list
- Includes local cache count in ceno sources counts
- Updates Ceno support email
- Known Issue: Pre-installed extensions are not localized

## [v2.4.3](https://gitlab.com/ceno-app/ceno-android/-/releases/v2.4.3) - 2025-03-24

This release:
- Fixes status bar, toolbar, and content overlap on Android 15 devices
- Updates Android-Components to v136.0
- Minor bug fixes

## [v2.4.2](https://gitlab.com/ceno-app/ceno-android/-/releases/v2.4.2) - 2025-02-24

This release:
- Updates AndroidManifest.xml

## [v2.4.1](https://gitlab.com/ceno-app/ceno-android/-/releases/v2.4.1) - 2025-02-19

This release:
- Fixes Ceno support email not being populated
- Updates Android-Components to v135.0
- Updates Ouinet to v0.31.1
- Disables Ceno Browser Service notification
- Adds notification to be shown when browsing in public mode

## [v2.4.0](https://gitlab.com/ceno-app/ceno-android/-/releases/v2.4.0) - 2024-12-20

This release:
- Implements initial "Clean Insights" metrics campaign
- Adds a hidden developer tools sub-menu
- Adds en locale strings
- Supports monochrome launcher icons
- Updates Android-Components to v133.0.3
- Adds a Ceno network connection status indicator on homepage
- Update cacert.pem bundle
- Updates translations for various locales

## [v2.3.3](https://gitlab.com/ceno-app/ceno-android/-/releases/v2.3.3) - 2024-11-18

This release:
- Updates Ceno extension to v1.8.1
- Updates Android-Components to v132.0.2
- Updates Ouinet to v0.31.0

## [v2.3.2](https://gitlab.com/ceno-app/ceno-android/-/releases/v2.3.2) - 2024-11-07

This release:
- Updates Android-Components to v132.0
- Updates UI tests
- Fixes tooltips for small screen devices
- Refactors permissions and adds option to revoke them through Settings
- Updates Ouinet to v0.30.1
- Adds "Take me to Ceno anyway" option to standby timeout dialog
- Fixes scroll to hide toolbar bug

## [v2.3.1](https://gitlab.com/ceno-app/ceno-android/-/releases/v2.3.1) - 2024-10-14

This release:
- Fixes bugs related to changing language of application from settings
- Updates Android-Components to v131.0
- Updates various Android libraries
- Fixes bugs with permissions tooltip
- Adds ability to select app language on the welcome tooltip
- Adds option to save logs to storage and improves share logs UI

## [v2.3.0](https://gitlab.com/ceno-app/ceno-android/-/releases/v2.3.0) - 2024-09-29

This release:
- Adds settings option to change language of application
- Replaces onboarding screens with tooltip-guided onboarding
- Updates to Standby page UI
- Updates Android-Components to v130.0.1
- Updates Ouinet to v0.29.1
- Updates translations for various locales

## [v2.2.3](https://gitlab.com/ceno-app/ceno-android/-/releases/v2.2.3) - 2024-09-18

This patch release:
- Adds option to export logs from standby page
- Removes autofill service from setting options
- Fixes UI bug with toolbar covering part of web view
- Updates Android-Components to v130.0
- Updates Ouinet to v0.29.0
- Forces toolbar to top position for Android 7.1 and earlier
- Minor bug fixes for crashes
- Updates translations for various locales

## [v2.2.2](https://gitlab.com/ceno-app/ceno-android/-/releases/v2.2.2) - 2024-09-05

This patch release:
- Updates Android-Components to 129.0.2
- Updates Ouinet to v0.28.0
- Updates UI for about page with new links and updated logos
- Sets default toolbar position to top for Android 7.1 and earlier
- Refactors string resources
- Updates translations for certain locales

## [v2.2.1](https://gitlab.com/ceno-app/ceno-android/-/releases/v2.2.1) - 2024-08-14

This patch release:
- Updates Android-Components to 128.0.3, [!191](https://gitlab.com/censorship-no/ceno-browser/-/merge_requests/191)
- Fix IllegalStateException crash related to toolbar status icon, [!183](https://gitlab.com/censorship-no/ceno-browser/-/merge_requests/183)
- Minor improvements to error page UI, including swipe-to-refresh action, [!183](https://gitlab.com/censorship-no/ceno-browser/-/merge_requests/183)
- Updates translations for certain locales, especially Persian, [!189](https://gitlab.com/censorship-no/ceno-browser/-/merge_requests/189)

## [v2.2.0](https://gitlab.com/ceno-app/ceno-android/-/releases/v2.2.0) - 2024-07-29

This minor release:
- Updates Android-Components to 128.0 !177
- Updates Ouinet to 0.27.0
- Improves error pages and provides list of attempted mechanisms !159
- Adds support for reader view mode !170
- Allows sharing webpages as pdfs and sharing url to other apps on device !173
- Displays indicator icon for content sources and hides progress bar after website has finished loading !175
- Fixes crash with bridge mode and adds success dialog after enabling bridge mode !163
- Updates UI for list of cached websites !147
- Updates UI for stopping Ceno !150
- Implements TOML-based dependency management !172
- Removes unused dependencies !176
- Improves organization of string resources
- Updates default suggested sites for RU locale !180
- Fixes bug with announcements not being hidden in all locales !179
- Updates translations for certain locales !182
- Update cacert.pem bundle

## [v2.1.6](https://gitlab.com/ceno-app/ceno-android/-/releases/v2.1.6) - 2024-07-01

This patch release:
- Updates Android-Components to 127.0.1, https://gitlab.com/censorship-no/ceno-browser/-/merge_requests/174
- Updates Ouinet to v0.26.0
- Packages the release as an AAB for play store distribution

## [v2.1.5](https://gitlab.com/ceno-app/ceno-android/-/releases/v2.1.5) - 2024-05-31

This patch release:
- Updates Android-Components to 126.0, !169
- Updates Ouinet to v0.25.1, applies full fix for immediate crash on some Android 9 and 10 devices, #163
- Disables scroll-to-hide toolbar feature until fix is found for known bug, #144


## [v2.1.4](https://gitlab.com/ceno-app/ceno-android/-/releases/v2.1.4) - 2024-05-22

This patch release:
- Downgrades Ouinet to v0.24.0, temporary fix for [#163](https://gitlab.com/censorship-no/ceno-browser/-/issues/163)

## [v2.1.3](https://gitlab.com/ceno-app/ceno-android/-/releases/v2.1.3) - 2024-05-17

This patch release:
- Fixes crash when playing video content or downloading files in Android 14, [!162](https://gitlab.com/censorship-no/ceno-browser/-/merge_requests/162)
- Updates Android-Components to 125.3.0, [!165](https://gitlab.com/censorship-no/ceno-browser/-/merge_requests/165)
- Updates Ouinet to v0.25.0, [!165](https://gitlab.com/censorship-no/ceno-browser/-/merge_requests/165)

## [v2.1.2](https://gitlab.com/ceno-app/ceno-android/-/releases/v2.1.2) - 2024-05-03

This patch release:
- Updates Android-Components to 124.1.0, [!158](https://gitlab.com/censorship-no/ceno-browser/-/merge_requests/158)
- Updates Ouinet to v0.24.0
- Updates target SDK to Android 14 (API 34)
- Removes unused references to Mozilla's crash reporting, [#147](https://gitlab.com/censorship-no/ceno-browser/-/issues/147)
- Updates translations for various locales, [!160](https://gitlab.com/censorship-no/ceno-browser/-/merge_requests/160)

Known bugs:
- Scroll-to-hide toolbar not working, will be fixed in upcoming patch release, [#144](https://gitlab.com/censorship-no/ceno-browser/-/issues/144)

## [v2.1.1](https://gitlab.com/ceno-app/ceno-android/-/releases/v2.1.1) - 2024-04-16

This patch release:
- Removes managed storage permission, !151
- Allows homepage announcement to be dismissed/timeout, !149
- Adds "From Ceno cache" to list of possible content sources, !152
- Localizes link in bridge mode homepage card
- Updates Gradle to 8.2.1 and kotlin-compiler to 1.9.23

Known bugs:

- Scroll-to-hide toolbar not working, will be fixed in upcoming patch release

## [v2.1.0](https://gitlab.com/ceno-app/ceno-android/-/releases/v2.1.0) - 2024-04-03

This minor release:
- Updates Android-Components to 123.1.0, !134
- Improves content sources visualization, !126
- Adds full dark mode as default, !116
- Allow export of full application logs, !118
- Adds standby page during startup, !122
- Adds option to enable/disable bridge mode, !127
- Updates default suggested sites, !130
- Implements fix for Int overflow issue, !119
- Removes Fxa references, !120
- Fixes onboarding warning fragment crash, !121
- Fixes notification crash for Android 13 and 14, !123
- Updates translations for various locales, !133, !142, !145

Known bugs:
- Scroll-to-hide toolbar not working #144, will be fixed in next patch release

## [v2.0.9](https://gitlab.com/ceno-app/ceno-android/-/releases/v2.0.9) - 2024-02-15

This patch release:
- Updates Android-Components to 122.0
- Updates translations for certain locales, [!124](https://gitlab.com/censorship-no/ceno-browser/-/merge_requests/124)

## [v2.0.8](https://gitlab.com/ceno-app/ceno-android/-/releases/v2.0.8) - 2024-01-04

This patch release:
- Updates Android-Components to 121.0
- Updates translations for certain locales
- Update cacert.pem bundle

## [v2.0.7](https://gitlab.com/ceno-app/ceno-android/-/releases/v2.0.7) - 2023-12-06

This is the official release of Ceno Browser v2.0.7. It contains all of the bug fixes and enhancements previously listed in v2.0.7 beta releases including,
- Fixes deprecated references in Manifest file [!76](https://gitlab.com/censorship-no/ceno-browser/-/merge_requests/76)
- Update/radio dialog extra BTs [!75](https://gitlab.com/censorship-no/ceno-browser/-/merge_requests/75)
- Re-enables sentry crash reporting [!82](https://gitlab.com/censorship-no/ceno-browser/-/merge_requests/82)
- Fixes bug with Ceno Browser Service Status not updating dynamically in Settings menu [!84](https://gitlab.com/censorship-no/ceno-browser/-/merge_requests/84)
- Reorganizes Settings menu options, [!87](https://gitlab.com/censorship-no/ceno-browser/-/merge_requests/87)
- Updates Personal Mode browsing, fixing multiple bugs and implementing UI improvements [!86](https://gitlab.com/censorship-no/ceno-browser/-/merge_requests/86)
- Updates several android dependencies, [!89](https://gitlab.com/censorship-no/ceno-browser/-/merge_requests/89)
- Updates Ouinet to v0.22.0, [!89](https://gitlab.com/censorship-no/ceno-browser/-/merge_requests/89)
- Updates uBlock Origin to v1.53.0, [!90](https://gitlab.com/censorship-no/ceno-browser/-/merge_requests/90)
- Updates Android-Components to 120.0, [!98](https://gitlab.com/censorship-no/ceno-browser/-/merge_requests/98)
- Adds rss announcements to public and personal homepage, [!96](https://gitlab.com/censorship-no/ceno-browser/-/merge_requests/96)
- Updates UI for public and personal browsing mode, [!97](https://gitlab.com/censorship-no/ceno-browser/-/merge_requests/97)
- Updates translations for certain locales, [!88](https://gitlab.com/censorship-no/ceno-browser/-/merge_requests/88) [!93](https://gitlab.com/censorship-no/ceno-browser/-/merge_requests/93) [!99](https://gitlab.com/censorship-no/ceno-browser/-/merge_requests/99) [!102](https://gitlab.com/censorship-no/ceno-browser/-/merge_requests/102) [!107](https://gitlab.com/censorship-no/ceno-browser/-/merge_requests/107) [!109](https://gitlab.com/censorship-no/ceno-browser/-/merge_requests/109

## [v2.0.6](https://gitlab.com/ceno-app/ceno-android/-/releases/v2.0.6) - 2023-10-06

This official release:
- Updates Android-Components to v118.1.1
- Fixes "proxy server refused connection" bug caused by latest Google Play system update, [#112](https://gitlab.com/censorship-no/ceno-browser/-/issues/112)

## [v2.0.5](https://gitlab.com/ceno-app/ceno-android/-/releases/v2.0.5) - 2023-10-03

This official release:
- Fixes onboarding navigation crash, [!77](https://gitlab.com/censorship-no/ceno-browser/-/merge_requests/77)
- Updates translations
- Disables Sentry crash reporting

## [v2.0.4](https://gitlab.com/ceno-app/ceno-android/-/releases/v2.0.4) - 2023-09-22

This is the official release of Ceno Browser v2.0.4. It contains all of the bug fixes and enhancements previously listed in v2.0.4 beta releases including:
- Avoid string index error if cached bytes is greater than zero but less than 1KB, fixes #82
- Updates the onboarding UI to new design, !49
- Increases size of touch area around tab close button, !48
- Removes http, https, and/or www from the front of urls displayed in address bar, !48
- Updates fragment transactions to use AndroidX navigation library, !47
- Refactors Add-ons activity to a fragment in single activity architecture, !47
- Improvements on the Homescreen, [!58](https://gitlab.com/censorship-no/ceno-browser/-/merge_requests/58)
- Port remaining Ceno-settings to Settings sub-page. [!55](https://gitlab.com/censorship-no/ceno-browser/-/merge_requests/55)
- Fix bug with changing theme returning user to empty browser fragment [!57](https://gitlab.com/censorship-no/ceno-browser/-/merge_requests/57)
- Updates Android-Components to v117.1.0 [!54](https://gitlab.com/censorship-no/ceno-browser/-/merge_requests/54)
- Fix minor bugs in the onboarding flow [!52](https://gitlab.com/censorship-no/ceno-browser/-/merge_requests/52)
- Fix app-wide deprecated references [!53](https://gitlab.com/censorship-no/ceno-browser/-/merge_requests/53)
- Translations update from Hosted Weblate [!51](https://gitlab.com/censorship-no/ceno-browser/-/merge_requests/51)
- Update cacert.pem
- Sentry crash reporting implementation [!70](https://gitlab.com/censorship-no/ceno-browser/-/merge_requests/70)

## [v2.0.3](https://gitlab.com/ceno-app/ceno-android/-/releases/v2.0.3) - 2023-06-20

This is the official release of Ceno Browser v2.0.3. It contains all of the bug fixes and enhancements previously listed in v2.0.3 beta releases including,
- Updates Android-Components to v114.1.0
- Fixes crash observed during shutdown, [#66](https://gitlab.com/censorship-no/ceno-browser/-/issues/66)
- Removes duplicate Urdu translations, [!34](https://gitlab.com/censorship-no/ceno-browser/-/merge_requests/34)
- Updates automated UI tests so they work with Ceno, [#5](https://gitlab.com/censorship-no/ceno-browser/-/issues/5), [!35](https://gitlab.com/censorship-no/ceno-browser/-/merge_requests/35)
- Adds more alternate icons to customization options, [#58](https://gitlab.com/censorship-no/ceno-browser/-/issues/58), [!37](https://gitlab.com/censorship-no/ceno-browser/-/merge_requests/37)
- Removes placeholder tab for home page, [!39](https://gitlab.com/censorship-no/ceno-browser/-/merge_requests/39)
- Refactors back stack navigation to/from browser and home page fragments
- Updates app name to "Ceno"
- Updates cacert.pem to bundle generated on Tue May 30 03:12:04 2023 GMT
- Fixes issue with search text disappearing, [#46](https://gitlab.com/censorship-no/ceno-browser/-/issues/46)
- Fixes bug with homescreen shortcuts not opening correctly, [#76](https://gitlab.com/censorship-no/ceno-browser/-/issues/76)
- Open log file in browser when requesting to download it, related to [#60](https://gitlab.com/censorship-no/ceno-browser/-/issues/60)
- Remove Add-on action buttons from toolbar menu and settings options

## [v2.0.2](https://gitlab.com/ceno-app/ceno-android/-/releases/v2.0.2) - 2023-04-21

This is the official release of Ceno Browser v2.0.2. It contains all of the bug fixes and enhancements previously listed in v2.0.2 beta releases including,
- Updates Ouinet to v0.21.10
- Updates Android-Components to v111.1.1
- Updates IP addresses of BT bootstrap nodes for certain locales
- Asks permission to show notifications in Android 13 or later
- Adds Ceno Settings options to main application settings page
- Adds option for home button
- Adds fade-out animation when clearing or stopping the browser
- Adds status indication to notification
- Adds support for x86_64 and x86 architectures
- Updates translations for various locales


## [v2.0.1](https://gitlab.com/ceno-app/ceno-android/-/releases/v2.0.1) - 2023-02-27

This is the official release of Ceno Browser v2.0.1. It updates Ouinet to v0.21.6 and updates Mozilla libraries to v109. It also fixes two UI/UX bugs, modifies the appearance of the Ceno Sources pop-up, and makes minor translation updates.

### Enhancements

- Updates the Ouinet libary to v0.21.6, which exposes some new options for the android client and is built with Gradle 7.
- Updates Android-Components to v109.2.0
- Minor updates to translations

### Bug fixes

- Changes layout and behavior of navigation buttons in menu, [#44](https://gitlab.com/censorship-no/ceno-browser/-/issues/44)
- Fixes deletion of entries in address bar while typing, [#46](https://gitlab.com/censorship-no/ceno-browser/-/issues/46)

## [v2.0.0](https://gitlab.com/ceno-app/ceno-android/-/releases/v2.0.0) - 2023-01-26

This is the official v2.0.0 release of CENO Browser. It adds theme customization options, minor enhancements to settings pages and translations, and fixes bugs found in last beta release.

### Features

- New theme customization options, #26

### Enhancements

- UI changes to General Settings pages
- Minor updates to el and es translations, !13

### Bug Fixes

- Fixes crash when clearing Ceno cache before browser is ready, #31
- Fixes crash when opening Add-ons without network connection, #32
- Fixes typo in private browsing new tab description

## [v2.0.0b2](https://gitlab.com/ceno-app/ceno-android/-/releases/v2.0.0b2) - 2023-01-20

This is a release of the beta version of CENO Browser. It adds new UI features, updates to Mozilla libraries and the Ceno Extension and a few bug fixes for issues found in previous beta release.

### Features

- New search engine customization options, #28
- Ability to set color of Ceno launcher icon, #22
- UI changes to browser toolbar, #24

### Enhancements

- Translations for locales; el, de, and iw, !12
- Updates Mozilla libraries to v108
- Updates Ceno Extension to v1.6.1
- Updates CA certificate bundle to 2023-01-10

### Bug Fixes

- Fixes possible crash during onboarding, #27
- Fixes possible crash during connectivity change, #30
- Fixes issue with layout of About page, #23

## [v2.0.0b1](https://gitlab.com/ceno-app/ceno-android/-/releases/v2.0.0b1) - 2023-01-05

This is a release of the beta version of CENO Browser. It adds full support for the following locales; es, fa, fr, ru, and uk, customization of the Clear button, and the ability to clear browsing data from settings page. It also makes several minor UI adjustments.

### Enhancements

- Adds full support for the following locales; es, fa, fr, ru, and uk.
- Adds customization options for the Clear button, allowing it to be removed from the toolbar and menu and for the default behavior of the button to be set.
- Adds ability to clear browsing data from settings, with check list of which data is to be deleted.
- Minor UI adjustments; reorders menu options, adds app bar when in settings pages, moves customization options into a sub menu of settings page, and allow the information card displayed on homepage to be dismissed.
- Updates Ouinet library to v0.21.5

### Bug fixes

- Fixes issue with starting and stopping of CENO background service in Android 11 or earlier, see #20

## [v2.0.0a5](https://gitlab.com/ceno-app/ceno-android/-/tags/v2.0.0a5) - 2022-12-09

- See changes [v2.0.0a4..v2.0.0a5](https://gitlab.com/ceno-app/ceno-android/-/compare/v2.0.0a4..v2.0.0a5)

## [v2.0.0a4](https://gitlab.com/ceno-app/ceno-android/-/tags/v2.0.0a4) - 2022-11-10

- See changes [v2.0.0a3..v2.0.0a4](https://gitlab.com/ceno-app/ceno-android/-/compare/v2.0.0a3..v2.0.0a4)

## [v2.0.0a3](https://gitlab.com/ceno-app/ceno-android/-/tags/v2.0.0a3) - 2022-10-28

- See changes [v2.0.0a2..v2.0.0a3](https://gitlab.com/ceno-app/ceno-android/-/compare/v2.0.0a2..v2.0.0a3)

## [v2.0.0a2](https://gitlab.com/ceno-app/ceno-android/-/tags/v2.0.0a2) - 2022-10-19

- See changes [v2.0.0a2..v2.0.0a2](https://gitlab.com/ceno-app/ceno-android/-/compare/v2.0.0a1..v2.0.0a2)

## [v2.0.0a1](https://gitlab.com/ceno-app/ceno-android/-/tags/v2.0.0a1) - 2022-10-17

- Initial alpha release
