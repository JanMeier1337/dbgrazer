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
package de.tweerlei.dbgrazer.extension.kafka;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.RecordMetadata;

/**
 * Perform HTTP requests
 * 
 * @author Robert Wruck
 */
public interface KafkaClientService
	{
	/**
	 * Get a KafkaConsumer for a link
	 * @param c Link name
	 * @return KafkaConsumer
	 */
	public Consumer<String, String> getConsumer(String c);
	
	/**
	 * Get a KafkaProducer for a link
	 * @param c Link name
	 * @return KafkaProducer
	 */
	public Producer<String, String> getProducer(String c);
	
	/**
	 * Fetch a single record
	 * @param c Link name
	 * @param topic Topic name
	 * @param partition Partition number
	 * @param offset Record offset
	 * @return ConsumerRecord or null
	 */
	public ConsumerRecord<String, String> fetchRecord(String c, String topic, int partition, long offset);
	
	/**
	 * Fetch records
	 * @param c Link name
	 * @param topic Topic name
	 * @param partition Partition number (null to fetch from all partitions)
	 * @param offset Record offset (null to fetch from current offset)
	 * @return ConsumerRecord or null
	 */
	public ConsumerRecords<String, String> fetchRecords(String c, String topic, Integer partition, Long offset);
	
	/**
	 * Send a record
	 * @param c Link name
	 * @param topic Topic name
	 * @param partition Partition number (null to send to any partition)
	 * @param key Message key
	 * @param value Message value
	 * @return RecordMetadata
	 */
	public RecordMetadata sendRecord(String c, String topic, Integer partition, String key, String value);
	}
