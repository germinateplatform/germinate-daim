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

package jhi.germinatedataimporter.util.log;

import java.io.*;
import java.util.logging.*;

/**
 * {@link ErrorLogger} is a utility class containing methods for error logging.
 *
 * @author Sebastian Raubach
 */
public class ErrorLogger extends FileLogger
{
	private static final String LOG_NAME = "g3di-internal-error";

	private static ErrorLogger INSTANCE;

	private ErrorLogger() throws IOException
	{
		super();
		INSTANCE = this;
	}

	public static synchronized ErrorLogger getInstance()
	{
		if (INSTANCE == null)
		{
			try
			{
				INSTANCE = new ErrorLogger();
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
		return ErrorLogger.class.getName();
	}

	@Override
	protected Level getLevel()
	{
		return Level.WARNING;
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
