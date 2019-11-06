/** jandegr
 * Some openGL test
 *
 * no oversampling (smooth lines) as long as it uses a pbuffer
 * several things to fix
 *
 * How to get the order the same as in the full mapview ?
 * tried order_corrected and other stuff FIXME FIXME FIXME
 *
 * plenty of stuff missing, poi's , thick dashed lines , ....
 *
 * for now works as a drop-in replacement for the lowqual preview map
 *
 * TODO : make it use Zanavi coding style
 *
 * WIP WIP WIP
 */




// apparently the order of the includes is important
// several unneeded

#include <EGL/egl.h>
#include <GLES2/gl2.h>


#include <stdlib.h>
#include <glib.h>
#include <stdio.h>
#include <math.h>
//#include <zlib.h>
//#include "config.h"
#include "debug.h"
//#include "string.h"
//#include "draw_info.h"
#include "point.h"
#include "graphics.h"
//#include "projection.h"
//#include "item.h"
#include "map.h"
//#include "coord.h"
#include "transform.h"
#include "plugin.h"
//#include "profile.h"
#include "mapset.h"
#include "layout.h"
#include "route.h"
//#include "util.h"
#include "callback.h"
#include "file.h"
//#include "event.h"
//
//#include "attr.h"
// ??????????????? track ?
#include "track.h"
#include "navit.h"
//#include "route.h"

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


GLuint gvPositionHandle;
GLint gvMvprojHandle;
GLint gvColorHandle;
//GLint gvMapcenterHandle;
GLint gvScaleXHandle;
GLint gvZvalHandle;
GLint gvUseDashesHandle;
GLint gvSourcePointHandle;
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


#if def0 // dashes but in world coords, resizes dashes with zoom
const char gVertexShader [] =
        "attribute vec2 vPosition;\n"
                "attribute vec2        texture_position; \n"
                "uniform mat4        mvproj;\n"
                "uniform mat2        invMvproj;\n"
                "uniform bool use_dashes;\n"
                "varying float v_use_dashes = 0.0;\n"
                "varying vec2 v_texture_position;\n"
                "varying vec2 position;\n"
                "uniform vec4  color;\n"
                "uniform int  zval;\n"
                "void main() {\n"
                "  v_texture_position=texture_position;\n"
                "  gl_Position = mvproj * vec4((vPosition.x), (vPosition.y) , zval, 1);\n"
                "  if (use_dashes){\n"
                "      v_use_dashes = 1.0;\n"
                "      position = invMvproj * vec2(gl_Position.x, gl_Position.y) ;\n"
                "  }\n"
                "}\n";

// met invMvproj moet er terug naar schermcoordinaten gerekend worde
// van de actuele positie
// bewerking op of met gl_position kan beter in de fragmentshader gebeuren,
// dat is daar ook beschikbaar !!!!
// of met gl_FragCoord werken ???
// http://www.shaderific.com/glsl-variables
//"precision mediump float;\n"

const char gFragmentShader [] =
        "precision highp float;\n"
                "uniform vec4  color;\n"
                "uniform sampler2D texture;\n"
                "uniform bool use_texture;\n"
                "varying float v_use_dashes;\n"
                "uniform vec2 sourcePoint;\n"
                "varying vec2 position;\n"
                "varying vec2 v_texture_position;\n"
                "void main() {\n"
                "   if (use_texture) {\n"
                "     gl_FragColor = texture2D(texture, v_texture_position);\n"
                "   }else if (v_use_dashes > 0.0){\n"
                "      if (cos(0.85 * abs(distance(sourcePoint.xy, position.xy))) - 0.3 > 0.0)\n"
                "         {\n"
                "             discard;\n"
                "         } else \n"
                "         {\n"
                "             gl_FragColor = color;\n"
                "         }\n"
                "   }else{\n"
                "     gl_FragColor = color;\n"
                "   }\n"
                "}\n";


const char gFragmentShader_NOdash [] =
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
#endif

