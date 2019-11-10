/**
 * ZANavi, Zoff Android Navigation system.
 * Copyright (C) 2011-2014 Zoff <zoff@zoff.cc>
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

//##############################################################################################################
//#
//# File: graphics.c
//# Description:
//# Comment:
//# Authors: Martin Schaller (04/2008)
//#
//##############################################################################################################

#include <stdlib.h>
#include <glib.h>
#include <stdio.h>
#include <math.h>
#include <zlib.h>
#include "config.h"
#include "debug.h"
#include "string.h"
#include "draw_info.h"
#include "point.h"
#include "graphics.h"
#include "projection.h"
#include "item.h"
#include "map.h"
#include "coord.h"
#include "transform.h"
#include "plugin.h"
#include "profile.h"
#include "mapset.h"
#include "layout.h"
#include "route.h"
#include "util.h"
#include "callback.h"
#include "file.h"
#include "event.h"
//
#include "attr.h"
#include "track.h"
#include "navit.h"
#include "route.h"

#ifdef HAVE_API_ANDROID
#include "android.h"
#else
// linux seems to need this explicitly
#include "pthread.h"
#endif

static pthread_cond_t uiConditionVariable = PTHREAD_COND_INITIALIZER;
static pthread_mutex_t uiConditionMutex = PTHREAD_MUTEX_INITIALIZER;





// #define NAVIT_FUNC_CALLS_DEBUG_PRINT 1


// --------------- debug function calls ------------------
// --------------- debug function calls ------------------
#ifdef NAVIT_FUNC_CALLS_DEBUG_PRINT
	#undef return2
	#define return2	dbg_func(0, global_func_indent_counter, "return(%d)\n", __LINE__);global_func_indent_counter--;return

	#define __F_START__ global_func_indent_counter++;dbg_func(0, global_func_indent_counter, "enter\n");
	#define __F_END__   dbg_func(0, global_func_indent_counter, "leave\n");global_func_indent_counter--;
#else
	#undef return2
	#define return2	return

	#define __F_START__
	#define __F_END__
#endif
// --------------- debug function calls ------------------
// --------------- debug function calls ------------------






//##############################################################################################################
//# Description:
//# Comment:
//# Authors: Martin Schaller (04/2008)
//##############################################################################################################

// above what "order" level to show only prerendered-map or vector-map
#define ORDER_USE_PRERENDERED_MAP 5
#define ORDER_USE_NORMAL_MAP 6
#define ORDER_USE_PRERENDERED_MAP__C 4
#define ORDER_USE_NORMAL_MAP__C 3
#define ORDER_USE_BORDERS_MAP 14

// minimum (line legth * 32) squared (in pixel) to show text label
#define MIN_LINE_LENGTH_FOR_TEXT_2 409600
// minimum (line legth * 32) squared (in pixel) to show text label -> for middle segments of streets
#define MIN_LINE_LENGTH_FOR_TEXT_MIDDLE_2 1638400

#define MAX_POI_ICONS_ON_MAP 30
#define MAX_POI_ICON_TEXTS_ON_MAP 12
#define ORDER_LEVEL_TO_SHOW_ALL_POI 14 // order
#define ORDER2_LEVEL_TO_SHOW_ALL_POI 12 // global_scale

#define MAX_PLACE_LABELS_ON_MAP 13
#define MAX_DISTRICT_LABELS_ON_MAP 13
#define MAX_MAJOR_PLACE_LABELS_ON_MAP 9

#define ORDER_LEVEL_FOR_STREET_SIMPLIFY 9
#define STREET_SIMPLIFY	24

#define X_COLOR_BACKGROUND_NM 0x6666, 0x6666, 0x6666, 0xffff
struct color background_night_mode = { X_COLOR_BACKGROUND_NM };


struct graphics
{
	struct graphics_priv *priv;
	struct graphics_methods meth;
	char *default_font;
	int font_len;
	struct graphics_font **font;
	struct graphics_gc *gc[3];
	struct attr **attrs;
	struct callback_list *cbl;
	struct point_rect r;
	int gamma, brightness, contrast;
	int colormgmt;
	int font_size;
	GList *selection;
};

/*
 struct display_context
 {
 struct graphics *gra;
 struct element *e;
 struct graphics_gc *gc;
 struct graphics_gc *gc_background;
 struct graphics_image *img;
 enum projection pro;
 int mindist;
 struct transformation *trans;
 enum item_type type;
 int maxlen;
 };

 #define HASH_SIZE 1024
 */

/*
 struct hash_entry
 {
 enum item_type type;
 struct displayitem *di;
 };
 */

/*
 struct displaylist {
 int busy;
 int workload;
 struct callback *cb;
 struct layout *layout, *layout_hashed;
 struct display_context dc;
 int order, order_hashed, max_offset;
 struct mapset *ms;
 struct mapset_handle *msh;
 struct map *m;
 int conv;
 struct map_selection *sel;
 struct map_rect *mr;
 struct callback *idle_cb;
 struct event_idle *idle_ev;
 unsigned int seq;
 struct hash_entry hash_entries[HASH_SIZE];
 };
 */

struct displaylist_icon_cache
{
	unsigned int seq;

};

/**
 * FIXME
 * @param <>
 * @returns <>
 * @author Martin Schaller (04/2008)
 */
struct displayitem
{
	struct displayitem *next;
	struct item item;
	char *label;
	// struct color color;
	unsigned int col_int_value;
	int count;
	struct coord c[0];
};

static void draw_circle(struct point *pnt, int diameter, int scale, int start, int len, struct point *res, int *pos, int dir);
static void graphics_process_selection(struct graphics *gra, struct displaylist *dl);
static void graphics_gc_init(struct graphics *this_);
static void graphics_draw_polygon_clipped(struct graphics *gra, struct graphics_gc *gc, struct point *pin, int count_in);


// ------------- TILES -------------
// ------------- TILES -------------
// ------------- TILES -------------
#include "mvt_tiles.h"
// ------------- TILES -------------
// ------------- TILES -------------
// ------------- TILES -------------



static void clear_hash(struct displaylist *dl)
{
	int i;
	for (i = 0; i < HASH_SIZE_GRAPHICS_; i++)
	{
		dl->hash_entries[i].type = type_none;
	}
}

static struct hash_entry *
get_hash_entry(struct displaylist *dl, enum item_type type)
{
	int hashidx = (type * 2654435761UL) & (HASH_SIZE_GRAPHICS_ - 1);
	int offset = dl->max_offset;
	do
	{
		if (!dl->hash_entries[hashidx].type)
		{
			return NULL;
		}
		if (dl->hash_entries[hashidx].type == type)
		{
			return &dl->hash_entries[hashidx];
		}
		hashidx = (hashidx + 1) & (HASH_SIZE_GRAPHICS_ - 1);
	}
	while (offset-- > 0);

	return NULL;
}

static struct hash_entry *
set_hash_entry(struct displaylist *dl, enum item_type type)
{
	int hashidx = (type * 2654435761UL) & (HASH_SIZE_GRAPHICS_ - 1);
	int offset = 0;
	for (;;)
	{
		if (!dl->hash_entries[hashidx].type)
		{
			dl->hash_entries[hashidx].type = type;
			if (dl->max_offset < offset)
				dl->max_offset = offset;
			return &dl->hash_entries[hashidx];
		}
		if (dl->hash_entries[hashidx].type == type)
			return &dl->hash_entries[hashidx];
		hashidx = (hashidx + 1) & (HASH_SIZE_GRAPHICS_ - 1);
		offset++;
	}
	return NULL;
}

static int graphics_set_attr_do(struct graphics *gra, struct attr *attr)
{
	switch (attr->type)
	{
		case attr_gamma:
			gra->gamma = attr->u.num;
			break;
		case attr_brightness:
			gra->brightness = attr->u.num;
			break;
		case attr_contrast:
			gra->contrast = attr->u.num;
			break;
		case attr_font_size:
			gra->font_size = attr->u.num;
			return 1;
		default:
			return 0;
	}
	gra->colormgmt = (gra->gamma != 65536 || gra->brightness != 0 || gra->contrast != 65536);
	graphics_gc_init(gra);
	return 1;
}

int graphics_set_attr(struct graphics *gra, struct attr *attr)
{
	int ret = 1;
	// //DBG // dbg(0,"enter\n");
	if (gra->meth.set_attr)
		ret = gra->meth.set_attr(gra->priv, attr);
	if (!ret)
		ret = graphics_set_attr_do(gra, attr);
	return ret != 0;
}

void graphics_set_rect(struct graphics *gra, struct point_rect *pr)
{
	gra->r = *pr;
}

/**
 * Creates a new graphics object
 * attr type required
 * @param <>
 * @returns <>
 * @author Martin Schaller (04/2008)
 */
struct graphics * graphics_new(struct attr *parent, struct attr **attrs)
{
	// dbg(0, "EEnter\n");

	int count = 0;
	struct graphics *this_;
	struct attr *type_attr;
	struct graphics_priv * (*graphicstype_new)(struct navit *nav, struct graphics_methods *meth, struct attr **attrs, struct callback_list *cbl);

	if (!(type_attr = attr_search(attrs, NULL, attr_type)))
	{
		return NULL;
	}

	// dbg(0, "plugins\n");

#ifdef PLUGSSS
	graphicstype_new = plugin_get_graphics_type(type_attr->u.str);
	if (!graphicstype_new)
	{
		return NULL;
	}
#endif

	// dbg(0, "g 003\n");

	this_=g_new0(struct graphics, 1);
	this_->cbl = callback_list_new("graphics_new:this_->cbl");

#ifdef PLUGSSS
	this_->priv = (*graphicstype_new)(parent->u.navit, &this_->meth, attrs, this_->cbl);
#else
	this_->priv = graphics_android_new(parent->u.navit, &this_->meth, attrs, this_->cbl);
#endif

	this_->attrs = attr_list_dup(attrs);
	this_->brightness = 0;
	this_->contrast = 65536;
	this_->gamma = 65536;
	this_->font_size = 20;

	// dbg(0, "g 004\n");

	while (*attrs)
	{
		count++;
		// dbg(0, "g 005 attr %d\n", count);
		graphics_set_attr_do(this_, *attrs);
		attrs++;
		// dbg(0, "g 006 attr\n");
	}

	// dbg(0, "return\n");
	return this_;
}

/**
 * FIXME
 * @param <>
 * @returns <>
 * @author Martin Schaller (04/2008)
 */
int graphics_get_attr(struct graphics *this_, enum attr_type type, struct attr *attr, struct attr_iter *iter)
{
	return attr_generic_get_attr(this_->attrs, NULL, type, attr, iter);
}

/**
 * FIXME
 * @param <>
 * @returns <>
 * @author Martin Schaller (04/2008)
 */
struct graphics * graphics_overlay_new(const char* name, struct graphics *parent, struct point *p, int w, int h, int alpha, int wraparound)
{
	struct graphics *this_;
	struct point_rect pr;
	if (!parent->meth.overlay_new)
		return NULL;this_=g_new0(struct graphics, 1);
	this_->priv = parent->meth.overlay_new(name, parent->priv, &this_->meth, p, w, h, alpha, wraparound);
	pr.lu.x = 0;
	pr.lu.y = 0;
	pr.rl.x = w;
	pr.rl.y = h;
	this_->font_size = 20;
	graphics_set_rect(this_, &pr);
	if (!this_->priv)
	{
		g_free(this_);
		this_ = NULL;
	}
	return this_;
}

/**
 * @brief Alters the size, position, alpha and wraparound for an overlay
 *
 * @param this_ The overlay's graphics struct
 * @param p The new position of the overlay
 * @param w The new width of the overlay
 * @param h The new height of the overlay
 * @param alpha The new alpha of the overlay
 * @param wraparound The new wraparound of the overlay
 */
void graphics_overlay_resize(struct graphics *this_, struct point *p, int w, int h, int alpha, int wraparound)
{
	if (!this_->meth.overlay_resize)
	{
		return;
	}

	this_->meth.overlay_resize(this_->priv, p, w, h, alpha, wraparound);
}

static void graphics_gc_init(struct graphics *this_)
{
	// dbg(0, "EEnter\n");

	struct color background = { COLOR_BACKGROUND_ };
	struct color black = { COLOR_BLACK_ };
	struct color white = { COLOR_WHITE_ };

	if (!this_->gc[0] || !this_->gc[1] || !this_->gc[2])
	{
		return;
	}

	graphics_gc_set_background(this_->gc[0], &background);
	graphics_gc_set_foreground(this_->gc[0], &background);

	graphics_gc_set_background(this_->gc[1], &black);
	graphics_gc_set_foreground(this_->gc[1], &white);
	graphics_gc_set_background(this_->gc[2], &white);
	graphics_gc_set_foreground(this_->gc[2], &black);

	// dbg(0, "return\n");
}

/**
 * FIXME
 * @param <>
 * @returns <>
 * @author Martin Schaller (04/2008)
 */
void graphics_init(struct graphics *this_)
{
	if (this_->gc[0])
	{
		return;
	}

	this_->gc[0] = graphics_gc_new(this_);
	this_->gc[1] = graphics_gc_new(this_);
	this_->gc[2] = graphics_gc_new(this_);

	graphics_gc_init(this_);

	graphics_background_gc(this_, this_->gc[0]);
}

/**
 * FIXME
 * @param <>
 * @returns <>
 * @author Martin Schaller (04/2008)
 */
void * graphics_get_data(struct graphics *this_, const char *type)
{
	return (this_->meth.get_data(this_->priv, type));
}

void graphics_add_callback(struct graphics *this_, struct callback *cb)
{
	callback_list_add(this_->cbl, cb);
}

void graphics_remove_callback(struct graphics *this_, struct callback *cb)
{
	callback_list_remove(this_->cbl, cb);
}

/**
 * FIXME
 * @param <>
 * @returns <>
 * @author Martin Schaller (04/2008)
 */
struct graphics_font * graphics_font_new(struct graphics *gra, int size, int flags)
{
	struct graphics_font *this_;

	this_=g_new0(struct graphics_font,1);
	this_->priv = gra->meth.font_new(gra->priv, &this_->meth, gra->default_font, size, flags);
	return this_;
}

struct graphics_font * graphics_named_font_new(struct graphics *gra, char *font, int size, int flags)
{
	struct graphics_font *this_;

	this_=g_new0(struct graphics_font,1);
	this_->priv = gra->meth.font_new(gra->priv, &this_->meth, font, size, flags);
	return this_;
}

/**
 * Destroy graphics
 * Called when navit exits
 * @param gra The graphics instance
 * @returns nothing
 * @author David Tegze (02/2011)
 */
void graphics_free(struct graphics *gra)
{
	if (!gra)
	{
		return;
	}

	gra->meth.graphics_destroy(gra->priv);
	g_free(gra->default_font);
	graphics_font_destroy_all(gra);
	g_free(gra);
}

/**
 * Free all loaded fonts.
 * Used when switching layouts.
 * @param gra The graphics instance
 * @returns nothing
 * @author Sarah Nordstrom (05/2008)
 */
void graphics_font_destroy_all(struct graphics *gra)
{
	int i;
	for (i = 0; i < gra->font_len; i++)
	{
		if (!gra->font[i])
		{
			continue;
		}

		gra->font[i]->meth.font_destroy(gra->font[i]->priv);
		gra->font[i] = NULL;
	}
}

/**
 * FIXME
 * @param <>
 * @returns <>
 * @author Martin Schaller (04/2008)
 */
struct graphics_gc * graphics_gc_new(struct graphics *gra)
{
	struct graphics_gc *this_;

	this_=g_new0(struct graphics_gc,1);
	this_->priv = gra->meth.gc_new(gra->priv, &this_->meth);
	this_->gra = gra;
	return this_;
}

/**
 * FIXME
 * @param <>
 * @returns <>
 * @author Martin Schaller (04/2008)
 */
void graphics_gc_destroy(struct graphics_gc *gc)
{
	gc->meth.gc_destroy(gc->priv);
	g_free(gc);
}

static void graphics_convert_color(struct graphics *gra, struct color *in, struct color *out)
{
	*out = *in;

	if (gra->brightness)
	{
		out->r += gra->brightness;
		out->g += gra->brightness;
		out->b += gra->brightness;
	}

	if (gra->contrast != 65536)
	{
		out->r = out->r * gra->contrast / 65536;
		out->g = out->g * gra->contrast / 65536;
		out->b = out->b * gra->contrast / 65536;
	}

	if (out->r < 0)
		out->r = 0;

	if (out->r > 65535)
		out->r = 65535;

	if (out->g < 0)
		out->g = 0;

	if (out->g > 65535)
		out->g = 65535;

	if (out->b < 0)
		out->b = 0;

	if (out->b > 65535)
		out->b = 65535;

	if (gra->gamma != 65536)
	{
		out->r = pow(out->r / 65535.0, gra->gamma / 65536.0) * 65535.0;
		out->g = pow(out->g / 65535.0, gra->gamma / 65536.0) * 65535.0;
		out->b = pow(out->b / 65535.0, gra->gamma / 65536.0) * 65535.0;
	}
}

/**
 * FIXME
 * @param <>
 * @returns <>
 * @author Martin Schaller (04/2008)
 */
void graphics_gc_set_foreground(struct graphics_gc *gc, struct color *c)
{
	struct color cn;
	if (gc->gra->colormgmt)
	{
		graphics_convert_color(gc->gra, c, &cn);
		c = &cn;
	}
	gc->meth.gc_set_foreground(gc->priv, c);
}

/**
 * FIXME
 * @param <>
 * @returns <>
 * @author Martin Schaller (04/2008)
 */
void graphics_gc_set_background(struct graphics_gc *gc, struct color *c)
{
	struct color cn;
	if (gc->gra->colormgmt)
	{
		graphics_convert_color(gc->gra, c, &cn);
		c = &cn;
	}
	gc->meth.gc_set_background(gc->priv, c);
}

/**
 * FIXME
 * @param <>
 * @returns <>
 * @author Martin Schaller (04/2008)
 */
void graphics_gc_set_stipple(struct graphics_gc *gc, struct graphics_image *img)
{
	gc->meth.gc_set_stipple(gc->priv, img ? img->priv : NULL);
}

/**
 * FIXME
 * @param <>
 * @returns <>
 * @author Martin Schaller (04/2008)
 */
void graphics_gc_set_linewidth(struct graphics_gc *gc, int width)
{
	gc->meth.gc_set_linewidth(gc->priv, width);
}

/**
 * FIXME
 * @param <>
 * @returns <>
 * @author Martin Schaller (04/2008)
 */
void graphics_gc_set_dashes(struct graphics *gra, struct graphics_gc *gc, int width, int offset, int dash_list[], int n, int order)
{
	if (gc->meth.gc_set_dashes)
	{
		gc->meth.gc_set_dashes(gra->priv, gc->priv, width, offset, dash_list, order);
	}
}

/**
 * Create a new image from file path scaled to w and h pixels
 * @param gra the graphics instance
 * @param path path of the image to load
 * @param w width to rescale to
 * @param h height to rescale to
 * @returns <>
 * @author Martin Schaller (04/2008)
 */
struct graphics_image * graphics_image_new_scaled(struct graphics *gra, char *path, int w, int h)
{
	struct graphics_image *this_;

	this_=g_new0(struct graphics_image,1);
	this_->height = h;
	this_->width = w;
	this_->priv = gra->meth.image_new(gra->priv, &this_->meth, path, &this_->width, &this_->height, &this_->hot, 0);
	if (!this_->priv)
	{
		g_free(this_);
		this_ = NULL;
	}
	return this_;
}

/**
 * Create a new image from file path scaled to w and h pixels and possibly rotated
 * @param gra the graphics instance
 * @param path path of the image to load
 * @param w width to rescale to
 * @param h height to rescale to
 * @param rotate angle to rotate the image. Warning, graphics might only support 90 degree steps here
 * @returns <>
 * @author Martin Schaller (04/2008)
 */
struct graphics_image * graphics_image_new_scaled_rotated(struct graphics *gra, char *path, int w, int h, int rotate)
{
	struct graphics_image *this_;

	this_=g_new0(struct graphics_image,1);
	this_->height = h;
	this_->width = w;
	this_->priv = gra->meth.image_new(gra->priv, &this_->meth, path, &this_->width, &this_->height, &this_->hot, rotate);
	if (!this_->priv)
	{
		g_free(this_);
		this_ = NULL;
	}
	return this_;
}

/**
 * Create a new image from file path
 * @param gra the graphics instance
 * @param path path of the image to load
 * @returns <>
 * @author Martin Schaller (04/2008)
 */
struct graphics_image * graphics_image_new(struct graphics *gra, char *path)
{
	return graphics_image_new_scaled(gra, path, -1, -1);
}

/**
 * FIXME
 * @param <>
 * @returns <>
 * @author Martin Schaller (04/2008)
 */
void graphics_image_free(struct graphics *gra, struct graphics_image *img)
{
	if (gra->meth.image_free)
		gra->meth.image_free(gra->priv, img->priv);
	g_free(img);
}

/**
 * FIXME
 * @param <>
 * @returns <>
 * @author Martin Schaller (04/2008)
 */
void graphics_draw_restore(struct graphics *this_, struct point *p, int w, int h)
{
	////DBG // dbg(0,"ooo enter ooo\n");

	this_->meth.draw_restore(this_->priv, p, w, h);
}

/**
 * FIXME
 * @param <>
 * @returns <>
 * @author Martin Schaller (04/2008)
 */
void graphics_draw_mode(struct graphics *this_, enum draw_mode_num mode)
{
	////DBG // dbg(0,"ooo enter ooo\n");

	this_->meth.draw_mode(this_->priv, mode);
}

/**
 * FIXME
 * @param <>
 * @returns <>
 * @author Martin Schaller (04/2008)
 */
void graphics_draw_lines(struct graphics *this_, struct graphics_gc *gc, struct point *p, int count)
{
	this_->meth.draw_lines(this_->priv, gc->priv, p, count);
}

void graphics_draw_lines_dashed(struct graphics *this_, struct graphics_gc *gc, struct point *p, int count, int order, int oneway)
{
	this_->meth.draw_lines_dashed(this_->priv, gc->priv, p, count, order, oneway);
}

/**
 * FIXME
 * @param <>
 * @returns <>
 * @author Martin Schaller (04/2008)
 */
