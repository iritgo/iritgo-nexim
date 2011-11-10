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


import de.iritgo.nexim.log.DefaultNeximLogger;
import de.iritgo.nexim.router.IMRouter;
import de.iritgo.nexim.xmpp.jabber.Streams;
import org.apache.mina.core.session.IoSession;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;
import java.io.OutputStreamWriter;


public abstract class AbstractIMSession implements IMSession
{
	protected static Long lastSessionId = new Long(System.currentTimeMillis());

	protected String defaultEncoding;

	protected OutputStreamWriter outputStreamWriter;

	private XmlPullParser xpp;

	protected IoSession ioSession;

	@SuppressWarnings("unused")
	private String encoding;

	protected IMRouter router;

	protected volatile Boolean disposed;

	protected long sessionId;

	protected Streams streams;

	protected String namespace;

	/** The default nexim logger interface         */
	protected DefaultNeximLogger defaultNeximLogger;

	/** Set the default nexim logger implementation         */
	public void setDefaultNeximLogger(DefaultNeximLogger defaultNeximLogger)
	{
		this.defaultNeximLogger = defaultNeximLogger;
	}

	public void setDefaultEncoding(String defaultEncoding)
	{
		this.defaultEncoding = defaultEncoding;
	}

	public final IMRouter getRouter()
	{
		return router;
	}

	public final void setImRouter(IMRouter router)
	{
		this.router = router;
	}

	public void setStreams(Streams streams)
	{
		this.streams = streams;
	}

	public Streams getStreams()
	{
		return streams;
	}

	//-------------------------------------------------------------------------
	public boolean isClosed()
	{
		boolean value = false;

		if (disposed != null)
		{
			synchronized (disposed)
			{
				value = disposed.booleanValue();
			}
		}

		return value;
	}

	//-------------------------------------------------------------------------
	public void setup(final IoSession ioSession) throws Exception
	{
		this.ioSession = ioSession;

		XmlPullParserFactory factory = XmlPullParserFactory.newInstance();

		factory.setNamespaceAware(true);
		xpp = factory.newPullParser();

		// to be checked -- getInputEncoding should detect encoding (if parser impl do so)
		/*m_encoding = m_xpp.getInputEncoding();
		 if( m_encoding == null || m_encoding.length() == 0 ){
		 m_encoding = m_defaultEncoding;
		 }
		 */

		//DataInputStream is = new DataInputStream( new de.iritgo.nexim.tools.InputStreamDebugger( socket.getInputStream(), getLogger() ) );
		/*
		                DataInputStream is = new DataInputStream(socket.getInputStream ());

		                InputStreamReader inputStreamReader = new InputStreamReader(is, defaultEncoding);

		                //InputStreamReader inputStreamReader = new InputStreamReader( new de.iritgo.nexim.tools.InputStreamDebugger( is, getLogger(), m_sessionId ) , m_defaultEncoding );
		                xpp.setInput (inputStreamReader);

		                DataOutputStream os = new DataOutputStream(socket.getOutputStream ());

		                outputStreamWriter = new OutputStreamWriter(os, defaultEncoding);

		                defaultNeximLogger.debug ("Starting session: " + sessionId + " with encoding " + encoding);
		 */
	}

	//-------------------------------------------------------------------------
	public final XmlPullParser getXmlPullParser()
	{
		return xpp;
	}

	//-------------------------------------------------------------------------
	public final long getId()
	{
		return sessionId;
	}

	//-------------------------------------------------------------------------
	public final String getEncoding()
	{
		return defaultEncoding;
	}

	//-------------------------------------------------------------------------
	public final void writeOutputStream(final String s) //throws IOException
	{
		defaultNeximLogger.debug("Output (" + sessionId + "/" + getConnectionType() + "): " + s);

		if (s != null && ioSession != null)
		{
			if (ioSession.isConnected())
			{
				try
				{
					synchronized (ioSession)
					{
						ioSession.write(s);
					}
				}
				catch (Exception e)
				{
					defaultNeximLogger.warn("Unable to send data: " + e.getMessage());
				}
			}
			else
			{
				defaultNeximLogger.warn("Unable to send data: Output socket closed or not connected");
			}
		}
	}

	//-------------------------------------------------------------------------
	@Override
	public final String toString()
	{
		return "I: " + getId();
	}

	//-------------------------------------------------------------------------
	@Override
	public final int hashCode()
	{
		Long sessionL = new Long(sessionId);

		return sessionL.hashCode();
	}

	//-------------------------------------------------------------------------
	// implementer to make PMD happy ,)
	@Override
	public boolean equals(final Object obj)
	{
		boolean result = false;

		if (obj instanceof IMSession)
		{
			result = obj == this;
		}

		return result;
	}

	public void setNamespace(String namespace)
	{
		this.namespace = namespace;
	}

	public String getNamespace()
	{
		return namespace;
	}
}
