Creating multi-plugin AIBench applications
******************************************

One of the main focus of the AIBench framework is to enhance reusability. For
achieving this, you can exploit the :ref:`underlying plugin-architecture of
AIBench <plugins-architecture>`.

In this sense, You can put your :ref:`Operations <creating-operations>`, :ref:`Views
<creating-views>` and :ref:`Datatypes <creating-datatypes>` in one, two or more
separated plugins, based on your own design decision. The only rules that have
to be followed are:

- If there are :ref:`Operations <creating-operations>` and/or :ref:`Views
  <creating-views>` in a plugin, this plugin must be connected to the properly
  extension points as it was explained before.
- If the :ref:`Operations <creating-operations>` and/or :ref:`Views
  <creating-views>` in a plugin make use of the :ref:`Datatypes
  <creating-datatypes>` located in other plugin, this plugin must
  :ref:`depend <creating-plugin-dependencies>` on the plugin containing the
  needed Datatypes.
  
You have to develop one Maven project for each plugin (see :doc:`maven`). For
example, if your application will have two plugins (``plugin1`` and ``plugin2``)
and ``plugin2`` depends on ``plugin1``, you will need to:

1. :ref:`Create an AIBench application <create-aibench-project>` for ``plugin1``
   using the archetype.
2. Perform ``mvn install`` in the ``plugin1`` application.
3. :ref:`Create an AIBench application <create-aibench-project>` for ``plugin2``
   and establish a :ref:`dependency to plugin1 <creating-plugin-dependencies>`
   (see :ref:`next section <creating-plugin-dependencies>`).

.. _creating-plugin-dependencies:

Creating a dependency between plugins
--------------------------------------
You will need to create dependencies between plugins when:

- You are developing a :doc:`multi-plugin AIBench application <multiplugin>` and
  some plugin needs to access classes or  resources from another plugin.
- You need to get access to classes and resources in another AIBench plugin, for
  example, if you want to use the AIBench Core and Workbench API (see
  :doc:`api`).

The connection and dependency between plugins is made through the ``pom.xml``
file and, optionally, trough the ``plugin.xml`` file.

In the ``pom.xml``, you need to create a regular maven dependency as well as to
declare the artifact id in the ``aibench.plugins.artifactIds`` property.

.. code-block:: xml

  <project>
    ...
    <properties>
    ...
      <aibench.plugins.artifactIds>
        aibench-core,aibench-workbench,aibench-shell,aibench-pluginmanager,another-plugin
      </aibench.plugins.artifactIds>
    ...
    </properties>
    ...
    <dependencies>
      ...
      <dependency>
        <groupId>my-group-id</groupId>
        <artifactId>another-plugin</artifactId>
        <version>[version]</version>
        <scope>compile</scope>
      </dependency>
      ...
    </dependencies>
    ...
  </project>
    
If you need to access classes or resources of one plugin from another, you will
also need to add a ``dependency`` in the ``plugin.xml``:

.. code-block:: xml

  <plugin start="true">
    <uid>my-aibench-application</uid>
    ...
    <dependencies>

      <!-- we will use the API so we need to 
      depend on Core and Workbench plugins -->
      <dependency uid="aibench.workbench"/>
      <dependency uid="aibench.core"/>

      <dependency uid="another.plugin"/>
      
    </dependencies>
    ...
  </plugin>
    
.. note::
  
  Why do you need to edit two files? The ``pom.xml`` makes that your application
  include another plugin, and the ``plugin.xml`` allows you to also *use* a
  plugin from another, that is, classes from one plugin can use classes from
  another plugin. Remember that plugins are isolated by default (see
  :ref:`plugins-architecture`).
    