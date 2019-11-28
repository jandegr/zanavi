/*
 * ZANavi, Zoff Android Navigation system.
 * Copyright (C) 2011-2015 Zoff <zoff@zoff.cc>
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.channels.FileChannel;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.LinkedBlockingQueue;

import org.json.JSONArray;
import org.json.JSONObject;
import org.xml.sax.InputSource;

import pub.devrel.easypermissions.EasyPermissions;
import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.backup.BackupManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.LabeledIntent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.ToneGenerator;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.core.view.MenuItemCompat;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.appcompat.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import bpi.sdbm.illuminance.SolarPosition;

import com.lamerman.FileDialog;
import com.lamerman.SelectionMode;
import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator;
import com.luckycatlabs.sunrisesunset.calculator.SolarEventCalculator;
import com.luckycatlabs.sunrisesunset.dto.Location2;
import com.zoffcc.applications.zanavi.NavitMapDownloader.ProgressThread;
import com.zoffcc.applications.zanavi.NavitOSDJava.drawOSDThread;
import com.zoffcc.applications.zanavi.NavitVehicle.location_coords;
import com.zoffcc.applications.zanavi.ZANaviListViewAdapter.ListViewItem;
import com.zoffcc.applications.zanavi_msg.ZanaviCloudApi;

import de.oberoner.gpx2navit_txt.MainFrame;

@SuppressLint("NewApi")
@SuppressWarnings("deprecation")
public class Navit extends AppCompatActivity implements Handler.Callback, SensorEventListener
{

	static
	{
		AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
	}

	private static final String TAG = "Navit";
	static final String VERSION_TEXT_LONG_INC_REV = "4611";
	static String ZANAVI_VERSION = "unknown";
	static String NavitAppVersion = "0";
	static String NavitAppVersion_string = "0";
	private static final int NAVIT_MIN_HORIZONTAL_DP_FOR_ACTIONBAR = 400;
	private int actionbar_items_will_fit = 2;
	private boolean actionabar_download_icon_visible = false;
	static boolean is_navigating = false;
	static boolean is_paused = true;
	static String PGP_KEY_ID = "0x2942032B";

	static boolean intro_flag_crash = false;
	static boolean intro_flag_firststart = true;
	static boolean intro_flag_update = true;
	static boolean intro_flag_info = true;
	static boolean intro_flag_nomaps = true;
	static boolean intro_flag_indexmissing = false; // keep this "false" as default
	private ProgressBar progressbar_main_activity = null;

	static boolean PAINT_OLD_API = true;

	//static final int DEFAULT_THEME_DARK = android.R.style.Theme_WithActionBar;
	//static final int DEFAULT_THEME_LIGHT = android.R.style.Theme_Material_Light;
	static final int DEFAULT_THEME_OLD_LIGHT = R.style.CustomActionBarThemeLight;
	static final int DEFAULT_THEME_OLD_DARK = R.style.CustomActionBarTheme;

	private static final int DEFAULT_THEME_OLD_LIGHT_M = R.style.CustomActionBarThemeLightM;
	static final int DEFAULT_THEME_OLD_DARK_M = R.style.CustomActionBarThemeM;

	final static String[] perms = { Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS, Manifest.permission.INTERNET, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.WAKE_LOCK };

	static int OSD_blueish_bg_color = 0;

	// ----------------- DEBUG ----------------
	static final boolean DEBUG_DRAW_VEHICLE = true; // * Release: true * // if "false" then dont draw green vehicle, set this to "true" on release builds!!
	private static final boolean NAVIT_ALWAYS_UNPACK_XMLFILE = false; // * Release: false * // always unpacks the navit.xml file, set this to "false" on release builds!!
	static final boolean NAVIT_DEBUG_TEXT_VIEW = false; // * Release: false * // show overlay with debug messages, set this to "false" on release builds!!
	static final boolean GFX_OVERSPILL = true; // * Release: true * // make gfx canvas bigger for rotation and zoom smoothness, set this to "true" on release builds!!
	static final boolean DEBUG_LUX_VALUE = false; // * Release: false * // show lux values, set to "false" on release builds!!
	private static final boolean PLAYSTORE_VERSION_CRASHDETECT = true; // * Release: true * //
	// ----------------- DEBUG ----------------

	// ----------------------------------------
	static final boolean FDBL = false;
	static final int CIDEBUG = 0;
	private boolean CIRUN = false;
	static String CI_TEST_CASE_TEXT = "";
	private static final boolean CI_ALLOWCRASHREPORTS = true;
	// ----------------------------------------

	static final float OVERSPILL_FACTOR = 1.4f; // 20% percent on each side

	// ------------------ BitCoin Addr --------
	final static String BITCOIN_DONATE_ADDR = "1ZANav18WY8ytM7bhnAEBS3bdrTohsD9p";

	private static ZanaviCloudApi plugin_api = null;
	private static final int PLUGIN_MSG_ID = 1;
	private static final int PLUGIN_MSG_CAT_zanavi_version = 1;
	private static final int PLUGIN_MSG_CAT_installed_maps = 2;
	private static final int PLUGIN_MSG_CAT_3d_mode = 3;

	static final ZANaviPrefs preferences = new ZANaviPrefs();
	private static final ZANaviPrefs preferencesOld = new ZANaviPrefs();
	static final int STREET_SEARCH_STRINGS_SAVE_COUNT = 10;
	static boolean search_ready = false;
	static boolean search_list_ready = false;

	private static Menu cur_menu = null;

	static final long NAVIT_START_INTENT_DRIVE_HOME = 1L;

	private static final int NAVIT_BACKBUTTON_TO_EXIT_TIME = 2000; // 2 secs.

	private static ContentResolver content_resolver = null;
	private static final String CR_AUTHORITY = "com.zoffcc.applications.zanavi_udonate.provider";
	private static final Uri CR_CONTENT_URI = Uri.parse("content://" + CR_AUTHORITY + "/" + "table1");

	public static Intent ZANaviMapDownloaderServiceIntent = null;

	private static float last_y_bottom_bar_touch = 0;
	static float cur_y_margin_bottom_bar_touch = 0;
	static int map_view_height = 100;
	static float bottom_y_margin_bottom_bar_touch = 0;
	static int actionBarHeight = 0;
	static int bottom_bar_px = 80;
	static int bottom_bar_slider_shadow_px = 4;
	private GestureDetector mGestureDetector = null;
	static int swipeMaxOffPath = 20;
	private ZANaviRoadbookFragment road_book = null;
	private static FragmentManager fragmentManager = null;
	private ImageView push_pin_view = null;
	private static List<ListViewItem> road_book_items = null;

	TextToSpeech mTts = null;

	private static ToneGenerator toneG = null;
	static boolean toneG_heard = false;

	boolean Global_Init_Finished = false; // C lib initialized
	static int Global_Location_update_not_allowed = 0; // 0 -> send location update to C functions
														// 1 -> DO NOT send location update to C functions, it may crash in this phase

	private final static String PREF_KEY_FIRST_START = "com.zoffcc.applications.zanavi.PREF_KEY_FIRST_START";
	final static String PREF_KEY_CRASH = "com.zoffcc.applications.zanavi.CRASH";
	final static String PREF_KEY_LASTALIVE = "com.zoffcc.applications.zanavi.LASTALIVE";
	private final static String PREF_KEY_LASTUPDATETS = "com.zoffcc.applications.zanavi.LASTUPDATETS";

	static String app_status_string = "undef";
	static long app_status_lastalive = -1L;
	static final long allowed_seconds_alive_for_crash = 1000 * 60 * 2L;

	// AlertDialog dialog_info_popup = null;
	Dialog dialog_info_popup = null;
	static int info_popup_seen_count = 0;
	static final int info_popup_seen_count_max = 2; // must look at the info pop 2 times

	static Navit sNavitObject = null;
	static AssetManager asset_mgr = null;

	private static boolean Navit_doubleBackToExitPressedOnce = false;

	private static String usedMegs_str_old = "";
	private static int Routgraph_enabled = 0;

	// -------- SUN / MOON ----------
	private long sun_moon__mLastCalcSunMillis = -1L;
	private double azmiuth_cache = -1;
	private double zenith_cache = -1;
	static boolean is_night = false;
	static boolean is_twilight = false;
	static double elevation = 0;
	// -------- SUN / MOON ----------

	static CWorkerThread cwthr = null;
	private NavitGraphics NG__map_main = null;
	private NavitGraphics NG__vehicle = null;
	private static NavitVehicle NV = null;
	private static NavitSpeech2 NSp = null;
	static drawOSDThread draw_osd_thread = null;

	static boolean use_index_search = false;
	static boolean index_search_realtime = false;

	static AlertDialog.Builder generic_alert_box = null;

	private final static int Navit_Status_COMPLETE_NEW_INSTALL = 1;
	private final static int Navit_Status_UPGRADED_TO_NEW_VERSION = 2;
	private final static int Navit_Status_NORMAL_STARTUP = 0;
	static Boolean Navit_DonateVersion_Installed = false;
	static Boolean Navit_Largemap_DonateVersion_Installed = false;
	private static Boolean Navit_index_on_but_no_idx_files = false;
	static Boolean Navit_maps_too_old = false;
	final static int Navit_SHOW_DEST_ON_MAP_ZOOMLEVEL = 8;
	private final static int Navit_MAX_RECENT_DESTINATIONS = 50;
	static String debug_item_dump = "";
	static String global_last_destination_name = "";

	static String sNavitMapDirectory;
	static File[] sNavitDataStorageDirs = null;

	static int GlobalScaleLevel = 0;

	static Navit getInstance() {
		return sNavitObject;
	}

	static Context getContext()
	{
		return sNavitObject.getBaseContext();
	}

	private static void CallbackMessageChannel(int i, String s)
	{
		cwthr.CallbackMessageChannel(i, s);
	}

	String getMapMD5path() {
		return sNavitMapDirectory + "/../MD5/";
	}

	String getNAVIT_DATA_DEBUG_DIR() {
		return sNavitMapDirectory + "/../../debug/";
	}

	String getCFGpath() {
		return sNavitMapDirectory + "/../";
	}

	NavitGraphics getN_NavitGraphics() {
		return NG__map_main;
	}

	public NavitGraphics getNG__vehicle() {
		return NG__vehicle;
	}

	static final class Navit_Address_Result_Struct implements Comparable<Navit_Address_Result_Struct>
	{
		String result_type; // TWN,STR,SHN
		String item_id; // H<ddddd>L<ddddd> -> item.id_hi item.id_lo
		float lat;
		float lon;
		String addr;

		// function to sort address result list
		public int compareTo(Navit_Address_Result_Struct comp)
		{
			return this.addr.toLowerCase().compareTo(comp.addr.toLowerCase());
		}
	}

	static final class Navit_Point_on_Map implements Serializable
	{
		/**
		 * struct for a point on the map
		 */
		private static final long serialVersionUID = 6899215049749155051L;
		String point_name = "";
		String addon = null; // null -> normal, "1" -> home location
		float lat = 0.0f;
		float lon = 0.0f;
	}

	static ArrayList<Navit_Point_on_Map> map_points;

	static final Set<String> Navit_Address_Result_double_index = new HashSet<>();

	static final class Navit_OSD_compass
	{
		Boolean angle_north_valid = false;
		float angle_north = 0.0f;
		Boolean angle_target_valid = false;
		float angle_target = 0.0f;
		Boolean direct_distance_to_target_valid = false;
		String direct_distance_to_target = "";
	}

	static final class Navit_OSD_route_001
	{
		Boolean driving_distance_to_target_valid = false;
		String driving_distance_to_target = "";
		Boolean arriving_time_valid = false;
		String arriving_time = "";
		Boolean arriving_secs_to_dest_valid = false;
		String arriving_secs_to_dest = "";
	}

	static final class Navit_OSD_route_nextturn
	{
		Boolean nextturn_image_filename_valid = false;
		String nextturn_image_filename = "";
		Boolean nextturn_image_valid = false;
		Bitmap nextturn_image = null;
		Boolean nextturn_distance_valid = false;
		String nextturn_distance = "";
		String nextturn_streetname = "";
		String nextturn_streetname_systematic = "";
	}

	static final class Navit_OSD_scale
	{
		Boolean scale_valid = false;
		String scale_text = "";
		int base = 0;
		int var = 0;
	}

	static final String CHANNEL_ID = "Zanavi_channel_01";


	static final Navit_OSD_compass OSD_compass = new Navit_OSD_compass();
	static final Navit_OSD_route_001 OSD_route_001 = new Navit_OSD_route_001();
	static final Navit_OSD_route_nextturn OSD_nextturn = new Navit_OSD_route_nextturn();
	static Navit_OSD_scale OSD_scale = new Navit_OSD_scale();

	SimGPS Simulate = null;
	private WatchMem watchmem = null;
	static int sats = 0;
	static int satsInFix = 0;

	// -------------- GPS fix and extrapolation vars -------------
	// -------------- GPS fix and extrapolation vars -------------
	static Location mLastLocation = null;
	static long mLastLocationMillis = -1;
	static boolean isGPSFix = false;
	static boolean pos_is_underground = false;
	static boolean tunnel_extrapolation = false;
	// -------------- GPS fix and extrapolation vars -------------
	// -------------- GPS fix and extrapolation vars -------------

	// public static Vibrator vibrator = null;

	Handler handler;
	static PowerManager.WakeLock wl;
	static PowerManager.WakeLock wl_cpu;
	static PowerManager.WakeLock wl_navigating;
	private NavitActivityResult[] ActivityResults;
	static AudioManager sNavitAudioManager = null;
	static DisplayMetrics metrics = null;
	public static String my_display_density = "mdpi";
	private boolean searchBoxShown = false;
	static final int MAPDOWNLOAD_PRI_DIALOG = 1;
	static final int MAPDOWNLOAD_SEC_DIALOG = 2;
	private static final int SEARCHRESULTS_WAIT_DIALOG = 3;
	private static final int SEARCHRESULTS_WAIT_DIALOG_OFFLINE = 4;
	private static final int ADDRESS_RESULTS_DIALOG_MAX = 10;
	private ProgressDialog mapdownloader_dialog_pri = null;
	private ProgressDialog mapdownloader_dialog_sec = null;
	private ProgressDialog search_results_wait = null;
	private ProgressDialog search_results_wait_offline = null;
	static Handler Navit_progress_h = null;
	static NavitMapDownloader mapdownloader_pri = null;
	private static final int NavitDownloaderPriSelectMap_id = 967;
	private static final int NavitDownloaderSecSelectMap_id = 968;
	static final int NavitDeleteSecSelectMap_id = 969;
	private static final int NavitRecentDest_id = 970;
	private static final int NavitGeoCoordEnter_id = 971;
	private static final int NavitGPXConvChooser_id = 972;
	private static final int NavitSendFeedback_id = 973;
	private static final int NavitReplayFileConvChooser_id = 974;
	private static final int ZANaviIntro_id = 975;
	private static final int ZANaviAbout_id = 976;
	static int download_map_id = 0;
	private ProgressThread progressThread_pri = null;
	private ProgressThread progressThread_sec = null;
	static int search_results_towns = 0;
	static int search_results_streets = 0;
	static int search_results_streets_hn = 0;
	static int search_results_poi = 0;
	static Boolean search_hide_duplicates = false;
	static Boolean NavitStartupAlreadySearching = false;
	private SearchResultsThread searchresultsThread = null;
	private SearchResultsThread searchresultsThread_offline = null;
	static Boolean NavitAddressSearchSpinnerActive = false;
	static final int MAP_NUM_PRIMARY = 11;
	private static final int NavitAddressSearch_id_offline = 70;
	private static final int NavitAddressSearch_id_online = 73;
	private static final int NavitAddressResultList_id = 71;
	static final int NavitAddressSearchCountry_id = 74;
	static final int NavitMapPreview_id = 75;
	private static final int NavitAddressSearch_id_gmaps = 76;
	private static final int NavitAddressSearch_id_sharedest = 77;
	private static final int ZANaviVoiceInput_id = 78;
	static final int NavitDonateFromSearch_id = 79;
	static int NavitSearchresultBarIndex = -1;
	static String NavitSearchresultBar_title = "";
	static String NavitSearchresultBar_text = "";
	static final List<Navit_Address_Result_Struct> NavitAddressResultList_foundItems = new ArrayList<>();

	static boolean DemoVehicle;

	static Typeface NavitStreetnameFont;

	private SensorManager sensorManager;
	// static final float lux_darkness_value = 4;
	private Sensor lightSensor = null;
	private SensorEventListener lightSensorEventListener;
	static boolean night_mode = false;
	static float debug_cur_lux_value = -1;

	static String lane_destination = "";
	static String lanes_text = "";
	static String lanes_text1 = "";
	static String lane_choices = "";
	static String lane_choices1 = "";
	static String lane_choices2 = "";
	static int lanes_num = 0;
	static int lanes_num_forward = 0;
	static int lanes_num1 = 0;
	static int lanes_num_forward1 = 0;
	static int seg_len = 0;
	static int cur_max_speed = -1;
	static int cur_max_speed_corr = -1;
	static boolean you_are_speeding = false;

	static Bitmap menu_button = null;
	static RectF menu_button_rect = new RectF(-100, 1, 1, 1);
	static RectF menu_button_rect_touch = new RectF(-100, 1, 1, 1);
	static boolean follow_current; //pin the mapview to position
	static Bitmap zoomin = null;
	static Bitmap zoomout = null;
	// public static Bitmap bigmap_bitmap = null;
	static Bitmap oneway_arrow;
	static Bitmap oneway_bicycle_arrow;
	static Bitmap nav_arrow_stopped;
	static Bitmap nav_arrow_stopped_small;
	static Bitmap nav_arrow_moving;
	static Bitmap nav_arrow_moving_grey;
	static Bitmap nav_arrow_moving_small;
	static Bitmap nav_arrow_moving_shadow;
	static Bitmap nav_arrow_moving_shadow_small;

	static String Navit_last_address_search_string = "";
	static String Navit_last_address_hn_string = "";
	private static Boolean Navit_last_address_full_file_search = false;
	static String Navit_last_address_search_country_iso2_string = "";
	static int Navit_last_address_search_country_flags = 3;
	private static int Navit_last_address_search_country_id = 0;
	static Boolean Navit_last_address_partial_match = true;
	static Geocoder Navit_Geocoder = null;
	static String UserAgentString_bind = null;
	static Boolean first_ever_startup = false;

	private static Boolean Navit_Announcer = true;

	static final int MAP_NUM_SECONDARY = 12;
	private static String NAVIT_DATA_SHARE_DIR;// = NAVIT_DATA_DIR + "/share";
	static final String Navit_DEST_FILENAME = "destinations.dat";

	static final int RC_PERM_001 = 11;

	static Resources sResources = null;
	private static Window sAppWindow = null;

	static String get_text(String in)
	{
		return NavitTextTranslations.get_text(in);
	}

	private boolean extractRes(String resname, String result)
	{
		int slash = -1;
		boolean needs_update = false;
		File resultfile;
		Resources res = getResources();
		Log.e(TAG, "Res Obj " + res);
		Log.e(TAG, "Res Name " + resname);
		Log.e(TAG, "result " + result);
		int id = res.getIdentifier(resname, "raw", "com.zoffcc.applications.zanavi");
		// int id = res.getIdentifier(resname, "raw", getPackageName());

		Log.e(TAG, "Res ID " + id);

		if (id == 0)
		{
			return false;
		}

		while ((slash = result.indexOf("/", slash + 1)) != -1)
		{
			if (slash != 0)
			{
				Log.e(TAG, "Checking " + result.substring(0, slash));
				resultfile = new File(result.substring(0, slash));
				if (!resultfile.exists())
				{
					Log.e(TAG, "Creating dir");
					if (!resultfile.mkdir()) return false;
					needs_update = true;
				}
			}
		}

		resultfile = new File(result);

		if (!resultfile.exists())
		{
			needs_update = true;
		}

		if (!needs_update)
		{
			try
			{
				InputStream resourcestream = res.openRawResource(id);
				FileInputStream resultfilestream = new FileInputStream(resultfile);
				byte[] resourcebuf = new byte[1024];
				byte[] resultbuf = new byte[1024];
				int i;

				while ((i = resourcestream.read(resourcebuf)) != -1)
				{
					if (resultfilestream.read(resultbuf) != i)
					{
						Log.e(TAG, "Result is too short");
						needs_update = true;
						break;
					}

					for (int j = 0; j < i; j++)
					{
						if (resourcebuf[j] != resultbuf[j])
						{
							Log.e(TAG, "Result is different");
							needs_update = true;
							break;
						}
					}
					if (needs_update) break;
				}

				if (!needs_update && resultfilestream.read(resultbuf) != -1)
				{
					Log.e(TAG, "Result is too long");
					needs_update = true;
				}

				if (resultfilestream != null)
				{
					resultfilestream.close();
				}
			}
			catch (Exception e)
			{
				Log.e(TAG, "Exception " + e.getMessage());
				return false;
			}
		}

		if (needs_update)
		{
			Log.e(TAG, "Extracting resource");
			try
			{
				InputStream resourcestream = res.openRawResource(id);
				FileOutputStream resultfilestream = new FileOutputStream(resultfile);
				byte[] buf = new byte[1024];
				int i;

				while ((i = resourcestream.read(buf)) != -1)
				{
					resultfilestream.write(buf, 0, i);
				}

				if (resultfilestream != null)
				{
					resultfilestream.close();
				}
			}
			catch (Exception e)
			{
				Log.e(TAG, "Exception " + e.getMessage());
				return false;
			}
		}
		return true;
	}

	static OnAudioFocusChangeListener focusChangeListener = new OnAudioFocusChangeListener()
	{
		public void onAudioFocusChange(int focusChange)
		{
			// AudioManager am = Navit.sNavitAudioManager;
			switch (focusChange)
			{

			case (AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK):
				// Lower the volume while ducking.
				//mediaPlayer.setVolume(0.2f, 0.2f);
				break;
			case (AudioManager.AUDIOFOCUS_LOSS_TRANSIENT):
				//pause();
				break;

			case (AudioManager.AUDIOFOCUS_LOSS):
				//stop();
				//ComponentName component = new ComponentName(AudioPlayerActivity.this, MediaControlReceiver.class);
				//am.unregisterMediaButtonEventReceiver(component);
				break;

			case (AudioManager.AUDIOFOCUS_GAIN):
				// Return the volume to normal and resume if paused.
				//mediaPlayer.setVolume(1f, 1f);
				//mediaPlayer.start();
				break;
			default:
				break;
			}
		}
	};

	//	private boolean checkPlayServices()

	//	{
	//		int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
	//
	//		Log.i("PlayServices", "isGooglePlayServicesAvailable=" + status);
	//
	//		if (status != ConnectionResult.SUCCESS)
	//		{
	//			if (GooglePlayServicesUtil.isUserRecoverableError(status))
	//			{
	//				Toast.makeText(this, "Recoverable error.", Toast.LENGTH_LONG).show();
	//				// showErrorDialog(status);
	//			}
	//			else
	//			{
	//				Toast.makeText(this, "This device is not supported.", Toast.LENGTH_LONG).show();
	//			}
	//			return false;
	//		}
	//		return true;
	//	}

	// ----------------------------------------------------------------------------------------------------------
	// thanks to: http://stackoverflow.com/questions/843675/how-do-i-find-out-if-the-gps-of-an-android-device-is-enabled
	// ----------------------------------------------------------------------------------------------------------
	private void buildAlertMessageNoGps()
	{
		try
		{
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(Navit.get_text("Your GPS is disabled, do you want to enable it?")).setCancelable(false).setPositiveButton(Navit.get_text("Yes"), new DialogInterface.OnClickListener()
			{
				public void onClick(final DialogInterface dialog, final int id)
				{
					startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
				}
			}).setNegativeButton(Navit.get_text("No"), new DialogInterface.OnClickListener()
			{
				public void onClick(final DialogInterface dialog, final int id)
				{
					dialog.cancel();
				}
			});
			AlertDialog alert = builder.create();
			alert.show();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	@TargetApi(23)
	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
	{
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
		{
			super.onRequestPermissionsResult(requestCode, permissions, grantResults);

			// Forward results to EasyPermissions
			EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
		}
	}

	private void smaller_top_bar(boolean horizonzal)
	{
		// not working properly, deactivate for now ----------------
		if (2 == (1 + 1) * 1)
		{
			return;
		}
		// not working properly, deactivate for now ----------------

		// -------------------------------------------------------------
		// -------------------------- smaller top bar  -----------------
		// -------------------------------------------------------------
		RelativeLayout v11 = findViewById(R.id.gui_top_container);
		int h11 = getResources().getDimensionPixelSize(R.dimen.gui_top_container_height);
		if (horizonzal)
		{
			// h11 = (int) ((float) h11 * 0.8f);
			h11 = h11 * 1;
		}
		else
		{
			h11 = (int) ((float) h11 * 0.7f);
		}
		android.view.ViewGroup.LayoutParams lp11 = v11.getLayoutParams();
		lp11.height = h11;
		v11.requestLayout();
		// -------------------------------------------------------------
		// -------------------------- smaller top bar  -----------------
		// -------------------------------------------------------------
	}


	private static void show_mem_used() // wrapper
	{
		try
		{
			Message msg = Navit_progress_h.obtainMessage();
			Bundle b = new Bundle();
			msg.what = 14;
			msg.setData(b);
			Navit_progress_h.sendMessage(msg);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private static void show_mem_used_real()
	{
		try
		{
			if (preferences.PREF_show_debug_messages)
			{
				// --------- OLD method -----------
				// --------- OLD method -----------
				//				int usedMegs;
				//				//System.gc();
				//				usedMegs = (int) (Debug.getNativeHeapAllocatedSize() / 1048576L);
				//				//Debug.MemoryInfo meminfo = new Debug.MemoryInfo();
				//				//Debug.getMemoryInfo(meminfo);
				//
				//				if (usedMegs_old != usedMegs)
				//				{
				//					String usedMegsString = String.format("Memory Used: %d MB", usedMegs);
				//					//System.out.println("" + meminfo.dalvikPrivateDirty + " " + meminfo.dalvikPss + " " + meminfo.dalvikSharedDirty + " nP:" + meminfo.nativePrivateDirty + " nPss:" + meminfo.nativePss + " nSh:" + meminfo.nativeSharedDirty + " o1:" + meminfo.otherPrivateDirty + " o2:" + meminfo.otherPss + " o3:" + meminfo.otherSharedDirty);
				//					Navit.set_debug_messages2(usedMegsString);
				//				}
				//				usedMegs_old = usedMegs;
				// --------- OLD method -----------
				// --------- OLD method -----------

				// --------- NEW method -----------
				// --------- NEW method -----------
				String usedMegs = logHeap(sNavitObject.getClass());
				if (usedMegs_str_old.compareTo(usedMegs) != 0)
				{
					Navit.set_debug_messages2(usedMegs);
				}
				usedMegs_str_old = usedMegs;
				// --------- NEW method -----------
				// --------- NEW method -----------
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static void set_debug_messages(String texta, String textb, String textc)
	{
		try
		{
			NavitGraphics.debug_line_1 = texta;
			NavitGraphics.debug_line_2 = textb;
			NavitGraphics.debug_line_3 = textc;
			NavitGraphics.NavitMsgTv_.setMaxLines(4);
			NavitGraphics.NavitMsgTv_.setLines(4);
			NavitGraphics.NavitMsgTv_.setText(" " + NavitGraphics.debug_line_1 + "\n " + NavitGraphics.debug_line_2 + "\n " + NavitGraphics.debug_line_3 + "\n " + NavitGraphics.debug_line_4);
			//System.out.println("invalidate 022");
			NavitGraphics.NavitMsgTv_.postInvalidate();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static void set_debug_messages1(String text)
	{
		try
		{
			NavitGraphics.debug_line_1 = text;
			NavitGraphics.NavitMsgTv_.setMaxLines(4);
			NavitGraphics.NavitMsgTv_.setLines(4);
			NavitGraphics.NavitMsgTv_.setText(" " + NavitGraphics.debug_line_1 + "\n " + NavitGraphics.debug_line_2 + "\n " + NavitGraphics.debug_line_3 + "\n " + NavitGraphics.debug_line_4);
			//System.out.println("invalidate 023");
			NavitGraphics.NavitMsgTv_.postInvalidate();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private static void set_debug_messages2(String text)
	{
		try
		{
			NavitGraphics.debug_line_2 = text;
			//NavitGraphics.NavitMsgTv_.setMaxLines(4);
			//NavitGraphics.NavitMsgTv_.setLines(4);

			NavitGraphics.NavitMsgTv_.setText(" " + NavitGraphics.debug_line_1 + "\n " + NavitGraphics.debug_line_2 + "\n " + NavitGraphics.debug_line_3 + "\n " + NavitGraphics.debug_line_4);
			//System.out.println("invalidate 024");
			NavitGraphics.NavitMsgTv_.postInvalidate();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private static void set_debug_messages3(String text)
	{
		try
		{
			NavitGraphics.debug_line_3 = text;
			NavitGraphics.NavitMsgTv_.setMaxLines(4);
			NavitGraphics.NavitMsgTv_.setLines(4);
			NavitGraphics.NavitMsgTv_.setText(" " + NavitGraphics.debug_line_1 + "\n " + NavitGraphics.debug_line_2 + "\n " + NavitGraphics.debug_line_3 + "\n " + NavitGraphics.debug_line_4);
			//System.out.println("invalidate 025");
			NavitGraphics.NavitMsgTv_.postInvalidate();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static void set_debug_messages4(String text)
	{
		try
		{
			NavitGraphics.debug_line_4 = text;
			NavitGraphics.NavitMsgTv_.setMaxLines(4);
			NavitGraphics.NavitMsgTv_.setLines(4);
			NavitGraphics.NavitMsgTv_.setText(" " + NavitGraphics.debug_line_1 + "\n " + NavitGraphics.debug_line_2 + "\n " + NavitGraphics.debug_line_3 + "\n " + NavitGraphics.debug_line_4);
			//System.out.println("invalidate 026");
			NavitGraphics.NavitMsgTv_.postInvalidate();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static void set_debug_messages3_wrapper(String text)
	{
		try
		{
			Message msg = Navit_progress_h.obtainMessage();
			Bundle b = new Bundle();
			msg.what = 15;
			b.putString("text", text);
			msg.setData(b);
			Navit_progress_h.sendMessage(msg);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static void set_debug_messages_say_wrapper(String text)
	{
		try
		{
			Message msg = Navit_progress_h.obtainMessage();
			Bundle b = new Bundle();
			msg.what = 30;
			b.putString("text", text);
			msg.setData(b);
			Navit_progress_h.sendMessage(msg);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	protected void onNewIntent(Intent intent)
	{
		super.onNewIntent(intent);
		handleIntent(intent);
	}

	private void sendCallBackMessage(int i) {
		Message msg2 = new Message();
		Bundle b2 = new Bundle();
		b2.putInt("Callback", i);
		msg2.setData(b2);
		callback_handler_55.sendMessage(msg2);
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		// if (Navit.METHOD_DEBUG) Navit.my_func_name(0);

		// ------- only after API level 9 -------
		// ------- only after API level 9 -------
		//		try
		//		{
		//			StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectAll().penaltyDeath().penaltyLog().build());
		//			StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectAll().penaltyLog().build());
		//
		//			StrictMode.ThreadPolicy old = StrictMode.getThreadPolicy();
		//			StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder(old).permitDiskWrites().build());
		//			old = StrictMode.getThreadPolicy();
		//			StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder(old).permitDiskReads().build());
		//
		//		}
		//		catch (NoClassDefFoundError e)
		//		{
		//		}
		// ------- only after API level 9 -------
		// ------- only after API level 9 -------

		// Log.e(TAG, "OnCreate");

		//		if (checkPlayServices())
		//		{
		//		}

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
		{
			NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
			NotificationChannel channel;
			channel = new NotificationChannel(CHANNEL_ID, "ZanaviNot", NotificationManager.IMPORTANCE_LOW);
			channel.setDescription("Zanavi Notification Channel");
			notificationManager.createNotificationChannel(channel);
		}

		ZANaviMainApplication.restore_error_msg(this.getApplicationContext());

		app_status_string = PreferenceManager.getDefaultSharedPreferences(this).getString(PREF_KEY_CRASH, "down");

		if (FDBL)
		{
			preferences.PREF_enable_debug_crashdetect = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("enable_debug_crashdetect", true);
		}
		else
		{
			preferences.PREF_enable_debug_crashdetect = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("enable_debug_crashdetect", PLAYSTORE_VERSION_CRASHDETECT);
		}

		System.out.println("app_status_string get:[onCreate]" + app_status_string);
		System.out.println("app_status_string=" + app_status_string);

		if (app_status_string.compareToIgnoreCase("down") != 0)
		{
			if (Navit.CI_ALLOWCRASHREPORTS)
			{
				intro_flag_crash = true;
				System.out.println("app_status_string:1:" + "intro_flag_crash=" + intro_flag_crash);
			}
			else
			{
				intro_flag_crash = false;
			}
		}
		else
		{
			intro_flag_crash = false;
		}

		if (checkForUpdate())
		{
			// reset crash flag if we just updated
			intro_flag_crash = false;
		}

		if (!preferences.PREF_enable_debug_crashdetect)
		{
			// reset crash flag if we preference set to "false"
			intro_flag_crash = false;
		}

		// --- if we have no stacktrace -> don't show crash screen ----------
		if (intro_flag_crash)
		{
			try
			{
				if (ZANaviMainApplication.last_stack_trace_as_string == null)
				{
					intro_flag_crash = false;
				}
				else if (ZANaviMainApplication.last_stack_trace_as_string.length() < 2)
				{
					intro_flag_crash = false;
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		// --- if we have no stacktrace -> don't show crash screen ----------

		System.out.println("app_status_string:2:" + "intro_flag_crash=" + intro_flag_crash);

		try
		{
			app_status_string = "running";
			PreferenceManager.getDefaultSharedPreferences(this).edit().putString(PREF_KEY_CRASH, "running").apply();
			System.out.println("app_status_string set:[onCreate]" + app_status_string);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		System.out.println("XXX:API=" + Integer.valueOf(android.os.Build.VERSION.SDK));
		Navit.PAINT_OLD_API = false;


		getPrefs_theme();
		getPrefs_theme_main();
		Navit.applySharedTheme(this, preferences.PREF_current_theme_M);

		super.onCreate(savedInstanceState);

		sNavitObject = this;
		asset_mgr = getAssets();

		PackageInfo pInfo;
		try
		{
			pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			ZANAVI_VERSION = pInfo.versionName;
		}
		catch (NameNotFoundException e4)
		{
		}

		// Intent intent = new Intent(this, ZANaviAboutPage.class);
		// startActivity(intent);

		// --------- check permissions -----------
		// --------- check permissions -----------
		// --------- check permissions -----------

		/*
		 *
		 * <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
		 * <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
		 * <uses-permission android:name="android.permission.ACCESS_LOCATION_EXTRA_COMMANDS" />
		 * <uses-permission android:name="android.permission.WAKE_LOCK" />
		 * <uses-permission android:name="android.permission.INTERNET" />
		 * <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
		 * <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
		 */

		//if (EasyPermissions.hasPermissions(this, perms))
		//{
		//	// have permissions!
		//}
		//else
		//{
		//	// ask for permissions
		//	EasyPermissions.requestPermissions(this, Navit.get_text("ZANavi needs some permissions..."), RC_PERM_001, perms);
		//}
		// --------- check permissions -----------
		// --------- check permissions -----------
		// --------- check permissions -----------

		OSD_blueish_bg_color = getResources().getColor(R.color.blueish_bg_color);

		last_orientation = getResources().getConfiguration().orientation;

		content_resolver = getContentResolver();

		Display display = getWindowManager().getDefaultDisplay();
		metrics = new DisplayMetrics();
		display.getMetrics(metrics);
		int width = display.getWidth();
		int height = display.getHeight();
		Log.e(TAG, "Navit -> pixels x=" + width + " pixels y=" + height);
		Log.e(TAG, "Navit -> dpi=" + metrics.densityDpi);
		Log.e(TAG, "Navit -> density=" + metrics.density);
		Log.e(TAG, "Navit -> scaledDensity=" + metrics.scaledDensity);

		road_book_items = new ArrayList<>();
		fragmentManager = getSupportFragmentManager();

		setContentView(R.layout.main_layout);

		Toolbar toolbar = findViewById(R.id.toolbar);

		if (toolbar != null)
		{
			try
			{
				setSupportActionBar(toolbar);
				// System.out.println("TTT01:" + toolbar);
			}
			catch (NoClassDefFoundError e)
			{
			}
		}

		try
		{
			getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE);
			getSupportActionBar().setDisplayUseLogoEnabled(false);
			getSupportActionBar().setIcon(R.drawable.icon);
			getSupportActionBar().setDisplayShowHomeEnabled(true);
			getSupportActionBar().setDisplayShowTitleEnabled(false);
		}
		catch (NoClassDefFoundError e)
		{
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		progressbar_main_activity = findViewById(R.id.progressbar_main_activity);
		progressbar_main_activity.setVisibility(View.GONE);
		progressbar_main_activity.setProgress(0);

		// ------------ bottom bar slider ----------------
		// ------------ bottom bar slider ----------------
		// ------------ bottom bar slider ----------------

		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
		{
			smaller_top_bar(true);
		}
		else
		{
			smaller_top_bar(false);
		}

		bottom_bar_px = (int) getResources().getDimension(R.dimen.gui_top_container_height);
		// System.out.println("VVV:bottom_bar_height:" + bottom_bar_px);
		bottom_bar_slider_shadow_px = (int) getResources().getDimension(R.dimen.bottom_slide_view_shadow_compat_height);
		// System.out.println("VVV:bottom_bar_slider_shadow_px:" + bottom_bar_slider_shadow_px);

		// final RelativeLayout a = (RelativeLayout) findViewById(R.id.bottom_bar_container);
		final FrameLayout a = findViewById(R.id.bottom_bar_slide);
		final RelativeLayout.LayoutParams pp22 = (RelativeLayout.LayoutParams) a.getLayoutParams();

		// Calculate ToolBar height
		try
		{
			TypedValue tv = new TypedValue();
			if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true))
			{
				actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
				System.out.println("VVV:abh:" + actionBarHeight);
			}
			else
			{
				actionBarHeight = NavitGraphics.dp_to_px(144);
			}
		}
		catch (Exception e)
		{
			actionBarHeight = NavitGraphics.dp_to_px(144);
		}

		final Toolbar view_toolbar_top = findViewById(R.id.toolbar);
		ViewTreeObserver vto = view_toolbar_top.getViewTreeObserver();
		vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener()
		{
			@Override
			public void onGlobalLayout()
			{
				view_toolbar_top.getViewTreeObserver().removeGlobalOnLayoutListener(this);
				// int width = view_toolbar_top.getMeasuredWidth();
				Navit.actionBarHeight = view_toolbar_top.getMeasuredHeight();
				// System.out.println("hhh:88=" + Navit.actionBarHeight);
				Navit.cur_y_margin_bottom_bar_touch = Navit.map_view_height + Navit.actionBarHeight + bottom_bar_px - Navit.bottom_bar_slider_shadow_px; // try to put view at bottom

				pp22.setMargins(0, (int) Navit.cur_y_margin_bottom_bar_touch, 0, 0); // left, top, right, bottom
				a.setLayoutParams(pp22);
				a.requestLayout();
			}
		});

		// actionBarHeight = 168;

		//		final int SWIPE_MIN_DISTANCE = NavitGraphics.dp_to_px(25);
		//		final float SWIPE_THRESHOLD_VELOCITY = 5.5f;
		//		final float FLING_PIXELS_PER_SECOND = 100;
		//		final float maxFlingVelocity = ViewConfiguration.get(this).getScaledMaximumFlingVelocity();
		final ViewConfiguration vc = ViewConfiguration.get(this);
		final int swipeMinDistance = vc.getScaledPagingTouchSlop();
		final int swipeThresholdVelocity = vc.getScaledMinimumFlingVelocity();
		swipeMaxOffPath = vc.getScaledTouchSlop();
		// (there is also vc.getScaledMaximumFlingVelocity() one could check against)

		// setup some values --------
		NavitGraphics.long_press_on_screen_max_distance = swipeMaxOffPath;
		// setup some values --------

		class MyGestureDetector extends SimpleOnGestureListener
		{
			@Override
			public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY)
			{
				try
				{
					//					float velocityPercentY = velocityY / maxFlingVelocity; // the percent is a value in the range of (0, 1]
					//					float normalizedVelocityY = velocityPercentY * FLING_PIXELS_PER_SECOND; // where PIXELS_PER_SECOND is a device-independent measurement

					//					System.out.println("VVV:" + (e1.getY() - e2.getY()) + " " + NavitGraphics.dp_to_px((int) (e1.getY() - e2.getY())) + " " + maxFlingVelocity + " " + velocityY + " " + velocityPercentY + " " + normalizedVelocityY + " " + SWIPE_THRESHOLD_VELOCITY);

					// System.out.println("VVV:2:" + swipeMinDistance + " " + swipeThresholdVelocity + " " + swipeMaxOffPath);

					// bottom to top
					if (e1.getY() - e2.getY() > swipeMinDistance && Math.abs(velocityY) > swipeThresholdVelocity)
					{
						//int featureWidth = getMeasuredWidth();
						//mActiveFeature = (mActiveFeature < (mItems.size() - 1)) ? mActiveFeature + 1 : mItems.size() - 1;
						//smoothScrollTo(mActiveFeature * featureWidth, 0);
						//System.out.println("GS:002:up:" + velocityY + " " + e2.getY() + " " + e1.getY());

						animate_bottom_bar_up();

						return true;
					}
					// top to bottom
					else if (e2.getY() - e1.getY() > swipeMinDistance && Math.abs(velocityY) > swipeThresholdVelocity)
					{
						//int featureWidth = getMeasuredWidth();
						//mActiveFeature = (mActiveFeature > 0) ? mActiveFeature - 1 : 0;
						//smoothScrollTo(mActiveFeature * featureWidth, 0);
						//System.out.println("GS:003:down:" + velocityY + " " + e1.getY() + " " + e2.getY());

						animate_bottom_bar_down();

						return true;
					}
				}
				catch (Exception e)
				{
					//System.out.println("GS:009:EE:" + e.getMessage());
				}
				return false;
			}
		}
		mGestureDetector = new GestureDetector(new MyGestureDetector());

		push_pin_view = findViewById(R.id.bottom_slide_left_side);
		if (preferences.PREF_follow_gps)
		{
			push_pin_view.setImageResource(R.drawable.pin1_down);
		}
		else
		{
			push_pin_view.setImageResource(R.drawable.pin1_up);
		}
		push_pin_view.setOnClickListener(v -> {
			try
			{
				toggle_follow_button();
			}
			catch (Exception e)
			{
			}
		});

		cur_y_margin_bottom_bar_touch = 0; // try to put view at bottom

		a.setOnTouchListener(new View.OnTouchListener()
		{
			@Override
			synchronized public boolean onTouch(View v, MotionEvent m)
			{

				int action = m.getAction();

				if (mGestureDetector.onTouchEvent(m))
				{
					//System.out.println("GS:001:fling!!");
					// System.out.println("FRAG:fling:011");
					return true;
				}
				else if (action == MotionEvent.ACTION_DOWN)
				{
					last_y_bottom_bar_touch = m.getY();

					// put roadbook into layout -----------
					FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

					try
					{
						if (road_book == null)
						{
							road_book = new ZANaviRoadbookFragment();
							// System.out.println("FRAG:attach:001");
							fragmentTransaction.replace(R.id.roadbook_fragment_container, road_book, "");
							fragmentTransaction.commitAllowingStateLoss();
							// fragmentTransaction.show(road_book);
						}
						else
						{
							// System.out.println("FRAG:attached:003");
						}
					}
					catch (Exception ef)
					{
					}
					// put roadbook into layout -----------

					return true;
				}
				else if ((action == MotionEvent.ACTION_UP) || (action == MotionEvent.ACTION_CANCEL))
				{
					// System.out.println("FRAG:up/cancel:012");

					// release
					if (cur_y_margin_bottom_bar_touch > (bottom_y_margin_bottom_bar_touch / 2))
					{
						// snap back to bottom
						animate_bottom_bar_down();
					}
					else
					{
						// snap top top
						animate_bottom_bar_up();
					}
				}
				else
				// if (action == MotionEvent.ACTION_MOVE)
				{
					// System.out.println("FRAG:*else*:012");

					if (Math.abs(last_y_bottom_bar_touch - m.getY()) > 2)
					{
						float last_margin = cur_y_margin_bottom_bar_touch;
						cur_y_margin_bottom_bar_touch = cur_y_margin_bottom_bar_touch - (last_y_bottom_bar_touch - m.getY());

						if ((cur_y_margin_bottom_bar_touch >= 0) && (cur_y_margin_bottom_bar_touch <= bottom_y_margin_bottom_bar_touch))
						{
							// System.out.println("VVV:move:" + cur_y_margin_bottom_bar_touch + " " + bottom_y_margin_bottom_bar_touch);

							last_y_bottom_bar_touch = m.getY() + (last_y_bottom_bar_touch - m.getY());
							RelativeLayout.LayoutParams relativeParams = (RelativeLayout.LayoutParams) a.getLayoutParams();
							relativeParams.setMargins(0, (int) cur_y_margin_bottom_bar_touch, 0, 0); // left, top, right, bottom
							a.setLayoutParams(relativeParams);
							a.requestLayout();
						}
						else
						{
							// System.out.println("VVV:revert");

							// revert position
							cur_y_margin_bottom_bar_touch = last_margin;
						}
					}

				}
				return true;
			}
		});
		// ------------ bottom bar slider ----------------
		// ------------ bottom bar slider ----------------
		// ------------ bottom bar slider ----------------

		// init cancel dialog!! ----------
		// init cancel dialog!! ----------
		Message msg2 = new Message();
		Bundle b2 = new Bundle();
		b2.putString("text", "");
		msg2.what = 0;
		msg2.setData(b2);
		ZANaviDownloadMapCancelActivity.canceldialog_handler.sendMessage(msg2);
		// init cancel dialog!! ----------
		// init cancel dialog!! ----------

		sAppWindow = getWindow();

		// ---------------- set some directories -----------------
		// ---------------- set some directories -----------------
		// = "/data/data/com.zoffcc.applications.zanavi"; // later use: Context.getFilesDir().getPath();
		String NAVIT_DATA_DIR = this.getFilesDir().getPath();
		this.getFilesDir().mkdirs();
		// ---
		// System.out.println("data dir=" + NAVIT_DATA_DIR);
		NAVIT_DATA_SHARE_DIR = NAVIT_DATA_DIR + "/share/";
		File tmp3 = new File(NAVIT_DATA_SHARE_DIR);
		tmp3.mkdirs();
		// ---
		String FIRST_STARTUP_FILE = NAVIT_DATA_SHARE_DIR + "/has_run_once.txt";
		String VERSION_FILE = NAVIT_DATA_SHARE_DIR + "/version.txt";
		// ---------------- set some directories -----------------
		// ---------------- set some directories -----------------

		try
		{
			toneG = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
		}
		catch (Exception e)
		{
		}

		try
		{
			// send overspill factor to C-code
			sendCallBackMessage(104);
		}
		catch (Exception eee)
		{
		}

		// ----- service -----
		// ----- service -----
		ZANaviMapDownloaderServiceIntent = new Intent(getBaseContext(), ZANaviMapDownloaderService.class);
		// ----- service -----
		// ----- service -----

		System.out.println("Navit:onCreate:JTHREAD ID=" + Thread.currentThread().getId());
		System.out.println("Navit:onCreate:THREAD ID=" + NavitGraphics.GetThreadId());

		// paint for bitmapdrawing on map
		NavitGraphics.paint_for_map_display.setAntiAlias(true);
		NavitGraphics.paint_for_map_display.setFilterBitmap(true);

		// sky
		NavitGraphics.paint_sky_day.setAntiAlias(true);
		NavitGraphics.paint_sky_day.setColor(Color.parseColor("#79BAEC"));
		NavitGraphics.paint_sky_night.setAntiAlias(true);
		NavitGraphics.paint_sky_night.setColor(Color.parseColor("#090909"));
		// stars
		NavitGraphics.paint_sky_night_stars.setColor(Color.parseColor("#DEDDEF"));
		// twilight
		NavitGraphics.paint_sky_twilight1.setColor(Color.parseColor("#090909"));
		NavitGraphics.paint_sky_twilight2.setColor(Color.parseColor("#113268"));
		NavitGraphics.paint_sky_twilight3.setColor(Color.parseColor("#79BAEC"));

		Random m = new Random();
		int i6;
		for (i6 = 0; i6 < (NavitGraphics.max_stars + 1); i6++)
		{
			NavitGraphics.stars_x[i6] = m.nextFloat();
			NavitGraphics.stars_y[i6] = m.nextFloat();
			NavitGraphics.stars_size[i6] = m.nextInt(3) + 1;
		}

		sResources = getResources();
		int ii;
		NavitGraphics.dl_thread_cur = 0;
		for (ii = 0; ii < NavitGraphics.dl_thread_max; ii++)
		{
			NavitGraphics.dl_thread[ii] = null;
		}

		String font_file_name = "Roboto-Regular.ttf"; // "LiberationSans-Regular.ttf";
		NavitStreetnameFont = Typeface.createFromAsset(getBaseContext().getAssets(), font_file_name);

		boolean navit_maps_loaded = false;


		int startup_status = Navit_Status_NORMAL_STARTUP;

		// setup graphics objects
		// setup graphics objects
		NG__vehicle = new NavitGraphics(this, 1, 0, 0, 50, 50, 65535, 0, 0);
		NG__map_main = new NavitGraphics(this, 0, 0, 0, 100, 100, 0, 0, 0);
		// setup graphics objects
		// setup graphics objects

		NV = new NavitVehicle(this);
		NSp = new NavitSpeech2(this);

		// init translated text ------------------------------------
		// NavitTextTranslations.init();
		final Runnable r = new Runnable()
		{
			public void run()
			{
				NavitTextTranslations.init();
			}
		};
		ThreadGroup group = new ThreadGroup("Group1");
		new Thread(group, r, "ZTransInit1", 100000).start(); // use 0.1MByte stack
		// init translated text ------------------------------------

		// set the new locale here -----------------------------------
		getPrefsLocale();
		activatePrefsLocale();
		// set the new locale here -----------------------------------

		// get the local language -------------
		Locale locale = java.util.Locale.getDefault();
		String lang = locale.getLanguage();
		String langu = lang;
		String langc = lang;
		Log.e(TAG, "lang=" + lang);
		int pos = langu.indexOf('_');
		if (pos != -1)
		{
			langc = langu.substring(0, pos);
			langu = langc + langu.substring(pos).toUpperCase(locale);
			Log.e(TAG, "substring lang " + langu.substring(pos).toUpperCase(locale));
			// set lang. for translation
			NavitTextTranslations.main_language = langc;
			NavitTextTranslations.sub_language = langu.substring(pos).toUpperCase(locale);
		}
		else
		{
			String country = locale.getCountry();
			Log.e(TAG, "Country1 " + country);
			Log.e(TAG, "Country2 " + country.toUpperCase(locale));
			langu = langc + "_" + country.toUpperCase(locale);
			// set lang. for translation
			NavitTextTranslations.main_language = langc;
			NavitTextTranslations.sub_language = country.toUpperCase(locale);
		}
		Log.e(TAG, "Language " + lang);
		// get the local language -------------

		TextView no_maps_text = this.findViewById(R.id.no_maps_text);
		no_maps_text.setText("\n\n\n" + Navit.get_text("No Maps installed") + "\n" + Navit.get_text("Please download a map") + "\n\n");

		try
		{
			try
			{
				no_maps_text.setVisibility(View.INVISIBLE);
			}
			catch (NoSuchMethodError e)
			{
			}

			try
			{
				no_maps_text.setActivated(false);
			}
			catch (NoSuchMethodError e)
			{
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		//		}

		// no_maps_text.postInvalidate();

		// set map cache size here -----------------------------------
		getPrefs_mapcache();
		activatePrefs_mapcache();
		// set map cache size here -----------------------------------

		// get map data dir and set it -----------------------------
		getPrefs_mapdir();
		activatePrefs_mapdir(true);
		// get map data dir and set it -----------------------------

		// get special prefs here ------------------------------------
		get_prefs_highdpi();
		// get special prefs here ------------------------------------

		// make sure the new path for the navitmap.bin file(s) exist!!
		File navit_maps_dir = new File(sNavitMapDirectory);
		navit_maps_dir.mkdirs();
		// create nomedia files
		File nomedia_file = new File(sNavitMapDirectory + ".nomedia");
		try
		{
			nomedia_file.createNewFile();
		}
		catch (Exception e1)
		{
			e1.printStackTrace();
		}
		// create nomedia files

		// check if we already have a borders.bin file (if not, then extract the included simplified one)
		File b_ = new File(sNavitMapDirectory + "/borders.bin");
		try
		{
			if (!b_.exists())
			{
				try
				{
					File c_ = new File(getMapMD5path() + "/borders.bin.md5");
					c_.delete();
				}
				catch (Exception e2)
				{

				}
				Log.e(TAG, "trying to extract borders simple resource to:" + sNavitMapDirectory + "/borders.bin");
				if (!extractRes("borders_simple", sNavitMapDirectory + "/borders.bin"))
				{
					Log.e(TAG, "Failed to extract borders simple resource to:" + sNavitMapDirectory + "/borders.bin");
				}
			}
		}
		catch (Exception e)
		{

		}
		// check if we already have a borders.bin file

		// make sure the new path for config files exist
		File navit_cfg_dir = new File(getCFGpath());
		navit_cfg_dir.mkdirs();

		// make sure the new path for the navitmap.bin file(s) exist!!
		File navit_mapsmd5_dir = new File(getMapMD5path());
		navit_mapsmd5_dir.mkdirs();

		// make sure the share dir exists, otherwise the infobox will not show
		File navit_data_share_dir = new File(NAVIT_DATA_SHARE_DIR);
		navit_data_share_dir.mkdirs();

		File dd = new File(getNAVIT_DATA_DEBUG_DIR());
		dd.mkdirs();

		// try to create cat. file if it does not exist
		File navit_maps_catalogue = new File(getCFGpath() + NavitMapDownloader.CAT_FILE);
		if (!navit_maps_catalogue.exists())
		{
			FileOutputStream fos_temp;
			try
			{
				fos_temp = new FileOutputStream(navit_maps_catalogue);
				fos_temp.write((NavitMapDownloader.MAP_CAT_HEADER + "\n").getBytes()); // just write header to the file
				fos_temp.flush();
				fos_temp.close();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}

		// ---------- downloader threads ----------------
		PackageInfo pkgInfo;
		try
		{
			// is the donate version installed?
			pkgInfo = getPackageManager().getPackageInfo("com.zoffcc.applications.zanavi_donate", 0);
			String sharedUserId = pkgInfo.sharedUserId;
			System.out.println("str nd=" + sharedUserId);
			if (sharedUserId.equals("com.zoffcc.applications.zanavi"))
			{
				System.out.println("##bonus 001##");
				Navit_DonateVersion_Installed = true;
				NavitMapDownloader.MULTI_NUM_THREADS = NavitMapDownloader.MULTI_NUM_THREADS_MAX;
			}
		}
		catch (NameNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		try
		{
			if (get_reglevel() == 1)
			{
				System.out.println("##U:bonus 001##");
				Navit_DonateVersion_Installed = true;
				NavitMapDownloader.MULTI_NUM_THREADS = NavitMapDownloader.MULTI_NUM_THREADS_MAX;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		try
		{
			// is the "large map" donate version installed?
			pkgInfo = getPackageManager().getPackageInfo("com.zoffcc.applications.zanavi_largemap_donate", 0);
			String sharedUserId = pkgInfo.sharedUserId;
			System.out.println("str lm=" + sharedUserId);

			if (sharedUserId.equals("com.zoffcc.applications.zanavi"))
			{
				System.out.println("##bonus 002##");
				Navit_DonateVersion_Installed = true;
				Navit_Largemap_DonateVersion_Installed = true;
				NavitMapDownloader.MULTI_NUM_THREADS = NavitMapDownloader.MULTI_NUM_THREADS_MAX;
			}
		}
		catch (NameNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		try
		{
			if (get_reglevel() == 1)
			{
				System.out.println("##U:bonus 002##");
				Navit_DonateVersion_Installed = true;
				Navit_Largemap_DonateVersion_Installed = true;
				NavitMapDownloader.MULTI_NUM_THREADS = NavitMapDownloader.MULTI_NUM_THREADS_MAX;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		// update map list
		NavitMapDownloader.init_maps_without_donate_largemaps();
		// ---------- downloader threads ----------------


		if (Navit.metrics.densityDpi >= 320) //&& (PREF_shrink_on_high_dpi))
		{
			menu_button = BitmapFactory.decodeResource(getResources(), R.drawable.menu_001);
		}
		else
		{
			menu_button = BitmapFactory.decodeResource(getResources(), R.drawable.menu_001_small);
		}

		follow_current = true;

		if ((Navit.metrics.densityDpi >= 320) && (preferences.PREF_shrink_on_high_dpi))
		{
			float factor;
			factor = (float) NavitGraphics.Global_Scaled_DPI_normal / (float) Navit.metrics.densityDpi;
			factor = factor * 1.7f;
			//
			BitmapFactory.Options o = new BitmapFactory.Options();
			o.inDither = true;
			//o.inScaled = true;
			//o.inTargetDensity = NavitGraphics.Global_Scaled_DPI_normal;
			nav_arrow_stopped = BitmapFactory.decodeResource(getResources(), R.drawable.navigation_arrow_stopped, o);
			nav_arrow_moving = BitmapFactory.decodeResource(getResources(), R.drawable.navigation_arrow_moving, o);
			nav_arrow_moving_grey = BitmapFactory.decodeResource(getResources(), R.drawable.navigation_arrow_moving_grey, o);
			nav_arrow_moving_shadow = BitmapFactory.decodeResource(getResources(), R.drawable.navigation_arrow_moving_shadow, o);

			nav_arrow_stopped_small = Bitmap.createScaledBitmap(Navit.nav_arrow_stopped, (int) (Navit.nav_arrow_stopped.getWidth() / NavitGraphics.strech_factor_3d_map * factor), (int) (Navit.nav_arrow_stopped.getHeight() / NavitGraphics.strech_factor_3d_map * factor), true);
			nav_arrow_moving_small = Bitmap.createScaledBitmap(Navit.nav_arrow_moving, (int) (Navit.nav_arrow_moving.getWidth() / NavitGraphics.strech_factor_3d_map * factor), (int) (Navit.nav_arrow_moving.getHeight() / NavitGraphics.strech_factor_3d_map * factor), true);
			nav_arrow_moving_shadow_small = Bitmap.createScaledBitmap(Navit.nav_arrow_moving_shadow, (int) (Navit.nav_arrow_moving_shadow.getWidth() / NavitGraphics.strech_factor_3d_map * factor), (int) (Navit.nav_arrow_moving_shadow.getHeight() / NavitGraphics.strech_factor_3d_map * factor), true);
		}
		else
		{
			nav_arrow_stopped = BitmapFactory.decodeResource(getResources(), R.drawable.navigation_arrow_stopped);
			nav_arrow_moving = BitmapFactory.decodeResource(getResources(), R.drawable.navigation_arrow_moving);
			nav_arrow_moving_grey = BitmapFactory.decodeResource(getResources(), R.drawable.navigation_arrow_moving_grey);
			nav_arrow_moving_shadow = BitmapFactory.decodeResource(getResources(), R.drawable.navigation_arrow_moving_shadow);

			nav_arrow_stopped_small = Bitmap.createScaledBitmap(Navit.nav_arrow_stopped, (int) (Navit.nav_arrow_stopped.getWidth() / NavitGraphics.strech_factor_3d_map), (int) (Navit.nav_arrow_stopped.getHeight() / NavitGraphics.strech_factor_3d_map), true);
			nav_arrow_moving_small = Bitmap.createScaledBitmap(Navit.nav_arrow_moving, (int) (Navit.nav_arrow_moving.getWidth() / NavitGraphics.strech_factor_3d_map), (int) (1.5 * Navit.nav_arrow_moving.getHeight() / NavitGraphics.strech_factor_3d_map), true);
			nav_arrow_moving_shadow_small = Bitmap.createScaledBitmap(Navit.nav_arrow_moving_shadow, (int) (Navit.nav_arrow_moving_shadow.getWidth() / NavitGraphics.strech_factor_3d_map), (int) (1.5 * Navit.nav_arrow_moving_shadow.getHeight() / NavitGraphics.strech_factor_3d_map), true);
		}

		zoomin = BitmapFactory.decodeResource(getResources(), R.drawable.zoom_in_32_32);
		zoomout = BitmapFactory.decodeResource(getResources(), R.drawable.zoom_out_32_32);

		//Navit.oneway_arrow = BitmapFactory.decodeResource(getResources(), R.drawable.oneway);
		oneway_arrow = BitmapFactory.decodeResource(getResources(), R.drawable.oneway_large);
		oneway_bicycle_arrow = BitmapFactory.decodeResource(getResources(), R.drawable.oneway_bicycle_large);

		// *******************
		// *******************
		// check/init the catalogue file for downloaded maps
		NavitMapDownloader.init_cat_file();
		// *******************
		// *******************

		boolean xmlconfig_unpack_file;
		boolean write_new_version_file = false;
		try
		{
			NavitAppVersion = "" + this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionCode;
			NavitAppVersion_string = "" + this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName;
		}
		catch (NameNotFoundException e)
		{
			e.printStackTrace();
			NavitAppVersion = "1";
			NavitAppVersion_string = "1";
		}
		catch (Exception e)
		{
			e.printStackTrace();
			NavitAppVersion = "2";
			NavitAppVersion_string = "2";
		}

		String navitAppVersion_prev = "-1";
		try
		{
			File navit_version = new File(VERSION_FILE);
			if (!navit_version.exists())
			{
				System.out.println("version file does not exist");
				navitAppVersion_prev = "-1";
				write_new_version_file = true;
			}
			else
			{
				// files exists, read in the prev. verison number
				System.out.println("version file is here");
				FileInputStream fos_temp;
				byte[] buffer = new byte[101];
				fos_temp = new FileInputStream(navit_version);
				int len = fos_temp.read(buffer, 0, 100);
				if (len != -1)
				{
					// use only len bytes to make the string (the rest is garbage!!)
					navitAppVersion_prev = new String(buffer).substring(0, len);
				}
				else
				{
					navitAppVersion_prev = "-1";
					write_new_version_file = true;
				}
				fos_temp.close();
			}

		}
		catch (Exception e)
		{
			navitAppVersion_prev = "-1";
			write_new_version_file = true;
			e.printStackTrace();
		}

		System.out.println("vprev:" + navitAppVersion_prev + " vcur:" + NavitAppVersion);

		intro_flag_update = false;
		if (navitAppVersion_prev.compareTo(NavitAppVersion) != 0)
		{
			// different version
			System.out.println("different version!!");
			write_new_version_file = true;
			xmlconfig_unpack_file = true;

			//if ((NavitAppVersion_prev.compareTo("-1") != 0) && (NavitAppVersion.compareTo("-1") != 0))
			//{
			// user has upgraded to a new version of ZANavi
			startup_status = Navit_Status_UPGRADED_TO_NEW_VERSION;
			intro_flag_update = true;
			//}
		}
		else
		{
			// same version
			System.out.println("same version");
			xmlconfig_unpack_file = false;
		}

		// write new version file
		if (write_new_version_file)
		{
			try
			{
				System.out.println("write version file");
				FileOutputStream fos_temp;
				File navit_version = new File(VERSION_FILE);
				navit_version.delete();
				fos_temp = new FileOutputStream(navit_version);
				fos_temp.write(NavitAppVersion.getBytes());
				fos_temp.flush();
				fos_temp.close();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}

		// Sample useragent strings:
		//
		//		Mozilla/5.0 (Windows NT 6.1; WOW64; rv:7.0a1) Gecko/20110616 Firefox/7.0a1 SeaMonkey/2.4a1
		//		Dalvik/1.4.0 (Linux; U; Android 2.3.3; GT-I9100 Build/GINGERBREAD)
		//		Dalvik/1.2.0 (Linux; U; Android 2.2.1; GT-S5830 Build/FROYO)
		//		Dalvik/1.4.0 (Linux; U; Android 2.3.3; HTC Desire S Build/GRI40)
		//		Dalvik/1.2.0 (Linux; U; Android 2.2.2; MB525 Build/3.4.2-179)
		//		Dalvik/1.4.0 (Linux; U; Android 2.3.3; HTC Wildfire S A510e Build/GRI40)
		//		Wget/1.10.2
		//		Dalvik/1.4.0 (Linux; U; Android 2.3.3; sdk Build/GRI34)
		//		Dalvik/1.2.0 (Linux; U; Android 2.2.2; MB525 Build/3.4.2-164)
		//		Dalvik/1.2.0 (Linux; U; Android 2.2; GT-I9000 Build/FROYO)
		//		Dalvik/1.2.0 (Linux; U; Android 2.2.1; GT-S5570L Build/FROYO)
		//		Dalvik/1.2.0 (Linux; U; Android 2.2.1; GT-I9000 Build/FROYO)
		//		Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; .NET CLR 1.1.4322; InfoPath.1)
		//String BOARD = android.os.Build.BOARD; //The name of the underlying board, like "goldfish".
		//String BOOTLOADER = android.os.Build.BOOTLOADER; //  The system bootloader version number.
		String BRAND = android.os.Build.BRAND; //The brand (e.g., carrier) the software is customized for, if any.
		//String CPU_ABI = android.os.Build.CPU_ABI; //The name of the instruction set (CPU type + ABI convention) of native code.
		//String CPU_ABI2 = android.os.Build.CPU_ABI2; //  The name of the second instruction set (CPU type + ABI convention) of native code.
		String DEVICE = android.os.Build.DEVICE; //  The name of the industrial design.
		String DISPLAY = android.os.Build.DISPLAY; //A build ID string meant for displaying to the user
		//String FINGERPRINT = android.os.Build.FINGERPRINT; //A string that uniquely identifies this build.
		//String HARDWARE = android.os.Build.HARDWARE; //The name of the hardware (from the kernel command line or /proc).
		//String HOST = android.os.Build.HOST;
		//String ID = android.os.Build.ID; //Either a changelist number, or a label like "M4-rc20".
		String MANUFACTURER = android.os.Build.MANUFACTURER; //The manufacturer of the product/hardware.
		//String MODEL = android.os.Build.MODEL; //The end-user-visible name for the end product.
		//String PRODUCT = android.os.Build.PRODUCT; //The name of the overall product.
		//String RADIO = android.os.Build.RADIO; //The radio firmware version number.
		//String TAGS = android.os.Build.TAGS; //Comma-separated tags describing the build, like "unsigned,debug".
		//String TYPE = android.os.Build.TYPE; //The type of build, like "user" or "eng".
		//String USER = android.os.Build.USER;

		String android_version = "Android " + android.os.Build.VERSION.SDK;
		String android_device = MANUFACTURER + " " + BRAND + " " + DEVICE;

		if (MANUFACTURER.equalsIgnoreCase("amazon"))
		{
			// we are on amazon device
			ZANaviNormalDonateActivity.on_amazon_device = true;
		}

		// debug
		// debug
		// android_device = "telechips telechips m801";
		// debug
		// debug

		String android_rom_name = DISPLAY;

		if (FDBL)
		{
			android_rom_name = android_rom_name + "; FD";
		}

		String userAgentString = null;
		if (Navit_DonateVersion_Installed == false)
		{
			userAgentString = "Mozilla/5.0 (Linux; U; " + "Z" + NavitAppVersion + "; " + android_version + "; " + android_device + " " + android_rom_name + ")";
			UserAgentString_bind = "Mozilla/5.0 @__THREAD__@ (Linux; U; " + "Z" + NavitAppVersion + "; " + android_version + "; " + android_device + " " + android_rom_name + ")";
		}
		else
		{
			if (Navit_Largemap_DonateVersion_Installed == false)
			{
				userAgentString = "Mozilla/5.0 (Linux; U; " + "donateZ" + NavitAppVersion + "; " + android_version + "; " + android_device + " " + android_rom_name + ")";
				UserAgentString_bind = "Mozilla/5.0 @__THREAD__@ (Linux; U; " + "donateZ" + NavitAppVersion + "; " + android_version + "; " + android_device + " " + android_rom_name + ")";
			}
			else

			{
				userAgentString = "Mozilla/5.0 (Linux; U; " + "LMdonateLMZ" + NavitAppVersion + "; " + android_version + "; " + android_device + " " + android_rom_name + ")";
				UserAgentString_bind = "Mozilla/5.0 @__THREAD__@ (Linux; U; " + "LMdonateLMZ" + NavitAppVersion + "; " + android_version + "; " + android_device + " " + android_rom_name + ")";
			}
		}
		// System.out.println("UA=" + UserAgentString);

		// --------- enable GPS ? --------------
		// --------- enable GPS ? --------------
		//		try
		//		{
		//			final LocationManager llmanager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		//			if (!llmanager.isProviderEnabled(LocationManager.GPS_PROVIDER))
		//			{
		//				buildAlertMessageNoGps();
		//			}
		//		}
		//		catch (Exception e)
		//		{
		//			e.printStackTrace();
		//		}
		// --------- enable GPS ? --------------
		// --------- enable GPS ? --------------


		try
		{
			sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		}
		catch (Exception e3)
		{
			e3.printStackTrace();
		}

		//		try
		//		{
		//			vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
		//		}
		//		catch (Exception e)
		//		{
		//			e.printStackTrace();
		//		}
		//sensorManager_ = sensorManager;

		// light sensor -------------------
		try
		{
			lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
			float lightsensor_max_value = lightSensor.getMaximumRange();
			lightSensorEventListener = new SensorEventListener()
			{
				@Override
				public void onAccuracyChanged(Sensor sensor, int accuracy)
				{
				}

				@Override
				public void onSensorChanged(SensorEvent event)
				{
					try
					{
						if (preferences.PREF_auto_night_mode)
						{
							if (event.sensor.getType() == Sensor.TYPE_LIGHT)
							{

								if (Navit.DEBUG_LUX_VALUE)
								{
									debug_cur_lux_value = event.values[0];
									NavitGraphics.NavitAOverlay_s.postInvalidate();
								}

								// System.out.println("Current Reading(Lux): cur=" + event.values[0] + " max=" + lightsensor_max_value);

								if (night_mode == false)
								{
									if (event.values[0] < preferences.PREF_night_mode_lux)
									{
										night_mode = true;
										set_night_mode(1);
										draw_map();
									}
								}
								// night_mode == true
								else if (event.values[0] > (preferences.PREF_night_mode_lux + preferences.PREF_night_mode_buffer))
								{
									night_mode = false;
									set_night_mode(0);
									draw_map();
								}
							}
						}
						else
						{
							try
							{
								sensorManager.unregisterListener(lightSensorEventListener);
								System.out.println("stop lightsensor");
							}
							catch (Exception e)
							{
							}

							try
							{
								night_mode = false;
								set_night_mode(0);
								draw_map();
							}
							catch (Exception e)
							{
							}
						}
					}
					catch (Exception e)
					{
						// e.printStackTrace();
					}
				}

			};
		}
		catch (Exception e)
		{
		}
		// light sensor -------------------

		generic_alert_box = new AlertDialog.Builder(this);
		/*
		 * show info box for first time users
		 */
		AlertDialog.Builder infobox = new AlertDialog.Builder(this);
		//. english text: Welcome to ZANavi
		infobox.setTitle(Navit.get_text("__INFO_BOX_TITLE__")); //TRANS
		infobox.setCancelable(false);
		final TextView message = new TextView(this);
		message.setFadingEdgeLength(20);
		message.setVerticalFadingEdgeEnabled(true);
		message.setPadding(10, 5, 10, 5);
		message.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
		message.setGravity(Gravity.LEFT);
		// message.setScrollBarStyle(TextView.SCROLLBARS_INSIDE_OVERLAY);
		// message.setVerticalScrollBarEnabled(true);
		RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.FILL_PARENT);
		rlp.leftMargin = 7;
		rlp.rightMargin = 7;

		Navit.Navit_Geocoder = null;
		try
		{
			// for online search
			Navit.Navit_Geocoder = new Geocoder(this);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		boolean info_popup_seen_count_end = false;
		File navit_first_startup = new File(FIRST_STARTUP_FILE);
		// if file does NOT exist, show the info box
		if (!navit_first_startup.exists())
		{
			// set first-ever-startup flag
			first_ever_startup = true;
			info_popup_seen_count_end = true; // don't show on first ever start of the app
			startup_status = Navit_Status_COMPLETE_NEW_INSTALL;
			FileOutputStream fos_temp;
			try
			{
				info_popup_seen_count++;
				fos_temp = new FileOutputStream(navit_first_startup);
				fos_temp.write(info_popup_seen_count); // use to store info popup seen count
				fos_temp.flush();
				fos_temp.close();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		else
		{
			FileOutputStream fos_temp;
			FileInputStream fis_temp;
			try
			{
				fis_temp = new FileInputStream(navit_first_startup);
				info_popup_seen_count = fis_temp.read();
				fis_temp.close();

				if (info_popup_seen_count < 0)
				{
					info_popup_seen_count = 0;
				}

				// we wrote "A" -> (int)65 previously, so account for that
				if (info_popup_seen_count == 65)
				{
					info_popup_seen_count = 0;
				}

				if (info_popup_seen_count > info_popup_seen_count_max)
				{
					info_popup_seen_count_end = true;
				}
				else
				{
					info_popup_seen_count++;
					fos_temp = new FileOutputStream(navit_first_startup);
					fos_temp.write(info_popup_seen_count); // use to store info popup seen count
					fos_temp.flush();
					fos_temp.close();
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		/*
		 * show info box for first time users
		 */

		//
		// ----------- info popup
		// ----------- info popup
		// ----------- info popup
		// ----------- info popup
		//

		intro_flag_info = false;
		if ((!info_popup_seen_count_end) && (startup_status == Navit_Status_NORMAL_STARTUP))
		{
			intro_flag_info = true;
		}

		System.out.println("info_popup_seen_count=" + info_popup_seen_count);
		System.out.println("info_popup_seen_count_end=" + info_popup_seen_count_end + " intro_flag_info=" + intro_flag_info + " startup_status=" + startup_status);

		// make handler statically available for use in "msg_to_msg_handler"
		Navit_progress_h = this.progress_handler;

		//		try
		//		{
		//			Navit.bigmap_bitmap = BitmapFactory.decodeResource(getResources(), R.raw.bigmap_colors_zanavi2);
		//		}
		//		catch (Exception e)
		//		{
		//			// when not enough memory is available, then disable large world overview map!
		//			System.gc();
		//			Navit.bigmap_bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
		//		}
		//		// ------no----- // Navit.bigmap_bitmap.setDensity(120); // set our dpi!!

		try
		{
			setVolumeControlStream(AudioManager.STREAM_MUSIC);
			ActivityResults = new NavitActivityResult[16];
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		try
		{
			sNavitAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		PowerManager pm = null;
		try
		{
			pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		try
		{
			// -- // pm.wakeUp(SystemClock.uptimeMillis()); // -- //
			// **screen always full on** // wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, "NavitDoNotDimScreen");
			// **screen can go off, cpu will stay on** // wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, "NavitDoNotDimScreen");

			// this works so far, lets the screen dim, but it cpu and screen stays on
			wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, "Navit:DoNotDimScreen");
		}
		catch (Exception e)
		{
			e.printStackTrace();
			wl = null;
		}

		try
		{
			wl_cpu = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "ZANavi:NeedCpu");
		}
		catch (Exception e)
		{
			e.printStackTrace();
			wl_cpu = null;
		}

		try
		{
			wl_navigating = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "ZANavi:NavigationOn");
		}
		catch (Exception e)
		{
			Log.e(TAG, "WakeLock NAV: create failed!!");
			e.printStackTrace();
			wl_navigating = null;
		}

		//		try
		//		{
		//			if (wl_navigating != null)
		//			{
		//				wl_navigating.acquire();
		//				Log.e(TAG, "WakeLock NAV: acquire 00");
		//			}
		//		}
		//		catch (Exception e)
		//		{
		//			Log.e(TAG, "WakeLock NAV: something wrong 00");
		//			e.printStackTrace();
		//		}

		//		try
		//		{
		//			if (wl != null)
		//			{
		//				try
		//				{
		//					wl.release();
		//				}
		//				catch (Exception e2)
		//				{
		//				}
		//				wl.acquire();
		//				Log.e(TAG, "WakeLock: acquire 1");
		//			}
		//		}
		//		catch (Exception e)
		//		{
		//			e.printStackTrace();
		//		}

		// -- extract overview maps --
		// -- extract overview maps --

		File navit_worldmap2_file = new File(sNavitMapDirectory + "/worldmap2.txt");
		if (!navit_worldmap2_file.exists())
		{
			if (!extractRes("worldmap2",  sNavitMapDirectory + "/worldmap2.txt"))
			{
				Log.e(TAG, "Failed to extract worldmap2.txt");
			}
		}

		File navit_worldmap5_file = new File(sNavitMapDirectory + "/worldmap5.txt");
		if (!navit_worldmap5_file.exists())
		{
			if (!extractRes("worldmap5", sNavitMapDirectory + "/worldmap5.txt"))
			{
				Log.e(TAG, "Failed to extract worldmap5.txt");
			}
		}
		// -- extract overview maps --
		// -- extract overview maps --

		Log.e(TAG, "trying to extract language resource " + NavitTextTranslations.main_language + "_" + NavitTextTranslations.sub_language);
		if (!extractRes(NavitTextTranslations.main_language + "_" + NavitTextTranslations.sub_language, NAVIT_DATA_DIR + "/locale/" + NavitTextTranslations.main_language + "_" + NavitTextTranslations.sub_language + "/LC_MESSAGES/navit.mo"))
		{
			Log.e(TAG, "Failed to extract language resource " + NavitTextTranslations.main_language + "_" + NavitTextTranslations.sub_language);
		}

		Log.e(TAG, "trying to extract language resource " + NavitTextTranslations.main_language + "_" + NavitTextTranslations.sub_language.toLowerCase());
		if (!extractRes(NavitTextTranslations.main_language + "_" + NavitTextTranslations.sub_language.toLowerCase(), NAVIT_DATA_DIR + "/locale/" + NavitTextTranslations.main_language + "_" + NavitTextTranslations.sub_language + "/LC_MESSAGES/navit.mo"))
		{
			Log.e(TAG, "Failed to extract language resource " + NavitTextTranslations.main_language + "_" + NavitTextTranslations.sub_language.toLowerCase());
		}

		Log.e(TAG, "trying to extract language resource " + NavitTextTranslations.main_language);
		if (!extractRes(NavitTextTranslations.main_language, NAVIT_DATA_DIR + "/locale/" + NavitTextTranslations.main_language + "/LC_MESSAGES/navit.mo"))
		{
			Log.e(TAG, "Failed to extract language resource " + NavitTextTranslations.main_language);
		}


		File navit_config_xml_file = new File(NAVIT_DATA_SHARE_DIR + "/navit.xml");
		if ((!navit_config_xml_file.exists()) || (NAVIT_ALWAYS_UNPACK_XMLFILE))
		{
			xmlconfig_unpack_file = true;
			Log.e(TAG, "navit.xml does not exist, unpacking in any case");
		}

		my_display_density = "mdpi";
		// ldpi display (120 dpi)

		NavitGraphics.Global_want_dpi = Navit.metrics.densityDpi;
		NavitGraphics.Global_want_dpi_other = Navit.metrics.densityDpi;

		if (Navit.metrics.densityDpi <= 120)
		{
			my_display_density = "ldpi";
			if (xmlconfig_unpack_file)
			{
				if (!extractRes("navitldpi", NAVIT_DATA_SHARE_DIR + "/navit.xml"))
				{
					Log.e(TAG, "Failed to extract navit.xml for ldpi device(s)");
				}
			}
		}
		// mdpi display (160 dpi)
		else if ((Navit.metrics.densityDpi <= 160))
		{
			my_display_density = "mdpi";
			if (xmlconfig_unpack_file)
			{
				if (!extractRes("navitmdpi", NAVIT_DATA_SHARE_DIR + "/navit.xml"))
				{
					Log.e(TAG, "Failed to extract navit.xml for mdpi device(s)");
				}
			}
		}
		// hdpi display (240 dpi)
		else if ((Navit.metrics.densityDpi < 320))
		//else if (Navit.metrics.densityDpi == 240)
		{
			my_display_density = "hdpi";
			if (xmlconfig_unpack_file)
			{
				if (!extractRes("navithdpi", NAVIT_DATA_SHARE_DIR + "/navit.xml"))
				{
					Log.e(TAG, "Failed to extract navit.xml for hdpi device(s)");
				}
			}
		}
		// xhdpi display >=320 dpi
		else
		{
			// set the map display DPI down. otherwise everything will be very small and unreadable
			// and performance will be very low
			if (preferences.PREF_shrink_on_high_dpi)
			{
				NavitGraphics.Global_want_dpi = NavitGraphics.Global_Scaled_DPI_normal;
			}
			NavitGraphics.Global_want_dpi_other = NavitGraphics.Global_Scaled_DPI_normal;

			Log.e(TAG, "found xhdpi device, this is not fully supported yet");
			Log.e(TAG, "using hdpi values for compatibility");
			my_display_density = "hdpi";
			if (xmlconfig_unpack_file)
			{
				if (!extractRes("navithdpi", NAVIT_DATA_SHARE_DIR + "/navit.xml"))
				{
					Log.e(TAG, "Failed to extract navit.xml for xhdpi device(s)");
				}
			}
		}

		int have_dpi = Navit.metrics.densityDpi;

		System.out.println("Global_want_dpi[001]=" + NavitGraphics.Global_want_dpi + ":" + Navit.metrics.densityDpi + ":" + NavitGraphics.Global_dpi_factor + ":" + NavitGraphics.Global_dpi_factor_better);

		if (NavitGraphics.Global_want_dpi == have_dpi)
		{
			NavitGraphics.Global_dpi_factor = 1;
			NavitGraphics.preview_coord_factor = 1;
		}
		else
		// this was missing??????!!!!!!!!!??????!!!!!!
		{
			NavitGraphics.Global_dpi_factor = ((float) NavitGraphics.Global_want_dpi / (float) have_dpi);
			NavitGraphics.preview_coord_factor = ((float) have_dpi / (float) NavitGraphics.Global_want_dpi);
		}

		System.out.println("Global_want_dpi[002]=" + NavitGraphics.Global_dpi_factor + ":" + NavitGraphics.preview_coord_factor);

		// --> dont use!! NavitMain(this, langu, android.os.Build.VERSION.SDK_INT);
		Log.e(TAG, "android.os.Build.VERSION.SDK_INT=" + Integer.valueOf(android.os.Build.VERSION.SDK));

		// -- report share dir back to C-code --
		//Message msg2 = new Message();
		//Bundle b2 = new Bundle();
		//b2.putInt("Callback", 82);
		//b2.putString("s", NAVIT_DATA_DIR + "/share/");
		//msg2.setData(b2);
		//N_NavitGraphics.callback_handler.sendMessage(msg2);
		// -- report share dir back to C-code --

		// -- report data dir back to C-code --
		//msg2 = new Message();
		//b2 = new Bundle();
		//b2.putInt("Callback", 84);
		//b2.putString("s", NAVIT_DATA_DIR + "/");
		//msg2.setData(b2);
		//N_NavitGraphics.callback_handler.sendMessage(msg2);
		// -- report share dir back to C-code --

		draw_osd_thread = new drawOSDThread();
		draw_osd_thread.start();

		cwthr = new CWorkerThread();
		cwthr.start();

		// --new--
		cwthr.StartMain(langu, "" + Navit.metrics.densityDpi, NAVIT_DATA_DIR, NAVIT_DATA_SHARE_DIR);

		Navit.show_mem_used();

		/*
		 * GpsStatus.Listener listener = new GpsStatus.Listener()
		 * {
		 * public void onGpsStatusChanged(int event)
		 * {
		 * //System.out.println("xxxxx");
		 * if (event == GpsStatus.GPS_EVENT_SATELLITE_STATUS)
		 * {
		 * }
		 * }
		 * };
		 */

		try
		{
			Intent sintent = new Intent();
			sintent.setPackage("com.zoffcc.applications.zanavi_msg");
			sintent.setAction("com.zoffcc.applications.zanavi_msg.ZanaviCloudService");
			// ComponentName cname = startService(sintent);
			// Log.i("NavitPlugin", "start Service res=" + cname);
			// System.out.println("NavitPlugin:bind to Service");
			boolean res_bind = bindService(sintent, serviceConnection, Context.BIND_AUTO_CREATE);
			// Log.i("NavitPlugin", "bind to Service res=" + res_bind);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		// ------------- get all the flags for intro pages -------------
		// ------------- get all the flags for intro pages -------------
		// ------------- get all the flags for intro pages -------------
		try
		{
			intro_flag_indexmissing = false;
			allow_use_index_search();
			if (Navit_index_on_but_no_idx_files)
			{
				if (!NavitMapDownloader.download_active_start)
				{
					intro_flag_indexmissing = true;
				}
			}

		}
		catch (Exception e)
		{
		}

		try
		{
			intro_flag_nomaps = false;
			if (!have_maps_installed())
			{
				if ((!NavitMapDownloader.download_active) && (!NavitMapDownloader.download_active_start))
				{
					intro_flag_nomaps = true;
				}
			}
		}
		catch (Exception e)
		{
		}

		intro_flag_firststart = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(PREF_KEY_FIRST_START, true);
		if (intro_flag_firststart)
		{
			intro_flag_update = false;
		}

		boolean intro_flag_permissions = true;
		if (EasyPermissions.hasPermissions(this, perms))
		{
			// have permissions!
			intro_flag_permissions = false;
		}
		else
		{
			// ask for permissions
			intro_flag_permissions = true;
		}
		// ------------- get all the flags for intro pages -------------
		// ------------- get all the flags for intro pages -------------
		// ------------- get all the flags for intro pages -------------

		// -------------- INTRO --------------
		// -------------- INTRO --------------
		// -------------- INTRO --------------

		int intro_show_count = 0;

		//		// -------------- INTRO --------------
		//		// -------------- INTRO --------------
		//		// -------------- INTRO --------------

		// here the intent just prints some logging msg's --jdg--
		// only take arguments here, onResume gets called all the time (e.g. when screenblanks, etc.)
		Intent startup_intent = this.getIntent();
		Log.e(TAG, "**1**A " + startup_intent.getAction());
		Log.e(TAG, "**1**D " + startup_intent.getDataString());
		Log.e(TAG, "**1**I " + startup_intent.toString());
		try
		{
			Log.e(TAG, "**1**DH E " + startup_intent.getExtras().describeContents());
		}
		catch (Exception ee)
		{
		}

		// --verplaatst van uit onStart()
		getPrefs();
		activatePrefs();

		// --verplaatst van uit onStart()
		// paint for bitmapdrawing on map
		if (preferences.PREF_use_anti_aliasing)
		{
			NavitGraphics.paint_for_map_display.setAntiAlias(true);
		}
		else
		{
			NavitGraphics.paint_for_map_display.setAntiAlias(false);
		}
		if (preferences.PREF_use_map_filtering)
		{
			NavitGraphics.paint_for_map_display.setFilterBitmap(true);
		}
		else
		{
			NavitGraphics.paint_for_map_display.setFilterBitmap(false);
		}

		//PackageInfo pkgInfo;
		try
		{
			// is the donate version installed?
			pkgInfo = getPackageManager().getPackageInfo("com.zoffcc.applications.zanavi_msg", 0);
			String sharedUserId = pkgInfo.sharedUserId;
			System.out.println("str nd=" + sharedUserId);
			if (sharedUserId.equals("com.zoffcc.applications.zanavi"))
			{
				System.out.println("##plugin 001##");
			}
		}
		catch (Exception e)
		{
			Log.e(TAG,"donate version not found" + e.getMessage());
		}

		// restore points
		read_map_points();

		try
		{
			intro_flag_indexmissing = false;
			allow_use_index_search();
			if (Navit_index_on_but_no_idx_files)
			{
				if (!NavitMapDownloader.download_active_start)
				{
					intro_flag_indexmissing = true;
				}
			}

		}
		catch (Exception e)
		{
		}

		intro_flag_firststart = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(PREF_KEY_FIRST_START, true);
		if (intro_flag_firststart)
		{
			intro_flag_update = false;
		}

		try
		{
			intro_flag_nomaps = false;
			if (!have_maps_installed())
			{
				if ((!NavitMapDownloader.download_active) && (!NavitMapDownloader.download_active_start))
				{
					intro_flag_nomaps = true;
				}
			}
		}
		catch (Exception e)
		{
		}

		if (navit_maps_loaded == false)
		{
			navit_maps_loaded = true;
			// activate all maps
			Log.e(TAG, "**** LOAD ALL MAPS **** start");
			sendCallBackMessage(20);
			Log.e(TAG, "**** LOAD ALL MAPS **** end");
		}


		if (EasyPermissions.hasPermissions(this, perms))
		{
			// have permissions!
			intro_flag_permissions = false;
		}
		else
		{
			// ask for permissions
			intro_flag_permissions = true;
		}

		if (Navit.CIDEBUG == 0) // -MAT-INTRO-
		{
			//			intro_flag_nomaps = true;
			//			intro_flag_info = true;
			//			intro_flag_firststart = false;
			//			intro_flag_update = false;
			//			intro_flag_indexmissing = false;
			//  		intro_flag_crash = true;

			if (intro_flag_crash || intro_flag_firststart || intro_flag_indexmissing || intro_flag_info || intro_flag_nomaps || intro_flag_permissions || intro_flag_update)
			{

				System.out.println("flags=" + "intro_flag_crash:" + intro_flag_crash + " intro_flag_firststart:" + intro_flag_firststart + " intro_flag_indexmissing:" + intro_flag_indexmissing + " intro_flag_info:" + intro_flag_info + " intro_flag_nomaps:" + intro_flag_nomaps + " intro_flag_permissions:" + intro_flag_permissions + " intro_flag_update:" + intro_flag_update);

				// intro pages
				System.out.println("ZANaviMainIntroActivity:" + "start count=" + intro_show_count);
				intro_show_count++;
				Intent intent = new Intent(this, ZANaviMainIntroActivityStatic.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
				startActivityForResult(intent, ZANaviIntro_id);
			}
		}

		Navit_maps_too_old = false;
		try
		{
			// draw map no-async
			sendCallBackMessage( 64);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void onStart()
	{
		// if (Navit.METHOD_DEBUG) Navit.my_func_name(0);

		//show_mem_used();

		super.onStart();

		Log.e(TAG, "onStart");

		sun_moon__mLastCalcSunMillis = -1L;
		while (!Global_Init_Finished)
		{
			Log.e(TAG, "onStart:Global_Init_Finished==0 !!!!!");
			try
			{
				Thread.sleep(60, 0); // sleep
			}
			catch (InterruptedException e)
			{
			}
		}

		// activate gps AFTER 3g-location
		// dit doet ook turn_on_sat_status()
		NavitVehicle.turn_on_precise_provider();



		// if (Navit.METHOD_DEBUG) Navit.my_func_name(1);
		if (progressbar_main_activity.getVisibility() == View.VISIBLE)
		{
			progressbar_main_activity.setProgress(0);
			progressbar_main_activity.setVisibility(View.GONE);
		}
		// hide main progress bar ------------

		try
		{
			sensorManager.registerListener(lightSensorEventListener, lightSensor, 8 * 1000000); // updates approx. every 8 seconds
		}
		catch (Exception e)
		{
		}

		// hold all map drawing -----------

		sendCallBackMessage(69);

		Intent intent = this.getIntent();
		handleIntent(intent);
	}

	@Override
	public void onRestart()
	{
		super.onRestart();
		Log.e(TAG, "OnRestart");
	}

	@SuppressLint("NewApi")
	@Override
	public void onResume()
	{
		Log.e(TAG, "OnResume");
		super.onResume();


		is_paused = false;

		Navit_doubleBackToExitPressedOnce = false;

		sAppWindow = getWindow();

		while (!Global_Init_Finished)
		{
			Log.e(TAG, "OnResume:Global_Init_Finished==0 !!!!!");
			try
			{
				Thread.sleep(30, 0); // sleep
			}
			catch (InterruptedException e)
			{
			}
		}

		//InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		//cwthr.NavitActivity2(1); // 1 doet niets --jdg--

		try
		{
			NSp.resume_me();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		NavitVehicle.turn_on_sat_status();

		try
		{
			if (wl != null)
			{
				//				try
				//				{
				//					wl.release();
				//				}
				//				catch (Exception e2)
				//				{
				//				}
				wl.acquire();
				Log.e(TAG, "WakeLock: acquire 2");
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		//Intent caller = this.getIntent();
		//System.out.println("A=" + caller.getAction() + " D=" + caller.getDataString());
		//System.out.println("C=" + caller.getComponent().flattenToString());

		// reset "maps too old" flag


		try
		{
			NavitGraphics.no_maps_container.setVisibility(View.INVISIBLE);

			//			if (!have_maps_installed())
			//			{
			//				// System.out.println("MMMM=no maps installed");
			//				// show semi transparent box "no maps installed" ------------------
			//				// show semi transparent box "no maps installed" ------------------
			//				NavitGraphics.no_maps_container.setVisibility(View.VISIBLE);
			//				try
			//				{
			//					NavitGraphics.no_maps_container.setActivated(true);
			//				}
			//				catch (NoSuchMethodError e)
			//				{
			//				}
			//
			//				show_case_001();
			//
			//				// show semi transparent box "no maps installed" ------------------
			//				// show semi transparent box "no maps installed" ------------------
			//			}
			//			else
			//			{
			//				NavitGraphics.no_maps_container.setVisibility(View.INVISIBLE);
			//				try
			//				{
			//					NavitGraphics.no_maps_container.setActivated(false);
			//				}
			//				catch (NoSuchMethodError e)
			//				{
			//				}
			//			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		sun_moon__mLastCalcSunMillis = -1L;

		//push_pin_view = findViewById(R.id.bottom_slide_left_side);
		//if (preferences.PREF_follow_gps)
		//{
		//	push_pin_view.setImageResource(R.drawable.pin1_down);
		//}
		//else
		//{
		//	push_pin_view.setImageResource(R.drawable.pin1_up);
		//}


		// activate gps AFTER 3g-location
		NavitVehicle.turn_on_precise_provider();

		// allow all map drawing -----------
		try
		{
			sendCallBackMessage(70);
		}
		catch (Exception e)
		{
		}
		// allow all map drawing -----------

		// --- disabled --- NavitVehicle.set_last_known_pos_fast_provider();

		try
		{
			//Simulate = new SimGPS(NavitVehicle.vehicle_handler_);
			//Simulate.start();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		try
		{
			watchmem = new WatchMem();
			watchmem.start();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		// ----- check if we have some index files downloaded -----

		if (Integer.valueOf(android.os.Build.VERSION.SDK) < Build.VERSION_CODES.HONEYCOMB)
		{
			if (Navit.have_maps_installed())
			{
				if (Navit_maps_too_old)
				{
					TextView no_maps_text = this.findViewById(R.id.no_maps_text);
					no_maps_text.setText("\n\n\n" + Navit.get_text("Some Maps are too old!") + "\n" + Navit.get_text("Please update your maps") + "\n\n");

					try
					{
						NavitGraphics.no_maps_container.setVisibility(View.VISIBLE);
						try
						{
							NavitGraphics.no_maps_container.setActivated(true);
						}
						catch (NoSuchMethodError e)
						{
						}
						NavitGraphics.no_maps_container.bringToFront();
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}
				else
				{
					allow_use_index_search();
					if (Navit_index_on_but_no_idx_files)
					{
						TextView no_maps_text = this.findViewById(R.id.no_maps_text);
						no_maps_text.setText("\n\n\n" + Navit.get_text("No Index for some Maps") + "\n" + Navit.get_text("Please update your maps") + "\n\n");

						try
						{
							NavitGraphics.no_maps_container.setVisibility(View.VISIBLE);
							try
							{
								NavitGraphics.no_maps_container.setActivated(true);
							}
							catch (NoSuchMethodError e)
							{
							}
							NavitGraphics.no_maps_container.bringToFront();
						}
						catch (Exception e)
						{
							e.printStackTrace();
						}
					}
					else
					{
						try
						{
							NavitGraphics.no_maps_container.setVisibility(View.INVISIBLE);
							try
							{
								NavitGraphics.no_maps_container.setActivated(false);
							}
							catch (NoSuchMethodError e)
							{
							}
						}
						catch (Exception e)
						{
							e.printStackTrace();
						}
					}
				}
			}
		}
		else
		{
			try
			{
				NavitGraphics.no_maps_container.setVisibility(View.INVISIBLE);
				try
				{
					NavitGraphics.no_maps_container.setActivated(false);
				}
				catch (NoSuchMethodError e)
				{
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		// ----- check if we have some index files downloaded -----

		// ---- DEBUG ----
		// ---- DEBUG ----
		// ---- DEBUG ----
		try
		{
			if (!NavitVehicle.is_pos_recording)
			{
				if (preferences.PREF_enable_debug_write_gpx)
				{
					NavitVehicle.pos_recording_start();
					NavitVehicle.pos_recording_add(0, 0, 0, 0, 0, 0);
				}
			}
		}
		catch (Exception e)
		{
		}
		// ---- DEBUG ----


		if (Navit.CIDEBUG == 1)
		{
			new Thread()
			{
				public void run()
				{
					try
					{
						System.out.println("DR_run_all_yaml_tests --> want");

						if (CIRUN == false)
						{
							System.out.println("DR_run_all_yaml_tests --> do");
							CIRUN = true;
							Thread.sleep(20000); // 20 secs.
							ZANaviDebugReceiver.DR_run_all_yaml_tests();
						}
					}
					catch (Exception e)
					{
					}
				}
			}.start();
		}
	}

	private void handleIntent(Intent intent) {

		Log.e(TAG,"handleIntent");
		String intent_data = null;


		// APP update, Map update
		try
		{
			System.out.println("XXIIXX:111");
			String mid_str = this.getIntent().getExtras().getString("com.zoffcc.applications.zanavi.mid");
			System.out.println("XXIIXX:111a:mid_str=" + mid_str);

			if (mid_str != null)
			{
				if (mid_str.equals("201:UPDATE-APP"))
				{
					// a new ZANavi version is available, show something to the user here -------------------
					// a new ZANavi version is available, show something to the user here -------------------
					return;
				}
				else if (mid_str.startsWith("202:UPDATE-MAP:"))
				{
					System.out.println("need to update1:" + mid_str);
					System.out.println("need to update2:" + mid_str.substring(15));

					auto_start_update_map(mid_str.substring(15));
					return;
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.out.println("XXIIXX:111:EEEE");
		}

		// dit is een raadsel
		try
		{
			System.out.println("XXIIXX:" + this.getIntent());
			Bundle bundle77 = this.getIntent().getExtras();
			System.out.println("XXIIXX:" + Utils.intent_flags_to_string(this.getIntent().getFlags()));
			if (bundle77 == null)
			{
				System.out.println("XXIIXX:" + "null");
			}
			else
			{
				for (String key : bundle77.keySet())
				{
					Object value = bundle77.get(key);
					System.out.println("XXIIXX:" + String.format("%s %s (%s)", key, value.toString(), value.getClass().getName()));
				}
			}
		}
		catch (Exception ee22)
		{
			String exst = Log.getStackTraceString(ee22);
			System.out.println("XXIIXX:ERR:" + exst);
		}
		// ---- Intent dump ----
		// ---- Intent dump ----

		try {
			Log.e(TAG, "handleIntent");
			Log.e(TAG, "**9**A " + intent.getAction());
			Log.e(TAG, "**9**D " + intent.getDataString());

			int type = 1; // default = assume it's a map coords intent

			try {
				String si = intent.getDataString();
				String tmp2 = si.split(":", 2)[0];
				Log.e(TAG, "**9a**A " + intent.getAction());
				Log.e(TAG, "**9a**D " + intent.getDataString() + " " + tmp2);
				if (tmp2.equals("file")) {
					Log.e(TAG, "**9b**D " + intent.getDataString() + " " + tmp2);
					if (si.toLowerCase().endsWith(".gpx")) {
						Log.e(TAG, "**9c**D " + intent.getDataString() + " " + tmp2);
						type = 4;
					}
				}
			} catch (Exception e2) {
			}

			if (type != 4) {
				Bundle extras = intent.getExtras();
				// System.out.println("DH:001");
				if (extras != null) {
					// System.out.println("DH:002");
					long lExtra = extras.getLong("com.zoffcc.applications.zanavi.ZANAVI_INTENT_type");
					// System.out.println("DH:003 l=" + l);
					if (lExtra != 0L) {
						// System.out.println("DH:004");
						if (lExtra == Navit.NAVIT_START_INTENT_DRIVE_HOME) {
							Log.e(TAG,"Drive Home");
							type = 2; // call from drive-home-widget
						}
						// ok, now remove that key
						extras.remove("com.zoffcc.applications.zanavi");
						intent.replaceExtras((Bundle) null);
						// System.out.println("DH:006");
					}
				}
			}

			// ------------------------  BIG LOOP  ------------------------
			// ------------------------  BIG LOOP  ------------------------
			if (type == 2) {
				// drive home

				// check if we have a home location
				int home_id = find_home_point();

				if (home_id != -1) {
					Message msg7 = progress_handler.obtainMessage();
					Bundle b7 = new Bundle();
					msg7.what = 2; // long Toast message
					b7.putString("text", Navit.get_text("driving to Home Location")); //TRANS
					msg7.setData(b7);
					progress_handler.sendMessage(msg7);

					// clear any previous destinations
					sendCallBackMessage(7);

					// set position to middle of screen -----------------------
					//					Message msg67 = new Message();
					//					Bundle b67 = new Bundle();
					//					b67.putInt("Callback", 51);
					//					b67.putInt("x", (int) (NavitGraphics.Global_dpi_factor * Navit.NG__map_main.view.getWidth() / 2));
					//					b67.putInt("y", (int) (NavitGraphics.Global_dpi_factor * Navit.NG__map_main.view.getHeight() / 2));
					//					msg67.setData(b67);
					//					N_NavitGraphics.callback_handler.sendMessage(msg67);

					try {
						Thread.sleep(60);
					} catch (Exception e) {
					}

					Navit.destination_set();

					// set destination to home location
					//					String lat = String.valueOf(map_points.get(home_id).lat);
					//					String lon = String.valueOf(map_points.get(home_id).lon);
					//					String q = map_points.get(home_id).point_name;
					route_wrapper(map_points.get(home_id).point_name, 0, 0, false, map_points.get(home_id).lat, map_points.get(home_id).lon, true);

					final Thread zoom_to_route_001 = new Thread() {
						int wait = 1;
						int count = 0;
						final int max_count = 60;

						@Override
						public void run() {
							while (wait == 1) {
								try {
									if ((NavitGraphics.navit_route_status == 17) || (NavitGraphics.navit_route_status == 33)) {
										zoom_to_route();
										wait = 0;
									} else {
										wait = 1;
									}

									count++;
									if (count > max_count) {
										wait = 0;
									} else {
										Thread.sleep(400);
									}
								} catch (Exception e) {
								}
							}
						}
					};
					zoom_to_route_001.start();

					//					try
					//					{
					//						show_geo_on_screen(Float.parseFloat(lat), Float.parseFloat(lon));
					//					}
					//					catch (Exception e2)
					//					{
					//						e2.printStackTrace();
					//					}

					try {
						Navit.follow_button_on();
					} catch (Exception e2) {
						e2.printStackTrace();
					}
				} else {
					// no home location set
					Message msg = progress_handler.obtainMessage();
					Bundle b = new Bundle();
					msg.what = 2; // long Toast message
					b.putString("text", Navit.get_text("No Home Location set")); //TRANS
					msg.setData(b);
					progress_handler.sendMessage(msg);
				}
			} else if (type == 4) {
				if (intent != null) {
					// Log.e(TAG, "**7**A " + intent.getAction() + System.currentTimeMillis() + " " + Navit.startup_intent_timestamp);
					Log.e(TAG, "**7**A " + intent.getAction());
					Log.e(TAG, "**7**D " + intent.getDataString());
					intent_data = intent.getDataString();
					try {
						intent_data = URLDecoder.decode(intent_data, "UTF-8");
					} catch (Exception e1) {
						e1.printStackTrace();
					}

					if (intent_data != null) {
						// file:///mnt/sdcard/zanavi_pos_recording_347834278.gpx
						String tmp1;
						tmp1 = intent_data.split(":", 2)[1].substring(2);

						Log.e(TAG, "**7**f=" + tmp1);

						// convert gpx file ---------------------
						convert_gpx_file_real(tmp1);
					}

				}
			} else if (type == 1) {
				if (intent != null) {
					Log.e(TAG, "**2**A " + intent.getAction());
					Log.e(TAG, "**2**D " + intent.getDataString());
					intent_data = intent.getDataString();
					if (intent_data != null) {
							// set position to middle of screen -----------------------
							// set position to middle of screen -----------------------
							// set position to middle of screen -----------------------
							//							Message msg67 = new Message();
							//							Bundle b67 = new Bundle();
							//							b67.putInt("Callback", 51);
							//							b67.putInt("x", (int) (NavitGraphics.Global_dpi_factor * Navit.NG__map_main.view.getWidth() / 2));
							//							b67.putInt("y", (int) (NavitGraphics.Global_dpi_factor * Navit.NG__map_main.view.getHeight() / 2));
							//							msg67.setData(b67);
							//							N_NavitGraphics.callback_handler.sendMessage(msg67);
							// set position to middle of screen -----------------------
							// set position to middle of screen -----------------------
							// set position to middle of screen -----------------------
					}

				}

				System.out.println("SUI:000a " + intent_data);

				if ((intent_data != null) && ((substring_without_ioobe(intent_data, 0, 18).equals("google.navigation:")) || (substring_without_ioobe(intent_data, 0, 23).equals("http://maps.google.com/")) || (substring_without_ioobe(intent_data, 0, 24).equals("https://maps.google.com/")))) {

					System.out.println("SUI:000b");

					// better use regex later, but for now to test this feature its ok :-)
					// better use regex later, but for now to test this feature its ok :-)

					// g: google.navigation:///?ll=49.4086,17.4855&entry=w&opt=
					// d: google.navigation:q=blabla-strasse # (this happens when you are offline, or from contacts)
					// b: google.navigation:q=48.25676,16.643
					// a: google.navigation:ll=48.25676,16.643&q=blabla-strasse
					// e: google.navigation:ll=48.25676,16.643&title=blabla-strasse
					//    sample: -> google.navigation:ll=48.026096,16.023993&title=N%C3%B6stach+43%2C+2571+N%C3%B6stach&entry=w
					//            -> google.navigation:ll=48.014413,16.005579&title=Hainfelder+Stra%C3%9Fe+44%2C+2571%2C+Austria&entry=w
					// f: google.navigation:ll=48.25676,16.643&...
					// c: google.navigation:ll=48.25676,16.643
					// h: http://maps.google.com/?q=48.222210,16.387058&z=16
					// i: https://maps.google.com/?q=48.222210,16.387058&z=16
					// i:,h: https://maps.google.com/maps/place?ftid=0x476d07075e933fc5:0xccbeba7fe1e3dd36&q=48.222210,16.387058&ui=maps_mini
					//
					// ??!!new??!!: http://maps.google.com/?cid=10549738100504591748&hl=en&gl=gb

					String lat;
					String lon;
					String q;

					String temp1 = null;
					String temp2 = null;
					String temp3;
					boolean parsable = false;
					boolean unparsable_info_box = true;
					try {
						intent_data = URLDecoder.decode(intent_data, "UTF-8");
					} catch (Exception e1) {
						e1.printStackTrace();
					}

					// DEBUG
					// DEBUG
					// DEBUG
					// intent_data = "google.navigation:q=Wien Burggasse 27";
					// intent_data = "google.navigation:q=48.25676,16.643";
					// intent_data = "google.navigation:ll=48.25676,16.643&q=blabla-strasse";
					// intent_data = "google.navigation:ll=48.25676,16.643";
					// DEBUG
					// DEBUG
					// DEBUG

					try {
						Log.e(TAG, "found DEBUG 1: " + intent_data.substring(0, 20));
						Log.e(TAG, "found DEBUG 2: " + intent_data.substring(20, 22));
						Log.e(TAG, "found DEBUG 3: " + intent_data.substring(20, 21));
						Log.e(TAG, "found DEBUG 4: " + intent_data.split("&").length);
						Log.e(TAG, "found DEBUG 4.1: yy" + intent_data.split("&")[1].substring(0, 1).toLowerCase() + "yy");
						Log.e(TAG, "found DEBUG 5: xx" + intent_data.split("&")[1] + "xx");
					} catch (Exception e) {
						e.printStackTrace();
					}

					if (!Navit.NavitStartupAlreadySearching) {
						if (intent_data.length() > 19) {
							// if h: then show target
							if (substring_without_ioobe(intent_data, 0, 23).equals("http://maps.google.com/")) {
								Uri uri = Uri.parse(intent_data);
								Log.e(TAG, "target found (h): " + uri.getQueryParameter("q"));
								parsable = true;
								intent_data = "google.navigation:ll=" + uri.getQueryParameter("q") + "&q=Target";
							}
							// if i: then show target
							else if (substring_without_ioobe(intent_data, 0, 24).equals("https://maps.google.com/")) {
								Uri uri = Uri.parse(intent_data);
								Log.e(TAG, "target found (i): " + uri.getQueryParameter("q"));
								parsable = true;
								intent_data = "google.navigation:ll=" + uri.getQueryParameter("q") + "&q=Target";
							}
							// if d: then start target search
							else if ((substring_without_ioobe(intent_data, 0, 20).equals("google.navigation:q=")) && ((!substring_without_ioobe(intent_data, 20, 21).equals('+')) && (!substring_without_ioobe(intent_data, 20, 21).equals('-')) && (!substring_without_ioobe(intent_data, 20, 22).matches("[0-9][0-9]")))) {
								Log.e(TAG, "target found (d): " + intent_data.split("q=", -1)[1]);
								Navit.NavitStartupAlreadySearching = true;
								start_targetsearch_from_intent(intent_data.split("q=", -1)[1]);
								// dont use this here, already starting search, so set to "false"
								parsable = false;
								unparsable_info_box = false;
							}
							// if b: then remodel the input string to look like a:
							else if (substring_without_ioobe(intent_data, 0, 20).equals("google.navigation:q=")) {
								intent_data = "ll=" + intent_data.split("q=", -1)[1] + "&q=Target";
								Log.e(TAG, "target found (b): " + intent_data);
								parsable = true;
							}
							// if g: [google.navigation:///?ll=49.4086,17.4855&...] then remodel the input string to look like a:
							else if (substring_without_ioobe(intent_data, 0, 25).equals("google.navigation:///?ll=")) {
								intent_data = "google.navigation:ll=" + intent_data.split("ll=", -1)[1].split("&", -1)[0] + "&q=Target";
								Log.e(TAG, "target found (g): " + intent_data);
								parsable = true;
							}
							// if e: then remodel the input string to look like a:
							else if ((substring_without_ioobe(intent_data, 0, 21).equals("google.navigation:ll=")) && (intent_data.split("&").length > 1) && (substring_without_ioobe(intent_data.split("&")[1], 0, 1).toLowerCase().equals("f"))) {
								int idx = intent_data.indexOf("&");
								intent_data = substring_without_ioobe(intent_data, 0, idx) + "&q=Target";
								Log.e(TAG, "target found (e): " + intent_data);
								parsable = true;
							}
							// if f: then remodel the input string to look like a:
							else if ((substring_without_ioobe(intent_data, 0, 21).equals("google.navigation:ll=")) && (intent_data.split("&").length > 1)) {
								int idx = intent_data.indexOf("&");
								intent_data = intent_data.substring(0, idx) + "&q=Target";
								Log.e(TAG, "target found (f): " + intent_data);
								parsable = true;
							}
							// already looks like a: just set flag
							else if ((substring_without_ioobe(intent_data, 0, 21).equals("google.navigation:ll=")) && (intent_data.split("&q=").length > 1)) {
								// dummy, just set the flag
								Log.e(TAG, "target found (a): " + intent_data);
								Log.e(TAG, "target found (a): " + intent_data.split("&q=").length);
								parsable = true;
							}
							// if c: then remodel the input string to look like a:
							else if ((substring_without_ioobe(intent_data, 0, 21).equals("google.navigation:ll=")) && (intent_data.split("&q=").length < 2)) {

								intent_data = intent_data + "&q=Target";
								Log.e(TAG, "target found (c): " + intent_data);
								parsable = true;
							}
						}
					} else {
						Log.e(TAG, "already started search from startup intent");
						parsable = false;
						unparsable_info_box = false;
					}

					if (parsable) {
						Log.e(TAG, "parsable");
						// now string should be in form --> a:
						// now split the parts off
						temp1 = intent_data.split("&q=", -1)[0];
						try {
							temp3 = temp1.split("ll=", -1)[1];
							temp2 = intent_data.split("&q=", -1)[1];
						} catch (Exception e) {
							// java.lang.ArrayIndexOutOfBoundsException most likely
							// so let's assume we dont have '&q=xxxx'
							temp3 = temp1;
						}

						if (temp2 == null) {
							// use some default name
							temp2 = "Target";
						}

						lat = temp3.split(",", -1)[0];
						lon = temp3.split(",", -1)[1];
						Log.e(TAG, "parsable lat = " + lat +" lon = " + lon);

						q = temp2;
						// is the "search name" url-encoded? i think so, lets url-decode it here
						q = URLDecoder.decode(q);
						// System.out.println();

						Navit.remember_destination(q, lat, lon);
						Navit.destination_set();

						Message msg = new Message();
						Bundle b = new Bundle();
						b.putInt("Callback", 3);
						b.putString("lat", lat);
						b.putString("lon", lon);
						b.putString("q", q);
						msg.setData(b);
						callback_handler_55.sendMessage(msg);

						Log.e(TAG, "destination zou gezet zijn ?? ");

						final Thread zoom_to_route_002 = new Thread() {
							int wait = 1;
							int count = 0;
							final int max_count = 60;

							@Override
							public void run() {
								while (wait == 1) {
									try {
										if ((NavitGraphics.navit_route_status == 17) || (NavitGraphics.navit_route_status == 33)) {
											zoom_to_route();
											wait = 0;
										} else {
											wait = 1;
										}

										count++;
										if (count > max_count) {
											wait = 0;
										} else {
											Thread.sleep(400);
										}
									} catch (Exception e) {
									}
								}
							}
						};
						zoom_to_route_002.start();

						//						try
						//						{
						//							Thread.sleep(400);
						//						}
						//						catch (InterruptedException e)
						//						{
						//						}
						//
						//						//						try
						//						//						{
						//						//							show_geo_on_screen(Float.parseFloat(lat), Float.parseFloat(lon));
						//						//						}
						//						//						catch (Exception e2)
						//						//						{
						//						//							e2.printStackTrace();
						//						//						}

						try {
							Navit.follow_button_on();
						} catch (Exception e2) {
							e2.printStackTrace();
						}
					} else {
						if (unparsable_info_box && !searchBoxShown) {
							try {
								searchBoxShown = true;
								String searchString = intent_data.split("q=")[1];
								searchString = searchString.split("&")[0];
								searchString = URLDecoder.decode(searchString); // decode the URL: e.g. %20 -> space
								Log.e(TAG, "Search String :" + searchString);
								executeSearch(searchString);
							} catch (Exception e) {
								// safety net
								try {
									Log.e(TAG, "problem with startup search 7 str=" + intent_data);
								} catch (Exception e2) {
									e2.printStackTrace();
								}
							}
						}
					}
				} else if ((intent_data != null) && (substring_without_ioobe(intent_data, 0, 10).equals("geo:0,0?q="))) {
					// g: geo:0,0?q=wien%20burggasse

					System.out.println("SUI:001");

					boolean unparsable_info_box = true;
					try {
						intent_data = URLDecoder.decode(intent_data, "UTF-8");
					} catch (Exception e1) {
						e1.printStackTrace();

					}

					System.out.println("SUI:002");

					if (!Navit.NavitStartupAlreadySearching) {
						if (intent_data.length() > 10) {
							// if g: then start target search
							Log.e(TAG, "target found (g): " + intent_data.split("q=", -1)[1]);
							Navit.NavitStartupAlreadySearching = true;
							start_targetsearch_from_intent(intent_data.split("q=", -1)[1]);
							// dont use this here, already starting search, so set to "false"
							unparsable_info_box = false;
						}
					} else {
						Log.e(TAG, "already started search from startup intent");
						unparsable_info_box = false;
					}

					if (unparsable_info_box && !searchBoxShown) {
						try {
							searchBoxShown = true;
							String searchString = intent_data.split("q=")[1];
							searchString = searchString.split("&")[0];
							searchString = URLDecoder.decode(searchString); // decode the URL: e.g. %20 -> space
							Log.e(TAG, "Search String :" + searchString);
							executeSearch(searchString);
						} catch (Exception e) {
							// safety net
							try {
								Log.e(TAG, "problem with startup search 88 str=" + intent_data);
							} catch (Exception e2) {
								e2.printStackTrace();
							}
						}
					}

				} else if ((intent_data != null) && (substring_without_ioobe(intent_data, 0, 4).equals("geo:"))) {
					// g: geo:16.8,46.3?z=15

					System.out.println("SUI:002a");

					boolean parsable = false;

					String tmp1;
					String tmp2;
					String tmp3;
					float lat1 = 0;
					float lon1 = 0;
					int zoom1 = 15;

					try {
						intent_data = URLDecoder.decode(intent_data, "UTF-8");
					} catch (Exception e1) {
						e1.printStackTrace();
					}

					if (!Navit.NavitStartupAlreadySearching) {
						try {
							tmp1 = intent_data.split(":", 2)[1];
							tmp2 = tmp1.split("\\?", 2)[0];
							tmp3 = tmp1.split("\\?", 2)[1];
							lat1 = Float.parseFloat(tmp2.split(",", 2)[0]);
							lon1 = Float.parseFloat(tmp2.split(",", 2)[1]);
							zoom1 = Integer.parseInt(tmp3.split("z=", 2)[1]);
							parsable = true;
						} catch (Exception e4) {
							e4.printStackTrace();
						}
					}

					if (parsable) {
						// geo: intent -> only show destination on map!

						// set nice zoomlevel before we show destination
						//						int zoom_want = zoom1;
						//						//
						//						Message msg = new Message();
						//						Bundle b = new Bundle();
						//						b.putInt("Callback", 33);
						//						b.putString("s", Integer.toString(zoom_want));
						//						msg.setData(b);
						//						try
						//						{
						//							N_NavitGraphics.callback_handler.sendMessage(msg);
						//							Navit.GlobalScaleLevel = Navit_SHOW_DEST_ON_MAP_ZOOMLEVEL;
						//							if ((zoom_want > 8) && (zoom_want < 17))
						//							{
						//								Navit.GlobalScaleLevel = (int) (Math.pow(2, (18 - zoom_want)));
						//								System.out.println("GlobalScaleLevel=" + Navit.GlobalScaleLevel);
						//							}
						//						}
						//						catch (Exception e)
						//						{
						//							e.printStackTrace();
						//						}
						//						if (PREF_save_zoomlevel)
						//						{
						//							setPrefs_zoomlevel();
						//						}
						// set nice zoomlevel before we show destination

						try {
							Navit.follow_button_off();
						} catch (Exception e2) {
							e2.printStackTrace();
						}

						show_geo_on_screen(lat1, lon1);
						//						final Thread zoom_to_route_003 = new Thread()
						//						{
						//							@Override
						//							public void run()
						//							{
						//								try
						//								{
						//									Thread.sleep(200);
						//									show_geo_on_screen(lat1, lon1);
						//								}
						//								catch (Exception e)
						//								{
						//								}
						//							}
						//						};
						//						zoom_to_route_003.start();

					}
				}
			}

			System.out.println("SUI:099 XX" + substring_without_ioobe(intent_data, 0, 10) + "XX");

			// clear intent
			// ------------------------  BIG LOOP  ------------------------
			// ------------------------  BIG LOOP  ------------------------
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("SUI:199");
		}
	}


	@Override
	public void onPause()
	{
		Log.e(TAG, "onPause");
		// if COMM stuff is running, stop it!
		ZANaviDebugReceiver.stop_me = true;

		// hide main progress bar ------------
		if (progressbar_main_activity.getVisibility() == View.VISIBLE)
		{
			progressbar_main_activity.setProgress(0);
			progressbar_main_activity.setVisibility(View.GONE);
		}
		// hide main progress bar ------------

		try
		{
			sensorManager.unregisterListener(lightSensorEventListener);
		}
		catch (Exception e)
		{
		}

		// ---- DEBUG ----
		// -- dump all callbacks --
		try
		{
			if (preferences.PREF_enable_debug_functions)
			{
				sendCallBackMessage(100);
			}
		}
		catch (Exception e)
		{
		}
		// -- dump all callbacks --
		// ---- DEBUG ----

		// ---- DEBUG ----
		try
		{
			if (!Navit.is_navigating)
			{
				if (preferences.PREF_enable_debug_write_gpx)
				{
					NavitVehicle.pos_recording_end();
				}
			}
		}
		catch (Exception e)
		{
		}
		// ---- DEBUG ----
		// ---- DEBUG ----
		// ---- DEBUG ----

		// System.out.println("@@ onPause @@");

		try
		{
			setPrefs_zoomlevel();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		try
		{
			watchmem.stop_me();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		try
		{
			//watchmem.join();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		try
		{
			//Simulate.stop_me();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		try
		{
			//Simulate.join();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		//Navit.show_mem_used();

		//		if (!Navit.is_navigating)
		//		{
		//			try
		//			{
		//				mTts.stop();
		//			}
		//			catch (Exception e)
		//			{
		//
		//			}
		//
		//			try
		//			{
		//				mTts.shutdown();
		//			}
		//			catch (Exception e)
		//			{
		//
		//			}
		//
		//			mTts = null;
		//		}

		super.onPause();

		// signal to backupmanager that data "is / could have" changed
		try
		{
			Class.forName("android.app.backup.BackupManager");
			BackupManager backupManager = new BackupManager(this);
			backupManager.dataChanged();
		}
		catch (ClassNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		turn_off_compass();

		// System.out.println("XXNAV: onpause:001");
		if (!Navit.is_navigating)
		{
			// System.out.println("XXNAV: onpause:002");
			NavitVehicle.turn_off_all_providers();
			NavitVehicle.turn_off_sat_status();
			// System.out.println("XXNAV: onpause:003");
		}


		//cwthr.NavitActivity2(-1); // -1 doet niets --jdg--

		Navit.show_mem_used();

		try
		{
			if (wl != null)
			{
				wl.release();
				Log.e(TAG, "WakeLock: release 1");
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		try
		{
			if (wl_cpu != null)
			{
				if (wl_cpu.isHeld())
				{
					wl_cpu.release();
					Log.e(TAG, "WakeLock CPU: release 1");
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		is_paused = true;
	}


	@Override
	public void onStop()
	{
		super.onStop();
		Log.e(TAG, "onStop");

		if (Navit.CIDEBUG != 0)
		{
			// ----- DEBUG -----
			ZANaviLogMessages.dump_vales_to_log();
			ZANaviLogMessages.dump_messages_to_log();
			// ----- DEBUG -----		
		}

		cwthr.NavitActivity2(-2); // saves center.txt --jdg--
		Navit.show_mem_used();

		//		if (!Navit.is_navigating)
		//		{
		//			try
		//			{
		//				mTts.stop();
		//			}
		//			catch (Exception e)
		//			{
		//
		//			}
		//
		//			try
		//			{
		//				mTts.shutdown();
		//			}
		//			catch (Exception e)
		//			{
		//
		//			}
		//
		//			mTts = null;
		//		}

		// save points
		write_map_points();
	}

	//	@Override
	//	public void processFinish(String output)
	//	{
	//		try
	//		{
	//			System.out.println("Navit:" + "processFinish:" + "start");
	//			String full_file_name = Navit.NAVIT_DATA_DEBUG_DIR + "/crashlog_single.txt";
	//			try
	//			{
	//				new File(full_file_name).delete();
	//			}
	//			catch (Exception e2)
	//			{
	//				e2.printStackTrace();
	//				System.out.println("Navit:processFinish:Ex02:" + e2.getMessage());
	//			}
	//			System.out.println("crashlogfile=" + full_file_name);
	//			Logging.writeToFile(output, Navit.this, full_file_name);
	//			System.out.println("Navit:" + "processFinish:" + "ready");
	//		}
	//		catch (Exception e)
	//		{
	//			e.printStackTrace();
	//			System.out.println("Navit:processFinish:Ex01:" + e.getMessage());
	//		}
	//	}

	@Override
	public void onDestroy()
	{
		Log.e(TAG, "onDestroy");

		//		// --- alive timestamp ---
		//		app_status_lastalive = System.currentTimeMillis();
		//		System.out.println("app_status_string set:[onDestroy]:app_status_lastalive=" + app_status_lastalive);
		//		PreferenceManager.getDefaultSharedPreferences(this).edit().putLong(PREF_KEY_LASTALIVE, app_status_lastalive).commit();
		//		// --- alive timestamp ---

			//			PreferenceManager.getDefaultSharedPreferences(this).edit().putString(PREF_KEY_CRASH, "down").commit();
		System.out.println("app_status_string set:[onDestroy]" + app_status_string);


		super.onDestroy();

		try
		{
			try
			{
				plugin_api.removeListener(zclientListener);
			}
			catch (Exception e)
			{
				e.printStackTrace();
				Log.i("NavitPlugin", "Failed to remove Listener", e);
			}
			unbindService(serviceConnection);
			Log.i("NavitPlugin", "Unbind from the service");
		}
		catch (Throwable t)
		{
			// catch any issues, typical for destroy routines
			// even if we failed to destroy something, we need to continue destroying
			Log.i("NavitPlugin", "Failed to unbind from the service", t);
		}


			mTts.stop();

		try
		{
			mTts.shutdown();
		}
		catch (Exception e)
		{

		}

		// ----- service stop -----
		// ----- service stop -----
		System.out.println("Navit:onDestroy -> stop ZANaviMapDownloaderService ---------");
		stopService(Navit.ZANaviMapDownloaderServiceIntent);
		//try
		//{
		//	Thread.sleep(1000);
		//}
		//catch (InterruptedException e)
		//{
		//}
		// ----- service stop -----
		// ----- service stop -----

		//NavitActivity(-3); // -3 doet niets --jdg--
		//Navit.show_mem_used();
	}

	public void setActivityResult(int requestCode, NavitActivityResult ActivityResult)
	{
		Log.e(TAG, "setActivityResult " + requestCode);
		ActivityResults[requestCode] = ActivityResult;
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event)
	{
		if (keyCode == KeyEvent.KEYCODE_MENU)
		{
			if (cur_menu != null)
			{
				// open the overflow menu
				cur_menu.performIdentifierAction(R.id.item_overflow, 0);
				return true;
			}
		}
		return super.onKeyUp(keyCode, event);
	}

	//	@Override
	//	public boolean onKeyDown(int keyCode, KeyEvent event)
	//	{
	//		if (keyCode == KeyEvent.KEYCODE_MENU)
	//		{
	//			return true;
	//		}
	//		return super.onKeyUp(keyCode, event);
	//	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu)
	{
		super.onPrepareOptionsMenu(menu);
		//
		menu.clear();

		// load the menu from XML
		getMenuInflater().inflate(R.menu.actionbaricons, menu);

		// NavitOverflowMenuItemID = R.id.item_overflow_menu_button;
		menu.findItem(R.id.share_menu_destination).setTitle(Navit.get_text("Share Destination"));
		menu.findItem(R.id.share_menu_location).setTitle(Navit.get_text("Share my Location"));
		menu.findItem(R.id.search_menu_offline).setTitle(get_text("address search (offline)"));
		menu.findItem(R.id.search_menu_online).setTitle(get_text("address search (online)"));
		menu.findItem(R.id.item_recentdest_menu_button).setTitle(get_text("Recent destinations"));
		menu.findItem(R.id.item_settings_menu_button).setTitle(get_text("Settings"));
		menu.findItem(R.id.item_search_menu_button).setTitle(get_text("Search"));

		// --- download icon ---
		menu.findItem(R.id.item_download_menu_button).setTitle(get_text("downloading map"));
		// --- download icon ---

		//
		menu.findItem(R.id.overflow_share_location).setTitle(Navit.get_text("Share my Location"));
		menu.findItem(R.id.overflow_share_destination).setTitle(Navit.get_text("Share Destination"));
		menu.findItem(R.id.overflow_settings).setTitle(Navit.get_text("Settings"));
		menu.findItem(R.id.overflow_zoom_to_route).setTitle(Navit.get_text("Zoom to Route"));

		if (ZANaviNormalDonateActivity.on_amazon_device)
		{
			menu.findItem(R.id.overflow_donate_item).setTitle(Navit.get_text("Donate"));
		}
		else
		{
			menu.findItem(R.id.overflow_donate_item).setTitle(Navit.get_text("Donate with Google Play"));
		}
		menu.findItem(R.id.overflow_donate_bitcoins_item).setTitle(Navit.get_text("Donate with Bitcoin"));
		//. TRANSLATORS: text to translate is: exit ZANavi
		menu.findItem(R.id.overflow_exit).setTitle(Navit.get_text("exit navit"));
		menu.findItem(R.id.overflow_toggle_poi).setTitle(Navit.get_text("toggle POI"));
		menu.findItem(R.id.overflow_announcer_on).setTitle(Navit.get_text("Announcer On"));
		menu.findItem(R.id.overflow_announcer_off).setTitle(Navit.get_text("Announcer Off"));
		menu.findItem(R.id.overflow_download_maps).setTitle(Navit.get_text("download maps"));
		menu.findItem(R.id.overflow_delete_maps).setTitle(Navit.get_text("delete maps"));
		menu.findItem(R.id.overflow_maps_age).setTitle(Navit.get_text("show Maps age"));
		menu.findItem(R.id.overflow_coord_dialog).setTitle(Navit.get_text("Coord Dialog"));
		menu.findItem(R.id.overflow_add_traffic_block).setTitle(Navit.get_text("add Traffic block"));
		menu.findItem(R.id.overflow_clear_traffic_block).setTitle(Navit.get_text("clear Traffic blocks"));
		menu.findItem(R.id.overflow_convert_gpx_file).setTitle(Navit.get_text("convert GPX file"));
		menu.findItem(R.id.overflow_replay_gps_file).setTitle(Navit.get_text("replay a ZANavi gps file"));
		menu.findItem(R.id.overflow_yaml_tests).setTitle(Navit.get_text("run YAML tests"));
		menu.findItem(R.id.overflow_clear_gpx_map).setTitle(Navit.get_text("clear GPX map"));
		// menu.findItem(R.id.overflow_dummy2)
		menu.findItem(R.id.overflow_demo_v_normal).setTitle(get_text("Demo Vehicle") + " [normal]");
		menu.findItem(R.id.overflow_demo_v_fast).setTitle(get_text("Demo Vehicle") + " [fast]");
		menu.findItem(R.id.overflow_speech_texts).setTitle(Navit.get_text("Speech Texts"));
		menu.findItem(R.id.overflow_nav_commands).setTitle(Navit.get_text("Nav. Commands"));
		menu.findItem(R.id.overflow_toggle_route_graph).setTitle(Navit.get_text("toggle Routegraph"));
		//menu.findItem(R.id.overflow_dummy1)
		menu.findItem(R.id.overflow_export_map_points_to_sdcard).setTitle(Navit.get_text("export Destinations"));
		menu.findItem(R.id.overflow_import_map_points_from_sdcard).setTitle(Navit.get_text("import Destinations"));
		menu.findItem(R.id.overflow_send_feedback).setTitle(Navit.get_text("send feedback"));
		menu.findItem(R.id.overflow_online_help).setTitle(Navit.get_text("online Help"));
		menu.findItem(R.id.overflow_about).setTitle(Navit.get_text("About"));
		//. TRANSLATORS: it means: "show current target in google maps"
		//. TRANSLATORS: please keep this text short, to fit in the android menu!
		menu.findItem(R.id.overflow_target_in_gmaps).setTitle(Navit.get_text("Target in gmaps"));
		//
		//
		menu.findItem(R.id.item_share_menu_button).setTitle(get_text("Share"));

		Display display_ = getWindowManager().getDefaultDisplay();
		Log.e(TAG, "Navit width in DP -> " + display_.getWidth() / Navit.metrics.density);
		Log.e(TAG, "Navit width in DP -> density=" + Navit.metrics.density);

		boolean actionbar_all_items_will_fit = false;
		try
		{
			View v4 = findViewById(R.id.item_settings_menu_button);
			// Log.e(TAG, "Navit width in DP -> v4=" + v4);
			int actionbar_item_width = 100;
			if ((v4 != null) && (v4.getWidth() > 0))
			{
				Log.e(TAG, "Navit width in DP -> v4.w=" + v4.getWidth());
				MenuItem menuItem = menu.findItem(R.id.item_settings_menu_button);
				// Log.e(TAG, "Navit width in DP -> mi=" + menuItem);
				// Log.e(TAG, "Navit width in DP -> i=" + menuItem.getIcon());
				Log.e(TAG, "Navit width in DP -> i.w=" + menuItem.getIcon().getIntrinsicWidth());
				actionbar_item_width = (int) ((v4.getWidth() + (menuItem.getIcon().getIntrinsicWidth() * 1.5f)) / 2);
			}
			else
			{
				MenuItem menuItem = menu.findItem(R.id.item_settings_menu_button);
				// Log.e(TAG, "Navit width in DP -> mi=" + menuItem);
				// Log.e(TAG, "Navit width in DP -> i=" + menuItem.getIcon());
				Log.e(TAG, "Navit width in DP -> i.w=" + menuItem.getIcon().getIntrinsicWidth());
				actionbar_item_width = (int) ((menuItem.getIcon().getIntrinsicWidth()) * 1.7f);
			}

			actionbar_items_will_fit = display_.getWidth() / actionbar_item_width;
			Log.e(TAG, "Navit width in DP -> number of items that will fit=" + actionbar_items_will_fit);
			if (actionbar_items_will_fit > 6) // now we need to fit max. 6 items on actionbar
			{
				actionbar_all_items_will_fit = true;
			}
			else
			{
				actionbar_all_items_will_fit = false;
			}
		}
		catch (Exception e)
		{
			if ((display_.getWidth() / Navit.metrics.density) < NAVIT_MIN_HORIZONTAL_DP_FOR_ACTIONBAR)
			{
				actionbar_all_items_will_fit = false;
			}
			else
			{
				actionbar_all_items_will_fit = true;
			}
		}

		if (actionbar_all_items_will_fit == false)
		{
			menu.findItem(R.id.item_share_menu_button).setVisible(false);
			menu.findItem(R.id.overflow_share_location).setVisible(true);
			if (NavitGraphics.CallbackDestinationValid2() == 0)
			{
				menu.findItem(R.id.overflow_share_destination).setVisible(false);
			}
			else
			{
				menu.findItem(R.id.overflow_share_destination).setVisible(true);
			}

			if (actionbar_items_will_fit < 6)
			{
				// also push the settings icons to overflow menu
				menu.findItem(R.id.item_settings_menu_button).setVisible(false);
				menu.findItem(R.id.overflow_settings).setVisible(true);
			}
			else
			{
				menu.findItem(R.id.item_settings_menu_button).setVisible(true);
				menu.findItem(R.id.overflow_settings).setVisible(false);
			}
		}
		else
		{
			menu.findItem(R.id.item_settings_menu_button).setVisible(true);
			menu.findItem(R.id.overflow_settings).setVisible(false);
			menu.findItem(R.id.overflow_share_location).setVisible(false);
			menu.findItem(R.id.overflow_share_destination).setVisible(false);
			menu.findItem(R.id.item_share_menu_button).setVisible(true);
		}

		cur_menu = menu;

		System.out.println("down_icon:000");

		try
		{
			MenuItem downloadViewMenuItem = menu.findItem(R.id.item_download_menu_button);
			System.out.println("down_icon:001");
			downloadViewMenuItem.setVisible(true);
			System.out.println("down_icon:002");
			System.out.println("down_icon:003");
			android.widget.ImageView v = (android.widget.ImageView) MenuItemCompat.getActionView(menu.findItem(R.id.item_download_menu_button));
			System.out.println("down_icon:004");
			v.setVisibility(View.VISIBLE);
			if (v != null)
			{
				System.out.println("down_icon:005");
				v.setImageBitmap(null);
				System.out.println("down_icon:006");

				v.setBackgroundResource(R.drawable.anim_download_icon_2);
				final AnimationDrawable anim = (AnimationDrawable) v.getBackground();

				// ----------------------------------
				// thanks to: http://stackoverflow.com/questions/14686802/animationdrawable-is-not-working-on-2-3-6-android-version
				// ----------------------------------
				v.post(new Runnable()
				{
					public void run()
					{
						try
						{
							anim.start();
							System.out.println("down_icon:006a");
						}
						catch (Exception e)
						{
							e.printStackTrace();
						}
					}
				});

				System.out.println("down_icon:007");

				v.setOnClickListener(new View.OnClickListener()
				{
					@Override
					public void onClick(View view)
					{
						Intent mapdownload_cancel_activity = new Intent(Navit.this, ZANaviDownloadMapCancelActivity.class);
						mapdownload_cancel_activity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
						startActivity(mapdownload_cancel_activity);
					}
				});
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.out.println("down_icon:099:EE");
		}

		if (actionabar_download_icon_visible)
		{
			menu.findItem(R.id.item_download_menu_button).setVisible(true);
		}
		else
		{
			menu.findItem(R.id.item_download_menu_button).setVisible(false);
		}

		if (NavitGraphics.CallbackDestinationValid2() > 0)
		{
			menu.findItem(R.id.item_endnavigation_menu_button).setVisible(true);
			menu.findItem(R.id.item_endnavigation_menu_button).setTitle(get_text("Stop Navigation"));
			menu.findItem(R.id.overflow_zoom_to_route).setVisible(true);
			menu.findItem(R.id.overflow_target_in_gmaps).setVisible(true);
			menu.findItem(R.id.share_menu_destination).setVisible(true);
		}
		else
		{
			menu.findItem(R.id.item_endnavigation_menu_button).setVisible(false);
			menu.findItem(R.id.overflow_zoom_to_route).setVisible(false);
			menu.findItem(R.id.overflow_target_in_gmaps).setVisible(false);
			menu.findItem(R.id.share_menu_destination).setVisible(false);
		}

		if (Navit_Announcer)
		{
			menu.findItem(R.id.overflow_announcer_off).setVisible(true);
			menu.findItem(R.id.overflow_announcer_on).setVisible(false);
		}
		else
		{
			menu.findItem(R.id.overflow_announcer_off).setVisible(false);
			menu.findItem(R.id.overflow_announcer_on).setVisible(true);
		}

		if (preferences.PREF_enable_debug_functions)
		{
			menu.findItem(R.id.overflow_dummy2).setVisible(true);
			menu.findItem(R.id.overflow_demo_v_normal).setVisible(true);
			menu.findItem(R.id.overflow_demo_v_fast).setVisible(true);
			menu.findItem(R.id.overflow_speech_texts).setVisible(true);
			menu.findItem(R.id.overflow_nav_commands).setVisible(true);
			menu.findItem(R.id.overflow_toggle_route_graph).setVisible(true);
			menu.findItem(R.id.overflow_replay_gps_file).setVisible(true);
			menu.findItem(R.id.overflow_yaml_tests).setVisible(true);
		}
		else
		{
			menu.findItem(R.id.overflow_dummy2).setVisible(false);
			menu.findItem(R.id.overflow_demo_v_normal).setVisible(false);
			menu.findItem(R.id.overflow_demo_v_fast).setVisible(false);
			menu.findItem(R.id.overflow_speech_texts).setVisible(false);
			menu.findItem(R.id.overflow_nav_commands).setVisible(false);
			menu.findItem(R.id.overflow_toggle_route_graph).setVisible(false);
			menu.findItem(R.id.overflow_replay_gps_file).setVisible(false);
			menu.findItem(R.id.overflow_yaml_tests).setVisible(false);
		}

		return true;
	}

	private void start_targetsearch_from_intent(String target_address)
	{
		Navit_last_address_partial_match = true; // this will overwrite the default setting --> this is not good
		Navit_last_address_search_string = target_address;
		Navit_last_address_hn_string = "";

		// ----------- CONFIG ---------
		// ----------- CONFIG ---------
		// ----------- CONFIG ---------
		boolean use_online_searchmode_here = true;
		boolean hide_duplicates_searchmode_here = false;
		// ----------- CONFIG ---------
		// ----------- CONFIG ---------
		// ----------- CONFIG ---------

		int dialog_num_;

		if (use_online_searchmode_here)
		{
			dialog_num_ = Navit.SEARCHRESULTS_WAIT_DIALOG;
			Navit.use_index_search = false;
			Log.e(TAG, "*google*:online search");
		}
		else
		{
			dialog_num_ = Navit.SEARCHRESULTS_WAIT_DIALOG_OFFLINE;
			Navit.use_index_search = Navit.allow_use_index_search();
		}

		// clear results
		Navit.NavitAddressResultList_foundItems.clear();
		Navit.Navit_Address_Result_double_index.clear();
		Navit.NavitSearchresultBarIndex = -1;
		Navit.NavitSearchresultBar_title = "";
		Navit.NavitSearchresultBar_text = "";
		search_hide_duplicates = false;

		if (Navit_last_address_search_string.equals(""))
		{
			// empty search string entered
			Toast.makeText(getApplicationContext(), Navit.get_text("No address found"), Toast.LENGTH_LONG).show(); //TRANS
		}
		else
		{
			// show dialog
			try
			{
				Log.e(TAG, "*google*:call-11: (0)num " + dialog_num_);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}

			if (hide_duplicates_searchmode_here)
			{
				search_hide_duplicates = true;
				// hide duplicates when searching
				// hide duplicates when searching
				sendCallBackMessage(45);
				// hide duplicates when searching
				// hide duplicates when searching
			}

			System.out.println("dialog -- 11:002");
			Message msg = progress_handler.obtainMessage();
			Bundle b = new Bundle();
			msg.what = 11;
			b.putInt("dialog_num", dialog_num_);
			msg.setData(b);
			progress_handler.sendMessage(msg);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		super.onCreateOptionsMenu(menu);

		return true;
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	@SuppressLint("NewApi")
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		//System.out.println("menu button pressed ID=" + item.getItemId());

		if ((item.getItemId() == R.id.share_menu_destination) || (item.getItemId() == R.id.overflow_share_destination) || (item.getItemId() == 23020))
		{
			// System.out.println("share destination pressed ID=" + item.getItemId());
			// ------------
			// ------------
			// share the current destination with your friends
			String current_target_string2 = NavitGraphics.CallbackGeoCalc(4, 1, 1);
			if (current_target_string2.equals("x:x"))
			{
				Log.e(TAG, "no target set!");
			}
			else
			{
				try
				{
					String[] tmp = current_target_string2.split(":", 2);

					if (Navit.OSD_route_001.arriving_time_valid)
					{
						share_location(tmp[0], tmp[1], Navit.get_text("Meeting Point"), Navit.get_text("Meeting Point"), Navit.OSD_route_001.arriving_time, true);
					}
					else
					{
						share_location(tmp[0], tmp[1], Navit.get_text("Meeting Point"), Navit.get_text("Meeting Point"), "", true);
					}

				}
				catch (Exception e)
				{
					e.printStackTrace();
					Log.e(TAG, "problem with target!");
				}
			}
			return true;
		}
		else if (item.getItemId() == R.id.item_download_menu_button)
		{
			// System.out.println("download icon pressed(1) ID=" + item.getItemId());

			Intent mapdownload_cancel_activity = new Intent(this, ZANaviDownloadMapCancelActivity.class);
			mapdownload_cancel_activity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
			startActivity(mapdownload_cancel_activity);

			return true;
		}
		else if ((item.getItemId() == R.id.share_menu_location) || (item.getItemId() == R.id.overflow_share_location) || (item.getItemId() == 23000))
		{
			// System.out.println("share location pressed ID=" + item.getItemId());
			// ------------
			// ------------
			// share the current location with your friends
			location_coords cur_target = new location_coords();
			try
			{
				geo_coord tmp = get_current_vehicle_position();
				cur_target.lat = tmp.Latitude;
				cur_target.lon = tmp.Longitude;

				if ((cur_target.lat == 0.0) && (cur_target.lon == 0.0))
				{
					cur_target = NavitVehicle.get_last_known_pos();
				}
			}
			catch (Exception e)
			{
				try
				{
					cur_target = NavitVehicle.get_last_known_pos();
				}
				catch (Exception e2)
				{
					cur_target = null;
				}
			}

			if (cur_target == null)
			{
				Log.e(TAG, "no location found!");
			}
			else
			{
				try
				{
					share_location(String.valueOf(cur_target.lat), String.valueOf(cur_target.lon), Navit.get_text("my Location"), Navit.get_text("my Location"), "", false);
				}
				catch (Exception e)
				{
					e.printStackTrace();
					Log.e(TAG, "problem with location!");
				}
			}
			return true;
		}
		else if ((item.getItemId() == R.id.item_settings_menu_button) || (item.getItemId() == R.id.overflow_settings) || (item.getItemId() == 490))
		{
			// open settings menu
			Intent settingsActivity = new Intent(getBaseContext(), NavitPreferences.class);
			startActivity(settingsActivity);

			return true;
		}
		else if (item.getItemId() == R.id.search_menu_offline)
		{
			// ok startup address search activity (offline binfile search)
			Navit.use_index_search = Navit.allow_use_index_search();
			Intent search_intent2 = new Intent(this, NavitAddressSearchActivity.class);
			search_intent2.putExtra("title", Navit.get_text("Enter: City and Street")); //TRANS
			search_intent2.putExtra("address_string", Navit_last_address_search_string);
			search_intent2.putExtra("hn_string", Navit_last_address_hn_string);
			search_intent2.putExtra("type", "offline");
			search_intent2.putExtra("search_country_id", Navit_last_address_search_country_id);

			String pm_temp2 = "0";
			if (Navit_last_address_partial_match)
			{
				pm_temp2 = "1";
			}

			search_intent2.putExtra("partial_match", pm_temp2);

			Navit.use_index_search = Navit.allow_use_index_search();
			if (Navit.use_index_search)
			{
				this.startActivityForResult(search_intent2, NavitAddressResultList_id);
			}
			else
			{
				this.startActivityForResult(search_intent2, NavitAddressSearch_id_offline);
			}

			return true;
		}
		else if (item.getItemId() == R.id.search_menu_online)
		{
			// ok startup address search activity (online google maps search)
			Navit.use_index_search = false;
			Intent search_intent = new Intent(this, NavitAddressSearchActivity.class);
			search_intent.putExtra("title", Navit.get_text("Enter: City and Street")); //TRANS
			search_intent.putExtra("address_string", Navit_last_address_search_string);
			//search_intent.putExtra("hn_string", Navit_last_address_hn_string);
			search_intent.putExtra("type", "online");
			String pm_temp = "0";
			if (Navit_last_address_partial_match)
			{
				pm_temp = "1";
			}
			search_intent.putExtra("partial_match", pm_temp);
			this.startActivityForResult(search_intent, NavitAddressSearch_id_online);

			return true;
		}
		else if (item.getItemId() == R.id.item_endnavigation_menu_button)
		{
			// stop navigation (this menu should only appear when navigation is actually on!)
			NavitGraphics.deactivate_nav_wakelock();
			sendCallBackMessage(7);
			Log.e(TAG, "stop navigation");

			if (preferences.PREF_enable_debug_write_gpx)
			{
				NavitVehicle.speech_recording_end();
			}

			// redraw all OSD elements
			Navit.OSD_route_001.arriving_time_valid = false;
			Navit.OSD_route_001.arriving_secs_to_dest_valid = false;
			Navit.OSD_route_001.driving_distance_to_target_valid = false;
			Navit.OSD_nextturn.nextturn_distance_valid = false;
			Navit.OSD_nextturn.nextturn_image_valid = false;
			Navit.OSD_compass.direct_distance_to_target_valid = false;
			NavitGraphics.OSD_new.postInvalidate();

			return true;
		}
		else if (item.getItemId() == R.id.item_recentdest_menu_button)
		{
			// show recent destination list
			Intent i2 = new Intent(this, NavitRecentDestinationActivity.class);
			this.startActivityForResult(i2, Navit.NavitRecentDest_id);

			return true;
		}
		else if (item.getItemId() == R.id.overflow_zoom_to_route)
		{
			return onOptionsItemSelected_wrapper(11);
		}
		else if (item.getItemId() == R.id.overflow_donate_item)
		{
			return onOptionsItemSelected_wrapper(26);
		}
		else if (item.getItemId() == R.id.overflow_donate_bitcoins_item)
		{
			return onOptionsItemSelected_wrapper(27);
		}
		else if (item.getItemId() == R.id.overflow_exit)
		{
			return onOptionsItemSelected_wrapper(99);
		}
		else if (item.getItemId() == R.id.overflow_toggle_poi)
		{
			return onOptionsItemSelected_wrapper(5);
		}
		else if (item.getItemId() == R.id.overflow_announcer_on)
		{
			return onOptionsItemSelected_wrapper(13);
		}
		else if (item.getItemId() == R.id.overflow_announcer_off)
		{
			return onOptionsItemSelected_wrapper(12);
		}
		else if (item.getItemId() == R.id.overflow_download_maps)
		{
			return onOptionsItemSelected_wrapper(3);
		}
		else if (item.getItemId() == R.id.overflow_delete_maps)
		{
			return onOptionsItemSelected_wrapper(8);
		}
		else if (item.getItemId() == R.id.overflow_maps_age)
		{
			return onOptionsItemSelected_wrapper(17);
		}
		else if (item.getItemId() == R.id.overflow_coord_dialog)
		{
			return onOptionsItemSelected_wrapper(19);
		}
		else if (item.getItemId() == R.id.overflow_add_traffic_block)
		{
			return onOptionsItemSelected_wrapper(21);
		}
		else if (item.getItemId() == R.id.overflow_clear_traffic_block)
		{
			return onOptionsItemSelected_wrapper(22);
		}
		else if (item.getItemId() == R.id.overflow_convert_gpx_file)
		{
			return onOptionsItemSelected_wrapper(20);
		}
		else if (item.getItemId() == R.id.overflow_clear_gpx_map)
		{
			return onOptionsItemSelected_wrapper(23);
		}
		else if (item.getItemId() == R.id.overflow_replay_gps_file)
		{
			return onOptionsItemSelected_wrapper(28);
		}
		else if (item.getItemId() == R.id.overflow_yaml_tests)
		{
			return onOptionsItemSelected_wrapper(609);
		}
		else if (item.getItemId() == R.id.overflow_demo_v_normal)
		{
			return onOptionsItemSelected_wrapper(601);
		}
		else if (item.getItemId() == R.id.overflow_demo_v_fast)
		{
			return onOptionsItemSelected_wrapper(604);
		}
		else if (item.getItemId() == R.id.overflow_speech_texts)
		{
			return onOptionsItemSelected_wrapper(602);
		}
		else if (item.getItemId() == R.id.overflow_nav_commands)
		{
			return onOptionsItemSelected_wrapper(603);
		}
		else if (item.getItemId() == R.id.overflow_toggle_route_graph)
		{
			return onOptionsItemSelected_wrapper(605);
		}
		else if (item.getItemId() == R.id.overflow_export_map_points_to_sdcard)
		{
			return onOptionsItemSelected_wrapper(607);
		}
		else if (item.getItemId() == R.id.overflow_import_map_points_from_sdcard)
		{
			return onOptionsItemSelected_wrapper(608);
		}
		else if (item.getItemId() == R.id.overflow_send_feedback)
		{
			return onOptionsItemSelected_wrapper(24);
		}
		else if (item.getItemId() == R.id.overflow_online_help)
		{
			return onOptionsItemSelected_wrapper(16);
		}
		else if (item.getItemId() == R.id.overflow_about)
		{
			return onOptionsItemSelected_wrapper(29);
		}
		else if (item.getItemId() == R.id.overflow_target_in_gmaps)
		{
			return onOptionsItemSelected_wrapper(15);
		}
		//		else
		//		{
		//			return onOptionsItemSelected_wrapper(item.getItemId());
		//		}

		return false;
	}

	@SuppressLint("NewApi")
	private boolean onOptionsItemSelected_wrapper(int id)
	{
		// Handle item selection
		switch (id)
		{
		case 1:
			// zoom in
			sendCallBackMessage(1);
			// if we zoom, hide the bubble
			if (NG__map_main.NavitAOverlay != null)
			{
				NG__map_main.NavitAOverlay.hide_bubble();
			}
			Log.e(TAG, "onOptionsItemSelected -> zoom in");
			break;
		case 2:
			// zoom out
			sendCallBackMessage(2);
			// if we zoom, hide the bubble
			if (NG__map_main.NavitAOverlay != null)
			{
				NG__map_main.NavitAOverlay.hide_bubble();
			}
			Log.e(TAG, "onOptionsItemSelected -> zoom out");
			break;
		case 3:
			// map download menu
			Intent map_download_list_activity = new Intent(this, NavitDownloadSelectMapActivity.class);
			this.startActivityForResult(map_download_list_activity, Navit.NavitDownloaderPriSelectMap_id);
			break;
		case 5:
			toggle_poi_pref();
			set_poi_layers();
			draw_map();
			break;
		case 6:
			// ok startup address search activity (online google maps search)
			Navit.use_index_search = false;
			Intent search_intent = new Intent(this, NavitAddressSearchActivity.class);
			search_intent.putExtra("title", Navit.get_text("Enter: City and Street")); //TRANS
			search_intent.putExtra("address_string", Navit_last_address_search_string);
			//search_intent.putExtra("hn_string", Navit_last_address_hn_string);
			search_intent.putExtra("type", "online");
			String pm_temp = "0";
			if (Navit_last_address_partial_match)
			{
				pm_temp = "1";
			}
			search_intent.putExtra("partial_match", pm_temp);
			this.startActivityForResult(search_intent, NavitAddressSearch_id_online);
			break;
		case 7:
			// ok startup address search activity (offline binfile search)
			Navit.use_index_search = Navit.allow_use_index_search();
			Intent search_intent2 = new Intent(this, NavitAddressSearchActivity.class);
			search_intent2.putExtra("title", Navit.get_text("Enter: City and Street")); //TRANS
			search_intent2.putExtra("address_string", Navit_last_address_search_string);
			search_intent2.putExtra("hn_string", Navit_last_address_hn_string);
			search_intent2.putExtra("type", "offline");
			search_intent2.putExtra("search_country_id", Navit_last_address_search_country_id);

			String pm_temp2 = "0";
			if (Navit_last_address_partial_match)
			{
				pm_temp2 = "1";
			}

			search_intent2.putExtra("partial_match", pm_temp2);
			this.startActivityForResult(search_intent2, NavitAddressSearch_id_offline);
			break;
		case 8:
			// map delete menu
			Intent map_delete_list_activity2 = new Intent(this, NavitDeleteSelectMapActivity.class);
			this.startActivityForResult(map_delete_list_activity2, Navit.NavitDeleteSecSelectMap_id);
			break;
		case 9:
			// stop navigation (this menu should only appear when navigation is actually on!)
			sendCallBackMessage(7);
			Log.e(TAG, "stop navigation");
			break;
		case 10:
			// open settings menu
			Intent settingsActivity = new Intent(getBaseContext(), NavitPreferences.class);
			startActivity(settingsActivity);
			break;
		case 11:
			//zoom_to_route
			zoom_to_route();
			break;
		case 12:

			// --------- make app crash ---------
			// --------- make app crash ---------
			// --------- make app crash ---------
			// ** // DEBUG // ** // crash_app_java(1);
			// ** // DEBUG // ** // crash_app_C();
			// --------- make app crash ---------
			// --------- make app crash ---------
			// --------- make app crash ---------

			// announcer off
			Navit_Announcer = false;
			sendCallBackMessage(34);
			try
			{
				invalidateOptionsMenu();
			}
			catch (Exception e)
			{
			}
			break;
		case 13:
			// announcer on
			Navit_Announcer = true;
			sendCallBackMessage(35);
			try
			{
				invalidateOptionsMenu();
			}
			catch (Exception e)
			{
			}
			break;
		case 14:
			// show recent destination list
			Intent i2 = new Intent(this, NavitRecentDestinationActivity.class);
			this.startActivityForResult(i2, Navit.NavitRecentDest_id);
			break;
		case 15:
			// show current target on googlemaps
			String current_target_string = NavitGraphics.CallbackGeoCalc(4, 1, 1);
			// Log.e(TAG, "got target  1: "+current_target_string);
			if (current_target_string.equals("x:x"))
			{
				Log.e(TAG, "no target set!");
			}
			else
			{
				try
				{
					String[] tmp = current_target_string.split(":", 2);
					googlemaps_show(tmp[0], tmp[1], "ZANavi Target");
				}
				catch (Exception e)
				{
					e.printStackTrace();
					Log.e(TAG, "problem with target!");
				}
			}
			break;
		case 16:
			// show online manual
			Log.e(TAG, "user wants online help, show the website lang=" + NavitTextTranslations.main_language.toLowerCase());
			// URL to ZANavi Manual (in english language)
			String url = "http://zanavi.cc/index.php/Manual";
			if (FDBL)
			{
				url = "http://fd.zanavi.cc/manual";
			}
			if (NavitTextTranslations.main_language.toLowerCase().equals("de"))
			{
				// show german manual
				url = "http://zanavi.cc/index.php/Manual/de";
				if (FDBL)
				{
					url = "http://fd.zanavi.cc/manualde";
				}
			}

			Intent i = new Intent(Intent.ACTION_VIEW);
			i.setData(Uri.parse(url));
			startActivity(i);
			break;
		case 17:
			// show age of maps (online)
			Intent i3 = new Intent(Intent.ACTION_VIEW);
			i3.setData(Uri.parse(NavitMapDownloader.ZANAVI_MAPS_AGE_URL));
			startActivity(i3);
			break;
		case 18:
			Intent intent_latlon = new Intent(Intent.ACTION_MAIN);
			//intent_latlon.setAction("android.intent.action.POINTPICK");
			intent_latlon.setPackage("com.cruthu.latlongcalc1");
			intent_latlon.setClassName("com.cruthu.latlongcalc1", "com.cruthu.latlongcalc1.LatLongMain");
			//intent_latlon.setClassName("com.cruthu.latlongcalc1", "com.cruthu.latlongcalc1.LatLongPointPick");
			try
			{
				startActivity(intent_latlon);
			}
			catch (Exception e88)
			{
				e88.printStackTrace();
				// show install page
				try
				{
					// String urlx = "http://market.android.com/details?id=com.cruthu.latlongcalc1";
					String urlx = "market://details?id=com.cruthu.latlongcalc1";
					Intent ix = new Intent(Intent.ACTION_VIEW);
					ix.setData(Uri.parse(urlx));
					startActivity(ix);
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
				}
			}
			break;
		case 19:
			// GeoCoordEnterDialog
			Intent it001 = new Intent(this, GeoCoordEnterDialog.class);
			this.startActivityForResult(it001, Navit.NavitGeoCoordEnter_id);
			break;
		case 20:
			// convert GPX file
			Intent intent77 = new Intent(getBaseContext(), FileDialog.class);
			File a = new File(preferences.PREF_last_selected_dir_gpxfiles);
			try
			{
				// convert the "/../" in the path to normal absolut dir
				intent77.putExtra(FileDialog.START_PATH, a.getCanonicalPath());
				//can user select directories or not
				intent77.putExtra(FileDialog.CAN_SELECT_DIR, false);
				// disable the "new" button
				intent77.putExtra(FileDialog.SELECTION_MODE, SelectionMode.MODE_OPEN);
				//alternatively you can set file filter
				//intent.putExtra(FileDialog.FORMAT_FILTER, new String[] { "gpx" });
				startActivityForResult(intent77, Navit.NavitGPXConvChooser_id);
			}
			catch (IOException e1)
			{
				e1.printStackTrace();
			}
			break;
		case 21:
			// add traffic block (like blocked road, or construction site) at current location of crosshair
			try
			{
				String traffic = "";
				if (Navit.GFX_OVERSPILL)
				{
					traffic = NavitGraphics.CallbackGeoCalc(7, (int) (NavitGraphics.Global_dpi_factor * (NavitGraphics.mCanvasWidth / 2 + NavitGraphics.mCanvasWidth_overspill)), (int) (NavitGraphics.Global_dpi_factor * (NavitGraphics.mCanvasHeight / 2 + NavitGraphics.mCanvasHeight_overspill)));
				}
				else
				{
					traffic = NavitGraphics.CallbackGeoCalc(7, (int) (NavitGraphics.Global_dpi_factor * NavitGraphics.mCanvasWidth / 2), (int) (NavitGraphics.Global_dpi_factor * NavitGraphics.mCanvasHeight / 2));
				}

				// System.out.println("traffic=" + traffic);
				File traffic_file_dir = new File(sNavitMapDirectory);
				traffic_file_dir.mkdirs();
				File traffic_file = new File(sNavitMapDirectory + "/traffic.txt");
				FileOutputStream fOut = null;
				OutputStreamWriter osw = null;
				try
				{
					fOut = new FileOutputStream(traffic_file, true);
					osw = new OutputStreamWriter(fOut);
					osw.write("type=traffic_distortion maxspeed=0" + "\n"); // item header
					osw.write(traffic); // item coordinates
					osw.close();
					fOut.close();
				}
				catch (Exception ef)
				{
					ef.printStackTrace();
				}

				// update route, if a route is set
				sendCallBackMessage(73);
				// draw map no-async
				sendCallBackMessage(64);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			break;
		case 22:
			// clear all traffic blocks
			try
			{
				File traffic_file = new File(sNavitMapDirectory + "/traffic.txt");
				traffic_file.delete();

				// update route, if a route is set
				sendCallBackMessage(73);
				// draw map no-async
				sendCallBackMessage(64);
			}
			catch (Exception e)
			{
			}
			break;
		case 23:
			// clear all GPX maps
			try
			{
				File gpx_file = new File(sNavitMapDirectory + "/gpxtracks.txt");
				gpx_file.delete();

				// draw map no-async
				sendCallBackMessage(64);
			}
			catch (Exception e)
			{
			}
			break;
		case 24:
			// show feedback form
			Intent i4 = new Intent(this, NavitFeedbackFormActivity.class);
			this.startActivityForResult(i4, Navit.NavitSendFeedback_id);
			break;
		case 25:
			// share the current destination with your friends			
			String current_target_string2 = NavitGraphics.CallbackGeoCalc(4, 1, 1);
			if (current_target_string2.equals("x:x"))
			{
				Log.e(TAG, "no target set!");
			}
			else
			{
				try
				{
					String[] tmp = current_target_string2.split(":", 2);

					if (Navit.OSD_route_001.arriving_time_valid)
					{
						share_location(tmp[0], tmp[1], Navit.get_text("Meeting Point"), Navit.get_text("Meeting Point"), Navit.OSD_route_001.arriving_time, true);
					}
					else
					{
						share_location(tmp[0], tmp[1], Navit.get_text("Meeting Point"), Navit.get_text("Meeting Point"), "", true);
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
					Log.e(TAG, "problem with target!");
				}
			}
			break;
		case 26:
			// donate
			Log.e(TAG, "start donate app");
			donate();
			break;
		case 27:
			// donate
			Log.e(TAG, "donate bitcoins");
			donate_bitcoins();
			break;
		case 28:
			// replay GPS file
			Intent intent771 = new Intent(getBaseContext(), FileDialog.class);
			File a1 = new File(getNAVIT_DATA_DEBUG_DIR());
			try
			{
				// convert the "/../" in the path to normal absolut dir
				intent771.putExtra(FileDialog.START_PATH, a1.getCanonicalPath());
				//can user select directories or not
				intent771.putExtra(FileDialog.CAN_SELECT_DIR, false);
				// disable the "new" button
				intent771.putExtra(FileDialog.SELECTION_MODE, SelectionMode.MODE_OPEN);
				//alternatively you can set file filter
				intent771.putExtra(FileDialog.FORMAT_FILTER, new String[] { "txt", "yaml" });
				startActivityForResult(intent771, Navit.NavitReplayFileConvChooser_id);
			}
			catch (IOException e1)
			{
				e1.printStackTrace();
			}
			break;
		case 29:
			// About Screen
			Intent it002 = new Intent(this, ZANaviAboutPage.class);
			this.startActivityForResult(it002, Navit.ZANaviAbout_id);
			break;
		case 88:
			// dummy entry, just to make "breaks" in the menu
			break;
		case 601:
			// DEBUG: activate demo vehicle and set position to position to screen center

			Navit.DemoVehicle = true;

			sendCallBackMessage(101);

			final Thread demo_v_001 = new Thread()
			{
				@Override
				public void run()
				{
					try
					{
						Thread.sleep(1000); // wait 1 seconds before we start

						try
						{
							float lat;
							float lon;

							String lat_lon = "";
							if (Navit.GFX_OVERSPILL)
							{
								lat_lon = NavitGraphics.CallbackGeoCalc(1, NavitGraphics.Global_dpi_factor * (NG__map_main.view.getWidth() / 2 + NavitGraphics.mCanvasWidth_overspill), NavitGraphics.Global_dpi_factor * (NG__map_main.view.getHeight() / 2 + NavitGraphics.mCanvasHeight_overspill));
							}
							else
							{
								lat_lon = NavitGraphics.CallbackGeoCalc(1, NavitGraphics.Global_dpi_factor * NG__map_main.view.getWidth() / 2, NavitGraphics.Global_dpi_factor * NG__map_main.view.getHeight() / 2);
							}
							String[] tmp = lat_lon.split(":", 2);
							//System.out.println("tmp=" + lat_lon);
							lat = Float.parseFloat(tmp[0]);
							lon = Float.parseFloat(tmp[1]);
							//System.out.println("ret=" + lat_lon + " lat=" + lat + " lon=" + lon);
							Location l = null;
							l = new Location("ZANavi Demo 001");
							l.setLatitude(lat);
							l.setLongitude(lon);
							l.setBearing(0.0f);
							l.setSpeed(0);
							l.setAccuracy(4.0f); // accuracy 4 meters
							// NavitVehicle.update_compass_heading(0.0f);
							NavitVehicle.set_mock_location__fast(l);
						}
						catch (Exception e)
						{
						}

						Message msg = new Message();
						Bundle b = new Bundle();
						b.putInt("Callback", 52);
						b.putString("s", "45"); // speed in km/h of Demo-Vehicle
						// b.putString("s", "20");

						msg.setData(b);
						callback_handler_55.sendMessage(msg);
					}
					catch (Exception e)
					{
					}
				}
			};
			demo_v_001.start();

			Message msg = new Message();
			Bundle b = new Bundle();
			b.putInt("Callback", 51);

			if (Navit.GFX_OVERSPILL)
			{
				b.putInt("x", (int) (NavitGraphics.Global_dpi_factor * ((NG__map_main.view.getWidth() / 2) + NavitGraphics.mCanvasWidth_overspill)));
				b.putInt("y", (int) (NavitGraphics.Global_dpi_factor * ((NG__map_main.view.getHeight() / 2) + NavitGraphics.mCanvasHeight_overspill)));
			}
			else
			{
				b.putInt("x", (int) (NavitGraphics.Global_dpi_factor * NG__map_main.view.getWidth() / 2));
				b.putInt("y", (int) (NavitGraphics.Global_dpi_factor * NG__map_main.view.getHeight() / 2));
			}
			msg.setData(b);
			callback_handler_55.sendMessage(msg);

			break;
		case 602:
			// DEBUG: toggle textview with spoken and translated string (to help with translation)
			try
			{
				if (NG__map_main.mNavitMsgTv2.getVisibility() == View.VISIBLE)
				{
					NG__map_main.mNavitMsgTv2.setVisibility(View.GONE);
					NG__map_main.mNavitMsgTv2.setEnabled(false);
					NavitGraphics.NavitMsgTv2sc_.setVisibility(View.GONE);
					NavitGraphics.NavitMsgTv2sc_.setEnabled(false);
				}
				else
				{
					NavitGraphics.NavitMsgTv2sc_.setVisibility(View.VISIBLE);
					NavitGraphics.NavitMsgTv2sc_.setEnabled(true);
					NG__map_main.mNavitMsgTv2.setVisibility(View.VISIBLE);
					NG__map_main.mNavitMsgTv2.setEnabled(true);
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			break;
		case 603:
			// DEBUG: show all possible navigation commands (also translated)
			NavitGraphics.generate_all_speech_commands();
			break;
		case 604:
			// DEBUG: activate FAST driving demo vehicle and set position to screen center

			Navit.DemoVehicle = true;

			msg = new Message();

			b = new Bundle();
			b.putInt("Callback", 52);
			b.putString("s", "800"); // speed in ~km/h of Demo-Vehicle
			msg.setData(b);
			callback_handler_55.sendMessage(msg);

			msg = new Message();
			b = new Bundle();
			b.putInt("Callback", 51);
			if (Navit.GFX_OVERSPILL)
			{
				b.putInt("x", (int) (NavitGraphics.Global_dpi_factor * ((NG__map_main.view.getWidth() / 2) + NavitGraphics.mCanvasWidth_overspill)));
				b.putInt("y", (int) (NavitGraphics.Global_dpi_factor * ((NG__map_main.view.getHeight() / 2) + NavitGraphics.mCanvasHeight_overspill)));
			}
			else
			{
				b.putInt("x", (int) (NavitGraphics.Global_dpi_factor * NG__map_main.view.getWidth() / 2));
				b.putInt("y", (int) (NavitGraphics.Global_dpi_factor * NG__map_main.view.getHeight() / 2));
			}
			msg.setData(b);
			callback_handler_55.sendMessage(msg);

			try
			{
				//for debugging only (jdg)
				float lat;
				float lon;

				String lat_lon = "";
				if (Navit.GFX_OVERSPILL)
				{
					lat_lon = NavitGraphics.CallbackGeoCalc(1, NavitGraphics.Global_dpi_factor * (NG__map_main.view.getWidth() / 2 + NavitGraphics.mCanvasWidth_overspill), NavitGraphics.Global_dpi_factor * (NG__map_main.view.getHeight() / 2 + NavitGraphics.mCanvasHeight_overspill));
				}
				else
				{
					lat_lon = NavitGraphics.CallbackGeoCalc(1, NavitGraphics.Global_dpi_factor * NG__map_main.view.getWidth() / 2, NavitGraphics.Global_dpi_factor * NG__map_main.view.getHeight() / 2);
				}

				String[] tmp = lat_lon.split(":", 2);
				//System.out.println("tmp=" + lat_lon);
				lat = Float.parseFloat(tmp[0]);
				lon = Float.parseFloat(tmp[1]);
				//System.out.println("ret=" + lat_lon + " lat=" + lat + " lon=" + lon);
				Location l = null;
				l = new Location("ZANavi Demo 001");
				l.setLatitude(lat);
				l.setLongitude(lon);
				l.setBearing(0.0f);
				l.setSpeed(0);
				l.setAccuracy(4.0f); // accuracy 4 meters
				// NavitVehicle.update_compass_heading(0.0f);
				NavitVehicle.set_mock_location__fast(l);
			}
			catch (Exception e)
			{
			}

			break;
		case 605:
			// DEBUG: toggle Routgraph on/off
			msg = new Message();
			b = new Bundle();
			b.putInt("Callback", 71);
			Navit.Routgraph_enabled = 1 - Navit.Routgraph_enabled;
			b.putString("s", "" + Navit.Routgraph_enabled);
			msg.setData(b);
			callback_handler_55.sendMessage(msg);
			break;
		case 606:
			// DEBUG: spill contents of index file(s)
			sendCallBackMessage(83);
			break;
		case 607:
			export_map_points_to_sdcard();
			break;
		case 608:
			import_map_points_from_sdcard();
			break;
		case 609:
			// run yaml tests
			new Thread()
			{
				public void run()
				{
					try
					{
						ZANaviDebugReceiver.DR_run_all_yaml_tests();
					}
					catch (Exception e)
					{
					}
				}
			}.start();
			break;
		case 99:
			try
			{
				if (wl_navigating != null)
				{
					//if (wl_navigating.isHeld())
					//{
					wl_navigating.release();
					Log.e(TAG, "WakeLock Nav: release 1");
					//}
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			// exit
			this.onPause();
			this.onStop();
			this.exit();
			//msg = new Message();
			//b = new Bundle();
			//b.putInt("Callback", 5);
			//b.putString("cmd", "quit();");
			//msg.setData(b);
			//N_NavitGraphics.callback_handler.sendMessage(msg);
			break;
		}
		return true;
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Log.e(TAG, "onActivityResult");
		switch (requestCode) {
			case Navit.ZANaviIntro_id:
				try {
					PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean(PREF_KEY_FIRST_START, false).apply();
				} catch (Exception e) {
					e.printStackTrace();
				}

			case Navit.NavitGPXConvChooser_id:
				try {
					Log.e(TAG, "onActivityResult 001");
					if (resultCode == AppCompatActivity.RESULT_OK) {
						String in_ = data.getStringExtra(FileDialog.RESULT_PATH);
						convert_gpx_file_real(in_);
					}
				} catch (Exception e77) {
					e77.printStackTrace();
				}
				break;

			case NavitReplayFileConvChooser_id:
				try {
					if (resultCode == AppCompatActivity.RESULT_OK) {
						final String in_ = data.getStringExtra(FileDialog.RESULT_PATH);
						final Thread replay_gpx_file_001 = new Thread() {
							@Override
							public void run() {
								try {
									Thread.sleep(2000); // wait 2 seconds before we start
									String date = new SimpleDateFormat("yyyy-MM-dd_HHmmss", Locale.GERMAN).format(new Date());
									ZANaviDebugReceiver.DR_replay_gps_file(in_, date);
								} catch (Exception e) {
								}
							}
						};
						replay_gpx_file_001.start();
					}
				} catch (Exception e77) {
					e77.printStackTrace();
				}
				break;

			case Navit.NavitDeleteSecSelectMap_id:
				try {
					if (resultCode == AppCompatActivity.RESULT_OK) {
						if (!data.getStringExtra("selected_id").equalsIgnoreCase(NavitDeleteSelectMapActivity.CANCELED_ID)) {
							System.out.println("Global_Location_update_not_allowed = 1");
							Navit.Global_Location_update_not_allowed = 1; // dont allow location updates now!

							// remove all sdcard maps
							sendCallBackMessage(19);

							try
							{
								Thread.sleep(100);
							}
							catch (InterruptedException e)
							{
							}

							Log.d(TAG, "delete map id=" + Integer.parseInt(data.getStringExtra("selected_id")));
							String map_full_line = NavitMapDownloader.OSM_MAP_NAME_ondisk_ORIG_LIST[Integer.parseInt(data.getStringExtra("selected_id"))];
							Log.d(TAG, "delete map full line=" + map_full_line);

							String del_map_name = sNavitMapDirectory + map_full_line.split(":", 2)[0];
							System.out.println("del map file :" + del_map_name);
							// remove from cat file
							NavitMapDownloader.remove_from_cat_file(map_full_line);
							// remove from disk
							File del_map_name_file = new File(del_map_name);
							del_map_name_file.delete();
							for (int jkl = 1; jkl < 51; jkl++) {
								File del_map_name_fileSplit = new File(del_map_name + "." + jkl);
								del_map_name_fileSplit.delete();
							}
							// also remove index file
							File del_map_name_file_idx = new File(del_map_name + ".idx");
							del_map_name_file_idx.delete();
							// remove also any MD5 files for this map that may be on disk
							try {
								String tmp = map_full_line.split(":", 2)[1];
								if (!tmp.equals(NavitMapDownloader.MAP_URL_NAME_UNKNOWN)) {
									tmp = tmp.replace("*", "");
									tmp = tmp.replace("/", "");
									tmp = tmp.replace("\\", "");
									tmp = tmp.replace(" ", "");
									tmp = tmp.replace(">", "");
									tmp = tmp.replace("<", "");
									System.out.println("removing md5 file:" + sNavitObject.getMapMD5path() + tmp + ".md5");
									File md5_final_filename = new File(sNavitObject.getMapMD5path() + tmp + ".md5");
									md5_final_filename.delete();
								}
							} catch (Exception e) {
								e.printStackTrace();
							}

							// remove map, and zoom out
							// ***** onStop();
							// ***** onCreate(getIntent().getExtras());

							try {
								Thread.sleep(100);
							} catch (InterruptedException e) {
							}

							// add all sdcard maps
							sendCallBackMessage(20);

							final Thread zoom_to_route_004 = new Thread() {
								int wait = 1;
								int count = 0;
								final int max_count = 60;

								@Override
								public void run() {
									while (wait == 1) {
										try {
											if ((NavitGraphics.navit_route_status == 17) || (NavitGraphics.navit_route_status == 33)) {
												zoom_to_route();
												wait = 0;
											} else {
												wait = 1;
											}

											count++;
											if (count > max_count) {
												wait = 0;
											} else {
												Thread.sleep(400);
											}
										} catch (Exception e) {
										}
									}
								}
							};
							zoom_to_route_004.start();

							System.out.println("Global_Location_update_not_allowed = 0");
							Navit.Global_Location_update_not_allowed = 0; // DO allow location updates now!
						}
					}
				} catch (Exception e) {
					Log.d(TAG, "error on onActivityResult 3");
					e.printStackTrace();
				}
				break;
			case Navit.NavitDownloaderPriSelectMap_id:
				try {
					if (resultCode == AppCompatActivity.RESULT_OK) {
						try {
							// Log.d(TAG, "PRI id=" + Integer.parseInt(data.getStringExtra("selected_id")));
							// set map id to download
							Navit.download_map_id = NavitMapDownloader.OSM_MAP_NAME_ORIG_ID_LIST[Integer.parseInt(data.getStringExtra("selected_id"))];
							// show the map download progressbar, and download the map
							if (Navit.download_map_id > -1) {
								// --------- start a map download (highest level) ---------
								// --------- start a map download (highest level) ---------
								// --------- start a map download (highest level) ---------
								// showDialog(Navit.MAPDOWNLOAD_PRI_DIALOG); // old method in app

								// new method in service
								Message msg = progress_handler.obtainMessage();
								// Bundle b = new Bundle();
								msg.what = 22;
								progress_handler.sendMessage(msg);

								// show license for OSM maps
								//. TRANSLATORS: please only translate the first word "Map data" and leave the other words in english
								Toast.makeText(getApplicationContext(), "Map data (c) OpenStreetMap contributors", Toast.LENGTH_SHORT).show();
								// --------- start a map download (highest level) ---------
								// --------- start a map download (highest level) ---------
								// --------- start a map download (highest level) ---------
							}
						} catch (NumberFormatException e) {
							Log.d(TAG, "NumberFormatException selected_id");
						}
					} else {
						// user pressed back key
					}
				} catch (Exception e) {
					Log.d(TAG, "error on onActivityResult");
					e.printStackTrace();
				}
				break;
			case Navit.NavitDownloaderSecSelectMap_id: // unused!!! unused!!! unused!!! unused!!! unused!!!
				break;
			case ZANaviVoiceInput_id:
				if (resultCode == AppCompatActivity.RESULT_OK) {
					try {
						String addr = data.getStringExtra("address_string");
						double lat = data.getDoubleExtra("lat", 0);
						double lon = data.getDoubleExtra("lon", 0);
						String hn = "";

						// save last address entry string
						preferences.PREF_StreetSearchStrings = pushToArray(preferences.PREF_StreetSearchStrings, addr, STREET_SEARCH_STRINGS_SAVE_COUNT);
						saveArray(preferences.PREF_StreetSearchStrings, "xxStrtSrhStrxx", STREET_SEARCH_STRINGS_SAVE_COUNT);

						Boolean partial_match = true;
						Navit.use_index_search = false;

						Navit_last_address_partial_match = partial_match;
						Navit_last_address_search_string = addr;
						Navit_last_address_hn_string = hn;

						Navit_last_address_full_file_search = false;

						// clear results
						Navit.NavitAddressResultList_foundItems.clear();
						Navit.Navit_Address_Result_double_index.clear();
						Navit.NavitSearchresultBarIndex = -1;
						Navit.NavitSearchresultBar_title = "";
						Navit.NavitSearchresultBar_text = "";
						Navit.search_results_towns = 0;
						Navit.search_results_streets = 0;
						Navit.search_results_streets_hn = 0;
						Navit.search_results_poi = 0;

						if (addr.equals("")) {
							// empty search string entered
							Toast.makeText(getApplicationContext(), Navit.get_text("No search string"), Toast.LENGTH_LONG).show(); //TRANS
						} else {
							System.out.println("Global_Location_update_not_allowed = 1");
							Navit.Global_Location_update_not_allowed = 1; // dont allow location updates now!

							// --> this still does the search // google_online_search_and_set_destination(addr);
							result_set_destination(lat, lon, addr);

							System.out.println("Global_Location_update_not_allowed = 0");
							Navit.Global_Location_update_not_allowed = 0; // DO allow location updates now!
						}

					} catch (Exception e) {

					}
				}
				break;
			case NavitAddressSearch_id_online:
			case NavitAddressSearch_id_offline:
				Log.e(TAG, "NavitAddressSearch_id_:001");
				try {
					if (resultCode == AppCompatActivity.RESULT_OK) {
						try {
							String addr = data.getStringExtra("address_string");
							String hn = "";
							try {
								// only from offline mask!
								hn = data.getStringExtra("hn_string");
							} catch (Exception e) {
								hn = "";
							}

							// save last address entry string
							preferences.PREF_StreetSearchStrings = pushToArray(preferences.PREF_StreetSearchStrings, addr, STREET_SEARCH_STRINGS_SAVE_COUNT);
							saveArray(preferences.PREF_StreetSearchStrings, "xxStrtSrhStrxx", STREET_SEARCH_STRINGS_SAVE_COUNT);

							Boolean partial_match = false;
							try {
								// only from offline mask!
								partial_match = data.getStringExtra("partial_match").equals("1");
							} catch (Exception e) {
							}

							sendCallBackMessage(44);

							if (requestCode == NavitAddressSearch_id_offline) {
								search_hide_duplicates = false;
								try {
									boolean hide_dup = data.getStringExtra("hide_dup").equals("1");
									if (hide_dup) {
										search_hide_duplicates = true;
										sendCallBackMessage(45);
									}
								} catch (Exception e) {
								}

								if (Navit.CIDEBUG == 0) {
									Navit.use_index_search = Navit.allow_use_index_search();
								}
							} else {
								Navit.use_index_search = false;
							}

							Navit_last_address_partial_match = partial_match;
							Navit_last_address_search_string = addr;
							Navit_last_address_hn_string = hn;

							try {
								// only from offline mask!
								Navit_last_address_full_file_search = data.getStringExtra("full_file_search").equals("1");
							} catch (Exception e) {
								Navit_last_address_full_file_search = false;
							}

							try {
								// only from offline mask!
								Navit_last_address_search_country_iso2_string = data.getStringExtra("address_country_iso2");

								Navit_last_address_search_country_flags = data.getIntExtra("address_country_flags", 3);
								// System.out.println("Navit_last_address_search_country_flags=" + Navit_last_address_search_country_flags);
								Navit_last_address_search_country_id = data.getIntExtra("search_country_id", 1); // default=*ALL*
								preferences.PREF_search_country = Navit_last_address_search_country_id;
								setPrefs_search_country();
							} catch (Exception e) {

							}

							// clear results
							Navit.NavitAddressResultList_foundItems.clear();
							Navit.Navit_Address_Result_double_index.clear();
							Navit.NavitSearchresultBarIndex = -1;
							Navit.NavitSearchresultBar_title = "";
							Navit.NavitSearchresultBar_text = "";
							Navit.search_results_towns = 0;
							Navit.search_results_streets = 0;
							Navit.search_results_streets_hn = 0;
							Navit.search_results_poi = 0;

							if (addr.equals("")) {
								// empty search string entered
								Toast.makeText(getApplicationContext(), Navit.get_text("No search string entered"), Toast.LENGTH_LONG).show(); //TRANS
							} else {
								System.out.println("Global_Location_update_not_allowed = 1");
								Navit.Global_Location_update_not_allowed = 1; // dont allow location updates now!

								if (requestCode == NavitAddressSearch_id_online) {
									// online googlemaps search
									try {
										Log.e(TAG, "call-11: (1)num " + Navit.SEARCHRESULTS_WAIT_DIALOG);
									} catch (Exception e) {
										e.printStackTrace();
									}

									System.out.println("dialog -- 11:003");
									System.out.println("online googlemaps search");
									Message msg = progress_handler.obtainMessage();
									Bundle b = new Bundle();
									msg.what = 11;
									b.putInt("dialog_num", Navit.SEARCHRESULTS_WAIT_DIALOG);
									msg.setData(b);
									progress_handler.sendMessage(msg);
								} else if (requestCode == NavitAddressSearch_id_offline) {
									// offline binfile search

									if (!Navit.use_index_search) {
										try {
											Log.e(TAG, "call-11: (2)num " + Navit.SEARCHRESULTS_WAIT_DIALOG_OFFLINE);
										} catch (Exception e) {
											e.printStackTrace();
										}

										// show dialog, and start search for the results
										// make it indirect, to give our activity a chance to startup
										// (remember we come straight from another activity and ours is still paused!)
										System.out.println("dialog -- 11:004");
										Message msg = progress_handler.obtainMessage();
										Bundle b = new Bundle();
										msg.what = 11;
										b.putInt("dialog_num", Navit.SEARCHRESULTS_WAIT_DIALOG_OFFLINE);
										msg.setData(b);
										progress_handler.sendMessage(msg);
									}
								}
							}
						} catch (NumberFormatException e) {
							Log.d(TAG, "NumberFormatException selected_id");
						}
					} else {
						// user pressed back key
						Log.e(TAG, "NavitAddressSearch_id_:900");
					}
				} catch (Exception e) {
					Log.d(TAG, "error on onActivityResult");
					e.printStackTrace();
				}
				Log.e(TAG, "NavitAddressSearch_id_:999");
				break;
			case Navit.NavitAddressResultList_id:
				try {
					if (resultCode == AppCompatActivity.RESULT_OK) {
						try {
							if (data.getStringExtra("what").equals("view")) {
								// get the coords for the destination
								int destination_id = Integer.parseInt(data.getStringExtra("selected_id"));

								// save last address entry string
								String addr = data.getStringExtra("address_string");
								preferences.PREF_StreetSearchStrings = pushToArray(preferences.PREF_StreetSearchStrings, addr, STREET_SEARCH_STRINGS_SAVE_COUNT);
								saveArray(preferences.PREF_StreetSearchStrings, "xxStrtSrhStrxx", STREET_SEARCH_STRINGS_SAVE_COUNT);

								try {
									Navit.follow_button_off();
								} catch (Exception e2) {
									e2.printStackTrace();
								}

								System.out.println("XSOM:009");

								if (Navit.use_index_search) {
									show_geo_on_screen_with_zoom_and_delay((float) Utils.transform_to_geo_lat(Navit.NavitAddressResultList_foundItems.get(destination_id).lat), (float) Utils.transform_to_geo_lon(Navit.NavitAddressResultList_foundItems.get(destination_id).lon));
								} else {
									show_geo_on_screen_with_zoom_and_delay(Navit.NavitAddressResultList_foundItems.get(destination_id).lat, Navit.NavitAddressResultList_foundItems.get(destination_id).lon);
								}
							} else if (data.getStringExtra("what").equals("set")) {
								Log.d(TAG, "adress result list id=" + Integer.parseInt(data.getStringExtra("selected_id")));

								// save last address entry string
								String addr = data.getStringExtra("address_string");
								preferences.PREF_StreetSearchStrings = pushToArray(preferences.PREF_StreetSearchStrings, addr, STREET_SEARCH_STRINGS_SAVE_COUNT);
								saveArray(preferences.PREF_StreetSearchStrings, "xxStrtSrhStrxx", STREET_SEARCH_STRINGS_SAVE_COUNT);

								// get the coords for the destination
								int destination_id = Integer.parseInt(data.getStringExtra("selected_id"));

								String _addr = "";
								double _lat = 0;
								double _lon = 0;

								// (float) Navit.transform_to_geo_lat(Navit.NavitAddressResultList_foundItems.get(destination_id).lat), (float) Navit.transform_to_geo_lon(Navit.NavitAddressResultList_foundItems.get(destination_id).lon)
								// (float) Navit.transform_to_geo_lat(Navit.NavitAddressResultList_foundItems.get(destination_id).lat)

								// ok now set target
								try {
									if (Navit.use_index_search) {
										_addr = Navit.NavitAddressResultList_foundItems.get(destination_id).addr;
										_lat = Utils.transform_to_geo_lat(Navit.NavitAddressResultList_foundItems.get(destination_id).lat);
										_lon = Utils.transform_to_geo_lon(Navit.NavitAddressResultList_foundItems.get(destination_id).lon);
										//Navit.remember_destination(Navit.NavitAddressResultList_foundItems.get(destination_id).addr, (float) Navit.transform_to_geo_lat(Navit.NavitAddressResultList_foundItems.get(destination_id).lat), (float) Navit.transform_to_geo_lon(Navit.NavitAddressResultList_foundItems.get(destination_id).lon));
									} else {
										_addr = Navit.NavitAddressResultList_foundItems.get(destination_id).addr;
										_lat = Navit.NavitAddressResultList_foundItems.get(destination_id).lat;
										_lon = Navit.NavitAddressResultList_foundItems.get(destination_id).lon;
										//Navit.remember_destination(Navit.NavitAddressResultList_foundItems.get(destination_id).addr, Navit.NavitAddressResultList_foundItems.get(destination_id).lat, Navit.NavitAddressResultList_foundItems.get(destination_id).lon);
									}
									// save points
									//write_map_points();
								} catch (Exception e) {
									e.printStackTrace();
								}

								route_wrapper(_addr, 0, 0, false, _lat, _lon, true);

								final Thread zoom_to_route_005 = new Thread() {
									int wait = 1;
									int count = 0;
									final int max_count = 60;

									@Override
									public void run() {
										while (wait == 1) {
											try {
												if ((NavitGraphics.navit_route_status == 17) || (NavitGraphics.navit_route_status == 33)) {
													zoom_to_route();
													wait = 0;
												} else {
													wait = 1;
												}

												count++;
												if (count > max_count) {
													wait = 0;
												} else {
													Thread.sleep(400);
												}
											} catch (Exception e) {
											}
										}
									}
								};
								zoom_to_route_005.start();
								// zoom_to_route();

								// ---------- DEBUG: write route to file ----------
								// ---------- DEBUG: write route to file ----------
								// ---------- DEBUG: write route to file ----------
								if (preferences.PREF_enable_debug_write_gpx) {
									write_route_to_gpx_file();
								}
								// ---------- DEBUG: write route to file ----------
								// ---------- DEBUG: write route to file ----------

								try {
									Navit.follow_button_on();
								} catch (Exception e2) {
									e2.printStackTrace();
								}

								//							if (Navit.use_index_search)
								//							{
								//								show_geo_on_screen((float) Navit.transform_to_geo_lat(Navit.NavitAddressResultList_foundItems.get(destination_id).lat), (float) Navit.transform_to_geo_lon(Navit.NavitAddressResultList_foundItems.get(destination_id).lon));
								//							}
								//							else
								//							{
								//								show_geo_on_screen(Navit.NavitAddressResultList_foundItems.get(destination_id).lat, Navit.NavitAddressResultList_foundItems.get(destination_id).lon);
								//							}
							} else {
								// -- nothing --
							}
						} catch (NumberFormatException e) {
							Log.d(TAG, "NumberFormatException selected_id");
						} catch (Exception e) {

						}
					} else {
						// user pressed back key
					}
				} catch (Exception e) {
					Log.d(TAG, "error on onActivityResult");
					e.printStackTrace();
				}
				break;
			case NavitAddressSearch_id_gmaps:
				try {
					if (resultCode == AppCompatActivity.RESULT_OK) {

					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			case NavitAddressSearch_id_sharedest:
				try {
					if (resultCode == AppCompatActivity.RESULT_OK) {

					}
					Log.d(TAG, "sharedest: finished");
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			case NavitGeoCoordEnter_id:
				try {
					if (resultCode == AppCompatActivity.RESULT_OK) {
						// lat lon enter activitiy result

						try {
							if (data.getStringExtra("what").equals("view")) {
								// get the coords for the destination
								float lat = Float.parseFloat(data.getStringExtra("lat"));
								float lon = Float.parseFloat(data.getStringExtra("lon"));

								// Log.d(TAG, "coord picker: " + lat);
								// Log.d(TAG, "coord picker: " + lon);

								// set nice zoomlevel before we show destination
								//							int zoom_want = Navit_SHOW_DEST_ON_MAP_ZOOMLEVEL;
								//							//
								//							Message msg = new Message();
								//							Bundle b = new Bundle();
								//							b.putInt("Callback", 33);
								//							b.putString("s", Integer.toString(zoom_want));
								//							msg.setData(b);
								//							try
								//							{
								//								N_NavitGraphics.callback_handler.sendMessage(msg);
								//								Navit.GlobalScaleLevel = zoom_want;
								//							}
								//							catch (Exception e)
								//							{
								//								e.printStackTrace();
								//							}
								//							if (PREF_save_zoomlevel)
								//							{
								//								setPrefs_zoomlevel();
								//							}
								// set nice zoomlevel before we show destination

								try {
									Navit.follow_button_off();
								} catch (Exception e2) {
									e2.printStackTrace();
								}

								show_geo_on_screen(lat, lon);
							} else {
								// get the coords for the destination
								float lat = Float.parseFloat(data.getStringExtra("lat"));
								float lon = Float.parseFloat(data.getStringExtra("lat"));
								String dest_name = "manual coordinates";

								// ok now set target
								try {
									dest_name = NavitGraphics.CallbackGeoCalc(8, lat, lon);
									if ((dest_name.equals(" ")) || (dest_name == null)) {
										dest_name = "manual coordinates";
									}
									//								Navit.remember_destination(dest_name, lat, lon);
									//								// save points
									//								write_map_points();
								} catch (Exception e) {
									e.printStackTrace();
								}

								//							// DEBUG: clear route rectangle list
								//							NavitGraphics.route_rects.clear();
								//
								//							if (NavitGraphics.navit_route_status == 0)
								//							{
								//								Navit.destination_set();
								//
								//								Message msg = new Message();
								//								Bundle b = new Bundle();
								//								b.putInt("Callback", 3);
								//								b.putString("lat", String.valueOf(lat));
								//								b.putString("lon", String.valueOf(lon));
								//								b.putString("q", dest_name);
								//								msg.setData(b);
								//								NavitGraphics.callback_handler.sendMessage(msg);
								//							}
								//							else
								//							{
								//								Message msg = new Message();
								//								Bundle b = new Bundle();
								//								b.putInt("Callback", 48);
								//								b.putString("lat", String.valueOf(lat));
								//								b.putString("lon", String.valueOf(lon));
								//								b.putString("q", dest_name);
								//								msg.setData(b);
								//								NavitGraphics.callback_handler.sendMessage(msg);
								//							}
								//

								route_wrapper(dest_name, 0, 0, false, lat, lon, true);

								final Thread zoom_to_route_006 = new Thread() {
									int wait = 1;
									int count = 0;
									final int max_count = 60;

									@Override
									public void run() {
										while (wait == 1) {
											try {
												if ((NavitGraphics.navit_route_status == 17) || (NavitGraphics.navit_route_status == 33)) {
													zoom_to_route();
													wait = 0;
												} else {
													wait = 1;
												}

												count++;
												if (count > max_count) {
													wait = 0;
												} else {
													Thread.sleep(400);
												}
											} catch (Exception e) {
											}
										}
									}
								};
								zoom_to_route_006.start();
								// zoom_to_route();

								// ---------- DEBUG: write route to file ----------
								// ---------- DEBUG: write route to file ----------
								// ---------- DEBUG: write route to file ----------
								if (preferences.PREF_enable_debug_write_gpx) {
									write_route_to_gpx_file();
								}
								// ---------- DEBUG: write route to file ----------
								// ---------- DEBUG: write route to file ----------

								try {
									Navit.follow_button_on();
								} catch (Exception e2) {
									e2.printStackTrace();
								}

								// show_geo_on_screen(lat, lon);
							}
						} catch (NumberFormatException e) {
							Log.d(TAG, "NumberFormatException selected_id");
						} catch (Exception e) {

						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			case NavitRecentDest_id:
				try {
					if (resultCode == AppCompatActivity.RESULT_OK) {
						Log.d(TAG, "recent dest id=" + Integer.parseInt(data.getStringExtra("selected_id")));
						// get the coords for the destination
						int destination_id = Integer.parseInt(data.getStringExtra("selected_id"));

						if (data.getStringExtra("what").equals("view")) {
							try {
								Navit.follow_button_off();
							} catch (Exception e2) {
								e2.printStackTrace();
							}

							float lat = Navit.map_points.get(destination_id).lat;
							float lon = Navit.map_points.get(destination_id).lon;
							show_geo_on_screen_with_zoom_and_delay(lat, lon, 150);
						} else {
							// ok now set target
							String dest_name = Navit.map_points.get(destination_id).point_name;
							float lat = Navit.map_points.get(destination_id).lat;
							float lon = Navit.map_points.get(destination_id).lon;

							// System.out.println("XXXXXX:" + lat + " " + lon);

							route_wrapper(dest_name, 0, 0, false, lat, lon, true);

							final Thread zoom_to_route_007 = new Thread() {
								int wait = 1;
								int count = 0;
								final int max_count = 60;

								@Override
								public void run() {
									while (wait == 1) {
										try {
											if ((NavitGraphics.navit_route_status == 17) || (NavitGraphics.navit_route_status == 33)) {
												zoom_to_route();
												wait = 0;
											} else {
												wait = 1;
											}

											count++;
											if (count > max_count) {
												wait = 0;
											} else {
												Thread.sleep(400);
											}
										} catch (Exception e) {
										}
									}
								}
							};
							zoom_to_route_007.start();

							// ---------- DEBUG: write route to file ----------
							// ---------- DEBUG: write route to file ----------
							// ---------- DEBUG: write route to file ----------
							if (preferences.PREF_enable_debug_write_gpx) {
								write_route_to_gpx_file();
							}
							// ---------- DEBUG: write route to file ----------
							// ---------- DEBUG: write route to file ----------

							try {
								Navit.follow_button_on();
							} catch (Exception e2) {
								e2.printStackTrace();
							}

						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;

			case NavitSendFeedback_id:
				try {
					if (resultCode == AppCompatActivity.RESULT_OK) {
						String feedback_text = data.getStringExtra("feedback_text");

						String subject_d_version = "";
						if (Navit_DonateVersion_Installed) {
							subject_d_version = subject_d_version + "D,";
						}

						if (Navit_Largemap_DonateVersion_Installed) {
							subject_d_version = subject_d_version + "L,";
						}

						try {
							int rl = get_reglevel();

							if (rl > 0) {
								subject_d_version = "U" + rl + ",";
							}
						} catch (Exception e) {
							e.printStackTrace();
						}

						String FD_addon = "";
						if (FDBL) {
							FD_addon = ",FD";
						}

						sendEmail("feedback@zanavi.cc", "ZANavi Feedback (v:" + subject_d_version + FD_addon + NavitAppVersion + " a:" + Build.VERSION.SDK + ")", feedback_text);
					}
				} catch (Exception e) {
					e.printStackTrace();
					Toast.makeText(getApplicationContext(), Navit.get_text("there was a problem with sending feedback"), Toast.LENGTH_SHORT).show(); //TRANS
				}
				break;

			default:
				Log.e(TAG, "onActivityResult " + requestCode + " " + resultCode);
				try {
					// ---------- what is this doing ????? ----------
					// ---------- what is this doing ????? ----------
					// ---------- what is this doing ????? ----------
					// ---------- what is this doing ????? ----------
					// ---------- what is this doing ????? ----------
					// ---------- what is this doing ????? ----------
					// ---------- what is this doing ????? ----------
					// ---------- what is this doing ????? ----------
					// ---------- what is this doing ????? ----------
					// ---------- what is this doing ????? ----------
					// ActivityResults[requestCode].onActivityResult(requestCode, resultCode, data);
					// ---------- what is this doing ????? ----------
					// ---------- what is this doing ????? ----------
					// ---------- what is this doing ????? ----------
					// ---------- what is this doing ????? ----------
					// ---------- what is this doing ????? ----------
					// ---------- what is this doing ????? ----------
					// ---------- what is this doing ????? ----------
					// ---------- what is this doing ????? ----------
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
		}
		Log.e(TAG, "onActivityResult finished");
	}

	class SCCB_object
	{
		int w;
		int h;
		Bitmap mb;
	}

	class CMC_object
	{
		int i;
		String s;
	}

	class MCB_object
	{
		int x1;
		int y1;
		int x2;

		int y2;
	}

	class TCB_object
	{
		int del;
		long id;
		NavitTimeout nt;
	}

	class GeCB_Object
	{
		int type;
		int a;
		float b;
		float c;
	}

	class LowQ_Object
	{
		String latlonzoom;
		int w;
		int h;
		int fontsize;
		int scale;
		int selection_range;
	}

	public class CWorkerThread extends Thread
	{
		private Boolean running;
		Boolean startmain = false;

		String lang;
		String display_density_string;
		int timeout_loop_counter = 0;
		String n_datadir;
		String n_sharedir;

		private final LinkedBlockingQueue<CMC_object> queue = new LinkedBlockingQueue<>();
		private final LinkedBlockingQueue<Integer> queue2 = new LinkedBlockingQueue<>();
		private final LinkedBlockingQueue<MCB_object> queue3 = new LinkedBlockingQueue<>();
		private final LinkedBlockingQueue<TCB_object> queue4 = new LinkedBlockingQueue<>();
		private final LinkedBlockingQueue<SCCB_object> queue5 = new LinkedBlockingQueue<>();
		private final LinkedBlockingQueue<Location> queue6 = new LinkedBlockingQueue<>();
		private final LinkedBlockingQueue<GeCB_Object> queue7 = new LinkedBlockingQueue<>();
		private final LinkedBlockingQueue<LowQ_Object> queue8 = new LinkedBlockingQueue<>();

		CWorkerThread()
		{
			this.running = true;
		}

		public void DrawLowqualMap_wrapper(String latlonzoom, int w, int h, int fontsize, int scale, int selection_range)
		{
			// if (Navit.METHOD_DEBUG) Navit.my_func_name(0);

			LowQ_Object o = new LowQ_Object();
			o.latlonzoom = latlonzoom;
			o.w = w;
			o.h = h;
			o.fontsize = fontsize;
			o.scale = scale;
			o.selection_range = selection_range;
			queue8.offer(o);
			this.interrupt();

			// if (Navit.METHOD_DEBUG) Navit.my_func_name(1);
		}

		public void SizeChangedCallback(int w, int h, Bitmap main_map_bitmap)
		{
			// if (Navit.METHOD_DEBUG) Navit.my_func_name(0);

			SCCB_object sccbo = new SCCB_object();
			sccbo.w = w;
			sccbo.h = h;
			sccbo.mb = main_map_bitmap;
			queue5.offer(sccbo);
			this.interrupt();

			// if (Navit.METHOD_DEBUG) Navit.my_func_name(1);
		}

		void TimeoutCallback2(NavitTimeout nt, int del, long id)
		{
			// if (Navit.METHOD_DEBUG) Navit.my_func_name(0);

			TCB_object tcbo = new TCB_object();
			tcbo.del = del;
			tcbo.id = id;
			tcbo.nt = nt;
			queue4.offer(tcbo);
			this.interrupt();
			//timeout_loop_counter++;

			//if (timeout_loop_counter > 100)
			//{
			//	timeout_loop_counter = 0;
			//	// run GC at every 100th loop
			//	// System.gc();
			//}
			// if (Navit.METHOD_DEBUG) Navit.my_func_name(1);
		}

		void CallbackMessageChannel(int i, String s)
		{
			// if (Navit.METHOD_DEBUG) Navit.my_func_name(0, "id=" + i);
			CMC_object cmco = new CMC_object();
			cmco.i = i;
			cmco.s = s;
			queue.offer(cmco);
			this.interrupt();
			// if (Navit.METHOD_DEBUG) Navit.my_func_name(1);
		}

		public void MotionCallback(int x1, int y1, int x2, int y2)
		{
			// if (Navit.METHOD_DEBUG) Navit.my_func_name(0);

			//System.out.println("MotionCallback:enter queue=" + queue3.size());
			MCB_object mcbo = new MCB_object();
			mcbo.x1 = x1;
			mcbo.y1 = y1;
			mcbo.x2 = x2;
			mcbo.y2 = y2;
			queue3.offer(mcbo);
			this.interrupt();
			// if (Navit.METHOD_DEBUG) Navit.my_func_name(1);
		}

		void NavitActivity2(int i)
		{
			// if (Navit.METHOD_DEBUG) Navit.my_func_name(0);

			queue2.offer(i);
			this.interrupt();
			// if (Navit.METHOD_DEBUG) Navit.my_func_name(1);
		}

		public void CallbackGeoCalc2(int type, int a, float b, float c)
		{
			// if (Navit.METHOD_DEBUG) Navit.my_func_name(0);

			GeCB_Object gcco = new GeCB_Object();
			gcco.type = type;
			gcco.a = a;
			gcco.b = b;
			gcco.c = c;
			queue7.offer(gcco);
			this.interrupt();

			// if (Navit.METHOD_DEBUG) Navit.my_func_name(1);
		}

		void StartMain(String lang, String display_density_string, String n_datadir, String n_sharedir)
		{
			// if (Navit.METHOD_DEBUG) Navit.my_func_name(0);

			//System.out.println("CWorkerThread:StartMain:JTHREAD ID=" + this.getId());
			//System.out.println("CWorkerThread:StartMain:THREAD ID=" + NavitGraphics.GetThreadId());

			this.startmain = true;
			this.lang = lang;
			this.n_datadir = n_datadir;
			this.n_sharedir = n_sharedir;
			this.display_density_string = display_density_string;
			this.interrupt();

			// if (Navit.METHOD_DEBUG) Navit.my_func_name(1);
		}

		void VehicleCallback3(Location location)
		{
			// if (Navit.METHOD_DEBUG) Navit.my_func_name(0);

			boolean your_are_speeding_old = Navit.you_are_speeding;

			//if ((Navit.cur_max_speed != -1) && (Navit.isGPSFix))
			if (Navit.cur_max_speed != -1)
			{
				Navit.you_are_speeding = false;
				float speed = (location.getSpeed() * 3.6f);
				if (speed >= 100 || (preferences.PREF_roadspeed_warning_margin > 9))
				{
					if (speed > (float) Navit.cur_max_speed * (((float) preferences.PREF_roadspeed_warning_margin + 100.0f) / 100.0f))
					{
						Navit.you_are_speeding = true;
					}
				}
				else
					{	// if warning_margin < 10 it is maxspeed + margin or maxspeed * ( 1 + margin/100)
						// 5 in France, 6 in Belgium, 3 in The Netherlands(+4 threshold) is the correction before a fine
						if (speed > (Navit.cur_max_speed + preferences.PREF_roadspeed_warning_margin))
						{
							Navit.you_are_speeding = true;
						}

					}


				if(Navit.you_are_speeding)
				{
					try
					{

						if (!toneG_heard)
						{
							// make "beep" sound to indicate we are going to fast!!
							if (toneG != null)
							{
								if (preferences.PREF_roadspeed_warning)
								{
									toneG.stopTone();
									toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 500);
								}
								toneG_heard = true;
							}

						}
					} catch (Exception e) {
					}
				}
				else
				{
					// reset "beep" flag
					Navit.toneG_heard = false;
				}
			}
			else
			{
				Navit.you_are_speeding = false;
			}

			// fixme
			// probably has to redraw if maxspeed changed
			// but still speeding to reflect the new maxspeed --jdg--jdg--
			if (your_are_speeding_old != Navit.you_are_speeding)
			{
				//System.out.println("xx paint 6 xx");
				// NavitOSDJava.draw_real_wrapper(false, true);
				ZANaviLinearLayout.redraw_OSD(8);
			}


			while (queue6.size() > 2)
			{
				try
				{
					// if too many gps updates are waiting, then only process the last few entry!!
					queue6.poll();
				}
				catch (Exception e)
				{
				}
			}

			queue6.offer(location);
			this.interrupt();

			// if (Navit.METHOD_DEBUG) Navit.my_func_name(1);
		}

		 void calc_sun_stats()
		{
			//
			//
			// SUN ----------------
			//
			//
			boolean sun_moon__must_calc_new = (SystemClock.elapsedRealtime() - sun_moon__mLastCalcSunMillis) > (60000 * 3); // calc new every 3 minutes

			if ((sun_moon__must_calc_new) || (azmiuth_cache == -1))
			{
				float lat = 0;
				float lon = 0;
				try
				{
					// String lat_lon = NavitGraphics.CallbackGeoCalc(1, NavitGraphics.Global_dpi_factor * NG__map_main.view.getWidth() / 2, NavitGraphics.Global_dpi_factor * NG__map_main.view.getHeight() / 2);
					String lat_lon;
					if (Navit.GFX_OVERSPILL)
					{
						lat_lon = NavitGraphics.CallbackGeoCalc(1, NavitGraphics.Global_dpi_factor * (NG__map_main.view.getWidth() / 2 + NavitGraphics.mCanvasWidth_overspill), NavitGraphics.Global_dpi_factor * (NG__map_main.view.getHeight() / 2 + NavitGraphics.mCanvasHeight_overspill));
					}
					else
					{
						lat_lon = NavitGraphics.CallbackGeoCalc(1, NavitGraphics.Global_dpi_factor * NG__map_main.view.getWidth() / 2, NavitGraphics.Global_dpi_factor * NG__map_main.view.getHeight() / 2);
					}

					String[] tmp = lat_lon.split(":", 2);
					//System.out.println("tmp=" + lat_lon);
					lat = Float.parseFloat(tmp[0]);
					lon = Float.parseFloat(tmp[1]);
					//System.out.println("ret=" + lat_lon + " lat=" + lat + " lon=" + lon);
				}
				catch (Exception e)
				{
				}

				try
				{
					sun_moon__mLastCalcSunMillis = SystemClock.elapsedRealtime();
					TimeZone t = TimeZone.getDefault(); // Since the user's time zone changes dynamically, avoid caching this value. Instead, use this method to look it up for each use. 
					//System.out.println(t.getID());
					SunriseSunsetCalculator sun_moon__calc = new SunriseSunsetCalculator(new Location2(String.valueOf(lat), String.valueOf(lon)), t.getID());
					Calendar sun_moon__cx = Calendar.getInstance();
					SolarPosition.SunCoordinates sun_moon__sc = SolarPosition.getSunPosition(new Date(), lat, lon);

					azmiuth_cache = sun_moon__sc.azimuth;
					zenith_cache = sun_moon__sc.zenithAngle;
					String sunrise_cache = sun_moon__calc.getOfficialSunriseForDate(sun_moon__cx);
					String sunset_cache = sun_moon__calc.getOfficialSunsetForDate(sun_moon__cx);
					//System.out.println("calc moon");
					SolarEventCalculator.moonCoor_ret moon_stats = sun_moon__calc.computeMoon(sun_moon__cx);
					double moon_azimuth_cache = moon_stats.az;
					double moon_evelation_cache = moon_stats.alt;
				}
				catch (Exception e)
				{
				}
			}
			//
			elevation = 90 - zenith_cache;
			//
			// day          -> +90.0  to - 0.83
			// evening dusk -> - 0.83 to -10.00
			if (elevation < -0.83)
			{
				is_night = true;
				if (elevation < -10.00)
				{
					is_twilight = false;
				}
				else
				{
					is_twilight = true;
				}
				//System.out.println("***NIGHT***");
			}
			else
			{
				is_night = false;
				//System.out.println("###DAY###");
			}
			//
			// SUN ----------------
			//
			//
		}

		void do_sun_calc()
		{
			//
			//
			// SUN ----------------
			//
			//
			try
			{
				this.calc_sun_stats();
			}
			catch (Exception e)
			{
				// on some systems BigInteger seems to crash, or maybe some values are out of range
				// until the bug is found, night modus is deactivated
				boolean calc_sun_enabled = false;
				is_twilight = false;
				is_night = false;
			}
			//System.out.println("sunrise: " + sunrise_cache);
			//System.out.println("sunset: " + sunset_cache);
			//System.out.println("azimuth: " + roundTwoDecimals(azmiuth_cache));
			//System.out.println("elevation: " + elevation);
			//
			//
			// SUN ----------------
			//
			//
		}

		public void run()
		{
			//System.out.println("CWorkerThread -- started --");
			while (this.running)
			{
				if ((queue4.size() == 0) && (queue6.size() == 0))
				{
					try
					{
						Thread.sleep(2000); // 2 secs.
					}
					catch (InterruptedException e)
					{
					}
				}

				if (this.startmain)
				{
					//System.out.println("CWorkerThread:startup_calls:JTHREAD ID=" + this.getId());
					//System.out.println("CWorkerThread:startup_calls:THREAD ID=" + NavitGraphics.GetThreadId());

					this.startmain = false;
					System.out.println("CWorkerThread -- NavitMain --");
					NavitMain(lang, display_density_string, n_datadir, n_sharedir, NavitGraphics.draw_bitmap_s);
					System.out.println("CWorkerThread -- NavitActivity(3) --");
					NavitActivity(3); //start the navit lib

					// -- set map detail level (after app startup) --
					// -- set map detail level (after app startup) --
					// -- set map detail level (after app startup) --
					try
					{
						getPrefs_more_map_detail();
						if (preferences.PREF_more_map_detail > 0)
						{
							Message msg2 = new Message();
							Bundle b2 = new Bundle();
							b2.putInt("Callback", 78);
							b2.putString("s", "" + preferences.PREF_more_map_detail);
							msg2.setData(b2);
							callback_handler_55.sendMessage(msg2);
						}
					}
					catch (Exception e)
					{
						// e.printStackTrace();
					}
					// -- set map detail level (after app startup) --


					// -- set map DPI factor (after app startup) --
					try
					{
						if ((Navit.metrics.densityDpi >= 320) && (!preferences.PREF_shrink_on_high_dpi))
						{
							double factor;
							factor = (double) Navit.metrics.densityDpi / (double) NavitGraphics.Global_Scaled_DPI_normal;

							Message msg2 = new Message();
							Bundle b2 = new Bundle();
							b2.putInt("Callback", 81);
							b2.putString("s", "" + factor);
							msg2.setData(b2);
							callback_handler_55.sendMessage(msg2);
						}
					}
					catch (Exception e)
					{
						// e.printStackTrace();
					}
					// -- set map DPI factor (after app startup) --
					// -- set map DPI factor (after app startup) --
					// -- set map DPI factor (after app startup) --
					Global_Init_Finished = true;
					//x.runOnUiThread(new Runnable()
					//{
					//	public void run()
					//	{
					//		NavitActivity(3);
					//	}
					//});

					//**getPrefs();
					//**activatePrefs();

					System.out.println("CWorkerThread -- calling:ready --");
				}

				try
				{
					while (queue8.size() > 0)
					{
						LowQ_Object l9 = queue8.poll();
						if (l9 != null)
						{
							// System.out.println("DrawLowqualMap");
							NavitGraphics.DrawLowqualMap(l9.latlonzoom, l9.w, l9.h, l9.fontsize, l9.scale, l9.selection_range);
						}
					}
				}
				catch (Exception e)
				{
				}

				Location l7;
				while (queue6.size() > 0)
				{
					try
					{
						// blocking call
						// l2 = queue6.take();
						// non-blocking call
						l7 = queue6.poll();
						if (l7 != null)
						{
							NavitVehicle.VehicleCallback(l7.getLatitude(), l7.getLongitude(), (l7.getSpeed() * 3.6f), l7.getBearing(), l7.getAltitude(), l7.getAccuracy(), (l7.getTime() / 1000L));
						}
					}

					catch (Exception e)
					{
					}

				}

				while (queue.size() > 0)
				{
					try
					{
						// blocking call
						// l2 = queue.take();
						// non-blocking call
						CMC_object l2 = queue.poll();
						if (l2 != null)
						{
							//System.out.println("CWorkerThread:CallbackMessageChannelReal_call:JTHREAD ID=" + this.getId());
							//System.out.println("CWorkerThread:CallbackMessageChannelReal_call:THREAD ID=" + NavitGraphics.GetThreadId());
							//System.out.println("CWorkerThread:CallbackMessageChannelReal:" + l2.i);
							NavitGraphics.CallbackMessageChannelReal(l2.i, l2.s);
							//System.out.println("CWorkerThread:CallbackMessageChannelReal:finished");
						}
					}
					catch (Exception e)
					{
					}

					// if GPS updates are pending, process them
					if (queue6.size() > 0)
					{
						try
						{
							// blocking call
							// l2 = queue6.take();
							// non-blocking call
							l7 = queue6.poll();
							if (l7 != null)
							{
								NavitVehicle.VehicleCallback(l7.getLatitude(), l7.getLongitude(), (l7.getSpeed() * 3.6f), l7.getBearing(), l7.getAltitude(), l7.getAccuracy(), (l7.getTime() / 1000L));
							}
						}
						catch (Exception e)
						{
						}
					}

				}

				while (queue5.size() > 0)
				{
					try
					{
						// blocking call
						// l6 = queue5.take();
						// non-blocking call
						SCCB_object l6 = queue5.poll();
						if (l6 != null)
						{
							//System.out.println("CWorkerThread:SizeChangedCallbackReal_call:JTHREAD ID=" + this.getId());
							//System.out.println("CWorkerThread:SizeChangedCallbackReal_call:THREAD ID=" + NavitGraphics.GetThreadId());
							NavitGraphics.SizeChangedCallbackReal(l6.w, l6.h, l6.mb);
						}
					}
					catch (Exception e)
					{
					}

				}

				int count_timeout_callbacks = 0;
				while (count_timeout_callbacks < 10 && queue4.size() > 0)
				{
					count_timeout_callbacks++;
					try
					{
						// blocking call
						// l5 = queue4.take();
						// non-blocking call
						TCB_object l5 = queue4.poll();
						if (l5 != null)
						{
							//System.out.println("CWorkerThread:TimeoutCallback_call:JTHREAD ID=" + this.getId());
							//System.out.println("CWorkerThread:TimeoutCallback_call:THREAD ID=" + NavitGraphics.GetThreadId());
							if ((l5.nt.running) || (!l5.nt.event_multi))
							{
								NavitGraphics.TimeoutCallback(l5.del, l5.id);
							}
							else
							{
								//	System.out.println("CWorkerThread:TimeoutCallback_call:running=false! cid=" + l5.id);
							}
						}
					}
					catch (Exception e)
					{
					}

					// if GPS updates are pending, process them
					if (queue6.size() > 0)
					{
						try
						{
							// blocking call
							// l2 = queue6.take();
							// non-blocking call
							l7 = queue6.poll();
							if (l7 != null)
							{
								NavitVehicle.VehicleCallback(l7.getLatitude(), l7.getLongitude(), (l7.getSpeed() * 3.6f), l7.getBearing(), l7.getAltitude(), l7.getAccuracy(), (l7.getTime() / 1000L));
							}
						}
						catch (Exception e)
						{
						}
					}

				}

				while (queue3.size() > 0)
				{
					try
					{
						// blocking call
						// l4 = queue3.take();
						// non-blocking call
						MCB_object l4 = queue3.poll();
						if (l4 != null)
						{
							//System.out.println("CWorkerThread:MotionCallbackReal_call:JTHREAD ID=" + this.getId());
							//System.out.println("CWorkerThread:MotionCallbackReal_call:THREAD ID=" + NavitGraphics.GetThreadId());
							if (queue3.size() > 0)
							{
								// if more moves are queued up, disable map drawing!
								NavitGraphics.MotionCallbackReal(l4.x1, l4.y1, l4.x2, l4.y2, 0);
							}
							else
							{
								// ok, also draw the map
								NavitGraphics.MotionCallbackReal(l4.x1, l4.y1, l4.x2, l4.y2, 1);
							}
						}
					}
					catch (Exception e)
					{
					}

				}

				while (queue7.size() > 0)
				{
					try
					{

						// if GPS updates are pending, process them
						if (queue6.size() > 0)
						{
							try
							{
								// blocking call
								// l2 = queue6.take();
								// non-blocking call
								l7 = queue6.poll();
								if (l7 != null)
								{
									NavitVehicle.VehicleCallback(l7.getLatitude(), l7.getLongitude(), (l7.getSpeed() * 3.6f), l7.getBearing(), l7.getAltitude(), l7.getAccuracy(), (l7.getTime() / 1000L));
								}
							}
							catch (Exception e)
							{
							}
						}

						GeCB_Object l8 = queue7.poll();
						if (l8 != null)
						{
							if (l8.type == 1)
							{
								Navit.OSD_nextturn.nextturn_streetname_systematic = "";
								// System.out.println("street name(1)");
								Navit.OSD_nextturn.nextturn_streetname = NavitGraphics.CallbackGeoCalc(8, l8.b, l8.c);
								// System.out.println("street name(2):" + Navit.OSD_nextturn.nextturn_streetname);

								if (preferences.PREF_item_dump)
								{
									// -------- DEBUG ------- DEBUG ---------
									// -------- DEBUG ------- DEBUG ---------
									// -------- DEBUG ------- DEBUG ---------
									Navit.debug_item_dump = NavitGraphics.CallbackGeoCalc(9, l8.b, l8.c);
									//System.out.println("xx paint 22 xx");
									NavitGraphics.NavitAOverlay_s.postInvalidate();
									// -------- DEBUG ------- DEBUG ---------
									// -------- DEBUG ------- DEBUG ---------
									// -------- DEBUG ------- DEBUG ---------
								}
								//System.out.println("OSD postinvalidate***");
								//System.out.println("xx paint 7 xx");
								// NavitOSDJava.draw_real_wrapper(true, false);
								ZANaviLinearLayout.redraw_OSD(1);
								//++ NavitGraphics.NavitAOSDJava_.postInvalidate();
							}
							else if (l8.type == 2)
							{
								NavitGraphics.CallbackGeoCalc(l8.a, l8.b, l8.c);
							}
							else if (l8.type == 13)
							{
								try
								{
									// get roadbook
									// String road_book_res = NavitGraphics.CallbackGeoCalc(13, l8.b, l8.c);
									String[] separated = NavitGraphics.GetRoadBookItems(12345);
									// System.out.println("ROADBOOK_RES=" + separated);

									// parse roadbook data ------------
									road_book_items.clear();
									ZANaviListViewAdapter.ListViewItem l = null;

									// String[] separated = road_book_res.split("\13");
									int jk = 0;
									int ResId = 0;
									for (jk = 0; jk < separated.length; jk++)
									{
										if (jk > 1)
										{
											// System.out.println("ROADBOOK_RES=" + jk + ":" + separated[jk]);
											String[] values = new String[5];
											String[] values2 = separated[jk].split(":");
											values[0] = values2[0];
											values[1] = values2[1];
											values[2] = values2[2];
											values[3] = values2[3];
											try
											{
												values[4] = values2[4];
											}
											catch (Exception ee)
											{
												values[4] = "";
											}
											// 0 string:distance short form
											// 1 lat
											// 2 lon
											// 3 icon name
											// 4 text

											// System.out.println("RBI:008:img=" + values[3]);

											if (values[3].compareTo("nav_waypoint") == 0)
											{
												try
												{
													ResId = Navit.sResources.getIdentifier("com.zoffcc.applications.zanavi:drawable/" + "nav_waypoint_bk_center", null, null);
												}
												catch (Exception e_res_id)
												{
													ResId = 0;
													e_res_id.printStackTrace();
												}
											}
											else if (values[3].compareTo("nav_destination") == 0)
											{
												try
												{
													ResId = Navit.sResources.getIdentifier("com.zoffcc.applications.zanavi:drawable/" + "nav_destination_bk_center", null, null);
												}
												catch (Exception e_res_id)
												{
													ResId = 0;
													e_res_id.printStackTrace();
												}

												if (ResId == 0)
												{
													// *TODO*
													System.out.println("NavitGraphics:" + "== missing roadbook icon(1) ==:" + "drawable/" + "nav_destination_bk_center");
												}
											}
											else
											{

												try
												{
													ResId = Navit.sResources.getIdentifier("com.zoffcc.applications.zanavi:drawable/" + values[3] + "_bk", null, null);
												}
												catch (Exception e_res_id)
												{
													ResId = 0;
													e_res_id.printStackTrace();
												}

												if (ResId == 0)
												{
													// *TODO*
													System.out.println("NavitGraphics:" + "== missing roadbook icon(2) ==:" + "drawable/" + values[3] + "_bk");
												}
											}

											try
											{
												// System.out.println("RBI:008+" + ResId);
												if (ResId != 0)
												{
													l = new ListViewItem(values[0], sResources.getDrawable(ResId), "", values[4], Float.parseFloat(values[1]), Float.parseFloat(values[2]));
												}
												else
												{
													l = new ListViewItem(values[0], sResources.getDrawable(R.drawable.mini_roundabout), "", values[4], Float.parseFloat(values[1]), Float.parseFloat(values[2]));
												}
												// System.out.println("RBI:008");
												road_book_items.add(l);
												// System.out.println("RBI:009");
											}
											catch (Exception ee)
											{
												// System.out.println("item=" + separated[jk] + " EEXX:" + ee.getMessage());
											}
										}
									}
									// System.out.println("RBI:010");

									try
									{
										Message msg = Navit_progress_h.obtainMessage();
										Bundle b = new Bundle();
										msg.what = 33;
										msg.setData(b);
										Navit_progress_h.sendMessage(msg);
									}
									catch (Exception e)
									{
										e.printStackTrace();
									}
									// System.out.println("RBI:011");
								}
								catch (Exception eerb)
								{
									eerb.printStackTrace();
								}
								// parse roadbook data ------------

							}
						}
					}
					catch (Exception e)
					{
					}

				}

				while (queue2.size() > 0)
				{
					try
					{
						// blocking call
						// l3 = queue2.take();
						// non-blocking call
						Integer l3 = queue2.poll();
						if (l3 != null)
						{
							int i3 = l3;
							//System.out.println("CWorkerThread:NavitActivity_call:JTHREAD ID=" + this.getId());
							//System.out.println("CWorkerThread:NavitActivity_call:THREAD ID=" + NavitGraphics.GetThreadId());
							//System.out.println("CWorkerThread:NavitActivity:" + i3);
							NavitActivity(i3);
						}
					}
					catch (Exception e)
					{
					}
				}

				// check sun position (and after interval, recalc values)
				do_sun_calc();
			}
			//System.out.println("CWorkerThread -- stopped --");
		}

		public void stop_me()
		{
			// if (Navit.METHOD_DEBUG) Navit.my_func_name(0);

			this.running = false;
			this.interrupt();

			// if (Navit.METHOD_DEBUG) Navit.my_func_name(1);
		}
	}

	class SmoothVehicle extends Thread
	{
		private Boolean running;

		SmoothVehicle()
		{
			this.running = true;
		}

		public void run()
		{
			while (this.running)
			{
				try
				{
					Thread.sleep(5000); // 5 secs.
				}
				catch (InterruptedException e)
				{
				}
			}
		}

		public void stop_me()
		{
			this.running = false;
		}
	}

	class WatchMem extends Thread
	{
		private Boolean running;

		WatchMem()
		{
			this.running = true;
		}

		public void run()
		{
			//System.out.println("WatchMem -- started --");
			while (this.running)
			{
				Navit.show_mem_used();

				try
				{
					Thread.sleep(5000); // 5 secs.
				}
				catch (InterruptedException e)
				{
				}
			}
			//System.out.println("WatchMem -- stopped --");
		}

		void stop_me()
		{
			this.running = false;
		}
	}

	class SimGPS extends Thread
	{
		private Boolean running;
		private final Handler h;

		SimGPS(Handler h_)
		{
			System.out.println("SimGPS -- inited --");
			this.h = h_;
			this.running = true;
		}

		public void run()
		{
			System.out.println("SimGPS -- started --");
			while (this.running)
			{
				float rnd_heading = (float) (Math.random() * 360d);
				float lat = 48.216023f;
				float lng = 16.391664f;
				//Location l = new Location("Network");
				//l.setLatitude(lat);
				//l.setLongitude(lng);
				//l.setBearing(rnd_heading);
				// NavitVehicle.set_mock_location__fast(l);
				// NavitVehicle.update_compass_heading(rnd_heading);
				if (this.h != null)
				{
					Message msg = this.h.obtainMessage();
					Bundle b = new Bundle();
					msg.what = 1;
					b.putFloat("b", rnd_heading);
					b.putFloat("lat", lat);
					b.putFloat("lng", lng);
					msg.setData(b);
					this.h.sendMessage(msg);
				}
				try
				{
					Thread.sleep(800);
				}
				catch (InterruptedException e)
				{
				}
			}
			System.out.println("SimGPS -- stopped --");
		}

		public void stop_me()
		{
			this.running = false;
		}
	}

	class SearchResultsThreadSpinnerThread extends Thread
	{
		final int dialog_num;
		int spinner_current_value;
		private Boolean running;
		final Handler mHandler;

		SearchResultsThreadSpinnerThread(Handler h, int dialog_num)
		{
			this.dialog_num = dialog_num;
			this.mHandler = h;
			this.spinner_current_value = 0;

			this.running = true;
			Log.e(TAG, "SearchResultsThreadSpinnerThread created");
		}

		public void run()
		{
			Log.e(TAG, "SearchResultsThreadSpinnerThread started");
			while (this.running)
			{
				if (Navit.NavitAddressSearchSpinnerActive == false)
				{
					this.running = false;
				}
				else
				{
					Message msg = mHandler.obtainMessage();
					Bundle b = new Bundle();
					msg.what = 10;
					b.putInt("dialog_num", this.dialog_num);
					b.putInt("max", Navit.ADDRESS_RESULTS_DIALOG_MAX);
					b.putInt("cur", this.spinner_current_value % (Navit.ADDRESS_RESULTS_DIALOG_MAX + 1));
					if ((Navit.NavitSearchresultBar_title.equals("")) && (Navit.NavitSearchresultBar_text.equals("")))
					{
						b.putString("title", Navit.get_text("getting search results")); //TRANS
						b.putString("text", Navit.get_text("searching ...")); //TRANS
					}
					else
					{
						b.putString("title", Navit.NavitSearchresultBar_title);
						b.putString("text", Navit.NavitSearchresultBar_text);
					}
					msg.setData(b);
					mHandler.sendMessage(msg);
					try
					{
						Thread.sleep(700);
					}
					catch (InterruptedException e)
					{
						// e.printStackTrace();
					}
					this.spinner_current_value++;
				}
			}
			Log.e(TAG, "SearchResultsThreadSpinnerThread ended");
		}
	}

	class SearchResultsThread extends Thread
	{
		private Boolean running;
		final Handler mHandler;
		final int my_dialog_num;

		SearchResultsThread(Handler h, int dialog_num)
		{
			this.running = true;
			this.mHandler = h;
			this.my_dialog_num = dialog_num;
			Log.e(TAG, "SearchResultsThread created");
		}

		void stop_me()
		{
			this.running = false;
		}

		public void run()
		{
			Log.e(TAG, "SearchResultsThread started");

			System.out.println("Global_Location_update_not_allowed = 1");
			Navit.Global_Location_update_not_allowed = 1; // dont allow location updates now!

			// initialize the dialog with sane values
			Message msg = mHandler.obtainMessage();
			Bundle b = new Bundle();
			msg.what = 10;
			b.putInt("dialog_num", this.my_dialog_num);
			b.putInt("max", Navit.ADDRESS_RESULTS_DIALOG_MAX);
			b.putInt("cur", 0);
			b.putString("title", Navit.get_text("getting search results")); //TRANS
			b.putString("text", Navit.get_text("searching ...")); //TRANS
			msg.setData(b);
			mHandler.sendMessage(msg);

			int partial_match_i = 0;
			if (Navit_last_address_partial_match)
			{
				partial_match_i = 1;
			}

			if (this.my_dialog_num == Navit.SEARCHRESULTS_WAIT_DIALOG_OFFLINE)
			{
				Navit.search_ready = false;

				// start the search, this could take a long time!!
				Log.e(TAG, "SearchResultsThread run1");
				// need lowercase to find stuff !!
				Navit_last_address_search_string = filter_bad_chars(Navit_last_address_search_string).toLowerCase();
				if ((Navit_last_address_hn_string != null) && (!Navit_last_address_hn_string.equals("")))
				{
					Navit_last_address_hn_string = filter_bad_chars(Navit_last_address_hn_string).toLowerCase();
				}

				if (Navit_last_address_full_file_search)
				{
					// flags (18)		-> order level to search at
					// ================
					//   0#0   0		-> search full world
					// lat#lon radius	-> search only this area, around lat,lon
					// ================
					NG__map_main.SearchResultList(3, partial_match_i, Navit_last_address_search_string, "", "", 18, Navit_last_address_search_country_iso2_string, "0#0", 0);
				}
				else
				{
					if (Navit.use_index_search)
					{
						// new method with index search
						// -----------------
						//Navit_last_address_search_string
						String street_ = "";
						String town_ = "";
						String hn_ = Navit_last_address_hn_string;

						int last_space = Navit_last_address_search_string.lastIndexOf(" ");
						if (last_space != -1)
						{
							street_ = Navit_last_address_search_string.substring(0, last_space);
							town_ = Navit_last_address_search_string.substring(last_space + 1);
							// System.out.println("XX" + street_ + "YY" + town_ + "ZZ");
						}
						else
						{
							street_ = Navit_last_address_search_string;
							town_ = "";
						}

						System.out.println("search params1=" + street_ + ":" + town_ + ":" + hn_);
						NG__map_main.SearchResultList(2, partial_match_i, street_, town_, hn_, Navit_last_address_search_country_flags, Navit_last_address_search_country_iso2_string, "0#0", 0);

						// sort result list
						Collections.sort(Navit.NavitAddressResultList_foundItems);
					}
					else
					{
						// old method search
						// -----------------
						// flags --> 3: search all countries
						//           2: search <iso2 string> country
						//           1: search default country (what you have set as language in prefs)
						System.out.println("searching ... 001");

						System.out.println("search params2a=" + Navit_last_address_hn_string);

						String old_s = Navit_last_address_search_string;
						String new_s = Navit_last_address_search_string;
						if ((Navit_last_address_hn_string != null) && (!Navit_last_address_hn_string.equals("")))
						{
							new_s = Navit_last_address_search_string + " " + Navit_last_address_hn_string;
						}

						System.out.println("search params2=" + new_s + ":" + Navit_last_address_search_country_flags + ":" + Navit_last_address_search_country_iso2_string);

						NG__map_main.SearchResultList(29, partial_match_i, new_s, "", "", Navit_last_address_search_country_flags, Navit_last_address_search_country_iso2_string, "0#0", 0);
						System.out.println("searching ... 002");

						// sort result list
						Collections.sort(Navit.NavitAddressResultList_foundItems);

						System.out.println("searching ... 099");
						Navit.search_ready = true;
					}
				}
				Log.e(TAG, "SearchResultsThread run2");
			}
			else if (this.my_dialog_num == Navit.SEARCHRESULTS_WAIT_DIALOG)
			{
				// online googlemaps search
				// google search
				Log.e(TAG, "SearchResultsThread run1 -> online googlemaps search");
				String addressInput = filter_bad_chars(Navit_last_address_search_string);
				try
				{
					List<Address> foundAdresses = Navit.Navit_Geocoder.getFromLocationName(addressInput, 3); //Search addresses
					System.out.println("found " + foundAdresses.size() + " results");
					// System.out.println("addr=" + foundAdresses.get(0).getLatitude() + " " + foundAdresses.get(0).getLongitude() + "" + foundAdresses.get(0).getAddressLine(0));

					Navit.NavitAddressSearchSpinnerActive = false;

					for (int results_step = 0; results_step < foundAdresses.size(); results_step++)
					{
						Navit.Navit_Address_Result_Struct tmp_addr = new Navit_Address_Result_Struct();
						tmp_addr.result_type = "STR";
						tmp_addr.item_id = "0";
						tmp_addr.lat = (float) foundAdresses.get(results_step).getLatitude();
						tmp_addr.lon = (float) foundAdresses.get(results_step).getLongitude();
						tmp_addr.addr = "";

						String c_code = foundAdresses.get(results_step).getCountryCode();
						if (c_code != null)
						{
							tmp_addr.addr = tmp_addr.addr + foundAdresses.get(results_step).getCountryCode() + ",";
						}

						String p_code = foundAdresses.get(results_step).getPostalCode();
						if (p_code != null)
						{
							tmp_addr.addr = tmp_addr.addr + foundAdresses.get(results_step).getPostalCode() + " ";
						}

						if (foundAdresses.get(results_step).getMaxAddressLineIndex() > -1)
						{
							for (int addr_line = 0; addr_line < foundAdresses.get(results_step).getMaxAddressLineIndex(); addr_line++)
							{
								if (addr_line > 0) tmp_addr.addr = tmp_addr.addr + " ";
								tmp_addr.addr = tmp_addr.addr + foundAdresses.get(results_step).getAddressLine(addr_line);
							}
						}

						Navit.NavitAddressResultList_foundItems.add(tmp_addr);

						if (tmp_addr.result_type.equals("TWN"))
						{
							Navit.search_results_towns++;
						}
						else if (tmp_addr.result_type.equals("STR"))
						{
							Navit.search_results_streets++;
						}
						else if (tmp_addr.result_type.equals("SHN"))
						{
							Navit.search_results_streets_hn++;
						}
						else if (tmp_addr.result_type.equals("POI"))
						{
							Navit.search_results_poi++;
						}

						// make the dialog move its bar ...
						Bundle b2 = new Bundle();
						b2.putInt("dialog_num", Navit.SEARCHRESULTS_WAIT_DIALOG);
						b2.putInt("max", Navit.ADDRESS_RESULTS_DIALOG_MAX);
						b2.putInt("cur", Navit.NavitAddressResultList_foundItems.size() % (Navit.ADDRESS_RESULTS_DIALOG_MAX + 1));
						b2.putString("title", Navit.get_text("loading search results")); //TRANS
						b2.putString("text", Navit.get_text("towns") + ":" + Navit.search_results_towns + " " + Navit.get_text("Streets") + ":" + Navit.search_results_streets + "/" + Navit.search_results_streets_hn + " " + Navit.get_text("POI") + ":" + Navit.search_results_poi);

						Navit.msg_to_msg_handler(b2, 10);
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
					System.out.println("seems googlemaps API is not working, try offline search");
				}
			}

			Navit.NavitAddressSearchSpinnerActive = false;

			if (Navit.NavitAddressResultList_foundItems.size() > 0)
			{
				open_search_result_list();
			}
			else
			{
				// no results found, show toast
				msg = mHandler.obtainMessage();
				b = new Bundle();
				msg.what = 2;
				b.putString("text", Navit.get_text("No Results found!")); //TRANS
				msg.setData(b);
				mHandler.sendMessage(msg);
			}

			// ok, remove dialog
			Log.e(TAG, "SearchResultsThread:remove dialog (99)");
			msg = mHandler.obtainMessage();
			b = new Bundle();
			msg.what = 99;
			b.putInt("dialog_num", this.my_dialog_num);
			msg.setData(b);
			mHandler.sendMessage(msg);

			// reset the startup-search flag
			Navit.NavitStartupAlreadySearching = false;

			System.out.println("Global_Location_update_not_allowed = 0");
			Navit.Global_Location_update_not_allowed = 0; // DO allow location updates now!

			Log.e(TAG, "SearchResultsThread ended");
		}
	}

	public static String filter_bad_chars(String in)
	{
		String out = in;
		out = out.replaceAll("\\n", " "); // newline -> space
		out = out.replaceAll("\\r", " "); // return -> space
		out = out.replaceAll("\\t", " "); // tab -> space
		out = out.trim();
		return out;
	}

	private static void msg_to_msg_handler(Bundle b, int id)
	{
		Message msg = Navit_progress_h.obtainMessage();
		msg.what = id;
		msg.setData(b);
		Navit_progress_h.sendMessage(msg);
	}

	private void open_search_result_list()
	{
		// open result list
		Intent address_result_list_activity = new Intent(this, NavitAddressResultListActivity.class);
		this.startActivityForResult(address_result_list_activity, Navit.NavitAddressResultList_id);
	}

	static final Handler callback_handler_55 = new Handler()
	{
		public void handleMessage(Message msg)
		{
			// handle 111111
			// if (Navit.METHOD_DEBUG) Navit.my_func_name(0, "" + msg.getData().getInt("Callback"));

			if (msg.getData().getInt("Callback") == 1)
			{
				// zoom in
				CallbackMessageChannel(1, "");
			}
			else if (msg.getData().getInt("Callback") == 2)
			{
				// zoom out
				CallbackMessageChannel(2, "");
			}
			else if (msg.getData().getInt("Callback") == 55599)
			{
				// calc route after adding points
				CallbackMessageChannel(55599, "");
			}
			else if (msg.getData().getInt("Callback") == 55503)
			{
				try
				{
					NavitVehicle.pos_recording_add(2, 0, 0, 0, 0, 0); // CLR
					NavitVehicle.pos_recording_add(3, Float.parseFloat(msg.getData().getString("lat")), Float.parseFloat(msg.getData().getString("lon")), 0, 0, 0);
				}
				catch (Exception e)
				{
				}

				// get values
				String lat = msg.getData().getString("lat");
				String lon = msg.getData().getString("lon");
				String q = msg.getData().getString("q");
				// set routing target to lat,lon
				CallbackMessageChannel(55503, lat + "#" + lon + "#" + q);
			}
			else if (msg.getData().getInt("Callback") == 3)
			{
				try
				{
					NavitVehicle.pos_recording_add(2, 0, 0, 0, 0, 0); // CLR
					NavitVehicle.pos_recording_add(3, Float.parseFloat(msg.getData().getString("lat")), Float.parseFloat(msg.getData().getString("lon")), 0, 0, 0);
				}
				catch (Exception e)
				{
				}

				// get values
				String lat = msg.getData().getString("lat");
				String lon = msg.getData().getString("lon");
				String q = msg.getData().getString("q");
				// set routing target to lat,lon
				CallbackMessageChannel(3, lat + "#" + lon + "#" + q);
			}
			else if (msg.getData().getInt("Callback") == 55548)
			{
				try
				{
					NavitVehicle.pos_recording_add(3, Float.parseFloat(msg.getData().getString("lat")), Float.parseFloat(msg.getData().getString("lon")), 0, 0, 0);
				}
				catch (Exception e)
				{
				}

				// get values
				String lat = msg.getData().getString("lat");
				String lon = msg.getData().getString("lon");
				String q = msg.getData().getString("q");
				// append to routing, add waypoint at lat,lon
				CallbackMessageChannel(55548, lat + "#" + lon + "#" + q);
			}
			else if (msg.getData().getInt("Callback") == 48)
			{
				try
				{
					NavitVehicle.pos_recording_add(3, Float.parseFloat(msg.getData().getString("lat")), Float.parseFloat(msg.getData().getString("lon")), 0, 0, 0);
				}
				catch (Exception e)
				{
				}

				// get values
				String lat = msg.getData().getString("lat");
				String lon = msg.getData().getString("lon");
				String q = msg.getData().getString("q");
				// append to routing, add waypoint at lat,lon
				CallbackMessageChannel(48, lat + "#" + lon + "#" + q);
			}
			else if (msg.getData().getInt("Callback") == 4)
			{
				// set routing target to pixel x,y
				int x = msg.getData().getInt("x");
				int y = msg.getData().getInt("y");

				CallbackMessageChannel(4, "" + x + "#" + y);
				try
				{
					Navit.follow_button_on();
				}
				catch (Exception e2)
				{
					e2.printStackTrace();
				}
			}
			else if (msg.getData().getInt("Callback") == 49)
			{
				// set routing target to pixel x,y
				int x = msg.getData().getInt("x");
				int y = msg.getData().getInt("y");

				CallbackMessageChannel(49, "" + x + "#" + y);
				try
				{
					Navit.follow_button_on();
				}
				catch (Exception e2)
				{
					e2.printStackTrace();
				}
			}
			else if (msg.getData().getInt("Callback") == 5)
			{
				// toggle layer on/off
				String s = msg.getData().getString("s");
				CallbackMessageChannel(5, s);
			}
			else if (msg.getData().getInt("Callback") == 7)
			{
				CallbackMessageChannel(7, "");
			}
			else if ((msg.getData().getInt("Callback") > 7) && (msg.getData().getInt("Callback") < 21))
			{
				CallbackMessageChannel(msg.getData().getInt("Callback"), "");
			}
			else if (msg.getData().getInt("Callback") == 21)
			{
				int x = msg.getData().getInt("x");
				int y = msg.getData().getInt("y");
				// ??? // ButtonCallback(1, 1, x, y); // down
			}
			else if (msg.getData().getInt("Callback") == 22)
			{
				int x = msg.getData().getInt("x");
				int y = msg.getData().getInt("y");
				// ??? // ButtonCallback(0, 1, x, y); // up
			}
			else if (msg.getData().getInt("Callback") == 23)
			{
				int x = msg.getData().getInt("x");
				int y = msg.getData().getInt("y");
				int x2 = msg.getData().getInt("x2");
				int y2 = msg.getData().getInt("y2");
				NavitGraphics.MotionCallback(x, y, x2, y2);
			}
			else if (msg.getData().getInt("Callback") == 24)
			{
				try
				{
					NavitGraphics.NavitMsgTv_.setEnabled(true);
					NavitGraphics.NavitMsgTv_.setVisibility(View.VISIBLE);
				}
				catch (Exception e)
				{

				}
			}
			else if (msg.getData().getInt("Callback") == 25)
			{
				try
				{
					NavitGraphics.NavitMsgTv_.setVisibility(View.INVISIBLE);
					NavitGraphics.NavitMsgTv_.setEnabled(false);
					NavitGraphics.NavitMsgTv_.setVisibility(View.GONE);
				}
				catch (Exception e)
				{

				}
			}
			else if (msg.getData().getInt("Callback") == 30)
			{
				// 2D
				// String s = msg.getData().getString("s");
				CallbackMessageChannel(30, "");
			}
			else if (msg.getData().getInt("Callback") == 31)
			{
				// 3D
				// String s = msg.getData().getString("s");
				CallbackMessageChannel(31, "");
			}
			else if (msg.getData().getInt("Callback") == 32)
			{
				// switch to specific 3D pitch
				String s = msg.getData().getString("s");
				CallbackMessageChannel(32, s);
			}
			else if (msg.getData().getInt("Callback") == 33)
			{
				// zoom to specific zoomlevel
				String s = msg.getData().getString("s");
				CallbackMessageChannel(33, s);
			}
			else if (msg.getData().getInt("Callback") == 34)
			{
				// announcer voice OFF
				CallbackMessageChannel(34, "");
			}
			else if (msg.getData().getInt("Callback") == 35)
			{
				// announcer voice ON
				CallbackMessageChannel(35, "");
			}
			else if (msg.getData().getInt("Callback") == 36)
			{
				// switch "Lock on road" ON
				CallbackMessageChannel(36, "");
			}
			else if (msg.getData().getInt("Callback") == 37)
			{
				// switch "Lock on road" OFF
				CallbackMessageChannel(37, "");
			}
			else if (msg.getData().getInt("Callback") == 38)
			{
				// switch "Northing" ON
				CallbackMessageChannel(38, "");
			}
			else if (msg.getData().getInt("Callback") == 39)
			{
				// switch "Northing" OFF
				CallbackMessageChannel(39, "");
			}
			else if (msg.getData().getInt("Callback") == 40)
			{
				// switch "Map follows Vehicle" ON
				CallbackMessageChannel(40, "");
			}
			else if (msg.getData().getInt("Callback") == 41)
			{
				// switch "Map follows Vehicle" OFF
				CallbackMessageChannel(41, "");
			}
			else if (msg.getData().getInt("Callback") == 42)
			{
				// routing mode "highways"
				CallbackMessageChannel(42, "");
			}
			else if (msg.getData().getInt("Callback") == 43)
			{
				// routing mode "normal roads"
				CallbackMessageChannel(43, "");
			}
			else if (msg.getData().getInt("Callback") == 44)
			{
				// show duplicates in search results
				CallbackMessageChannel(44, "");
			}
			else if (msg.getData().getInt("Callback") == 45)
			{
				// filter duplicates in search results
				CallbackMessageChannel(45, "");
			}
			else if (msg.getData().getInt("Callback") == 46)
			{
				// stop searching and show results found until now
				CallbackMessageChannel(46, "");
			}
			else if (msg.getData().getInt("Callback") == 47)
			{
				// change maps data dir
				String s = msg.getData().getString("s");
				CallbackMessageChannel(47, s);
			}
			else if (msg.getData().getInt("Callback") == 50)
			{
				// we request to stop drawing the map
				CallbackMessageChannel(50, "");
			}
			else if (msg.getData().getInt("Callback") == 51)
			{
				// set position to pixel x,y
				int x = msg.getData().getInt("x");
				int y = msg.getData().getInt("y");
				CallbackMessageChannel(51, "" + x + "#" + y);
			}
			else if (msg.getData().getInt("Callback") == 52)
			{
				// switch to demo vehicle
				String s = msg.getData().getString("s");
				CallbackMessageChannel(52, s);
			}
			else if (msg.getData().getInt("Callback") == 53)
			{
				// dont speak streetnames
				CallbackMessageChannel(53, "");
			}
			else if (msg.getData().getInt("Callback") == 54)
			{
				// speak streetnames
				CallbackMessageChannel(54, "");
			}
			else if (msg.getData().getInt("Callback") == 55)
			{
				// set cache size for (map-)files
				String s = msg.getData().getString("s");
				CallbackMessageChannel(55, s);
			}
			//			else if (msg.getData().getInt("Callback") == 56)
			//			{
			//				// draw polylines with/without circles at the end
			//				String s = msg.getData().getString("s");
			//				NavitGraphics.CallbackMessageChannel(56, s); // 0 -> draw circles, 1 -> DO NOT draw circles
			//			}
			else if (msg.getData().getInt("Callback") == 57)
			{
				// keep drawing streets as if at "order" level xxx
				String s = msg.getData().getString("s");
				CallbackMessageChannel(57, s);
			}
			else if (msg.getData().getInt("Callback") == 58)
			{
				// street search radius factor (multiplier)
				String s = msg.getData().getString("s");
				CallbackMessageChannel(58, s);
			}
			else if (msg.getData().getInt("Callback") == 59)
			{
				// enable layer "name"
				String s = msg.getData().getString("s");
				CallbackMessageChannel(59, s);
			}
			else if (msg.getData().getInt("Callback") == 60)
			{
				// disable layer "name"
				String s = msg.getData().getString("s");
				CallbackMessageChannel(60, s);
			}
			else if (msg.getData().getInt("Callback") == 61)
			{
				// zoom to specific zoomlevel at given point as center
				// pixel-x#pixel-y#zoom-level
				String s = msg.getData().getString("s");
				CallbackMessageChannel(61, s);
			}
			else if (msg.getData().getInt("Callback") == 62)
			{
				// disable map drawing
				CallbackMessageChannel(62, "");
			}
			else if (msg.getData().getInt("Callback") == 63)
			{
				// enable map drawing
				CallbackMessageChannel(63, "");
			}
			else if (msg.getData().getInt("Callback") == 64)
			{
				// draw map
				CallbackMessageChannel(64, "");
			}
			else if (msg.getData().getInt("Callback") == 65)
			{
				// draw map async
				CallbackMessageChannel(65, "");
			}
			else if (msg.getData().getInt("Callback") == 66)
			{
				// enable "multipolygons"
				CallbackMessageChannel(66, "");
			}
			else if (msg.getData().getInt("Callback") == 67)
			{
				// disable "multipolygons"
				CallbackMessageChannel(67, "");
			}
			else if (msg.getData().getInt("Callback") == 68)
			{
				// shift "order" by this value (only for drawing objects)
				String s = msg.getData().getString("s");
				CallbackMessageChannel(68, s);
			}
			else if (msg.getData().getInt("Callback") == 69)
			{
				// stop drawing map
				CallbackMessageChannel(69, "");
			}
			else if (msg.getData().getInt("Callback") == 70)
			{
				// allow drawing map
				CallbackMessageChannel(70, "");
			}
			else if (msg.getData().getInt("Callback") == 71)
			{
				// activate/deactivate "route graph" display
				// 0 -> deactivate
				// 1 -> activate
				String s = msg.getData().getString("s");
				CallbackMessageChannel(71, s);
			}
			else if (msg.getData().getInt("Callback") == 72)
			{
				// update the route path and route graph (e.g. after setting new roadblocks)
				// does not update destinations!!!
				CallbackMessageChannel(72, "");
			}
			else if (msg.getData().getInt("Callback") == 73)
			{
				// update the route path and route graph (e.g. after setting new roadblocks)
				// this destroys the route graph and calcs everything totally new!
				CallbackMessageChannel(73, "");
			}

			else if (msg.getData().getInt("Callback") == 74)
			{
				// allow demo vechile to move
				CallbackMessageChannel(74, "");
			}
			else if (msg.getData().getInt("Callback") == 75)
			{
				// stop demo vechile
				CallbackMessageChannel(75, "");
			}
			else if (msg.getData().getInt("Callback") == 76)
			{
				// show route rectangles
				CallbackMessageChannel(76, "");
			}
			else if (msg.getData().getInt("Callback") == 77)
			{
				// do not show route rectangles
				CallbackMessageChannel(77, "");
			}
			else if (msg.getData().getInt("Callback") == 78)
			{
				// shift layout "order" values
				String s = msg.getData().getString("s");
				CallbackMessageChannel(78, s);
			}
			else if (msg.getData().getInt("Callback") == 79)
			{
				// set traffic light delay/cost
				String s = msg.getData().getString("s");
				CallbackMessageChannel(79, s);
			}
			else if (msg.getData().getInt("Callback") == 80)
			{
				// set autozoom flag to 0 or 1
				String s = msg.getData().getString("s");
				CallbackMessageChannel(80, s);
			}
			else if (msg.getData().getInt("Callback") == 81)
			{
				// resize layout items by factor
				String s = msg.getData().getString("s");
				CallbackMessageChannel(81, s);
			}
			else if (msg.getData().getInt("Callback") == 82)
			{
				// report share dir
				String s = msg.getData().getString("s");
				CallbackMessageChannel(82, s);
			}
			else if (msg.getData().getInt("Callback") == 83)
			{
				// spill all the index files to log output
				CallbackMessageChannel(83, "");
			}
			else if (msg.getData().getInt("Callback") == 84)
			{
				// report data dir
				String s = msg.getData().getString("s");
				CallbackMessageChannel(84, s);
			}
			else if (msg.getData().getInt("Callback") == 85)
			{
				// C linedrawing flag
				String s = msg.getData().getString("s");
				CallbackMessageChannel(85, s);
			}
			else if (msg.getData().getInt("Callback") == 86)
			{
				// avoid sharp turns flag to 0 or 1
				String s = msg.getData().getString("s");
				CallbackMessageChannel(86, s);
			}
			else if (msg.getData().getInt("Callback") == 87)
			{
				// // avoid sharp turns minimum angle. if turn is harder than this angle then set penalty
				String s = msg.getData().getString("s");
				CallbackMessageChannel(87, s);
			}
			else if (msg.getData().getInt("Callback") == 88)
			{
				// avoid sharp turns penalty
				String s = msg.getData().getString("s");
				CallbackMessageChannel(88, s);
			}
			else if (msg.getData().getInt("Callback") == 89)
			{
				// search radius for housenumbers for streets
				String s = msg.getData().getString("s");
				CallbackMessageChannel(89, s);
			}
			else if (msg.getData().getInt("Callback") == 90)
			{
				// set vehicleprofile to value of string s ('car','bike')
				String s = msg.getData().getString("s");
				CallbackMessageChannel(90, s);
			}
			else if (msg.getData().getInt("Callback") == 91)
			{
				// change vehicle profile's roadprofile values
				String s = msg.getData().getString("s");
				CallbackMessageChannel(91, s);
			}
			else if (msg.getData().getInt("Callback") == 92)
			{
				// change vehicle profile's roadprofile values 2
				String s = msg.getData().getString("s");
				CallbackMessageChannel(92, s);
			}
			else if (msg.getData().getInt("Callback") == 93)
			{
				// change vehicle profile's roadprofile values 3
				String s = msg.getData().getString("s");
				CallbackMessageChannel(93, s);
			}
			else if (msg.getData().getInt("Callback") == 94)
			{
				// change priority for cycle lanes
				String s = msg.getData().getString("s");
				CallbackMessageChannel(94, s);
			}
			//else if (msg.getData().getInt("Callback") == 95)
			//{
			//	// change priority for cycle tracks
			//	String s = msg.getData().getString("s");
			//	NavitGraphics.CallbackMessageChannel(95, s);
			//}
			else if (msg.getData().getInt("Callback") == 96)
			{
				// dump route to GPX file, "s" -> full pathname to output file
				String s = msg.getData().getString("s");
				CallbackMessageChannel(96, s);
			}
			else if (msg.getData().getInt("Callback") == 97)
			{
				// set positon to lat#lon#name
				String lat = msg.getData().getString("lat");
				String lon = msg.getData().getString("lon");
				String q = msg.getData().getString("q");
				CallbackMessageChannel(97, lat + "#" + lon + "#" + q);
			}
			else if (msg.getData().getInt("Callback") == 98)
			{
				// set connected_pref value
				String s = msg.getData().getString("s");
				CallbackMessageChannel(98, s);
			}
			else if (msg.getData().getInt("Callback") == 99)
			{
				// set angle_pref value
				String s = msg.getData().getString("s");
				CallbackMessageChannel(99, s);
			}
			else if (msg.getData().getInt("Callback") == 100)
			{
				// dump callbacks to log
				CallbackMessageChannel(100, "");
			}
			else if (msg.getData().getInt("Callback") == 101)
			{
				// set demo vehicle flag for tracking
				CallbackMessageChannel(101, "");
			}
			else if (msg.getData().getInt("Callback") == 102)
			{
				// set gpsfix flag
				String s = msg.getData().getString("s");
				CallbackMessageChannel(102, s);
			}
			else if (msg.getData().getInt("Callback") == 103)
			{
				// draw location of self (car) x% lower than screen center
				String s = msg.getData().getString("s");
				CallbackMessageChannel(103, s);
			}
			else if (msg.getData().getInt("Callback") == 104)
			{
				// send OVERSPILL_FACTOR to C-code
				String s;
				if (Navit.GFX_OVERSPILL)
				{
					s = "" + (OVERSPILL_FACTOR * 100);
				}
				else
				{
					s = "" + 100;
				}

				CallbackMessageChannel(104, s);
			}
			else if (msg.getData().getInt("Callback") == 105)
			{
				// zoom to specific zoomlevel without redrawing the map!
				String s = msg.getData().getString("s");
				CallbackMessageChannel(105, s);
			}
			else if (msg.getData().getInt("Callback") == 106)
			{
				// factor for routing/road speed
				String s = msg.getData().getString("s");
				CallbackMessageChannel(106, s);
			}
			else if (msg.getData().getInt("Callback") == 107)
			{
				// level 0 announcement seconds
				String s = msg.getData().getString("s");
				CallbackMessageChannel(107, s);
			}
			else if (msg.getData().getInt("Callback") == 108)
			{
				// level 1 announcement seconds
				String s = msg.getData().getString("s");
				CallbackMessageChannel(108, s);
			}
			else if (msg.getData().getInt("Callback") == 109)
			{
				// level 2 announcement seconds
				String s = msg.getData().getString("s");
				CallbackMessageChannel(109, s);
			}
			else if (msg.getData().getInt("Callback") == 110)
			{
				// generic int option CallBack [<option name>:<option value "int">]
				String s = msg.getData().getString("s");
				CallbackMessageChannel(110, s);
			}
			else if (msg.getData().getInt("Callback") == 111)
			{
				// show real gps position on map
				String s = msg.getData().getString("s");
				CallbackMessageChannel(111, s);
			}
			else if (msg.getData().getInt("Callback") == 112)
			{
				// show maps debug view
				String s = msg.getData().getString("s");
				CallbackMessageChannel(112, s);
			}
			else if (msg.getData().getInt("Callback") == 113)
			{
				// cancel preview map drawing
				CallbackMessageChannel(113, "x");
			}
			else if (msg.getData().getInt("Callback") == 114)
			{
				// set night mode 0|1
				String s = msg.getData().getString("s");
				CallbackMessageChannel(114, s);
			}
			else if (msg.getData().getInt("Callback") == 115)
			{
				// set debug test number
				String s = msg.getData().getString("s");
				CallbackMessageChannel(115, s);
			}
			else if (msg.getData().getInt("Callback") == 9901)
			{
				// if follow mode is on, then dont show freeview streetname
				//if (!Navit.PREF_follow_gps)
				//{
				//	Navit.cwthr.CallbackGeoCalc2(1, 0, mCanvasWidth / 2, mCanvasHeight / 2);
				//}
			}
			else if (msg.getData().getInt("Callback") == 98001)
			{
				int id = msg.getData().getInt("id");
				int i = msg.getData().getInt("i");
				NavitGraphics.return_generic_int_real(id, i);
			}
			else if (msg.getData().getInt("Callback") == 9001)
			{
				NavitGraphics.busyspinner_.setVisibility(View.INVISIBLE);
				NavitGraphics.busyspinnertext_.setVisibility(View.INVISIBLE);
			}
			else if (msg.getData().getInt("Callback") == 9002)
			{
				NavitGraphics.busyspinner_.setVisibility(View.VISIBLE);
				NavitGraphics.busyspinnertext_.setVisibility(View.VISIBLE);
			}

			// if (Navit.METHOD_DEBUG) Navit.my_func_name(1);
		}
	};

	private final Handler progress_handler = new Handler()
	{
		@SuppressLint("NewApi")
		public void handleMessage(Message msg)
		{

			// System.out.println("progress_handler:msg:" + msg.what + "::" + msg.getData().toString());

			switch (msg.what)
			{
			case 0:
				// dismiss dialog, remove dialog
				try
				{
					Log.e(TAG, "0: dismiss dialog num " + msg.getData().getInt("dialog_num"));
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}

				try
				{
					dismissDialog(msg.getData().getInt("dialog_num"));
					removeDialog(msg.getData().getInt("dialog_num"));
				}
				catch (Exception e)
				{
				}


				// ----- service stop -----
				getContext().stopService(Navit.ZANaviMapDownloaderServiceIntent);
				String this_map_name = "map";
				try
				{
					this_map_name = msg.getData().getString("map_name");
				}
				catch (Exception e)
				{
				}

				String statusString;
				// exit_code=0 -> OK, map was downloaded fine
				if (msg.getData().getInt("exit_code") == 0)
				{
					statusString = "ready";
				}
				else
				{
					statusString = "ERROR";
				}
					// reload sdcard maps
					sendCallBackMessage(18);
					final int NOTIFICATION_ID = 0;

					NotificationCompat.Builder builder;
					builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
							.setSmallIcon(R.drawable.icon) //displays only a grey square -- jdg --
							.setContentTitle("ZANavi")
							.setContentText(this_map_name + ":" + Navit.get_text(statusString))
							.setOngoing(true)
							.setChannelId(CHANNEL_ID);
					builder.setAutoCancel(true);
					Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.icon);
					builder.setLargeIcon(bm);

					Intent in = new Intent();
					in.setClass(getContext(), Navit.class);
					in.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
					PendingIntent p_activity = PendingIntent.getActivity(getContext(), 0, in, PendingIntent.FLAG_UPDATE_CURRENT);
					builder.setContentIntent(p_activity);

					Notification notification = builder.build();
					NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
					notificationManager.notify(NOTIFICATION_ID, notification);
					if (msg.getData().getInt("exit_code") == 0)
					{
						zoom_out_full();
					}
				break;
			case 1:
				// change progressbar values
				try
				{
					int what_dialog = msg.getData().getInt("dialog_num");
					if (what_dialog == MAPDOWNLOAD_PRI_DIALOG)
					{
						mapdownloader_dialog_pri.setMax(msg.getData().getInt("max"));
						mapdownloader_dialog_pri.setProgress(msg.getData().getInt("cur"));
						mapdownloader_dialog_pri.setTitle(msg.getData().getString("title"));
						mapdownloader_dialog_pri.setMessage(msg.getData().getString("text"));
					}
					else if (what_dialog == MAPDOWNLOAD_SEC_DIALOG)
					{
						mapdownloader_dialog_sec.setMax(msg.getData().getInt("max"));
						mapdownloader_dialog_sec.setProgress(msg.getData().getInt("cur"));
						mapdownloader_dialog_sec.setTitle(msg.getData().getString("title"));
						mapdownloader_dialog_sec.setMessage(msg.getData().getString("text"));
					}
				}
				catch (Exception e)
				{
				}
				break;
			case 2:
				Toast.makeText(getApplicationContext(), msg.getData().getString("text"), Toast.LENGTH_SHORT).show();
				break;
			case 3:
				Toast.makeText(getApplicationContext(), msg.getData().getString("text"), Toast.LENGTH_LONG).show();
				break;
			case 10:
				// change values - generic
				try
				{
					int what_dialog_generic = msg.getData().getInt("dialog_num");
					if (what_dialog_generic == SEARCHRESULTS_WAIT_DIALOG)
					{
						search_results_wait.setMax(msg.getData().getInt("max"));
						search_results_wait.setProgress(msg.getData().getInt("cur"));
						search_results_wait.setTitle(msg.getData().getString("title"));
						search_results_wait.setMessage(msg.getData().getString("text"));
					}
					else if (what_dialog_generic == SEARCHRESULTS_WAIT_DIALOG_OFFLINE)
					{
						search_results_wait_offline.setMax(msg.getData().getInt("max"));
						search_results_wait_offline.setProgress(msg.getData().getInt("cur"));
						search_results_wait_offline.setTitle(msg.getData().getString("title"));
						search_results_wait_offline.setMessage(msg.getData().getString("text"));
					}
				}
				catch (Exception e)
				{
				}
				break;
			case 11:
				// show dialog - generic
				try
				{
					// just in case, remove the dialog if it should be shown already!
					dismissDialog(msg.getData().getInt("dialog_num"));
					removeDialog(msg.getData().getInt("dialog_num"));
				}
				catch (Exception e)
				{
					// System.out.println("Ex D1: " + e.toString());
				}
				System.out.println("showDialog 001 dialog_num=" + msg.getData().getInt("dialog_num"));
				showDialog(msg.getData().getInt("dialog_num"));
				break;
			case 12:
				// turn on compass
				turn_on_compass();
				break;
			case 13:
				// turn off compass
				turn_off_compass();
				break;
			case 14:
				// set used mem in textview
				show_mem_used_real();
				break;
			case 15:
				// set debug text line 3
				Navit.set_debug_messages3(msg.getData().getString("text"));
				break;
			case 16:
				// refresh NavitAndriodOverlay
				try
				{
					//Log.e("NavitGraphics", "xx 1");
					//System.out.println("invalidate 027");
					NavitGraphics.NavitAOverlay_s.invalidate();
					//Log.e("NavitGraphics", "xx 2");
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
				break;
			case 17:
				try
				{

					generic_alert_box.setMessage(Navit.get_text("Possibly not enough space on your device!")).setPositiveButton(Navit.get_text("Ok"), new DialogInterface.OnClickListener() // TRANS
							{
								public void onClick(DialogInterface dialog, int id)
								{
									// Handle Ok
								}
							}).create();
					generic_alert_box.setCancelable(false);
					generic_alert_box.setTitle(Navit.get_text("device space")); // TRANS
					generic_alert_box.show();
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
				break;
			//			case 18:
			//				try
			//				{
			//					openOptionsMenu_wrapper();
			//				}
			//				catch (Exception e)
			//				{
			//				}
			//				break;
			case 19:
				open_voice_recog_screen();
				break;
			case 20:
				dim_screen();
				break;
			case 21:
				default_brightness_screen();
				break;
			case 22:
				try
				{
					// ----- service start -----
					// ----- service start -----
					startService(Navit.ZANaviMapDownloaderServiceIntent);
					// ----- service start -----
					// ----- service start -----

					//					try
					//					{
					//						Thread.sleep(200);
					//					}
					//					catch (InterruptedException e)
					//					{
					//					}

					//					if (!ZANaviMapDownloaderService.service_running)
					//					{
					//						System.out.println("ZANaviMapDownloaderService -> not running yet ...");
					//						try
					//						{
					//							Thread.sleep(2000);
					//						}
					//						catch (InterruptedException e)
					//						{
					//						}
					//					}
					//
					//					if (!ZANaviMapDownloaderService.service_running)
					//					{
					//						System.out.println("ZANaviMapDownloaderService -> not running yet ...");
					//						try
					//						{
					//							Thread.sleep(2000);
					//						}
					//						catch (InterruptedException e)
					//						{
					//						}
					//					}

					// -------- // ZANaviMapDownloaderService.start_map_download();
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
				break;
			case 23:

				// show actionbar download icon
				try
				{
					// show download actionbar icon
					//cur_menu.findItem(R.id.item_download_menu_button).setTitle("");
					actionabar_download_icon_visible = true;
					cur_menu.findItem(R.id.item_download_menu_button).setVisible(true);
					//cur_menu.findItem(R.id.item_download_menu_button).setEnabled(true);
					// ****** // cur_menu.findItem(R.id.item_download_menu_button).setIcon(R.drawable.anim_download_icon);
					// cur_menu.findItem(R.id.item_download_menu_button).setIcon((Drawable) null);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}

				try
				{
					show_status_bar();
					getSupportActionBar().setDisplayShowTitleEnabled(true);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}

				//				try
				//				{
				//					View menuItemView = findViewById(R.id.item_download_menu_button);
				//					menuItemView.setBackgroundResource(R.drawable.anim_download_icon_2);
				//					//					menuItemView.setOnClickListener(new View.OnClickListener()
				//					//					{
				//					//						public void onClick(View v)
				//					//						{
				//					//							try
				//					//							{
				//					//								//menuItemView.setBackgroundResource(R.drawable.anim_download_icon_1);
				//					//								//AnimationDrawable frameAnimation = (AnimationDrawable) menuItemView.getBackground();
				//					//								//frameAnimation.start();
				//					//								// menuItemView.setAlpha(100);
				//					//								View menuItemView = findViewById(R.id.item_download_menu_button);
				//					//								menuItemView.setBackgroundResource(R.drawable.anim_download_icon_1);
				//					//
				//					//								System.out.println("download icon pressed(2)");
				//					//
				//					//								Intent mapdownload_cancel_activity = new Intent(Navit.sBaseContext, ZANaviDownloadMapCancelActivity.class);
				//					//								mapdownload_cancel_activity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
				//					//								startActivity(mapdownload_cancel_activity);
				//					//
				//					//								new Handler().postDelayed(new Runnable()
				//					//								{
				//					//									@Override
				//					//									public void run()
				//					//									{
				//					//										if (Navit.cur_menu.findItem(R.id.item_download_menu_button).isVisible())
				//					//										{
				//					//											View menuItemView = findViewById(R.id.item_download_menu_button);
				//					//											menuItemView.setBackgroundResource(R.drawable.anim_download_icon_2);
				//					//											AnimationDrawable frameAnimation = (AnimationDrawable) menuItemView.getBackground();
				//					//											frameAnimation.start();
				//					//										}
				//					//									}
				//					//								}, 50);
				//					//							}
				//					//							catch (Exception e)
				//					//							{
				//					//							}
				//					//						}
				//					//					});
				//					AnimationDrawable frameAnimation = (AnimationDrawable) menuItemView.getBackground();
				//					frameAnimation.start();
				//				}
				//				catch (Exception e)
				//				{
				//					e.printStackTrace();
				//				}

				break;
			case 24:
				// hide actionbar download icon

				//				try
				//				{
				//					View menuItemView = findViewById(R.id.item_download_menu_button);
				//					menuItemView.setBackground((Drawable) null);
				//
				//					new Handler().postDelayed(new Runnable()
				//					{
				//						@Override
				//						public void run()
				//						{
				//							if (Navit.cur_menu.findItem(R.id.item_download_menu_button).isVisible())
				//							{
				//								View menuItemView = findViewById(R.id.item_download_menu_button);
				//								menuItemView.setBackground((Drawable) null);
				//							}
				//						}
				//					}, 50);
				//				}
				//				catch (Exception e)
				//				{
				//					e.printStackTrace();
				//				}

				try
				{
					// hide download actionbar icon
					actionabar_download_icon_visible = false;
					cur_menu.findItem(R.id.item_download_menu_button).setVisible(false);
					//cur_menu.findItem(R.id.item_download_menu_button).setEnabled(false);
					// cur_menu.findItem(R.id.item_download_menu_button).setIcon((Drawable) null);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}

				try
				{
					getSupportActionBar().setDisplayShowTitleEnabled(false);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}

				try
				{
					hide_status_bar();
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}

				break;
			case 25:
				// Log.e(TAG, "nav: 25");
				NavitGraphics.deactivate_nav_wakelock_real();
				break;
			case 26:
				// Log.e(TAG, "nav: 26");
				NavitGraphics.activate_nav_wakelock_real();
				break;
			case 27:
				show_status_bar();
				break;
			case 28:
				hide_status_bar();
				break;
			case 29:
				invalidateOptionsMenu();
				break;
			case 30:
				try
				{
					NG__map_main.mNavitMsgTv2.append(msg.getData().getString("text"));
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
				break;
			case 31:
				try
				{
					// map download menu
					Intent map_download_list_activity = new Intent(getContext(), NavitDownloadSelectMapActivity.class);
					startActivityForResult(map_download_list_activity, Navit.NavitDownloaderPriSelectMap_id);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
				break;
			case 32:
				try
				{
					if (preferences.PREF_follow_gps)
					{
						push_pin_view.setImageResource(R.drawable.pin1_down);
					}
					else
					{
						push_pin_view.setImageResource(R.drawable.pin1_up);
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
				break;
			case 33:
				if (road_book != null)
				{
					if (road_book_items != null)
					{
						try
						{
							// send new roadbook items to fragment and reload it
							road_book.reload_items(road_book_items);
						}
						catch (Exception e)
						{
						}
					}
				}
				break;
			case 34:
				// show bubble
				NavitGraphics.whats_here_container_wrap.setVisibility(View.VISIBLE);
				NG__map_main.whats_here_text.setText("  " + Navit.get_text("loading ...") + "  ");
				break;
			case 35:
				// hide bubble
				NavitGraphics.whats_here_container_wrap.setVisibility(View.INVISIBLE);
				NG__map_main.whats_here_text.setText("  " + Navit.get_text("loading ...") + "  ");
				// and move out of screen

				if (!Navit.PAINT_OLD_API)
				{
					NavitGraphics.whats_here_container_wrap.setX(-2000);
					NavitGraphics.whats_here_container_wrap.setY(-2000);
					// NavitGraphics.whats_here_container_wrap.requestLayout();

					NG__map_main.whats_here_pointer_image.setX(0);
				}
				else
				{
					NavitGraphics.params_whats_here_container_wrap = (android.widget.RelativeLayout.LayoutParams) NavitGraphics.whats_here_container_wrap.getLayoutParams();
					NavitGraphics.params_whats_here_container_wrap.leftMargin = -2000;
					NavitGraphics.params_whats_here_container_wrap.topMargin = -2000;
					NavitGraphics.whats_here_container_wrap.setLayoutParams(NavitGraphics.params_whats_here_container_wrap);
					NavitGraphics.whats_here_container_wrap.requestLayout();

					NavitGraphics.params_whats_here_pointer_image = (RelativeLayout.LayoutParams) NG__map_main.whats_here_pointer_image.getLayoutParams();
					// NavitGraphics.params_whats_here_pointer_image.leftMargin = 0;
					NG__map_main.whats_here_pointer_image.setLayoutParams(NavitGraphics.params_whats_here_pointer_image);
					NG__map_main.whats_here_pointer_image.requestLayout();

				}
				break;
			case 36:
				// move bubble to x,y
				int width_in_px = getResources().getDimensionPixelSize(R.dimen.whats_here_container_width);
				int new_x = NavitGraphics.NavitAOverlay_s.bubble_001.x;
				int new_diff = 0;
				if ((NavitGraphics.NavitAOverlay_s.bubble_001.x + width_in_px) > NavitGraphics.mCanvasWidth)
				{
					new_diff = (NavitGraphics.NavitAOverlay_s.bubble_001.x + width_in_px) - NavitGraphics.mCanvasWidth;
					new_x = NavitGraphics.NavitAOverlay_s.bubble_001.x - ((NavitGraphics.NavitAOverlay_s.bubble_001.x + width_in_px) - NavitGraphics.mCanvasWidth);
				}

				new_x = new_x - NavitGraphics.dp_to_px(17);

				if (!Navit.PAINT_OLD_API)
				{
					NavitGraphics.whats_here_container_wrap.setX(new_x);
					NavitGraphics.whats_here_container_wrap.setY(NavitGraphics.NavitAOverlay_s.bubble_001.y + NavitGraphics.dp_to_px(17));
					// NavitGraphics.whats_here_container_wrap.requestLayout();

					NG__map_main.whats_here_pointer_image.setX(new_diff);
				}
				else
				{
					NavitGraphics.params_whats_here_container_wrap = (android.widget.RelativeLayout.LayoutParams) NavitGraphics.whats_here_container_wrap.getLayoutParams();
					NavitGraphics.params_whats_here_container_wrap.leftMargin = new_x;
					NavitGraphics.params_whats_here_container_wrap.topMargin = NavitGraphics.NavitAOverlay_s.bubble_001.y - NavitGraphics.whats_here_container_wrap.getHeight() + NavitGraphics.dp_to_px(17);
					NavitGraphics.whats_here_container_wrap.setLayoutParams(NavitGraphics.params_whats_here_container_wrap);
					NavitGraphics.whats_here_container_wrap.requestLayout();

					NavitGraphics.params_whats_here_pointer_image = (RelativeLayout.LayoutParams) NG__map_main.whats_here_pointer_image.getLayoutParams();
					// has some bugs // NavitGraphics.params_whats_here_pointer_image.leftMargin = (NavitGraphics.NavitAOverlay_s.bubble_001.x + width_in_px) - NavitGraphics.mCanvasWidth;
					NG__map_main.whats_here_pointer_image.setLayoutParams(NavitGraphics.params_whats_here_pointer_image);
					NG__map_main.whats_here_pointer_image.requestLayout();
					NG__map_main.whats_here_pointer_image.setVisibility(View.INVISIBLE);
				}
				break;
			case 37:
				// set text for point on screen
				String dest_name = "Point on Screen";

				try
				{
					if (Navit.GFX_OVERSPILL)
					{
						dest_name = NavitGraphics.CallbackGeoCalc(8, (NavitGraphics.NavitAOverlay_s.bubble_001.x + NavitGraphics.mCanvasWidth_overspill) * NavitGraphics.Global_dpi_factor, (NavitGraphics.NavitAOverlay_s.bubble_001.y + NavitGraphics.mCanvasHeight_overspill) * NavitGraphics.Global_dpi_factor);
					}
					else
					{
						dest_name = NavitGraphics.CallbackGeoCalc(8, NavitGraphics.NavitAOverlay_s.bubble_001.x * NavitGraphics.Global_dpi_factor, NavitGraphics.NavitAOverlay_s.bubble_001.y * NavitGraphics.Global_dpi_factor);
					}

					if ((dest_name.equals(" ")) || (dest_name == null))
					{
						dest_name = "Point on Screen";
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}

				NG__map_main.whats_here_text.setText(dest_name);
				break;
			case 38:
				try
				{
					NavitAddressSearchActivity.adapter.clear();
					NavitAddressSearchActivity.adapter.notifyDataSetChanged();
					// NavitAddressSearchActivity.listview.postInvalidate();
				}
				catch (Exception e)
				{
					System.out.println("AAEE:001");
				}
				break;
			case 39:
				try
				{
					NavitAddressSearchActivity.adapter.notifyDataSetChanged();
				}
				catch (Exception e)
				{
					System.out.println("AAEE:008");
				}
				break;
			case 40:
				// set main progress bar
				try
				{
					// update main progress bar
					progressbar_main_activity.setIndeterminate(false);
					if (progressbar_main_activity.getVisibility() != View.VISIBLE)
					{
						progressbar_main_activity.setProgress(0);
						progressbar_main_activity.setVisibility(View.VISIBLE);
					}

					if (msg.getData().getInt("pg") < 1)
					{
						progressbar_main_activity.setProgress(1); // always set % to at least 1, that user can see that its a progressbar
					}
					else
					{
						progressbar_main_activity.setProgress(msg.getData().getInt("pg"));
					}
				}
				catch (Exception e)
				{
					System.out.println("AAEE:011");
				}
				break;
			case 41:
				// hide main progress bar
				try
				{
					if (progressbar_main_activity.getVisibility() == View.VISIBLE)
					{
						progressbar_main_activity.setProgress(0);
						progressbar_main_activity.setIndeterminate(true);
						progressbar_main_activity.setVisibility(View.GONE);
					}
				}
				catch (Exception e)
				{
					System.out.println("AAEE:012");
				}
				break;
			case 42:
				// show main progress bar, and set indeterminate
				try
				{
					// update main progress bar
					if (progressbar_main_activity.getVisibility() != View.VISIBLE)
					{
						progressbar_main_activity.setProgress(0);
						progressbar_main_activity.setVisibility(View.VISIBLE);
					}
					progressbar_main_activity.setIndeterminate(true);
				}
				catch (Exception e)
				{
					System.out.println("AAEE:013");
				}
				break;

			case 99:
				// dismiss dialog, remove dialog - generic
				try
				{
					Log.e(TAG, "99: dismiss dialog num " + msg.getData().getInt("dialog_num"));
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
				try
				{
					dismissDialog(msg.getData().getInt("dialog_num"));
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
				try
				{
					removeDialog(msg.getData().getInt("dialog_num"));
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
				break;

			}
		}
	};

	@TargetApi(Build.VERSION_CODES.FROYO)
	protected Dialog onCreateDialog(int id)
	{

		System.out.println("onCreateDialog id=" + id);

		switch (id)
		{
		// ==============---------- real search offline (old style) here ----------==============
		// ==============---------- real search offline (old style) here ----------==============
		// ==============---------- real search offline (old style) here ----------==============
		case Navit.SEARCHRESULTS_WAIT_DIALOG_OFFLINE:
			search_results_wait_offline = new ProgressDialog(this);
			search_results_wait_offline.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			search_results_wait_offline.setTitle("--");
			search_results_wait_offline.setMessage("--");
			search_results_wait_offline.setCancelable(true); // allow to stop search
			search_results_wait_offline.setProgress(0);
			search_results_wait_offline.setMax(10);

			search_results_wait_offline.setOnCancelListener(new OnCancelListener()
			{
				public void onCancel(DialogInterface dialog)
				{
					sendCallBackMessage(46);
					Log.e(TAG, "onCancel: search_results_wait offline");
				}
			});

			/*
			 * search_results_wait.setButton("stop", new DialogInterface.OnClickListener()
			 * {
			 * public void onClick(DialogInterface dialog, int which)
			 * {
			 * // Use either finish() or return() to either close the activity or just the dialog
			 * return;
			 * }
			 * });
			 */

			DialogInterface.OnDismissListener mOnDismissListener4 = new DialogInterface.OnDismissListener()
			{
				public void onDismiss(DialogInterface dialog)
				{
					Log.e(TAG, "onDismiss: search_results_wait offline");
					dialog.dismiss();
					dialog.cancel();
					searchresultsThread_offline.stop_me();
				}
			};
			search_results_wait_offline.setOnDismissListener(mOnDismissListener4);
			System.out.println("new SearchResultsThread 001");
			searchresultsThread_offline = new SearchResultsThread(progress_handler, Navit.SEARCHRESULTS_WAIT_DIALOG_OFFLINE);
			searchresultsThread_offline.start();

			NavitAddressSearchSpinnerActive = true;
			SearchResultsThreadSpinnerThread spinner_thread_offline = new SearchResultsThreadSpinnerThread(progress_handler, Navit.SEARCHRESULTS_WAIT_DIALOG_OFFLINE);
			spinner_thread_offline.start();

			return search_results_wait_offline;
		case Navit.SEARCHRESULTS_WAIT_DIALOG:
			search_results_wait = new ProgressDialog(this);
			search_results_wait.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			search_results_wait.setTitle("--");
			search_results_wait.setMessage("--");
			search_results_wait.setCancelable(false);
			search_results_wait.setProgress(0);
			search_results_wait.setMax(10);

			DialogInterface.OnDismissListener mOnDismissListener3 = new DialogInterface.OnDismissListener()
			{
				public void onDismiss(DialogInterface dialog)
				{
					Log.e(TAG, "onDismiss: search_results_wait");
					dialog.dismiss();
					dialog.cancel();
					searchresultsThread.stop_me();
				}
			};
			search_results_wait.setOnDismissListener(mOnDismissListener3);
			System.out.println("new SearchResultsThread 002");
			searchresultsThread = new SearchResultsThread(progress_handler, Navit.SEARCHRESULTS_WAIT_DIALOG);
			searchresultsThread.start();

			NavitAddressSearchSpinnerActive = true;
			SearchResultsThreadSpinnerThread spinner_thread = new SearchResultsThreadSpinnerThread(progress_handler, Navit.SEARCHRESULTS_WAIT_DIALOG);
			spinner_thread.start();

			return search_results_wait;
		case Navit.MAPDOWNLOAD_PRI_DIALOG:
			mapdownloader_dialog_pri = new ProgressDialog(this);
			mapdownloader_dialog_pri.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			mapdownloader_dialog_pri.setTitle("--");
			mapdownloader_dialog_pri.setMessage("--");
			mapdownloader_dialog_pri.setCancelable(false);
			mapdownloader_dialog_pri.setCanceledOnTouchOutside(false);
			mapdownloader_dialog_pri.setProgress(0);
			mapdownloader_dialog_pri.setMax(200);

			WindowManager.LayoutParams dialog_lparams = mapdownloader_dialog_pri.getWindow().getAttributes();
			dialog_lparams.screenBrightness = 0.1f;
			mapdownloader_dialog_pri.getWindow().setAttributes(dialog_lparams);

			DialogInterface.OnDismissListener mOnDismissListener1 = new DialogInterface.OnDismissListener()
			{
				public void onDismiss(DialogInterface dialog)
				{
					android.view.WindowManager.LayoutParams dialog_lparams = mapdownloader_dialog_pri.getWindow().getAttributes();
					mapdownloader_dialog_pri.getWindow().setAttributes(dialog_lparams);
					mapdownloader_dialog_pri.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
					mapdownloader_dialog_pri.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
					Log.e(TAG, "onDismiss: mapdownloader_dialog pri");
					dialog.dismiss();
					dialog.cancel();
					progressThread_pri.stop_thread();
				}
			};

			try
			{
				mapdownloader_dialog_pri.setButton(AlertDialog.BUTTON_NEGATIVE, Navit.get_text("Cancel"), new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						mapdownloader_dialog_pri.dismiss();
					}
				});
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}

			mapdownloader_dialog_pri.setOnDismissListener(mOnDismissListener1);
			mapdownloader_pri = new NavitMapDownloader(this);
			progressThread_pri = mapdownloader_pri.new ProgressThread(progress_handler, NavitMapDownloader.z_OSM_MAPS[Navit.download_map_id], MAP_NUM_PRIMARY);
			progressThread_pri.start();
			//
			// show license for OSM maps
			//. TRANSLATORS: please only translate the first word "Map data" and leave the other words in english
			Toast.makeText(getApplicationContext(), "Map data (c) OpenStreetMap contributors", Toast.LENGTH_SHORT).show(); //TRANS
			return mapdownloader_dialog_pri;
		case Navit.MAPDOWNLOAD_SEC_DIALOG:
			mapdownloader_dialog_sec = new ProgressDialog(this);
			mapdownloader_dialog_sec.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			mapdownloader_dialog_sec.setTitle("--");
			mapdownloader_dialog_sec.setMessage("--");

			mapdownloader_dialog_sec.setCancelable(true);
			mapdownloader_dialog_sec.setProgress(0);
			mapdownloader_dialog_sec.setMax(200);
			DialogInterface.OnDismissListener mOnDismissListener2 = new DialogInterface.OnDismissListener()
			{
				public void onDismiss(DialogInterface dialog)
				{
					Log.e(TAG, "onDismiss: mapdownloader_dialog sec");
					dialog.dismiss();
					dialog.cancel();
					progressThread_sec.stop_thread();
				}
			};
			mapdownloader_dialog_sec.setOnDismissListener(mOnDismissListener2);
			NavitMapDownloader mapdownloader_sec = new NavitMapDownloader(this);
			progressThread_sec = mapdownloader_sec.new ProgressThread(progress_handler, NavitMapDownloader.z_OSM_MAPS[Navit.download_map_id], MAP_NUM_SECONDARY);
			progressThread_sec.start();
			//
			// show license for OSM maps
			//. TRANSLATORS: please only translate the first word "Map data" and leave the other words in english
			Toast.makeText(getApplicationContext(), "Map data (c) OpenStreetMap contributors", Toast.LENGTH_SHORT).show(); //TRANS
			return mapdownloader_dialog_sec;
		}
		// should never get here!!
		return null;
	}

	void disableSuspend()
	{
		// wl.acquire();
		// wl.release();
	}

	@SuppressWarnings("unused")
	void exit2()
	{
		System.out.println("in exit2");
	}

	private void exit()
	{
		try
		{
			if (toneG != null)
			{
				toneG.stopTone();
				toneG.release();

			}
		}
		catch (Exception e)
		{
		}

		NavitVehicle.turn_off_all_providers();
		//try
		//{
		//	NavitSpeech.stop_me();
		//}
		//catch (Exception s)
		//{
		//	s.printStackTrace();
		//}

		try
		{
			if (preferences.PREF_enable_debug_write_gpx)
			{
				NavitVehicle.pos_recording_end();
			}
		}
		catch (Exception e)
		{
		}

		try
		{
			mTts.stop();
		}
		catch (Exception e)
		{

		}

		try
		{
			mTts.shutdown();
		}
		catch (Exception e)
		{

		}
		mTts = null;

		try
		{
			try
			{
				plugin_api.removeListener(zclientListener);
			}
			catch (Exception e)
			{
				e.printStackTrace();
				Log.i("NavitPlugin", "Failed to remove Listener", e);
			}
			unbindService(serviceConnection);
			Log.i("NavitPlugin", "Unbind from the service");
		}
		catch (Throwable t)
		{
			// catch any issues, typical for destroy routines
			// even if we failed to destroy something, we need to continue destroying
			Log.i("NavitPlugin", "Failed to unbind from the service", t);
		}

		try
		{
			if (wl_navigating != null)
			{
				//if (wl_navigating.isHeld())
				//{
				wl_navigating.release();
				Log.e(TAG, "WakeLock Nav: release 1");
				//}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		Log.e(TAG, "1***************** exit called ****************");
		Log.e(TAG, "2***************** exit called ****************");
		Log.e(TAG, "3***************** exit called ****************");
		Log.e(TAG, "4***************** exit called ****************");
		Log.e(TAG, "5***************** exit called ****************");
		Log.e(TAG, "6***************** exit called ****************");
		Log.e(TAG, "7***************** exit called ****************");
		Log.e(TAG, "8***************** exit called ****************");

		//		try
		//		{
		//			// hide download actionbar icon
		//			Navit.cur_menu.findItem(R.id.item_download_menu_button).setVisible(false);
		//			Navit.cur_menu.findItem(R.id.item_download_menu_button).setEnabled(false);
		//		}
		//		catch (Exception e)
		//		{
		//		}

		// ----- service stop -----
		// ----- service stop -----
		System.out.println("Navit:exit -> stop ZANaviMapDownloaderService ---------");
		ZANaviMapDownloaderService.stop_downloading();
		stopService(Navit.ZANaviMapDownloaderServiceIntent);
		// ----- service stop -----
		// ----- service stop -----

		try
		{
			PreferenceManager.getDefaultSharedPreferences(this).edit().putString(PREF_KEY_CRASH, "down").apply();
			System.out.println("app_status_string set:[exit]" + "down");
			System.out.println("app_status_string(1)=" + app_status_string);
			app_status_string = "down";
			System.out.println("app_status_string(2)=" + app_status_string);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		// +++++ // System.gc();
		NavitActivity(-4); // roept navit_destroy aan in C
		Log.e(TAG, "XX1***************** exit called ****************");
		finish();
		Log.e(TAG, "XX2***************** exit called ****************");
		System.runFinalizersOnExit(true);
		Log.e(TAG, "XX3***************** exit called ****************");
		System.exit(0);
		Log.e(TAG, "XX4***************** exit called ****************");
	}

	public boolean handleMessage(Message m)
	{
		//Log.e(TAG, "Handler received message");
		return true;
	}

	public static void set_2d3d_mode_in_settings()
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
		SharedPreferences.Editor editor = prefs.edit();
		editor.putBoolean("show_3d_map", preferences.PREF_show_3d_map);
		editor.apply();
	}

	// review this --jdg--
	public static void follow_button_on()
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
		SharedPreferences.Editor editor = prefs.edit();
		follow_current = true;
		preferences.PREF_follow_gps = true;
		editor.putBoolean("follow_gps", preferences.PREF_follow_gps);
		editor.apply();

		// hold all map drawing -----------

		sNavitObject.sendCallBackMessage(69);

		// hold all map drawing -----------
		Message msg = new Message();
		Bundle b = new Bundle();
		msg.setData(b);
		msg = Navit_progress_h.obtainMessage();
		msg.what = 32;
		try
		{
			Navit_progress_h.sendMessage(msg);
		}
		catch (Exception e)
		{
		}

		getPrefs();
		activatePrefsReal();

		// follow mode ON -----------
		sNavitObject.sendCallBackMessage(74);

		// allow all map drawing -----------
		sNavitObject.sendCallBackMessage(70);

		NavitVehicle.set_last_known_pos_fast_provider();

		// JB fix
		//NavitGraphics.NavitAOSDJava_.postInvalidate();
		//System.out.println("xx paint 12 xx");
		NavitGraphics.OSD_new.postInvalidate();
		NavitGraphics.NavitAOverlay_s.postInvalidate();
	}

	public static void follow_button_off()
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
		SharedPreferences.Editor editor = prefs.edit();
		follow_current = false;
		preferences.PREF_follow_gps = false;
		editor.putBoolean("follow_gps", preferences.PREF_follow_gps);
		editor.apply();
		getPrefs();
		activatePrefsReal();

		// follow mode OFF -----------
		sNavitObject.sendCallBackMessage(75);

		Message msg;
		msg = Navit_progress_h.obtainMessage();
		msg.what = 32;
		try
		{
			Navit_progress_h.sendMessage(msg);
		}
		catch (Exception e)
		{
		}

		// JB fix
		//NavitGraphics.NavitAOSDJava_.postInvalidate();
		//System.out.println("xx paint 13 xx");
		NavitGraphics.OSD_new.postInvalidate();
		NavitGraphics.NavitAOverlay_s.postInvalidate();

	}

	private static void toggle_poi_pref()
	{
		// PREF_show_poi_on_map
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
		SharedPreferences.Editor editor = prefs.edit();
		if (preferences.PREF_show_poi_on_map)
		{
			preferences.PREF_show_poi_on_map = false;
		}
		else
		{
			preferences.PREF_show_poi_on_map = true;
		}
		editor.putBoolean("show_poi_on_map", preferences.PREF_show_poi_on_map);
		editor.apply();
	}

	private static void toggle_follow_button()
	{
		// the "red needle" OSD calls this function only!!
		//Log.e("NavitVehicle", "toggle_follow_button");
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
		SharedPreferences.Editor editor = prefs.edit();
		if (preferences.PREF_follow_gps)
		{
			follow_current = false;
			preferences.PREF_follow_gps = false;

			// follow mode OFF -----------
			sNavitObject.sendCallBackMessage(75);
		}
		else
		{
			follow_current = true;
			preferences.PREF_follow_gps = true;

			// follow mode ON -----------
			sNavitObject.sendCallBackMessage(74);
		}
		editor.putBoolean("follow_gps", preferences.PREF_follow_gps);
		editor.apply();
		//if (!PREF_follow_gps)
		//{
		//	// no compass turning without follow mode!
		//	PREF_use_compass_heading_base = false;
		//}
		//if (!PREF_use_compass_heading_base)
		//{
		//	// child is always "false" when parent is "false" !!
		//	PREF_use_compass_heading_always = false;
		//}

		Message msg = Navit_progress_h.obtainMessage();
		msg.what = 32;
		try
		{
			Navit_progress_h.sendMessage(msg);
		}
		catch (Exception e)
		{
		}

		// hold all map drawing -----------
		sNavitObject.sendCallBackMessage(69);

		getPrefs();
		activatePrefsReal();

		// allow all map drawing -----------
		sNavitObject.sendCallBackMessage(70);

		NavitVehicle.set_last_known_pos_fast_provider();

		// JB fix
		//NavitGraphics.NavitAOSDJava_.postInvalidate();
		//System.out.println("xx paint 14 xx");
		NavitGraphics.OSD_new.postInvalidate();
		NavitGraphics.NavitAOverlay_s.postInvalidate();
	}

	private static void setPrefs_search_country()
	{
		// if (Navit.METHOD_DEBUG) Navit.my_func_name(0);

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
		SharedPreferences.Editor editor = prefs.edit();
		editor.putInt("search_country_id", preferences.PREF_search_country);
		editor.apply();

		// if (Navit.METHOD_DEBUG) Navit.my_func_name(1);
	}

	private static void setPrefs_zoomlevel()
	{
		// if (Navit.METHOD_DEBUG) Navit.my_func_name(0);

		System.out.println("1 save zoom level: " + Navit.GlobalScaleLevel);
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
		SharedPreferences.Editor editor = prefs.edit();
		editor.putInt("zoomlevel_num", Navit.GlobalScaleLevel);
		editor.apply();
		//System.out.println("2 save zoom level: " + Navit.GlobalScaleLevel);

		// if (Navit.METHOD_DEBUG) Navit.my_func_name(1);
	}

	private static void setPrefs_selected_gpx_dir()
	{
		// if (Navit.METHOD_DEBUG) Navit.my_func_name(0);

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString("last_selected_dir_gpxfiles", preferences.PREF_last_selected_dir_gpxfiles);
		editor.apply();

		// if (Navit.METHOD_DEBUG) Navit.my_func_name(1);
	}

	private static void getPrefs_more_map_detail()
	{
		// if (Navit.METHOD_DEBUG) Navit.my_func_name(0);

		// int ret = 0;
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
		try
		{
			preferences.PREF_more_map_detail = Integer.parseInt(prefs.getString("more_map_detail", "0"));
		}
		catch (Exception e)
		{
			preferences.PREF_more_map_detail = 0;
		}

		// if (Navit.METHOD_DEBUG) Navit.my_func_name(1);
	}

	private static void get_prefs_highdpi()
	{
		// if (Navit.METHOD_DEBUG) Navit.my_func_name(0);

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
		try
		{
			preferences.PREF_shrink_on_high_dpi = prefs.getBoolean("shrink_on_high_dpi", true);
		}
		catch (Exception e)
		{
			preferences.PREF_shrink_on_high_dpi = true;
		}

		// if (Navit.METHOD_DEBUG) Navit.my_func_name(1);
	}

	private static void saveArray(String[] array, String arrayName, int size)
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
		SharedPreferences.Editor editor = prefs.edit();
		editor.putInt(arrayName + "_size", size);
		for (int i = 0; i < array.length; i++)
		{
			if (array[i] == null)
			{
				editor.putString(arrayName + "_" + i, "");
			}
			else
			{
				editor.putString(arrayName + "_" + i, array[i]);
			}
		}
		editor.apply();
	}

	private static String[] loadArray(String arrayName, int size)
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
		String[] array = new String[size];
		for (int i = 0; i < size; i++)
		{
			try

			{
				array[i] = prefs.getString(arrayName + "_" + i, "");
			}
			catch (Exception e)
			{
				array[i] = "";
			}
			//System.out.println("array" + i + "=" + array[i]);
		}

		return array;
	}

	private static String[] pushToArray(String[] array_in, String value, int size)
	{
		for (int j = 0; j < size; j++)
		{
			if (array_in[j].equals(value))
			{
				// our value is already in the array, dont add it twice!
				return array_in;
			}
		}

		String[] array = new String[size];
		for (int i = size - 1; i > 0; i--)
		{
			array[i] = array_in[i - 1];
		}
		array[0] = value;
		return array;
	}

	private static void getPrefs()
	{
		// if (Navit.METHOD_DEBUG) Navit.my_func_name(0);

		// save old pref values ---------------
		ZANaviPrefs.deep_copy(preferences, preferencesOld);
		// save old pref values ---------------

		// Get the xml/preferences.xml preferences
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
		preferences.PREF_use_fast_provider = prefs.getBoolean("use_fast_provider", true);
		preferences.PREF_allow_gui_internal = prefs.getBoolean("allow_gui_internal", false);
		preferences.PREF_follow_gps = prefs.getBoolean("follow_gps", true);
		preferences.PREF_use_compass_heading_base = prefs.getBoolean("use_compass_heading_base", false);
		preferences.PREF_use_compass_heading_always = prefs.getBoolean("use_compass_heading_always", false);
		preferences.PREF_use_compass_heading_fast = prefs.getBoolean("use_compass_heading_fast", false);
		preferences.PREF_use_anti_aliasing = prefs.getBoolean("use_anti_aliasing", true);
		preferences.PREF_use_map_filtering = prefs.getBoolean("use_map_filtering", true);
		preferences.PREF_gui_oneway_arrows = prefs.getBoolean("gui_oneway_arrows", true);
		preferences.PREF_c_linedrawing = prefs.getBoolean("c_linedrawing", false);

		preferences.PREF_show_debug_messages = prefs.getBoolean("show_debug_messages", false);

		preferences.PREF_show_3d_map = prefs.getBoolean("show_3d_map", false);
		send_data_to_plugin_bg(PLUGIN_MSG_CAT_3d_mode, String.valueOf(preferences.PREF_show_3d_map));

		preferences.PREF_use_smooth_drawing = prefs.getBoolean("use_smooth_drawing", true);
		preferences.PREF_use_more_smooth_drawing = prefs.getBoolean("use_more_smooth_drawing", false);
		if (preferences.PREF_use_smooth_drawing == false)
		{
			preferences.PREF_use_more_smooth_drawing = false;
		}
		if (preferences.PREF_use_more_smooth_drawing == true)
		{
			preferences.PREF_use_smooth_drawing = true;
		}

		boolean b1 = prefs.getBoolean("show_real_gps_pos", false);
		if (b1 == false)
		{
			preferences.PREF_show_real_gps_pos = 0;
		}
		else
		{
			preferences.PREF_show_real_gps_pos = 1;
		}

		if (preferences.PREF_use_more_smooth_drawing)
		{
			NavitGraphics.Vehicle_delay_real_gps_position = 595;
		}
		else
		{
			NavitGraphics.Vehicle_delay_real_gps_position = 450;
		}

		preferences.PREF_use_lock_on_roads = prefs.getBoolean("use_lock_on_roads", true);
		preferences.PREF_use_route_highways = prefs.getBoolean("use_route_highways", true);
		preferences.PREF_save_zoomlevel = prefs.getBoolean("save_zoomlevel", true);
		preferences.PREF_search_country = prefs.getInt("search_country_id", 1); // default=*ALL*
		preferences.PREF_zoomlevel_num = prefs.getInt("zoomlevel_num", 174698); // default zoom level = 174698 // shows almost the whole world
		preferences.PREF_show_sat_status = prefs.getBoolean("show_sat_status", true);
		preferences.PREF_use_agps = prefs.getBoolean("use_agps", true);
		preferences.PREF_enable_debug_functions = prefs.getBoolean("enable_debug_functions", false);
		preferences.PREF_show_turn_restrictions = prefs.getBoolean("show_turn_restrictions", false);
		preferences.PREF_auto_night_mode = prefs.getBoolean("auto_night_mode", true);

		if (FDBL)
		{
			try
			{
				if (!prefs.contains("enable_debug_crashdetect"))
				{
					prefs.edit().putBoolean("enable_debug_crashdetect", true).apply();
					System.out.println("setting default value of enable_debug_crashdetect to \"true\"");
				}
			}
			catch (Exception e4)
			{
			}
			preferences.PREF_enable_debug_crashdetect = prefs.getBoolean("enable_debug_crashdetect", true);
		}
		else
		{
			if (PLAYSTORE_VERSION_CRASHDETECT)
			{
				try
				{
					if (!prefs.contains("enable_debug_crashdetect"))
					{
						prefs.edit().putBoolean("enable_debug_crashdetect", true).apply();
						System.out.println("setting default value of enable_debug_crashdetect to \"true\"");
					}
				}
				catch (Exception e4)
				{
				}
			}
			preferences.PREF_enable_debug_crashdetect = prefs.getBoolean("enable_debug_crashdetect", PLAYSTORE_VERSION_CRASHDETECT);
		}
		// System.out.println("night mode=" + preferences.PREF_auto_night_mode);

		try
		{
			// recreate the menu items
			Message msg = Navit_progress_h.obtainMessage();
			Bundle b = new Bundle();
			msg.what = 29;
			msg.setData(b);
			Navit_progress_h.sendMessage(msg);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		preferences.PREF_enable_debug_write_gpx = prefs.getBoolean("enable_debug_write_gpx", false);
		preferences.PREF_enable_debug_enable_comm = prefs.getBoolean("enable_debug_enable_comm", false);

		preferences.PREF_speak_street_names = prefs.getBoolean("speak_street_names", true);
		preferences.PREF_use_custom_font = prefs.getBoolean("use_custom_font", true);
		preferences.PREF_draw_polyline_circles = prefs.getBoolean("draw_polyline_circles", true);
		preferences.PREF_streetsearch_r = prefs.getString("streetsearch_r", "2");
		preferences.PREF_route_style = prefs.getString("route_style", "3");
		preferences.PREF_item_dump = prefs.getBoolean("item_dump", false);
		preferences.PREF_show_route_rects = prefs.getBoolean("show_route_rects", false);
		preferences.PREF_trafficlights_delay = prefs.getString("trafficlights_delay", "0");
		preferences.PREF_avoid_sharp_turns = "0";
		//if (tmp)
		//{
		//	preferences.PREF_avoid_sharp_turns = "1";
		//}
		preferences.PREF_autozoom_flag = prefs.getBoolean("autozoom_flag", true);

		preferences.PREF_show_multipolygons = prefs.getBoolean("show_multipolygons", true);
		preferences.PREF_use_index_search = true; // prefs.getBoolean("use_index_search", true);

		// PREF_show_2d3d_toggle = prefs.getBoolean("show_2d3d_toggle", true);
		preferences.PREF_show_2d3d_toggle = true;

		// PREF_show_vehicle_3d = prefs.getBoolean("show_vehicle_3d", true);
		preferences.PREF_show_vehicle_3d = true;

		preferences.PREF_speak_filter_special_chars = prefs.getBoolean("speak_filter_special_chars", true);
		try
		{
			preferences.PREF_routing_engine = Integer.parseInt(prefs.getString("routing_engine", "0"));
		}
		catch (Exception e)
		{
			preferences.PREF_routing_engine = 0;
		}

		// send to C code --------
		CallbackMessageChannel(55598, "" + preferences.PREF_routing_engine);
		// send to C code --------

		preferences.PREF_routing_profile = prefs.getString("routing_profile", "car");
		preferences.PREF_road_priority_001 = (prefs.getInt("road_priority_001", (68 - 10)) + 10); // must ADD minimum value!!
		preferences.PREF_road_priority_002 = (prefs.getInt("road_priority_002", (329 - 10)) + 10); // must ADD minimum value!!
		preferences.PREF_road_priority_003 = (prefs.getInt("road_priority_003", (5000 - 10)) + 10); // must ADD minimum value!!
		preferences.PREF_road_priority_004 = (prefs.getInt("road_priority_004", (5 - 0)) + 0); // must ADD minimum value!!
		preferences.PREF_night_mode_lux = (prefs.getInt("night_mode_lux", (10 - 1)) + 1); // must ADD minimum value!!
		preferences.PREF_night_mode_buffer = (prefs.getInt("night_mode_buffer", (20 - 1)) + 1); // must ADD minimum value!!

		// preferences.PREF_road_prio_weight_street_1_city = (prefs.getInt("road_prio_weight_street_1_city", (30 - 10)) + 10); // must ADD minimum value!!

		preferences.PREF_traffic_speed_factor = (prefs.getInt("traffic_speed_factor", (83 - 20)) + 20); // must ADD minimum value!!

		preferences.PREF_tracking_connected_pref = (prefs.getInt("tracking_connected_pref", (250 - 0)) + 0); // must ADD minimum value!!
		preferences.PREF_tracking_angle_pref = (prefs.getInt("tracking_angle_pref", (40 - 0)) + 0); // must ADD minimum value!!

		preferences.PREF_streets_only = prefs.getBoolean("streets_only", false);
		preferences.PREF_show_status_bar = prefs.getBoolean("show_status_bar", true);
		preferences.PREF_show_poi_on_map = prefs.getBoolean("show_poi_on_map", false);
		preferences.PREF_last_selected_dir_gpxfiles = prefs.getString("last_selected_dir_gpxfiles", sNavitMapDirectory + "/../");

		preferences.PREF_roadspeed_warning = prefs.getBoolean("roadspeed_warning", false);
		preferences.PREF_lane_assist = prefs.getBoolean("lane_assist", false);

		try
		{
			preferences.PREF_roadspeed_warning_margin = Integer.parseInt(prefs.getString("roadspeed_warning_margin", "5"));
		}
		catch (Exception e)
		{
			preferences.PREF_roadspeed_warning_margin = 5;
		}

		preferences.PREF_StreetSearchStrings = loadArray("xxStrtSrhStrxx", STREET_SEARCH_STRINGS_SAVE_COUNT);

		try
		{
			preferences.PREF_drawatorder = Integer.parseInt(prefs.getString("drawatorder", "0"));
		}
		catch (Exception e)
		{
			preferences.PREF_drawatorder = 0;
		}

		//try
		//{
		//	PREF_cancel_map_drawing_timeout = Integer.parseInt(prefs.getString("cancel_map_drawing_timeout", "1"));
		//}
		//catch (Exception e)
		//{
		preferences.PREF_cancel_map_drawing_timeout = 1;
		//}

		try
		{
			preferences.PREF_map_font_size = Integer.parseInt(prefs.getString("map_font_size", "3"));
		}
		catch (Exception e)
		{
			preferences.PREF_map_font_size = 2;
		}

		Navit_last_address_search_country_id = preferences.PREF_search_country;
		Navit_last_address_search_country_iso2_string = NavitAddressSearchCountrySelectActivity.CountryList_Human[preferences.PREF_search_country][0];

		if (!preferences.PREF_follow_gps)
		{
			// no compass turning without follow mode!
			preferences.PREF_use_compass_heading_base = false;
		}

		if (!preferences.PREF_use_compass_heading_base)
		{
			// child is always "false" when parent is "false" !!
			preferences.PREF_use_compass_heading_always = false;
		}

		preferences.PREF_show_maps_debug_view = prefs.getBoolean("show_maps_debug_view", false);

		preferences.PREF_show_vehicle_in_center = prefs.getBoolean("show_vehicle_in_center", false);
		preferences.PREF_use_imperial = prefs.getBoolean("use_imperial", false);
		Navit.cur_max_speed = -1; // to update speedwarning graphics

		//		System.out.println("get settings");
		//      System.out.println("PREF_search_country=" + PREF_search_country);
		//		System.out.println("PREF_follow_gps=" + PREF_follow_gps);
		//		System.out.println("PREF_use_fast_provider=" + PREF_use_fast_provider);
		//		System.out.println("PREF_allow_gui_internal=" + PREF_allow_gui_internal);
		//		System.out.println("PREF_use_compass_heading_base=" + PREF_use_compass_heading_base);
		//		System.out.println("PREF_use_compass_heading_always=" + PREF_use_compass_heading_always);
		//		System.out.println("PREF_show_vehicle_in_center=" + PREF_show_vehicle_in_center);
		//		System.out.println("PREF_use_imperial=" + PREF_use_imperial);

		// if (Navit.METHOD_DEBUG) Navit.my_func_name(1);
	}

	private static void activatePrefs()
	{
		// if (Navit.METHOD_DEBUG) Navit.my_func_name(0);

		activatePrefsReal();

		if (preferences.PREF_save_zoomlevel)
		{
			// only if really started, but NOT if returning from our own child activities!!

			//System.out.println("3 restore zoom level: " + Navit.GlobalScaleLevel);
			//System.out.println("4 restore zoom level: " + PREF_zoomlevel_num);

			Message msg = new Message();
			Bundle b = new Bundle();
			b.putInt("Callback", 33);
			b.putString("s", Integer.toString(preferences.PREF_zoomlevel_num));
			msg.setData(b);

			try
			{
				callback_handler_55.sendMessage(msg);
				Navit.GlobalScaleLevel = preferences.PREF_zoomlevel_num;
				//System.out.println("5 restore zoom level: " + PREF_zoomlevel_num);
			}
			catch (Exception e)
			{
			}
		}
		else
		{
			preferences.PREF_zoomlevel_num = Navit.GlobalScaleLevel;
		}

		// if (Navit.METHOD_DEBUG) Navit.my_func_name(1);
	}

	private static void set_TurnRestrictions_layers()
	{
		int on_ = 59; // on

		if (!preferences.PREF_show_turn_restrictions)
		{
			on_ = 60; // off
		}

		Message msg = new Message();
		Bundle b = new Bundle();
		b.putInt("Callback", on_);
		b.putString("s", "TurnRestrictions");
		msg.setData(b);
		callback_handler_55.sendMessage(msg);
	}

	private static void set_poi_layers()
	{
		int on_ = 59;
		int off_ = 60;

		// System.out.println("POI:1");
		if (preferences.PREF_show_poi_on_map)
		{
			// System.out.println("POI:2");
			on_ = 60;
			off_ = 59;
		}

		// toggle the normal POI layers (to avoid double POIs)
		Message msg = new Message();
		Bundle b = new Bundle();
		b.putInt("Callback", on_);
		b.putString("s", "POI Symbols");
		msg.setData(b);
		callback_handler_55.sendMessage(msg);

		msg = new Message();
		b = new Bundle();
		b.putInt("Callback", on_);
		b.putString("s", "POI Labels");
		msg.setData(b);
		callback_handler_55.sendMessage(msg);

		// toggle full POI icons on/off
		msg = new Message();
		b = new Bundle();
		b.putInt("Callback", off_);
		b.putString("s", "Android-POI-Icons-full");
		msg.setData(b);
		callback_handler_55.sendMessage(msg);

		msg = new Message();
		b = new Bundle();
		b.putInt("Callback", off_);
		b.putString("s", "Android-POI-Labels-full");
		msg.setData(b);
		callback_handler_55.sendMessage(msg);

		// draw_map();
	}

	private static void activatePrefsReal()
	{

	    // fixme --jdg--jdg-- apparently this is wip, needs to be implemented for real
		boolean need_recalc_route = false; // do we need to recalc the route?

		// call some functions to activate the new settings
		if (preferences.PREF_follow_gps)
		{
			follow_current = true;
		}
		else
		{
			follow_current = false;
		}

		if (preferences.PREF_use_fast_provider)
		{
			NavitVehicle.turn_on_fast_provider();
		}
		else
		{
			NavitVehicle.turn_off_fast_provider();
		}

		if (preferences.PREF_show_sat_status)
		{
			NavitVehicle.turn_on_sat_status();
		}
		else
		{
			// status always on !
			//
			// NavitVehicle.turn_off_sat_status();
			NavitVehicle.turn_on_sat_status();
		}

		if (preferences.PREF_show_status_bar)
		{
			show_status_bar_wrapper();
		}
		else
		{
			hide_status_bar_wrapper();
		}

		if (preferences.PREF_allow_gui_internal)
		{
			sNavitObject.sendCallBackMessage(10);
		}
		else
		{
			sNavitObject.sendCallBackMessage(9);
		}

		if (preferences.PREF_use_compass_heading_base)
		{
			// turn on compass
			msg_to_msg_handler(new Bundle(), 12);
			Message msg = new Message();
			Bundle b = new Bundle();
			b.putInt("Callback", 11);
			msg.setData(b);
			try
			{
				callback_handler_55.sendMessage(msg);
			}
			catch (Exception e)
			{
			}
		}
		else
		{
			// turn off compass
			msg_to_msg_handler(new Bundle(), 13);
			Message msg = new Message();
			Bundle b = new Bundle();
			b.putInt("Callback", 12);
			msg.setData(b);
			try
			{
				callback_handler_55.sendMessage(msg);
			}
			catch (Exception e)
			{
			}
		}

		if (preferences.PREF_show_maps_debug_view == true)
		{
			// show real gps pos
			Message msg = new Message();
			Bundle b = new Bundle();
			b.putInt("Callback", 112);
			b.putString("s", "1");
			msg.setData(b);
			try
			{
				callback_handler_55.sendMessage(msg);
			}
			catch (Exception e)
			{
			}
		}
		else
		{
			Message msg = new Message();
			Bundle b = new Bundle();
			b.putInt("Callback", 112);
			b.putString("s", "0");
			msg.setData(b);
			try
			{
				callback_handler_55.sendMessage(msg);
			}
			catch (Exception e)
			{
			}
		}

		if (preferences.PREF_show_real_gps_pos == 1)
		{
			// show real gps pos
			Message msg = new Message();
			Bundle b = new Bundle();
			b.putInt("Callback", 111);
			b.putString("s", "1");
			msg.setData(b);
			try
			{
				callback_handler_55.sendMessage(msg);
			}
			catch (Exception e)
			{
			}
		}
		else
		{
			Message msg = new Message();
			Bundle b = new Bundle();
			b.putInt("Callback", 111);
			b.putString("s", "0");
			msg.setData(b);
			try
			{
				callback_handler_55.sendMessage(msg);
			}
			catch (Exception e)
			{
			}
		}

		// search radius for housenumbers near streets -----------------
		Message msg43 = new Message();
		Bundle b43 = new Bundle();
		b43.putInt("Callback", 89);
		b43.putString("s", "1500");
		msg43.setData(b43);
		try
		{
			callback_handler_55.sendMessage(msg43);
		}
		catch (Exception e)
		{
		}
		// search radius for housenumbers near streets -----------------

		// set routing profile -----------------
		if (Navit_Largemap_DonateVersion_Installed == true)
		{
			Message msg43a = new Message();
			Bundle b43a = new Bundle();
			b43a.putInt("Callback", 90);
			b43a.putString("s", preferences.PREF_routing_profile); // set routing profile
			msg43a.setData(b43a);
			try
			{
				callback_handler_55.sendMessage(msg43a);
			}
			catch (Exception e)
			{
			}

			// need_recalc_route = true;
		}
		// set routing profile -----------------

		Message msg99a = new Message();
		Bundle b99a = new Bundle();
		b99a.putInt("Callback", 98);
		// System.out.println("tracking_connected_pref=" + PREF_tracking_connected_pref);
		b99a.putString("s", "" + preferences.PREF_tracking_connected_pref); // set routing profile
		msg99a.setData(b99a);
		try
		{
			callback_handler_55.sendMessage(msg99a);
		}
		catch (Exception e)
		{
		}

		msg99a = new Message();
		b99a = new Bundle();
		b99a.putInt("Callback", 99);
		// System.out.println("tracking_angle_pref=" + PREF_tracking_angle_pref);
		b99a.putString("s", "" + preferences.PREF_tracking_angle_pref); // set routing profile
		msg99a.setData(b99a);
		try
		{
			callback_handler_55.sendMessage(msg99a);
		}
		catch (Exception e)
		{
		}

		// change road profile -----------------
		if (Navit_Largemap_DonateVersion_Installed == true)
		{
			if (preferences.PREF_routing_profile.equals("bike-normal"))
			{
				Message msg43b = new Message();
				Bundle b43b = new Bundle();
				b43b.putInt("Callback", 91);
				System.out.println("road_priority_001=" + preferences.PREF_road_priority_001);
				b43b.putString("s", "" + preferences.PREF_road_priority_001); // set routing profile
				msg43b.setData(b43b);
				try
				{
					callback_handler_55.sendMessage(msg43b);
				}
				catch (Exception e)
				{
				}

				msg43b = new Message();
				b43b = new Bundle();
				b43b.putInt("Callback", 92);
				System.out.println("road_priority_002=" + preferences.PREF_road_priority_002);
				b43b.putString("s", "" + preferences.PREF_road_priority_002); // set routing profile
				msg43b.setData(b43b);
				try
				{
					callback_handler_55.sendMessage(msg43b);
				}
				catch (Exception e)
				{
				}

				msg43b = new Message();
				b43b = new Bundle();
				b43b.putInt("Callback", 93);
				System.out.println("road_priority_003=" + preferences.PREF_road_priority_003);
				b43b.putString("s", "" + preferences.PREF_road_priority_003); // set routing profile
				msg43b.setData(b43b);
				try
				{
					callback_handler_55.sendMessage(msg43b);
				}
				catch (Exception e)
				{
				}

				msg43b = new Message();
				b43b = new Bundle();
				b43b.putInt("Callback", 94);
				System.out.println("road_priority_004=" + preferences.PREF_road_priority_004);
				b43b.putString("s", "" + preferences.PREF_road_priority_004); // set routing profile
				msg43b.setData(b43b);
				try
				{
					callback_handler_55.sendMessage(msg43b);
				}
				catch (Exception e)
				{
				}

				// switch off layers --------------------
				msg43b = new Message();
				b43b = new Bundle();
				b43b.putInt("Callback", 60);
				b43b.putString("s", "POI traffic lights");
				msg43b.setData(b43b);
				try
				{
					callback_handler_55.sendMessage(msg43b);
				}
				catch (Exception e)
				{
				}
				// switch off layers --------------------

				// switch ON layers --------------------
				msg43b = new Message();
				b43b = new Bundle();
				b43b.putInt("Callback", 59);
				b43b.putString("s", "POI bicycle");
				msg43b.setData(b43b);
				try
				{
					callback_handler_55.sendMessage(msg43b);
				}
				catch (Exception e)
				{
				}
				// switch ON layers --------------------

			}
			else
			{
				// switch off layers --------------------
				Message msg43b = new Message();
				Bundle b43b = new Bundle();
				b43b.putInt("Callback", 60);
				b43b.putString("s", "POI bicycle");
				msg43b.setData(b43b);
				try
				{
					callback_handler_55.sendMessage(msg43b);
				}
				catch (Exception e)
				{
				}
				// switch off layers --------------------

				// switch ON layers --------------------
				msg43b = new Message();
				b43b = new Bundle();
				b43b.putInt("Callback", 59);
				b43b.putString("s", "POI traffic lights");
				msg43b.setData(b43b);
				try
				{
					callback_handler_55.sendMessage(msg43b);
				}
				catch (Exception e)
				{
				}
				// switch ON layers --------------------
			}
		}
		else
		{
			// switch off layers --------------------
			Message msg43b = new Message();
			Bundle b43b = new Bundle();
			b43b.putInt("Callback", 60);
			b43b.putString("s", "POI bicycle");
			msg43b.setData(b43b);
			try
			{
				callback_handler_55.sendMessage(msg43b);
			}
			catch (Exception e)
			{
			}
			// switch off layers --------------------

			// switch ON layers --------------------
			msg43b = new Message();
			b43b = new Bundle();
			b43b.putInt("Callback", 59);
			b43b.putString("s", "POI traffic lights");
			msg43b.setData(b43b);
			try
			{
				callback_handler_55.sendMessage(msg43b);
			}
			catch (Exception e)
			{
			}
			// switch ON layers --------------------
		}
		// change road profile -----------------

		// -- debug -- change some prio weights --
		//		if ((!preferences.PREF_routing_profile.equals("bike-normal")) && (!preferences.PREF_routing_profile.equals("bike-no-oneway")) && (!preferences.PREF_routing_profile.equals("bike-avoid-roads")))
		//		{
		//			Message msg93 = new Message();
		//			Bundle b93 = new Bundle();
		//			b93.putInt("Callback", 110);
		//			b93.putString("s", "street_1_city#route_prio_weight:" + preferences.PREF_road_prio_weight_street_1_city);
		//			msg93.setData(b93);
		//			try
		//			{
		//				NavitGraphics.callback_handler.sendMessage(msg93);
		//			}
		//			catch (Exception e)
		//			{
		//			}
		//
		//			if (preferencesOld.PREF_road_prio_weight_street_1_city != preferences.PREF_road_prio_weight_street_1_city)
		//			{
		//				need_recalc_route = true;
		//			}
		//		}
		// -- debug -- change some prio weights --

		if (NavitGraphics.navit_route_status == 0)
		{
			if (preferences.PREF_c_linedrawing)
			{
				Message msg = new Message();
				Bundle b = new Bundle();
				b.putInt("Callback", 85);
				b.putString("s", "1");
				msg.setData(b);
				try
				{
					callback_handler_55.sendMessage(msg);
				}
				catch (Exception e)
				{
				}
			}
			else
			{
				Message msg = new Message();
				Bundle b = new Bundle();
				b.putInt("Callback", 85);
				b.putString("s", "0");
				msg.setData(b);
				try
				{
					callback_handler_55.sendMessage(msg);
				}
				catch (Exception e)
				{
				}
			}
		}

		Message msg33 = new Message();
		Bundle b33 = new Bundle();
		b33.putInt("Callback", 103);
		if (preferences.PREF_show_vehicle_in_center)
		{
			b33.putString("s", "0");
		}
		else
		{
			b33.putString("s", "" + NavitGraphics.lower_than_center_percent);
		}
		msg33.setData(b33);
		try
		{
			callback_handler_55.sendMessage(msg33);
		}
		catch (Exception e88)
		{
		}

		if (preferences.PREF_use_imperial)
		{
			try
			{
				sNavitObject.sendCallBackMessage(16);
			}
			catch (Exception e)
			{
			}
		}
		else
		{
			try
			{
				sNavitObject.sendCallBackMessage(15);
			}
			catch (Exception e)
			{
			}
		}

		if (preferences.PREF_show_debug_messages)
		{
			try
			{
				sNavitObject.sendCallBackMessage(24);
			}
			catch (Exception e)
			{
			}
		}
		else
		{
			try
			{
				sNavitObject.sendCallBackMessage(25);
			}
			catch (Exception e)
			{
			}
		}

		//		if (PREF_show_3d_map)
		//		{
		//			Message msg = new Message();
		//			Bundle b = new Bundle();
		//			b.putInt("Callback", 31);
		//			msg.setData(b);
		//			try
		//			{
		//				N_NavitGraphics.callback_handler.sendMessage(msg);
		//			}
		//			catch (Exception e)
		//			{
		//			}
		//		}
		//		else
		//		{
		//			Message msg = new Message();
		//			Bundle b = new Bundle();
		//			b.putInt("Callback", 30);
		//			msg.setData(b);
		//			try
		//			{
		//				N_NavitGraphics.callback_handler.sendMessage(msg);
		//			}
		//			catch (Exception e)
		//			{
		//			}
		//		}

		if (preferences.PREF_use_lock_on_roads)
		{
			try
			{
				sNavitObject.sendCallBackMessage(36);
			}
			catch (Exception e)
			{
			}
		}
		else
		{
			try
			{
				sNavitObject.sendCallBackMessage(37);
			}
			catch (Exception e)
			{
			}
		}

		//		if (PREF_draw_polyline_circles)
		//		{
		//			Message msg = new Message();
		//			Bundle b = new Bundle();
		//			b.putString("s", "0");
		//			b.putInt("Callback", 56);
		//			msg.setData(b);
		//			try
		//			{
		//				N_NavitGraphics.callback_handler.sendMessage(msg);
		//			}
		//			catch (Exception e)
		//			{
		//			}
		//		}
		//		else
		//		{
		//			Message msg = new Message();
		//			Bundle b = new Bundle();
		//			b.putString("s", "1");
		//			b.putInt("Callback", 56);
		//			msg.setData(b);
		//			try
		//			{
		//				N_NavitGraphics.callback_handler.sendMessage(msg);
		//			}
		//			catch (Exception e)
		//			{
		//			}
		//		}

		if (preferences.PREF_use_route_highways)
		{
			try
			{
				sNavitObject.sendCallBackMessage(42);
			}
			catch (Exception e)
			{
			}
		}
		else
		{
			try
			{
				sNavitObject.sendCallBackMessage(43);
			}
			catch (Exception e)
			{
			}
		}

		Message msg7 = new Message();
		Bundle b7 = new Bundle();
		b7.putInt("Callback", 57);
		b7.putString("s", "" + preferences.PREF_drawatorder);
		msg7.setData(b7);
		try
		{
			callback_handler_55.sendMessage(msg7);
		}
		catch (Exception e)
		{
		}

		msg7 = new Message();
		b7 = new Bundle();
		b7.putInt("Callback", 58);
		b7.putString("s", preferences.PREF_streetsearch_r);
		msg7.setData(b7);
		try
		{
			callback_handler_55.sendMessage(msg7);
		}
		catch (Exception e)
		{
		}

		if (preferences.PREF_speak_street_names)
		{
			try
			{
				sNavitObject.sendCallBackMessage(54);
			}
			catch (Exception e)
			{
			}
		}
		else
		{
			try
			{
				sNavitObject.sendCallBackMessage(53);
			}
			catch (Exception e)
			{
			}

		}

		try
		{
			NavitGraphics.OverlayDrawThread_cancel_drawing_timeout = NavitGraphics.OverlayDrawThread_cancel_drawing_timeout__options[preferences.PREF_cancel_map_drawing_timeout];
			NavitGraphics.OverlayDrawThread_cancel_thread_sleep_time = NavitGraphics.OverlayDrawThread_cancel_thread_sleep_time__options[preferences.PREF_cancel_map_drawing_timeout];
			NavitGraphics.OverlayDrawThread_cancel_thread_timeout = NavitGraphics.OverlayDrawThread_cancel_thread_timeout__options[preferences.PREF_cancel_map_drawing_timeout];
		}
		catch (Exception e)
		{

		}

		// route variant
		Message msg67 = new Message();
		Bundle b67 = new Bundle();
		// turn off 1
		b67.putInt("Callback", 60);
		b67.putString("s", "route_001");
		msg67.setData(b67);

		try
		{
			callback_handler_55.sendMessage(msg67);
		}
		catch (Exception e)
		{
		}
		// turn off 2
		msg67 = new Message();
		b67 = new Bundle();
		b67.putInt("Callback", 60);
		b67.putString("s", "route_002");
		msg67.setData(b67);
		try
		{
			callback_handler_55.sendMessage(msg67);
		}
		catch (Exception e)
		{
		}
		// turn off 3
		msg67 = new Message();
		b67 = new Bundle();
		b67.putInt("Callback", 60);
		b67.putString("s", "route_003");
		msg67.setData(b67);
		try
		{
			callback_handler_55.sendMessage(msg67);
		}
		catch (Exception e)
		{
		}

		// turn on the wanted route style
		msg67 = new Message();
		b67 = new Bundle();
		b67.putInt("Callback", 59);
		b67.putString("s", "route_00" + preferences.PREF_route_style);
		msg67.setData(b67);
		try
		{
			callback_handler_55.sendMessage(msg67);
		}
		catch (Exception e)
		{
		}
		// route variant

		// show route rectanlges -----
		if (preferences.PREF_show_route_rects)
		{
			try
			{
				sNavitObject.sendCallBackMessage(76);
			}
			catch (Exception e)
			{
			}
		}
		else
		{
			try
			{
				sNavitObject.sendCallBackMessage(77);
			}
			catch (Exception e)
			{
			}
		}
		// show route rectanlges -----

		// show route multipolygons -----
		if (preferences.PREF_show_multipolygons)
		{
			try
			{
				sNavitObject.sendCallBackMessage(66);
			}
			catch (Exception e)
			{
			}
		}
		else
		{
			try
			{
				sNavitObject.sendCallBackMessage(67);
			}
			catch (Exception e)
			{
			}
		}
		// show route multipolygons -----

		// traffic lights delay ----
		msg67 = new Message();
		b67 = new Bundle();
		b67.putInt("Callback", 79);
		//System.out.println("traffic lights delay:" + PREF_trafficlights_delay);
		// (PREF_trafficlights_delay / 10) seconds delay for each traffic light
		b67.putString("s", preferences.PREF_trafficlights_delay); // (delay in 1/10 of a second)
		msg67.setData(b67);
		try
		{
			callback_handler_55.sendMessage(msg67);
		}
		catch (Exception e)
		{
		}
		// traffic lights delay ----

		// avoid sharp turns ----
		msg67 = new Message();
		b67 = new Bundle();
		b67.putInt("Callback", 86);
		b67.putString("s", preferences.PREF_avoid_sharp_turns);
		msg67.setData(b67);
		try
		{
			callback_handler_55.sendMessage(msg67);
		}
		catch (Exception e)
		{
		}

		msg67 = new Message();
		b67 = new Bundle();
		b67.putInt("Callback", 87);
		b67.putString("s", "47"); // **DANGER** sharp turn max angle hardcoded here!! **DANGER**
		msg67.setData(b67);
		try
		{
			callback_handler_55.sendMessage(msg67);
		}
		catch (Exception e)
		{
		}

		msg67 = new Message();
		b67 = new Bundle();
		b67.putInt("Callback", 88);
		b67.putString("s", "6000");
		msg67.setData(b67);
		try
		{
			callback_handler_55.sendMessage(msg67);
		}
		catch (Exception e)
		{
		}
		// avoid sharp turns ----

		// autozoom flag ----
		msg67 = new Message();
		b67 = new Bundle();
		b67.putInt("Callback", 80);
		if (preferences.PREF_autozoom_flag)
		{
			b67.putString("s", "1"); // (0 or 1)
		}
		else
		{
			b67.putString("s", "0"); // (0 or 1)			
		}
		msg67.setData(b67);
		try
		{
			callback_handler_55.sendMessage(msg67);
		}
		catch (Exception e)
		{

		}
		// autozoom flag ----

		if ((Navit.Navit_Largemap_DonateVersion_Installed) || (Navit.Navit_DonateVersion_Installed))
		{
			// use pref
		}
		else
		{
			preferences.PREF_roadspeed_warning = false;
		}

		if ((Navit.Navit_Largemap_DonateVersion_Installed) || (Navit.Navit_DonateVersion_Installed))
		{
			// use pref
		}
		else
		{
			preferences.PREF_lane_assist = false;
		}

		if (preferences.PREF_streets_only)
		{
			// ----------------------- streets only pref -------------------
			// 59 -> enable
			// 60 -> disable
			Message msg31 = new Message();
			Bundle b31 = new Bundle();
			b31.putInt("Callback", 60);
			b31.putString("s", "polygons001");
			msg31.setData(b31);
			callback_handler_55.sendMessage(msg31);

			msg31 = new Message();
			b31 = new Bundle();
			b31.putInt("Callback", 60);
			b31.putString("s", "polygons");
			msg31.setData(b31);
			callback_handler_55.sendMessage(msg31);

			msg31 = new Message();
			b31 = new Bundle();
			b31.putInt("Callback", 60);
			b31.putString("s", "POI Symbols");
			msg31.setData(b31);
			callback_handler_55.sendMessage(msg31);

			msg31 = new Message();
			b31 = new Bundle();
			b31.putInt("Callback", 60);
			b31.putString("s", "POI Labels");
			msg31.setData(b31);
			callback_handler_55.sendMessage(msg31);

			msg31 = new Message();
			b31 = new Bundle();
			b31.putInt("Callback", 60);
			b31.putString("s", "Android-POI-Icons-full");
			msg31.setData(b31);
			callback_handler_55.sendMessage(msg31);

			msg31 = new Message();
			b31 = new Bundle();
			b31.putInt("Callback", 60);
			b31.putString("s", "Android-POI-Labels-full");
			msg31.setData(b31);
			callback_handler_55.sendMessage(msg31);

			msg31 = new Message();
			b31 = new Bundle();
			b31.putInt("Callback", 60);
			b31.putString("s", "streets");
			msg31.setData(b31);
			callback_handler_55.sendMessage(msg31);

			msg31 = new Message();
			b31 = new Bundle();
			b31.putInt("Callback", 60);
			b31.putString("s", "streets_1");
			msg31.setData(b31);
			callback_handler_55.sendMessage(msg31);

			msg31 = new Message();
			b31 = new Bundle();
			b31.putInt("Callback", 60);
			b31.putString("s", "streets_2");
			msg31.setData(b31);
			callback_handler_55.sendMessage(msg31);

			msg31 = new Message();
			b31 = new Bundle();
			b31.putInt("Callback", 59);
			b31.putString("s", "streets_STR_ONLY");
			msg31.setData(b31);
			callback_handler_55.sendMessage(msg31);

			msg31 = new Message();
			b31 = new Bundle();
			b31.putInt("Callback", 59);
			b31.putString("s", "streets_1_STR_ONLY");
			msg31.setData(b31);
			callback_handler_55.sendMessage(msg31);

			msg31 = new Message();
			b31 = new Bundle();
			b31.putInt("Callback", 59);
			b31.putString("s", "streets_2_STR_ONLY");
			msg31.setData(b31);
			callback_handler_55.sendMessage(msg31);
			// ----------------------- streets only pref -------------------
		}
		else
		{
			// ----------------------- streets only pref -------------------
			// 59 -> enable
			// 60 -> disable
			Message msg31 = new Message();
			Bundle b31 = new Bundle();
			b31.putInt("Callback", 59);
			b31.putString("s", "polygons001");
			msg31.setData(b31);
			callback_handler_55.sendMessage(msg31);

			msg31 = new Message();
			b31 = new Bundle();
			b31.putInt("Callback", 59);
			b31.putString("s", "polygons");
			msg31.setData(b31);
			callback_handler_55.sendMessage(msg31);

			msg31 = new Message();
			b31 = new Bundle();
			b31.putInt("Callback", 59);
			b31.putString("s", "POI Symbols");
			msg31.setData(b31);
			callback_handler_55.sendMessage(msg31);

			msg31 = new Message();
			b31 = new Bundle();
			b31.putInt("Callback", 59);
			b31.putString("s", "POI Labels");
			msg31.setData(b31);
			callback_handler_55.sendMessage(msg31);

			msg31 = new Message();
			b31 = new Bundle();
			b31.putInt("Callback", 60);
			b31.putString("s", "Android-POI-Icons-full");
			msg31.setData(b31);
			callback_handler_55.sendMessage(msg31);

			msg31 = new Message();
			b31 = new Bundle();
			b31.putInt("Callback", 60);
			b31.putString("s", "Android-POI-Labels-full");
			msg31.setData(b31);
			callback_handler_55.sendMessage(msg31);

			msg31 = new Message();
			b31 = new Bundle();
			b31.putInt("Callback", 59);
			b31.putString("s", "streets");
			msg31.setData(b31);
			callback_handler_55.sendMessage(msg31);

			msg31 = new Message();
			b31 = new Bundle();
			b31.putInt("Callback", 59);
			b31.putString("s", "streets_1");
			msg31.setData(b31);
			callback_handler_55.sendMessage(msg31);

			msg31 = new Message();
			b31 = new Bundle();
			b31.putInt("Callback", 59);
			b31.putString("s", "streets_2");
			msg31.setData(b31);
			callback_handler_55.sendMessage(msg31);

			msg31 = new Message();
			b31 = new Bundle();
			b31.putInt("Callback", 60);
			b31.putString("s", "streets_STR_ONLY");
			msg31.setData(b31);
			callback_handler_55.sendMessage(msg31);

			msg31 = new Message();
			b31 = new Bundle();
			b31.putInt("Callback", 60);
			b31.putString("s", "streets_1_STR_ONLY");
			msg31.setData(b31);
			callback_handler_55.sendMessage(msg31);

			msg31 = new Message();
			b31 = new Bundle();
			b31.putInt("Callback", 60);
			b31.putString("s", "streets_2_STR_ONLY");
			msg31.setData(b31);
			callback_handler_55.sendMessage(msg31);
			// ----------------------- streets only pref -------------------
		}

		// ---------- set traffic factor for road speeds ------------
		try
		{
			Message msg_ss1 = new Message();
			Bundle b_ss1 = new Bundle();
			b_ss1.putInt("Callback", 106);
			b_ss1.putString("s", Integer.toString(preferences.PREF_traffic_speed_factor));
			msg_ss1.setData(b_ss1);
			callback_handler_55.sendMessage(msg_ss1);
		}
		catch (Exception e)
		{
		}
		// ---------- set traffic factor for road speeds ------------

		// ------- PREF_show_poi_on_map ---------------------------------
		set_poi_layers();
		// ------- PREF_show_poi_on_map ---------------------------------

		set_TurnRestrictions_layers();

		// set vars for mapdir change (only really takes effect after restart!)
		getPrefs_mapdir();
	}

	private static void getPrefs_mapdir()
	{
		// Get the xml/preferences.xml preferences
		sNavitDataStorageDirs = androidx.core.content.ContextCompat.getExternalFilesDirs(getContext(), null);

		if (sNavitDataStorageDirs.length > 0)
		{
			System.out.println("DataStorageDir count=" + sNavitDataStorageDirs.length);

			//just show possible dirs in the log
			for (int jj2 = 0; jj2 < sNavitDataStorageDirs.length; jj2++)
			{
				if (sNavitDataStorageDirs[jj2] != null)
				{
					System.out.println("DataStorageDir[" + jj2 + "]=" + sNavitDataStorageDirs[jj2].getAbsolutePath() + "/zanavi/maps/");
				}
			}
		}
		else
		{
			//hier een enkel exemplaar proberen ????
		}
		//String default_sdcard_dir = sNavitDataStorageDirs[0].getAbsolutePath() + "/zanavi/maps/";
		String default_sdcard_dir = getContext().getExternalFilesDir(null) + "/zanavi/maps/";
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
		sNavitMapDirectory = prefs.getString("map_directory", default_sdcard_dir);
		System.out.println("DDD:04:" + sNavitMapDirectory);

		String Navit_storage_directory_select = prefs.getString("storage_directory", "-1");
		int Navit_storage_directory_select_i = 0;
		try
		{
			Navit_storage_directory_select_i = Integer.parseInt(Navit_storage_directory_select);
		}
		catch (Exception e)
		{

		}
		System.out.println("DataStorageDir[sel 1]=" + sNavitMapDirectory);
		System.out.println("DataStorageDir[sel 2]=" + Navit_storage_directory_select);

		if (Navit_storage_directory_select_i > 0)
		{
			if ((sNavitDataStorageDirs[Navit_storage_directory_select_i - 1].getAbsolutePath().length() > "/zanavi/maps/".length()) && (sNavitDataStorageDirs[Navit_storage_directory_select_i - 1].getAbsolutePath().endsWith("/zanavi/maps/")))
			{
				sNavitMapDirectory = sNavitDataStorageDirs[Navit_storage_directory_select_i - 1].getAbsolutePath();
			}
			else
			{
				sNavitMapDirectory = sNavitDataStorageDirs[Navit_storage_directory_select_i - 1].getAbsolutePath() + "/zanavi/maps/";
			}
			System.out.println("DDD:06:" + sNavitMapDirectory);
		}
		System.out.println("DataStorageDir[*in use*]=" + sNavitMapDirectory);

		System.out.println("CI:DataStorageDir in use:" + sNavitMapDirectory);
		ZANaviLogMessages.ap("MapDataStorageDir", sNavitMapDirectory);

		// Navit_storage_directory_select:
		// -1    --> first run -> select best dir for user
		//  0    --> use custom directory
		//  1..n --> select default dir on SD Card number 1..n

		// ** DEBUG ** set dir manually ** // NavitDataDirectory_Maps = default_sdcard_dir + "/zanavi/maps/";
		// ** DEBUG ** NavitDataDirectory_Maps = prefs.getString("navit_mapsdir", "/sdcard" + "/zanavi/maps/");
		//Log.e(TAG, "new sdcard dir=" + NavitDataDirectory_Maps);

		// ---- log ----
		try
		{
			if (sNavitDataStorageDirs != null)
			{
				if (sNavitDataStorageDirs.length > 0)
				{
					int new_count = 0;
					for (File sNavitDataStorageDir : sNavitDataStorageDirs) {
						if (sNavitDataStorageDir != null) {
							new_count++;
						}
					}

					CharSequence[] entries = new CharSequence[new_count + 1];
					entries[0] = "Custom Path";
					long avail_space;
					String avail_space_string;

					for (int ij2 = 0; ij2 < sNavitDataStorageDirs.length; ij2++)
					{
						if ((sNavitDataStorageDirs[ij2].getAbsolutePath().length() > "/zanavi/maps/".length()) &&
								(!sNavitDataStorageDirs[ij2].getAbsolutePath().endsWith("/zanavi/maps/")))
						{
							System.out.println("DDD:06a:" + ij2 + ":" + Navit.sNavitDataStorageDirs[ij2].getAbsolutePath());
							String temp = sNavitDataStorageDirs[ij2].getAbsolutePath();
							sNavitDataStorageDirs[ij2] = null;
							sNavitDataStorageDirs[ij2] = new File(temp + "/zanavi/maps/");
							System.out.println("DDD:06b:" + ij2 + ":" + Navit.sNavitDataStorageDirs[ij2].getAbsolutePath());
						}
					}

					new_count = 0;

					for (int ij = 0; ij < sNavitDataStorageDirs.length; ij++)
					{
						// System.out.println("DataStorageDir prefs list=" + Navit.NavitDataStorageDirs[ij]);

						System.out.println("DDD:07:" + ij + ":" + sNavitDataStorageDirs[ij].getAbsolutePath());

						if (sNavitDataStorageDirs[ij] != null)
						{

							try
							{
								File dir = new File(sNavitDataStorageDirs[ij].getAbsolutePath());
								dir.mkdirs();
							}
							catch (Exception emkdir)
							{
								System.out.println("DDD:07mkdirs:Error" + ":" + sNavitDataStorageDirs[ij].getAbsolutePath());
							}

							avail_space = NavitAvailableSpaceHandler.getExternalAvailableSpaceInMB(sNavitDataStorageDirs[ij].getAbsolutePath());
							String avail_space_str = NavitAvailableSpaceHandler.getExternalAvailableSpaceInMBformattedString(sNavitDataStorageDirs[ij].getAbsolutePath());
							if (avail_space < 0)
							{
								avail_space_string = "";
							}
							else if (avail_space > 1200)
							{
								avail_space_str = NavitAvailableSpaceHandler.getExternalAvailableSpaceInGBformattedString(sNavitDataStorageDirs[ij].getAbsolutePath());
								avail_space_string = " [" + avail_space_str + "GB free]";
							}
							else
							{
								avail_space_string = " [" + avail_space_str + "MB free]";
							}

							// System.out.println("DataStorageDir avail space=" + avail_space);

							entries[new_count + 1] = "SD Card:" + sNavitDataStorageDirs[ij].getAbsolutePath() + avail_space_string;

							ZANaviLogMessages.ap("NavitDataStorageDirs_" + ij, "" + entries[new_count + 1]);

							new_count++;
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			System.out.println("DataStorageDir Ex01.2");
			e.printStackTrace();
		}
	}

	static void change_maps_dir(Context c, int num, String path)
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);

		try
		{
			prefs.edit().putString("map_directory", path).apply();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		try
		{
			prefs.edit().putString("storage_directory", "" + num).apply();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		sNavitMapDirectory = prefs.getString("map_directory", "");
		String Navit_storage_directory_select = prefs.getString("storage_directory", "-1");
		System.out.println("change_maps_dir:DataStorageDir[sel 1]=" + sNavitMapDirectory);
		System.out.println("change_maps_dir:DataStorageDir[sel 2]=" + Navit_storage_directory_select);

		activatePrefs_mapdir(false);
	}

	private static String sanity_check_maps_dir(String check_dir)
	{
		String ret = check_dir;
		ret = ret.replaceAll("\\n", ""); // newline -> ""
		ret = ret.replaceAll("\\r", ""); // return -> ""
		ret = ret.replaceAll("\\t", ""); // tab -> ""
		ret = ret.replaceAll(" ", ""); // space -> ""
		ret = ret.replaceAll("\"", ""); // \" -> ""
		ret = ret.replaceAll("'", ""); // \' -> ""
		ret = ret.replaceAll("\\\\", ""); // "\" -> ""
		if (!ret.endsWith("/"))
		{
			ret = ret + "/";
		}
		// System.out.println("sanity check:" + ret);
		return ret;
	}

	private static void activatePrefs_mapdir(Boolean at_startup)
	{
		// if (Navit.METHOD_DEBUG) Navit.my_func_name(0);

		// activate the new directory
		sNavitMapDirectory = sanity_check_maps_dir(sNavitMapDirectory);

		//System.out.println("xxxxxxxx************XXXXXXXXXXX");
		//System.out.println("xxxxxxxx************XXXXXXXXXXX");
		//System.out.println("xxxxxxxx************XXXXXXXXXXX");
		//System.out.println("xxxxxxxx************XXXXXXXXXXX");
		//System.out.println("xxxxxxxx************XXXXXXXXXXX");
		//System.out.println("xxxxxxxx************XXXXXXXXXXX");
		//System.out.println("xxxxxxxx************XXXXXXXXXXX");
		//System.out.println("xxxxxxxx************XXXXXXXXXXX");
		//System.out.println("xxxxxxxx************XXXXXXXXXXX");

		Handler h_temp;
		h_temp = callback_handler_55;
		//System.out.println("handler 1=" + h_temp.toString());

		Message msg1 = new Message();
		Bundle b1 = new Bundle();
		b1.putInt("Callback", 47);
		b1.putString("s", sNavitMapDirectory);
		msg1.setData(b1);
		h_temp.sendMessage(msg1);

		if (!at_startup)
		{
			sNavitObject.sendCallBackMessage(18);
		}

		// if (Navit.METHOD_DEBUG) Navit.my_func_name(1);
	}

	private void getPrefs_theme()
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		int theme_tmp = Integer.parseInt(prefs.getString("current_theme", "0"));
		// 0 -> Navit.DEFAULT_THEME_OLD_DARK
		// 1 -> Navit.DEFAULT_THEME_OLD_LIGHT
		preferences.PREF_current_theme = Navit.DEFAULT_THEME_OLD_DARK;
		if (theme_tmp == 1)
		{
			preferences.PREF_current_theme = Navit.DEFAULT_THEME_OLD_LIGHT;
		}
	}

	private void getPrefs_theme_main()
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		int theme_tmp = Integer.parseInt(prefs.getString("current_theme", "0"));
		// 0 -> Navit.DEFAULT_THEME_OLD_DARK
		// 1 -> Navit.DEFAULT_THEME_OLD_LIGHT
		preferences.PREF_current_theme_M = Navit.DEFAULT_THEME_OLD_DARK_M;
		if (theme_tmp == 1)
		{
			preferences.PREF_current_theme_M = Navit.DEFAULT_THEME_OLD_LIGHT_M;
		}
	}

	private static void getPrefsLocale()
	{
		// if (Navit.METHOD_DEBUG) Navit.my_func_name(0);

		// Get the xml/preferences.xml preferences
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
		preferences.PREF_navit_lang = prefs.getString("navit_lang", "*DEFAULT*");
		System.out.println("**** ***** **** pref lang=" + preferences.PREF_navit_lang);

		// if (Navit.METHOD_DEBUG) Navit.my_func_name(1);
	}

	private static void activatePrefsLocale()
	{
		// if (Navit.METHOD_DEBUG) Navit.my_func_name(0);

		// creating locale
		if (!preferences.PREF_navit_lang.equals("*DEFAULT*"))
		{
			Locale locale2 = null;
			if (preferences.PREF_navit_lang.contains("_"))
			{
				String _lang = preferences.PREF_navit_lang.split("_", 2)[0];
				String _country = preferences.PREF_navit_lang.split("_", 2)[1];
				System.out.println("l=" + _lang + " c=" + _country);
				locale2 = new Locale(_lang, _country);
			}
			else
			{
				locale2 = new Locale(preferences.PREF_navit_lang);
			}
			Locale.setDefault(locale2);
			Configuration config2 = new Configuration();
			config2.locale = locale2;
			// updating locale
			getContext().getResources().updateConfiguration(config2, null);
		}

		// if (Navit.METHOD_DEBUG) Navit.my_func_name(1);
	}

	private static void getPrefs_mapcache()
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
		try
		{
			preferences.PREF_mapcache = Integer.parseInt(prefs.getString("mapcache", "" + (10 * 1024)));
		}
		catch (Exception e)
		{
			e.printStackTrace();
			preferences.PREF_mapcache = 10 * 1024;
		}
		System.out.println("**** ***** **** pref mapcache=" + preferences.PREF_mapcache);
	}

	private static void activatePrefs_mapcache()
	{
		// if (Navit.METHOD_DEBUG) Navit.my_func_name(0);

		Handler h_temp2;
		h_temp2 = callback_handler_55;
		System.out.println("activatePrefs_mapcache ");
		Message msg7 = new Message();
		Bundle b7 = new Bundle();
		b7.putInt("Callback", 55);
		b7.putString("s", String.valueOf(preferences.PREF_mapcache * 1024));
		msg7.setData(b7);
		h_temp2.sendMessage(msg7);

		// if (Navit.METHOD_DEBUG) Navit.my_func_name(1);
	}

	public native void NavitMain(String lang, String display_density_string, String n_datadir, String n_sharedir, Bitmap main_map_bitmap);

	public native void NavitActivity(int activity);

	/*
	 * this is used to load the 'navit' native library on
	 * application startup. The library has already been unpacked at
	 * installation time by the package manager.
	 */
	static
	{
		System.loadLibrary("navit");
	}

	/*
	 * Show a search activity with the string "search" filled in
	 */
	private void executeSearch(String search)
	{
		Navit.use_index_search = Navit.allow_use_index_search();
		Intent search_intent = new Intent(this, NavitAddressSearchActivity.class);
		search_intent.putExtra("title", Navit.get_text("Enter: City and Street")); //TRANS
		search_intent.putExtra("address_string", search);
		search_intent.putExtra("type", "offline");
		search_intent.putExtra("search_country_id", Navit_last_address_search_country_id);
		String pm_temp = "0";
		if (Navit_last_address_partial_match)
		{
			pm_temp = "1";
		}
		search_intent.putExtra("partial_match", pm_temp);
		this.startActivityForResult(search_intent, NavitAddressSearch_id_offline);
	}

	private void share_location(String lat, String lon, String name, String subject_text, String time_at_destination, boolean is_dest)
	{
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("text/plain");
		String url;
		final String map_zoomlevel = "18";
		// url = "" + lat + "," + lon + "\n" + name;
		url = "http://maps.google.com/?q=" + lat + "," + lon + "&z=" + map_zoomlevel + "\n\n" + name;

		if (is_dest)
		{
			if (time_at_destination.compareTo("") != 0)
			{
				url = url + " " + time_at_destination;
			}
		}

		intent.putExtra(Intent.EXTRA_TEXT, url);
		intent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject_text);
		//Uri uri = Uri.parse("geo:0,0?z=16&q=" + lat + "," + lon);
		//intent.putExtra(Intent.EXTRA_STREAM, uri);

		// shut down TTS ---------------
		//		if (!Navit.is_navigating)
		//		{
		//			try
		//			{
		//				mTts.stop();
		//			}
		//			catch (Exception e)
		//			{
		//
		//			}
		//
		//			try
		//			{
		//				mTts.shutdown();
		//			}
		//			catch (Exception e)
		//			{
		//
		//			}
		//		}
		// shut down TTS ---------------

		startActivityForResult(Intent.createChooser(intent, Navit.get_text("Share")), NavitAddressSearch_id_sharedest); // TRANS
	}

	/*
	 * open google maps at a given coordinate
	 */
	private void googlemaps_show(String lat, String lon, String name)
	{
		// geo:latitude,longitude
		String url = null;
		Intent gmaps_intent = new Intent(Intent.ACTION_VIEW);

		//url = "geo:" + lat + "," + lon + "?z=" + "16";
		//url = "geo:0,0?q=" + lat + "," + lon + " (" + name + ")";
		url = "geo:0,0?z=16&q=" + lat + "," + lon + " (" + name + ")";

		gmaps_intent.setData(Uri.parse(url));
		this.startActivityForResult(gmaps_intent, NavitAddressSearch_id_gmaps);
	}

	private void zoom_out_full()
	{
		// if (Navit.METHOD_DEBUG) Navit.my_func_name(0);

		System.out.println("*** Zoom out FULL ***");
		sendCallBackMessage(8);

		// if (Navit.METHOD_DEBUG) Navit.my_func_name(1);
	}

	private static void show_geo_on_screen_with_zoom_and_delay(float lat, float lon)
	{
		show_geo_on_screen_with_zoom_and_delay(lat, lon, 0);
	}

	@SuppressLint("NewApi")
	private static void show_geo_on_screen_with_zoom_and_delay(final float lat, final float lon, final int millis_start_delay)
	{
		System.out.println("XSOM:010");

		final Thread temp_work_thread = new Thread()
		{
			@Override
			public void run()
			{
				try
				{
					Thread.sleep((long) millis_start_delay);
					Navit.show_geo_on_screen_no_draw(lat, lon);
					Thread.sleep(500);
					Navit.set_zoom_level_no_draw(Navit.Navit_SHOW_DEST_ON_MAP_ZOOMLEVEL);
					Thread.sleep(120);
					Navit.draw_map();
				}
				catch (Exception e2)
				{
					e2.printStackTrace();
				}
			}
		};
		temp_work_thread.start();
	}

	private static void show_geo_on_screen(float lat, float lng)
	{
		// this function sets screen center to "lat, lon", and just returns a dummy string!
		Navit.cwthr.CallbackGeoCalc2(2, 3, lat, lng);
	}

	static public void show_geo_on_screen_no_draw(float lat, float lng)
	{
		// this function sets screen center to "lat, lon", and just returns a dummy string!
		Navit.cwthr.CallbackGeoCalc2(2, 15, lat, lng);
	}

	public void zoom_to_route()
	{
		try
		{
			//System.out.println("");
			//System.out.println("*** Zoom to ROUTE ***");
			//System.out.println("");
			sendCallBackMessage(17);

			set_map_position_to_screen_center();
		}
		catch (Exception e)
		{
		}
	}

	void set_map_position_to_screen_center()
	{
		try
		{
			Message msg = new Message();
			Bundle b = new Bundle();
			b.putInt("Callback", 51);

			if (Navit.GFX_OVERSPILL)
			{
				b.putInt("x", (int) (NavitGraphics.Global_dpi_factor * ((NG__map_main.view.getWidth() / 2) + NavitGraphics.mCanvasWidth_overspill)));
				b.putInt("y", (int) (NavitGraphics.Global_dpi_factor * ((NG__map_main.view.getHeight() / 2) + NavitGraphics.mCanvasHeight_overspill)));
			}
			else
			{
				b.putInt("x", (int) (NavitGraphics.Global_dpi_factor * NG__map_main.view.getWidth() / 2));
				b.putInt("y", (int) (NavitGraphics.Global_dpi_factor * NG__map_main.view.getHeight() / 2));
			}
			msg.setData(b);
			callback_handler_55.sendMessage(msg);
		}
		catch (Exception e)
		{
		}
	}

	private void turn_on_compass()
	{
		try
		{
			if (!preferences.PREF_use_compass_heading_fast)

			{
				// Slower
				sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_NORMAL);
			}
			else
			{
				// FAST
				sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_UI);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private void turn_off_compass()
	{
		try
		{
			sensorManager.unregisterListener(this);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void onSensorChanged(SensorEvent event)
	{

		if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
		{
			// System.out.println("Sensor.TYPE_MAGNETIC_FIELD");
			return;
		}

		if (event.sensor.getType() == Sensor.TYPE_ORIENTATION)
		{
			// System.out.println("Sensor.TYPE_ORIENTATION");

			// compass
			float myAzimuth = event.values[0];
			// double myPitch = event.values[1];
			// double myRoll = event.values[2];

			//String out = String.format("Azimuth: %.2f", myAzimuth);
			//System.out.println("compass: " + out);
			NavitVehicle.update_compass_heading(myAzimuth);
		}
	}

	public void onAccuracyChanged(Sensor sensor, int accuracy)
	{
		// compass
	}

	private void hide_status_bar()
	{
		if (!preferences.PREF_show_status_bar)
		{
			// Hide the Status Bar
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN | WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}
	}

	private void show_status_bar()
	{
		// Show the Status Bar
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN, WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN | WindowManager.LayoutParams.FLAG_FULLSCREEN);
	}

	public static Boolean downloadGPSXtra(Context context)
	{
		Boolean ret = false;
		Boolean ret2;
		try
		{
			LocationManager locationmanager2 = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
			Bundle bundle = new Bundle();
			//ret2 = locationmanager2.sendExtraCommand("gps", "delete_aiding_data", null);
			//ret = ret2;
			// System.out.println("ret0=" + ret);
			ret2 = locationmanager2.sendExtraCommand("gps", "force_xtra_injection", bundle);
			ret = ret2;
			//System.out.println("ret1=" + ret2);
			ret2 = locationmanager2.sendExtraCommand("gps", "force_time_injection", bundle);
			ret = ret || ret2;
			//System.out.println("ret2=" + ret2);
		}
		catch (Exception e)
		{
			//System.out.println("*XX*");
			e.printStackTrace();
		}
		return ret;
	}

	private static void remove_oldest_normal_point()
	{
		int i;
		for (i = 0; i < map_points.size(); i++)
		{
			Navit_Point_on_Map element_temp = map_points.get(i);
			if (element_temp.addon == null)
			{
				// its a normal (non home, non special item), so can remove it, and return.
				break;
			}
		}
	}

	static int find_home_point()
	{
		int home_id = -1;
		int i;

		for (i = 0; i < map_points.size(); i++)
		{
			Navit_Point_on_Map element_temp = map_points.get(i);
			if (element_temp.addon != null)
			{
				if (element_temp.addon.equals("1"))
				{
					// found home
					return i;
				}
			}
		}
		return home_id;
	}

	private static void readd_home_point()
	{
		try
		{
			int home_id = find_home_point();
			if (home_id != -1)
			{
				Navit_Point_on_Map element_old = map_points.get(home_id);
				map_points.remove(home_id);
				map_points.add(element_old);
			}
		}
		catch (Exception e)
		{
		}
	}

	private static void add_map_point(Navit_Point_on_Map element)
	{
		if (element == null)
		{
			return;
		}

		if (map_points == null)
		{
			map_points = new ArrayList<>();
		}

		int el_pos = get_destination_pos(element);
		// int home_id = find_home_point();
		boolean is_home = is_home_element(element);

		// System.out.println("EEPOS:el_pos=" + el_pos + " is_home=" + is_home);

		if (is_home)
		{
			// its the "home" destination
			return;
		}

		if (map_points.size() > Navit_MAX_RECENT_DESTINATIONS)
		{
			try
			{
				// map_points.remove(0);
				remove_oldest_normal_point();
			}
			catch (Exception e)
			{
			}
		}

		if (el_pos == -1)
		{
			// if not duplicate, then add
			map_points.add(element);
			readd_home_point();
			write_map_points();
		}
		else
		{
			try
			{
				// if already in list, then first remove and add again
				// that moves it to the top of the list
				Navit_Point_on_Map element_old = map_points.get(el_pos);
				map_points.remove(el_pos);
				map_points.add(element_old);
				readd_home_point();
				write_map_points();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	private void read_map_points()
	{
		deserialize_map_points();
	}

	static void write_map_points()
	{
		if (map_points != null)
		{
			serialize_map_points();
		}
	}

	private static void serialize_map_points()
	{
		FileOutputStream fos;
		try
		{
			fos = new FileOutputStream(NAVIT_DATA_SHARE_DIR + Navit_DEST_FILENAME);
			// openFileOutput(CFG_FILENAME_PATH + Navit_DEST_FILENAME, Context.MODE_PRIVATE);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(map_points);
			oos.close();
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private void export_map_points_to_sdcard()
	{
		String orig_file = NAVIT_DATA_SHARE_DIR + Navit_DEST_FILENAME;
		String dest_file_dir = getCFGpath() + "../export/";

		try
		{
			File dir = new File(dest_file_dir);
			dir.mkdirs();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		try
		{
			File source = new File(orig_file);
			File destination = new File(dest_file_dir + Navit_DEST_FILENAME);

			if (source.exists())
			{
				FileInputStream fi = new FileInputStream(source);
				FileOutputStream fo = new FileOutputStream(destination);
				FileChannel src = fi.getChannel();
				FileChannel dst = fo.getChannel();
				dst.transferFrom(src, 0, src.size());
				src.close();
				dst.close();
				fi.close();
				fo.close();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private void import_map_points_from_sdcard()
	{
		String orig_file = NAVIT_DATA_SHARE_DIR + Navit_DEST_FILENAME;
		String dest_file_dir = getCFGpath() + "../export/";

		try
		{
			File source = new File(dest_file_dir + Navit_DEST_FILENAME);
			File destination = new File(orig_file);

			if (source.exists())
			{
				FileInputStream fi = new FileInputStream(source);
				FileOutputStream fo = new FileOutputStream(destination);
				FileChannel src = fi.getChannel();
				FileChannel dst = fo.getChannel();
				dst.transferFrom(src, 0, src.size());
				src.close();
				dst.close();
				fi.close();
				fo.close();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		read_map_points();
	}

	//@SuppressWarnings("unchecked")
	private void deserialize_map_points()
	{
		// if (Navit.METHOD_DEBUG) Navit.my_func_name(0);

		FileInputStream fis = null;
		ObjectInputStream ois = null;

		try
		{
			fis = new FileInputStream(NAVIT_DATA_SHARE_DIR + Navit_DEST_FILENAME);
			// openFileInput(CFG_FILENAME_PATH + Navit_DEST_FILENAME);
			ois = new ObjectInputStream(fis);
			map_points = (ArrayList<Navit_Point_on_Map>) ois.readObject();
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
			map_points = new ArrayList<>();
		}
		catch (IOException e)
		{
			e.printStackTrace();
			map_points = new ArrayList<>();
		}
		catch (ClassNotFoundException e)
		{
			e.printStackTrace();
			map_points = new ArrayList<>();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			map_points = new ArrayList<>();
		}

		try
		{
			if (ois != null)
			{
				ois.close();
			}
		}
		catch (Exception e)
		{
		}

		try
		{
			if (fis != null)
			{
				ois.close();
			}
		}
		catch (Exception e)
		{
		}

		//		for (int j = 0; j < map_points.size(); j++)
		//		{
		//			System.out.println("####******************" + j + ":" + map_points.get(j).point_name);
		//		}

		// if (Navit.METHOD_DEBUG) Navit.my_func_name(1);

	}

	static void remember_destination_xy(String name, int x, int y)
	{
		// i=1 -> pixel a,b (x,y)      -> geo   string "lat(float):lng(float)"
		// i=2 -> geo   a,b (lat,lng)  -> pixel string "x(int):y(int)"
		String lat_lon = NavitGraphics.CallbackGeoCalc(1, (x + NavitGraphics.mCanvasWidth_overspill) * NavitGraphics.Global_dpi_factor, (y + NavitGraphics.mCanvasHeight_overspill) * NavitGraphics.Global_dpi_factor);
		try
		{
			String[] tmp = lat_lon.split(":", 2);
			//System.out.println("tmp=" + lat_lon);
			float lat = Float.parseFloat(tmp[0]);
			float lon = Float.parseFloat(tmp[1]);
			//System.out.println("ret=" + lat_lon + " lat=" + lat + " lon=" + lon);
			remember_destination(name, lat, lon);
		}
		catch (Exception e)
		{
			e.printStackTrace();

		}
	}

	static void remember_destination(String name, String lat, String lon)
	{
		try
		{
			//System.out.println("22 **## " + name + " " + lat + " " + lon + " ##**");
			remember_destination(name, Float.parseFloat(lat), Float.parseFloat(lon));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private static void remember_destination(String name, float lat, float lon)
	{
		//System.out.println("11 **## " + name + " " + lat + " " + lon + " ##**");
		Navit_Point_on_Map t = new Navit_Point_on_Map();
		t.point_name = name;
		t.lat = lat;
		t.lon = lon;
		add_map_point(t);
	}

	static void destination_set()
	{
		// status = "destination set"
		NavitGraphics.navit_route_status = 1;
	}

	static Boolean check_dup_destination(Navit_Point_on_Map element)
	{
		Boolean ret = false;
		Navit_Point_on_Map t;
		for (int i = 0; i < map_points.size(); i++)
		{
			t = map_points.get(i);
			if (t.addon == null)
			{
				if ((t.point_name.equals(element.point_name)) && (t.lat == element.lat) && (t.lon == element.lon) && (element.addon == null))
				{
					return true;
				}
			}
			else
			{
				if ((t.point_name.equals(element.point_name)) && (t.lat == element.lat) && (t.lon == element.lon) && (t.addon.equals(element.addon)))
				{
					return true;
				}
			}
		}
		return ret;
	}

	private static int get_destination_pos(Navit_Point_on_Map element)
	{
		int ret = -1;
		Navit_Point_on_Map t;
		for (int i = 0; i < map_points.size(); i++)
		{
			t = map_points.get(i);
			if (t.addon == null)
			{
				if ((t.point_name.equals(element.point_name)) && (t.lat == element.lat) && (t.lon == element.lon) && (element.addon == null))
				{
					return i;
				}
			}
			else
			{
				if ((t.point_name.equals(element.point_name)) && (t.lat == element.lat) && (t.lon == element.lon) && (t.addon.equals(element.addon)))
				{
					return i;
				}
			}
		}
		return ret;
	}

	private static boolean is_home_element(Navit_Point_on_Map element)
	{
		int home_id = find_home_point();
		if (home_id != -1)
		{
			Navit_Point_on_Map home_element = map_points.get(home_id);

			if ((home_element.point_name.equals(element.point_name)) && (home_element.lat == element.lat) && (home_element.lon == element.lon))
			{
				return true;
			}

		}

		return false;
	}

	static NavitSpeech2 get_speech_object()
	{
		System.out.println("get_speech_object");
		return NSp;
	}

	static NavitVehicle get_vehicle_object()
	{
		System.out.println("get_vehicle_object");
		return NV;
	}

	//kan vanuit C (android_graphics) ook op het navit object aangeroepen worden
	//en hoeft dan niet meer static te zijn
	@SuppressWarnings("unused")
	static NavitGraphics get_graphics_object_by_name(String name)
	{
		System.out.println("get_graphics_object_by_name:*" + name + "*");

		if (name.equals("type:map-main"))
		{
			System.out.println("map-main");
			return sNavitObject.NG__map_main;
		}
		else
		{
			System.out.println("vehicle");
			return sNavitObject.NG__vehicle;
		}
	}

	static Handler vehicle_handler = new Handler()
	{
		public void handleMessage(Message msg)
		{
			//System.out.println("vehicle_handler:handleMessage:JTHREAD ID=" + Thread.currentThread().getId());
			//System.out.println("vehicle_handler:handleMessage:THREAD ID=" + NavitGraphics.GetThreadId());

			switch (msg.what)
			{
			case 1:
				Location l = new Location("Network");
				l.setLatitude(msg.getData().getFloat("lat"));
				l.setLongitude(msg.getData().getFloat("lng"));
				l.setBearing(msg.getData().getFloat("b"));
				l.setSpeed(0.8f);
				NavitVehicle.set_mock_location__fast(l);
				break;
			case 2:
				if (NavitVehicle.update_location_in_progress)
				{
				}
				else
				{
					NavitVehicle.update_location_in_progress = true;
					NavitVehicle.VehicleCallback2(NavitVehicle.last_location);
					NavitVehicle.update_location_in_progress = false;
				}
				break;
			}
		}
	};

	private static boolean allow_use_index_search()
	{
		boolean ret = false;

		// MAP_FILENAME_PATH
		File folder = new File(sNavitMapDirectory);
		File[] listOfFiles = folder.listFiles();
		File idx;
		File md5_file;
		FileOutputStream fos;

		int files = 0;
		int file_without_index = 0;

		for (File file : listOfFiles)
		{
			if (file.isFile())
			{
				// System.out.println("FFNN:" + file.getName());

				if (file.getName().endsWith(".bin"))
				{
					if (!file.getName().equals("borders.bin"))
					{
						if (!file.getName().equals("coastline.bin"))
						{
							files++;
							idx = new File(folder, file.getName() + ".idx");
							if (!idx.exists()) {
								file_without_index++;

								String servername = "";
								try
								{
									servername = NavitMapDownloader.is_in_cat_file_disk_name(file.getName()).split(":", 2)[1];
								}
								catch (Exception ee)
								{
								}

								if (!servername.equals(""))
								{
									// index for this map is missing. hack MD5 file so we can download it again
									md5_file = new File(sNavitObject.getMapMD5path() + "/" + servername + ".md5");

									System.out.println("FFNN:hack MD5:" + md5_file.getAbsolutePath() + " s=" + servername);

									if ((md5_file.exists()) && (md5_file.canWrite()))
									{
										try
										{
											fos = new FileOutputStream(md5_file);
											fos.write(65);
											fos.write(65);
											fos.write(65);
											fos.close();
										}
										catch (Exception e1)
										{
											// System.out.println("FFNN:EEEEEEEEEEEEEE");
										}
									}
								}
							}
						}
					}
				}

				if (file.getName().endsWith(".bin.idx"))
				{
					ret = true;
				}
			}
		}

		if (files > 0)
		{
			if (file_without_index > 0)
			{
				Navit_index_on_but_no_idx_files = true;
			}
			else
			{
				Navit_index_on_but_no_idx_files = false;
			}
		}

		return ret;
	}

	private void sendEmail(String recipient, String subject, String message)
	{
		try
		{
			final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
			emailIntent.setType("plain/text");
			if (recipient != null) emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[] { recipient });
			if (subject != null) emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
			if (message != null) emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, message);

			startActivity(Intent.createChooser(emailIntent, Navit.get_text("Send feedback via email ...")));

		}
		catch (ActivityNotFoundException e)
		{
			// cannot send email for some reason
		}
	}

	void sendEmailWithAttachment(Context c, final String recipient, final String subject, final String message, final String full_file_name, final String full_file_name_suppl)
	{
		try
		{
			Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", recipient, null));
			emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
			ArrayList<Uri> uris = new ArrayList<>();
			uris.add(Uri.parse("file://" + full_file_name));
			try
			{
				if (new File(full_file_name_suppl).length() > 0)
				{
					uris.add(Uri.parse("file://" + full_file_name_suppl));
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			List<ResolveInfo> resolveInfos = getPackageManager().queryIntentActivities(emailIntent, 0);
			List<LabeledIntent> intents = new ArrayList<>();

			if (resolveInfos.size() != 0)
			{
				for (ResolveInfo info : resolveInfos)
				{
					Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
					System.out.println("email:" + "comp=" + info.activityInfo.packageName + " " + info.activityInfo.name);
					intent.setComponent(new ComponentName(info.activityInfo.packageName, info.activityInfo.name));
					intent.putExtra(Intent.EXTRA_EMAIL, new String[] { recipient });
					if (subject != null) intent.putExtra(Intent.EXTRA_SUBJECT, subject);
					if (message != null) intent.putExtra(Intent.EXTRA_TEXT, message);
					intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
					intents.add(new LabeledIntent(intent, info.activityInfo.packageName, info.loadLabel(getPackageManager()), info.icon));
				}
				Intent chooser = Intent.createChooser(intents.remove(intents.size() - 1), Navit.get_text("Send email with attachments"));
				chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, intents.toArray(new LabeledIntent[0]));
				startActivity(chooser);
			}
			else
			{
				System.out.println("email:" + "No Email App found");
				new AlertDialog.Builder(c).setMessage(Navit.get_text("No Email App found")).setPositiveButton(Navit.get_text("Ok"), null).show();
			}

			//			final Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", recipient, null));
			//			if (recipient != null) emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[] { recipient });
			//			if (subject != null) emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
			//			if (message != null) emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, message);
			//			if (full_file_name != null)
			//			{
			//				emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + full_file_name));
			//				//ArrayList<Uri> uris = new ArrayList<>();
			//				//uris.add(Uri.parse("file://" + full_file_name));
			//				//emailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris); //ArrayList<Uri> of attachment Uri's
			//			}
			//
			//			List<ResolveInfo> resolveInfos = getPackageManager().queryIntentActivities(emailIntent, 0);
			//			if (resolveInfos.size() != 0)
			//			{
			//				String packageName = resolveInfos.get(0).activityInfo.packageName;
			//				String name = resolveInfos.get(0).activityInfo.name;
			//
			//				emailIntent.setAction(Intent.ACTION_SEND);
			//				emailIntent.setComponent(new ComponentName(packageName, name));
			//
			//				System.out.println("email:" + "comp=" + packageName + " " + name);
			//
			//				startActivity(emailIntent);
			//			}
			//			else
			//			{
			//				System.out.println("email:" + "No Email App found");
			//				new AlertDialog.Builder(c).setMessage(Navit.get_text("No Email App found")).setPositiveButton(Navit.get_text("Ok"), null).show();
			//			}

		}
		catch (ActivityNotFoundException e)
		{
			// cannot send email for some reason
		}
	}


	public void onBackPressed()
	{
		// do something on back.
		//System.out.println("no back key!");
		// super.onBackPressed();
		// !!disable the back key otherwise!!

		if (Navit_doubleBackToExitPressedOnce)
		{

			try
			{
				if (wl_navigating != null)
				{
					//if (wl_navigating.isHeld())
					//{
					wl_navigating.release();
					Log.e(TAG, "WakeLock Nav: release 1");
					//}
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}

			// super.onBackPressed(); --> this would only put the app in background
			// --------
			// exit the app here
			this.onPause();
			this.onStop();
			this.exit();
			// --------
			return;
		}

		try
		{
			Navit_doubleBackToExitPressedOnce = true;
			Toast.makeText(this, Navit.get_text("Please press BACK again to Exit"), Toast.LENGTH_SHORT).show();

			new Handler().postDelayed(new Runnable()
			{
				@Override
				public void run()
				{
					Navit_doubleBackToExitPressedOnce = false;
				}
			}, NAVIT_BACKBUTTON_TO_EXIT_TIME);
		}
		catch (Exception e)
		{
		}

		try
		{
			// if bottom bar is up, animate it down on "back" press
			animate_bottom_bar_down();
		}
		catch (Exception e)
		{
		}

	}

	//	public void openOptionsMenu_wrapper()
	//	{
	//		//openOptionsMenu();
	//
	//		prepare_emu_options_menu();
	//		NavitGraphics.emu_menu_view.set_adapter(EmulatedMenuView.MenuItemsList2, EmulatedMenuView.MenuItemsIdMapping2);
	//		NavitGraphics.emu_menu_view.bringToFront();
	//		NavitGraphics.emu_menu_view.setVisibility(View.VISIBLE);
	//	}

	private static String logHeap(Class clazz)
	{
		Double allocated = (double) Debug.getNativeHeapAllocatedSize() / (double) (1048576);
		Double sum_size = Debug.getNativeHeapSize() / 1048576.0;
		Double free = Debug.getNativeHeapFreeSize() / 1048576.0;
		DecimalFormat df = new DecimalFormat();
		df.setMaximumFractionDigits(2);
		df.setMinimumFractionDigits(2);

		// Log.d(TAG, "MemMem:DEBUG: =================================");
		Log.d(TAG, "MemMem:DEBUG:heap native: allc " + df.format(allocated) + "MB sum=" + df.format(sum_size) + "MB (" + df.format(free) + "MB free) in [" + clazz.getName().replaceAll("com.zoffcc.applications.zanavi.", "") + "]");
		Log.d(TAG, "MemMem:DEBUG:java memory: allc: " + df.format(Double.valueOf(Runtime.getRuntime().totalMemory() / 1048576)) + "MB sum=" + df.format(Double.valueOf(Runtime.getRuntime().maxMemory() / 1048576)) + "MB (" + df.format(Double.valueOf(Runtime.getRuntime().freeMemory() / 1048576)) + "MB free)");

		calcAvailableMemory();

		String mem_type = "NATIVE";
		try
		{
			mem_type = "JAVA";
		}
		catch (Exception e)
		{
		}
		return ("" + df.format(allocated) + "/" + df.format(sum_size) + "(" + df.format(free) + ")" + ":" + df.format(Double.valueOf(Runtime.getRuntime().totalMemory() / 1048576)) + "/" + df.format(Double.valueOf(Runtime.getRuntime().maxMemory() / 1048576)) + "(" + df.format(Double.valueOf(Runtime.getRuntime().freeMemory() / 1048576)) + ") " + mem_type);
	}

	public static String logHeap_for_batch(Class clazz)
	{
		try
		{
			Double allocated = (double) Debug.getNativeHeapAllocatedSize() / (double) (1048576);
			Double sum_size = Debug.getNativeHeapSize() / 1048576.0;
			Double free = Debug.getNativeHeapFreeSize() / 1048576.0;
			DecimalFormat df = new DecimalFormat();
			df.setMaximumFractionDigits(2);
			df.setMinimumFractionDigits(2);

			// Log.d(TAG, "MemMem:DEBUG: =================================");
			Log.d(TAG, "MemMem:DEBUG:heap native: allc " + df.format(allocated) + "MB sum=" + df.format(sum_size) + "MB (" + df.format(free) + "MB free) in [" + clazz.getName().replaceAll("com.zoffcc.applications.zanavi.", "") + "]");
			Log.d(TAG, "MemMem:DEBUG:java memory: allc: " + df.format(Double.valueOf(Runtime.getRuntime().totalMemory() / 1048576)) + "MB sum=" + df.format(Double.valueOf(Runtime.getRuntime().maxMemory() / 1048576)) + "MB (" + df.format(Double.valueOf(Runtime.getRuntime().freeMemory() / 1048576)) + "MB free)");

			// calcAvailableMemory();

			String mem_type = "NATIVE";
			try
			{
				mem_type = "JAVA";
			}
			catch (Exception e)
			{
			}
			// return ("" + df.format(allocated) + "/" + df.format(sum_size) + "(" + df.format(free) + ")" + ":" + df.format(Double.valueOf(Runtime.getRuntime().totalMemory() / 1048576)) + "/" + df.format(Double.valueOf(Runtime.getRuntime().maxMemory() / 1048576)) + "(" + df.format(Double.valueOf(Runtime.getRuntime().freeMemory() / 1048576)) + ") " + mem_type);
			return ("==MEM==:" + "J:" + ((double) (Runtime.getRuntime().totalMemory() / 1048576)) + ":" + ((double) (Runtime.getRuntime().maxMemory() / 1048576)) + ",N:" + allocated + ":" + sum_size);
		}
		catch (Exception e2)
		{
			return ("==MEM==:ERROR");
		}
	}

	private static long calcAvailableMemory()
	{
		try
		{
			long value = Runtime.getRuntime().maxMemory();
			String type;
			value = (value / 1024 / 1024) - (Runtime.getRuntime().totalMemory() / 1024 / 1024);
			type = "JAVA";
			Log.i(TAG, "avail.mem size=" + value + "MB, type=" + type);
			return value;
		}
		catch (Exception e)
		{
			return 0L;
		}
	}

	public void google_online_search_and_set_destination(String address_string)
	{
		// online googlemaps search
		// String addressInput = filter_bad_chars(address_string);
		try
		{
			List<Address> foundAdresses = Navit.Navit_Geocoder.getFromLocationName(address_string, 1); //Search addresses
			//System.out.println("found " + foundAdresses.size() + " results");
			//System.out.println("addr=" + foundAdresses.get(0).getLatitude() + " " + foundAdresses.get(0).getLongitude() + "" + foundAdresses.get(0).getAddressLine(0));

			int results_step = 0;
			Navit.Navit_Address_Result_Struct tmp_addr = new Navit_Address_Result_Struct();
			tmp_addr.result_type = "STR";
			tmp_addr.item_id = "0";
			tmp_addr.lat = (float) foundAdresses.get(results_step).getLatitude();
			tmp_addr.lon = (float) foundAdresses.get(results_step).getLongitude();
			tmp_addr.addr = "";

			String c_code = foundAdresses.get(results_step).getCountryCode();
			if (c_code != null)
			{
				tmp_addr.addr = tmp_addr.addr + foundAdresses.get(results_step).getCountryCode() + ",";
			}

			String p_code = foundAdresses.get(results_step).getPostalCode();
			if (p_code != null)
			{
				tmp_addr.addr = tmp_addr.addr + foundAdresses.get(results_step).getPostalCode() + " ";
			}

			if (foundAdresses.get(results_step).getMaxAddressLineIndex() > -1)
			{
				for (int addr_line = 0; addr_line < foundAdresses.get(results_step).getMaxAddressLineIndex(); addr_line++)
				{
					if (addr_line > 0) tmp_addr.addr = tmp_addr.addr + " ";
					tmp_addr.addr = tmp_addr.addr + foundAdresses.get(results_step).getAddressLine(addr_line);
				}
			}

			//			// DEBUG: clear route rectangle list
			//			NavitGraphics.route_rects.clear();
			//
			//			System.out.println("found:" + tmp_addr.addr + " " + tmp_addr.lat + " " + tmp_addr.lon);
			//
			//			try
			//			{
			//				Navit.remember_destination(tmp_addr.addr, tmp_addr.lat, tmp_addr.lon);
			//				// save points
			//				write_map_points();
			//			}
			//			catch (Exception e)
			//			{
			//				e.printStackTrace();
			//			}
			//
			//			if (NavitGraphics.navit_route_status == 0)
			//			{
			//				Toast.makeText(getApplicationContext(), Navit.get_text("setting destination to") + "\n" + tmp_addr.addr, Toast.LENGTH_SHORT).show(); //TRANS
			//				Navit.destination_set();
			//
			//				Message msg = new Message();
			//				Bundle b = new Bundle();
			//				b.putInt("Callback", 3);
			//				b.putString("lat", String.valueOf(tmp_addr.lat));
			//				b.putString("lon", String.valueOf(tmp_addr.lon));
			//				b.putString("q", tmp_addr.addr);
			//				msg.setData(b);
			//				NavitGraphics.callback_handler.sendMessage(msg);
			//			}
			//			else
			//			{
			//				Toast.makeText(getApplicationContext(), Navit.get_text("new Waypoint") + "\n" + tmp_addr.addr, Toast.LENGTH_SHORT).show(); //TRANS
			//				Message msg = new Message();
			//				Bundle b = new Bundle();
			//				b.putInt("Callback", 48);
			//				b.putString("lat", String.valueOf(tmp_addr.lat));
			//				b.putString("lon", String.valueOf(tmp_addr.lon));
			//				b.putString("q", tmp_addr.addr);
			//				msg.setData(b);
			//				NavitGraphics.callback_handler.sendMessage(msg);
			//			}

			route_wrapper(tmp_addr.addr, 0, 0, false, tmp_addr.lat, tmp_addr.lon, true);

			// ---------- DEBUG: write route to file ----------
			// ---------- DEBUG: write route to file ----------
			// ---------- DEBUG: write route to file ----------
			if (preferences.PREF_enable_debug_write_gpx)
			{
				write_route_to_gpx_file();
			}
			// ---------- DEBUG: write route to file ----------
			// ---------- DEBUG: write route to file ----------

			try
			{
				Navit.follow_button_on();
			}
			catch (Exception e2)
			{
				e2.printStackTrace();
			}

			show_geo_on_screen(tmp_addr.lat, tmp_addr.lon);

		}
		catch (Exception e)
		{
			e.printStackTrace();
			Toast.makeText(getApplicationContext(), Navit.get_text("google search API is not working at this moment, try offline search"), Toast.LENGTH_SHORT).show();
		}
	}

	private void result_set_destination(double lat, double lon, String addr)
	{
		try
		{
			int results_step = 0;
			Navit.Navit_Address_Result_Struct tmp_addr = new Navit_Address_Result_Struct();
			tmp_addr.result_type = "STR";
			tmp_addr.item_id = "0";
			tmp_addr.lat = (float) lat;
			tmp_addr.lon = (float) lon;
			tmp_addr.addr = addr;

			//			// DEBUG: clear route rectangle list
			//			NavitGraphics.route_rects.clear();
			//
			//			System.out.println("found:" + tmp_addr.addr + " " + tmp_addr.lat + " " + tmp_addr.lon);
			//
			//			try
			//			{
			//				Navit.remember_destination(tmp_addr.addr, tmp_addr.lat, tmp_addr.lon);
			//				// save points
			//				write_map_points();
			//			}
			//			catch (Exception e)
			//			{
			//				e.printStackTrace();
			//			}
			//
			//			if (NavitGraphics.navit_route_status == 0)
			//			{
			//				Toast.makeText(getApplicationContext(), Navit.get_text("setting destination to") + "\n" + tmp_addr.addr, Toast.LENGTH_SHORT).show(); //TRANS
			//				Navit.destination_set();
			//
			//				Message msg = new Message();
			//				Bundle b = new Bundle();
			//				b.putInt("Callback", 3);
			//				b.putString("lat", String.valueOf(tmp_addr.lat));
			//				b.putString("lon", String.valueOf(tmp_addr.lon));
			//				b.putString("q", tmp_addr.addr);
			//				msg.setData(b);
			//				NavitGraphics.callback_handler.sendMessage(msg);
			//			}
			//			else
			//			{
			//				Toast.makeText(getApplicationContext(), Navit.get_text("new Waypoint") + "\n" + tmp_addr.addr, Toast.LENGTH_SHORT).show(); //TRANS
			//				Message msg = new Message();
			//				Bundle b = new Bundle();
			//				b.putInt("Callback", 48);
			//				b.putString("lat", String.valueOf(tmp_addr.lat));
			//				b.putString("lon", String.valueOf(tmp_addr.lon));
			//				b.putString("q", tmp_addr.addr);
			//				msg.setData(b);
			//				NavitGraphics.callback_handler.sendMessage(msg);
			//			}

			route_wrapper(tmp_addr.addr, 0, 0, false, tmp_addr.lat, tmp_addr.lon, true);

			try
			{
				Navit.follow_button_on();
			}
			catch (Exception e2)
			{
				e2.printStackTrace();
			}

			// ---------- DEBUG: write route to file ----------
			// ---------- DEBUG: write route to file ----------
			// ---------- DEBUG: write route to file ----------
			if (preferences.PREF_enable_debug_write_gpx)
			{
				write_route_to_gpx_file();
			}
			// ---------- DEBUG: write route to file ----------
			// ---------- DEBUG: write route to file ----------

			show_geo_on_screen(tmp_addr.lat, tmp_addr.lon);

		}
		catch (Exception e)
		{
			e.printStackTrace();
			Toast.makeText(getApplicationContext(), Navit.get_text("google search API is not working at this moment, try offline search"), Toast.LENGTH_SHORT).show();
		}
	}

	private void open_voice_recog_screen()
	{
		Intent ii = new Intent(this, ZANaviVoiceInput.class);
		this.startActivityForResult(ii, Navit.ZANaviVoiceInput_id);
	}

	static void dim_screen_wrapper()
	{
		try
		{
			Message msg = Navit.Navit_progress_h.obtainMessage();
			Bundle b = new Bundle();
			msg.what = 20;
			msg.setData(b);
			Navit.Navit_progress_h.sendMessage(msg);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private static void show_status_bar_wrapper()
	{
		try
		{
			Message msg = Navit.Navit_progress_h.obtainMessage();
			Bundle b = new Bundle();
			msg.what = 27;
			msg.setData(b);
			Navit.Navit_progress_h.sendMessage(msg);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private static void hide_status_bar_wrapper()
	{
		try
		{
			Message msg = Navit.Navit_progress_h.obtainMessage();
			Bundle b = new Bundle();
			msg.what = 28;
			msg.setData(b);
			Navit.Navit_progress_h.sendMessage(msg);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	static void default_brightness_screen_wrapper()
	{
		try
		{
			Message msg = Navit.Navit_progress_h.obtainMessage();
			Bundle b = new Bundle();
			msg.what = 21;
			msg.setData(b);
			Navit.Navit_progress_h.sendMessage(msg);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private static void dim_screen()
	{
		try
		{
			WindowManager.LayoutParams params_wm = sAppWindow.getAttributes();
			// params_wm.flags = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
			params_wm.screenBrightness = 0.1f;
			sAppWindow.setAttributes(params_wm);
			//sAppWindow.addFlags(WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private static void default_brightness_screen()
	{
		try
		{
			WindowManager.LayoutParams params_wm = sAppWindow.getAttributes();
			// params_wm.flags = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
			params_wm.screenBrightness = -1f;
			sAppWindow.setAttributes(params_wm);
			//sAppWindow.clearFlags(WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private void donate_bitcoins()
	{
		try
		{
			Intent donate_activity = new Intent(this, ZANaviDonateActivity.class);
			// donate_activity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP );
			startActivity(donate_activity);
		}
		catch (Exception e)
		{
		}
	}

	private void donate()
	{
		try
		{
			Intent donate_activity = new Intent(this, ZANaviNormalDonateActivity.class);
			// donate_activity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP );
			startActivity(donate_activity);
		}
		catch (Exception e)
		{
		}
	}

	private static int debug_indent = -1;
	private static final String debug_indent_spaces = "                                                                                                                                                                                     ";

	// type: 0 -> enter, 1 -> leave, 2 .. n -> return(#n)
	static void my_func_name(int type)
	{
		int debug_indent2;

		// -- switch off !! --
		// -- switch off !! --
		debug_indent = 0;
		// -- switch off !! --
		// -- switch off !! --

		try
		{
			StackTraceElement[] a = Thread.currentThread().getStackTrace();
			if (type == 0)
			{
				//debug_indent++;
				debug_indent2 = debug_indent;
				//if (debug_indent2 > debug_indent_spaces.length())
				//{
				//	debug_indent2 = debug_indent_spaces.length() - 1;
				//}
				System.out.println("FUNJ:" + debug_indent_spaces.substring(0, Math.abs(2 * debug_indent2)) + "zanav:" + a[3].getClassName() + "." + a[3].getMethodName() + ":enter");
			}
			else if (type == 1)
			{
				debug_indent2 = debug_indent;
				//if (debug_indent2 > debug_indent_spaces.length())
				//{
				//	debug_indent2 = debug_indent_spaces.length() - 1;
				//}
				System.out.println("FUNJ:" + debug_indent_spaces.substring(0, Math.abs(2 * debug_indent2)) + "zanav:" + a[3].getClassName() + "." + a[3].getMethodName() + ":leave");
				//debug_indent--;
			}
			else
			{
				debug_indent2 = debug_indent;
				//if (debug_indent2 > debug_indent_spaces.length())
				//{
				//	debug_indent2 = debug_indent_spaces.length() - 1;
				//}
				System.out.println("FUNJ:" + debug_indent_spaces.substring(0, Math.abs(2 * debug_indent2)) + "zanav:" + a[3].getClassName() + "." + a[3].getMethodName() + ":return(" + type + ")");
				//debug_indent--;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	// type: 0 -> enter, 1 -> leave, 2 .. n -> return(#n)
	static void my_func_name(int type, String msg)
	{
		int debug_indent2;

		// -- switch off !! --
		// -- switch off !! --
		debug_indent = 0;
		// -- switch off !! --
		// -- switch off !! --

		try
		{
			StackTraceElement[] a = Thread.currentThread().getStackTrace();
			if (type == 0)
			{
				//debug_indent++;
				debug_indent2 = debug_indent;
				//if (debug_indent2 > debug_indent_spaces.length())
				//{
				//	debug_indent2 = debug_indent_spaces.length() - 1;
				//}
				System.out.println("FUNJ:" + debug_indent_spaces.substring(0, Math.abs(2 * debug_indent2)) + "zanav:" + a[3].getClassName() + "." + a[3].getMethodName() + ":enter" + ":" + msg);
			}
			else if (type == 1)
			{
				debug_indent2 = debug_indent;
				//if (debug_indent2 > debug_indent_spaces.length())
				//{
				//	debug_indent2 = debug_indent_spaces.length() - 1;
				//}
				System.out.println("FUNJ:" + debug_indent_spaces.substring(0, Math.abs(2 * debug_indent2)) + "zanav:" + a[3].getClassName() + "." + a[3].getMethodName() + ":leave" + ":" + msg);
				//debug_indent--;
			}
			else
			{
				debug_indent2 = debug_indent;
				//if (debug_indent2 > debug_indent_spaces.length())
				//{
				//	debug_indent2 = debug_indent_spaces.length() - 1;
				//}
				System.out.println("FUNJ:" + debug_indent_spaces.substring(0, Math.abs(2 * debug_indent2)) + "zanav:" + a[3].getClassName() + "." + a[3].getMethodName() + ":return(" + type + ")" + ":" + msg);
				//debug_indent--;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private static void write_route_to_gpx_file()
	{
		final Thread write_route_to_gpx_file_001 = new Thread()
		{
			int wait = 1;
			int count = 0;
			final int max_count = 300; // wait 2 minutes for route to be calculated

			@Override
			public void run()
			{
				while (wait == 1)
				{
					try
					{
						if ((NavitGraphics.navit_route_status == 17) || (NavitGraphics.navit_route_status == 33))
						{
							Message msg = new Message();
							Bundle b = new Bundle();
							b.putInt("Callback", 96);
							String date = new SimpleDateFormat("yyyy-MM-dd_HHmmss", Locale.GERMAN).format(new Date());
							String filename = sNavitObject.getNAVIT_DATA_DEBUG_DIR() + "zanavi_route_" + date + ".gpx";
							b.putString("s", filename);
							System.out.println("Debug:" + "file=" + filename);
							msg.setData(b);
							callback_handler_55.sendMessage(msg);

							Message msg7 = Navit_progress_h.obtainMessage();
							Bundle b7 = new Bundle();
							msg7.what = 2; // long Toast message
							b7.putString("text", Navit.get_text("saving route to GPX-file") + " " + filename); //TRANS
							msg7.setData(b7);
							Navit_progress_h.sendMessage(msg7);

							wait = 0;
						}
						else
						{
							wait = 1;
						}

						count++;
						if (count > max_count)
						{
							wait = 0;

							Message msg7 = Navit_progress_h.obtainMessage();
							Bundle b7 = new Bundle();
							msg7.what = 2; // long Toast message
							b7.putString("text", Navit.get_text("saving route to GPX-file failed")); //TRANS
							msg7.setData(b7);
							Navit_progress_h.sendMessage(msg7);
						}
						else
						{
							Thread.sleep(400);
						}
					}
					catch (Exception e)
					{
					}
				}
			}
		};
		write_route_to_gpx_file_001.start();
	}

	private void convert_gpx_file_real(String gpx_file)
	{
		File tt2 = new File(gpx_file);
		preferences.PREF_last_selected_dir_gpxfiles = tt2.getParent();
		Log.e(TAG, "last_selected_dir_gpxfiles " + preferences.PREF_last_selected_dir_gpxfiles);
		setPrefs_selected_gpx_dir();

		String out_ = sNavitMapDirectory + "/gpxtracks.txt";
		Log.e(TAG, "onActivityResult 002 " + gpx_file + " " + out_);
		MainFrame.do_conversion(gpx_file, out_);

		// draw map no-async
		sendCallBackMessage(64);
	}

	static boolean want_tunnel_extrapolation()
	{
		if ((!isGPSFix) && (pos_is_underground == true) && (NavitGraphics.CallbackDestinationValid2() > 0))
		{
			// gps fix is lost
			//	and
			// our position is underground
			//	and
			// we have a destination set
			//
			// --> we want tunnel extrapolation

			// this assumes at least some last gps fix put it on the underground segment -- jdg --


			return true;
		}

		return false;
	}

	static void applySharedTheme(Activity act, int Theme_id)
	{
		act.setTheme(Theme_id);
	}

	private final com.zoffcc.applications.zanavi_msg.ZListener.Stub zclientListener = new com.zoffcc.applications.zanavi_msg.ZListener.Stub()
	{
		@Override
		public String handleUpdated(String data) {
			// Log.i("NavitPlugin", "update from Plugin=" + data);
			return "Navit says:\"PONG\"";
		}
	};

	private static final Object sync_plugin_send = new Object();

	private static void send_data_to_plugin_bg(final int msg_cat, final String data)
	{
		// send data to plugin (plugin will send to server) in another task! --------------------------
		new AsyncTask<Void, Void, String>()
		{
			@Override
			protected String doInBackground(Void... params)
			{
				synchronized (sync_plugin_send)
				{
					try
					{
						if (plugin_api == null)
						{
							try
							{
								Thread.sleep(3000); // wait until the service is bound
							}
							catch (Exception e)
							{
							}
						}

						String response = plugin_api.getResult(PLUGIN_MSG_ID, msg_cat, data);

					}
					catch (RemoteException e)
					{
						// Log.e("NavitPlugin", "Failed(1) to send msg to plugin:cat=" + PLUGIN_MSG_CAT_zanavi_version + " data=" + data, e);
					}
					catch (Exception e)
					{
						// Log.e("NavitPlugin", "Failed(2) to send msg to plugin:cat=" + PLUGIN_MSG_CAT_zanavi_version + " data=" + data, e);
					}
				}
				return "";
			}

			@Override
			protected void onPostExecute(String msg)
			{

			}
		}.execute(null, null, null);
		// send data to plugin (plugin will send to server) in another task! --------------------------
	}

	private final ServiceConnection serviceConnection = new ServiceConnection()
	{
		@Override
		public void onServiceConnected(ComponentName name, IBinder service)
		{
			Log.i("NavitPlugin", "Service connection established");

			// that's how we get the client side of the IPC connection
			plugin_api = ZanaviCloudApi.Stub.asInterface(service);
			try
			{
				plugin_api.addListener(zclientListener);
			}
			catch (RemoteException e)
			{
				Log.e("NavitPlugin", "Failed(1) to add listener", e);
			}
			catch (Exception e)
			{
				Log.e("NavitPlugin", "Failed(2) to add listener", e);
			}

			send_installed_maps_to_plugin();
			send_data_to_plugin_bg(PLUGIN_MSG_CAT_zanavi_version, Navit.NavitAppVersion);
		}

		@Override
		public void onServiceDisconnected(ComponentName name)
		{
			plugin_api = null;
			Log.i("NavitPlugin", "Service connection closed");
		}
	};

	/**
	 * send installed maps and download date/time (time up to minute) in UTC to plugin
	 */
	static void send_installed_maps_to_plugin()
	{
		try
		{
			/*
			 * { "201502032311":"austria.bin","201502032312":"germany.bin" }
			 */

			String data = "";

			// dirty hardcode JSON data struct
			final String maps_and_timestamp_data_start = "{ \"";
			final String maps_and_timestamp_data_sep1 = "\":\"";
			final String maps_and_timestamp_data_sep2 = "\",\"";
			final String maps_and_timestamp_data_end = "\" }";

			NavitMapDownloader.init_cat_file_maps_timestamps();

			data = data + maps_and_timestamp_data_start;

			String map_name = "";
			Iterator<String> k = NavitMapDownloader.map_catalogue_date.listIterator();
			while (k.hasNext())
			{
				map_name = k.next();
				data = data + map_name.split(":", 2)[1];
				data = data + maps_and_timestamp_data_sep1;
				data = data + map_name.split(":", 2)[0];
				if (k.hasNext())
				{
					data = data + maps_and_timestamp_data_sep2;
				}
			}
			data = data + maps_and_timestamp_data_end;

			// System.out.println("PLUGIN:MAPS:" + data);

			send_data_to_plugin_bg(PLUGIN_MSG_CAT_installed_maps, data);
		}
		catch (Exception e)
		{
		}
	}

	static boolean have_maps_installed()
	{
		int count_maps_installed = NavitMapDownloader.cat_file_maps_have_installed_any();
		return count_maps_installed != 0;
	}

	/**
	 * auto start map download/update
	 * 
	 * @param map_name
	 *            mapfilename with ending ".bin"
	 */
	private void auto_start_update_map(String map_name)
	{

		int count = NavitMapDownloader.z_OSM_MAPS.length;
		Navit.download_map_id = -1;
		for (int i = 0; i < count; i++)
		{
			if (!NavitMapDownloader.z_OSM_MAPS[i].is_continent)
			{
				// System.out.println("mmm1=" + NavitMapDownloader.z_OSM_MAPS[i].url);
				if (NavitMapDownloader.z_OSM_MAPS[i].url.equals(map_name))
				{
					Navit.download_map_id = i;
					System.out.println("mMM2=" + NavitMapDownloader.z_OSM_MAPS[i].map_name);
				}
			}
		}

		// show the map download progressbar, and download the map
		if (Navit.download_map_id > -1)
		{
			// new method in service
			Message msg = progress_handler.obtainMessage();
			Bundle b = new Bundle();
			msg.what = 22;
			progress_handler.sendMessage(msg);
		}
	}

	void show_case_001()
	{
		//		try
		//		{
		//			new ShowcaseView.Builder(this).setTarget(new ViewTarget(R.id.no_maps_button, this)).setContentTitle("").setContentText("").singleShot(Navit.SHOWCASEVIEW_ID_001).build();
		//		}
		//		catch (Exception e1)
		//		{
		//		}
		//		catch (NoSuchMethodError e2)
		//		{
		//		}
		//		catch (NoClassDefFoundError e3)
		//		{
		//		}
	}

	static void set_zoom_level(int want_zoom_level)
	{
		try
		{
			Bundle b = new Bundle();
			Message msg = new Message();
			b.putInt("Callback", 33);
			b.putString("s", Integer.toString(want_zoom_level));
			msg.setData(b);
			callback_handler_55.sendMessage(msg);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	static void set_zoom_level_no_draw(int want_zoom_level)
	{
		try
		{
			Bundle b = new Bundle();
			Message msg = new Message();
			b.putInt("Callback", 105);
			b.putString("s", Integer.toString(want_zoom_level));
			msg.setData(b);
			callback_handler_55.sendMessage(msg);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	static void draw_map()
	{
		try
		{
			// draw map no-async
			sNavitObject.sendCallBackMessage(64);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private final static int bottom_bar_snap_duration = 190;

	private void animate_bottom_bar_up()
	{
		final FrameLayout a = findViewById(R.id.bottom_bar_slide);
		TranslateAnimation animation = new TranslateAnimation(0, 0, 0, -cur_y_margin_bottom_bar_touch);
		animation.setDuration(bottom_bar_snap_duration); // animation duration
		animation.setFillAfter(true);
		animation.setFillEnabled(true);
		animation.setRepeatCount(0); // animation repeat count
		animation.setAnimationListener(new AnimationListener()
		{
			@Override
			public void onAnimationStart(Animation animation)
			{
			}

			@Override
			public void onAnimationRepeat(Animation animation)
			{
			}

			@Override
			public void onAnimationEnd(Animation animation)
			{
				cur_y_margin_bottom_bar_touch = 0;
				RelativeLayout.LayoutParams relativeParams = (RelativeLayout.LayoutParams) a.getLayoutParams();
				relativeParams.setMargins(0, (int) cur_y_margin_bottom_bar_touch, 0, 0); // left, top, right, bottom
				a.setLayoutParams(relativeParams);
				a.requestLayout();

				TranslateAnimation anim = new TranslateAnimation(0, 0, 0, 0);
				anim.setFillAfter(true);
				anim.setFillEnabled(true);
				anim.setDuration(1);
				a.startAnimation(anim);
			}
		});
		a.startAnimation(animation);
	}

	static void animate_bottom_bar_down()
	{
		final FrameLayout a = sNavitObject.findViewById(R.id.bottom_bar_slide);

		// System.out.println("FRAG:animate_bottom_bar_down:014");

		// set bottom end positon correctly??
		bottom_y_margin_bottom_bar_touch = Navit.map_view_height + Navit.actionBarHeight + bottom_bar_px - Navit.bottom_bar_slider_shadow_px;

		final int move_by = (int) (bottom_y_margin_bottom_bar_touch - cur_y_margin_bottom_bar_touch);
		TranslateAnimation animation = new TranslateAnimation(0, 0, 0, move_by); //  new TranslateAnimation(xFrom,xTo, yFrom,yTo)
		animation.setDuration(bottom_bar_snap_duration); // animation duration
		animation.setFillAfter(true);
		animation.setFillEnabled(true);
		animation.setRepeatCount(0); // animation repeat count
		animation.setAnimationListener(new AnimationListener()
		{

			@Override
			public void onAnimationStart(Animation animation)
			{
			}

			@Override
			public void onAnimationRepeat(Animation animation)
			{
			}

			@Override
			public void onAnimationEnd(Animation animation)
			{
				// set bottom end positon correctly??
				bottom_y_margin_bottom_bar_touch = Navit.map_view_height + Navit.actionBarHeight + bottom_bar_px - Navit.bottom_bar_slider_shadow_px;

				cur_y_margin_bottom_bar_touch = bottom_y_margin_bottom_bar_touch;
				RelativeLayout.LayoutParams relativeParams = (RelativeLayout.LayoutParams) a.getLayoutParams();
				relativeParams.setMargins(0, (int) bottom_y_margin_bottom_bar_touch, 0, 0); // left, top, right, bottom
				a.setLayoutParams(relativeParams);
				a.requestLayout();

				TranslateAnimation anim = new TranslateAnimation(0, 0, 0, 0);
				anim.setFillAfter(true);
				anim.setFillEnabled(true);
				anim.setDuration(1);
				a.startAnimation(anim);

				// remove roadbook fragment -----------
				try
				{
					if (sNavitObject.road_book != null)
					{
						FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
						// System.out.println("FRAG:dettach:002");
						fragmentTransaction.detach(sNavitObject.road_book);
						fragmentTransaction.remove(sNavitObject.road_book).commit();
						sNavitObject.road_book = null;
					}
				}
				catch (Exception ef)
				{
				}
				// remove roadbook fragment -----------

			}
		});
		a.startAnimation(animation);
	}

	synchronized static int find_max_font_size_for_height(String sample_text, int height, int max_font_size, int padding_in_dp)
	{
		int bh = 0;
		Paint p = new Paint();
		Rect bounds = new Rect();
		p.setTextSize(max_font_size);
		// preferences.measureText(s);
		p.getTextBounds(sample_text, 0, sample_text.length(), bounds);

		int ret_font_size = max_font_size;

		int loop_counter_max = 400;
		int loop_counter = 0;
		int padding_in_px = 0;
		if (padding_in_dp > 0)
		{
			padding_in_px = NavitGraphics.dp_to_px(padding_in_dp);
		}

		bh = bounds.height();
		//System.out.println("bh(1)=" + bh);
		while ((bh + padding_in_px) > height)
		{
			loop_counter++;
			if (loop_counter > loop_counter_max)
			{
				break;
			}

			ret_font_size--;
			p.setTextSize(ret_font_size);
			// preferences.measureText(s);
			p.getTextBounds(sample_text, 0, sample_text.length(), bounds);
			bh = bounds.height();
		}

		return ret_font_size;
	}

	synchronized static int find_max_letters_for_width_and_fontsize(String max_length_text, int width, int max_font_size, int padding_in_dp)
	{
		int ret_max_letters = 10;

		int padding_in_px = 0;
		if (padding_in_dp > 0)
		{
			//System.out.println("aaa2:l:1:x:" + padding_in_dp);
			padding_in_px = NavitGraphics.dp_to_px(padding_in_dp);
			//System.out.println("aaa2:l:1:x:2=" + padding_in_dp + " " + padding_in_px + " " + NavitGraphics.dp_to_px(padding_in_dp) + " " + NavitGraphics.Global_dpi_factor_better);
		}
		//System.out.println("aaa2:l:1:x:1=" + padding_in_px);

		int c = 0;
		Paint p = new Paint();
		p.setTextSize(max_font_size);

		String s = max_length_text;

		if ((s == null) || (s.equals("")))
		{
			s = "Mlox3miwoMlox3miwoMlox3miwoMlox3miwoMlox3miwoMlox3miwoMlox3miwoMlox3miwoMlox3miwoMlox3miwo";
		}

		int l = s.length();
		ret_max_letters = l;
		float mt = p.measureText(s);
		//System.out.println("aaa2:l:1:" + l + " " + s);
		//System.out.println("aaa2:l:22:." + mt + " padding_in_px=" + padding_in_px + " " + width);

		while (mt + padding_in_px > width)
		{
			//System.out.println("aaa2:l:22:+" + mt + " " + padding_in_px + " " + width);

			c++;
			if (c == l)
			{
				// no more letters
				//System.out.println("aaa2:l:1:no more letters:" + l + " " + s);
				break;
			}
			s = s.substring(0, s.length() - 1);
			mt = p.measureText(s);
			ret_max_letters--;
		}

		//System.out.println("aaa2:l:1:res=" + ret_max_letters);
		return ret_max_letters;
	}

	synchronized static int find_max_font_size_for_width(String sample_text, int width, int max_font_size, int padding_in_dp)
	{
		int bh = 0;
		Paint p = new Paint();
		Rect bounds = new Rect();
		p.setTextSize(max_font_size);
		p.getTextBounds(sample_text, 0, sample_text.length(), bounds);

		int ret_font_size = max_font_size;

		int loop_counter_max = 400;
		int loop_counter = 0;
		int padding_in_px = 0;
		if (padding_in_dp > 0)
		{
			padding_in_px = NavitGraphics.dp_to_px(padding_in_dp);
		}

		bh = bounds.width();
		while ((bh + padding_in_px) > width)
		{
			loop_counter++;
			if (loop_counter > loop_counter_max)
			{
				break;
			}

			ret_font_size--;
			p.setTextSize(ret_font_size);
			p.getTextBounds(sample_text, 0, sample_text.length(), bounds);
			bh = bounds.width();
			// mt = preferences.measureText(s);
		}

		return ret_font_size;
	}

	private static int last_orientation = Configuration.ORIENTATION_LANDSCAPE;

	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
		super.onConfigurationChanged(newConfig);

		if (last_orientation != newConfig.orientation)
		{
			// Checks the orientation of the screen
			if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE)
			{
				// setContentView(R.layout.main_layout);

				// -- bottom bar --
				int h = NavitGraphics.mCanvasHeight;
				try
				{
					int h001;
					android.view.ViewGroup.LayoutParams lp001;

					View v003 = findViewById(R.id.osd_nextturn_new);
					h001 = getResources().getDimensionPixelSize(R.dimen.osd_nextturn_new_height);
					lp001 = v003.getLayoutParams();
					lp001.height = h001;
					v003.requestLayout();

					v003 = findViewById(R.id.bottom_bar);
					h001 = getResources().getDimensionPixelSize(R.dimen.bottom_bar_height);
					lp001 = v003.getLayoutParams();
					lp001.height = h001;
					v003.requestLayout();

					LinearLayout v002 = findViewById(R.id.bottom_slide_view);
					h001 = getResources().getDimensionPixelSize(R.dimen.bottom_slide_view_height);
					lp001 = v002.getLayoutParams();
					lp001.height = h001;
					v002.requestLayout();

					v003 = findViewById(R.id.osd_timetodest_new);
					h001 = getResources().getDimensionPixelSize(R.dimen.osd_timetodest_new_height);
					lp001 = v003.getLayoutParams();
					lp001.height = h001;
					v003.requestLayout();

					v002 = findViewById(R.id.bottom_line_container);
					h001 = getResources().getDimensionPixelSize(R.dimen.bottom_line_container_height);
					lp001 = v002.getLayoutParams();
					lp001.height = h001;
					v002.requestLayout();

					RelativeLayout v001 = findViewById(R.id.gui_top_container);
					h001 = getResources().getDimensionPixelSize(R.dimen.gui_top_container_height);
					lp001 = v001.getLayoutParams();
					lp001.height = h001;
					v001.requestLayout();

					int ml = getResources().getDimensionPixelSize(R.dimen.margin_left_speeding);
					int mb = getResources().getDimensionPixelSize(R.dimen.margin_bottom_speeding);
					v003 = findViewById(R.id.view_speeding);
					RelativeLayout.LayoutParams relativeParams_001 = (RelativeLayout.LayoutParams) v003.getLayoutParams();
					relativeParams_001.setMargins(ml, 0, 0, mb); // left, top, right, bottom
					v003.setLayoutParams(relativeParams_001);
					v003.requestLayout();

					smaller_top_bar(true);

					// Toast.makeText(this, "landscape", Toast.LENGTH_SHORT).show();

					bottom_bar_px = (int) getResources().getDimension(R.dimen.gui_top_container_height);
					bottom_bar_slider_shadow_px = (int) getResources().getDimension(R.dimen.bottom_slide_view_shadow_compat_height);

					Navit.cur_y_margin_bottom_bar_touch = h + Navit.actionBarHeight + bottom_bar_px - Navit.bottom_bar_slider_shadow_px; // try to put view at bottom
				}
				catch (Exception e)
				{
					Navit.cur_y_margin_bottom_bar_touch = h + Navit.actionBarHeight + bottom_bar_px - Navit.bottom_bar_slider_shadow_px; // try to put view at bottom
				}
				Navit.bottom_y_margin_bottom_bar_touch = Navit.cur_y_margin_bottom_bar_touch;
				// -- bottom bar --
			}
			else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT)
			{
				// setContentView(R.layout.main_layout);

				// -- bottom bar --
				int h = NavitGraphics.mCanvasHeight;
				try
				{
					int h001;
					android.view.ViewGroup.LayoutParams lp001;

					View v003 = findViewById(R.id.osd_nextturn_new);
					h001 = getResources().getDimensionPixelSize(R.dimen.osd_nextturn_new_height);
					lp001 = v003.getLayoutParams();
					lp001.height = h001;
					v003.requestLayout();

					v003 = findViewById(R.id.bottom_bar);
					h001 = getResources().getDimensionPixelSize(R.dimen.bottom_bar_height);
					lp001 = v003.getLayoutParams();
					lp001.height = h001;
					v003.requestLayout();

					LinearLayout v002 = findViewById(R.id.bottom_slide_view);
					h001 = getResources().getDimensionPixelSize(R.dimen.bottom_slide_view_height);
					lp001 = v002.getLayoutParams();
					lp001.height = h001;
					v002.requestLayout();

					v003 = findViewById(R.id.osd_timetodest_new);
					h001 = getResources().getDimensionPixelSize(R.dimen.osd_timetodest_new_height);
					lp001 = v003.getLayoutParams();
					lp001.height = h001;
					v003.requestLayout();

					v002 = findViewById(R.id.bottom_line_container);
					h001 = getResources().getDimensionPixelSize(R.dimen.bottom_line_container_height);
					lp001 = v002.getLayoutParams();
					lp001.height = h001;
					v002.requestLayout();

					RelativeLayout v001 = findViewById(R.id.gui_top_container);
					h001 = getResources().getDimensionPixelSize(R.dimen.gui_top_container_height);
					lp001 = v001.getLayoutParams();
					lp001.height = h001;
					v001.requestLayout();

					int ml = getResources().getDimensionPixelSize(R.dimen.margin_left_speeding);
					int mb = getResources().getDimensionPixelSize(R.dimen.margin_bottom_speeding);
					v003 = findViewById(R.id.view_speeding);
					RelativeLayout.LayoutParams relativeParams_001 = (RelativeLayout.LayoutParams) v003.getLayoutParams();
					relativeParams_001.setMargins(ml, 0, 0, mb); // left, top, right, bottom
					v003.setLayoutParams(relativeParams_001);
					v003.requestLayout();

					smaller_top_bar(false);

					// Toast.makeText(this, "protrait", Toast.LENGTH_SHORT).show();

					bottom_bar_px = (int) getResources().getDimension(R.dimen.gui_top_container_height);
					bottom_bar_slider_shadow_px = (int) getResources().getDimension(R.dimen.bottom_slide_view_shadow_compat_height);

					Navit.cur_y_margin_bottom_bar_touch = h + Navit.actionBarHeight + bottom_bar_px - Navit.bottom_bar_slider_shadow_px; // try to put view at bottom
				}
				catch (Exception e)
				{
					Navit.cur_y_margin_bottom_bar_touch = h + Navit.actionBarHeight + bottom_bar_px - Navit.bottom_bar_slider_shadow_px; // try to put view at bottom
				}
				Navit.bottom_y_margin_bottom_bar_touch = Navit.cur_y_margin_bottom_bar_touch;
				// -- bottom bar --
			}

			last_orientation = newConfig.orientation;
		}
	}

	static void long_toast(String msg)
	{
		try
		{
			Message msg7 = Navit_progress_h.obtainMessage();
			Bundle b7 = new Bundle();
			msg7.what = 2; // long Toast message
			b7.putString("text", msg);
			msg7.setData(b7);
			Navit_progress_h.sendMessage(msg7);
		}
		catch (Exception e)
		{
		}
	}

	private static void short_toast(String msg)
	{
		try
		{
			Message msg7 = Navit_progress_h.obtainMessage();
			Bundle b7 = new Bundle();
			msg7.what = 3; // short Toast message
			b7.putString("text", msg);
			msg7.setData(b7);
			Navit_progress_h.sendMessage(msg7);
		}
		catch (Exception e)
		{
		}
	}

	static void route_wrapper(String addr, float lat_start, float lon_start, boolean start_coords_valid, double lat_end, double lon_end, boolean remember_dest)
	{
		global_last_destination_name = NavitSpeech2.filter_out_special_chars_for_dest_string(addr);
		// System.out.println("HOME002:" + addr + " = " + global_last_destination_name);

		if (preferences.PREF_routing_engine == 1)
		{
			route_online_OSRM(addr, lat_start, lon_start, start_coords_valid, lat_end, lon_end, remember_dest);
		}
		else if (preferences.PREF_routing_engine == 0)
		{
			route_offline_ZANavi(addr, lat_start, lon_start, start_coords_valid, lat_end, lon_end, remember_dest);
		}
	}

	private static void route_offline_ZANavi(String addr, float lat_start, float lon_start, boolean start_coords_valid, double lat_end, double lon_end, boolean remember_dest)
	{
		if (remember_dest)
		{
			try
			{
				Navit.remember_destination(addr, "" + lat_end, "" + lon_end);
				// save points
				write_map_points();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}

		// DEBUG: clear route rectangle list
		NavitGraphics.route_rects.clear();

		if (NavitGraphics.navit_route_status == 0)
		{
			short_toast(Navit.get_text("setting destination to") + "\n" + addr);

			Navit.destination_set();

			Message msg = new Message();
			Bundle b = new Bundle();
			b.putInt("Callback", 3);
			b.putString("lat", "" + lat_end);
			b.putString("lon", "" + lon_end);
			b.putString("q", addr);
			msg.setData(b);
			callback_handler_55.sendMessage(msg);
		}
		else
		{
			short_toast(Navit.get_text("new Waypoint") + "\n" + addr);

			Message msg = new Message();
			Bundle b = new Bundle();
			b.putInt("Callback", 48);
			b.putString("lat", "" + lat_end);
			b.putString("lon", "" + lon_end);
			b.putString("q", addr);
			msg.setData(b);
			callback_handler_55.sendMessage(msg);
		}

	}

	private static void route_online_OSRM(final String addr, float lat_start, float lon_start, boolean start_coords_valid, final double lat_end, final double lon_end, final boolean remember_dest)
	{
		// http://router.project-osrm.org/viaroute?loc=46.3456438,17.450&loc=47.34122,17.5332&instructions=false&alt=false

		if (!start_coords_valid)
		{
			location_coords cur_target = new location_coords();
			try
			{
				geo_coord tmp = get_current_vehicle_position();
				cur_target.lat = tmp.Latitude;
				cur_target.lon = tmp.Longitude;
			}
			catch (Exception e)
			{
			}

			try
			{
				lat_start = (float) cur_target.lat;
				lon_start = (float) cur_target.lon;
			}
			catch (Exception e)
			{
				e.printStackTrace();
				Log.e(TAG, "problem with location!");
			}
		}

		// old API --jdg--jdg--
		//final String request_url = String.format(Locale.US, "http://router.project-osrm.org/viaroute?loc=%4.6f,%4.6f&loc=%4.6f,%4.6f&instructions=true&alt=false", lat_start, lon_start, lat_end, lon_end);
		// fixme
		// new API, url works but more work is needed --jdg--jdg--
		final String request_url = String.format(Locale.US, "http://router.project-osrm.org/route/v1/driving/%4.6f,%4.6f;%4.6f,%4.6f?steps=true", lon_start, lat_start, lon_end, lat_end);

		// StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		// StrictMode.setThreadPolicy(policy);

		try
		{
			// System.out.println("XML:S:001 url=" + request_url);
			final URL url = new URL(request_url);
			Log.e(TAG, url.toString());
			// System.out.println("XML:S:002");
			//			SAXParserFactory factory = SAXParserFactory.newInstance();
			//			System.out.println("XML:S:003");
			//			SAXParser parser = factory.newSAXParser();
			//			System.out.println("XML:S:004");
			//			XMLReader xmlreader = parser.getXMLReader();
			//			System.out.println("XML:S:005");
			//			xmlreader.setContentHandler(new ZANaviXMLHandler());
			//			System.out.println("XML:S:006");

			final Thread add_to_route = new Thread()
			{
				@Override
				public void run()
				{
					try
					{

						// --------------
						// --------------
						// --------------
						// ------- allow this HTTPS cert ---
						// --------------
						// --------------
						// --------------
						//						X509HostnameVerifier hnv = new X509HostnameVerifier()
						//						{
						//
						//							@Override
						//							public void verify(String hostname, SSLSocket arg1) throws IOException
						//							{
						//								Log.d("SSL", "DANGER !!! trusted hostname=" + hostname + " DANGER !!!");
						//							}
						//
						//							@Override
						//							public void verify(String hostname, X509Certificate cert) throws SSLException
						//							{
						//								Log.d("SSL", "DANGER !!! trusted hostname=" + hostname + " DANGER !!!");
						//							}
						//
						//							@Override
						//							public void verify(String hostname, String[] cns, String[] subjectAlts) throws SSLException
						//							{
						//								Log.d("SSL", "DANGER !!! trusted hostname=" + hostname + " DANGER !!!");
						//							}
						//
						//							@Override
						//							public boolean verify(String hostname, SSLSession session)
						//							{
						//								Log.d("SSL", "DANGER !!! trusted hostname=" + hostname + " DANGER !!!");
						//								return true;
						//							}
						//						};
						//
						//						SSLContext context = SSLContext.getInstance("TLS");
						//						context.init(null, new X509TrustManager[] { new X509TrustManager()
						//						{
						//							public java.security.cert.X509Certificate[] getAcceptedIssuers()
						//							{
						//								return new java.security.cert.X509Certificate[0];
						//							}
						//
						//							@Override
						//							public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws java.security.cert.CertificateException
						//							{
						//							}
						//
						//							@Override
						//							public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws java.security.cert.CertificateException
						//							{
						//							}
						//						} }, new SecureRandom());
						//						javax.net.ssl.SSLSocketFactory sslf = context.getSocketFactory();
						//
						//						HostnameVerifier hnv_default = HttpsURLConnection.getDefaultHostnameVerifier();
						//						javax.net.ssl.SSLSocketFactory sslf_default = HttpsURLConnection.getDefaultSSLSocketFactory();
						//						HttpsURLConnection.setDefaultHostnameVerifier(hnv);
						//						HttpsURLConnection.setDefaultSSLSocketFactory(sslf);
						//
						//						DefaultHttpClient client = new DefaultHttpClient();
						//
						//						SSLSocketFactory socketFactory = SSLSocketFactory.getSocketFactory();
						//						SchemeRegistry registry = new SchemeRegistry();
						//						registry.register(new Scheme("https", socketFactory, 443));
						//						ThreadSafeClientConnManager mgr = new ThreadSafeClientConnManager(client.getParams(), registry);
						//						DefaultHttpClient httpClient = new DefaultHttpClient(mgr, client.getParams());
						//
						//						socketFactory.setHostnameVerifier(hnv);
						//
						//						HttpGet get_request = new HttpGet(request_url);
						//						HttpResponse http_response = httpClient.execute(get_request);
						//						HttpEntity responseEntity = http_response.getEntity();
						//
						//						HttpsURLConnection.setDefaultHostnameVerifier(hnv_default);
						//						HttpsURLConnection.setDefaultSSLSocketFactory(sslf_default);
						// --------------
						// --------------
						// --------------
						// ------- allow this HTTPS cert ---
						// --------------
						// --------------
						// --------------

						InputSource is = new InputSource();
						is.setEncoding("utf-8");
						// is.setByteStream(responseEntity.getContent());
						is.setByteStream(url.openStream());
						// System.out.println("XML:S:007");

						String response = slurp(is.getByteStream(), 16384);
						// response = response.replaceAll("&", "&amp;");

						// System.out.println("XML:S:007.a res=" + response);

						final JSONObject obj = new JSONObject(response);

						//   System.out.println(person.getInt("id"));

						final String route_geometry = obj.getString("route_geometry");
						final JSONArray route_instructions_array = obj.getJSONArray("route_instructions");

						int loop_i;
						JSONArray instruction;
						int[] instruction_pos = new int[route_instructions_array.length()];
						for (loop_i = 0; loop_i < route_instructions_array.length(); loop_i++)
						{
							instruction = (JSONArray) route_instructions_array.get(loop_i);
							instruction_pos[loop_i] = Integer.parseInt(instruction.get(3).toString());
							// System.out.println("XML:instr. pos=" + instruction_pos[loop_i]);
						}

						// System.out.println("XML:S:009 o=" + route_geometry);

						List<geo_coord> gc_list = decode_function(route_geometry, 6);

						if (gc_list.size() < 2)
						{
							// no real route found!! (only 1 point)
						}
						else
						{

							Message msg = new Message();
							Bundle b = new Bundle();

							int loop = 0;

							geo_coord cur = new geo_coord();
							geo_coord old = new geo_coord();
							geo_coord corr = new geo_coord();

							cur.Latitude = gc_list.get(loop).Latitude;
							cur.Longitude = gc_list.get(loop).Longitude;

							int first_found = 1;

							if (gc_list.size() > 2)
							{
								int instr_count = 1;

								for (loop = 1; loop < gc_list.size(); loop++)
								{

									old.Latitude = cur.Latitude;
									old.Longitude = cur.Longitude;
									cur.Latitude = gc_list.get(loop).Latitude;
									cur.Longitude = gc_list.get(loop).Longitude;

									if ((instruction_pos[instr_count] == loop) || (loop == (gc_list.size() - 1)))
									{

										if (loop == (gc_list.size() - 1))
										{
											corr = cur;
										}
										else
										{
											corr = get_point_on_line(old, cur, 70);
										}

										// -- add waypoint --
										//									b.putInt("Callback", 55548);
										//									b.putString("lat", "" + corr.Latitude);
										//									b.putString("lon", "" + corr.Longitude);
										//									b.putString("q", " ");
										//									msg.setData(b);
										try
										{
											// NavitGraphics.callback_handler.sendMessage(msg);
											if (first_found == 1)
											{
												first_found = 0;
												CallbackMessageChannel(55503, corr.Latitude + "#" + corr.Longitude + "#" + "");
												// System.out.println("XML:rR:" + loop + " " + corr.Latitude + " " + corr.Longitude);
											}
											else
											{
												CallbackMessageChannel(55548, corr.Latitude + "#" + corr.Longitude + "#" + "");
												// System.out.println("XML:rw:" + loop + " " + corr.Latitude + " " + corr.Longitude);
											}
											// Thread.sleep(25);
										}
										catch (Exception e)
										{
											e.printStackTrace();
										}
										// -- add waypoint --

										instr_count++;

									}

								}
							}

							if (remember_dest)
							{
								try
								{
									Navit.remember_destination(addr, "" + lat_end, "" + lon_end);
									// save points
									write_map_points();
								}
								catch (Exception e)
								{
									e.printStackTrace();
								}
							}

							b.putInt("Callback", 55599);
							msg.setData(b);
							try
							{
								// System.out.println("XML:calc:");
								Thread.sleep(10);
								callback_handler_55.sendMessage(msg);
							}
							catch (Exception e)
							{
								e.printStackTrace();
							}

						}

					}
					catch (Exception e2)
					{
						e2.printStackTrace();
					}
				}
			};
			add_to_route.start();

			// convert to coords -------------
			// convert to coords -------------

		}
		catch (Exception e)
		{
			// System.out.println("XML:S:EEE");
			e.printStackTrace();
		}
	}

	private static List<geo_coord> decode_function(String encoded, double precision)
	{

		precision = Math.pow(10, -precision);
		int len = encoded.length();
		int index = 0;
		int lat = 0;
		int lng = 0;
		double lat_f;
		double lon_f;

		final List<geo_coord> latLongList = new ArrayList<>();
		latLongList.clear();

		while (index < len)
		{

			int b;
			int shift = 0;
			int result = 0;

			do
			{
				b = encoded.charAt(index++) - 63;
				result |= (b & 0x1f) << shift;
				shift += 5;

			}
			while (b >= 0x20);

			int dlat;
			if ((result & 1) != 0)
			{
				dlat = ~(result >> 1);
			}
			else
			{
				dlat = (result >> 1);
			}

			lat += dlat;
			shift = 0;
			result = 0;

			do
			{
				b = encoded.charAt(index++) - 63;
				result |= (b & 0x1f) << shift;
				shift += 5;
			}
			while (b >= 0x20);

			int dlng = 0;
			if ((result & 1) != 0)
			{
				dlng = ~(result >> 1);
			}
			else
			{
				dlng = (result >> 1);
			}

			lng += dlng;
			//array.push( {lat: lat * precision, lng: lng * precision} );
			// array.push( [lat * precision, lng * precision] );

			// System.out.println("XML:lat=" + (lat * precision) + " lon=" + (lng * precision));

			lat_f = lat * precision;
			lon_f = lng * precision;

			geo_coord gc = new geo_coord();
			gc.Latitude = lat_f;
			gc.Longitude = lon_f;
			latLongList.add(gc);
		}

		return latLongList;
	}

	static class geo_coord
	{
		public double Latitude;
		public double Longitude;
	}

	private static geo_coord get_current_vehicle_position()
	{
		geo_coord ret = new geo_coord();
		String current_target_string2 = NavitGraphics.CallbackGeoCalc(14, 1, 1);
		// System.out.println("GET CUR POS:" + current_target_string2);
		ret.Latitude = 0;
		ret.Longitude = 0;
		try
		{
			String[] tmp = current_target_string2.split(":", 2);
			ret.Latitude = Double.parseDouble(tmp[0]);
			ret.Longitude = Double.parseDouble(tmp[1]);
		}
		catch (Exception e)
		{
			// System.out.println("GET CUR POS:ERROR " + e.getMessage());
		}
		return ret;
	}

	static int[] geo_to_px(float lat, float lon)
	{
		int[] ret = new int[3];

		ret[0] = -100;
		ret[1] = -100;
		ret[2] = 0; // invalid

		try
		{
			String x_y = NavitGraphics.CallbackGeoCalc(2, lat, lon);

			if (Navit.GFX_OVERSPILL)
			{
				String[] tmp = x_y.split(":", 2);
				int x = Integer.parseInt(tmp[0]);
				int y = Integer.parseInt(tmp[1]);

				ret[0] = (int) (((float) x + (float) NavitGraphics.mCanvasWidth_overspill) * NavitGraphics.Global_dpi_factor);
				ret[1] = (int) (((float) y + (float) NavitGraphics.mCanvasHeight_overspill) * NavitGraphics.Global_dpi_factor);

				ret[2] = 1; // VALID
			}
			else
			{
				String[] tmp = x_y.split(":", 2);
				int x = Integer.parseInt(tmp[0]);
				int y = Integer.parseInt(tmp[1]);

				ret[0] = (int) ((float) x * NavitGraphics.Global_dpi_factor);
				ret[1] = (int) ((float) y * NavitGraphics.Global_dpi_factor);

				ret[2] = 1; // VALID
			}

		}
		catch (Exception e)
		{
		}

		return ret;
	}

	static geo_coord px_to_geo(int x, int y)
	{
		geo_coord out = new geo_coord();
		try
		{

			String lat_lon;
			if (Navit.GFX_OVERSPILL)
			{
				lat_lon = NavitGraphics.CallbackGeoCalc(1, (x + NavitGraphics.mCanvasWidth_overspill) * NavitGraphics.Global_dpi_factor, (y + NavitGraphics.mCanvasHeight_overspill) * NavitGraphics.Global_dpi_factor);
			}
			else
			{
				lat_lon = NavitGraphics.CallbackGeoCalc(1, x * NavitGraphics.Global_dpi_factor, y * NavitGraphics.Global_dpi_factor);
			}

			String[] tmp = lat_lon.split(":", 2);
			out.Latitude = Float.parseFloat(tmp[0]);
			out.Longitude = Float.parseFloat(tmp[1]);
		}
		catch (Exception e)
		{
		}

		return out;
	}

	static double get_percent_coord(double start, double end, int perecent_pos)
	{
		return start + (end - start) * ((float) perecent_pos / 100.0f);
	}

	private static geo_coord get_point_on_line(geo_coord start, geo_coord end, int perecent_pos)
	{
		geo_coord out = new geo_coord();

		out.Latitude = start.Latitude + (end.Latitude - start.Latitude) * ((float) perecent_pos / 100.0f);
		out.Longitude = start.Longitude + (end.Longitude - start.Longitude) * ((float) perecent_pos / 100.0f);

		return out;
	}

	private static String slurp(final InputStream is, final int bufferSize)
	{
		final char[] buffer = new char[bufferSize];
		final StringBuilder out = new StringBuilder();
		Reader in;

		try
		{

			in = new InputStreamReader(is, "UTF-8");

			for (;;)
			{
				int rsz;
				rsz = in.read(buffer, 0, buffer.length);

				if (rsz < 0)
				{
					break;
				}
				out.append(buffer, 0, rsz);
			}

		}
		catch (IOException e)
		{
		}
		catch (Exception ex)
		{
		}

		return out.toString();
	}

	private static final Handler UIHandler;
	static
	{
		UIHandler = new Handler(Looper.getMainLooper());
	}

	private static void recalculate_route()
	{
		try
		{
			// update route, if a route is set
			sNavitObject.sendCallBackMessage(73);
		}
		catch (Exception e)
		{
		}
	}

	static void runOnUI(Runnable runnable)
	{
		UIHandler.post(runnable);
	}

	private String substring_without_ioobe(String in, int start, int end)
	{
		String ret = ";:;:****no match****;:;:";

		try
		{
			ret = in.substring(start, end);
		}
		catch (Exception e)
		{
			// return dummy-no-match String
		}

		return ret;
	}

	static private Cursor c = null;
	static private final Uri uri = CR_CONTENT_URI;

	static int get_reglevel()
	{
		int ret = 0;

		try
		{
			c = null;
			Thread thread = new Thread()
			{
				public void run()
				{
					try
					{
						c = content_resolver.query(uri, null, null, null, null);
					}
					catch (Exception c1)
					{
						System.out.println("CPVD:reg(e002)=" + c1.getMessage());
					}
				}
			};
			thread.start();
			try
			{
				thread.join();
			}
			catch (InterruptedException e)
			{
			}

			if (c == null)
			{
				System.out.println("CPVD:Cursor c == null.");
				System.out.println("CPVD:reg(0)=" + ret);
				return ret;
			}

			while (c.moveToNext())
			{
				String column1 = c.getString(0);
				String column2 = c.getString(1);
				String column3 = c.getString(2);

				System.out.println("CPVD:column1=" + column1 + " column2=" + column2 + " column3=" + column3);

				if (Integer.parseInt(column1) == 1)
				{
					if (column2.equals("reg"))
					{
						ret = Integer.parseInt(column3);
						System.out.println("CPVD:reg(1)=" + ret);
					}
				}
			}
			c.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.out.println("CPVD:reg(e001)=" + e.getMessage());
		}

		System.out.println("CPVD:reg(2)=" + ret);
		return ret;
	}

	static void take_map_screenshot(String dir_name, String name_base)
	{
		try
		{
			View v1 = Navit.getInstance().getN_NavitGraphics().view;
			v1.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
			v1.setDrawingCacheEnabled(true);
			Bitmap bm = v1.getDrawingCache();

			FileOutputStream out = null;
			try
			{
				out = new FileOutputStream(dir_name + "/" + name_base + ".png");
				bm.compress(Bitmap.CompressFormat.PNG, 100, out);
			}
			catch (Exception e)
			{
				e.printStackTrace();
				System.out.println("TSCR:004 " + e.getMessage());
			}
			finally
			{
				v1.setDrawingCacheEnabled(false);

				try
				{
					if (out != null)
					{
						out.close();
					}
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
		catch (Exception e4)
		{
		}
	}

	static void take_phone_screenshot(Activity a, String dir_name, String name_base)
	{
		try
		{
			View v1 = a.getWindow().getDecorView().getRootView();
			v1.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
			v1.setDrawingCacheEnabled(true);
			Bitmap bm = v1.getDrawingCache();

			FileOutputStream out = null;
			try
			{
				out = new FileOutputStream(dir_name + "/" + name_base + ".png");
				bm.compress(Bitmap.CompressFormat.PNG, 100, out);
			}
			catch (Exception e)
			{
				e.printStackTrace();
				System.out.println("TSCR:004 " + e.getMessage());
			}
			finally
			{
				v1.setDrawingCacheEnabled(false);

				try
				{
					if (out != null)
					{
						out.close();
					}
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
		catch (Exception e4)
		{
		}
	}

	static String stacktrace_to_string(Exception e)
	{
		try
		{
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			return errors.toString();
		}
		catch (Exception e2)
		{
			try
			{
				return e.getMessage();
			}
			catch (Exception e3)
			{
				return "xxx";
			}
		}
	}

	static void static_show_route_graph(int v)
	{
		// DEBUG: toggle Routgraph on/off
		try
		{
			if (v == 1)
			{
				Navit.Routgraph_enabled = 1;

				Message msg = new Message();
				Bundle b = new Bundle();
				b.putInt("Callback", 71);
				b.putString("s", "" + Navit.Routgraph_enabled);
				msg.setData(b);
				callback_handler_55.sendMessage(msg);
			}
			else if (v == 0)
			{
				Navit.Routgraph_enabled = 0;

				Message msg = new Message();
				Bundle b = new Bundle();
				b.putInt("Callback", 71);
				b.putString("s", "" + Navit.Routgraph_enabled);
				msg.setData(b);
				callback_handler_55.sendMessage(msg);
			}
			else
			{
				Navit.Routgraph_enabled = 0;

				Message msg = new Message();
				Bundle b = new Bundle();
				b.putInt("Callback", 71);
				b.putString("s", "" + Navit.Routgraph_enabled);
				msg.setData(b);
				callback_handler_55.sendMessage(msg);

				Thread.sleep(350);

				System.out.println("static_show_route_graph:v=" + v);

				msg = new Message();
				b = new Bundle();
				b.putInt("Callback", 71);
				b.putString("s", "" + v);
				msg.setData(b);
				callback_handler_55.sendMessage(msg);
			}
		}
		catch (Exception e)
		{
		}

	}

	/*
	 * start a search with given values
	 */
	static void executeSearch_with_values(String street, String town, String hn, boolean offline, boolean index, boolean partialmatch, boolean hide_dupl)
	{
		Intent search_intent = new Intent(sNavitObject, NavitAddressSearchActivity.class);
		search_intent.putExtra("title", Navit.get_text("Enter: City and Street")); //TRANS
		String addr1 = "";
		String addr2 = "";

		if ((town != null) && (street != null))
		{
			if (index)
			{
				search_intent.putExtra("address_string", street + " " + town);
				addr1 = street + " " + town;
				addr2 = street + " " + town;
			}
			else
			{
				search_intent.putExtra("address_string", town + " " + street);
				addr1 = town + " " + street;
				addr2 = town + " " + street;
			}
		}
		else if (town != null)
		{
			search_intent.putExtra("address_string", town);
			addr1 = town;
			addr2 = town;
		}
		else if (street != null)
		{
			search_intent.putExtra("address_string", street);
			addr1 = street;
			addr2 = street;
		}

		if (hn != null)
		{
			search_intent.putExtra("hn_string", hn);
		}

		if (offline)
		{
			search_intent.putExtra("type", "offline");
		}
		else
		{
			search_intent.putExtra("type", "online");
		}

		String pm_temp = "0";
		if (partialmatch)
		{
			pm_temp = "1";
		}
		search_intent.putExtra("partial_match", pm_temp);

		if (index)
		{
			Navit_last_address_partial_match = partialmatch;
			Navit_last_address_search_string = addr2;
			Navit_last_address_hn_string = hn;
			Navit_last_address_full_file_search = false;
			search_hide_duplicates = hide_dupl;
			sNavitObject.startActivityForResult(search_intent, NavitAddressSearch_id_offline);
		}
		else
		{
			Navit_last_address_partial_match = partialmatch;
			Navit_last_address_search_string = addr1;
			Navit_last_address_hn_string = hn;
			Navit_last_address_full_file_search = false;
			search_hide_duplicates = hide_dupl;

			// only from offline mask!
			// { "*A", "*AA", "*ALL*" }
			Navit_last_address_search_country_iso2_string = "*A";
			Navit_last_address_search_country_flags = 3;
			Navit_last_address_search_country_id = 1; // default=*ALL*
			preferences.PREF_search_country = Navit_last_address_search_country_id;
			setPrefs_search_country();

			// show duplicates in search ------------
			sNavitObject.sendCallBackMessage(44);
			// show duplicates in search ------------

			if (hide_dupl)
			{
				search_hide_duplicates = true;
				// hide duplicates when searching
				// hide duplicates when searching
				sNavitObject.sendCallBackMessage(45);
				// hide duplicates when searching
				// hide duplicates when searching
			}

			System.out.println("dialog -- 11:001");
			Message msg = Navit_progress_h.obtainMessage();
			Bundle b = new Bundle();
			msg.what = 11;
			b.putInt("dialog_num", Navit.SEARCHRESULTS_WAIT_DIALOG_OFFLINE);
			msg.setData(b);
			Navit_progress_h.sendMessage(msg);
		}
	}

	private static void set_night_mode(int i)
	{
		try
		{
			// i==0 --> day mode
			// i==1 --> night mode
			NavitGraphics.CallbackMessageChannelReal(114, "" + i);
		}
		catch (Exception e)
		{
		}
	}

	static String run_cmd(String command)
	{
		try
		{
			System.out.println("CCMMDD:" + "run:" + command);

			final Process process = Runtime.getRuntime().exec(command);
			final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			final StringBuilder log = new StringBuilder();
			final String separator = System.getProperty("line.separator");

			String line;
			while ((line = bufferedReader.readLine()) != null)
			{
				log.append(line);
				log.append(separator);
			}

			return log.toString();
		}
		catch (IOException ioe)
		{
			System.out.println("CCMMDD:" + "Ex 01:" + ioe.getMessage());
			return null;
		}
		catch (Exception e)
		{
			System.out.println("CCMMDD:" + "Ex 02:" + e.getMessage());
			return null;
		}
	}

	// --------- make app crash ---------
	// --------- make app crash ---------
	// --------- make app crash ---------
	static void crash_app_java(int type)
	{
		System.out.println("+++++======= CRASH  =======+++++");
		System.out.println("+++++======= CRASH  =======+++++");
		System.out.println("+++++======= CRASH  =======+++++");
		System.out.println("+++++======= CRASH  =======+++++");
		System.out.println("+++++======= CRASH  =======+++++");
		System.out.println("+++++======================+++++");
		System.out.println("+++++======= TYPE:J =======+++++");
		System.out.println("+++++======================+++++");
		if (type == 1)
		{
			Java_Crash_001();
		}
		else if (type == 2)
		{
			Java_Crash_002();
		}
		else
		{
			stackOverflow();
		}
	}

	static void crash_app_C()
	{
		System.out.println("+++++======= CRASH  =======+++++");
		System.out.println("+++++======= CRASH  =======+++++");
		System.out.println("+++++======= CRASH  =======+++++");
		System.out.println("+++++======= CRASH  =======+++++");
		System.out.println("+++++======= CRASH  =======+++++");
		System.out.println("+++++======================+++++");
		System.out.println("+++++======= TYPE:C =======+++++");
		System.out.println("+++++======================+++++");
		AppCrashC();
	}

	private static void Java_Crash_001()
	{
		Integer i = null;
		i.byteValue();
	}

	static native void AppCrashC();

	private static void Java_Crash_002()
	{
		View v = null;
		v.bringToFront();
	}

	private static void stackOverflow()
	{
		stackOverflow();
	}

	// --------- make app crash ---------
	// --------- make app crash ---------
	// --------- make app crash ---------

	private boolean checkForUpdate()
	{
		PackageInfo info = null;
		try
		{
			info = getPackageManager().getPackageInfo(getPackageName(), 0);
		}
		catch (Exception e)
		{
			Log.e(TAG, "could not get package info");
		}

		if (info == null)
		{
			return false;
		}

		long last_update_ts_used = PreferenceManager.getDefaultSharedPreferences(this).getLong(PREF_KEY_LASTUPDATETS, -1L);

		if (info.lastUpdateTime != last_update_ts_used)
		{
			if (info.firstInstallTime != info.lastUpdateTime)
			{
				if (info.lastUpdateTime + (long) (1000 * 60 * 2) > System.currentTimeMillis())
				{
					// we updated in the last 2 minutes
					Log.e(TAG, "updated app:updated app within last 2 minutes");
					PreferenceManager.getDefaultSharedPreferences(this).edit().putLong(PREF_KEY_LASTUPDATETS, info.lastUpdateTime).apply();
					return true;
				}
			}
		}
		else
		{
			Log.e(TAG, "updated app:already used this update timestamp");
		}

		return false;
	}

	public static boolean get_fdbl()
	{
		return FDBL;
	}
}
