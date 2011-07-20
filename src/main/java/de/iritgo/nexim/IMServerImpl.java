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

package de.iritgo.nexim;


import de.iritgo.nexim.log.DefaultNeximLogger;
import de.iritgo.nexim.network.XMLProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.SocketSessionConfig;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import java.net.InetAddress;
import java.net.InetSocketAddress;


public class IMServerImpl implements IMServer
{
	/** The default nexim logger interface         */
	private DefaultNeximLogger defaultNeximLogger;

	private NioSocketAcceptor clientNioSocketAcceptor;

	private NioSocketAcceptor serverNioSocketAcceptor;

	// Requirements
	private ServerParameters serverParameters;

	// Autoconfigs
	@SuppressWarnings("unused")
	private int listenBacklog;

	private String bindAddress;

	/** Set the default nexim logger implementation         */
	public void setDefaultNeximLogger (DefaultNeximLogger defaultNeximLogger)
	{
		this.defaultNeximLogger = defaultNeximLogger;
	}

	public void setClientNioSocketAcceptor (NioSocketAcceptor nioSocketAcceptor)
	{
		this.clientNioSocketAcceptor = nioSocketAcceptor;
	}

	public void setServerNioSocketAcceptor (NioSocketAcceptor nioSocketAcceptor)
	{
		this.serverNioSocketAcceptor = nioSocketAcceptor;
	}

	public void setServerParameters (ServerParameters serverParameters)
	{
		this.serverParameters = serverParameters;
	}

	public void setListenBacklog (int listenBacklog)
	{
		this.listenBacklog = listenBacklog;
	}

	public void setBindAddress (String bindAddress)
	{
		this.bindAddress = bindAddress;
	}

	// -------------------------------------------------------------------------
	public void initialize ()
	{
		try
		{
			@SuppressWarnings("unused")
			InetAddress bindTo = null;

			if (bindAddress != null && bindAddress.length () > 0)
			{
				bindTo = InetAddress.getByName ("localhost");
			}
			SocketSessionConfig config = clientNioSocketAcceptor.getSessionConfig ();

			clientNioSocketAcceptor.getFilterChain ().addLast ("protocolFilter",
							new ProtocolCodecFilter (new XMLProtocolCodecFactory ()));
			clientNioSocketAcceptor.getFilterChain ().addLast ("logger", new LoggingFilter ());

			clientNioSocketAcceptor.bind (new InetSocketAddress (serverParameters.getLocalClientPort ()));

			serverNioSocketAcceptor.getFilterChain ().addLast ("protocolFilter",
							new ProtocolCodecFilter (new XMLProtocolCodecFactory ()));
			serverNioSocketAcceptor.getFilterChain ().addLast ("logger", new LoggingFilter ());

			serverNioSocketAcceptor.setCloseOnDeactivation (true);
			serverNioSocketAcceptor.bind (new InetSocketAddress (serverParameters.getLocalServerPort ()));
		}
		catch (Exception e)
		{
			defaultNeximLogger.error (e.getMessage (), e);
		}
	}

	// -------------------------------------------------------------------------
	public void dispose ()
	{
		defaultNeximLogger.debug ("Disposing Server");
	}

	public void shutdown ()
	{
		serverNioSocketAcceptor.unbind ();
	}
}
