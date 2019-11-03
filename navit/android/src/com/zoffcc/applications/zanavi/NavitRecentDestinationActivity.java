/**
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

package com.zoffcc.applications.zanavi;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;

import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.widget.Toolbar.LayoutParams;

import com.retain.dialog.RenameDialog;
import com.retain.dialog.RenameHandlerInterface;
import com.zoffcc.applications.zanavi.Navit.Navit_Point_on_Map;

import java.util.ArrayList;

public class NavitRecentDestinationActivity extends ListActivity
{
	private int selected_id = -1;
	private int my_id = 0;
	private String[] context_items = null;
	static Navit_Point_on_Map t = null;
	static int t_position = -1;
	static int t_size = -1;
	static Boolean refresh_items = false;
	static NavitRecentDestinationActivity my = null;
	private static ArrayList<String> listview_items = new ArrayList<String>();
	private static ArrayList<String> listview_addons = new ArrayList<String>();

	@Override
	protected void onPostCreate(Bundle savedInstanceState)
	{
		super.onPostCreate(savedInstanceState);
		Toolbar bar;

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
		{
			ViewGroup root_view = (ViewGroup) findViewById(my_id).getParent().getParent();

			bar = (Toolbar) LayoutInflater.from(this).inflate(R.layout.settings_toolbar, root_view, false);
			bar.setTitle(Navit.get_text("Recent destinations"));
			root_view.addView(bar, 0); // insert at top
		}
		else
		{
			ViewGroup root_view = (ViewGroup) findViewById(android.R.id.content);
			ListView content = (ListView) root_view.getChildAt(0);

			root_view.removeAllViews();

			LinearLayout ll = new LinearLayout(this);
			ll.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
			ll.setOrientation(LinearLayout.VERTICAL);

			bar = (Toolbar) LayoutInflater.from(this).inflate(R.layout.settings_toolbar, root_view, false);
			bar.setTitle(Navit.get_text("Recent destinations"));
			root_view.addView(ll);

			ll.addView(bar);
			ll.addView(content);
		}

		bar.setNavigationOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				finish();
			}
		});
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		Navit.applySharedTheme(this, Navit.p.PREF_current_theme);

		super.onCreate(savedInstanceState);
		overridePendingTransition(R.anim.pull_in_from_right, R.anim.hold);

		my = this;

		context_items = new String[] { Navit.get_text("delete Destination"), Navit.get_text("rename Destination"), Navit.get_text("set as Home Location"), Navit.get_text("show destination on map"), Navit.get_text("Use as destination") }; // TRANS

		listview_items.clear();
		listview_addons.clear();

		// crash reported on google play store
		// gueard against nullpointer
		if (Navit.map_points == null)
		{
			Navit.map_points = new ArrayList<Navit_Point_on_Map>();
		}
		// crash reported on google play store
		// guard against nullpointer

		String[] t = new String[Navit.map_points.size()];
		String[] t_addons = new String[Navit.map_points.size()];
		try
		{
			int j = 0;
			for (j = Navit.map_points.size() - 1; j >= 0; j--)
			{
				t[Navit.map_points.size() - j - 1] = Navit.map_points.get(j).point_name;
				t_addons[Navit.map_points.size() - j - 1] = Navit.map_points.get(j).addon;
			}

			for (j = 0; j < t.length; j++)
			{
				listview_items.add(t[j]);
				listview_addons.add(t_addons[j]);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			t = new String[1];
			t[0] = "* Error *";
			listview_items.add(t[0]);
			listview_addons.add(null);
		}
		NavitArrayAdapter adapter = new NavitArrayAdapter(this, listview_items, listview_addons);

		//if (convertView == null)
		//{
		//	LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		//	convertView = inflater.inflate(R.layout.search_result_list_header, null);
		//}

		// getListView().setBackgroundColor(Color.rgb(0, 0, 0));

		this.setListAdapter(adapter);
		this.getListView().setFastScrollEnabled(true);
		registerForContextMenu(this.getListView());
		my_id = this.getListView().getId();
	}

	@Override
	protected void onPause()
	{
		// Whenever this activity is paused (i.e. looses focus because another activity is started etc)
		// Override how this activity is animated out of view
		// The new activity is kept still and this activity is pushed out to the left
		overridePendingTransition(R.anim.hold, R.anim.push_out_to_right);
		super.onPause();
	}

	public static Handler handler1 = new Handler()
	{
		public void handleMessage(Message msg)
		{
			if (msg.getData().getInt("what") == 1)
			{
				int i = msg.getData().getInt("i");
				refresh_items_real(i);
			}
		}
	};

	public static void refresh_items(int i)
	{
		Message msg = new Message();
		Bundle b = new Bundle();
		b.putInt("what", 1);
		b.putInt("i", i);
		msg.setData(b);
		handler1.sendMessage(msg);
	}

	public static void refresh_items_real(int i)
	{
		String[] t = new String[Navit.map_points.size()];
		String[] t_addons = new String[Navit.map_points.size()];
		NavitArrayAdapter adapter = (NavitArrayAdapter) my.getListAdapter();
		listview_items.clear();
		listview_addons.clear();
		adapter.notifyDataSetChanged();
		try
		{
			int j = 0;
			for (j = Navit.map_points.size() - 1; j >= 0; j--)
			{
				t[Navit.map_points.size() - j - 1] = Navit.map_points.get(j).point_name;
				t_addons[Navit.map_points.size() - j - 1] = Navit.map_points.get(j).addon;
				// System.out.println("name=" + Navit.map_points.get(j).point_name + " addon=" + Navit.map_points.get(j).addon);
			}
			for (j = 0; j < t.length; j++)
			{
				listview_items.add(t[j]);
				listview_addons.add(t_addons[j]);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			t = new String[1];
			t[0] = "* Error *";
			listview_items.add(t[0]);
			listview_addons.add(null);
		}
		adapter.notifyDataSetChanged();
		refresh_items = false;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)
	{
		if (my_id != 0)
		{
			if (v.getId() == my_id)
			{
				AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
				menu.setHeaderTitle(Navit.map_points.get(Navit.map_points.size() - info.position - 1).point_name);
				String[] menuItems = context_items;
				for (int i = 0; i < menuItems.length; i++)
				{
					menu.add(Menu.NONE, i, i, menuItems[i]);
				}
			}
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item)
	{
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		int menuItemIndex = item.getItemId();
		// String menuItemName = context_items[menuItemIndex];
		t_position = info.position;
		t_size = Navit.map_points.size();
		// String listItemName = Navit.map_points.get(t_size - t_position - 1).point_name;

		switch (menuItemIndex)
		{
		case 0:
			// delete item
			Navit.map_points.remove(t_size - t_position - 1);
			// save it
			Navit.write_map_points();
			// refresh
			refresh_items = true;
			refresh_items(1);
			break;
		case 1:
			// rename item
			NavitRecentDestinationActivity.t = Navit.map_points.get(t_size - t_position - 1);
			String title = Navit.get_text("Rename Destination"); //TRANS
			RenameDialog rd = new RenameDialog(this, title, t.point_name, new RenameHandlerInterface.OnRenameItemListener()
			{
				@Override
				public void onRenameItem(String newname)
				{
					NavitRecentDestinationActivity.t.point_name = newname;
					Navit.map_points.set(t_size - t_position - 1, NavitRecentDestinationActivity.t);
					System.out.println("new=" + newname);
					// save it
					Navit.write_map_points();
					// refresh
					refresh_items = true;
					refresh_items(0);
				}
			}

			);
			rd.show();
			break;
		case 3:
			// show position on map
			NavitRecentDestinationActivity.t = Navit.map_points.get(t_size - t_position - 1);
			// compensate "selected_id" for reverse listing order of items!
			this.selected_id = t_size - t_position - 1;
			executeDone(1);
			break;
		case 4:
			// navigate to position
			NavitRecentDestinationActivity.t = Navit.map_points.get(t_size - t_position - 1);
			// compensate "selected_id" for reverse listing order of items!
			this.selected_id = t_size - t_position - 1;
			executeDone(0);
			break;
		case 2:
			// find old HOME item
			int old_home_id = Navit.find_home_point();
			if (old_home_id != -1)
			{
				NavitRecentDestinationActivity.t = Navit.map_points.get(old_home_id);
				NavitRecentDestinationActivity.t.addon = null;
				Navit.map_points.set(old_home_id, NavitRecentDestinationActivity.t);
			}
			// in case of double home, because of some strange error
			old_home_id = Navit.find_home_point();
			if (old_home_id != -1)
			{
				NavitRecentDestinationActivity.t = Navit.map_points.get(old_home_id);
				NavitRecentDestinationActivity.t.addon = null;
				Navit.map_points.set(old_home_id, NavitRecentDestinationActivity.t);
			}
			// in case of double home, because of some strange error
			old_home_id = Navit.find_home_point();
			if (old_home_id != -1)
			{
				NavitRecentDestinationActivity.t = Navit.map_points.get(old_home_id);
				NavitRecentDestinationActivity.t.addon = null;
				Navit.map_points.set(old_home_id, NavitRecentDestinationActivity.t);
			}

			// set HOME item
			NavitRecentDestinationActivity.t = Navit.map_points.get(t_size - t_position - 1);
			NavitRecentDestinationActivity.t.addon = "1";
			// delete item
			Navit.map_points.remove(t_size - t_position - 1);
			// add it to the first position
			Navit.map_points.add(NavitRecentDestinationActivity.t);
			// save it
			Navit.write_map_points();
			// refresh
			refresh_items = true;
			refresh_items(0);
			break;
		}
		return true;
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id)
	{
		super.onListItemClick(l, v, position, id);
		// Get the item that was clicked

		int t_p = position;
		int t_s = Navit.map_points.size();
		// compensate "selected_id" for reverse listing order of items!
		this.selected_id = t_s - t_p - 1;
		// close this activity
		executeDone(0);
	}

	private void executeDone(int mode)
	{
		Intent resultIntent = new Intent();
		resultIntent.putExtra("selected_id", String.valueOf(this.selected_id));
		if (mode == 1)
		{
			resultIntent.putExtra("what", "view");
		}
		else
		{
			resultIntent.putExtra("what", "navigate");
		}
		setResult(Activity.RESULT_OK, resultIntent);
		finish();
	}
}
