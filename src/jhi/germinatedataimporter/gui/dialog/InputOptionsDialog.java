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

import java.text.*;
import java.util.*;

import jhi.germinatedataimporter.gui.i18n.*;
import jhi.germinatedataimporter.gui.viewer.*;
import jhi.germinatedataimporter.gui.widget.ColumnMapper.*;
import jhi.germinatedataimporter.util.*;
import jhi.swtcommons.gui.dialog.*;
import jhi.swtcommons.gui.layout.*;
import jhi.swtcommons.util.*;

/**
 * {@link InputOptionsDialog} is a {@link Dialog} to select settings for the input files
 *
 * @author Sebastian Raubach
 */
public class InputOptionsDialog extends I18nDialog
{
	private FileSeparatorComboViewer separatorCombo;
	private InputLocaleComboViewer   numberFormatCombo;
	private Text                     exampleText;

	private double exampleNumber = 1234567.89;
	private DecimalFormat numberFormatter;
	private Button        removeSpaces;
	private Boolean       removeSpacesBoolean;
	private Button        askAgain;
	private Boolean       askAgainBoolean;

	private InputOptions options;

	private FileSeparator originalSeparator;

	public InputOptionsDialog(Shell parentShell, InputOptions options)
	{
		super(parentShell);
		setBlockOnOpen(true);

		this.options = options;
	}

	/**
	 * Restores the previous selection. If there is no previous selection, restore from the {@link ParameterStore} ({@link
	 * GerminateParameter#inputseparator} and {@link GerminateParameter#inputlocale} are always set)
	 */
	private void restorePreviousState()
	{
		GerminateParameterStore store = GerminateParameterStore.getInstance();
		removeSpacesBoolean = (Boolean) store.get(GerminateParameter.removetrailingspaces);
		askAgainBoolean = (Boolean) store.get(GerminateParameter.inputalwaysask);

		if (options.separator == null)
			options.separator = (FileSeparator) store.get(GerminateParameter.inputseparator);

		separatorCombo.setSelection(new StructuredSelection(options.separator));

		if (options.locale == null)
			options.locale = (Locale) store.get(GerminateParameter.inputlocale);

		numberFormatter = (DecimalFormat) DecimalFormat.getInstance(options.locale);
		exampleText.setText(numberFormatter.format(exampleNumber));

		if (removeSpacesBoolean == null)
			removeSpacesBoolean = true;

		removeSpaces.setSelection(removeSpacesBoolean);

		if (askAgainBoolean == null)
			askAgainBoolean = true;

		askAgain.setSelection(askAgainBoolean);

		numberFormatCombo.setSelection(new StructuredSelection(options.locale));

		originalSeparator = options.separator;
	}

	@Override
	protected Control createDialogArea(Composite parent)
	{
		final Composite composite = (Composite) super.createDialogArea(parent);
		GridLayoutUtils.useValues(2, false).applyTo(composite);
		GridDataUtils.usePredefined(GridDataUtils.GridDataStyle.FILL_BOTH).widthHint(300 * Resources.getZoomFactor() / 100).applyTo(composite);

		Label separatorLabel = new Label(composite, SWT.NONE);
		separatorLabel.setText(RB.getString(RB.DIALOG_INPUT_OPTIONS_SEPARATOR));

		separatorCombo = new FileSeparatorComboViewer(composite, SWT.READ_ONLY);

		Label numberFormatLabel = new Label(composite, SWT.NONE);
		numberFormatLabel.setText(RB.getString(RB.DIALOG_INPUT_OPTIONS_NUMBER_FORMAT));

		numberFormatCombo = new InputLocaleComboViewer(composite, SWT.NONE);
		numberFormatCombo.addSelectionChangedListener(e -> {
			Locale locale = numberFormatCombo.getSelectedItem();
			numberFormatter = (DecimalFormat) DecimalFormat.getInstance(locale);
			exampleText.setText(numberFormatter.format(exampleNumber));
		});

		Label exampleLabel = new Label(composite, SWT.NONE);
		exampleLabel.setText(RB.getString(RB.DIALOG_INPUT_OPTIONS_NUMBER_FORMAT_EXAMPLE));

		exampleText = new Text(composite, SWT.BORDER | SWT.READ_ONLY);

		new Label(composite, SWT.NONE);
		new Label(composite, SWT.NONE);

		removeSpaces = new Button(composite, SWT.CHECK);
		removeSpaces.setText(RB.getString(RB.DIALOG_INPUT_OPTIONS_REMOVE_SPACES));
		removeSpaces.addListener(SWT.Selection, event -> removeSpacesBoolean = removeSpaces.getSelection());

		askAgain = new Button(composite, SWT.CHECK);
		askAgain.setText(RB.getString(RB.DIALOG_INPUT_OPTIONS_ASK_AGAIN));
		askAgain.addListener(SWT.Selection, event -> askAgainBoolean = askAgain.getSelection());

		GridDataUtils.usePredefined(GridDataUtils.GridDataStyle.END_CENTER_FALSE).applyTo(separatorLabel);
		GridDataUtils.usePredefined(GridDataUtils.GridDataStyle.FILL_BOTH).applyTo(separatorCombo.getCombo());
		GridDataUtils.usePredefined(GridDataUtils.GridDataStyle.END_CENTER_FALSE).applyTo(numberFormatLabel);
		GridDataUtils.usePredefined(GridDataUtils.GridDataStyle.FILL_BOTH).applyTo(numberFormatCombo.getCombo());
		GridDataUtils.usePredefined(GridDataUtils.GridDataStyle.END_CENTER_FALSE).applyTo(exampleLabel);
		GridDataUtils.usePredefined(GridDataUtils.GridDataStyle.FILL_BOTH).applyTo(exampleText);
		GridDataUtils.usePredefined(GridDataUtils.GridDataStyle.FILL_TOP).horizontalSpan(2).applyTo(removeSpaces);
		GridDataUtils.usePredefined(GridDataUtils.GridDataStyle.FILL_TOP).horizontalSpan(2).applyTo(askAgain);

		restorePreviousState();

		return composite;
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
		shell.setText(RB.getString(RB.DIALOG_INPUT_OPTIONS_TITLE));
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
		if (options.locale == null)
		{
			DialogUtils.showError(RB.getString(RB.ERROR_INPUT_OPTIONS_INVALID_LOCALE));
			return;
		}
		else if (separatorCombo.getSelectedItem() == null)
		{
			DialogUtils.showError(RB.getString(RB.ERROR_INPUT_OPTIONS_INVALID_SEPARATOR));
			return;
		}

		options.locale = numberFormatCombo.getSelectedItem();
		options.separator = separatorCombo.getSelectedItem();

        /* Remember the user selection */
		GerminateParameterStore store = GerminateParameterStore.getInstance();
		store.put(GerminateParameter.removetrailingspaces, removeSpacesBoolean);
		store.put(GerminateParameter.inputalwaysask, askAgainBoolean);
		store.put(GerminateParameter.inputseparator, options.separator);
		store.put(GerminateParameter.inputlocale, options.locale);

		super.okPressed();
	}

	public boolean fileSeparatorChanged()
	{
		return !originalSeparator.equals(options.separator);
	}

	public InputOptions getInputOptions()
	{
		return options;
	}
}
