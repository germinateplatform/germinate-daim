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

import jhi.germinatedataimporter.database.entities.*;
import jhi.swtcommons.util.*;

/**
 * @author Sebastian Raubach
 */
public class GerminateParameterStore extends ParameterStore
{
	private static GerminateParameterStore INSTANCE;

	public static synchronized GerminateParameterStore getInstance()
	{
		if (INSTANCE == null)
			INSTANCE = new GerminateParameterStore();

		return INSTANCE;
	}

	@Override
	public String getAsString(Parameter key)
	{
		String result = super.getAsString(key);

		if (result == null)
			return null;
		else if (key.getType().equals(DatabaseDatabase.class))
			return ((DatabaseDatabase) get(key)).getName();
		else if (key.getType().equals(FileSeparator.class))
			return ((FileSeparator) get(key)).name();
		else
			return result;
	}
}
