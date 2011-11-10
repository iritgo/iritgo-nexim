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


import de.iritgo.nexim.Configuration;
import de.iritgo.nexim.log.DefaultNeximLogger;
import de.iritgo.nexim.user.roster.RosterManager;


public class UserManagerImpl implements UserManager
{
	/** */
	@SuppressWarnings("unused")
	private RosterManager rosterManager;

	/** */
	private UserDAO userDAO;

	/** */
	@SuppressWarnings("unused")
	private DefaultNeximLogger logger;

	/** */
	private UserPermissionService userPermissionService;

	/** */
	private Configuration configuration;

	/**
	 * @param rosterManager
	 */
	public void setRosterManager(RosterManager rosterManager)
	{
		this.rosterManager = rosterManager;
	}

	/**
	 * @param userDAO
	 */
	public void setUserDAO(UserDAO userDAO)
	{
		this.userDAO = userDAO;
	}

	/**
	 * @param logger
	 */
	public void setDefaultNeximLogger(DefaultNeximLogger logger)
	{
		this.logger = logger;
	}

	/**
	 * @param userPermissionService
	 */
	public void setUserPermissionService(UserPermissionService userPermissionService)
	{
		this.userPermissionService = userPermissionService;
	}

	/**
	 * @param configuration The new configuration.
	 */
	public void setConfiguration(Configuration configuration)
	{
		this.configuration = configuration;
	}

	/**
	 * @see de.iritgo.nexim.user.UserManager#isAuthenticationTypeSupported(de.iritgo.nexim.user.UserManager.AuthenticationType)
	 */
	public boolean isAuthenticationTypeSupported(UserManager.AuthenticationType type)
	{
		for (String configuredType : configuration.getSupportedAuthenticationTypes())
		{
			if (type.equals(UserManager.AuthenticationType.valueOf(configuredType)))
			{
				return true;
			}
		}

		return false;
	}

	/**
	 * @see de.iritgo.nexim.user.UserManager#authenticate(de.iritgo.nexim.user.User, de.iritgo.nexim.user.UserManager.AuthenticationType, java.lang.String, java.lang.String)
	 */
	public final void authenticate(User user, UserManager.AuthenticationType type, String value, String sessionId)
		throws Exception
	{
		if (! userDAO.isValidPassword(user, type, value, sessionId))
		{
			throw new Exception("Invalid password");
		}
	}

	/**
	 * @see de.iritgo.nexim.user.UserManager#createNewUser()
	 */
	public User createNewUser() throws Exception
	{
		return new User();
	}

	public void addUser(String jid) throws RegistrationNotAllowedException
	{
		User user = getDummyUserFormJID(jid);

		addUser(user);
	}

	/**
	 * @see de.iritgo.nexim.user.UserManager#addUser(de.iritgo.nexim.user.User)
	 */
	public void addUser(User user) throws RegistrationNotAllowedException
	{
		if (userPermissionService.allowNewUserRegistration()
						&& userPermissionService.allowNewUserRegistration(user.getName()))
		{
			userDAO.addUser(user);
		}
		else
		{
			throw new RegistrationNotAllowedException();
		}
	}

	/**
	 * @see de.iritgo.nexim.user.UserManager#removeUser(java.lang.String)
	 */
	public void removeUser(String jid) throws UnRegistrationNotAllowedException
	{
		User user = getDummyUserFormJID(jid);

		removeUser(user);
	}

	/**
	 * @see de.iritgo.nexim.user.UserManager#removeUser(de.iritgo.nexim.user.User)
	 */
	public void removeUser(User user) throws UnRegistrationNotAllowedException
	{
		if (userPermissionService.allowRemoveUserRegistration()
						&& userPermissionService.allowRemoveUserRegistration(user.getName()))
		{
			userDAO.removeUser(user.getName());
		}
		else
		{
			throw new UnRegistrationNotAllowedException();
		}
	}

	/**
	 * @param jidName
	 * @return
	 */
	private User getDummyUserFormJID(String jidName)
	{
		User user = new User();
		int index = jidName.indexOf('/');

		if (index < 0)
		{
			user.setName(jidName);
			user.setPassword(jidName);
		}
		else
		{
			user.setName(jidName.substring(0, index));
			user.setPassword(jidName.substring(index + 1));
		}

		return user;
	}
}
