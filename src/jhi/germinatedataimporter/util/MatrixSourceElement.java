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

package jhi.germinatedataimporter.util;

import jhi.germinatedataimporter.gui.i18n.*;

/**
 * @author Sebastian Raubach
 */
public enum MatrixSourceElement
{
	ROW_ID(RB.getString(RB.LABEL_MATRIX_ROW_ID)),
	COL_ID(RB.getString(RB.LABEL_MATRIX_COL_ID)),
	VALUE(RB.getString(RB.LABEL_MATRIX_VALUE));

	private String name;

	MatrixSourceElement(String name)
	{
		this.name = name;
	}

	public String getName()
	{
		return name;
	}
}
