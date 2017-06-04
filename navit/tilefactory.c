/** jandegr
 * Some openGL test
 *
 * no oversampling (smooth lines) as long as it uses a pbuffer
 * several things to fix
 *
 * How to get the order the same as in the full mapview ?
 * tried order_corrected and other stuff FIXME FIXME FIXME
 *
 * plenty of stuff missing, poi's , dashed lines , ....
 *
 * for now works as a drop-in replacement for the lowqual preview map
 *
 * TODO : make it use Zanavi coding style
 *
 * WIP WIP WIP
 */




// apparently the order of the includes is important
// several unneeded

#if dedf0

#include <stdlib.h>
//#include <string.h>
//#include <poll.h>
#include <glib.h>
#include "android.h"
//#include <android/log.h>
#include <android/bitmap.h>
#include "navit.h"
//#include "config_.h"
//#include "command.h"
#include "debug.h"
#include "transform.h"
#include "map.h"
#include "coffeecatch.h"
#include "coffeejni.h"
#include "mapset.h"
#include "layout.h"
#include "graphics.h"
#include "item.h"
#include "attr.h"
#endif

#include <EGL/egl.h>
#include <GLES2/gl2.h>


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

struct openGL_hash_entry {
    int z;
    int defer;
    GList *elements;
};

GHashTable* htab = NULL;
int htab_order = 99999;
int mapcenter_x = 0;
int mapcenter_y = 0;

jclass NavitGraphicsClass3 = NULL;
jmethodID Preview_set_bitmap = NULL;


EGLConfig eglConf;
EGLSurface eglSurface;
EGLContext eglCtx;
EGLDisplay eglDisp;

GLuint gvMvprojHandle;
GLuint gvPositionHandle;
GLuint gvColorHandle;
GLuint gvMapcenterHandle;
GLuint gvZvalHandle;
GLfloat glMaxLineWidth;

// EGL config attributes
const EGLint confAttr[] = {
		EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT,    // very important!
		EGL_SURFACE_TYPE, EGL_PBUFFER_BIT,          // we will create a pixelbuffer surface
		EGL_RED_SIZE,   8,
		EGL_GREEN_SIZE, 8,
		EGL_BLUE_SIZE,  8,
		EGL_ALPHA_SIZE, 8,     // if you need the alpha channel
		EGL_DEPTH_SIZE, 16,    // if you need the depth buffer
		EGL_NONE
};

// EGL context attributes
const EGLint ctxAttr[] = {
		EGL_CONTEXT_CLIENT_VERSION, 2,
		EGL_NONE
};

//const char gVertexShader [] =
//		"attribute vec2 vPosition;\n"
//				"attribute vec2        texture_position; \n"
//				"uniform mat4        mvproj;\n"
//				"varying vec2 v_texture_position;\n"
//				"uniform lowp vec4  color;\n"
//				"void main() {\n"
//				"  v_texture_position=texture_position;\n"
//				"  gl_Position = mvproj * vec4(vPosition, 0, 1);\n"
//				"}\n";
// scaling seperate
//const char gVertexShader [] =
//        "attribute vec2 vPosition;\n"
//                "uniform float scaleX;\n"
//                "uniform vec4 mapcenter;\n"
//                "attribute vec2        texture_position; \n"
//                "uniform mat4        mvproj;\n"
//                "varying vec2 v_texture_position;\n"
//                "uniform lowp vec4  color;\n"
//                "void main() {\n"
//                "  v_texture_position=texture_position;\n"
//                "  gl_Position = mvproj * vec4( scaleX * (vPosition.x - mapcenter.x), scaleX * (vPosition.y - mapcenter.y) , 0, 1);\n"
//                "}\n";

// no scaling needed, already in matrix mapcenter handled in the vertexshader
const char gVertexShaderMapcenter [] =
        "attribute vec3 vPosition;\n"
                "uniform vec4 mapcenter;\n"
                "attribute vec2        texture_position; \n"
                "uniform mat4        mvproj;\n"
                "varying vec2 v_texture_position;\n"
                "uniform vec4  color;\n"
                "void main() {\n"
                "  v_texture_position=texture_position;\n"
                "  gl_Position = mvproj * vec4((vPosition.x - mapcenter.x), (vPosition.y - mapcenter.y) , vPosition.z, 1);\n"
                "}\n";