const char gVertexShader [] =
        "attribute vec2 vPosition;\n" // zo een vec4 maken waarbij 3 en 4 first point zijn en dan niet met line strip tekenen !!
                "attribute vec2        texture_position; \n"
                "uniform mat4        mvproj;\n"
                "uniform bool use_dashes;\n"
                "uniform vec2 sourcePoint;\n"
                "uniform float scaleX;"
                "uniform vec4  color;\n"
                "uniform int  zval;\n"
                "varying vec2 v_texture_position;\n"
                "varying float vDistanceFromSource;\n"
                "void main() {\n"
                "  v_texture_position=texture_position;\n"
                "  gl_Position = mvproj * vec4((vPosition.x), (vPosition.y) , zval, 1);\n"
                "  if (use_dashes){\n"
                "      vec4 position = mvproj * vec4(sourcePoint.x, sourcePoint.y, zval, 1) ;\n"
                "      vDistanceFromSource = distance(position.xy, gl_Position.xy) * scaleX;\n"
                "  }\n"
                "}\n";

const char gFragmentShader [] =
        "precision highp float;\n"
                "uniform vec4  color;\n"
                "uniform sampler2D texture;\n"
                "uniform bool use_texture;\n"
                "uniform bool use_dashes;\n"
                "varying float vDistanceFromSource;\n"
                "varying vec2 v_texture_position;\n"
                "void main() {\n"
                "   if (use_texture) {\n"
                "     gl_FragColor = texture2D(texture, v_texture_position);\n"
                "   }else if (use_dashes){\n"
                "      if (cos(0.65 * abs(vDistanceFromSource)) - 0.3 > 0.0)\n"
                "         {\n"
                "             discard;\n"
                "         } else \n"
                "         {\n"
                "             gl_FragColor = color;\n"
                "         }\n"
                "   }else{\n"
                "     gl_FragColor = color;\n"
                "   }\n"
                "}\n";


static void printGLString(const char *name, GLenum s) {
	const char *v = (const char *) glGetString(s);
	dbg(0,"GL %s = %s\n", name, v);
}

