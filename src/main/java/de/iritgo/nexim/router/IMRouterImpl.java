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

package de.iritgo.nexim.router;


import de.iritgo.nexim.S2SConnectorManager;
import de.iritgo.nexim.ServerParameters;
import de.iritgo.nexim.log.DefaultNeximLogger;
import de.iritgo.nexim.log.MessageLogger;
import de.iritgo.nexim.log.MessageRecorder;
import de.iritgo.nexim.session.IMClientSession;
import de.iritgo.nexim.session.IMSession;
import de.iritgo.nexim.session.SessionsManager;
import de.iritgo.nexim.tools.JIDParser;
import de.iritgo.nexim.user.User;
import de.iritgo.nexim.user.UserDAO;
import de.iritgo.nexim.user.deferrable.Deferrable;
import de.iritgo.nexim.user.deferrable.DeferrableMessageManager;
import de.iritgo.nexim.xmpp.Transitable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;


public class IMRouterImpl implements IMRouter
{
	public class TransitableAndSession
	{
		private Transitable m_transitable;

		private IMSession m_session;

		public TransitableAndSession(Transitable transitable, IMSession session)
		{
			m_transitable = transitable;
			m_session = session;
		}

		public Transitable getTransitable()
		{
			return m_transitable;
		}

		public IMSession getSession()
		{
			return m_session;
		}

		public String getHostname()
		{
			return JIDParser.getHostname(m_transitable.getTo());
		}
	}

	public class RemoteDeliveryThreadPerHost extends Thread
	{
		private LinkedBlockingQueue<TransitableAndSession> perHostRemoteDeliveryQueue;

		private IMSession remoteSession = null;

		private String hostname;

		private String currentStatus;

		public RemoteDeliveryThreadPerHost(String hostname)
		{
			this.hostname = hostname;
			perHostRemoteDeliveryQueue = new LinkedBlockingQueue<TransitableAndSession>();
			currentStatus = "";
		}

		public void enqueue(TransitableAndSession tas)
		{
			defaultNeximLogger.debug("Adding tas for " + hostname + " this thread (" + this + ") isAlive: " + isAlive()
							+ " current status: " + currentStatus);
			perHostRemoteDeliveryQueue.add(tas);
		}

		@Override
		public void run()
		{
			currentStatus = "Started";
			defaultNeximLogger.debug("Starting thread " + this);

			while (true)
			{
				TransitableAndSession tas = null;

				try
				{
					tas = perHostRemoteDeliveryQueue.poll(120, TimeUnit.SECONDS);
				}
				catch (InterruptedException e)
				{
					defaultNeximLogger.debug(e.getMessage(), e);
				}

				defaultNeximLogger.debug("Remove tas for " + hostname);

				if (tas != null)
				{
					deliver(tas);
					defaultNeximLogger.debug("Delivered tas for " + hostname);
				}
				else
				{
					synchronized (remoteDeliveryThreadMap)
					{
						if (perHostRemoteDeliveryQueue.isEmpty())
						{
							defaultNeximLogger.debug("Removing thread (" + this + "/" + hostname + ") from list");

							//RemoteDeliveryThreadPerHost remoteDeliveryThread = (RemoteDeliveryThreadPerHost)
							remoteDeliveryThreadMap.remove(hostname);

							break;
						}
					}
				}
			}

			//m_validHost.remove( m_hostname );
			sessionsManager.release(remoteSession);
			remoteSession = null;

			currentStatus = "Ended";
			defaultNeximLogger.debug("Ending thread " + this);
		}

