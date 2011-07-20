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
import org.xmlpull.v1.XmlPullParser;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;


public class S2SConnectorImpl implements S2SConnector, Runnable
{
	/** The default nexim logger interface         */
	private DefaultNeximLogger defaultNeximLogger;

	// Requirements
	private ServerParameters serverParameters;

	// Configuration
	private int deliveryConnectionTimeout;

	// Locals
	private SessionsManager sessionsManager;

	private IMConnectionHandler connectionHandler;

	private String toHostName;

	private IMServerSession session;

	private IMRouter router;

	private volatile boolean isAlive = false;

	private volatile boolean ready = false;

	private volatile boolean sendResult = false;

	private volatile boolean sendVerify = false;

	private volatile String verifyDialbackValue;

	private volatile String verifyId;

	/** Set the default nexim logger implementation         */
	public void setDefaultNeximLogger (DefaultNeximLogger defaultNeximLogger)
	{
		this.defaultNeximLogger = defaultNeximLogger;
	}

	public void setServerParameters (ServerParameters serverParameters)
	{
		this.serverParameters = serverParameters;
	}

	public void setDeliveryConnectionTimeout (int deliveryConnectionTimeout)
	{
		this.deliveryConnectionTimeout = deliveryConnectionTimeout;
	}

	//-------------------------------------------------------------------------
	public void setToHostname (String toHostname)
	{
		this.toHostName = toHostname;
	}

	//-------------------------------------------------------------------------
	public void setRouter (IMRouter router)
	{
		this.router = router;
	}

	//-------------------------------------------------------------------------
	public void setIMConnectionHandler (IMConnectionHandler connectionHandler)
	{
		this.connectionHandler = connectionHandler;
	}

	//-------------------------------------------------------------------------
	public void setSessionsManager (SessionsManager sessionsManager)
	{
		this.sessionsManager = sessionsManager;
	}

	//----------------------------------------------------------------------
	public IMServerSession getSession () throws Exception
	{
		if (session == null)
		{
			session = sessionsManager.getNewServerSession ();
			session.setImRouter (router);
			session.setRemoteHostname (toHostName);
		}

		return session;
	}

	//----------------------------------------------------------------------
	public boolean isAlive ()
	{
		return isAlive;
	}

	//----------------------------------------------------------------------
	public void run ()
	{
		isAlive = true;

		try
		{
			//Socket socket = new Socket( toHostname, m_serverParameters.getRemoteServerPort() );
			Socket socket = new Socket ();
			InetSocketAddress insa = new InetSocketAddress (toHostName, serverParameters.getRemoteServerPort ());

			defaultNeximLogger.debug ("Trying to connect (timeout " + deliveryConnectionTimeout + " ms) to "
							+ toHostName + ":" + serverParameters.getRemoteServerPort ());
			socket.connect (insa, deliveryConnectionTimeout);
			defaultNeximLogger.info ("Connection to " + toHostName + ":" + serverParameters.getRemoteServerPort ()
							+ " successfull");

			//socket.setKeepAlive( true );
			IMServerSession session = getSession ();

			//TODO:!!
			//			session.setup (socket);
			final XmlPullParser xpp = session.getXmlPullParser ();

			int eventType = xpp.getEventType ();

			while (eventType != XmlPullParser.START_DOCUMENT)
			{
				eventType = xpp.getEventType ();
			}

			// initial connection string
			String s = "<?xml version='1.0' encoding='" + session.getEncoding () + "' ?>";

			s += "<stream:stream xmlns:stream='http://etherx.jabber.org/streams' " + "xmlns='jabber:server' " + "to='"
							+ toHostName + "' " + "from='" + serverParameters.getHostName () + "' " + "id='"
							+ session.getId () + "' " + "xmlns:db='jabber:server:dialback'>";

			session.writeOutputStream (s);

			ready = true;

			if (sendVerify)
			{
				sendVerify (verifyDialbackValue, verifyId);
			}

			if (sendResult)
			{
				sendResult ();
			}

			connectionHandler.process (session);
		}
		catch (Exception e)
		{
			defaultNeximLogger.error ("L2R " + toHostName + " session exception: " + e.getMessage (), e);
		}
		finally
		{
			isAlive = false;

			//TODO:Was dat?
			if (session == null)
			{
				return;
			}

			if (! session.isClosed ())
			{
				defaultNeximLogger.info ("Release session " + session.getId ());
				sessionsManager.release (session);
			}

			// unlock all thread
			synchronized (session)
			{
				session.notifyAll ();
			}
		}
	}

	//----------------------------------------------------------------------
	public void sendResult () throws IOException
	{
		if (! ready)
		{
			sendResult = true;
		}
		else
		{
			if (session.getDialbackValue () == null)
			{
				String dialbackValue = Long.toString (session.getId ());

				session.setDialbackValue (dialbackValue);

				String s = "<db:result from='" + serverParameters.getHostName () + "' to='" + toHostName + "'>";

				s += dialbackValue;
				s += "</db:result>";
				defaultNeximLogger.info ("Started dialback validation for host " + toHostName + " id "
								+ session.getId ());
				session.writeOutputStream (s);
			}
		}
	}

	//----------------------------------------------------------------------
	public void sendVerify (String dialbackValue, String id) throws IOException
	{
		if (! ready)
		{
			sendVerify = true;
			verifyDialbackValue = dialbackValue;
			verifyId = id;
		}
		else
		{
			String s = "<db:verify from='" + serverParameters.getHostName () + "' to='" + toHostName + "' id='" + id
							+ "'>";

			s += dialbackValue;
			s += "</db:verify>";
			session.writeOutputStream (s);
		}
	}
} // class
