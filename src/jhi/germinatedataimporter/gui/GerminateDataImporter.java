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

package jhi.germinatedataimporter.gui;

import org.eclipse.jface.window.*;
import org.eclipse.swt.*;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

import java.io.*;
import java.util.*;
import java.util.List;

import jhi.germinatedataimporter.gui.dialog.*;
import jhi.germinatedataimporter.gui.i18n.*;
import jhi.germinatedataimporter.gui.widget.*;
import jhi.germinatedataimporter.util.*;
import jhi.germinatedataimporter.util.thread.*;
import jhi.swtcommons.gui.*;
import jhi.swtcommons.gui.layout.*;
import jhi.swtcommons.util.*;
import jhi.swtcommons.util.PropertyReader;

/**
 * {@link GerminateDataImporter} is the main class of Germinate Daim
 *
 * @author Sebastian Raubach
 */
public class GerminateDataImporter extends RestartableApplication
{
	private static final String APP_ID         = "3640-5581-0913-1782";
	private static final String UPDATE_ID      = "314";
	private static final String VERSION_NUMBER = "x.xx.xx.xx";
	private static final String UPDATER_URL    = "https://ics.hutton.ac.uk/resources/g3dataimporter/installers/updates.xml";
	private static final String TRACKER_URL    = "https://ics.hutton.ac.uk/resources/g3dataimporter/logs/g3dataimporter.pl";

	public static  boolean               WITHIN_JAR;
	private static GerminateDataImporter INSTANCE;
	private        CTabFolder            tabFolder;
	private        WelcomePlaceholder    welcomePlaceholder;
	private        Label                 statusBarLabel;
	private        ProgressBar           statusBarProgress;
	private        Composite             statusBarComp;
	private int statusBarHeight = 0;
	private Map<CTabItem, SimpleColumnMapper> rowMapper;
	private MenuItem                          importExportMenuItem;

	public static synchronized GerminateDataImporter getInstance()
	{
		if (INSTANCE != null)
			return INSTANCE;
		else
			throw new RuntimeException("No instance of GerminateDataImporter available");
	}

	public static void main(String[] args)
	{
		/* Check if we are running from within a jar or the IDE */
		WITHIN_JAR = GerminateDataImporter.class.getResource(GerminateDataImporter.class.getSimpleName() + ".class").toString().startsWith("jar");

		new GerminateDataImporter();
	}

	/**
	 * Adds a new {@link CTabItem} with a {@link SimpleColumnMapper} to the {@link CTabFolder}
	 *
	 * @return The created {@link CTabItem}
	 */
	private Tuple.Triple<CTabItem, SimpleColumnMapper, MatrixColumnMapper> addNewTab()
	{
		if (rowMapper == null)
			rowMapper = new HashMap<>();

		boolean first = tabFolder.getItemCount() == 0;
		CTabItem rowItem = new CTabItem(tabFolder, SWT.NONE);
		rowItem.setText(RB.getString(RB.TAB_ROW));
		rowItem.setShowClose(!first);

		SimpleColumnMapper rm = new SimpleColumnMapper(tabFolder, rowItem);
		rowMapper.put(rowItem, rm);

		CTabItem matrixItem = new CTabItem(tabFolder, SWT.NONE);
		matrixItem.setText(RB.getString(RB.TAB_MATRIX));
		matrixItem.setShowClose(!first);

		MatrixColumnMapper mm = new MatrixColumnMapper(tabFolder, matrixItem);

		return new Tuple.Triple<>(rowItem, rm, mm);
	}

	/**
	 * Opens the {@link DatabaseCredentialsDialog}
	 */
	private void openConnectionDialog()
	{
		DatabaseCredentialsDialog credentials = new DatabaseCredentialsDialog(shell);

		if (credentials.open() == Window.OK)
		{
			reset();
		}
	}

