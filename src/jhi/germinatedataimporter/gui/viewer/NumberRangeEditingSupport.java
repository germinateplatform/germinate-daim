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

import jhi.germinatedataimporter.gui.dialog.NumberRangeDialog.*;

public class NumberRangeEditingSupport extends EditingSupport
{

	private TableViewer viewer;
	private CellEditor  editor;
	private boolean     isMin;

	public NumberRangeEditingSupport(TableViewer viewer, boolean isMin)
	{
		super(viewer);
		this.viewer = viewer;
		this.isMin = isMin;
		this.editor = new TextCellEditor(viewer.getTable());
	}

	@Override
	protected CellEditor getCellEditor(Object element)
	{
		return editor;
	}

	@Override
	protected boolean canEdit(Object element)
	{
		return true;
	}

	@Override
	protected Object getValue(Object element)
	{
		NumberRange range = (NumberRange) element;
		return Double.toString(isMin ? range.min : range.max);
	}

	@Override
	protected void setValue(Object element, Object userInputValue)
	{
		NumberRange range = (NumberRange) element;
		try
		{
			double value = Double.parseDouble((String) userInputValue);
			if (isMin)
				range.min = value;
			else
				range.max = value;

			viewer.update(element, null);
		}
		catch (NumberFormatException e)
		{
			/* Do nothing here */
		}
	}
}
