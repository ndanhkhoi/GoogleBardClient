package io.github.googlebardclient.model;

public class Choice {
    private String id;
    private String content;

    public Choice() {
        // No argument constructor
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "Choice{" +
                "id='" + id + '\'' +
                ", content='" + content + '\'' +
                '}';
    }
}
