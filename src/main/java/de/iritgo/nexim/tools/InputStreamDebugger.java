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


import de.iritgo.nexim.log.DefaultNeximLogger;
import java.io.IOException;
import java.io.InputStream;


public class InputStreamDebugger extends InputStream
{
	//TODO: Service!!!
	private InputStream is;

	private long id;

	private DefaultNeximLogger logger;

	/** The default nexim logger interface         */
	@SuppressWarnings("unused")
	private DefaultNeximLogger defaultNeximLogger;

	public InputStreamDebugger(InputStream is, DefaultNeximLogger logger, long id)
	{
		this.is = is;
		this.logger = logger;
		this.id = id;
	}

	/** Set the default nexim logger implementation         */
	public void setDefaultNeximLogger(DefaultNeximLogger defaultNeximLogger)
	{
		this.defaultNeximLogger = defaultNeximLogger;
	}

	@Override
	public int available() throws IOException
	{
		return is.available();
	}

	@Override
	public void close() throws IOException
	{
		is.close();
	}

	@Override
	public void mark(int readlimit)
	{
		is.mark(readlimit);
	}

	@Override
	public boolean markSupported()
	{
		return is.markSupported();
	}

	@Override
	public int read() throws IOException
	{
		int b = is.read();

		logger.info("Input (" + id + "): " + new Character((char) b));

		return b;
	}

	@Override
	public int read(byte[] b) throws IOException
	{
		int i = is.read(b);

		if (i != - 1)
		{
			logger.info("Input (" + id + "): " + new String(b, 0, i));
		}

		return i;
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException
	{
		int i = is.read(b, off, len);

		if (i != - 1)
		{
			logger.info("Input (" + id + "): " + new String(b, off, i));
		}

		return i;
	}

	@Override
	public void reset() throws IOException
	{
		is.reset();
	}

	@Override
	public long skip(long n) throws IOException
	{
		return is.skip(n);
	}
}
