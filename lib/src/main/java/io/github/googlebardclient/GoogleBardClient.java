package io.github.googlebardclient;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import io.github.googlebardclient.model.Choice;
import io.github.googlebardclient.model.Conversation;
import io.github.googlebardclient.model.Result;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GoogleBardClient {

    private static final String BASE_URL = "https://bard.google.com";
    private static final String CHAT_URL = "_/BardChatUi/data/assistant.lamda.BardFrontendService/StreamGenerate";

    private static final String UPLOAD_URL = "https://content-push.googleapis.com/upload/";
    private static final Random random = new Random();
    private static final Headers IMG_UPLOAD_HEADERS = new Headers.Builder()
            .add("authority", "content-push.googleapis.com")
            .add("accept", "*/*")
            .add("accept-language", "en-US,en;q=0.7")
            .add("authorization", "Basic c2F2ZXM6cyNMdGhlNmxzd2F2b0RsN3J1d1U=") // Constant Authorization Key
            .add("content-type", "application/x-www-form-urlencoded;charset=UTF-8")
            .add("origin", "https://bard.google.com")
            .add("push-id", "feeds/mcudyrk2a4khkz") // Constant
            .add("referer", "https://bard.google.com/")
            .add("x-goog-upload-header-content-length", "")
            .add("x-goog-upload-protocol", "resumable")
            .add("x-tenant-id", "bard-storage")
            .build();
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!-_";
    private static final int STRING_LENGTH = 1228;

    private final Logger log = LoggerFactory.getLogger(GoogleBardClient.class);
    private final String secure1psid;
    private final String secure1psidts;
    private final OkHttpClient httpClient;
    private final Gson gson = new Gson();
    private Conversation conversation = new Conversation();

    public GoogleBardClient(String secure1psid, String secure1psidts) {
        this(secure1psid, secure1psidts, Duration.ofMinutes(5));
    }

    public GoogleBardClient(String secure1psid, String secure1psidts, Duration timeout) {
        this.secure1psid = secure1psid;
        this.secure1psidts = secure1psidts;
        this.httpClient = new OkHttpClient.Builder()
                .callTimeout(timeout)
                .readTimeout(timeout)
                .connectTimeout(timeout)
                .build();
    }

    public void resetConversation() {
        conversation = new Conversation();
    }

    public Result chat(String prompt) {
        try {
            log.info("Start call Google Bard");
            if (conversation.getAt().isEmpty()) {
                getConversationData();
            }
            Headers headers = createHeaders();
            HttpUrl.Builder urlBuilder = Objects.requireNonNull(HttpUrl.parse(BASE_URL + "/" + CHAT_URL)).newBuilder()
                    .addQueryParameter("bl", conversation.getBl())
                    .addQueryParameter("rt", "c")
                    .addQueryParameter("_reqid", conversation.getReqId());
            String url = urlBuilder.build().toString();
            RequestBody body = createChatRequestBody(prompt);
            Request request = new Request.Builder()
                    .url(url)
                    .method("POST", body)
                    .headers(headers)
                    .build();
            Response response = httpClient.newCall(request).execute();
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
        catch (Exception ex) {
            log.warn("Processing failure", ex);
        }
        return null;
    }

    private void getConversationData() {
        log.info("Getting conversation data... ");
        try {
            Headers headers = createHeaders();
            Request request = new Request.Builder()
                    .url(BASE_URL)
                    .get()
                    .headers(headers)
                    .build();
            Response response = httpClient.newCall(request).execute();
            if (response.body() != null) {
                String responseStr = response.body().string();
                String at = findValueByKey(responseStr, "SNlM0e");
                String bl = findValueByKey(responseStr, "cfb2h");
                conversation.setAt(at);
                conversation.setBl(bl);
                conversation.setReqId(generateRandomNumber(4));
            }
        }
        catch (Exception ex) {
            log.warn("Get conversation data fail", ex);
        }
    }

    private Headers createHeaders() {
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

    private RequestBody createChatRequestBody(String prompt) {
        List<Object> messageStruct = Arrays.asList(
                Collections.singletonList(prompt),
                null,
                Arrays.asList(conversation.getConversationId(), conversation.getResponseId(), conversation.getChoiceId())
        );
        List<Object> fReqStruct = Arrays.asList(
                null,
                gson.toJson(messageStruct)
        );
        String fReq = gson.toJson(fReqStruct);
        log.info("f.req is: {}", fReq);
        return new FormBody.Builder()
                .add("f.req", fReq)
                .add("at", conversation.getAt())
                .build();
    }


    private static String findValueByKey(String rawData, String key) {
        String patternStr = "\"" + key + "\":\\s*\"(.*?)\"";
        Pattern pattern = Pattern.compile(patternStr);
        Matcher matcher = pattern.matcher(rawData);

        if (matcher.find()) {
            return matcher.group(1);
        }
        else {
            return null;
        }
    }

    private Result parseResult(String responseText) {
        String chatData = gson.fromJson(responseText.split("\n")[3], JsonArray.class)
                .get(0).getAsJsonArray()
                .get(2).getAsString();
        JsonArray jsonChatData = gson.fromJson(chatData, JsonArray.class);
        String content = jsonChatData.get(4).getAsJsonArray()
                .get(0).getAsJsonArray()
                .get(1).getAsJsonArray()
                .get(0).getAsString();
        String conversationId = jsonChatData.get(1).getAsJsonArray()
                .get(0).getAsString();
        String responseId = jsonChatData.get(1).getAsJsonArray()
                .get(1).getAsString();
        String factualityQueries = jsonChatData.get(3).isJsonNull() ? "" : jsonChatData.get(3).getAsString();
        String textQuery = jsonChatData.get(2).isJsonNull() ? "" :
                jsonChatData.get(2).getAsJsonArray()
                        .get(0).getAsJsonArray()
                        .get(0).getAsString();
        List<Choice> choices = new ArrayList<>();
        jsonChatData.get(4)
                .getAsJsonArray()
                .forEach(e -> {
                    Choice choice = new Choice();
                    choice.setId(e.getAsJsonArray().get(0).getAsString());
                    choice.setContent(e.getAsJsonArray().get(1).getAsString());
                    choices.add(choice);
                });

        List<String> images = new ArrayList<>();
        try {
            if (jsonChatData.size() >= 3) {
                if (jsonChatData.get(4).getAsJsonArray().get(0).getAsJsonArray().size() >= 4) {
                    if (jsonChatData.get(4).getAsJsonArray().get(0).getAsJsonArray().get(4).isJsonArray()) {
                        jsonChatData.get(4).getAsJsonArray().get(0).getAsJsonArray().get(4).getAsJsonArray()
                                .forEach(img -> images.add(
                                        img.getAsJsonArray()
                                                .get(0).getAsJsonArray()
                                                .get(0).getAsJsonArray()
                                                .get(0)
                                                .getAsString()
                                ));
                    }
                }
            }
        }
        catch (Exception ex) {
            log.warn("Can not parse image");
        }
        Result result = new Result();
        result.setContent(content);
        result.setConversationId(conversationId);
        result.setResponseId(responseId);
        result.setFactualityQueries(factualityQueries);
        result.setTextQuery(textQuery);
        result.setChoices(choices);
        result.setImages(images);
        return result;
    }

    private static String generateRandomNumber(int digits) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < digits; i++) {
            int randomDigit = random.nextInt(10);
            sb.append(randomDigit);
        }

        return sb.toString();
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

    public Result askAboutImage(byte[] image, String filename, String question, String lang) {
        try {
            log.info("Start call Google Bard");
            if (conversation.getAt().isEmpty()) {
                getConversationData();
            }
            String finalFileName = Objects.toString(filename, "uploaded_photo.jpg");
            String finalQuestion = Objects.toString(question, "what is in the image?");
            String finalLang = Objects.toString(lang, "en");
            String image_url = uploadImage(image, finalFileName);

            HttpUrl.Builder urlBuilder = Objects.requireNonNull(HttpUrl.parse(BASE_URL + "/" + CHAT_URL)).newBuilder()
                    .addQueryParameter("bl", conversation.getBl())
                    .addQueryParameter("rt", "c")
                    .addQueryParameter("_reqid", conversation.getReqId());

            Request request = new Request.Builder()
                    .url(urlBuilder.build())
                    .method("POST", createAskImageBody(image_url, finalFileName, finalQuestion, finalLang))
                    .headers(createHeaders())
                    .build();
            Response response = httpClient.newCall(request).execute();
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
        catch (Exception ex) {
            log.warn("Processing failure", ex);
        }
        return null;
    }

    private RequestBody createAskImageBody(String imageUrl, String filename, String question, String lang) {
        UUID uuid = UUID.randomUUID();
        String uuidString = uuid.toString().replace("-", ""); // Remove hyphens
        String uuid32Chars = uuidString.substring(0, 32); // Get the first 32 characters

        List<Object> image_arr = Collections.singletonList(Arrays.asList(Arrays.asList(imageUrl, 1), filename));
        List<Object> messageStruct = Arrays.asList(
                Arrays.asList(question, 0, null, image_arr, null, null, 0),
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
                gson.toJson(messageStruct)
        );
        String fReq = gson.toJson(fReqStruct);
        log.info("f.req is: {}", fReq);
        return new FormBody.Builder()
                .add("f.req", fReq)
                .add("at", conversation.getAt())
                .build();
    }

    private static String generateRandomString() {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(STRING_LENGTH);

        for (int i = 0; i < STRING_LENGTH; i++) {
            int randomIndex = random.nextInt(CHARACTERS.length());
            sb.append(CHARACTERS.charAt(randomIndex));
        }

        return sb.toString();
    }

}
