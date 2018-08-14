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
package de.tweerlei.dbgrazer.link.backend.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import de.tweerlei.dbgrazer.common.file.FileAccess;
import de.tweerlei.dbgrazer.common.service.ConfigFileStore;
import de.tweerlei.dbgrazer.link.backend.LinkPersister;
import de.tweerlei.spring.config.ConfigAccessor;

/**
 * LinkLoader that uses an ExternalFileAccess
 * 
 * @author Robert Wruck
 */
@Service("externalLinkLoader")
public class ExternalFileLinkLoader extends AbstractFileLinkLoader
	{
	/**
	 * Constructor
	 * @param store ConfigFileStore
	 * @param configService ConfigAccessor
	 * @param persister LinkPersister
	 * @param fileAccess FileAccess
	 */
	@Autowired
	public ExternalFileLinkLoader(ConfigFileStore store, ConfigAccessor configService, LinkPersister persister,
			@Qualifier("externalFileAccess") FileAccess fileAccess)
		{
		super(store, configService, persister, fileAccess);
		}
	}
