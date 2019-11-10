/**
 * ZANavi, Zoff Android Navigation system.
 * Copyright (C) 2011-2012 Zoff <zoff@zoff.cc>
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

/**
 * Navit, a modular navigation system.
 * Copyright (C) 2005-2009 Navit Team
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

#include <stdio.h>
#include <string.h>
#include <glib.h>
#include <time.h>
#include "config.h"
#include "debug.h"
#include "coord.h"
#include "item.h"
#include "log.h"
#include "plugin.h"
#include "transform.h"
#include "util.h"
#include "event.h"
// #include "coord.h"
#include "navit.h"
#include "transform.h"
#include "projection.h"
#include "point.h"
#include "graphics.h"
#include "callback.h"
#include "color.h"
#include "layout.h"
#include "vehicle.h"
#ifdef HAVE_API_ANDROID
#include "android.h"
#endif

// forward rev
struct navit *global_navit;

struct vehicle
{
	struct vehicle_methods meth;
	struct vehicle_priv *priv;
	struct callback_list *cbl;
	struct log *nmea_log, *gpx_log;
	char *gpx_desc;
	struct attr **attrs;

	// cursor
	struct cursor *cursor;
	int cursor_fixed;
	struct callback *animate_callback;
	struct event_timeout *animate_timer;
	struct point cursor_pnt;
	struct graphics *gra;
	struct graphics_gc *bg;
	struct transformation *trans;
	int angle;
	int speed;
	int sequence;
	GHashTable *log_to_cb;
};

static void vehicle_draw_do(struct vehicle *this_, int lazy);
static void vehicle_log_nmea(struct vehicle *this_, struct log *log);
static void vehicle_log_gpx(struct vehicle *this_, struct log *log);
static void vehicle_log_textfile(struct vehicle *this_, struct log *log);
static void vehicle_log_binfile(struct vehicle *this_, struct log *log);
static int vehicle_add_log(struct vehicle *this_, struct log *log);
void vehicle_remove_cursor(struct vehicle *this_);
extern int hold_drawing; // in navit.c

/**
 * Creates a new vehicle
 */
struct vehicle *
vehicle_new(struct attr *parent, struct attr **attrs)
{
#ifdef NAVIT_FUNC_CALLS_DEBUG_PRINT
	dbg(0,"+#+:enter\n");
#endif
	struct vehicle *this_;
	struct attr *source;
	struct vehicle_priv *(*vehicletype_new)(struct vehicle_methods * meth, struct callback_list * cbl, struct attr ** attrs);
	char *type, *colon;
	struct pcoord center;

	//DBG dbg(0, "enter\n");
	source = attr_search(attrs, NULL, attr_source);
	if (!source)
	{
		//DBG dbg(0, "no source\n");
		return NULL;
	}

	type = g_strdup(source->u.str);
	colon = strchr(type, ':');
	if (colon)
	{
		*colon = '\0';
	}
	////DBG dbg(0, "source='%s' type='%s'\n", source->u.str, type);

#ifdef PLUGSSS
	vehicletype_new = plugin_get_vehicle_type(type);
	if (!vehicletype_new)
	{
		//DBG dbg(0, "invalid type '%s'\n", type);
		g_free(type);
		return NULL;
	}
#endif

	this_ = g_new0(struct vehicle, 1);
	this_->cbl = callback_list_new("vehicle_new:this_->cbl");



#ifdef PLUGSSS
	this_->priv = vehicletype_new(&this_->meth, this_->cbl, attrs);
#else

	if (strncmp("demo", type, 4) == 0)
	{
		this_->priv = vehicle_demo_new(&this_->meth, this_->cbl, attrs);
	}
	else if (strncmp("android", type, 7) == 0)
	{
		this_->priv = vehicle_android_new_android(&this_->meth, this_->cbl, attrs);
	}
#endif

	g_free(type);


	//DBG dbg(0, "veh new 2\n");
	if (!this_->priv)
	{
		//DBG dbg(0, "vehicletype_new failed\n");
		callback_list_destroy(this_->cbl);
		g_free(this_);
		return NULL;
	}
	//DBG dbg(0, "veh new 3\n");
	this_->attrs = attr_list_dup(attrs);
	//DBG dbg(0, "veh new 4\n");
	this_->trans = transform_new();

	// set bad 0/0 location ??? i dont know really
	/*
	 struct coord_geo g;
	 struct coord co;
	 enum projection pro=projection_mg;
	 g.lat=53.13;
	 g.lng=11.70;
	 transform_from_geo(pro, &g, &co);
	 center.x=co.x;
	 center.y=co.y;
	 center.pro = pro;
	 */

	center.pro = projection_screen;
	center.x = 0;
	center.y = 0;
	//DBG dbg(0, "veh new 5\n");
	transform_setup(this_->trans, &center, 16, 0);
	//DBG dbg(0, "veh new 6\n");

	this_->log_to_cb = g_hash_table_new(NULL, NULL);
	//DBG dbg(0, "leave\n");
	return this_;
}

