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

package jhi.germinatedataimporter.gui.dialog;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

import java.util.*;

import jhi.germinatedataimporter.gui.*;
import jhi.germinatedataimporter.gui.i18n.*;
import jhi.germinatedataimporter.gui.widget.*;
import jhi.germinatedataimporter.util.*;
import jhi.swtcommons.gui.dialog.*;
import jhi.swtcommons.gui.layout.*;
import jhi.swtcommons.gui.viewer.*;
import jhi.swtcommons.util.*;

/**
 * {@link AboutDialog} extends {@link jhi.swtcommons.gui.dialog.BannerDialog} and shows the company logo along with email and web address.
 *
 * @author Sebastian Raubach
 */
public class AboutDialog extends BannerDialog
{

	private TabFolder tabFolder;
	private Text      license;

	public AboutDialog(Shell parentShell)
	{
		super(parentShell, Resources.Images.GERMINATE_LOGO, Resources.Colors.DARK_GREY);
		setBlockOnOpen(true);
	}

	@Override
	protected Control createButtonBar(Composite parent)
	{
		/* We don't want a button bar, so just return null */
		return null;
	}

	@Override
	protected void configureShell(Shell shell)
	{
		super.configureShell(shell);
		shell.setText(RB.getString(RB.DIALOG_ABOUT_TITLE));
	}

	@Override
	protected Point getInitialLocation(Point initialSize)
	{
		/* Center the dialog based on the parent */
		return ShellUtils.getLocationCenteredTo(getParentShell(), initialSize);
	}

	@Override
	protected void createContent(Composite composite)
	{
		GridLayoutUtils.useDefault().applyTo(composite);

        /* Add the app name */
		Label name = new Label(composite, SWT.WRAP);
		name.setText(RB.getString(RB.APPLICATION_TITLE) + " (" + Install4jUtils.getVersion(GerminateDataImporter.class) + ")");

		Resources.Fonts.applyFontSize(name, 16);

		tabFolder = new TabFolder(composite, SWT.NONE);

		createAboutPart(tabFolder);
		createLicensePart(tabFolder);

		GridLayoutUtils.useDefault().applyTo(tabFolder);
		GridDataUtils.usePredefined(GridDataUtils.GridDataStyle.FILL_BOTH).widthHint(400 * Resources.getZoomFactor() / 100).applyTo(tabFolder);
		GridDataUtils.usePredefined(GridDataUtils.GridDataStyle.FILL_BOTH).applyTo(composite);
		GridDataUtils.usePredefined(GridDataUtils.GridDataStyle.CENTER_TOP).applyTo(name);
	}

	@Override
	public void create()
	{
		super.create();

		if (OSUtils.isMac())
		{
			tabFolder.setSelection(1);
			tabFolder.setSelection(0);
		}
	}

	/**
	 * Creates the {@link TabFolder} containing the license
	 *
	 * @param parent The parent {@link TabFolder}
	 */
	private void createLicensePart(TabFolder parent)
	{
		TabItem item = new TabItem(parent, SWT.NONE);
		item.setText(RB.getString(RB.DIALOG_ABOUT_TAB_LICENSE));

		Composite composite = new Composite(parent, SWT.NONE);

		LicenseFileComboViewer licenseFileComboViewer = new LicenseFileComboViewer(composite, SWT.NONE)
		{
			@Override
			protected LinkedHashMap<String, String> getLicenseData()
			{
				LinkedHashMap<String, String> result = new LinkedHashMap<>();

				result.put(RB.getString(RB.APPLICATION_TITLE), "LICENSE");
				result.put("Database Commons", "licences/database-commons.txt");
				result.put("Faenza Icon Set", "licences/faenza.txt");
				result.put("Joda-Time", "licences/joda-time.txt");
				result.put("Simple Xml Serialization", "licences/simple-xml.txt");

				return result;
			}

			@Override
			protected void showLicense(String text, String path)
			{
				try
				{
					license.setText(FileUtils.readLicense(path));
				}
				catch (Exception e)
				{
					license.setText(RB.getString(RB.ERROR_ABOUT_LICENSE));
					e.printStackTrace();
				}
			}
		};

		license = new Text(composite, SWT.WRAP | SWT.BORDER | SWT.V_SCROLL | SWT.READ_ONLY);
		license.setBackground(getShell().getDisplay().getSystemColor(SWT.COLOR_WHITE));

		licenseFileComboViewer.init();

        /* Do some layout magic here */
		GridLayoutUtils.useDefault().applyTo(composite);

		GridDataUtils.usePredefined(GridDataUtils.GridDataStyle.FILL_BOTH).applyTo(composite);
		GridDataUtils.usePredefined(GridDataUtils.GridDataStyle.FILL_TOP).applyTo(licenseFileComboViewer.getCombo());
		GridDataUtils.usePredefined(GridDataUtils.GridDataStyle.FILL_BOTH).heightHint(1).applyTo(license);

		item.setControl(composite);
	}

	/**
	 * Creates the {@link TabFolder} containing the about text
	 *
	 * @param parent The parent {@link TabFolder}
	 */
	private void createAboutPart(TabFolder parent)
	{
		TabItem item = new TabItem(parent, SWT.NONE);
		item.setText(RB.getString(RB.DIALOG_ABOUT_TAB_ABOUT));

		Composite composite = new Composite(parent, SWT.NONE);

		CommonGUI.addAboutPart(composite);

		new Label(composite, SWT.NONE);

		Hyperlink jhi = new Hyperlink(composite, SWT.NONE);
		jhi.setImage(Resources.Images.JHI_LOGO);
		jhi.setToolTipText(RB.getString(RB.DIALOG_ABOUT_JHI_URL));

        /* Do some layout magic here */
		GridLayoutUtils.useDefault().marginBottom(15).marginWidth(15).applyTo(composite);

		GridDataUtils.usePredefined(GridDataUtils.GridDataStyle.FILL_BOTH).applyTo(composite);
		GridDataUtils.usePredefined(GridDataUtils.GridDataStyle.CENTER_BOTH).applyTo(jhi.getControl());

		item.setControl(composite);
	}
}