const char gVertexShader_3d [] =
        "attribute vec3 vPosition;\n"
                "attribute vec2        texture_position; \n"
                "uniform mat4        mvproj;\n"
                "varying vec2 v_texture_position;\n"
                "uniform vec4  color;\n"
                "void main() {\n"
                "  v_texture_position=texture_position;\n"
                "  gl_Position = mvproj * vec4((vPosition.x), (vPosition.y) , vPosition.z, 1);\n"
                "}\n";

const char gVertexShader [] =
        "attribute vec2 vPosition;\n"
                "attribute vec2        texture_position; \n"
                "uniform mat4        mvproj;\n"
                "varying vec2 v_texture_position;\n"
                "uniform vec4  color;\n"
                "uniform int  zval;\n"
                "void main() {\n"
                "  v_texture_position=texture_position;\n"
                "  gl_Position = mvproj * vec4((vPosition.x), (vPosition.y) , zval, 1);\n"
                "}\n";

const char gFragmentShader [] =
		"precision mediump float;\n"
				"uniform vec4  color;\n"
				"uniform sampler2D texture;\n"
				"uniform bool use_texture;\n"
				"uniform bool use_dashes;\n"
				"varying vec2 v_texture_position;\n"
				"void main() {\n"
				"   if (use_texture) {\n"
				"     gl_FragColor = texture2D(texture, v_texture_position);\n"
				"   }else if (use_dashes){\n"
				"     discard;\n"
				"   }else{\n"
				"     gl_FragColor = color;\n"
				"   }\n"
				"}\n";


static void printGLString(const char *name, GLenum s) {
	const char *v = (const char *) glGetString(s);
	dbg(0,"GL %s = %s\n", name, v);
}

static void checkGlError(const char* op) {
	for (GLint error = glGetError(); error; error = glGetError()) {
		dbg(0,"after %s() glError (0x%x)\n", op, error);
	}
}

GLuint loadShader(GLenum shaderType, const char* pSource) {
	GLuint shader = glCreateShader(shaderType);
	if (shader) {
		glShaderSource(shader, 1, &pSource, NULL);
		glCompileShader(shader);
		GLint compiled = 0;
		glGetShaderiv(shader, GL_COMPILE_STATUS, &compiled);
		if (!compiled) {
			GLint infoLen = 0;
			glGetShaderiv(shader, GL_INFO_LOG_LENGTH, &infoLen);
			if (infoLen) {
				char* buf = (char*) malloc(infoLen);
				if (buf) {
					glGetShaderInfoLog(shader, infoLen, NULL, buf);
					dbg(0,"Could not compile shader %d:\n%s\n",
						shaderType, buf);
					free(buf);
				}
				glDeleteShader(shader);
				shader = 0;
			}
		}
	}
	return shader;
}


GLuint createProgram(const char* pVertexSource, const char* pFragmentSource) {
	GLuint vertexShader = loadShader(GL_VERTEX_SHADER, pVertexSource);
	if (!vertexShader) {
		return 0;
	}

	GLuint pixelShader = loadShader(GL_FRAGMENT_SHADER, pFragmentSource);
	if (!pixelShader) {
		return 0;
	}

	GLuint program = glCreateProgram();
	if (program) {
		glAttachShader(program, vertexShader);
		checkGlError("glAttachShader");
		glAttachShader(program, pixelShader);
		checkGlError("glAttachShader");
		glLinkProgram(program);
		GLint linkStatus = GL_FALSE;
		glGetProgramiv(program, GL_LINK_STATUS, &linkStatus);
		if (linkStatus != GL_TRUE) {
			GLint bufLength = 0;
			glGetProgramiv(program, GL_INFO_LOG_LENGTH, &bufLength);
			if (bufLength) {
				char* buf = (char*) malloc(bufLength);
				if (buf) {
					glGetProgramInfoLog(program, bufLength, NULL, buf);
					dbg(lvl_info,"Could not link program:\n%s\n", buf);
					free(buf);
				}
			}
			glDeleteProgram(program);
			program = 0;
		}
	}
	return program;
}


void shutdownEGL() {
	eglMakeCurrent(eglDisp, EGL_NO_SURFACE, EGL_NO_SURFACE, EGL_NO_CONTEXT);
	eglDestroyContext(eglDisp, eglCtx);
	eglDestroySurface(eglDisp, eglSurface);
	eglTerminate(eglDisp);

	eglDisp = EGL_NO_DISPLAY;
	eglSurface = EGL_NO_SURFACE;
	eglCtx = EGL_NO_CONTEXT;
}

