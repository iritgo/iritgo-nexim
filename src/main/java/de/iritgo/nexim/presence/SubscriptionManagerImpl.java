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

package de.iritgo.nexim.presence;


import de.iritgo.nexim.ServerParameters;
import de.iritgo.nexim.log.DefaultNeximLogger;
import de.iritgo.nexim.session.IMSession;
import de.iritgo.nexim.tools.JIDParser;
import de.iritgo.nexim.user.roster.AddRosterItemNotAllowedException;
import de.iritgo.nexim.user.roster.IMRosterItem;
import de.iritgo.nexim.user.roster.RemoveRosterItemNotAllowedException;
import de.iritgo.nexim.user.roster.RosterManager;
import java.util.Collection;
import java.util.List;


public class SubscriptionManagerImpl implements SubscriptionManager
{
	private IMPresenceHolder presenceHolder;

	private RosterManager rosterManager;

	private ServerParameters serverParameters;

	/** The default nexim logger interface         */
	@SuppressWarnings("unused")
	private DefaultNeximLogger defaultNeximLogger;

	/** Set the default nexim logger implementation         */
	public void setDefaultNeximLogger(DefaultNeximLogger defaultNeximLogger)
	{
		this.defaultNeximLogger = defaultNeximLogger;
	}

	public void setImPresenceHolder(IMPresenceHolder presenceHolder)
	{
		this.presenceHolder = presenceHolder;
	}

	public void setRosterManager(RosterManager rosterManager)
	{
		this.rosterManager = rosterManager;
	}

	public void setServerParameters(ServerParameters serverParameters)
	{
		this.serverParameters = serverParameters;
	}

	//-------------------------------------------------------------------------
	public void process(final IMSession session, IMPresence presence) throws Exception
	{
		presence = new DeferrableIMPresenceImpl(presence);

		// remove ressources
		String to = JIDParser.getJID(presence.getTo());
		String from = JIDParser.getJID(presence.getFrom());

		presence.setTo(to);
		presence.setFrom(from);

		boolean toLocalHost = false;

		if (serverParameters.getHostNameList().contains(JIDParser.getHostname(to)))
		{
			toLocalHost = true;
		}

		boolean fromLocalHost = false;

		if (serverParameters.getHostNameList().contains(JIDParser.getHostname(from)))
		{
			fromLocalHost = true;
		}

		String type = presence.getType();

		if (IMPresence.TYPE_SUBSCRIBE.equals(type))
		{
			if (fromLocalHost)
			{
				preSubscribe(session, presence, toLocalHost);
			}
			else
			{
				postSubscribe(session, presence);
			}
		}

		else if (IMPresence.TYPE_SUBSCRIBED.equals(type))
		{
			if (fromLocalHost)
			{
				preSubscribed(session, presence, toLocalHost);
			}
			else
			{
				postSubscribed(session, presence);
			}
		}

		else if (IMPresence.TYPE_UNSUBSCRIBE.equals(type))
		{
			if (fromLocalHost)
			{
				preUnsubscribe(session, presence, toLocalHost);
			}
			else
			{
				postUnsubscribe(session, presence);
			}
		}

		else if (IMPresence.TYPE_UNSUBSCRIBED.equals(type))
		{
			if (fromLocalHost)
			{
				preUnsubscribed(session, presence, toLocalHost);
			}
			else
			{
				postUnsubscribed(session, presence);
			}
		}

		else
		{
			session.getRouter().route(session, presence);
		}
	}

	// ------------------------------------------------------------------------
	private void preSubscribe(IMSession session, IMPresence presence, boolean toLocalHost) throws Exception
	{
		String to = presence.getTo();
		String from = presence.getFrom();

		IMRosterItem roster = getItem(from, to);

		String rosterAck = "<iq type='set' to='" + from + "'><query xmlns='jabber:iq:roster'>";

		if (roster != null)
		{
			roster.setAsk(IMRosterItem.ASK_SUBSCRIBE);
			rosterAck += roster.toString();
			roster.setAsk(null);
		}

		rosterAck += "</query></iq>";

		//		emitToAllRegisteredSession (session, from, rosterAck);

		if (toLocalHost)
		{
			postSubscribe(session, presence);
		}
		else
		{
			session.getRouter().route(session, presence);
		}
	}