void graphics_draw_circle(struct graphics *this_, struct graphics_gc *gc, struct point *p, int r)
{
	int i = 0;

	if (this_->meth.draw_circle)
	{
		this_->meth.draw_circle(this_->priv, gc->priv, p, r);
	}
	else
	{
		struct point *pnt = g_alloca(sizeof(struct point) * (r * 4 + 64));
		draw_circle(p, r, 0, -1, 1026, pnt, &i, 1);
		pnt[i] = pnt[0];
		i++;
		this_->meth.draw_lines(this_->priv, gc->priv, pnt, i);
	}
}

/**
 * FIXME
 * @param <>
 * @returns <>
 * @author Martin Schaller (04/2008)
 */
void graphics_draw_rectangle(struct graphics *this_, struct graphics_gc *gc, struct point *p, int w, int h)
{
	this_->meth.draw_rectangle(this_->priv, gc->priv, p, w, h);
}

void graphics_draw_rectangle_rounded(struct graphics *this_, struct graphics_gc *gc, struct point *plu, int w, int h, int r, int fill)
{
	struct point *p = g_alloca(sizeof(struct point) * (r * 4 + 32));
	struct point pi0 = { plu->x + r, plu->y + r };
	struct point pi1 = { plu->x + w - r, plu->y + r };
	struct point pi2 = { plu->x + w - r, plu->y + h - r };
	struct point pi3 = { plu->x + r, plu->y + h - r };
	int i = 0;

	draw_circle(&pi2, r * 2, 0, -1, 258, p, &i, 1);
	draw_circle(&pi1, r * 2, 0, 255, 258, p, &i, 1);
	draw_circle(&pi0, r * 2, 0, 511, 258, p, &i, 1);
	draw_circle(&pi3, r * 2, 0, 767, 258, p, &i, 1);
	p[i] = p[0];
	i++;
	if (fill)
		this_->meth.draw_polygon(this_->priv, gc->priv, p, i);
	else
		this_->meth.draw_lines(this_->priv, gc->priv, p, i);
}

/**
 * FIXME
 * @param <>
 * @returns <>
 * @author Martin Schaller (04/2008)
 */
void graphics_draw_text(struct graphics *this_, struct graphics_gc *gc1, struct graphics_gc *gc2, struct graphics_font *font, char *text, struct point *p, int dx, int dy)
{
	this_->meth.draw_text(this_->priv, gc1->priv, gc2 ? gc2->priv : NULL, font->priv, text, p, dx, dy);
}

/**
 * FIXME
 * @param <>
 * @returns <>
 * @author Martin Schaller (04/2008)
 */
void graphics_get_text_bbox(struct graphics *this_, struct graphics_font *font, char *text, int dx, int dy, struct point *ret, int estimate)
{
	this_->meth.get_text_bbox(this_->priv, font->priv, text, dx, dy, ret, estimate);
}

/**
 * FIXME
 * @param <>
 * @returns <>
 * @author Martin Schaller (04/2008)
 */
void graphics_overlay_disable(struct graphics *this_, int disable)
{
	if (this_->meth.overlay_disable)
		this_->meth.overlay_disable(this_->priv, disable);
}

/**
 * FIXME
 * @param <>
 * @returns <>
 * @author Martin Schaller (04/2008)
 */
void graphics_draw_image(struct graphics *this_, struct graphics_gc *gc, struct point *p, struct graphics_image *img)
{
	this_->meth.draw_image(this_->priv, gc->priv, p, img->priv);
}

/**
 *
 *
 * @author Zoff (2011)
 */
void graphics_draw_bigmap(struct graphics *this_, struct graphics_gc *gc, int yaw, int order, float clat, float clng, int x, int y, int scx, int scy, int px, int py, int valid)
{
	this_->meth.draw_bigmap(this_->priv, gc->priv, yaw, order, clat, clng, x, y, scx, scy, px, py, valid);
}

//##############################################################################################################
//# Description:
//# Comment:
//# Authors: Martin Schaller (04/2008)
//##############################################################################################################
int graphics_draw_drag(struct graphics *this_, struct point *p)
{
__F_START__

	if (!this_->meth.draw_drag)
	{
		return2 0;
	}
	////DBG // dbg(0,"draw DRAG start ...\n");
	this_->meth.draw_drag(this_->priv, p);
	////DBG // dbg(0,"draw DRAG end ...\n");
	return2 1;

__F_END__

}

void graphics_background_gc(struct graphics *this_, struct graphics_gc *gc)
{
	////DBG // dbg(0,"ooo enter ooo\n");

	this_->meth.background_gc(this_->priv, gc ? gc->priv : NULL);
}

#include "attr.h"
#include "popup.h"
#include <stdio.h>

#if 0
//##############################################################################################################
//# Description:
//# Comment:
//# Authors: Martin Schaller (04/2008)
//##############################################################################################################
static void popup_view_html(struct popup_item *item, char *file)
{
	char command[1024];
	sprintf(command,"firefox %s", file);
	system(command);
}

struct transformatin *tg;
enum projection pg;

//##############################################################################################################
//# Description:
//# Comment:
//# Authors: Martin Schaller (04/2008)
//##############################################################################################################
static void graphics_popup(struct display_list *list, struct popup_item **popup)
{
	struct item *item;
	struct attr attr;
	struct map_rect *mr;
	struct coord c;
	struct popup_item *curr_item,*last=NULL;
	item=list->data;
	mr=map_rect_new(item->map, NULL, NULL, 0);
	printf("id hi=0x%x lo=0x%x\n", item->id_hi, item->id_lo);
	item=map_rect_get_item_byid(mr, item->id_hi, item->id_lo);
	if (item)
	{
		if (item_attr_get(item, attr_name, &attr))
		{
			curr_item=popup_item_new_text(popup,attr.u.str,1);
			if (item_attr_get(item, attr_info_html, &attr))
			{
				popup_item_new_func(&last,"HTML Info",1, popup_view_html, g_strdup(attr.u.str));
			}
			if (item_attr_get(item, attr_price_html, &attr))
			{
				popup_item_new_func(&last,"HTML Preis",2, popup_view_html, g_strdup(attr.u.str));
			}
			curr_item->submenu=last;
		}
	}
	map_rect_destroy(mr);
}
#endif

/**
 * FIXME
 * @param <>
 * @returns <>
 * @author Martin Schaller (04/2008)
 */
static void xdisplay_free(struct displaylist *dl)
{
	int i;
	for (i = 0; i < HASH_SIZE_GRAPHICS_; i++)
	{
		struct displayitem *di = dl->hash_entries[i].di;
		while (di)
		{
			struct displayitem *next = di->next;
			g_free(di);
			di = next;
		}
		dl->hash_entries[i].di = NULL;
	}
}

/**
 * FIXME
 * @param <>
 * @returns <>
 * @author Martin Schaller (04/2008)
 */
static void display_add(struct hash_entry *entry, struct item *item, int count, struct coord *c, char **label, int label_count, int color_yes_no, unsigned int color_int)
{
	struct displayitem *di;
	int len, i;
	// int len2;
	char *p;

	len = sizeof(*di) + count * sizeof(*c);
	if (label && label_count > 0)
	{
		for (i = 0; i < label_count; i++)
		{
			if (label[i])
			{
				//dbg(0, "label=X%sX\n", label[i]);
				len += strlen(label[i]);
				if (i < (label_count - 1))
				{
					len = len + 2; // put ', '(==2 chars) between labels!
				}
			}
		}
		len++; // NULL at the end
	}

	p = g_malloc(len);
	//len2 = len - (sizeof(*di) + count * sizeof(*c));
	//dbg(0,"malloc len:%d len2:%d\n", len, len2);

	di = (struct displayitem *) p;
	p += sizeof(*di) + count * sizeof(*c);
	di->item = *item;

	if (color_yes_no)
	{
		//di->color.r = r;
		//di->color.g = g;
		//di->color.b = b;
		//di->color.a = a;
		di->col_int_value = color_int;
	}
	else
	{
		// 0 --> no custom color is set
		//di->color.a = 0;
		di->col_int_value = (unsigned int)0;
	}

	if (label && label_count > 0)
	{
		di->label = p;
		for (i = 0; i < label_count; i++)
		{
			if (label[i])
			{
				strncpy(p, label[i], strlen(label[i])); // copy string without!! NULL byte at the end
				p += strlen(label[i]);
				if (i < (label_count - 1))
				{
					// put ', ' between labels!
					*p++ = ',';
					*p++ = ' ';
				}
			}
		}
		*p++ = '\0'; // add NULL at the end
		//p = di->label;
		//p = p + len2;
		//*p = '\0'; // for safety NULL at the real end!
		//dbg(0, "add:strlen=%d p=%s\n", strlen(di->label), di->label);
	}
	else
	{
		di->label = NULL;
	}

	di->count = count;
	memcpy(di->c, c, count * sizeof(*c));
	di->next = entry->di;
	entry->di = di;
}

/**
 * FIXME
 * @param <>
 * @returns <>
 * @author Martin Schaller (04/2008)
 */
static void label_line(struct graphics *gra, struct graphics_gc *fg, struct graphics_gc *bg, struct graphics_font *font, struct point *p, int count, char *label)
{
	int i, x, y, tl, tlm, th, thm, tlsq, l;
	float lsq;
	// double dx, dy;
	float dx, dy;
	struct point p_t;
	struct point pb[5];

	if (gra->meth.get_text_bbox)
	{
		gra->meth.get_text_bbox(gra->priv, font->priv, label, 0x10000, 0x0, pb, 1);
		// tl -> text length
		tl = (pb[2].x - pb[0].x);
		// th -> text height
		th = (pb[0].y - pb[1].y);
	}
	else
	{
		// tl -> text length
		tl = strlen(label) * 4;
		// th -> text height
		th = 8;
	}
	tlm = tl * 32;
	thm = th * 36;
	// tlsq -> (text length * 32) squared
	tlsq = tlm * tlm;
	for (i = 0; i < count - 1; i++)
	{
		dx = p[i + 1].x - p[i].x;
		dx *= 32;
		dy = p[i + 1].y - p[i].y;
		dy *= 32;
		// lsq -> (line length * 32) squared
		lsq = dx * dx + dy * dy;
		if (lsq > tlsq)
		{


			// warning: suggest parentheses around '&&' within '||' [-Wparentheses]
			if (((int) lsq > MIN_LINE_LENGTH_FOR_TEXT_MIDDLE_2) || (((i == 0) || (i == (count - 2)) && ((int) lsq > (int) MIN_LINE_LENGTH_FOR_TEXT_2))))
			{


				// segments in the middle of the "way" need to be longer for streetname to be drawn
				// l -> line length
				l = (int) sqrtf_fast2(lsq);
				x = p[i].x;
				y = p[i].y;
				if (dx < 0)
				{
					dx = -dx;
					dy = -dy;
					x = p[i + 1].x;
					y = p[i + 1].y;
				}
				x += (l - tlm) * dx / l / 64;
				y += (l - tlm) * dy / l / 64;
				x -= dy * thm / l / 64;
				y += dx * thm / l / 64;
				p_t.x = x;
				p_t.y = y;
#if 0
				//DBG // dbg(0,"display_text: '%s', %d, %d, %d, %d %d\n", label, x, y, dx*0x10000/l, dy*0x10000/l, l);
#endif
				if (x < gra->r.rl.x && x + tl > gra->r.lu.x && y + tl > gra->r.lu.y && y - tl < gra->r.rl.y)
				{
					gra->meth.draw_text(gra->priv, fg->priv, bg ? bg->priv : NULL, font->priv, label, &p_t, dx * 0x10000 / l, dy * 0x10000 / l);
				}
			}
		}
	}
}

static void display_draw_arrow(struct point *p, int dx, int dy, int l, struct graphics_gc *gc, struct graphics *gra)
{
	struct point pnt[3];
	pnt[0] = pnt[1] = pnt[2] = *p;
	pnt[0].x += -dx * l / 65536 + dy * l / 65536;
	pnt[0].y += -dy * l / 65536 - dx * l / 65536;
	pnt[2].x += -dx * l / 65536 - dy * l / 65536;
	pnt[2].y += -dy * l / 65536 + dx * l / 65536;
	gra->meth.draw_lines(gra->priv, gc->priv, pnt, 3);
}

static void display_draw_arrows(struct graphics *gra, struct graphics_gc *gc, struct point *pnt, int count)
{

	int i, dx, dy, l;
	struct point p;
	for (i = 0; i < count - 1; i++)
	{
		dx = pnt[i + 1].x - pnt[i].x;
		dy = pnt[i + 1].y - pnt[i].y;
		l = sqrt(dx * dx + dy * dy);
		if (l)
		{
			dx = dx * 65536 / l;
			dy = dy * 65536 / l;
			p = pnt[i];
			p.x += dx * 15 / 65536;
			p.y += dy * 15 / 65536;
			display_draw_arrow(&p, dx, dy, 10, gc, gra);
			p = pnt[i + 1];
			p.x -= dx * 15 / 65536;
			p.y -= dy * 15 / 65536;
			display_draw_arrow(&p, dx, dy, 10, gc, gra);
		}
	}
}

static int intersection(struct point * a1, int adx, int ady, struct point * b1, int bdx, int bdy, struct point * res)
{
	int n, a, b;
	n = bdy * adx - bdx * ady;
	a = bdx * (a1->y - b1->y) - bdy * (a1->x - b1->x);
	b = adx * (a1->y - b1->y) - ady * (a1->x - b1->x);
	if (n < 0)
	{
		n = -n;
		a = -a;
		b = -b;
	}
#if 0
	if (a < 0 || b < 0)
	return 0;
	if (a > n || b > n)
	return 0;
#endif
	if (n == 0)
		return 0;
	res->x = a1->x + a * adx / n;
	res->y = a1->y + a * ady / n;
	return 1;
}

struct circle
{
	short x, y, fowler;
}
		circle64[] =
				{ { 0, 128, 0 }, { 13, 127, 13 }, { 25, 126, 25 }, { 37, 122, 38 }, { 49, 118, 53 }, { 60, 113, 67 }, { 71, 106, 85 }, { 81, 99, 104 }, { 91, 91, 128 }, { 99, 81, 152 }, { 106, 71, 171 }, { 113, 60, 189 }, { 118, 49, 203 }, { 122, 37, 218 }, { 126, 25, 231 }, { 127, 13, 243 }, { 128, 0, 256 }, { 127, -13, 269 }, { 126, -25, 281 }, { 122, -37, 294 }, { 118, -49, 309 }, { 113, -60, 323 }, { 106, -71, 341 }, { 99, -81, 360 }, { 91, -91, 384 }, { 81, -99, 408 }, { 71, -106, 427 }, { 60, -113, 445 }, { 49, -118, 459 }, { 37, -122, 474 }, { 25, -126, 487 }, { 13, -127, 499 }, { 0, -128, 512 }, { -13, -127, 525 }, { -25, -126, 537 }, { -37, -122, 550 }, { -49, -118, 565 }, { -60, -113, 579 }, { -71, -106, 597 }, { -81, -99, 616 }, { -91, -91, 640 }, { -99, -81, 664 }, { -106, -71, 683 }, { -113, -60, 701 }, { -118, -49, 715 }, { -122, -37, 730 }, { -126, -25, 743 }, { -127, -13, 755 }, { -128, 0, 768 }, { -127, 13, 781 }, { -126, 25, 793 }, { -122, 37, 806 }, { -118, 49, 821 }, { -113, 60, 835 }, { -106, 71, 853 }, { -99, 81, 872 }, { -91, 91, 896 }, { -81, 99, 920 }, { -71, 106, 939 }, { -60, 113, 957 }, { -49, 118, 971 }, { -37, 122, 986 }, { -25, 126, 999 }, { -13, 127, 1011 }, };

static void draw_circle(struct point *pnt, int diameter, int scale, int start, int len, struct point *res, int *pos, int dir)
{
	struct circle *c;

#if 0
	//DBG // dbg(0,"diameter=%d start=%d len=%d pos=%d dir=%d\n", diameter, start, len, *pos, dir);
#endif
	int count = 64;
	int end = start + len;
	int i, step;
	c = circle64;
	if (diameter > 128)
		step = 1;
	else if (diameter > 64)
		step = 2;
	else if (diameter > 24)
		step = 4;
	else if (diameter > 8)
		step = 8;
	else
		step = 16;
	if (len > 0)
	{
		while (start < 0)
		{
			start += 1024;
			end += 1024;
		}
		while (end > 0)
		{
			i = 0;
			while (i < count && c[i].fowler <= start)
			{
				i += step;
			}
			while (i < count && c[i].fowler < end)
			{
				if (1 < *pos || 0 < dir)
				{
					res[*pos].x = pnt->x + ((c[i].x * diameter + 128) >> 8);
					res[*pos].y = pnt->y + ((c[i].y * diameter + 128) >> 8);
					(*pos) += dir;
				}
				i += step;
			}
			end -= 1024;
			start -= 1024;
		}
	}
	else
	{
		while (start > 1024)
		{
			start -= 1024;
			end -= 1024;
		}
		while (end < 1024)
		{
			i = count - 1;
			while (i >= 0 && c[i].fowler >= start)
				i -= step;
			while (i >= 0 && c[i].fowler > end)
			{
				if (1 < *pos || 0 < dir)
				{
					res[*pos].x = pnt->x + ((c[i].x * diameter + 128) >> 8);
					res[*pos].y = pnt->y + ((c[i].y * diameter + 128) >> 8);
					(*pos) += dir;
				}
				i -= step;
			}
			start += 1024;
			end += 1024;
		}
	}
}

static int fowler(int dy, int dx)
{
	int adx, ady; /* Absolute Values of Dx and Dy */
	int code; /* Angular Region Classification Code */

	adx = (dx < 0) ? -dx : dx; /* Compute the absolute values. */
	ady = (dy < 0) ? -dy : dy;

	code = (adx < ady) ? 1 : 0;
	if (dx < 0)
		code += 2;
	if (dy < 0)
		code += 4;

	switch (code)
	{
		case 0:
			return (dx == 0) ? 0 : 128 * ady / adx; /* [  0, 45] */
		case 1:
			return (256 - (128 * adx / ady)); /* ( 45, 90] */
		case 3:
			return (256 + (128 * adx / ady)); /* ( 90,135) */
		case 2:
			return (512 - (128 * ady / adx)); /* [135,180] */
		case 6:
			return (512 + (128 * ady / adx)); /* (180,225] */
		case 7:
			return (768 - (128 * adx / ady)); /* (225,270) */
		case 5:
			return (768 + (128 * adx / ady)); /* [270,315) */
		case 4:
			return (1024 - (128 * ady / adx));/* [315,360) */
	}
	return 0;
}

static int int_sqrt(unsigned int n)
{
	unsigned int h, p = 0, q = 1, r = n;

	/* avoid q rollover */
	if (n >= (1 << (sizeof(n) * 8 - 2)))
	{
		q = 1 << (sizeof(n) * 8 - 2);
	}
	else
	{
		while (q <= n)
		{
			q <<= 2;
		}
		q >>= 2;
	}

	while (q != 0)
	{
		h = p + q;
		p >>= 1;
		if (r >= h)
		{
			p += q;
			r -= h;
		}
		q >>= 2;
	}
	return p;
}

struct offset
{
	int px, py, nx, ny;
};

static void calc_offsets(int wi, int l, int dx, int dy, struct offset *res)
{
	int x, y;

	x = (dx * wi) / l;
	y = (dy * wi) / l;
	if (x < 0)
	{
		res->nx = -x / 2;
		res->px = (x - 1) / 2;
	}
	else
	{
		res->nx = -(x + 1) / 2;
		res->px = x / 2;
	}
	if (y < 0)
	{
		res->ny = -y / 2;
		res->py = (y - 1) / 2;
	}
	else
	{
		res->ny = -(y + 1) / 2;
		res->py = y / 2;
	}
}

// this func. is now obsolete!! and unused!!!!
// this func. is now obsolete!! and unused!!!!
// this func. is now obsolete!! and unused!!!!
static void graphics_draw_polyline_as_polygon(struct graphics *gra, struct graphics_gc *gc, struct point *pnt, int count, int *width, int step, int fill, int order, int oneway)
{
	int maxpoints = 200;
	struct point *res = g_alloca(sizeof(struct point) * maxpoints);
	struct point pos, poso, neg, nego;
	int i, dx = 0, dy = 0, l = 0, dxo = 0, dyo = 0;
	struct offset o, oo = { 0, 0, 0, 0 };
	int fow = 0, fowo = 0, delta;
	int wi, ppos = maxpoints / 2, npos = maxpoints / 2;
	int state, prec = 5;
	int max_circle_points = 20;
	int lscale = 16;
	i = 0;
	for (;;)
	{
		wi = *width;
		width += step;
		if (i < count - 1)
		{
			int dxs, dys, lscales;

			dx = (pnt[i + 1].x - pnt[i].x);
			dy = (pnt[i + 1].y - pnt[i].y);
#if 0
			l = int_sqrt(dx * dx * lscale * lscale + dy * dy * lscale * lscale);
#else
			dxs = dx * dx;
			dys = dy * dy;
			lscales = lscale * lscale;
			if (dxs + dys > lscales)
				l = int_sqrt(dxs + dys) * lscale;
			else
				l = int_sqrt((dxs + dys) * lscales);
#endif
			fow = fowler(-dy, dx);
		}
		if (!l)
			l = 1;
		if (wi * lscale > 10000)
			lscale = 10000 / wi;
		dbg_assert(wi * lscale <= 10000);
		calc_offsets(wi * lscale, l, dx, dy, &o);
		pos.x = pnt[i].x + o.ny;
		pos.y = pnt[i].y + o.px;
		neg.x = pnt[i].x + o.py;
		neg.y = pnt[i].y + o.nx;
		if (!i)
			state = 0;
		else if (i == count - 1)
			state = 2;
		else if (npos < max_circle_points || ppos >= maxpoints - max_circle_points)
			state = 3;
		else
			state = 1;
		switch (state)
		{
			case 1:
				if (fowo != fow)
				{
					poso.x = pnt[i].x + oo.ny;
					poso.y = pnt[i].y + oo.px;
					nego.x = pnt[i].x + oo.py;
					nego.y = pnt[i].y + oo.nx;
					delta = fowo - fow;
					if (delta < 0)
						delta += 1024;
					if (delta < 512)
					{
						if (intersection(&pos, dx, dy, &poso, dxo, dyo, &res[ppos]))
						{
							ppos++;
						}
						res[--npos] = nego;
						--npos;
						if (fill == 1)
						{
							if (draw_polylines_fast == 0)
							{
								draw_circle(&pnt[i], wi, prec, fowo - 512, -delta, res, &npos, -1);
							}
						}
						res[npos] = neg;
					}
					else
					{
						res[ppos++] = poso;
						if (fill == 1)
						{
							if (draw_polylines_fast == 0)
							{
								draw_circle(&pnt[i], wi, prec, fowo, 1024 - delta, res, &ppos, 1);
							}
						}
						res[ppos++] = pos;
						if (intersection(&neg, dx, dy, &nego, dxo, dyo, &res[npos - 1]))
						{
							npos--;
						}
					}
				}
				break;
			case 2:
			case 3:
				res[--npos] = neg;
				--npos;
				if (fill == 1)
				{
					if (draw_polylines_fast == 0)
					{
						draw_circle(&pnt[i], wi, prec, fow - 512, -512, res, &npos, -1);
					}
				}
				res[npos] = pos;
				res[ppos++] = pos;
				dbg_assert(npos > 0);
				dbg_assert(ppos < maxpoints);
				if (fill == 1)
				{
					gra->meth.draw_polygon2(gra->priv, gc->priv, res + npos, ppos - npos, order, oneway);
				}
				else
				{
					gra->meth.draw_lines_dashed(gra->priv, gc->priv, res + npos, ppos - npos, order, oneway);
				}
				if (state == 2)
					break;
				npos = maxpoints / 2;
				ppos = maxpoints / 2;
			case 0:
				res[ppos++] = neg;
				if (fill == 1)
				{
					if (draw_polylines_fast == 0)
					{
						draw_circle(&pnt[i], wi, prec, fow + 512, 512, res, &ppos, 1);
					}
				}
				res[ppos++] = pos;
				break;
		}

		i++;

		if (i >= count)
		{
			break;
		}

		if (step)
		{
			wi = *width;
			calc_offsets(wi * lscale, l, dx, dy, &oo);
		}
		else
		{
			oo = o;
		}

		dxo = -dx;
		dyo = -dy;
		fowo = fow;
	}
}
// this func. is now obsolete!! and unused!!!!
// this func. is now obsolete!! and unused!!!!
// this func. is now obsolete!! and unused!!!!


