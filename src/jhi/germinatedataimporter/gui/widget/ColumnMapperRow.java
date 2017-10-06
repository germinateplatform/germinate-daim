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

package jhi.germinatedataimporter.gui.widget;

import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;

/**
 * {@link ColumnMapperRow} represents a single row in the {@link SimpleColumnMapper}, i.e. one mapping between a file column and a database column
 *
 * @author Sebastian Raubach
 */
public abstract class ColumnMapperRow extends Composite
{
	/**
	 * Creates a new instance of {@link ColumnMapper}
	 *
	 * @param parent The parent
	 * @param style  The style bits
	 */
	public ColumnMapperRow(Composite parent, int style)
	{
		super(parent, style);

		this.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
	}

	protected abstract <T extends ColumnMapperRowDTO> T getDTO();

	/**
	 * Simple DTO class used to store data selected by the user in the {@link ColumnMapperRow}
	 *
	 * @author Sebastian Raubach
	 */
	public abstract class ColumnMapperRowDTO
	{
	}
}
