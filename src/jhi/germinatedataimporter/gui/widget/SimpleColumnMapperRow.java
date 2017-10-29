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

package jhi.germinatedataimporter.gui.widget;

import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.window.*;
import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;

import java.util.*;

import jhi.database.shared.exception.*;
import jhi.germinatedataimporter.database.entities.*;
import jhi.germinatedataimporter.database.entities.DatabaseColumn.*;
import jhi.germinatedataimporter.gui.dialog.*;
import jhi.germinatedataimporter.gui.dialog.DateOptionsDialog.*;
import jhi.germinatedataimporter.gui.dialog.NumberRangeDialog.*;
import jhi.germinatedataimporter.gui.i18n.*;
import jhi.germinatedataimporter.gui.viewer.*;
import jhi.germinatedataimporter.util.*;
import jhi.swtcommons.gui.layout.*;
import jhi.swtcommons.gui.viewer.*;
import jhi.swtcommons.util.*;

/**
 * {@link SimpleColumnMapperRow} represents a single row in the {@link SimpleColumnMapper}, i.e. one mapping between a file column and a database
 * column
 *
 * @author Sebastian Raubach
 */
public class SimpleColumnMapperRow extends ColumnMapperRow
{
	private AdvancedComboViewer<String> columnsFile;
	private DatabaseColumnComboViewer   columnsDatabase;

	private Button chooseDate;
	private Button chooseRange;
	private Button manualEntry;
	private Button setCondition;

	private DateOption              dateOption;
	private Collection<NumberRange> numberRanges;
	private String                  manualEntryValue;
	private Condition               condition;
	private DatabaseTable           table;

	/**
	 * Creates a new instance of {@link SimpleColumnMapperRow}
	 *
	 * @param parent The parent
	 * @param style  The style bits
	 * @param mapper The parent {@link SimpleColumnMapper}
	 */
	public SimpleColumnMapperRow(Composite parent, int style, final SimpleColumnMapper mapper)
	{
		super(parent, style);

        /* Button to choose a date option */
		chooseDate = new Button(this, SWT.PUSH);
		chooseDate.setImage(Resources.Images.DATE);
		chooseDate.setToolTipText(RB.getString(RB.TOOLTIP_MAPPING_DATE));
		chooseDate.setEnabled(false);
		chooseDate.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		chooseDate.addListener(SWT.Selection, e -> {
			DateOptionsDialog dialog = new DateOptionsDialog(getShell(), dateOption);

			if (Window.OK == dialog.open())
			{
				setDateOption(dialog.getDateOption());
			}
		});

        /* Button to define valid input range */
		chooseRange = new Button(this, SWT.PUSH);
		chooseRange.setImage(Resources.Images.NUMBER_RANGE);
		chooseRange.setToolTipText(RB.getString(RB.TOOLTIP_MAPPING_RANGE));
		chooseRange.setEnabled(false);
		chooseRange.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		chooseRange.addListener(SWT.Selection, e -> {
			NumberRangeDialog dialog = new NumberRangeDialog(getShell(), numberRanges);

			if (Window.OK == dialog.open())
			{
				dateOption = null;
				manualEntryValue = null;

				numberRanges = dialog.getNumberRange();
			}
		});

        /* Button to define valid input range */
		manualEntry = new Button(this, SWT.PUSH);
		manualEntry.setImage(Resources.Images.MANUAL_ENTRY);
		manualEntry.setToolTipText(RB.getString(RB.TOOLTIP_MAPPING_MANUAL_RANGE));
		manualEntry.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		manualEntry.addListener(SWT.Selection, e -> {
			InputDialog dialog = new InputDialog(getShell(), RB.getString(RB.DIALOG_MANUAL_ENTRY_TITLE), RB.getString(RB.DIALOG_MANUAL_ENTRY_MESSAGE), StringUtils.isEmpty(manualEntryValue) ? ""
					: manualEntryValue, null);

			if (Window.OK == dialog.open())
			{
				setManualEntryValue(dialog.getValue());
			}
		});

        /* Create the widgets */
		columnsFile = new AdvancedComboViewer<String>(this, SWT.READ_ONLY)
		{
			@Override
			protected String getDisplayText(String item)
			{
				return item;
			}
		};
		columnsFile.addSelectionChangedListener(e -> {
			/* Try to select the same item in the other combo */
			columnsDatabase.selectItem(getColumnFile());
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
			chooseDate.setEnabled(col.isDate());
			chooseRange.setEnabled(col.isDecimal());
			columnsFile.getCombo().setEnabled(true);
			manualEntry.setEnabled(!columnsDatabase.getSelectedItem().isDate());

			dateOption = null;
			numberRanges = null;
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

		Button delete = new Button(this, SWT.PUSH);
		delete.setImage(Resources.Images.DELETE);
		delete.setToolTipText(RB.getString(RB.TOOLTIP_GENERAL_DELETE));
		delete.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));

        /* Apply layouts */
		GridLayoutUtils.useValues(8, false).applyTo(this);
		GridDataUtils.usePredefined(GridDataUtils.GridDataStyle.FILL_TOP).minimumWidth(400).applyTo(this);
		GridDataUtils.usePredefined(GridDataUtils.GridDataStyle.FILL_CENTER).applyTo(columnsFile.getCombo());
		GridDataUtils.usePredefined(GridDataUtils.GridDataStyle.FILL_CENTER).applyTo(columnsDatabase.getCombo());
		GridDataUtils.usePredefined(GridDataUtils.GridDataStyle.BEGINNING_CENTER_FALSE).applyTo(chooseDate);
		GridDataUtils.usePredefined(GridDataUtils.GridDataStyle.BEGINNING_CENTER_FALSE).applyTo(chooseRange);
		GridDataUtils.usePredefined(GridDataUtils.GridDataStyle.BEGINNING_CENTER_FALSE).applyTo(manualEntry);
		GridDataUtils.usePredefined(GridDataUtils.GridDataStyle.END_CENTER_FALSE).applyTo(setCondition);
		GridDataUtils.usePredefined(GridDataUtils.GridDataStyle.END_CENTER_FALSE).applyTo(delete);

        /* Listen for delete events */
		delete.addListener(SWT.Selection, e -> {
			SimpleColumnMapperRow.this.dispose();
			mapper.onDelete(SimpleColumnMapperRow.this);
		});
	}