/**
 * Destroys a vehicle
 * 
 * @param this_ The vehicle to destroy
 */
void vehicle_destroy(struct vehicle *this_)
{
#ifdef NAVIT_FUNC_CALLS_DEBUG_PRINT
	dbg(0,"+#+:enter\n");
#endif
	if (this_->animate_callback)
	{
		callback_destroy(this_->animate_callback);
		event_remove_timeout(this_->animate_timer);
	}
	transform_destroy(this_->trans);
	this_->meth.destroy(this_->priv);
	callback_list_destroy(this_->cbl);
	attr_list_free(this_->attrs);
	if (this_->bg)
		graphics_gc_destroy(this_->bg);
	if (this_->gra)
		graphics_free(this_->gra);
	g_free(this_);
}

/**
 * Creates an attribute iterator to be used with vehicles
 */
struct attr_iter *
vehicle_attr_iter_new(void)
{
#ifdef NAVIT_FUNC_CALLS_DEBUG_PRINT
	dbg(0,"+#+:enter\n");
#endif
return (struct attr_iter *)g_new0(void *,1);
}

/**
 * Destroys a vehicle attribute iterator
 *
 * @param iter a vehicle attr_iter
 */
void vehicle_attr_iter_destroy(struct attr_iter *iter)
{
#ifdef NAVIT_FUNC_CALLS_DEBUG_PRINT
	dbg(0,"+#+:enter\n");
#endif
	g_free(iter);
}

/**
 * Generic get function
 *
 * @param this_ Pointer to a vehicle structure
 * @param type The attribute type to look for
 * @param attr Pointer to an attr structure to store the attribute
 * @param iter A vehicle attr_iter
 */
int vehicle_get_attr(struct vehicle *this_, enum attr_type type, struct attr *attr, struct attr_iter *iter)
{
#ifdef NAVIT_FUNC_CALLS_DEBUG_PRINT
	dbg(0,"+#+:enter\n");
#endif
	////DBG dbg(0,"enter\n");
	int ret;
	if (this_->meth.position_attr_get)
	{
		ret = this_->meth.position_attr_get(this_->priv, type, attr);
		if (ret)
		{
			return ret;
		}
	}
	if (type == attr_log_gpx_desc)
	{
		attr->u.str = this_->gpx_desc;
		return 1;
	}
	////DBG dbg(0,"before return\n");

#ifdef NAVIT_FUNC_CALLS_DEBUG_PRINT
	dbg(0,"+#+:leave\n");
#endif

	return attr_generic_get_attr(this_->attrs, NULL, type, attr, iter);
}

/**
 * Generic set function
 *
 * @param this_ Pointer to a vehicle structure
 * @param attr Pointer to an attr structure for the attribute to be set
 * @return nonzero on success, zero on failure
 */
