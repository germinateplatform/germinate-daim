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
import org.eclipse.jface.operation.*;
import org.eclipse.swt.*;

import java.io.*;
import java.lang.reflect.*;
import java.sql.*;
import java.text.*;
import java.util.*;
import java.util.Date;
import java.util.logging.*;

import jhi.database.server.*;
import jhi.database.shared.exception.*;
import jhi.germinatedataimporter.database.entities.*;
import jhi.germinatedataimporter.database.entities.DatabaseColumn.*;
import jhi.germinatedataimporter.gui.*;
import jhi.germinatedataimporter.gui.dialog.*;
import jhi.germinatedataimporter.gui.i18n.*;
import jhi.germinatedataimporter.gui.widget.ColumnMapper.*;
import jhi.germinatedataimporter.gui.widget.*;
import jhi.germinatedataimporter.gui.widget.SimpleColumnMapperRow.*;
import jhi.germinatedataimporter.util.*;
import jhi.germinatedataimporter.util.exception.*;
import jhi.germinatedataimporter.util.log.*;
import jhi.swtcommons.util.*;

/**
 * {@link RowDataImportThread} implements {@link IRunnableWithProgress} and is a thread that imports the data into the database.
 *
 * @author Sebastian Raubach
 */
public abstract class RowDataImportThread extends DataImportThread
{
	protected List<SimpleColumnMapperRowDTO> mapping;
	protected DecimalFormat                  numberFormat;
	protected int                            updatedIds;

	/**
	 * Creates a new instance of {@link RowDataImportThread}
	 *
	 * @param options The {@link InputOptions}s
	 * @param mapping The mapping of file column name, {@link DatabaseColumn} and {@link Condition} ({@link Condition} can be <code>null</code>)
	 * @param table   The {@link DatabaseTable} to import the data to
	 */
	public RowDataImportThread(InputOptions options, List<SimpleColumnMapperRowDTO> mapping, DatabaseTable table)
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

		prepareQueries();

		List<Long> generatedIds = new ArrayList<>();
		updatedIds = 0;

