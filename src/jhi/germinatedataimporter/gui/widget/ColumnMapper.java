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

import org.eclipse.jface.window.*;
import org.eclipse.swt.*;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.widgets.*;

import java.io.*;
import java.util.*;
import java.util.List;

import jhi.database.shared.exception.*;
import jhi.germinatedataimporter.database.entities.*;
import jhi.germinatedataimporter.gui.dialog.*;
import jhi.germinatedataimporter.gui.i18n.*;
import jhi.germinatedataimporter.gui.viewer.*;
import jhi.germinatedataimporter.util.*;
import jhi.germinatedataimporter.util.xml.*;
import jhi.swtcommons.gui.layout.*;
import jhi.swtcommons.util.*;

/**
 * @author Sebastian Raubach
 */
public abstract class ColumnMapper
{
	/** The currently available {@link SimpleColumnMapperRow}s */
	protected List<ColumnMapperRow> rows = new ArrayList<>();

	/** The column names parsed from the input file */
	protected Set<String>   columnsFile;
	/** The currently selected {@link DatabaseTable} */
	protected DatabaseTable selectedTable;

	protected ScrolledComposite        wrapper;
	protected Composite                rowContainer;
	protected DatabaseTableComboViewer tableCombo;
	protected Button                   chooseFile;
	protected Button                   inputOptions;
	protected Composite                content;
	protected Composite                dynamicContent;
	protected Button                   revert;
	protected Button                   run;
	protected Button                   update;

	protected CTabItem contentHolder;

	protected List<Long> generatedIds;

	protected DatabaseTable databaseTable;

	protected InputOptions options = new InputOptions();

	protected TrimmedLabel selectedFileName;
	protected File         selectedFile;