struct wpoint
{
	int x, y, w;
};

static int clipcode(struct wpoint *p, struct point_rect *r)
{
	int code = 0;
	if (p->x < r->lu.x)
		code = 1;
	if (p->x > r->rl.x)
		code = 2;
	if (p->y < r->lu.y)
		code |= 4;
	if (p->y > r->rl.y)
		code |= 8;
	return code;
}

#define	DONT_INTERSECT    0
#define	DO_INTERSECT      1
#define COLLINEAR         2
#define SAME_SIGNS( a, b )	\
		(((long) ((unsigned long) a ^ (unsigned long) b)) >= 0 )

static int lines_intersect(int x1, int y1, /* First line segment */
int x2, int y2,

int x3, int y3, /* Second line segment */
int x4, int y4,

int *x, int *y /* Output value:
 * point of intersection */
)
{
	int a1, a2, b1, b2, c1, c2; /* Coefficients of line eqns. */
	int r1, r2, r3, r4; /* 'Sign' values */
	int denom, offset, num; /* Intermediate values */

	/* Compute a1, b1, c1, where line joining points 1 and 2
	 * is "a1 x  +  b1 y  +  c1  =  0".
	 */

	a1 = y2 - y1;
	b1 = x1 - x2;
	c1 = x2 * y1 - x1 * y2;

	/* Compute r3 and r4.
	 */
	r3 = a1 * x3 + b1 * y3 + c1;
	r4 = a1 * x4 + b1 * y4 + c1;

	/* Check signs of r3 and r4.  If both point 3 and point 4 lie on
	 * same side of line 1, the line segments do not intersect.
	 */
	if (r3 != 0 && r4 != 0 && SAME_SIGNS( r3, r4 ))
	{
		return (DONT_INTERSECT);
	}

	/* Compute a2, b2, c2 */
	a2 = y4 - y3;
	b2 = x3 - x4;
	c2 = x4 * y3 - x3 * y4;

	/* Compute r1 and r2 */
	r1 = a2 * x1 + b2 * y1 + c2;
	r2 = a2 * x2 + b2 * y2 + c2;

	/* Check signs of r1 and r2.  If both point 1 and point 2 lie
	 * on same side of second line segment, the line segments do
	 * not intersect.
	 */
	if (r1 != 0 && r2 != 0 && SAME_SIGNS( r1, r2 ))
	{
		return (DONT_INTERSECT);
	}

	/* Line segments intersect: compute intersection point.
	 */

	denom = a1 * b2 - a2 * b1;
	if (denom == 0)
	{
		return (COLLINEAR);
	}
	offset = denom < 0 ? -denom / 2 : denom / 2;

	/* The denom/2 is to get rounding instead of truncating.  It
	 * is added or subtracted to the numerator, depending upon the
	 * sign of the numerator.
	 */
	/*
	 num = b1 * c2 - b2 * c1;
	 *x = ( num < 0 ? num - offset : num + offset ) / denom;
	 num = a2 * c1 - a1 * c2;
	 *y = ( num < 0 ? num - offset : num + offset ) / denom;
	 */

	return (DO_INTERSECT);
} /* lines_intersect */

static int clip_line_aprox(struct wpoint *p1, struct wpoint *p2, struct point_rect *r)
{
	int code1, code2;
	code1 = clipcode(p1, r);
	code2 = clipcode(p2, r);
	if (code1 & code2)
	{
		// line completely invisible!
		return 0;
	}
	else if ((code1 == 0) && (code2 == 0))
	{
		// line completely visible!
		return 1;
	}
	else if ((code1 == 0) || (code2 == 0))
	{
		// at least 1 point of line is visible
		return 2;
	}
	else
	{
		int xx;
		int yy;
		// top
		int ret_ = lines_intersect(p1->x, p1->y, p2->x, p2->y, r->lu.x, r->lu.y, r->rl.x, r->lu.y, &xx, &yy);
		if (ret_ == DO_INTERSECT)
		{
			return 3;
		}
		// bottom
		ret_ = lines_intersect(p1->x, p1->y, p2->x, p2->y, r->lu.x, r->rl.y, r->rl.x, r->rl.y, &xx, &yy);
		if (ret_ == DO_INTERSECT)
		{
			return 3;
		}
		// left
		ret_ = lines_intersect(p1->x, p1->y, p2->x, p2->y, r->lu.x, r->lu.y, r->lu.x, r->rl.y, &xx, &yy);
		if (ret_ == DO_INTERSECT)
		{
			return 3;
		}
		// right
		ret_ = lines_intersect(p1->x, p1->y, p2->x, p2->y, r->rl.x, r->lu.y, r->rl.x, r->rl.y, &xx, &yy);
		if (ret_ == DO_INTERSECT)
		{
			return 3;
		}
	}
	// not visible
	return 0;
}

static int clip_line(struct wpoint *p1, struct wpoint *p2, struct point_rect *r)
{
	int code1, code2, ret = 1;
	int dx, dy, dw;
	code1 = clipcode(p1, r);
	if (code1)
		ret |= 2;
	code2 = clipcode(p2, r);
	if (code2)
		ret |= 4;
	dx = p2->x - p1->x;
	dy = p2->y - p1->y;
	dw = p2->w - p1->w;
	while (code1 || code2)
	{
		if (code1 & code2)
		{
			return 0;
		}
		if (code1 & 1)
		{
			p1->y += (r->lu.x - p1->x) * dy / dx;
			p1->w += (r->lu.x - p1->x) * dw / dx;
			p1->x = r->lu.x;
		}
		else if (code1 & 2)
		{
			p1->y += (r->rl.x - p1->x) * dy / dx;
			p1->w += (r->rl.x - p1->x) * dw / dx;
			p1->x = r->rl.x;
		}
		else if (code1 & 4)
		{
			p1->x += (r->lu.y - p1->y) * dx / dy;
			p1->w += (r->lu.y - p1->y) * dw / dy;
			p1->y = r->lu.y;
		}
		else if (code1 & 8)
		{
			p1->x += (r->rl.y - p1->y) * dx / dy;
			p1->w += (r->rl.y - p1->y) * dw / dy;
			p1->y = r->rl.y;
		}
		code1 = clipcode(p1, r);
		if (code1 & code2)
			return 0;
		if (code2 & 1)
		{
			p2->y += (r->lu.x - p2->x) * dy / dx;
			p2->w += (r->lu.x - p2->x) * dw / dx;
			p2->x = r->lu.x;
		}
		else if (code2 & 2)
		{
			p2->y += (r->rl.x - p2->x) * dy / dx;
			p2->w += (r->rl.x - p2->x) * dw / dx;
			p2->x = r->rl.x;
		}
		else if (code2 & 4)
		{
			p2->x += (r->lu.y - p2->y) * dx / dy;
			p2->w += (r->lu.y - p2->y) * dw / dy;
			p2->y = r->lu.y;
		}
		else if (code2 & 8)
		{
			p2->x += (r->rl.y - p2->y) * dx / dy;
			p2->w += (r->rl.y - p2->y) * dw / dy;
			p2->y = r->rl.y;
		}
		code2 = clipcode(p2, r);
	}
	return ret;
}



// --------------- COLORs ---------------
static struct color bicycle_blue =	{ 0x0000,0x0000,0xf9f9,0xffff }; // RR GG BB AA
static struct color bicycle_green =	{ 0x0808,0x8a8a,0x0808,0xffff }; // RR GG BB AA
// --------------- COLORs ---------------


static void graphics_draw_polyline_clipped(struct graphics *gra, struct graphics_gc *gc, struct point *pa, int count, int *width, int step, int poly, int order, int oneway, int dashes, struct color *c, int mark_way)
{

	struct point *p = g_alloca(sizeof(struct point) * (count + 1));
	struct wpoint p1, p2;
	int i;
	int code;
	int wmax;
	int out = 0;
	// const int max_segs = 2000;
	int max_segs = 20; // send max this many segments to java with one single call
	struct point_rect r = gra->r;


	if (order < global_order_level_for_fast_draw)
	{
		max_segs = 100;
	}

	//if (count > 30)
	//{
	//	dbg(0,"segment count=%d\n", count);
	//}

#if 0
	// check if whole line is within a 2x2 pixel square
	if (order < 11)
	{
		const int max_dist = 2*2;
		int need_draw = 0;
		int diff;
		for (i = 0; i < count; i++)
		{
			if (i > 0)
			{
				p2.x = pa[i].x;
				p2.y = pa[i].y;
				diff = (p2.x - p1.x) * (p2.y - p1.y);
				if (diff < 0)
				{
					diff = -diff;
				}

				if (diff > max_dist)
				{
					// line is bigger, so we need to draw it
					need_draw = 1;
					break;
				}
			}
			else
			{
				p1.x = pa[i - 1].x;
				p1.y = pa[i - 1].y;
			}
		}

		if (need_draw == 0)
		{
			// dont draw this line
			return;
		}
	}
#endif

	// calc visible area on screen
	wmax = width[0];
	r.lu.x -= wmax;
	r.lu.y -= wmax;
	r.rl.x += wmax;
	r.rl.y += wmax;

	for (i = 0; i < count; i++)
	{
		if (i > 0)
		{
			p1.x = pa[i - 1].x;
			p1.y = pa[i - 1].y;
			p2.x = pa[i].x;
			p2.y = pa[i].y;
			/* 0 = invisible, 1 = completely visible, 2,3 = at least part of line visible */
			code = clip_line_aprox(&p1, &p2, &r);
			// code = 1;

			if (code > 0)
			{
				if (out == 0)
				{
					p[out].x = p1.x;
					p[out].y = p1.y;
					out++;
				}
				p[out].x = p2.x;
				p[out].y = p2.y;
				out++;

				if ((out <= max_segs) && (i < (count - 1)))
				{
					// ok gather more line segs
					continue;
				}
			}
			else // (code == 0)
			{
				if (out == 0)
				{
					// first visible line seg not yet found, search on ...
					continue;
				}
			}

			// PAINT --- LINE SEGMENTS ------------
			// PAINT --- LINE SEGMENTS ------------

			if ((poly == 1) || (poly == 0))
			{
				// normal street
				//if (1 == 0)
				//{
				//	// draw as polygon --> OLD method
				//	graphics_draw_polyline_as_polygon(gra, gc, p, out, w, step, 1, order, 0);
				//}
				//else
				//{
				// draw as line
				if (mark_way == 1)
				{
					// dbg(0, "CYCLE LANE:001\n");
					// mark way with bicycle lanes
					gra->meth.draw_lines3(gra->priv, gc->priv, p, out, order, width[i] + 4, dashes, &bicycle_green, global_clinedrawing_active, 0);
				}
				else if (mark_way == 2)
				{
					// dbg(0, "CYCLE LANE:001\n");
					// mark way with bicycle tracks
					gra->meth.draw_lines3(gra->priv, gc->priv, p, out, order, width[i] + 4, dashes, &bicycle_blue, global_clinedrawing_active, 0);
				}
				gra->meth.draw_lines3(gra->priv, gc->priv, p, out, order, width[i], dashes, c, global_clinedrawing_active, 1);
				//draw_lines_count_3++;
				//}

				// one way arrow
				if ((oneway > 0) && (order > 13))
				{
					gra->meth.draw_lines2(gra->priv, gc->priv, p, out, order, oneway);
					//draw_lines_count_2++;
				}
			}
			else if (poly == 2)
			{
				// ******* street is underground ********
				// ******* street is underground ********
				// ******* street is underground ********

				//if (1 == 0)
				//{
				//	// draw as polygon --> OLD method
				//	graphics_draw_polyline_as_polygon(gra, gc, p, out, w, step, 0, order, 0);
				//}
				//else
				//{

				if (mark_way == 1)
				{
					// mark way with bicycle lanes
					gra->meth.draw_lines4(gra->priv, gc->priv, p, out, order, width[i] + 4, 1, dashes, &bicycle_green, 0);
				}
				else if (mark_way == 2)
				{
					// mark way with bicycle track
					gra->meth.draw_lines4(gra->priv, gc->priv, p, out, order, width[i] + 4, 1, dashes, &bicycle_blue, 0);
				}

				// draw as line
				gra->meth.draw_lines4(gra->priv, gc->priv, p, out, order, width[i], 1, dashes, c, 1);
				//draw_lines_count_4++;
				//}

				// one way arrow
				if ((oneway > 0) && (order > 13))
				{
					gra->meth.draw_lines2(gra->priv, gc->priv, p, out, order, oneway);
					//draw_lines_count_2++;
				}
			}
			else if (poly == 3)
			{
				// ******* street has bridge ********
				// ******* street has bridge ********
				// ******* street has bridge ********

				if (mark_way == 1)
				{
					// mark way with bicycle lanes
					gra->meth.draw_lines4(gra->priv, gc->priv, p, out, order, width[i] + 4, 2, dashes, &bicycle_green, 0);
				}
				else if (mark_way == 2)
				{
					// mark way with bicycle track
					gra->meth.draw_lines4(gra->priv, gc->priv, p, out, order, width[i] + 4, 2, dashes, &bicycle_blue, 0);
				}

				gra->meth.draw_lines4(gra->priv, gc->priv, p, out, order, width[i], 2, dashes, c, 1);
				//draw_lines_count_4++;

				// one way arrow
				if ((oneway > 0) && (order > 13))
				{
					gra->meth.draw_lines2(gra->priv, gc->priv, p, out, order, oneway);
					//draw_lines_count_2++;
				}
			}
			// --> now NOT used anymore!!
			else // poly==0 -> street that is only a line (width=1)
			{
				// OLD // gra->meth.draw_lines2(gra->priv, gc->priv, p, out, order, 0);

				gra->meth.draw_lines3(gra->priv, gc->priv, p, out, order, width[i], dashes, c, global_clinedrawing_active, 1);
				//draw_lines_count_3++;

				// one way arrow
				if ((oneway > 0) && (order > 13))
				{
					gra->meth.draw_lines2(gra->priv, gc->priv, p, out, order, oneway);
					//draw_lines_count_2++;
				}
			}

			out = 0; // reset point counter after painting
			// PAINT --- LINE SEGMENTS ------------
			// PAINT --- LINE SEGMENTS ------------
			// PAINT --- LINE SEGMENTS ------------
		}
	}
}

static int is_inside(struct point *p, struct point_rect *r, int edge)
{
	switch (edge)
	{
		case 0:
			return p->x >= r->lu.x;
		case 1:
			return p->x <= r->rl.x;
		case 2:
			return p->y >= r->lu.y;
		case 3:
			return p->y <= r->rl.y;
		default:
			return 0;
	}
}

static void poly_intersection(struct point *p1, struct point *p2, struct point_rect *r, int edge, struct point *ret)
{
	int dx = p2->x - p1->x;
	int dy = p2->y - p1->y;
	switch (edge)
	{
		case 0:
			ret->y = p1->y + (r->lu.x - p1->x) * dy / dx;
			ret->x = r->lu.x;
			break;
		case 1:
			ret->y = p1->y + (r->rl.x - p1->x) * dy / dx;
			ret->x = r->rl.x;
			break;
		case 2:
			ret->x = p1->x + (r->lu.y - p1->y) * dx / dy;
			ret->y = r->lu.y;
			break;
		case 3:
			ret->x = p1->x + (r->rl.y - p1->y) * dx / dy;
			ret->y = r->rl.y;
			break;
	}
}

static void graphics_draw_polygon_clipped(struct graphics *gra, struct graphics_gc *gc, struct point *pin, int count_in)
{
	struct point_rect r = gra->r;
	struct point *pout, *p, *s, pi, *p1, *p2;
	int limit = 10000;
	struct point *pa1 = g_alloca(sizeof(struct point) * (count_in < limit ? count_in * 8 + 1 : 0));
	struct point *pa2 = g_alloca(sizeof(struct point) * (count_in < limit ? count_in * 8 + 1 : 0));
	int count_out, edge = 3;
	int i;
#if 0
	r.lu.x+=20;
	r.lu.y+=20;
	r.rl.x-=20;
	r.rl.y-=20;
#endif
	if (count_in < limit)
	{
		p1 = pa1;
		p2 = pa2;
	}
	else
	{
		p1 = g_new0(struct point, count_in*8+1);
		p2 = g_new0(struct point, count_in*8+1);
	}

	pout=p1;
	for (edge = 0; edge < 4; edge++)
	{
		p=pin;
		s=pin+count_in-1;
		count_out=0;
		for (i = 0; i < count_in; i++)
		{
			if (is_inside(p, &r, edge))
			{
				if (! is_inside(s, &r, edge))
				{
					poly_intersection(s,p,&r,edge,&pi);
					pout[count_out++]=pi;
				}
				pout[count_out++]=*p;
			}
			else
			{
				if (is_inside(s, &r, edge))
				{
					poly_intersection(p,s,&r,edge,&pi);
					pout[count_out++]=pi;
				}
			}
			s=p;
			p++;
		}
		count_in=count_out;
		if (pin == p1)
		{
			pin=p2;
			pout=p1;
		}
		else
		{
			pin=p1;
			pout=p2;
		}
	}



	gra->meth.draw_polygon(gra->priv, gc->priv, pin, count_in);
	if (count_in >= limit)
	{
		g_free(p1);
		g_free(p2);
	}
}

static void display_context_free(struct display_context *dc)
{
	if (dc->gc)
		graphics_gc_destroy(dc->gc);
	if (dc->gc_background)
		graphics_gc_destroy(dc->gc_background);
	if (dc->img)
		graphics_image_free(dc->gra, dc->img);
	dc->gc = NULL;
	dc->gc_background = NULL;
	dc->img = NULL;
}

static struct graphics_font *
get_font(struct graphics *gra, int size)
{
	if (size > 64)
	{
		size = 64;
	}

	if (size >= gra->font_len)
	{
		gra->font=g_renew(struct graphics_font *, gra->font, size+1);
		while (gra->font_len <= size)
		{
			gra->font[gra->font_len++] = NULL;
		}
	}

	if (!gra->font[size])
	{
		gra->font[size] = graphics_font_new(gra, size * gra->font_size, 0);
	}

	return gra->font[size];
}

void graphics_draw_text_std(struct graphics *this_, int text_size, char *text, struct point *p)
{
	struct graphics_font *font = get_font(this_, text_size);
	struct point bbox[4];
	int i;

	graphics_get_text_bbox(this_, font, text, 0x10000, 0, bbox, 0);
	for (i = 0; i < 4; i++)
	{
		bbox[i].x += p->x;
		bbox[i].y += p->y;
	}
	graphics_draw_rectangle(this_, this_->gc[2], &bbox[1], bbox[2].x - bbox[0].x, bbox[0].y - bbox[1].y + 5);
	graphics_draw_text(this_, this_->gc[1], this_->gc[2], font, text, p, 0x10000, 0);
}

char *
graphics_icon_path(char *icon)
{
	static char *navit_sharedir;
	char *ret = NULL;
	struct file_wordexp *wordexp = NULL;
	// dbg(1, "enter %s\n", icon);
	if (strchr(icon, '$'))
	{
		wordexp = file_wordexp_new(icon);
		if (file_wordexp_get_count(wordexp))
		{
			icon = file_wordexp_get_array(wordexp)[0];
		}
	}

	if (strchr(icon, '/'))
	{
		ret = g_strdup(icon);
	}
	else
	{
#ifdef HAVE_API_ANDROID
		// get resources for the correct screen density
		//
		// this part not needed, android unpacks only the correct version into res/drawable dir!
		// // dbg(1,"android icon_path %s\n",icon);
		// static char *android_density;
		// android_density = getenv("ANDROID_DENSITY");
		// ret=g_strdup_printf("res/drawable-%s/%s",android_density ,icon);
		ret=g_strdup_printf("res/drawable/%s" ,icon);
#else
		if (!navit_sharedir)
		{
			navit_sharedir = getenv("NAVIT_SHAREDIR");
		}
		ret = g_strdup_printf("%s/xpm/%s", navit_sharedir, icon);
#endif
	}

	if (wordexp)
	{
		file_wordexp_destroy(wordexp);
	}

	return ret;
}

