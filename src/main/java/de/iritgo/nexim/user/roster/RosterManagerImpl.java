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


import de.iritgo.nexim.user.UserPermissionService;
import java.util.LinkedList;
import java.util.List;


public class RosterManagerImpl implements RosterManager
{
	private RosterDAO rosterDAO;

	private UserPermissionService userPermissionService;

	public void setRosterDAO(RosterDAO rosterDAO)
	{
		this.rosterDAO = rosterDAO;
	}

	/**
	 * @param userPermissionService the userPermissionService to set
	 */
	public void setUserPermissionService(UserPermissionService userPermissionService)
	{
		this.userPermissionService = userPermissionService;
	}

	public void addItem(String username, IMRosterItem rosterItem) throws AddRosterItemNotAllowedException
	{
		if (userPermissionService.allowAddRosterItem(username, rosterItem))
		{
			rosterDAO.addItem(username, rosterItem);
		}
		else
		{
			throw new AddRosterItemNotAllowedException();
		}
	}

	/**
	 * @see de.iritgo.nexim.user.roster.RosterManager#removeItem(java.lang.String, java.lang.String)
	 */
	public void removeItem(String username, IMRosterItem rosterItem) throws RemoveRosterItemNotAllowedException
	{
		if (userPermissionService.allowAddRosterItem(username, rosterItem))
		{
			rosterDAO.removeItem(username, rosterItem);
		}
		else
		{
			throw new RemoveRosterItemNotAllowedException();
		}
	}

	/**
	 * @see de.iritgo.nexim.user.roster.RosterManager#getItem(java.lang.String, java.lang.String)
	 */
	public IMRosterItem getItem(String username, String itemJID)
	{
		return rosterDAO.getItem(username, itemJID);
	}

	/**
	 * @see de.iritgo.nexim.user.roster.RosterManager#removeItem(java.lang.String, java.lang.String)
	 */
	public void removeItem(String username, String itemJID) throws RemoveRosterItemNotAllowedException
	{
		List<IMRosterItem> items = rosterDAO.getRosterItems(username);

		synchronized (items)
		{
			for (IMRosterItem item : items)
			{
				if (item.getJID().equals(itemJID))
				{
					removeItem(username, item);

					return;
				}
			}
		}
	}

	/**
	 * @throws Exception
	 * @see de.iritgo.nexim.user.roster.RosterManager#processItem(de.iritgo.nexim.user.roster.RosterItemProcessor)
	 */
	public void processItems(String username, RosterItemProcessor itemProcessor) throws Exception
	{
		List<IMRosterItem> itemsTmp = rosterDAO.getRosterItems(username);
		List<IMRosterItem> items = null;

		synchronized (itemsTmp)
		{
			items = new LinkedList<IMRosterItem>(itemsTmp);
		}

		for (IMRosterItem item : items)
		{
			itemProcessor.process(item);
		}
	}
}
