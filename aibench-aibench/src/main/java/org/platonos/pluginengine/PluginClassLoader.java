
package org.platonos.pluginengine;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FilePermission;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.Permission;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import org.platonos.pluginengine.logging.ILogger;
import org.platonos.pluginengine.logging.LoggerLevel;

import es.uvigo.ei.aibench.Util;

/**
 * Loads classes and resources from Plugin archives and embedded libraries without unpacking the archive to disk and reverses the
 * class look up heirarchy. Each Plugin has its own PluginClassLoader so it can be unloaded. This class takes a different approach
 * from the JDK 1.2+ delegation model. Instead of delegating to the parent loader first, the Plugin's classpath is checked first,
 * then each dependent loader gets a chance. This is essential for a Plugin to be packaged with its own classes and libraries.
 * @author Kevin Duffey (kevinmduffey@yahoo.com)
 * @author Evert
 * @author Nathan Sweet (misc@n4te.com)
 */
final class PluginClassLoader extends URLClassLoader {
	private static final String JAVASTR = "java.";
	private static final String JAVAXSTR = "javax.";
	private static final String PLUGINENGINESTR = "org.platonos.pluginengine";
	private static final String ENTRY_SEP = "!/";
	private static final String CLASSSTR = ".class";
	private static final String JARSTR = ".jar";
	private static final String ZIPSTR = ".zip";
	private static final String PRIVATE = ".pluginprivate.";

	private final Map<String, String> packagesMap = new HashMap<String, String>(100);
	private final Plugin plugin;
	private final ILogger logger;
	private final AccessControlContext acc;
	private final PluginArchiveURLClassPath ucp;

	/**
	 * Filters out all files that do not have embedded library extensions.
	 */
	private static final FilenameFilter embeddedLibraryFilter = new FilenameFilter() {
		public boolean accept (File dir, String name) {
			name = name.toLowerCase();
			return name.endsWith(JARSTR) || name.endsWith(ZIPSTR);
		}
	};

	static PluginClassLoader createClassLoader (Plugin plugin) {
		URL pluginURL = plugin.getPluginURL();

		URL[] classpath;
		if (pluginURL == null) {
			classpath = new URL[0];
		} else if (plugin.isArchive()) {
			classpath = new URL[] {pluginURL};
		} else {
			// Add any libraries found in the exploded directory plugin to the classpath.
			File archive = Util.urlToFile(pluginURL);
			File[] libraryFiles = archive.listFiles(embeddedLibraryFilter);
			classpath = new URL[libraryFiles.length + 1];
			classpath[0] = pluginURL;
			for (int i = 0; i < libraryFiles.length; i++) {
				try {
					classpath[i + 1] = libraryFiles[i].toURI().toURL();
				} catch (MalformedURLException ex) {
					plugin.getPluginEngine().getLogger().log(LoggerLevel.WARNING,
						"Error adding library to URL classpath: " + libraryFiles[i], ex);
				}

			}
		}

		return new PluginClassLoader(classpath, plugin);
	}

	private PluginClassLoader (URL[] urls, Plugin plugin) {
		super(urls, PluginEngine.class.getClassLoader());
		
		this.plugin = plugin;
		logger = plugin.getPluginEngine().getLogger();

		if (plugin.isArchive()) {
			acc = AccessController.getContext();
			ucp = new PluginArchiveURLClassPath(urls);
		} else {
			acc = null;
			ucp = null;
		}
	}

