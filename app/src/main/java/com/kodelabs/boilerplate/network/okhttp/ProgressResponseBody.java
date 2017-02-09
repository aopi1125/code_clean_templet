package com.kodelabs.boilerplate.network.okhttp;

import com.kodelabs.boilerplate.network.listener.ProgressListener;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;
import okio.Source;

/**
 * Created by chenwf on 2017/01/04.
 */
public class ProgressResponseBody extends ResponseBody {

    private ResponseBody delegate;
    private ProgressListener progressListener;
    private BufferedSource progressSource;

    protected ProgressResponseBody(ResponseBody delegate, ProgressListener progressListener) {
        this.delegate = delegate;
        this.progressListener = progressListener;
    }

    @Override
    public MediaType contentType() {
        return delegate.contentType();
    }

    @Override
    public long contentLength() {
        return delegate.contentLength();
    }

    @Override
    public BufferedSource source() {
        if (progressSource == null) {
            progressSource = Okio.buffer(new ProgressSource(delegate.source()));
        }
        return progressSource;
    }


    private class ProgressSource extends ForwardingSource {

        private long bytesRead = 0;

        public ProgressSource(Source delegate) {
            super(delegate);
        }

        @Override
        public long read(Buffer source, long byteCount) throws IOException {
            long read = super.read(source, byteCount);
            if (read > 0) {
                bytesRead += read;
            }
            if (progressListener != null) {
                progressListener.onResponseProgress(bytesRead, contentLength(), read<0);
            }
            return read;
        }
    }
}