void
set_color(struct color *color)
{
//	dbg(lvl_error,"set color r = %i, g = %i, b = %i, a = %i\n",color->r,color->g,color->b,color->a);
    GLfloat col[4];
    col[0]=(GLfloat)color->r/65535;
    col[1]=(GLfloat)color->g/65535;
    col[2]=(GLfloat)color->b/65535;
    col[3]=(GLfloat)1.0;
//	dbg(lvl_error,"set color r = %f, g = %f, b = %f, a = %f\n",col[0],col[1],col[2],col[3]);
    glUniform4fv(gvColorHandle, 1,col);
}

/**
 * draws wit z
 *
 * use this if the points were not processed and are still int's
 */
static inline void
draw_array(struct point *p, int count, int z, GLenum mode)
{
    int i;
    GLshort x[count*2]; //CPU does mapcenter so this can be a GLshort

    for (i = 0 ; i < count ; i++) {
        x[i*2]=(GLshort)(p[i].x - mapcenter_x);
        x[i*2+1]=(GLshort)(p[i].y - mapcenter_y);
    }

    glVertexAttribPointer(gvPositionHandle, 2, GL_SHORT, GL_FALSE, 0, x);
    glEnableVertexAttribArray(gvPositionHandle);
    glUniform1i(gvZvalHandle,z);

    glDrawArrays(mode, 0, count);
}

/**
 * Draws polygons with an optional outline.
 *
 */
int drawStencil(struct openGL_hash_entry *entry, struct point *p, int count) {

    int i;
    GList *elements = entry->elements;
    struct element *element_data = elements->data;
    GLshort boundingbox[8];
    boundingbox[0] = boundingbox[2] = boundingbox[4] = boundingbox[6] = (GLshort)p[0].x - mapcenter_x; // topleft, topright, bottomright, bottomleft
    boundingbox[1] = boundingbox[3] = boundingbox[5] = boundingbox[7] = (GLshort)p[0].y - mapcenter_y;
    if (element_data->type == 2) // is it actually a polygon ?
    {
        struct color *color = &(element_data->color);
        set_color(color);
        GLshort x[count * 2];
        glUniform1i(gvZvalHandle,entry->z);

        for (int i = 0 ; i < count ; i++)
        {
            x[i*2]=(GLshort)(p[i].x - mapcenter_x);
            x[i*2+1]=(GLshort)(p[i].y - mapcenter_y);
            if (x[i*2] < boundingbox[0]) // expand leftwards
            {
                boundingbox[0] = boundingbox[6] = x[i*2] ;
            }
            else
            {
                if (x[i*2] > boundingbox[2]) // expand to the right
                {
                    boundingbox[2] = boundingbox[4] = x[i*2];
                }
            }
            if (x[i*2+1] < boundingbox[1]) // expand upwards
            {
                boundingbox[1] = boundingbox[3] = x[i*2+1];
            }
            else
            {
                if (x[i*2+1] > boundingbox[5]) // expand downwards
                {
                    boundingbox[5] = boundingbox[7] = x[i*2+1];
                }
            }
        }

        glClear(GL_STENCIL_BUFFER_BIT);
        glEnable(GL_STENCIL_TEST);
        glColorMask(GL_FALSE, GL_FALSE, GL_FALSE, GL_FALSE);
        glStencilFunc(GL_ALWAYS, 0, 1);
        glStencilOp(GL_KEEP, GL_KEEP, GL_INVERT);
        glStencilMask(1);
        glDisable(GL_DEPTH_TEST); // for some reason stencil does not work with depthtest enabled

        glVertexAttribPointer(gvPositionHandle, 2, GL_SHORT, GL_FALSE, 0, x);
        glEnableVertexAttribArray(gvPositionHandle);

        glDrawArrays(GL_TRIANGLE_FAN, 0, count);
//        checkGlError("glDrawArrays");

        if (elements->next)
        {
            struct element *element_next_data = elements->next->data;
            if (element_next_data->type == 1) // does the polygon have an outline ?
            {
                glLineWidth((GLfloat) 1.0);
                glStencilFunc(GL_ALWAYS, 2, 3);
                glStencilOp(GL_KEEP, GL_KEEP, GL_REPLACE);
                glStencilMask(3);
                // points are still up, can draw the outline right away
                glDrawArrays(GL_LINE_STRIP, 0, count);
                checkGlError("glDrawArrays");
            }
        }

        glEnable(GL_DEPTH_TEST); // reenable depthtest after stencil

        glColorMask(GL_TRUE, GL_TRUE, GL_TRUE, GL_TRUE);
        glStencilFunc(GL_EQUAL, 1, 1);
        glStencilOp(GL_KEEP, GL_KEEP, GL_KEEP);
        glVertexAttribPointer(gvPositionHandle, 2, GL_SHORT, GL_FALSE, 0, boundingbox);
        glEnableVertexAttribArray(gvPositionHandle);
        glDrawArrays(GL_TRIANGLE_FAN, 0, 4);
        if (elements->next)
        {
            struct element *element_next_data = elements->next->data;
            if (element_next_data->type == 1) // paint outline for polygon
            {
                struct color *color = &(element_next_data->color);
                set_color(color);
                glStencilFunc(GL_EQUAL, 2, 2);
                glDrawArrays(GL_TRIANGLE_FAN, 0, 4);
            }
        }
        glDisable(GL_STENCIL_TEST);
        return 0;
    }
}