int vehicle_set_attr(struct vehicle *this_, struct attr *attr)
{
#ifdef NAVIT_FUNC_CALLS_DEBUG_PRINT
	dbg(0,"+#+:enter\n");
#endif
	int ret = 1;
	if (this_->meth.set_attr)
	{
		//dbg(0,"xx 1\n");
		ret = this_->meth.set_attr(this_->priv, attr);
		//dbg(0,"xx 1.1\n");
	}

	if (ret == 1 && attr->type == attr_log_gpx_desc)
	{
		g_free(this_->gpx_desc);
		this_->gpx_desc = attr->u.str;
	}

	if (ret == 1 && attr->type != attr_navit && attr->type != attr_pdl_gps_update)
	{
		//dbg(0,"xx 3\n");
		this_->attrs = attr_generic_set_attr(this_->attrs, attr);
	}

#ifdef NAVIT_FUNC_CALLS_DEBUG_PRINT
	dbg(0,"+#+:leave\n");
#endif

	return ret != 0;
}

/**
 * Generic add function
 *
 * @param this_ A vehicle
 * @param attr A struct attr
 */
int vehicle_add_attr(struct vehicle *this_, struct attr *attr)
{
#ifdef NAVIT_FUNC_CALLS_DEBUG_PRINT
	dbg(0,"+#+:enter\n");
#endif
	int ret = 1;

	switch (attr->type)
	{
		case attr_callback:
			callback_list_add(this_->cbl, attr->u.callback);
			break;
		case attr_log:
			ret = vehicle_add_log(this_, attr->u.log);
			break;
			// currently supporting oldstyle cursor config.
		case attr_cursor:
			this_->cursor_fixed = 1;
			vehicle_set_cursor(this_, attr->u.cursor, 1);
			break;
		default:
			break;
	}

	if (ret)
	{
		this_->attrs = attr_generic_add_attr(this_->attrs, attr);
	}

#ifdef NAVIT_FUNC_CALLS_DEBUG_PRINT
	dbg(0,"+#+:leave\n");
#endif

	return ret;
}

/**
 * @brief Generic remove function.
 *
 * Used to remove a callback from the vehicle.
 * @param this_ A vehicle
 * @param attr
 */
int vehicle_remove_attr(struct vehicle *this_, struct attr *attr)
{
#ifdef NAVIT_FUNC_CALLS_DEBUG_PRINT
	dbg(0,"+#+:enter\n");
#endif
	struct callback *cb;
	switch (attr->type)
	{
		case attr_callback:
			callback_list_remove(this_->cbl, attr->u.callback);
			break;
		case attr_log:
			cb = g_hash_table_lookup(this_->log_to_cb, attr->u.log);
			if (!cb)
				return 0;
			g_hash_table_remove(this_->log_to_cb, attr->u.log);
			callback_list_remove(this_->cbl, cb);
			break;
		case attr_cursor:
			vehicle_remove_cursor(this_);
			break;
		default:
			this_->attrs = attr_generic_remove_attr(this_->attrs, attr);
			return 0;
	}
	return 1;
}

/**
 * Sets the cursor of a vehicle.
 *
 * @param this_ A vehicle
 * @param cursor A cursor
 * @author Ralph Sennhauser (10/2009)
 */
