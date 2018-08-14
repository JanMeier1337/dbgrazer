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
package de.tweerlei.dbgrazer.web.controller.jdbc;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import de.tweerlei.common.util.StringUtils;
import de.tweerlei.common5.jdbc.model.ColumnDescription;
import de.tweerlei.common5.jdbc.model.PrimaryKeyDescription;
import de.tweerlei.common5.jdbc.model.QualifiedName;
import de.tweerlei.common5.jdbc.model.TableDescription;
import de.tweerlei.dbgrazer.extension.jdbc.JdbcConstants;
import de.tweerlei.dbgrazer.extension.jdbc.MetadataService;
import de.tweerlei.dbgrazer.extension.jdbc.SQLGeneratorService;
import de.tweerlei.dbgrazer.extension.jdbc.MetadataService.ColumnMode;
import de.tweerlei.dbgrazer.extension.jdbc.SQLGeneratorService.Joins;
import de.tweerlei.dbgrazer.extension.jdbc.SQLGeneratorService.OrderBy;
import de.tweerlei.dbgrazer.extension.jdbc.SQLGeneratorService.Style;
import de.tweerlei.dbgrazer.link.service.LinkService;
import de.tweerlei.dbgrazer.query.exception.CancelledByUserException;
import de.tweerlei.dbgrazer.query.exception.PerformQueryException;
import de.tweerlei.dbgrazer.query.model.QueryType;
import de.tweerlei.dbgrazer.query.model.Result;
import de.tweerlei.dbgrazer.query.model.RowHandler;
import de.tweerlei.dbgrazer.query.model.RowIterator;
import de.tweerlei.dbgrazer.query.model.RowTransferer;
import de.tweerlei.dbgrazer.query.model.StatementHandler;
import de.tweerlei.dbgrazer.query.model.impl.AsyncRowIterator;
import de.tweerlei.dbgrazer.query.model.impl.MonitoringStatementHandler;
import de.tweerlei.dbgrazer.query.model.impl.StatementCollection;
import de.tweerlei.dbgrazer.query.model.impl.StatementWriter;
import de.tweerlei.dbgrazer.query.service.QueryService;
import de.tweerlei.dbgrazer.web.constant.MessageKeys;
import de.tweerlei.dbgrazer.web.exception.AccessDeniedException;
import de.tweerlei.dbgrazer.web.model.CompareProgressMonitor;
import de.tweerlei.dbgrazer.web.model.TableFilterEntry;
import de.tweerlei.dbgrazer.web.model.TaskCompareProgressMonitor;
import de.tweerlei.dbgrazer.web.model.TaskDMLProgressMonitor;
import de.tweerlei.dbgrazer.web.model.TaskProgress;
import de.tweerlei.dbgrazer.web.service.DataFormatterFactory;
import de.tweerlei.dbgrazer.web.service.QueryPerformerService;
import de.tweerlei.dbgrazer.web.service.TaskProgressService;
import de.tweerlei.dbgrazer.web.service.UserSettingsManager;
import de.tweerlei.dbgrazer.web.service.jdbc.ResultCompareService;
import de.tweerlei.dbgrazer.web.session.ConnectionSettings;
import de.tweerlei.dbgrazer.web.session.UserSettings;
import de.tweerlei.ermtools.dialect.SQLDialect;
import de.tweerlei.ermtools.dialect.impl.SQLDialectFactory;
import de.tweerlei.spring.service.TimeService;

/**
 * Controller for running queries
 * 
 * @author Robert Wruck
 */
