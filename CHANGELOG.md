## [4.3.7.1] - 2025-02-20
## Changed
- Enable copy & paste in "About opsi-configed" dialog

### Fixed
- Issue: after reloading all data, the properties for the products (localboot and netboot) are gone.
- Correct location of list selection dialog in health check client selection and property editing
- Create the correct config `opsiclientd.event_net_connection.active` and not `event_net_connection.active`
- Don't clear newly added options for single-vlaue properties (if selected)

## [4.3.7.0] - 2025-02-18
### Added
- Feature for selecting multiple groups in host and product tree
- Show loading screen on search query execution
- Option in client table to invert selection

### Changed
- Switch from opsi-package-manager to opsi-cli
- Remove possibility to create config in Client and Depot configuration

### Fixed
- Highlighting issue in Health Check dialog under Windows
- Issue: lost selection after full reload
- Issue: cancel selection buttons incorrectly shown after full reload

## [4.3.6.8] - 2025-03-13
### Fixed
- Correct size in driver upload
- Issue: `ClientSelectionDialog` UI not properly updated, when clicking on "Edit search" in `SavedSearchesDialog`
- Issue: user-roles not enforced correctly

### Changed
- Open new instance of `ClientSelectionDialog`, when clicking on "Create new search" in `SavedSearchesDialog`
- `NPE` when reloading all data after changing groupaccess for client tree
- Init tab components at start

## [4.3.6.7] - 2025-03-06
### Fixed
- Issue: non scrollable notes field in client info panel
- Issue: Old selection disappears after changing visible columns in client table
- On some occasions occuring `NPE`, when opsi-configed starts.
- `NPE` when opening install package dialog after building the package first in "Make opsi package..." dialog 
- Dialogs rendered improperly after restart (for example, new client dialog)
- `JComboBox` resizing issue that caused nearby button to shift or become hidden when the selected value changed or new data was added.
- WebDAV Functionality Breaks with SSO Integration

### Added
- Select all products/depots/clients in the trees and list with a click on a button in the tab

## [4.3.6.5] - 2025-02-24
### Fixed
- Errors in rendering of some columns in product table

## [4.3.6.4] - 2025-02-20
### Fixed
- Correctly stop editing in product property states to prevent different errors
- Only write empty entry and no time when no date was entered in start/end time for health check downtime
- Nullpointer exception when updating license pool

### Changed
- Parallelize and improve performance in loading statistics in licensing management
- Remove unnecessary target configuration column in product tables

## [4.3.6.3] - 2025-02-18
### Changed
- Sort messages after warning level in Health Check
- Sort "none"/empty values in state and action columns in product configuration to the end
- We parallelize loading data to improve the performance

### Fixed
- Nullpointer exception when creating a new group, clicking on the group and then elsewhere
- Freeze of configed when waiting for response when firing event to many clients


## [4.3.6.2] - 2025-02-04
### Fixed
- NullpointerException in default properties in depot configuration

### Changed
- Don't use placeholders in input fields
- Make it possible to copy the property name into the clipboard
- Small redesign of Login Dialog

## [4.3.6.1] - 2025-01-28
### Fixed
- Only delete productPropertyStates for selected product type
- Nullpointerexception when editing group that was just created
- We can now return to the dialog when we cancel trusting the certificate when we login by SSO
- Exception in SearchTargetModelFromTable when no result found in search
- Exception when a ProductPropertyState exists, but the respective ProductProperty does not exist any more

### Changed
- Rework all the Server console dialogs to use standard java library (JOptionPane) instead of own classes
- Rework Saved Searches Dialog and Remote Control dialog in order use JOptionPane instead of own classes


## We go back to version 4.3.6.x because the release of opsi 4.4 was canceled...

## [4.4.0.14] - 2025-01-09
### Fixed
- Not saving ProductPropertyStates and ConfigStates when you edit again after saving

### Changed
- Use normal login without SSO when password or OTP field active and pressing "ENTER"-key
- Don't stop configed when there are irregular arguments
- Use new CellEditor to edit date in tables
- Use new CellEditor to edit notes in license tables
- Rework the dialog frames to use standard java library (JOptionPane) instead of own classes

