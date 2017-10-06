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

import org.eclipse.jface.action.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;

import java.util.*;

import jhi.germinatedataimporter.gui.dialog.NumberRangeDialog.*;
import jhi.germinatedataimporter.gui.i18n.*;
import jhi.germinatedataimporter.util.Resources.*;
import jhi.swtcommons.gui.layout.*;
import jhi.swtcommons.gui.viewer.*;

/**
 * {@link NumberRangeTableViewer} extends {@link AdvancedTableViewer} and can be used to show {@link NumberRange} objects. The cells can be modified.
 * This is achieved by using {@link NumberRangeEditingSupport}
 *
 * @author Sebastian Raubach
 */
public class NumberRangeTableViewer extends AdvancedTableViewer<NumberRange>
{

	/**
	 * Creates a new instance of {@link NumberRangeTableViewer} with the given parent {@link Composite} and the given style
	 *
	 * @param parent The parent {@link Composite}
	 * @param style  The style
	 */
	public NumberRangeTableViewer(Composite parent, int style)
	{
		super(parent, style);

		createColumns();

		setContentProvider(ArrayContentProvider.getInstance());

		final Table table = getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

        /* Add a context menu */
		MenuManager menuMgr = new MenuManager();
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(manager -> {
			if (getSelectedItem() == null)
				return;

			manager.add(new Action()
			{
				@Override
				public String getText()
				{
					return RB.getString(RB.TOOLTIP_GENERAL_DELETE);
				}

				@Override
				public void run()
				{
					deleteItem();
				}
			});
		});
		Menu menu = menuMgr.createContextMenu(getControl());
		getControl().setMenu(menu);

        /* Now add some buttons */
		Composite buttonBar = new Composite(parent, SWT.NONE);

		Button add = new Button(buttonBar, SWT.PUSH);
		add.setToolTipText(RB.getString(RB.TOOLTIP_GENERAL_ADD));
		add.setImage(Images.ADD);
		add.addListener(SWT.Selection, event -> addItem());

		Button delete = new Button(buttonBar, SWT.PUSH);
		delete.setToolTipText(RB.getString(RB.TOOLTIP_GENERAL_DELETE));
		delete.setImage(Images.DELETE);
		delete.addListener(SWT.Selection, event -> deleteItem());

		GridLayoutUtils.useValues(2, true).marginHeight(0).marginWidth(0).applyTo(buttonBar);
		GridDataUtils.usePredefined(GridDataUtils.GridDataStyle.BEGINNING_CENTER_FALSE).applyTo(buttonBar);
	}

	/**
	 * Adds a new empty {@link NumberRange} to the model
	 */
	private void addItem()
	{
		@SuppressWarnings("unchecked")
		Collection<NumberRange> range = (Collection<NumberRange>) getInput();

		if (range == null)
			range = new ArrayList<>();

		range.add(new NumberRange(0, 0));
		setInput(range);
	}

	/**
	 * Deletes the currently selected item. If none is selected, this method does nothing
	 *
	 * @see #getSelectedItem()
	 */
	private void deleteItem()
	{
		NumberRange selection = getSelectedItem();

		if (selection != null)
		{
			@SuppressWarnings("unchecked")
			Collection<NumberRange> range = (Collection<NumberRange>) getInput();
			range.remove(selection);
			setInput(range);
		}
	}

	/**
	 * Creates the two columns for {@link NumberRange#min} and {@link NumberRange#max}. Both columns support editing via {@link
	 * NumberRangeEditingSupport}.
	 *
	 * @see NumberRangeEditingSupport
	 */
	private void createColumns()
	{
		TableViewerColumn column = new TableViewerColumn(this, SWT.NONE);
		column.getColumn().setWidth(143);
		column.getColumn().setText(RB.getString(RB.DIALOG_NUMBER_RANGE_MIN));
		column.getColumn().setResizable(false);
		column.setLabelProvider(new ColumnLabelProvider()
		{
			@Override
			public String getText(Object element)
			{
				NumberRange range = (NumberRange) element;
				return Double.toString(range.min);
			}
		});
		column.setEditingSupport(new NumberRangeEditingSupport(this, true));

		column = new TableViewerColumn(this, SWT.NONE);
		column.getColumn().setWidth(143);
		column.getColumn().setText(RB.getString(RB.DIALOG_NUMBER_RANGE_MAX));
		column.getColumn().setResizable(false);
		column.setLabelProvider(new ColumnLabelProvider()
		{
			@Override
			public String getText(Object element)
			{
				NumberRange range = (NumberRange) element;
				return Double.toString(range.max);
			}
		});
		column.setEditingSupport(new NumberRangeEditingSupport(this, false));
	}
}
