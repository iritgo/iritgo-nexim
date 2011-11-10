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

package de.iritgo.nexim.user.deferrable;


import de.iritgo.nexim.log.DefaultNeximLogger;
import de.iritgo.nexim.tools.XStreamStore;
import java.io.File;
import java.util.List;


public class DeferrableMessageDAOImpl implements DeferrableMessageDAO
{
	// Locals
	private XStreamStore repository;

	// Configuration
	private String filename;

	private String encoding;

	/** The default nexim logger interface         */
	private DefaultNeximLogger defaultNeximLogger;

	/** Set the default nexim logger implementation         */
	public void setDefaultNeximLogger(DefaultNeximLogger defaultNeximLogger)
	{
		this.defaultNeximLogger = defaultNeximLogger;
	}

	public void setFilename(String filename)
	{
		this.filename = filename;
	}

	public void setEncoding(String encoding)
	{
		this.encoding = encoding;
	}

	public void initialize()
	{
		File storeFile = new File(filename);

		if (! storeFile.exists())
		{
			storeFile.getParentFile().mkdirs();
		}

		//TODO: XStream as Service!!!
		repository = new XStreamStore(storeFile, defaultNeximLogger, encoding);
		repository.substitute("de.iritgo.nexim.xmpp.IMMessage", "message");
		repository.load();
	}

	//--------------------------------------------------------------------------
	public List getDeferrableList(String username)
	{
		if (repository == null)
		{
			try
			{
				initialize();
			}
			catch (Exception x)
			{
			}
		}

		List list = null;

		try
		{
			list = (List) repository.get(username);
		}
		catch (Exception e)
		{
			System.out.println("User " + username + " message list not found");
		}

		return list;
	}

	//--------------------------------------------------------------------------
	public void setDeferrableList(String username, List deferrableList)
	{
		if (repository == null)
		{
			try
			{
				initialize();
			}
			catch (Exception x)
			{
			}
		}

		if (username != null && deferrableList != null)
		{
			repository.put(username, deferrableList);
		}
	}
}
