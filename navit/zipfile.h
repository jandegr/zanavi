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
 * modify it under the terms of the GNU Library General Public License
 * version 2 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this program; if not, write to the
 * Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
 * Boston, MA  02110-1301, USA.
 */

#ifndef __ZIPFILE_H__
#ifdef HAVE_API_WIN32_CE
/* cegcc doesn't honor the __attribute__(packed) need pragma to work */
#pragma pack(push)
#pragma pack(1)
#endif

#define zip_split_sig 0x08074b50

struct zip_split
{
	int zipsplitsig;
};

/* this is the normal "magic" header we use!! */
#define zip_lfh_sig 0x04034b50
/* this is the normal "magic" header we use!! */

#ifdef _MSC_VER
#pragma pack(push,1)
#endif /* _MSC_VER */
struct zip_lfh
{
	int ziplocsig;
	short zipver; // 4
	short zipgenfld; // 6
	short zipmthd; // 8
	short ziptime; // 10
	short zipdate; // 12
	int zipcrc; // 14
	unsigned int zipsize; // 18
	unsigned int zipuncmp; // 22
	unsigned short zipfnln; // 26
	unsigned short zipxtraln; // 30
	char zipname[0]; // 34
#ifndef _MSC_VER
}__attribute__ ((packed));
#else /* _MSC_VER */
};
#pragma pack(pop)
#endif /* _MSC_VER */

#define zip_cd_sig 0x02014b50

#ifdef _MSC_VER
#pragma pack(push,1)
#endif /* _MSC_VER */
struct zip_cd
{
	int zipcensig;
	char zipcver;
	char zipcos;
	char zipcvxt;
	char zipcexos;
	short zipcflg;
	short zipcmthd;
	short ziptim;
	short zipdat;
	int zipccrc;
	unsigned int zipcsiz;
	unsigned int zipcunc;
	unsigned short zipcfnl;
	unsigned short zipcxtl;
	unsigned short zipccml;
	unsigned short zipdsk;
	unsigned short zipint;
	unsigned int zipext;
	unsigned int zipofst;
	char zipcfn[0];
#ifndef _MSC_VER
}__attribute__ ((packed));
#else /* _MSC_VER */
};
#pragma pack(pop)
#endif /* _MSC_VER */

#ifdef _MSC_VER
#pragma pack(push,1)
#endif /* _MSC_VER */
struct zip_cd_ext
{
	short tag;
	short size;
	unsigned long long zipofst;
#ifndef _MSC_VER
}__attribute__ ((packed));
#else /* _MSC_VER */
};
#pragma pack(pop)
#endif /* _MSC_VER */

#ifdef _MSC_VER
#pragma pack(push,1)
#endif /* _MSC_VER */
struct zip_enc
{
	short efield_header;
	short efield_size;
	short version;
	char vendor_id1, vendor_id2;
	char encryption_strength;
	short compress_method;
#ifndef _MSC_VER
}__attribute__ ((packed));
#else /* _MSC_VER */
};
#pragma pack(pop)
#endif /* _MSC_VER */

#define zip_eoc_sig 0x6054b50

#ifdef _MSC_VER
#pragma pack(push,1)
#endif /* _MSC_VER */
struct zip_eoc
{
	int zipesig; /* end of central dir signature */
	unsigned short zipedsk; /* number of this disk */
	unsigned short zipecen; /* number of the disk with the start of the central directory */
	unsigned short zipenum; /* total number of entries in the central directory on this disk */
	unsigned short zipecenn; /* total number of entries in the central directory */
	unsigned long long zipecsz; /* size of the central directory */
	unsigned long long zipeofst; /* offset of start of central directory with respect to the starting disk number */
	short zipecoml; /* .ZIP file comment length */
	char zipecom[0]; /* .ZIP file comment */
#ifndef _MSC_VER
}__attribute__ ((packed));
#else /* _MSC_VER */
};
#pragma pack(pop)
#endif /* _MSC_VER */

#define zip64_eoc_sig 0x6064b50

#ifdef _MSC_VER
#pragma pack(push,1)
#endif /* _MSC_VER */
struct zip64_eoc
{
	int zip64esig; /* zip64 end of central dir signature */
	unsigned long long zip64esize; /* size of zip64 end of central directory record */
	unsigned short zip64ever; /* version made by */
	unsigned short zip64eneed; /* version needed to extract */
	unsigned int zip64edsk; /* number of this disk */
	unsigned int zip64ecen; /* number of the disk with the start of the central directory */
	unsigned long long zip64enum; /* total number of entries in the central directory on this disk */
	unsigned long long zip64ecenn; /* total number of entries in the central directory */
	unsigned long long zip64ecsz; /* size of the central directory */
	unsigned long long zip64eofst; /* offset of start of central directory with respect to the starting disk number */
	char zip64ecom[0]; /* zip64 extensible data sector */
#ifndef _MSC_VER
}__attribute__ ((packed));
#else /* _MSC_VER */
};
#pragma pack(pop)
#endif /* _MSC_VER */

#define zip64_eocl_sig 0x07064b50

#ifdef _MSC_VER
#pragma pack(push,1)
#endif /* _MSC_VER */
struct zip64_eocl
{
	int zip64lsig;
	int zip64ldsk;
	long long zip64lofst;
	int zip74lnum;
#ifndef _MSC_VER
}__attribute__ ((packed));
#else /* _MSC_VER */
};
#pragma pack(pop)
#endif /* _MSC_VER */

#define __ZIPFILE_H__
#ifdef __CEGCC__
#pragma pack(pop)
#endif

#endif
