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

import jhi.germinatedataimporter.gui.i18n.*;
import jhi.germinatedataimporter.gui.viewer.*;
import jhi.germinatedataimporter.util.*;
import jhi.swtcommons.gui.dialog.*;
import jhi.swtcommons.gui.layout.*;
import jhi.swtcommons.util.*;

/**
 * {@link PreferencesDialog} is a {@link Dialog} to change the Germinate Daim preferences
 *
 * @author Sebastian Raubach
 */
public class PreferencesDialog extends I18nDialog
{
	private boolean changed = false;
	private GUILocaleComboViewer      localeComboViewer;
	private UpdateIntervalComboViewer updateIntervalComboViewer;

	public PreferencesDialog(Shell parentShell)
	{
		super(parentShell);
		setBlockOnOpen(true);
	}

	@Override
	protected Control createDialogArea(Composite parent)
	{
		final Composite composite = (Composite) super.createDialogArea(parent);

		Group localeGroup = new Group(composite, SWT.NONE);
		localeGroup.setText(RB.getString(RB.DIALOG_PREFERENCES_LOCALE));

		localeComboViewer = new GUILocaleComboViewer(localeGroup, SWT.NONE);

		Label restart = new Label(localeGroup, SWT.NONE);
		restart.setText(RB.getString(RB.DIALOG_PREFERENCES_RESTART_REQUIRED));

		Group updateGroup = new Group(composite, SWT.NONE);
		updateGroup.setText(RB.getString(RB.DIALOG_SETTINGS_GENERAL_UPDATE_TITLE));

		Label updateMessage = new Label(updateGroup, SWT.NONE);
		updateMessage.setText(RB.getString(RB.DIALOG_SETTINGS_GENERAL_UPDATE_MESSAGE));

		updateIntervalComboViewer = new UpdateIntervalComboViewer(updateGroup, SWT.NONE);

		GridLayoutUtils.useDefault().applyTo(composite);
		GridLayoutUtils.useDefault().applyTo(localeGroup);
		GridLayoutUtils.useDefault().applyTo(updateGroup);

		GridDataUtils.usePredefined(GridDataUtils.GridDataStyle.FILL_BOTH).widthHint(300 * Resources.getZoomFactor() / 100).applyTo(composite);
		GridDataUtils.usePredefined(GridDataUtils.GridDataStyle.FILL_BOTH).applyTo(localeGroup);
		GridDataUtils.usePredefined(GridDataUtils.GridDataStyle.FILL_TOP).applyTo(updateGroup);
		GridDataUtils.usePredefined(GridDataUtils.GridDataStyle.FILL_BOTH).applyTo(localeComboViewer.getCombo());
		GridDataUtils.usePredefined(GridDataUtils.GridDataStyle.FILL_TOP).applyTo(updateIntervalComboViewer.getCombo());
		GridDataUtils.usePredefined(GridDataUtils.GridDataStyle.END_CENTER).applyTo(restart);

		return composite;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent)
	{
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
	}

	@Override
	protected void configureShell(Shell shell)
	{
		super.configureShell(shell);
		shell.setText(RB.getString(RB.DIALOG_PREFERENCES_TITLE));
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
		GerminateParameterStore store = GerminateParameterStore.getInstance();
		store.put(GerminateParameter.updateInterval, updateIntervalComboViewer.getSelectedItem());

		changed = localeComboViewer.isChanged();

		if (changed)
		{
			DialogUtils.showQuestion(RB.getString(RB.DIALOG_PREFERENCES_DO_RESTART), result -> {
				changed = result;

				if (result)
				{
					/* Store in the ParameterStore */
					store.put(GerminateParameter.locale, localeComboViewer.getSelectedItem());
				}

				PreferencesDialog.super.okPressed();
			});
		}
		else
		{
			super.okPressed();
		}
	}

	public boolean isChanged()
	{
		return changed;
	}
}
