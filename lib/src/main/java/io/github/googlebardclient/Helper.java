package io.github.googlebardclient;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import io.github.googlebardclient.model.Choice;
import io.github.googlebardclient.model.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class Helper {

    private static final Logger log = LoggerFactory.getLogger(Helper.class);
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!-_";
    private static final int STRING_LENGTH = 1228;
    private static final SecureRandom random = new SecureRandom();
    private static final Gson gson = new Gson();

    private Helper() {
    }

    static String generateRandomString() {
        StringBuilder sb = new StringBuilder(STRING_LENGTH);

        for (int i = 0; i < STRING_LENGTH; i++) {
            int randomIndex = random.nextInt(CHARACTERS.length());
            sb.append(CHARACTERS.charAt(randomIndex));
        }

        return sb.toString();
    }

    static String generateRandomNumber(int digits) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < digits; i++) {
            int randomDigit = random.nextInt(10);
            sb.append(randomDigit);
        }

        return sb.toString();
    }

    static String random32CharsUUIDv4() {
        UUID uuid = UUID.randomUUID();
        String uuidString = uuid.toString().replace("-", ""); // Remove hyphens
        return uuidString.substring(0, 32);
    }

    static String findValueByKey(String rawData, String key) {
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

    static <T> String toJson(T object) {
        return gson.toJson(object);
    }

    static Result parseResult(String responseText) {
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
            if (jsonChatData.size() >= 3
                    && jsonChatData.get(4).getAsJsonArray().get(0).getAsJsonArray().size() >= 4
                    && jsonChatData.get(4).getAsJsonArray().get(0).getAsJsonArray().get(4).isJsonArray()) {
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

}
