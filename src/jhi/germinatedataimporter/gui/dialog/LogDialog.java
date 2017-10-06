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

package jhi.germinatedataimporter.gui.dialog;

import org.eclipse.swt.*;
import org.eclipse.swt.dnd.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import org.joda.time.DateTime;
import org.joda.time.*;

import java.text.*;
import java.util.*;
import java.util.List;
import java.util.logging.*;

import jhi.germinatedataimporter.gui.*;
import jhi.germinatedataimporter.gui.i18n.*;
import jhi.germinatedataimporter.util.*;
import jhi.germinatedataimporter.util.log.*;
import jhi.swtcommons.gui.layout.*;
import jhi.swtcommons.util.*;

/**
 * {@link LogDialog} is used to track events in the {@link GerminateDataImporter}. Entries can be added from anywhere using {@link #getInstance()}
 * (with <code>null</code>) to get the isntance and then calling {@link #add(String, String)}.
 *
 * @author Sebastian Raubach
 */
public class LogDialog
{
	public static final int ENTRIES_PER_HEADER = 10;

	private static Shell shell;
	private static int width  = -1;
	private static int height = -1;
	private static LogDialog INSTANCE;
	private static List<VisibilityHandler> visibilityHandlerList = new ArrayList<>();
	private Tree     log;
	private String   lastHeader;
	private TreeItem lastHeaderItem;
	private TreeItem moreItem;
	private int              entriesWithThisHeader = 0;
	private boolean          updateInsteadOfAdd    = false;
	private SimpleDateFormat format                = new SimpleDateFormat(RB.getString(RB.DIALOG_LOG_TIME_FORMAT));
	private DateTime         lastUpdate            = null;
	private Composite content;

	private LogDialog(Shell parentShell)
	{
		content = new Composite(parentShell, SWT.NONE);
		GridLayoutUtils.useDefault().applyTo(parentShell);
		GridLayoutUtils.useValues(2, false).applyTo(content);

		log = new Tree(content, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.FULL_SELECTION);
		log.setHeaderVisible(true);
		GridDataUtils.usePredefined(GridDataUtils.GridDataStyle.FILL_BOTH).applyTo(content);
		GridDataUtils.usePredefined(GridDataUtils.GridDataStyle.FILL_BOTH).horizontalSpan(2).applyTo(log);

		Button clear = new Button(content, SWT.PUSH);
		clear.setText(RB.getString(RB.DIALOG_LOG_CLEAR));
		clear.addListener(SWT.Selection, event -> {
			log.removeAll();
			entriesWithThisHeader = 1;
			updateInsteadOfAdd = false;
			moreItem = null;
			lastHeaderItem = null;
			lastHeader = null;
		});

		Button showLogFile = new Button(content, SWT.PUSH);
		showLogFile.setText(RB.getString(RB.DIALOG_LOG_SHOW_LOG_FILE));
		showLogFile.addListener(SWT.Selection, event -> OSUtils.open(SQLLogger.getInstance().getLogFile()));

		GridDataUtils.usePredefined(GridDataUtils.GridDataStyle.BEGINNING_CENTER).applyTo(clear);
		GridDataUtils.usePredefined(GridDataUtils.GridDataStyle.END_CENTER).applyTo(showLogFile);

		new TreeColumn(log, SWT.NONE).setText(RB.getString(RB.DIALOG_LOG_TIME));
		new TreeColumn(log, SWT.NONE).setText(RB.getString(RB.DIALOG_LOG_CONTENT));

		log.getColumn(0).setWidth(120);
		log.getColumn(1).setWidth(2000);

        /* Add a context menu that allows copying stuff to the clipboard */
		final Menu menu = new Menu(log);
		log.setMenu(menu);
		log.addListener(SWT.MenuDetect, e -> {
			/* Dispose old menu items */
			MenuItem[] items = menu.getItems();
			for (MenuItem item : items)
			{
				item.dispose();
			}

			/* If no item in the table is selected, return */
			if (log.getSelectionCount() == 0)
				return;

			final TreeItem selectedItem = log.getSelection()[0];

			/* Don't show the menu for root items */
			if (selectedItem.getParentItem() == null)
				return;

			/* Create a new item and listen for selection */
			MenuItem newItem = new MenuItem(menu, SWT.NONE);
			newItem.setText(RB.getString(RB.DIALOG_LOG_COPY_TO_CLIPBOARD));
			newItem.addListener(SWT.Selection, event -> {
				/*
				 * Copy the content of the selected item to the
				 * clipboard
				 */
				Clipboard clipboard = new Clipboard(Display.getDefault());
				String plainText = log.getSelection()[0].getText(1);
				TextTransfer textTransfer = TextTransfer.getInstance();
				clipboard.setContents(new String[]{plainText}, new Transfer[]{textTransfer});
				clipboard.dispose();
			});
		});
	}

	public static synchronized LogDialog getInstance()
	{
		if (INSTANCE == null)
			throw new RuntimeException("LogDialog#init(Shell) has to be called first");
		else
			return INSTANCE;
	}

	public static void reset()
	{
		if (INSTANCE != null)
		{
			INSTANCE.dispose();
			INSTANCE = null;
		}

		width = -1;
		height = -1;
	}

	public static void addVisibilityHandler(VisibilityHandler handler)
	{
		visibilityHandlerList.add(handler);
	}

