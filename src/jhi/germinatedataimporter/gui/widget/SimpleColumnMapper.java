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
import org.eclipse.jface.operation.*;
import org.eclipse.jface.window.*;
import org.eclipse.swt.*;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import org.joda.time.DateTime;

import java.io.*;
import java.lang.reflect.*;
import java.sql.*;
import java.text.*;
import java.util.*;
import java.util.List;
import java.util.stream.*;

import jhi.database.shared.exception.*;
import jhi.germinatedataimporter.database.entities.*;
import jhi.germinatedataimporter.gui.dialog.*;
import jhi.germinatedataimporter.gui.dialog.DateOptionsDialog.*;
import jhi.germinatedataimporter.gui.i18n.*;
import jhi.germinatedataimporter.gui.widget.SimpleColumnMapperRow.*;
import jhi.germinatedataimporter.util.*;
import jhi.germinatedataimporter.util.exception.*;
import jhi.germinatedataimporter.util.thread.*;
import jhi.germinatedataimporter.util.xml.*;
import jhi.swtcommons.gui.layout.*;
import jhi.swtcommons.util.*;

/**
 * {@link SimpleColumnMapper} let's the user pick a mapping between input file columns and database columns.
 *
 * @author Sebastian Raubach
 */
public class SimpleColumnMapper extends ColumnMapper
{
	private State state = State.INSERT;

	/**
	 * Creates a new instance of {@link SimpleColumnMapper} with the given {@link Composite} as its parent
	 *
	 * @param parent The parent
	 */
	public SimpleColumnMapper(Composite parent, CTabItem contentHolder)
	{
		super(parent, contentHolder);
	}

	/**
	 * Starts the import process. Uses a {@link RowDataImportThread} to import the data using the given mapping.
	 *
	 * @param mapping The {@link List} of {@link SimpleColumnMapperRowDTO}s
	 */
	private void startImport(List<SimpleColumnMapperRowDTO> mapping)
	{
		try
		{
			databaseTable = selectedTable;
			IRunnableWithProgress op;

			switch (state)
			{
				case INSERT_EXISTS:
					op = new RowDataInsertExistThread(options, mapping, tableCombo.getSelectedItem())
					{
						@Override
						public Tuple.Pair<Boolean, Boolean> onImportError(Exception e, boolean checkedState)
						{
							return SimpleColumnMapper.this.onImportError(e, checkedState);
						}

						@Override
						public void onImportFailed(List<Long> generatedIds, int updatedIds, Exception e)
						{
							SimpleColumnMapper.this.onImportFailed(generatedIds, updatedIds, e);
						}

						@Override
						public void onImportCancelled(List<Long> generatedIds, int updatedIds)
						{
							SimpleColumnMapper.this.onImportCancelled(generatedIds, updatedIds);
						}

						@Override
						public void onImportFinished(List<Long> generatedIds, int updatedIds)
						{
							SimpleColumnMapper.this.onImportFinished(generatedIds, updatedIds);
						}
					};
					break;
				case UPDATE:
					op = new RowDataUpdateThread(options, mapping, tableCombo.getSelectedItem())
					{
						@Override
						public Tuple.Pair<Boolean, Boolean> onImportError(Exception e, boolean checkedState)
						{
							return SimpleColumnMapper.this.onImportError(e, checkedState);
						}

						@Override
						public void onImportFailed(List<Long> generatedIds, int updatedIds, Exception e)
						{
							SimpleColumnMapper.this.onImportFailed(generatedIds, updatedIds, e);
						}

						@Override
						public void onImportCancelled(List<Long> generatedIds, int updatedIds)
						{
							SimpleColumnMapper.this.onImportCancelled(generatedIds, updatedIds);
						}

						@Override
						public void onImportFinished(List<Long> generatedIds, int updatedIds)
						{
							SimpleColumnMapper.this.onImportFinished(generatedIds, updatedIds);
						}
					};
					break;
				case INSERT:
					op = new RowDataInsertThread(options, mapping, tableCombo.getSelectedItem())
					{
						@Override
						public Tuple.Pair<Boolean, Boolean> onImportError(Exception e, boolean checkedState)
						{
							return SimpleColumnMapper.this.onImportError(e, checkedState);
						}

						@Override
						public void onImportFailed(List<Long> generatedIds, int updatedIds, Exception e)
						{
							SimpleColumnMapper.this.onImportFailed(generatedIds, updatedIds, e);
						}

						@Override
						public void onImportCancelled(List<Long> generatedIds, int updatedIds)
						{
							SimpleColumnMapper.this.onImportCancelled(generatedIds, updatedIds);
						}

						@Override
						public void onImportFinished(List<Long> generatedIds, int updatedIds)
						{
							SimpleColumnMapper.this.onImportFinished(generatedIds, updatedIds);
						}
					};
					break;
				default:
					return;
			}

            /* Start the progress dialog */
			new ProgressMonitorDialog(content.getShell()).run(true, true, op);
		}
		catch (InvocationTargetException | InterruptedException e)
		{
			DialogUtils.handleException(e);
		}
	}

