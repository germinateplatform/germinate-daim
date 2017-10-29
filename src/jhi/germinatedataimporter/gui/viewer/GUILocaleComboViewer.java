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

import java.util.*;
import java.util.List;

import jhi.germinatedataimporter.gui.i18n.*;
import jhi.germinatedataimporter.util.*;
import jhi.swtcommons.gui.viewer.*;

/**
 * {@link GUILocaleComboViewer} extends {@link AdvancedComboViewer} and displays {@link Locale}s for the GUI. It will update {@link
 * GerminateParameter#locale} after selection
 *
 * @author Sebastian Raubach
 */
public class GUILocaleComboViewer extends AdvancedComboViewer<Locale>
{
	private boolean changed       = false;
	private Locale  prevSelection = null;

	public GUILocaleComboViewer(Composite parent, int style)
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

        /* Listen for selection changes */
		this.addSelectionChangedListener(e -> {
			Locale selection = getSelectedItem();

			if (!selection.equals(prevSelection))
				changed = true;
		});
	}

	/**
	 * Fill the {@link ComboViewer}
	 */
	private void fill()
	{
		/* Get all the supported Locales */
		List<Locale> locales = RB.SUPPORTED_LOCALES;
		Collections.sort(locales, (o1, o2) -> o1.getDisplayName().compareTo(o2.getDisplayName()));
		setInput(locales);

        /* Select the first element (or the currently stored one) */
		Locale locale = (Locale) GerminateParameterStore.getInstance().get(GerminateParameter.locale);
		if (locale != null)
			prevSelection = locale;
		else
			prevSelection = locales.get(0);
		setSelection(new StructuredSelection(prevSelection));
	}

	/**
	 * Returns <code>true</code> if the user changed the selection, <code>false</code> otherwise
	 *
	 * @return <code>true</code> if the user changed the selection, <code>false</code> otherwise
	 */
	public boolean isChanged()
	{
		return changed;
	}

	@Override
	protected String getDisplayText(Locale item)
	{
		return item.getDisplayName(item);
	}
}
