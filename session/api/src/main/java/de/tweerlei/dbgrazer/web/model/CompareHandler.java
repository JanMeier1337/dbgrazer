/*
 * Copyright 2018 tweerlei Wruck + Buchmeier GbR - http://www.tweerlei.de/
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.tweerlei.dbgrazer.web.model;

import java.util.List;
import java.util.Set;

import de.tweerlei.dbgrazer.query.model.ColumnDef;
import de.tweerlei.dbgrazer.query.model.ResultRow;

/**
 * Handle result comparisons
 * 
 * @author Robert Wruck
 */
public interface CompareHandler
	{
	/**
	 * Row added
	 * @param tableName Table name
	 * @param columns Columns
	 * @param values Values
	 * @param pk PK column indices
	 */
	public void rowAdded(String tableName, List<ColumnDef> columns, ResultRow values, Set<Integer> pk);
	
	/**
	 * Row changed
	 * @param tableName Table name
	 * @param columns Columns
	 * @param oldValues Previous values
	 * @param newValues New values
	 * @param pk PK column indices
	 * @return true if an actual change was confirmed, false if oldValues matched newValues
	 */
	public boolean rowChanged(String tableName, List<ColumnDef> columns, ResultRow oldValues, ResultRow newValues, Set<Integer> pk);
	
	/**
	 * Row removed
	 * @param tableName Table name
	 * @param columns Columns
	 * @param values Previous values
	 * @param pk PK column indices
	 */
	public void rowRemoved(String tableName, List<ColumnDef> columns, ResultRow values, Set<Integer> pk);
	
	/**
	 * Flush results
	 */
	public void flush();
	}