static inline void checkGlError(const char* op) {

//	for (GLint error = glGetError(); error; error = glGetError()) {
//		dbg(0,"after %s() glError (0x%x)\n", op, error);
//	}

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

/**
 * Sets the transformation matrices for the shader(s).
 *
 * @param width of the bitmap
 * @param tr the current transformation of the screen
 */
void set_matrices(int width, const struct transformation *tr)
{
    GLfloat yawcos = (GLfloat)cos(-M_PI * tr->yaw / 180);
    GLfloat yawsin = (GLfloat)sin(-M_PI * tr->yaw / 180);

    GLfloat matrix[16];

    matrix[0]= (GLfloat)(yawcos *(2.0/width) / tr->scale);
    matrix[1]= (GLfloat)(yawsin *(2.0/width) / tr->scale);
    matrix[2]=0.0;
    matrix[3]=0.0;
    matrix[4]= matrix[1];
    matrix[5] = -matrix[0];
    matrix[6]=0.0;
    matrix[7]=0.0;
    matrix[8]=0.0;
    matrix[9]=0.0;
    matrix[10]=-0.001f; // z multiplier
    matrix[11]=0.0;
    matrix[12]=0.0;
    matrix[13]=0.0;
    matrix[14]=0.0;
    matrix[15]=1.0;

    glUniformMatrix4fv(gvMvprojHandle, 1, GL_FALSE, matrix);
}


/**
 * Sets the openGL color uniform
 * for the shaderprograms.
 *
 * @param color
 */
static inline void
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
 * draws any primitive with z
 *
 * use this if the points were not processed and
 * still have int coords
 *
 * @param p first of the points
 * @param count the number of points
 * @param z
 * @param mode the openGL draw mode
 */
static inline void
draw_array(struct point *p, int count, int z, GLenum mode)
{
    int i;
    GLshort pcoord[count*2]; //CPU does mapcenter so this can be a GLshort

    for (i = 0 ; i < count ; i++) {
        pcoord[i*2]=(GLshort)(p[i].x - mapcenter_x);
        pcoord[i*2+1]=(GLshort)(p[i].y - mapcenter_y);
    }

    glVertexAttribPointer(gvPositionHandle, 2, GL_SHORT, GL_FALSE, 0, pcoord);
    //glEnableVertexAttribArray(gvPositionHandle);
    glUniform1i(gvZvalHandle,z);

    glDrawArrays(mode, 0, count);
}

/**
 * Draws the polygon elements of a hash entry with an optional outline.
 *
 * @param entry the hash entry derived from an itemgra
 * @param p first of the points
 * @param count the number of points
 * @return nothing
 */
int drawStencil(struct openGL_hash_entry *entry, struct point *p, int count) {

    int i;
    GList *elements = entry->elements;
    struct element *element_data = elements->data;
    GLshort boundingbox[8];
    boundingbox[0] = boundingbox[2] = boundingbox[4] = boundingbox[6] = (GLshort)(p[0].x - mapcenter_x); // topleft, topright, bottomright, bottomleft
    boundingbox[1] = boundingbox[3] = boundingbox[5] = boundingbox[7] = (GLshort)(p[0].y - mapcenter_y);
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
        //glEnableVertexAttribArray(gvPositionHandle);

        glDrawArrays(GL_TRIANGLE_FAN, 0, count);
        checkGlError("glDrawArrays");

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
        //glEnableVertexAttribArray(gvPositionHandle);
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

static inline void
setdashes(int enable)
{
    if (enable == TRUE)
    {
        glUniform1i(gvUseDashesHandle,TRUE);
    }
    else
    {
        glUniform1i(gvUseDashesHandle,FALSE);
    }


}

/**
 * Draws the line elements of a hash entry
 * version with GLshort and mapcenter handled by CPU
 *
 * @param entry the hash entry derived from an itemgra
 * @param p first of the points
 * @param count the number of points
 */
static void
draw_lines(struct openGL_hash_entry *entry, struct point *p, int count)
{
    GList *elements = entry->elements;
    int z = entry->z;

    while(elements)
    {
        struct element *element_data = elements->data;
//        dbg(0, "draw element_data z  = %f\n", z);

        if (element_data->type == 1 && count > 1)
        {
            struct element_polyline *line = &(element_data->u);
            struct color *color = &(element_data->color);
            set_color(color);
            if (line->width <  1)
            {
                line->width = 1;
            }
            if (line->width <  5) /////////!!!!!!!
            {   // let GPU draw them as lines
                glLineWidth((GLfloat)line->width);
                glUniform1i(gvZvalHandle,z);
                if (line->dash_num)
                {
                    setdashes(TRUE);

                    for(int i=0; i<(count-1); i++)
                    {
                        GLfloat segcoord[4];
                        segcoord[0] = (GLfloat)(p[0 + i].x - mapcenter_x);
                        segcoord[1] = (GLfloat)(p[0 + i].y - mapcenter_y);
                        segcoord[2] = (GLfloat)(p[1 + i].x - mapcenter_x);
                        segcoord[3] = (GLfloat)(p[1 + i].y - mapcenter_y);

                        GLfloat sourcePoint[2];
                        sourcePoint[0] = segcoord[0];
                        sourcePoint[1] = segcoord[1];

                        // glUniform2iv gives errors
                        glUniform2fv(gvSourcePointHandle,1, segcoord);
                        checkGlError("glUniform2fv sourcePoint");

                        glVertexAttribPointer(gvPositionHandle, 2, GL_FLOAT, GL_FALSE, 0, segcoord);
                        checkGlError("in draw_array glVertexAttribPointer");

                        //glEnableVertexAttribArray(gvPositionHandle);
                        //checkGlError("in draw_array glEnableVertexAttribArray");

                        glDrawArrays(GL_LINES, 0, 2);
                        checkGlError("glDrawArrays");
                    }
                    setdashes(FALSE);
                }
                else
                {
                    draw_array(p, count, z, GL_LINE_STRIP);
                }
            }
            else
            {   // construct boxes and an end and startcap
                count--; // 1 roadsegment less than number of points
                GLshort proad[(count * 8) + 4]; // 4 points per segment + one extra for start and end

                glUniform1i(gvZvalHandle,z);

                int thickness = line->width;

                // keep track of the number of coords produced
                int ccounter = 0;
                for (int j = 0; j < count; j++)
                {
                    double dx = (p[1 + j].x - p[0 + j].x); //delta x
                    double dy = (p[1 + j].y - p[0 + j].y); //delta y
                    double linelength = sqrt(dx * dx + dy * dy);

                    // add start cap
                    // TODO a few more points for thick lines
                    if (ccounter == 0)
                    {
                        int firstx = p[0 + j].x - (int)(dx * thickness/ ( 2 * linelength )) - mapcenter_x;
                        int firsty = p[0 + j].y - (int)(dy  * thickness/ (2 * linelength)) - mapcenter_y;
                        proad[ccounter] = (GLshort) firstx;
                        ccounter++;
                        proad[ccounter] = (GLshort) firsty;
                        ccounter++;
                    }

                    int lastx = 0;
                    int lasty = 0;
                    if (j == (count-1)) // remember these for the end cap
                    {
                        lastx = p[1 + j].x + (int) (dx * thickness / (2 * linelength))- mapcenter_x;
                        lasty = p[1 + j].y + (int) (dy * thickness / (2 * linelength))- mapcenter_y;
                    }

                    dx = dx / linelength;
                    dy = dy / linelength;
                    //perpendicular vector with length thickness * 0.5
                    const int px = (int)((thickness * (-dy)) / 2);
                    const int py = (int)((thickness * dx) / 2 );
                    //	dbg(lvl_error, "dx = %lf, dy = %lf, px = %lf, py = %lf\n", dx, dy, px, py);

                    proad[ccounter] = (GLshort)(p[0 + j].x - px - mapcenter_x);
                    ccounter ++;
                    proad[ccounter] = (GLshort)(p[0 + j].y - py - mapcenter_y);
                    ccounter ++;
                    proad[ccounter] = (GLshort)(p[0 + j].x + px - mapcenter_x);
                    ccounter ++;
                    proad[ccounter] = (GLshort)(p[0 + j].y + py - mapcenter_y);
                    ccounter ++;
                    proad[ccounter] = (GLshort)(p[1 + j].x - px - mapcenter_x);
                    ccounter ++;
                    proad[ccounter] = (GLshort)(p[1 + j].y - py - mapcenter_y);
                    ccounter ++;
                    proad[ccounter] = (GLshort)(p[1 + j].x + px - mapcenter_x);
                    ccounter ++;
                    proad[ccounter] = (GLshort)(p[1 + j].y + py - mapcenter_y);
                    ccounter ++;

                    // add end cap
                    if (j == (count-1))
                    {
                        proad[ccounter] = (GLshort) lastx;
                        ccounter++;
                        proad[ccounter] = (GLshort) lasty;
                        ccounter++;
                    }
                }
                glVertexAttribPointer(gvPositionHandle, 2, GL_SHORT, GL_FALSE, 0, proad);
                checkGlError("in draw_array glVertexAttribPointer");

                //glEnableVertexAttribArray(gvPositionHandle);
                //checkGlError("in draw_array glEnableVertexAttribArray");

                glDrawArrays(GL_TRIANGLE_STRIP, 0, (ccounter/2)); // 4 points for a segment + startpoint
                checkGlError("glDrawArrays");
#if def0
                // debug
                if (!elements->next)
                {
                    glUniform1i(gvZvalHandle, z + 1);
                    struct color *color2 = g_alloca(sizeof(struct color));
                    color2->r = 65000;
                    color2->b = 0;
                    color2->g = 0;
                    set_color(color2);
                    glLineWidth(1.0f);
                    glDrawArrays(GL_LINE_STRIP, 0, ccounter/2); // 4 points for a segment + startpoint
                    checkGlError("glDrawArrays");
                }
                // end debug
#endif
                count ++; // restore count to the number of points
            }
        }
        elements = elements->next;
        z ++;
    }
}


/**
 * Draws the elements of a hash entry for the given points.
 *
 * @param entry the hash entry derived from an itemgra
 * @param p first of the points
 * @param count the number of points
 * @param triangles TRUE or FALSE
 * @return nothing
 *
 */
void draw_elements( struct openGL_hash_entry *entry ,struct point *points, int count, int triangles )
{

    GList *elements = entry->elements;
    struct element *element = elements->data;
    // dbg(0,"elemnt ENTRY type = %i\n", element->type);

    // missing element arrows, text, circle and so on

    if (element->type == 1) //polyline, can have another one as second element
    {
        draw_lines(entry, points, count);
    }
    else
    {
        if (element->type == 2) //polygon, can have an outline as second element
        {
            if (triangles == TRUE) // no need to stecil pretriangulated polygons
            {                      // and those seem to have lost their outline as well
                struct color *color = &(element->color);
                set_color(color);
                draw_array(points, count, entry->z, GL_TRIANGLE_STRIP);

#if def0
                // some test for triangulated polygons
                // draws the triangles as red lines
                // above the polygon
                struct color *color2 = g_alloca(sizeof(struct color));
                color2->r = 65000;
                color2->b = 0;
                color2->g = 0;
                set_color(color2);
                glLineWidth(3.0f);
                draw_array(p, count, entry->z +1 , GL_LINES);
#endif
            }
            else // not yet triangulated so use stencil and draw optional outline
            {
                drawStencil(entry, points, count);
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
    if (htab)
    {
        g_hash_table_foreach(htab, free_element, NULL);
        g_hash_table_destroy(htab);
    }
}

/**
 * Fills the hash (if needed) with the layout
 * elements first to appear based on the order.
 *
 * @param layout
 * @param order
 */
void fillhash(const struct layout *layout, int order)
{
    if (!htab || htab_order != order) // check for changes in layout as well !!!!
        // what if a layer just gets deactivated by the user ?
    {
        freehtab();
        htab = g_hash_table_new(g_str_hash, g_str_equal);
        htab_order = order;

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
                    //dbg(0,"itemgra min = %i, max = %i",ordermin , ordermax);

                    GList *elements = itemgra->elements;
                    GList *type = itemgra->type;
                    struct element *element = elements->data;
                    if (ordermin <= order && ordermax >= order)
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
}


void DrawLowqualMap(JNIEnv* env, jobject thiz, jobject latlonzoom, int width, int height, int font_size, int scale, int sel_range)
{
#ifdef NAVIT_FUNC_CALLS_DEBUG_PRINT
    dbg(0,"+#+:enter\n");
#endif
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
    struct coord_geo geocoord;
    char *part;
    char *stopstring;

    // lat
    part = strtok(parse_str, "#");
    geocoord.lat = strtof(part, &stopstring);
    // lon
    part = strtok(NULL, "#");
    geocoord.lng = strtof(part, &stopstring);
    // zoom
    part = strtok(NULL, "#");
    zoom = atoi(part);

    struct coord c;
    transform_from_geo(projection_mg, &geocoord, &c);

    struct item *item;
    struct map_rect *mr = NULL;
    struct mapset *mapset;
    struct mapset_handle *msh;
    struct map *map = NULL;
    struct attr map_name_attr;
    struct attr attr;

    struct map_selection sel;
    const int selection_range = sel_range;

    const int max = 1000;   // was 100, 600 fixes flooding fo river Dender near Ninove
                            // 1000 fixes its flooding south of Lessines
    int count;
    struct point *p = g_alloca(sizeof(struct point) * max); // some anomaly in navit, struct coord and struct point
                                                            // are the same. struct coord has an x and an y coord,
                                                            // together they make a point and not a coord

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
//    dbg(0,"layout = %s order = %i delta = %i zoom = %i\n", layout->name, order,layout->order_delta, zoom);
//    dbg(0,"order_corrected = %i, shift_order = %i \n", order_corrected, shift_order);

 //   order = order + layout->order_delta; ------------------- !!!!!! ---------------------
 //   ------------------ must probably use order_corrected or so ---------------------
    // see line 3138 in graphics.c

    // FIXME FIXME !!!!

    order = zoom; //?????

    // set map background color
    glClearColor(((float)layout->color.r)/65535,((float)layout->color.g)/65535 , ((float)layout->color.b)/65535, 1.0);
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

    // check the hash and (re)fill if needed
    fillhash(layout, order_corrected);

    mapcenter_x = tr->map_center.x;
    mapcenter_y = tr->map_center.y;

    set_matrices(width, tr);

    // inform the shader of the scale, used for dashes and such
    glUniform1f(gvScaleXHandle, (GLfloat)(width/2.0));

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
                             // NOT DURING TESTING
                             //   dbg(0, "global_cancel_preview_map_drawing = 1");
                             //   break;
                            }

                            struct openGL_hash_entry *entry;
                            entry = g_hash_table_lookup(htab, item_to_name(item->type));
                            if (entry)
                            {
                                count = item_coord_get_within_selection(item, (struct coord*)p, max, &sel);

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

                                    //dbg(0,"draw type %s\n",item_to_name(item->type));
                                    if (entry->defer == FALSE)
                                    {
                                        if (!strstr(item_to_name(item->type),"triang"))
                                        {
                                            // draw non-triangulated elements
                                            draw_elements(entry, p, count, FALSE);
                                        }
                                        else
                                        {
                                            // draw triangulated elements
                                            draw_elements(entry, p, count, TRUE);
                                        }
                                    }
                                    else
                                    {
                                      //  defer_elements(entry, (struct coord*)p, count);    TODO
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
        dbg(0, "no method found for Preview_set_bitmap \n");
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
    (*jnienv2)->CallStaticVoidMethod(jnienv2,NavitGraphicsClass3, Preview_set_bitmap, bitmapObj);

}

/**
 * An openGL version of the lowqual map
 */
JNIEXPORT void JNICALL
Java_com_zoffcc_applications_zanavi_NavitGraphics_DrawLowqualMap(JNIEnv* env, jclass thiz, jstring latlonzoom,
        jint width, jint height, jint font_size, jint scale, jint sel_range)
{

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

        // gives GLint
        gvPositionHandle = glGetAttribLocation(gProgram, "vPosition");
        checkGlError("glGetAttribLocation vPosition");
        // but this wants GLunit
        glEnableVertexAttribArray(gvPositionHandle); //always used, enable as default


        gvMvprojHandle = glGetUniformLocation(gProgram, "mvproj");
        checkGlError("glGetUniformLocation mvproj");

//        gvInvMvprojHandle = glGetUniformLocation(gProgram, "invMvproj");
//        checkGlError("glGetUniformLocation mvproj");

        gvColorHandle = glGetUniformLocation(gProgram, "color");
        checkGlError("glGetUniformLocation color");

//        gvMapcenterHandle = glGetUniformLocation(gProgram, "mapcenter");
//        checkGlError("glGetUniformLocation mapcenter");


        gvScaleXHandle = glGetUniformLocation(gProgram, "scaleX");
        checkGlError("glGetUniformLocation scaleX");

        gvZvalHandle = glGetUniformLocation(gProgram, "zval");
        checkGlError("glGetUniformLocation zval");

        gvUseDashesHandle = glGetUniformLocation(gProgram, "use_dashes");
        checkGlError("glGetUniformLocation use_dashes");

        gvSourcePointHandle = glGetUniformLocation(gProgram, "sourcePoint");
        checkGlError("glGetUniformLocation sourcePoint");

        glViewport(0, 0, width, height);
        checkGlError("glViewport");

        glGetFloatv(GL_ALIASED_LINE_WIDTH_RANGE, lineWidthRange);
        checkGlError("glGetFloatv linewidthrange");
        glMaxLineWidth = lineWidthRange[1];
    }

// setup openGL END

// The actual drawing
    DrawLowqualMap(env, thiz, latlonzoom, width, height, font_size, scale, sel_range);

    // send the bitmap to Java
    sendBitmap(env, width, height);

	// each time or never ????
//	shutdownEGL();
}
