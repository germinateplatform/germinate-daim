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

package jhi.germinatedataimporter.gui.viewer;

import org.eclipse.jface.viewers.*;
import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;

import java.util.*;
import java.util.List;

import jhi.swtcommons.gui.viewer.*;

/**
 * {@link InputLocaleComboViewer} extends {@link AdvancedComboViewer} and displays {@link Locale}s
 *
 * @author Sebastian Raubach
 */
public class InputLocaleComboViewer extends AdvancedComboViewer<Locale>
{
	public InputLocaleComboViewer(Composite parent, int style)
	{
		super(parent, style | SWT.READ_ONLY);

		this.setLabelProvider(new LabelProvider()
		{
			@Override
			public String getText(Object element)
			{
				if (element instanceof Locale)
				{
					return getDisplayText((Locale) element);
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
		List<Locale> locales = new ArrayList<>(Arrays.asList(Locale.getAvailableLocales()));
		Collections.sort(locales, (o1, o2) -> o1.getDisplayName().compareTo(o2.getDisplayName()));
		setInput(locales);
	}

	@Override
	protected String getDisplayText(Locale item)
	{
		return item.getDisplayName();
	}
}
