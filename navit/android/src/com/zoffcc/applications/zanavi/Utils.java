package com.zoffcc.applications.zanavi;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;

class Utils
{
    private static final String TAG ="Utils";

    static void copyFile(File sourceFile, File destFile) throws IOException
    {
        if (!sourceFile.exists())
        {
            return;
        }
        if (!destFile.exists())
        {
            destFile.createNewFile();
        }
        FileChannel source;
        FileChannel destination;
        source = new FileInputStream(sourceFile).getChannel();
        destination = new FileOutputStream(destFile).getChannel();
        if (destination != null && source != null)
        {
            destination.transferFrom(source, 0, source.size());
        }
        if (source != null)
        {
            source.close();
        }
        if (destination != null)
        {
            destination.close();
        }

    }

    static String intent_flags_to_string(int flags)
    {
        String ret = "(" + String.format("%#x", flags) + ") ";

        // Intent flags
        final int FLAG_GRANT_READ_URI_PERMISSION = 0x00000001;
        final int FLAG_GRANT_WRITE_URI_PERMISSION = 0x00000002;
        final int FLAG_FROM_BACKGROUND = 0x00000004;
        final int FLAG_DEBUG_LOG_RESOLUTION = 0x00000008;
        final int FLAG_EXCLUDE_STOPPED_PACKAGES = 0x00000010;
        final int FLAG_INCLUDE_STOPPED_PACKAGES = 0x00000020;
        final int FLAG_ACTIVITY_NO_HISTORY = 0x40000000;
        final int FLAG_ACTIVITY_SINGLE_TOP = 0x20000000;
        final int FLAG_ACTIVITY_NEW_TASK = 0x10000000;
        final int FLAG_ACTIVITY_MULTIPLE_TASK = 0x08000000;
        final int FLAG_ACTIVITY_CLEAR_TOP = 0x04000000;
        final int FLAG_ACTIVITY_FORWARD_RESULT = 0x02000000;
        final int FLAG_ACTIVITY_PREVIOUS_IS_TOP = 0x01000000;
        final int FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS = 0x00800000;
        final int FLAG_ACTIVITY_BROUGHT_TO_FRONT = 0x00400000;
        final int FLAG_ACTIVITY_RESET_TASK_IF_NEEDED = 0x00200000;
        final int FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY = 0x00100000;
        final int FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET = 0x00080000;
        final int FLAG_ACTIVITY_NO_USER_ACTION = 0x00040000;
        final int FLAG_ACTIVITY_REORDER_TO_FRONT = 0X00020000;
        final int FLAG_ACTIVITY_NO_ANIMATION = 0X00010000;
        final int FLAG_ACTIVITY_CLEAR_TASK = 0X00008000;
        final int FLAG_ACTIVITY_TASK_ON_HOME = 0X00004000;
        /*
         * final int FLAG_RECEIVER_REGISTERED_ONLY = 0x40000000;
         * final int FLAG_RECEIVER_REPLACE_PENDING = 0x20000000;
         * final int FLAG_RECEIVER_FOREGROUND = 0x10000000;
         * final int FLAG_RECEIVER_REGISTERED_ONLY_BEFORE_BOOT = 0x08000000;
         * final int FLAG_RECEIVER_BOOT_UPGRADE = 0x04000000;
         */

        int first = 1;
        String sep;

        if ((flags & FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS) == FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
        {
            first = 0;
            sep = "";
            ret = ret + sep + "FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS";
        }
        if ((flags & FLAG_ACTIVITY_BROUGHT_TO_FRONT) == FLAG_ACTIVITY_BROUGHT_TO_FRONT)
        {
            if (first == 1)
            {
                first = 0;
                sep = "";
            }
            else
            {
                sep = ",";
            }
            ret = ret + sep + "FLAG_ACTIVITY_BROUGHT_TO_FRONT";
        }
        if ((flags & FLAG_ACTIVITY_RESET_TASK_IF_NEEDED) == FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
        {
            if (first == 1)
            {
                first = 0;
                sep = "";
            }
            else
            {
                sep = ",";
            }
            ret = ret + sep + "FLAG_ACTIVITY_RESET_TASK_IF_NEEDED";
        }
        if ((flags & FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) == FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY)
        {
            if (first == 1)
            {
                first = 0;
                sep = "";
            }
            else
            {
                sep = ",";
            }
            ret = ret + sep + "FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY";
        }
        if ((flags & FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET) == FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET)
        {
            if (first == 1)
            {
                first = 0;
                sep = "";
            }
            else
            {
                sep = ",";
            }
            ret = ret + sep + "FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET";
        }
        if ((flags & FLAG_ACTIVITY_NO_USER_ACTION) == FLAG_ACTIVITY_NO_USER_ACTION)
        {
            if (first == 1)
            {
                first = 0;
                sep = "";
            }
            else
            {
                sep = ",";
            }
            ret = ret + sep + "FLAG_ACTIVITY_NO_USER_ACTION";
        }
        if ((flags & FLAG_ACTIVITY_REORDER_TO_FRONT) == FLAG_ACTIVITY_REORDER_TO_FRONT)
        {
            if (first == 1)
            {
                first = 0;
                sep = "";
            }
            else
            {
                sep = ",";
            }
            ret = ret + sep + "FLAG_ACTIVITY_REORDER_TO_FRONT";
        }
        if ((flags & FLAG_ACTIVITY_NO_ANIMATION) == FLAG_ACTIVITY_NO_ANIMATION)
        {
            if (first == 1)
            {
                first = 0;
                sep = "";
            }
            else
            {
                sep = ",";
            }
            ret = ret + sep + "FLAG_ACTIVITY_NO_ANIMATION";
        }
        if ((flags & FLAG_ACTIVITY_CLEAR_TASK) == FLAG_ACTIVITY_CLEAR_TASK)
        {
            if (first == 1)
            {
                first = 0;
                sep = "";
            }
            else
            {
                sep = ",";
            }
            ret = ret + sep + "FLAG_ACTIVITY_CLEAR_TASK";
        }
        if ((flags & FLAG_ACTIVITY_TASK_ON_HOME) == FLAG_ACTIVITY_TASK_ON_HOME)
        {
            if (first == 1)
            {
                first = 0;
                sep = "";
            }
            else
            {
                sep = ",";
            }
            ret = ret + sep + "FLAG_ACTIVITY_TASK_ON_HOME";
        }

        if ((flags & FLAG_GRANT_READ_URI_PERMISSION) == FLAG_GRANT_READ_URI_PERMISSION)
        {
            if (first == 1)
            {
                first = 0;
                sep = "";
            }
            else
            {
                sep = ",";
            }
            ret = ret + sep + "FLAG_GRANT_READ_URI_PERMISSION";
        }

        if ((flags & FLAG_GRANT_WRITE_URI_PERMISSION) == FLAG_GRANT_WRITE_URI_PERMISSION)
        {
            if (first == 1)
            {
                first = 0;
                sep = "";
            }
            else
            {
                sep = ",";
            }
            ret = ret + sep + "FLAG_GRANT_WRITE_URI_PERMISSION";
        }

        if ((flags & FLAG_FROM_BACKGROUND) == FLAG_FROM_BACKGROUND)
        {
            if (first == 1)
            {
                first = 0;
                sep = "";
            }
            else
            {
                sep = ",";
            }
            ret = ret + sep + "FLAG_FROM_BACKGROUND";
        }
        if ((flags & FLAG_DEBUG_LOG_RESOLUTION) == FLAG_DEBUG_LOG_RESOLUTION)
        {
            if (first == 1)
            {
                first = 0;
                sep = "";
            }
            else
            {
                sep = ",";
            }
            ret = ret + sep + "FLAG_DEBUG_LOG_RESOLUTION";
        }
        if ((flags & FLAG_EXCLUDE_STOPPED_PACKAGES) == FLAG_EXCLUDE_STOPPED_PACKAGES)
        {
            if (first == 1)
            {
                first = 0;
                sep = "";
            }
            else
            {
                sep = ",";
            }
            ret = ret + sep + "FLAG_EXCLUDE_STOPPED_PACKAGES";
        }
        if ((flags & FLAG_INCLUDE_STOPPED_PACKAGES) == FLAG_INCLUDE_STOPPED_PACKAGES)
        {
            if (first == 1)
            {
                first = 0;
                sep = "";
            }
            else
            {
                sep = ",";
            }
            ret = ret + sep + "FLAG_INCLUDE_STOPPED_PACKAGES";
        }
        if ((flags & FLAG_ACTIVITY_NO_HISTORY) == FLAG_ACTIVITY_NO_HISTORY)
        {
            if (first == 1)
            {
                first = 0;
                sep = "";
            }
            else
            {
                sep = ",";
            }
            ret = ret + sep + "FLAG_ACTIVITY_NO_HISTORY";
        }
        if ((flags & FLAG_ACTIVITY_SINGLE_TOP) == FLAG_ACTIVITY_SINGLE_TOP)
        {
            if (first == 1)
            {
                first = 0;
                sep = "";
            }
            else
            {
                sep = ",";
            }
            ret = ret + sep + "FLAG_ACTIVITY_SINGLE_TOP";
        }
        if ((flags & FLAG_ACTIVITY_NEW_TASK) == FLAG_ACTIVITY_NEW_TASK)
        {
            if (first == 1)
            {
                first = 0;
                sep = "";
            }
            else
            {
                sep = ",";
            }
            ret = ret + sep + "FLAG_ACTIVITY_NEW_TASK";
        }
        if ((flags & FLAG_ACTIVITY_MULTIPLE_TASK) == FLAG_ACTIVITY_MULTIPLE_TASK)
        {
            if (first == 1)
            {
                first = 0;
                sep = "";
            }
            else
            {
                sep = ",";
            }
            ret = ret + sep + "FLAG_ACTIVITY_MULTIPLE_TASK";
        }
        if ((flags & FLAG_ACTIVITY_CLEAR_TOP) == FLAG_ACTIVITY_CLEAR_TOP)
        {
            if (first == 1)
            {
                first = 0;
                sep = "";
            }
            else
            {
                sep = ",";
            }
            ret = ret + sep + "FLAG_ACTIVITY_CLEAR_TOP";
        }
        if ((flags & FLAG_ACTIVITY_FORWARD_RESULT) == FLAG_ACTIVITY_FORWARD_RESULT)
        {
            if (first == 1)
            {
                first = 0;
                sep = "";
            }
            else
            {
                sep = ",";
            }
            ret = ret + sep + "FLAG_ACTIVITY_FORWARD_RESULT";
        }
        if ((flags & FLAG_ACTIVITY_PREVIOUS_IS_TOP) == FLAG_ACTIVITY_PREVIOUS_IS_TOP)
        {
            if (first == 1)
            {
                sep = "";
            }
            else
            {
                sep = ",";
            }
            ret = ret + sep + "FLAG_ACTIVITY_PREVIOUS_IS_TOP";
        }

        return ret;
    }

    static void copyFiles(File sourceLocation, File targetLocation) throws IOException
    {
        if (sourceLocation.isDirectory())
        {
            if (!targetLocation.exists())
            {
                targetLocation.mkdir();
            }
            File[] files = sourceLocation.listFiles();
            for (File file : files)
            {
                InputStream in = new FileInputStream(file);
                OutputStream out = new FileOutputStream(targetLocation + "/" + file.getName());

                // Copy the bits from input stream to output stream
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0)
                {
                    out.write(buf, 0, len);
                }
                in.close();
                out.close();
            }

        }
    }

    static void deleteFile(String inputPath, String inputFile)
    {
        try
        {
            // delete the original file
            new File(inputPath + inputFile).delete();
        }
        catch (Exception e)
        {
            Log.e(TAG, e.getMessage());
        }
    }

    static void copyFile(String inputPath, String inputFile, String outputPath)
    {

        InputStream in;
        OutputStream out;
        try
        {

            //create output directory if it doesn't exist
            File dir = new File(outputPath);
            if (!dir.exists())
            {
                dir.mkdirs();
            }

            in = new FileInputStream(inputPath + inputFile);
            out = new FileOutputStream(outputPath + inputFile);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1)
            {
                out.write(buffer, 0, read);
            }
            in.close();

            // write the output file (You have now copied the file)
            out.flush();
            out.close();
        }
        catch (FileNotFoundException fnfe1)
        {
            Log.e(TAG, fnfe1.getMessage());
        }
        catch (Exception e)
        {
            Log.e(TAG, e.getMessage());
        }
    }

