/*
 *
 * Copyright (C) 2012-2014 R T Huitema. All Rights Reserved.
 * Web: www.42.co.nz
 * Email: robert@42.co.nz
 * Author: R T Huitema
 *
 * This file is provided AS IS with NO WARRANTY OF ANY KIND, INCLUDING THE
 * WARRANTY OF DESIGN, MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

/** based on http://www.java-examples.com/create-zip-file-directory-recursively-using-zipoutputstream-example */
package nz.co.fortytwo.signalk.server.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

public class ZipUtils {
	private static Logger logger = Logger.getLogger(ZipUtils.class);

	public static void zip(File sourceDir, File zipFile) {

		try {
			// create object of FileOutputStream
			FileOutputStream fout = new FileOutputStream(zipFile);

			// create object of ZipOutputStream from FileOutputStream
			ZipOutputStream zout = new ZipOutputStream(fout);
			addDirectory(zout, sourceDir, sourceDir);

			// close the ZipOutputStream
			zout.close();

			logger.info("Zip file has been created!");

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

	}

	/**
	 * Add the directory recursively into a zip file
	 * @param zout
	 * @param fileSource
	 * @param sourceDir
	 */
	private static void addDirectory(ZipOutputStream zout, File fileSource, File sourceDir) {

		// get sub-folder/files list
		File[] files = fileSource.listFiles();

		logger.debug("Adding directory " + fileSource.getName());

		for (int i = 0; i < files.length; i++) {
			try {
				String name = files[i].getAbsolutePath();
				name = name.substring((int) sourceDir.getAbsolutePath().length());
				// if the file is directory, call the function recursively
				if (files[i].isDirectory()) {
					addDirectory(zout, files[i], sourceDir);
					continue;
				}

				/*
				 * we are here means, its file and not directory, so
				 * add it to the zip file
				 */

				logger.debug("Adding file " + files[i].getName());

				// create object of FileInputStream
				FileInputStream fin = new FileInputStream(files[i]);

				zout.putNextEntry(new ZipEntry(name));

				IOUtils.copy(fin, zout);
				
				zout.closeEntry();

				// close the InputStream
				fin.close();

			} catch (IOException ioe) {
				logger.error(ioe.getMessage(), ioe);
			}
		}

	}

	/**
	 * Unzip a zipFile into a directory
	 * @param targetDir
	 * @param zipFile
	 * @throws ZipException
	 * @throws IOException
	 */
	public static void unzip(File targetDir,File zipFile) throws ZipException, IOException{
		ZipFile zip = new ZipFile(zipFile);
		
		@SuppressWarnings("unchecked")
		Enumeration<ZipEntry> z = (Enumeration<ZipEntry>) zip.entries();
		while(z.hasMoreElements()){
			ZipEntry entry = z.nextElement();
			File f = new File(targetDir, entry.getName());
			if(f.isDirectory()){
				if(!f.exists()){
					f.mkdirs();
				}
			}else{
				f.getParentFile().mkdirs();
				InputStream in = zip.getInputStream(entry);
				BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(f));
				IOUtils.copy(in, out);
				in.close();
				out.flush();
				out.close();
			}
			
		}
		zip.close();
	}
}