static void
draw_lines(struct openGL_hash_entry *entry, struct point *p, int count)
{
    GList *elements = entry->elements;
    int z = entry->z;

    while(elements)
    {
        struct element *element_data = elements->data;
//        dbg(0, "draw element_data z  = %f\n", z);

        if (element_data->type == 1)
        {

            struct element_polyline *line = &(element_data->u);
            struct color *color = &(element_data->color);
            set_color(color);
            if (line->width <  5) /////////!!!!!!!
            {  // width zero may crash it ???
                glLineWidth((GLfloat)line->width);
                draw_array(p, count, z, GL_LINE_STRIP);
            }
            else
            {
                // version with GLshort and mapcenter handled by CPU

                GLshort proad[(count - 1) * 8];

                // zou moeten zijn sarten met een halve cirkel in elk punt
                // op het eind nog een toevoegen

                glUniform1i(gvZvalHandle,z);

                int thickness = line->width;
                // check there are at least 2 point !!!!!!!!!!!!!! todo
                count--; // 1 wegsegment minder dan count
                for (int j = 0; j < count; j++)
                {
                    int i = j * 8;
                    double dx = (float) (p[1 + j].x - p[0 + j].x); //delta x
                    double dy = (float) (p[1 + j].y - p[0 + j].y); //delta y
                    double linelength = sqrt(dx * dx + dy * dy);
                    dx /= linelength;
                    dy /= linelength;

                    const int px = (int)((thickness * (-dy)) / 2); //perpendicular vector with length thickness * 0.5
                    const int py = (int)((thickness * dx) / 2);
                    //	dbg(lvl_error, "dx = %lf, dy = %lf, px = %lf, py = %lf\n", dx, dy, px, py);

                    proad[0 + i] = (GLshort)(p[0 + j].x - px - mapcenter_x);
                    proad[1 + i] = (GLshort)(p[0 + j].y - py - mapcenter_y);
                    proad[2 + i] = (GLshort)(p[0 + j].x + px - mapcenter_x);
                    proad[3 + i] = (GLshort)(p[0 + j].y + py - mapcenter_y);
                    proad[4 + i] = (GLshort)(p[1 + j].x - px - mapcenter_x);
                    proad[5 + i] = (GLshort)(p[1 + j].y - py - mapcenter_y);
                    proad[6 + i] = (GLshort)(p[1 + j].x + px - mapcenter_x);
                    proad[7 + i] = (GLshort)(p[1 + j].y + py - mapcenter_y);
                }
                glVertexAttribPointer(gvPositionHandle, 2, GL_SHORT, GL_FALSE, 0, proad);
            				checkGlError("in draw_array glVertexAttribPointer");
                glEnableVertexAttribArray(gvPositionHandle);
            				checkGlError("in draw_array glEnableVertexAttribArray");
                glDrawArrays(GL_TRIANGLE_STRIP, 0, 4 * count);
            				checkGlError("glDrawArrays");
                count ++; // just in case we use coords once more
            }
        }
        elements = elements->next;
        z ++;
    }
}

