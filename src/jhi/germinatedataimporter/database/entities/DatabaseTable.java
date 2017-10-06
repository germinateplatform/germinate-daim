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

package jhi.germinatedataimporter.database.entities;

import java.util.*;

import jhi.database.server.*;
import jhi.database.server.parser.*;
import jhi.database.server.query.*;
import jhi.database.shared.exception.*;
import jhi.database.shared.util.*;
import jhi.germinatedataimporter.gui.dialog.*;
import jhi.germinatedataimporter.gui.i18n.*;
import jhi.germinatedataimporter.util.*;
import jhi.swtcommons.util.*;

/**
 * {@link DatabaseTable} is a bean class for a column in the database.
 *
 * @author Sebastian Raubach
 */
public class DatabaseTable extends DatabaseObject
{
	public static final  String               HEADER_COLUMN_NAME        = "COLUMN_NAME";
	public static final  String               HEADER_DATA_TYPE          = "DATA_TYPE";
	public static final  String               HEADER_FOREIGN_KEY_COLUMN = "FOREIGN_KEY_COLUMN";
	public static final  String               HEADER_FOREIGN_KEY_TABLE  = "FOREIGN_KEY_TABLE";
	public static final  String               HEADER_PRIMARY_KEY        = "PRIMARY_KEY";
	public static final  String               HEADER_IS_NULL            = "IS_NULLABLE";
	public static final  String               HEADER_COUNT              = "count";
	private static final int                  DELETE_CHUNK_SIZE         = 1000;
	private              List<DatabaseColumn> columns                   = new ArrayList<>();

	private String name;
	private long   size;

	public DatabaseTable()
	{

	}

	public DatabaseTable(String name, long size)
	{
		this.name = name;
		this.size = size;
	}