		private void deliver(TransitableAndSession tas)
		{
			Transitable transitable = tas.getTransitable();

			try
			{
				boolean failedToDeliver = true;

				for (int retry = 0; retry < deliveryMaxRetry; retry++)
				{
					try
					{
						defaultNeximLogger.debug("Trying to send (" + transitable + ") to hostname " + hostname
										+ " step " + retry);

						if (remoteSession == null || remoteSession.isClosed())
						{
							remoteSession = s2sConnectorManager.getRemoteSessionWaitForValidation(hostname,
											deliveryMessageQueueTimeout);
						}

						remoteSession.writeOutputStream(transitable.toString());
						messageLogger.log(transitable);
						messageRecorder.record(transitable);
						defaultNeximLogger.debug("Sent (" + transitable + ") to hostname " + hostname + " step "
										+ retry);
						failedToDeliver = false;

						break;
					}
					catch (java.net.SocketException e)
					{
						sessionsManager.release(remoteSession);
						remoteSession = null;
						temporise(e);
					}
					catch (java.io.IOException e)
					{
						sessionsManager.release(remoteSession);
						remoteSession = null;
						temporise(e);
					}
					catch (Exception e)
					{
						sessionsManager.release(remoteSession);
						remoteSession = null;
						//m_validHost.remove( m_hostname );
						defaultNeximLogger.warn("Remote send failed " + e.getMessage(), e);

						break;
					}
				}

				if (failedToDeliver)
				{
					String to = transitable.getTo();

					defaultNeximLogger.info("Failed to sent (from " + transitable.getFrom() + ") to hostname "
									+ hostname);

					String from = transitable.getFrom();

					transitable.setError("Delivery failed");
					transitable.setErrorCode(500);
					transitable.setFrom(to);
					transitable.setTo(from);
					transitable.setType(Transitable.TYPE_ERROR);

					try
					{
						tas.getSession().writeOutputStream(transitable.toString());
						messageLogger.log(transitable);
						messageRecorder.record(transitable);
					}
					catch (IOException e)
					{
						defaultNeximLogger.warn("Error delivery failed " + e.getMessage(), e);
					}
				}
			}
			catch (Exception e)
			{
				defaultNeximLogger.warn(e.getMessage(), e);
			}
		}

		private final void temporise(Exception e)
		{
			defaultNeximLogger.warn("Remote send failed (retying in " + deliveryRetryDelay + "ms) " + e.getMessage());
			sessionsManager.release(remoteSession);
			remoteSession = null;

			try
			{
				sleep(deliveryRetryDelay);
			}
			catch (InterruptedException ie)
			{
				defaultNeximLogger.debug(ie.getMessage(), ie);
			}

			// we retry
		}
	}

	/** The default nexim logger interface         */
	private DefaultNeximLogger defaultNeximLogger;

	/** The user dao */
	private UserDAO userDAO;

	// Requirements
	private ServerParameters serverParameters;

	private SessionsManager sessionsManager;

	private DeferrableMessageManager deferrableMessageManager;

	@SuppressWarnings("unused")
	private UserDAO accountHolder;

	private MessageLogger messageLogger;

	private MessageRecorder messageRecorder;

	// Configurations
	private int deliveryRetryDelay;

	private int deliveryMaxRetry;

	private long deliveryMessageQueueTimeout;

	// Locals
	private Map<String, IMSession> sessionMap;

	private Map<String, RemoteDeliveryThreadPerHost> remoteDeliveryThreadMap;

	private S2SConnectorManager s2sConnectorManager;

	/** Set the default nexim logger implementation         */
	public void setDefaultNeximLogger(DefaultNeximLogger defaultNeximLogger)
	{
		this.defaultNeximLogger = defaultNeximLogger;
	}

	public void setUserDAO(UserDAO userDAO)
	{
		this.userDAO = userDAO;
	}

	public void setServerParameters(ServerParameters serverParameters)
	{
		this.serverParameters = serverParameters;
	}

	public void setSessionsManager(SessionsManager sessionsManager)
	{
		this.sessionsManager = sessionsManager;
	}

	public void setMessageRecorder(MessageRecorder messageRecorder)
	{
		this.messageRecorder = messageRecorder;
	}

	public void setDeliveryRetryDelay(int deliveryRetryDelay)
	{
		this.deliveryRetryDelay = deliveryRetryDelay;
	}

	public void setDeliveryMaxRetry(int deliveryMaxRetry)
	{
		this.deliveryMaxRetry = deliveryMaxRetry;
	}

	public void setDeliveryMessageQueueTimeout(long deliveryMessageQueueTimeout)
	{
		this.deliveryMessageQueueTimeout = deliveryMessageQueueTimeout;
	}

	public void setDeferrableMessageManager(DeferrableMessageManager deferrableMessageManager)
	{
		this.deferrableMessageManager = deferrableMessageManager;
	}

