package io.github.googlebardclient.model;

import java.util.List;

public class Result {
    private String content;
    private String conversationId;
    private String responseId;
    private String factualityQueries;
    private String textQuery;
    private List<Choice> choices;

    public Result() {
        // No argument constructor
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
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

    public String getFactualityQueries() {
        return factualityQueries;
    }

    public void setFactualityQueries(String factualityQueries) {
        this.factualityQueries = factualityQueries;
    }

    public String getTextQuery() {
        return textQuery;
    }

    public void setTextQuery(String textQuery) {
        this.textQuery = textQuery;
    }

    public List<Choice> getChoices() {
        return choices;
    }

    public void setChoices(List<Choice> choices) {
        this.choices = choices;
    }

    @Override
    public String toString() {
        return "Result{" +
                "content='" + content + '\'' +
                ", conversationId='" + conversationId + '\'' +
                ", responseId='" + responseId + '\'' +
                ", factualityQueries='" + factualityQueries + '\'' +
                ", textQuery='" + textQuery + '\'' +
                ", choices='" + choices + '\'' +
                '}';
    }
}
