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
package de.tweerlei.dbgrazer.text.service;

import java.util.Set;

/**
 * Format strings
 * 
 * @author Robert Wruck
 */
public interface TextFormatterService
	{
	/**
	 * Get the supported format names for getTextFormatter
	 * @return Format names
	 */
	public Set<String> getSupportedTextFormats();
	
	/**
	 * Format a String using a given format name
	 * @param text String to format
	 * @param format Format tag
	 * @return Formatted String
	 */
	public String format(String text, String format);
	
	/**
	 * Check whether the result of {@link #format(String, String)} is already encoded for usage in XML
	 * @param format Format tag
	 * @return true if encoded, false for raw text
	 */
	public boolean isXMLEncoded(String format);
	}
