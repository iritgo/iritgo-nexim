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

package de.iritgo.nexim.xmpp;


import de.iritgo.nexim.tools.XMLToString;
import de.iritgo.nexim.user.deferrable.Deferrable;


public class IMMessage implements Transitable, Deferrable
{
	private static final long serialVersionUID = 15L;

	public static final String TYPE_CHAT = "chat";

	private String to;

	private String from;

	private String type;

	private String subject;

	private String body;

	private String thread;

	private String error;

	private Integer errorCode;

	public final void setTo (String to)
	{
		this.to = to;
	}

	public final String getTo ()
	{
		return to;
	}

	public final void setFrom (String from)
	{
		this.from = from;
	}

	public final String getFrom ()
	{
		return from;
	}

	public final void setType (String type)
	{
		this.type = type;
	}

	public final String getType ()
	{
		return type;
	}

	public final void setSubject (String subject)
	{
		this.subject = subject;
	}

	public final String getSubject ()
	{
		return subject;
	}

	public final void setBody (String body)
	{
		this.body = body;
	}

	public final String getBody ()
	{
		return body;
	}

	public final void setThread (String thread)
	{
		this.thread = thread;
	}

	public final String getThread ()
	{
		return thread;
	}

	public final void setError (String error)
	{
		this.error = error;
	}

	public void setErrorCode (int errorCode)
	{
		this.errorCode = new Integer (errorCode);
	}

	@Override
	public String toString ()
	{
		XMLToString message = new XMLToString ("message");

		message.addFilledAttribut ("to", to);
		message.addFilledAttribut ("from", from);
		message.addFilledAttribut ("type", type);

		if (subject != null)
		{
			XMLToString subject = new XMLToString ("subject");

			subject.addTextNode (this.subject);
			message.addElement (subject);
		}

		if (body != null)
		{
			XMLToString body = new XMLToString ("body");

			body.addTextNode (this.body);
			message.addElement (body);
		}

		if (thread != null)
		{
			XMLToString thread = new XMLToString ("thread");

			thread.addTextNode (this.thread);
			message.addElement (thread);
		}

		if (error != null)
		{
			XMLToString error = new XMLToString ("error");

			error.addTextNode (this.error);

			if (errorCode != null)
			{
				error.addFilledAttribut ("code", errorCode.toString ());
			}

			message.addElement (error);
		}

		return message.toString ();
	}
}
