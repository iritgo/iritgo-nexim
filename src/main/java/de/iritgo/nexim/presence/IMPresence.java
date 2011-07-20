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


import de.iritgo.nexim.xmpp.Transitable;


public interface IMPresence extends Transitable
{
	public static final String TYPE_AVAILABLE = "available";

	public static final String TYPE_UNAVAILABLE = "unavailable";

	public static final String TYPE_SUBSCRIBE = "subscribe";

	public static final String TYPE_SUBSCRIBED = "subscribed";

	public static final String TYPE_UNSUBSCRIBE = "unsubscribe";

	public static final String TYPE_UNSUBSCRIBED = "unsubscribed";

	public static final String TYPE_PROBE = "probe";

	public void setStatus (String status);

	public String getStatus ();

	public String getPriority ();

	public void setPriority (String priority);

	public void setShow (String show);

	public String getShow ();

	public Object clone ();
}
