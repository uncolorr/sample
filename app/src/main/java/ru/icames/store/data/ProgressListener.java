package ru.icames.store.data;

public interface ProgressListener {
    void update(long bytesRead, long contentLength, boolean done);
}