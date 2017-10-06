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

import java.text.*;
import java.util.*;

import jhi.database.server.*;
import jhi.database.shared.exception.*;
import jhi.germinatedataimporter.database.entities.*;
import jhi.germinatedataimporter.gui.dialog.*;
import jhi.germinatedataimporter.gui.i18n.*;
import jhi.germinatedataimporter.gui.widget.*;
import jhi.germinatedataimporter.util.*;
import jhi.swtcommons.util.*;

/**
 * @author Sebastian Raubach
 */
public abstract class RowDataInsertExistThread extends RowDataInsertThread
{
	private String selectQuery;

	/**
	 * Creates a new instance of {@link RowDataImportThread}
	 *
	 * @param options The {@link ColumnMapper.InputOptions}s
	 * @param mapping The mapping of file column name, {@link DatabaseColumn} and {@link DatabaseColumn.Condition} ({@link DatabaseColumn.Condition}
	 *                can be <code>null</code>)
	 * @param table   The {@link DatabaseTable} to import the data to
	 */
	public RowDataInsertExistThread(ColumnMapper.InputOptions options, List<SimpleColumnMapperRow.SimpleColumnMapperRowDTO> mapping, DatabaseTable table)
	{
		super(options, mapping, table);
	}

	@Override
	protected void prepareQueries()
	{
		super.prepareQueries();

		StatementType.SELECT.prefix = "SELECT * FROM " + table.getName() + " WHERE ";

		selectQuery = prepareStatement();
	}

	private String prepareStatement()
	{
		/* Build the SQL statement for insertion */
		StringBuilder builder = new StringBuilder(StatementType.SELECT.prefix);

		int placeholderCounter = 1;
		/* Now create placeholders or sub-queries or constants */
		for (int i = 0; i < mapping.size(); i++)
		{
			SimpleColumnMapperRow.SimpleColumnMapperRowDTO dto = mapping.get(i);

			if (!dto.toUpdate)
				continue;

			builder.append(dto.columnDatabase.getName());

            /*
			 * If there is a condition, add the sub-query to get the appropriate
             * id
             */
			if (dto.keyCondition != null)
			{
				DatabaseColumn.Condition cond = dto.keyCondition;
				builder.append(StatementType.SELECT.operator)
					   .append(String.format(SQLUtils.SELECT_SUBQUERY, cond.getTable().getName(), cond.getColumn().getName()));

				dto.indexInSelectStatement = placeholderCounter++;
			}
			/*
			 * If there is a DateOption and the user wants to use NOW(), don't
             * create a placeholder, just insert NOW()
             */
			else if (dto.dateOption instanceof DateOptionsDialog.NowOption)
			{
				builder.append(StatementType.SELECT.nowPlaceholder);
			}
			/* If it's a date option other than NOW() OR if it's a manual entry */
			else if (dto.dateOption != null || !StringUtils.isEmpty(dto.manualEntry))
			{
				/*
				 * This is the tricky part, we have to be able to figure out the
                 * parameter index later on. So we act as if this was a column
                 * from the input file
                 */
				dto.indexInSelectStatement = placeholderCounter++;
				builder.append(StatementType.SELECT.operator)
					   .append("?");
			}
			/* Otherwise, just add the content itself */
			else
			{
				dto.indexInSelectStatement = placeholderCounter++;
				builder.append(StatementType.SELECT.operator)
					   .append("?");
			}

			if (i < mapping.size() - 1)
				builder.append(StatementType.SELECT.separator);
		}

		int index = builder.lastIndexOf(StatementType.SELECT.separator);
		if (index == builder.length() - StatementType.SELECT.separator.length())
		{
			builder.delete(index, index + StatementType.SELECT.separator.length());
		}

		builder.append(StatementType.SELECT.postfix);

		return builder.toString();
	}

	private long checkIfExists(Database database, String[] parts) throws DatabaseException, ParseException
	{
		DatabaseStatement stmt = database.prepareStatement(selectQuery);

        /* Iterate over all selected DatabaseColumns */
		for (SimpleColumnMapperRow.SimpleColumnMapperRowDTO dto : mapping)
		{
			int sourceIndex = dto.indexInSourceFile;
			int placeholderIndex = dto.indexInSelectStatement;

			boolean doContinue = setPart(dto, stmt, parts, sourceIndex, placeholderIndex);

			if (!doContinue)
				return -1;
		}

		DatabaseResult rs = stmt.query();

		if (rs.next())
		{
			return rs.getLong("id");
		}

		return -1;
	}

	@Override
	protected List<Long> runStatement(Database database, String[] parts) throws DatabaseException, ParseException
	{
		DatabaseStatement stmt;

		long existingId = checkIfExists(database, parts);

		if (existingId != -1)
		{
			/* Do nothing here, as the entry already exists */

			return new ArrayList<>();
		}
		else
		{
			stmt = getInsertStatement(database, parts);

			if (stmt == null)
				return new ArrayList<>();

			LogDialog.getInstance().add(RB.getString(RB.DIALOG_LOG_INSERT), stmt.getStringRepresentation());

            /* Execute and store the generated ids */
			return stmt.execute();
		}
	}
}
