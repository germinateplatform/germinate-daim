/**
 * Copyright 2017 Sebastian Raubach and Paul Shaw from the Information and Computational Sciences Group at JHI Dundee
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations
 * under the License.
 */

package jhi.germinatedataimporter.util;

import java.io.*;
import java.util.*;

import jhi.germinatedataimporter.gui.*;
import jhi.germinatedataimporter.gui.widget.ColumnMapper.*;
import jhi.swtcommons.util.*;

/**
 * {@link FileUtils} contains methods to read/write to {@link File}s.
 *
 * @author Sebastian Raubach
 */
public class FileUtils
{
	private static final String LICENSE_FILE = "LICENCE.txt";

	/**
	 * Reads the headers from the given {@link File} (from the first line)
	 *
	 * @param options The {@link InputOptions}
	 * @return The distinct file headers
	 * @throws IOException Thrown if the file interaction fails
	 */
	public static Set<String> readHeaders(InputOptions options) throws IOException
	{
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(options.file), "UTF8"));

		Set<String> result = new LinkedHashSet<>();

		String line = br.readLine();

		if (!StringUtils.isEmpty(line))
		{
			String[] parts = line.split(options.separator.getSeparator());

			for (String part : parts)
			{
				part = part.trim();
				if (!StringUtils.isEmpty(part))
					result.add(part);
			}
		}

		br.close();

		return result;
	}

	public static String readLicense(String path) throws IOException
	{
		StringBuilder builder = new StringBuilder();
		BufferedReader br;
		if (GerminateDataImporter.WITHIN_JAR)
			br = new BufferedReader(new InputStreamReader(FileUtils.class.getResourceAsStream("/" + path), "UTF-8"));
		else
			br = new BufferedReader(new FileReader(path));

		String line;

		while ((line = br.readLine()) != null)
		{
			builder.append(line).append("\n");
		}

		br.close();

		return builder.toString();
	}
}
