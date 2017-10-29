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

package jhi.germinatedataimporter.database.entities;

import jhi.database.server.*;
import jhi.database.server.parser.*;
import jhi.database.server.query.*;
import jhi.database.shared.exception.*;
import jhi.database.shared.util.*;
import jhi.germinatedataimporter.util.*;
import jhi.swtcommons.util.*;

/**
 * {@link DatabaseColumn} is a bean class for a column in the database.
 *
 * @author Sebastian Raubach
 */
public class DatabaseColumn extends DatabaseObject
{
	private static final String TYPE_DECIMAL = "decimal";
	private static final String TYPE_DOUBLE  = "double";
	private static final String TYPE_FLOAT   = "float";

	private static final String PRIMARY_KEY = "PRIMARY";

	private static final String NO  = "NO";
	private static final String YES = "YES";

	private static final String DATE_TIMESTAMP = "timestamp";
	private static final String DATE_DATETIME  = "datetime";
	private static final String DATE_DATE      = "date";

	private String  name;
	private String  type;
	private Boolean isPrimaryKey;
	private Boolean isForeignKey;
	private String  foreignKeyTable;
	private String  foreignKeyColumn;
	private Boolean canBeNull;
	private Boolean isDecimal;

	DatabaseColumn(String name, String type, String isPrimaryKey, String foreignKeyTable, String foreignKeyColumn, String canBeNull)
	{
		this.name = name;
		this.type = type;

		this.isPrimaryKey = !StringUtils.isEmpty(isPrimaryKey) && isPrimaryKey.startsWith(PRIMARY_KEY);
		this.isForeignKey = foreignKeyColumn != null;

		this.foreignKeyTable = foreignKeyTable;
		this.foreignKeyColumn = foreignKeyColumn;

        /* The column can be null if either: It's the primary key (since we use
		 * auto_increment) OR if it's defined in the database that it can be
         * null */
		this.canBeNull = this.isPrimaryKey || (NO.equals(canBeNull) ? false : (YES.equals(canBeNull) ? true : null));

		this.isDecimal = TYPE_DECIMAL.equalsIgnoreCase(type) || TYPE_FLOAT.equalsIgnoreCase(type) || TYPE_DOUBLE.equalsIgnoreCase(type);
	}

