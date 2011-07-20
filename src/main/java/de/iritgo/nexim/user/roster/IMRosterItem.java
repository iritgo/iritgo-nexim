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

package de.iritgo.nexim.user.roster;


import de.iritgo.nexim.tools.JIDParser;
import de.iritgo.nexim.tools.XMLToString;


public class IMRosterItem implements java.io.Serializable
{
	private static final long serialVersionUID = 15L;

	public static final String SUBSCRIPTION_REMOVE = "remove";

	public static final String SUBSCRIPTION_BOTH = "both";

	public static final String SUBSCRIPTION_NONE = "none";

	public static final String SUBSCRIPTION_TO = "to";

	public static final String SUBSCRIPTION_FROM = "from";

	public static final String ASK_SUBSCRIBE = "subscribe";

	public static final String ASK_UNSUBSCRIBE = "unsubscribe";

	private String name;

	private String jid;

	private String group;

	private String subscription;

	private String ask;

	public final void setName (String name)
	{
		this.name = name;
	}

	public final String getName ()
	{
		return name;
	}

	public final void setJID (String jid)
	{
		if (jid != null)
		{
			this.jid = JIDParser.getJID (jid);
		}
	}

	public final String getJID ()
	{
		return jid;
	}

	public final void setGroup (String group)
	{
		this.group = group;
	}

	public final String getGroup ()
	{
		return group;
	}

	public final void setSubscription (String subscription)
	{
		this.subscription = subscription;
	}

	public final String getSubscription ()
	{
		return subscription;
	}

	public final void setAsk (String ask)
	{
		this.ask = ask;
	}

	public final String getAsk ()
	{
		return ask;
	}

	@Override
	public boolean equals (Object obj)
	{
		return jid.equals (((IMRosterItem) obj).jid);
	}

	@Override
	public String toString ()
	{
		XMLToString item = new XMLToString ("item");

		item.addFilledAttribut ("name", name);
		item.addFilledAttribut ("jid", jid);
		item.addFilledAttribut ("subscription", subscription);
		item.addFilledAttribut ("ask", ask);

		XMLToString group = new XMLToString ("group");

		group.addTextNode (this.group);
		item.addElement (group);

		return item.toString ();
	}
}
