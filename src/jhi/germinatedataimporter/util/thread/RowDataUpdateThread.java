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
public abstract class RowDataUpdateThread extends RowDataInsertThread
{
	private String updateQuery;
	private String selectQuery;

	/**
	 * Creates a new instance of {@link RowDataImportThread}
	 *
	 * @param options The {@link ColumnMapper.InputOptions}s
	 * @param mapping The mapping of file column name, {@link DatabaseColumn} and {@link DatabaseColumn.Condition} ({@link DatabaseColumn.Condition}
	 *                can be <code>null</code>)
	 * @param table   The {@link DatabaseTable} to import the data to
	 */
	public RowDataUpdateThread(ColumnMapper.InputOptions options, List<SimpleColumnMapperRow.SimpleColumnMapperRowDTO> mapping, DatabaseTable table)
	{
		super(options, mapping, table);
	}

	@Override
	protected void prepareQueries()
	{
		StatementType.SELECT.prefix = "SELECT * FROM " + table.getName() + " WHERE ";
		StatementType.UPDATE.prefix = "UPDATE " + table.getName() + " SET ";

		selectQuery = prepareStatement(StatementType.SELECT);
		updateQuery = prepareStatement(StatementType.UPDATE);

		StringBuilder builder = new StringBuilder("INSERT INTO " + table.getName() + "(");

        /* First, add the database columns into which we want to import data */
		builder.append(mapping.get(0).columnDatabase.getName());
		for (int i = 1; i < mapping.size(); i++)
		{
			builder.append(", ")
				   .append(mapping.get(i).columnDatabase.getName());
		}

		builder.append(") VALUES (");

		StatementType.INSERT.prefix = builder.toString();

		insertQuery = prepareInsertStatement();
	}

	private String prepareStatement(StatementType type)
	{
		/* Build the SQL statement for insertion */
		StringBuilder builder = new StringBuilder(type.prefix);

		int placeholderCounter = 1;
		/* Now create placeholders or sub-queries or constants */
		for (int i = 0; i < mapping.size(); i++)
		{
			SimpleColumnMapperRow.SimpleColumnMapperRowDTO dto = mapping.get(i);

			switch (type)
			{
				case SELECT:
					if (dto.toUpdate)
						continue;
					break;
				case UPDATE:
					if (!dto.toUpdate)
						continue;
					break;
			}

			builder.append(dto.columnDatabase.getName());

            /*
			 * If there is a condition, add the sub-query to get the appropriate
             * id
             */
			if (dto.keyCondition != null)
			{
				DatabaseColumn.Condition cond = dto.keyCondition;
				builder.append(type.operator)
					   .append(String.format(SQLUtils.SELECT_SUBQUERY, cond.getTable().getName(), cond.getColumn().getName()));

				setIndex(type, dto, placeholderCounter++);
			}
			/*
			 * If there is a DateOption and the user wants to use NOW(), don't
             * create a placeholder, just insert NOW()
             */
			else if (dto.dateOption instanceof DateOptionsDialog.NowOption)
			{
				builder.append(type.nowPlaceholder);
			}
			/* If it's a date option other than NOW() OR if it's a manual entry */
			else if (dto.dateOption != null || !StringUtils.isEmpty(dto.manualEntry))
			{
				/*
				 * This is the tricky part, we have to be able to figure out the
                 * parameter index later on. So we act as if this was a column
                 * from the input file
                 */
				setIndex(type, dto, placeholderCounter++);
				builder.append(type.operator)
					   .append("?");
			}
			/* Otherwise, just add the content itself */
			else
			{
				setIndex(type, dto, placeholderCounter++);
				builder.append(type.operator)
					   .append("?");
			}

			if (i < mapping.size() - 1)
				builder.append(type.separator);
		}

		if (builder.lastIndexOf(type.separator) == builder.length() - type.separator.length())
			builder.delete(builder.length() - type.separator.length(), builder.length());

		builder.append(type.postfix);

		return builder.toString();
	}

	private void setIndex(StatementType type, SimpleColumnMapperRow.SimpleColumnMapperRowDTO dto, int index)
	{
		switch (type)
		{
			case SELECT:
				dto.indexInSelectStatement = index;
				break;
			case UPDATE:
				dto.indexInUpdateStatement = index;
				break;
		}
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

	private DatabaseStatement getUpdateStatement(Database database, String[] parts, long id) throws DatabaseException, ParseException
	{
		DatabaseStatement stmt = database.prepareStatement(updateQuery);

		int counter = 1;
		/* Iterate over all selected DatabaseColumns */
		for (SimpleColumnMapperRow.SimpleColumnMapperRowDTO dto : mapping)
		{
			int sourceIndex = dto.indexInSourceFile;
			int placeholderIndex = dto.indexInUpdateStatement;

			counter = Math.max(counter, placeholderIndex);

			boolean doContinue = setPart(dto, stmt, parts, sourceIndex, placeholderIndex);

			if (!doContinue)
				return null;
		}

		stmt.setLong(++counter, id);

		return stmt;
	}

	@Override
	protected List<Long> runStatement(Database database, String[] parts) throws DatabaseException, ParseException
	{
		DatabaseStatement stmt;

		long existingId = checkIfExists(database, parts);

		if (existingId != -1)
		{
			stmt = getUpdateStatement(database, parts, existingId);

			if (stmt == null)
				return new ArrayList<>();

			LogDialog.getInstance().add(RB.getString(RB.DIALOG_LOG_INSERT), stmt.getStringRepresentation());

                        /* Execute and store the generated ids */
			updatedIds += stmt.executeUpdate();

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
