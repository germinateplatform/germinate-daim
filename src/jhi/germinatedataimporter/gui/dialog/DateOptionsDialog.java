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

package jhi.germinatedataimporter.gui.dialog;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.*;
import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.widgets.DateTime;
import org.joda.time.*;

import java.text.*;
import java.util.*;
import java.util.List;

import jhi.germinatedataimporter.database.entities.*;
import jhi.germinatedataimporter.gui.i18n.*;
import jhi.germinatedataimporter.gui.widget.*;
import jhi.swtcommons.gui.dialog.*;
import jhi.swtcommons.gui.layout.*;
import jhi.swtcommons.util.*;

/**
 * {@link DateOptionsDialog} is a {@link Dialog} to set date conditions for {@link DatabaseColumn}s
 *
 * @author Sebastian Raubach
 */
public class DateOptionsDialog extends I18nDialog
{
	private Text          pattern;
	private DateTime      dateTime;
	private Text          patternExample;
	private CheckBoxGroup calendarGroup;
	private CheckBoxGroup nowGroup;
	private CheckBoxGroup patternGroup;

	private DateOption result;

	public DateOptionsDialog(Shell parentShell, DateOption previousOption)
	{
		super(parentShell);
		setBlockOnOpen(true);

		result = previousOption;
	}

	@Override
	protected Control createDialogArea(Composite parent)
	{
		final Composite composite = (Composite) super.createDialogArea(parent);
		GridLayoutUtils.useValues(1, false).applyTo(composite);

        /* Add the calendar */
		calendarGroup = new CheckBoxGroup(composite, SWT.NONE);
		calendarGroup.setText(RB.getString(RB.DIALOG_DATE_OPTIONS_CALENDAR));

		dateTime = new DateTime(calendarGroup.getContent(), SWT.CALENDAR);
		calendarGroup.deactivate();

        /* Add the NOW part */
		nowGroup = new CheckBoxGroup(composite, SWT.NONE);
		nowGroup.setText(RB.getString(RB.DIALOG_DATE_OPTIONS_USE_NOW));

		new Label(nowGroup.getContent(), SWT.WRAP).setText(RB.getString(RB.DIALOG_DATE_OPTIONS_USE_NOW_DESC));
		nowGroup.deactivate();

        /* Add the SimpleDateFormat part */
		patternGroup = new CheckBoxGroup(composite, SWT.NONE);
		patternGroup.setText(RB.getString(RB.DIALOG_DATE_OPTIONS_PATTERN));

		pattern = new Text(patternGroup.getContent(), SWT.BORDER);
		pattern.addListener(SWT.KeyUp, e -> {
			try
			{
				SimpleDateFormat format = new SimpleDateFormat(pattern.getText());
				patternExample.setText(format.format(new Date()));
			}
			catch (IllegalArgumentException e1)
			{
				patternExample.setText(RB.getString(RB.ERROR_DATE_OPTIONS_INVALID_PATTERN));
			}
		});

		patternExample = new Text(patternGroup.getContent(), SWT.BORDER);
		patternExample.setEditable(false);

        /* Layout */
		GridLayoutUtils.useDefault().applyTo(calendarGroup);
		GridLayoutUtils.useDefault().applyTo(nowGroup);
		GridLayoutUtils.useDefault().applyTo(patternGroup);

		GridDataUtils.usePredefined(GridDataUtils.GridDataStyle.FILL_BOTH).applyTo(composite);
		GridDataUtils.usePredefined(GridDataUtils.GridDataStyle.CENTER_BOTH).applyTo(dateTime);
		GridDataUtils.usePredefined(GridDataUtils.GridDataStyle.FILL_CENTER).applyTo(calendarGroup);
		GridDataUtils.usePredefined(GridDataUtils.GridDataStyle.FILL_CENTER).applyTo(nowGroup);
		GridDataUtils.usePredefined(GridDataUtils.GridDataStyle.FILL_CENTER).applyTo(patternGroup);
		GridDataUtils.usePredefined(GridDataUtils.GridDataStyle.FILL_CENTER).applyTo(pattern);
		GridDataUtils.usePredefined(GridDataUtils.GridDataStyle.FILL_CENTER).applyTo(patternExample);

		patternGroup.deactivate();

		List<CheckBoxGroup> groups = new ArrayList<>();
		groups.add(calendarGroup);
		groups.add(nowGroup);
		groups.add(patternGroup);

		synchronizeGroups(groups);

		restoreDateOption();

		return composite;
	}

