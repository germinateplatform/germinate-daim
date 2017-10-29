/**
 * Copyright 2017 Information and Computational Sciences,
 * The James Hutton Institute.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jhi.germinatedataimporter.util.thread;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.operation.*;
import org.simpleframework.xml.*;
import org.simpleframework.xml.core.*;

import java.io.*;
import java.lang.reflect.*;

import jhi.germinatedataimporter.gui.i18n.*;
import jhi.germinatedataimporter.util.xml.*;
import jhi.swtcommons.util.*;


/**
 * {@link XmlWriterThread} implements {@link IRunnableWithProgress}. It is used to export the {@link jhi.germinatedataimporter.util.xml.InputMapping}
 * to a File
 *
 * @author Sebastian Raubach
 */
public class XmlWriterThread implements IRunnableWithProgress
{
	private InputMapping mapping;
	private File         file;

	/**
	 * Writes the given {@link InputMapping}s to the {@link File}
	 *
	 * @param file    The {@link File} to write the barcodes to
	 * @param mapping The {@link InputMapping} to export
	 */
	public XmlWriterThread(File file, InputMapping mapping)
	{
		this.mapping = mapping;
		this.file = file;
	}

	@Override
	public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
	{
		/* Ensure there is a monitor of some sort */
		if (monitor == null)
			monitor = new NullProgressMonitor();

        /* Tell the user what you are doing */
		monitor.beginTask(RB.getString(RB.THREAD_EXPORT_MAPPING_TITLE), IProgressMonitor.UNKNOWN);

		Serializer serializer = new Persister();

		try
		{
			serializer.write(mapping, file);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			DialogUtils.handleException(e);
		}

		DialogUtils.showInformation(RB.getString(RB.INFORMATION_SAVE, file.getAbsolutePath()));
	}
}
