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
 * {@link MatrixSourcElementViewer} extends {@link AdvancedComboViewer} and displays {@link MatrixSourceElement}s
 *
 * @author Sebastian Raubach
 */
public class MatrixSourcElementViewer extends AdvancedComboViewer<MatrixSourceElement>
{
	public MatrixSourcElementViewer(Composite parent, int style)
	{
		super(parent, style | SWT.READ_ONLY);

		this.setLabelProvider(new LabelProvider()
		{
			@Override
			public String getText(Object element)
			{
				if (element instanceof MatrixSourceElement)
				{
					return getDisplayText((MatrixSourceElement) element);
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
		setInput(MatrixSourceElement.values());
	}

	@Override
	protected String getDisplayText(MatrixSourceElement item)
	{
		return item.getName();
	}
}
