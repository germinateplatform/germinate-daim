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

import java.util.*;

import jhi.database.shared.exception.*;
import jhi.germinatedataimporter.database.entities.*;

/**
 * {@link DatabaseTableObserver} is used as a very basic sort of observer. {@link DatabaseTableListener}s can register ( {@link
 * #register(DatabaseTableListener)}) to get notified whenever {@link #notifyListenersAndRefresh(DatabaseTable)} is called. <p> {@link
 * #notifyListenersAndRefresh(DatabaseTable)} will refresh the {@link DatabaseTable} ({@link DatabaseTable#refresh()}).
 *
 * @author Sebastian Raubach
 */
public class DatabaseTableObserver
{
	private static final Set<DatabaseTableListener> LISTENERS = new HashSet<>();

	/**
	 * Adds the given {@link DatabaseTableListener} to the observer set
	 *
	 * @param listener The {@link DatabaseTableListener} to add
	 */
	public static void register(DatabaseTableListener listener)
	{
		LISTENERS.add(listener);
	}

	/**
	 * Removes the given {@link DatabaseTableListener} from the observer set
	 *
	 * @param listener The {@link DatabaseTableListener} to remove
	 */
	public static void deregister(DatabaseTableListener listener)
	{
		LISTENERS.remove(listener);
	}

	/**
	 * Notifies all {@link DatabaseTableListener}s in the observer set
	 *
	 * @param table The {@link DatabaseTable} that changed. It will be refreshed ( {@link DatabaseTable#refresh()}) internally before notifying the
	 *              observers.
	 */
	public static void notifyListenersAndRefresh(DatabaseTable table)
	{
		try
		{
			table.refresh();
		}
		catch (DatabaseException e)
		{
			e.printStackTrace();
		}

		for (DatabaseTableListener listener : LISTENERS)
			listener.onDatabaseTableUpdated(table);
	}

	/**
	 * Notifies all {@link DatabaseTableListener}s in the ovserver set
	 *
	 * @param tables The {@link DatabaseTable} that changed.
	 */
	public static void notifyListeners(List<DatabaseTable> tables)
	{
		for (DatabaseTableListener listener : LISTENERS)
			listener.onDatabaseTablesUpdated(tables);
	}

	/**
	 * {@link DatabaseTableListener} is an interface used to register with the {@link DatabaseTableObserver}
	 *
	 * @author Sebastian Raubach
	 */
	public interface DatabaseTableListener
	{
		/**
		 * This method is called whenever {@link DatabaseTableObserver#notifyListenersAndRefresh(DatabaseTable)} is called.
		 *
		 * @param table The {@link DatabaseTable} that changed.
		 */
		void onDatabaseTableUpdated(DatabaseTable table);

		/**
		 * This method is called whenever {@link DatabaseTableObserver#notifyListeners(List)} is called.
		 *
		 * @param tables The {@link DatabaseTable} that changed.
		 */
		void onDatabaseTablesUpdated(List<DatabaseTable> tables);
	}
}
