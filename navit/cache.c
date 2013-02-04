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

#include "glib_slice.h"
#ifdef DEBUG_CACHE
#include <stdio.h>
#endif
#include <string.h>
#include "debug.h"
#include "types.h"
#include "cache.h"

static void cache_entry_dump(struct cache *cache, struct cache_entry *entry)
{
	int i, size;
	//dbg(0,"Usage: %d size %d\n",entry->usage, entry->size);
	if (cache)
		size = cache->id_size;
	else
		size = 5;
	for (i = 0; i < size; i++)
	{
		dbg(0, "0x%x\n", entry->id[i]);
	}
}

static void cache_list_dump(char *str, struct cache *cache, struct cache_entry_list *list)
{
	struct cache_entry *first = list->first;
	dbg(0, "dump %s %d\n", str, list->size);
	while (first)
	{
		cache_entry_dump(cache, first);
		first = first->next;
	}
}

static guint cache_hash4(gconstpointer key)
{
	int *id = (int *) key;
	return id[0];
}

static guint cache_hash20(gconstpointer key)
{
	int *id = (int *) key;
	return id[0] ^ id[1] ^ id[2] ^ id[3] ^ id[4];
}

static gboolean cache_equal4(gconstpointer a, gconstpointer b)
{
	int *ida = (int *) a;
	int *idb = (int *) b;

	return ida[0] == idb[0];
}

static gboolean cache_equal20(gconstpointer a, gconstpointer b)
{
	int *ida = (int *) a;
	int *idb = (int *) b;

	return (ida[0] == idb[0] && ida[1] == idb[1] && ida[2] == idb[2] && ida[3] == idb[3] && ida[4] == idb[4]);
}

struct cache *
cache_new(int id_size, int size)
{
	struct cache *cache=g_new0(struct cache, 1);

	cache->id_size = id_size / 4;
	cache->entry_size = cache->id_size * sizeof(int) + sizeof(struct cache_entry);
	cache->size = size;
	//cache->real_size_bytes = 0;

	dbg(0, "_c id_size=%d\n", id_size);
	dbg(0, "_c size=%d\n", size);

	switch (id_size)
	{
		case 4:
			cache->hash = g_hash_table_new(cache_hash4, cache_equal4);
			break;
		case 20:
			cache->hash = g_hash_table_new(cache_hash20, cache_equal20);
			break;
		default:
			dbg(0, "cache with id_size of %d not supported\n", id_size);
			g_free(cache);
			cache = NULL;
	}
	return cache;
}

static void cache_insert_mru(struct cache *cache, struct cache_entry_list *list, struct cache_entry *entry)
{
	entry->prev = NULL;
	entry->next = list->first;
	entry->where = list;
	if (entry->next)
		entry->next->prev = entry;
	list->first = entry;
	if (!list->last)
		list->last = entry;
	list->size += entry->size;
	if (cache)
		g_hash_table_insert(cache->hash, (gpointer) entry->id, entry);
}

static void cache_remove_from_list(struct cache_entry_list *list, struct cache_entry *entry)
{
	if (entry->prev)
		entry->prev->next = entry->next;
	else
		list->first = entry->next;
	if (entry->next)
		entry->next->prev = entry->prev;
	else
		list->last = entry->prev;
	list->size -= entry->size;
}

static void cache_remove(struct cache *cache, struct cache_entry *entry)
{
	// dbg(1,"remove 0x%x 0x%x 0x%x 0x%x 0x%x\n", entry->id[0], entry->id[1], entry->id[2], entry->id[3], entry->id[4]);
	g_hash_table_remove(cache->hash, (gpointer)(entry->id));
	g_slice_free1(entry->size, entry);
	// real size
	//dbg(0,"1cache->real_size_bytes="LONGLONG_FMT"\n", cache->real_size_bytes);
	//cache->real_size_bytes = cache->real_size_bytes - entry->size;
}

static struct cache_entry *
cache_remove_lru_helper(struct cache_entry_list *list)
{
	struct cache_entry *last = list->last;
	if (!last)
		return NULL;
	list->last = last->prev;
	if (last->prev)
		last->prev->next = NULL;
	else
		list->first = NULL;
	list->size -= last->size;
	return last;
}

static struct cache_entry *
cache_remove_lru(struct cache *cache, struct cache_entry_list *list)
{
	struct cache_entry *last;
	int seen = 0;
	while (list->last && list->last->usage && seen < list->size)
	{
		last = cache_remove_lru_helper(list);
		cache_insert_mru(NULL, list, last);
		seen += last->size;
	}
	last = list->last;
	if (!last || last->usage || seen >= list->size)
	{
		return NULL;
	}
	//dbg(1,"removing %d\n", last->id[0]);
	cache_remove_lru_helper(list);
	if (cache)
	{
		cache_remove(cache, last);
		return NULL;
	}
	return last;
}

