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
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.apache.mina.filter.codec.demux.MessageDecoderAdapter;
import org.apache.mina.filter.codec.demux.MessageDecoderResult;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class XMLRequestDecoder extends MessageDecoderAdapter
{
	private static Pattern noBodyTagPattern = Pattern.compile ("\\A\\s*(<\\w+[^<]*?/>)");

	private static Pattern startTagPattern = Pattern.compile ("\\A\\s*<(\\w+)[^>]*>");

	private int contentLength;

	private CharsetDecoder decoder = Charset.forName ("UTF-8").newDecoder ();

	public XMLRequestDecoder ()
	{
	}

	public MessageDecoderResult decodable (IoSession session, IoBuffer in)
	{
		try
		{
			String inc = in.getString (in.remaining (), decoder);

			if (inc.indexOf ("stream:stream") >= 0)
			{
				contentLength = inc.length ();

				return MessageDecoderResult.OK;
			}

			Matcher matcher = noBodyTagPattern.matcher (inc);

			if (matcher.find ())
			{
				int end = matcher.end ();

				contentLength = inc.substring (0, end).getBytes ().length;

				return MessageDecoderResult.OK;
			}

			matcher = startTagPattern.matcher (inc);

			if (matcher.find ())
			{
				String tag = matcher.group (1);
				Pattern stopTagPattern = Pattern.compile ("</" + tag + ">");

				matcher = stopTagPattern.matcher (inc);

				if (matcher.find ())
				{
					int end = matcher.end ();

					contentLength = inc.substring (0, end).getBytes ().length;

					return MessageDecoderResult.OK;
				}

				return MessageDecoderResult.NEED_DATA;
			}
		}
		catch (Exception x)
		{
		}

		return MessageDecoderResult.NEED_DATA;
	}

	public MessageDecoderResult decode (IoSession session, IoBuffer in, ProtocolDecoderOutput out) throws Exception
	{
		try
		{
			String inc = in.getString (contentLength, decoder);

			if (inc.indexOf ("stream:stream") >= 0)
			{
				WelcomeMessage welcomeMessage = new WelcomeMessage ();

				welcomeMessage.message = inc;
				out.write (welcomeMessage);
			}
			else
			{
				//				System.out.println ("--------------Start------------------");
				//				System.out.println (inc);
				//				System.out.println ("--------------End------------------");
				out.write (inc);
			}

			return MessageDecoderResult.OK;
		}
		catch (CharacterCodingException e)
		{
			e.printStackTrace ();
		}

		return MessageDecoderResult.NOT_OK;
	}
}