	public ColumnMapper(Composite parent, CTabItem contentHolder)
	{
		content = new Composite(parent, SWT.NONE);

		contentHolder.setControl(content);

		this.contentHolder = contentHolder;

		GerminateParameterStore store = GerminateParameterStore.getInstance();
		options.separator = (FileSeparator) store.get(GerminateParameter.inputseparator);
		options.locale = (Locale) store.get(GerminateParameter.inputlocale);

        /* Combo with the database tables */
		tableCombo = new DatabaseTableComboViewer(content, SWT.READ_ONLY);
		tableCombo.addSelectionChangedListener(e -> {
			selectedTable = tableCombo.getSelectedItem();

			if (columnsFile != null && columnsFile.size() > 0 && selectedTable != null)
				setUpContent();
		});

        /* Button to select the input file */
		chooseFile = new Button(content, SWT.PUSH);
		chooseFile.setToolTipText(RB.getString(RB.TOOLTIP_IMPORT_CHOOSE_FILE));
		chooseFile.setImage(Resources.Images.FOLDER);
		chooseFile.addListener(SWT.Selection, e -> {
			FileDialog dialog = new FileDialog(content.getShell());
			dialog.setFilterExtensions(new String[]{"*.txt;*.tsv;*.csv", "*.*"});

			/* See if there is a location to restore */
			String path = store.getAsString(GerminateParameter.inputfile);
			if (!StringUtils.isEmpty(path))
				dialog.setFilterPath(path);

			String selection = dialog.open();

			if (!StringUtils.isEmpty(selection))
			{
				File file = new File(selection);

				if (file.exists() && file.isFile())
					onFileSelected(file);
			}
		});

		inputOptions = new Button(content, SWT.PUSH);
		inputOptions.setToolTipText(RB.getString(RB.TOOLTIP_IMPORT_INPUT_OPTIONS));
		inputOptions.setImage(Resources.Images.SETTINGS);
		inputOptions.addListener(SWT.Selection, event -> {
			InputOptionsDialog optionsDialog = new InputOptionsDialog(content.getShell(), options);
			int result = optionsDialog.open();

			if (result == Window.OK)
			{
				options = optionsDialog.getInputOptions();

				if (options.file != null)
				{
					try
					{
						columnsFile = FileUtils.readHeaders(options);
					}
					catch (IOException e1)
					{
						DialogUtils.handleException(e1);
					}
				}

				if (optionsDialog.fileSeparatorChanged() && columnsFile != null && columnsFile.size() > 0 && selectedTable != null)
					setUpContent();
			}
		});

		selectedFileName = new TrimmedLabel(content, SWT.NONE);
		selectedFileName.setTrim(SWT.CENTER);

		Composite buttonBar = new Composite(content, SWT.NONE);

        /* Button to start the import process */
		revert = new Button(buttonBar, SWT.PUSH);
		revert.setToolTipText(RB.getString(RB.TOOLTIP_IMPORT_REVERT));
		revert.setImage(Resources.Images.UNDO);
		revert.setEnabled(false);
		revert.addListener(SWT.Selection, event -> DialogUtils.showQuestion(RB.getString(RB.QUESTION_UNDO, generatedIds.size()), result -> {
			if (result)
			{
				deleteItems(databaseTable, generatedIds);
				DatabaseTableObserver.notifyListenersAndRefresh(selectedTable);
			}
		}));

		boolean supportsUpdate = supportsUpdate();
		/* Button to update data entries */
		update = new Button(buttonBar, SWT.PUSH);
		update.setToolTipText(RB.getString(RB.TOOLTIP_IMPORT_UPDATE_DATA));
		update.setImage(Resources.Images.DATABASE_UPDATE);
		update.setEnabled(false);
		update.setVisible(supportsUpdate);
		update.addListener(SWT.Selection, e -> update());

        /* Button to start the import process */
		run = new Button(buttonBar, SWT.PUSH);
		run.setToolTipText(RB.getString(RB.TOOLTIP_IMPORT_IMPORT_DATA));
		run.setImage(Resources.Images.DATABASE_IMPORT);
		run.setEnabled(false);
		run.addListener(SWT.Selection, e -> insert());

		GridLayoutUtils.useValues(5, false).applyTo(content);
		GridLayoutUtils.useValues(supportsUpdate ? 3 : 2, true).applyTo(buttonBar);

		GridDataUtils.usePredefined(GridDataUtils.GridDataStyle.FILL_BOTH).applyTo(content);
		GridDataUtils.usePredefined(GridDataUtils.GridDataStyle.END_CENTER_FALSE).applyTo(buttonBar);
		GridDataUtils.usePredefined(GridDataUtils.GridDataStyle.BEGINNING_CENTER_FALSE).applyTo(tableCombo.getCombo());
		GridDataUtils.usePredefined(GridDataUtils.GridDataStyle.BEGINNING_CENTER_FALSE).applyTo(chooseFile);
		GridDataUtils.usePredefined(GridDataUtils.GridDataStyle.BEGINNING_CENTER_FALSE).applyTo(inputOptions);
		GridDataUtils.usePredefined(GridDataUtils.GridDataStyle.END_CENTER).applyTo(revert);
		GridDataUtils.usePredefined(GridDataUtils.GridDataStyle.END_CENTER).exclude(!supportsUpdate).applyTo(update);
		GridDataUtils.usePredefined(GridDataUtils.GridDataStyle.END_CENTER).applyTo(run);

		reset();
	}

	/**
	 * Deletes the items with the given ids from the {@link DatabaseTable}
	 *
	 * @param table The {@link DatabaseTable}
	 * @param ids   The ids of the items to delete
	 */
	protected void deleteItems(DatabaseTable table, List<Long> ids)
	{
		try
		{
			/* Delete them */
			table.deleteItemsWithId(ids);
			table.setAutoIncrement();
			/* Reset selection */
			setGeneratedIds(null, null);
		}
		catch (DatabaseException e)
		{
			DialogUtils.handleException(e);
		}
	}

	/**
	 * Remembers the {@link DatabaseTable} and generated ids of the last insert process
	 *
	 * @param table The {@link DatabaseTable}
	 * @param ids   The generated ids
	 */
	protected void setGeneratedIds(DatabaseTable table, List<Long> ids)
	{
		databaseTable = table;
		generatedIds = ids;

		Display.getDefault().asyncExec(() -> revert.setEnabled(!CollectionUtils.isEmpty(generatedIds)));
	}

