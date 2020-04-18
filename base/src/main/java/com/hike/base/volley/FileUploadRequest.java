package com.hike.base.volley;

import android.text.TextUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by SAUNIK on 25-02-2016.
 */
public class FileUploadRequest {
    private byte[] multipartBody;

    private final String twoHyphens = "--";
    private final String lineEnd = "\r\n";
    public static String boundary = "*****" + Long.toString(System.currentTimeMillis()) + "*****";
    public static final String mMimeType = "multipart/form-data;boundary=" + boundary;

    private void buildPart(DataOutputStream dataOutputStream, byte[] fileData,String key, String fileName) throws IOException {
        dataOutputStream.writeBytes(twoHyphens + boundary + lineEnd);
        dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"" + key + "\"; filename=\""
                + fileName + "\"" + lineEnd);
        dataOutputStream.writeBytes(lineEnd);

        ByteArrayInputStream fileInputStream = new ByteArrayInputStream(fileData);
        int bytesAvailable = fileInputStream.available();

        int maxBufferSize = 1024 * 1024;
        int bufferSize = Math.min(bytesAvailable, maxBufferSize);
        byte[] buffer = new byte[bufferSize];

        // read file and write it into form...
        int bytesRead = fileInputStream.read(buffer, 0, bufferSize);

        while (bytesRead > 0) {
            dataOutputStream.write(buffer, 0, bufferSize);
            bytesAvailable = fileInputStream.available();
            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            bytesRead = fileInputStream.read(buffer, 0, bufferSize);
            dataOutputStream.flush();
        }

        dataOutputStream.writeBytes(lineEnd);
    }

    public HashMap<String, String> getheader(String token) {
        HashMap<String, String> headerMap = new HashMap<String, String>();
        headerMap.put("Connection", "Keep-Alive");
        headerMap.put("User-Agent", "Android Multipart HTTP Client 1.0");
        headerMap.put("Content-Type", "multipart/form-data; boundary=" + boundary);
        headerMap.put("Accept", "application/json");
        if (!TextUtils.isEmpty(token)) {
            headerMap.put("Authorization", token);
        }
        return headerMap;
    }

    public byte[] getMultipartBody(Map<String, String> params, byte[] fileData, String fileKey, String fileName) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        try {
            // Upload POST Data
            Iterator<String> keys = params.keySet().iterator();
            while (keys.hasNext()) {
                String key = keys.next();
                String value = params.get(key);
                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"" + key + "\"" + lineEnd);
                dos.writeBytes("Content-Type: text/plain" + lineEnd);
                dos.writeBytes(lineEnd);
                dos.writeBytes(value);
                dos.writeBytes(lineEnd);
            }
            dos.writeBytes(twoHyphens + boundary + lineEnd);
            buildPart(dos, fileData, fileKey, fileName);
            // send multipart form data necesssary after file data
            dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
            // pass to multipart body
            multipartBody = bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return multipartBody;
    }
}