	/**
	 * Returns the {@link LogDialog} instance. The first time this method is called, the {@link Shell} <b>mustn't</b> be <code>null</code>. The {@link
	 * Shell} will be ignored in further method calls.
	 *
	 * @param shell The parent {@link Shell}
	 */
	public static synchronized void init(Shell shell)
	{
		if (INSTANCE == null)
		{
			LogDialog.shell = shell;
			shell.setImage(Resources.Images.GERMINATE_TRAY);
			shell.setText(RB.getString(RB.DIALOG_LOG_TITLE));
			INSTANCE = new LogDialog(shell);

            /* Prevent actual close events and just hide the shell instead */
			shell.addListener(SWT.Close, event -> {
				event.doit = false;
				LogDialog.shell.setVisible(false);

				for (VisibilityHandler handler : visibilityHandlerList)
				{
					handler.onVisibilityChanged(false);
				}
			});
			/* Listen for resize events and remember the size */
			shell.addListener(SWT.Resize, new Listener()
			{
				private boolean isFirstTime = true;

				@Override
				public void handleEvent(Event event)
				{
					if (isFirstTime)
					{
						isFirstTime = false;
						return;
					}
					width = LogDialog.shell.getSize().x;
					height = LogDialog.shell.getSize().y;
				}
			});
			/* Listen for escape key press events */
			shell.addListener(SWT.Traverse, event -> {
				if (event.detail == SWT.TRAVERSE_ESCAPE)
				{
					event.detail = SWT.TRAVERSE_NONE;
					event.doit = false;
					LogDialog.shell.setVisible(false);
				}
			});
			/*
			 * Always position the log window next to the parent (either right
             * or left)
             */
			Listener relocateListener = event -> relocate();
			shell.addListener(SWT.Show, relocateListener);
			shell.getParent().addListener(SWT.Move, relocateListener);
			shell.getParent().addListener(SWT.Resize, relocateListener);
		}
	}

	/**
	 * Aligns the log window to it's parent. If possible, it will be displayed to the right of the parent window, if not, to the left. If there isn't
	 * enough space on either side, it will be left where it is
	 */
	private static void relocate()
	{
		Shell parent = (Shell) shell.getParent();
		/* If the main window is either maximized or full-screen, do nothing */
		if (parent.getMaximized() || parent.getFullScreen())
			return;

		Rectangle parentBounds = parent.getBounds();
		Rectangle displayBounds = Display.getDefault().getBounds();

        /* Check if there's sufficient space to the right of the parent */
		if ((displayBounds.width - (parentBounds.width + parentBounds.x)) >= width)
			LogDialog.shell.setLocation(parentBounds.x + parentBounds.width, parentBounds.y);
		/* If not, check the left */
		else if (parentBounds.x >= width)
			LogDialog.shell.setLocation(parentBounds.x - width, parentBounds.y);
	}

	private void dispose()
	{
		if (content != null && !content.isDisposed())
			content.dispose();

		if (shell != null && !shell.isDisposed())
			setVisible(false);
	}

	/**
	 * Shows/hides the {@link LogDialog}
	 *
	 * @param visible The new visibility state
	 */
	public void setVisible(boolean visible)
	{
		shell.setVisible(visible);

		if (visible)
		{
			shell.open();
			if (width != -1 && height != -1)
				shell.setSize(width, height);
			else
				ShellUtils.applySize(shell);
			shell.forceActive();
		}
	}

	/**
	 * Adds a new item to the {@link LogDialog}. <p> <b>IMPORTANT:</b> Note that at most {@link #ENTRIES_PER_HEADER} ( {@value #ENTRIES_PER_HEADER})
	 * many items with the same header are allowed. If another header is used in between, the counter is reset.
	 *
	 * @param header  The header (category) of the log entry. This will be used as the parent element name
	 * @param content The content of the log entry.
	 */
	public void add(final String header, final String content)
	{
		Display.getDefault().asyncExec(() -> {
			DateTime now = new DateTime();
			if (header.equals(lastHeader))
			{
				entriesWithThisHeader++;
				if (updateInsteadOfAdd)
				{
					if (new Interval(lastUpdate, now).toDuration().isLongerThan(new Duration(1000)))
					{
						entriesWithThisHeader = 1;
						updateInsteadOfAdd = false;
						moreItem = null;
						lastHeaderItem = new TreeItem(log, SWT.NONE);
						lastHeaderItem.setText(0, header);

						addToParent(lastHeaderItem, content);
					}
					else
					{
						moreItem.setText(1, RB.getString(RB.DIALOG_LOG_MORE, entriesWithThisHeader - ENTRIES_PER_HEADER));
					}
				}
				else if (entriesWithThisHeader > ENTRIES_PER_HEADER)
				{
					updateInsteadOfAdd = true;
					moreItem = new TreeItem(lastHeaderItem, SWT.NONE);
					moreItem.setText(1, RB.getString(RB.DIALOG_LOG_MORE, entriesWithThisHeader - ENTRIES_PER_HEADER));
				}
				else
				{
					addToParent(lastHeaderItem, content);
				}
			}
			else
			{
				lastHeader = header;
				entriesWithThisHeader = 1;
				updateInsteadOfAdd = false;
				moreItem = null;
				lastHeaderItem = new TreeItem(log, SWT.NONE);
				lastHeaderItem.setText(0, header);

				addToParent(lastHeaderItem, content);
			}
			lastUpdate = now;
		});

		SQLLogger.getInstance().log(Level.INFO, content);
	}

	/**
	 * Adds a new {@link TreeItem} to the given parent {@link TreeItem}. The current date will be used for the first column
	 *
	 * @param parent  The parent {@link TreeItem}
	 * @param content The content of the new {@link TreeItem}
	 */
	private void addToParent(TreeItem parent, String content)
	{
		TreeItem item = new TreeItem(parent, SWT.NONE);
		item.setText(0, format.format(new Date(System.currentTimeMillis())));
		item.setText(1, content);

		parent.setExpanded(true);

		log.setTopItem(item);
	}

	public interface VisibilityHandler
	{
		void onVisibilityChanged(boolean visible);
	}
}