void vehicle_set_cursor(struct vehicle *this_, struct cursor *cursor, int overwrite)
{
#ifdef NAVIT_FUNC_CALLS_DEBUG_PRINT
	dbg(0,"+#+:enter\n");
#endif
	struct point sc;
	if (this_->cursor_fixed && !overwrite)
	{
		return;
	}

#if 0
	if (this_->animate_callback)
	{
		event_remove_timeout(this_->animate_timer);
		this_->animate_timer = NULL; // dangling pointer! prevent double freeing.
		callback_destroy(this_->animate_callback);
		this_->animate_callback = NULL; // dangling pointer! prevent double freeing.
	}

	if (cursor && cursor->interval)
	{
		this_->animate_callback = callback_new_2(callback_cast(vehicle_draw_do), this_, 0);
		//dbg(0, "event_add_timeout %d,%d,%p", cursor->interval, 1, this_->animate_callback);
		this_->animate_timer = event_add_timeout(cursor->interval, 1, this_->animate_callback);
	}

	if (cursor && this_->gra && this_->cursor)
	{
		this_->cursor_pnt.x += (this_->cursor->w - cursor->w) / 2;
		this_->cursor_pnt.y += (this_->cursor->h - cursor->h) / 2;
		graphics_overlay_resize(this_->gra, &this_->cursor_pnt, cursor->w, cursor->h, 65535, 0);
	}
#endif

	// ###############################################
	// ###############################################
	// set fixed size of navigation cursor to 50,50 !!
	if (cursor)
	{
		cursor->w = 50;
		cursor->h = 50;
	}

	if (this_->cursor)
	{
		this_->cursor->w = 50;
		this_->cursor->h = 50;
	}
	// set fixed size of navigation cursor to 50,50 !!
	// ###############################################
	// ###############################################


	if (cursor)
	{
		sc.x = cursor->w / 2;
		sc.y = cursor->h / 2;
		//if (!this_->cursor && this_->gra)
		//{
		//	graphics_overlay_disable(this_->gra, 0);
		//}
	}
	else
	{
		sc.x = sc.y = 0;
		//if (this_->cursor && this_->gra)
		//{
		//	graphics_overlay_disable(this_->gra, 1);
		//}
	}

	transform_set_screen_center(this_->trans, &sc);

	this_->cursor = cursor;

#ifdef NAVIT_FUNC_CALLS_DEBUG_PRINT
	dbg(0,"+#+:leave\n");
#endif

}

void vehicle_remove_cursor(struct vehicle *this_)
{
#ifdef NAVIT_FUNC_CALLS_DEBUG_PRINT
	dbg(0,"+#+:enter\n");
#endif
	struct point sc;

	if (this_->animate_callback)
	{
		event_remove_timeout(this_->animate_timer);
		this_->animate_timer = NULL; // dangling pointer! prevent double freeing.
		callback_destroy(this_->animate_callback);
		this_->animate_callback = NULL; // dangling pointer! prevent double freeing.
	}

	if (this_->cursor && this_->gra)
	{
	}

	if (this_->cursor && this_->gra)
	{
		graphics_overlay_disable(this_->gra, 1);
	}

	this_->cursor = NULL;
}

/**
 * Draws a vehicle on top of a graphics.
 *
 * @param this_ The vehicle
 * @param gra The graphics
 * @param pnt Screen coordinates of the vehicle.
 * @param lazy use lazy draw mode.
 * @param angle The angle relative to the map.
 * @param speed The speed of the vehicle.
 */
