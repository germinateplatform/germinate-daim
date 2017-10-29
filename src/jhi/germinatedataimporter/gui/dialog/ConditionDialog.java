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

package jhi.germinatedataimporter.gui.dialog;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

import jhi.germinatedataimporter.database.entities.*;
import jhi.germinatedataimporter.database.entities.DatabaseColumn.*;
import jhi.germinatedataimporter.gui.i18n.*;
import jhi.germinatedataimporter.gui.viewer.*;
import jhi.germinatedataimporter.gui.widget.*;
import jhi.germinatedataimporter.util.*;
import jhi.swtcommons.gui.dialog.*;
import jhi.swtcommons.gui.layout.*;
import jhi.swtcommons.util.*;

/**
 * {@link ConditionDialog} is a {@link Dialog} to create {@link Condition}s for {@link DatabaseColumn}s
 *
 * @author Sebastian Raubach
 */
public class ConditionDialog extends I18nDialog
{
	private Condition                 condition;
	private DatabaseTable             table;
	private DatabaseColumn            column;
	private DatabaseTableComboViewer  tables;
	private DatabaseColumnComboViewer columns;
	private CheckBoxGroup             group;
	private List                      examples;

	public ConditionDialog(Shell parentShell, DatabaseTable table, DatabaseColumn column, Condition previousCondition)
	{
		super(parentShell);
		setBlockOnOpen(true);

		this.table = table;
		this.column = column;
		this.condition = previousCondition;
	}

	@Override
	protected Control createDialogArea(Composite parent)
	{
		final Composite composite = (Composite) super.createDialogArea(parent);
		GridLayoutUtils.useValues(2, false).applyTo(composite);
		GridDataUtils.usePredefined(GridDataUtils.GridDataStyle.FILL_BOTH).widthHint(300 * Resources.getZoomFactor() / 100).applyTo(composite);

		group = new CheckBoxGroup(composite, SWT.NONE);
		group.setText(RB.getString(RB.DIALOG_CONDITION_USE));
		GridLayoutUtils.useDefault().applyTo(group);
		GridDataUtils.usePredefined(GridDataUtils.GridDataStyle.FILL_BOTH).applyTo(group);

		Label warning = new Label(group.getContent(), SWT.WRAP);
		warning.setText(RB.getString(RB.DIALOG_CONDITION_SELECT_FIRST));

		Composite content = new Composite(group.getContent(), SWT.NONE);
		GridLayoutUtils.useValues(2, false).applyTo(content);

		new Label(content, SWT.NONE).setText(RB.getString(RB.DIALOG_CONDITION_TABLE));

		tables = new DatabaseTableComboViewer(content, SWT.READ_ONLY);
		tables.setDatabase((DatabaseDatabase) GerminateParameterStore.getInstance().get(GerminateParameter.database));
		tables.addSelectionChangedListener(e -> {
			columns.setInput(tables.getSelectedItem().getColumns());
			group.getContent().layout();

			/* Clear the examples, since a new table has been selected */
			examples.removeAll();
		});

		new Label(content, SWT.NONE).setText(RB.getString(RB.DIALOG_CONDITION_COLUMN));

		columns = new DatabaseColumnComboViewer(content, SWT.READ_ONLY);
		columns.addSelectionChangedListener(arg0 -> {
			DatabaseTable table1 = tables.getSelectedItem();
			DatabaseColumn column1 = columns.getSelectedItem();

			showExampleData(table1, column1);
		});

		Label examplesLabel = new Label(content, SWT.NONE);
		examplesLabel.setText(RB.getString(RB.DIALOG_CONDITION_EXAMPLE_DATA));

		examples = new List(content, SWT.BORDER | SWT.V_SCROLL);

		GridDataUtils.usePredefined(GridDataUtils.GridDataStyle.FILL_BOTH).applyTo(content);
		GridDataUtils.usePredefined(GridDataUtils.GridDataStyle.FILL_CENTER).applyTo(warning);
		GridDataUtils.usePredefined(GridDataUtils.GridDataStyle.FILL_CENTER).applyTo(tables.getCombo());
		GridDataUtils.usePredefined(GridDataUtils.GridDataStyle.FILL_CENTER).applyTo(columns.getCombo());
		GridDataUtils.usePredefined(GridDataUtils.GridDataStyle.FILL_TOP).applyTo(examplesLabel);
		GridDataUtils.usePredefined(GridDataUtils.GridDataStyle.FILL_CENTER).applyTo(examples);

		if (condition != null)
			restoreCondition();

		return composite;
	}

