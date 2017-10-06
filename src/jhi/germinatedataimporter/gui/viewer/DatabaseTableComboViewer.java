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

package jhi.germinatedataimporter.gui.viewer;

import org.eclipse.jface.viewers.*;
import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;

import java.util.List;

import jhi.database.shared.exception.*;
import jhi.germinatedataimporter.database.entities.*;
import jhi.germinatedataimporter.util.*;
import jhi.germinatedataimporter.util.DatabaseTableObserver.*;
import jhi.swtcommons.gui.viewer.*;
import jhi.swtcommons.util.*;

/**
 * {@link DatabaseTableComboViewer} extends {@link AdvancedComboViewer} and displays {@link DatabaseTable}s
 *
 * @author Sebastian Raubach
 */
public class DatabaseTableComboViewer extends AdvancedComboViewer<DatabaseTable> implements DatabaseTableListener
{
	public DatabaseTableComboViewer(Composite parent, int style)
	{
		super(parent, style | SWT.READ_ONLY);

		this.setLabelProvider(new LabelProvider()
		{
			@Override
			public String getText(Object element)
			{
				if (element instanceof DatabaseTable)
				{
					DatabaseTable table = (DatabaseTable) element;
					return table.getName() + " (" + table.getSize() + ")";
				}
				return super.getText(element);
			}
		});

        /* Register as observer */
		DatabaseTableObserver.register(this);

        /* When the combo is disposed, unregister */
		this.getCombo().addListener(SWT.Dispose, event -> DatabaseTableObserver.deregister(DatabaseTableComboViewer.this));
	}

	/**
	 * Sets the database for which this {@link DatabaseTableComboViewer} displays the {@link DatabaseTable}s
	 */
	public void setDatabase(DatabaseDatabase database)
	{
		try
		{
			this.setInput(database.getTables());
		}
		catch (DatabaseException e)
		{
			DialogUtils.handleException(e);
		}
	}

	@Override
	protected String getDisplayText(DatabaseTable item)
	{
		return item.getName();
	}

	@Override
	public void onDatabaseTableUpdated(final DatabaseTable table)
	{
		Display.getDefault().asyncExec(() -> DatabaseTableComboViewer.this.refresh(table, false));
	}

	@Override
	public void onDatabaseTablesUpdated(final List<DatabaseTable> tables)
	{
		Display.getDefault().asyncExec(() -> {
			for (DatabaseTable table : tables)
				DatabaseTableComboViewer.this.refresh(table, false);
		});
	}
}