	// ------------------------------------------------------------------------
	private void postSubscribe(IMSession session, IMPresence presence) throws Exception
	{
		String to = presence.getTo();
		String from = presence.getFrom();

		IMRosterItem item = getItem(to, from);

		if (item == null)
		{
			item = new IMRosterItem();
			item.setSubscription(IMRosterItem.SUBSCRIPTION_NONE);
			item.setJID(from);
			item.setName(from);
			item.setGroup("General");
			addRoster(to, item);
		}

		String subscription = item.getSubscription();
		if (IMRosterItem.SUBSCRIPTION_TO.equals(subscription) || IMRosterItem.SUBSCRIPTION_NONE.equals(subscription)
						|| subscription == null)
		{
			if (IMRosterItem.SUBSCRIPTION_FROM.equals(subscription))
			{
				setRosterSubcription(to, from, IMRosterItem.SUBSCRIPTION_BOTH);
			}
			else
			{
				setRosterSubcription(to, from, IMRosterItem.SUBSCRIPTION_TO);
			}

			if (IMRosterItem.SUBSCRIPTION_TO.equals(subscription))
			{
				setRosterSubcription(to, from, IMRosterItem.SUBSCRIPTION_BOTH);
			}
			else
			{
				setRosterSubcription(to, from, IMRosterItem.SUBSCRIPTION_FROM);
			}

			session.getRouter().route(session, presence);
		}
		else if (IMRosterItem.SUBSCRIPTION_BOTH.equals(subscription)
						|| IMRosterItem.SUBSCRIPTION_FROM.equals(subscription))
		{
			IMPresence presence2 = (IMPresence) presence.clone();
			presence2.setType(IMPresence.TYPE_SUBSCRIBED);

			//More to do....
			//			System.out.println (session.getNamespace () + "SendPresence:" + presence2.toString ());
			session.getRouter().route(session, presence2);
		}
	}

	// ------------------------------------------------------------------------

	// ------------------------------------------------------------------------
	private void preSubscribed(IMSession session, IMPresence presence, boolean toLocalHost) throws Exception
	{
		String to = presence.getTo();
		String from = presence.getFrom();

		IMRosterItem roster = getItem(from, to);
		String rosterAck = "<iq type='set'><query xmlns='jabber:iq:roster'>";

		if (roster != null)
		{
			rosterAck += roster.toString();
		}

		rosterAck += "</query></iq>";
		//		emitToAllRegisteredSession (session, from, rosterAck);

		if (toLocalHost)
		{
			postSubscribed(session, presence);
		}
		else
		{
			session.getRouter().route(session, presence);
		}

		Collection<IMPresence> col = presenceHolder.getPresence(from);

		if (col != null && ! col.isEmpty())
		{
			for (IMPresence currentPresence : col)
			{
				currentPresence = (IMPresence) currentPresence.clone();
				currentPresence.setTo(to);
				session.getRouter().route(session, currentPresence);
			}
		}
	}

	// ------------------------------------------------------------------------
	private void postSubscribed(IMSession session, IMPresence presence) throws Exception
	{
		String to = presence.getTo();
		String from = presence.getFrom();

		IMRosterItem roster = getItem(to, from);

		if (roster == null)
		{
			roster = new IMRosterItem();
			roster.setSubscription(IMRosterItem.SUBSCRIPTION_NONE);
			roster.setJID(from);
			roster.setName(from);
			roster.setGroup("General");
			addRoster(to, roster);
		}

		String subscription = roster.getSubscription();

		if (IMRosterItem.SUBSCRIPTION_FROM.equals(subscription) || IMRosterItem.SUBSCRIPTION_NONE.equals(subscription)
						|| subscription == null)
		{
			session.getRouter().route(session, presence);

			//roster.setSubscription( IMRosterItem.SUBSCRIPTION_TO );
			if (IMRosterItem.SUBSCRIPTION_FROM.equals(subscription))
			{
				setRosterSubcription(to, from, IMRosterItem.SUBSCRIPTION_BOTH);
				roster.setSubscription(IMRosterItem.SUBSCRIPTION_BOTH);
			}
			else
			{
				setRosterSubcription(to, from, IMRosterItem.SUBSCRIPTION_TO);
				roster.setSubscription(IMRosterItem.SUBSCRIPTION_TO);
			}

			String rosterAck = "<iq type='set'><query xmlns='jabber:iq:roster'>";

			rosterAck += roster.toString();
			rosterAck += "</query></iq>";
			//			emitToAllRegisteredSession (session, to, rosterAck);
		}
	}

