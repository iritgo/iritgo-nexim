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

package de.iritgo.nexim.xmpp.jabber.iq.vcardtemp;


import de.iritgo.nexim.ServerParameters;
import de.iritgo.nexim.session.DefaultSessionProcessor;
import de.iritgo.nexim.session.IMClientSession;
import de.iritgo.nexim.session.IMSession;
import de.iritgo.nexim.tools.JIDParser;
import de.iritgo.nexim.user.privat.PrivateDataManager;
import de.iritgo.nexim.xmpp.IMIq;
import org.xmlpull.v1.XmlPullParser;


public class VCardImpl extends DefaultSessionProcessor implements VCard
{
	private ServerParameters serverParameters;

	private PrivateDataManager privateDataManager;

	public void setServerParameters (ServerParameters serverParameters)
	{
		this.serverParameters = serverParameters;
	}

	public void setPrivateDataManager (PrivateDataManager privateDataManager)
	{
		this.privateDataManager = privateDataManager;
	}

	//-------------------------------------------------------------------------
	@Override
	public void process (final IMSession session, final Object context) throws Exception
	{
		String type = ((IMIq) context).getType ();

		// GET
		if (IMIq.TYPE_GET.equals (type))
		{
			get (session, context);
		}
		else if (IMIq.TYPE_SET.equals (type))
		{
			set ((IMClientSession) session, context);
		}
		else if (IMIq.TYPE_RESULT.equals (type))
		{
			result (session, context);
		}
	}

	//-------------------------------------------------------------------------
	private void get (final IMSession session, Object context) throws Exception
	{
		final XmlPullParser xpp = session.getXmlPullParser ();
		final String vcardname = xpp.getNamespace () + ':' + xpp.getName ();

		String iqId = ((IMIq) context).getId ();
		String to = ((IMIq) context).getTo ();
		String from = ((IMIq) context).getFrom ();

		if (to == null || to.length () == 0)
		{
			to = ((IMClientSession) session).getUser ().getJID ();
		}

		if (from == null || from.length () == 0)
		{
			from = ((IMClientSession) session).getUser ().getJID ();
		}

		IMIq iq = null;

		if (serverParameters.getHostNameList ().contains (JIDParser.getHostname (to)))
		{
			String data = privateDataManager.getData (to, vcardname.toLowerCase ());

			if (data == null)
			{
				data = "<vCard xmlns='vcard-temp'/>";
			}

			getLogger ().debug ("Get " + to + "/" + vcardname + " vcard: " + data);

			// local request
			iq = new IMIq ();
			iq.setFrom (to);
			iq.setTo (from);
			iq.setId (iqId);
			iq.setType (IMIq.TYPE_RESULT);
			iq.setStringData (data);
		}
		else
		{
			iq = new IMIq ();
			iq.setFrom (from);
			iq.setTo (to);
			iq.setId (iqId);
			iq.setType (IMIq.TYPE_GET);
			iq.setStringData ("<vCard xmlns='vcard-temp'/>");
		}

		session.getRouter ().route (session, iq);

		skip (xpp);
	}

	//-------------------------------------------------------------------------
	private void set (final IMClientSession session, final Object context) throws Exception
	{
		final XmlPullParser xpp = session.getXmlPullParser ();

		String vcardname = xpp.getNamespace () + ':' + xpp.getName ();

		String data = serialize (xpp).toString ();

		getLogger ().debug ("Set " + session.getUser ().getJID () + "/" + vcardname + " vcard: " + data);

		if (data != null)
		{
			privateDataManager.setData (session.getUser ().getJID (), vcardname.toLowerCase (), data);
		}

		IMIq iq = (IMIq) context;
		String iqId = iq.getId ();
		String to = iq.getTo ();
		String from = iq.getFrom ();

		if (to == null)
		{
			to = session.getUser ().getJID ();
		}

		if (from == null)
		{
			to = session.getUser ().getJID ();
		}

		String s = "<iq type='result'";

		s += " from='" + to + "'";
		s += " to='" + from + "'";
		s += " id='" + iqId + "'/>";

		getLogger ().info ("-------->" + s);
		session.writeOutputStream (s);
	}

	//-------------------------------------------------------------------------
	private void result (final IMSession session, final Object context) throws Exception
	{
		final XmlPullParser xpp = session.getXmlPullParser ();
		String to = ((IMIq) context).getTo ();

		if (serverParameters.getHostNameList ().contains (JIDParser.getHostname (to)))
		{
			// local request
			String iqId = ((IMIq) context).getId ();
			String from = ((IMIq) context).getFrom ();
			String data = serialize (xpp).toString ();

			IMIq iq = new IMIq ();

			iq.setFrom (from);
			iq.setTo (to);
			iq.setId (iqId);
			iq.setType (IMIq.TYPE_RESULT);
			iq.setStringData (data);
			session.getRouter ().route (session, iq);
		}

		else
		{
			getLogger ().warn ("Abnormal result for remote delivery?");
			skip (xpp);
		}
	}
}