## Added
- Add option of Visibility to method processActionRequest ("Execute") in product tables
- Add options to Health Check to en/disable the check for clients and depots

## [4.4.0.13] - 2024-12-09
### Changed
- Rework editing and creation of configs, configStates, productProperties and depot properties

## [4.4.0.12] - 2024-12-04
### Fixed
- No selection when no element found from search
- Remember last used domain to create client
- Always show loading animation when loading data

## [4.4.0.11] - 2024-11-13
### Changed
- Improve login dialog usability

## [4.4.0.10] - 2024-11-06
### Added
- Add option to log in via Single Sign On (SSO)

## [4.4.0.9] - 2024-10-30
### Fixed
- Correct path for saving certificates when entering port in login dialog
- Start configed when trusting the certificate once
- Nullpointerexception when user is not in group opsi admin
- Correctly fix default values as specific values for product property states on clients and depots
- Correctly deactivate filter in client selection

### Changed
- Move reload and save buttons to top toolbar
- Some small changes in component and gap sizes
- Health Check now works with new opsiconfd version

## [4.4.0.8] - 2024-09-25
### Changed
- Make it possible to delete/create configs in config state editing for depots and clients
- Don't create unnecessary copies of configs when (re)loading -> better performance
- Rotate up to 10 logfiles
- Make the logfile name independent from the user name

### Fixed
- Nullpointerexception when unexpected entry in config "configed.host_actions_disabled"
- Create whole path of folders for CA-Certificate if folders don't exist yet


## [4.4.0.7] - 2024-09-18
### Changed
- Remove unnecessary feature to arrange dialogs
- Cast Maps/Lists instead of remapping them always, better for performance
- Remove waiting loop in when loading data in login. Will save up to 500ms every login.
- Find a better way to get a timeout. Timeout is set at 5000ms now.
- Download all CA certificats instead of only the opsi CA certificate
- Use path <user-config-dir>/opsi/services/<service_host>_<service_port>/ca-certs.pem for CA certificats, which is also used by opsi-cli

### Fixed
- Show correct client in HostStatusPanel after full reload
- Correctly refresh client configuration page after full reload
- Nullpointer Exception when reloading host configs of a depot
- Also reset netboot product states when the user wants to reset Localboot AND Netboot product states

## [4.4.0.6] - 2024-09-05
### Fixed
- Show correctly the new lines ("\n") in tooltips
- Correct support page in help menu

### Changed
- Small change in Layout in Dialogs
- Rework GUI design, put View change to the left, etc.


## [4.4.0.5] - 2024-08-15

### Changed
- Keep connection and certificate validators so that they don't have to be recreated with every server request to improve performance

## [4.4.0.4] - 2024-08-14

### Changed
- Show licenses management, opsi licensing info, health check and the dashboard in MainFrame

### Fixed
- Space between options in command for uninstalling opsi products


## [4.4.0.3] - 2024-08-08

### Changed
- Only show enabled tabs in Client/Depot/Server configuration
- Lazy loading for all tabs in Client/Depot/Server configuration apart from Client and Product tables

### Fixed
- Don't unnecessarily refresh saved searches when editing search
- Problems with column visibility in Product and Client tables after full reload
- Start Remote Desktop Processes synchronuously instead of one after another when selected several clients


## [4.4.0.2] - 2024-07-18

### Changed
- Assume server version 4.3 if no header was found
- Show that data have changed in client info only when really changed (and not by pressing "CTRL" for example)
- Show border around tooltip in dark mode
- Rework Logging so that message strings are not build when not needed (performance)
- React to "ENTER" input when in host and OTP dialog in login
- Scroll in scrollPane instead of inside the table rows in host configs, product properties, etc.

### Fixed
- Don't use native library from flatlaf in MacOS to prevent crashes


## [4.4.0.1] - 2024-07-11

### Changed
- All icons (apart from the hardware icons) have been replaced. Mainly by Intellij Icons in SVG format
- Checking for client reachability is removed since it's obsolete thanks to the messagebus


## [4.3.3.10] - 2024-07-08

