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
import org.eclipse.swt.*;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

import java.io.*;
import java.lang.reflect.*;
import java.sql.*;
import java.text.*;
import java.util.*;
import java.util.List;
import java.util.stream.*;

import jhi.database.shared.exception.*;
import jhi.germinatedataimporter.database.entities.*;
import jhi.germinatedataimporter.gui.i18n.*;
import jhi.germinatedataimporter.gui.widget.MatrixColumnMapperRow.*;
import jhi.germinatedataimporter.util.*;
import jhi.germinatedataimporter.util.exception.*;
import jhi.germinatedataimporter.util.thread.*;
import jhi.swtcommons.gui.layout.*;
import jhi.swtcommons.util.*;

/**
 * {@link MatrixColumnMapper} let's the user pick a mapping between input file columns and database columns.
 *
 * @author Sebastian Raubach
 */
public class MatrixColumnMapper extends ColumnMapper
{
	/**
	 * Creates a new instance of {@link MatrixColumnMapper} with the given {@link Composite} as its parent
	 *
	 * @param parent The parent
	 */
	public MatrixColumnMapper(Composite parent, CTabItem contentHolder)
	{
		super(parent, contentHolder);
	}

	/**
	 * Starts the import process. Uses a {@link RowDataImportThread} to import the data using the given mapping.
	 *
	 * @param mapping The {@link List} of {@link MatrixColumnMapperRowDTO}s
	 */
	private void startImport(List<MatrixColumnMapperRowDTO> mapping)
	{
		try
		{
			databaseTable = selectedTable;
			IRunnableWithProgress op = new MatrixDataImportThread(options, mapping, tableCombo.getSelectedItem())
			{
				@Override
				public Tuple.Pair<Boolean, Boolean> onImportError(Exception e, boolean checkedState)
				{
					String message = e.getLocalizedMessage();
					if (e.getCause() instanceof SQLException)
						message = DatabaseExceptionUtils.getMessageFromException((SQLException) e.getCause());

					return DialogUtils.showQuestionToggle(RB.getString(RB.ERROR_IMPORT_ASK_FOR_CONTINUE, message), RB.getString(RB.ERROR_IMPORT_ASK_FOR_CONTINUE_TOGGLE), checkedState);
				}

				@Override
				public void onImportFailed(final List<Long> generatedIds, int updatedIds, Exception e)
				{
					String message = "";

					if (e instanceof ParseException)
					{
						message = RB.getString(RB.ERROR_IMPORT_PARSE_EXCEPTION, e.getLocalizedMessage());
					}
					else if (e instanceof DatabaseException)
					{
						message = RB.getString(RB.ERROR_IMPORT_DATABASE_EXCEPTION, e.getLocalizedMessage());
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

				@Override
				public void onImportCancelled(final List<Long> generatedIds, int updatedIds)
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

				@Override
				public void onImportFinished(List<Long> generatedIds, int updatedIds)
				{
					setGeneratedIds(databaseTable, generatedIds);

                    /* Notify the user that the import has finished successfully */
					if (generatedIds.size() == 1)
						DialogUtils.showInformation(RB.getString(RB.INFORMATION_IMPORT_SUCCESS_ONE, generatedIds.size(), updatedIds));
					else
						DialogUtils.showInformation(RB.getString(RB.INFORMATION_IMPORT_SUCCESS_MANY, generatedIds.size(), updatedIds));

					DatabaseTableObserver.notifyListenersAndRefresh(selectedTable);
				}
			};

            /* Start the progress dialog */
			new ProgressMonitorDialog(content.getShell()).run(true, true, op);
		}
		catch (InvocationTargetException | InterruptedException e)
		{
			DialogUtils.handleException(e);
		}
	}

	/**
	 * Checks if all the conditions are fulfilled
	 *
	 * @throws MissingConstraintException   Thrown if a selected {@link DatabaseColumn} doesn't have a valid constraint defined
	 * @throws DatabaseException            Thrown if the database communication fails
	 * @throws MissingColumnException       Thrown if the user forgot to map a {@link DatabaseColumn} that CANNOT be null
	 * @throws DuplicateColumnException     Thrown if the user selected a {@link DatabaseColumn} at least twice or if a {@link
	 *                                      MatrixSourceElement#COL_ID} or {@link MatrixSourceElement#ROW_ID} have been selected at least twice
	 * @throws MissingInputColumnException  Thrown if a {@link DatabaseColumn} doesn't have an input column mapped to it
	 * @throws MissingMatrixColumnException Thrown if a required {@link MatrixSourceElement} hasn't been selected
	 * @throws IllegalArgumentException     Thrown if either there are no {@link SimpleColumnMapperRow}s OR one of the mapping elements is
	 *                                      <code>null</code> or empty.
	 */
	private void checkConditions() throws MissingConstraintException, DatabaseException, MissingColumnException, MissingInputColumnException, DuplicateColumnException, MissingMatrixColumnException
	{
		if (rows == null || rows.size() < 1)
			throw new IllegalArgumentException();

		for (ColumnMapperRow rowAbstr : rows)
		{
			MatrixColumnMapperRow row = (MatrixColumnMapperRow) rowAbstr;
			if (row.getColumnDatabase() == null)
				throw new IllegalArgumentException();
		}

        /* Check if all columns that have to be set are set */
		for (DatabaseColumn col : selectedTable.getColumns())
		{
			if (!col.getCanBeNull())
			{
				boolean found = false;
				for (ColumnMapperRow rowAbstr : rows)
				{
					MatrixColumnMapperRow row = (MatrixColumnMapperRow) rowAbstr;
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

		Set<DatabaseColumn> selectedDatabaseColumns = new HashSet<>();

		for (ColumnMapperRow rowAbstr : rows)
		{
			MatrixColumnMapperRow row = (MatrixColumnMapperRow) rowAbstr;
			MatrixSourceElement matrixInput = row.getMatrixSourceElement();
			DatabaseColumn database = row.getColumnDatabase();

            /* Check for duplicates */
			if (selectedDatabaseColumns.contains(database))
				throw new DuplicateColumnException(database);

			selectedDatabaseColumns.add(database);

			if (matrixInput == null && !StringUtils.isEmpty(row.getManualEntry()))
			{
				/* If we're using a manual entry, we don't need an input column */
			}
			else if (matrixInput == null)
			{
				throw new MissingInputColumnException(database);
			}
			else if (database.isForeignKey() && row.getCondition() == null)
			{
				throw new MissingConstraintException(database);
			}
		}

		outer:
		for (MatrixSourceElement element : MatrixSourceElement.values())
		{
			for (ColumnMapperRow rowAbstr : rows)
			{
				MatrixColumnMapperRow row = (MatrixColumnMapperRow) rowAbstr;

				if (element.equals(row.getMatrixSourceElement()))
				{
					continue outer;
				}
			}

			throw new MissingMatrixColumnException(element);
		}
	}

	protected void insert()
	{
		try
		{
			checkConditions();

			List<MatrixColumnMapperRowDTO> mapping = getMapping();

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
		catch (MissingMatrixColumnException e1)
		{
			DialogUtils.showError(RB.getString(RB.ERROR_MAPPING_MISSING_MATRIX_COLUMN, e1.getElement().getName()));
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

		wrapper.setContent(rowContainer);
		wrapper.setExpandHorizontal(true);
		wrapper.setExpandVertical(true);
		wrapper.setMinSize(rowContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT));

        /* Add a new row */
		add.addListener(SWT.Selection, e -> addRow(true, null));

		addRow(false, MatrixSourceElement.ROW_ID);
		addRow(false, MatrixSourceElement.COL_ID);
		addRow(false, MatrixSourceElement.VALUE);

		content.layout();
	}

	private void addRow(boolean canBeDeleted, MatrixSourceElement type)
	{
		MatrixColumnMapperRow row = new MatrixColumnMapperRow(rowContainer, SWT.NONE, this);
		row.setTable(selectedTable);
		row.setCanBeDeleted(canBeDeleted);
		row.setType(type);
		rows.add(row);
		dynamicContent.layout(true, true);

		Point targetSize = rowContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT);

		wrapper.setMinSize(targetSize);
		wrapper.setOrigin(0, targetSize.y);
	}

	/**
	 * Returns the mapping between file columns and database columns
	 *
	 * @return The mapping between file columns and database columns
	 */
	private List<MatrixColumnMapperRowDTO> getMapping()
	{
		return rows.stream().map(row -> ((MatrixColumnMapperRow) row).getDTO()).collect(Collectors.toList());
	}

	@Override
	protected void update()
	{
	}

	@Override
	protected boolean supportsUpdate()
	{
		return false;
	}
}
