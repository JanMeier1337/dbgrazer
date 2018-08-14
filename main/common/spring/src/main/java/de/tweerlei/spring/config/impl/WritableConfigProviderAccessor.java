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
package de.tweerlei.spring.config.impl;

import de.tweerlei.spring.config.ConfigKey;
import de.tweerlei.spring.config.WritableConfigProvider;
import de.tweerlei.spring.config.WritableConfigProviderHolder;
import de.tweerlei.spring.service.SerializerFactory;

/**
 * ConfigAccessor backed by a ConfigProvider
 * 
 * @author Robert Wruck
 */
public class WritableConfigProviderAccessor extends AbstractWritableConfigAccessor implements WritableConfigProviderHolder
	{
	private final WritableConfigProvider provider;
	private final SerializerFactory factory;
	
	/**
	 * Constructor
	 * @param provider WritableConfigProvider
	 * @param factory SerializerFactory
	 */
	public WritableConfigProviderAccessor(WritableConfigProvider provider, SerializerFactory factory)
		{
		this.provider = provider;
		this.factory = factory;
		}
	
	public <T> T getRaw(ConfigKey<T> key)
		{
		final String value = provider.get(key.getKey());
		if (value == null)
			return (null);
		
		final T ret = factory.decode(key.getType(), value);
		return (ret);
		}
	
	public <T> T putRaw(ConfigKey<T> key, T value)
		{
		String s;
		if (value == null)
			s = null;
		else
			s = factory.encode(key.getType(), value);
		
		s = provider.put(key.getKey(), s);
		if (s == null)
			return (null);
		
		final T ret = factory.decode(key.getType(), s);
		return (ret);
		}
	
	public WritableConfigProvider getConfigProvider()
		{
		return (provider);
		}
	}