/**remove the debug_triangles hack some later*/
void draw_elements( struct openGL_hash_entry *entry ,struct point *p, int count, int debug_triangles )
{

    GList *elements = entry->elements;
    struct element *element = elements->data;
//    dbg(0,"elemnt ENTRY type = %i\n", element->type);

        // missing element arrows

    if (element->type == 1) //polyline, can have another one as second element
    {
        draw_lines(entry, p, count);
    }
    else
    {
        if (element->type == 2) //polygon, can have an outline as second element
        {
               drawStencil(entry, p, count);

            // some test to debug water
            // draws the triangles as red lines
            // above the polygon
            if (debug_triangles == TRUE)
            {
                struct color *color = g_alloca(sizeof(struct color));
                color->r = 65000;
                color->b = 0;
                color->g = 0;
                set_color(color);
                glLineWidth(3.0f);
                draw_array(p, count, entry->z +1 , GL_LINES);
            }

        }
    }

#if def0  // dit loopje werkt !!!
    while (elements){ // deze lijkt eindelijk te werken
        dbg(0,"elemnt type = %i\n", element->type);
        elements = elements->next;
        if (elements)
        {
            dbg(0,"WEL next\n");
            element = elements->data;
        } else
        {
            dbg(0,"NO next\n");
        }

    }
#endif
}

void free_element( gpointer key, gpointer value, gpointer userData )
{
    g_free(value);
}


void freehtab()
{
    if (!htab)
    {
        return;
    }
    g_hash_table_foreach (htab, free_element, NULL);
    g_hash_table_destroy(htab);
}

