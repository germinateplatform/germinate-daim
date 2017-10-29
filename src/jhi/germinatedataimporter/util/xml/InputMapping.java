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

package jhi.germinatedataimporter.util.xml;

import org.simpleframework.xml.*;

import java.text.*;
import java.util.*;

/**
 * @author Sebastian Raubach
 */
@Root(name = "g3di", strict = false)
public class InputMapping
{
	public static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd");

	@Element(name = "input-file")
	private String inputFile;
	@Element(name = "target-table")
	private String targetTable;

	@ElementList(name = "simple-mapping", inline = true, required = false)
	private List<SimpleMapping> simpleMappings = new ArrayList<>();

	@ElementList(name = "reference-mapping", inline = true, required = false)
	private List<ReferenceMapping> referenceMappings = new ArrayList<>();

	@ElementList(name = "constant-mapping", inline = true, required = false)
	private List<ConstantMapping> constantMappings = new ArrayList<>();

	@ElementList(name = "date-mapping", inline = true, required = false)
	private List<DateMapping> dateMappings = new ArrayList<>();

	public InputMapping()
	{
	}

	public String getInputFile()
	{
		return inputFile;
	}

	public InputMapping setInputFile(String inputFile)
	{
		this.inputFile = inputFile;
		return this;
	}

	public String getTargetTable()
	{
		return targetTable;
	}

	public InputMapping setTargetTable(String targetTable)
	{
		this.targetTable = targetTable;
		return this;
	}

	public List<SimpleMapping> getSimpleMappings()
	{
		return simpleMappings;
	}

	public InputMapping setSimpleMappings(List<SimpleMapping> simpleMappings)
	{
		this.simpleMappings = simpleMappings;
		return this;
	}

	public InputMapping addSimpleMapping(SimpleMapping simpleMapping)
	{
		simpleMappings.add(simpleMapping);
		return this;
	}

	public List<ReferenceMapping> getReferenceMappings()
	{
		return referenceMappings;
	}

	public InputMapping setReferenceMappings(List<ReferenceMapping> referenceMappings)
	{
		this.referenceMappings = referenceMappings;
		return this;
	}

	public InputMapping addReferenceMapping(ReferenceMapping referenceMapping)
	{
		referenceMappings.add(referenceMapping);
		return this;
	}

	public List<ConstantMapping> getConstantMappings()
	{
		return constantMappings;
	}

	public InputMapping setConstantMappings(List<ConstantMapping> constantMappings)
	{
		this.constantMappings = constantMappings;
		return this;
	}

	public InputMapping addConstantMapping(ConstantMapping constantMapping)
	{
		constantMappings.add(constantMapping);
		return this;
	}

	public List<DateMapping> getDateMappings()
	{
		return dateMappings;
	}

	public InputMapping setDateMappings(List<DateMapping> dateMappings)
	{
		this.dateMappings = dateMappings;
		return this;
	}

	public InputMapping addDateMappings(DateMapping dateMapping)
	{
		dateMappings.add(dateMapping);
		return this;
	}

	public static abstract class Mapping
	{
		@Element(name = "database-column")
		protected String databaseColumn;

		public Mapping()
		{
		}

		public String getDatabaseColumn()
		{
			return databaseColumn;
		}

		public Mapping setDatabaseColumn(String databaseColumn)
		{
			this.databaseColumn = databaseColumn;
			return this;
		}
	}

	@Root(name = "simple-mapping", strict = false)
	public static class SimpleMapping extends Mapping
	{
		@Element(name = "file-column")
		protected String fileColumn;

		public SimpleMapping()
		{
		}

		public String getFileColumn()
		{
			return fileColumn;
		}

		public SimpleMapping setFileColumn(String fileColumn)
		{
			this.fileColumn = fileColumn;
			return this;
		}
	}

	@Root(name = "date-mapping", strict = false)
	public static class DateMapping extends Mapping
	{
		@Element(name = "file-column", required = false)
		protected String fileColumn;

		@Element(name = "date-format", required = false)
		protected String dateFormat;

		@Element(name = "date", required = false)
		protected String date;

		@Element(name = "now", required = false)
		protected boolean now = false;

		public DateMapping()
		{
		}

		public String getDateFormat()
		{
			return dateFormat;
		}

		public DateMapping setDateFormat(String dateFormat)
		{
			this.dateFormat = dateFormat;
			return this;
		}

		public String getDate()
		{
			return date;
		}

		public DateMapping setDate(String date)
		{
			this.date = date;
			return this;
		}

		public boolean isNow()
		{
			return now;
		}

		public DateMapping setNow(boolean now)
		{
			this.now = now;
			return this;
		}

		public String getFileColumn()
		{
			return fileColumn;
		}

		public DateMapping setFileColumn(String fileColumn)
		{
			this.fileColumn = fileColumn;
			return this;
		}
	}

	@Root(name = "reference-mapping", strict = false)
	public static class ReferenceMapping extends SimpleMapping
	{
		@Element(name = "reference-table")
		protected String referenceTable;
		@Element(name = "reference-column")
		protected String referenceColumn;

		public ReferenceMapping()
		{
		}

		public String getReferenceTable()
		{
			return referenceTable;
		}

		public ReferenceMapping setReferenceTable(String referenceTable)
		{
			this.referenceTable = referenceTable;
			return this;
		}

		public String getReferenceColumn()
		{
			return referenceColumn;
		}

		public ReferenceMapping setReferenceColumn(String referenceColumn)
		{
			this.referenceColumn = referenceColumn;
			return this;
		}
	}

	@Root(name = "constant-mapping", strict = false)
	public static class ConstantMapping extends Mapping
	{
		@Element(name = "constant")
		protected String constant;

		public ConstantMapping()
		{
		}

		public String getConstant()
		{
			return constant;
		}

		public ConstantMapping setConstant(String constant)
		{
			this.constant = constant;
			return this;
		}
	}
}
