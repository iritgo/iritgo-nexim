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

package de.iritgo.nexim.xmpp.jabber.iq.search;


import de.iritgo.nexim.ServerParameters;
import de.iritgo.nexim.session.DefaultSessionProcessor;
import de.iritgo.nexim.session.IMClientSession;
import de.iritgo.nexim.session.IMSession;
import de.iritgo.nexim.user.User;
import de.iritgo.nexim.user.UserDAO;
import de.iritgo.nexim.xmpp.IMIq;
import org.xmlpull.v1.XmlPullParser;
import java.util.List;


public class QueryImpl extends DefaultSessionProcessor implements Query
{
	private ServerParameters serverParameters;

	private UserDAO userDAO;

	public void setServerParameters(ServerParameters serverParameters)
	{
		this.serverParameters = serverParameters;
	}

	public void setUserDAO(UserDAO userDAO)
	{
		this.userDAO = userDAO;
	}

	//-------------------------------------------------------------------------
	@Override
	public void process(final IMSession session, final Object context) throws Exception
	{
		IMClientSession clientSession = (IMClientSession) session;
		String type = ((IMIq) context).getType();

		// GET
		if (IMIq.TYPE_GET.equals(type))
		{
			get(clientSession, context);
		}
		else if (IMIq.TYPE_SET.equals(type))
		{
			set(clientSession, context);
		}
	}

	//-------------------------------------------------------------------------
	private void get(final IMClientSession session, Object context) throws Exception
	{
		String iqId = ((IMIq) context).getId();

		String s = "<iq type='result'";

		s += " from='" + serverParameters.getHostName() + "'";
		s += " to='" + session.getUser().getJIDAndRessource() + "'";
		s += " id='" + iqId + "'";
		s += ">";
		s += "<query xmlns='jabber:iq:search'>";
		s += "<nick/>";
		/*
		 We will be able to search thru all these when we'll refactor user-manager lib
		 s += "<first/>";
		 s += "<last/>";
		 s += "<email/>";
		 */
		s += "<instructions>Fill in one or more fields to search for any matching Jabber users.</instructions>";
		s += "</query></iq>";

		session.writeOutputStream(s);
	}

	//-------------------------------------------------------------------------
	private void set(final IMClientSession session, final Object context) throws Exception
	{
		final XmlPullParser xpp = session.getXmlPullParser();
		String iqId = ((IMIq) context).getId();

		xpp.nextTag(); // <nick>

		String searchText = xpp.nextText();

		xpp.nextTag(); // </nick>

		getLogger().debug("Search for account name " + searchText);

		// maybe we should proceed to a search via the vcard...
		List<User> users = userDAO.getUsers(searchText);

		String s = "<iq type='result'";

		s += " from='" + serverParameters.getHostName() + "'";
		s += " to='" + session.getUser().getJIDAndRessource() + "'";
		s += " id='" + iqId + "'";
		s += ">";

		if (users.size() > 0L)
		{
			s += "<query xmlns='jabber:iq:search'>";

			for (int i = 0, l = users.size(); i < l; i++)
			{
				User user = users.get(i);

				s += "<item jid='" + user.getName() + '@' + serverParameters.getHostName() + "'>";
				s += "<first>" + user.getName() + "</first>";
				s += "<last>" + user.getName() + "</last>";
				s += "<nick>" + user.getName() + "</nick>";
				s += "<email>" + user.getName() + '@' + serverParameters.getHostName() + "</email>";
				s += "</item>";
			}

			s += "</query>";
		}
		else
		{
			s += "<query xmlns='jabber:iq:search'/>";
		}

		s += "</iq>";

		session.writeOutputStream(s);
	}
}
