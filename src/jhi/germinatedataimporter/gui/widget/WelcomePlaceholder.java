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

import jhi.germinatedataimporter.gui.i18n.*;
import jhi.germinatedataimporter.util.*;
import jhi.swtcommons.gui.layout.*;

/**
 * {@link WelcomePlaceholder} is a {@link Composite} that acts as a welcome page for the user when the application is first started. <p> It contains a
 * welcome message, a description, both the JHI and Germinate logo and the usual JHI background.
 *
 * @author Sebastian Raubach
 */
public class WelcomePlaceholder extends Composite
{
	private Image scaled;

	public WelcomePlaceholder(Composite parent, int style)
	{
		super(parent, style);

        /* Set a background image */
		final Image image = Resources.Images.HUTTON_BACKGROUND;
		this.setBackgroundImage(image);
		this.setBackgroundMode(SWT.INHERIT_FORCE);

        /* Listen for resize events and scale the background image */
		this.addListener(SWT.Resize, event -> {
			/* Check if we gained or lost weight... */
			Point size = WelcomePlaceholder.this.getSize();

			/* Since the size can actually reach 0, but the Image
			 * constructor fails for 0, we catch this here */
			if (size.x <= 0 || size.y <= 0)
				return;

			/* Create a new Image in the correct size */
			// Point newSize = getAspectRatioSize(size, image.getBounds());
			Point newSize = size;
			Image newScaled = new Image(Display.getDefault(), newSize.x, newSize.y);
			GC gc = new GC(newScaled);
			gc.setAntialias(SWT.ON);
			gc.setInterpolation(SWT.HIGH);
			/* Scale the original image to the new size */
			gc.drawImage(image, 0, 0, image.getBounds().width, image.getBounds().height, 0, 0, newSize.x, newSize.y);
			/* You create it, you dispose it */
			gc.dispose();

			/* Remember to dispose the previous Image */
			if (scaled != null && !scaled.isDisposed())
				scaled.dispose();
			scaled = newScaled;

			/* Don't forget to set it again */
			WelcomePlaceholder.this.setBackgroundImage(scaled);
		});
		/* Remember to dispose the scaled image */
		this.addListener(SWT.Dispose, event -> {
			if (scaled != null && !scaled.isDisposed())
				scaled.dispose();
		});

        /* Welcome text */
		Label welcomeText = new Label(this, SWT.WRAP);
		welcomeText.setText(RB.getString(RB.WELCOME_TITLE));
		Resources.Fonts.applyFontSize(welcomeText, 16);

		new Label(this, SWT.NONE);

        /* Description text */
		Label description = new Label(this, SWT.WRAP);
		Resources.Fonts.applyFontSize(description, 12);
		description.setText(RB.getString(RB.WELCOME_TEXT));

        /* Add the common part that's also used in the AboutDialog */
		CommonGUI.addAboutPart(this);

        /* Logos */
		Composite logos = new Composite(this, SWT.NONE);

		Label germinate = new Label(logos, SWT.NONE);
		germinate.setImage(Resources.Images.GERMINATE_LOGO);

		Label jhi = new Label(logos, SWT.NONE);
		jhi.setImage(Resources.Images.JHI_LOGO);

        /* Layout */
		GridLayoutUtils.useDefault().applyTo(this);
		GridLayoutUtils.useValues(2, false).applyTo(logos);

		GridDataUtils.usePredefined(GridDataUtils.GridDataStyle.FILL_BOTH).applyTo(this);
		GridDataUtils.usePredefined(GridDataUtils.GridDataStyle.FILL_TOP).applyTo(welcomeText);
		GridDataUtils.usePredefined(GridDataUtils.GridDataStyle.FILL_TOP).applyTo(description);
		GridDataUtils.usePredefined(GridDataUtils.GridDataStyle.FILL_BOTTOM).applyTo(logos);
		GridDataUtils.usePredefined(GridDataUtils.GridDataStyle.BEGINNING_CENTER).applyTo(germinate);
		GridDataUtils.usePredefined(GridDataUtils.GridDataStyle.END_CENTER).applyTo(jhi);
	}

	/**
	 * Returns a scaled version of the given bounds so that at least one side still fits into the given targetSize. <p> That means that the returned
	 * size exceeds the targetSize in AT MOST one dimension
	 *
	 * @param targetSize The container size
	 * @param bounds     The original size
	 * @return The scaled version of the input bounds so that AT MOST one dimension exceeds the targetSize
	 */
	@SuppressWarnings("unused")
	private Point getAspectRatioSize(Point targetSize, Rectangle bounds)
	{
		float w = targetSize.x / (1f * bounds.width);
		float h = targetSize.y / (1f * bounds.height);

		float targetWidth = bounds.width;
		float targetHeight = bounds.height;

		if (w > h)
		{
			targetWidth *= w;
			targetHeight *= w;
		}
		else
		{
			targetWidth *= h;
			targetHeight *= h;
		}

		return new Point(Math.round(targetWidth), Math.round(targetHeight));
	}
}