	// ------------------------------------------------------------------------

	// ------------------------------------------------------------------------
	private void preUnsubscribe(IMSession session, IMPresence presence, boolean toLocalHost) throws Exception
	{
		String to = presence.getTo();
		String from = presence.getFrom();

		IMRosterItem roster = getItem(from, to);

		if (roster != null)
		{
			roster.setSubscription(IMRosterItem.SUBSCRIPTION_NONE);

			String rosterAck = "<iq type='set'><query xmlns='jabber:iq:roster'>";

			rosterAck += roster.toString();
			rosterAck += "</query></iq>";
			//			emitToAllRegisteredSession (session, from, rosterAck);

			removeRoster(from, to);
		}

		if (toLocalHost)
		{
			postUnsubscribe(session, presence);
		}
		else
		{
			session.getRouter().route(session, presence);
		}
	}

	// ------------------------------------------------------------------------
	private void postUnsubscribe(IMSession session, IMPresence presence) throws Exception
	{
		session.getRouter().route(session, presence);
	}

	// ------------------------------------------------------------------------

	// ------------------------------------------------------------------------
	private void preUnsubscribed(IMSession session, IMPresence presence, boolean toLocalHost) throws Exception
	{
		String to = presence.getTo();
		String from = presence.getFrom();

		if (toLocalHost)
		{
			postUnsubscribed(session, presence);
		}
		else
		{
			session.getRouter().route(session, presence);
		}

		IMPresence currentPresence = new IMPresenceImpl();

		currentPresence.setFrom(from);
		currentPresence.setTo(to);
		currentPresence.setType(IMPresence.TYPE_UNAVAILABLE);
	}

	// ------------------------------------------------------------------------
	private void postUnsubscribed(IMSession session, IMPresence presence) throws Exception
	{
		String to = presence.getTo();
		String from = presence.getFrom();

		IMRosterItem roster = getItem(to, from);

		if (roster != null)
		{
			roster.setSubscription(IMRosterItem.SUBSCRIPTION_NONE);

			String rosterAck = "<iq type='set'><query xmlns='jabber:iq:roster'>";

			rosterAck += roster.toString();
			rosterAck += "</query></iq>";
			//			emitToAllRegisteredSession (session, to, rosterAck);

			removeRoster(to, from);
		}

		session.getRouter().route(session, presence);
	}

	// ------------------------------------------------------------------------

	// ------------------------------------------------------------------------
	@SuppressWarnings("unchecked")
	private final void addRoster(String usernameJID, IMRosterItem rosterItem) throws AddRosterItemNotAllowedException
	{
		String username = JIDParser.getName(usernameJID);

		rosterManager.addItem(username, rosterItem);
	}

	// ------------------------------------------------------------------------
	@SuppressWarnings("unchecked")
	private final void setRosterSubcription(String usernameJID, String itemJID, String subscription)
	{
		String username = JIDParser.getName(usernameJID);

		IMRosterItem roster = rosterManager.getItem(username, itemJID);

		if (roster != null)
		{
			roster.setSubscription(subscription);
		}
	}

	// ------------------------------------------------------------------------
	@SuppressWarnings("unchecked")
	private final void removeRoster(String usernameJID, String itemJID) throws RemoveRosterItemNotAllowedException
	{
		String username = JIDParser.getName(usernameJID);

		rosterManager.removeItem(username, itemJID);
	}

	// ------------------------------------------------------------------------
	@SuppressWarnings("unchecked")
	private final IMRosterItem getItem(String usernameJID, String itemJID)
	{
		String username = JIDParser.getName(usernameJID);

		return rosterManager.getItem(username, itemJID);
	}

	// ------------------------------------------------------------------------
	private final void emitToAllRegisteredSession(IMSession session, String usernameJID, String str) throws Exception
	{
		// emit roster ack to client / should be all active ressource....
		String username = JIDParser.getName(usernameJID);
		List<IMSession> sessionList = session.getRouter().getAllRegisteredSession(username);

		for (int i = 0, l = sessionList.size(); i < l; i++)
		{
			IMSession s = sessionList.get(i);

			s.writeOutputStream(str);
		}
	}
}