	/**
	 * Disposes all existing {@link CTabItem}s and creates a new one
	 */
	private void reset()
	{
		DatabaseTableScanThread.stopAll();

		if (tabFolder == null)
		{
			welcomePlaceholder.dispose();

            /* Create a tab folder to hold the ColumnMappers */
			tabFolder = new CTabFolder(shell, SWT.TOP);
			tabFolder.setBorderVisible(true);

			GridLayoutUtils.useDefault().applyTo(tabFolder);
			GridDataUtils.usePredefined(GridDataUtils.GridDataStyle.FILL_BOTH).applyTo(tabFolder);

			statusBarComp = new Composite(shell, SWT.BORDER);
			GridLayoutUtils.useValues(2, false).applyTo(statusBarComp);
			GridDataUtils.usePredefined(GridDataUtils.GridDataStyle.FILL_BOTTOM_FALSE).heightHint(0).applyTo(statusBarComp);

			statusBarLabel = new Label(statusBarComp, SWT.NONE);
			statusBarProgress = new ProgressBar(statusBarComp, SWT.NORMAL);
			statusBarProgress.setVisible(false);

			GridDataUtils.usePredefined(GridDataUtils.GridDataStyle.FILL_CENTER).applyTo(statusBarLabel);
			GridDataUtils.usePredefined(GridDataUtils.GridDataStyle.END_CENTER_FALSE).applyTo(statusBarProgress);

			shell.layout(true);
		}

		for (CTabItem item : tabFolder.getItems())
			item.dispose();

		tabFolder.setSelection(addNewTab().getFirst());

		tabFolder.addListener(SWT.Selection, e -> {
			importExportMenuItem.setEnabled(rowMapper.get(tabFolder.getItem(tabFolder.getSelectionIndex())) != null);
		});
		importExportMenuItem.setEnabled(true);
	}

	/**
	 * Creates the {@link Menu}.
	 */
	private void createMenuBar()
	{
		Menu oldMenu = shell.getMenuBar();
		if (oldMenu != null && !oldMenu.isDisposed())
			oldMenu.dispose();

		Menu menuBar = new Menu(shell, SWT.BAR);
		Menu fileMenu = new Menu(menuBar);
		Menu importExportMenu = new Menu(menuBar);
		Menu aboutMenu = new Menu(menuBar);

        /* File */
		MenuItem item = new MenuItem(menuBar, SWT.CASCADE);
		item.setText(RB.getString(RB.MENU_MAIN_FILE));
		item.setMenu(fileMenu);

        /* Help */
		item = new MenuItem(menuBar, SWT.CASCADE);
		item.setText(RB.getString(RB.MENU_MAIN_HELP));
		item.setMenu(aboutMenu);

        /* File - Connect */
		item = new MenuItem(fileMenu, SWT.NONE);
		item.setText(RB.getString(RB.MENU_MAIN_FILE_CONNECT));
		item.addListener(SWT.Selection, e -> openConnectionDialog());

		/* File - Import/Export */
		importExportMenuItem = new MenuItem(fileMenu, SWT.CASCADE);
		importExportMenuItem.setText(RB.getString(RB.MENU_MAIN_FILE_IMPORT_EXPORT));
		importExportMenuItem.setMenu(importExportMenu);
		importExportMenuItem.setEnabled(false);

		/* File - Import/Export - Import from XML */
		item = new MenuItem(importExportMenu, SWT.NONE);
		item.setText(RB.getString(RB.MENU_MAIN_FILE_IMPORT_EXPORT_IMPORT_XML));
		item.addListener(SWT.Selection, e -> {
			if (tabFolder.getSelectionIndex() == 0)
			{
				FileDialog dialog = new FileDialog(shell, SWT.OPEN);
				dialog.setFilterExtensions(new String[]{"*.xml", "*.*"});

				/* See if there is a location to restore */
				GerminateParameterStore store = GerminateParameterStore.getInstance();
				String path = store.getAsString(GerminateParameter.inputfile);
				if (!StringUtils.isEmpty(path))
					dialog.setFilterPath(path);

				String f = dialog.open();
				if (!StringUtils.isEmpty(f))
				{
					File file = new File(f);

					if (file.exists() && file.isFile())
					{
						GerminateParameterStore.getInstance().put(GerminateParameter.inputfile, file.getParentFile().getAbsolutePath());
						rowMapper.get(tabFolder.getItem(tabFolder.getSelectionIndex())).importFromXml(file);
					}
				}
			}
		});

		/* File - Import/Export - Export to XML */
		item = new MenuItem(importExportMenu, SWT.NONE);
		item.setText(RB.getString(RB.MENU_MAIN_FILE_IMPORT_EXPORT_EXPORT_XML));
		item.addListener(SWT.Selection, e -> {
			if (tabFolder.getSelectionIndex() == 0)
			{
				FileDialog dialog = new FileDialog(shell, SWT.SAVE);
				dialog.setFilterExtensions(new String[]{"*.xml", "*.*"});

				/* See if there is a location to restore */
				GerminateParameterStore store = GerminateParameterStore.getInstance();
				String path = store.getAsString(GerminateParameter.inputfile);
				if (!StringUtils.isEmpty(path))
					dialog.setFilterPath(path);

				String f = dialog.open();

				if (!StringUtils.isEmpty(f))
				{
					File file = new File(f);
					GerminateParameterStore.getInstance().put(GerminateParameter.inputfile, file.getParentFile().getAbsolutePath());
					rowMapper.get(tabFolder.getItem(tabFolder.getSelectionIndex())).exportToXml(file);
				}
			}
		});

        /* File - Exit */
		addQuitMenuItemListener(RB.getString(RB.MENU_MAIN_FILE_EXIT), fileMenu, e -> shutdown());

        /* Help - Online help */
		item = new MenuItem(aboutMenu, SWT.NONE);
		item.setText(RB.getString(RB.MENU_MAIN_HELP_ONLINE_HELP));
		item.addListener(SWT.Selection, e -> OSUtils.open(RB.getString(RB.URL_ONLINE_HELP)));

        /* Help - Log */
		item = new MenuItem(aboutMenu, SWT.CHECK);
		item.setText(RB.getString(RB.MENU_MAIN_HELP_LOG));
		item.setSelection(false);

		final MenuItem logMenuItem = item;
		item.addListener(SWT.Selection, e -> LogDialog.getInstance().setVisible(logMenuItem.getSelection()));
		LogDialog.addVisibilityHandler(visible -> logMenuItem.setSelection(false));

         /* Help - Settings */
		addPreferencesMenuItemListener(RB.getString(RB.MENU_MAIN_HELP_PREFERENCES), aboutMenu, e -> {
			PreferencesDialog dialog = new PreferencesDialog(shell);

			if (dialog.open() == Window.OK)
			{
				if (dialog.isChanged())
					onRestart();
			}
		});

		/* Help - Check for updates */
		item = new MenuItem(aboutMenu, SWT.NONE);
		item.setText(RB.getString(RB.MENU_MAIN_HELP_UPDATE));
		item.addListener(SWT.Selection, e -> checkForUpdate(false));
		item.setEnabled(WITHIN_JAR);

        /* Help - About */
		addAboutMenuItemListener(RB.getString(RB.MENU_MAIN_HELP_ABOUT), aboutMenu, e -> new AboutDialog(shell).open());

		shell.setMenuBar(menuBar);
	}

