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

package jhi.germinatedataimporter.util.thread;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.bindings.keys.*;
import org.eclipse.jface.operation.*;
import org.eclipse.swt.*;

import java.io.*;
import java.lang.reflect.*;
import java.sql.*;
import java.util.*;
import java.util.logging.*;
import java.util.regex.*;

import jhi.database.server.query.*;
import jhi.database.shared.exception.*;
import jhi.germinatedataimporter.database.entities.*;
import jhi.germinatedataimporter.database.entities.DatabaseColumn.*;
import jhi.germinatedataimporter.gui.*;
import jhi.germinatedataimporter.gui.dialog.*;
import jhi.germinatedataimporter.gui.i18n.*;
import jhi.germinatedataimporter.gui.widget.ColumnMapper.*;
import jhi.germinatedataimporter.gui.widget.MatrixColumnMapperRow.*;
import jhi.germinatedataimporter.util.*;
import jhi.germinatedataimporter.util.exception.*;
import jhi.germinatedataimporter.util.log.*;
import jhi.swtcommons.util.*;

/**
 * {@link MatrixDataImportThread} implements {@link IRunnableWithProgress} and is a thread that imports the data into the database.
 *
 * @author Sebastian Raubach
 */
public abstract class MatrixDataImportThread extends DataImportThread
{
	private List<MatrixColumnMapperRowDTO> mapping;

	/**
	 * Creates a new instance of {@link MatrixDataImportThread}
	 *
	 * @param options The {@link InputOptions}s
	 * @param mapping The mapping of file column name, {@link DatabaseColumn} and {@link Condition} ({@link Condition} can be <code>null</code>)
	 * @param table   The {@link DatabaseTable} to import the data to
	 */
	public MatrixDataImportThread(InputOptions options, List<MatrixColumnMapperRowDTO> mapping, DatabaseTable table)
	{
		super(options, table);
		this.mapping = mapping;
	}

	@Override
	public final void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
	{
		/* Ensure there is a monitor of some sort */
		if (monitor == null)
			monitor = new NullProgressMonitor();

		GerminateDataImporter.getInstance().setTaskBarProgress(SWT.INDETERMINATE, 0);

        /* Tell the user what you are doing */
		monitor.beginTask(RB.getString(RB.DIALOG_IMPORT_PROGRESS_TITLE), IProgressMonitor.UNKNOWN);
		monitor.subTask(RB.getString(RB.DIALOG_IMPORT_PROGRESS_PREPARATION));

		if (mapping == null || mapping.size() < 1)
		{
			monitor.done();
			return;
		}

        /* Build the SQL statement for insertion */
		StringBuilder builder = new StringBuilder("INSERT INTO " + table.getName() + "(");

        /* First, add the database columns into which we want to import data */
		builder.append(mapping.get(0).columnDatabase.getName());
		for (int i = 1; i < mapping.size(); i++)
		{
			builder.append(", " + mapping.get(i).columnDatabase.getName());
		}

		builder.append(") VALUES (");

        /* Now create placeholders or sub-queries or constants */
		for (int i = 0; i < mapping.size(); i++)
		{
			/* If there is a condition, add the sub-query to get the appropriate
			 * id */
			if (mapping.get(i).keyCondition != null)
			{
				Condition cond = mapping.get(i).keyCondition;
				builder.append(String.format(SQLUtils.SELECT_SUBQUERY, cond.getTable().getName(), cond.getColumn().getName()));
			}
			/* Otherwise, just add the content itself */
			else
			{
				builder.append("?");
			}

			if (i < mapping.size() - 1)
				builder.append(", ");
		}

		builder.append(")");

		String theQuery = builder.toString();

		List<Long> generatedIds = new ArrayList<>();
		int updatedIds = 0;

		try
		{
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(options.file), "UTF8"));

            /* Skip the first line (headers) */
			String line = br.readLine();
			String[] headers = line.split(options.separator.getSeparator(), -1);

			long rowCounter = 1l;
			long cellCounter = 1l;

			Boolean trimCells = (Boolean) GerminateParameterStore.getInstance().get(GerminateParameter.removetrailingspaces);