### Fixed
- Errors with table and opsi-host-key in CSV client import
- Buttons in CSV template dialog on bottom not shown completely


## [4.3.3.9] - 2024-06-27

### Changed
- Make it possible to connect to server and server console without VPN module
- Don't allow connection to clients without VPN module


## [4.3.3.8] - 2024-06-25

### Changed
- Disable Terminal with user roles config "ssh.menu_serverconsole.active"

### Fixed
- Nullpointer Exception when messagebus events are received before data are loaded on start

## [4.3.3.7] - 2024-06-24

### Fixed
- Nullpointer Exception when deleting client


## [4.3.3.6] - 2024-06-24

### Fixed
- Don't fire updateTable event when row is -1 (causes wrong rendering of cells)
- Nullpointer Exception when product is updates that is not in product list (e.g. user roles)


## [4.3.3.5] - 2024-06-24

### Changed
- Sort results when searching for directories on server

### Fixed
- Nullpointer Exception when updating table with messagebus
- ClientTable not refreshed correctly when adding another depot to the selection while some clients are selected
- Nullpointer Exception when exporting groups of clients to CSV (for reimport)
- Center frames in export options on main frame

## [4.3.3.4] - 2024-06-19

### Fixed
- Performance problem when selecting clients in client tree and then show product info

## [4.3.3.3] - 2024-06-18

### Changed
- Use Swing component instead of JavaFX for loading mechanism because of performance issues in JavaFX

### Fixed
- Button to copy HostKey into Clipboard

## [4.3.3.2] - 2024-06-10

### Changed
- No public changes

## [4.3.3.1] - 2024-06-06

### Fixed
- Create all 4 WAN configs (before only 3 of them were created)
- Make it impossible to delete all clients and depots by accident

### Changed
- Rework design of Search in tables
- Rework design of login dialog
- Rework design of checkboxes in client info panel
- Rework design of all user/password input fields
- Remove Wake on LAN timer, only directly wake up clients
- Remove Product installation in Product Actions panel (already exists in server console)
- Select all products in a group in table on double click
- Show the products from depots selected in depotslist in product tree

## [4.3.3.0] - 2024-05-14

### Changed
- Replace SSH server console with messagebus server console
- Remove user roles config key `ssh.serverconfiguration.active`
- Remove MySQL client search (was available only before 4.3 with MySQL)
- Remove multi client hardware info (was available only before 4.3 with MySQL)
- Update to Java 21
- Update to JavaFX 21

### Fixed
- Actions done several times in Client menu after full reload

## [4.3.2.14] - 2024-04-29

### Changed
- Select software (with proper license pool) in license management after applying change in "Software name -> License pool" dialog

### Fixed
- Don't add options twice to reset products after full reload
- Problems in Client Tree after full reload
- `NullPointerException` on reloading localboot or netboot table (occuring when new product versions in database exist)
- Now also show subgroups of permitted product groups
- `ArrayIndexOutOfBoundsException` on selecting alternative view in license management dialog
- In "Software name -> License pool" lower table is not updated on selection of new software name
- Don't add option of empty String for visible columns in product table on first start

## [4.3.2.13] - 2024-04-24
### Fixed
- Include JavaFX modules for macOS
- Ability to open messagebus terminal on a client directly
- `NullPointerException` when trying to select group that does not exist any more 
- Show groups in directory group in userroles if parent group is not allowed

### Changed
- Don't define default session for terminal connection, to allow for user to select session.

## [4.3.2.12] - 2024-04-17

### Changed
- Include software entry's ident to "Missing software entry" dialog's message to make apparent which software entry is missing
- Use insecure connection in Messagebus, when certificate verification is disabled
- Maintain consistent log levels for unspecified lines
- Save user preferences on table's display fields
- On reload in "localboot products" and "netboot products" tabs, installed packages are shown in and deleted packages are removed from the table
- New style for reload animation