	private void onImportFinished(List<Long> generatedIds, int updatedIds)
	{
		setGeneratedIds(databaseTable, generatedIds);

		/* Notify the user that the import has finished successfully */
		if (generatedIds.size() == 1)
			DialogUtils.showInformation(RB.getString(RB.INFORMATION_IMPORT_SUCCESS_ONE, generatedIds.size(), updatedIds));
		else
			DialogUtils.showInformation(RB.getString(RB.INFORMATION_IMPORT_SUCCESS_MANY, generatedIds.size(), updatedIds));

		DatabaseTableObserver.notifyListenersAndRefresh(selectedTable);
	}

	private void onImportCancelled(List<Long> generatedIds, int updatedIds)
	{
		/*
		 * Ask the user if the inserted items should be deleted
		 * again
		 */
		DialogUtils.showQuestion(RB.getString(RB.ERROR_IMPORT_CANCELLED, generatedIds.size()), result -> {
			if (result)
			{
				deleteItems(databaseTable, generatedIds);
			}
			else
			{
				setGeneratedIds(databaseTable, generatedIds);
			}
		});
	}

	private void onImportFailed(List<Long> generatedIds, int updatedIds, Exception e)
	{
		String message = "";

		if (e instanceof ParseException)
		{
			message = RB.getString(RB.ERROR_IMPORT_PARSE_EXCEPTION, e.getLocalizedMessage());
		}
		else if (e instanceof DatabaseException)
		{
//			String m = DatabaseExceptionUtils.getMessageFromException((SQLException) e);

			message = RB.getString(RB.ERROR_IMPORT_DATABASE_EXCEPTION, "");
		}
		else if (e instanceof IOException)
		{
			message = RB.getString(RB.ERROR_IMPORT_IO_EXCEPTION, e.getLocalizedMessage());
		}
		else if (e instanceof NumberFormatException)
		{
			message = RB.getString(RB.ERROR_IMPORT_NUMBER_FORMAT_EXCEPTION, e.getLocalizedMessage());
		}

		/* If nothing has been inserted into the database yet */
		if (CollectionUtils.isEmpty(generatedIds))
		{
			/* Just show the error */
			DialogUtils.showError(message);
		}
		else
		{
			/* Else ask the user if they should be deleted again */
			DialogUtils.showQuestion(message + RB.getString(RB.ERROR_IMPORT_ASK_FOR_UNDO, generatedIds.size()), result -> {
				if (result)
				{
					deleteItems(databaseTable, generatedIds);
				}
				else
				{
					setGeneratedIds(databaseTable, generatedIds);
				}
			});
		}
	}

	private Tuple.Pair<Boolean, Boolean> onImportError(Exception e, boolean checkedState)
	{
		String message = e.getLocalizedMessage();
		if (e.getCause() instanceof SQLException)
			message = DatabaseExceptionUtils.getMessageFromException((SQLException) e.getCause());

		return DialogUtils.showQuestionToggle(RB.getString(RB.ERROR_IMPORT_ASK_FOR_CONTINUE, message), RB.getString(RB.ERROR_IMPORT_ASK_FOR_CONTINUE_TOGGLE), checkedState);
	}

