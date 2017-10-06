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

package jhi.germinatedataimporter.gui.widget;

import org.eclipse.swt.*;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

import jhi.swtcommons.util.*;

/**
 * {@link Hyperlink} is a wrapper for {@link CLabel} that takes care of the {@link Cursor} and {@link Color}. It also handles {@link SWT#MouseUp}
 * events and uses the tooltip to open the given URL/email using {@link OSUtils#open(String)}
 *
 * @author Sebastian Raubach
 */
public class Hyperlink
{
	private CLabel label;
	private boolean isEmail = false;

	/**
	 * Creates a new instance of {@link Hyperlink} with the given parent {@link Composite} and the given style bits
	 *
	 * @param parent The parent {@link Composite}
	 * @param style  The style bits
	 */
	public Hyperlink(Composite parent, int style)
	{
		label = new CLabel(parent, style);
		label.setCursor(parent.getShell().getDisplay().getSystemCursor(SWT.CURSOR_HAND));
		label.setForeground(parent.getShell().getDisplay().getSystemColor(SWT.COLOR_LINK_FOREGROUND));
		label.addListener(SWT.MouseUp, e -> {
			String link = label.getToolTipText();

			if (StringUtils.isEmpty(link))
				return;

			if (isEmail)
				link = "mailto:" + link;
			OSUtils.open(link);
		});
	}

	/**
	 * Returns the contained {@link CLabel}
	 *
	 * @return The contained {@link CLabel}
	 */
	public CLabel getControl()
	{
		return label;
	}

	/**
	 * Set if the {@link Hyperlink} represents an email address or not
	 *
	 * @param isEmail Set to <code>true</code> of this {@link Hyperlink} represents an email address
	 */
	public void setIsEmail(boolean isEmail)
	{
		this.isEmail = isEmail;
	}

	/**
	 * Set the label's text. The value <code>null</code> clears it.
	 *
	 * @param text the text to be displayed in the label or null
	 * @see CLabel#setText(String)
	 */
	public void setText(String text)
	{
		label.setText(text);
	}

	/**
	 * Sets the receiver's tool tip text to the argument, which may be null indicating that the default tool tip for the control will be shown.
	 *
	 * @param text the new tool tip text (or <code>null</code>)
	 * @see CLabel#setToolTipText(String)
	 */
	public void setToolTipText(String text)
	{
		label.setToolTipText(text);
	}

	public void setImage(Image image)
	{
		label.setImage(image);
	}
}
