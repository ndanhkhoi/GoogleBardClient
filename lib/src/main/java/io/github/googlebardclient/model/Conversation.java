package io.github.googlebardclient.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Conversation {

    private String at = "";
    private String bl = "";

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
}
