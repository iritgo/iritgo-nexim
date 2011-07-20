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
 * Default logger implementation.
 */
public class DefaultNeximLoggerImpl implements DefaultNeximLogger
{
	/** Prefix printed before each log message. */
	private String prefix = "";

	/**
	 * Set the log prefix.
	 *
	 * @param prefix
	 *            The new log prefix
	 */
	public void setPrefix (String prefix)
	{
		if (prefix != null && ! prefix.isEmpty ())
		{
			this.prefix = prefix + " ";
		}
		else
		{
			this.prefix = "";
		}
	}

	/**
	 * @see de.iritgo.nexim.log.DefaultNeximLogger#error(java.lang.String)
	 */
	public void error (String message)
	{
		System.out.println (prefix + "ERROR: " + message);
	}

	/**
	 * @see de.iritgo.nexim.log.DefaultNeximLogger#error(java.lang.String,
	 *      java.lang.Exception)
	 */
	public void error (String message, Exception e)
	{
		System.out.println (prefix + "ERROR: " + message + " : " + e);
	}

	/**
	 * @see de.iritgo.nexim.log.DefaultNeximLogger#debug(java.lang.String)
	 */
	public void debug (String message)
	{
		//		System.outqq.println (prefix + "DEBUG: " + message);
	}

	/**
	 * @see de.iritgo.nexim.log.DefaultNeximLogger#debug(java.lang.String,
	 *      java.lang.Exception)
	 */
	public void debug (String message, Exception e)
	{
		//		System.out.println (prefix + "DEBUG: " + message + " : " + e);
	}

	/**
	 * @see de.iritgo.nexim.log.DefaultNeximLogger#warn(java.lang.String)
	 */
	public void warn (String message)
	{
		System.out.println (prefix + "WARN: " + message);
	}

	/**
	 * @see de.iritgo.nexim.log.DefaultNeximLogger#debug(java.lang.String,
	 *      java.lang.Exception)
	 */
	public void warn (String message, Exception e)
	{
		System.out.println (prefix + "WARN: " + message + " : " + e);
	}

	/**
	 * @see de.iritgo.nexim.log.DefaultNeximLogger#info(java.lang.String)
	 */
	public void info (String message)
	{
		System.out.println (prefix + "INFO: " + message);
	}

	/**
	 * @see de.iritgo.nexim.log.DefaultNeximLogger#debug(java.lang.String,
	 *      java.lang.Exception)
	 */
	public void info (String message, Exception e)
	{
		System.out.println (prefix + "INFO: " + message + " : " + e);
	}

	/**
	 * @see de.iritgo.nexim.log.DefaultNeximLogger#isInfoEnabled()
	 */
	public boolean isInfoEnabled ()
	{
		return true;
	}
}