void vehicle_draw(struct vehicle *this_, struct graphics *gra, struct point *pnt, int lazy, int angle, int speed)
{
#ifdef NAVIT_FUNC_CALLS_DEBUG_PRINT
	dbg(0,"+#+:enter\n");
#endif

	//if (hold_drawing > 0)
	//{
	//	return;
	//}

	if (angle < 0)
	{
		angle += 360;
	}

	//// dbg(1, "enter this=%p gra=%p pnt=%p lazy=%d dir=%d speed=%d\n", this_, gra,
	//		pnt, lazy, angle, speed);
	//dbg(0, "point %d,%d\n", pnt->x, pnt->y);
	this_->cursor_pnt = *pnt;
	this_->angle = angle;
	this_->speed = speed;
	if (!this_->cursor)
	{
		return;
	}

	//dbg(0,"cpx:%d, cpy:%d, cw:%d, ch:%d\n", this_->cursor_pnt.x, this_->cursor_pnt.y, this_->cursor->w, this_->cursor->h);

	//*** this_->cursor_pnt.x -= this_->cursor->w / 2;
	//*** this_->cursor_pnt.y -= this_->cursor->h / 2;

#if 0
	if (!this_->gra)
	{
		struct color c;
		dbg(0,"Create new Vehicle Overlay Graphics\n");
		this_->gra = graphics_overlay_new("type:vehicle",gra, &this_->cursor_pnt, this_->cursor->w, this_->cursor->h, 65535, 0);
		if (this_->gra)
		{
			this_->bg = graphics_gc_new(this_->gra);
			c.r = 0;
			c.g = 0;
			c.b = 0;
			c.a = 0;
			graphics_gc_set_foreground(this_->bg, &c);
			graphics_background_gc(this_->gra, this_->bg);
		}
	}
#endif

	//  ++++++++++ // transform_set_yaw(this_->trans, -this_->angle);
	// vehicle_draw_do(this_, lazy);

#ifdef HAVE_API_ANDROID
	//dbg(0,"x=%d y=%d angle=%d speed=%d\n",this_->cursor_pnt.x, this_->cursor_pnt.y, angle, speed);

	//int dx = 0;
	//int dy = 0;
	//int dangle = 0;

	//struct navit_vehicle *nv2 = global_navit->vehicle;
	//struct point pnt2;
	//dbg(0,"dir=%d dx=%d, dy=%d\n", nv2->dir, dx, dy);
	//transform(this_->trans, projection_mg, &nv2->coord, &pnt2, 1, 0, 0, NULL);
	//dbg(0,"px=%d, py=%d\n", pnt2.x, pnt2.y);

	set_vehicle_values_to_java(this_->cursor_pnt.x, this_->cursor_pnt.y, angle, speed);
#endif

}

int vehicle_get_cursor_data(struct vehicle *this, struct point *pnt, int *angle, int *speed)
{
#ifdef NAVIT_FUNC_CALLS_DEBUG_PRINT
	dbg(0,"+#+:enter\n");
#endif
	*pnt = this->cursor_pnt;
	*angle = this->angle;
	*speed = this->speed;
	return 1;
}

//int vehicle_set_cursor_data_01(struct vehicle *this, struct point *pnt)
//{
//#ifdef NAVIT_FUNC_CALLS_DEBUG_PRINT
//	dbg(0,"+#+:enter\n");
//#endif
//	this->cursor_pnt.x = pnt->.x;
//	this->cursor_pnt.y = pnt->.y;
//	return 1;
//}


static void vehicle_draw_do(struct vehicle *this_, int lazy)
{
	// UNUSED ---------
}

/**
 * Writes to an NMEA log.
 *
 * @param this_ Pointer to the vehicle structure of the data source
 * @param log Pointer to a log structure for the log file
 */
static void vehicle_log_nmea(struct vehicle *this_, struct log *log)
{
#ifdef NAVIT_FUNC_CALLS_DEBUG_PRINT
	dbg(0,"+#+:enter\n");
#endif
	struct attr pos_attr;

	if (!this_->meth.position_attr_get)
	{
		return;
	}

	if (!this_->meth.position_attr_get(this_->priv, attr_position_nmea, &pos_attr))
	{
		return;
	}

	log_write(log, pos_attr.u.str, strlen(pos_attr.u.str), 0);
}


void vehicle_log_gpx_add_tag(char *tag, char **logstr)
{
#ifdef NAVIT_FUNC_CALLS_DEBUG_PRINT
	dbg(0,"+#+:enter\n");
#endif
	char *ext_start = "\t<extensions>\n";
	char *ext_end = "\t</extensions>\n";
	char *trkpt_end = "</trkpt>";
	char *start = NULL, *end = NULL;
	if (!*logstr)
	{
		start = g_strdup(ext_start);
		end = g_strdup(ext_end);
	}
	else
	{
		char *str = strstr(*logstr, ext_start);
		int len;
		if (str)
		{
			len = str - *logstr + strlen(ext_start);
			start = g_strdup(*logstr);
			start[len] = '\0';
			end = g_strdup(str + strlen(ext_start));
		}
		else
		{
			str = strstr(*logstr, trkpt_end);
			len = str - *logstr;
			end = g_strdup_printf("%s%s", ext_end, str);
			str = g_strdup(*logstr);
			str[len] = '\0';
			start = g_strdup_printf("%s%s", str, ext_start);
			g_free(str);
		}
	}
	*logstr = g_strdup_printf("%s%s%s", start, tag, end);
	g_free(start);
	g_free(end);
}

