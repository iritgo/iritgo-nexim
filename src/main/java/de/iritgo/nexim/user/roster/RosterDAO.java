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


import java.util.List;


public interface RosterDAO
{
	/**
	 * Retrieve the roster list for the given username
	 *
	 * @param username The user name
	 * @return The roster list
	 */
	public List<IMRosterItem> getRosterItems (String username);

	/**
	 * Set a new roster list for the given user name
	 *
	 * @param username The username
	 * @param roster The roster list
	 */
	public void addItem (String username, IMRosterItem item);

	/**
	 * Remove a item from the list
	 *
	 * @param username
	 * @param itemJID
	 */
	public void removeItem (String username, IMRosterItem item);

	/**
	 * Get roster item from a given username
	 *
	 * @param username The username
	 * @param itemJID The item jid
	 * @return The item
	 */
	public IMRosterItem getItem (String username, String itemJID);
}
