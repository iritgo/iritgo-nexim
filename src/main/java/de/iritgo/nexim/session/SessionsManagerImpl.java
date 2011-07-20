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

package de.iritgo.nexim.session;


import de.iritgo.nexim.log.DefaultNeximLogger;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public class SessionsManagerImpl implements SessionsManager
{
	// We really need this to be able to also shutdown non registered sessions
	private Map<Long, IMSession> activeSessions;

	private SessionFactory sessionFactory;

	/** The default nexim logger interface         */
	private DefaultNeximLogger defaultNeximLogger;

	/** Set the default nexim logger implementation         */
	public void setDefaultNeximLogger (DefaultNeximLogger defaultNeximLogger)
	{
		this.defaultNeximLogger = defaultNeximLogger;
	}

	public void setSessionFactory (SessionFactory sessionFactory)
	{
		this.sessionFactory = sessionFactory;
	}

	//-------------------------------------------------------------------------
	public IMServerSession getNewServerSession () throws Exception
	{
		IMServerSession session = sessionFactory.createServerSession ();

		// Are server session even unregistered?
		return session;
	}

	//-------------------------------------------------------------------------
	public IMClientSession getNewClientSession () throws Exception
	{
		IMClientSession session = sessionFactory.createClientSession ();

		synchronized (activeSessions)
		{
			activeSessions.put (new Long (session.getId ()), session);
		}

		return session;
	}

	//-------------------------------------------------------------------------
	public void initialize ()
	{
		activeSessions = new HashMap<Long, IMSession> ();
	}

	//-------------------------------------------------------------------------
	public void release (IMSession session)
	{
		if (session != null)
		{
			try
			{
				if (! session.isClosed ())
				{
					session.close ();
				}
				else
				{
					defaultNeximLogger.warn ("Session " + session.getId () + " already diposed");
				}
			}
			catch (Exception e)
			{
				defaultNeximLogger.warn ("Session " + session.getId () + " release failure " + e.getMessage (), e);
			}

			// Remove from sessionsMap
			synchronized (activeSessions)
			{
				activeSessions.remove (new Long (session.getId ()));
			}
		} // if
	}

	//-------------------------------------------------------------------------
	public void releaseSessions ()
	{
		defaultNeximLogger.debug ("Releasing sessions ");

		// Avoid concurrent mod
		Map<Long, IMSession> clonedSessions = new HashMap<Long, IMSession> (activeSessions);
		Iterator it = clonedSessions.values ().iterator ();

		while (it.hasNext ())
		{
			IMSession sess = (IMSession) it.next ();

			release (sess);
		} // end of while ()
	}
}
