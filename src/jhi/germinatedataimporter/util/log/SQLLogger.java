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

package jhi.germinatedataimporter.util.log;

import java.io.*;
import java.util.logging.*;

/**
 * {@link SQLLogger} is a utility class containing methods for error logging.
 *
 * @author Sebastian Raubach
 */
public class SQLLogger extends FileLogger
{
	private static final String LOG_NAME = "g3di-sql";

	private static SQLLogger INSTANCE;

	private SQLLogger() throws IOException
	{
		super();
		INSTANCE = this;
	}

	public static synchronized SQLLogger getInstance()
	{
		if (INSTANCE == null)
		{
			try
			{
				INSTANCE = new SQLLogger();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}

		return INSTANCE;
	}

	@Override
	protected String getLogFileName()
	{
		return LOG_NAME;
	}

	@Override
	protected String getLoggerName()
	{
		return SQLLogger.class.getName();
	}

	@Override
	protected Level getLevel()
	{
		return Level.INFO;
	}

	@Override
	protected void deleteOldLogFiles()
	{
		File[] files = getFilesMatching("^" + LOG_NAME + ".*\\.log");

		if (files != null)
		{
			for (File file : files)
				file.delete();
		}
	}
}