		try
		{
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(options.file), "UTF8"));

            /* Skip the first line (headers) */
			String line = br.readLine();
			String[] headers = line.split(options.separator.getSeparator(), -1);

            /* Remember the position (column index) in the input file */
			for (SimpleColumnMapperRowDTO dto : mapping)
			{
				for (int i = 0; i < headers.length; i++)
				{
					if (headers[i].trim().equals(dto.columnFile))
					{
						dto.indexInSourceFile = i;
						break;
					}
				}
			}

			int counter = 1;

			Database database = Database.connect();

			numberFormat = (DecimalFormat) NumberFormat.getInstance(options.locale);

			Boolean trimCells = (Boolean) GerminateParameterStore.getInstance().get(GerminateParameter.removetrailingspaces);

			while ((line = br.readLine()) != null)
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

				monitor.subTask(RB.getString(RB.DIALOG_IMPORT_PROGRESS_ROW, counter++));

				String[] parts = line.split(options.separator.getSeparator(), -1);

				if (trimCells != null && trimCells)
				{
					for (int i = 0; i < parts.length; i++)
					{
						if (parts[i] != null)
							parts[i] = parts[i].trim();
					}
				}

				try
				{
					if (parts.length != headers.length)
						throw new InvalidColumnNumberException("Columns found: " + parts.length + ". Columns expected: " + headers.length);

					/* Execute and store the generated ids */
					generatedIds.addAll(runStatement(database, parts));
				}
				catch (ParseException | DatabaseException | NumberFormatException | InvalidColumnNumberException e)
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
						database.close();

						GerminateDataImporter.getInstance().setTaskBarProgress(SWT.DEFAULT, 0);
						onImportFailed(generatedIds, updatedIds, e);
						br.close();
						e.printStackTrace();

						return;
					}
					else
					{
						/*
						 * Else check if user wants to ignore this exception in
                         * the future
                         */
						exceptionsToIgnore.put(e.getClass(), shouldContinue.getSecond());
					}
				}
			}

			br.close();

			database.close();
		}
		catch (DatabaseException | IOException e)
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

	protected boolean setPart(SimpleColumnMapperRow.SimpleColumnMapperRowDTO dto, DatabaseStatement stmt, String[] parts, int sourceIndex, int placeholderIndex) throws DatabaseException, ParseException
	{
		/* If there's a number range defined, check it */
		if (dto.numberRanges != null)
		{
			if (placeholderIndex == -1 || sourceIndex == -1)
				return true;

			Collection<NumberRangeDialog.NumberRange> ranges = dto.numberRanges;

			double value = numberFormat.parse(parts[sourceIndex]).doubleValue();

            /*
			 * If the value is not in the defined ranges, continue with the next
             * line of the input file
             */
			boolean inRange = false;

			for (NumberRangeDialog.NumberRange range : ranges)
			{
				if (value >= range.min && value <= range.max)
				{
					inRange = true;
					break;
				}
			}

			if (!inRange)
				return false;

            /* If we get here, it's safe to add it */
			stmt.setDouble(placeholderIndex, value);
		}
		/* If it's a date column, we need to do something else */
		else if (dto.dateOption != null)
		{
			DateOptionsDialog.DateOption option = dto.dateOption;

            /* Check if it actually exists */
			if ((option instanceof DateOptionsDialog.NowOption) || placeholderIndex == -1 || (option instanceof DateOptionsDialog.PatternOption && sourceIndex == -1))
				return true;

            /* Now replace the placeholder at the index */
			if (option instanceof DateOptionsDialog.CalendarOption)
			{
				stmt.setDate(placeholderIndex, ((DateOptionsDialog.CalendarOption) option).date.toDate());
			}
			else if (option instanceof DateOptionsDialog.PatternOption)
			{
				if (StringUtils.isEmpty(parts[sourceIndex]))
				{
					stmt.setNull(placeholderIndex, Types.VARCHAR);
				}
				else
				{
					SimpleDateFormat dateFormat = ((DateOptionsDialog.PatternOption) option).format;
					boolean isTimestamp = dateFormat.toPattern().contains("HH") && dateFormat.toPattern().contains("mm");

					Date date = dateFormat.parse(parts[sourceIndex]);

					if (isTimestamp)
						stmt.setTimestamp(placeholderIndex, date);
					else
						stmt.setDate(placeholderIndex, date);
				}
			}
		}
		/* If there is a manual entry to use */
		else if (!StringUtils.isEmpty(dto.manualEntry))
		{
			/* Check if it actually exists */
			if (placeholderIndex == -1)
				return true;

			stmt.setString(placeholderIndex, dto.manualEntry);
		}
		/* If it's not a date, simply copy from the input file */
		else
		{
			if (placeholderIndex == -1 || sourceIndex == -1)
				return true;

			String part = parts[sourceIndex];

			if (StringUtils.isEmpty(part))
				stmt.setNull(placeholderIndex, Types.VARCHAR);
			else
				stmt.setString(placeholderIndex, part);
		}

		return true;
	}

	protected abstract void prepareQueries();

	protected abstract List<Long> runStatement(Database database, String[] parts) throws DatabaseException, ParseException;

	protected enum StatementType
	{
		INSERT("", ")", ", ", "", "NOW()"),
		UPDATE("", " WHERE id = ?", ", ", " = ", " = NOW()"),
		SELECT("", " ORDER BY id DESC LIMIT 1", " AND ", " = ", " LIKE \"%\"");

		String prefix;
		String postfix;
		String separator;
		String operator;
		String nowPlaceholder;

		StatementType(String prefix, String postfix, String separator, String operator, String nowPlaceholder)
		{
			this.prefix = prefix;
			this.postfix = postfix;
			this.separator = separator;
			this.operator = operator;
			this.nowPlaceholder = nowPlaceholder;
		}
	}
}
