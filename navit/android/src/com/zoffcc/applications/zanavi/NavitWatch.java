/**
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

import java.lang.Thread;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

class NavitWatch implements Runnable
{
	private final Thread thread;

	public Handler handler;
	/*
	 * private static Handler handler = new Handler()
	 * {
	 * public void handleMessage(Message m)
	 * {
	 * Log.e("NavitWatch", "Handler received message");
	 * }
	 * };
	 */

	private boolean removed;
	private final long watch_fd;
	private final int watch_cond;
	private final long watch_callbackid;
	private boolean callback_pending;
	private final Runnable callback_runnable;

	public native void poll(long fd, int cond);

	public native void WatchCallback(long id);

	NavitWatch(long fd, int cond, long callbackid)
	{
		Log.e("NavitWatch", "Creating new thread for " + fd + " " + cond + " from current thread " + java.lang.Thread.currentThread().getName());
		watch_fd = fd;
		watch_cond = cond;
		watch_callbackid = callbackid;
		final NavitWatch navitwatch = this;
		callback_runnable = new Runnable()
		{
			public void run()
			{
				navitwatch.callback();
			}
		};
		thread = new Thread(this, "poll thread");
		thread.start();
	}

	public void run()
	{
		Looper.prepare();
		handler = new Handler()
		{
			public void handleMessage(Message msg)
			{
				Log.e("Navit", "Handler received message");
			}
		};
		Looper.loop();

		for (;;)
		{
			Log.e("NavitWatch", "Polling " + watch_fd + " " + watch_cond + " from " + java.lang.Thread.currentThread().getName());
			poll(watch_fd, watch_cond);
			Log.e("NavitWatch", "poll returned");
			if (removed) break;
			callback_pending = true;
			handler.post(callback_runnable);
			try
			{
				Log.e("NavitWatch", "wait");
				synchronized (this)
				{
					if (callback_pending) this.wait();
				}
				Log.e("NavitWatch", "wait returned");
			}
			catch (Exception e)
			{
				Log.e("NavitWatch", "Exception " + e.getMessage());
			}
			if (removed) break;
		}
	}

	private void callback()
	{
		Log.e("NavitWatch", "Calling Callback");
		if (!removed) WatchCallback(watch_callbackid);
		synchronized (this)
		{
			callback_pending = false;
			Log.e("NavitWatch", "Waking up");
			this.notify();
		}
	}

	public void remove()
	{
		removed = true;
		thread.interrupt();
	}
}
