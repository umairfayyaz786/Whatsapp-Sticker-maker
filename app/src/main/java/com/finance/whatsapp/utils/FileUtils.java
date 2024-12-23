package com.finance.whatsapp.utils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.provider.OpenableColumns;

import com.finance.whatsapp.Constants.Constants;

import java.io.*;
import java.util.Objects;
import java.util.Random;

public class FileUtils {

    public static String generateRandomIdentifier() {
        String possibilities = "abcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder generatedIdentifier = new StringBuilder();
        for (int i = 0; i < Constants.STICKER_PACK_IDENTIFIER_LENGHT; i++) {
            generatedIdentifier.append(possibilities.charAt(random.nextInt(possibilities.length() - 1)));
        }
        return generatedIdentifier.toString();
    }

    public static void initializeDirectories(Context context) {
        File directory = new File(Constants.STICKERS_DIRECTORY_PATH);
        if (!directory.exists()) {
            directory.mkdir();
            String value = "{\"androidPlayStoreLink\": \"\",\"iosAppStoreLink\": \"\",\"stickerPacks\": [ ]}";
            try {
                PrintWriter out = new PrintWriter(Constants.STICKERS_DIRECTORY_PATH + "/contents.json");
                out.write(value);
                out.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public static byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    static void deleteFolder(String path) {
        File dir = new File(path);
        if (dir.exists()) {
            File[] files = dir.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    deleteFolder(files[i].getPath());
                } else {
                    files[i].delete();
                }
            }
        }
        dir.delete();
    }

    public static void deleteFile(String path, Context context) {
        File file = new File(path);
        if (file.exists()) {
            file.delete();
        }
        context.getContentResolver().delete(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                MediaStore.MediaColumns.DATA + "='" + path + "'", null
        );
    }

    public static String getImageRealPathFromURI(Context context, Uri contentUri) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try (Cursor cursor = context.getContentResolver().query(contentUri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    return cursor.getString(nameIndex);
                }
            }
        } else {
            String[] proj = {MediaStore.Images.Media.DATA};
            try (Cursor cursor = context.getContentResolver().query(contentUri, proj, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    return cursor.getString(column_index);
                }
            }
        }
        return null;
    }

    public static String getFolderSizeLabel(String path) {
        long size = getFolderSize(new File(path)) / 1024; // Get size and convert bytes into Kb.
        if (size >= 1024) {
            return (size / 1024) + " MB";
        } else {
            return size + " KB";
        }
    }

    private static long getFolderSize(File file) {
        long size = 0;
        if (file.isDirectory()) {
            for (File child : file.listFiles()) {
                size += getFolderSize(child);
            }
        } else {
            size = file.length();
        }
        return size;
    }
    public static String getFileName(Context context, Uri uri) {
        String fileName = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    fileName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME));
                }
            }
        } else if (uri.getScheme().equals("file")) {
            fileName = new File(uri.getPath()).getName();
        }
        return fileName;
    }
}
