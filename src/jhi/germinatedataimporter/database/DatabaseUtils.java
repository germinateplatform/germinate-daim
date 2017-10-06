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

package jhi.germinatedataimporter.database;

import jhi.germinatedataimporter.util.*;
import jhi.swtcommons.util.*;

/**
 * @author Sebastian Raubach
 */
public class DatabaseUtils
{
	/**
	 * Returns a String of the form <SERVER>:<PORT>/<DATABASE>
	 *
	 * @return A String of the form <SERVER>:<PORT>/<DATABASE>
	 */
	public static String getServerString()
	{
		GerminateParameterStore store = GerminateParameterStore.getInstance();

		String server = store.getAsString(GerminateParameter.server);
		String database = store.getAsString(GerminateParameter.database);
		String port = store.getAsString(GerminateParameter.port);

		if (!StringUtils.isEmpty(port))
		{
			return server + ":" + port + "/" + database;
		}
		else
		{
			return server + "/" + database;
		}
	}

	/**
	 * Returns a String of the form <SERVER>:<PORT>/<DATABASE>
	 *
	 * @param server   The server string
	 * @param database The database string
	 * @param port     The port string
	 * @return A String of the form <SERVER>:<PORT>/<DATABASE>
	 */
	public static String getServerString(String server, String database, String port)
	{
		if (!StringUtils.isEmpty(port))
		{
			return server + ":" + port + "/" + database;
		}
		else
		{
			return server + "/" + database;
		}
	}
}
