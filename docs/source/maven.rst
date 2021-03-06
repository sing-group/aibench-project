Maven
*****

AIBench uses Maven to build itself, as well as to build applications with
AIBench. We also provide you with a `Maven archetype
<https://maven.apache.org/guides/introduction/introduction-to-archetypes.html>`_
for AIBench applications.  This archetype allows you to create both a regular
AIBench plugin or an AIBench runnable application.

.. _one-plugin-per-project:

.. note:: 
  
  Each AIBench plugin is developed as an **independent** Maven artifact (or
  project, if you like). In this sense, you can only develop only *one* plugin
  per project. If your application is composed of more than one plugin, you have
  to create several projects (see :doc:`multiplugin`). Each project is, at the
  same time, a runnable AIBench application running all plugins the project
  depends on.

The important Maven phases during the build lifecycle are:

- **compile**: which creates a runnable application in the ``/target`` folder
  (you will se the run.sh there).
- **package**: which will also create a ``.jar`` file with the plugin you are
  developing with this Maven project. You can use directly this plugin in other
  AIBench projects. If those projects are also Maven-based, you can run the
  install phase (recommended).
- **install**: makes available this plugin for the rest of Maven-based AIBench
  projects in your computer.


Create an AIBench application using the Maven archetype
=======================================================

You have to run the following command:

.. code-block:: console

  mvn archetype:generate \
  -DarchetypeGroupId=es.uvigo.ei.sing \
  -DarchetypeArtifactId=aibench-archetype \
  -DarchetypeVersion=2.5.1-SNAPSHOT  \
  -DgroupId=es.uvigo.ei.sing \
  -DartifactId=my-aibench-application\
  -DinteractiveMode=false \
  -DarchetypeRepository=http://sing.ei.uvigo.es/maven2

This will create the new application under the folder
``my-aibench-application``. You can select the version of the archetype in
``-DarchetypeVersion``. The version of the archetype also corresponds to the
AIBench version.

Alternatively, if you want to create an AIBench application using the latest
source code version of AIBench, you have to download and build the latest
version of AIBench.

.. code-block:: console

  git clone https://github.com/sing-group/aibench-project.git
  cd aibench-project
  mvn install
  cd ..

Now you have to see what is the last version of AIBench you have downloaded.
You can see it in ``aibench-project/pom.xml`` inside the ``version`` tag.

Finally, you have to issue the next command to create your AIBench application:

.. code-block:: console
  
  mvn archetype:generate \
  -DarchetypeGroupId=es.uvigo.ei.sing \
  -DarchetypeArtifactId=aibench-archetype \
  -DarchetypeVersion=[put-here-the-aibench-version-you-downloaded] \
  -DgroupId=es.uvigo.ei.sing \
  -DartifactId=my-aibench-application \
  -DinteractiveMode=false 

.. _build-application: 

Build the AIBench application
=============================

You have only perform:

.. code-block:: console

  mvn compile


Inside the ``/target`` directory you will find the entire AIBench application.

In order to package your plugin in order to place it in other AIBench
applications, you have to:

.. code-block:: console

  mvn package

Inside the ``/target`` directory you will find a ``.jar`` with your plugin.

If you want to develop another AIBench application with Maven and which depends
on your plugin, run:

.. code-block:: console

  mvn install

You will be able to add your plugin as a dependency in another Maven-based
AIBench projects as is explained later.

Using the pom.xml
=================

Managing dependencies
---------------------

In AIBench there are three types of dependencies:

- **AIBench Core-libraries** (for example, the plugin engine of AIBench), which
  are placed under ``/lib`` directory in an AIBench application.
- **AIBench basic plugins**, needed by you application (for example, ``Core``,
  ``Workbench``, other plugins developed by you or by third-party developers,
  etc.), which are placed under the ``/plugins_bin`` directory.
- **Third-party Libraries** needed by your plugin, which are placed *inside*
  your plugin (inside your ``jar``).

All of these dependencies are managed as regular dependencies in Maven, however,
you have to also indicate the type of dependency in two special properties
inside the ``pom.xml`` of your plugin:
  
.. code-block:: xml

  <properties>
    <aibench.lib.artifactIds>
      javatar,aibench-aibench,jhall,log4j
    </aibench.lib.artifactIds>
    <aibench.plugins.artifactIds>
      aibench-core,aibench-workbench,aibench-shell,aibench-pluginmanager
    </aibench.plugins.artifactIds>
  </properties>

- The ``aibench.lib.artifactsIds`` is a comma-separated list (avoid spaces!) of
  AIBench Core-libraries (normally, you will not change this).
- The ``aibench.plugins.artifactsIds`` is a comma-separated list (avoid spaces!)
  of other AIBench plugins that you need in your application, so they will be
  placed inside ``plugins_bin`` (see also :doc:`multiplugin`).

The rest of dependencies, not listed in these two lists, will be placed inside
of your plugin.


Using Eclipse with m2e
======================

There is an issue with m2e in order to interpret the ``pom.xml`` of our
archetype. 

You have to:

1. Ignore the errors with the 'executions' nodes in the ``pom.xml``, as Eclipse
   quick-fix suggests.
2. Run ``maven compile`` (using Eclipse if you want), for the first time you
   create the project and everytime you change your dependencies. This will
   create the ``/target/lib`` and the ``/target/plugins_bin`` directory with all
   the needed jar files. Update your project (F5) in order to see these changes.
3. In order to run/debug application, you have to create the following *Java Run
   Configuration*.
   
   1. Set the ``es.uvigo.ei.aibench.Launcher`` as *Main class*.
   2. Set "plugins_bin" as *Program argument*.
   3. Remove every entry in the *User Entries classpath*.
   4. Add all the Jars inside the ``/target/lib`` directory to the User Entries
      classpath.
   5. Set the *Working directory* to
      ``${workspace_loc:youraibenchapplication/target}``
