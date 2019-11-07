/**
 * ZANavi, Zoff Android Navigation system.
 * Copyright (C) 2011-2016 Zoff <zoff@zoff.cc>
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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.util.Log;

public class ZANaviMainApplication extends Application
{
	static String last_stack_trace_as_string = "";
	static int i = 0;

	@Override
	public void onCreate()
	{
		System.out.println("ZANaviMainApp:" + "onCreate");
		super.onCreate();

		Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler()
		{
			@Override
			public void uncaughtException(Thread thread, Throwable e)
			{
				handleUncaughtException(thread, e);
			}
		});

	}

	private void save_error_msg()
	{
		PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext()).edit().putString("last_crash_text", last_stack_trace_as_string).apply();
		System.out.println("ZANaviMainApp:" + "save_error_msg=" + last_stack_trace_as_string);
	}

	static void restore_error_msg(Context c)
	{
		last_stack_trace_as_string = PreferenceManager.getDefaultSharedPreferences(c).getString("last_crash_text", "");
		System.out.println("ZANaviMainApp:" + "restore_error_msg=" + last_stack_trace_as_string);
	}

	private void handleUncaughtException(Thread thread, Throwable e)
	{
		last_stack_trace_as_string = e.getMessage();
		boolean stack_trace_ok = false;

		try
		{
			Writer writer = new StringWriter();
			PrintWriter printWriter = new PrintWriter(writer);
			e.printStackTrace(printWriter);
			last_stack_trace_as_string = writer.toString();

			System.out.println("ZANaviMainApp:" + "stack trace ok");
			stack_trace_ok = true;
		}
		catch (Exception ee)
		{
		}
		catch (OutOfMemoryError ex2)
		{
			System.out.println("ZANaviMainApp:" + "stack trace *error*");
		}

		if (!stack_trace_ok)
		{
			try
			{
				last_stack_trace_as_string = Log.getStackTraceString(e);
				System.out.println("ZANaviMainApp:" + "stack trace ok (addon 1)");
				stack_trace_ok = true;
			}
			catch (Exception ee)
			{
			}
			catch (OutOfMemoryError ex2)
			{
				System.out.println("ZANaviMainApp:" + "stack trace *error* (addon 1)");
			}
		}

		try
		{
			save_error_msg();
		}
		catch (Exception ee)
		{
		}
		catch (OutOfMemoryError ex2)
		{
		}

		// System.out.println("ZANaviMainApp:" + "handleUncaughtException" + " thread.name=" + thread.getName() + " thread.id=" + thread.getId() + " Ex=" + last_stack_trace_as_string.replace("\n", " xxx "));

		PendingIntent intent = PendingIntent.getActivity(getBaseContext(), 0, new Intent(getApplicationContext(), Navit.class), Intent.FLAG_ACTIVITY_NEW_TASK);

		AlarmManager mgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 2000, intent); // restart app after 2 second delay

		System.exit(2);
	}
}