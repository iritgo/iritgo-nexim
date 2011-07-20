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


import java.util.List;


public interface UserDAO
{
	/**
	 * @param newUser
	 */
	public void addUser (User newUser);

	/**
	 * @param username
	 * @return
	 */
	public User getUser (String username);

	/**
	 * @param searchPattern
	 * @return
	 */
	public List<User> getUsers (String searchPattern);

	/**
	 * @param username
	 * @return
	 */
	public User removeUser (String username);

	/**
	 * @param user
	 * @param type
	 * @param password
	 * @param sessionId
	 * @return
	 */
	public boolean isValidPassword (User user, UserManager.AuthenticationType type, String password, String sessionId);
}
