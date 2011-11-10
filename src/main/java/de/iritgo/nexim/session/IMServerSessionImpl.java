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

package de.iritgo.nexim.session;


import de.iritgo.nexim.ServerParameters;


public class IMServerSessionImpl extends AbstractIMSession implements IMServerSession
{
	private ServerParameters serverParameters;

	private String remoteHostname;

	private volatile boolean dialbackValid;

	private volatile String dialbackValue;

	private IMServerSession twinSession;

	public void setServerParameters(ServerParameters serverParameters)
	{
		this.serverParameters = serverParameters;
	}

	//-------------------------------------------------------------------------
	public void initialize()
	{
		dialbackValid = false;
		disposed = new Boolean(false);

		synchronized (lastSessionId)
		{
			sessionId = lastSessionId.longValue();
			lastSessionId = new Long(sessionId + 1);
		}
	}

	//-------------------------------------------------------------------------
	@SuppressWarnings("deprecation")
	public void close()
	{
		defaultNeximLogger.debug("Closing session id " + getId());

		synchronized (disposed)
		{
			dialbackValid = false;
			dialbackValue = null;

			try
			{
				if (twinSession != null)
				{
					twinSession.setTwinSession(null);
				}
			}
			catch (Exception e)
			{
				defaultNeximLogger.warn("Session dispose failed (stage1): " + e.getMessage(), e);
			}

			try
			{
				writeOutputStream("</stream:stream>");
			}
			catch (Exception e)
			{
				defaultNeximLogger.warn("Session dispose failed (stage2): " + e.getMessage());
			}

			try
			{
				defaultNeximLogger.debug("Session " + sessionId + " closed");

				if (ioSession != null && ioSession.isConnected())
				{
					ioSession.close();

					//TODO: Delete: outputStreamWriter.close ();
				}
			}
			catch (Exception e)
			{
				defaultNeximLogger.warn("Session dispose failed (stage3): " + e.getMessage(), e);
			}

			defaultNeximLogger.debug("Session " + sessionId + " disposed ");
		} // synchro

		disposed = new Boolean(true);
	}

	//-------------------------------------------------------------------------
	//-------------------------------------------------------------------------
	public boolean getDialbackValid()
	{
		return dialbackValid;
	}

	//-------------------------------------------------------------------------
	public void setDialbackValid(boolean value)
	{
		int ctype = getConnectionType();

		if (ctype == S2S_R2L_CONNECTION || ctype == S2S_L2R_CONNECTION)
		{
			dialbackValid = value;
		}
	}

	//-------------------------------------------------------------------------
	public String getDialbackValue()
	{
		return dialbackValue;
	}

	//-------------------------------------------------------------------------
	public void setDialbackValue(String dialback)
	{
		dialbackValue = dialback;
	}

	//-------------------------------------------------------------------------
	public IMServerSession getTwinSession()
	{
		return twinSession;
	}

	//-------------------------------------------------------------------------
	public void setTwinSession(IMServerSession session)
	{
		twinSession = session;
	}

	//-------------------------------------------------------------------------
	//-------------------------------------------------------------------------
	public final String getRemoteHostname()
	{
		return remoteHostname;
	}

	//-------------------------------------------------------------------------
	public final void setRemoteHostname(final String remoteHostname)
	{
		this.remoteHostname = remoteHostname;
	}

	//-------------------------------------------------------------------------
	public int getConnectionType()
	{
		@SuppressWarnings("unused")
		int type = UNKNOWN_CONNECTION;

		if (ioSession != null)
		{
			if (ioSession.getLocalAddress().equals(serverParameters.getLocalServerPort())
							|| ioSession.getLocalAddress().equals(serverParameters.getLocalSSLServerPort()))
			{
				type = S2S_R2L_CONNECTION;
			}
			else if (ioSession.getLocalAddress().equals(serverParameters.getRemoteServerPort()))
			{
				type = S2S_L2R_CONNECTION;
			}
		}

		return S2S_R2L_CONNECTION;
	}
}
