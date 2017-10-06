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

package jhi.germinatedataimporter.util.exception;

import jhi.germinatedataimporter.database.entities.*;

/**
 * {@link MissingInputColumnException} extends {@link Exception} and is thrown if a {@link DatabaseColumn} doesn't have an input column mapped to it
 *
 * @author Sebastian Raubach
 */
public class MissingInputColumnException extends Exception
{
	private static final long serialVersionUID = -270742989704546818L;

	private DatabaseColumn column;

	public MissingInputColumnException(DatabaseColumn column)
	{
		super();
		this.column = column;
	}

	public MissingInputColumnException(String message)
	{
		super(message);
	}

	public MissingInputColumnException(Exception e)
	{
		super(e);
	}

	public DatabaseColumn getColumn()
	{
		return column;
	}
}
