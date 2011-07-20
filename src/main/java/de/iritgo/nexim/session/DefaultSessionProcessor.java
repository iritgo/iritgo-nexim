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
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;


public class DefaultSessionProcessor implements SessionProcessor
{
	/** The default nexim logger interface         */
	private DefaultNeximLogger defaultNeximLogger;

	private SessionProcessorRegistry sessionProcessorRegistry;

	/** Set the default nexim logger implementation         */
	public void setDefaultNeximLogger (DefaultNeximLogger defaultNeximLogger)
	{
		this.defaultNeximLogger = defaultNeximLogger;
	}

	public void setSessionProcessorRegistry (SessionProcessorRegistry sessionProcessorRegistry)
	{
		this.sessionProcessorRegistry = sessionProcessorRegistry;
	}

	protected DefaultNeximLogger getLogger ()
	{
		return defaultNeximLogger;
	}

	public void process (final IMSession session) throws Exception
	{
		process (session, null);
	}

	public void process (final IMSession session, final Object context) throws Exception
	{
		final XmlPullParser xpp = session.getXmlPullParser ();
		final String currentEventName = getEventName (session, xpp.getNamespace (), xpp.getName ());

		for (int eventType = xpp.next (); eventType != XmlPullParser.END_DOCUMENT; eventType = xpp.next ())
		{
			if (eventType == XmlPullParser.START_TAG)
			{
				processStartTag (session, context);
			}
			else if (eventType == XmlPullParser.TEXT)
			{
				processText (session, context);
			}
			else if (eventType == XmlPullParser.END_TAG)
			{
				if (currentEventName.equals (getEventName (session, xpp.getNamespace (), xpp.getName ())))
				{
					processEndTag (session, context);

					break;
				}
			}
		} // for
		//getLogger().debug( "END_DOCUMENT" );
	}

	//-------------------------------------------------------------------------
	public void processStartTag (final IMSession session, final Object context) throws Exception
	{
		final XmlPullParser xpp = session.getXmlPullParser ();
		final String eventName = getEventName (session, xpp.getNamespace (), xpp.getName ());

		getLogger ().debug ("[" + session.getId () + "] <" + eventName + ">");

		SessionProcessor processor = null;

		try
		{
			processor = (SessionProcessor) sessionProcessorRegistry.getProcessor (eventName);
		}
		catch (Exception e)
		{
			getLogger ().debug (e.getMessage (), e);
		}

		if (processor != null)
		{
			//getLogger().debug( "Got processor "+processor+" for " +roleName);
			try
			{
				processor.process (session, context);
			}
			finally
			{
				//m_serviceManager.release( processor );
			}
		}
		else
		{
			getLogger ().warn ("No processor for event: " + eventName + " in " + getClass ().getName ());
			/*
			 * TODO: Service unavailable
			                        <iq from='capulet.lit' to='juliet@capulet.lit/balcony' id='c2s1' type='error'>
			                          <ping xmlns='urn:xmpp:ping'/>
			                          <error type='cancel'>
			                            <service-unavailable xmlns='urn:ietf:params:xml:ns:xmpp-stanzas'/>
			                          </error>
			                        </iq>
			 */
			skip (xpp);
		}
	}

	/**
	 * @param context
	 */

	//-------------------------------------------------------------------------
	public void processEndTag (final IMSession session, final Object context) throws Exception
	{
		final XmlPullParser xpp = session.getXmlPullParser ();
		final String eventName = getEventName (session, xpp.getNamespace (), xpp.getName ());

		getLogger ().debug ("[" + session.getId () + "] </" + eventName + ">");
	}

	//-------------------------------------------------------------------------
	/**
	 * @param context
	 */
	public void processText (final IMSession session, final Object context) throws Exception
	{
		final String text = session.getXmlPullParser ().getText ().trim ();

		if (text.length () > 0)
		{
			getLogger ().debug ("[ " + text + " ]");
		}
	}

	//-------------------------------------------------------------------------
	protected void skip (final XmlPullParser xpp) throws XmlPullParserException, java.io.IOException
	{
		int eventType = xpp.getEventType ();

		if (eventType == XmlPullParser.START_TAG)
		{
			while (eventType != XmlPullParser.END_TAG)
			{
				eventType = xpp.next ();

				if (eventType == XmlPullParser.START_TAG)
				{
					skip (xpp);
				}
			}
		}
	}

	//-------------------------------------------------------------------------
	protected StringBuffer serialize (final XmlPullParser xpp) throws XmlPullParserException, java.io.IOException
	{
		StringBuffer sb = null;

		int eventType = xpp.getEventType ();

		if (eventType == XmlPullParser.START_TAG)
		{
			sb = getStartElementAsStringBuffer (xpp);

			String elementName = xpp.getName ();

			while (eventType != XmlPullParser.END_TAG)
			{
				eventType = xpp.next ();

				if (eventType == XmlPullParser.START_TAG)
				{
					sb.append (serialize (xpp));
				}
				else if (eventType == XmlPullParser.TEXT)
				{
					sb.append (xpp.getText ());
				}
			} // while

			sb.append ("</").append (elementName).append (">");
		}

		return sb;
	}

	//-------------------------------------------------------------------------
	protected String asString (final XmlPullParser xpp) throws XmlPullParserException
	{
		String s = null;
		int eventType = xpp.getEventType ();

		if (eventType == XmlPullParser.START_TAG)
		{
			s = getStartElementAsStringBuffer (xpp).toString ();
		}

		if (eventType == XmlPullParser.TEXT)
		{
			s = xpp.getText ();
		}

		if (eventType == XmlPullParser.END_TAG)
		{
			s = "</" + xpp.getName () + ">";
		}

		return s;
	}

	//-------------------------------------------------------------------------
	private StringBuffer getStartElementAsStringBuffer (final XmlPullParser xpp)
	{
		StringBuffer sb = new StringBuffer ();

		String elementName = xpp.getName ();
		String elementNamespace = xpp.getNamespace ();

		// no access to stream and its default namespce
		sb.append ("<").append (elementName);

		if (elementNamespace != null && elementNamespace.length () > 0)
		{
			sb.append (" xmlns='").append (elementNamespace).append ("'");
		}

		for (int i = 0, l = xpp.getAttributeCount (); i < l; i++)
		{
			String value = xpp.getAttributeValue (i);
			String name = xpp.getAttributeName (i);

			sb.append (" ").append (name).append ("='").append (value).append ("'");
		}

		sb.append (">");

		return sb;
	}

	protected String getEventName (final IMSession session, final String currentNamespace, final String name)
	{
		String ns = getNamespace (session, currentNamespace);

		ns = ns != null ? ns : "";

		return ns + ":" + name;
	}

	/**
	 * Get namespace, using the Streams namespace if current is null or empty string.
	 */
	protected String getNamespace (final IMSession session, String current)
	{
		String ns = current;

		if (current == null || current.length () == 0)
		{
			// try get the streams namespace
			ns = session.getNamespace ();
		} // end of if ()

		return ns;
	}
}
