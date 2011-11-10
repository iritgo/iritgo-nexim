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


import org.apache.commons.lang.StringUtils;


public class XMLToString
{
	private String m_elementName;

	private StringBuffer m_buffer;

	private StringBuffer m_innerBuffer;

	public XMLToString(String elementName)
	{
		m_buffer = new StringBuffer();
		m_buffer.append('<').append(elementName);
		m_elementName = elementName;
	}

	public void addAttribut(String name, String value)
	{
		if (name != null && name.length() > 0 && value != null)
		{
			m_buffer.append(' ').append(name).append("='").append(value).append("'");
		}
	}

	public void addFilledAttribut(String name, String value)
	{
		if (name != null && name.length() > 0 && value != null && value.length() > 0)
		{
			m_buffer.append(' ').append(name).append("='").append(value).append("'");
		}
	}

	public void addTextNode(String text)
	{
		if (text != null && text.length() > 0)
		{
			if (m_innerBuffer == null)
			{
				m_innerBuffer = new StringBuffer();
			}

			m_innerBuffer.append(convert(text));
		}
	}

	public void addStringElement(String stringElement)
	{
		if (stringElement != null)
		{
			if (m_innerBuffer == null)
			{
				m_innerBuffer = new StringBuffer();
			}

			m_innerBuffer.append(stringElement);
		}
	}

	public void addElement(XMLToString xmlToString)
	{
		if (xmlToString != null)
		{
			if (m_innerBuffer == null)
			{
				m_innerBuffer = new StringBuffer();
			}

			m_innerBuffer.append(xmlToString.toStringBuffer());
		}
	}

	@Override
	public String toString()
	{
		return toStringBuffer().toString();
	}

	public StringBuffer toStringBuffer()
	{
		StringBuffer buffer = new StringBuffer();

		if (m_innerBuffer != null)
		{
			buffer.append(m_buffer).append('>').append(m_innerBuffer).append("</").append(m_elementName).append('>');
		}
		else
		{
			buffer.append(m_buffer).append("/>");
		}

		return buffer;
	}

	// -----------------------------------------------------------------------
	// should be optimized...
	private static final String convert(String s)
	{
		s = StringUtils.replace(s, "&", "&amp;");
		s = StringUtils.replace(s, "<", "&lt;");
		s = StringUtils.replace(s, ">", "&gt;");

		return s;
	}
}