### Fixed
- Flags `-ff` and `--feature-flags` to enable currently in development features, for testing purposes
- `NullPointerException` on product reset
- `MySQLdb.integrityError` when moving clients to NOT_ASSIGNED directory/group
- Product filter disabled after a reload or switching client selection
- "Failed actions" search and selection
- The lower control panel in logviewer keeps disappearing on resize
- For hardware devices with the same name in different hardware class, the information is shown incorrectly
- Can't change table column visibility in product table after full reload
- When group, that has subgroups, is permitted for a user, the subgroups aren't included in a group.
- On reload in "default properties" tab no changes are made to the table (removed packages are shown and installed packages aren't shown)

## [4.3.2.11] - 2024-03-28

### Fixed
- Correctly init ClientTree when reloading and when user roles have been changed
- User roles product group access

## [4.3.2.10] - 2024-03-26

### Fixed
- Invalid Range error when updating product table (Manually or by messagebus)

### Changed
- Export whole table into PDF and not only selected rows
- Add item to change size between product info and product description

## [4.3.2.9] - 2024-03-21

### Changed
- Don't show server defaults for WAN, UEFI and Install on Shutdown in new client dialogue
- Make it possible to edit WAN, UEFI and Install on Shutdown in new client dialogue even when enabled by server default

### Fixed
- Connectivity status of depots
- OTP field to login dialog
- OTP option to command line
- SWAudit command line options

## [4.3.2.8] - 2024-03-15

### Changed
- Use better icons for Checkboxes and some other cases

### Fixed
- Add button to download diagnostic data in Health Check dialogue
- Updating Host Info Data

## [4.3.2.7] - 2024-03-14

### Changed
- Remove config states usage in `TableSearchPane`
- Remove usage of icons in narrow layout of `TableSearchPane` (use checkboxes instead)
- Make product/client icon filled when object is selected in table

### Fixed
- New option for searching multiple words in `TableSearchPane`
- Show product description in tooltip in tree

## [4.3.2.6] - 2024-03-06

### Fixed
- `Nullpointerexception` on reload
- `Nullpointerexception` on updating product table, when list is reduced

## [4.3.2.5] - 2024-03-05

### Fixed
- Tooltip for changed states in Product id

### Changed
- Update group icons
- Tooltip for products in product tree

## [4.3.2.4] - 2024-02-28

### Changed
- Update Icons in client tree
- Only show Clients in allowed group, but not in their subgroups

### Fixed
- Show every product only once in product tree
- Show icons for products and product groups in the product tree

## [4.3.2.3] - 2024-02-27

### Fixed
- `NullpointerException` when messagebus update on products
- Sort product tables on start

## [4.3.2.2] - 2024-02-26

### Fixed
- `NullpointerException` on reload

## [4.3.2.1] - 2024-02-26

### Fixed
- Add a product tree to show recursively all product groups and preselect the products for the tables
- Tabs to switch between the depot selection and the product/client trees

## [4.3.1.11] - 2024-02-28

### Fixed
- Sorting host groups alphabetically

## [4.3.1.10] - 2024-02-26

### Fixed
- Remote control should start only on space when in client table


## [4.3.1.9] - 2024-02-21

### Fixed
- If `user.{<username>}.privilege.host.createclient` is disabled "Create new opsi-client" button disappears only when client menu or popup menu is opened
- All products are included in PDF export, when exporting Localboot or Netboot products
- The page numbering in PDF exports is incorrect 1/3, 2/3, 3/3, 4/3

### Changed
- Allow to select only depots, which are specified in `user.{<username>}.privilege.host.depotaccess.depots`
- Show only groups, which are specified in `user.{<username>}.privilege.host.groupaccess.hostgroups`
- Show only the total number of clients, that are visible for user, next to "Clients total:" label
- Support TXT files in logviewer
- Removed partial loading mechanism for logfiles

## [4.3.1.8] - 2024-02-08

### Fixed
- `NullPointerException` when opening product selection dialogue

## [4.3.1.7] - 2024-02-08

### Fixed
- Modules file uploading via SFTP
- `NullPointerException` when priority or position columns in localboot or netboot product tab is displayed by default
- Product states and actions for netboot products aren't shown
- No depot selected by default
- Messagebus updates for connected clients
- opsiHostKey for CSV export. You can now choose which data to include into the CSV file

### Changed
- Remove net_connection from default WAN config
- Provide tooltips for depot selection (on the right side next to "Edit properties on depot(s)" label) in "default properties" tab
- Remove unnecessary tooltips in tables, tabs, etc.

## [4.3.1.6] - 2024-02-01

### Fixed
- Host paramater `user.{<user>}.privilege.host.all.registered_readonly` has no effect
- The authorization is overriden, when logging in with an uppercase in the username
- Enable messagebus updates after reconnection to server
- Logviewer can open files without file extension

### Changed
- Rename RPC method `softwareLicenseFromLicensePool_delete` to `softwareLicenseToLicensePool_delete`
- Small performance improvement on startup, reload and depot change in environments with many clients
- Make extra credits dialogue to separate Credits from About dialogue

### Fixed
- Add search field for product selection dialogue

## [4.3.1.5] - 2024-01-19

### Fixed
- Parameter `user.{<user>}.privilege.product.groupaccess.productgroups` has no effect on dispalyed products.
- Keep client selection in product table after full reload

### Changed
- Show only netboot products, that are available in specified groups in `user.{<user>}.privilege.product.groupaccess.productgroups` parameter.
- Allow only group selection, that are specified in `user.{<user>}.privilege.product.groupaccess.productgroups` parameter.
- Better form in free client search

## [4.3.1.4] - 2024-01-12

### Added
- Auto completion and sort clients in license usage

### Fixed
- `ArrayIndexOutOfBoundsException` on calling `-qg` or `--definegroupbysearch` command with two arguments/parameters
- Software information not shown
- Log files opening in context menus by double-click

### Changed
- Rework Clienttree (performance)
- Selection inactive colors
- Colors in tables
- Setting Product version when setting `installed`

## [4.3.1.3] - 2024-01-09

### Added
- Logviewer options to menubar in opsi-logviewer
- Connect messagebus-terminal to clients and depot servers
- Partial loading for logfiles

### Fixed
- NullPointerException some time after closing Terminal
- Bug in buttons to add users and user roles
- NullPointerException in Messagebus when creating/deleting clients
- Delete data from licensing managment when deconnecting from server
- Update clientinfo correctly when reloading data
- NullPointerException on closing Dashboard when selected a specific depot
- NullPointerException on opening license displayer without license management module

### Changed
- Make it possible to enter empty values in editable tables
- Make it possible to enter empty values in client information
- Rework element and gap sizes in licensing management

## [4.3.1.1] - 2023-12-15

### Changed
- Don't preselect saved depot selection because it causes performance issues

## [4.3.1.0] - 2023-11-14

### Fixed
- Configed was not loading when restricted to productgroups
- Center License Information frame before showing it
- Fix typo for hardware address in host column names
- Clear client selection on depot change (old selection was kept)

### Changed
- Update translations in English, German and French
- Don't warn from dependency-requirements in opsi 4.3

## [4.3.0.13] - 2023-11-07

### Fixed
- Error message not shown in AbstractErrorListProducer
- Saving SystemUUID for a client now possible
- Reactivating user roles working again in dialogue

### Changed
- Removed Fading Glasspane from Error producer and waking clients
- Removed unnecessary calls (performance)
- All product actions call 'processActionRequests' instead of 'on_demand'

## [4.3.0.12] - 2023-10-31

### Fixed
- Create PDF-files from Hardware information
- Displayed column editing in "Hardware Information" tab
- Showing if column is displayed in "opsi config editor hardware classes / database columns" dialog
- Calculation for remaining software license
- Vanishing new entries on save in license management frame
- Reloading of license management frame
- Using SPACE+CTRL for (de)selecting clients
- Retrieve version for products, that were installed as once or custom installation even though installationStatus is not_installed

### Changed
- Remove unnecessaray Logging-operation (performance)
- Remove HARDWARE prefix from column names in "Hardware Information" tab
- Change labeling for vendors in "Hardware Information" tab
- Localization issues in standard-dialogs like JFileChooser or JOptionPane
- Remove duplicate RPC method calls on reloading license and starting license frame

## [4.3.0.11] - 2023-10-19

### Fixed
- Make search button text visible in logpane
- Serialization issue, when trying to save depot configuration
- Initialize saved states before login
- Show hardware info for multiple clients on opsi-server 4.2 or before (NullpointerException)
- Not all products are displayed in depot-configuration

### Changed
- Rework reload mechanism so that reload loads exactly the same data as the starting process
- All product actions now save data before acting

## [4.3.0.10] - 2023-10-17

### Fixed
- Bug changing product properties
- Nullpointerexception when product does not exist in general, but only on depot

## [4.3.0.9] - 2023-10-16

### Changed
- Themes (Light / Dark)
- Open Sans Font
- Don't load software information on start (performance)
- Only save shown software information (less memory usage)
- Remove unnecessary calls on startup (performance)
- Remove unnecessary calls on reload (performance)
- Reset only changed products (performance)
- Dashboard loading reworked (performance)
- Rework copying clients (performance)
- Remove unnecessary calls when starting client creation dialog (performance)
- Show only available modules as option in opsi-licensing
- Client tree selection / navigation reworked
- Open multiple windows in logviewer when opening archives with several files
- Remove UEFI-Boot information in opsi 4.3 (only show as active, if client has entry in "clientconfig.uefinetbootlabel" config)

## [4.2.22.16] - 2023-10-13

### Fixed
- Fix bug showing 'processor' information in hardware information


## [4.2.22.15] - 2023-10-02

### Fixed
- Surround user password in quotation marks (password is acquired from SSH deploy opsi-client-agent frame)
- Hide password in log file

## [4.2.22.14] - 2023-09-27

### Fixed
- Configed can start now when a client is in two different DIRECTORY groups

## [4.2.22.13] - 2023-08-28

### Fixed
- Groups (specialities) available again 

## [4.2.22.12] - 2023-08-24

### Fixed
- Show actual clientlist in column "boundToHost" in licence management

# [4.2.22.11] - 2023-08-21

### Fixed
- Open several opsi-logviewers with zip-files now possible again

## [4.2.22.10] - 2023-07-25

### Fixed
- Show Selection box for report in Product table correctly

## [4.2.22.9] - 2023-07-19

### Fixed
- Sort position of products correctly
- Make it possible to read all textfiles in logviewer
- Go to previously selected line when changing loglevel

## [4.2.22.8] - 2023-07-17

### Fixed
- Problems with editing and deleting product properties
- opsi-configed did not start when invoking persistenceController.addGroup because it was null
- Cursor bug when changing loglevel in logviewer
- Right property is selected in list when changing values

## [4.2.22.7] - 2023-07-11

### Fixed
- Issue with ReachableUpdater slowing the program down

## [4.2.22.6] - 2023-07-05

### Fixed
- Creating and editing groups works again
- Fix wrong message after successful driver upload
- Fix session information
- Use gzip/lz4 and messagepack also for POST-requests to the server (small performance advantage)

## [4.2.22.5] - 2023-06-28

### Fixed
- Creating und deleting licensepool - product relations works again

## [4.2.22.4] - 2023-06-23

### Fixed
- SSH console will now execute all commands instead of only the first one

## [4.2.22.3] - 2023-06-19

### Changed
- Reworking the loading animation when waiting for Data from the server

## [4.2.22.2] - 2023-06-15

### Fixed
- Opening zip-files with the opsi-logviewer works
- Client selection remains after reload

### Changed
- Login Frame is now not always in foreground and has icon

## [4.2.22.1] - 2023-06-01

### Added
- opsi-logviewer is now part of opsi-configed

### Changed
- Were using now Messagepack for datatransfer with server -> performance boost
- Property for language removed, since language changes in configed will be saved for user

### Fixed
- You cannot make on-demand actions when in readonly mode
- Issue with creating Licences fixed (Volume, etc.)

## [4.2.21.3] - 2023-05-09

### Added
- Icon for connection status to Messagebus shown on bottom right

### Changed
- Activity Panel moved to bottom right

### Fixed
- Installing and removing of opsi-packages
- Problems with different actions in Licensemanagement
- Problem with client selection with CTRL-key
- Startup of configed on some filebackend servers

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