@Controller
public class DataDiffController
	{
	/**
	 * Helper class used as form backing object
	 */
	public static final class FormBackingObject
		{
		private String catalog;
		private String schema;
		private String object;
		private String connection2;
		private String catalog2;
		private String schema2;
		private String filter;
		private OrderBy order;
		private boolean merge;
		private String mode;
		private final Set<String> pkColumns;
		private final Set<String> dataColumns;
		
		/**
		 * Constructor
		 */
		public FormBackingObject()
			{
			this.pkColumns = new LinkedHashSet<String>();
			this.dataColumns = new LinkedHashSet<String>();
			}
		
		/**
		 * Get the catalog
		 * @return the catalog
		 */
		public String getCatalog()
			{
			return catalog;
			}
		
		/**
		 * Set the catalog
		 * @param catalog the catalog to set
		 */
		public void setCatalog(String catalog)
			{
			this.catalog = catalog;
			}
		
		/**
		 * Get the schema
		 * @return the schema
		 */
		public String getSchema()
			{
			return schema;
			}
		
		/**
		 * Set the schema
		 * @param schema the schema to set
		 */
		public void setSchema(String schema)
			{
			this.schema = schema;
			}
		
		/**
		 * Get the object
		 * @return the object
		 */
		public String getObject()
			{
			return object;
			}
		
		/**
		 * Set the object
		 * @param object the object to set
		 */
		public void setObject(String object)
			{
			this.object = object;
			}
		
		/**
		 * Get the connection2
		 * @return the connection2
		 */
		public String getConnection2()
			{
			return connection2;
			}
		
		/**
		 * Set the connection2
		 * @param connection2 the connection2 to set
		 */
		public void setConnection2(String connection2)
			{
			this.connection2 = connection2;
			}
		
		/**
		 * Get the catalog2
		 * @return the catalog2
		 */
		public String getCatalog2()
			{
			return catalog2;
			}
		
		/**
		 * Set the catalog2
		 * @param catalog2 the catalog2 to set
		 */
		public void setCatalog2(String catalog2)
			{
			this.catalog2 = catalog2;
			}
		
		/**
		 * Get the schema2
		 * @return the schema2
		 */
		public String getSchema2()
			{
			return schema2;
			}
		
		/**
		 * Set the schema2
		 * @param schema2 the schema2 to set
		 */
		public void setSchema2(String schema2)
			{
			this.schema2 = schema2;
			}
		
		/**
		 * Get the filter
		 * @return the filter
		 */
		public String getFilter()
			{
			return filter;
			}
		
		/**
		 * Set the filter
		 * @param filter the filter to set
		 */
		public void setFilter(String filter)
			{
			this.filter = filter;
			}
		
		/**
		 * Get the order
		 * @return the order
		 */
		public OrderBy getOrder()
			{
			return order;
			}
		
		/**
		 * Set the order
		 * @param order the order to set
		 */
		public void setOrder(OrderBy order)
			{
			this.order = order;
			}
		
		/**
		 * Get the merge
		 * @return the merge
		 */
		public boolean isMerge()
			{
			return merge;
			}
		
		/**
		 * Set the merge
		 * @param merge the merge to set
		 */
		public void setMerge(boolean merge)
			{
			this.merge = merge;
			}
		
		/**
		 * Get the mode
		 * @return the mode
		 */
		public String getMode()
			{
			return mode;
			}
		
		/**
		 * Set the mode
		 * @param mode the mode to set
		 */
		public void setMode(String mode)
			{
			this.mode = mode;
			}
		
		/**
		 * Get the pkColumns
		 * @return the pkColumns
		 */
		public Set<String> getPkColumns()
			{
			return pkColumns;
			}
		
		/**
		 * Get the dataColumns
		 * @return the dataColumns
		 */
		public Set<String> getDataColumns()
			{
			return dataColumns;
			}
		}
	
	/**
	 * Helper bean for diff results
	 */
	public static final class DiffResult
		{
		private final String sql;
		private final SortedMap<String, TaskProgress> comparisonResult;
		private final long duration;
		private final boolean moreAvailable;
		
		/**
		 * Constructor
		 * @param sql Generated SQL
		 * @param comparisonResult Comparison statistics
		 * @param duration Time taken in msec
		 * @param moreAvailable true if not all rows were compared
		 */
		public DiffResult(String sql, SortedMap<String, TaskProgress> comparisonResult, long duration, boolean moreAvailable)
			{
			this.sql = sql;
			this.comparisonResult = comparisonResult;
			this.duration = duration;
			this.moreAvailable = moreAvailable;
			}

		/**
		 * Get the sql
		 * @return the sql
		 */
		public String getSql()
			{
			return sql;
			}
		
		/**
		 * Get the comparisonResult
		 * @return the comparisonResult
		 */
		public SortedMap<String, TaskProgress> getComparisonResult()
			{
			return comparisonResult;
			}

		/**
		 * Get the duration
		 * @return the duration
		 */
		public long getDuration()
			{
			return duration;
			}
		
		/**
		 * Get the moreAvailable
		 * @return the moreAvailable
		 */
		public boolean isMoreAvailable()
			{
			return moreAvailable;
			}
		}
	
	private static final class RowProducer implements Runnable
		{
		private final QueryPerformerService runner;
		private final String connection;
		private final String statement;
		private final String label;
		private final RowHandler handler;
		
		public RowProducer(QueryPerformerService runner, String connection, String statement, String label, RowHandler handler)
			{
			this.runner = runner;
			this.connection = connection;
			this.statement = statement;
			this.label = label;
			this.handler = handler;
			}
		
		@Override
		public void run()
			{
			try	{
				runner.performCustomQuery(connection, JdbcConstants.QUERYTYPE_MULTIPLE, statement, label, handler);
				}
			catch (PerformQueryException e)
				{
				Logger.getLogger(getClass().getCanonicalName()).log(Level.WARNING, "RowProducer for " + connection + " failed", e.getCause());
				}
			}
		}
/*	
	private static final class RowConsumer implements Runnable
		{
		private final QueryPerformerService runner;
		private final String connection;
		private final String type;
		private final AsyncStatementIterator statements;
		private Result result;
		private RuntimeException error;
		
		public RowConsumer(QueryPerformerService runner, String connection, String type, AsyncStatementIterator statements)
			{
			this.runner = runner;
			this.connection = connection;
			this.type = type;
			this.statements = statements;
			}
		
		public Result getResult()
			{
			return (result);
			}
		
		public RuntimeException getError()
			{
			return (error);
			}
		
		@Override
		public void run()
			{
			try	{
				result = runner.performCustomQueries(connection, type, statements);
				}
			catch (PerformQueryException e)
				{
				statements.abort();
				error = e.getCause();
				throw error;
				}
			
			Logger.getLogger(getClass().getCanonicalName()).log(Level.INFO, "RowConsumer for " + connection + " finished");
			}
		}
*/	
	private static final class DiffRowTransferer implements RowTransferer
		{
		private final ResultCompareService transformer;
		private final RowIterator dst;
		private final CompareProgressMonitor monitor;
		private final TableDescription tableDesc;
		private final OrderBy orderBy;
		private final SQLDialect dialect;
		private final boolean merge;
		
		public DiffRowTransferer(ResultCompareService transformer, RowIterator dst, CompareProgressMonitor monitor, TableDescription tableDesc, OrderBy orderBy, SQLDialect dialect, boolean merge)
			{
			this.transformer = transformer;
			this.dst = dst;
			this.monitor = monitor;
			this.tableDesc = tableDesc;
			this.dialect = dialect;
			this.merge = merge;
			this.orderBy = orderBy;
			}
		
		@Override
		public Object transfer(RowIterator rows, StatementHandler handler)
			{
			if (orderBy == OrderBy.PK)
				transformer.compareResultsByPK(rows, dst, handler, monitor, tableDesc, dialect, merge);
			else
				transformer.compareResultsIgnoringPK(rows, dst, handler, monitor, tableDesc, dialect);
			return null;
			}
		
		@Override
		public String getPrepareStatement()
			{
			return (dialect.prepareInsert(tableDesc));
			}
		
		@Override
		public String getCleanupStatement()
			{
			return (dialect.finishInsert(tableDesc));
			}
		}
	
	private final TimeService timeService;
	private final MetadataService metadataService;
	private final QueryService queryService;
	private final QueryPerformerService runner;
	private final LinkService linkService;
	private final UserSettingsManager userSettingsManager;
	private final TaskProgressService taskProgressService;
	private final SQLGeneratorService sqlGenerator;
	private final DataFormatterFactory dataFormatterFactory;
	private final ResultCompareService transformer;
	private final UserSettings userSettings;
	private final ConnectionSettings connectionSettings;
	private final Logger logger;
	
	/**
	 * Constructor
	 * @param metadataService MetadataService
	 * @param queryService QueryService
	 * @param timeService TimeService
	 * @param runner QueryPerformerService
	 * @param linkService LinkService
	 * @param userSettingsManager UserSettingsManager
	 * @param sqlGenerator SQLGeneratorService
	 * @param dataFormatterFactory DataFormatterFactory
	 * @param transformer ResultCompareService
	 * @param taskProgressService TaskProgressService
	 * @param userSettings UserSettings
	 * @param connectionSettings ConnectionSettings
	 */
	@Autowired
	public DataDiffController(MetadataService metadataService, QueryService queryService, QueryPerformerService runner,
			TimeService timeService, LinkService linkService, UserSettingsManager userSettingsManager,
			SQLGeneratorService sqlGenerator, ResultCompareService transformer,
			DataFormatterFactory dataFormatterFactory, TaskProgressService taskProgressService,
			UserSettings userSettings, ConnectionSettings connectionSettings)
		{
		this.timeService = timeService;
		this.metadataService = metadataService;
		this.queryService = queryService;
		this.runner = runner;
		this.linkService = linkService;
		this.userSettingsManager = userSettingsManager;
		this.sqlGenerator = sqlGenerator;
		this.dataFormatterFactory = dataFormatterFactory;
		this.taskProgressService = taskProgressService;
		this.transformer = transformer;
		this.userSettings = userSettings;
		this.connectionSettings = connectionSettings;
		this.logger = Logger.getLogger(getClass().getCanonicalName());
		}
	
	/**
	 * Get the FormBackingObject
	 * @return FormBackingObject
	 */
	@ModelAttribute("model")
	public FormBackingObject getFormBackingObject()
		{
		final FormBackingObject ret = new FormBackingObject();
		
		return (ret);
		}
	
	/**
	 * Show the schema selection dialog
	 * @param fbo FormBackingObject
	 * @return Model
	 */
	@RequestMapping(value = "/db/*/dml.html", method = RequestMethod.GET)
	public Map<String, Object> showDMLForm(
			@ModelAttribute("model") FormBackingObject fbo
			)
		{
		if (!connectionSettings.isBrowserEnabled())
			throw new AccessDeniedException();
		
		final Map<String, Object> model = new HashMap<String, Object>();
		
		if ((fbo.getConnection2() != null) && (fbo.getCatalog2() != null) && (fbo.getSchema2() != null))
			{
			connectionSettings.getParameterHistory().put("connection2", fbo.getConnection2());
			connectionSettings.getParameterHistory().put("catalog2", fbo.getCatalog2());
			connectionSettings.getParameterHistory().put("schema2", fbo.getSchema2());
			}
		else
			{
			fbo.setConnection2(connectionSettings.getParameterHistory().get("connection2"));
			fbo.setCatalog2(connectionSettings.getParameterHistory().get("catalog2"));
			fbo.setSchema2(connectionSettings.getParameterHistory().get("schema2"));
			}
		
		if (connectionSettings.getParameterHistory().get("order") != null)
			fbo.setOrder(OrderBy.valueOf(connectionSettings.getParameterHistory().get("order")));
		fbo.setMerge(Boolean.valueOf(connectionSettings.getParameterHistory().get("merge")));
		fbo.setMode(connectionSettings.getParameterHistory().get("mode"));
		
		final Map<String, String> all = linkService.findAllLinkNames(userSettingsManager.getEffectiveUserGroups(userSettings.getPrincipal()), null, null);
		model.put("allConnections", all);
		
		final Set<QueryType> resultTypes = queryService.findScriptQueryTypes(connectionSettings.getType());
		model.put("resultTypes", resultTypes);
		
		model.put("orders", OrderBy.values());
		
		final QualifiedName qn = new QualifiedName(fbo.getCatalog(), fbo.getSchema(), fbo.getObject());
		final TableFilterEntry filter = connectionSettings.getTableFilters().get(qn.toString());
		if (filter != null)
			fbo.setFilter(filter.getWhere());
		
		final TableDescription srcDesc = metadataService.getTableInfo(connectionSettings.getLinkName(), qn, ColumnMode.ALL);
		final Set<Integer> pk = srcDesc.getPKColumns();
		
		fbo.getPkColumns().clear();
		fbo.getDataColumns().clear();
		int i = 0;
		for (ColumnDescription c : srcDesc.getColumns())
			{
			if (pk.contains(i))
				fbo.getPkColumns().add(c.getName());
			else
				fbo.getDataColumns().add(c.getName());
			i++;
			}
		
		model.put("allColumns", srcDesc.getColumns());
		
		if (fbo.getConnection2() != null)
			{
			model.put("catalogs", metadataService.getCatalogs(fbo.getConnection2()));
			if (fbo.getCatalog2() != null)
				model.put("schemas", metadataService.getSchemas(fbo.getConnection2()));
			}
		
		model.put("extensionJS", "jdbc.js");
		
		return (model);
		}
	
	/**
	 * Show the schema selection dialog
	 * @return Model
	 */
	@RequestMapping(value = "/ws/*/form-dml.html", method = RequestMethod.GET)
	public Map<String, Object> showDMLWSForm()
		{
		final Map<String, Object> model = new HashMap<String, Object>();
		
		final Map<String, String> all = linkService.findAllLinkNames(null, null, null);
		model.put("allConnections", all);
		
		final Set<QueryType> resultTypes = queryService.findScriptQueryTypes(connectionSettings.getType());
		model.put("resultTypes", resultTypes);
		
		model.put("orders", OrderBy.values());
		
		return (model);
		}
	
	/**
	 * Show a parameter input form
	 * @param fbo FormBackingObject
	 * @return Model
	 */
	@RequestMapping(value = "/db/*/ajax/dml.html", method = RequestMethod.POST)
	public Map<String, Object> runDML(
			@ModelAttribute("model") FormBackingObject fbo
			)
		{
		if (!connectionSettings.isBrowserEnabled())
			throw new AccessDeniedException();
		
		final Map<String, Object> model = new HashMap<String, Object>();
		
		final TaskDMLProgressMonitor p = taskProgressService.createDMLProgressMonitor();
		if (p == null)
			{
			model.put("alreadyRunning", Boolean.TRUE);
			model.put("progress", taskProgressService.getProgress());
			return (model);
			}
		final TaskCompareProgressMonitor c = taskProgressService.createCompareProgressMonitor();
		
		final QualifiedName qn = new QualifiedName(fbo.getCatalog(), fbo.getSchema(), fbo.getObject());
		
		try	{
			final DiffResult result = runDMLInternal(fbo.getCatalog(), fbo.getSchema(), fbo.getObject(), fbo.getConnection2(), fbo.getCatalog2(), fbo.getSchema2(), fbo.getFilter(), fbo.getOrder(), fbo.isMerge(), fbo.getMode(), fbo.getPkColumns(), fbo.getDataColumns(), p, c, false);
			
			model.put("result", result);
			
			connectionSettings.getParameterHistory().put("connection2", fbo.getConnection2());
			connectionSettings.getParameterHistory().put("catalog2", fbo.getCatalog2());
			connectionSettings.getParameterHistory().put("schema2", fbo.getSchema2());
			connectionSettings.getParameterHistory().put("order", (fbo.getOrder() == null) ? null : fbo.getOrder().name());
			connectionSettings.getParameterHistory().put("merge", String.valueOf(fbo.isMerge()));
			connectionSettings.getParameterHistory().put("mode", fbo.getMode());
			
			final TableFilterEntry ent = connectionSettings.getTableFilters().get(qn.toString());
			if (ent != null)
				connectionSettings.getTableFilters().put(qn.toString(), new TableFilterEntry(fbo.getFilter(), ent.getOrderBy()));
			else
				connectionSettings.getTableFilters().put(qn.toString(), new TableFilterEntry(fbo.getFilter(), ""));
			}
		catch (PerformQueryException e)
			{
			model.put("exception", e.getCause());
			}
		catch (CancelledByUserException e)
			{
			model.put("cancelled", Boolean.TRUE);
			}
		catch (RuntimeException e)
			{
			logger.log(Level.WARNING, "runCompareIDs", e);
			model.put("exception", e);
			}
		finally
			{
			taskProgressService.removeCompareProgressMonitor();
			taskProgressService.removeDMLProgressMonitor();
			}
		
		return (model);
		}
	
	/**
	 * Show a parameter input form
	 * @param fbo FormBackingObject
	 * @return Model
	 */
	@RequestMapping(value = "/ws/*/dml.html", method = RequestMethod.GET)
	public Map<String, Object> runDMLWS(
			@ModelAttribute("model") FormBackingObject fbo
			)
		{
		final Map<String, Object> model = new HashMap<String, Object>();
		
		final TaskDMLProgressMonitor p = new TaskDMLProgressMonitor();
		final TaskCompareProgressMonitor c = new TaskCompareProgressMonitor();
		
		try	{
			final DiffResult result = runDMLInternal(fbo.getCatalog(), fbo.getSchema(), fbo.getObject(), fbo.getConnection2(), fbo.getCatalog2(), fbo.getSchema2(), fbo.getFilter(), fbo.getOrder(), fbo.isMerge(), fbo.getMode(), fbo.getPkColumns(), fbo.getDataColumns(), p, c, true);
			
			model.put("result", result);
			}
		catch (PerformQueryException e)
			{
			model.put("exception", e.getCause());
			}
		catch (CancelledByUserException e)
			{
			model.put("cancelled", Boolean.TRUE);
			}
		catch (RuntimeException e)
			{
			logger.log(Level.WARNING, "runCompareIDs", e);
			model.put("exception", e);
			}
		
		return (model);
		}
	
	private DiffResult runDMLInternal(
			String catalog,
			String schema,
			String table,
			String conn2,
			String catalog2,
			String schema2,
			String filter,
			OrderBy order,
			boolean merge,
			String mode,
			Set<String> pkColumns,
			Set<String> dataColumns,
			TaskDMLProgressMonitor p,
			TaskCompareProgressMonitor c,
			boolean flush
			) throws PerformQueryException
		{
		if (!StringUtils.empty(mode) && connectionSettings.isWritable())
			{
			if (order == OrderBy.NONE)
				return (runCompareFull(new QualifiedName(catalog, schema, table), new QualifiedName(catalog2, schema2, table), conn2, filter, merge, mode, c, p, flush));
			else
				return (runCompareByPK(new QualifiedName(catalog, schema, table), new QualifiedName(catalog2, schema2, table), conn2, filter, merge, mode, pkColumns, dataColumns, order, c, p, flush));
			}
		else
			{
			if (order == OrderBy.NONE)
				return (compareFull(new QualifiedName(catalog, schema, table), new QualifiedName(catalog2, schema2, table), conn2, filter, merge, c, p, flush));
			else
				return (compareByPK(new QualifiedName(catalog, schema, table), new QualifiedName(catalog2, schema2, table), conn2, filter, merge, pkColumns, dataColumns, order, c, p, flush));
			}
		}
	
	private DiffResult compareFull(QualifiedName srcName, QualifiedName dstName, String conn2, String filter, boolean merge, TaskCompareProgressMonitor c, TaskDMLProgressMonitor p, boolean flush) throws PerformQueryException
		{
		if (flush)
			metadataService.flushCache(connectionSettings.getLinkName());
		
		final TableDescription srcDesc = metadataService.getTableInfo(connectionSettings.getLinkName(), srcName, ColumnMode.SORTED);
		// Don't read destination table description because we want to select the same columns (and fail if that's not possible) 
		final TableDescription dstDesc = new TableDescription(dstName.getCatalogName(), dstName.getSchemaName(), dstName.getObjectName(), srcDesc.getComment(), srcDesc.getType(), srcDesc.getPrimaryKey(), srcDesc.getColumns(), srcDesc.getIndices(), srcDesc.getReferencedKeys(), srcDesc.getReferencingKeys(), srcDesc.getPrivileges());
		
		final SQLDialect dialect = getSQLDialect();
		final String prepare = dialect.prepareInsert(dstDesc);
		final String cleanup = dialect.finishInsert(dstDesc);
		
		final StringWriter sw = new StringWriter();
		final StatementHandler h = new MonitoringStatementHandler(new StatementWriter(sw, dialect.getStatementTerminator()), p.getTotalStatements());
		
		h.comment(getHeader(connectionSettings.getLinkName(), conn2));
		
		if (!StringUtils.empty(prepare))
			h.statement(prepare);
		
		final DiffResult tempResult = compareFull(srcDesc, dstDesc, conn2, dialect, filter, merge, h, c, false);
		
		if (!StringUtils.empty(cleanup))
			h.statement(cleanup);
		
		return (new DiffResult(sw.toString(), tempResult.getComparisonResult(), tempResult.getDuration(), tempResult.isMoreAvailable()));
		}
	
	private DiffResult runCompareFull(QualifiedName srcName, QualifiedName dstName, String conn2, String filter, boolean merge, String mode, TaskCompareProgressMonitor c, TaskDMLProgressMonitor p, boolean flush) throws PerformQueryException
		{
		if (flush)
			metadataService.flushCache(connectionSettings.getLinkName());
		
		final TableDescription srcDesc = metadataService.getTableInfo(connectionSettings.getLinkName(), srcName, ColumnMode.SORTED);
		// Don't read destination table description because we want to select the same columns (and fail if that's not possible) 
		final TableDescription dstDesc = new TableDescription(dstName.getCatalogName(), dstName.getSchemaName(), dstName.getObjectName(), srcDesc.getComment(), srcDesc.getType(), srcDesc.getPrimaryKey(), srcDesc.getColumns(), srcDesc.getIndices(), srcDesc.getReferencedKeys(), srcDesc.getReferencingKeys(), srcDesc.getPrivileges());
		
		final SQLDialect dialect = getSQLDialect();
		final String prepare = dialect.prepareInsert(dstDesc);
		final String cleanup = dialect.finishInsert(dstDesc);
		
		final StatementCollection h = new StatementCollection(prepare, cleanup);
		
		final DiffResult tempResult = compareFull(srcDesc, dstDesc, conn2, dialect, filter, merge, h, c, true);
		
		final Result r = runner.performCustomQueries(connectionSettings.getLinkName(), h, mode, p);
		
		return (new DiffResult(String.valueOf(r.getFirstRowSet().getFirstValue()), tempResult.getComparisonResult(), tempResult.getDuration(), tempResult.isMoreAvailable()));
		}
	
	private DiffResult compareFull(TableDescription srcDesc, TableDescription dstDesc, String conn2, SQLDialect dialect, String filter, boolean merge, StatementHandler h, TaskCompareProgressMonitor c, boolean export) throws PerformQueryException
		{
		final long start = timeService.getCurrentTime();
		
		final String srcStmt = sqlGenerator.generateSelect(srcDesc, Style.SIMPLE, Joins.NONE, filter, OrderBy.PK, dialect);
		final Result r1 = runner.performCustomQuery(connectionSettings.getLinkName(), JdbcConstants.QUERYTYPE_MULTIPLE, srcStmt, null, null, "diff", export, null);
		
		final String dstStmt = sqlGenerator.generateSelect(dstDesc, Style.SIMPLE, Joins.NONE, filter, OrderBy.PK, dialect);
		final Result r2 = runner.performCustomQuery(conn2, JdbcConstants.QUERYTYPE_MULTIPLE, dstStmt, null, null, "diff", export, null);
		
		transformer.compareResults(r1.getFirstRowSet(), r2.getFirstRowSet(), h, c, srcDesc, dialect, merge);
		
		final long end = timeService.getCurrentTime();
		
		final boolean moreAvailable = r1.getFirstRowSet().isMoreAvailable() || r2.getFirstRowSet().isMoreAvailable();
		
		return (new DiffResult(null, collectStatistics(c, null), end - start, moreAvailable));
		}
	
	private String getHeader(String c1, String c2)
		{
		return (dataFormatterFactory.getMessage(MessageKeys.DML_COMPARE_HEADER, c1, c2));
		}
	
	private SQLDialect getSQLDialect()
		{
		return (SQLDialectFactory.getSQLDialect(connectionSettings.getDialectName()));
		}
	
	private DiffResult compareByPK(QualifiedName srcName, QualifiedName dstName, String conn2, String filter, boolean merge, Set<String> pkColumns, Set<String> dataColumns, OrderBy order, TaskCompareProgressMonitor c, TaskDMLProgressMonitor p, boolean flush) throws PerformQueryException
		{
		final SQLDialect dialect = getSQLDialect();
		
		if (flush)
			metadataService.flushCache(connectionSettings.getLinkName());
		
		final TableDescription origDesc = metadataService.getTableInfo(connectionSettings.getLinkName(), srcName, ColumnMode.SORTED);
		final TableDescription srcDesc = filterColumns(origDesc, pkColumns, dataColumns);
		// Don't read destination table description because we want to select the same columns (and fail if that's not possible) 
		final TableDescription dstDesc = new TableDescription(dstName.getCatalogName(), dstName.getSchemaName(), dstName.getObjectName(), srcDesc.getComment(), srcDesc.getType(), srcDesc.getPrimaryKey(), srcDesc.getColumns(), srcDesc.getIndices(), srcDesc.getReferencedKeys(), srcDesc.getReferencingKeys(), srcDesc.getPrivileges());
		
		final String prepare = dialect.prepareInsert(dstDesc);
		final String cleanup = dialect.finishInsert(dstDesc);
		
		final StringWriter sw = new StringWriter();
		final StatementHandler h3 = new MonitoringStatementHandler(new StatementWriter(sw, dialect.getStatementTerminator()), p.getTotalStatements());
		
		h3.comment(getHeader(connectionSettings.getLinkName(), conn2));
		
		if (!StringUtils.empty(prepare))
			h3.statement(prepare);
		
		final DiffResult tempResult = compareByPK(srcDesc, dstDesc, conn2, dialect, filter, merge, order, h3, JdbcConstants.QUERYTYPE_TOLERANT_SCRIPT, c, null, false);
		
		if (!StringUtils.empty(cleanup))
			h3.statement(cleanup);
		
		return (new DiffResult(sw.toString(), tempResult.getComparisonResult(), tempResult.getDuration(), tempResult.isMoreAvailable()));
		}
	
	private DiffResult runCompareByPK(QualifiedName srcName, QualifiedName dstName, String conn2, String filter, boolean merge, String mode, Set<String> pkColumns, Set<String> dataColumns, OrderBy order, TaskCompareProgressMonitor c, TaskDMLProgressMonitor p, boolean flush) throws PerformQueryException
		{
		final SQLDialect dialect = getSQLDialect();
		
		if (flush)
			metadataService.flushCache(connectionSettings.getLinkName());
		
		final TableDescription origDesc = metadataService.getTableInfo(connectionSettings.getLinkName(), srcName, ColumnMode.SORTED);
		final TableDescription srcDesc = filterColumns(origDesc, pkColumns, dataColumns);
		// Don't read destination table description because we want to select the same columns (and fail if that's not possible) 
		final TableDescription dstDesc = new TableDescription(dstName.getCatalogName(), dstName.getSchemaName(), dstName.getObjectName(), srcDesc.getComment(), srcDesc.getType(), srcDesc.getPrimaryKey(), srcDesc.getColumns(), srcDesc.getIndices(), srcDesc.getReferencedKeys(), srcDesc.getReferencingKeys(), srcDesc.getPrivileges());
		
		return (compareByPK(srcDesc, dstDesc, conn2, dialect, filter, merge, order, null, mode, c, p, true));
		}
	
	private TableDescription filterColumns(TableDescription srcDesc, Set<String> pkColumns, Set<String> dataColumns)
		{
		if ((pkColumns == null) || (dataColumns == null) || (pkColumns.isEmpty() && dataColumns.isEmpty()))
			return (srcDesc);
		
		final List<String> pkCols = new ArrayList<String>();
		final List<ColumnDescription> columns = new ArrayList<ColumnDescription>();
		
		for (ColumnDescription c : srcDesc.getColumns())
			{
			if (pkColumns.contains(c.getName()))
				{
				pkCols.add(c.getName());
				columns.add(c);
				}
			else if (dataColumns.contains(c.getName()))
				{
				columns.add(c);
				}
			}
		
		final PrimaryKeyDescription pk = new PrimaryKeyDescription(null, pkCols);
		
		return (new TableDescription(srcDesc.getName().getCatalogName(), srcDesc.getName().getSchemaName(), srcDesc.getName().getObjectName(), srcDesc.getComment(), srcDesc.getType(), pk, columns, null, null, null, null));
		}
	
	private DiffResult compareByPK(TableDescription srcDesc, TableDescription dstDesc, String conn2, SQLDialect dialect, String filter, boolean merge, OrderBy order, StatementHandler h3, String type, TaskCompareProgressMonitor c, TaskDMLProgressMonitor p, boolean export) throws PerformQueryException
		{
		final String srcStmt = sqlGenerator.generateSelect(srcDesc, Style.SIMPLE, Joins.NONE, filter, order, dialect);
		final String dstStmt = sqlGenerator.generateSelect(dstDesc, Style.SIMPLE, Joins.NONE, filter, order, dialect);
		
		final AsyncRowIterator h2 = new AsyncRowIterator();
		final RowProducer p2 = new RowProducer(runner, conn2, dstStmt, "diff", h2);
		final Thread dst = new Thread(p2);
		
		dst.start();
		
		final long start = timeService.getCurrentTime();
		
		final Result res;
		try	{
			final DiffRowTransferer transferer = new DiffRowTransferer(transformer, h2, c, srcDesc, order, dialect, merge);
			res = runner.transferRows(connectionSettings.getLinkName(), srcStmt, transferer, h3, type, p, export);
			}
		finally
			{
			try	{
				h2.abort();
				dst.interrupt();
				dst.join();
				}
			catch (InterruptedException e)
				{
				logger.log(Level.SEVERE, "Error joining RowProducer for " + conn2, e);
	//			throw new RuntimeException(e);
				}
			}
		
		final long end = timeService.getCurrentTime();
		
		return (new DiffResult(String.valueOf(res.getFirstRowSet().getFirstValue()), collectStatistics(c, p), end - start, !export));
		}
	
	private SortedMap<String, TaskProgress> collectStatistics(TaskCompareProgressMonitor c, TaskDMLProgressMonitor p)
		{
//		return (taskProgressService.getProgress());
		final SortedMap<String, TaskProgress> ret = new TreeMap<String, TaskProgress>();
		ret.put(MessageKeys.SOURCE_ROWS, c.getSourceRows());
		ret.put(MessageKeys.DESTINATION_ROWS, c.getDestinationRows());
		ret.put(MessageKeys.MATCHED, c.getMatchedRows());
		ret.put(MessageKeys.INSERTED, c.getInsertedRows());
		ret.put(MessageKeys.UPDATED, c.getUpdatedRows());
		ret.put(MessageKeys.DELETED, c.getDeletedRows());
		if (p != null)
			{
			ret.put(MessageKeys.TOTAL_STATEMENTS, p.getTotalStatements());
			ret.put(MessageKeys.FAILED_STATEMENTS, p.getFailedStatements());
			ret.put(MessageKeys.TOTAL_ROWS, p.getTotalRows());
			ret.put(MessageKeys.COMMITTED_ROWS, p.getCommittedRows());
			}
		return (ret);
		}
	}
