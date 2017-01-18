AIBench API
***********

Here you can find a brief summary of how to interact with AIBench API to add
special behaviour or customizations to your application. Interacting with
a framework API is normally done via hooks, which allow you to run code at
specific points, as well as calls to the framework's core objects.

API: Basic runtime objects
==========================

You can get access to the Core and the Workbench, via the *Singleton* design
pattern, and then, get access to the :ref:`Clipboard <clipboard>`, or the 
MainWindow, for example. You can read the JavaDocs docs for more details.

.. code-block:: java

  Core theCore = Core.getInstance();
  Clipboard theClipboard = Core.getInstance().getClipboard();
  
  Workbench theWorkbench = Workbench.getInstance();
  MainWindow theMainWindow = Workbench.getInstance().getMainWindow();
  
.. note::
  
  If you want to use the Core and/or Workbench objects, **you must depend on**
  the Core or Workbench plugins (see :ref:`creating-plugin-dependencies`).
  
Hooks
=====

Plugin lifecycle
----------------

You can add a class to listen when AIBench starts and add custom behaviour. 
You should implement a class extending ``PluginLifecycle`` class and put it
in the :ref:`plugin.xml <the-plugin-xml-file>`.

.. code-block:: xml

  <plugin start="true">
    <uid>myplugin</uid>
    <name>MyPlugin</name>
    <version>0.1</version>
    <lifecycleclass>myplugin.Lifecycle</lifecycleclass>
    
    <!-- rest of the plugin -->
  </plugin>

Core Listeners
--------------

You can listen to the main core events:

- ``ClipboardListener``, which notifies you when a :ref:`Clipboard <clipboard>`
  element is added or removed.
- ``HistoryListener``, which notifies you when a history element was added
  (i.e.: an operation has been executed) or removed.
- ``CoreListener``, which notifies you when some operation is enabled or
  disabled.
  
Adding listeners is simple:

.. code-block:: java
  
  Core.getInstance().addCoreListener( /* your CoreListener */ );
  
  Core.getInstance().getClipboard()
    .addClipboardListener( /* your ClipboardListener */);
    
  Core.getInstance().getHistory()
    .addHistoryListener( /* your HistoryListener */);
  
  
Workbench Listeners
-------------------

You can listen to Workbench events by adding a ``WorkbenchListener`` to the
Workbench. This will allow you to be notified when some view is shown, closed or
hidden, as well as when some component is added or removed.

Adding a listener to the Workbench is simple:

.. code-block:: java
  
  Workbench.getInstance().addWorkbenchListener( /* your WorkbenchListener */ );
  
Examples
========

Invoke Operations programmatically
----------------------------------

In this example, we show how to interact with the ``Core`` to invoke an Operation.

.. code-block:: java

  // the operation receives two files
  ParamSpec[] paramsSpec = new ParamSpec[] { 
      new ParamSpec(
          "inputfile", 
          File.class,
          new File(inputPdfPath),
          ParamSource.STRING_CONSTRUCTOR)
      ,
      new ParamSpec(
          "outputfile",
          File.class,
          new File(outputPdfPath), 
          ParamSource.STRING_CONSTRUCTOR)
  };

  OperationDefinition op = 
    Core.getInstance().getOperationById("operations.pdftotxt");
  
  Core.getInstance().executeOperation(op, null, paramsSpec); 
  
This will launch the operation in background (asynchronously). If you want to
synchronize your calling code with the operation completion, you can use
an ``ProgressHandler`` to be notified when the operation finishes.
  
.. code-block:: java

  // the operation receives two files
  ParamSpec[] paramsSpec = new ParamSpec[] { 
      new ParamSpec(
          "inputfile", 
          File.class,
          new File(inputPdfPath),
          ParamSource.STRING_CONSTRUCTOR)
      ,
      new ParamSpec(
          "outputfile",
          File.class,
          new File(outputPdfPath), 
          ParamSource.STRING_CONSTRUCTOR)
  };

  OperationDefinition op = Core.getInstance().getOperationById("operations.pdftotxt");
  
  final Object lockingObject = new Object();
  final List<Object> theResults = new ArrayList<Object>();

  ProgressHandler handler = new ProgressHandler(){

    public void validationError(Throwable t){}
    public void operationStart(Object progressBean, Object operationID){}
    public void operationError(Throwable t){}
				
    public void operationFinished(List<Object> results, List<ClipboardItem> clipboardItems){
			
        theResults.addAll(results);

        synchronized(lockingObject){
            lockingObject.notify();
        }
    }
  };

  synchronized(lockingObject){
      Core.getInstance().executeOperation(op, handler, paramsSpec); 
      try{
          lockingObject.wait();
      } catch(InterruptedException e){ }
  }
      
  
Clipboard-based enabling/disabling operations
---------------------------------------------

Here it is a ``ClipboardListener`` to enable/disable operations based on the
presence of objects of a given class in the :ref:`Clipboard <clipboard>`.

First, we will disable by default the operation which needs that a specific
object be available in the :ref:`Clipboard <clipboard>`.

.. code-block:: java

  @Operation(name="operation", enabled=false)
  

Then, we create the ``ClipboardListener``:

.. code-block:: java

  class ClipboardBasedOperationActivator implements ClipboardListener {
  	
    private HashMap<String, HashSet<Class>> operationRequirements =
      new HashMap<String, HashSet<Class>>();
  	
    public void addRequirement(String uid, Class c) {
      HashSet<Class> reqs = operationRequirements.get(uid);
      if (reqs == null){
        reqs = new HashSet<Class>();
        operationRequirements.put(uid, reqs);
      }
      reqs.add(c);		
    }
  	
    private void processClipboard() {
      for (String uid: operationRequirements.keySet()) {
        boolean requirementsSatisfied = true;
        for (Class c: operationRequirements.get(uid)) {
          if (Core.getInstance().getClipboard().getItemsByClass(c).size()==0) {
            requirementsSatisfied = false;
            break;
          }
        }
        if (requirementsSatisfied) {
          Core.getInstance().enableOperation(uid);
        } else {
          Core.getInstance().disableOperation(uid);
        }
      }
  		
    }
    public void elementAdded(ClipboardItem arg0) {
      processClipboard();
    }

    public void elementRemoved(ClipboardItem arg0) {
      processClipboard();
    }
  }

Finally, in order to start listening to :ref:`Clipboard <clipboard>` events form
the begining, we should then create and plug a ``PluginLifecycle``:

.. code-block:: xml

  <lifecycleclass>mypackage.Lifecycle</lifecycleclass>


.. code-block:: java

  package mypackage;
  // imports
  public class Lifecycle extends PluginLifecycle {

    public void start() {
      ClipboardBasedOperationActivator activator = 
        new ClipboardBasedOperationActivator();
        
      // configure the requirements
      // require that an instance of MyDataType must be in the clipboard 
      // in order to enable the "my.operation.id"
      
      activator.addRequirent("my.operation.id", MyDataType.class);
      
      Core.getInstance().getClipboard().addClipboardListener(activator);
    }
  }

