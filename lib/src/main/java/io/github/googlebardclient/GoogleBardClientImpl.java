package io.github.googlebardclient;

import io.github.googlebardclient.model.Conversation;
import io.github.googlebardclient.model.Result;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.util.*;

import static io.github.googlebardclient.Constants.*;
import static io.github.googlebardclient.Helper.*;

class GoogleBardClientImpl implements GoogleBardClient {

    private final Logger log = LoggerFactory.getLogger(GoogleBardClientImpl.class);
    private final String secure1psid;
    private final String secure1psidts;
    private final OkHttpClient httpClient;
    private Conversation conversation = new Conversation();

    GoogleBardClientImpl(String secure1psid, String secure1psidts) {
        this(secure1psid, secure1psidts, Duration.ofMinutes(5));
    }

    GoogleBardClientImpl(String secure1psid, String secure1psidts, Duration timeout) {
        this.secure1psid = secure1psid;
        this.secure1psidts = secure1psidts;
        this.httpClient = new OkHttpClient.Builder()
                .callTimeout(timeout)
                .readTimeout(timeout)
                .connectTimeout(timeout)
                .build();
    }

    private Headers createChatHeaders() {
        return new Headers.Builder()
                .add("Host", "bard.google.com")
                .add("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8")
                .add("X-Same-Domain", "1")
                .add("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.114 Safari/537.36")
                .add("Origin", "https://bard.google.com/")
                .add("Referer", "https://bard.google.com/")
                .add("Cookie", String.format("__Secure-1PSID=%s;__Secure-1PSIDTS=%s", secure1psid, secure1psidts))
                .build();
    }

    private RequestBody createChatRequestBody(String imageUrl, String filename, String question, String lang) {
        UUID uuid = UUID.randomUUID();
        String uuidString = uuid.toString().replace("-", ""); // Remove hyphens
        String uuid32Chars = uuidString.substring(0, 32); // Get the first 32 characters

        List<Object> imageArr = imageUrl == null ?
                new ArrayList<>() :
                Collections.singletonList(Arrays.asList(Arrays.asList(imageUrl, 1), filename));
        List<Object> messageStruct = Arrays.asList(
                Arrays.asList(question, 0, null, imageArr, null, null, 0),
                Collections.singletonList(lang),
                Arrays.asList(conversation.getConversationId(), conversation.getResponseId(), conversation.getChoiceId()),
                generateRandomString(),   // Unknown random string value (1000 characters +) - If needed, can replace with a random string generator
                uuid32Chars,   //  Random uuidv4 (32 characters)
                null,
                Collections.singletonList(0),
                0,
                null,
                null,
                1,
                0
        );
        List<Object> fReqStruct = Arrays.asList(
                null,
                toJson(messageStruct)
        );
        String fReq = toJson(fReqStruct);
        log.info("f.req is: {}", fReq);
        return new FormBody.Builder()
                .add("f.req", fReq)
                .add("at", conversation.getAt())
                .build();
    }

    private Result call(Request request) throws IOException {
        try (Response response = httpClient.newCall(request).execute()) {
            if (response.code() != 200) {
                log.warn("Failed to call Google Bard. Status code is: {}", response.code());
            }
            else if (response.body() != null) {
                log.info("Call successfully. Parsing data...");
                Result result = parseResult(response.body().string());
                conversation.setConversationId(result.getConversationId());
                conversation.setResponseId(result.getResponseId());
                conversation.setChoiceId(result.getChoices().get(0).getId());
                conversation.setReqId(String.valueOf(Integer.parseInt(conversation.getReqId()) + 1000));
                log.info("Done! Process successfully");
                return result;
            }
        }
        return null;
    }

    private void getConversationData() {
        log.info("Getting conversation data... ");
        try {
            Headers headers = createChatHeaders();
            Request request = new Request.Builder()
                    .url(BASE_URL)
                    .get()
                    .headers(headers)
                    .build();
            try (Response response = httpClient.newCall(request).execute()) {
                if (response.body() != null) {
                    String responseStr = response.body().string();
                    String at = findValueByKey(responseStr, "SNlM0e");
                    String bl = findValueByKey(responseStr, "cfb2h");
                    conversation.setAt(at);
                    conversation.setBl(bl);
                    conversation.setReqId(generateRandomNumber(4));
                }
            }
        }
        catch (Exception ex) {
            log.warn("Get conversation data fail", ex);
        }
    }

