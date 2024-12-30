package com.writesmith;

import com.oaigptconnector.model.exception.OpenAIGPTException;
import com.writesmith.connectionpool.SQLConnectionPoolInstance;
import com.writesmith.core.service.endpoints.TranscribeSpeechEndpoint;
import com.writesmith.core.service.request.TranscribeSpeechRequest;
import com.writesmith.core.service.response.TranscribeSpeechResponse;
import com.writesmith.exceptions.DBObjectNotFoundFromQueryException;
import com.writesmith.exceptions.responsestatus.InvalidFileTypeException;
import com.writesmith.keys.Keys;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sqlcomponentizer.dbserializer.DBSerializerException;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;

public class TranscriptionTests {

    @BeforeAll
    static void setUp() throws SQLException {
        SQLConnectionPoolInstance.create(Constants.MYSQL_URL, Keys.MYSQL_USER, Keys.MYSQL_PASS, 10);
    }

    @Test
    @DisplayName("Transcription Test")
    void transcriptionTest() throws IOException, DBSerializerException, SQLException, OpenAIGPTException, DBObjectNotFoundFromQueryException, InterruptedException, InvocationTargetException, IllegalAccessException, NoSuchMethodException, InstantiationException, InvalidFileTypeException {
        TranscribeSpeechRequest request = new TranscribeSpeechRequest(
                "PtTBwHP4/AzHbGXfuHMpdRkWVcGbG+S/uBwKwcKKhpyMN76QaxLc49QvEpnXXgMsSS7k0kYR4+AG3QQ8a7zchOosgTCC/k7kkl4mKXEMk0ciQm++k/Nlj2pGnSojtAKjNTfD0WgsmxXAQ6hcAy08MqlhEjYVCaF83C3yXRwZd+U=",
                "FileName.m4a",
                Files.readAllBytes(Paths.get("/Users/alexcoundouriotis/IdeaProjects/WriteSmith-Server/lib/src/main/resources/voiceFile.m4a"))
        );

        TranscribeSpeechResponse response = TranscribeSpeechEndpoint.transcribeSpeech(request);

        System.out.println(response.getText());
    }


}
