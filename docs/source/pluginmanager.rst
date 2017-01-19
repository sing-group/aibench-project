Managing plugins at runtime
***************************

AIBench allows you to enable/disable plugins, as well as to give updates to
your application remotely via the ``Plugin Manager`` plugin. Basically, you
have to provide HTTP access to updated versions of the plugins your application
is composed of.

First of all, the ``src/main/resources/conf/plugins.conf`` file, all
The file is used, by now, to enable/disable plugins

.. code-block:: jproperties

  myplugin.active=false


The `PluginManager` plugin
==========================

This plugin adds a new panel in the Workbench's bottom zone to bring an Plugin
Information Panel which:

- Shows the current available plugins, their version, and their state
  (disabled/enabled).
- Allows the user to enable/disable current installed plugins, by changing their
  option in the ``plugins.conf``. Changes only will take effect after AIBench
  restarts.
- Allows a quick plugin update and missing dependency plugins install.
- Allows the user to manage a plugin repository to download. The user can change
  the repository url and can view the plugins stored through a Plugin Repository
  Dialog. The Plugin Repository Dialog informs about which plugins can be
  installed or updated. When the plugin is downloaded/updated, AIBench should be
  restarted. When the user searches for a new plugin, AIBench connects and lists
  the available plugins from the repository showing something like:

  +----------------------+-----------------+------------------+----------------+-----------------------+
  |Plugin name/Dependency|Installed Version|Repository Version|Required Version|Download               |
  +======================+=================+==================+================+=======================+
  |New plugin            |             1.5 |              2.0 |                | [Update - Version 2.0]|
  +----------------------+-----------------+------------------+----------------+-----------------------+
  | aplugin(1.1+)        |             1.4 |             none |             1.4|                       |
  +----------------------+-----------------+------------------+----------------+-----------------------+
  | bplugin(1.2)         |            none |              1.2 |             1.2|[Install - Version 1.2]|
  +----------------------+-----------------+------------------+----------------+-----------------------+
  | New plugin 2         |            none |              1.0 |                |[Install - Version 1.0]|
  +----------------------+-----------------+------------------+----------------+-----------------------+

- The PluginManager checks the dependencies between the plugins to warn the user
  about inconsistencies or incompatibilities when a plugin is disabled or when a
  plugins is updated/installed.

Setting up and configure your remote plugin repository
======================================================

You will be able to create a web server with new or updated plugins
(repository). A repository should:

- Be available via HTTP. The URL of the store and the repository metadata file
  can be configured in the ``src/main/resources/conf/pluginmanager.conf``.

  .. code-block:: jproperties
  
    # URL of the repository.
    pluginrepository.host=http://my.software.org/plugins
    
    # File containing the repository metadata.
    pluginrepository.infofile=plugins.dat
    
    # Directory where the plugins are downloaded before being installed.
    plugininstaller.dir=plugins_install				

- The server should declare a metadata file with available plugins information,
  i.e.: http://my.software.org/plugins/plugins.dat

  .. code-block:: jproperties
  
    # The plugin UID.
    plugin1.uid=myplugin
    
    # The name of the plugin.
    plugin1.name=My plugin
    
    # The version of the plugin stored in the repository.
    plugin1.version=1.1
    
    # The plugins needed by the plugin (dependencies). - Optional
    plugin1.needs=pluginA[1.5+];pluginB[1.7]
    
    # The file containing the plugin files. 
    # May be in .zip, .jar, .tar or .tar.gz format.
    plugin1.file=plugin1.zip 
    
    # The md5 of the file. Allows client-side validation of the downloaded
    # files. - Optional
    plugin1.md5=1a2b3c4d5e6f7890 

    plugin2.uid=myotherplugin
    plugin2.name=My other plugin 
    plugin2.version=1.5
    plugin2.file=plugin2.zip

- The client is responsible to check the current AIBench meets the dependencies. 