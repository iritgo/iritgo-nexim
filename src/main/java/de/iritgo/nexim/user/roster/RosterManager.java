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


public interface RosterManager
{
	/**
	 * Add a item to the roster list
	 *
	 * @param username The username
	 * @param roster The roster list item
	 */
	public void addItem(String username, IMRosterItem item) throws AddRosterItemNotAllowedException;

	/**
	 * Remove a roster item
	 *
	 * @param username The user name
	 * @param itemJID The itemJID to remove
	 */
	public void removeItem(String username, IMRosterItem item) throws RemoveRosterItemNotAllowedException;

	/**
	 * Remove a roster item
	 *
	 * @param username The user name
	 * @param itemJID The itemJID to remove
	 */
	public void removeItem(String username, String itemJID) throws RemoveRosterItemNotAllowedException;

	/**
	 * Get a roster item from the given user
	 *
	 * @param username The username
	 * @param itemJID The item jid
	 * @return
	 */
	public IMRosterItem getItem(String username, String itemJID);

	/**
	 * Processor pattern to iterate over the  item list
	 *
	 * @param itemProcessor
	 */
	public void processItems(String username, RosterItemProcessor itemProcessor) throws Exception;
}