	/**
	 * Checks if all the conditions are fulfilled
	 *
	 * @throws MissingConstraintException  Thrown if a selected {@link DatabaseColumn} doesn't have a valid constraint defined
	 * @throws DatabaseException           Thrown if the database communication fails
	 * @throws MissingColumnException      Thrown if the user forgot to map a {@link DatabaseColumn} that CANNOT be null
	 * @throws DuplicateColumnException    Thrown if the user selected a {@link DatabaseColumn} at least twice
	 * @throws MissingInputColumnException Thrown if a {@link DatabaseColumn} doesn't have an input column mapped to it
	 * @throws IllegalArgumentException    Thrown if either there are no {@link SimpleColumnMapperRow}s OR one of the mapping elements is
	 *                                     <code>null</code> or empty.
	 */
	private void checkConditions(boolean isUpdate) throws MissingConstraintException, DatabaseException, MissingColumnException, DuplicateColumnException, MissingInputColumnException
	{
		if (rows == null || rows.size() < 1)
			throw new IllegalArgumentException();

		for (ColumnMapperRow rowAbstr : rows)
		{
			SimpleColumnMapperRow row = (SimpleColumnMapperRow) rowAbstr;
			if (row.getColumnDatabase() == null)
				throw new IllegalArgumentException();
		}

		if (!isUpdate)
		{
			/* Check if all columns that have to be set are set */
			for (DatabaseColumn col : selectedTable.getColumns())
			{
				if (!col.getCanBeNull())
				{
					boolean found = false;
					for (ColumnMapperRow rowAbstr : rows)
					{
						SimpleColumnMapperRow row = (SimpleColumnMapperRow) rowAbstr;
						if (row.getColumnDatabase().equals(col))
						{
							found = true;
							break;
						}
					}

					if (!found)
						throw new MissingColumnException(col);
				}
			}
		}

		Set<DatabaseColumn> selectedDatabaseColumns = new HashSet<>();

		for (ColumnMapperRow rowAbstr : rows)
		{
			SimpleColumnMapperRow row = (SimpleColumnMapperRow) rowAbstr;
			String file = row.getColumnFile();
			DatabaseColumn database = row.getColumnDatabase();

            /* Check for duplicates */
			if (selectedDatabaseColumns.contains(database))
				throw new DuplicateColumnException(database);

			selectedDatabaseColumns.add(database);

			if (StringUtils.isEmpty(file) && (row.getDateOption() instanceof CalendarOption || row.getDateOption() instanceof NowOption || !StringUtils.isEmpty(row.getManualEntry())))
			{
				/*
				 * If we're either using NOW() or a specified date or a manual
                 * entry, we don't need an input column
                 */
			}
			else if (StringUtils.isEmpty(file))
			{
				throw new MissingInputColumnException(database);
			}
			else if (database.isForeignKey() && row.getCondition() == null)
			{
				throw new MissingConstraintException(database);
			}
		}
	}

	protected void insert()
	{
		Display.getDefault().asyncExec(() -> {
			MessageDialog dialog = new MessageDialog(content.getShell(),
					RB.getString(RB.DIALOG_IMPORT_EXISTS_TITLE),
					null,
					RB.getString(RB.DIALOG_IMPORT_EXISTS_MESSAGE),
					MessageDialog.QUESTION,
					new String[]{RB.getStringInternal(jhi.swtcommons.gui.i18n.RB.WINDOW_BUTTON_YES), RB.getStringInternal(jhi.swtcommons.gui.i18n.RB.WINDOW_BUTTON_NO)},
					1);

			if (dialog.open() == Window.OK)
			{
				List<SimpleColumnMapperRowDTO> mapping = getMapping();
				UpdateRowSelectionDialog d = new UpdateRowSelectionDialog(content.getShell(), RB.getString(RB.DIALOG_INSERT_EXISTS_SELECTION_TITLE), RB.getString(RB.DIALOG_INSERT_EXISTS_SELECTION_MESSAGE), mapping);

				if (d.open() == Window.OK)
				{
					state = State.INSERT_EXISTS;
					run(mapping);
				}
			}
			else
			{
				state = State.INSERT;
				run(getMapping());
			}
		});
	}