    private String uploadImage(byte[] image, String filename) {
        try {
            Request request;
            Response response;

            request = new Request.Builder()
                    .url(UPLOAD_URL)
                    .method("OPTIONS", null)
                    .build();
            httpClient.newCall(request).execute();

            request = new Request.Builder()
                    .url(UPLOAD_URL)
                    .headers(IMG_UPLOAD_HEADERS)
                    .addHeader("size", image.length + "")
                    .addHeader("x-goog-upload-command", "start")
                    .method("POST", new FormBody.Builder().add("File name", filename).build())
                    .build();
            response = httpClient.newCall(request).execute();
            String uploadUrl = response.header("X-Goog-Upload-Url");

            request = new Request.Builder()
                    .url(uploadUrl)
                    .method("OPTIONS", null)
                    .build();
            httpClient.newCall(request).execute();

            request = new Request.Builder()
                    .url(uploadUrl)
                    .headers(IMG_UPLOAD_HEADERS)
                    .addHeader("size", image.length + "")
                    .addHeader("x-goog-upload-command", "upload, finalize")
                    .addHeader("X-Goog-Upload-Offset", "0")
                    .method("POST", RequestBody.create(image, MediaType.parse("application/octet-stream")))
                    .build();
            response = httpClient.newCall(request).execute();
            return response.body().string();
        }
        catch (Exception ex) {
            log.warn("Upload failure", ex);
        }
        return null;
    }

    @Override
    public void resetConversation() {
        conversation = new Conversation();
    }

    @Override
    public Result chat(String prompt) {
        return chat(prompt, "en");
    }

    @Override
    public Result chat(String prompt, String lang) {
        String finalLang = Objects.toString(lang, "en");
        try {
            log.info("Start call Google Bard");
            if (conversation.getAt().isEmpty()) {
                getConversationData();
            }
            Headers headers = createChatHeaders();
            HttpUrl.Builder urlBuilder = Objects.requireNonNull(HttpUrl.parse(BASE_URL + "/" + CHAT_URL)).newBuilder()
                    .addQueryParameter("bl", conversation.getBl())
                    .addQueryParameter("rt", "c")
                    .addQueryParameter("_reqid", conversation.getReqId());
            String url = urlBuilder.build().toString();
            RequestBody body = createChatRequestBody(null, null, prompt, finalLang);
            Request request = new Request.Builder()
                    .url(url)
                    .method("POST", body)
                    .headers(headers)
                    .build();
            return call(request);
        }
        catch (Exception ex) {
            log.warn("Processing failure", ex);
        }
        return null;
    }

    public Result askAboutImage(byte[] image, String filename, String question, String lang) {
        try {
            log.info("Start call Google Bard");
            if (conversation.getAt().isEmpty()) {
                getConversationData();
            }
            String finalFileName = Objects.toString(filename, "uploaded_photo.jpg");
            String finalQuestion = Objects.toString(question, "what is in the image?");
            String finalLang = Objects.toString(lang, "en");
            String imageUrl = uploadImage(image, finalFileName);

            Headers headers = createChatHeaders();
            HttpUrl.Builder urlBuilder = Objects.requireNonNull(HttpUrl.parse(BASE_URL + "/" + CHAT_URL)).newBuilder()
                    .addQueryParameter("bl", conversation.getBl())
                    .addQueryParameter("rt", "c")
                    .addQueryParameter("_reqid", conversation.getReqId());
            RequestBody body = createChatRequestBody(imageUrl, finalFileName, finalQuestion, finalLang);
            Request request = new Request.Builder()
                    .url(urlBuilder.build())
                    .method("POST", body)
                    .headers(headers)
                    .build();
            return call(request);
        }
        catch (Exception ex) {
            log.warn("Processing failure", ex);
        }
        return null;
    }

}
