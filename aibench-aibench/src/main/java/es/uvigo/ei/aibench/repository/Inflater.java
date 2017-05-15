/*
 * #%L
 * The AIBench basic runtime and plugin engine
 * %%
 * Copyright (C) 2006 - 2017 Daniel Glez-Peña and Florentino Fdez-Riverola
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */
package es.uvigo.ei.aibench.repository;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.ice.tar.TarEntry;
import com.ice.tar.TarInputStream;

/**
 * @author Miguel Reboiro Jato
 *
 */
public class Inflater {
	private static final int BUFFER_SIZE = 8192;

	public static boolean inflate(File source, File output) {
		boolean done = true;
		if (Inflater.inflatableFile(source)) {
			String sourceName = source.getName();
			try {
				if (sourceName.endsWith("jar")) {
					Inflater.copyFile(source, output);
				} else if (sourceName.endsWith("zip")) {
					done = Inflater.inflateZip(source, output);
				} else if (sourceName.endsWith("tar.gz")) {
					done = Inflater.inflateTarGz(source, output);
				} else if (sourceName.endsWith("tar")) {
					done = Inflater.inflateTar(source, output);
				}
			} catch (IOException e) {
				e.printStackTrace();
				done = false;
			}
		} else {
			done = false;
		}
		return done;
	}
	
	public static boolean inflatableFile(File source) {
		return Inflater.inflatableFile(source.getName());
	}
	
	public static boolean inflatableFile(String fileName) {
		return fileName.endsWith(".jar") ||
			fileName.endsWith(".zip") ||
			fileName.endsWith(".tar.gz") ||
			fileName.endsWith(".tar");
	}

	private static void copyFile(File source, File output)
	throws IOException {
		try (FileInputStream fis = new FileInputStream(source);
			FileOutputStream fos = new FileOutputStream(output)
		) {
			final FileChannel sourceChannel = fis.getChannel();
			final FileChannel outputChannel = fos.getChannel();
			
			sourceChannel.transferTo(0, sourceChannel.size(), outputChannel);
		}
	}

	private static boolean inflateZip(File source, File output)
	throws IOException {
		if (output.mkdir()) {
			ZipInputStream zis = null;
			BufferedOutputStream bos = null;
			try {
				zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(source), Inflater.BUFFER_SIZE));
				ZipEntry entry;
				byte[] data = new byte[Inflater.BUFFER_SIZE];
				int len;
				File file;
				while ((entry = zis.getNextEntry()) != null) {
					file = new File(output, entry.getName());
					if (entry.isDirectory()) {
						file.mkdir();
					} else {
						bos = new BufferedOutputStream(new FileOutputStream(file), Inflater.BUFFER_SIZE);
						while ((len = zis.read(data)) != -1) {
							bos.write(data, 0, len);
						}
						bos.flush();
						bos.close();
						bos = null;
					}
				}
				
				zis.close();
			} catch (IOException ioe) {
				throw ioe;
			} finally {
				try {
					if (zis != null) zis.close();
				} catch (IOException ioe) {}
				try {
					if (bos != null) {
						bos.flush();
						bos.close();
					}
				} catch (IOException ioe) {}
			}

			return true;
		} else {
			return false;
		}
	}

	private static boolean inflateTarGz(File source, File output)
	throws IOException {
		if (output.mkdir()) {
			BufferedOutputStream bos = null;
			TarInputStream tis = null;
			try {
				byte[] data = new byte[Inflater.BUFFER_SIZE];
				int len;
				TarEntry entry;
				File file;
				
				tis = new TarInputStream(new GZIPInputStream(new FileInputStream(source), Inflater.BUFFER_SIZE), Inflater.BUFFER_SIZE);
				while ((entry = tis.getNextEntry()) != null) {
					file = new File(output, entry.getName());
					if (entry.isDirectory()) {
						file.mkdir();
					} else {
						bos = new BufferedOutputStream(new FileOutputStream(file));
						while ((len = tis.read(data)) != -1) {
							bos.write(data, 0, len);
						}
						bos.flush();
						bos.close();
						bos = null;
					}
				}
				tis.close();
			} catch (IOException ioe) {
				throw ioe;
			} finally {
				try {
					if (bos != null) {
						bos.flush();
						bos.close();
					}
				} catch (IOException ioe) {}
				try {
					if (tis != null) tis.close();
				} catch (IOException ioe) {}
			}

			return true;
		} else {
			return false;
		}
	}

	private static boolean inflateTar(File source, File output)
	throws IOException {
		if (output.mkdir()) {
			BufferedOutputStream bos = null;
			TarInputStream tis = null;
			try {
				byte[] data = new byte[Inflater.BUFFER_SIZE];
				int len;
				TarEntry entry;
				File file;
				
				tis = new TarInputStream(new FileInputStream(source), Inflater.BUFFER_SIZE);
				while ((entry = tis.getNextEntry()) != null) {
					file = new File(output, entry.getName());
					if (entry.isDirectory()) {
						file.mkdir();
					} else {
						bos = new BufferedOutputStream(new FileOutputStream(file));
						while ((len = tis.read(data)) != -1) {
							bos.write(data, 0, len);
						}
						bos.flush();
						bos.close();
						bos = null;
					}
				}
				tis.close();
			} catch (IOException ioe) {
				throw ioe;
			} finally {
				try {
					if (bos != null) {
						bos.flush();
						bos.close();
					}
				} catch (IOException ioe) {}
				try {
					if (tis != null) tis.close();
				} catch (IOException ioe) {}
			}

			return true;
		} else {
			return false;
		}
	}
}