void *
cache_entry_new(struct cache *cache, void *id, int size)
{
	struct cache_entry *ret;
	size += cache->entry_size;
	// cache->misses += size;
	cache->misses += 1;
	ret = (struct cache_entry *) g_slice_alloc0(size);
	// real size
	//dbg(0,"2cache->real_size_bytes="LONGLONG_FMT"\n", cache->real_size_bytes);
	//cache->real_size_bytes = cache->real_size_bytes + size;
	ret->size = size;
	ret->usage = 1;
	memcpy(ret->id, id, cache->id_size * sizeof(int));
	return &ret->id[cache->id_size];
}

void cache_entry_destroy(struct cache *cache, void *data)
{
	struct cache_entry *entry = (struct cache_entry *) ((char *) data - cache->entry_size);
	// dbg(1,"destroy 0x%x 0x%x 0x%x 0x%x 0x%x\n", entry->id[0], entry->id[1], entry->id[2], entry->id[3], entry->id[4]);
	entry->usage--;
}

static struct cache_entry *
cache_trim(struct cache *cache, struct cache_entry *entry)
{
	struct cache_entry *new_entry;
	//dbg(1,"trim 0x%x 0x%x 0x%x 0x%x 0x%x\n", entry->id[0], entry->id[1], entry->id[2], entry->id[3], entry->id[4]);
	//dbg(1,"Trim %x from %d -> %d\n", entry->id[0], entry->size, cache->size);
	if (cache->entry_size < entry->size)
	{
		g_hash_table_remove(cache->hash, (gpointer)(entry->id));

		new_entry = g_slice_alloc0(cache->entry_size);
		// real size
		//dbg(0,"3cache->real_size_bytes="LONGLONG_FMT"\n", cache->real_size_bytes);
		//cache->real_size_bytes = cache->real_size_bytes + cache->entry_size;
		memcpy(new_entry, entry, cache->entry_size);
		g_slice_free1(entry->size, entry);
		// real size
		//cache->real_size_bytes = cache->real_size_bytes - entry->size;

		new_entry->size = cache->entry_size;

		g_hash_table_insert(cache->hash, (gpointer) new_entry->id, new_entry);
	}
	else
	{
		new_entry = entry;
	}

	return new_entry;
}

static struct cache_entry *
cache_move(struct cache *cache, struct cache_entry_list *old, struct cache_entry_list *new)
{
	struct cache_entry *entry;
	entry = cache_remove_lru(NULL, old);
	if (!entry)
		return NULL;
	entry = cache_trim(cache, entry);
	cache_insert_mru(NULL, new, entry);
	return entry;
}

static int cache_replace(struct cache *cache)
{
	if (cache->t1.size >= MAX(1, cache->t1_target))
	{
		//dbg(1,"replace 12\n");
		if (!cache_move(cache, &cache->t1, &cache->b1))
			cache_move(cache, &cache->t2, &cache->b2);
	}
	else
	{
		//dbg(1,"replace t2\n");
		if (!cache_move(cache, &cache->t2, &cache->b2))
			cache_move(cache, &cache->t1, &cache->b1);
	}
#if 0
	if (! entry)
	{
		cache_dump(cache);
		exit(0);
	}
#endif
	return 1;
}

void cache_flush(struct cache *cache, void *id)
{
	struct cache_entry *entry = g_hash_table_lookup(cache->hash, id);
	if (entry)
	{
		cache_remove_from_list(entry->where, entry);
		cache_remove(cache, entry);
	}
}

void cache_flush_data(struct cache *cache, void *data)
{
	struct cache_entry *entry = (struct cache_entry *) ((char *) data - cache->entry_size);
	if (entry)
	{
		cache_remove_from_list(entry->where, entry);
		cache_remove(cache, entry);
	}
}