static int limit_count(struct coord *c, int count)
{
	int i;
	for (i = 1; i < count; i++)
	{
		if (c[i].x == c[0].x && c[i].y == c[0].y)
			return i + 1;
	}
	return count;
}

static void displayitem_draw(struct displayitem *di, void *dummy, struct display_context *dc, int order, int allow_dashed, int run_type, int is_first)
{

	int *width = g_alloca(sizeof(int) * dc->maxlen);
	struct point *pa = g_alloca(sizeof(struct point) * dc->maxlen);
	struct graphics *gra = dc->gra;
	struct graphics_gc *gc = dc->gc;
	struct element *e = dc->e;
	struct graphics_image *img = dc->img;
	struct point p;
	char *path;
	struct color custom_color;
	int dont_draw = 0;

	int oneway;
	int mark_way;

	//if (run_type > 100) // dbg(0,"enter\n");

	while (di)
	{

		// stop drawing is requested
		if (cancel_drawing_global == 1)
		{
			break;
		}

		if (run_type != 99)
		{
			if (run_type == 1)
			{
				if (di->item.flags & NAVIT_AF_UNDERGROUND)
				{
					// next item
					di = di->next;
					continue;
				}
				else if (di->item.flags & NAVIT_AF_BRIDGE)
				{
					// next item
					di = di->next;
					continue;
				}
			}
			else if (run_type == 2)
			{
				// tunnel
				if (di->item.flags & NAVIT_AF_UNDERGROUND)
				{
				}
				else
				{
					// next item
					di = di->next;
					continue;
				}
			}
			else if (run_type == 3)
			{
				// bridge
				if (di->item.flags & NAVIT_AF_BRIDGE)
				{
				}
				else
				{
					// next item
					di = di->next;
					continue;
				}
			}
		}

		int i, count = di->count, mindist = dc->mindist;

		if (!gc)
		{
			gc = graphics_gc_new(gra);

			if (dc->e->type == element_polyline)
			{
				if (global_night_mode == 1)
				{
					graphics_gc_set_foreground(gc, &e->u.polyline.nightcol);
					// dbg(0, "nightcol set draw fg %d %d %d\n", e->u.polyline.nightcol.r , e->u.polyline.nightcol.g, e->u.polyline.nightcol.b);
				}
				else
				{
					graphics_gc_set_foreground(gc, &e->color);
				}
			}
			else
			{
				graphics_gc_set_foreground(gc, &e->color);
			}
			dc->gc = gc;
		}

		if (item_type_is_area(dc->type) && (dc->e->type == element_polyline || dc->e->type == element_text))
		{
			count = limit_count(di->c, count);
		}

		if (dc->type == type_poly_water_tiled)
		{
			mindist = 0;
			if (order < 12)
			{
				mindist = 4;
			}
		}
		else if (dc->type == type_border_country)
		{
			if (order < 10)
			{
				mindist = 4;
			}
		}
		else if (dc->type == type_poly_wood_from_triang)
		{
			if (order > 11)
			{
				mindist = 0;
			}
		}
		else if (dc->type == type_poly_water_from_triang)
		{
			if (order > 10)
			{
				mindist = 0;
			}
		}
#if 0
		else
		{
			if (order < global_order_level_for_fast_draw)
			{
				dbg(0, "else: mindist_old=%d\n", mindist);
				mindist = 5;
				dbg(0, "else: mindist_new=%d\n", mindist);
			}
		}
#endif
		//dbg(0, "mindist now=%d\n", mindist);


		if (dc->e->type == element_polyline)
		{
			count = transform(dc->trans, dc->pro, di->c, pa, count, mindist, e->u.polyline.width, width);
		}
		else
		{
			count = transform(dc->trans, dc->pro, di->c, pa, count, mindist, 0, NULL);
		}

		//dbg(0,"**dc->type=%s count=%d\n", item_to_name(dc->type), count);
		//dbg(0,"** e->type=%s\n", item_to_name(e->type));


		// system_log(0,"**dc->type=%s count=%d\n", item_to_name(dc->type), count);
		// system_log(0,"** e->type=%s int=%d\n", item_to_name(e->type), e->type);

		switch (e->type)
		{
			case element_polygon:
				graphics_draw_polygon_clipped(gra, gc, pa, count);
				break;
			case element_polyline:
			{
				gc->meth.gc_set_linewidth(gc->priv, 1);

				int poly = e->u.polyline.width > 1;

				// detect underground streets/lines/etc ...
				//if ((allow_dashed) && (di->item.flags & NAVIT_AF_UNDERGROUND))
				if (di->item.flags & NAVIT_AF_UNDERGROUND)
				{
					poly = 2;
				}
				else if (di->item.flags & NAVIT_AF_BRIDGE)
				{
					poly = 3;
				}

				oneway = 0;
				if (di->item.flags & NAVIT_AF_ONEWAYREV)
				{
					oneway = 2;
					if (di->item.flags & NAVIT_AF_ONEWAY_BICYCLE_NO)
					{
						oneway = oneway + 4; // oneway does not apply to bicycles here
					}
				}
				else if (di->item.flags & NAVIT_AF_ONEWAY)
				{
					oneway = 1;
					if (di->item.flags & NAVIT_AF_ONEWAY_BICYCLE_NO)
					{
						oneway = oneway + 4; // oneway does not apply to bicycles here
					}
				}

				mark_way = 0;
				// dbg(0, "CYCLE LANE:002 is_first=%d\n", is_first);
				if ((dc->type != type_cycleway) && (is_first == 1) && ((global_vehicle_profile == 1) || (global_vehicle_profile == 2)))
				{
					if (di->item.flags & NAVIT_AF_BICYCLE_LANE)
					{
						mark_way = 1; // way with cycle lane
					}
					else if (di->item.flags & NAVIT_AF_BICYCLE_TRACK)
					{
						mark_way = 2; // way with cycle track (sperate bicycle lane)
					}
				}

				// -------- apply dashes -------
				//if (e->u.polyline.dash_num > 0)
				//{
				//	graphics_gc_set_dashes(gra, gc, e->u.polyline.width, e->u.polyline.offset, e->u.polyline.dash_table, e->u.polyline.dash_num, order);
				//}
				// -------- apply dashes -------

				//for (i = 0; i < count; i++)
				//{
				//	if (width[i] < 1)
				//	{
				//		width[i] = 1;
				//	}
				//}

				if (dc->type == type_border_country)
				{
					graphics_draw_polyline_clipped(gra, gc, pa, count, width, 99, poly, order, oneway, e->u.polyline.dash_num, &e->color, 0);
				}
				else
				{
					// use custom color for underground trains
					if (dc->type == type_rail_subway)
					{
						//dbg(0, "colour1=r:%d g:%d b:%d a:%d\n", e->color.r, e->color.g, e->color.b, e->color.a);
						if (di->col_int_value != 0)
						{
							//dbg(0, "colour2=r:%d g:%d b:%d a:%d\n", di->color.r, di->color.g, di->color.b, di->color.a);
							//e->color.r = di->color.r;
							//e->color.g = di->color.g;
							//e->color.b = di->color.b;
							//e->color.a = di->color.a;
							custom_color.r = (di->col_int_value >> 16) & 0xff;
							custom_color.g = (di->col_int_value >> 8) & 0xff;
							custom_color.b = di->col_int_value & 0xff;

							custom_color.r = custom_color.r << 8;
							custom_color.g = custom_color.g << 8;
							custom_color.b = custom_color.b << 8;

							custom_color.a = 0xffff;

							graphics_draw_polyline_clipped(gra, gc, pa, count, width, 1, poly, order, oneway, e->u.polyline.dash_num, &custom_color, 0);
						}
						else
						{
							if (global_night_mode == 1)
							{
								graphics_draw_polyline_clipped(gra, gc, pa, count, width, 1, poly, order, oneway, e->u.polyline.dash_num, &e->u.polyline.nightcol, 0);
							}
							else
							{
								graphics_draw_polyline_clipped(gra, gc, pa, count, width, 1, poly, order, oneway, e->u.polyline.dash_num, &e->color, 0);
							}
						}
					}
					// change color of route if in bicycle-route-mode and we want to drive against the oneway street
					else if ((dc->type == type_street_route || dc->type == type_street_route_waypoint) && (global_vehicle_profile == 1) && ((di->col_int_value >> 26) & 3))
					{
						// if oneway and route goes against it
						if (  ((di->col_int_value >> 24) & 2) && ((di->col_int_value >> 26) & 1)  )
						{
							//struct attr attr_98;
							//if (item_attr_get(&(di->item), attr_direction, &attr_98))
							//{

							// custom_color.a = ((di->col_int_value >> 24) & 3); // "1" == 1 , "-1" == 3 , "0" == 0 direction of route on street
							// ((di->col_int_value >> 26) & 3) // 1 == NAVIT_AF_ONEWAY, 2 == NAVIT_AF_ONEWAYREV

							// dbg(0, "direction(2).0=%x\n", di->col_int_value);
							// dbg(0, "direction(2)=%x oneway bicycle=%d\n", custom_color.a, ((di->col_int_value >> 26) & 3));
							//}

							// mark route in ORANGE here
							custom_color.r = 0xff << 8;
							custom_color.g = 0x80 << 8;
							custom_color.b = 0x00 << 8;

							custom_color.a = 0xffff;

							graphics_draw_polyline_clipped(gra, gc, pa, count, width, 1, poly, order, oneway, e->u.polyline.dash_num, &custom_color, 0);
						}
						else
						{
							graphics_draw_polyline_clipped(gra, gc, pa, count, width, 1, poly, order, oneway, e->u.polyline.dash_num, &e->color, 0);
						}
					}
					else // all other ways
					{
						if (global_night_mode == 1)
						{
							graphics_draw_polyline_clipped(gra, gc, pa, count, width, 1, poly, order, oneway, e->u.polyline.dash_num, &e->u.polyline.nightcol, mark_way);
						}
						else
						{
							graphics_draw_polyline_clipped(gra, gc, pa, count, width, 1, poly, order, oneway, e->u.polyline.dash_num, &e->color, mark_way);
						}
					}
				}

				// -------- cancel dashes -------
				//if (e->u.polyline.dash_num > 0)
				//{
				//	int dummy_1[1];
				//	dummy_1[0] = 0;
				//	graphics_gc_set_dashes(gra, gc, e->u.polyline.width, e->u.polyline.offset, dummy_1, e->u.polyline.dash_num, order);
				//}
				//if (run_type > 100) // dbg(0,"gg005\n");
				// -------- cancel dashes -------
			}
				break;
			case element_circle:
				if (count)
				{
					//// dbg(0, "graphics_draw_circle\n");

					if (di->label)
					{
						dont_draw = 0;
						// dbg(0,"poi-texton_map:m#%d:t#%d:p#%d:%s\n", label_major_on_map_count, label_on_map_count, poi_on_map_count, item_to_name(dc->type));
						// count labels and poi-texts and stop after drawing more than n of those
						if (item_is_poi(dc->type))
						{
							if (
								(poi_on_map_count > MAX_POI_ICON_TEXTS_ON_MAP) &&
								((long)global_scale > (long)ORDER2_LEVEL_TO_SHOW_ALL_POI)
								)
							{
								dont_draw = 1;
							}
							else
							{
								poi_on_map_count++;
							}
						}
						else if (item_is_town_label_no_major(dc->type))
						{
							if (label_on_map_count > MAX_PLACE_LABELS_ON_MAP)
							{
								dont_draw = 1;
							}
							else
							{
								label_on_map_count++;
							}
						}
						else if (item_is_town_label_major(dc->type))
						{
							if (label_major_on_map_count > MAX_MAJOR_PLACE_LABELS_ON_MAP)
							{
								dont_draw = 1;
							}
							else
							{
								label_major_on_map_count++;
							}
						}
						else if (item_is_district_label(dc->type))
						{
							// dbg(0, "district:%s\n", di->label);

							if (label_district_on_map_count > MAX_DISTRICT_LABELS_ON_MAP)
							{
								dont_draw = 1;
							}
							else
							{
								//dbg(0, "district *DRAW*\n");
								label_district_on_map_count++;
							}
						}


						if (dont_draw == 0)
						{
							if (e->u.circle.width > 1)
							{
								gc->meth.gc_set_linewidth(gc->priv, e->u.polyline.width);
							}
							graphics_draw_circle(gra, gc, pa, e->u.circle.radius);

							// -----------------------------

							if (e->text_size)
							{
								struct graphics_font *font = get_font(gra, e->text_size);
								struct graphics_gc *gc_background = dc->gc_background;
								if (!gc_background && e->u.circle.background_color.a)
								{
									gc_background = graphics_gc_new(gra);
									graphics_gc_set_foreground(gc_background, &e->u.circle.background_color);
									dc->gc_background = gc_background;
								}
								p.x = pa[0].x + 4 + e->u.circle.radius;  // move text label a bit to the right, so it does not overlap the circle
								p.y = pa[0].y + (int)(e->text_size / 2); // move label a bit down, so that it is in the middle of the circle (on y-axis)

								if (font)
								{
									gra->meth.draw_text(gra->priv, gc->priv, gc_background ? gc_background->priv : NULL, font->priv, di->label, &p, 0x10000, 0);
								}
								else
								{
									//DBG // dbg(0, "Failed to get font with size %d\n", e->text_size);
								}

							}
						}
					}
					else // circle without label
					{
						if (dont_draw == 0)
						{
							if (e->u.circle.width > 1)
							{
								gc->meth.gc_set_linewidth(gc->priv, e->u.polyline.width);
							}
							graphics_draw_circle(gra, gc, pa, e->u.circle.radius);
						}
					}
				}
				break;
			case element_text:
				if (count && di->label)
				{
					//if (run_type > 100) // dbg(0,"gg006\n");

					struct graphics_font *font = get_font(gra, e->text_size);
					struct graphics_gc *gc_background = dc->gc_background;

					if (!gc_background && e->u.text.background_color.a)
					{
						gc_background = graphics_gc_new(gra);
						graphics_gc_set_foreground(gc_background, &e->u.text.background_color);
						dc->gc_background = gc_background;
					}

					if (font)
					{
						if (order > 8)
						{
							label_line(gra, gc, gc_background, font, pa, count, di->label);
						}
					}
					else
					{
						//DBG // dbg(0, "Failed to get font with size %d\n", e->text_size);
					}
				}
				break;
			case element_icon:
				if (count)
				{
					dont_draw = 0;

					// dbg(0,"MAP:poi-on_map:#%d:%s\n", poi_icon_on_map_count, item_to_name(dc->type));

					if (item_is_poi(dc->type))
					{
						if (
							(poi_icon_on_map_count > MAX_POI_ICONS_ON_MAP) &&
							((long)global_scale > (long)ORDER2_LEVEL_TO_SHOW_ALL_POI)
							)
						{
							// send_alert_to_java(99, "POI-filter:002:dont draw POI icon");
							dont_draw = 1;
						}
						else
						{
							poi_icon_on_map_count++;
						}
					}

					if (dont_draw == 0)
					{
						if (!img || item_is_custom_poi(di->item))
						{
							if (item_is_custom_poi(di->item))
							{
								char *icon;
								char *src;
								if (img)
								{
									graphics_image_free(dc->gra, img);
								}
								src = e->u.icon.src;
								if (!src || !src[0])
								{
									src = "%s";
								}
								icon = g_strdup_printf(src, di->label + strlen(di->label) + 1);

								path = graphics_icon_path(icon);
								g_free(icon);
							}
							else
							{
								path = graphics_icon_path(e->u.icon.src);

								// dbg(0,"MAP:icon=%s\n", path);
							}

							img = graphics_image_new_scaled_rotated(gra, path, e->u.icon.width, e->u.icon.height, e->u.icon.rotation);
							if (img)
							{
								dc->img = img;

								// compensate for streched images on high dpi devices
								img->hot.x = (int)((float)(img->hot.x) / (float)global_dpi_factor);
								img->hot.y = (int)((float)(img->hot.y) / (float)global_dpi_factor);

								// dbg(0, "POI_ICON:img2_factor=%f\n", (float)global_dpi_factor);
								// dbg(0, "POI_ICON:img2_h=%d\n", img->height);
								// dbg(0, "POI_ICON:img2_w=%d\n", img->width);
								// dbg(0, "POI_ICON:img2_hotx=%d\n", img->hot.x);
								// dbg(0, "POI_ICON:img2_hoty=%d\n", img->hot.y);
								// dbg(0, "POI_ICON:img2_icon: '%s'\n", path);

							}
							else
							{
								// missing icon //
								dbg(0, "-- ICON MISSING -- failed to load icon '%s'\n", path);
							}

							g_free(path);
						}

						if (img)
						{
							p.x = pa[0].x - img->hot.x;
							p.y = pa[0].y - img->hot.y;

							//// dbg(0,"hot: %d %d\n", img->hot.x, img->hot.y);
							gra->meth.draw_image(gra->priv, gra->gc[0]->priv, &p, img->priv);
						}
					}
				}
				break;
			case element_image:
				//dbg(0, "***image***: '%s'\n", di->label);
				if (gra->meth.draw_image_warp)
				{
					gra->meth.draw_image_warp(gra->priv, gra->gc[0]->priv, pa, count, di->label);
				}
				//else
				//{
				//	dbg(0,"draw_image_warp not supported by graphics driver drawing: image='%s' count=%d\n",di->label, count);
				//}
				break;
			case element_maptile: // same as image, just has a diferent name in the mapfile, so we can do cool things with it!
				//dbg(0, "***maptile***: '%s'\n", di->label);
				if (gra->meth.draw_image_warp)
				{
					gra->meth.draw_image_warp(gra->priv, gra->gc[0]->priv, pa, count, di->label);
				}
				//else
				//{
				//	dbg(0,"draw_image_warp not supported by graphics driver drawing: image='%s' count=%d\n",di->label, count);
				//}
				break;
			case element_arrows:
				display_draw_arrows(gra, gc, pa, count);
				break;
			//default:
				// dbg(0, "Unhandled element type %d\n", e->type);
				// printf("Unhandled element type %d\n", e->type);
				// system_log(0, "Unhandled element type %d\n", e->type);
		}
		di = di->next;
	}

	//if (run_type > 100) // dbg(0,"gg099\n");
}





/**
 * FIXME
 * @param <>
 * @returns <>
 * @author Martin Schaller (04/2008)
 */
static void xdisplay_draw_elements(struct graphics *gra, struct displaylist *display_list, struct itemgra *itm, int run_type)
{

	struct element *e;
	GList *es, *types;
	struct display_context *dc = &display_list->dc;
	struct hash_entry *entry;
	int draw_it = 1;
	int is_first_item = 1;

#ifdef NAVIT_MEASURE_TIME_DEBUG
	clock_t s_ = debug_measure_start();
#endif

	is_first_item = 1;

	es = itm->elements;
	while (es)
	{
		//// dbg(0,"*es\n");
		e = es->data;
		dc->e = e;
		types = itm->type;
		while (types)
		{
			draw_it = 1;

			// stop drawing is requested
			if (cancel_drawing_global == 1)
			{
				break;
			}

			dc->type = GPOINTER_TO_INT(types->data);
			// dbg(0,"**type=%s\n", item_to_name(dc->type));
			// system_log(0,"**type=%s\n", item_to_name(dc->type));

			if (global_draw_multipolygons == 0)
			{
				if (dc->type == type_poly_water_from_relations)
				{
					// ok "poly_water_from_relations" is found, now what?
					draw_it = 0;
				}
				else if (dc->type == type_poly_water_from_triang)
				{
					draw_it = 0;
				}
				else if (dc->type == type_wood_from_relations)
				{
					draw_it = 0;
				}
				else if (dc->type == type_poly_wood_from_triang)
				{
					draw_it = 0;
				}
				else if (dc->type == type_poly_building_from_triang)
				{
					draw_it = 0;
				}
			}

			if (draw_it == 1)
			{
				entry = get_hash_entry(display_list, dc->type);
				if (entry && entry->di)
				{
					//dbg(0,"++type=%s\n", item_to_name(dc->type));

					// system_log(0,"++type=%s\n", item_to_name(dc->type));

					//dbg(0, "is_first_item=%d run_type=%d\n", is_first_item, run_type);
					//if (!strcmp(item_to_name(dc->type), "border_country"))
					//{
					//	displayitem_draw(entry->di, NULL, dc, display_list->order, 1, 101);
					//}
					//else
					//{
					displayitem_draw(entry->di, NULL, dc, display_list->order, 1, run_type, is_first_item);
					is_first_item = 0;
					//}
					// // dbg(0,"**+gc free\n");
					display_context_free(dc);
				}
			}
			types = g_list_next(types);
			draw_it = 1;
		}
		es = g_list_next(es);
	}

#ifdef NAVIT_MEASURE_TIME_DEBUG
	debug_mrp("xdisplay_draw_elements:", debug_measure_end(s_));
#endif


}

void graphics_draw_itemgra(struct graphics *gra, struct itemgra *itm, struct transformation *t, char *label)
{
	// dbg(0, "EEnter\n");

	// HINT: seems to only be called from vehicle.c (draw the vehicle on screen)

	GList *es;
	struct display_context dc;
	int max_coord = 32;
	char *buffer = g_alloca(sizeof(struct displayitem) + max_coord * sizeof(struct coord));
	struct displayitem *di = (struct displayitem *) buffer;
	es = itm->elements;
	di->item.type = type_none;
	di->item.id_hi = 0;
	di->item.id_lo = 0;
	di->item.map = NULL;
	di->label = label;
	dc.gra = gra;
	dc.gc = NULL;
	dc.gc_background = NULL;
	dc.img = NULL;
	dc.pro = projection_screen;
	dc.mindist = 0;
	dc.trans = t;
	dc.type = type_none;
	dc.maxlen = max_coord;
	while (es)
	{
		// dbg(0, "while loop\n");
		struct element *e = es->data;
		if (e->coord_count)
		{
			if (e->coord_count > max_coord)
			{
				//DBG // dbg(0, "maximum number of coords reached: %d > %d\n", e->coord_count, max_coord);
				di->count = max_coord;
			}
			else
			{
				di->count = e->coord_count;
			}
			memcpy(di->c, e->coord, di->count * sizeof(struct coord));
		}
		else
		{
			di->c[0].x = 0;
			di->c[0].y = 0;
			di->count = 1;
		}
		dc.e = e;
		di->next = NULL;
		displayitem_draw(di, NULL, &dc, transform_get_scale(t), 0, 99, 0);
		display_context_free(&dc);
		es = g_list_next(es);
	}
}

