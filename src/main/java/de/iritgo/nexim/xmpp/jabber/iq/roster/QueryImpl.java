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

package de.iritgo.nexim.xmpp.jabber.iq.roster;


import de.iritgo.nexim.presence.IMPresence;
import de.iritgo.nexim.presence.IMPresenceHolder;
import de.iritgo.nexim.presence.IMPresenceImpl;
import de.iritgo.nexim.presence.SubscriptionManager;
import de.iritgo.nexim.session.DefaultSessionProcessor;
import de.iritgo.nexim.session.IMClientSession;
import de.iritgo.nexim.session.IMSession;
import de.iritgo.nexim.user.UserManager;
import de.iritgo.nexim.user.roster.IMRosterItem;
import de.iritgo.nexim.user.roster.RosterItemProcessor;
import de.iritgo.nexim.user.roster.RosterManager;
import de.iritgo.nexim.xmpp.IMIq;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;


public class QueryImpl extends DefaultSessionProcessor implements Query
{
	// Requirements
	private IMPresenceHolder presenceHolder;

	private SubscriptionManager subscriptionManager;

	@SuppressWarnings("unused")
	private UserManager userManager;

	private RosterManager rosterManager;

	public void setImPresenceHolder(IMPresenceHolder presenceHolder)
	{
		this.presenceHolder = presenceHolder;
	}

	public void setSubscriptionManager(SubscriptionManager subscriptionManager)
	{
		this.subscriptionManager = subscriptionManager;
	}

	public void setUserManager(UserManager userManager)
	{
		this.userManager = userManager;
	}

	public void setRosterManager(RosterManager rosterManager)
	{
		this.rosterManager = rosterManager;
	}

	//-------------------------------------------------------------------------
	@Override
	public void process(final IMSession session, final Object context) throws Exception
	{
		String iqId = ((IMIq) context).getId();
		String type = ((IMIq) context).getType();

		getLogger().debug("Roster query type = " + type + " iqId " + iqId);

		if (IMIq.TYPE_GET.equals(type))
		{
			get(iqId, (IMClientSession) session);
		}
		else if (IMIq.TYPE_SET.equals(type))
		{
			set(iqId, (IMClientSession) session);
		}
	}

	// ------------------------------------------------------------------------
	private void set(String iqId, IMClientSession session) throws Exception
	{
		IMRosterItem roster = new IMRosterItem();

		//session.setRosterItem( roster );
		super.process(session, roster);

		// shall we remove?
		if (IMRosterItem.SUBSCRIPTION_REMOVE.equals(roster.getSubscription()))
		{
			//removeFromRosterList( rosterList, roster.getJID() );
			String rosterAck = "<iq type='set'><query xmlns='jabber:iq:roster'>";

			rosterAck += roster.toString();
			rosterAck += "</query></iq>";

			rosterAck += "<iq type='result' id='" + iqId + "'/>";

			emitToAllRegisteredSession(session, rosterAck);

			// emit unsubscrib presence to removed buddy
			IMPresence presence = new IMPresenceImpl();

			presence.setTo(roster.getJID());
			presence.setFrom(session.getUser().getJID());
			presence.setType(IMPresence.TYPE_UNSUBSCRIBE);
			subscriptionManager.process(session, presence);
			// emit unsubscrib presence to removed buddy
			presence = new IMPresenceImpl();
			presence.setTo(roster.getJID());
			presence.setFrom(session.getUser().getJID());
			presence.setType(IMPresence.TYPE_UNSUBSCRIBED);
			subscriptionManager.process(session, presence);
		}

		// we set
		else
		{
			getLogger().debug("Setting roster item " + roster);

			String username = session.getUser().getName();

			IMRosterItem localroster = rosterManager.getItem(username, roster.getJID());

			if (localroster == null)
			{
				roster.setSubscription(IMRosterItem.SUBSCRIPTION_NONE);
			}
			else
			{
				localroster.setName(roster.getName());
				localroster.setGroup(roster.getGroup());
				roster = localroster;
			}

			// build roster ack string
			if (roster.getName() == null || roster.getName().length() == 0)
			{
				roster.setName(roster.getJID());
			}

			if (roster.getGroup() == null || roster.getGroup().length() == 0)
			{
				roster.setGroup("General");
			}

			getLogger().debug("Got roster: " + roster);

			//roster.setSubscription( IMRosterItem.SUBSCRIPTION_NONE );
			String rosterAck = "<iq type='set' to='" + session.getUser().getJIDAndRessource() + "' id='" + iqId
							+ "'><query xmlns='jabber:iq:roster'>";

			rosterAck += roster.toString();
			rosterAck += "</query></iq>";

			rosterAck += "<iq type='result' to='" + session.getUser().getJIDAndRessource() + "' id='" + iqId + "'/>";

			String subscription = roster.getSubscription();

			//			if (IMRosterItem.SUBSCRIPTION_FROM.equals (subscription)
			//							|| IMRosterItem.SUBSCRIPTION_NONE.equals (subscription) || subscription == null)
			{
				emitToAllRegisteredSession(session, rosterAck);
			}

			// remove/replace prev occurence of the buddy
			rosterManager.removeItem(username, roster.getJID());
			rosterManager.addItem(username, roster);
		} // else add buddy
	} // set

	// ------------------------------------------------------------------------
	private void get(String iqId, final IMClientSession session) throws Exception
	{
		final StringBuffer s = new StringBuffer("<iq type='" + IMIq.TYPE_RESULT + "' id='" + iqId + "' from='"
						+ session.getUser().getJIDAndRessource() + "'>" + "<query xmlns='jabber:iq:roster'>");

		String username = session.getUser().getName();

		rosterManager.processItems(username, new RosterItemProcessor()
		{
			public void process(IMRosterItem item) throws Exception
			{
				s.append(item.toString());
			}
		});

		s.append("</query></iq>");
		session.writeOutputStream(s.toString());

		rosterManager.processItems(username, new RosterItemProcessor()
		{
			public void process(IMRosterItem item) throws Exception
			{
				String subscription = item.getSubscription();

				if (IMRosterItem.SUBSCRIPTION_BOTH.equals(subscription)
								|| IMRosterItem.SUBSCRIPTION_TO.equals(subscription))
				{
					Collection col = presenceHolder.getPresence(item.getJID());

					if (col != null && ! col.isEmpty())
					{
						Iterator iter = col.iterator();

						while (iter.hasNext())
						{
							IMPresence currentPresence = (IMPresence) iter.next();

							session.writeOutputStream(currentPresence.toString());
						}
					}

					// probe the presence
					else
					{
						IMPresence presence = new IMPresenceImpl();

						presence.setFrom(session.getUser().getJID());
						presence.setTo(item.getJID());
						presence.setType(IMPresence.TYPE_PROBE);
						session.getRouter().route(session, presence);
					}
				} // subscribtion
			}
		});
	} // get

	// ------------------------------------------------------------------------
	private final void emitToAllRegisteredSession(IMClientSession session, String str) throws Exception
	{
		// emit roster ack to client / should be all active ressource....
		List sessionList = session.getRouter().getAllRegisteredSession(session.getUser().getName());

		for (int i = 0, l = sessionList.size(); i < l; i++)
		{
			IMSession s = (IMSession) sessionList.get(i);

			s.writeOutputStream(str);
		}
	}
}
