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


import de.iritgo.nexim.router.IMRouter;
import de.iritgo.nexim.session.DefaultSessionProcessor;
import de.iritgo.nexim.session.IMClientSession;
import de.iritgo.nexim.session.IMSession;
import de.iritgo.nexim.xmpp.IMMessage;
import org.xmlpull.v1.XmlPullParser;


public class MessageImpl extends DefaultSessionProcessor implements Message
{
	//-------------------------------------------------------------------------
	@Override
	public void process(final IMSession session, final Object context) throws Exception
	{
		XmlPullParser xpp = session.getXmlPullParser();

		String type = xpp.getAttributeValue("", "type");

		IMMessage message = new IMMessage();

		String to = xpp.getAttributeValue("", "to");
		String from = xpp.getAttributeValue("", "from");

		if (session instanceof IMClientSession)
		{
			if (from == null || from.length() == 0)
			{
				from = ((IMClientSession) session).getUser().getJIDAndRessource();
			}

			// Handle ping your self
			if (to == null || to.length() == 0)
			{
				to = ((IMClientSession) session).getUser().getJIDAndRessource();
			} // end of if ()
		}

		message.setTo(to);
		message.setFrom(from);
		message.setType(type);

		super.process(session, message);

		IMRouter router = session.getRouter();

		router.route(session, message);

		/*
		 String iqMsg = session.getMessageData().getId();

		 String s = "<iq type='"+IqData.TYPE_RESULT+"' id='"+iqId+"'>"
		 +"<query xmlns='jabber:iq:roster'>"
		 +"<item jid='romeo@montague.net' name='Romeo' subscription='both'/>"
		 +"</query></iq>";


		 session.writeOutputStream( s );
		 */
	}
}