void *
cache_lookup(struct cache *cache, void *id)
{
	struct cache_entry *entry;

	//dbg(1,"get %d\n", ((int *)id)[0]);
	entry = g_hash_table_lookup(cache->hash, id);
	if (entry == NULL)
	{
		cache->insert = &cache->t1;
#ifdef DEBUG_CACHE
		fprintf(stderr,"-");
#endif
		//dbg(1,"not in cache\n");
		return NULL;
	}

	// dbg(1,"found 0x%x 0x%x 0x%x 0x%x 0x%x\n", entry->id[0], entry->id[1], entry->id[2], entry->id[3], entry->id[4]);
	if (entry->where == &cache->t1 || entry->where == &cache->t2)
	{
		// cache->hits += entry->size;
		cache->hits += 1;
#ifdef DEBUG_CACHE
		if (entry->where == &cache->t1)
		{
			fprintf(stderr,"h");
		}
		else
		{
			fprintf(stderr,"H");
		}
#endif
		// dbg(1,"in cache %s\n", entry->where == &cache->t1 ? "T1" : "T2");
		cache_remove_from_list(entry->where, entry);
		cache_insert_mru(NULL, &cache->t2, entry);
		entry->usage++;
		return &entry->id[cache->id_size];
	}
	else
	{
		if (entry->where == &cache->b1)
		{
#ifdef DEBUG_CACHE
			fprintf(stderr,"m");
#endif
			//dbg(1,"in phantom cache B1\n");
			cache->t1_target = MIN(cache->t1_target + MAX(cache->b2.size / cache->b1.size, 1), cache->size);
			cache_remove_from_list(&cache->b1, entry);
		}
		else if (entry->where == &cache->b2)
		{
#ifdef DEBUG_CACHE
			fprintf(stderr,"M");
#endif
			//dbg(1,"in phantom cache B2\n");
			cache->t1_target = MAX(cache->t1_target - MAX(cache->b1.size / cache->b2.size, 1), 0);
			cache_remove_from_list(&cache->b2, entry);
		}
		else
		{
			//dbg(0,"**ERROR** invalid where\n");
		}
		cache_replace(cache);
		cache_remove(cache, entry);
		cache->insert = &cache->t2;
		return NULL;
	}
}

void cache_insert(struct cache *cache, void *data)
{
	struct cache_entry *entry = (struct cache_entry *) ((char *) data - cache->entry_size);
	//dbg(1,"insert 0x%x 0x%x 0x%x 0x%x 0x%x\n", entry->id[0], entry->id[1], entry->id[2], entry->id[3], entry->id[4]);
	if (cache->insert == &cache->t1)
	{
		if (cache->t1.size + cache->b1.size >= cache->size)
		{
			if (cache->t1.size < cache->size)
			{
				cache_remove_lru(cache, &cache->b1);
				cache_replace(cache);
			}
			else
			{
				cache_remove_lru(cache, &cache->t1);
			}
		}
		else
		{
			if (cache->t1.size + cache->t2.size + cache->b1.size + cache->b2.size >= cache->size)
			{
				if (cache->t1.size + cache->t2.size + cache->b1.size + cache->b2.size >= 2 * cache->size)
				{
					cache_remove_lru(cache, &cache->b2);
				}
				cache_replace(cache);
			}
		}
	}
	cache_insert_mru(cache, cache->insert, entry);
}

void *
cache_insert_new(struct cache *cache, void *id, int size)
{
	void *data = cache_entry_new(cache, id, size);
	cache_insert(cache, data);
	return data;
}

void cache_stats(struct cache *cache)
{
	unsigned long c_ratio = 0;
	//if ((cache->hits + cache->misses) > 0)
	//{
	//	c_ratio = (long) (cache->hits) * (long) (100L);
	//	//dbg(0,"1 c_ratio=%lu\n", c_ratio);
	//	c_ratio = c_ratio / (long) (cache->hits + cache->misses);
	//	//dbg(0,"2 c_ratio=%lu\n", c_ratio);
	//
	//	dbg(0, "hits %d misses %lu hitratio %d size %d entry_size %d id_size %d T1 target %d\n", cache->hits, cache->misses, c_ratio, cache->size, cache->entry_size, cache->id_size, cache->t1_target);
	//	// dbg(0,"T1:%d B1:%d T2:%d B2:%d\n", cache->t1.size, cache->b1.size, cache->t2.size, cache->b2.size);
	//
	//	// if numbers are too big, reset them
	//	if ((cache->hits > 1000000000) || (cache->misses > 1000000000))
	//	{
	//		cache->hits = 0;
	//		cache->misses = 0;
	//	}
	//}

	if ((cache->hits + cache->misses) > 0)
	{
		c_ratio = (long) (cache->hits) * (long) (100L);
		c_ratio = c_ratio / (long) (cache->hits + cache->misses);

		dbg(0, "hits %lu misses %lu hitratio %d size %d entry_size %d id_size %d T1 target %d\n", cache->hits, cache->misses, c_ratio, cache->size, cache->entry_size, cache->id_size, cache->t1_target);

		// if numbers are too big, reset them
		if ((cache->hits > 1000000000) || (cache->misses > 1000000000))
		{
			cache->hits = 0;
			cache->misses = 0;
		}
	}

	// dbg(0,"CACHE wanted size=%d real size=%lu\n", cache->size, cache->real_size_bytes);
}

void cache_dump(struct cache *cache)
{
	struct cache_entry *first;
	first = cache->t1.first;
	cache_stats(cache);
	cache_list_dump("T1", cache, &cache->t1);
	cache_list_dump("B1", cache, &cache->b1);
	cache_list_dump("T2", cache, &cache->t2);
	cache_list_dump("B2", cache, &cache->b2);
	dbg(0, "dump end\n");
}

