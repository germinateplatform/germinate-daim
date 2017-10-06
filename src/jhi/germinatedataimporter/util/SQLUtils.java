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

package jhi.germinatedataimporter.util;

/**
 * {@link SQLUtils} contains most of the SQL query constants used in Germinate Daim along with methods to manipulate them.
 *
 * @author Sebastian Raubach
 */
public class SQLUtils
{
	public static final String AUTO_INCREMENT = "AUTO_INCREMENT";

	public static final String SELECT_TABLES_OF_DATABASE             = "SHOW TABLE STATUS FROM %s WHERE Comment != \"VIEW\"";
	public static final String SELECT_TABLES_OF_DATABASE_WITH_NAME   = "SHOW TABLE STATUS FROM %s WHERE Comment != \"VIEW\" AND Name = ?";
	public static final String SELECT_COUNT_OF_TABLE                 = "SELECT COUNT(*) AS count FROM %s";
	public static final String SELECT_COLUMNS_OF_TABLE               = "SELECT c.COLUMN_NAME, c.IS_NULLABLE, c.DATA_TYPE, GROUP_CONCAT(k.REFERENCED_COLUMN_NAME) AS FOREIGN_KEY_COLUMN, GROUP_CONCAT(k.CONSTRAINT_NAME ORDER BY FIELD(k.CONSTRAINT_NAME, 'PRIMARY') DESC SEPARATOR ' ') AS PRIMARY_KEY, GROUP_CONCAT(k.REFERENCED_TABLE_NAME) AS FOREIGN_KEY_TABLE FROM information_schema.columns AS c LEFT JOIN information_schema.KEY_COLUMN_USAGE AS k ON c.TABLE_SCHEMA = k.TABLE_SCHEMA AND c.TABLE_NAME = k.TABLE_NAME AND c.COLUMN_NAME = k.COLUMN_NAME WHERE c.TABLE_SCHEMA = ? AND c.TABLE_NAME = ? GROUP BY COLUMN_NAME, IS_NULLABLE, DATA_TYPE";
	public static final String SELECT_COLUMNS_OF_TABLE_WITH_NAME     = "SELECT c.COLUMN_NAME, c.IS_NULLABLE, c.DATA_TYPE, GROUP_CONCAT(k.REFERENCED_COLUMN_NAME) AS FOREIGN_KEY_COLUMN, GROUP_CONCAT(k.CONSTRAINT_NAME ORDER BY FIELD(k.CONSTRAINT_NAME, 'PRIMARY') DESC SEPARATOR ' ') AS PRIMARY_KEY, GROUP_CONCAT(k.REFERENCED_TABLE_NAME) AS FOREIGN_KEY_TABLE FROM information_schema.columns AS c LEFT JOIN information_schema.KEY_COLUMN_USAGE AS k ON c.TABLE_SCHEMA = k.TABLE_SCHEMA AND c.TABLE_NAME = k.TABLE_NAME AND c.COLUMN_NAME = k.COLUMN_NAME WHERE c.TABLE_SCHEMA = ? AND c.TABLE_NAME = ? AND c.COLUMN_NAME = ? GROUP BY COLUMN_NAME, IS_NULLABLE, DATA_TYPE";
	public static final String SELECT_SUBQUERY                       = "(SELECT id FROM %s WHERE %s=? LIMIT 1)";
	public static final String DELETE_ENTRIES                        = "DELETE FROM %s WHERE id IN (%s)";
	public static final String SELECT_EXAMPLE_DATA_FROM_TABLE_COLUMN = "SELECT * FROM (SELECT DISTINCT(%s) FROM %s ORDER BY RAND() LIMIT 10) AS a ORDER BY %s";
	public static final String SELECT_AUTO_INCREMENT                 = "SELECT AUTO_INCREMENT FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ?";
	public static final String ALTER_AUTO_INCREMENT                  = "ALTER TABLE %s AUTO_INCREMENT = ?";
	public static final String SELECT_MAX_ID                         = "SELECT MAX(id) AS AUTO_INCREMENT FROM %s";

	/**
	 * Generates a SQL placeholder String of the form: "?,?,?,?" for the given size.
	 *
	 * @param size The number of placeholders to generate
	 * @return The generated String
	 */
	public static String generateSqlPlaceholderString(int size)
	{
		if (size < 1)
			return "";

		StringBuilder builder = new StringBuilder();

		builder.append("?");

		for (int i = 1; i < size; i++)
		{
			builder.append(",?");
		}

		return builder.toString();
	}
}
