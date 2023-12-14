package io.github.googlebardclient;

import io.github.googlebardclient.model.Result;

public interface GoogleBardClient {

    void resetConversation();

    Result chat(String prompt);

    Result chat(String prompt, String lang);

    Result askAboutImage(byte[] image, String filename, String question, String lang);

}
