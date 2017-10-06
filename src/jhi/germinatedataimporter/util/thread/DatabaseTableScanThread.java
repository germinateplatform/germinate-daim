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

import java.util.*;

import jhi.database.shared.exception.*;
import jhi.germinatedataimporter.database.entities.*;
import jhi.germinatedataimporter.gui.*;
import jhi.germinatedataimporter.gui.i18n.*;
import jhi.germinatedataimporter.util.*;

/**
 * {@link DatabaseTableScanThread} scans all {@link DatabaseTable}s and calls their {@link DatabaseTable#getColumns()} and {@link
 * DatabaseTable#refresh()} methods.
 *
 * @author Sebastian Raubach
 */
public class DatabaseTableScanThread extends Thread
{
	private static boolean isRunning = false;
	private static DatabaseTableScanThread INSTANCE;

	private List<DatabaseTable> tables;

	public DatabaseTableScanThread(List<DatabaseTable> tables)
	{
		this.tables = tables;
	}

	public static void stopAll()
	{
		if (INSTANCE != null)
			INSTANCE.interrupt();
	}

	@Override
	public void interrupt()
	{
		super.interrupt();
		isRunning = false;
		GerminateDataImporter.getInstance().setStatusBar(0, 0, RB.getString(RB.STATUS_IDLE));
	}

	@Override
	public void run()
	{
		if (isRunning)
			return;

		isRunning = true;
		INSTANCE = this;

		int counter = 1;
		for (DatabaseTable table : tables)
		{
			if (this.isInterrupted())
				return;

			try
			{
				GerminateDataImporter.getInstance().setStatusBar(counter++, tables.size(), RB.getString(RB.STATUS_UPDATING_TABLE_SIZE, table.getName()));
				table.getColumns();
				table.refresh();
			}
			catch (DatabaseException e)
			{
				e.printStackTrace();
			}
		}

		DatabaseTableObserver.notifyListeners(tables);

		GerminateDataImporter.getInstance().setStatusBar(0, 0, RB.getString(RB.STATUS_IDLE));

		INSTANCE = null;
		isRunning = false;
	}
}
