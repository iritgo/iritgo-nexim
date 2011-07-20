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

package de.iritgo.nexim.xmpp.jabber.iq.browse;


import de.iritgo.nexim.ServerParameters;
import de.iritgo.nexim.session.DefaultSessionProcessor;
import de.iritgo.nexim.session.IMClientSession;
import de.iritgo.nexim.session.IMSession;
import de.iritgo.nexim.xmpp.IMIq;


public class QueryImpl extends DefaultSessionProcessor implements Query
{
	private ServerParameters serverParameters;

	public void setServerParameters (ServerParameters serverParameters)
	{
		this.serverParameters = serverParameters;
	}

	//-------------------------------------------------------------------------
	@Override
	public void process (final IMSession session, final Object context) throws Exception
	{
		IMClientSession clientSession = (IMClientSession) session;
		String type = ((IMIq) context).getType ();

		// GET
		if (IMIq.TYPE_GET.equals (type))
		{
			get (clientSession, context);
		}
		else if (IMIq.TYPE_SET.equals (type))
		{
			set (clientSession, context);
		}
	}

	//-------------------------------------------------------------------------
	private void get (final IMClientSession session, Object context) throws Exception
	{
		//final XmlPullParser xpp = session.getXmlPullParser();
		String iqId = ((IMIq) context).getId ();

		String s = "<iq type='result'";

		s += " from='" + serverParameters.getHostName () + "'";
		s += " to='" + session.getUser ().getJIDAndRessource () + "'";
		s += " id='" + iqId + "'";
		s += ">";
		s += "<service jid='" + serverParameters.getHostName ()
						+ "' name='OpenIM Server' type='jabber' xmlns='jabber:iq:browse'>";
		s += "<item category='service' jid='" + serverParameters.getHostName ()
						+ "' name='OpenIM User Directory' type='jud'>";
		s += "<ns>jabber:iq:register</ns>";
		s += "</item>";
		s += "</service></iq>";

		session.writeOutputStream (s);
	}

	//-------------------------------------------------------------------------
	/**
	 * @param session
	 * @param context
	 */
	private void set (final IMClientSession session, final Object context) throws Exception
	{
		//final XmlPullParser xpp = session.getXmlPullParser();
		getLogger ().warn ("Skipping jabber:iq:browse:query set");
	}
}
