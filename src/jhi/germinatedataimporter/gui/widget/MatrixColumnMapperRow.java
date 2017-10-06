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

import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.window.*;
import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;

import java.util.regex.*;

import jhi.database.shared.exception.*;
import jhi.germinatedataimporter.database.entities.*;
import jhi.germinatedataimporter.database.entities.DatabaseColumn.*;
import jhi.germinatedataimporter.gui.dialog.*;
import jhi.germinatedataimporter.gui.i18n.*;
import jhi.germinatedataimporter.gui.viewer.*;
import jhi.germinatedataimporter.util.*;
import jhi.swtcommons.gui.layout.*;
import jhi.swtcommons.util.*;

/**
 * {@link MatrixColumnMapperRow} represents a single row in the {@link MatrixColumnMapper}, i.e. one mapping between a file column and a database
 * column
 *
 * @author Sebastian Raubach
 */
public class MatrixColumnMapperRow extends ColumnMapperRow
{
	protected String                    manualEntryValue;
	protected Condition                 condition;
	private   Button                    delete;
	private   Button                    manualEntry;
	private   MatrixSourcElementViewer  columnsInput;
	private   Button                    regex;
	private   DatabaseColumnComboViewer columnsDatabase;
	private   Button                    setCondition;
	private   DatabaseTable             table;
	private   String                    regexOption;

	private boolean canBeDeleted = true;