	public void setManualEntryValue(String value)
	{
		dateOption = null;
		numberRanges = null;

		manualEntryValue = value;

		if (StringUtils.isEmpty(manualEntryValue))
		{
			manualEntryValue = null;
			columnsFile.getCombo().setEnabled(true);
		}
		else
		{
			columnsFile.setSelection(null);
			columnsFile.getCombo().setEnabled(false);
		}
	}

	/**
	 * Creates a {@link ColumnMapperRowDTO} from this {@link SimpleColumnMapperRow}
	 *
	 * @return The {@link ColumnMapperRowDTO} from this {@link SimpleColumnMapperRow}
	 */

	@SuppressWarnings("unchecked")
	public SimpleColumnMapperRowDTO getDTO()
	{
		return new SimpleColumnMapperRowDTO(getColumnFile(), getColumnDatabase(), getCondition(), getDateOption(), getNumberRanges(), getManualEntry());
	}

	public Condition getCondition()
	{
		return condition;
	}

	public void setCondition(Condition condition)
	{
		this.condition = condition;

		DatabaseColumn col = columnsDatabase.getSelectedItem();

		if (condition == null)
			setCondition.setImage(col.isForeignKey() ? Resources.Images.WARNING : Resources.Images.KEY);
		else
			setCondition.setImage(Resources.Images.OK);
	}

	public DateOption getDateOption()
	{
		return dateOption;
	}

	public void setDateOption(DateOption dateOption)
	{
		this.dateOption = dateOption;

		numberRanges = null;
		manualEntryValue = null;

		/*
		 * If it's an option that doesn't need an input column,
		 * deselect the input column and disable the combo
		 */
		if (dateOption instanceof NowOption || dateOption instanceof CalendarOption)
		{
			columnsFile.setSelection(null);
			columnsFile.getCombo().setEnabled(false);
		}
		else
		{
			columnsFile.getCombo().setEnabled(true);
		}
	}

	public String getColumnFile()
	{
		return columnsFile.getSelectedItem();
	}

	public DatabaseColumn getColumnDatabase()
	{
		return columnsDatabase.getSelectedItem();
	}

	public Collection<NumberRange> getNumberRanges()
	{
		return numberRanges;
	}

	public String getManualEntry()
	{
		return manualEntryValue;
	}

	public void setColumnsFile(Collection<String> items)
	{
		columnsFile.setInput(items);
	}

	public boolean selectColumnFile(String item)
	{
		return columnsFile.selectItem(item);
	}

	public void setTable(DatabaseTable table)
	{
		this.table = table;
		columnsDatabase.setTable(table);
	}

	public boolean selectColumnDatabase(String item)
	{
		return columnsDatabase.selectItem(item);
	}

	/**
	 * Simple DTO class used to store data selected by the user in the {@link SimpleColumnMapperRow}
	 *
	 * @author Sebastian Raubach
	 */
	public class SimpleColumnMapperRowDTO extends ColumnMapperRowDTO
	{
		public final String                  columnFile;
		public final DatabaseColumn          columnDatabase;
		public final Condition               keyCondition;
		public final DateOption              dateOption;
		public final Collection<NumberRange> numberRanges;
		public final String                  manualEntry;
		public int     indexInInsertStatement = -1;
		public int     indexInSelectStatement = -1;
		public int     indexInUpdateStatement = -1;
		public int     indexInSourceFile      = -1;
		public boolean toUpdate               = false;

		public SimpleColumnMapperRowDTO(String columnFile, DatabaseColumn columnDatabase, Condition keyCondition, DateOption dateOption, Collection<NumberRange> numberRanges, String manualEntry)
		{
			this.columnFile = columnFile;
			this.columnDatabase = columnDatabase;
			this.keyCondition = keyCondition;
			this.dateOption = dateOption;
			this.numberRanges = numberRanges;
			this.manualEntry = manualEntry;
		}

		@Override
		public String toString()
		{
			return "SimpleColumnMapperRowDTO{" +
					"columnFile='" + columnFile + '\'' +
					", columnDatabase=" + columnDatabase +
					", keyCondition=" + keyCondition +
					", dateOption=" + dateOption +
					", numberRanges=" + numberRanges +
					", manualEntry='" + manualEntry + '\'' +
					", indexInInsertStatement=" + indexInInsertStatement +
					", indexInSelectStatement=" + indexInSelectStatement +
					", indexInUpdateStatement=" + indexInUpdateStatement +
					", indexInSourceFile=" + indexInSourceFile +
					", toUpdate=" + toUpdate +
					'}';
		}
	}
}