	/**
	 * Handles finding classes in this order: cache, classpath, dependency loaders, parent loader. This gives Plugins first try at
	 * finding a class so its libraries are found instead of the parent's, which may be the wrong version.
	 * @throws ClassNotFoundException If the class could not be found.
	 */
	public Class<?> loadClass (String className, boolean resolve) throws ClassNotFoundException {
		Class<?> clazz = null;

		try {
			// If the class is a system class, immediately delegate to the system loader.
			if (className.startsWith(JAVASTR) || className.startsWith(JAVAXSTR)) {
				try {
					clazz = findSystemClass(className);
					return clazz;
				} catch (ClassNotFoundException ex) {
					// Some libraries included with a plugin may have a java or javax class (such as JavaMail). Because of this, a
					// plugin must be allowed to look for these classes if not found in the system loader.
					logger.log(LoggerLevel.FINE, "Unable to find class in system loader: " + className, ex);
				}
			}

			// If the class is any part of the engine, immediately delegate to the parent loader.
			if (className.startsWith(PLUGINENGINESTR)) {
				clazz = PluginClassLoader.class.getClassLoader().loadClass(className);
				return clazz;
			}

			// Attempt to find class within cache or classpath.
			try {
				clazz = loadClassFromClassPath(className);
				logger.log(LoggerLevel.FINE, "Found class in Plugin \"" + getPlugin() + "\": " + className, null);
				return clazz;
			} catch (ClassNotFoundException ex) {
				logger.log(LoggerLevel.FINE, "Unable to find class in Plugin \"" + getPlugin() + "\": " + className, ex);
			}

			// The class is not yet found. Look in dependencies.
			for (Dependency dependency:plugin.getDependencies()) {
				if (!dependency.isResolved()) continue; // Dependency is unresolved.
				clazz = dependency.getResolvedToPlugin().pluginClassloader.loadClassFromDependentLoader(className);
				if (clazz != null) return clazz;
			}

			if (plugin.getDependentPluginLookup()) {
				// Look in dependent plugins.
				for (Plugin dependentPlugin:plugin.getDependentPlugins()) {
					clazz = dependentPlugin.pluginClassloader.loadClassFromDependentLoader(className);
					if (clazz != null) return clazz;
				}
			}

			// The class was not found in the cache, classpath or dependent loaders so now check the parent loader. This is
			// not wrapped in a try/catch block because if it isn't found in any parent loaders, a ClassNotFoundException should
			// be thrown.
			clazz = Class.forName(className, true, getParent());
			logger.log(LoggerLevel.FINE, "Found class in parent loader: " + className, null);
			return clazz;
		} finally {
			if (resolve) resolveClass(clazz);
		}
	}

	/**
	 * Called by the loadClass method to find a class within a plugin's classpath.
	 */
	Class<?> loadClassFromClassPath (String className) throws ClassNotFoundException {
		Class<?> clazz = super.findLoadedClass(className);

		if (clazz == null) {
			clazz = findClass(className);
		}

		if (clazz != null) {
			// Make class access start the plugin.
			if (!plugin.start()) {
				throw new ClassNotFoundException("Unable to start Plugin \"" + plugin + "\". Class cannot be acquired: "
					+ className);
			}
		} else
			throw new ClassNotFoundException(className);

		return clazz;
	}

	/**
	 * Called by the loadClass method when it is looking to find a class in a dependent loader. This uses loadClassFromClassPath,
	 * but first makes sure that any classname that has a .pluginprivate in it is NOT returned as it is private to the plugin it
	 * originates from.
	 */
	private Class<?> loadClassFromDependentLoader (String className) {
		synchronized (plugin) {}

		// Do not allow any .pluginprivate classes to be returned
		if (className.indexOf(PRIVATE) > 0) {
			String message = "Blocked dependent loader access to package \"" + PRIVATE + "\" in plugin \"" + plugin
				+ "\" for: " + className;
			logger.log(LoggerLevel.FINE, message, null);
			return null;
		}

		try {
			Class<?> clazz = loadClassFromClassPath(className);
			logger.log(LoggerLevel.FINE, "Found class in dependent Plugin \"" + plugin + "\": " + className, null);
			return clazz;
		} catch (ClassNotFoundException ex) {
			// Ignore exception, keep on looping to try all delegate loaders.
			logger.log(LoggerLevel.FINE, "Unable to find class in dependent Plugin \"" + plugin + "\": " + className, ex);
			return null;
		}
	}