	/**
	 * Attempts to set the progress of the taskbar item
	 *
	 * @param state    The state ({@link SWT#INDETERMINATE}, {@link SWT#PAUSED}, etc)
	 * @param progress The progress [0, 100]
	 * @see TaskItem#setProgressState(int)
	 * @see TaskItem#setProgress(int)
	 */
	public void setTaskBarProgress(final int state, final int progress)
	{
		display.asyncExec(() -> {
			final TaskItem item = getTaskBarItem();

			if (item == null)
				return;

			if (!item.isDisposed())
			{
				item.setProgressState(state);
				item.setProgress(progress);
			}
		});
	}

	/**
	 * Returns the system task bar or <code>null</code> if there is none
	 *
	 * @return The system task bar or <code>null</code> if there is none
	 */
	private TaskItem getTaskBarItem()
	{
		TaskBar bar = display.getSystemTaskBar();
		if (bar == null)
			return null;
		TaskItem item = bar.getItem(shell);
		if (item == null)
			item = bar.getItem(null);
		return item;
	}

	@Override
	protected void onStart()
	{
		INSTANCE = this;

		DatabaseTableScanThread.stopAll();

        /* Set the app name (fix for "SWT" menu item on MacOS X) */
		Display.setAppName(RB.getString(RB.APPLICATION_TITLE));

        /* Prepare the log window */
		LogDialog.reset();
		LogDialog.init(new Shell(shell, SWT.SHELL_TRIM));

		shell.setText(RB.getString(RB.APPLICATION_TITLE));
		shell.setImage(Resources.Images.GERMINATE_TRAY);

		WidgetUtils.dispose(tabFolder, welcomePlaceholder, statusBarComp);
		tabFolder = null;
		welcomePlaceholder = null;
		statusBarComp = null;

		welcomePlaceholder = new WelcomePlaceholder(shell, SWT.NONE);

        /* Create content and menu bar */
		createMenuBar();
	}

