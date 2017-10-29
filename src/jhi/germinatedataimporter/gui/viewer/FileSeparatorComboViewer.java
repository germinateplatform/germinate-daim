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

package jhi.germinatedataimporter.gui.viewer;

import org.eclipse.jface.viewers.*;
import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;

import jhi.germinatedataimporter.util.*;
import jhi.swtcommons.gui.viewer.*;

/**
 * {@link FileSeparatorComboViewer} extends {@link AdvancedComboViewer} and displays {@link FileSeparator}s
 *
 * @author Sebastian Raubach
 */
public class FileSeparatorComboViewer extends AdvancedComboViewer<FileSeparator>
{
	public FileSeparatorComboViewer(Composite parent, int style)
	{
		super(parent, style | SWT.READ_ONLY);

		this.setLabelProvider(new LabelProvider()
		{
			@Override
			public String getText(Object element)
			{
				if (element instanceof FileSeparator)
				{
					return getDisplayText((FileSeparator) element);
				}
				else
				{
					return super.getText(element);
				}
			}
		});

		fill();
	}

	private void fill()
	{
		setInput(FileSeparator.values());

        /* Select the first element */
		FileSeparator separator = (FileSeparator) GerminateParameterStore.getInstance().get(GerminateParameter.inputseparator);
		if (separator != null)
			setSelection(new StructuredSelection(separator));
		else
			setSelection(new StructuredSelection(FileSeparator.values()[0]));
	}

	@Override
	protected String getDisplayText(FileSeparator item)
	{
		return item.getName();
	}
}