/**
 * Writes to a GPX log.
 *
 * @param this_ Pointer to the vehicle structure of the data source
 * @param log Pointer to a log structure for the log file
 */
static void vehicle_log_gpx(struct vehicle *this_, struct log *log)
{
#ifdef NAVIT_FUNC_CALLS_DEBUG_PRINT
	dbg(0,"+#+:enter\n");
#endif
	struct attr attr, *attrp, fix_attr;
	enum attr_type *attr_types;
	char *logstr;
	char *extensions = "\t<extensions>\n";

	if (!this_->meth.position_attr_get)
		return;
	if (log_get_attr(log, attr_attr_types, &attr, NULL))
		attr_types = attr.u.attr_types;
	else
		attr_types = NULL;
	if (this_->meth.position_attr_get(this_->priv, attr_position_fix_type, &fix_attr))
	{
		if (fix_attr.u.num == 0)
			return;
	}
	if (!this_->meth.position_attr_get(this_->priv, attr_position_coord_geo, &attr))
		return;
	logstr = g_strdup_printf("<trkpt lat=\"%f\" lon=\"%f\">\n", attr.u.coord_geo->lat, attr.u.coord_geo->lng);
	if (attr_types && attr_types_contains_default(attr_types, attr_position_time_iso8601, 0))
	{
		if (this_->meth.position_attr_get(this_->priv, attr_position_time_iso8601, &attr))
		{
			logstr = g_strconcat_printf(logstr, "\t<time>%s</time>\n", attr.u.str);
		}
		else
		{
			char *timep = current_to_iso8601();
			logstr = g_strconcat_printf(logstr, "\t<time>%s</time>\n", timep);
			g_free(timep);
		}
	}
	if (this_->gpx_desc)
	{
		logstr = g_strconcat_printf(logstr, "\t<desc>%s</desc>\n", this_->gpx_desc);
		g_free(this_->gpx_desc);
		this_->gpx_desc = NULL;
	}
	if (attr_types_contains_default(attr_types, attr_position_height, 0) && this_->meth.position_attr_get(this_->priv, attr_position_height, &attr))
		logstr = g_strconcat_printf(logstr, "\t<ele>%.6f</ele>\n", *attr.u.numd);
	// <magvar> magnetic variation in degrees; we might use position_magnetic_direction and position_direction to figure it out
	// <geoidheight> Height (in meters) of geoid (mean sea level) above WGS84 earth ellipsoid. As defined in NMEA GGA message (field 11, which vehicle_wince.c ignores)
	// <name> GPS name (arbitrary)
	// <cmt> comment
	// <src> Source of data
	// <link> Link to additional information (URL)
	// <sym> Text of GPS symbol name
	// <type> Type (classification)
	// <fix> Type of GPS fix {'none'|'2d'|'3d'|'dgps'|'pps'}, leave out if unknown. Similar to position_fix_type but more detailed.
	if (attr_types_contains_default(attr_types, attr_position_sats_used, 0) && this_->meth.position_attr_get(this_->priv, attr_position_sats_used, &attr))
		logstr = g_strconcat_printf(logstr, "\t<sat>%d</sat>\n", attr.u.num);
	if (attr_types_contains_default(attr_types, attr_position_hdop, 0) && this_->meth.position_attr_get(this_->priv, attr_position_hdop, &attr))
		logstr = g_strconcat_printf(logstr, "\t<hdop>%.6f</hdop>\n", *attr.u.numd);
	// <vdop>, <pdop> Vertical and position dilution of precision, no corresponding attribute
	if (attr_types_contains_default(attr_types, attr_position_direction, 0) && this_->meth.position_attr_get(this_->priv, attr_position_direction, &attr))
		logstr = g_strconcat_printf(logstr, "\t<course>%.1f</course>\n", *attr.u.numd);
	if (attr_types_contains_default(attr_types, attr_position_speed, 0) && this_->meth.position_attr_get(this_->priv, attr_position_speed, &attr))
		logstr = g_strconcat_printf(logstr, "\t<speed>%.2f</speed>\n", (*attr.u.numd / 3.6));
	if (attr_types_contains_default(attr_types, attr_profilename, 0) && (attrp = attr_search(this_->attrs, NULL, attr_profilename)))
	{
		logstr = g_strconcat_printf(logstr, "%s\t\t<navit:profilename>%s</navit:profilename>\n", extensions, attrp->u.str);
		extensions = "";
	}
	if (attr_types_contains_default(attr_types, attr_position_radius, 0) && this_->meth.position_attr_get(this_->priv, attr_position_radius, &attr))
	{
		logstr = g_strconcat_printf(logstr, "%s\t\t<navit:radius>%.2f</navit:radius>\n", extensions, *attr.u.numd);
		extensions = "";
	}
	if (!strcmp(extensions, ""))
	{
		logstr = g_strconcat_printf(logstr, "\t</extensions>\n");
	}
	logstr = g_strconcat_printf(logstr, "</trkpt>\n");
	callback_list_call_attr_1(this_->cbl, attr_log_gpx, &logstr);
	log_write(log, logstr, strlen(logstr), 0);
	g_free(logstr);
}

