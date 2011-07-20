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


import de.iritgo.nexim.session.DefaultSessionProcessor;
import de.iritgo.nexim.session.IMClientSession;
import de.iritgo.nexim.session.IMSession;
import de.iritgo.nexim.xmpp.IMIq;
import org.xmlpull.v1.XmlPullParser;


public class IqImpl extends DefaultSessionProcessor implements Iq
{
	@Override
	public void process (final IMSession session, final Object context) throws Exception
	{
		XmlPullParser xpp = session.getXmlPullParser ();

		for (int i = 0, l = xpp.getAttributeCount (); i < l; i++)
		{
			getLogger ().debug (
							"Attribut ns: " + xpp.getAttributeNamespace (i) + " name: " + xpp.getAttributeName (i)
											+ " value: " + xpp.getAttributeValue (i));
		}

		IMIq iq = new IMIq ();

		iq.setId (xpp.getAttributeValue ("", "id"));
		iq.setType (xpp.getAttributeValue ("", "type"));
		iq.setTo (xpp.getAttributeValue ("", "to"));
		iq.setFrom (xpp.getAttributeValue ("", "from"));
		getLogger ().debug ("Got IQ " + iq);

		super.process (session, iq);

		if (IMIq.TYPE_RESULT.equals (iq.getType ()))
		{
			iq.setFrom (((IMClientSession) session).getUser ().getJIDAndRessource ());
			session.getRouter ().route (session, iq);
		}
	}
}