void DrawLowqualMap(JNIEnv* env, jobject thiz, jobject latlonzoom, int width, int height, int font_size, int scale, int sel_range)
{
#ifdef NAVIT_FUNC_CALLS_DEBUG_PRINT
    dbg(0,"+#+:enter\n");
#endif
    // config_get_attr(config, attr_navit, &attr, NULL);
    dbg(lvl_info,"enter\n");
    struct layout  * layout;
    int order;
    const char *s;
    int zoom;
    s = (*env)->GetStringUTFChars(env, latlonzoom, NULL);
    char parse_str[strlen(s) + 1];
    strcpy(parse_str, s);
    (*env)->ReleaseStringUTFChars(env, latlonzoom, s);

    // show map preview for (lat#lon#zoom)
    struct coord_geo g;
    char *p;
    char *stopstring;

    // lat
    p = strtok(parse_str, "#");
    g.lat = strtof(p, &stopstring);
    // lon
    p = strtok(NULL, "#");
    g.lng = strtof(p, &stopstring);
    // zoom
    p = strtok(NULL, "#");
    zoom = atoi(p);

    struct coord c;
    transform_from_geo(projection_mg, &g, &c);

    struct item *item;
    struct map_rect *mr = NULL;
    struct mapset *mapset;
    struct mapset_handle *msh;
    struct map *map = NULL;
    struct attr map_name_attr;
    struct attr attr;

    struct map_selection sel;
    const int selection_range = sel_range;

    const int max = 400; // was 100
    int count;
    struct coord *ca = g_alloca(sizeof(struct coord) * max);
 //   struct point *pa = g_alloca(sizeof(struct point) * max); // not used if GPU does all transformations

    sel.next = NULL;
    sel.order = zoom;
    sel.range.min = type_none;
    sel.range.max = type_last;
    sel.u.c_rect.lu.x = c.x - selection_range;
    sel.u.c_rect.lu.y = c.y + selection_range;
    sel.u.c_rect.rl.x = c.x + selection_range;
    sel.u.c_rect.rl.y = c.y - selection_range;

    struct transformation *tr;
    tr = transform_dup(global_navit->trans);
    struct point p_center;
    p_center.x = width / 2;
    p_center.y = height / 2;
    transform_set_screen_center(tr, &p_center);
    transform_set_center(tr, &c);
    transform_set_scale(tr, scale);
    enum projection pro = transform_get_projection(global_navit->trans_cursor);

    // reset cancel flag
    global_cancel_preview_map_drawing = 0;

    mapset = global_navit->mapsets->data;



    layout = global_navit->layout_current;
    order = transform_get_order(tr);

    // FIXME FIXME !!!!

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

    order_corrected = zoom -1; //?????????????????????

//    dbg(lvl_info,"offX = %i, offY = %i\n",tr->offx, tr->offy);
//    dbg(lvl_info,"Xscale = %f, Yscale = %f\n", tr->xscale,tr->yscale );
//    dbg(lvl_info,"screen center x = %i, y = %i\n", tr->screen_center.x, tr->screen_center.y);
//    dbg(lvl_info,"map center x = %i, y = %i\n", tr->map_center.x, tr->map_center.y);
//    dbg(lvl_info,"scale = %f\n", tr->scale);
//    dbg(lvl_info,"scale_shift = %f\n", tr->scale_shift);
    dbg(0,"layout = %s order = %i delta = %i zoom = %i\n", layout->name, order,layout->order_delta, zoom);
    dbg(0,"order_corrected = %i, shift_order = %i \n", order_corrected, shift_order);

 //   order = order + layout->order_delta; ------------------- !!!!!! ---------------------
 //   ------------------ must probably use order_corrected or so ---------------------
    // see line 3138 in graphics.c

    // FIXME FIXME !!!!

    order = zoom; //?????
//  set map background color
    glClearColor(((float)layout->color.r)/65535,((float)layout->color.g)/65535 , ((float)layout->color.b)/65535, 1.0);
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

    if (!htab || htab_order != order_corrected) // check for changes in layout as well !!!!
                                                // what if a lyaer just gets deactivated by the user ?
    {
        freehtab();
        htab = g_hash_table_new(g_str_hash, g_str_equal);
        htab_order = order_corrected;

        GList *current_layer = layout->layers;
        int z = 10;
        while (current_layer)
        {
            struct layer *layer = current_layer->data;
         //         dbg(0, "layer %s\n", layer->name);
            if (layer->active && (!strstr(layer->name,"POI"))&& (!strstr(layer->name,"labels")))
            {
                //          dbg(0, "layer %s +++ ACTIVE\n", layer->name);
                GList *current_itemgra = layer->itemgras;
                while (current_itemgra)
                {
                    struct itemgra *itemgra = current_itemgra->data;
                    int ordermin = itemgra->order.min;  // zou rechtstreeks kunnen gebruikt worden, was ander probleem
                    int ordermax = itemgra->order.max;
                    //           dbg(0,"itemgra min = %i, max = %i",ordermin , ordermax);

                    GList *elements = itemgra->elements;
                    GList *type = itemgra->type;
                    struct element *element = elements->data;
                    if (ordermin <= order_corrected && ordermax >= order_corrected)
                    {
                        while (type)
                        {
                            //dbg(lvl_error,"item %i\n",element->type); // dit is lijn , polygon en zo
                            struct openGL_hash_entry *entry = g_new0(struct openGL_hash_entry, 1);
                            entry->z = z;
                            entry->elements = elements;
                            entry->defer = FALSE;
                            if (!g_hash_table_lookup(htab, item_to_name(type->data)))
                            {
                                if (strstr(item_to_name(type->data),"building"))
                                {
                                    entry->defer = TRUE;
                                }
                                g_hash_table_insert(htab, item_to_name(
                                           type->data), // anders blaast text er de polygons uit !!
                                                        entry); // nooit NULL toevoegen !!!!
                            }
                            //           dbg(0, "inserted item %s\n",item_to_name(type->data));  //dit is item poly_park enzovoort
                            type = type->next;
                        }
                        z = z + 10; // all types in an itemgra draw with the same Z
                    }
                    current_itemgra = current_itemgra->next;
                }
            }
         //   else we have an inactive layer, do nothing

            current_layer = current_layer->next;
        }
    }

    mapcenter_x = tr->map_center.x;
    mapcenter_y = tr->map_center.y;

    navit_float yawc = navit_cos(-M_PI * tr->yaw / 180);
    navit_float yaws = navit_sin(-M_PI * tr->yaw / 180);

    GLfloat matrix[16];

    matrix[0]=(GLfloat)(yawc)*(2.0/width) / tr->scale;
    matrix[1]=(GLfloat)(yaws)*(2.0/width) / tr->scale;
    matrix[2]=0.0;
    matrix[3]=0.0;
    matrix[4]= matrix[1];
    matrix[5] = -matrix[0];
    matrix[6]=0.0;
    matrix[7]=0.0;
    matrix[8]=0.0;
    matrix[9]=0.0;
    matrix[10]=-0.001; // z multiplier
    matrix[11]=0.0;
    matrix[12]=0.0;
    matrix[13]=0.0;
    matrix[14]=0.0;
    matrix[15]=1.0;

    glUniformMatrix4fv(gvMvprojHandle, 1, GL_FALSE, matrix);

#if def0
    GLfloat matrix[4];

    matrix[0]=(GLfloat)(yawc)*(2.0/width) / tr->scale;
    matrix[1]=(GLfloat)(yaws)*(2.0/width) / tr->scale;
    matrix[2]= matrix[1];
    matrix[3] = -matrix[0];
    glUniformMatrix2fv(gvMvprojHandle, 1, GL_FALSE, matrix);
#endif


    msh = mapset_open(mapset);
    while (msh && (map = mapset_next(msh, 0)))
    {
        if (map_get_attr(map, attr_name, &map_name_attr, NULL))
        {
            if (strncmp("_ms_sdcard_map:", map_name_attr.u.str, 15) == 0)
            {
                if (strncmp("_ms_sdcard_map:/sdcard/zanavi/maps/navitmap", map_name_attr.u.str,
                            38) == 0)
                {
                    // its an sdcard map
                    mr = map_rect_new(map, &sel);
                    if (mr)
                    {
                        while ((item = map_rect_get_item(mr)))
                        {
                            if (global_cancel_preview_map_drawing == 1)
                            {
                                dbg(0, "global_cancel_preview_map_drawing = 1");
                                break;
                            }

                            struct openGL_hash_entry *entry;
                            if (entry = g_hash_table_lookup(htab, item_to_name(item->type)))
                            {
                                count = item_coord_get_within_selection(item, ca, max, &sel);

                                if (count)
                                {
                                    struct attr attr_77;
                                    if (item_attr_get(item, attr_flags, &attr_77))
                                    {
                                        item->flags = attr_77.u.num;
                                    }
                                    else
                                    {
                                        item->flags = 0;
                                    }

                                        //     dbg(0,"draw type %s\n",item_to_name(item->type));
                                    if (entry->defer == FALSE) {
                                        /**remove debug trangles hack some later*/
                                        if (!strstr(item_to_name(item->type),"triang"))
                                        {
                                            draw_elements(entry, ca, count, FALSE);
                                        }
                                        else
                                        {
                                            draw_elements(entry, ca, count, TRUE);
                                        }
                                    }
                                    else
                                    {
                                      //  defer_elements(entry, ca, count);    TODO
                                    }
                                }
                            }
                        }
                        map_rect_destroy(mr);

                    }
                }
            }
        }
    }
    mapset_close(msh);
}




