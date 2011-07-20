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

package de.iritgo.nexim.xmpp.jabber.server;


import de.iritgo.nexim.presence.IMPresence;
import de.iritgo.nexim.presence.IMPresenceHolder;
import de.iritgo.nexim.presence.IMPresenceImpl;
import de.iritgo.nexim.presence.SubscriptionManager;
import de.iritgo.nexim.session.DefaultSessionProcessor;
import de.iritgo.nexim.session.IMSession;
import org.xmlpull.v1.XmlPullParser;
import java.util.Collection;
import java.util.Iterator;


public class PresenceImpl extends DefaultSessionProcessor implements Presence
{
	// Requirements
	private IMPresenceHolder presenceHolder;

	private SubscriptionManager subscriptionManager;

	public void setImPresenceHolder (IMPresenceHolder presenceHolder)
	{
		this.presenceHolder = presenceHolder;
	}

	public void setSubscriptionManager (SubscriptionManager subscriptionManager)
	{
		this.subscriptionManager = subscriptionManager;
	}

	//-------------------------------------------------------------------------
	@Override
	public void process (final IMSession session, final Object context) throws Exception
	{
		XmlPullParser xpp = session.getXmlPullParser ();
		String type = xpp.getAttributeValue ("", "type");
		String to = xpp.getAttributeValue ("", "to");
		String from = xpp.getAttributeValue ("", "from");

		IMPresence presence = new IMPresenceImpl ();

		presence.setType (type);
		presence.setTo (to);
		presence.setFrom (from);

		super.process (session, presence);

		getLogger ().debug ("Got presence (to " + to + "): " + presence);

		if (to == null || to.length () == 0)
		{
			// emit presence associated to roster friends?
			getLogger ().debug ("To is not specified, what should we do?");
		}
		else
		{
			String presenceType = presence.getType ();

			if (IMPresence.TYPE_PROBE.equals (presenceType))
			{
				getLogger ().info ("Probed from " + from + " to " + to);

				// check availability
				Collection col = presenceHolder.getPresence (to);

				if (col != null && ! col.isEmpty ())
				{
					Iterator iter = col.iterator ();

					while (iter.hasNext ())
					{
						IMPresence localPresence = (IMPresence) iter.next ();

						localPresence = (IMPresence) localPresence.clone ();
						localPresence.setTo (from);
						session.getRouter ().route (session, localPresence);
					}
				}

				// unavailable
				else
				{
					IMPresence localPresence = new IMPresenceImpl ();

					localPresence.setType (IMPresence.TYPE_UNAVAILABLE);
					localPresence.setFrom (to);
					localPresence.setTo (from);
					session.getRouter ().route (session, localPresence);
				}
			}

			else
			{
				IMPresence localPresence = (IMPresence) presence.clone ();

				subscriptionManager.process (session, localPresence);
			}
		} // if to null
	}
}
