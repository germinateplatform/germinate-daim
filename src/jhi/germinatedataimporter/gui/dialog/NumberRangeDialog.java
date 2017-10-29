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

import java.util.*;

import jhi.germinatedataimporter.gui.i18n.*;
import jhi.germinatedataimporter.gui.viewer.*;
import jhi.germinatedataimporter.util.*;
import jhi.swtcommons.gui.dialog.*;
import jhi.swtcommons.gui.layout.*;
import jhi.swtcommons.util.*;

/**
 * {@link NumberRangeDialog} is a {@link Dialog} to choose a {@link NumberRange}
 *
 * @author Sebastian Raubach
 */
public class NumberRangeDialog extends I18nDialog
{
	private Collection<NumberRange> previousRanges;
	private NumberRangeTableViewer  viewer;

	/**
	 * Creates a new instance of {@link NumberRangeDialog} with the given parent {@link Shell} and the {@link NumberRange} to restore
	 *
	 * @param parentShell    The parent {@link Shell}
	 * @param previousRanges The {@link Collection} of {@link NumberRange}s to restore (can be <code>null</code>)
	 */
	public NumberRangeDialog(Shell parentShell, Collection<NumberRange> previousRanges)
	{
		super(parentShell);
		setBlockOnOpen(true);

		this.previousRanges = previousRanges;
	}

	@Override
	protected Control createDialogArea(Composite parent)
	{
		final Composite composite = (Composite) super.createDialogArea(parent);

		Label text = new Label(composite, SWT.WRAP);
		text.setText(RB.getString(RB.DIALOG_NUMBER_RANGE_DESCRIPTION, Double.toString(1234567.890)));

		viewer = new NumberRangeTableViewer(composite, SWT.NO_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);

		GridLayoutUtils.useDefault().applyTo(composite);
		GridDataUtils.usePredefined(GridDataUtils.GridDataStyle.FILL_BOTH).widthHint(300 * Resources.getZoomFactor() / 100).applyTo(composite);
		GridDataUtils.usePredefined(GridDataUtils.GridDataStyle.FILL_TOP).applyTo(text);
		GridDataUtils.usePredefined(GridDataUtils.GridDataStyle.FILL_BOTH).heightHint(100 * Resources.getZoomFactor() / 100).applyTo(viewer.getTable());

		restoreNumberRanges();

		return composite;
	}

	/**
	 * Restores the previously selected {@link NumberRange}
	 */
	private void restoreNumberRanges()
	{
		if (CollectionUtils.isEmpty(previousRanges))
			return;

		viewer.setInput(previousRanges);
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
		shell.setText(RB.getString(RB.DIALOG_NUMBER_RANGE_TITLE));
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
		@SuppressWarnings("unchecked")
		Collection<NumberRange> ranges = (Collection<NumberRange>) viewer.getInput();

		if (ranges != null)
		{
			long invalidCount = ranges.stream()
									  .filter(r -> r.min > r.max)
									  .count();

			if (invalidCount > 0)
			{
				DialogUtils.showError(RB.getString(RB.ERROR_NUMBER_RANGE_MIN_LARGER_THAN_MAX));
				return;
			}
		}

		previousRanges = ranges;

		super.okPressed();
	}

	/**
	 * Returns the {@link Collection} of selected {@link NumberRange}s
	 *
	 * @return The {@link Collection} of selected {@link NumberRange}s
	 */
	public Collection<NumberRange> getNumberRange()
	{
		return previousRanges;
	}

	/**
	 * DTO representing a number range (interval between two doubles)
	 *
	 * @author Sebastian Raubach
	 */
	public static class NumberRange
	{
		/** The minimum of the {@link NumberRange} (inclusive) */
		public double min;
		/** The maximum of the {@link NumberRange} (inclusive) */
		public double max;

		/**
		 * Creates a new instance of {@link NumberRange} with the given min and max values
		 *
		 * @param min The minimum of the {@link NumberRange} (inclusive)
		 * @param max The maximum of the {@link NumberRange} (inclusive)
		 */
		public NumberRange(double min, double max)
		{
			this.min = min;
			this.max = max;
		}

		/**
		 * Creates a new instance of {@link NumberRange} with {@link #min} and {@link #max} set to 0.
		 */
		public NumberRange()
		{
			this.min = 0;
			this.max = 0;
		}

		@Override
		public String toString()
		{
			return "NumberRange [min=" + min + ", max=" + max + "]";
		}
	}
}
