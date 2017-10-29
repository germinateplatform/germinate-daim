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
import org.eclipse.swt.widgets.*;

import java.util.*;

import jhi.germinatedataimporter.gui.i18n.*;
import jhi.germinatedataimporter.util.*;
import jhi.swtcommons.gui.layout.*;

/**
 * @author Sebastian Raubach
 */
public class CommonGUI
{
	public static void addAboutPart(Composite composite)
	{
		/* Add the description */
		Label desc = new Label(composite, SWT.WRAP);
		desc.setText(RB.getString(RB.DIALOG_ABOUT_DESCRIPTION));

        /* Add some space */
		new Label(composite, SWT.NONE);

        /* Add additional text */
		Label additional = new Label(composite, SWT.WRAP);
		additional.setText(RB.getString(RB.DIALOG_ABOUT_ADDITIONAL));

        /* Add some space */
		new Label(composite, SWT.NONE);

        /* Add the copyright information */
		Label copyright = new Label(composite, SWT.WRAP);
		copyright.setText(RB.getString(RB.DIALOG_ABOUT_COPYRIGHT, Calendar.getInstance().get(Calendar.YEAR)));

		Composite linkWrapper = new Composite(composite, SWT.NONE);

        /* Add the link to the website */
		Hyperlink website = new Hyperlink(linkWrapper, SWT.WRAP);
		website.setText(RB.getString(RB.DIALOG_ABOUT_WEBSITE_TITLE));
		website.setToolTipText(RB.getString(RB.DIALOG_ABOUT_WEBSITE_URL));
		website.setImage(Resources.Images.WEB);

        /* Add the email address */
		Hyperlink email = new Hyperlink(linkWrapper, SWT.WRAP);
		email.setText(RB.getString(RB.DIALOG_ABOUT_EMAIL_TITLE));
		email.setToolTipText(RB.getString(RB.DIALOG_ABOUT_EMAIL_URL));
		email.setImage(Resources.Images.EMAIL);
		email.setIsEmail(true);

        /* Add the twitter link */
		Hyperlink twitter = new Hyperlink(linkWrapper, SWT.NONE);
		twitter.setText(RB.getString(RB.DIALOG_ABOUT_TWITTER_TITLE));
		twitter.setToolTipText(RB.getString(RB.DIALOG_ABOUT_TWITTER_URL));
		twitter.setImage(Resources.Images.TWITTER_LOGO);

		GridLayoutUtils.useDefault().applyTo(linkWrapper);

		GridDataUtils.usePredefined(GridDataUtils.GridDataStyle.FILL_TOP).applyTo(desc);
		GridDataUtils.usePredefined(GridDataUtils.GridDataStyle.FILL_TOP).applyTo(additional);
		GridDataUtils.usePredefined(GridDataUtils.GridDataStyle.CENTER_TOP).applyTo(copyright);
		GridDataUtils.usePredefined(GridDataUtils.GridDataStyle.CENTER_TOP).applyTo(linkWrapper);
		GridDataUtils.usePredefined(GridDataUtils.GridDataStyle.FILL_CENTER).applyTo(email.getControl());
		GridDataUtils.usePredefined(GridDataUtils.GridDataStyle.FILL_CENTER).applyTo(website.getControl());
		GridDataUtils.usePredefined(GridDataUtils.GridDataStyle.FILL_CENTER).applyTo(twitter.getControl());
	}
}