	private void restoreDateOption()
	{
		if (result instanceof NowOption)
		{
			nowGroup.activate();
		}
		else if (result instanceof PatternOption)
		{
			patternGroup.activate();
			pattern.setText(((PatternOption) result).format.toPattern());
			pattern.notifyListeners(SWT.KeyUp, new Event());
		}
		else if (result instanceof CalendarOption)
		{
			calendarGroup.activate();
			org.joda.time.DateTime time = ((CalendarOption) result).date;
			dateTime.setDate(time.getYear(), time.getMonthOfYear() - 1, time.getDayOfMonth());
		}
	}

	/**
	 * Makes sure that only one {@link CheckBoxGroup} can be enabled at a time
	 *
	 * @param groups The {@link CheckBoxGroup}s to synchronize
	 */
	private void synchronizeGroups(final List<CheckBoxGroup> groups)
	{
		final List<Button> buttons = new ArrayList<>();

		Listener listener = e -> {
			Button group = (Button) e.widget;

			for (int i = 0; i < buttons.size(); i++)
			{
				if (buttons.get(i).equals(group))
				{
					groups.get(i).activate();
				}
				else
				{
					groups.get(i).deactivate();
				}
			}
		};

		for (CheckBoxGroup group : groups)
		{
			buttons.add(group.getCheckBox());
			group.getCheckBox().addListener(SWT.Selection, listener);
		}
	}

	@Override
	protected void okPressed()
	{
		if (calendarGroup.isActivated())
		{
			MutableDateTime date = new MutableDateTime();
			date.setDayOfMonth(dateTime.getDay());
			date.setMonthOfYear(dateTime.getMonth() + 1);
			date.setYear(dateTime.getYear());
			result = new CalendarOption(date.toDateTime());
		}
		else if (nowGroup.isActivated())
		{
			result = new NowOption();
		}
		else if (patternGroup.isActivated())
		{
			if (StringUtils.isEmpty(pattern.getText()))
			{
				DialogUtils.showError(RB.getString(RB.ERROR_DATE_OPTIONS_INVALID_PATTERN));
				return;
			}

			try
			{
				SimpleDateFormat format = new SimpleDateFormat(pattern.getText());
				result = new PatternOption(format);
			}
			catch (IllegalArgumentException e)
			{
				DialogUtils.showError(RB.getString(RB.ERROR_DATE_OPTIONS_INVALID_PATTERN));
				return;
			}
		}
		else
		{
			DialogUtils.showError(RB.getString(RB.ERROR_DATE_OPTIONS_SELECT_ONE));
			return;
		}

		super.okPressed();
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent)
	{
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

	@Override
	protected Point getInitialLocation(Point initialSize)
	{
		/* Center the dialog based on the parent */
		return ShellUtils.getLocationCenteredTo(getParentShell(), initialSize);
	}

	@Override
	protected void configureShell(Shell shell)
	{
		super.configureShell(shell);
		shell.setText(RB.getString(RB.DIALOG_DATE_OPTIONS_TITLE));
	}

	/**
	 * Returns the selected {@link DateOption}. This can either be {@link CalendarOption}, {@link PatternOption} or {@link NowOption}.
	 *
	 * @return The {@link jhi.germinatedataimporter.gui.dialog.DateOptionsDialog.DateOption}
	 */
	public DateOption getDateOption()
	{
		return result;
	}

	/**
	 * A dummy interface for the return type of the {@link DateOptionsDialog}
	 *
	 * @author Sebastian Raubach
	 */
	public interface DateOption
	{
	}

	/**
	 * This class is used if the user selected a {@link Date} from the {@link DateTime} widget.
	 *
	 * @author Sebastian Raubach
	 */
	public static class CalendarOption implements DateOption
	{
		public org.joda.time.DateTime date;

		public CalendarOption(org.joda.time.DateTime date)
		{
			this.date = date;
		}
	}

	/**
	 * This class is used if the user selects a custom pattern to create a {@link SimpleDateFormat}
	 *
	 * @author Sebastian Raubach
	 */
	public static class PatternOption implements DateOption
	{
		public SimpleDateFormat format;

		public PatternOption(SimpleDateFormat format)
		{
			this.format = format;
		}
	}

	/**
	 * This dummy class is used if the user selected to use <code>NOW()</code> as the date
	 *
	 * @author Sebastian Raubach
	 */
	public static class NowOption implements DateOption
	{
	}
}
