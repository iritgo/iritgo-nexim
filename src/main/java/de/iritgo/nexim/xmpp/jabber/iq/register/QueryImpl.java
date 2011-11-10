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

package de.iritgo.nexim.xmpp.jabber.iq.register;


import de.iritgo.nexim.ServerParameters;
import de.iritgo.nexim.router.IMRouter;
import de.iritgo.nexim.session.DefaultSessionProcessor;
import de.iritgo.nexim.session.IMClientSession;
import de.iritgo.nexim.session.IMSession;
import de.iritgo.nexim.user.RegistrationNotAllowedException;
import de.iritgo.nexim.user.UnRegistrationNotAllowedException;
import de.iritgo.nexim.user.User;
import de.iritgo.nexim.user.UserDAO;
import de.iritgo.nexim.user.UserManager;
import de.iritgo.nexim.xmpp.IMIq;
import java.util.HashMap;
import java.util.Map;


public class QueryImpl extends DefaultSessionProcessor implements Query
{
	private ServerParameters serverParameters;

	private UserManager userManager;

	private UserDAO userDAO;

	public void setServerParameters(ServerParameters serverParameters)
	{
		this.serverParameters = serverParameters;
	}

	public void setUserManager(UserManager userManager)
	{
		this.userManager = userManager;
	}

	public void setUserDAO(UserDAO userDAO)
	{
		this.userDAO = userDAO;
	}

	//-------------------------------------------------------------------------
	@Override
	public void process(final IMSession session, final Object context) throws Exception
	{
		IMClientSession clientSession = (IMClientSession) session;

		User currentUser = clientSession.getUser();
		User user = userManager.createNewUser();

		clientSession.setUser(user);

		Map<Integer, Boolean> contextMap = new HashMap<Integer, Boolean>();

		contextMap.put(CTX_SHOULD_REMOVE, Boolean.FALSE);
		super.process(session, contextMap);

		String iqId = ((IMIq) context).getId();
		String type = ((IMIq) context).getType();

		// GET
		if (IMIq.TYPE_GET.equals(type))
		{
			String s = "<iq type='"
							+ IMIq.TYPE_RESULT
							+ "' id='"
							+ iqId
							+ "' from='"
							+ serverParameters.getHostName()
							+ "'>"
							+ "<query xmlns='jabber:iq:register'>"
							+ "<instructions>Choose a username and password to register with this service.</instructions>"
							+ "<password/><username/>" + "</query></iq>";

			session.writeOutputStream(s);
		}

		// SET
		else if (IMIq.TYPE_SET.equals(type))
		{
			Boolean shouldRemove = (Boolean) contextMap.get(CTX_SHOULD_REMOVE);

			if (shouldRemove.booleanValue())
			{
				try
				{
					userManager.removeUser(currentUser.getName());

					String s = "<iq type='" + IMIq.TYPE_RESULT + "' id='" + iqId + "' />";

					session.writeOutputStream(s);
					clientSession.setUser(null);
				}
				catch (UnRegistrationNotAllowedException x)
				{
					String s = "<iq type='" + IMIq.TYPE_ERROR + "' id='" + iqId + "' />";

					session.writeOutputStream(s);
				}
			}

			else
			{ // no remove

				User existingUser = userDAO.getUser(user.getName());

				if (existingUser == null)
				{
					try
					{
						userManager.addUser(user);

						IMRouter router = session.getRouter();

						router.registerSession(clientSession);

						String s = "<iq type='" + IMIq.TYPE_RESULT + "' id='" + iqId + "' />";

						session.writeOutputStream(s);
					}
					catch (RegistrationNotAllowedException x)
					{
						String s = "<iq type='" + IMIq.TYPE_ERROR + "' id='" + iqId + "' />";

						session.writeOutputStream(s);
					}
				}
				else if (currentUser != null)
				{ // account already exists and we are logged

					try
					{
						String s = null;

						if (currentUser.getName() != null && currentUser.getName().equals(user.getName()))
						{
							userManager.addUser(user);
							s = "<iq type='" + IMIq.TYPE_RESULT + "' id='" + iqId + "' />";
						}
						else
						{
							clientSession.setUser(currentUser);
							s = "<iq type='" + IMIq.TYPE_ERROR + "' id='" + iqId + "' />";
						}

						session.writeOutputStream(s);
					}
					catch (RegistrationNotAllowedException x)
					{
						String s = "<iq type='" + IMIq.TYPE_ERROR + "' id='" + iqId + "' />";

						session.writeOutputStream(s);
					}
				}

				else
				{ // abnormal sitatuation sending error

					String s = "<iq type='" + IMIq.TYPE_ERROR + "' id='" + iqId + "' />";

					session.writeOutputStream(s);
				}
			} // else shouldremove
		}
	}
}
