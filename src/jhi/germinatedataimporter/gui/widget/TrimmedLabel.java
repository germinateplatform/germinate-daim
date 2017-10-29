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

package jhi.germinatedataimporter.gui.widget;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

import jhi.swtcommons.gui.layout.*;
import jhi.swtcommons.util.*;

/**
 * {@link TrimmedLabel} is {@link Label} that trims the contained text to fit into the available space
 *
 * @author Sebastian Raubach
 */
public class TrimmedLabel
{
	private Label label;
	private int trim = SWT.BEGINNING;
	private String text;

	/**
	 * Creates a new instance of {@link TrimmedLabel} with the given parent {@link Composite} and the given style bits
	 *
	 * @param parent The parent {@link Composite}
	 * @param style  The style bits
	 */
	public TrimmedLabel(Composite parent, int style)
	{
		label = new Label(parent, style);

		label.addListener(SWT.Resize, event -> setText(text));

		GridDataUtils.usePredefined(GridDataUtils.GridDataStyle.FILL_CENTER).applyTo(label);
	}

	/**
	 * Sets the text
	 *
	 * @param value The new text
	 */
	public void setText(String value)
	{
		if (label != null && !label.isDisposed() && !StringUtils.isEmpty(value))
		{
			text = value;
			label.setText(shortenText(value, label));
		}
	}

	/**
	 * Set the trim position ({@link SWT#BEGINNING}, {@link SWT#END} or {@link SWT#CENTER}). The text will be trimmed accordingly at the beginning,
	 * the end or the center.
	 *
	 * @param trim {@link SWT#BEGINNING}, {@link SWT#END} or {@link SWT#CENTER}
	 */
	public void setTrim(int trim)
	{
		this.trim = trim;
	}

	/**
	 * Shortens/trims the text to fit into the available space
	 *
	 * @param textValue The text content
	 * @param control   The element determining the available space
	 * @return The shortened/trimmed text
	 */
	private String shortenText(String textValue, Control control)
	{
		if (textValue == null)
		{
			return null;
		}
		GC gc = new GC(control);
		int maxWidth = control.getBounds().width;
		int maxExtent = gc.textExtent(textValue).x;

		if (maxExtent < maxWidth)
		{
			gc.dispose();
			return textValue;
		}
		int length = textValue.length();

		for (int i = 0; i < length; i++)
		{
			String part;
			switch (trim)
			{
				case SWT.END:
					part = textValue.substring(0, length - i) + "...";
					break;
				case SWT.CENTER:
					int index = (int) Math.floor((length - i) / 2);
					part = textValue.substring(0, index) + "..." + textValue.substring(index + i, length);
					break;
				case SWT.BEGINNING:
				default:
					part = "..." + textValue.substring(i, length);
					break;
			}

			if (gc.textExtent(part).x < maxWidth)
			{
				gc.dispose();
				return part;
			}
		}

		gc.dispose();
		return textValue;
	}
}