/**
 * FIXME
 * @param <>
 * @returns <>
 * @author Martin Schaller (04/2008)
 */
static void xdisplay_draw_layer(struct displaylist *display_list, struct graphics *gra, struct layer *lay, int order)
{

	GList *itms;
	struct itemgra *itm;

	int run_type = 0; // 99 -> normal
	//  1 -> normal streets (except tunnels and bridges)
	//  2 -> tunnel
	//  3 -> bridge

	int send_refresh = 0;

	int order_corrected = order + shift_order;
	if (order_corrected < limit_order_corrected)
	{
		order_corrected = limit_order_corrected;
	}

	int order_corrected_2 = order + shift_order;
	if (order < 0)
	{
		order_corrected_2 = 0;
	}

	int fast_draw_mode = 0;
	//dbg(0,"orderlevel=%d\n", order);
	if (order < global_order_level_for_fast_draw)
	{
		//dbg(0,"fast_draw_mode=1\n");
		fast_draw_mode = 1;
		run_type = 99; // draw only 1 pass, bridges and tunnels will be drawn in any order
	}

	// dbg(0,"layer name=%s\n", lay->name);
	// system_log(0,"layer name=%s\n", lay->name);

	// reset max drawing counters ----------------------
	poi_on_map_count = 0;
	label_on_map_count = 0;
	label_district_on_map_count = 0;
	label_major_on_map_count = 0;
	poi_icon_on_map_count = 0;
	// reset max drawing counters ----------------------


	if ((strncmp("streets_1", lay->name, 9) == 0))
	{
		//// dbg(0,"MT:7.3.1 - tunnel start\n");
		//draw_lines_count_2 = 0;
		//draw_lines_count_3 = 0;
		//draw_lines_count_4 = 0;

		send_refresh = 1;

		//dbg(0,"a1 fast_draw_mode=%d run_type=%d\n", fast_draw_mode, run_type);


	  if (fast_draw_mode == 0)
	  {

		//dbg(0,"fast_draw_mode===0 ?\n");


		run_type = 2;
		itms = lay->itemgras;
		while (itms)
		{
			// stop drawing is requested
			if (cancel_drawing_global == 1)
			{
				//DBG // dbg(0, "** STOP MD 002 **\n");
				break;
			}

			itm = itms->data;
			//if (order_corrected >= itm->order.min && order_corrected <= itm->order.max)
			if (order_corrected_2 >= itm->order.min && order_corrected_2 <= itm->order.max)
			{
				xdisplay_draw_elements(gra, display_list, itm, run_type);
			}
			itms = g_list_next(itms);
		}

		//// dbg(0,"lines count2=%lld\n", draw_lines_count_2);
		//// dbg(0,"lines count3=%lld\n", draw_lines_count_3);
		//// dbg(0,"lines count4=%lld\n", draw_lines_count_4);
		//draw_lines_count_2 = 0;
		//draw_lines_count_3 = 0;
		//draw_lines_count_4 = 0;
		//// dbg(0,"MT:7.3.2 - streets start\n");

		run_type = 1;
		itms = lay->itemgras;
		while (itms)
		{
			// stop drawing is requested
			if (cancel_drawing_global == 1)
			{
				//DBG // dbg(0, "** STOP MD 002 **\n");
				break;
			}

			itm = itms->data;
			//if (order_corrected >= itm->order.min && order_corrected <= itm->order.max)
			if (order_corrected_2 >= itm->order.min && order_corrected_2 <= itm->order.max)
			{
				xdisplay_draw_elements(gra, display_list, itm, run_type);
			}
			itms = g_list_next(itms);
		}

		/*
		 // dbg(0,"lines count2=%lld\n", draw_lines_count_2);
		 // dbg(0,"lines count3=%lld\n", draw_lines_count_3);
		 // dbg(0,"lines count4=%lld\n", draw_lines_count_4);
		 draw_lines_count_2 = 0;
		 draw_lines_count_3 = 0;
		 draw_lines_count_4 = 0;
		 // dbg(0,"MT:7.3.3 - bridges start\n");
		 */

	  }

		//dbg(0,"a2 fast_draw_mode=%d run_type=%d\n", fast_draw_mode, run_type);


	    if (fast_draw_mode == 0)
		{
			run_type = 3;
		}
		else
		{
			run_type = 99;
		}

		//dbg(0,"a3 fast_draw_mode=%d run_type=%d\n", fast_draw_mode, run_type);

		itms = lay->itemgras;
		while (itms)
		{
			// stop drawing is requested
			if (cancel_drawing_global == 1)
			{
				//DBG // dbg(0, "** STOP MD 002 **\n");
				break;
			}

			itm = itms->data;
			//if (order_corrected >= itm->order.min && order_corrected <= itm->order.max)
			if (order_corrected_2 >= itm->order.min && order_corrected_2 <= itm->order.max)
			{
				xdisplay_draw_elements(gra, display_list, itm, run_type);
			}
			itms = g_list_next(itms);
		}

		/*
		 // dbg(0,"lines count2=%lld\n", draw_lines_count_2);
		 // dbg(0,"lines count3=%lld\n", draw_lines_count_3);
		 // dbg(0,"lines count4=%lld\n", draw_lines_count_4);
		 draw_lines_count_2 = 0;
		 draw_lines_count_3 = 0;
		 draw_lines_count_4 = 0;
		 // dbg(0,"MT:7.3.4 - ready\n");
		 */

	}
	else
	{
		run_type = 99;
		itms = lay->itemgras;
		while (itms)
		{
			// stop drawing is requested
			if (cancel_drawing_global == 1)
			{
				//DBG // dbg(0, "** STOP MD 002 **\n");
				break;
			}

			itm = itms->data;
			//if (order_corrected >= itm->order.min && order_corrected <= itm->order.max)
			if (order_corrected_2 >= itm->order.min && order_corrected_2 <= itm->order.max)
			{
				xdisplay_draw_elements(gra, display_list, itm, run_type);
			}
			itms = g_list_next(itms);
		}
	}

	//if (strncmp("streets__tunnel", lay->name, 15) == 0)
	//{
	//}
	//else if (strncmp("streets__bridge", lay->name, 15) == 0)
	//{
	//}

	// dirty hack to draw "waypoint(s)" ---------------------------
	if (strncmp("Internal", lay->name, 8) == 0)
	{
		// if (global_routing_engine != 1)
		{
			if (global_navit->route)
			{
				if (global_navit->destination_valid == 1)
				{
					int count_ = 0;
					int curr_ = 0;
					count_ = g_list_length(global_navit->route->destinations);
					if (count_ > 1)
					{
						if (!global_img_waypoint)
						{
							char *path2;
							path2 = graphics_icon_path("nav_waypoint_bk_center.png");
							global_img_waypoint = graphics_image_new_scaled_rotated(gra, path2, 59, 59, 0);

							// compensate for streched images on high dpi devices
							global_img_waypoint->hot.x = (int)((float)global_img_waypoint->width / 2.0f / (float)global_dpi_factor);
							global_img_waypoint->hot.y = (int)((float)global_img_waypoint->height / 2.0f / (float)global_dpi_factor);

							//dbg(0, "img_factor=%f\n", (float)global_dpi_factor);
							//dbg(0, "img_h=%d\n", global_img_waypoint->height);
							//dbg(0, "img_w=%d\n", global_img_waypoint->width);
							//dbg(0, "img_hotx=%d\n", global_img_waypoint->hot.x);
							//dbg(0, "img_hoty=%d\n", global_img_waypoint->hot.y);

							g_free(path2);
						}

						struct point p2;
						struct coord pc77;
						GList *ldest = global_navit->route->destinations;
						while (ldest)
						{
							curr_++;
							if (curr_ < count_)
							{
								struct route_info *dst = ldest->data;
								pc77.x = dst->c.x;
								pc77.y = dst->c.y;
								//// dbg(0, "draw1 curr=%d x y: %d %d\n", curr_, dst->c.x, dst->c.y);
								enum projection pro = transform_get_projection(global_navit->trans_cursor);
								transform(global_navit->trans, pro, &pc77, &p2, 1, 0, 0, NULL);
								// transform(global_navit->trans, projection_mg, &pc77, &p2, 1, 0, 0, NULL);
								p2.x = p2.x - global_img_waypoint->hot.x; // hot = 29
								p2.y = p2.y - global_img_waypoint->hot.y; // hot = 29
								gra->meth.draw_image(gra->priv, gra->gc[0]->priv, &p2, global_img_waypoint->priv);
							}
							// next dest. / waypoint
							ldest = g_list_next(ldest);
						}
					}
				}
			}
		}


#ifdef NAVIT_ROUTING_DEBUG_PRINT
		enum projection pro3 = transform_get_projection(global_navit->trans_cursor);
		struct point *p_temp = g_alloca(sizeof(struct point) * (2 + 1));
#else
#ifdef NAVIT_DEBUG_COORD_LIST
		enum projection pro3 = transform_get_projection(global_navit->trans_cursor);
		struct point *p_temp = g_alloca(sizeof(struct point) * (2 + 1));
#endif
#endif


#ifdef NAVIT_TRACKING_SHOW_REAL_GPS_POS

				if (global_tracking_show_real_gps_pos == 1)
				{

				// -------- DEBUG -------- draw real GPS position ---------
				// -------- DEBUG -------- draw real GPS position ---------
				// -------- DEBUG -------- draw real GPS position ---------
				if (!global_img_waypoint)
				{
					char *path2;
					path2 = graphics_icon_path("nav_waypoint_bk_center.png");
					global_img_waypoint = graphics_image_new_scaled_rotated(gra, path2, 59, 59, 0);

					// compensate for streched images on high dpi devices
					global_img_waypoint->hot.x = (int)((float)global_img_waypoint->width / 2.0f / (float)global_dpi_factor);
					global_img_waypoint->hot.y = (int)((float)global_img_waypoint->height / 2.0f / (float)global_dpi_factor);

					g_free(path2);
				}

				struct coord_geo g33;
				struct coord c33;
				struct point p2;
				g33.lat = global_v_pos_lat;
				g33.lng = global_v_pos_lng;

				transform_from_geo(projection_mg, &g33, &c33);
				enum projection pro = transform_get_projection(global_navit->trans_cursor);


				// cos = x
				// sin = y
				struct point *p_temp3 = g_alloca(sizeof(struct point) * (2 + 1));
				struct color debug_red2 = { 0xfafa,0x1010,0x0000,0xffff }; // RR GG BB AA

				transform(global_navit->trans, pro, &c33, &p2, 1, 0, 0, NULL);
				p_temp3[0].x = p2.x;
				p_temp3[0].y = p2.y;

				double dd = tracking_get_direction(global_navit->tracking);
				double temp_4 = (global_v_pos_dir - dd) - 90.0f;

				p_temp3[0].x = p_temp3[0].x + 36 * (navit_cos(temp_4 * M_PI / 180));
				p_temp3[0].y = p_temp3[0].y + 36 * (navit_sin(temp_4 * M_PI / 180));

				p_temp3[1].x = p_temp3[0].x;
				p_temp3[1].y = p_temp3[0].y;

				p_temp3[1].x = p_temp3[1].x + 35 * (navit_cos(temp_4 * M_PI / 180));
				p_temp3[1].y = p_temp3[1].y + 35 * (navit_sin(temp_4 * M_PI / 180));

				gra->meth.draw_lines3(gra->priv, gra->gc[0]->priv, p_temp3, 2, 15, 14, 0, &debug_red2, global_clinedrawing_active, 1);


#if 0
				// WINNER Segment on route segment --------------
				struct point *p_temp_win_3 = g_alloca(sizeof(struct point) * (2 + 1));
				struct color debug_win_blue2 = { 0x0000,0x0000,0xfafa,0xffff }; // RR GG BB AA
				struct point p_win2;

				transform(global_navit->trans, pro, &global_debug_route_seg_winner_p_start, &p_win2, 1, 0, 0, NULL);
				p_temp_win_3[0].x = p_win2.x - 140;
				p_temp_win_3[0].y = p_win2.y;

				p_temp_win_3[1].x = p_win2.x + 140;
				p_temp_win_3[1].y = p_win2.y;

				gra->meth.draw_lines3(gra->priv, gra->gc[0]->priv, p_temp_win_3, 2, 15, 4, 0, &debug_win_blue2, global_clinedrawing_active, 1);

				p_temp_win_3[0].x = p_win2.x;
				p_temp_win_3[0].y = p_win2.y - 140;

				p_temp_win_3[1].x = p_win2.x;
				p_temp_win_3[1].y = p_win2.y + 140;

				gra->meth.draw_lines3(gra->priv, gra->gc[0]->priv, p_temp_win_3, 2, 15, 4, 0, &debug_win_blue2, global_clinedrawing_active, 1);

				// WINNER Segment on route segment --------------
#endif

#if 0
				// WINNER route segment direction --------------

				struct color debug_win_green2 = { 0x0000,0xfafa,0x0000,0xffff }; // RR GG BB AA

				transform(global_navit->trans, pro, &global_debug_route_seg_winner_start, &p_win2, 1, 0, 0, NULL);

				p_temp_win_3[0].x = p_win2.x - 90;
				p_temp_win_3[0].y = p_win2.y;

				p_temp_win_3[1].x = p_win2.x + 90;
				p_temp_win_3[1].y = p_win2.y;

				gra->meth.draw_lines3(gra->priv, gra->gc[0]->priv, p_temp_win_3, 2, 15, 12, 0, &debug_win_green2, global_clinedrawing_active, 1);



				p_temp_win_3[0].x = p_win2.x;
				p_temp_win_3[0].y = p_win2.y;

				transform(global_navit->trans, pro, &global_debug_route_seg_winner_end, &p_win2, 1, 0, 0, NULL);

				p_temp_win_3[1].x = p_win2.x;
				p_temp_win_3[1].y = p_win2.y;

				gra->meth.draw_lines3(gra->priv, gra->gc[0]->priv, p_temp_win_3, 2, 15, 42, 0, &debug_win_green2, global_clinedrawing_active, 1);

				// WINNER route segment direction --------------
#endif



				// ------ text -------
				graphics_gc_set_foreground(gra->gc[0], &debug_red2);
				graphics_gc_set_linewidth(gra->gc[0], 8);
				struct point p7;
				struct graphics_font *font8 = get_font(gra, 21);
				char *dir_label=g_strdup_printf("%4.1f : %4.1f", (float)global_v_pos_dir, (float)dd);
				p7.x = p2.x + 25;  // move text label a bit to the right, so it does not overlap the circle
				p7.y = p2.y - 160;  // move label a bit up (y-axis)
				gra->meth.draw_text(gra->priv, gra->gc[0]->priv, NULL, font8->priv, dir_label, &p7, 0x10000, 0);
				g_free(dir_label);
				// ------ text -------


				p2.x = p2.x - global_img_waypoint->hot.x; // hot = 29
				p2.y = p2.y - global_img_waypoint->hot.y; // hot = 29
				gra->meth.draw_image(gra->priv, gra->gc[0]->priv, &p2, global_img_waypoint->priv);

				// -------- DEBUG -------- draw real GPS position ---------
				// -------- DEBUG -------- draw real GPS position ---------
				// -------- DEBUG -------- draw real GPS position ---------

				}

#endif

#ifdef NAVIT_ROUTING_DEBUG_PRINT

				// -------- DEBUG -------- draw winner track segment ---------
				// -------- DEBUG -------- draw winner track segment ---------
				// -------- DEBUG -------- draw winner track segment ---------
				struct color debug_orange = { 0xffff,0x4040,0x0000,0xffff }; // RR GG BB AA

				if (global_navit->route)
				{
					if (global_navit->destination_valid == 1)
					{
						transform(global_navit->trans, pro3, &global_debug_route_seg_winner_start, &p_temp[0], 1, 0, 0, NULL);
						transform(global_navit->trans, pro3, &global_debug_route_seg_winner_end, &p_temp[1], 1, 0, 0, NULL);
						gra->meth.draw_lines3(gra->priv, gra->gc[0]->priv, p_temp, 2, 15, 30, 0, &debug_orange, global_clinedrawing_active, 1);
					}
				}

				struct color debug_turkoise = { 0x0404,0xb4b4,0xaeae,0xffff }; // RR GG BB AA
				transform(global_navit->trans, pro3, &global_debug_seg_winner_start, &p_temp[0], 1, 0, 0, NULL);
				transform(global_navit->trans, pro3, &global_debug_seg_winner_end, &p_temp[1], 1, 0, 0, NULL);
				gra->meth.draw_lines3(gra->priv, gra->gc[0]->priv, p_temp, 2, 15, 20, 0, &debug_turkoise, global_clinedrawing_active, 1);


				if (global_navit->route)
				{
					if (global_navit->destination_valid == 1)
					{
						struct color debug_red = { 0xffff,0x0000,0x0000,0xffff }; // RR GG BB AA
						graphics_gc_set_foreground(gra->gc[0], &debug_red);
						graphics_gc_set_linewidth(gra->gc[0], 10);
						transform(global_navit->trans, pro3, &global_debug_route_seg_winner_p_start, &p_temp[0], 1, 0, 0, NULL);
						gra->meth.draw_circle(gra->priv, gra->gc[0]->priv, &p_temp[0], 44);
					}
				}

				struct color debug_green = { 0x0000,0xffff,0x0000,0xffff }; // RR GG BB AA
				graphics_gc_set_foreground(gra->gc[0], &debug_green);
				graphics_gc_set_linewidth(gra->gc[0], 8);
				transform(global_navit->trans, pro3, &global_debug_seg_winner_p_start, &p_temp[0], 1, 0, 0, NULL);
				gra->meth.draw_circle(gra->priv, gra->gc[0]->priv, &p_temp[0], 36);

				if (global_navit->route)
				{
					if (global_navit->destination_valid == 1)
					{
						struct color debug_yellow = { 0xf7f7,0xfefe,0x2e2e,0xffff }; // RR GG BB AA
						transform(global_navit->trans, pro3, &global_debug_seg_route_start, &p_temp[0], 1, 0, 0, NULL);
						transform(global_navit->trans, pro3, &global_debug_seg_route_end, &p_temp[1], 1, 0, 0, NULL);
						gra->meth.draw_lines3(gra->priv, gra->gc[0]->priv, p_temp, 2, 15, 8, 0, &debug_yellow, global_clinedrawing_active, 1);
					}
				}

				// -------- DEBUG -------- draw winner track segment ---------
				// -------- DEBUG -------- draw winner track segment ---------
				// -------- DEBUG -------- draw winner track segment ---------
#endif


// ============================== debug lines ==============================
// ============================== debug lines ==============================
// ============================== debug lines ==============================
#ifdef NAVIT_DEBUG_COORD_LIST
				struct color debug_purple = { 0xffff,0x0000,0xffff,0xffff }; // RR GG BB AA
				transform(global_navit->trans, pro3, &global_debug_trlast_start, &p_temp[0], 1, 0, 0, NULL);
				transform(global_navit->trans, pro3, &global_debug_trlast_end, &p_temp[1], 1, 0, 0, NULL);
				gra->meth.draw_lines3(gra->priv, gra->gc[0]->priv, p_temp, 2, 15, 18, 0, &debug_purple, global_clinedrawing_active, 1);


				struct color debug_purple0 = { 0x1010,0xfefe,0xefef,0xffff }; // RR GG BB AA
				struct color debug_purple1 = { 0x3030,0xaeae,0x4f4f,0xffff }; // RR GG BB AA
				struct color debug_purple2 = { 0x1010,0x7878,0x3030,0xffff }; // RR GG BB AA
				struct color debug_red3 = { 0xffff,0x0000,0x0000,0xffff }; // RR GG BB AA
				struct color *debug_color_pp;
				int ii2;
				int jj;

				// dbg(0,"global_debug_coord_list:LOOP:num=%d\n", global_debug_coord_list_items);

				for (ii2 = 0; ii2 < global_debug_coord_list_items; ii2++)
				{

					//dbg(0,"global_debug_coord_list:LOOP:coord_list=%d\n", ii2);

					transform(global_navit->trans, pro3, &global_debug_coord_list[ii2], &p_temp[0], 1, 0, 0, NULL);
					ii2++;
					transform(global_navit->trans, pro3, &global_debug_coord_list[ii2], &p_temp[1], 1, 0, 0, NULL);

					jj = ((ii2 - 1) % 3);
					if (jj == 0)
					{
						debug_color_pp = &debug_purple0;
					}
					else if (jj == 1)
					{
						debug_color_pp = &debug_purple1;
					}
					else
					{
						debug_color_pp = &debug_purple2;
					}


					gra->meth.draw_lines3(gra->priv, gra->gc[0]->priv, p_temp, 2, 15, 6, 0, debug_color_pp, global_clinedrawing_active, 1);

					// ------ text -------
					graphics_gc_set_foreground(gra->gc[0], &debug_red3);
					graphics_gc_set_linewidth(gra->gc[0], 6);
					struct point p7;
					struct graphics_font *font8 = get_font(gra, 21);
					char *dir_label=g_strdup_printf("%d", (ii2 - 1));
					p7.x = p_temp[0].x + 0;  // move text label a bit to the right, so it does not overlap the circle
					p7.y = p_temp[0].y + 0;  // move label a bit down (y-axis)
					gra->meth.draw_text(gra->priv, gra->gc[0]->priv, NULL, font8->priv, dir_label, &p7, 0x10000, 0);
					g_free(dir_label);
					// ------ text -------

					// ------ text -------
					//graphics_gc_set_foreground(gra->gc[0], &debug_red3);
					//graphics_gc_set_linewidth(gra->gc[0], 6);
					//struct point p7;
					//struct graphics_font *font8 = get_font(gra, 21);
					dir_label=g_strdup_printf("%d", ii2);
					p7.x = p_temp[1].x + 8;  // move text label a bit to the right, so it does not overlap the circle
					p7.y = p_temp[1].y + 8;  // move label a bit down (y-axis)
					gra->meth.draw_text(gra->priv, gra->gc[0]->priv, NULL, font8->priv, dir_label, &p7, 0x10000, 0);
					g_free(dir_label);
					// ------ text -------

				}
#endif
// ============================== debug lines ==============================
// ============================== debug lines ==============================
// ============================== debug lines ==============================



	}
	// dirty hack to draw "waypoint(s)" ---------------------------
	// dirty hack to draw "route arrows" ---------------------------
	else if (strncmp("RouteArrows", lay->name, 11) == 0)
	{
		if (global_navit->route)
		{
			if (global_navit->destination_valid == 1)
			{

#define MAX_ROUTE_ARROW_LINE_LENGTH 50.0f
#define MAX_ROUTE_ARROW_LINE_LENGTH_SQ 2500
#define MIN_ROUTE_ARROW_LINE_LENGTH_SQ 2000
#define MAX_ROUTE_ARROW_TO_SHOW 2 // default 2

				struct navigation *nav = NULL;
				struct map *map=NULL;
				struct map_rect *mr=NULL;
				struct item *item8 =NULL;
				struct attr attr8;
				struct coord c8;
				struct coord c11;
				struct color route_arrrow_green_1 = { 0x0404,0xb4b4,0xaeae,0xffff }; // RR GG BB AA first line and arrow
				struct color route_arrrow_green_2 = { 0x0909,0x8484,0x7f7f,0xffff }; // RR GG BB AA all the next lines and arrows
				struct color *route_arrrow_green = &route_arrrow_green_1;
				struct point *p8 = g_alloca(sizeof(struct point));
				struct point *p9_temp = g_alloca(sizeof(struct point) * (2 + 1));
				struct point *p11_temp = g_alloca(sizeof(struct point) * (2 + 1));
				enum projection pro3 = transform_get_projection(global_navit->trans_cursor);
				int route_arrow_count = 0;
				int arrow_waiting = 0;


#ifdef NAVIT_SHOW_ROUTE_ARROWS

				graphics_gc_set_foreground(gra->gc[0], route_arrrow_green);
				graphics_gc_set_linewidth(gra->gc[0], 8);

				nav = navit_get_navigation(global_navit);
				if (nav)
				{
					map = navigation_get_map(nav);
					if (map)
					{
						mr = map_rect_new(map,NULL);
						if (mr)
						{
							while ((item8 = map_rect_get_item(mr)))
							{

								if (item8->type == type_nav_destination)
								{
									break;
								}

								if (item_attr_get(item8, attr_navigation_long, &attr8))
								{
									route_arrow_count++;

									if (route_arrow_count > MAX_ROUTE_ARROW_TO_SHOW)
									{
										break;
									}
									else if (route_arrow_count == 2)
									{
										route_arrrow_green = &route_arrrow_green_2;
									}

									item_coord_get(item8, &c8, 1);

#if 0
									graphics_gc_set_foreground(gra->gc[0], &route_arrrow_green);
									graphics_gc_set_linewidth(gra->gc[0], 8);
									transform(global_navit->trans, pro3, &c8, p8, 1, 0, 0, NULL);
									gra->meth.draw_circle(gra->priv, gra->gc[0]->priv, p8, 45);

									struct graphics_font *font8 = get_font(gra, 25);
									char *arrow_label=g_strdup_printf("%d", route_arrow_count);

									p8->x = p8->x + 45;  // move text label a bit to the right, so it does not overlap the circle
									p8->y = p8->y + (int)(25 / 2); // move label a bit down, so that it is in the middle of the circle (on y-axis)

									gra->meth.draw_text(gra->priv, gra->gc[0]->priv, NULL, font8->priv, arrow_label, p8, 0x10000, 0);
									g_free(arrow_label);
#endif


									// --- find the segments for this maneuver ---
									struct route *route2 = NULL;
									struct map *route_map2 = NULL;
									struct map_rect *mr2 = NULL;
									struct item *item2 = NULL;
									struct coord c2;
									struct coord c2_prev;
									float route_line_length_sq;
									int first2 = 1;
									int first3 = 0;
									int found_seg = 0;
									float dx, dy;
									int added_seg = 0;

									c2.x = 0;
									c2.y = 0;
									c2_prev.x = c2.x;
									c2_prev.y = c2.y;

									route2 = navit_get_route(global_navit);
									if (route2)
									{
										route_map2 = route_get_map(route2);
										if (route_map2)
										{
											mr2 = map_rect_new(route_map2, NULL);
											if (mr2)
											{
												item2 = map_rect_get_item(mr2);
												while (item2)
												{
													if (!item_coord_get(item2, &c2, 1))
													{
														first3 = 1;
														item2 = map_rect_get_item(mr2);
														continue;
													}

													// loop all coords of the route ----------------------
													if (first3 == 1)
													{
														first3 = 0;
													}
													else
													{
														if ( ((c2.x == c8.x) && (c2.y == c8.y)) || (found_seg == 1) )
														{
															//if (first2 == 1)
															//{
															//	first2 = 0;
															//}
															//else
															{
																//dbg(0, "11draw:c2.x=%d c2.y=%d c2_prev.x=%d c2_prev.y=%d\n", c2.x, c2.y, c2_prev.x, c2_prev.y);
																if (found_seg == 1)
																{
																	// draw arrow head at the end

																	route_line_length_sq = (float)transform_distance_sq(&c2_prev, &c2);
																	if ((added_seg == 0) && (route_line_length_sq < MIN_ROUTE_ARROW_LINE_LENGTH_SQ))
																	{
																		// draw line
																		transform(global_navit->trans, pro3, &c2_prev, &p9_temp[0], 1, 0, 0, NULL);
																		transform(global_navit->trans, pro3, &c2, &p9_temp[1], 1, 0, 0, NULL);
																		gra->meth.draw_lines3(gra->priv, gra->gc[0]->priv, p9_temp, 2, 15, 28, 0, route_arrrow_green, global_clinedrawing_active, 1);

																		// add next coord to line/arrow
																		found_seg = 0;
																		added_seg = 1;
																	}
																	else
																	{
																		if (route_line_length_sq > MAX_ROUTE_ARROW_LINE_LENGTH_SQ)
																		{
																			route_line_length_sq = sqrt(route_line_length_sq);
																			// make line shorter (move coord c2 backward)
																			dx = ((float)(c2.x - c2_prev.x)) / route_line_length_sq;
																			dy = ((float)(c2.y - c2_prev.y)) / route_line_length_sq;
																			c2.x = c2_prev.x + (int)(dx * MAX_ROUTE_ARROW_LINE_LENGTH);
																			c2.y = c2_prev.y + (int)(dy * MAX_ROUTE_ARROW_LINE_LENGTH);
																		}

																		transform(global_navit->trans, pro3, &c2_prev, &p9_temp[0], 1, 0, 0, NULL);
																		transform(global_navit->trans, pro3, &c2, &p9_temp[1], 1, 0, 0, NULL);
																		arrow_waiting = 1;
																		p11_temp[0].x = p9_temp[0].x;
																		p11_temp[0].y = p9_temp[0].y;
																		p11_temp[1].x = p9_temp[1].x;
																		p11_temp[1].y = p9_temp[1].y;
																		c11.x = c2.x;
																		c11.y = c2.y;
																		// gra->meth.draw_lines3(gra->priv, gra->gc[0]->priv, p9_temp, 2, 15, 28, 0, route_arrrow_green, global_clinedrawing_active, 4);
																	}
																}
																else
																{
																	route_line_length_sq = (float)transform_distance_sq(&c2_prev, &c2);
																	if (route_line_length_sq > MAX_ROUTE_ARROW_LINE_LENGTH_SQ)
																	{
																		route_line_length_sq = sqrt(route_line_length_sq);
																		// make line shorter (move coord c2_prev forward)
																		dx = ((float)(c2.x - c2_prev.x)) / route_line_length_sq;
																		dy = ((float)(c2.y - c2_prev.y)) / route_line_length_sq;
																		c2_prev.x = c2.x - (int)(dx * MAX_ROUTE_ARROW_LINE_LENGTH);
																		c2_prev.y = c2.y - (int)(dy * MAX_ROUTE_ARROW_LINE_LENGTH);
																	}

																	transform(global_navit->trans, pro3, &c2_prev, &p9_temp[0], 1, 0, 0, NULL);
																	transform(global_navit->trans, pro3, &c2, &p9_temp[1], 1, 0, 0, NULL);

																	gra->meth.draw_lines3(gra->priv, gra->gc[0]->priv, p9_temp, 2, 15, 28, 0, route_arrrow_green, global_clinedrawing_active, 1);

																	if (arrow_waiting == 1)
																	{
																		arrow_waiting = 0;
																		if (((abs(c2.x - c11.x)<4) && (abs(c2.y - c11.y)<4))  ||  ((abs(c2_prev.x - c11.x)<4) && (abs(c2_prev.y - c11.y)<4)))
																		{
																			gra->meth.draw_lines3(gra->priv, gra->gc[0]->priv, p11_temp, 2, 15, 28, 0, route_arrrow_green, global_clinedrawing_active, 1);
																		}
																		else
																		{
																			if (route_arrow_count == 2)
																			{
																				gra->meth.draw_lines3(gra->priv, gra->gc[0]->priv, p11_temp, 2, 15, 28, 0, &route_arrrow_green_1, global_clinedrawing_active, 4);
																			}
																			else
																			{
																				gra->meth.draw_lines3(gra->priv, gra->gc[0]->priv, p11_temp, 2, 15, 28, 0, &route_arrrow_green_2, global_clinedrawing_active, 4);
																			}
																		}
																	}

																}
															}

															if (found_seg == 0)
															{
																found_seg++;
															}
															else if (found_seg == 1)
															{
																break;
															}

														}
													}
													c2_prev.x = c2.x;
													c2_prev.y = c2.y;

													// loop all coords of the route ----------------------
												}
												map_rect_destroy(mr2);
											}
										}
									}
									// --- find the segments for this maneuver ---

								}
							}
							map_rect_destroy(mr);

							if (arrow_waiting == 1)
							{
								// if there is still an arrow to be drawn, draw it now
								arrow_waiting = 0;
								gra->meth.draw_lines3(gra->priv, gra->gc[0]->priv, p11_temp, 2, 15, 28, 0, route_arrrow_green, global_clinedrawing_active, 4);
							}

						}
					}
				}

#endif



#ifdef NAVIT_FREE_TEXT_DEBUG_PRINT
				// ------- free text list ----------
				// ------- free text list ----------

				struct color debug_blue2a = { 0x0000,0x0000,0xffff,0xffff }; // RR GG BB AA
				struct color debug_black2a = { 0x0000,0x0000,0x0000,0xffff }; // RR GG BB AA
				struct point *p_temp13a = g_alloca(sizeof(struct point));
				struct point *p9_temp13a = g_alloca(sizeof(struct point) * (2 + 1));
				enum projection pro4a = transform_get_projection(global_navit->trans_cursor);
				graphics_gc_set_linewidth(gra->gc[0], 8);

				int ij1a;
				for (ij1a=0; ij1a < global_freetext_list_count; ij1a++)
				{
					transform(global_navit->trans, pro4a, &global_freetext_list[ij1a].c1, p_temp13a, 1, 0, 0, NULL);
					graphics_gc_set_foreground(gra->gc[0], &debug_blue2a);
					gra->meth.draw_circle(gra->priv, gra->gc[0]->priv, p_temp13a, 36);

					// ------ text -------
					struct point p7a;
					struct graphics_font *font8a = get_font(gra, 10);
					graphics_gc_set_foreground(gra->gc[0], &debug_black2a);
					p7a.x = p_temp13a->x + 25;  // move text label a bit to the right, so it does not overlap the circle
					p7a.y = p_temp13a->y + 25;  // move label a bit down (y-axis)
					gra->meth.draw_text(gra->priv, gra->gc[0]->priv, NULL, font8a->priv, global_freetext_list[ij1a].text, &p7a, 0x10000, 0);
					// ------ text -------
				}

				// ------- free text list ----------
				// ------- free text list ----------
#endif






				// ------- sharp turn angle list ----------
				// ------- sharp turn angle list ----------
#ifdef NAVIT_ANGLE_LIST_DEBUG_PRINT_DRAW
				struct color debug_green2 = { 0x0000,0xffff,0x0000,0xffff }; // RR GG BB AA
				struct color debug_blue2 = { 0x0000,0x0000,0xffff,0xffff }; // RR GG BB AA
				struct color debug_red2 = { 0xffff,0x0000,0x0000,0xffff }; // RR GG BB AA
				struct point *p_temp13 = g_alloca(sizeof(struct point));
				struct point *p9_temp13 = g_alloca(sizeof(struct point) * (2 + 1));
				enum projection pro4 = transform_get_projection(global_navit->trans_cursor);
				graphics_gc_set_linewidth(gra->gc[0], 8);

				int ij1;
				for (ij1=0; ij1 < global_sharp_turn_list_count;ij1++)
				{
					transform(global_navit->trans, pro4, &global_sharp_turn_list[ij1].c1, p_temp13, 1, 0, 0, NULL);
					if (global_sharp_turn_list[ij1].dir == 1)
					{
						graphics_gc_set_foreground(gra->gc[0], &debug_green2);
					}
					else
					{
						graphics_gc_set_foreground(gra->gc[0], &debug_blue2);
					}
					gra->meth.draw_circle(gra->priv, gra->gc[0]->priv, p_temp13, 36);


					// --- line of turn angle ---
					p9_temp13[0].x = p_temp13->x;
					p9_temp13[0].y = p_temp13->y;
					transform(global_navit->trans, pro3, &global_sharp_turn_list[ij1].cs, &p9_temp13[1], 1, 0, 0, NULL);
					gra->meth.draw_lines3(gra->priv, gra->gc[0]->priv, p9_temp13, 2, 15, 12, 0, &debug_red2, global_clinedrawing_active, 1);
					transform(global_navit->trans, pro3, &global_sharp_turn_list[ij1].ce, &p9_temp13[1], 1, 0, 0, NULL);
					gra->meth.draw_lines3(gra->priv, gra->gc[0]->priv, p9_temp13, 2, 15, 5, 0, &debug_green2, global_clinedrawing_active, 1);
					// --- line of turn angle ---



					// ------ text -------
					// graphics_gc_set_foreground(gra->gc[0], &debug_red2);
					// graphics_gc_set_linewidth(gra->gc[0], 8);
					struct point p7;
					struct graphics_font *font8 = get_font(gra, 24);
					char *dir_label=g_strdup_printf("%d", global_sharp_turn_list[ij1].angle);
					// dbg(0, "st.angle(2)=%d\n", global_sharp_turn_list[ij1].angle);

					if (global_sharp_turn_list[ij1].angle > 0)
					{
						p7.x = p_temp13->x + 25;  // move text label a bit to the right, so it does not overlap the circle
						p7.y = p_temp13->y + 25;
						// p7.y = p_temp13->y + 25 + (int)((float)global_sharp_turn_list[ij1].angle / 2.0f);  // move label a bit down (y-axis)
					}
					else
					{
						p7.x = p_temp13->x + 25;  // move text label a bit to the right, so it does not overlap the circle
						p7.y = p_temp13->y + 0;
						// p7.y = p_temp13->y + 0 + (int)((float)global_sharp_turn_list[ij1].angle / 2.0f);  // move label a bit down (y-axis)
					}
					gra->meth.draw_text(gra->priv, gra->gc[0]->priv, NULL, font8->priv, dir_label, &p7, 0x10000, 0);
					g_free(dir_label);
					// ------ text -------

				}
#endif
				// ------- sharp turn angle list ----------
				// ------- sharp turn angle list ----------

			}
		}
	}
	// dirty hack to draw "route arrows" ---------------------------


	if (send_refresh == 1)
	{
		// dummy "ready" signal ------------------------------------------
		// dbg(0, "dummy \"ready\" signal (layers)\n");
		gra->meth.draw_lines4(gra->priv, NULL, NULL, NULL, 1, 1, 96, 0, NULL, 1);
		// dummy "ready" signal ------------------------------------------
	}

}