	public void setMessageLogger(MessageLogger messageLogger)
	{
		this.messageLogger = messageLogger;
	}

	public void setAccountRepositoryHolder(UserDAO accountHolder)
	{
		this.accountHolder = accountHolder;
	}

	//-------------------------------------------------------------------------
	public void initialize()
	{
		//m_validHost = new HashSet();
		sessionMap = new ConcurrentHashMap<String, IMSession>();
		remoteDeliveryThreadMap = new HashMap<String, RemoteDeliveryThreadPerHost>();
	}

	//-------------------------------------------------------------------------
	public S2SConnectorManager getS2SConnectorManager()
	{
		return s2sConnectorManager;
	}

	//-------------------------------------------------------------------------
	public void setS2SConnectorManager(S2SConnectorManager s2sConnectorManager)
	{
		this.s2sConnectorManager = s2sConnectorManager;
	}

	//-------------------------------------------------------------------------
	public void registerSession(final IMClientSession session)
	{
		final User user = session.getUser();

		if (session.getConnectionType() == IMSession.C2S_CONNECTION && user != null)
		{
			defaultNeximLogger.debug("Session map before register : " + sessionMap);
			defaultNeximLogger.debug("Register session user: " + user.getNameAndRessource() + " session id "
							+ session.getId());

			try
			{
				IMSession prevSession = sessionMap.get(user.getNameAndRessource());

				if (prevSession != null)
				{
					defaultNeximLogger.debug("Allready register session: " + prevSession.getId());
					sessionsManager.release(prevSession);
				}
			}
			catch (Exception e)
			{
				defaultNeximLogger.error(e.getMessage(), e);
			}

			synchronized (sessionMap)
			{
				sessionMap.put(user.getNameAndRessource(), session);
			}

			try
			{
				deliverQueueMessage(session, user.getName());
			}
			catch (Exception e)
			{
				defaultNeximLogger.warn("Failed to deliver queue message " + e.getMessage(), e);
			}
		} // if
	}

	public void unregisterSession(final IMClientSession session)
	{
		if (session instanceof IMClientSession)
		{
			User user = (session).getUser();

			if (user != null)
			{
				defaultNeximLogger.debug("Unregister register session user: " + user.getJIDAndRessource()
								+ " session id " + session.getId());

				synchronized (sessionMap)
				{
					sessionMap.remove(user.getNameAndRessource());
				}

				//TODO: We need an event system!!!!! We can not set the user to offline

				//m_sessionMap.remove( user.getName() );
			}
		}
	}

	public List<IMSession> getAllRegisteredSession(final String name)
	{
		List<IMSession> list = new ArrayList<IMSession>(1);
		//TODO: java.util.ConcurrentModificationException ->sessionMap
		final String[] nameArray = sessionMap.keySet().toArray(new String[0]);

		for (int i = 0, l = nameArray.length; i < l; i++)
		{
			defaultNeximLogger.debug("Check if " + name + " could match " + nameArray[i]);

			if (nameArray[i].startsWith(name))
			{
				list.add(sessionMap.get(nameArray[i]));
			}
		}

		return list;
	}

	//-------------------------------------------------------------------------
	private IMClientSession getRegisteredSession(final String name)
	{
		IMClientSession session = (IMClientSession) sessionMap.get(name);

		defaultNeximLogger.debug(">>> getting session for " + name + " having map key " + sessionMap.keySet());

		if (session == null)
		{
			String username = name;

			if (name.indexOf('/') > 0)
			{
				// we have a ressource => get the login
				username = JIDParser.getName(name);
			}

			//TODO: check if correct (was name)
			//FIXME: blla

			//TODO: sdfdsf
			List<IMSession> clientSessions = getAllRegisteredSession(username);

			for (int i = 0, l = clientSessions.size(); i < l; i++)
			{
				IMClientSession s = (IMClientSession) clientSessions.get(i);

				if (session == null || (getPriorityNumber(s) > getPriorityNumber(session)))
				{
					session = s;
					defaultNeximLogger.debug("Select session " + s);
				}
			}
		}

		return session;
	}

