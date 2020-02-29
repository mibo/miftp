# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]
Planned changes/updates/fixes/features for to be part of one of the next _releases/versions_:
  * Add feature to _FTP_ server to allow to keep _flagged_ files longer (to enable  `SlackImageDiffNotifier` to flag images) 
  * Add feature to _Service_ to specify areas which are ignored for image diff

## [1.0.2] - 2020-02-29
### Fixed
  * Fixed wrong handling of `diff_ignore_threshold` in `SlackImageDiffNotifier`

## [1.0.1] - 2020-02-27
### Added
  * Added parameter `diff_sensitivity` which is used to set sensitivity for the image comparision 

### Changed
  * Updated Spring Boot to `2.2.5.RELEASE`

## [1.0.0] - 2020-02-18
### Added
  * From this version (`1.0.0`)  the project now follows the Semantic Versioning
    (and as result for every not backward compatible change the major version will be increased)

### Changed
  * Changed configuration for SlackImageDiffNotifier.
    As it is a not backward compatible change and the features are fine (for now) the decision was to release it as `1.0.0`

## [0.9.1] - 2020-01-22
### Changed
  * Updated Spring Boot to `2.2.4.RELEASE`

## [0.9.0] - 2020-01-10
### Added
  * Added option to select image diff area (used in `SlackImageDiffNotifier`)

### Changed
  * Updated Kotlin to `1.3.60`
  * Updated Spring Boot to `2.2.2.RELEASE`

## [0.8.3] - 2019-10-28
### Changed
  * Updated Kotlin to `1.3.50`
  * Updated Spring Boot to `2.2.0.RELEASE`

## [0.8.2] - 2019-10-03
### Fixed
  * Refactored FTP View/Context to fix issues when one user is connected more then once 
    (see [PR](https://github.com/mibo/miftp/pull/14)). Before this leads to strange behaviour related to current work dir.
  
## [0.8.1] - 2019-10-01
### Changed
  * Changed image link and file count for health (see [PR](https://github.com/mibo/miftp/pull/14))

## [0.8.0] - 2019-09-27
### Added
  * Add feature to _Service_ to make files available (public web accessible) via a token (instead user user/pwd) 
    ([Pull request](https://github.com/mibo/miftp/pull/12)).

## [0.7.4] - 2019-09-08
### Fixed
  * 286c764 Fixed http client close handling

## [0.7.3] - 2019-08-30
### Fixed
  * a3827de Changed scheduler from fixed rate to fixed dela

## [0.7.2] - 2019-08-27
### Added
  * 8b9099c Added removal option for empty directories (default=false)

### Fixed
  * 00ee6ed Moved/changed clean up scheduler to work only on main FsView
  * Fix deletion issue (workaround) for some strange FTP client

## [0.7.1] - 2019-08-20
### Added
  * Added direct base url access (not only '/')
  * Added build info to health state and html view
  * Added option to 'lock' files for deletion

### Fixed
  * 769a9f9 (origin/fix-path-removal-issue) Fix for strange behavior of some FTP clients
  * 68aa190 Improved path/file/dir deletion

## [0.7.0] - 2019-08-18
### Added
  * New endpoint `/go/latestFile?content` to get latest modified file
  * New endpoint `/health` to get (basic) health information
  * Add new endpoint to overview page (HTML)
  * Added notification (callback) option via `FileSystemListener`
  * Added notifier extension to main app and first `SlackNotifier` implementation

### Changed
  * Formats (Date, Size) in overview page (HTML)
  * Updated to Spring Boot version `2.1.6`

## [0.6.0] - 2019-01-29
### Added
  * Support for creation of absolute path with not existing parent directories in FTP Server
    * In such cases all not existing path elements are automatically created
  * Support for `logback` `LOG` level configuration via ENV params

### Changed
  * Sort list of files by last modified date (desc) which is reflected in Index HTML Page
  * Changed DateTime Format for last modified date formatted which is reflected in Index HTML Page

### Fixed
  * Missing implementation for `dispose()` in `InMemoryFsView`

## [0.5.0] - 2019-01-24
### Added
  * Support for Directories in FTP Server
  * Support for Directories in REST Server (Index HTML Page)

### Changed
  * Layout and content of Index HTML Page

## [0.4.0] - 2019-01-19
### Added
  * Support for _URL (path) prefix_

### Changed
  * Changed from JavaScript view (index.html) to server side generated HTML (with `Thymeleaf`)

## [0.3.0] - 2019-01-16
### Added
  * Activated/configured _SSL_ for server REST endpoint
  * Added options for _SSL_ address settings (req. for Docker)
  * Updated Docker scripts (accordingly to the changes related to released artifact)

## [0.2.0] - 2019-01-07
### Added
  * Added option for _pasv_ address settings (req. for Docker)
  * Added Docker scripts into project (not related to released artifact)

## [0.1.1] - 2019-01-04
### Added
  * Support for _PASV_ port configuration (which is necessary for running in Docker container)

### Fixed
  * Docker _run and build_ script

## [0.1.0] - 2019-01-04
### Added
  * First release which contains _very basic_ FTP server functions (write/read in root; w/o sub-folders)
  * Provides basic REST based access to FTP content
  * Provides _very basic_ HTML page to access FTP content

<!--### Added => for new features.-->
<!--### Changed => for changes in existing functionality.-->
<!--### Deprecated => for soon-to-be removed features.-->
<!--### Removed => for now removed features.-->
<!--### Fixed => for any bug fixes.-->
<!--### Security => in case of vulnerabilities.-->