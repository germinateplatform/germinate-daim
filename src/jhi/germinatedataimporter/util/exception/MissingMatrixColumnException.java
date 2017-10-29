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

package jhi.germinatedataimporter.util.exception;

import jhi.germinatedataimporter.util.*;

/**
 * {@link MissingMatrixColumnException} extends {@link Exception} and is thrown if a column that cannot be null hasn't been mapped by the user
 *
 * @author Sebastian Raubach
 */
public class MissingMatrixColumnException extends Exception
{
	private static final long serialVersionUID = -6700598862698179488L;

	private MatrixSourceElement element;

	public MissingMatrixColumnException(MatrixSourceElement element)
	{
		super();
		this.element = element;
	}

	public MissingMatrixColumnException(String message)
	{
		super(message);
	}

	public MissingMatrixColumnException(Exception e)
	{
		super(e);
	}

	public MatrixSourceElement getElement()
	{
		return element;
	}
}
