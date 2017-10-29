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

package jhi.germinatedataimporter.gui.viewer;

import org.eclipse.jface.viewers.*;
import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;

import jhi.germinatedataimporter.database.entities.*;
import jhi.swtcommons.gui.viewer.*;

/**
 * {@link DatabaseColumnComboViewer} extends {@link AdvancedComboViewer} and displays {@link DatabaseColumn}s
 *
 * @author Sebastian Raubach
 */
public class DatabaseColumnComboViewer extends AdvancedComboViewer<DatabaseColumn>
{
	public DatabaseColumnComboViewer(Composite parent, int style)
	{
		super(parent, style | SWT.READ_ONLY);

		this.setLabelProvider(new LabelProvider()
		{
			@Override
			public String getText(Object element)
			{
				if (element instanceof DatabaseColumn)
				{
					return getDisplayText((DatabaseColumn) element);
				}
				else
				{
					return super.getText(element);
				}
			}
		});
	}

	/**
	 * Sets the {@link DatabaseTable} for which this {@link DatabaseColumnComboViewer} displays the {@link DatabaseColumn}s
	 *
	 * @param table The {@link DatabaseTable}
	 */
	public void setTable(DatabaseTable table)
	{
		this.setInput(table.getColumns());
	}

	@Override
	protected String getDisplayText(DatabaseColumn item)
	{
		return item.getName();
	}
}
