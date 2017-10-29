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

package jhi.germinatedataimporter.util.exception;

import java.sql.*;

import jhi.germinatedataimporter.gui.i18n.*;

/**
 * @author Sebastian Raubach
 */
public class DatabaseExceptionUtils
{
	public static String getMessageFromException(SQLException e)
	{
		int errorCode = e.getErrorCode();

		switch (errorCode)
		{
			case 1048:
				return RB.getString(RB.ERROR_MYSQL_1048);
		}

		return e.getLocalizedMessage();
	}
}
