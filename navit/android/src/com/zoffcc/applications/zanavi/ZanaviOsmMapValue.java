package com.zoffcc.applications.zanavi;

import java.net.URL;

class ZanaviOsmMapValue {

    private String serverURL = null; // specifies an URL that overrides the Zanavi mapserver(s)
    final String map_name;
    final String url; // is of the from somemap.bin
    long est_size_bytes;
    private final boolean is_continent;
    private final int continent_id;
    private long mIdxSize = 0;
    private String mBuildDateString;

    ZanaviOsmMapValue(String mapname, String url, long bytes_est, Boolean is_con, int con_id) {
        this.is_continent = is_con;
        this.continent_id = con_id;
        this.map_name = mapname;
        this.url = url;
        this.est_size_bytes = bytes_est;
        this.mBuildDateString = "";
    }

    private String getEstSizeHumanString() {
        if (this.est_size_bytes > 0) {
            if (((int) ((float) (this.est_size_bytes) / 1024f / 1024f)) > 0) {
                return " " + (int) ((float) (this.est_size_bytes) / 1024f / 1024f) + "MB";
            } else {
                return " " + (int) ((float) (this.est_size_bytes) / 1024f) + "kB";
            }
        } else {
            return "";
        }
    }

    ZanaviOsmMapValue(String mapname, String url, long bytes_est, Boolean is_con, int con_id, String serverURL, long idxSize, String buildDateString) {
        this(mapname, url, bytes_est, is_con, con_id);
        this.serverURL = serverURL;
        this.mIdxSize = idxSize;
        this.mBuildDateString = buildDateString;
    }

    public String toString() {
        return "continent_id=" + this.continent_id + " est_size_bytes=" + this.est_size_bytes + " est_size_bytes_human_string=\"" + this.getEstSizeHumanString() + "\" map_name=\"" + this.map_name + "\" text_for_select_list=\"" + this.getTextForSelectList() + "\" url=" + this.url;
    }

    URL getMD5URL() {
        if (this.serverURL != null) {
            try {
                return new URL(this.serverURL + this.url + ".md5");
            }
            catch (Exception e) {

            }
        }
        return null;
    }

    URL getMapUrl() {
        if (this.serverURL != null) {
            try {
                return new URL(this.serverURL + this.url);
            }
            catch (Exception e){

            }
        }
        return null;
    }

    long getEstSize() {
        return this.est_size_bytes;
    }

    URL getIdxUrl() {
        if (this.serverURL != null) {
            try {
                return new URL(this.serverURL + this.url + ".idx");
            }
            catch (Exception e){

            }
        }
        return null;
    }

    long getIdxSize() {
        return this.mIdxSize;
    }

    String getTextForSelectList() {
        String text = this.map_name + " " + this.getEstSizeHumanString();
        if (this.mBuildDateString.length() > 5) {
            text = text + " - " + this.mBuildDateString;
        }
        return text;
    }

    boolean isContinent() {
        return this.is_continent;
    }

    int getContinentId() {
        return this.continent_id;
    }
}