			while ((line = br.readLine()) != null)
			{
				try
				{
					monitor.subTask(RB.getString(RB.DIALOG_IMPORT_PROGRESS_DATUM, rowCounter++, cellCounter));

					String[] parts = line.split(options.separator.getSeparator(), -1);

					if (trimCells != null && trimCells)
					{
						for (int i = 0; i < parts.length; i++)
						{
							if (parts[i] != null)
								parts[i] = parts[i].trim();
						}
					}

					if (parts.length != headers.length)
						throw new InvalidColumnNumberException("Columns found: " + parts.length + ". Columns expected: " + headers.length);

					String rowIdent = parts[0];

					outer:
					for (int col = 1; col < headers.length; col++)
					{
						/* Check if the user pressed "cancel" */
						if (monitor.isCanceled())
						{
							monitor.done();
							br.close();

							GerminateDataImporter.getInstance().setTaskBarProgress(SWT.DEFAULT, 0);
							onImportCancelled(generatedIds, updatedIds);

							return;
						}

						String colIdent = headers[col];

						ValueQuery query = new ValueQuery(theQuery);

                        /* Iterate over all selected DatabaseColumns */
						for (MatrixColumnMapperRowDTO dto : mapping)
						{
							try
							{
								String value = null;

                                /* If it's not a predefined mapping */
								if (dto.element == null)
								{
									value = dto.manualEntry;
								}
								else
								{
									switch (dto.element)
									{
										case COL_ID:
											value = colIdent;
											break;

										case ROW_ID:
											value = rowIdent;
											break;

										case VALUE:
											/* Ignore empty cells */
											if (StringUtils.isEmpty(parts[col]))
											{
												query.closeDatabase();
												continue outer;
											}
											/* Process the regex */
											else if (dto.regex != null)
											{
												Matcher m = dto.regex.matcher(parts[col]);

												if (m.find())
													value = m.group();
												else
													throw new ParseException("Regex '" + dto.regex.toString() + "' didn't find a match in: '" + parts[col] + "'.");
											}
											/* Process manual entry */
											else if (!StringUtils.isEmpty(dto.manualEntry))
											{
												value = dto.manualEntry;
											}
											/* Just copy the value */
											else
											{
												value = parts[col];
											}
											break;
									}
								}

                                /* Either set or nullify */
								if (StringUtils.isEmpty(value))
									query.setNull(Types.VARCHAR);
								else
									query.setString(value);
							}
							catch (ParseException | NumberFormatException e)
							{
								ErrorLogger.getInstance().log(Level.SEVERE, e);

                                /* Check if the user wants to ignore exceptions
								 * of this type */
								Boolean dontAskAgain = exceptionsToIgnore.get(e.getClass());
								if (dontAskAgain != null && dontAskAgain)
									continue;
								else if (dontAskAgain == null)
									dontAskAgain = false;

                                /* Ask user what to do */
								Tuple.Pair<Boolean, Boolean> shouldContinue = onImportError(e, dontAskAgain);

                                /* If user doesn't want to continue, fail */
								if (!shouldContinue.getFirst())
								{
									GerminateDataImporter.getInstance().setTaskBarProgress(SWT.DEFAULT, 0);
									onImportFailed(generatedIds, updatedIds, e);
									br.close();
									e.printStackTrace();

									/* Remember to close the database connection */
									query.closeDatabase();

									return;
								}
								else
								{
                                    /* Else check if user wants to ignore this
                                     * exception in the future */
									exceptionsToIgnore.put(e.getClass(), shouldContinue.getSecond());

									continue;
								}
							}
						}

						monitor.subTask(RB.getString(RB.DIALOG_IMPORT_PROGRESS_DATUM, rowCounter, cellCounter++));

						LogDialog.getInstance().add(RB.getString(RB.DIALOG_LOG_INSERT), query.getStringRepresentation());

                        /* Execute and store the generated ids */
						generatedIds.addAll(query.execute());
					}
				}
				catch (DatabaseException | InvalidColumnNumberException e)
				{
					ErrorLogger.getInstance().log(Level.SEVERE, e);
                    
                    /* Check if the user wants to ignore exceptions of this type */
					Boolean dontAskAgain = exceptionsToIgnore.get(e.getClass());
					if (dontAskAgain != null && dontAskAgain)
						continue;
					else if (dontAskAgain == null)
						dontAskAgain = false;

                    /* Ask user what to do */
					Tuple.Pair<Boolean, Boolean> shouldContinue = onImportError(e, dontAskAgain);

                    /* If user doesn't want to continue, fail */
					if (!shouldContinue.getFirst())
					{
						GerminateDataImporter.getInstance().setTaskBarProgress(SWT.DEFAULT, 0);
						onImportFailed(generatedIds, updatedIds, e);
						br.close();
						e.printStackTrace();

						return;
					}
					else
					{
                        /* Else check if user wants to ignore this exception in
                         * the future */
						exceptionsToIgnore.put(e.getClass(), shouldContinue.getSecond());

						continue;
					}
				}
			}

			br.close();
		}
		catch (IOException e)
		{
			ErrorLogger.getInstance().log(Level.SEVERE, e);

			GerminateDataImporter.getInstance().setTaskBarProgress(SWT.DEFAULT, 0);
			onImportFailed(generatedIds, updatedIds, e);
		}

        /* We are done */
		monitor.done();

		GerminateDataImporter.getInstance().setTaskBarProgress(SWT.DEFAULT, 0);

		onImportFinished(generatedIds, updatedIds);
	}
}
