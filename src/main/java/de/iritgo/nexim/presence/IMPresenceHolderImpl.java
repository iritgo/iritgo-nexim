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

package de.iritgo.nexim.presence;


import de.iritgo.nexim.log.DefaultNeximLogger;
import de.iritgo.nexim.tools.JIDParser;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


public class IMPresenceHolderImpl implements IMPresenceHolder
{
	/** The default nexim logger interface         */
	@SuppressWarnings("unused")
	private DefaultNeximLogger defaultNeximLogger;

	Map<String, Map<String, IMPresence>> presenceMap = new HashMap<String, Map<String, IMPresence>>();

	/** Set the default nexim logger implementation         */
	public void setDefaultNeximLogger(DefaultNeximLogger defaultNeximLogger)
	{
		this.defaultNeximLogger = defaultNeximLogger;
	}

	public void setPresence(String jid, IMPresence presence)
	{
		synchronized (presenceMap)
		{
			String name = JIDParser.getName(jid);
			Map<String, IMPresence> map = presenceMap.get(name);

			if (map == null)
			{
				map = new HashMap<String, IMPresence>();
			}

			map.put(jid, presence);
			presenceMap.put(name, map);
		}
	}

	public Collection<IMPresence> getPresence(String jid)
	{
		Collection<IMPresence> col = null;

		synchronized (presenceMap)
		{
			String name = JIDParser.getName(jid);
			Map<String, IMPresence> map = presenceMap.get(name);

			if (map != null)
			{
				col = map.values();
			}
		}

		return col;
	}

	public IMPresence removePresence(String jid)
	{
		IMPresence presence = null;

		synchronized (presenceMap)
		{
			String name = JIDParser.getName(jid);
			Map<String, IMPresence> map = presenceMap.get(name);

			if (map != null)
			{
				presence = map.remove(jid);

				if (map.isEmpty())
				{
					presenceMap.remove(map);
				}
			}
		}

		return presence;
	}
}
