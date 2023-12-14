package io.github.googlebardclient;

import okhttp3.Headers;

final class Constants {

    static final String BASE_URL = "https://bard.google.com";
    static final String CHAT_URL = "_/BardChatUi/data/assistant.lamda.BardFrontendService/StreamGenerate";
    static final String UPLOAD_URL = "https://content-push.googleapis.com/upload/";
    static final Headers IMG_UPLOAD_HEADERS = new Headers.Builder()
            .add("authority", "content-push.googleapis.com")
            .add("accept", "*/*")
            .add("accept-language", "en-US,en;q=0.7")
            .add("authorization", "Basic c2F2ZXM6cyNMdGhlNmxzd2F2b0RsN3J1d1U=") // Constant Authorization Key
            .add("content-type", "application/x-www-form-urlencoded;charset=UTF-8")
            .add("origin", BASE_URL)
            .add("push-id", "feeds/mcudyrk2a4khkz") // Constant
            .add("referer", BASE_URL)
            .add("x-goog-upload-header-content-length", "")
            .add("x-goog-upload-protocol", "resumable")
            .add("x-tenant-id", "bard-storage")
            .build();

    private Constants() {
    }

}