/**
 * FIXME
 * @param <>
 * @returns <>
 * @author Martin Schaller (04/2008)
 */
static void xdisplay_draw(struct displaylist *display_list, struct graphics *gra, struct layout *l, int order)
{
__F_START__

	GList *lays;
	struct layer *lay;

	// int draw_vector_map = 1;

	// if zoomed out too much then use prerendered map tiles
	//if (display_list->order < ORDER_USE_PRERENDERED_MAP)
	//{
	//	draw_vector_map = 0;
	//}

#if 0
	if (!draw_vector_map)
	{
		// draw prerendered big mapimage --- HERE ---
		struct transformation *t;
		t = display_list->dc.trans;
		struct coord *cp;
		cp = transform_get_center(t);
		struct attr attr;
		struct config
		{
			struct attr **attrs;
			struct callback_list *cbl;
		}*config;
		struct point p;
		int valid = 0;
		if (global_navit)
		{
			if ((global_navit->vehicle) && (global_navit->vehicle->vehicle))
			{
				//int a, s;
				//struct point pnt2;
				//vehicle_get_cursor_data(global_navit->vehicle->vehicle, &pnt2, &a, &s);

				//struct attr pos_attr;
				//if (vehicle_get_attr(global_navit->vehicle->vehicle, attr_position_coord_geo, &pos_attr, NULL))
				//{
				//	////DBG // dbg(0,"1 lat=%f, lng=%f\n",pos_attr.u.coord_geo->lat,pos_attr.u.coord_geo->lng);
				//}

				valid = 1;
			}
		}

		struct coord *c;
		c = &(t->map_center);

		enum projection pro = transform_get_projection(global_navit->trans_cursor);
		struct coord_geo g22;
		struct coord *c22;
		struct point mcenter_pnt;
		c22 = &(t->map_center);
		transform_to_geo(projection_mg, c22, &g22);
		transform(global_navit->trans, pro, c22, &mcenter_pnt, 1, 0, 0, NULL);

		struct coord c99;
		struct coord_geo g99;
		struct point cursor_pnt;
		struct point p99;
		long my_scale;

		my_scale = transform_get_scale(global_navit->trans);

		g99.lat = 0.0;
		g99.lng = 0.0;
		// g99.lat = 80.0;
		// g99.lng = -174.0;
		transform_from_geo(pro, &g99, &c99);
		transform(global_navit->trans, pro, &c99, &cursor_pnt, 1, 0, 0, NULL);

		// now really draw it
		struct graphics *gra22 = display_list->dc.gra;
		struct graphics_gc *gc22 = display_list->dc.gc;
		if (!gc22)
		{
			gc22 = graphics_gc_new(gra22);
		}
		// graphics_draw_bigmap(gra22, gc22, -t->yaw, display_list->order, g22.lat, g22.lng, cursor_pnt.x, cursor_pnt.y, mcenter_pnt.x, mcenter_pnt.y, global_vehicle_pos_onscreen.x, global_vehicle_pos_onscreen.y, valid);
		graphics_draw_bigmap(gra22, gc22, -t->yaw, (int) (my_scale / 100), g22.lat, g22.lng, cursor_pnt.x, cursor_pnt.y, mcenter_pnt.x, mcenter_pnt.y, global_vehicle_pos_onscreen.x, global_vehicle_pos_onscreen.y, valid);
	}
#endif

	// reset value; --> not sure here, maybe it should NOT be reset here!!!???
	// cancel_drawing_global = 0;







	// stop drawing is requested
	if (cancel_drawing_global != 1)
	{

		if ((display_list->order >= ORDER_USE_PRERENDERED_MAP) || (global_show_maps_debug_view)) // ==5 , (4,3,2,1,0)--> no vector map
		{
			// MAPNIK -- MVT --
			// MAPNIK -- MVT --
			// MAPNIK -- MVT --
			int screen_width, screen_height;
			struct coord c_screen;
			struct point p_screen;
			struct coord_geo g_screen_lt;
			struct coord_geo g_screen_rt;
			struct coord_geo g_screen_rb;

			transform_get_size(global_navit->trans, &screen_width, &screen_height);
			// dbg(0, "transform_get_size:w=%d h=%d\n", screen_width, screen_height);
			// dbg(0, "navit_maps_dir=%s\n", navit_maps_dir);

			p_screen.x = 0;
			p_screen.y = 0;
			transform_reverse(global_navit->trans, &p_screen, &c_screen);
			transform_to_geo(projection_mg, &c_screen, &g_screen_lt);

			p_screen.x = screen_width;
			p_screen.y = 0;
			transform_reverse(global_navit->trans, &p_screen, &c_screen);
			transform_to_geo(projection_mg, &c_screen, &g_screen_rt);

			p_screen.x = screen_width;
			p_screen.y = screen_height;
			transform_reverse(global_navit->trans, &p_screen, &c_screen);
			transform_to_geo(projection_mg, &c_screen, &g_screen_rb);

			// dbg(0, "bbox=%f %f %f %f %f %f\n", g_screen_lt.lat, g_screen_lt.lng, g_screen_rt.lat, g_screen_rt.lng, g_screen_rb.lat, g_screen_rb.lng);


			if ((display_list->order > 5) || (global_show_maps_debug_view))
			{
				loop_mapnik_tiles(g_screen_lt.lat, g_screen_lt.lng, g_screen_rt.lat, g_screen_rt.lng, g_screen_rb.lat, g_screen_rb.lng, 12, navit_maps_dir, display_list);
			}
/*
			else if (display_list->order > 4)
			{
				loop_mapnik_tiles(g_screen_lt.lat, g_screen_lt.lng, g_screen_rt.lat, g_screen_rt.lng, g_screen_rb.lat, g_screen_rb.lng, 6, navit_maps_dir, display_list);
			}
			else
			{
				loop_mapnik_tiles(g_screen_lt.lat, g_screen_lt.lng, g_screen_rt.lat, g_screen_rt.lng, g_screen_rb.lat, g_screen_rb.lng, 2, navit_maps_dir, display_list);
			}
*/
			// MAPNIK -- MVT --
			// MAPNIK -- MVT --
			// MAPNIK -- MVT --
		}
	}






	lays = l->layers;
	while (lays)
	{
		// stop drawing is requested
		if (cancel_drawing_global == 1)
		{
			break;
		}

		lay = lays->data;
		if (lay->active)
		{
			//ZZZZZZZZZZZZZZZZZZZZZ//
			//ZZZZZZZZZZZZZZZZZZZZZ//
			//ZZZZZZZZZZZZZZZZZZZZZ//
			//ZZZZZZZZZZZZZZZZZZZZZ//
			//ZZZZZZZZZZZZZZZZZZZZZ//
			//ZZZZZZZZZZZZZZZZZZZZZ//
			//ZZZZZZZZZZZZZZZZZZZZZ//
			//ZZZZZZZZZZZZZZZZZZZZZ//
			xdisplay_draw_layer(display_list, gra, lay, order);
			//ZZZZZZZZZZZZZZZZZZZZZ//
			//ZZZZZZZZZZZZZZZZZZZZZ//
			//ZZZZZZZZZZZZZZZZZZZZZ//
			//ZZZZZZZZZZZZZZZZZZZZZ//
			//ZZZZZZZZZZZZZZZZZZZZZ//
			//ZZZZZZZZZZZZZZZZZZZZZ//
			//ZZZZZZZZZZZZZZZZZZZZZ//
			//ZZZZZZZZZZZZZZZZZZZZZ//
		}
		lays = g_list_next(lays);
	}



	// reset value;
	// cancel_drawing_global = 0;

	// dummy "start" signal ------------------------------------------
	// // dbg(0, "dummy \"start\" signal\n");
	// gra->meth.draw_lines4(gra->priv, NULL, NULL, NULL, 1, 1, 97, 0);
	// dummy "start" signal ------------------------------------------

__F_END__
}

