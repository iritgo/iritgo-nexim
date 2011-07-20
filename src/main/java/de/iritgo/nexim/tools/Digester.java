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


import java.security.MessageDigest;


public class Digester
{
	// ===============================================================================
	/** quick array to convert byte values to hex codes */
	private final static char[] HEX =
	{
					'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
	};

	// -----------------------------------------------------------------------
	public static final String digest (long value)
	{
		return digest (Long.toString (value));
	}

	// -----------------------------------------------------------------------
	public static final String digest (String value)
	{
		String digest = null;

		try
		{
			MessageDigest messageDigest = MessageDigest.getInstance ("SHA1");

			digest = bytesToHex (messageDigest.digest (value.getBytes ()));
		}
		catch (Exception e)
		{
			e.printStackTrace ();
		}

		return digest;
	}

	/**
	 * This utility method is passed an array of bytes. It returns
	 * this array as a String in hexadecimal format. This is used
	 * internally by <code>digest()</code>. Data is returned in
	 * the format specified by the Jabber protocol.
	 */
	private static String bytesToHex (byte[] data)
	{
		StringBuffer retval = new StringBuffer ();

		for (int i = 0; i < data.length; i++)
		{
			retval.append (HEX[(data[i] >> 4) & 0x0F]);
			retval.append (HEX[data[i] & 0x0F]);
		}

		return retval.toString ();
	}
}
