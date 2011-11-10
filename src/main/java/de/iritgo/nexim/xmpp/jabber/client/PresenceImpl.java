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

package de.iritgo.nexim.xmpp.jabber.client;


import de.iritgo.nexim.presence.IMPresence;
import de.iritgo.nexim.presence.IMPresenceHolder;
import de.iritgo.nexim.presence.IMPresenceImpl;
import de.iritgo.nexim.presence.SubscriptionManager;
import de.iritgo.nexim.router.IMRouter;
import de.iritgo.nexim.session.DefaultSessionProcessor;
import de.iritgo.nexim.session.IMClientSession;
import de.iritgo.nexim.session.IMSession;
import de.iritgo.nexim.user.roster.IMRosterItem;
import de.iritgo.nexim.user.roster.RosterItemProcessor;
import de.iritgo.nexim.user.roster.RosterManager;
import org.xmlpull.v1.XmlPullParser;
import java.util.Locale;


public class PresenceImpl extends DefaultSessionProcessor implements Presence
{
	// Requirements
	private IMPresenceHolder presenceHolder;

	private SubscriptionManager subscriptionManager;

	private RosterManager rosterManager;

	public void setImPresenceHolder(IMPresenceHolder presenceHolder)
	{
		this.presenceHolder = presenceHolder;
	}

	public void setSubscriptionManager(SubscriptionManager subscriptionManager)
	{
		this.subscriptionManager = subscriptionManager;
	}

	public void setRosterManager(RosterManager rosterManager)
	{
		this.rosterManager = rosterManager;
	}

	//-------------------------------------------------------------------------
	@Override
	public void process(final IMSession session, final Object context) throws Exception
	{
		IMClientSession clientSession = (IMClientSession) session;

		XmlPullParser xpp = session.getXmlPullParser();

		String type = xpp.getAttributeValue("", "type");
		String to = xpp.getAttributeValue("", "to");

		//		System.out.println ("!!!!!!!!!!!!!!!!!!!" + xpp.getAttributeValue ("", "id"));

		String from = xpp.getAttributeValue("", "from");

		if (from == null || from.length() == 0)
		{
			from = clientSession.getUser().getJIDAndRessource();
		}

		final IMPresence presence = new IMPresenceImpl();

		presence.setType(type);
		presence.setFrom(from);

		super.process(session, presence);

		clientSession.setPresence(presence);

		if (type == null || type.length() == 0 || IMPresence.TYPE_AVAILABLE.equals(type)
						|| IMPresence.TYPE_UNAVAILABLE.equals(type))
		{
			presenceHolder.setPresence(from, presence);
		}

		getLogger().debug("Got presence (to " + to + ") " + presence);

		final IMRouter router = session.getRouter();

		if (to == null || to.length() == 0 || to.equals("null"))
		{
			// emit presence associated to roster friends
			rosterManager.processItems(clientSession.getUser().getName(), new RosterItemProcessor()
			{
				public void process(IMRosterItem item) throws Exception
				{
					IMPresence localPresence = (IMPresence) presence.clone();

					localPresence.setTo(item.getJID());
					router.route(session, localPresence);
				}
			});
		}
		else
		{
			IMPresence localPresence = (IMPresence) presence.clone();

			localPresence.setTo(to);

			subscriptionManager.process(session, localPresence);
		}
	}

	public String getFeatureDesription(Locale locale)
	{
		return null;
	}

	public String getFeatureTag()
	{
		return "<feature var='jabber:iq:presence'/>";
	}

	// ------------------------------------------------------------------------
}
