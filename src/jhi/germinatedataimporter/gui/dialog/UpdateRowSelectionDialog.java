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
import java.util.stream.*;

import jhi.germinatedataimporter.database.entities.*;
import jhi.germinatedataimporter.database.entities.DatabaseColumn.*;
import jhi.germinatedataimporter.gui.viewer.*;
import jhi.germinatedataimporter.gui.widget.*;
import jhi.germinatedataimporter.util.*;
import jhi.swtcommons.gui.dialog.*;
import jhi.swtcommons.gui.layout.*;
import jhi.swtcommons.util.*;

/**
 * {@link UpdateRowSelectionDialog} is a {@link Dialog} to create {@link Condition}s for {@link DatabaseColumn}s
 *
 * @author Sebastian Raubach
 */
public class UpdateRowSelectionDialog extends I18nDialog
{
	private String                                               title;
	private String                                               message;
	private List<SimpleColumnMapperRow.SimpleColumnMapperRowDTO> rows;
	private RowListViewer                                        rowListViewer;

	public UpdateRowSelectionDialog(Shell parentShell, String title, String message, List<SimpleColumnMapperRow.SimpleColumnMapperRowDTO> rows)
	{
		super(parentShell);
		setBlockOnOpen(true);

		this.title = title;
		this.message = message;
		this.rows = rows.stream()
						.filter(r -> r.columnDatabase != null)
						.collect(Collectors.toList());
	}

	@Override
	protected Control createDialogArea(Composite parent)
	{
		final Composite composite = (Composite) super.createDialogArea(parent);

		Label label = new Label(composite, SWT.WRAP);
		label.setText(message);

		rowListViewer = new RowListViewer(composite, SWT.MULTI);
		rowListViewer.setInput(rows);

		GridLayoutUtils.useValues(1, false).applyTo(composite);
		GridDataUtils.usePredefined(GridDataUtils.GridDataStyle.FILL_BOTH).widthHint(300 * Resources.getZoomFactor() / 100).applyTo(composite);
		GridDataUtils.usePredefined(GridDataUtils.GridDataStyle.FILL_TOP).applyTo(label);
		GridDataUtils.usePredefined(GridDataUtils.GridDataStyle.FILL_BOTH).applyTo(rowListViewer.getControl());

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
		shell.setText(title);
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
		List<SimpleColumnMapperRow.SimpleColumnMapperRowDTO> selectedItems = rowListViewer.getSelectedItems();

		for (SimpleColumnMapperRow.SimpleColumnMapperRowDTO dto : selectedItems)
		{
			dto.toUpdate = true;
		}

		super.okPressed();
	}
}
