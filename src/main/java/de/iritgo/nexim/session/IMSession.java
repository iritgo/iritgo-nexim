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


import de.iritgo.nexim.router.IMRouter;
import de.iritgo.nexim.xmpp.jabber.Streams;
import org.apache.mina.core.session.IoSession;
import org.xmlpull.v1.XmlPullParser;
import java.io.IOException;


public interface IMSession
{
	public static final int UNKNOWN_CONNECTION = 0;

	public static final int C2S_CONNECTION = 1;

	public static final int S2S_L2R_CONNECTION = 2;

	public static final int S2S_R2L_CONNECTION = 3;

	public void setup(IoSession ioSession) throws Exception;

	public void setDefaultEncoding(String defaultEncoding);

	public boolean isClosed();

	public void close();

	public long getId();

	public XmlPullParser getXmlPullParser();

	public int getConnectionType();

	public void writeOutputStream(String s) throws IOException;

	public String getEncoding();

	public void setImRouter(IMRouter router);

	public IMRouter getRouter();

	public void setStreams(Streams streams);

	public Streams getStreams();

	public void setNamespace(String namespace);

	public String getNamespace();
}