/**
 * Writes to a text log.
 *
 * @param this_ Pointer to the vehicle structure of the data source
 * @param log Pointer to a log structure for the log file
 */
static void vehicle_log_textfile(struct vehicle *this_, struct log *log)
{
#ifdef NAVIT_FUNC_CALLS_DEBUG_PRINT
	dbg(0,"+#+:enter\n");
#endif
	struct attr pos_attr, fix_attr;
	char *logstr;
	if (!this_->meth.position_attr_get)
		return;
	if (this_->meth.position_attr_get(this_->priv, attr_position_fix_type, &fix_attr))
	{
		if (fix_attr.u.num == 0)
			return;
	}
	if (!this_->meth.position_attr_get(this_->priv, attr_position_coord_geo, &pos_attr))
		return;
	logstr = g_strdup_printf("%f %f type=trackpoint\n", pos_attr.u.coord_geo->lng, pos_attr.u.coord_geo->lat);
	callback_list_call_attr_1(this_->cbl, attr_log_textfile, &logstr);
	log_write(log, logstr, strlen(logstr), 0);
}

/**
 * Writes to a binary log.
 *
 * @param this_ Pointer to the vehicle structure of the data source
 * @param log Pointer to a log structure for the log file
 */
static void vehicle_log_binfile(struct vehicle *this_, struct log *log)
{
#ifdef NAVIT_FUNC_CALLS_DEBUG_PRINT
	dbg(0,"+#+:enter\n");
#endif
	struct attr pos_attr, fix_attr;
	int *buffer;
	int *buffer_new;
	int len, limit = 1024, done = 0, radius = 25;
	struct coord c;
	enum log_flags flags;

	if (!this_->meth.position_attr_get)
		return;
	if (this_->meth.position_attr_get(this_->priv, attr_position_fix_type, &fix_attr))
	{
		if (fix_attr.u.num == 0)
			return;
	}
	if (!this_->meth.position_attr_get(this_->priv, attr_position_coord_geo, &pos_attr))
		return;
	transform_from_geo(projection_mg, pos_attr.u.coord_geo, &c);
	if (!c.x || !c.y)
		return;
	while (!done)
	{
		buffer = log_get_buffer(log, &len);
		if (!buffer || !len)
		{
			buffer_new = g_malloc(5 * sizeof(int));
			buffer_new[0] = 2;
			buffer_new[1] = type_track;
			buffer_new[2] = 0;
		}
		else
		{
			buffer_new = g_malloc((buffer[0] + 3) * sizeof(int));
			memcpy(buffer_new, buffer, (buffer[0] + 1) * sizeof(int));
		}
		//// dbg(1, "c=0x%x,0x%x\n", c.x, c.y);
		buffer_new[buffer_new[0] + 1] = c.x;
		buffer_new[buffer_new[0] + 2] = c.y;
		buffer_new[0] += 2;
		buffer_new[2] += 2;
		if (buffer_new[2] > limit)
		{
			int count = buffer_new[2] / 2;
			struct coord *out = g_alloca(sizeof(struct coord) * (count));
			struct coord *in = (struct coord *) (buffer_new + 3);
			int count_out = transform_douglas_peucker(in, count, radius, out);
			memcpy(in, out, count_out * 2 * sizeof(int));
			buffer_new[0] += (count_out - count) * 2;
			buffer_new[2] += (count_out - count) * 2;
			flags = log_flag_replace_buffer | log_flag_force_flush | log_flag_truncate;
		}
		else
		{
			flags = log_flag_replace_buffer | log_flag_keep_pointer | log_flag_keep_buffer | log_flag_force_flush;
			done = 1;
		}
		log_write(log, (char *) buffer_new, (buffer_new[0] + 1) * sizeof(int), flags);
	}
}

