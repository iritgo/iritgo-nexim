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


import de.iritgo.nexim.log.DefaultNeximLogger;
import de.iritgo.nexim.router.IMRouter;
import de.iritgo.nexim.session.IMServerSession;
import de.iritgo.nexim.session.SessionsManager;
import java.util.HashMap;
import java.util.Map;


public class S2SConnectorManagerImpl implements S2SConnectorManager
{
	private Map<String, S2SConnector> hostnameAndS2SMap;

	private IMConnectionHandler connectionHandler;

	private S2SConnectorFactory s2sConnectorFactory;

	// Requirements
	private IMRouter router;

	private SessionsManager sessionsManager;

	/** The default nexim logger interface         */
	private DefaultNeximLogger defaultNeximLogger;

	/** Set the default nexim logger implementation         */
	public void setDefaultNeximLogger(DefaultNeximLogger defaultNeximLogger)
	{
		this.defaultNeximLogger = defaultNeximLogger;
	}

	public void setImRouter(IMRouter router)
	{
		this.router = router;
	}

	public void setSessionsManager(SessionsManager sessionsManager)
	{
		this.sessionsManager = sessionsManager;
	}

	public void setS2sConnectorFactory(S2SConnectorFactory connectorFactory)
	{
		s2sConnectorFactory = connectorFactory;
	}

	//-------------------------------------------------------------------------
	public void initialize()
	{
		hostnameAndS2SMap = new HashMap<String, S2SConnector>();
	}

	//-------------------------------------------------------------------------
	public void setConnectionHandler(IMConnectionHandler connectionHandler)
	{
		this.connectionHandler = connectionHandler;
	}

	//-------------------------------------------------------------------------
	public IMServerSession getCurrentRemoteSession(String hostname) throws Exception
	{
		IMServerSession session = null;

		synchronized (hostnameAndS2SMap)
		{
			S2SConnector s2s = hostnameAndS2SMap.get(hostname);

			if (s2s != null && ! s2s.getSession().isClosed())
			{
				session = s2s.getSession();
			}
		}

		return session;
	}

	//----------------------------------------------------------------------
	public IMServerSession getRemoteSessionWaitForValidation(String hostname, long timeout) throws Exception
	{
		IMServerSession session = null;
		S2SConnector s2s = null;

		synchronized (hostnameAndS2SMap)
		{
			s2s = (S2SConnector) hostnameAndS2SMap.get(hostname);

			if (s2s != null && ! s2s.getSession().isClosed())
			{
				session = s2s.getSession();
			}
			else
			{
				s2s = getS2SConnector(hostname);
				session = s2s.getSession();
			}
		}

		synchronized (session)
		{
			// wait for validation
			if (! session.getDialbackValid())
			{
				s2s.sendResult();
				defaultNeximLogger.info("Wait validation for " + hostname + " for session " + session);
				session.wait(timeout);
			}
		}

		if (! session.getDialbackValid())
		{
			throw new Exception("Unable to get dialback validation for " + hostname + " after timeout " + timeout
							+ " ms");
		}

		defaultNeximLogger.info("Validation granted from " + hostname + " for session " + session);

		return session;
	} // getremote session

	//-------------------------------------------------------------------------
	public void verifyRemoteHost(String hostname, String dialbackValue, String id, IMServerSession session)
		throws Exception
	{
		S2SConnector s2s = getS2SConnector(hostname);

		s2s.sendVerify(dialbackValue, id);

		if (! s2s.getSession().getDialbackValid())
		{
			s2s.sendResult();
		}

		session.setTwinSession(s2s.getSession());
		s2s.getSession().setTwinSession(session);
	}

	//-------------------------------------------------------------------------
	private S2SConnector getS2SConnector(String hostname) throws Exception
	{
		S2SConnector s2s = null;

		synchronized (hostnameAndS2SMap)
		{
			s2s = hostnameAndS2SMap.get(hostname);

			if (s2s != null && ! s2s.isAlive())
			{
				defaultNeximLogger.info("Removing s2s for hostname (thread not alive) " + hostname);
				hostnameAndS2SMap.remove(hostname);
				s2s = null;
			}

			if (s2s == null || s2s.getSession().isClosed())
			{
				s2s = s2sConnectorFactory.createS2SConnector();
				s2s.setIMConnectionHandler(connectionHandler);
				s2s.setRouter(router);
				s2s.setSessionsManager(sessionsManager);
				s2s.setToHostname(hostname);
				new Thread(s2s).start();
				hostnameAndS2SMap.put(hostname, s2s);
			}
		}

		return s2s;
	}
}
