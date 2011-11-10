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

package de.iritgo.nexim.network;


import de.iritgo.nexim.IMConnectionHandler;
import de.iritgo.nexim.session.IMSession;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import java.io.IOException;
import java.net.ProtocolException;
import java.util.HashMap;


public class Server2ServerHandler extends IoHandlerAdapter
{
	private HashMap<IoSession, IMSession> sessionMapping = new HashMap<IoSession, IMSession>();

	private IMConnectionHandler imConnectionHandler;

	public void setImConnectionHandler(IMConnectionHandler imConnectionHandler)
	{
		this.imConnectionHandler = imConnectionHandler;
	}

	@Override
	public void sessionOpened(IoSession session)
	{
		sessionMapping.put(session, imConnectionHandler.sessionOpened(session, false));

		imConnectionHandler.sessionOpened(session, false);
	}

	@Override
	public void messageReceived(IoSession session, Object xmlMessage)
	{
		IMSession imSession = sessionMapping.get(session);

		if (xmlMessage instanceof WelcomeMessage)
		{
			@SuppressWarnings("unused")
			WelcomeMessage message = (WelcomeMessage) xmlMessage;

			try
			{
				imConnectionHandler.handleEncodingHandshake(imSession);
			}
			catch (ProtocolException e)
			{
				e.printStackTrace();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}

			return;
		}

		imConnectionHandler.process((String) xmlMessage, imSession);
	}

	@Override
	public void sessionIdle(IoSession session, IdleStatus status)
	{
		//        SessionLog.info(session, "Disconnecting the idle.");
		//        session.close();
	}

	@Override
	@SuppressWarnings("deprecation")
	public void exceptionCaught(IoSession session, Throwable cause)
	{
		session.close();
	}
}
