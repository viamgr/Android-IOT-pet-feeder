package com.viam.feeder.data.domain.specification;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;

public class ProgressRequestBody extends RequestBody {
    private static final int DEFAULT_BUFFER_SIZE = 2048;
    private InputStream mFile;
    private int mSize;
    private String mPath;
    private String content_type;
    private String upload;

    public ProgressRequestBody(final InputStream file, int size, String content_type) {
        this.content_type = content_type;
        mFile = file;
        mSize = size;
    }

    static long getInputLength(InputStream inputStream) {
        try {
            if (inputStream instanceof FilterInputStream) {
                FilterInputStream filtered = (FilterInputStream) inputStream;
                Field field = FilterInputStream.class.getDeclaredField("in");
                field.setAccessible(true);
                InputStream internal = (InputStream) field.get(filtered);
                return getInputLength(internal);
            } else if (inputStream instanceof ByteArrayInputStream) {
                ByteArrayInputStream wrapper = (ByteArrayInputStream) inputStream;
                Field field = ByteArrayInputStream.class.getDeclaredField("buf");
                field.setAccessible(true);
                byte[] buffer = (byte[]) field.get(wrapper);
                return buffer.length;
            } else if (inputStream instanceof FileInputStream) {
                FileInputStream fileStream = (FileInputStream) inputStream;
                return fileStream.getChannel().size();
            }
        } catch (NoSuchFieldException | IllegalAccessException | IOException exception) {
            // Ignore all errors and just return -1.
        }
        return -1;
    }

    @Nullable
    @Override
    public MediaType contentType() {
        return MediaType.parse(content_type + "/*");
    }

    @Override
    public long contentLength() throws IOException {
        return mSize;
    }

    @Override
    public void writeTo(@NonNull BufferedSink sink) throws IOException {
        long fileLength = mSize;
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        long uploaded = 0;

        int read;
        Handler handler = new Handler(Looper.getMainLooper());
        while ((read = mFile.read(buffer)) != -1) {
            uploaded += read;
            sink.write(buffer, 0, read);
            handler.post(new ProgressUpdater(uploaded, fileLength));
        }

    }

    private class ProgressUpdater implements Runnable {
        private long mUploaded;
        private long mTotal;

        ProgressUpdater(long uploaded, long total) {
            mUploaded = uploaded;
            mTotal = total;
        }

        @Override
        public void run() {
            //update progress here in the UI
        }
    }
}
