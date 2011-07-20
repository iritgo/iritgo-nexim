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

package de.iritgo.nexim.user;


import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import de.iritgo.nexim.log.DefaultNeximLogger;
import de.iritgo.nexim.tools.Digester;
import org.apache.commons.io.IOUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 *
 */
public class UserDAOImpl implements UserDAO
{
	/** */
	private Map<String, User> users;

	/** */
	private File storeFile;

	/** */
	private XStream xstream;

	/** */
	private String filename = "user.txt";

	/** */
	private boolean regexpSearch;

	/** The default neXim logger interface */
	private DefaultNeximLogger defaultNeximLogger;

	/**
	 * Set the default neXim logger implementation.
	 *
	 * @param defaultNeximLogger The logger to set
	 */
	public void setDefaultNeximLogger (DefaultNeximLogger defaultNeximLogger)
	{
		this.defaultNeximLogger = defaultNeximLogger;
	}

	/**
	 * @param filename
	 */
	public void setFilename (String filename)
	{
		this.filename = filename;
	}

	/**
	 * @param regexpSearch
	 */
	public void setRegexpSearch (boolean regexpSearch)
	{
		this.regexpSearch = regexpSearch;
	}

	/**
	 *
	 */
	public void initialize ()
	{
		storeFile = new File (filename);
		defaultNeximLogger.info ("Using user database file: " + storeFile);

		if (! storeFile.exists ())
		{
			storeFile.getParentFile ().mkdirs ();
		}

		xstream = new XStream (new DomDriver ());
		xstream.alias ("user", User.class);
		users = loadMap (storeFile);
	}

	/**
	 * @see de.iritgo.nexim.user.UserDAO#getUser(java.lang.String)
	 */
	public User getUser (String username)
	{
		if (users == null)
		{
			try
			{
				initialize ();
			}
			catch (Exception x)
			{
				x.printStackTrace ();
			}
		}

		User user = null;

		try
		{
			if (username != null && username.length () > 0)
			{
				synchronized (users)
				{
					if (users.containsKey (username))
					{
						user = (User) users.get (username);
					}
				}
			}
		}
		catch (Exception x)
		{
			defaultNeximLogger.error (x.getMessage (), x);
			user = null;
		}

		if (user == null)
		{
			defaultNeximLogger.warn ("User " + username + " not found");
		}

		return user;
	}

	/**
	 * @see de.iritgo.nexim.user.UserDAO#getUsers(java.lang.String)
	 */
	public List<User> getUsers (String searchPattern)
	{
		List<User> list = new ArrayList<User> ();

		if (! regexpSearch)
		{
			searchPattern = searchPattern.replaceAll ("\\*", ".*");
		}

		try
		{
			synchronized (users)
			{
				Iterator<User> iter = users.values ().iterator ();

				while (iter.hasNext ())
				{
					String name = iter.next ().toString ();

					if (name.matches (searchPattern))
					{
						User user = (User) users.get (name);

						list.add (user);
					}
				}
			}
		}
		catch (Exception x)
		{
			defaultNeximLogger.error (x.getMessage (), x);
		}

		return list;
	}

	/**
	 * @see de.iritgo.nexim.user.UserDAO#removeUser(java.lang.String)
	 */
	public User removeUser (String username)
	{
		User user = null;

		synchronized (users)
		{
			user = (User) users.remove (username);

			if (user != null)
			{
				saveMap (storeFile, users);
			}
			else
			{
				defaultNeximLogger.warn ("User " + username + " not found");
			}
		}

		return user;
	}

	/**
	 * @see de.iritgo.nexim.user.UserDAO#addUser(de.iritgo.nexim.user.User)
	 */
	public void addUser (User newUser)
	{
		User user = new User ();

		// TODO: The lowercase things!
		user.setName (newUser.getName ().toLowerCase ());
		user.setPassword (newUser.getPassword ());

		defaultNeximLogger.info ("Setting account in repository " + user);

		synchronized (users)
		{
			users.put (user.getName (), user);
			saveMap (storeFile, users);
		}
	}

	/**
	 * @return
	 */
	public List<User> getUsers ()
	{
		List<User> list = new ArrayList<User> ();

		synchronized (users)
		{
			Iterator<User> iter = users.values ().iterator ();

			while (iter.hasNext ())
			{
				User o = (User) iter.next ();

				defaultNeximLogger.debug ("Item " + o + " account " + getUser (o.toString ()));
				list.add (o);
			}
		}

		return list;
	}

	/**
	 * @param file
	 * @param map
	 */
	private void saveMap (File file, Map<String, User> map)
	{
		String xstreamData = xstream.toXML (map);
		FileOutputStream fos = null;

		try
		{
			fos = new FileOutputStream (file);
			fos.write (xstreamData.getBytes ());
		}
		catch (IOException e)
		{
			defaultNeximLogger.error (e.getMessage (), e);
		}
		finally
		{
			if (fos != null)
			{
				try
				{
					fos.close ();
				}
				catch (IOException e)
				{
					defaultNeximLogger.error (e.getMessage ());
				}
			}
		}
	}

	/**
	 * @param file
	 * @return
	 */
	private Map<String, User> loadMap (File file)
	{
		Map<String, User> map = null;

		if (file.exists ())
		{
			try
			{
				FileInputStream fis = new FileInputStream (file);
				String xmlData = IOUtils.toString (fis);

				fis.close ();
				map = (Map<String, User>) xstream.fromXML (xmlData);
			}
			catch (Exception e)
			{
				defaultNeximLogger.error (e.getMessage (), e);
			}
		}
		else
		{
			map = new HashMap<String, User> ();
		}

		return map;
	}

	/**
	 * @see de.iritgo.nexim.user.UserDAO#isValidPassword(de.iritgo.nexim.user.User, de.iritgo.nexim.user.UserManager.AuthenticationType, java.lang.String, java.lang.String)
	 */
	public boolean isValidPassword (User user, UserManager.AuthenticationType type, String password, String sessionId)
	{
		// All passwords are correct!
		return true;

		//		switch (type)
		//		{
		//			case PLAIN:
		//				return password.equals (password);
		//
		//			case DIGEST:
		//				return Digester.digest (sessionId + password).equals (password);
		//
		//			default:
		//				return false;
		//		}
	}
}
