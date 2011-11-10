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

package de.iritgo.nexim.xmpp.jabber.disco;


import de.iritgo.nexim.ServerParameters;
import de.iritgo.nexim.session.DefaultSessionProcessor;
import de.iritgo.nexim.session.IMClientSession;
import de.iritgo.nexim.session.IMSession;
import de.iritgo.nexim.session.SessionProcessor;
import de.iritgo.nexim.session.SessionProcessorRegistry;
import de.iritgo.nexim.xmpp.IMIq;
import de.iritgo.nexim.xmpp.XMPPFeatureDescriptor;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import java.io.IOException;


public class InfoQueryImpl extends DefaultSessionProcessor implements InfoQuery
{
	private ServerParameters serverParameters;

	private SessionProcessorRegistry sessionProcessorRegistry;

	public void setServerParameters(ServerParameters serverParameters)
	{
		this.serverParameters = serverParameters;
	}

	@Override
	public void setSessionProcessorRegistry(SessionProcessorRegistry sessionProcessorRegistry)
	{
		this.sessionProcessorRegistry = sessionProcessorRegistry;
	}

	@Override
	public void process(IMSession session, Object context) throws Exception
	{
		IMIq imIq = (IMIq) context;
		String type = imIq.getType();

		// GET
		if (IMIq.TYPE_GET.equals(type))
		{
			get(session, imIq);
		}
	}

	private void get(IMSession session, IMIq imIq) throws IOException, XmlPullParserException
	{
		final XmlPullParser xpp = session.getXmlPullParser();
		final String eventName = xpp.getNamespace() + ':' + xpp.getName();

		String iqId = imIq.getId();

		String from = serverParameters.getHostName();
		String to = ((IMClientSession) session).getUser().getJID();

		IMIq iq = null;

		StringBuffer data = new StringBuffer().append("<query xmlns='http://jabber.org/protocol/disco#info'>");

		/*
		                "<identity> " +
		        "category='conference' " +
		        "type='text' " +
		        "name='Play-Specific Chatrooms'/> " +
		        "<identity " +
		        "category='directory' " +
		        "type='chatroom' " +
		        "name='Play-Specific Chatrooms'/> " +
		        "<feature var='http://jabber.org/protocol/disco#info'/> " +
		        "<feature var='http://jabber.org/protocol/disco#items'/> " +
		        "<feature var='http://jabber.org/protocol/muc'/> " +
		        "<feature var='jabber:iq:register'/> " +
		        "<feature var='jabber:iq:search'/> " +
		        "<feature var='jabber:iq:version'/> " +
		 */
		for (SessionProcessor processor : sessionProcessorRegistry.getProcessors())
		{
			if (processor instanceof XMPPFeatureDescriptor)
			{
				data.append(((XMPPFeatureDescriptor) processor).getFeatureTag() + " ");
			}
		}

		data.append("</query> ");

		getLogger().debug("Get " + to + "/" + eventName + " send: " + data);

		// local request
		iq = new IMIq();
		iq.setFrom(from);
		iq.setTo(to);
		iq.setId(iqId);
		iq.setType(IMIq.TYPE_RESULT);
		iq.setStringData(data.toString());

		session.getRouter().route(session, iq);

		skip(xpp);
	}
}
