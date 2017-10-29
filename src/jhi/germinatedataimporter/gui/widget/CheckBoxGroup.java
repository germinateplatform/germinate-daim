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
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

import java.util.*;
import java.util.List;

/**
 * Instances of this class provide an etched border with a title and a checkbox. If the checkbox is checked, the content of the composite is enabled.
 * If the checkbox is unchecked, the content of the composite is disabled, thus not editable. <p> <dl> <dt><b>Styles:</b></dt> <dd>BORDER</dd>
 * <dt><b>Events:</b></dt> <dd>(none)</dd> </dl>
 */
@SuppressWarnings("unused")
public class CheckBoxGroup extends Canvas implements PaintListener
{
	private final Composite               content;
	private final List<SelectionListener> selectionListeners;
	protected     Button                  button;
	private boolean transparent = false;

	/**
	 * Constructs a new instance of this class given its parent and a style value describing its behavior and appearance. <p> The style value is
	 * either one of the style constants defined in class <code>SWT</code> which is applicable to instances of this class, or must be built by
	 * <em>bitwise OR</em>'ing together (that is, using the <code>int</code> "|" operator) two or more of those <code>SWT</code> style constants. The
	 * class description lists the style constants that are applicable to the class. Style bits are also inherited from superclasses. </p>
	 *
	 * @param parent a widget which will be the parent of the new instance (cannot be null)
	 * @param style  the style of widget to construct
	 * @throws IllegalArgumentException <ul> <li>ERROR_NULL_ARGUMENT - if the parent is null</li> </ul>
	 * @throws SWTException             <ul> <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the parent</li> </ul>
	 * @see Composite#Composite(Composite, int)
	 * @see SWT#BORDER
	 * @see Widget#getStyle
	 */
	public CheckBoxGroup(final Composite parent, final int style)
	{
		super(parent, style);
		super.setLayout(new GridLayout());

		this.selectionListeners = new ArrayList<>();

		createCheckBoxButton();

		this.content = new Composite(this, style);
		this.content.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));

		this.addPaintListener(this);
	}

	/**
	 * Enable all widgets of a control
	 *
	 * @param control control to enable/disable
	 */
	public static void enableAllChildrenWidgets(final Control control)
	{
		if (control instanceof Composite)
		{
			for (final Control c : ((Composite) control).getChildren())
			{
				enableAllChildrenWidgets(c);
			}
		}
		control.setEnabled(true);
	}

	/**
	 * Disable all widgets of a control
	 *
	 * @param control control to enable/disable
	 */
	public static void disableAllChildrenWidgets(final Control control)
	{
		if (control instanceof Composite)
		{
			for (final Control c : ((Composite) control).getChildren())
			{
				disableAllChildrenWidgets(c);
			}
		}
		control.setEnabled(false);
	}

	private void createCheckBoxButton()
	{
		this.button = new Button(this, SWT.CHECK);
		final GridData gdButton = new GridData(GridData.BEGINNING, GridData.CENTER, true, false);
		gdButton.horizontalIndent = 15;
		this.button.setLayoutData(gdButton);
		this.button.setSelection(true);
		this.button.pack();

		this.button.addSelectionListener(new SelectionAdapter()
		{
			/**
			 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
			 */
			@Override
			public void widgetSelected(final SelectionEvent e)
			{
				e.doit = fireSelectionListeners(e);
				if (!e.doit)
				{
					return;
				}
				if (CheckBoxGroup.this.button.getSelection())
				{
					CheckBoxGroup.this.activate();
				}
				else
				{
					CheckBoxGroup.this.deactivate();
				}
			}
		});
	}

	/**
	 * Fire the selection listeners
	 *
	 * @param selectionEvent mouse event
	 * @return true if the selection could be changed, false otherwise
	 */
	private boolean fireSelectionListeners(final SelectionEvent selectionEvent)
	{
		selectionEvent.widget = this;
		for (final SelectionListener listener : this.selectionListeners)
		{
			listener.widgetSelected(selectionEvent);
			if (!selectionEvent.doit)
			{
				return false;
			}
		}
		return true;
	}

	/**
	 * Activate the content
	 */
	public void activate()
	{
		this.button.setSelection(true);
		enableAllChildrenWidgets(this.content);
	}

	/**
	 * Adds the listener to the collection of listeners who will be notified when the user changes the receiver's selection, by sending it one of the
	 * messages defined in the <code>SelectionListener</code> interface. <p> When <code>widgetSelected</code> is called, the item field of the event
	 * object is valid. If the receiver has the <code>SWT.CHECK</code> style and the check selection changes, the event object detail field contains
	 * the value <code>SWT.CHECK</code>. <code>widgetDefaultSelected</code> is typically called when an item is double-clicked. The item field of the
	 * event object is valid for default selection, but the detail field is not used. </p>
	 *
	 * @param listener the listener which should be notified when the user changes the receiver's selection
	 * @throws IllegalArgumentException <ul> <li>ERROR_NULL_ARGUMENT - if the listener is null</li> </ul>
	 * @throws SWTException             <ul> <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li> <li>ERROR_THREAD_INVALID_ACCESS - if
	 *                                  not called from the thread that created the receiver</li> </ul>
	 * @see SelectionListener
	 * @see #removeSelectionListener
	 * @see SelectionEvent
	 */
	public void addSelectionListener(final SelectionListener listener)
	{
		checkWidget();
		this.selectionListeners.add(listener);
	}

	/**
	 * Deactivate the content
	 */
	public void deactivate()
	{
		this.button.setSelection(false);
		disableAllChildrenWidgets(this.content);
	}

	/**
	 * @return <code>true</code> if the content is activated, <code>false</code> otherwise
	 */
	public boolean isActivated()
	{
		return this.button.getSelection();
	}

	/**
	 * @see org.eclipse.swt.widgets.Composite#getLayout()
	 */
	@Override
	public Layout getLayout()
	{
		return this.content.getLayout();
	}

	/**
	 * @see org.eclipse.swt.widgets.Composite#setLayout(org.eclipse.swt.widgets.Layout)
	 */
	@Override
	public void setLayout(final Layout layout)
	{
		this.content.setLayout(layout);
	}

	// ------------------------------------ Getters and Setters

	/**
	 * Removes the listener from the collection of listeners who will be notified when the user changes the receiver's selection.
	 *
	 * @param listener the listener which should no longer be notified
	 * @throws IllegalArgumentException <ul> <li>ERROR_NULL_ARGUMENT - if the listener is null</li> </ul>
	 * @throws SWTException             <ul> <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li> <li>ERROR_THREAD_INVALID_ACCESS - if
	 *                                  not called from the thread that created the receiver</li> </ul>
	 * @see SelectionListener
	 * @see #addSelectionListener
	 */
	public void removeSelectionListener(final SelectionListener listener)
	{
		checkWidget();
		this.selectionListeners.remove(listener);
	}

	/**
	 * @see org.eclipse.swt.widgets.Composite#setFocus()
	 */
	@Override
	public boolean setFocus()
	{
		return this.content.setFocus();
	}

	/**
	 * @return the text of the button
	 */
	public String getText()
	{
		return this.button.getText();
	}

	/**
	 * @param text the text of the button to set
	 */
	public void setText(final String text)
	{
		this.button.setText(text);
	}

	/**
	 * @return the font of the button
	 */
	@Override
	public Font getFont()
	{
		return this.button.getFont();
	}

	/**
	 * @param font the font to set
	 */
	@Override
	public void setFont(final Font font)
	{
		this.button.setFont(font);
	}

	/**
	 * @return the content of the group
	 */
	public Composite getContent()
	{
		return this.content;
	}

	public boolean isTransparent()
	{
		return this.transparent;
	}

	public void setTransparent(final boolean transparent)
	{
		this.transparent = transparent;
		if (transparent)
		{
			setBackgroundMode(SWT.INHERIT_DEFAULT);
			this.content.setBackgroundMode(SWT.INHERIT_DEFAULT);
		}
	}

	@Override
	public void paintControl(final PaintEvent paintEvent)
	{
		if (paintEvent.widget == this)
		{
			drawWidget(paintEvent.gc);
		}
	}

	/**
	 * Draws the widget
	 */
	private void drawWidget(final GC gc)
	{
		final Rectangle rect = this.getClientArea();
		final int margin = (int) (this.button.getSize().y * 1.5);
		final int startY = margin / 2;

		gc.setForeground(this.getDisplay().getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW));
		gc.drawRoundRectangle(1, startY, rect.width - 2, rect.height - startY - 2, 2, 2);

		gc.setForeground(this.getDisplay().getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
		gc.drawRoundRectangle(2, startY + 1, rect.width - 4, rect.height - startY - 4, 2, 2);
	}

	public Button getCheckBox()
	{
		return button;
	}
}
