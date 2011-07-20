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


public class UserPermissionServiceImpl implements UserPermissionService
{
	/**
	 * @see de.iritgo.nexim.user.UserPermissionService#allowNewUserRegistration()
	 */
	public boolean allowNewUserRegistration ()
	{
		return true;
	}

	/**
	 * @see de.iritgo.nexim.user.UserPermissionService#allowNewUserRegistration(java.lang.String)
	 */
	public boolean allowNewUserRegistration (String username)
	{
		return true;
	}

	/**
	 * @see de.iritgo.nexim.user.UserPermissionService#allowRemoveUserRegistration()
	 */
	public boolean allowRemoveUserRegistration ()
	{
		return true;
	}

	/**
	 * @see de.iritgo.nexim.user.UserPermissionService#allowRemoveUserRegistration(java.lang.String)
	 */
	public boolean allowRemoveUserRegistration (String username)
	{
		return true;
	}

	/**
	 * @see de.iritgo.nexim.user.UserPermissionService#allowAddRosterItem(java.lang.String, de.iritgo.nexim.user.roster.IMRosterItem)
	 */
	public boolean allowAddRosterItem (String username, IMRosterItem item)
	{
		return true;
	}

	/**
	 * @see de.iritgo.nexim.user.UserPermissionService#allowRemoveRosterItem(java.lang.String, de.iritgo.nexim.user.roster.IMRosterItem)
	 */
	public boolean allowRemoveRosterItem (String username, IMRosterItem item)
	{
		return true;
	}
}
