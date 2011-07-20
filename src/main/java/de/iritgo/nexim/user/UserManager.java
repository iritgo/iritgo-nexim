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


public interface UserManager
{
	/** Authentication types */
	public static enum AuthenticationType
	{
		PLAIN, DIGEST;
	}

	/**
	 * Check the support of a given authentication type.
	 *
	 * @param type The type
	 * @return True if supported
	 */
	public boolean isAuthenticationTypeSupported (AuthenticationType type);

	/**
	 * Throws a exception if the user has provide a wrong password or digest
	 *
	 * @param user The user pojo
	 * @param type The authentication type
	 * @param value The password or the digest string
	 * @param sessionId The session id
	 * @throws Exception
	 */
	public void authenticate (User user, AuthenticationType type, String value, String sessionId) throws Exception;

	/**
	 * Register a new user by the xmpp jid string
	 *
	 * @param xmppAddUserString The jid
	 */
	public void addUser (String jid) throws RegistrationNotAllowedException;

	/**
	 * Register a new user
	 *
	 * @param user The user
	 */
	public void addUser (User user) throws RegistrationNotAllowedException;

	/**
	 * Register a remove user by the xmpp jid string
	 *
	 * @param xmppAddUserString The jid
	 */
	public void removeUser (String jid) throws UnRegistrationNotAllowedException;

	/**
	 * Remove a user
	 *
	 * @param user The user
	 */
	public void removeUser (User user) throws UnRegistrationNotAllowedException;

	/**
	 * Create a new user obect.
	 *
	 * @return The new user
	 * @throws Exception
	 */
	public User createNewUser () throws Exception;
}
