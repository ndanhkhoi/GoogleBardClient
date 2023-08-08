package io.github.googlebardclient;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import io.github.googlebardclient.model.Conversation;
import io.github.googlebardclient.model.ResponseData;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.commons.text.StringEscapeUtils;

import java.io.IOException;
import java.time.Duration;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class GoogleBardClient {

    private static final String BASE_URL = "https://bard.google.com";
    private static final String CHAT_URL = "_/BardChatUi/data/assistant.lamda.BardFrontendService/StreamGenerate";
    private final String token;
    private final OkHttpClient httpClient;
    private final Gson gson = new Gson();
    private Conversation conversation = new Conversation();

    public GoogleBardClient(String token) {
        this(token, Duration.ofMinutes(5));
    }

    public GoogleBardClient(String token, Duration timeout) {
        this.token = token;
        this.httpClient = new OkHttpClient.Builder()
                .callTimeout(timeout)
                .readTimeout(timeout)
                .connectTimeout(timeout)
                .build();
    }

    public void resetConversation() {
        conversation = new Conversation();
    }

    public String chat(String prompt) {
        try {
            if (conversation.getAt().isEmpty()) {
                getConversationData();
            }
            Headers okhttpHeaders = Headers.of(createHeaders(token));
            HttpUrl.Builder urlBuilder = HttpUrl.parse(BASE_URL + "/" + CHAT_URL).newBuilder()
                    .addQueryParameter("bl", conversation.getBl())
                    .addQueryParameter("rt", "c")
                    .addQueryParameter("_reqid", "0");
            String url = urlBuilder.build().toString();
            List<String> fReqData = new ArrayList<>();
            fReqData.add(null);
            fReqData.add(
                    String.format("[[%s],null,%s]",
                            "\"" + StringEscapeUtils.escapeJson(prompt) + "\"",
                            gson.toJson(Arrays.asList(conversation.getC(), conversation.getR(), conversation.getRc()))
                    ));
            String fReq = gson.toJson(fReqData);
            RequestBody body = new FormBody.Builder()
                    .add("f.req", fReq)
                    .add("at", conversation.getAt())
                    .build();
            Request request = new Request.Builder()
                    .url(url)
                    .method("POST", body)
                    .headers(okhttpHeaders)
                    .build();
            Response response = httpClient.newCall(request).execute();
            if (response.body() != null) {
                ResponseData res = parseResponse(response.body().string());
                conversation.setC(res.getC());
                conversation.setR(res.getR());
                conversation.setRc(res.getRc());
                return res.getResponses().get(6);
            }
        }
        catch (Exception ex) {
            log.warn("Failed to call Google Bard", ex);
        }
        return "No answer";
    }

    private void getConversationData() throws IOException {
        Headers okhttpHeaders = Headers.of(createHeaders(token));
        Request request = new Request.Builder()
                .url(BASE_URL)
                .get()
                .headers(okhttpHeaders)
                .build();
        Response response = httpClient.newCall(request).execute();
        if (response.body() != null) {
            String responseStr = response.body().string();
            String at = findValueByKey(responseStr, "SNlM0e");
            String bl = findValueByKey(responseStr, "cfb2h");
            conversation.setAt(at);
            conversation.setBl(bl);
        }
    }

    private static String findValueByKey(String rawData, String key) {
        String patternStr = "\"" + key + "\":\\s*\"(.*?)\"";
        Pattern pattern = Pattern.compile(patternStr);
        Matcher matcher = pattern.matcher(rawData);

        if (matcher.find()) {
            return matcher.group(1);
        } else {
            return null;
        }
    }

    private static Map<String, String> createHeaders(String token) {
        Map<String, String> headers = new HashMap<>();

        headers.put("Host", "bard.google.com");
        headers.put("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
        headers.put("X-Same-Domain", "1");
        headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.114 Safari/537.36");
        headers.put("Origin", "https://bard.google.com/");
        headers.put("Referer", "https://bard.google.com/");
        headers.put("Cookie", "__Secure-1PSID=" + token);

        return headers;
    }

    private void parseData(ResponseData resData, JsonElement data) {
        if (data != null && data.isJsonPrimitive()) {
            String dataStr = data.getAsString();
            if (dataStr.startsWith("c_")) {
                resData.setC(dataStr);
                return;
            }
            if (dataStr.startsWith("r_")) {
                resData.setR(dataStr);
                return;
            }
            if (dataStr.startsWith("rc_")) {
                resData.setRc(dataStr);
                return;
            }
            resData.getResponses().add(dataStr);
        }
        if (data != null && data.isJsonArray()) {
            ((JsonArray) data).forEach((e) -> parseData(resData, e));
        }
    }

    private ResponseData parseResponse(String text) {
        ResponseData resData = new ResponseData();
        String[] lines = text.split("\n");
        for (String line : lines) {
            if (line.contains("wrb.fr")) {
                JsonArray data = gson.fromJson(line, JsonArray.class);
                String responsesData = ((JsonArray) data.get(0)).get(2).getAsString();
                JsonArray responsesDataArr = gson.fromJson(responsesData, JsonArray.class);
                responsesDataArr.forEach(e -> parseData(resData, e));
            }
        }
        return resData;
    }

}
