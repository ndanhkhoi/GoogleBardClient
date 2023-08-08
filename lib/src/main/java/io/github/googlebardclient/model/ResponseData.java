package io.github.googlebardclient.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class ResponseData {
    /**
     * conversationId
     * */
    private String c = "";

    /**
     * requestId
     * */
    private String r = "";

    /**
     * responseId
     * */
    private String rc = "";

    private List<String> responses = new ArrayList<>();
}