/**
 * Pulls a bitmap and sends it to Java
 *
 *
 * */
void sendBitmap(JNIEnv* env, int width, int height){

    void * pPixels;

    JNIEnv *jnienv2;
//    jnienv2 = jni_getenv(); // probably no need to do this
    jnienv2 = env;

    if (NavitGraphicsClass3 == NULL)
    {
        if (!android_find_class_global("com/zoffcc/applications/zanavi/NavitGraphics", &NavitGraphicsClass3))
        {
            NavitGraphicsClass3 = NULL;
            return;
        }
    }

    if (Preview_set_bitmap == NULL)
    {
        android_find_static_method(NavitGraphicsClass3, "Preview_set_bitmap", "(Landroid/graphics/Bitmap;)V", &Preview_set_bitmap);
    }

    if (Preview_set_bitmap == NULL)
    {
        dbg(lvl_error, "no method found for Preview_set_bitmap \n");
        return;
    }

    jclass bitmapConfig = (*jnienv2)->FindClass(jnienv2,"android/graphics/Bitmap$Config");
    jfieldID argb8888FieldID = (*jnienv2)->GetStaticFieldID(jnienv2,bitmapConfig, "ARGB_8888",
                                                          "Landroid/graphics/Bitmap$Config;");
    jobject argb8888Obj = (*jnienv2)->GetStaticObjectField(jnienv2,bitmapConfig, argb8888FieldID);

    jclass bitmapClass = (*jnienv2)->FindClass(jnienv2,"android/graphics/Bitmap");
    jmethodID createBitmapMethodID = (*jnienv2)->GetStaticMethodID(jnienv2,bitmapClass,"createBitmap",
                                                                   "(IILandroid/graphics/Bitmap$Config;)Landroid/graphics/Bitmap;");

    jobject bitmapObj = (*jnienv2)->CallStaticObjectMethod(jnienv2,bitmapClass, createBitmapMethodID,
                                                           width, height, argb8888Obj);
    AndroidBitmap_lockPixels(jnienv2, bitmapObj, &pPixels);
    glReadPixels(0, 0, width, height, GL_RGBA, GL_UNSIGNED_BYTE, pPixels);
    AndroidBitmap_unlockPixels(jnienv2, bitmapObj);
    (*jnienv2)->CallStaticObjectMethod(jnienv2,NavitGraphicsClass3, Preview_set_bitmap, bitmapObj);

}

