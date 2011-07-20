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

package de.iritgo.nexim.user;


import de.iritgo.nexim.user.roster.IMRosterItem;


public interface UserPermissionService
{
	/**
	 * @return
	 */
	public boolean allowNewUserRegistration ();

	/**
	 * @param username
	 * @return
	 */
	public boolean allowNewUserRegistration (String username);

	/**
	 * @return
	 */
	public boolean allowRemoveUserRegistration ();

	/**
	 * @param username
	 * @return
	 */
	public boolean allowRemoveUserRegistration (String username);

	/**
	 * @param username
	 * @param item
	 * @return
	 */
	public boolean allowAddRosterItem (String username, IMRosterItem item);

	/**
	 * @param username
	 * @param item
	 * @return
	 */
	public boolean allowRemoveRosterItem (String username, IMRosterItem item);
}