	/**
	 * Creates a new instance of {@link MatrixColumnMapperRow}
	 *
	 * @param parent The parent
	 * @param style  The style bits
	 * @param mapper The parent {@link MatrixColumnMapper}
	 */
	public MatrixColumnMapperRow(Composite parent, int style, final MatrixColumnMapper mapper)
	{
		super(parent, style);

        /* Button to define valid input range */
		manualEntry = new Button(this, SWT.PUSH);
		manualEntry.setImage(Resources.Images.MANUAL_ENTRY);
		manualEntry.setToolTipText(RB.getString(RB.TOOLTIP_MAPPING_MANUAL_RANGE));
		manualEntry.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		manualEntry.addListener(SWT.Selection, e -> {
			if (canBeDeleted || MatrixSourceElement.VALUE.equals(columnsInput.getSelectedItem()))
			{
				InputDialog dialog = new InputDialog(getShell(), RB.getString(RB.DIALOG_MANUAL_ENTRY_TITLE), RB.getString(RB.DIALOG_MANUAL_ENTRY_MESSAGE),
						StringUtils.isEmpty(manualEntryValue) ? "" : manualEntryValue, null);

				if (Window.OK == dialog.open())
				{
					regexOption = null;

					manualEntryValue = dialog.getValue();

					if (StringUtils.isEmpty(manualEntryValue))
					{
						if (canBeDeleted)
							columnsInput.getCombo().setEnabled(true);
						regex.setEnabled(true);
						manualEntryValue = null;
					}
					else
					{
						regex.setEnabled(false);
						if (canBeDeleted)
						{
							columnsInput.setSelection(null);
							columnsInput.getCombo().setEnabled(false);
						}
					}

					manualEntryValue = dialog.getValue();
					manualEntry.setEnabled(true);
				}
			}
		});

		regex = new Button(this, SWT.PUSH);
		regex.setImage(Resources.Images.REGEX);
		regex.setToolTipText(RB.getString(RB.TOOLTIP_MAPPING_REGEX));
		regex.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		regex.addListener(SWT.Selection, event -> {
			if (canBeDeleted || MatrixSourceElement.VALUE.equals(columnsInput.getSelectedItem()))
			{
				InputDialog dialog = new InputDialog(getShell(), RB.getString(RB.DIALOG_REGEX_TITLE), RB.getString(RB.DIALOG_REGEX_MESSAGE), StringUtils.isEmpty(regexOption) ? "" : regexOption,
						null);

				if (Window.OK == dialog.open())
				{
					manualEntryValue = null;

					regexOption = dialog.getValue();

					if (StringUtils.isEmpty(regexOption))
					{
						manualEntry.setEnabled(true);
						regexOption = null;
					}
					else
					{
						try
						{
							Pattern.compile(regexOption);

							manualEntry.setEnabled(false);
						}
						catch (PatternSyntaxException e)
						{
							DialogUtils.showError(RB.getString(RB.ERROR_MAPPING_INVALID_REGEX));
							regex = null;
							manualEntry.setEnabled(true);
						}
					}

					if (canBeDeleted)
						columnsInput.getCombo().setEnabled(true);
				}
			}
		});

		columnsInput = new MatrixSourcElementViewer(this, SWT.READ_ONLY);
		columnsInput.addSelectionChangedListener(event -> {
			MatrixSourceElement element = columnsInput.getSelectedItem();

			if (MatrixSourceElement.VALUE.equals(element))
			{
				regex.setEnabled(true);
				manualEntry.setEnabled(true);
			}
			else
			{
				regex.setEnabled(false);
				manualEntry.setEnabled(false);
				regexOption = null;
				manualEntryValue = null;
			}
		});

        /* Add the arrow */
		new MappingArrow(this, SWT.CENTER);

		columnsDatabase = new DatabaseColumnComboViewer(this, SWT.READ_ONLY);
		columnsDatabase.addSelectionChangedListener(e -> {
			DatabaseColumn col = columnsDatabase.getSelectedItem();
			setCondition.setImage(col.isForeignKey() ? Resources.Images.WARNING : Resources.Images.KEY);

			if (col.isForeignKey())
			{
				try
				{
					DatabaseTable foreignTable = DatabaseTable.getTableWithName(col.getForeignKeyTable());
					DatabaseColumn foreignColumn = DatabaseColumn.getColumnWithName(foreignTable, col.getForeignKeyColumn());

					if (foreignTable != null && foreignColumn != null)
					{
						condition = new Condition(foreignTable, foreignColumn);
						setCondition.setImage(Resources.Images.OK);
					}
					else
					{
						condition = null;
					}
				}
				catch (DatabaseException ex)
				{
					/* Reset the condition here */
					condition = null;
				}
			}
			else
			{
				condition = null;
			}

			/* Re-enable controls that might have been disabled before */
			setCondition.setEnabled(true);
			if (canBeDeleted)
			{
				columnsInput.getCombo().setEnabled(true);
				manualEntry.setEnabled(true);
				regex.setEnabled(true);
			}

			manualEntryValue = null;
		});

        /* Button to set foreign key constraints */
		setCondition = new Button(this, SWT.PUSH);
		setCondition.setImage(Resources.Images.KEY);
		setCondition.setToolTipText(RB.getString(RB.TOOLTIP_MAPPING_CONDITION));
		setCondition.setEnabled(false);
		setCondition.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		setCondition.addListener(SWT.Selection, e -> {
			DatabaseColumn col = columnsDatabase.getSelectedItem();

			ConditionDialog dialog = new ConditionDialog(getShell(), table, col, condition);

			if (Window.OK == dialog.open())
			{
				condition = dialog.getCondition();
				if (condition == null)
					setCondition.setImage(col.isForeignKey() ? Resources.Images.WARNING : Resources.Images.KEY);
				else
					setCondition.setImage(Resources.Images.OK);
			}
		});

		delete = new Button(this, SWT.PUSH);
		delete.setImage(Resources.Images.DELETE);
		delete.setToolTipText(RB.getString(RB.TOOLTIP_GENERAL_DELETE));
		delete.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));

        /* Apply layouts */
		GridLayoutUtils.useValues(8, false).applyTo(this);
		GridDataUtils.usePredefined(GridDataUtils.GridDataStyle.FILL_TOP).minimumWidth(400).applyTo(this);
		GridDataUtils.usePredefined(GridDataUtils.GridDataStyle.FILL_CENTER).applyTo(columnsInput.getCombo());
		GridDataUtils.usePredefined(GridDataUtils.GridDataStyle.FILL_CENTER).applyTo(columnsDatabase.getCombo());
		GridDataUtils.usePredefined(GridDataUtils.GridDataStyle.BEGINNING_CENTER_FALSE).applyTo(regex);
		GridDataUtils.usePredefined(GridDataUtils.GridDataStyle.BEGINNING_CENTER_FALSE).applyTo(manualEntry);
		GridDataUtils.usePredefined(GridDataUtils.GridDataStyle.END_CENTER_FALSE).applyTo(setCondition);
		GridDataUtils.usePredefined(GridDataUtils.GridDataStyle.END_CENTER_FALSE).applyTo(delete);

        /* Listen for delete events */
		delete.addListener(SWT.Selection, e -> {
			if (canBeDeleted)
			{
				MatrixColumnMapperRow.this.dispose();
				mapper.onDelete(MatrixColumnMapperRow.this);
			}
		});
	}

	public void setTable(DatabaseTable table)
	{
		this.table = table;
		columnsDatabase.setTable(table);
	}

	public void setCanBeDeleted(boolean canBeDeleted)
	{
		this.canBeDeleted = canBeDeleted;
		delete.setEnabled(canBeDeleted);
		manualEntry.setEnabled(canBeDeleted);
		regex.setEnabled(canBeDeleted);
		columnsInput.getCombo().setEnabled(canBeDeleted);
	}

	public void setType(MatrixSourceElement type)
	{
		if (type != null)
			columnsInput.setSelection(new StructuredSelection(type));
	}

	/**
	 * Creates a {@link ColumnMapperRowDTO} from this {@link SimpleColumnMapperRow}
	 *
	 * @return The {@link ColumnMapperRowDTO} from this {@link SimpleColumnMapperRow}
	 */
	@SuppressWarnings("unchecked")
	public MatrixColumnMapperRowDTO getDTO()
	{
		return new MatrixColumnMapperRowDTO(getMatrixSourceElement(), getRegex(), getColumnDatabase(), getCondition(), getManualEntry());
	}

	public Condition getCondition()
	{
		return condition;
	}

	public String getRegex()
	{
		return regexOption;
	}

	public MatrixSourceElement getMatrixSourceElement()
	{
		return columnsInput.getSelectedItem();
	}

	public DatabaseColumn getColumnDatabase()
	{
		return columnsDatabase.getSelectedItem();
	}

	public String getManualEntry()
	{
		return manualEntryValue;
	}

	public class MatrixColumnMapperRowDTO extends ColumnMapperRowDTO
	{
		public final Pattern             regex;
		public final DatabaseColumn      columnDatabase;
		public final Condition           keyCondition;
		public final String              manualEntry;
		public       MatrixSourceElement element;

		public MatrixColumnMapperRowDTO(MatrixSourceElement element, String regex, DatabaseColumn columnDatabase, Condition keyCondition, String manualEntry)
		{
			this.element = element;
			if (!StringUtils.isEmpty(regex))
				this.regex = Pattern.compile(regex);
			else
				this.regex = null;
			this.columnDatabase = columnDatabase;
			this.keyCondition = keyCondition;
			this.manualEntry = manualEntry;
		}
	}
}
