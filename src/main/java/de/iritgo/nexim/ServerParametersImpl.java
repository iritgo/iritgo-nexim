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


import java.net.InetAddress;
import java.util.List;


public class ServerParametersImpl implements ServerParameters
{
	private List<String> hostnames;

	private int localClientPort;

	private int localClientThreadPool;

	private int localSSLClientPort;

	private int localSSLClientThreadPool;

	private int localServerPort;

	private int localServerThreadPool;

	private int localSSLServerPort;

	private int localSSLServerThreadPool;

	private int remoteServerPort;

	//-------------------------------------------------------------------------
	public void initialize()
	{
		if (hostnames.size() == 0)
		{
			try
			{
				hostnames.add(InetAddress.getLocalHost().getHostName());
			}
			catch (java.net.UnknownHostException e)
			{
			}
		}
	}

	public void setHostnames(List<String> hostnames)
	{
		this.hostnames = hostnames;
	}

	public void setLocalClientPort(int localClientPort)
	{
		this.localClientPort = localClientPort;
	}

	public void setLocalClientThreadPool(int localClientThreadPool)
	{
		this.localClientThreadPool = localClientThreadPool;
	}

	public void setLocalSSLClientPort(int localSSLClientPort)
	{
		this.localSSLClientPort = localSSLClientPort;
	}

	public void setLocalSSLClientThreadPool(int localSSLClientThreadPool)
	{
		this.localSSLClientThreadPool = localSSLClientThreadPool;
	}

	public void setLocalServerPort(int localServerPort)
	{
		this.localServerPort = localServerPort;
	}

	public void setLocalServerThreadPool(int localServerThreadPool)
	{
		this.localServerThreadPool = localServerThreadPool;
	}

	public void setLocalSSLServerPort(int localSSLServerPort)
	{
		this.localSSLServerPort = localSSLServerPort;
	}

	public void setLocalSSLServerThreadPool(int localSSLServerThreadPool)
	{
		this.localSSLServerThreadPool = localSSLServerThreadPool;
	}

	public void setRemoteServerPort(int remoteServerPort)
	{
		this.remoteServerPort = remoteServerPort;
	}

	//-------------------------------------------------------------------------
	public final int getLocalClientPort()
	{
		return localClientPort;
	}

	//-------------------------------------------------------------------------
	public final int getLocalSSLClientPort()
	{
		return localSSLClientPort;
	}

	//-------------------------------------------------------------------------
	public final int getLocalServerPort()
	{
		return localServerPort;
	}

	//-------------------------------------------------------------------------
	public final int getLocalSSLServerPort()
	{
		return localSSLServerPort;
	}

	//-------------------------------------------------------------------------
	public final List getHostNameList()
	{
		return hostnames;
	}

	//-------------------------------------------------------------------------
	public final String getHostName()
	{
		return (String) hostnames.get(0);
	}

	//-------------------------------------------------------------------------
	public final int getRemoteServerPort()
	{
		return remoteServerPort;
	}

	//-------------------------------------------------------------------------
	public int getLocalClientThreadPool()
	{
		return localClientThreadPool;
	}

	//-------------------------------------------------------------------------
	public int getLocalSSLServerThreadPool()
	{
		return localSSLServerThreadPool;
	}

	//-------------------------------------------------------------------------
	public int getLocalServerThreadPool()
	{
		return localServerThreadPool;
	}

	//-------------------------------------------------------------------------
	public int getLocalSSLClientThreadPool()
	{
		return localSSLClientThreadPool;
	}
}
