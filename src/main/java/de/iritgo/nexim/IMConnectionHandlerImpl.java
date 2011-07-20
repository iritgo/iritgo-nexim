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

package de.iritgo.nexim;


import de.iritgo.nexim.router.IMRouter;
import de.iritgo.nexim.session.DefaultSessionProcessor;
import de.iritgo.nexim.session.IMSession;
import de.iritgo.nexim.session.SessionsManager;
import org.apache.mina.core.session.IoSession;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import java.io.StringReader;


public class IMConnectionHandlerImpl extends DefaultSessionProcessor implements IMConnectionHandler
{
	private ServerParameters serverParameters;

	private SessionsManager sessionsManager;

	private IMRouter router;

	private S2SConnectorManager s2sConnectorManager;

	public void setServerParameters (ServerParameters serverParameters)
	{
		this.serverParameters = serverParameters;
	}

	public void setSessionsManager (SessionsManager sessionsManager)
	{
		this.sessionsManager = sessionsManager;
	}

	public void setImRouter (IMRouter iMRouter)
	{
		this.router = iMRouter;
	}

	public void setS2SConnectorManager (S2SConnectorManager s2sConnectorManager)
	{
		this.s2sConnectorManager = s2sConnectorManager;
	}

	//-------------------------------------------------------------------------
	public void initialize ()
	{
		s2sConnectorManager.setConnectionHandler (this);
		router.setS2SConnectorManager (s2sConnectorManager);
	}

	public void sessionClosed (@SuppressWarnings("unused") IoSession iosession, IMSession session)
	{
		sessionsManager.release (session);
	}

	public IMSession sessionOpened (IoSession iosession, boolean clientConnectionMode)
	{
		IMSession session = null;

		try
		{
			if (clientConnectionMode)
			{
				session = sessionsManager.getNewClientSession ();
			}
			else
			{
				session = sessionsManager.getNewServerSession ();
			}

			session.setImRouter (router);

			getLogger ().debug (
							"######## [" + serverParameters.getHostName () + "] New session instance: "
											+ session.getId ());

			//TODO: Encoding
			session.setDefaultEncoding ("UTF-8");
			session.setup (iosession);
		}
		catch (Exception e)
		{
			getLogger ().error (e.getMessage (), e);
		}

		return session;
	}

	public void handleEncodingHandshake (IMSession session) throws java.io.IOException, java.net.ProtocolException
	{
		session.setNamespace ("jabber:client");

		String s = "<stream:stream xmlns:stream='http://etherx.jabber.org/streams' " + "id='" + session.getId ()
						+ "' xmlns='jabber:client' from='" + serverParameters.getHostName () + "'>";

		;

		session.writeOutputStream (s);
	}

	public void process (String xmlMessage, IMSession session)
	{
		final XmlPullParser xpp = session.getXmlPullParser ();

		try
		{
			xpp.setInput (new StringReader (xmlMessage));
			super.process (session);
		}
		catch (XmlPullParserException e)
		{
			e.printStackTrace ();
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace ();
		}
	}

	public void setup (IoSession ioSession, IMSession session)
	{
		try
		{
			session.setup (ioSession);
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace ();
		}
	}

	public void dispose ()
	{
		getLogger ().debug ("Disposing Router");
		// We must stop all sessions!
		// Hope the pull parser stops gracefully!
		router.releaseSessions ();

		// Unfortunately we may also have sessions that was never authenticated
		// and therefore is not yet part of the router sessions
		sessionsManager.releaseSessions ();
	}
}
