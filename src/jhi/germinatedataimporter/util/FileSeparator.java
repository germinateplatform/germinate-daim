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

package jhi.germinatedataimporter.util;

/**
 * {@link FileSeparator} contains all allowed file separators.
 *
 * @author Sebastian Raubach
 */
public enum FileSeparator
{
	TAB("\t", "tab"),
	COMMA(",", ","),
	SEMICOLON(";", ";"),
	PIPE("\\|", "|");

	private String separator;
	private String name;

	FileSeparator(String separator, String name)
	{
		this.separator = separator;
		this.name = name;
	}

	public String getSeparator()
	{
		return separator;
	}

	public void setSeparator(String separator)
	{
		this.separator = separator;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}
}
