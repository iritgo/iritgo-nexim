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

package de.iritgo.nexim.user;


public class User
{
	/** */
	private String name;

	/** */
	private String hostname;

	/** */
	private String password;

	/** */
	private String digest;

	/** */
	private String resource;

	/**
	 * @return
	 */
	public final String getName ()
	{
		return name;
	}

	/**
	 * @param name
	 */
	public final void setName (final String name)
	{
		this.name = name;
	}

	/**
	 * @return
	 */
	public final String getHostname ()
	{
		return hostname;
	}

	/**
	 * @param hostname
	 */
	public final void setHostname (final String hostname)
	{
		this.hostname = hostname;
	}

	/**
	 * @param password
	 */
	public final void setPassword (final String password)
	{
		this.password = password;
	}

	/**
	 * @return
	 */
	public final String getPassword ()
	{
		return password;
	}

	/**
	 * @return
	 */
	public final String getResource ()
	{
		return resource;
	}

	/**
	 * @return
	 */
	public final String getDigest ()
	{
		return digest;
	}

	/**
	 * @param digest
	 */
	public final void setDigest (final String digest)
	{
		this.digest = digest;
	}

	/**
	 * @param resource
	 */
	public final void setResource (final String resource)
	{
		this.resource = resource;
	}

	/**
	 * @return
	 */
	public final String getJID ()
	{
		String s = name;

		if (hostname != null)
		{
			s += "@" + hostname;
		}

		return s;
	}

	/**
	 * @return
	 */
	public final String getNameAndRessource ()
	{
		return name + "/" + resource;
	}

	/**
	 * @return
	 */
	public final String getJIDAndRessource ()
	{
		return getJID () + "/" + resource;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString ()
	{
		return getJIDAndRessource ();
	}
}
