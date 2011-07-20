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


import de.iritgo.nexim.presence.IMPresence;
import de.iritgo.nexim.presence.IMPresenceHolder;
import de.iritgo.nexim.presence.IMPresenceImpl;
import de.iritgo.nexim.user.User;
import de.iritgo.nexim.user.UserManager;
import de.iritgo.nexim.user.roster.IMRosterItem;
import de.iritgo.nexim.user.roster.RosterItemProcessor;
import de.iritgo.nexim.user.roster.RosterManager;


public class IMClientSessionImpl extends AbstractIMSession implements IMClientSession
{
	// Locals
	private User user;

	private IMPresence presence;

	/* The user manager */
	@SuppressWarnings("unused")
	private UserManager userManager;

	private RosterManager rosterManager;

	// Requirements
	private IMPresenceHolder presenceHolder;

	public void setImPresenceHolder (IMPresenceHolder presenceHolder)
	{
		this.presenceHolder = presenceHolder;
	}

	/**
	 * @param rosterManager the rosterManager to set
	 */
	public void setRosterManager (RosterManager rosterManager)
	{
		this.rosterManager = rosterManager;
	}

	/**
	 * Set the user manager
	 */
	public void setUserManager (UserManager userManager)
	{
		this.userManager = userManager;
	}

	//-------------------------------------------------------------------------
	public void initialize ()
	{
		disposed = new Boolean (false);

		synchronized (lastSessionId)
		{
			sessionId = lastSessionId.longValue ();
			lastSessionId = new Long (sessionId + 1);
		}
	}

	//-------------------------------------------------------------------------
	public void close ()
	{
		defaultNeximLogger.debug ("Closing session id " + getId ());

		synchronized (disposed)
		{
			try
			{
				// set disconnected to all roster friend
				if (user != null && getConnectionType () == IMSession.C2S_CONNECTION)
				{
					presenceHolder.removePresence (user.getJIDAndRessource ());

					defaultNeximLogger.debug ("Remove presence jid " + user.getJIDAndRessource ());

					// emit unavailaible to all user
					final IMPresence presence = new IMPresenceImpl ();

					presence.setFrom (user.getJIDAndRessource ());
					presence.setType (IMPresence.TYPE_UNAVAILABLE);
					presence.setStatus ("Disconnected");

					final IMClientSession clientSession = this;

					rosterManager.processItems (user.getName (), new RosterItemProcessor ()
					{
						public void process (IMRosterItem item) throws Exception
						{
							defaultNeximLogger.debug ("Item " + item);

							IMPresence localPresence = (IMPresence) presence.clone ();

							localPresence.setTo (item.getJID ());

							if (router != null)
							{
								router.route (clientSession, localPresence);
							}
						}
					});
				}

				if (router != null)
				{
					router.unregisterSession (this);
				}
			}
			catch (Exception e)
			{
				defaultNeximLogger.warn ("Session dispose failed (stage1): " + e.getMessage (), e);
			}

			try
			{
				writeOutputStream ("</stream:stream>");
			}
			catch (Exception e)
			{
				defaultNeximLogger.warn ("Session dispose failed (stage2): " + e.getMessage ());
			}

			try
			{
				defaultNeximLogger.debug ("Session " + sessionId + " closed");

				/*
				                                if (socket != null && ! socket.isClosed ())
				                                {
				                                        socket.close ();
				                                        outputStreamWriter.close ();
				                                }
				 */
			}
			catch (Exception e)
			{
				defaultNeximLogger.warn ("Session dispose failed (stage3): " + e.getMessage (), e);
			}

			defaultNeximLogger.debug ("Session " + sessionId + " disposed ");
		}

		disposed = new Boolean (true);
	}

	//-------------------------------------------------------------------------
	//-------------------------------------------------------------------------
	//-------------------------------------------------------------------------
	public final void setUser (final User user)
	{
		this.user = user;
	}

	//-------------------------------------------------------------------------
	public final User getUser ()
	{
		return user;
	}

	//-------------------------------------------------------------------------
	public IMPresence getPresence ()
	{
		return presence;
	}

	//-------------------------------------------------------------------------
	public void setPresence (IMPresence presence)
	{
		this.presence = presence;
	}

	//-------------------------------------------------------------------------
	//-------------------------------------------------------------------------
	public int getConnectionType ()
	{
		return C2S_CONNECTION;
	}
}
