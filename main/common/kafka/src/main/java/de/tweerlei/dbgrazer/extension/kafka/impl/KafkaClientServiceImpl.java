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
package de.tweerlei.dbgrazer.extension.kafka.impl;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.tweerlei.common.util.StringUtils;
import de.tweerlei.common5.collections.StringComparators;
import de.tweerlei.dbgrazer.common.service.ConfigFileStore;
import de.tweerlei.dbgrazer.extension.kafka.ConfigKeys;
import de.tweerlei.dbgrazer.extension.kafka.KafkaClientService;
import de.tweerlei.dbgrazer.link.model.LinkDef;
import de.tweerlei.dbgrazer.link.service.LinkListener;
import de.tweerlei.dbgrazer.link.service.LinkManager;
import de.tweerlei.dbgrazer.link.service.LinkService;
import de.tweerlei.spring.config.ConfigAccessor;

/**
 * Default impl.
 * 
 * @author Robert Wruck
 */
@Service
public class KafkaClientServiceImpl implements KafkaClientService, LinkListener, LinkManager
	{
	private static class KafkaConnectionHolder
		{
		private final List<KafkaConsumer<String, String>> allConnections;
		private final ThreadLocal<KafkaConsumer<String, String>> activeConnection;
		
		public KafkaConnectionHolder()
			{
			this.allConnections = new LinkedList<KafkaConsumer<String, String>>();
			this.activeConnection = new ThreadLocal<KafkaConsumer<String, String>>();
			}
		
		public KafkaConsumer<String, String> getConsumer()
			{
			return (activeConnection.get());
			}
		
		public synchronized void setConsumer(KafkaConsumer<String, String> consumer)
			{
			allConnections.add(consumer);
			activeConnection.set(consumer);
			}
		
		public synchronized void close()
			{
			for (KafkaConsumer<String, String> consumer : allConnections)
				consumer.close();
			}
		}
	
	private final ConfigFileStore configFileStore;
	private final ConfigAccessor configService;
	private final LinkService linkService;
	private final Logger logger;
	private final Map<String, KafkaConnectionHolder> activeConnections;
	
	/**
	 * Constructor
	 * @param configFileStore ConfigFileStore
	 * @param configService ConfigAccessor
	 * @param linkService LinkService
	 */
	@Autowired
	public KafkaClientServiceImpl(ConfigFileStore configFileStore, ConfigAccessor configService, LinkService linkService)
		{
		this.configFileStore = configFileStore;
		this.configService = configService;
		this.linkService = linkService;
		this.logger = Logger.getLogger(getClass().getCanonicalName());
		this.activeConnections = new ConcurrentHashMap<String, KafkaConnectionHolder>();
		}
	
	/**
	 * Register for config changes
	 */
	@PostConstruct
	public void init()
		{
		linkService.addListener(this);
		linkService.addManager(this);
		}
	
	@Override
	public void linksChanged()
		{
		closeConnections();
		}
	
	@Override
	public void linkChanged(String link)
		{
		closeConnection(link);
		}
	
	/**
	 * Close all connections
	 */
	@PreDestroy
	public synchronized void closeConnections()
		{
		logger.log(Level.INFO, "Closing " + activeConnections.size() + " connections");
		
		for (KafkaConnectionHolder holder : activeConnections.values())
			holder.close();
		
		activeConnections.clear();
		}
	
	private synchronized void closeConnection(String link)
		{
		final KafkaConnectionHolder holder = activeConnections.remove(link);
		if (holder != null)
			{
			logger.log(Level.INFO, "Closing connection " + link);
			holder.close();
			}
		}
	
	@Override
	public KafkaConsumer<String, String> getConsumer(String c)
		{
		final KafkaConnectionHolder holder = activeConnections.get(c);
		if (holder != null)
			{
			final KafkaConsumer<String, String> ret = holder.getConsumer();
			if (ret != null)
				return (ret);
			}
		
		return (createClient(c));
		}
	
	@Override
	public ConsumerRecord<String, String> fetchRecord(String c, String topic, int partition, long offset)
		{
		final KafkaConsumer<String, String> consumer = getConsumer(c);
		final TopicPartition tp = new TopicPartition(topic, partition);
		
		consumer.assign(Collections.singleton(tp));
		consumer.seek(tp, offset);
		final ConsumerRecords<String, String> records = consumer.poll(configService.get(ConfigKeys.KAFKA_FETCH_TIMEOUT));
		consumer.unsubscribe();
		
		for (ConsumerRecord<String, String> record : records)
			{
			if (record.offset() == offset)
				return (record);
			}
		
		return (null);
		}
	
	@Override
	public ConsumerRecords<String, String> fetchRecords(String c, String topic, Integer partition, Long offset)
		{
		final KafkaConsumer<String, String> consumer = getConsumer(c);
		
		if (partition != null)
			{
			final TopicPartition tp = new TopicPartition(topic, partition);
			consumer.assign(Collections.singleton(tp));
			if (offset != null)
				consumer.seek(tp, offset);
			}
		else
			consumer.subscribe(Collections.singleton(topic));
		
		final ConsumerRecords<String, String> records = consumer.poll(configService.get(ConfigKeys.KAFKA_FETCH_TIMEOUT));
		
		consumer.unsubscribe();
		
		return (records);
		}
	
	@Override
	public Map<String, Integer> getLinkStats()
		{
		final Map<String, Integer> ret = new TreeMap<String, Integer>(StringComparators.CASE_INSENSITIVE);
		
		for (Map.Entry<String, KafkaConnectionHolder> ent : activeConnections.entrySet())
			ret.put(ent.getKey(), 1);
		
		return (ret);
		}
	
	private synchronized KafkaConsumer<String, String> createClient(String c)
		{
		KafkaConnectionHolder holder = activeConnections.get(c);
		KafkaConsumer<String, String> ret = null;
		if (holder != null)
			{
			ret = holder.getConsumer();
			if (ret != null)
				return (ret);
			}
		else
			{
			holder = new KafkaConnectionHolder();
			activeConnections.put(c, holder);
			}
		
		final LinkDef def = linkService.getLink(c, null);
		if ((def == null) /*|| !(def.getType() instanceof WebserviceLinkType)*/)
			throw new RuntimeException("Unknown link " + c);
		
		final Properties props = new Properties();
		props.putAll(def.getProperties());
		
		// Override specific properties from link settings 
		props.setProperty("bootstrap.servers", def.getUrl());
		if (!StringUtils.empty(def.getUsername()))
			props.setProperty("sasl.jaas.config", "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"" + def.getUsername() + "\" password=\"" + def.getPassword() + "\";");
		
		// Resolve key store paths
		final String keyStorePath = props.getProperty("ssl.keystore.location");
		if (!StringUtils.empty(keyStorePath))
			props.setProperty("ssl.keystore.location", configFileStore.getFileLocation(keyStorePath).getAbsolutePath());
		
		final String trustStorePath = props.getProperty("ssl.truststore.location");
		if (!StringUtils.empty(trustStorePath))
			props.setProperty("ssl.truststore.location", configFileStore.getFileLocation(trustStorePath).getAbsolutePath());
		
		// Always treat contents as Strings
		props.setProperty("key.deserializer", StringDeserializer.class.getName());
		props.setProperty("value.deserializer", StringDeserializer.class.getName());
		
		// Don't commit offsets, start at earliest message
		props.setProperty("enable.auto.commit", "false");
		props.setProperty("auto.offset.reset", "earliest");
		
		ret = new KafkaConsumer<String, String>(props);
		
		holder.setConsumer(ret);
		return (ret);
		}
	}
