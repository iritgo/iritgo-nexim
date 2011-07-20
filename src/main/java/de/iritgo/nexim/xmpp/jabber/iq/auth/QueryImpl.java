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

package de.iritgo.nexim.xmpp.jabber.iq.auth;


import de.iritgo.nexim.ServerParameters;
import de.iritgo.nexim.router.IMRouter;
import de.iritgo.nexim.session.DefaultSessionProcessor;
import de.iritgo.nexim.session.IMClientSession;
import de.iritgo.nexim.session.IMSession;
import de.iritgo.nexim.user.User;
import de.iritgo.nexim.user.UserDAO;
import de.iritgo.nexim.user.UserManager;
import de.iritgo.nexim.xmpp.IMIq;


public class QueryImpl extends DefaultSessionProcessor implements Query
{
	private ServerParameters serverParameters;

	private UserManager userManager;

	private UserDAO userDAO;

	public void setServerParameters (ServerParameters serverParameters)
	{
		this.serverParameters = serverParameters;
	}

	public void setUserManager (UserManager userManager)
	{
		this.userManager = userManager;
	}

	public void setUserDAO (UserDAO userDAO)
	{
		this.userDAO = userDAO;
	}

	//-------------------------------------------------------------------------
	@Override
	public void process (final IMSession session, final Object context) throws Exception
	{
		IMClientSession clientSession = (IMClientSession) session;

		String iqId = ((IMIq) context).getId ();
		String type = ((IMIq) context).getType ();

		User tmpAuthUser = userManager.createNewUser ();

		clientSession.setUser (tmpAuthUser);
		tmpAuthUser.setHostname (serverParameters.getHostName ());

		// GET
		if (IMIq.TYPE_GET.equals (type))
		{
			super.process (session, context);

			String s = null;

			User daoUser = userDAO.getUser (tmpAuthUser.getName ());

			if (daoUser == null)
			{ // user does not exists
				s = "<iq type='" + IMIq.TYPE_ERROR + "' id='" + iqId + "'>"
								+ "<query xmlns='jabber:iq:auth'><username>" + tmpAuthUser.getName ()
								+ "</username></query>" + "<error code='401'>Unauthorized</error>" + "</iq>";
			}
			else
			{ // user exists
				s = "<iq type='" + IMIq.TYPE_RESULT + "' id='" + iqId + "' from='" + serverParameters.getHostName ()
								+ "'>" + "<query xmlns='jabber:iq:auth'>" + "<username>" + tmpAuthUser.getName ()
								+ "</username>";

				if (userManager.isAuthenticationTypeSupported (UserManager.AuthenticationType.PLAIN))
				{
					s += "<password/>";
				}

				if (userManager.isAuthenticationTypeSupported (UserManager.AuthenticationType.DIGEST))
				{
					s += "<digest/>";
				}

				s += "<resource/></query></iq>";
			}

			session.writeOutputStream (s);
		}

		// SET
		else if (IMIq.TYPE_SET.equals (type))
		{
			super.process (session, context);

			User daoUser = userDAO.getUser (tmpAuthUser.getName ());

			try
			{
				if (tmpAuthUser.getPassword () != null)
				{
					userManager.authenticate (daoUser, UserManager.AuthenticationType.PLAIN,
									tmpAuthUser.getPassword (), Long.toString (session.getId ()));
				}
				else
				{
					userManager.authenticate (daoUser, UserManager.AuthenticationType.DIGEST, tmpAuthUser.getDigest (),
									Long.toString (session.getId ()));
				}

				IMRouter router = session.getRouter ();

				router.registerSession (clientSession);

				String s = "<iq type='" + IMIq.TYPE_RESULT + "' id='" + iqId + "' />";

				session.writeOutputStream (s);
			}
			catch (Exception e)
			{
				getLogger ().debug (e.getMessage (), e);

				String s = "<iq type='" + IMIq.TYPE_ERROR + "' id='" + iqId + "' />";

				session.writeOutputStream (s);
			}
		}
	}
}
