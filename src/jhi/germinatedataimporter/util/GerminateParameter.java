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

import java.util.*;

import jhi.germinatedataimporter.database.entities.*;
import jhi.swtcommons.util.*;

/**
 * @author Sebastian Raubach
 */
public enum GerminateParameter implements Parameter
{
	locale(Locale.class),
	updateInterval(Install4jUtils.UpdateInterval.class),
	password(String.class),
	username(String.class),
	server(String.class),
	servers(List.class),
	database(DatabaseDatabase.class),
	databases(List.class),
	port(String.class),
	inputfile(String.class),
	inputseparator(FileSeparator.class),
	inputlocale(Locale.class),
	inputalwaysask(Boolean.class),
	removetrailingspaces(Boolean.class),
	//	updateData(Boolean.class),
	userId(String.class);

	private Class<?> type;

	GerminateParameter(Class<?> type)
	{
		this.type = type;
	}

	public Class<?> getType()
	{
		return type;
	}
}