/**
 * FIXME
 * @param <>
 * @returns <>
 * @author Martin Schaller (04/2008)
 */
extern void *route_selection;

static void displaylist_update_layers(struct displaylist *displaylist, GList *layers, int order)
{
	////DBG // dbg(0,"ooo enter ooo\n");

	int order_corrected = order + shift_order;
	//int saved=displaylist->order;
	if (order < limit_order_corrected)
	{
		order_corrected = limit_order_corrected;
		// displaylist->order=0;
	}

	while (layers)
	{
		struct layer *layer = layers->data;
		GList *itemgras = layer->itemgras;
		while (itemgras)
		{
			struct itemgra *itemgra = itemgras->data;
			GList *types = itemgra->type;
			if (itemgra->order.min <= order_corrected && itemgra->order.max >= order_corrected)
			{
				while (types)
				{
					enum item_type type = (enum item_type) types->data;
					set_hash_entry(displaylist, type);
					types = g_list_next(types);
				}
			}
			itemgras = g_list_next(itemgras);
		}
		layers = g_list_next(layers);
	}
	// displaylist->order=saved;
}

static void displaylist_update_hash(struct displaylist *displaylist)
{
	////DBG // dbg(0,"ooo enter ooo\n");

	displaylist->max_offset = 0;
	clear_hash(displaylist);
	displaylist_update_layers(displaylist, displaylist->layout->layers, displaylist->order);
	// dbg(1, "max offset %d\n", displaylist->max_offset);
}

static void do_draw(struct displaylist *displaylist, int cancel, int flags)
{

__F_START__

	// int rnd = rand();
	// dbg(0, "DO__DRAW:%d enter\n", rnd);

	// try to cancel any previous drawing that might be going on ...
	// try to cancel any previous drawing that might be going on ...
	// try to cancel any previous drawing that might be going on ...
	// dbg(0, "DO__DRAW:%d cancel_drawing_global 1=%d\n", rnd, cancel_drawing_global);
	cancel_drawing_global = 1;
	// dbg(0, "DO__DRAW:%d cancel_drawing_global 2=%d\n", rnd, cancel_drawing_global);
	// try to cancel any previous drawing that might be going on ...
	// try to cancel any previous drawing that might be going on ...
	// try to cancel any previous drawing that might be going on ...


	// dbg(0, "DO__DRAW:%d lock mutex\n", rnd);
	pthread_mutex_lock(&uiConditionMutex);
	// dbg(0, "DO__DRAW:%d OK lock mutex\n", rnd);

	// drawing is now starting ...
	// drawing is now starting ...
	// drawing is now starting ...
	// dbg(0, "DO__DRAW:%d cancel_drawing_global 3=%d\n", rnd, cancel_drawing_global);
	cancel_drawing_global = 0;
	// dbg(0, "DO__DRAW:%d cancel_drawing_global 4=%d\n", rnd, cancel_drawing_global);
	// drawing is now starting ...
	// drawing is now starting ...
	// drawing is now starting ...


#ifdef HAVE_API_ANDROID
	// ---- disable map view -----
	// ---- disable map view -----
	// ---- disable map view -----
	if (disable_map_drawing == 1)
	{
		//android_return_generic_int(2, 0);

		//// dbg(0,"set:0:displaylist->busy=%d\n",displaylist->busy);
		displaylist->busy = 0;

		// dbg(0,"DO__DRAW:%d UN-lock mutex 001\n", rnd);
		pthread_mutex_unlock(&uiConditionMutex);
		// dbg(0,"DO__DRAW:%d OK UN-lock mutex 001\n", rnd);

		// dbg(0,"DO__DRAW:%d return 001\n", rnd);
		return2;
	}
	// ---- disable map view -----
	// ---- disable map view -----
	// ---- disable map view -----
#endif

	clock_t s_;
#ifdef NAVIT_MEASURE_TIME_DEBUG
	s_ = debug_measure_start();
#endif

	struct item *item;
	int count, max = displaylist->dc.maxlen, workload = 0;
	struct coord *ca = g_alloca(sizeof(struct coord) * max);
	struct attr attr, attr2;
	enum projection pro;
	int draw_vector_map = 1; // do we draw the vecotor map, or not? 0=false, 1=true
	int draw_tile_map = 1;   // do we draw the prerendered tile map? 0=false, 1=true
	int mapset_counter = 0;
	int mapset_need_draw = 0;
	int only_labels = 0;

	// int r_, g_, b_, a_;
	unsigned int col_int_value;
	struct attr attr_cc;

	// // dbg(0,"ooo enter ooo %d\n",displaylist->order);

	int order_corrected = displaylist->order + shift_order;
	int saved = displaylist->order;
	if (order_corrected < limit_order_corrected)
	{
		order_corrected = limit_order_corrected;
	}

	if (displaylist->order != displaylist->order_hashed || displaylist->layout != displaylist->layout_hashed)
	{
		displaylist_update_hash(displaylist);
		displaylist->order_hashed = displaylist->order;
		displaylist->layout_hashed = displaylist->layout;
	}

	pro = transform_get_projection(displaylist->dc.trans);

	// if zoomed out too much then use prerendered map tiles
	if (global_clinedrawing_active)
	{
		if (displaylist->order_hashed < ORDER_USE_PRERENDERED_MAP__C)
		{
			draw_vector_map = 0;
		}

		if (displaylist->order_hashed > ORDER_USE_NORMAL_MAP__C)
		{
			draw_tile_map = 0;
		}
	}
	else
	{
		if (displaylist->order_hashed < ORDER_USE_PRERENDERED_MAP) // ==5 , (4,3,2,1,0)--> no vector map
		{
			draw_vector_map = 0;
		}

		if (displaylist->order_hashed > ORDER_USE_NORMAL_MAP) // ==6 , (7,8,9, ...)--> no tile map
		{
			draw_tile_map = 0;
		}
	}

	if (global_show_maps_debug_view)
	{
		draw_vector_map = 1;
		draw_tile_map = 0;
	}

	displaylist->order = order_corrected;

	//DBG // dbg(0, "XXXXXYYYYYYY Draw: 003\n");

	// reset value;
	// cancel_drawing_global = 0;

	char *ttt22 = NULL;


	while (!cancel)
	{
		if (!displaylist->msh)
		{
			displaylist->msh = mapset_open(displaylist->ms);
		}

		if (!displaylist->m)
		{
			displaylist->m = mapset_next(displaylist->msh, 1);
			if (!displaylist->m)
			{
				mapset_close(displaylist->msh);
				displaylist->msh = NULL;
				break;
			}

			mapset_counter++;
			mapset_need_draw = 1;
			only_labels = 0;

			struct attr map_name_attr;

			if (map_get_attr(displaylist->m, attr_name, &map_name_attr, NULL))
			{
				// dbg(0,"MAP:#+* start reading map file #+*=%s\n", map_name_attr.u.str);
				if (ttt22)
				{
					g_free(ttt22);
				}
				ttt22 = g_strdup(map_name_attr.u.str);

				if (strncmp("_ms_sdcard_map:", map_name_attr.u.str, 15) == 0)
				{
					if (strncmp("_ms_sdcard_map:/sdcard/zanavi/maps/borders.bin", map_name_attr.u.str, 41) == 0)
					{
						if (draw_tile_map == 1)
						{
							// if its the countryborder map
							mapset_need_draw = 0;
						}
						else if (displaylist->order_hashed >= ORDER_USE_BORDERS_MAP)
						{
							// on high detail -> dont draw bordermap anymore. it sometimes causes heavy slowdowns!!
							mapset_need_draw = 0;
						}
					}

/*
					else if (strncmp("_ms_sdcard_map:/sdcard/zanavi/maps/coastline.bin", map_name_attr.u.str, 48) == 0)
					{
						if (draw_tile_map == 1)
						{
							// if its the countryborder map
							mapset_need_draw = 0;
						}
					}
*/


#if 0
					else if (strncmp("_ms_sdcard_map:-special-:worldmap6.txt", map_name_attr.u.str, 38) == 0)
					{
						// if its a worldmapX.txt
						if (draw_vector_map == 1)
						{
							//dbg(0, "wm5: v=1");
							mapset_need_draw = 0;
						}
						else
						{
							//dbg(0, "wm5: v=0");

							if (displaylist->order_hashed < 4)
							{
								//dbg(0, "wm5: nd=0");
								mapset_need_draw = 0;
							}
							else
							{
								//dbg(0, "wm5: nd=1");
								mapset_need_draw = 1;
							}
						}
					}
#endif
					else if (strncmp("_ms_sdcard_map:-special-:worldmap5.txt", map_name_attr.u.str, 38) == 0)
					{
						// if its a worldmapX.txt
						if (draw_tile_map == 0)
						{
							mapset_need_draw = 0;
						}
						else
						{
							if (displaylist->order_hashed < 3)
							{
								mapset_need_draw = 0;
							}
							else
							{
								mapset_need_draw = 1;
							}
						}
					}
					else if (strncmp("_ms_sdcard_map:-special-:worldmap2.txt", map_name_attr.u.str, 38) == 0)
					{
						// if its a worldmapX.txt
						if (draw_tile_map == 0)
						{
							mapset_need_draw = 0;
						}
						else
						{
							if (displaylist->order_hashed < 3)
							{
								mapset_need_draw = 1;
							}
							else
							{
								mapset_need_draw = 0;
							}
						}
					}
					else
					{
						// if its an sdcard street map
						if (draw_vector_map == 1)
						{
							mapset_need_draw = 1;
							if (draw_tile_map == 1)
							{
								only_labels = 1;
							}
						}
						else
						{
							mapset_need_draw = 0;
						}
					}
				}
			}

			////DBG // dbg(0,"---------==============>>>>>>>>>> ***** %d ****",mapset_counter);

			displaylist->dc.pro = map_projection(displaylist->m);
			displaylist->conv = map_requires_conversion(displaylist->m);
			if (route_selection)
			{
				displaylist->sel = route_selection;
			}
			else
			{
				displaylist->sel = transform_get_selection(displaylist->dc.trans, displaylist->dc.pro, displaylist->order);
			}
			displaylist->mr = map_rect_new(displaylist->m, displaylist->sel);
		}


		if (displaylist->mr)
		{
			// dbg(0,"MAP:001:map=%s\n", ttt22);
			// draw vector map, or not?
			// dbg(0,"draw_vector_map=%d mapset_need_draw=%d\n", draw_vector_map, mapset_need_draw);
			// ****** if ((draw_vector_map) || (mapset_need_draw == 1))
			if (mapset_need_draw == 1)
			{
				//dbg(0,"MAP:002:map=%s\n", ttt22);
				//if (ttt22)
				//{
				//	system_log(0, "MAP:map=%s\n", ttt22);
				//}
				//else
				//{
				//	system_log(0, "MAP:map=NULL\n");
				//}

				//// dbg(0, "XXXXXYYYYYYY Draw: A.01\n");


				// ------ READ all items in this map rectangle ---------
				// ------ READ all items in this map rectangle ---------
				// ------ READ all items in this map rectangle ---------
				// ------ READ all items in this map rectangle ---------
				// ------ READ all items in this map rectangle ---------
				// dbg(0,"#+* start reading map file #+*\n");
				int _item_counter_ = 0;
				while ((item = map_rect_get_item(displaylist->mr)))
				{
					//dbg(0,"MAP:003:map=%s\n", ttt22);

					_item_counter_++;

					int label_count = 0;
					char *labels[3];
					struct hash_entry *entry;

					if (cancel_drawing_global == 1)
					{
						// stop drawing map is requested
						//DBG // dbg(0, "** STOP MD 001 **\n");
						break;
					}

					if (item == &busy_item)
					{
						if (displaylist->workload)
						{
							// restore order :-)
							displaylist->order = saved;

							//// dbg(0,"set:0:displaylist->busy=%d\n",displaylist->busy);
							displaylist->busy = 0;

							// dbg(0, "DO__DRAW:%d UN-lock mutex 002\n", rnd);
							pthread_mutex_unlock(&uiConditionMutex);
							// dbg(0, "DO__DRAW:%d OK UN-lock mutex 002\n", rnd);

							//// dbg(0,"return 002\n");
							// dbg(0, "DO__DRAW:%d return 002\n", rnd);
							return2;
						}
						else
						{
							continue;
						}
					}

					if (only_labels == 1)
					{
						if ((!item_is_town(*item)) && (!item_is_district(*item)))
						{
							// if its not a label, dont draw it
							continue;
						}
					}

					entry = get_hash_entry(displaylist, item->type);

					//// dbg(0, "XXXXXYYYYYYY Draw: A.item1 %p %i\n", entry, item->type);

					// DEBUG -------- zoffzoff
					// DEBUG -------- zoffzoff
					// DEBUG -------- zoffzoff
					//item_dump_attr_stdout(item, displaylist->m);
					// DEBUG -------- zoffzoff
					// DEBUG -------- zoffzoff
					// DEBUG -------- zoffzoff

					//if (item->type == type_rg_segment)
					//{
					//	system_log(0, "type_rg_segment:001");
					//}

					if (!entry)
					{
						continue;
					}

					count = item_coord_get_within_selection(item, ca, item->type < type_line ? 1 : max, displaylist->sel);

					if (!count)
					{
						continue;
					}

					//dbg(0,"MAP:004:map=%s\n", ttt22);

					//// dbg(0, "XXXXXYYYYYYY Draw: A.item2\n");

					if (displaylist->dc.pro != pro)
					{
						//dbg(0,"from to\n");
						transform_from_to_count(ca, displaylist->dc.pro, ca, pro, count);
					}

					if (count == max)
					{
						//DBG // dbg(0,"point count overflow %d for %s "ITEM_ID_FMT"\n", count,item_to_name(item->type),ITEM_ID_ARGS(*item));
						displaylist->dc.maxlen = max * 2;
					}

					//if (item_is_custom_poi(*item))
					//{
					//	if (item_attr_get(item, attr_icon_src, &attr2))
					//	{
					//		labels[1] = map_convert_string(displaylist->m, attr2.u.str);
					//	}
					//	else
					//	{
					//		labels[1] = NULL;
					//	}
					//	label_count = 2;
					//}
					//else
					//{
						labels[0] = NULL;
						labels[1] = NULL;
						labels[2] = NULL;
						label_count = 0;
					//}

					// DEBUG -------- zoffzoff
					// DEBUG -------- zoffzoff
					// DEBUG -------- zoffzoff
					// item_dump_attr_stdout(item, displaylist->m);
					// DEBUG -------- zoffzoff
					// DEBUG -------- zoffzoff
					// DEBUG -------- zoffzoff






					// --------======== LABELS ========--------
					// --------======== LABELS ========--------
					if (item_attr_get(item, attr_label, &attr))
					{
						if (global_show_english_labels < 2)
						{
							// NORMAL
							//dbg(0, "c:%d name=%s\n", label_count, attr.u.str);
							labels[label_count] = attr.u.str;
							label_count++;
						}
					}

					if (item_attr_get(item, attr_street_name_match, &attr))
					{
						if ((global_show_english_labels == 1)||(global_show_english_labels == 2))
						{
							// ENGLISH or alternate
							//dbg(0, "c:%d street_en_name=%s\n", label_count, attr.u.str);
							labels[label_count] = attr.u.str;
							label_count++;
						}
					}
					else if (item_attr_get(item, attr_town_name_match, &attr))
					{
						if ((global_show_english_labels == 1)||(global_show_english_labels == 2))
						{
							// ENGLISH or alternate
							//dbg(0, "c:%d town_en_name=%s\n", label_count, attr.u.str);
							labels[label_count] = attr.u.str;
							label_count++;
						}
					}
					else
					{
						if (global_show_english_labels == 2)
						{
							// we want ENGLISH labels, but the item does not have it
							// --> so show normal label instead (item should not be nameless on map)
							if (item_attr_get(item, attr_label, &attr))
							{
								// NORMAL
								//dbg(0, "c:%d name=%s\n", label_count, attr.u.str);
								labels[label_count] = attr.u.str;
								label_count++;
							}
						}
					}
					// --------======== LABELS ========--------
					// --------======== LABELS ========--------






					// DEBUG -------- zoffzoff
					// DEBUG -------- zoffzoff
					// DEBUG -------- zoffzoff
					// item_dump_attr_stdout(item, displaylist->m);
					// DEBUG -------- zoffzoff
					// DEBUG -------- zoffzoff
					// DEBUG -------- zoffzoff

					struct attr attr_77;
					if (item_attr_get(item, attr_flags, &attr_77))
					{
						// // dbg(0,"uuuuuuuuuuuuu %s uuuuu %d\n",item_to_name(item->type), attr_77.u.num);
						item->flags = attr_77.u.num;
					}
					else
					{
						item->flags = 0;
					}

					//struct attr *attr88;
					//if (item_attr_get(item, attr_flags, &attr88))
					//{
					////DBG // dbg(0,"item flags=%d\n",attr88->u.num);
					//}
					//attr88=NULL;


					/*
					 if (item->flags & NAVIT_AF_UNDERGROUND)
					 {
					 // dbg(0,"is UNDERGROUND\n");
					 }
					 else if (item->flags & NAVIT_AF_BRIDGE)
					 {
					 // dbg(0,"is BRIDGE\n");
					 }
					 */

					// a_=0;
					if (item_attr_get(item, attr_colour2, &attr_cc))
					{

						col_int_value = (unsigned int)attr_cc.u.num;
						//dbg(0, "ccooll:1 c=%d\n", col_int_value);

						//r_= (col_int_value >> 16) & 0xff;
						//g_= (col_int_value >> 8) & 0xff;
						//b_= col_int_value & 0xff;

						//dbg(0, "ccooll:2 r=%d g=%d b=%d\n", r_,g_,b_);

						//r_ = r_ << 8;
						//g_ = g_ << 8;
						//b_ = b_ << 8;

						//dbg(0, "ccooll:3 r=%d g=%d b=%d\n", r_,g_,b_);

						//a_ = 0xffff;
					}
					else
					{
						col_int_value = 0;
					}


					struct attr attr_88;
					if ((item->type == type_street_route) || (item->type == type_street_route_waypoint))
					{
						if (item_attr_get(item, attr_direction, &attr_88))
						{
							//dbg(0, "direction(1a1)=%d %x\n", attr_88.u.num, col_int_value);
							col_int_value = col_int_value & 0xffffff;
							//dbg(0, "direction(1a2)=%x %x\n", attr_88.u.num, col_int_value);
							//dbg(0, "direction(1a2.1)=%x %x\n", (attr_88.u.num & 3), ((attr_88.u.num & 3) << 24));
							col_int_value = col_int_value | ((attr_88.u.num & 3) << 24);
							//dbg(0, "direction(1a3)=%x %x\n", attr_88.u.num, col_int_value);
						}
						else
						{
							//dbg(0, "direction(1b1)=%x\n", col_int_value);
							col_int_value = col_int_value & 0xffffff;
							//dbg(0, "direction(1b2)=%x\n", col_int_value);
						}

						if (item_attr_get(item, attr_details, &attr_88))
						{
							// dbg(0, "direction(0)=%x\n", attr_88.u.num);

							// #define NAVIT_AF_ONEWAY				(1<<0)
							// #define NAVIT_AF_ONEWAYREV			(1<<1)
							// #define NAVIT_AF_ONEWAY_BICYCLE_NO	(1<<16)

							if (attr_88.u.num & NAVIT_AF_ONEWAY_BICYCLE_NO)
							{
								// dbg(0, "direction(0)=%x\n", (attr_88.u.num & (NAVIT_AF_ONEWAY|NAVIT_AF_ONEWAYREV)));
								// dbg(0, "direction(0.1)=%x %x\n", col_int_value, (attr_88.u.num & (NAVIT_AF_ONEWAY|NAVIT_AF_ONEWAYREV)) << 26 );
								col_int_value = col_int_value | ((attr_88.u.num & (NAVIT_AF_ONEWAY|NAVIT_AF_ONEWAYREV)) << 26);
							}
						}
					}


					// --------======== LABELS ========--------
					// --------======== LABELS ========--------
					if (label_count > 0)
					{
						labels[0] = map_convert_string(displaylist->m, labels[0]);
					}
					if (label_count > 1)
					{
						labels[1] = map_convert_string(displaylist->m, labels[1]);
					}

					display_add(entry, item, count, ca, labels, label_count, 1, col_int_value);

					if (label_count > 0)
					{
						map_convert_free(labels[0]);
					}
					if (label_count > 1)
					{
						map_convert_free(labels[1]);
					}

					//if (displaylist->conv && label_count)
					//{
					//	labels[0] = map_convert_string(displaylist->m, labels[0]);
					//	display_add(entry, item, count, ca, labels, label_count, 1, col_int_value);
					//	map_convert_free(labels[0]);
					//}
					//else
					//{
					//	display_add(entry, item, count, ca, labels, label_count, 1, col_int_value);
					//}
					//if (labels[1])
					//{
					//	map_convert_free(labels[1]);
					//}
					// --------======== LABELS ========--------
					// --------======== LABELS ========--------


					//workload++;
					/*
					 if (workload == displaylist->workload)
					 {
					 // restore order :-)
					 displaylist->order = saved;
					 // reset value;
					 cancel_drawing_global = 0;

					 // dbg(0,"set:0:displaylist->busy=%d\n",displaylist->busy);
					 displaylist->busy = 0;

					 // dbg(0,"return 003\n");
					 return2;
					 }
					 */

				} // while item=map_rect_get_item
				// ------ READ all items in this map rectangle ---------
				// ------ READ all items in this map rectangle ---------
				// ------ READ all items in this map rectangle ---------
				// ------ READ all items in this map rectangle ---------
				// ------ READ all items in this map rectangle ---------

				//dbg(0,"MAP:013:map=%s\n", ttt22);

				////DBG // dbg(0, "XXXXXYYYYYYY Draw: A.02\n");

				// ************** map_rect_destroy(displaylist->mr);
			}

			map_rect_destroy(displaylist->mr);
		}


		if (!route_selection)
		{
			map_selection_destroy(displaylist->sel);
		}

		displaylist->mr = NULL;
		displaylist->sel = NULL;
		displaylist->m = NULL;
	} // while ----


	// dbg(0, "DO__DRAW:%d load ready\n", rnd);

#ifdef NAVIT_MEASURE_TIME_DEBUG
	debug_mrp("do_draw:load", debug_measure_end(s_));
#endif
	s_ = debug_measure_start();



	// dbg(0, "DO__DRAW:%d dummy \"draw-start\" signal\n", rnd);
#ifdef HAVE_API_ANDROID
		android_return_generic_int(2, 77);
#endif

	// remove the "wait" screen
	//#ifdef HAVE_API_ANDROID
	//	android_return_generic_int(2, 0);
	//#endif

	//DBG // dbg(0, "XXXXXYYYYYYY Draw: 004\n");

	// reset value;
	// cancel_drawing_global = 0;

	// restore order :-)
	displaylist->order = saved;

	// profile(1,"process_selection\n");


	if (displaylist->idle_ev)
	{
		event_remove_idle(displaylist->idle_ev);
		displaylist->idle_ev = NULL;
	}

	if (displaylist->idle_cb)
	{
		callback_destroy(displaylist->idle_cb);
		displaylist->idle_cb = NULL;
	}

	//// dbg(0,"set:0:displaylist->busy=%d\n",displaylist->busy);
	//displaylist->busy = 0;

	// graphics_process_selection(displaylist->dc.gra, displaylist);

	//profile(1, "draw\n");

	//DBG // dbg(0, "XXXXXYYYYYYY Draw: 005\n");

	if (!cancel)
	{
		int flags2 = flags;
		if (!(flags2 & 2))
		{
			// -- now always clean bg of screen!! 2013-07-08 Zoff
			//if (!draw_vector_map)
			//{
			//	// dont clean bg of screen when drawing prerendered tiles
			//	flags2 = flags2 + 2;
			//}
		}
		//DBG // dbg(0,"call graphics_displaylist_draw 3")
		//// dbg(0,"# MT:002 #\n");

		// stop drawing is requested
		if (cancel_drawing_global != 1)
		{
			graphics_displaylist_draw(displaylist->dc.gra, displaylist, displaylist->dc.trans, displaylist->layout, flags2);
		}
		//// dbg(0,"# MT:003 #\n");
	}

#ifdef NAVIT_MEASURE_TIME_DEBUG
	debug_mrp("do_draw:draw", debug_measure_end(s_));
#endif

	// dbg(0, "DO__DRAW:%d draw ready\n", rnd);

#ifdef HAVE_API_ANDROID
	if (cur_mapdraw_time_index < 11)
	{
		mapdraw_time[cur_mapdraw_time_index] = debug_measure_end_tsecs(s_);
		// // dbg(0,"maptime: %d\n", mapdraw_time[cur_mapdraw_time_index]);
		cur_mapdraw_time_index++;
	}

	if (cur_mapdraw_time_index > 10)
	{
		cur_mapdraw_time_index = 0;
		int jk;
		int mean_time = 0;
		for (jk=0;jk<11;jk++)
		{
			mean_time = mean_time + mapdraw_time[jk];
		}
		android_return_generic_int(6, (int)((float)mean_time / (float)10));
	}
#endif

	//// dbg(0,"set:0:displaylist->busy=%d\n",displaylist->busy);
	displaylist->busy = 0;

	//DBG // dbg(0, "XXXXXYYYYYYY Draw: 006\n");

	map_rect_destroy(displaylist->mr);
	if (!route_selection)
	{
		map_selection_destroy(displaylist->sel);
	}
	mapset_close(displaylist->msh);
	displaylist->mr = NULL;
	displaylist->sel = NULL;
	displaylist->m = NULL;
	displaylist->msh = NULL;

	//dbg(0, "callback\n");

	// only some old crap need this!! ----------
	// only some old crap need this!! ----------
	///  ****   callback_call_1(displaylist->cb, cancel);
	// only some old crap need this!! ----------
	// only some old crap need this!! ----------


	// dbg(0, "DO__DRAW:%d UN-lock mutex leave\n", rnd);
	pthread_mutex_unlock(&uiConditionMutex);
	// dbg(0, "DO__DRAW:%d OK UN-lock mutex leave\n", rnd);

	// dbg(0, "DO__DRAW:%d cancel_drawing_global 99=%d\n", rnd, cancel_drawing_global);

	if (cancel_drawing_global != 1)
	{
		// dummy "ready" signal ------------------------------------------
		// dbg(0, "DO__DRAW:%d dummy \"ready\" signal\n", rnd);
		// gra->meth.draw_lines4(displaylist->dc.gra, NULL, NULL, NULL, 1, 1, 99);
#ifdef HAVE_API_ANDROID
		android_return_generic_int(2, 2);
#endif
		// dummy "ready" signal ------------------------------------------
	}
	else
	{
		// dummy "cancel" signal ------------------------------------------
		// dbg(0, "DO__DRAW:%d dummy \"cancel\" signal\n", rnd);
		//gra->meth.draw_lines4(displaylist->dc.gra, NULL, NULL, NULL, 1, 1, 95);
#ifdef HAVE_API_ANDROID
		android_return_generic_int(2, 3);
#endif
		// dummy "ready" signal ------------------------------------------
	}

	// dbg(0, "DO__DRAW:%d leave\n", rnd);
	// dbg(0, "DO__DRAW:%d __\n", rnd);

__F_END__
}