	private void run(List<SimpleColumnMapperRowDTO> mapping)
	{
		try
		{
			checkConditions(state == State.UPDATE);

			startImport(mapping);
		}
		catch (IllegalArgumentException e1)
		{
			DialogUtils.showError(RB.getString(RB.ERROR_MAPPING_INVALID_SELECTION));
		}
		catch (MissingInputColumnException e1)
		{
			DialogUtils.showError(RB.getString(RB.ERROR_MAPPING_MISSING_INPUT_COLUMN, e1.getColumn().getName()));
		}
		catch (MissingConstraintException e1)
		{
			DialogUtils.showError(RB.getString(RB.ERROR_MAPPING_MISSING_KEY_CONSTAINT, e1.getColumn().getName()));
		}
		catch (MissingColumnException e1)
		{
			DialogUtils.showError(RB.getString(RB.ERROR_MAPPING_MISSING_COLUMN, e1.getColumn().getName()));
		}
		catch (DuplicateColumnException e1)
		{
			DialogUtils.showError(RB.getString(RB.ERROR_MAPPING_DUPLICATE_COLUMN, e1.getColumn().getName()));
		}
		catch (DatabaseException e1)
		{
			DialogUtils.handleException(e1);
		}
	}

	/**
	 * Sets up the content
	 */
	protected void setUpContent()
	{
		if (dynamicContent != null && !dynamicContent.isDisposed())
			dynamicContent.dispose();

		rows.forEach(ColumnMapperRow::dispose);

		rows.clear();

		run.setEnabled(true);
		update.setEnabled(true);

		dynamicContent = new Composite(content, SWT.NONE);

		GridLayoutUtils.useValues(1, false).applyTo(dynamicContent);
		GridDataUtils.usePredefined(GridDataUtils.GridDataStyle.FILL_BOTH).horizontalSpan(5).applyTo(dynamicContent);

		Button add = new Button(dynamicContent, SWT.PUSH);
		add.setImage(Resources.Images.ADD);
		add.setToolTipText(RB.getString(RB.TOOLTIP_GENERAL_ADD));

		wrapper = new ScrolledComposite(dynamicContent, SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
		GridLayoutUtils.useValues(1, false).applyTo(wrapper);
		GridDataUtils.usePredefined(GridDataUtils.GridDataStyle.FILL_BOTH).horizontalSpan(2).applyTo(wrapper);

		rowContainer = new Composite(wrapper, SWT.NONE);
		rowContainer.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		GridLayoutUtils.useValues(1, false).applyTo(rowContainer);
		GridDataUtils.usePredefined(GridDataUtils.GridDataStyle.FILL_BOTH).applyTo(rowContainer);

		SimpleColumnMapperRow row = new SimpleColumnMapperRow(rowContainer, SWT.NONE, this);
		row.setTable(selectedTable);
		row.setColumnsFile(columnsFile);
		rows.add(row);

		wrapper.setContent(rowContainer);
		wrapper.setExpandHorizontal(true);
		wrapper.setExpandVertical(true);
		wrapper.setMinSize(rowContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT));

        /* Add a new row */
		add.addListener(SWT.Selection, e -> {
			SimpleColumnMapperRow row1 = new SimpleColumnMapperRow(rowContainer, SWT.NONE, SimpleColumnMapper.this);
			row1.setTable(SimpleColumnMapper.this.selectedTable);
			row1.setColumnsFile(SimpleColumnMapper.this.columnsFile);
			rows.add(row1);
			dynamicContent.layout(true, true);

			Point targetSize = rowContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT);

			wrapper.setMinSize(targetSize);
			wrapper.setOrigin(0, targetSize.y);
		});

