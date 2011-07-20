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

package de.iritgo.nexim.xmpp.jabber;


import de.iritgo.nexim.ServerParameters;
import de.iritgo.nexim.session.DefaultSessionProcessor;
import de.iritgo.nexim.session.IMServerSession;
import de.iritgo.nexim.session.IMSession;
import org.xmlpull.v1.XmlPullParser;


public class StreamsImpl extends DefaultSessionProcessor implements Streams
{
	// Requerements
	protected ServerParameters serverParameters;

	// Locals
	protected String namespace;

	public void setServerParameters (ServerParameters serverParameters)
	{
		this.serverParameters = serverParameters;
	}

	@Override
	public void process (final IMSession session, final Object context) throws Exception
	{
		final XmlPullParser xpp = session.getXmlPullParser ();

		namespace = xpp.getNamespace (null);

		session.setStreams (this);

		processAttribute (session, context);

		if (session instanceof IMServerSession)
		{
			getLogger ().info (
							"Start stream " + ((IMServerSession) session).getRemoteHostname () + " id "
											+ session.getId ());
		}

		//		super.process (session, context);
		if (session instanceof IMServerSession)
		{
			getLogger ().info (
							"Stop stream " + ((IMServerSession) session).getRemoteHostname () + " id "
											+ session.getId ());
		}
	}

	//-------------------------------------------------------------------------
	/**
	 * @param context
	 */
	public void processAttribute (final IMSession session, final Object context) throws Exception
	{
		final XmlPullParser xpp = session.getXmlPullParser ();
		String to = xpp.getAttributeValue ("", "to");
		String from = xpp.getAttributeValue ("", "from");

		if (from == null || from.length () == 0)
		{
			getLogger ().debug ("from attribut not specified in stream declaration");
		}
		else
		{
			if (session instanceof IMServerSession)
			{
				((IMServerSession) session).setRemoteHostname (from);
			}
		}

		if (session.getConnectionType () == IMSession.S2S_L2R_CONNECTION)
		{
			getLogger ().debug ("Local to Remote connection " + to);
		}
		else
		{
			String s = "<stream:stream xmlns:stream='http://etherx.jabber.org/streams' " + "id='" + session.getId ()
							+ "' ";

			if (session.getConnectionType () == IMSession.C2S_CONNECTION)
			{
				s += "xmlns='jabber:client' ";
			}
			else if (session.getConnectionType () == IMSession.S2S_R2L_CONNECTION)
			{
				s += "xmlns='jabber:server' xmlns:db='jabber:server:dialback' ";
			}

			s += "from='" + serverParameters.getHostName () + "'>";
			session.writeOutputStream (s);
		}
	}

	public String getNamespace ()
	{
		return namespace;
	}
}