/**
 * FIXME
 * @param <>
 * @returns <>
 * @author Martin Schaller (04/2008)
 */
void graphics_displaylist_draw(struct graphics *gra, struct displaylist *displaylist, struct transformation *trans, struct layout *l, int flags)
{
__F_START__

	// // dbg(0,"ooo enter ooo flags=%d\n", flags);


	int order = transform_get_order(trans);
	displaylist->dc.trans = trans;
	displaylist->dc.gra = gra;

	// *********DISABLED*******
	// *********DISABLED*******
	// *********DISABLED*******
	// set min. distancte of 2 points on line at which a point will be left out when zoomed out too much!!
	// *********DISABLED******* displaylist->dc.mindist = transform_get_scale(trans) / 2;
	//// dbg(0,"mindist would be:%d\n", (int)(transform_get_scale(trans) / 2));
	displaylist->dc.mindist = 0;
	if (order < 6)
	{
		displaylist->dc.mindist = transform_get_scale(trans) * 4;
	}
	else if (order < 9)
	{
		displaylist->dc.mindist = transform_get_scale(trans) * 3;
	}
	else if (order < 13)
	{
		displaylist->dc.mindist = transform_get_scale(trans) * 2;
	}
	// *********DISABLED*******
	// *********DISABLED*******
	// *********DISABLED*******


	// FIXME find a better place to set the background color
	if (l)
	{

		if (global_night_mode == 1)
		{
			graphics_gc_set_background(gra->gc[0], &background_night_mode);
			graphics_gc_set_foreground(gra->gc[0], &background_night_mode);
		}
		else
		{
			graphics_gc_set_background(gra->gc[0], &l->color);
			graphics_gc_set_foreground(gra->gc[0], &l->color);
		}
		gra->default_font = g_strdup(l->font);
	}

	graphics_background_gc(gra, gra->gc[0]);

	if (flags & 1)
	{
		// calls -> navit.c navit_predraw --> draw all vehicles
		// *********++-- DISABLED --++******* // callback_list_call_attr_0(gra->cbl, attr_predraw);
		navit_predraw(global_navit);
	}

	gra->meth.draw_mode(gra->priv, (flags & 8) ? draw_mode_begin_clear : draw_mode_begin);

	if (!(flags & 2))
	{
		// clear the gfx object pipeline ------------------------------
		// // dbg(0, "clear the gfx object pipeline\n");
		// gra->meth.draw_lines4(gra->priv, NULL, NULL, NULL, 1, 1, 98);

		// clear the display/screen/whatever here
		// dbg(0, "clear the screen: rectangle=%d,%d - %d,%d\n", gra->r.lu.x, gra->r.lu.y, gra->r.rl.x, gra->r.rl.y);
		gra->meth.draw_rectangle(gra->priv, gra->gc[0]->priv, &gra->r.lu, gra->r.rl.x - gra->r.lu.x, gra->r.rl.y - gra->r.lu.y);
	}

	if (l)
	{
		// draw the mapitems
		// // dbg(0,"o , l->d = %d , %d\n",order,l->order_delta);
		xdisplay_draw(displaylist, gra, l, order + l->order_delta);
	}

	if (flags & 1)
	{
		// calls: "graphics_displaylist_draw"
		// ***********++-- DISABLED --++*********** // callback_list_call_attr_0(gra->cbl, attr_postdraw);
	}

	if (!(flags & 4))
	{
		gra->meth.draw_mode(gra->priv, draw_mode_end);
	}

__F_END__
}

static void graphics_load_mapset(struct graphics *gra, struct displaylist *displaylist, struct mapset *mapset, struct transformation *trans, struct layout *l, int async, struct callback *cb, int flags)
{
__F_START__

	int order = transform_get_order(trans);

	if (displaylist->busy)
	{
		if (async == 1)
		{
			return2;
		}
		return2;
	}
	xdisplay_free(displaylist);

	displaylist->dc.gra = gra;
	displaylist->ms = mapset;
	displaylist->dc.trans = trans;
	displaylist->workload = async ? 100 : 0;
	displaylist->cb = cb;
	displaylist->seq++;

	if (l)
	{
		order += l->order_delta;
	}
	displaylist->order = order;
	displaylist->busy = 1;
	displaylist->layout = l;

	// ---------- DISABLED ------ no more async!!
	/*
	 if (async)
	 {
	 //DBG // dbg(0,"§§async");
	 if (!displaylist->idle_cb)
	 {
	 //DBG // dbg(0,"§§async --> callback");
	 displaylist->idle_cb = callback_new_3(callback_cast(do_draw), displaylist, 0, flags);
	 }
	 //DBG // dbg(0,"§§async --> add idle");
	 displaylist->idle_ev = event_add_idle(50, displaylist->idle_cb);
	 }
	 else
	 {
	 //DBG // dbg(0,"@@sync");
	 do_draw(displaylist, 0, flags);
	 }
	 */

	if (async)
	{
		//if (!displaylist->idle_cb)
		//{
			// this calls "do_draw"
			// *++*-- DISABLED --*++* // displaylist->idle_cb = callback_new_3(callback_cast(do_draw), displaylist, 0, flags);
			// *++*-- DISABLED --*++* // callback_add_names(displaylist->idle_cb, "graphics_load_mapset", "do_draw");
			do_draw(displaylist, 0, flags);
		//}
		//dbg(0, "DO__DRAW:call 003 (async callback)\n");
		// *++*-- DISABLED --*++* // displaylist->idle_ev = event_add_idle(1000, displaylist->idle_cb);
	}
	else
	{
		//// dbg(0,"**draw 2.b5\n");
		// dbg(0, "DO__DRAW:call 001\n");
		do_draw(displaylist, 0, flags);
	}

__F_END__
}

/**
 * FIXME
 * @param <>
 * @returns <>
 * @author Martin Schaller (04/2008)
 */
void graphics_draw(struct graphics *gra, struct displaylist *displaylist, struct mapset *mapset, struct transformation *trans, struct layout *l, int async, struct callback *cb, int flags)
{
__F_START__
	//// dbg(0,"ooo enter ooo\n");

	// dbg(0, "DO__DRAW:gras_draw enter\n");
	graphics_load_mapset(gra, displaylist, mapset, trans, l, async, cb, flags);
	// dbg(0, "DO__DRAW:gras_draw leave\n");
__F_END__
}

int graphics_draw_cancel(struct graphics *gra, struct displaylist *displaylist)
{
__F_START__

	if (!displaylist->busy)
	{
		return2 0;
	}
	// dbg(0, "DO__DRAW:call 002\n");
	do_draw(displaylist, 1, 0);
	return2 1;

__F_END__
}

/**
 * FIXME
 * @param <>
 * @returns <>
 * @author Martin Schaller (04/2008)
 */
struct displaylist_handle
{
	struct displaylist *dl;
	struct displayitem *di;
	int hashidx;
};

/**
 * FIXME
 * @param <>
 * @returns <>
 * @author Martin Schaller (04/2008)
 */
struct displaylist_handle * graphics_displaylist_open(struct displaylist *displaylist)
{
	struct displaylist_handle *ret;

	ret=g_new0(struct displaylist_handle, 1);
	ret->dl = displaylist;

	return ret;
}

/**
 * FIXME
 * @param <>
 * @returns <>
 * @author Martin Schaller (04/2008)
 */
struct displayitem * graphics_displaylist_next(struct displaylist_handle *dlh)
{
	struct displayitem *ret;
	if (!dlh)
		return NULL;
	for (;;)
	{
		if (dlh->di)
		{
			ret = dlh->di;
			dlh->di = ret->next;
			break;
		}
		if (dlh->hashidx == HASH_SIZE_GRAPHICS_)
		{
			ret = NULL;
			break;
		}
		if (dlh->dl->hash_entries[dlh->hashidx].type)
			dlh->di = dlh->dl->hash_entries[dlh->hashidx].di;
		dlh->hashidx++;
	}
	return ret;
}

/**
 * FIXME
 * @param <>
 * @returns <>
 * @author Martin Schaller (04/2008)
 */
void graphics_displaylist_close(struct displaylist_handle *dlh)
{
	g_free(dlh);
}

/**
 * FIXME
 * @param <>
 * @returns <>
 * @author Martin Schaller (04/2008)
 */
struct displaylist * graphics_displaylist_new(void)
{
	struct displaylist *ret=g_new0(struct displaylist, 1);

	ret->dc.maxlen = 16384;
	ret->busy = 0;

	return ret;
}

/**
 * FIXME
 * @param <>
 * @returns <>
 * @author Martin Schaller (04/2008)
 */
struct item * graphics_displayitem_get_item(struct displayitem *di)
{
	return &di->item;
}

int graphics_displayitem_get_coord_count(struct displayitem *di)
{
	return di->count;
}

/**
 * FIXME
 * @param <>
 * @returns <>
 * @author Martin Schaller (04/2008)
 */
char * graphics_displayitem_get_label(struct displayitem *di)
{
	return di->label;
}

int graphics_displayitem_get_displayed(struct displayitem *di)
{
	return 1;
}

/**
 * FIXME
 * @param <>
 * @returns <>
 * @author Martin Schaller (04/2008)
 */
static int within_dist_point(struct point *p0, struct point *p1, int dist)
{
	if (p0->x == 32767 || p0->y == 32767 || p1->x == 32767 || p1->y == 32767)
		return 0;
	if (p0->x == -32768 || p0->y == -32768 || p1->x == -32768 || p1->y == -32768)
		return 0;
	if ((p0->x - p1->x) * (p0->x - p1->x) + (p0->y - p1->y) * (p0->y - p1->y) <= dist * dist)
	{
		return 1;
	}
	return 0;
}

/**
 * FIXME
 * @param <>
 * @returns <>
 * @author Martin Schaller (04/2008)
 */
static int within_dist_line(struct point *p, struct point *line_p0, struct point *line_p1, int dist)
{
	int vx, vy, wx, wy;
	int c1, c2;
	struct point line_p;

	if (line_p0->x < line_p1->x)
	{
		if (p->x < line_p0->x - dist)
			return 0;
		if (p->x > line_p1->x + dist)
			return 0;
	}
	else
	{
		if (p->x < line_p1->x - dist)
			return 0;
		if (p->x > line_p0->x + dist)
			return 0;
	}
	if (line_p0->y < line_p1->y)
	{
		if (p->y < line_p0->y - dist)
			return 0;
		if (p->y > line_p1->y + dist)
			return 0;
	}
	else
	{
		if (p->y < line_p1->y - dist)
			return 0;
		if (p->y > line_p0->y + dist)
			return 0;
	}

	vx = line_p1->x - line_p0->x;
	vy = line_p1->y - line_p0->y;
	wx = p->x - line_p0->x;
	wy = p->y - line_p0->y;

	c1 = vx * wx + vy * wy;
	if (c1 <= 0)
		return within_dist_point(p, line_p0, dist);
	c2 = vx * vx + vy * vy;
	if (c2 <= c1)
		return within_dist_point(p, line_p1, dist);

	line_p.x = line_p0->x + vx * c1 / c2;
	line_p.y = line_p0->y + vy * c1 / c2;
	return within_dist_point(p, &line_p, dist);
}

/**
 * FIXME
 * @param <>
 * @returns <>
 * @author Martin Schaller (04/2008)
 */
static int within_dist_polyline(struct point *p, struct point *line_pnt, int count, int dist, int close)
{
	int i;
	for (i = 0; i < count - 1; i++)
	{
		if (within_dist_line(p, line_pnt + i, line_pnt + i + 1, dist))
		{
			return 1;
		}
	}
	if (close)
		return (within_dist_line(p, line_pnt, line_pnt + count - 1, dist));
	return 0;
}

/**
 * FIXME
 * @param <>
 * @returns <>
 * @author Martin Schaller (04/2008)
 */
static int within_dist_polygon(struct point *p, struct point *poly_pnt, int count, int dist)
{
	int i, j, c = 0;
	for (i = 0, j = count - 1; i < count; j = i++)
	{
		if ((((poly_pnt[i].y <= p->y) && (p->y < poly_pnt[j].y)) || ((poly_pnt[j].y <= p->y) && (p->y < poly_pnt[i].y))) && (p->x < (poly_pnt[j].x - poly_pnt[i].x) * (p->y - poly_pnt[i].y) / (poly_pnt[j].y - poly_pnt[i].y) + poly_pnt[i].x))
			c = !c;
	}
	if (!c)
		return within_dist_polyline(p, poly_pnt, count, dist, 1);
	return c;
}

/**
 * FIXME
 * @param <>
 * @returns <>
 * @author Martin Schaller (04/2008)
 */
int graphics_displayitem_within_dist(struct displaylist *displaylist, struct displayitem *di, struct point *p, int dist)
{
	struct point *pa = g_alloca(sizeof(struct point) * displaylist->dc.maxlen);
	int count;

	count = transform(displaylist->dc.trans, displaylist->dc.pro, di->c, pa, di->count, 1, 0, NULL);

	if (di->item.type < type_line)
	{
		return within_dist_point(p, &pa[0], dist);
	}
	if (di->item.type < type_area)
	{
		return within_dist_polyline(p, pa, count, dist, 0);
	}
	return within_dist_polygon(p, pa, count, dist);
}

static void graphics_process_selection_item(struct displaylist *dl, struct item *item)
{
#if 0 /* FIXME */
	struct displayitem di,*di_res;
	GHashTable *h;
	int count,max=dl->dc.maxlen;
	struct coord ca[max];
	struct attr attr;
	struct map_rect *mr;

	di.item=*item;
	di.label=NULL;
	di.count=0;
	h=g_hash_table_lookup(dl->dl, GINT_TO_POINTER(di.item.type));
	if (h)
	{
		di_res=g_hash_table_lookup(h, &di);
		if (di_res)
		{
			di.item.type=(enum item_type)item->priv_data;
			display_add(dl, &di.item, di_res->count, di_res->c, NULL, 0);
			return;
		}
	}

	mr=map_rect_new(item->map, NULL);
	item=map_rect_get_item_byid(mr, item->id_hi, item->id_lo);
	count=item_coord_get(item, ca, item->type < type_line ? 1: max);

	if (!item_attr_get(item, attr_label, &attr))
	{
	attr.u.str=NULL;
	}

	if (dl->conv && attr.u.str && attr.u.str[0])
	{
		char *str=map_convert_string(item->map, attr.u.str);
		display_add(dl, item, count, ca, &str, 1);
		map_convert_free(str);
	}
	else
	display_add(dl, item, count, ca, &attr.u.str, 1);
	map_rect_destroy(mr);
#endif
}

void graphics_add_selection(struct graphics *gra, struct item *item, enum item_type type, struct displaylist *dl)
{
	struct item *item_dup=g_new(struct item, 1);
	*item_dup = *item;
	item_dup->priv_data = (void *) type;
	gra->selection = g_list_append(gra->selection, item_dup);
	if (dl)
		graphics_process_selection_item(dl, item_dup);
}

void graphics_remove_selection(struct graphics *gra, struct item *item, enum item_type type, struct displaylist *dl)
{
	GList *curr;
	int found;

	for (;;)
	{
		curr = gra->selection;
		found = 0;
		while (curr)
		{
			struct item *sitem = curr->data;
			if (item_is_equal(*item, *sitem))
			{
				if (dl)
				{
					struct displayitem di;
					/* Unused Variable
					 GHashTable *h; */
					di.item = *sitem;
					di.label = NULL;
					di.count = 0;
					di.item.type = type;
#if 0 /* FIXME */
					h=g_hash_table_lookup(dl->dl, GINT_TO_POINTER(di.item.type));
					if (h)
					g_hash_table_remove(h, &di);
#endif
				}
				g_free(sitem);
				gra->selection = g_list_remove(gra->selection, curr->data);
				found = 1;
				break;
			}
		}
		if (!found)
			return;
	}
}

void graphics_clear_selection(struct graphics *gra, struct displaylist *dl)
{
	while (gra->selection)
	{
		struct item *item = (struct item *) gra->selection->data;
		graphics_remove_selection(gra, item, (enum item_type) item->priv_data, dl);
	}
}

static void graphics_process_selection(struct graphics *gra, struct displaylist *dl)
{
	GList *curr;

	curr = gra->selection;
	while (curr)
	{
		struct item *item = curr->data;
		graphics_process_selection_item(dl, item);
		curr = g_list_next(curr);
	}
}
