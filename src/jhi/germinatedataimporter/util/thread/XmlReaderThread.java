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
import org.eclipse.swt.widgets.*;
import org.simpleframework.xml.*;
import org.simpleframework.xml.core.*;

import java.io.*;

import jhi.germinatedataimporter.gui.i18n.*;
import jhi.germinatedataimporter.gui.widget.*;
import jhi.germinatedataimporter.util.xml.*;
import jhi.swtcommons.util.*;

/**
 * {@link XmlReaderThread} implements {@link IRunnableWithProgress}. It is used to read a given .xml {@link File} and parse each item into a {@link
 * jhi.germinatedataimporter.util.xml.InputMapping}
 *
 * @author Sebastian Raubach
 */
public class XmlReaderThread implements IRunnableWithProgress
{
	private File         file;
	private InputMapping mapping;

	private ColumnMapper mapper;

	public XmlReaderThread(File file, ColumnMapper mapper)
	{
		this.file = file;
		this.mapper = mapper;
	}

	@Override
	public void run(IProgressMonitor monitor)
	{
		/* Ensure there is a monitor of some sort */
		if (monitor == null)
			monitor = new NullProgressMonitor();

        /* Tell the user what you are doing */
		monitor.beginTask(RB.getString(RB.THREAD_IMPORT_MAPPING_TITLE), IProgressMonitor.UNKNOWN);

		Serializer serializer = new Persister();

		try
		{
			mapping = serializer.read(InputMapping.class, file);

			Display.getDefault().asyncExec(() -> {
				boolean failed = mapper.setInputMapping(mapping);

				if (failed)
					DialogUtils.showError(RB.getString(RB.ERROR_IMPORT_XML_INVALID_MAPPING));
			});
		}
		catch (Exception e)
		{
			DialogUtils.handleException(e);
		}
	}

	public InputMapping getMapping()
	{
		return mapping;
	}
}