/**
 * An openGL version of the lowqual map
 */
JNIEXPORT void JNICALL
Java_com_zoffcc_applications_zanavi_NavitGraphics_DrawLowqualMap(JNIEnv* env, jobject thiz, jobject latlonzoom, int width, int height, int font_size, int scale, int sel_range) {

    GLuint gProgram;
    GLfloat lineWidthRange[2];

    const EGLint surfaceAttr[] =
            {
                    EGL_WIDTH, width,
                    EGL_HEIGHT, height,
                    EGL_NONE
            };

    EGLint eglMajVers, eglMinVers;
    EGLint numConfigs;

    if (!eglSurface) { // maybe some more tests

        eglDisp = eglGetDisplay(EGL_DEFAULT_DISPLAY);
        eglInitialize(eglDisp, &eglMajVers, &eglMinVers);

        // choose the first config, i.e. best config
        eglChooseConfig(eglDisp, confAttr, &eglConf, 1, &numConfigs);

        eglCtx = eglCreateContext(eglDisp, eglConf, EGL_NO_CONTEXT, ctxAttr);

        // create a pixelbuffer surface
        eglSurface = eglCreatePbufferSurface(eglDisp, eglConf, surfaceAttr);

        eglMakeCurrent(eglDisp, eglSurface, eglSurface, eglCtx);

        // end setupEGL

        // setup openGL

        printGLString("Version", GL_VERSION);
        printGLString("Vendor", GL_VENDOR);
        printGLString("Renderer", GL_RENDERER);
        printGLString("Extensions", GL_EXTENSIONS);

        dbg(lvl_info, "setupGraphics(%d, %d)", width, height);
        gProgram = createProgram(gVertexShader, gFragmentShader);
        if (!gProgram) {
            dbg(lvl_error, "Could not create program.");
            return;
        }

        glUseProgram(gProgram);
        checkGlError("glUseProgram");

        glEnable(GL_DEPTH_TEST); // needed if drawing with z

        gvPositionHandle = glGetAttribLocation(gProgram, "vPosition");
        checkGlError("glGetAttribLocation vPosition");

        gvMvprojHandle = glGetUniformLocation(gProgram, "mvproj");
        checkGlError("glGetUniformLocation mvproj");

        gvColorHandle = glGetUniformLocation(gProgram, "color");
        checkGlError("glGetUniformLocation color");

        gvMapcenterHandle = glGetUniformLocation(gProgram, "mapcenter");
        checkGlError("glGetUniformLocation mapcenter");

        gvZvalHandle = glGetUniformLocation(gProgram, "zval");
        checkGlError("glGetUniformLocation");

        glViewport(0, 0, width, height);
        checkGlError("glViewport");

        glGetFloatv(GL_ALIASED_LINE_WIDTH_RANGE, lineWidthRange);
        checkGlError("glGetFloatv linewidthrange");
        glMaxLineWidth = lineWidthRange[1];
    }
    // no real need for this matrix anymore
    // at each request a custom one is made

//    for (int i = 0 ; i < 16 ; i++){
//        matrix[i]=0.0;
//    }

//      this one is to render to the screen
//    matrix[0]=2.0/width;
//    matrix[5]=-2.0/height;
//    matrix[10]=1;
//    matrix[12]=-1;
//    matrix[13]=1;
//    matrix[15]=1;

    // this one is to render with a bitmap as final target
    // but transformation handled by navit
//    matrix[0]=2.0/width;
//    matrix[5]=2.0/height;
//    matrix[10]=1;
//    matrix[12]=-1; //
//    matrix[13]=-1; //
//    matrix[15]=1;

// not rotated and needs scaling
//    matrix[0]=1;
//    matrix[5]=-1;
//    matrix[10]=1;
//    matrix[12]=0; //
//    matrix[13]=0; //
//    matrix[15]=1;

//    glUniformMatrix4fv(gvMvprojHandle, 1, GL_FALSE, matrix);


// setup openGL END

// The actual drawing
    DrawLowqualMap(env, thiz, latlonzoom, width, height, font_size, scale, sel_range);

    // send the bitmap to Java
    sendBitmap(env, width, height);

	// each time or never ????
//	shutdownEGL();
}
