package com.simon.lib.cacheloader.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.zip.CRC32;

/**
 * @author mengsun
 * @date 2015-11-17 17:00:34
 */
public class IOUtils {

    public static final int UNIT = 1024;

    public static byte[] read(InputStream is) throws IOException {
        byte[] data = null;
        byte[] buffer = new byte[4 * 1024 * 1024];
        int length = -1;
        int offset = 0;
        while ((length = is.read(buffer, offset, buffer.length - offset)) != -1) {
            if (offset + length >= buffer.length) {
                data = flush(buffer, data, buffer.length);
                offset = 0;
            } else {
                offset += length;
            }
        }
        data = flush(buffer, data, offset);
        return data;
    }

    private static byte[] flush(byte[] buffer, byte[] target, int dataLen) {
        byte[] temp = new byte[(target == null ? 0 : target.length) + dataLen];
        if (target != null) {
            System.arraycopy(target, 0, temp, 0, target.length);
        }
        System.arraycopy(buffer, 0, temp, (target == null ? 0 : target.length), dataLen);
        return temp;
    }

    public static byte[] read(String filePath) {
        FileInputStream is = null;
        try {
            is = new FileInputStream(filePath);
            return read(is);
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        } catch (OutOfMemoryError e) {
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }
        }
        return null;
    }

    public static byte[] readUrl(String httpUrl) throws Exception {
        HttpURLConnection conn = null;
        byte[] data = null;
        try {
            URL url = new URL(httpUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5000);
            conn.connect();
            data = IOUtils.read(conn.getInputStream());
        } catch (OutOfMemoryError e) {
            return null;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        return data;
    }

    public static byte[] readWithCRC(String filePath, long crc) {
        byte[] data = read(filePath);
        if (getCRC(data) == crc && crc > 0) {
            return data;
        }
        return null;
    }

    public static boolean write(byte[] data, String filePath) {
        FileOutputStream os = null;
        File f = new File(filePath);
        if (!f.getParentFile().exists() && !f.getParentFile().mkdirs()) {
            return false;
        }
        try {
            if (f.exists() || !f.createNewFile()) {
                return false;
            }
            os = new FileOutputStream(f);
            os.write(data);
            os.flush();
            return true;
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                }
            }
        }
        return false;
    }

    public static long getCRC(byte[] data) {
        long crc = 0;
        if (data != null) {
            CRC32 crc32 = new CRC32();
            crc32.update(data);
            crc = crc32.getValue();
        }
        return crc;
    }

    public static void delete(String filePath) {
        File f = new File(filePath);
        if (f.isDirectory()) {
            for (File sub : f.listFiles()) {
                sub.delete();
            }
        }
        if (f.exists()) {
            f.delete();
        }
    }

    public static Object getSizeStr(long cacheSize) {
        return null;
    }

    public static long formatSize(String unit) {
        if (unit.equalsIgnoreCase("T") || unit.equalsIgnoreCase("TB")) {
            return UNIT * UNIT * UNIT * UNIT;
        } else if (unit.equalsIgnoreCase("G") || unit.equalsIgnoreCase("GB")) {
            return UNIT * UNIT * UNIT;
        } else if (unit.equalsIgnoreCase("M") || unit.equalsIgnoreCase("MB")) {
            return UNIT * UNIT;
        } else if (unit.equalsIgnoreCase("K") || unit.equalsIgnoreCase("KB")) {
            return UNIT;
        } else if (unit.equalsIgnoreCase("B")) {
            return 1;
        }
        throw new IllegalArgumentException("can not parse unit" + unit);
    }

    public static String formatSize(float size, String format) {
        long kb = UNIT;
        long mb = (kb * UNIT);
        long gb = (mb * UNIT);
        long tb = (gb * UNIT);
        if (size < kb) {
            return String.format("%dB", (int) size);
        } else if (size < mb) {
            return String.format(format + "KB", size / kb);
        } else if (size < gb) {
            return String.format(format + "MB", size / mb);
        } else if (size < tb) {
            return String.format(format + "GB", size / gb);
        } else {
            return String.format(format + "TB", size / gb / UNIT);
        }
    }

    public static long getLength(File file) {
        long size = file.length();
        if (file.isDirectory()) {
            for (File f : file.listFiles()) {
                size += getLength(f);
            }
        }
        return size;
    }
}