	/**
	 * Returns the {@link DatabaseColumn} with the given name in the given {@link DatabaseTable}
	 *
	 * @param table      The {@link DatabaseTable}
	 * @param columnName The name of the column
	 * @return The column
	 * @throws DatabaseException Thrown if the interaction with the database fails
	 */
	public static DatabaseColumn getColumnWithName(DatabaseTable table, String columnName) throws DatabaseException
	{
		if (table == null)
			return null;

		return new DatabaseObjectQuery<DatabaseColumn>(SQLUtils.SELECT_COLUMNS_OF_TABLE_WITH_NAME)
				.setString(GerminateParameterStore.getInstance().getAsString(GerminateParameter.database))
				.setString(table.getName())
				.setString(columnName)
				.run()
				.getObject(DatabaseColumn.Parser.Instance.getInstance());
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getType()
	{
		return type;
	}

	public void setType(String type)
	{
		this.type = type;
	}

	@SuppressWarnings("unused")
	public Boolean isPrimaryKey()
	{
		return isPrimaryKey;
	}

	public Boolean isForeignKey()
	{
		return isForeignKey;
	}

	public String getForeignKeyTable()
	{
		return foreignKeyTable;
	}

	@SuppressWarnings("unused")
	public void setForeignKeyTable(String foreignKeyTable)
	{
		this.foreignKeyTable = foreignKeyTable;
	}

	public String getForeignKeyColumn()
	{
		return foreignKeyColumn;
	}

	@SuppressWarnings("unused")
	public void setForeignKeyColumn(String foreignKeyColumn)
	{
		this.foreignKeyColumn = foreignKeyColumn;
	}

	public Boolean isDecimal()
	{
		return isDecimal;
	}

	public Boolean getCanBeNull()
	{
		return canBeNull;
	}

	@SuppressWarnings("unused")
	public void setCanBeNull(Boolean canBeNull)
	{
		this.canBeNull = canBeNull;
	}

	/**
	 * Checks if this {@link DatabaseColumn} is either a {@link #DATE_DATE} or {@link #DATE_DATETIME} or {@link #DATE_DATETIME}.
	 *
	 * @return <code>true</code> if this {@link DatabaseColumn} represents a date
	 */
	public boolean isDate()
	{
		return DATE_DATE.equalsIgnoreCase(this.type) || DATE_DATETIME.equalsIgnoreCase(this.type) || DATE_TIMESTAMP.equalsIgnoreCase(this.type);
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((canBeNull == null) ? 0 : canBeNull.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		result = prime * result + ((isDecimal == null) ? 0 : isDecimal.hashCode());
		result = prime * result + ((isPrimaryKey == null) ? 0 : isPrimaryKey.hashCode());
		result = prime * result + ((isForeignKey == null) ? 0 : isForeignKey.hashCode());
		result = prime * result + ((foreignKeyTable == null) ? 0 : foreignKeyTable.hashCode());
		result = prime * result + ((foreignKeyColumn == null) ? 0 : foreignKeyColumn.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		boolean result;

		if (obj != null && obj instanceof DatabaseColumn)
		{
			DatabaseColumn col = (DatabaseColumn) obj;

            /* Don't check for the key here, since it can be null even for valid
			 * objects */
			if (col.getName() == null || col.getType() == null)
			{
				result = false;
			}
			else
			{
				result = col.name.equals(this.name)
						&& col.type.equals(this.type)
						&& col.isDecimal.equals(this.isDecimal)
						&& col.canBeNull.equals(this.canBeNull)
						&& col.isForeignKey.equals(this.isForeignKey)
						&& col.isPrimaryKey.equals(this.isPrimaryKey);
			}
		}
		else
		{
			result = false;
		}

		return result;
	}

	@Override
	public String toString()
	{
		return "DatabaseColumn [name=" + name + ", type=" + type + ", isPrimaryKey=" + isPrimaryKey + ", isForeignKey=" + isForeignKey + ", foreignKeyTable=" + foreignKeyTable + ", foreignKeyColumn="
				+ foreignKeyColumn + ", canBeNull=" + canBeNull + ", isDecimal=" + isDecimal + "]";
	}

	public static class Condition
	{
		private DatabaseTable  table;
		private DatabaseColumn column;

		public Condition(DatabaseTable table, DatabaseColumn column)
		{
			this.table = table;
			this.column = column;
		}

		public DatabaseTable getTable()
		{
			return table;
		}

		public void setTable(DatabaseTable table)
		{
			this.table = table;
		}

		public DatabaseColumn getColumn()
		{
			return column;
		}

		public void setColumn(DatabaseColumn column)
		{
			this.column = column;
		}

		@Override
		public String toString()
		{
			return "Condition [table=" + table + ", column=" + column + "]";
		}
	}

	public static class Parser implements DatabaseObjectParser<DatabaseColumn>
	{
		private Parser()
		{

		}

		public void clearCache()
		{

		}

		@Override
		public DatabaseColumn parse(DatabaseResult row, boolean foreignsFromResultSet) throws DatabaseException
		{
			return new DatabaseColumn(
					row.getString(DatabaseTable.HEADER_COLUMN_NAME),
					row.getString(DatabaseTable.HEADER_DATA_TYPE),
					row.getString(DatabaseTable.HEADER_PRIMARY_KEY),
					row.getString(DatabaseTable.HEADER_FOREIGN_KEY_TABLE),
					row.getString(DatabaseTable.HEADER_FOREIGN_KEY_COLUMN),
					row.getString(DatabaseTable.HEADER_IS_NULL));
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
