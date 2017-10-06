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
import java.util.regex.*;

import jhi.germinatedataimporter.util.*;

public abstract class FileLogger
{
	protected final String FILE_PATTERN  = "%s.log";
	private final   File   CONFIG_FOLDER = new File(System.getProperty("user.home"), "." + PropertyReader.PROPERTIES_FOLDER_NEW);
	protected Logger      LOGGER;
	protected FileHandler fileHandler;

	protected File file;

	/**
	 * Sets up logging
	 *
	 * @throws IOException Thrown if the log file coulnd't be created
	 */
	protected FileLogger() throws IOException
	{
		deleteOldLogFiles();

		file = new File(CONFIG_FOLDER, String.format(FILE_PATTERN, getLogFileName()));

		if (!file.exists())
			file.createNewFile();

        /* Disable logging to the console */
		Logger rootLogger = Logger.getLogger("");
		Handler[] handlers = rootLogger.getHandlers();

		if (handlers != null)
		{
			for (int i = 0; i < handlers.length; i++)
			{
				if (handlers[i] instanceof ConsoleHandler)
				{
					rootLogger.removeHandler(handlers[i]);
					break;
				}
			}
		}

		LOGGER = Logger.getLogger(getLoggerName());
		LOGGER.setLevel(getLevel());

		try
		{
			fileHandler = new FileHandler(file.getAbsolutePath(), true);
			/* create a TXT formatter */
			fileHandler.setFormatter(new SimpleFormatter());
			LOGGER.addHandler(fileHandler);
		}
		catch (SecurityException | IOException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Logs the given error to the log file
	 *
	 * @param logLevel The log {@link Level}
	 * @param msg      The log message
	 */
	public synchronized void log(Level logLevel, String msg)
	{
		if (LOGGER != null)
			LOGGER.log(logLevel, msg);
	}

	/**
	 * Logs the given {@link Throwable} to the log file
	 *
	 * @param logLevel The log {@link Level}
	 * @param msg      The log message
	 * @param e        The {@link Throwable}
	 */
	public synchronized void log(Level logLevel, String msg, Throwable e)
	{
		if (LOGGER != null)
			LOGGER.log(logLevel, msg, e);
	}

	/**
	 * Logs the given {@link Throwable} to the log file
	 *
	 * @param logLevel The log {@link Level}
	 * @param e        The {@link Throwable}
	 */
	public synchronized void log(Level logLevel, Throwable e)
	{
		log(logLevel, e.toString(), e);
	}

	protected abstract String getLogFileName();

	protected abstract String getLoggerName();

	protected abstract Level getLevel();

	protected abstract void deleteOldLogFiles();

	public String getLogFile()
	{
		return file.getAbsolutePath();
	}

	protected File[] getFilesMatching(String regex)
	{
		final Pattern p = Pattern.compile(regex);

		return CONFIG_FOLDER.listFiles(file1 -> {
			return p.matcher(file1.getName()).matches();
		});
	}
}