	/**
	 * Overrides findClass to handle Plugin archive file lookup.
	 */
	public Class<?> findClass (final String className) throws ClassNotFoundException {
		// First try each ExtensionClassLoader.
		for (Extension extension:plugin.getExtensions()) {
			if (extension.extensionClassLoader.isExtensionClass(className)) {
				Class<?> clazz = extension.extensionClassLoader.loadExtensionClass(className);
				if (clazz == null) throw new ClassNotFoundException(className);
				return clazz;
			}
		}

		try {
			return super.findClass(className);
		} catch (ClassNotFoundException ex) {
			if (!plugin.isArchive()) throw ex;
		}

		if (packagesMap.isEmpty()) buildPackagesMap();

		try {
			return AccessController.doPrivileged(new PrivilegedExceptionAction<Class<?>>() {
				public Class<?> run () throws ClassNotFoundException {
					String classNamePath = className.replace('.', '/');
					int index = classNamePath.lastIndexOf('/');

					if (index == -1) throw new ClassNotFoundException(className);

					String packageName = classNamePath.substring(0, index);

					if (!packagesMap.containsKey(packageName)) throw new ClassNotFoundException(className);

					String libName = (String)packagesMap.get(packageName);
					try {
						ZipFile parFile = new ZipFile(getURLs()[0].getFile());
						ZipInputStream libInput = new ZipInputStream(parFile.getInputStream(parFile.getEntry(libName)));
						ZipEntry entry;
						String fileName = classNamePath + CLASSSTR;
						while ((entry = libInput.getNextEntry()) != null) {
							if (fileName.equals(entry.getName())) {
								int size = (int)entry.getSize();
								if (size == -1) {
									parFile = new ZipFile(plugin.getPluginURL().getFile());
									InputStream input = parFile.getInputStream(parFile.getEntry(libName));
									ZipScanner scanner = new ZipScanner(input);

									while ((entry = scanner.getNextEntry()) != null) {
										if (fileName.equals(entry.getName())) {
											size = (int)entry.getSize();
											break;
										}
									}

									input.close();
								}

								byte[] data = new byte[size];
								int off = 0, len = data.length, read;
								while ((read = libInput.read(data, off, len)) > 0) {
									off += read;
									len -= read;
								}

								libInput.close();
								parFile.close();

								logger.log(LoggerLevel.FINE, "Found class in Plugin \"" + getPlugin() + "\" in embedded library \""
									+ libName + "\": " + className, null);

								return defineClass(className, data, 0, data.length);
							}
						}
					} catch (IOException ex) {
						logger.log(LoggerLevel.SEVERE, "Error parsing Plugin archive.", ex);
					}
					throw new ClassNotFoundException(className);
				}
			});
		} catch (PrivilegedActionException ex) {
			throw (ClassNotFoundException)ex.getException();
		}
	}

	/**
	 * Gets the first resource with the given name or null if it is not found. Looks first in the URLClassPath of the Plugin, if
	 * the resource is not found then if there is a parent the parent URLClassPath is checked, else the bootstrap URLClassPath is
	 * used.
	 * @return The URL of the resource or null if the resource is not found.
	 */
	public URL getResource (final String name) {
		URL url = findResource(name);

		if (url == null && plugin.isArchive()) {
			url = AccessController.doPrivileged(new PrivilegedAction<URL>() {
				public URL run () {
					return ucp.getParResource(name);
				}
			}, acc);
		}

		if (url == null) {
			if (getParent() != null) {
				url = getParent().getResource(name);
			} else {
				url = ClassLoader.getSystemResource(name);
			}
		}

		if (url != null) {
			// Make resource access start the plugin.
			if (!plugin.start()) {
				url = null;
				logger.log(LoggerLevel.WARNING, "Unable to start Plugin \"" + plugin + "\". Resource cannot be acquired: "
					+ name, null);
			}
		}

		return url;
	}

	/**
	 * Returns the path to a native library at the root of a Plugin archive or Plugin directory.
	 */
	protected String findLibrary (String libName) {
		libName = System.mapLibraryName(libName);
		try {
			String path = plugin.getExtractedResourcePath(libName);
			logger.log(LoggerLevel.FINE, "Found library in Plugin \"" + getPlugin() + "\": " + libName+" path: "+path, null);
			
			return path;
		} catch (IOException ex) {
			logger.log(LoggerLevel.WARNING, "Unable to find library in Plugin \"" + getPlugin() + "\": " + libName, ex);
			return super.findLibrary(libName);
		}
	}

