#ifdef HAVE_API_ANDROID

#include <jni.h>
#include <android/bitmap.h>
extern JNIEnv *jnienv;
extern JavaVM *cachedJVM;
extern jobject *android_activity;
extern jobject *main_map_bitmap;
extern AndroidBitmapInfo *main_map_bitmapinfo;
extern struct callback_list *android_activity_cbl;
extern int android_version;

struct jni_object
{
	JNIEnv* env;
	jobject jo;
	jmethodID jm;
};

#else

typedef int jobject;
typedef int jmethodID;

struct jni_object
{
	int dummy;
};

#endif

AndroidBitmapInfo main_map_bitmapinfo2;

int android_find_class_global(char *name, jclass *ret);
int android_find_method(jclass class, char *name, char *args, jmethodID *ret);
int android_find_static_method(jclass class, char *name, char *args, jmethodID *ret);
void send_osd_values(char *id, char *text1, char *text2, char *text3, int i1, int i2, int i3, int i4, float f1, float f2, float f3);
void set_vehicle_values_to_java(int x, int y, int angle, int speed);
void set_vehicle_values_to_java_delta(int dx, int dy, int dangle, int dzoom, int l_old, int l_new);
void android_send_generic_text(int id, char *text);
void android_return_generic_int(int id, int i);
void send_osd_values(char *id, char *text1, char *text2, char *text3, int i1, int i2, int i3, int i4, float f1, float f2, float f3);
void send_alert_to_java(int id, const char *text);
void send_route_rect_to_java(int x1, int y1, int x2, int y2, int order);
void android_return_search_result(struct jni_object *jni_o, char *str);

JNIEnv* jni_getenv();


