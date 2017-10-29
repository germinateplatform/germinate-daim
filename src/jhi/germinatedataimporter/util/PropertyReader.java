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

package jhi.germinatedataimporter.util;

import java.io.*;
import java.nio.file.*;
import java.util.*;

import jhi.germinatedataimporter.database.entities.*;
import jhi.germinatedataimporter.gui.*;
import jhi.swtcommons.util.*;

/**
 * {@link PropertyReader} is a wrapper around {@link Properties} to read properties.
 *
 * @author Sebastian Raubach
 */
public class PropertyReader extends jhi.swtcommons.util.PropertyReader
{
	public static final  String PROPERTIES_FOLDER_NEW       = "jhi-ics";
	private static final String INPUT_FILE_PATH             = "input.file.path";
	private static final String INPUT_FILE_SEPARATOR        = "input.file.separator";
	private static final String INPUT_FILE_NUMBER_FORMAT    = "input.file.locale";
	private static final String INPUT_FILE_ALWAYS_ASK       = "input.file.always.ask";
	private static final String INPUT_FILE_REMOVE_SPACES    = "input.file.remove.spaces";
	private static final String DATABASE_SERVER             = "database.server";
	private static final String DATABASE_DATABASE           = "database.database";
	private static final String DATABASE_PORT               = "database.port";
	private static final String PREFERENCES_LOCALE          = "preferences.locale";
	private static final String PREFERENCES_UPDATE_INTERVAL = "preferences.update.interval";
	private static final String INTERNAL_USER_ID            = "internal.user.id";
	/** The name of the properties file (slash necessary for MacOS X) */
	private static final String PROPERTIES_FILE             = "/g3di.properties";
	private static final String PROPERTIES_FOLDER_OLD       = "scri-bioinf";
	private static File localFile;

	public PropertyReader()
	{
		super(PROPERTIES_FILE);
	}

