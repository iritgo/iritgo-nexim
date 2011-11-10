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

package de.iritgo.nexim.network;


import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;
import org.apache.mina.filter.codec.demux.MessageEncoder;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


public class XMLResponseEncoder implements MessageEncoder
{
	public static final Set TYPES;

	static
	{
		Set types = new HashSet();

		types.add(String.class);
		TYPES = Collections.unmodifiableSet(types);
	}

	private static final byte[] CRLF = new byte[]
	{
					0x0D, 0x0A
	};

	public XMLResponseEncoder()
	{
	}

	public void encode(IoSession session, Object message, ProtocolEncoderOutput out) throws Exception
	{
		String msg = (String) message;
		//		System.out.println ("S:" + msg);
		IoBuffer buf = IoBuffer.allocate(256);

		buf.setAutoExpand(true);

		try
		{
			CharsetEncoder encoder = Charset.forName("UTF-8").newEncoder();

			buf.putString(msg, encoder);
			buf.put(CRLF);
		}
		catch (CharacterCodingException ex)
		{
			ex.printStackTrace();
		}

		buf.flip();
		out.write(buf);
	}

	public Set getMessageTypes()
	{
		return TYPES;
	}
}
