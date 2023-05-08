# Changelog
## [unpublished] - 2023-x-x

### Added
- Icon for connection status to Messagebus shown on bottom right

### Changed
- Activity Panel moved to bottom right

### Fixed
- Installing and removing of opsi-packages
- Problems with different actions in Licensemanagement

## [4.2.21.2] - 2023-04-18

### Changed
- Strip whitespace when adding new configs
- Markdown changelog

### Fixed
- Fix startup if opsiclientd.event_on_shutdown.active is not boolean / null
- Remove product property tls_alternative_cipher

## [4.2.21.1] - 2023-04-14

### Fixed
- Fix client selection dialog for non MySQL

## [4.2.21.0] - 2023-04-12

### Added
- Improve messagebus connection handling, auto reconnect
- Improve 2FA handling, add password dialog

### Changed
- Do not show essential / devel log messages in GUI

### Fixed
- Do not process expired messagebus messages
- Fix SLF4J warnings
- Fix UpdateCommand casting error
- Correct order of action requests in combobox
- Fix boolean table cell renderer (WAN column in client table)

## [4.2.20.14] - 2023-03-30

### Added
- Add start param "--disable-certificate-verification" to disable certificate verification
- Add property "disable_certificate_verification" to disable certificate verification to package


## [4.2.20.13] - 2023-03-29

### Fixed
- Requesting session-info works now


## [4.2.20.12] - 2023-03-14

### Fixed
- small bugfix (removed unnecessary logging-call)


## [4.2.20.11] - 2023-03-10

### Fixed
- bugfix in licensinginfomap: works now also with users that are not opsiadmin


## [4.2.20.10] - 2023-03-07

### Changed
- changing loglevel colors, adapting to opsi-standards and in logpanel adapting to white background

### Fixed
- bugfix in "show only selected products"


## [4.2.20.9] - 2023-03-03

### Fixed
- bugfix with option sshConnectOnStart


## [4.2.20.8] - 2023-03-01

### Changed
- sort remote control commands alphabetically
- center filechooser on main frame

### Fixed
- Will now keep selected clients on reload


## [4.2.20.7] - 2023-02-28

### Changed
- loading panel rework activity of elements on loading


## [4.2.20.6] - 2023-02-24

### Fixed
- some small bugfixes


## [4.2.20.5] - 2023-02-21

### Changed
- Performance improvement: Removed all unnecessary calls of config_updateObjects on start and reload


## [4.2.20.4] - 2023-02-21

### Removed
- removed option for HTTPS-compression on login-panel because we will encrypt always

### Changed
- Performance improvement: Less unnecessary calls of config_updateObjects on start and reload

### Fixed
- other bugfixes in certificate check
- don't use certificate check before server version opsi 4.2, because it would not work


## [4.2.20.3] - 2023-02-20

### Changed
- Reworking the login panel to remove the loading frame on login


## [4.2.20.2] - 2023-02-09

### Added
- Warning on button for licensing in main frame

### Changed
- reworking the location of yes / no / ok ... buttons
- small changes in behaviour in frame for changing configs


## [4.2.20.1] - 2023-01-23

### Added
- Check server TLS certficate / warn if new cert (ssh-like)
- IPv6-support in input dialogs
- new options for resetting products
- Tooltip in Markdown over Links

### Changed
- Reworking Loading Panel on login
- Reworking positions and sizes of windows
- sizes of columns now will not change on reload (clientlist, Localboot products and Netboot products)
- some performance boosts
- JavaFX now used to start Browser in Markdown (works better under Linux)
- Small changes in Dependencies-Infos

### Fixed
- Bugfixes in Clientsearch


## [4.2.19.11] - 2023-01-20

### Changed
- Changed directory of java so that old links will work

### Fixed
- Bugfix in clientsearch by failed installation


## [4.2.19.10] - 2023-01-13

### Fixed
- Another Bugfix in clientsearch


## [4.2.19.9] - 2023-01-05

### Fixed
- Bugfix in clientsearch


## [4.2.19.8] - 2023-01-05

### Fixed
- fixed problem with starting firefox for looking on the opsiclientd-timeline


## [4.2.19.6] - 2023-01-05

### Fixed
- Small buxfixes


## [4.2.19.4] - 2022-10-18

### Added
- Markdown support in advice and description of products

### Removed
- Removed java-properties in opsi-package since java is now always delivered with program
- Removed properties fallback_tlsv1 and sqldata_force

### Changed
- Clients are now marked after their creation
- Faster creation of clients with CSV-Import
- new Logging (standard-loglevel is now 4, performance is now better)


## [4.2.18.1] - 2022-20-18

### Added
- CSV-Import for creating new clients

### Changed
- Popup in Seconds for messages sent to clients;