	/**
	 * Attempts to get example data from the given {@link DatabaseTable} and {@link DatabaseColumn}. Displays it in {@link #examples}.
	 *
	 * @param table  The currently selected {@link DatabaseTable}
	 * @param column The currently selected {@link DatabaseColumn}
	 */
	private void showExampleData(DatabaseTable table, DatabaseColumn column)
	{
		java.util.List<String> result = table.getExampleDataFromColumn(column);

		if (CollectionUtils.isEmpty(result))
		{
			examples.removeAll();
		}
		else
		{
			String[] items = result.toArray(new String[result.size()]);

			for (int i = 0; i < items.length; i++)
			{
				if (items[i] == null)
					items[i] = "null";
			}

			examples.setItems(items);
		}
	}

	/**
	 * Restores a previously selected {@link Condition}
	 */
	private void restoreCondition()
	{
		if (condition.getTable() != null && condition.getColumn() != null)
		{
			tables.setSelection(new StructuredSelection(condition.getTable()));
			columns.setSelection(new StructuredSelection(condition.getColumn()));
		}
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent)
	{
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

	@Override
	protected void configureShell(Shell shell)
	{
		super.configureShell(shell);
		shell.setText(RB.getString(RB.DIALOG_CONDITION_TITLE, column.getName()));
	}

	@Override
	protected Point getInitialLocation(Point initialSize)
	{
		/* Center the dialog based on the parent */
		return ShellUtils.getLocationCenteredTo(getParentShell(), initialSize);
	}

	@Override
	protected void okPressed()
	{
		if (group.isActivated())
		{
			final DatabaseColumn selectedColumn = columns.getSelectedItem();
			final DatabaseTable selectedTable = tables.getSelectedItem();

			if (selectedColumn == null || selectedTable == null)
			{
				DialogUtils.showError(RB.getString(RB.DIALOG_DB_CRED_ERROR_MESSAGE));
				return;
			}

            /* Check if constraint table and source table are the same */
			if (table.equals(selectedTable))
			{
				DialogUtils.showError(RB.getString(RB.ERROR_CONSTAINT_SAME_TABLE));
				return;
			}

            /* Check if the selected table has data */
			if (selectedTable.getSize() < 1)
			{
				DialogUtils.showError(RB.getString(RB.ERROR_CONSTAINT_EMPTY_REFERENCE_TABLE, selectedTable.getName()));
				return;
			}

            /* Check if the selected column is itself a foreign key column */
			if (selectedColumn.isForeignKey())
			{
				/* If so, ask if the user wants to continue anyway */
				DialogUtils.showQuestion(RB.getString(RB.WARNING_CONDITION_COLUMN_IS_FOREIGN_KEY, selectedColumn.getName()), positive -> {
					if (positive)
					{
						condition = new Condition(selectedTable, selectedColumn);
						ConditionDialog.this.close();
					}
				});
				return;
			}

			condition = new Condition(selectedTable, selectedColumn);
			super.okPressed();
		}
		else
		{
			condition = null;
			super.okPressed();
		}
	}

	/**
	 * Returns the {@link Condition} created from the user's selection
	 *
	 * @return The {@link Condition} created from the user's selection
	 */
	public Condition getCondition()
	{
		return condition;
	}
}