/**
 * Register a new log to receive data.
 *
 * @param this_ Pointer to the vehicle structure of the data source
 * @param log Pointer to a log structure for the log file
 */
static int vehicle_add_log(struct vehicle *this_, struct log *log)
{
#ifdef NAVIT_FUNC_CALLS_DEBUG_PRINT
	dbg(0,"+#+:enter\n");
#endif
	struct callback *cb;
	struct attr type_attr;
	if (!log_get_attr(log, attr_type, &type_attr, NULL))
		return 1;

	if (!strcmp(type_attr.u.str, "nmea"))
	{
		cb = callback_new_attr_2(callback_cast(vehicle_log_nmea), attr_position_coord_geo, this_, log);
	}
	else if (!strcmp(type_attr.u.str, "gpx"))
	{
		char *header = "<?xml version='1.0' encoding='UTF-8'?>\n"
			"<gpx version='1.1' creator='Navit http://navit.sourceforge.net'\n"
			"     xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'\n"
			"     xmlns:navit='http://www.navit-project.org/schema/navit'\n"
			"     xmlns='http://www.topografix.com/GPX/1/1'\n"
			"     xsi:schemaLocation='http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd'>\n"
			"<trk>\n"
			"<trkseg>\n";
		char *trailer = "</trkseg>\n</trk>\n</gpx>\n";
		log_set_header(log, header, strlen(header));
		log_set_trailer(log, trailer, strlen(trailer));
		cb = callback_new_attr_2(callback_cast(vehicle_log_gpx), attr_position_coord_geo, this_, log);
	}
	else if (!strcmp(type_attr.u.str, "textfile"))
	{
		char *header = "type=track\n";
		log_set_header(log, header, strlen(header));
		cb = callback_new_attr_2(callback_cast(vehicle_log_textfile), attr_position_coord_geo, this_, log);
	}
	else if (!strcmp(type_attr.u.str, "binfile"))
	{
		cb = callback_new_attr_2(callback_cast(vehicle_log_binfile), attr_position_coord_geo, this_, log);
	}
	else
		return 1;
	g_hash_table_insert(this_->log_to_cb, log, cb);
	callback_list_add(this_->cbl, cb);
	return 0;
}

void vehicle_update_(struct vehicle *this_, double lat, double lon, float speed, float direction, double height, float radius, long gpstime)
{
#ifdef HAVE_API_ANDROID
	if (this_->meth.update_location_direct)
	{
		// dbg(0,"location=%p\n", location);
		this_->meth.update_location_direct(lat, lon, speed, direction, height, radius, gpstime);
	}
#endif
}


