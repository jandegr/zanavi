/*
 * ZANavi, Zoff Android Navigation system.
 * Copyright (C) 2011 Zoff <zoff@zoff.cc>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * version 2 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the
 * Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
 * Boston, MA  02110-1301, USA.
 */

/*
 * Navit, a modular navigation system.
 * Copyright (C) 2005-2008 Navit Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * version 2 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the
 * Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
 * Boston, MA  02110-1301, USA.
 */

package com.zoffcc.applications.zanavi;

import android.util.Log;

class NavitTimeout extends Thread
{
	private static final String TAG = "NavitTimeout";
	final boolean event_multi;
	private final long event_callbackid;
	private final int event_timeout;
	Boolean running;

	// public native void TimeoutCallback(int del, int id);

	NavitTimeout(int timeout, boolean multi, long callbackid)
	{
		event_timeout = timeout;
		event_multi = multi;
		event_callbackid = callbackid;
		running = true;
		this.start();
	}

	public void run()
	{
		while (running)
		{
			try
			{
				Thread.sleep(event_timeout, 0);
			}
			catch (InterruptedException e)
			{
				Log.e(TAG,"Interrupted " + e.getMessage());
			}

			if (running)
			{
				if (event_multi)
				{
					Navit.cwthr.TimeoutCallback2(this, 0, event_callbackid);
				}
				else
				{
					running = false;
					Navit.cwthr.TimeoutCallback2(this, 1, event_callbackid);
				}
			}
		}

		try
		{
			Thread.sleep(event_timeout, 1000); // sleep 1 secs. to wait for timeout remove call (in C code)
		}
		catch (InterruptedException e)
		{
			Log.e(TAG,"Interrupted " + e.getMessage());
		}
	}

	// ---------- !!! C code does NOT call remove anymore !!! ----------
	public void remove()
	{
		running = false;
		this.interrupt();
	}
}
