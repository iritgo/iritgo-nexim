/**
 * This file is part of the neXim XMPP server
 *
 * Copyright (C) 2005-2011 Iritgo Technologies
 * Copyright (C) 2003-2005 BueroByte GbR
 * Copyright (c) 2003, OpenIM Project http://open-im.net
 *
 * Iritgo licenses this file to You under the BSD License
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * BSD License http://www.opensource.org/licenses/bsd-license.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.iritgo.nexim.log;


/**
 * The default logger interface for neXim.
 * The implementation is defined in the Spring configuration "nexim-logger.spring.xml".
 */
public interface DefaultNeximLogger
{
	/**
	 * Error message
	 *
	 * @param message The message to log
	 */
	public void error(String message);

	/**
	 * Error message
	 *
	 * @param message The message
	 * @param e The exception
	 */
	public void error(String message, Exception e);

	/**
	 * Debug message
	 *
	 * @param message The message to log
	 */
	public void debug(String message);

	/**
	 * Debug message
	 *
	 * @param message The message
	 * @param e The exception
	 */
	public void debug(String message, Exception e);

	/**
	 * Warn message
	 *
	 * @param message The message
	 */
	public void warn(String message);

	/**
	 * Warn message
	 *
	 * @param message The message
	 * @param e The exception
	 */
	public void warn(String message, Exception e);

	/**
	 * Info message
	 *
	 * @param message The message
	 */
	public void info(String message);

	/**
	 * Info message
	 *
	 * @param message The message
	 * @param e The exception
	 */
	public void info(String message, Exception e);

	/**
	 * If info logging enabled
	 *
	 * @return True if enabeld
	 */
	public boolean isInfoEnabled();
}
