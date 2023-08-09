package io.github.googlebardclient.model;

public class Conversation {

    private String at = "";
    private String bl = "";
    private String reqId = "0";

    private String conversationId = "";

    private String responseId = "";

    private String choiceId = "";

    public Conversation() {
        // No argument constructor
    }

    public String getAt() {
        return at;
    }

    public void setAt(String at) {
        this.at = at;
    }

    public String getBl() {
        return bl;
    }

    public void setBl(String bl) {
        this.bl = bl;
    }

    public String getReqId() {
        return reqId;
    }

    public void setReqId(String reqId) {
        this.reqId = reqId;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getResponseId() {
        return responseId;
    }

    public void setResponseId(String responseId) {
        this.responseId = responseId;
    }

    public String getChoiceId() {
        return choiceId;
    }

    public void setChoiceId(String choiceId) {
        this.choiceId = choiceId;
    }

    @Override
    public String toString() {
        return "Conversation{" +
                "at='" + at + '\'' +
                ", bl='" + bl + '\'' +
                ", reqId='" + reqId + '\'' +
                ", conversationId='" + conversationId + '\'' +
                ", responseId='" + responseId + '\'' +
                ", choiceId='" + choiceId + '\'' +
                '}';
    }
}