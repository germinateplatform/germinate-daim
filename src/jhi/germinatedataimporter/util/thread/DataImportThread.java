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

import org.eclipse.jface.operation.*;

import java.io.*;
import java.text.*;
import java.util.*;

import jhi.germinatedataimporter.database.entities.*;
import jhi.germinatedataimporter.gui.widget.ColumnMapper.*;
import jhi.swtcommons.util.*;

/**
 * {@link DataImportThread} implements {@link IRunnableWithProgress} and is a thread that imports the data into the database.
 *
 * @author Sebastian Raubach
 */
public abstract class DataImportThread implements IRunnableWithProgress
{
	protected InputOptions  options;
	protected DatabaseTable table;

	protected Map<Class<?>, Boolean> exceptionsToIgnore = new HashMap<>();

	/**
	 * Creates a new instance of {@link DataImportThread}
	 *
	 * @param options The {@link InputOptions}s
	 * @param table   The {@link DatabaseTable} to import the data to
	 */
	public DataImportThread(InputOptions options, DatabaseTable table)
	{
		this.options = options;
		this.table = table;
	}

	/**
	 * @param e The Exception
	 * @return
	 */
	public abstract Tuple.Pair<Boolean, Boolean> onImportError(Exception e, boolean checkedState);

	/**
	 * Called when any of the following {@link Exception}s occurs during the import process: {@link IOException}, {@link ParseException}, {@link
	 * jhi.database.shared.exception.DatabaseException}, {@link NumberFormatException}.
	 *
	 * @param generatedIds The ids that have been generated during the import process
	 * @param updatedIds   The number of items that have been updated
	 * @param e            The {@link Exception}
	 */
	public abstract void onImportFailed(List<Long> generatedIds, int updatedIds, Exception e);

	/**
	 * Called when the import process was cancelled by the user.
	 *
	 * @param generatedIds The ids that have been generated during the import process
	 * @param updatedIds   The number of items that have been updated
	 */
	public abstract void onImportCancelled(List<Long> generatedIds, int updatedIds);

	/**
	 * Called when the import process has finished with the generated ids of imported data items.
	 *
	 * @param generatedIds The ids that have been generated during the import process
	 * @param updatedIds   The number of items that have been updated
	 */
	public abstract void onImportFinished(List<Long> generatedIds, int updatedIds);
}