	private final int getPriorityNumber(IMClientSession session)
	{
		int priorityNumber = 0;

		if (session.getPresence() != null)
		{
			String priorityStr = session.getPresence().getPriority();

			if (priorityStr != null)
			{
				try
				{
					priorityNumber = Integer.parseInt(priorityStr);
				}
				catch (Exception e)
				{
					defaultNeximLogger.error(e.getMessage(), e);
				}
			}
		}

		return priorityNumber;
	}

	@SuppressWarnings("unchecked")
	public void route(final IMSession currentSession, final Transitable transit) throws java.io.IOException
	{
		final String to = transit.getTo();

		//final String from = transit.getFrom();
		final String toHostname = JIDParser.getHostname(to);

		if (serverParameters.getHostNameList().contains(toHostname))
		{ // local delivery

			final IMClientSession session = getRegisteredSession(JIDParser.getNameAndRessource(to));

			if (session == null)
			{
				if (transit instanceof Deferrable)
				{
					final String username = JIDParser.getName(to);
					User user = userDAO.getUser(username);

					if (user == null)
					{
						defaultNeximLogger.debug(to + " unknown user. Transit value was: " + transit);

						String from = transit.getFrom();

						transit.setError("Not Found");
						transit.setErrorCode(404);
						transit.setFrom(to);
						transit.setTo(from);
						transit.setType(Transitable.TYPE_ERROR);

						messageLogger.log(transit);
						currentSession.writeOutputStream(transit.toString());
						messageLogger.log(transit);
						messageRecorder.record(transit);
					}
					else
					{
						defaultNeximLogger
										.debug(to
														+ " is not connected for getting message, should store for offline dispatch. Transit value was: "
														+ transit);

						List<Transitable> list = deferrableMessageManager.getDeferrableList(username);

						if (list == null)
						{
							list = new ArrayList<Transitable>();
						}

						list.add(transit);
						deferrableMessageManager.setDeferrableList(username, list);
					}
				}
			}
			else
			{
				transit.setTo(session.getUser().getJIDAndRessource());
				session.writeOutputStream(transit.toString());
				//messageLogger.log (transit);
				messageRecorder.record(transit);
			} // else
		} // if
		else
		{ // remote delivery
			defaultNeximLogger.debug("Remote delivery to " + transit.getTo());
			enqueueRemoteDelivery(transit, currentSession);
			defaultNeximLogger.debug("Enqueued to " + transit.getTo());

			//new Thread( new AsyncDeliverer( transit, toHostname, currentSession ) ).start();
		}
	}

	//-------------------------------------------------------------------------
	public void deliverQueueMessage(IMSession currentSession, String username) throws java.io.IOException
	{
		final List list = deferrableMessageManager.getDeferrableList(username);

		if (list != null)
		{
			for (int i = 0, l = list.size(); i < l; i++)
			{
				route(currentSession, (Transitable) list.get(i));
			}
		}

		// empty list
		deferrableMessageManager.setDeferrableList(username, new ArrayList());
	}

	//-------------------------------------------------------------------------
	private void enqueueRemoteDelivery(Transitable transitable, IMSession session)
	{
		TransitableAndSession tas = new TransitableAndSession(transitable, session);

		final String hostname = tas.getHostname();

		synchronized (remoteDeliveryThreadMap)
		{
			RemoteDeliveryThreadPerHost remoteDeliveryThread = remoteDeliveryThreadMap.get(hostname);

			if (remoteDeliveryThread == null)
			{
				// should get from a pool (to implem later)
				if (hostname == null)
				{
					defaultNeximLogger.warn("Absurd hostname for Transitable " + transitable);
				}

				remoteDeliveryThread = new RemoteDeliveryThreadPerHost(hostname);
				remoteDeliveryThread.enqueue(tas);

				remoteDeliveryThread.start();
				remoteDeliveryThreadMap.put(hostname, remoteDeliveryThread);
			}

			else
			{
				remoteDeliveryThread.enqueue(tas);
			}
		} // sync
	}

	public void releaseSessions()
	{
		defaultNeximLogger.debug("Releasing sessions ");

		synchronized (sessionMap)
		{
			Iterator it = sessionMap.values().iterator();

			while (it.hasNext())
			{
				IMSession sess = (IMSession) it.next();

				sessionsManager.release(sess);
			} // end of while ()
		}
	}
}
