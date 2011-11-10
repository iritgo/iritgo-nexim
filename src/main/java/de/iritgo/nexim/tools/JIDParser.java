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

package de.iritgo.nexim.tools;


public class JIDParser
{
	//-------------------------------------------------------------------------
	public static final String getJID(final String jidAndRes)
	{
		return getName(jidAndRes) + '@' + getHostname(jidAndRes);
	}

	//-------------------------------------------------------------------------
	public static final String getHostname(final String jidAndRes)
	{
		String hostname = null;

		int index = jidAndRes.indexOf('@');

		if (index > 0)
		{
			hostname = jidAndRes.substring(index + 1);
			index = hostname.indexOf('/');

			if (index > 0)
			{
				hostname = hostname.substring(0, index);
			}

			hostname = hostname.toLowerCase();
		}

		return hostname;
	}

	//-------------------------------------------------------------------------
	public static final String getNameAndRessource(final String jidAndRes)
	{
		String nameAndRes = null;

		if (jidAndRes != null)
		{
			int index = jidAndRes.indexOf('@');

			if (index > 0)
			{
				nameAndRes = jidAndRes.substring(0, index).toLowerCase();
				index = jidAndRes.lastIndexOf('/');

				if (index > 0)
				{
					nameAndRes += jidAndRes.substring(index);
				}
			}
		}

		return nameAndRes;
	}

	//-------------------------------------------------------------------------
	public static final String getName(final String jidAndRes)
	{
		String name = jidAndRes;

		if (jidAndRes != null)
		{
			int index = jidAndRes.lastIndexOf('/');

			if (index > 0)
			{
				name = jidAndRes.substring(0, index);
			}

			index = name.indexOf('@');

			if (index > 0)
			{
				name = name.substring(0, index);
			}

			if (name != null)
			{
				name = name.toLowerCase();
			}
		}

		return name;
	}
}
