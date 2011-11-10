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


import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import de.iritgo.nexim.log.DefaultNeximLogger;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class XStreamStore
{
	public static final String DEFAULT_ENCODING = "UTF-8";

	private File file;

	private XStream xstream;

	private DefaultNeximLogger logger;

	private ConcurrentHashMap map;

	private String substituteFrom;

	private String substituteTo;

	private String xmlProlog;

	public XStreamStore(File file, DefaultNeximLogger logger)
	{
		this(file, logger, DEFAULT_ENCODING);
	}

	public XStreamStore(File file, DefaultNeximLogger logger, String encoding)
	{
		this.file = file;
		this.logger = logger;
		xstream = new XStream(new DomDriver());

		String enc = encoding != null ? encoding : DEFAULT_ENCODING;

		xmlProlog = "<?xml version='1.0' encoding='" + enc + "'?>";
	}

	public void load()
	{
		map = loadMap();
	}

	//  --------------------------------------------------------------------------
	public void alias(String name, Class classz)
	{
		xstream.alias(name, classz);
	}

	//  --------------------------------------------------------------------------
	public void substitute(String from, String to)
	{
		substituteFrom = from;
		substituteTo = to;
	}

	//  --------------------------------------------------------------------------
	public Object get(Object key)
	{
		if (map == null)
		{
			map = loadMap();
		}

		Object value = null;

		synchronized (map)
		{
			value = map.get(key);
		}

		return value;
	}

	//  --------------------------------------------------------------------------
	public Object remove(Object key)
	{
		Object value = null;

		synchronized (map)
		{
			value = map.remove(key);
			saveMap(map);
		}

		return value;
	}

	//  --------------------------------------------------------------------------
	@SuppressWarnings("unchecked")
	public void put(Object key, Object value)
	{
		if (map == null)
		{
			map = loadMap();
		}

		synchronized (map)
		{
			value = map.put(key, value);
			saveMap(map);
		}
	}

	private DefaultNeximLogger getLogger()
	{
		return logger;
	}

	public void save()
	{
		synchronized (map)
		{
			saveMap(map);
		}
	}

	private void saveMap(Map map)
	{
		String xstreamData = xstream.toXML(map);

		if (substituteFrom != null && substituteTo != null)
		{
			xstreamData = StringUtils.replace(xstreamData, substituteFrom, substituteTo);
		}

		xstreamData = xmlProlog + "\n" + xstreamData;

		//getLogger().info("saving roster " + xstreamData);
		FileOutputStream fos = null;

		try
		{
			fos = new FileOutputStream(file);
			fos.write(xstreamData.getBytes());
		}
		catch (IOException e)
		{
			getLogger().error(e.getMessage(), e);
		}
		finally
		{
			if (fos != null)
			{
				try
				{
					fos.close();
				}
				catch (IOException e)
				{
					getLogger().error(e.getMessage());
				}
			}
		}
	}

	//--------------------------------------------------------------------------
	private ConcurrentHashMap loadMap()
	{
		ConcurrentHashMap map = null;

		if (file.exists())
		{
			try
			{
				FileInputStream fis = new FileInputStream(file);
				String xmlData = IOUtils.toString(fis);

				fis.close();

				if (substituteFrom != null && substituteTo != null)
				{
					xmlData = StringUtils.replace(xmlData, substituteTo, substituteFrom);
				}

				map = new ConcurrentHashMap((Map) xstream.fromXML(xmlData));
			}
			catch (Exception e)
			{
				getLogger().error(e.getMessage(), e);
			}
		}
		else
		{
			System.out.println("No " + file + " => starting with void store");
			map = new ConcurrentHashMap();
		}

		return map;
	}
}