    //
    // thanks to:
    //
    // http://stackoverflow.com/questions/4178168/how-to-programmatically-move-copy-and-delete-files-and-directories-on-sd
    //
    static void moveFile(String inputPath, String inputFile, String outputPath)
    {
        InputStream in = null;
        OutputStream out = null;
        try
        {
            //create output directory if it doesn't exist
            File dir = new File(outputPath);
            if (!dir.exists())
            {
                dir.mkdirs();
            }

            in = new FileInputStream(inputPath + inputFile);
            out = new FileOutputStream(outputPath + inputFile);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1)
            {
                out.write(buffer, 0, read);
            }
            in.close();

            // write the output file
            out.flush();
            out.close();

            // delete the original file
            new File(inputPath + inputFile).delete();
        }

        catch (FileNotFoundException fnfe1)
        {
            Log.e(TAG, fnfe1.getMessage());
        }
        catch (Exception e)
        {
            Log.e(TAG, e.getMessage());
        }

    }

    static void deleteRecursive(final File fileOrDirectory) throws IOException
    {
        System.out.println("DeleteRecursive:" + "---> enter:" + fileOrDirectory.getCanonicalPath());
        if (fileOrDirectory.isDirectory())
        {
            // final String[] children = fileOrDirectory.list();
            //for (int i = 0; i < children.length; i++)
            for (File child : fileOrDirectory.listFiles())
            {
                // final File temp = new File(fileOrDirectory, children[i]);
                if (child.isDirectory())
                {
                    System.out.println("DeleteRecursive:" + "    Recursive Call" + child.getCanonicalPath());
                    deleteRecursive(child);
                }
                else
                {
                    System.out.println("DeleteRecursive:" + "    Delete File 2 can=" + child.getCanonicalPath());
                    // System.out.println("DeleteRecursive:" + "    Delete File 2 abs=" + temp.getAbsolutePath());
                    boolean b = child.delete();
                    if (!b)
                    {
                        System.out.println("DeleteRecursive:" + "[**]DELETE FAIL 1" + child.getCanonicalPath());
                    }
                }
            }

        }

        if (fileOrDirectory.delete())
        {
            System.out.println("DeleteRecursive:" + "[ok]Delete File 1" + fileOrDirectory.getCanonicalPath());
        }
        else
        {
            // fileOrDirectory.
            System.out.println("DeleteRecursive:" + "[**]DELETE FAIL 2 can=" + fileOrDirectory.getCanonicalPath());
            // System.out.println("DeleteRecursive:" + "[**]DELETE FAIL 2 abs=" + fileOrDirectory.getAbsolutePath());
        }

        System.out.println("DeleteRecursive:" + "---> return");
    }

    static void copyDirectoryOneLocationToAnotherLocation(File sourceLocation, File targetLocation) throws IOException
    {
        if (sourceLocation.isDirectory())
        {
            if (!targetLocation.exists())
            {
                targetLocation.mkdir();
            }

            String[] children = sourceLocation.list();
            for (int i = 0; i < sourceLocation.listFiles().length; i++)
            {
                copyDirectoryOneLocationToAnotherLocation(new File(sourceLocation, children[i]), new File(targetLocation, children[i]));
            }
        }
        else
        {
            long last_mod = 0L;
            boolean use_last_mod = true;
            try
            {
                last_mod = sourceLocation.lastModified();
            }
            catch (Exception e)
            {
                use_last_mod = false;
            }

            InputStream in = new FileInputStream(sourceLocation);
            OutputStream out = new FileOutputStream(targetLocation);

            // Copy the bits from instream to outstream
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0)
            {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
            if (use_last_mod)
            {
                try
                {
                    targetLocation.setLastModified(last_mod);
                }
                catch (Exception e)
                {
                }
            }
        }
    }

    private final static double M_PI_div_180 = 0.01745329251994329576;
    private final static double M_PI_div_360 = 0.008726646259971647884618;
    // cab be changed fron C code through navitgraphics
    static double __GEO_ACCURACY_FACTOR__ = 2.000;
    static double __EARTH_RADIUS__ = 6378137.000;

    public static int transform_from_geo_lat(double lat)
    {
        /* ZZ GEO TRANS ZZ */
        return (int) ((Math.log(Math.tan((Math.PI / 4f) + lat * M_PI_div_360)) * __EARTH_RADIUS__) * __GEO_ACCURACY_FACTOR__);
    }

    public static int transform_from_geo_lon(double lon)
    {
        /* ZZ GEO TRANS ZZ */

        return (int) ((lon * __EARTH_RADIUS__ * M_PI_div_180) * __GEO_ACCURACY_FACTOR__);
    }

    public static double transform_to_geo_lat(float y) // y
    {
        /* ZZ GEO TRANS ZZ */
        return (Math.atan(Math.exp((y / __GEO_ACCURACY_FACTOR__) / __EARTH_RADIUS__)) / M_PI_div_360 - 90);
    }

    public static double transform_to_geo_lon(float x) // x
    {
        /* ZZ GEO TRANS ZZ */
        return ((x / __GEO_ACCURACY_FACTOR__) / __EARTH_RADIUS__ / M_PI_div_180);
    }
}