	/**
	 * Index the embeded jar/zip files in the plugin archive in a Map. As key the package name is used and the value is the library
	 * filename that contains the package. This method is only called for Plugin archives.
	 */
	private synchronized void buildPackagesMap () {
		try {
			ZipFile zipFile = new ZipFile(plugin.getPluginURL().getFile());
			Enumeration<? extends ZipEntry> entries = zipFile.entries();
			while (entries.hasMoreElements()) {
				ZipEntry parEntry = entries.nextElement();
				String parEntryName = parEntry.getName().toLowerCase();

				if (parEntryName.indexOf('/') == -1 && parEntryName.endsWith(JARSTR) || parEntryName.endsWith(ZIPSTR)) {
					String libName = parEntry.getName();
					ZipInputStream input = new ZipInputStream(zipFile.getInputStream(parEntry));
					ZipEntry embededEntry;

					while ((embededEntry = input.getNextEntry()) != null) {
						String name = embededEntry.getName();
						int index = name.lastIndexOf('/');

						if (index != -1 && index < name.length() - 1) {
							name = name.substring(0, index);
						}

						if (!packagesMap.containsKey(name)) {
							packagesMap.put(name, libName);
						}
					}
				}
			}
		} catch (IOException ex) {
			logger.log(LoggerLevel.SEVERE, "Error parsing Plugin archive: " + plugin.getPluginURL(), ex);
		}
	}

	/**
	 * Returns the Plugin that this PluginClassLoader loads classes for.
	 * @see PluginEngine#getPlugin(Class)
	 */
	Plugin getPlugin () {
		return plugin;
	}

	public String toString () {
		return "PluginClassLoader(" + plugin + ")";
	}

	// --------------------------------------------------------------------

	/**
	 * When the method java.net.URL.openConnection or java.net.URL.openStream is called the URL instance will use its handler to
	 * open the connection or stream. In the case of URLs that refers to a resource that is in an embededed jar/zip file and the
	 * resource is non class file this handler (EmbededURLStreamHandler) is used.
	 */
	private class EmbededURLStreamHandler extends URLStreamHandler {
		/**
		 * Because an instance of this handler is used by one URL the data for the resource were the URL refers to is passed to the
		 * constructor and stored in a private variable, so when openConnection is called on this class the data can be passed to
		 * the EmbededURLConnection (see EmbededURLConnection.getInputStream for more details).
		 */
		private byte[] data;

		public EmbededURLStreamHandler (byte[] data) {
			this.data = data;
		}

		protected URLConnection openConnection (URL url) throws IOException {
			return new EmbededURLConnection(url, data);
		}
	}

	/**
	 * Instances of this class are used to get resouces that are in an embededed jar/zip file and the resource is a non-class file.
	 */
	private class EmbededURLConnection extends JarURLConnection {
		/*
		 * Used to store the data of the resource for the URL that is passed to the constructor of this class.
		 */
		private byte[] data;

		private String contentType;

		private FilePermission permission;

		private static final String READ = "read";

		private static final String FILE_PROT = "file:";

		public EmbededURLConnection (URL url, byte[] data) throws MalformedURLException, IOException {
			super(url);
			this.data = data;
		}

		public Object getContent () throws IOException {
			throw new UnsupportedOperationException("getContent() is unsupported.");
		}

		public int getContentLength () {
			return data.length;
		}

		public String getContentType () {
			if (null == contentType) {
				try {
					contentType = guessContentTypeFromStream(new ByteArrayInputStream(data));
				} catch (IOException ex) {
					logger.log(LoggerLevel.SEVERE, "Error calling guessContentTypeFromStream.", ex);
				}

				if (null == contentType) {
					String file = url.getFile();
					contentType = guessContentTypeFromName(file.substring(file.lastIndexOf("!/") + 2));
				}

				if (null == contentType) {
					contentType = "content/unknown";
				}
			}
			return contentType;
		}

