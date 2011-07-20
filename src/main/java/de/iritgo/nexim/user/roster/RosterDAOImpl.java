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

package de.iritgo.nexim.user.roster;


import de.iritgo.nexim.log.DefaultNeximLogger;
import de.iritgo.nexim.tools.XStreamStore;
import java.io.File;
import java.util.LinkedList;
import java.util.List;


public class RosterDAOImpl implements RosterDAO
{
	// Locals
	private XStreamStore repository;

	// Configuration
	private String filename = "users.txt";

	private String encoding;

	/** The default nexim logger interface */
	private DefaultNeximLogger defaultNeximLogger;

	/** Set the default nexim logger implementation */
	public void setDefaultNeximLogger (DefaultNeximLogger defaultNeximLogger)
	{
		this.defaultNeximLogger = defaultNeximLogger;
	}

	public void setFilename (String filename)
	{
		this.filename = filename;
	}

	public void setEncoding (String encoding)
	{
		this.encoding = encoding;
	}

	// -------------------------------------------------------------------------
	public void initialize ()
	{
		File storeFile = new File (filename);

		if (! storeFile.exists ())
		{
			storeFile.getParentFile ().mkdirs ();
		}

		// TODO: StreamStore as service
		repository = new XStreamStore (storeFile, defaultNeximLogger, encoding);
		repository.substitute ("de.iritgo.nexim.user.roster.IMRosterItem", "item");
		// repository.load ();
	}

	// --------------------------------------------------------------------------
	public List<IMRosterItem> getRosterItems (String username)
	{
		if (repository == null)
		{
			try
			{
				initialize ();
			}
			catch (Exception x)
			{
			}
		}

		List list = null;

		try
		{
			list = (List) repository.get (username);

			if (list == null)
			{
				list = new LinkedList<IMRosterItem> ();
				setRosterList (username, list);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace ();

			list = new LinkedList ();
			setRosterList (username, list);
		}

		synchronized (list)
		{
			return new LinkedList<IMRosterItem> (list);
		}
	}

	/**
	 * @see de.iritgo.nexim.user.roster.RosterDAO#addRosterItem(java.lang.String,
	 *      de.iritgo.nexim.user.roster.IMRosterItem)
	 */
	public void addItem (String username, IMRosterItem rosterItem)
	{
		if (repository == null)
		{
			try
			{
				initialize ();
			}
			catch (Exception x)
			{
			}
		}

		if (username != null)
		{
			List<IMRosterItem> rosterList = (List<IMRosterItem>) repository.get (username);

			synchronized (rosterList)
			{

				if (rosterList == null)
				{
					rosterList = new LinkedList<IMRosterItem> ();
					repository.put (username, rosterList);
				}

				rosterList.add (rosterItem);
			}
			// repository.save ();
		}
	}

	public void setRosterList (String username, List<IMRosterItem> rosterList)
	{
		if (repository == null)
		{
			try
			{
				initialize ();
			}
			catch (Exception x)
			{
			}
		}

		if (username != null && rosterList != null)
		{
			repository.put (username, rosterList);
		}
	}

	/**
	 * @see de.iritgo.nexim.user.roster.RosterDAO#removeItem(java.lang.String,
	 *      java.lang.String)
	 */
	public void removeItem (String username, IMRosterItem item)
	{
		defaultNeximLogger.debug ("Removing roster item " + item.getJID ());

		List<IMRosterItem> rosterList = (List<IMRosterItem>) repository.get (username);
		synchronized (rosterList)
		{
			rosterList.remove (item);
		}
		// repository.save ();
	}

	/**
	 * @see de.iritgo.nexim.user.roster.RosterDAO#getItem(java.lang.String,
	 *      java.lang.String)
	 */
	public IMRosterItem getItem (String username, String itemJID)
	{
		List<IMRosterItem> items = getRosterItems (username);

		synchronized (items)
		{
			for (IMRosterItem item : items)
			{
				if (item.getJID ().equals (itemJID))
				{
					return item;
				}
			}
		}

		return null;
	}
}
