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
package de.tweerlei.dbgrazer.web.export.dml.download;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import de.tweerlei.common.io.ByteOrderMarkWriter;
import de.tweerlei.ermtools.dialect.SQLDialect;
import de.tweerlei.spring.web.view.AbstractDownloadSource;
import de.tweerlei.spring.web.view.DownloadSource;

/**
 * Download a RowSet as SQL INSERT statements
 * 
 * @author Robert Wruck
 */
public abstract class AbstractDDLDownloadSource extends AbstractDownloadSource
	{
	private static final String CHARSET = "UTF-8";
	private static final String CONTENT_TYPE = "text/plain; charset=" + CHARSET;
	private static final String EXTENSION = ".sql";
	
	private final SQLDialect dialect;
	
	/**
	 * Constructor
	 * @param name File base name
	 * @param dialect SQLDialect
	 */
	public AbstractDDLDownloadSource(String name, SQLDialect dialect)
		{
		this.dialect = dialect;
		
		this.setAttachment(true);
		this.setExpireTime(DownloadSource.ALWAYS);
		this.setFileName(name + EXTENSION);
		}
	
	@Override
	public final String getContentType()
		{
		return (CONTENT_TYPE);
		}
	
	@Override
	public final void write(OutputStream stream) throws IOException
		{
		final Writer osw = new ByteOrderMarkWriter(new OutputStreamWriter(stream, CHARSET));
		try	{
			writeDDL(osw, dialect);
			}
		finally
			{
			osw.flush();
			}
		}
	
	/**
	 * Write the SQL statements
	 * @param sw SQLWriter
	 * @param d SQLDialect
	 * @throws IOException on error
	 */
	protected abstract void writeDDL(Writer sw, SQLDialect d) throws IOException;
	}