		public String getHeaderField (String arg0) {
			throw new UnsupportedOperationException("getHeaderField(String arg0) is unsupported.");
		}

		public InputStream getInputStream () throws IOException {
			return new ByteArrayInputStream(data);
		}

		/**
		 * Throws an UnsupportedOperationException becourse a JarFile can't be a file that is embeded in another file like pluginn
		 * archives.
		 */
		public JarFile getJarFile () throws IOException {
			throw new UnsupportedOperationException("getJarFile() is unsupported.");
		}

		public Permission getPermission () throws IOException {
			if (null == permission) {
				String file = getURL().getFile();
				int a = file.indexOf(FILE_PROT);
				int index = file.indexOf('!');
				file = file.substring(a + FILE_PROT.length(), index);
				permission = new FilePermission(file, READ);
			}

			return permission;
		}

		public void connect () throws IOException {
		}
	}

	private class PluginArchiveURLClassPath {
		private URL[] urls;

		public PluginArchiveURLClassPath (URL[] urls) {
			this.urls = urls;
		}

		public URL[] getURLs () {
			return urls;
		}

		@SuppressWarnings("unused")
		public void addURL(URL url) {
			URL[] temp = urls;
			urls = new URL[temp.length + 1];
			System.arraycopy(temp, 0, urls, 0, temp.length);
			urls[temp.length] = url;
		}

		public URL findResource (String name, boolean check) {
			URL url = getParResource(name);
			return null != url ? url : null;
		}

		@SuppressWarnings("unused")
		public Enumeration<URL> findResources (final String name, final boolean check) {
			return new Enumeration<URL>() {
				URL url = findResource(name, check);

				private boolean hasMore = null != url;

				public boolean hasMoreElements () {
					return hasMore;
				}

				public URL nextElement () {
					if (hasMore) {
						hasMore = false;
						return url;
					}
					return null;
				}
			};
		}

		/**
		 * Used internally by this URLClassPath instance to find a resource inside of a plugin archive file.
		 * @param name The name of the resource within the Plugin archive file to find.
		 * @return The URL of the resource or null if the resource doens't exist.
		 */
		private URL getParResource (String name) {
			if (packagesMap.isEmpty()) buildPackagesMap();
			int index = name.lastIndexOf('/');
			String packageName = null;
			if (index > 0) {
				packageName = name.substring(0, index);
			} else {
				packageName = name;
			}

			if (packagesMap.containsKey(packageName)) {
				String libName = (String)packagesMap.get(packageName);

				try {
					ZipFile parFile = new ZipFile(getURLs()[0].getFile());
					InputStream input = parFile.getInputStream(parFile.getEntry(libName));
					ZipInputStream libInput = new ZipInputStream(input);

					ZipEntry entry;

					while ((entry = libInput.getNextEntry()) != null) {
						if (name.equals(entry.getName())) {
							int size = (int)entry.getSize();

							if (size == -1) {
								parFile = new ZipFile(plugin.getPluginURL().getFile());
								input = parFile.getInputStream(parFile.getEntry(libName));
								ZipScanner scanner = new ZipScanner(input);

								while ((entry = scanner.getNextEntry()) != null) {
									if (name.equals(entry.getName())) {
										size = (int)entry.getSize();
										break;
									}
								}

								input.close();
							}

							byte[] data = new byte[size];
							int off = 0, len = data.length, read;

							while ((read = libInput.read(data, off, len)) > 0) {
								off += read;
								len -= read;
							}

							StringBuffer buffer = new StringBuffer(getURLs()[0].toString());
							buffer.append(ENTRY_SEP);
							buffer.append(libName);
							buffer.append(ENTRY_SEP);
							buffer.append(name);

							return new URL("jar", "", -1, buffer.toString(), new EmbededURLStreamHandler(data));
						}
					}
				} catch (Exception ex) {
					logger.log(LoggerLevel.SEVERE, "Error parsing Plugin archive: " + plugin.getPluginURL(), ex);
				}
				return null;
			} else {
				return null;
			}
		}
	}

