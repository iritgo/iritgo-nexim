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


public class VerifyImpl extends DefaultSessionProcessor implements Verify
{
	private String dialbackValue;

	//-------------------------------------------------------------------------
	@Override
	public void process (final IMSession session, final Object context) throws Exception
	{
		IMServerSession serverSession = (IMServerSession) session;

		XmlPullParser xpp = session.getXmlPullParser ();
		String type = xpp.getAttributeValue ("", "type");
		String from = xpp.getAttributeValue ("", "from");
		String to = xpp.getAttributeValue ("", "to");
		String id = xpp.getAttributeValue ("", "id");

		super.process (session, context);

		getLogger ().debug ("Got m_dialbackValue " + dialbackValue);

		if ("valid".equals (type))
		{
			String s = "<db:result to='" + from + "' from='" + to + "' type='valid' id='" + id + "'/>";

			getLogger ().debug ("Verfication valid " + s);
			serverSession.getTwinSession ().writeOutputStream (s);

			//session.getRouter().validateRemoteHost( session, from, m_dialbackValue );
		}
		else if (dialbackValue != null)
		{
			IMServerSession local2remoteSession = session.getRouter ().getS2SConnectorManager ()
							.getCurrentRemoteSession (from);

			if (local2remoteSession != null && dialbackValue.equals (local2remoteSession.getDialbackValue ()))
			{
				getLogger ().debug (
								"Verification valid from " + from + " to " + to + " id " + id + " dialbackId "
												+ dialbackValue);

				//session.getRouter().validateRemoteHost( session, from, m_dialbackValue );
				String s = "<db:verify from='" + to + "' " + "to='" + from + "' " + "id='" + id + "' "
								+ "type='valid'/>";

				session.writeOutputStream (s);
			}
			else
			{
				// should send unvalid?
				if (local2remoteSession == null)
				{
					getLogger ().warn ("Abnormal: local2remoteSession null");
				}
				else
				{
					getLogger ().warn (
									"Unvalid Dialback " + dialbackValue + " expected "
													+ local2remoteSession.getDialbackValue ());
				}
			}
		}
	}

	//-------------------------------------------------------------------------
	public void processText (final IMSession session, final Object context) throws Exception
	{
		dialbackValue = session.getXmlPullParser ().getText ().trim ();
	}
}