		content.layout();
	}

	/**
	 * Returns the mapping between file columns and database columns
	 *
	 * @return The mapping between file columns and database columns
	 */
	private List<SimpleColumnMapperRowDTO> getMapping()
	{
		return rows.stream()
				   .map(row -> ((SimpleColumnMapperRow) row).getDTO())
				   .collect(Collectors.toList());
	}

	@Override
	protected void update()
	{
		state = State.UPDATE;

		DialogUtils.showQuestion(RB.getString(RB.QUESTION_CONFIRM_UPDATE), result -> {
			if (result)
			{
				List<SimpleColumnMapperRowDTO> mapping = getMapping();
				UpdateRowSelectionDialog dialog = new UpdateRowSelectionDialog(content.getShell(), RB.getString(RB.DIALOG_UPDATE_ROW_SELECTION_TITLE), RB.getString(RB.DIALOG_UPDATE_ROW_SELECTION_MESSAGE), mapping);

				if (dialog.open() == Window.OK)
				{
					run(mapping);
				}
			}
		});
	}

	@Override
	protected boolean supportsUpdate()
	{
		return true;
	}

	public void importFromXml(File file)
	{
		IRunnableWithProgress progress = new XmlReaderThread(file, this);

		/* Start the progress dialog */
		try
		{
			new ProgressMonitorDialog(content.getShell()).run(true, true, progress);
		}
		catch (InvocationTargetException | InterruptedException ex)
		{
			DialogUtils.handleException(ex);
		}
	}

	public void exportToXml(File file)
	{
		IRunnableWithProgress progress = new XmlWriterThread(file, getInputMapping());

		/* Start the progress dialog */
		try
		{
			new ProgressMonitorDialog(content.getShell()).run(true, true, progress);
		}
		catch (InvocationTargetException | InterruptedException ex)
		{
			DialogUtils.handleException(ex);
		}
	}

	private InputMapping getInputMapping()
	{
		InputMapping mapping = new InputMapping();
		mapping.setInputFile(selectedFile.getAbsolutePath());
		mapping.setTargetTable(selectedTable.getName());

		for (ColumnMapperRow row : rows)
		{
			try
			{
				SimpleColumnMapperRow r = (SimpleColumnMapperRow) row;

				DateOption dateOption = r.getDateOption();
				if (dateOption != null)
				{
					InputMapping.DateMapping m = new InputMapping.DateMapping();
					if (dateOption instanceof NowOption)
					{
						m.setNow(true);
					}
					else if (dateOption instanceof PatternOption)
					{
						PatternOption patternOption = (PatternOption) dateOption;
						m.setDateFormat(patternOption.format.toPattern());
						m.setFileColumn(r.getColumnFile());
					}
					else if (dateOption instanceof CalendarOption)
					{
						CalendarOption calendarOption = (CalendarOption) dateOption;
						m.setDate(InputMapping.SDF.format(calendarOption.date.toDate()));
					}

					m.setDatabaseColumn(r.getColumnDatabase().getName());

					mapping.addDateMappings(m);
				}
				else if (!StringUtils.isEmpty(r.getManualEntry()))
				{
					InputMapping.ConstantMapping m = new InputMapping.ConstantMapping();
					m.setConstant(r.getManualEntry());
					m.setDatabaseColumn(r.getColumnDatabase().getName());

					mapping.addConstantMapping(m);
				}
				else if (r.getCondition() != null)
				{
					InputMapping.ReferenceMapping m = new InputMapping.ReferenceMapping();
					m.setDatabaseColumn(r.getColumnDatabase().getName());
					m.setFileColumn(r.getColumnFile());
					m.setReferenceTable(r.getCondition().getTable().getName());
					m.setReferenceColumn(r.getCondition().getColumn().getName());

					mapping.addReferenceMapping(m);
				}
				else
				{
					InputMapping.SimpleMapping m = new InputMapping.SimpleMapping();
					m.setDatabaseColumn(r.getColumnDatabase().getName());
					m.setFileColumn(r.getColumnFile());

					mapping.addSimpleMapping(m);
				}
			}
			catch (IllegalArgumentException e)
			{
				e.printStackTrace();
			}
		}

		return mapping;
	}

	public boolean setInputMapping(InputMapping mapping)
	{
		super.setInputMapping(mapping);

		rows.forEach(ColumnMapperRow::dispose);
		rows.clear();

		boolean mappingFailed = false;

		for (InputMapping.SimpleMapping m : mapping.getSimpleMappings())
			mappingFailed |= setMapping(m);

		for (InputMapping.ReferenceMapping m : mapping.getReferenceMappings())
			mappingFailed |= setMapping(m);

		for (InputMapping.ConstantMapping m : mapping.getConstantMappings())
			mappingFailed |= setMapping(m);

		for (InputMapping.DateMapping m : mapping.getDateMappings())
			mappingFailed |= setMapping(m);

		dynamicContent.layout(true, true);

		Point targetSize = rowContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT);

		wrapper.setMinSize(targetSize);
		wrapper.setOrigin(0, targetSize.y);

		return mappingFailed;
	}

	private boolean setMapping(InputMapping.Mapping mapping)
	{
		SimpleColumnMapperRow row = new SimpleColumnMapperRow(rowContainer, SWT.NONE, SimpleColumnMapper.this);
		row.setTable(SimpleColumnMapper.this.selectedTable);
		row.setColumnsFile(SimpleColumnMapper.this.columnsFile);

		if (!row.selectColumnDatabase(mapping.getDatabaseColumn()))
		{
			row.dispose();
			return true;
		}

		if (mapping instanceof InputMapping.SimpleMapping)
		{
			InputMapping.SimpleMapping m = (InputMapping.SimpleMapping) mapping;
			if (!row.selectColumnFile(m.getFileColumn()))
			{
				row.dispose();
				return true;
			}
		}
		if (mapping instanceof InputMapping.ReferenceMapping)
		{
			InputMapping.ReferenceMapping m = (InputMapping.ReferenceMapping) mapping;

			try
			{
				DatabaseTable foreignTable = DatabaseTable.getTableWithName(m.getReferenceTable());
				DatabaseColumn foreignColumn = DatabaseColumn.getColumnWithName(foreignTable, m.getReferenceColumn());

				DatabaseColumn.Condition condition = null;

				if (foreignTable != null && foreignColumn != null)
				{
					condition = new DatabaseColumn.Condition(foreignTable, foreignColumn);
				}
				row.setCondition(condition);
			}
			catch (DatabaseException e)
			{
				e.printStackTrace();
			}
		}
		if (mapping instanceof InputMapping.ConstantMapping)
		{
			InputMapping.ConstantMapping m = (InputMapping.ConstantMapping) mapping;

			row.setManualEntryValue(m.getConstant());
		}
		if (mapping instanceof InputMapping.DateMapping)
		{
			InputMapping.DateMapping m = (InputMapping.DateMapping) mapping;

			try
			{
				if (m.isNow())
				{
					row.setDateOption(new DateOptionsDialog.NowOption());
				}
				else if (!StringUtils.isEmpty(m.getDateFormat()))
				{
					row.setDateOption(new DateOptionsDialog.PatternOption(new SimpleDateFormat(m.getDateFormat())));
					if (!row.selectColumnFile(m.getFileColumn()))
					{
						row.dispose();
						return true;
					}
				}
				else if (!StringUtils.isEmpty(m.getDate()))
				{
					row.setDateOption(new CalendarOption(new DateTime(InputMapping.SDF.parse(m.getDate()))));
				}
			}
			catch (IllegalArgumentException | ParseException e)
			{
				e.printStackTrace();
			}
		}

		rows.add(row);

		return false;
	}

	private enum State
	{
		INSERT,
		UPDATE,
		INSERT_EXISTS
	}
}
