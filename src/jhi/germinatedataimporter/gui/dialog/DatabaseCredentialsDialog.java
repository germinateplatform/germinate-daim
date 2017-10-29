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
import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

import java.util.List;

import jhi.database.server.*;
import jhi.database.shared.exception.*;
import jhi.germinatedataimporter.database.*;
import jhi.germinatedataimporter.database.entities.*;
import jhi.germinatedataimporter.gui.i18n.*;
import jhi.germinatedataimporter.util.*;
import jhi.swtcommons.gui.dialog.*;
import jhi.swtcommons.gui.layout.*;
import jhi.swtcommons.util.*;

/**
 * {@link DatabaseCredentialsDialog} extends {@link Dialog} and asks the user for the database credentials. <p> On {@link #okPressed()} it will
 * attempt a connection. If this is successful, it will store the entered data in the {@link ParameterStore}, if not, it will show an error message.
 *
 * @author Sebastian Raubach
 */
public class DatabaseCredentialsDialog extends BannerDialog implements Listener
{
	private String serverString;
	private String databaseString;
	private String portString;
	private String usernameString;
	private String passwordString;

	private Combo  serverText;
	private Combo  databaseText;
	private Text   portText;
	private Text   usernameText;
	private Text   passwordText;
	private Button usePort;

	public DatabaseCredentialsDialog(Shell parentShell)
	{
		super(parentShell, Resources.Images.GERMINATE_LOGO, Resources.Colors.DARK_GREY);
		setBlockOnOpen(true);
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent)
	{
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

	@Override
	protected void okPressed()
	{
		serverString = serverText.getText();
		databaseString = databaseText.getText();
		portString = portText.getText();
		usernameString = usernameText.getText();
		passwordString = passwordText.getText();

		if (StringUtils.isEmpty(serverString, databaseString, usernameString))
		{
			DialogUtils.showError(RB.getString(RB.DIALOG_DB_CRED_ERROR_MESSAGE));
			return;
		}
		else if (usePort.getSelection() && StringUtils.isEmpty(portString))
		{
			DialogUtils.showError(RB.getString(RB.DIALOG_DB_CRED_ERROR_MESSAGE_PORT));
			return;
		}
		else
		{
			/* Try to connect */
			try
			{
				String connectionString = DatabaseUtils.getServerString(serverString, databaseString, portString);
				/* Test the connection first */
				Database.testConnect(connectionString, usernameString, passwordString);
				/* Then set the parameters */
				Database.init(connectionString, usernameString, passwordString);
			}
			catch (DatabaseException e)
			{
				DialogUtils.handleException(e);
				return;
			}

            /* Store the selection */
			GerminateParameterStore store = GerminateParameterStore.getInstance();
			store.put(GerminateParameter.server, serverString);
			store.put(GerminateParameter.database, new DatabaseDatabase(databaseString));
			store.put(GerminateParameter.port, portString);
			store.put(GerminateParameter.username, usernameString);
			store.put(GerminateParameter.password, passwordString);

			@SuppressWarnings("unchecked")
			List<String> servers = (List<String>) store.get(GerminateParameter.servers);
			@SuppressWarnings("unchecked")
			List<String> databases = (List<String>) store.get(GerminateParameter.databases);

			if (!servers.contains(serverString))
				servers.add(serverString);
			else
				servers.add(servers.remove(servers.indexOf(serverString)));
			if (!databases.contains(databaseString))
				databases.add(databaseString);
			else
				databases.add(databases.remove(databases.indexOf(databaseString)));

			super.okPressed();
		}
	}

	@Override
	protected void configureShell(Shell shell)
	{
		super.configureShell(shell);
		shell.setText(RB.getString(RB.DIALOG_DB_CRED_TITLE));
	}

	@Override
	protected Point getInitialLocation(Point initialSize)
	{
		/* Center the dialog based on the parent */
		return ShellUtils.getLocationCenteredTo(getParentShell(), initialSize);
	}

	@Override
	public void handleEvent(Event e)
	{
		if (e.widget instanceof Text)
			((Text) e.widget).selectAll();
	}

	@Override
	protected void createContent(Composite parent)
	{
		GerminateParameterStore store = GerminateParameterStore.getInstance();
		serverString = store.getAsString(GerminateParameter.server, "");
		databaseString = store.getAsString(GerminateParameter.database, "");
		portString = store.getAsString(GerminateParameter.port, "");
		usernameString = store.getAsString(GerminateParameter.username, "");
		passwordString = store.getAsString(GerminateParameter.password, "");

		@SuppressWarnings("unchecked")
		List<String> servers = (List<String>) store.get(GerminateParameter.servers);
		String[] serverStrings = servers.toArray(new String[servers.size()]);
		@SuppressWarnings("unchecked")
		List<String> databases = (List<String>) store.get(GerminateParameter.databases);
		String[] databaseStrings = databases.toArray(new String[databases.size()]);

        /* SERVER */
		Label serverLabel = new Label(parent, SWT.NONE);
		serverLabel.setText(RB.getString(RB.DIALOG_DB_CRED_SERVER));
		serverText = new Combo(parent, SWT.BORDER);
		serverText.setItems(serverStrings);
		serverText.select(servers.indexOf(serverString));
		serverText.addListener(SWT.FocusIn, this);
		new Label(parent, SWT.NONE);

        /* DATABASE */
		Label databaseLabel = new Label(parent, SWT.NONE);
		databaseLabel.setText(RB.getString(RB.DIALOG_DB_CRED_DATABASE));
		databaseText = new Combo(parent, SWT.BORDER);
		databaseText.setItems(databaseStrings);
		databaseText.select(databases.indexOf(databaseString));
		databaseText.addListener(SWT.FocusIn, this);
		new Label(parent, SWT.NONE);

        /* PORT */
		Label portLabel = new Label(parent, SWT.NONE);
		portLabel.setText(RB.getString(RB.DIALOG_DB_CRED_PORT));
		portText = new Text(parent, SWT.BORDER);
		portText.setText(portString);
		portText.setEnabled(!StringUtils.isEmpty(portString));
		portText.addListener(SWT.FocusIn, this);
		/* Validate the input */
		portText.addListener(SWT.Verify, event -> {
			final String old = portText.getText();
			final String current = old.substring(0, event.start) + event.text + old.substring(event.end);

			/* Empty text is allowed */
			if (current.equals(""))
				return;

			try
			{
				/* We only allow integers */
				int value = Integer.parseInt(current);

				/* In the range of 0-65535 */
				if (value < 0 || value > 65535)
					event.doit = false;
			}
			catch (NumberFormatException e)
			{
				/* Not an integer? Don't do it! */
				event.doit = false;
			}
		});

		usePort = new Button(parent, SWT.CHECK);
		usePort.setSelection(!StringUtils.isEmpty(portString));
		usePort.addListener(SWT.Selection, e -> {
			/* Enable/disable the text field and clear it */
			portText.setEnabled(!portText.getEnabled());
			portText.setText("");
		});

        /* USERNAME */
		Label usernameLabel = new Label(parent, SWT.NONE);
		usernameLabel.setText(RB.getString(RB.DIALOG_DB_CRED_USERNAME));
		usernameText = new Text(parent, SWT.BORDER);
		usernameText.setText(usernameString);
		usernameText.addListener(SWT.FocusIn, this);
		new Label(parent, SWT.NONE);

        /* PASSWORD */
		Label passwordLabel = new Label(parent, SWT.NONE);
		passwordLabel.setText(RB.getString(RB.DIALOG_DB_CRED_PASSWORD));
		passwordText = new Text(parent, SWT.BORDER | SWT.PASSWORD);
		passwordText.setText(passwordString);
		passwordText.addListener(SWT.FocusIn, this);
		new Label(parent, SWT.NONE);

		Control[] tabList = new Control[]{serverText, databaseText, portText, usernameText, passwordText};

		parent.setTabList(tabList);

        /* Now we want to set the focus to the first control that is a Text,
		 * isn't disabled and is empty */
		for (Control control : tabList)
		{
			/* If it's enabled and a Text */
			if (control.isEnabled() && control instanceof Text)
			{
				/* If the content is empty */
				if (StringUtils.isEmpty(((Text) control).getText()))
				{
					/* Force the focus and break the loop */
					control.setFocus();
					control.forceFocus();
					break;
				}
			}
			else if (control.isEnabled() && control instanceof Combo)
			{
				/* If the content is empty */
				if (StringUtils.isEmpty(((Combo) control).getText()))
				{
					/* Force the focus and break the loop */
					control.setFocus();
					control.forceFocus();
					break;
				}
			}
		}

		GridLayoutUtils.useValues(3, false).applyTo(parent);

		GridDataUtils.usePredefined(GridDataUtils.GridDataStyle.FILL_BOTH).widthHint(300 * Resources.getZoomFactor() / 100).applyTo(parent);
		GridDataUtils.usePredefined(GridDataUtils.GridDataStyle.END_CENTER_FALSE).applyTo(serverLabel);
		GridDataUtils.usePredefined(GridDataUtils.GridDataStyle.FILL_BOTH).applyTo(serverText);
		GridDataUtils.usePredefined(GridDataUtils.GridDataStyle.END_CENTER_FALSE).applyTo(databaseLabel);
		GridDataUtils.usePredefined(GridDataUtils.GridDataStyle.FILL_BOTH).applyTo(databaseText);
		GridDataUtils.usePredefined(GridDataUtils.GridDataStyle.END_CENTER_FALSE).applyTo(portLabel);
		GridDataUtils.usePredefined(GridDataUtils.GridDataStyle.FILL_BOTH).applyTo(portText);
		GridDataUtils.usePredefined(GridDataUtils.GridDataStyle.END_CENTER_FALSE).applyTo(usernameLabel);
		GridDataUtils.usePredefined(GridDataUtils.GridDataStyle.FILL_BOTH).applyTo(usernameText);
		GridDataUtils.usePredefined(GridDataUtils.GridDataStyle.END_CENTER_FALSE).applyTo(passwordLabel);
		GridDataUtils.usePredefined(GridDataUtils.GridDataStyle.FILL_BOTH).applyTo(passwordText);
	}
}