	/**
	 * A very limited implementation of ZipInputStream. The purpose is to get the size and compressed size of the entries. Only
	 * tested with embeded jar file. Tested with the folowing compression methods: Max portable (enhanged deflate untested),
	 * Normal, Fast, Super fast, None.
	 */
	private static class ZipScanner {
		// Contants from java.util.zip.ZipContants
		private static final int LOCHDR = 30; // LOC header size

		private static final int EXTHDR = 16; // EXT header size

		private static final long LOCSIG = 0x04034b50L; // "PK\003\004"

		private static final long EXTSIG = 0x08074b50L; // "PK\007\008"

		private static final int LOCFLG = 6; // general purpose bit flag

		private static final int LOCHOW = 8; // compression method

		private static final int LOCSIZ = 18; // compressed size

		private static final int LOCLEN = 22; // uncompressed size

		private static final int LOCNAM = 26; // filename length

		private static final int EXTSIZ = 8; // compressed size

		private static final int EXTLEN = 12; // uncompressed size

		private final InputStream in;

		private ZipEntry entry;

		private byte[] loc = new byte[LOCHDR];

//		private static int count = 0;

		public ZipScanner (InputStream in) {
			this.in = new BufferedInputStream(in, 512);
		}

		public ZipEntry getNextEntry () throws IOException {
			int off = 0, len = LOCHDR, read;

			// Read the local file header
			while ((read = in.read(loc, off, len)) > 0) {
				off += read;
				len -= read;
			}
			if (get32(loc, 0) != LOCSIG) {
				// invalid local file header or end of zip file.
				return null;
			}

			// Read the file name
			byte[] name = new byte[loc[LOCNAM]];
			off = 0;
			len = loc[LOCNAM];
			while ((read = in.read(name, off, len)) > 0) {
				off += read;
				len -= read;
			}

			// Get the general purpose bit flag and compression method.
			int flag = get16(loc, LOCFLG);
			int method = get16(loc, LOCHOW);
			if (method == ZipEntry.DEFLATED && (flag & 8) == 8) {
				// Deflated entry with EXT header.

				byte extheader[] = new byte[EXTHDR];

				// Try to find EXT header
				while (in.available() > 0) {
					extheader[0] = extheader[1];
					extheader[1] = extheader[2];
					extheader[2] = extheader[3];
					extheader[3] = (byte)in.read();

					// Possible start of EXT header found.
					if (extheader[0] == 'P') {
						// EXT header signitures found
						if (get32(extheader, 0) == EXTSIG) {
							off = 4;
							len = 12;
							// Read the remaining part of EXT header
							while ((read = in.read(extheader, off, len)) > 0) {
								off += read;
								len -= read;
							}
							// Create the entry and set this size and compressed
							// size.
							entry = new ZipEntry(new String(name));
							entry.setMethod(method);
							entry.setCrc(flag);
							entry.setSize(get32(extheader, EXTLEN));
							entry.setCompressedSize(get32(extheader, EXTSIZ));
							return entry;
						}
					}
				}
			} else {
				// Entry is stored or deflated with no EXT header.
				// Create the entry and set this size and compressed size.
				entry = new ZipEntry(new String(name));
				entry.setMethod(method);
				entry.setSize(get32(loc, LOCLEN));
				long cSize = get32(loc, LOCSIZ);
				entry.setCompressedSize(cSize);

				// if compressed size is bigger than 0 skip data.
				if (cSize > 0) {
					byte data[] = new byte[(int)cSize];
					off = 0;
					len = (int)cSize;
					while ((read = in.read(data, off, len)) > 0) {
						off += read;
						len -= read;
					}
				}
				return entry;
			}
			return null;
		}

		// Method from ZipInputSream (for more info see
		// java.util.zip.ZipInputSream).
		private static final int get16 (byte b[], int off) {
			return (b[off] & 0xff) | ((b[off + 1] & 0xff) << 8);
		}

		// Method from ZipInputSream (for more info see
		// java.util.zip.ZipInputSream).
		private static final long get32 (byte b[], int off) {
			return get16(b, off) | ((long)get16(b, off + 2) << 16);
		}
	}

}