	/**
	 * Loads the properties {@link File}. It will try to load the local file first (in home directory). If this file doesn't exist, it will fall back
	 * to the default within the jar (or the local file in the project during development).
	 *
	 * @throws IOException Thrown if the file interaction fails
	 */
	public void load() throws IOException
	{
		/* Move old file to new location */
		File oldFile = new File(new File(System.getProperty("user.home"), "." + PROPERTIES_FOLDER_OLD), PROPERTIES_FILE);
		File newFile = new File(new File(System.getProperty("user.home"), "." + PROPERTIES_FOLDER_NEW), PROPERTIES_FILE);

		/* If there's a new file AND an old one, just delete the old one */
		if (newFile.exists() && oldFile.exists())
		{
			oldFile.delete();
		}
		/* If there's no new one, but an old one, copy it across, then delete the old one */
		else if (!newFile.exists() && oldFile.exists())
		{
			newFile.getParentFile().mkdirs();
			Files.copy(oldFile.toPath(), newFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
			oldFile.delete();
		}


		localFile = newFile;

		InputStream stream = null;

		try
		{
			if (localFile.exists())
			{
				stream = new FileInputStream(localFile);
			}
			else
			{
				if (GerminateDataImporter.WITHIN_JAR)
					stream = PropertyReader.class.getResourceAsStream(PROPERTIES_FILE);
				else
					stream = new FileInputStream(new File("res", PROPERTIES_FILE));
			}

			properties.load(stream);
		}
		finally
		{
			if (stream != null)
			{
				try
				{
					stream.close();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}

		GerminateParameterStore store = GerminateParameterStore.getInstance();

		store.put(GerminateParameter.inputfile, getProperty(INPUT_FILE_PATH));

        /* Get the FileSeparator and fall back on TAB if necessary */
		String separatorString = getProperty(INPUT_FILE_SEPARATOR);
		if (StringUtils.isEmpty(separatorString))
			store.put(GerminateParameter.inputseparator, FileSeparator.TAB);
		else
			store.put(GerminateParameter.inputseparator, FileSeparator.valueOf(separatorString));

        /* Get the Locale and fall back on ENGLISH if necessary */
		String localeString = getProperty(INPUT_FILE_NUMBER_FORMAT);
		if (StringUtils.isEmpty(localeString))
			store.put(GerminateParameter.inputlocale, Locale.ENGLISH);
		else
			store.put(GerminateParameter.inputlocale, new Locale(localeString));

		/* Get the update interval and fall back on STARTUP if necessary */
		String updateIntervalString = getProperty(PREFERENCES_UPDATE_INTERVAL);
		Install4jUtils.UpdateInterval updateInterval;
		try
		{
			updateInterval = Install4jUtils.UpdateInterval.valueOf(updateIntervalString);
		}
		catch (Exception e)
		{
			updateInterval = Install4jUtils.UpdateInterval.STARTUP;
		}
		store.put(GerminateParameter.updateInterval, updateInterval);

        /* Get the Locale of the GUI fall back on ENGLISH if necessary */
		String localeGUIString = getProperty(PREFERENCES_LOCALE);
		Locale locale;
		if (StringUtils.isEmpty(localeGUIString))
			locale = Locale.ENGLISH;
		else
			locale = new Locale(localeGUIString);
		store.put(GerminateParameter.locale, locale);
		Locale.setDefault(locale);

        /* Get the value for always-ask (after opening a file) */
		String alwaysAsk = getProperty(INPUT_FILE_ALWAYS_ASK);
		if (StringUtils.isEmpty(alwaysAsk))
			store.put(GerminateParameter.inputalwaysask, true);
		else
			store.put(GerminateParameter.inputalwaysask, Boolean.parseBoolean(alwaysAsk));

		/* Get the value for removing spaces (during data import) */
		String removeSpaces = getProperty(INPUT_FILE_REMOVE_SPACES);
		if (StringUtils.isEmpty(removeSpaces))
			store.put(GerminateParameter.removetrailingspaces, true);
		else
			store.put(GerminateParameter.removetrailingspaces, Boolean.parseBoolean(removeSpaces));

        /* Get previously used database servers */
		List<String> servers = getPropertyListAsString(DATABASE_SERVER, ",");
		store.put(GerminateParameter.servers, servers);
		if (!CollectionUtils.isEmpty(servers))
			store.put(GerminateParameter.server, servers.get(servers.size() - 1));

        /* Get previously used databases */
		List<String> databases = getPropertyListAsString(DATABASE_DATABASE, ",");
		store.put(GerminateParameter.databases, databases);
		if (!CollectionUtils.isEmpty(databases))
			store.put(GerminateParameter.database, new DatabaseDatabase(databases.get(databases.size() - 1)));

		store.put(GerminateParameter.port, getProperty(DATABASE_PORT));

//		store.put(GerminateParameter.updateData, false);

		/* Get the user id */
		store.put(GerminateParameter.userId, getProperty(INTERNAL_USER_ID, SystemUtils.createGUID(32)));
	}

	/**
	 * Stores the {@link Parameter}s from the {@link ParameterStore} to the {@link Properties} object and then saves it using {@link
	 * Properties#store(Writer, String)}.
	 *
	 * @throws IOException Thrown if the file interaction fails
	 */
	public void store() throws IOException
	{
		if (localFile == null)
			return;

		GerminateParameterStore store = GerminateParameterStore.getInstance();

		set(INPUT_FILE_PATH, store.getAsString(GerminateParameter.inputfile));
		set(INPUT_FILE_SEPARATOR, store.getAsString(GerminateParameter.inputseparator));
		set(INPUT_FILE_NUMBER_FORMAT, store.getAsString(GerminateParameter.inputlocale));
		set(INPUT_FILE_ALWAYS_ASK, store.getAsString(GerminateParameter.inputalwaysask));
		set(INPUT_FILE_REMOVE_SPACES, store.getAsString(GerminateParameter.removetrailingspaces));
		set(PREFERENCES_LOCALE, store.getAsString(GerminateParameter.locale));
		set(DATABASE_SERVER, store.getAsString(GerminateParameter.servers));
		set(DATABASE_DATABASE, store.getAsString(GerminateParameter.databases));
		set(DATABASE_PORT, store.getAsString(GerminateParameter.port));
		set(PREFERENCES_UPDATE_INTERVAL, store.getAsString(GerminateParameter.updateInterval));
		set(INTERNAL_USER_ID, store.getAsString(GerminateParameter.userId));

		localFile.getParentFile().mkdirs();
		localFile.createNewFile();
		properties.store(new FileOutputStream(localFile), null);
	}
}
