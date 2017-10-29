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

/**
 * {@link MappingArrow} is a wrapper class for a {@link Label} with a custom {@link SWT#Paint} {@link Listener} that draws an arrow from left to
 * right.
 *
 * @author Sebastian Raubach
 */
public class MappingArrow
{
	/**
	 * Creates a new instance of {@link MappingArrow}
	 *
	 * @param parent a composite control which will be the parent of the new instance (cannot be null)
	 * @param style  the style of control to construct
	 */
	public MappingArrow(Composite parent, int style)
	{
		/* Create the label and color its background */
		final Label mapping = new Label(parent, style);
		mapping.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));

        /* Draw the arrow */
		mapping.addListener(SWT.Paint, e -> {
			Point size = mapping.getSize();

			/* Make sure the height is divisible by 2 */
			if (size.y % 2 != 0)
				size.y--;

			/* Define the arrow head */
			int[] points = new int[6];
			points[0] = size.x - 10;
			points[1] = 0;
			points[2] = size.x;
			points[3] = size.y / 2;
			points[4] = size.x - 10;
			points[5] = size.y;

			/* Make it look good */
			int width = 1; // TODO: Set based on OS zoom level
			e.gc.setAdvanced(true);
			e.gc.setAntialias(SWT.ON);
			e.gc.setLineWidth(width);
			e.gc.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));

			/* Draw the main arrow */
			e.gc.drawLine(width, size.y / 2, size.x - 10, size.y / 2);
			/* Draw the arrow base */
			e.gc.drawLine(width / 2, 2, width / 2, size.y - 2);
			/* Draw the head */
			e.gc.fillPolygon(points);
		});

        /* Center it vertically */
		GridDataUtils.usePredefined(GridDataUtils.GridDataStyle.FILL_CENTER).applyTo(mapping);
	}
}