	/**
	 * Returns the {@link DatabaseTable} with the given name
	 *
	 * @param tableName The name of the table
	 * @return The table with this name. <code>null</code> if it doesn't exist
	 * @throws DatabaseException Thrown if the communication with the database fails
	 */
	public static DatabaseTable getTableWithName(String tableName) throws DatabaseException
	{
		DatabaseDatabase database = (DatabaseDatabase) GerminateParameterStore.getInstance().get(GerminateParameter.database);

		if (database == null)
			return null;

		return new DatabaseObjectQuery<DatabaseTable>(String.format(SQLUtils.SELECT_TABLES_OF_DATABASE_WITH_NAME, database.getName()))
				.setString(tableName)
				.run()
				.getObject(DatabaseTable.Parser.Instance.getInstance());
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public long getSize()
	{
		return size;
	}

	public void setSize(int size)
	{
		this.size = size;
	}

	/**
	 * Tries to refresh this {@link DatabaseTable}. This includes updating it's size. <p> Since the row count using <code>SHOW TABLE STATUS</code> for
	 * InnoDB is an approximation, we have to get it by calling <code>SELECT COUNT(1)</code> instead.
	 *
	 * @return <code>true</code> if this {@link DatabaseTable} has been refreshed, <code>false</code> otherwise
	 * @throws DatabaseException Thrown if the communication with the database fails
	 */
	public boolean refresh() throws DatabaseException
	{
		Integer size = new ValueQuery(String.format(SQLUtils.SELECT_COUNT_OF_TABLE, this.name))
				.run(HEADER_COUNT)
				.getInt();

		if (size != null)
		{
			this.size = size;
			return true;
		}
		else
		{
			return false;
		}
	}

	/**
	 * Returns the AUTO_INCREMENT value of this {@link DatabaseTable}
	 *
	 * @return The AUTO_INCREMENT value of this {@link DatabaseTable}
	 * @throws DatabaseException Thrown if the communication with the database fails
	 */
	public long getAutoIncrement() throws DatabaseException
	{
		return new ValueQuery(SQLUtils.SELECT_AUTO_INCREMENT)
				.setString(GerminateParameterStore.getInstance().getAsString(GerminateParameter.database))
				.setString(this.name)
				.run(SQLUtils.AUTO_INCREMENT)
				.getLong(1);
	}

	/**
	 * Sets the AUTO_INCREMENT of this {@link DatabaseTable}
	 *
	 * @throws DatabaseException Thrown if the communication with the database fails
	 */
	public void setAutoIncrement() throws DatabaseException
	{
		long autoIncrement = new ValueQuery(String.format(SQLUtils.SELECT_MAX_ID, this.name))
				.run(SQLUtils.AUTO_INCREMENT)
				.getLong(1);

		new ValueQuery(String.format(SQLUtils.ALTER_AUTO_INCREMENT, this.name))
				.setLong(autoIncrement + 1)
				.execute();
	}

	/**
	 * Returns all {@link DatabaseColumn} of the given table
	 *
	 * @return The {@link DatabaseColumn}s
	 */
	public List<DatabaseColumn> getColumns()
	{
		if (columns.size() <= 0)
		{
			try
			{
				DatabaseObjectQuery.DatabaseObjectStreamer<DatabaseColumn> streamer = new DatabaseObjectQuery<DatabaseColumn>(SQLUtils.SELECT_COLUMNS_OF_TABLE)
						.setString(GerminateParameterStore.getInstance().getAsString(GerminateParameter.database))
						.setString(DatabaseTable.this.name)
						.getStreamer(DatabaseColumn.Parser.Instance.getInstance());

				DatabaseColumn row;

				while ((row = streamer.next()) != null)
				{
					columns.add(row);
				}

				return columns;
			}
			catch (DatabaseException e)
			{
				DialogUtils.handleException(e);
			}
		}

		return columns;
	}

	/**
	 * Deletes the items with the given ids from this {@link DatabaseTable}
	 *
	 * @param ids The ids to remove
	 * @throws DatabaseException Thrown if the communication with the database fails
	 */
	public void deleteItemsWithId(List<Long> ids) throws DatabaseException
	{
		if (CollectionUtils.isEmpty(ids))
			return;

        /* We delete items in chunks to avoid huge IN statements */
		for (int chunk = 0; chunk < Math.ceil(ids.size() / (1.0 * DELETE_CHUNK_SIZE)); chunk++)
		{
			int size = Math.min(DELETE_CHUNK_SIZE, ids.size() - chunk * DELETE_CHUNK_SIZE);
			String formatted = String.format(SQLUtils.DELETE_ENTRIES, this.name, SQLUtils.generateSqlPlaceholderString(size));

			ValueQuery query = new ValueQuery(formatted)
					.setLongs(ids.subList(DELETE_CHUNK_SIZE * chunk, Math.min(ids.size(), DELETE_CHUNK_SIZE * (chunk + 1))));

			LogDialog.getInstance().add(RB.getString(RB.DIALOG_LOG_DELETE), query.getStringRepresentation());

			query.execute();
		}
	}

	/**
	 * Returns example data from this {@link DatabaseTable} and the given {@link DatabaseColumn}. A random set of at most 10 random entries is chosen
	 * and returned.
	 *
	 * @param column The {@link DatabaseColumn}
	 * @return The example data or <code>null</code> if a {@link DatabaseException} occurs
	 */
	public List<String> getExampleDataFromColumn(DatabaseColumn column)
	{
		try
		{
			return new ValueQuery(String.format(SQLUtils.SELECT_EXAMPLE_DATA_FROM_TABLE_COLUMN, column.getName(), name, column.getName()))
					.run(column.getName())
					.getStrings();
		}
		catch (DatabaseException e)
		{
			return null;
		}
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj != null && obj instanceof DatabaseTable)
		{
			DatabaseTable table = (DatabaseTable) obj;

			return table.getName() != null && table.getName().equals(this.name);
		}
		else
		{
			return false;
		}
	}

	@Override
	public String toString()
	{
		return "DatabaseTable [name=" + name + ", size=" + size + "]";
	}

	public static class Parser implements DatabaseObjectParser<DatabaseTable>
	{
		private Parser()
		{

		}

		public void clearCache()
		{

		}

		@Override
		public DatabaseTable parse(DatabaseResult row, boolean foreignsFromResultSet) throws DatabaseException
		{
			DatabaseTable table = new DatabaseTable();

			table.setName(row.getString(DatabaseDatabase.HEADER_NAME));
			table.setSize(row.getInt(DatabaseDatabase.HEADER_ROWS));

			return table;
		}

		public static final class Instance
		{
			public static Parser getInstance()
			{
				return InstanceHolder.INSTANCE;
			}

			private static final class InstanceHolder
			{
				private static final Parser INSTANCE = new Parser();
			}
		}
	}
}
