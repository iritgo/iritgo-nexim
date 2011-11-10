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

package de.iritgo.nexim.xmpp.jabber.server.dialback;


import de.iritgo.nexim.session.DefaultSessionProcessor;
import de.iritgo.nexim.session.IMServerSession;
import de.iritgo.nexim.session.IMSession;
import org.xmlpull.v1.XmlPullParser;


public class ResultImpl extends DefaultSessionProcessor implements Result
{
	private String dialbackValue;

	//-------------------------------------------------------------------------
	@Override
	public void process(final IMSession session, final Object context) throws Exception
	{
		IMServerSession serverSession = (IMServerSession) session;

		XmlPullParser xpp = session.getXmlPullParser();

		//String to = xpp.getAttributeValue( "", "to" );
		String from = xpp.getAttributeValue("", "from");
		String type = xpp.getAttributeValue("", "type");

		if (from != null && from.length() > 0)
		{
			serverSession.setRemoteHostname(from);
		}

		super.process(session, context);

		//String id = xpp.getAttributeValue( "", "id" );
		if ("valid".equals(type))
		{
			getLogger().debug("Result valid from " + from);
			serverSession.setDialbackValid(true);

			synchronized (session)
			{
				session.notifyAll();
			}
		}
		else if (dialbackValue != null)
		{
			getLogger().debug("Verify " + from + " dialback " + dialbackValue);

			if (serverSession.getTwinSession() == null)
			{
				session.getRouter().getS2SConnectorManager().verifyRemoteHost(from, dialbackValue,
								Long.toString(session.getId()), serverSession);
			}
		}
	}

	//-------------------------------------------------------------------------
	@Override
	public void processText(final IMSession session, final Object context) throws Exception
	{
		dialbackValue = session.getXmlPullParser().getText().trim();
	}
}