	public void onFileSelected(File selection)
	{
		selectedFile = selection;

		selectedFileName.setText(selection.getName());
		/*
		 * If the user hasn't selected and input options or s/he selected to
         * show the dialog each time
         */
		if (checkInputOptions())
		{
			/* Show a dialog asking for input options */
			InputOptionsDialog optionsDialog = new InputOptionsDialog(content.getShell(), options);
			int result = optionsDialog.open();

			if (result != Window.OK)
				return;
			else
			{
				/* Remember the selected options locally in the ColumnMapper */
				options = optionsDialog.getInputOptions();
			}
		}

		options.file = selection;

		Set<String> newColumns = new HashSet<>();
		GerminateParameterStore.getInstance().put(GerminateParameter.inputfile, options.file.getParentFile().getAbsolutePath());
		try
		{
			newColumns = FileUtils.readHeaders(options);
		}
		catch (IOException e1)
		{
			DialogUtils.handleException(e1);
		}

		if (CollectionUtils.isEmpty(newColumns))
		{
			DialogUtils.showError(RB.getString(RB.ERROR_FILE_NO_DATA));
		}
		else
		{
			boolean areEqual = newColumns.equals(columnsFile);

			columnsFile = newColumns;

			if (selectedTable != null && !areEqual)
				setUpContent();

//			boolean sameColumnsAsMapping;
//
//			if (this instanceof SimpleColumnMapper)
//			{
//				List<String> rowColumns = rows.stream()
//											  .map(ColumnMapperRow::getDTO)
//											  .map(dto -> (SimpleColumnMapperRow.SimpleColumnMapperRowDTO) dto)
//											  .map(dto -> dto.columnFile)
//											  .collect(Collectors.toList());
//
//				sameColumnsAsMapping = rowColumns.size() > 0;
//				for (String column : newColumns)
//				{
//					if (!rowColumns.contains(column))
//						sameColumnsAsMapping = false;
//				}
//
//				columnsFile = newColumns;
//
//				if (selectedTable != null && !sameColumnsAsMapping)
//					setUpContent();
//			}
//			else
//			{
//				columnsFile = newColumns;
//
//				if (selectedTable != null)
//					setUpContent();
//			}
		}
	}

	protected boolean checkInputOptions()
	{
		GerminateParameterStore store = GerminateParameterStore.getInstance();

		if ((boolean) store.get(GerminateParameter.inputalwaysask))
			return true;
		else if (StringUtils.isEmpty(store.getAsString(GerminateParameter.inputlocale)))
			return true;
		else if (store.get(GerminateParameter.inputseparator) == null)
			return true;
		else
			return false;
	}

	/**
	 * Resets the {@link SimpleColumnMapper}, i.e., clears user selections and removes all {@link SimpleColumnMapperRow}s.
	 */
	protected void reset()
	{
		tableCombo.setDatabase((DatabaseDatabase) GerminateParameterStore.getInstance().get(GerminateParameter.database));
		content.layout();

		selectedTable = null;
		if (columnsFile != null)
			columnsFile.clear();

		run.setEnabled(false);
		update.setEnabled(false);

		if (dynamicContent != null && !dynamicContent.isDisposed())
			dynamicContent.dispose();
	}

	protected abstract void setUpContent();

	protected abstract void insert();

	protected abstract void update();

	protected abstract boolean supportsUpdate();

	public boolean setInputMapping(final InputMapping mapping)
	{
		tableCombo.selectItem(mapping.getTargetTable());

		onFileSelected(new File(mapping.getInputFile()));

		return true;
	}

	/**
	 * Deletes the given {@link ColumnMapperRow} from the {@link MatrixColumnMapper}
	 *
	 * @param row The {@link ColumnMapperRow} to delete
	 */
	public void onDelete(ColumnMapperRow row)
	{
		rows.remove(row);
		dynamicContent.layout(true, true);
		wrapper.setMinSize(rowContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT));
	}

	/**
	 * {@link InputOptions} is a simple DTO holding the {@link FileSeparator}, {@link Locale} and {@link File} used to process the input
	 *
	 * @author Sebastian Raubach
	 */
	public static class InputOptions
	{
		public FileSeparator separator;
		public Locale        locale;
		public File          file;
	}
}