	@Override
	protected int getMinWidth()
	{
		return ShellUtils.MIN_WIDTH + 50;
	}

	@Override
	protected int getMinHeight()
	{
		return ShellUtils.MIN_HEIGHT + 50;
	}

	@Override
	protected void onExit()
	{
		/* We don't need to dispose anything here, the parent takes care of it
		 * all for us */

		LogDialog.reset();
	}

	@Override
	protected void initResources()
	{
		Resources.initialize();
	}

	@Override
	protected void disposeResources()
	{
		Resources.disposeResources();
	}

	@Override
	protected void onFileDropped(final List<File> files)
	{
		if (tabFolder == null)
		{
			DialogUtils.showError(RB.getString(RB.ERROR_DND_CONNECT_TO_DATABASE));
			return;
		}

		if (files.size() > 1)
		{
			DialogUtils.showError(RB.getString(RB.ERROR_DROP_FILES, files.size()));
		}

		String message = RB.getString(RB.QUESTION_DROP_FILE);

        /* Ask the user if the dropped files should be opened in new tabs */
		DialogUtils.showQuestion(message, result -> {
			if (result)
			{
				File file = files.get(0);
				Tuple.Triple<CTabItem, SimpleColumnMapper, MatrixColumnMapper> newItem = addNewTab();

				tabFolder.setSelection(newItem.getFirst());

				newItem.getSecond().onFileSelected(file);

				newItem.getThird().onFileSelected(file);

				importExportMenuItem.setEnabled(true);
			}
		});
	}

	/**
	 * Updates the status bar
	 *
	 * @param progress The progress
	 * @param maximum  The maximum
	 * @param message  The status message
	 */
	public void setStatusBar(final int progress, final int maximum, final String message)
	{
		display.asyncExec(() -> {
			if (!statusBarComp.isDisposed())
			{
				boolean show = maximum != 0;

				GridData data = (GridData) statusBarComp.getLayoutData();

				if (show)
				{
					data.heightHint = statusBarComp.computeSize(SWT.DEFAULT, SWT.DEFAULT).y;
				}
				else
				{
					data.heightHint = 0;
				}

				if (statusBarHeight != data.heightHint)
				{
					statusBarComp.getParent().layout();
					statusBarHeight = data.heightHint;
				}

				statusBarProgress.setVisible(show);
				statusBarProgress.setMaximum(maximum);
				statusBarProgress.setSelection(progress);
				statusBarLabel.setText(message);
				statusBarLabel.pack();
			}
		});
	}

	protected PropertyReader getPropertyReader()
	{
		return new jhi.germinatedataimporter.util.PropertyReader();
	}

	@Override
	protected void onPreStart()
	{
		checkForUpdate(true);
	}

	private void checkForUpdate(boolean startupCall)
	{
		if (WITHIN_JAR)
		{
			/* Check if an update is available */
			Install4jUtils i4j = new Install4jUtils(APP_ID, UPDATE_ID);

			Install4jUtils.UpdateInterval interval = startupCall ? (Install4jUtils.UpdateInterval) GerminateParameterStore.getInstance().get(GerminateParameter.updateInterval) : Install4jUtils.UpdateInterval.STARTUP;

			i4j.setDefaultVersionNumber(VERSION_NUMBER);
			i4j.setUser(interval, GerminateParameterStore.getInstance().getAsString(GerminateParameter.userId), 0);
			i4j.setURLs(UPDATER_URL, TRACKER_URL);
			if (!startupCall)
			{
				i4j.setCallback(updateAvailable -> {
					if (!updateAvailable)
						DialogUtils.showInformation(RB.getString(RB.INFORMATION_NO_UPDATE_AVAILABLE));
				});
			}

			i4j.doStartUpCheck(GerminateDataImporter.class);
		}
	}
}
