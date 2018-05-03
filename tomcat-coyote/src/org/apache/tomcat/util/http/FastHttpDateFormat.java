// 
// Decompiled by Procyon v0.5.30
// 

package org.apache.tomcat.util.http;

import java.util.Locale;
import java.text.ParseException;
import java.text.DateFormat;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.TimeZone;
import java.text.SimpleDateFormat;

public final class FastHttpDateFormat
{
    private static final int CACHE_SIZE;
    private static final SimpleDateFormat format;
    private static final SimpleDateFormat[] formats;
    private static final TimeZone gmtZone;
    private static volatile long currentDateGenerated;
    private static String currentDate;
    private static final ConcurrentHashMap<Long, String> formatCache;
    private static final ConcurrentHashMap<String, Long> parseCache;
    
    public static final String getCurrentDate() {
        final long now = System.currentTimeMillis();
        if (now - FastHttpDateFormat.currentDateGenerated > 1000L) {
            synchronized (FastHttpDateFormat.format) {
                if (now - FastHttpDateFormat.currentDateGenerated > 1000L) {
                    FastHttpDateFormat.currentDate = FastHttpDateFormat.format.format(new Date(now));
                    FastHttpDateFormat.currentDateGenerated = now;
                }
            }
        }
        return FastHttpDateFormat.currentDate;
    }
    
    public static final String formatDate(final long value, final DateFormat threadLocalformat) {
        final Long longValue = new Long(value);
        final String cachedDate = FastHttpDateFormat.formatCache.get(longValue);
        if (cachedDate != null) {
            return cachedDate;
        }
        String newDate = null;
        final Date dateValue = new Date(value);
        if (threadLocalformat != null) {
            newDate = threadLocalformat.format(dateValue);
            updateFormatCache(longValue, newDate);
        }
        else {
            synchronized (FastHttpDateFormat.formatCache) {
                synchronized (FastHttpDateFormat.format) {
                    newDate = FastHttpDateFormat.format.format(dateValue);
                }
                updateFormatCache(longValue, newDate);
            }
        }
        return newDate;
    }
    
    public static final long parseDate(final String value, final DateFormat[] threadLocalformats) {
        final Long cachedDate = FastHttpDateFormat.parseCache.get(value);
        if (cachedDate != null) {
            return cachedDate;
        }
        Long date = null;
        if (threadLocalformats != null) {
            date = internalParseDate(value, threadLocalformats);
            updateParseCache(value, date);
        }
        else {
            synchronized (FastHttpDateFormat.parseCache) {
                date = internalParseDate(value, FastHttpDateFormat.formats);
                updateParseCache(value, date);
            }
        }
        if (date == null) {
            return -1L;
        }
        return date;
    }
    
    private static final Long internalParseDate(final String value, final DateFormat[] formats) {
        Date date = null;
        for (int i = 0; date == null && i < formats.length; ++i) {
            try {
                date = formats[i].parse(value);
            }
            catch (ParseException ex) {}
        }
        if (date == null) {
            return null;
        }
        return new Long(date.getTime());
    }
    
    private static void updateFormatCache(final Long key, final String value) {
        if (value == null) {
            return;
        }
        if (FastHttpDateFormat.formatCache.size() > FastHttpDateFormat.CACHE_SIZE) {
            FastHttpDateFormat.formatCache.clear();
        }
        FastHttpDateFormat.formatCache.put(key, value);
    }
    
    private static void updateParseCache(final String key, final Long value) {
        if (value == null) {
            return;
        }
        if (FastHttpDateFormat.parseCache.size() > FastHttpDateFormat.CACHE_SIZE) {
            FastHttpDateFormat.parseCache.clear();
        }
        FastHttpDateFormat.parseCache.put(key, value);
    }
    
    static {
        CACHE_SIZE = Integer.parseInt(System.getProperty("org.apache.tomcat.util.http.FastHttpDateFormat.CACHE_SIZE", "1000"));
        format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
        formats = new SimpleDateFormat[] { new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US), new SimpleDateFormat("EEEEEE, dd-MMM-yy HH:mm:ss zzz", Locale.US), new SimpleDateFormat("EEE MMMM d HH:mm:ss yyyy", Locale.US) };
        gmtZone = TimeZone.getTimeZone("GMT");
        FastHttpDateFormat.format.setTimeZone(FastHttpDateFormat.gmtZone);
        FastHttpDateFormat.formats[0].setTimeZone(FastHttpDateFormat.gmtZone);
        FastHttpDateFormat.formats[1].setTimeZone(FastHttpDateFormat.gmtZone);
        FastHttpDateFormat.formats[2].setTimeZone(FastHttpDateFormat.gmtZone);
        FastHttpDateFormat.currentDateGenerated = 0L;
        FastHttpDateFormat.currentDate = null;
        formatCache = new ConcurrentHashMap<Long, String>(FastHttpDateFormat.CACHE_SIZE);
        parseCache = new ConcurrentHashMap<String, Long>(FastHttpDateFormat.CACHE_SIZE);
    }
}
