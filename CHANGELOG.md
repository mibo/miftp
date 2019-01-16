# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]
Planned changes/updates/fixes/features for to be part of one of the next _releases/versions_

## [0.3.0-SNAPSHOT] - 2019-01-xx
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