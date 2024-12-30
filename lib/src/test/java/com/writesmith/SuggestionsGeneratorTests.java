package com.writesmith;

import com.oaigptconnector.model.JSONSchemaDeserializerException;
import com.oaigptconnector.model.OAISerializerException;
import com.oaigptconnector.model.exception.OpenAIGPTException;
import com.writesmith.core.service.generators.SuggestionsGenerator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SuggestionsGeneratorTests {

    private static String authTokenRandom = "PtTBwHP4/AzHbGXfuHMpdRkWVcGbG+S/uBwKwcKKhpyMN76QaxLc49QvEpnXXgMsSS7k0kYR4+AG3QQ8a7zchOosgTCC/k7kkl4mKXEMk0ciQm++k/Nlj2pGnSojtAKjNTfD0WgsmxXAQ6hcAy08MqlhEjYVCaF83C3yXRwZd+U=";

    @BeforeAll
    static void setup() {

    }

    @Test
    @DisplayName("Test Generate Full")
    void testGenerateFull() {
        final Integer count = 1;
        final String conversation = "This is a test conversation about elephants. They are mammals and large and stuff!";
        final List<String> differentThan = List.of(
                "Their size is large.",
                "Find out about their trunks"
        );

        List<SuggestionsGenerator.Suggestion> suggestions;
        try {
            suggestions = SuggestionsGenerator.generateSuggestions(1, conversation, differentThan);

        } catch (OAISerializerException | OpenAIGPTException | IOException | InterruptedException | JSONSchemaDeserializerException e) {
            throw new RuntimeException(e);
        }

        suggestions.forEach(System.out::println);
    }

    @Test
    @DisplayName("Test Generate Nulls")
    void testGenerateNulls() {
        final Integer count = null;
        final String conversation = "Conversation should never be null"; // Is it acceptable for the program to trigger a null pointer exception in this case
        final List<String> differentThan = null;

        List<SuggestionsGenerator.Suggestion> suggestions;
        try {
            suggestions = SuggestionsGenerator.generateSuggestions(1, conversation, differentThan);

        } catch (OAISerializerException | OpenAIGPTException | IOException | InterruptedException | JSONSchemaDeserializerException e) {
            throw new RuntimeException(e);
        }

        suggestions.forEach(System.out::println);
    }

    @Test
    @DisplayName("Test Generate All Empty")
    void testGenerateAllEmpty() {
        final Integer count = 1;
        final String conversation = ""; // Is it acceptable for the program to trigger a null pointer exception in this case
        final List<String> differentThan = new ArrayList<>();

        List<SuggestionsGenerator.Suggestion> suggestions;
        try {
            suggestions = SuggestionsGenerator.generateSuggestions(1, conversation, differentThan);

        } catch (OAISerializerException | OpenAIGPTException | IOException | InterruptedException | JSONSchemaDeserializerException e) {
            throw new RuntimeException(e);
        }

        suggestions.forEach(System.out::println);
    }

    @Test
    @DisplayName("Test Generate Empty but Conversation")
    void testGenerateAllEmptyButConversation() {
        final Integer count = 1;
        final String conversation = "This is a conversation about ants. They are tiny and teeny and little."; // Is it acceptable for the program to trigger a null pointer exception in this case
        final List<String> differentThan = new ArrayList<>();

        List<SuggestionsGenerator.Suggestion> suggestions;
        try {
            suggestions = SuggestionsGenerator.generateSuggestions(1, conversation, differentThan);

        } catch (OAISerializerException | OpenAIGPTException | IOException | InterruptedException | JSONSchemaDeserializerException e) {
            throw new RuntimeException(e);
        }

        suggestions.forEach(System.out::println);
    }

    @Test
    @DisplayName("Test Generate Zero Count")
    void testGenerateZeroCount() {
        final Integer count = 0;
        final String conversation = "This is a conversation on bees. They are sharp but very sweet and loveable and make happy fun goopy gloop."; // Is it acceptable for the program to trigger a null pointer exception in this case
        final List<String> differentThan = new ArrayList<>();

        List<SuggestionsGenerator.Suggestion> suggestions;
        try {
            suggestions = SuggestionsGenerator.generateSuggestions(1, conversation, differentThan);

        } catch (OAISerializerException | OpenAIGPTException | IOException | InterruptedException | JSONSchemaDeserializerException e) {
            throw new RuntimeException(e);
        }

        suggestions.forEach(System.out::println);
    }

}
