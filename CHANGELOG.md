# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]
Planned changes/updates/fixes/features for to be part of one of the next _releases/versions_:
  * Add feature to _FTP_ server to allow to keep _flagged_ images longer (to enable  `SlackImageDiffNotifier` to flag images) 

## [0.8.0-SNAPSHOT] - 2019-xx-xx
### Added
  * ...

### Changed
  * ...

### Fixed

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