package com.writesmith;

import com.writesmith.connectionpool.SQLConnectionPoolInstance;
import com.writesmith.core.apple.iapvalidation.AppleHttpVerifyReceipt;
import com.writesmith.core.apple.iapvalidation.ReceiptUpdater;
import com.writesmith.core.apple.iapvalidation.ReceiptValidator;
import com.writesmith.model.database.objects.Receipt;
import com.writesmith.database.managers.ReceiptDBManager;
import com.writesmith.common.exceptions.AutoIncrementingDBObjectExistsException;
import com.writesmith.common.exceptions.CapReachedException;
import com.writesmith.common.exceptions.DBObjectNotFoundFromQueryException;
import com.writesmith.deprecated.helpers.chatfiller.ChatLegacyWrapper;
import com.writesmith.deprecated.helpers.chatfiller.OpenAIGPTChatWrapperFiller;
import com.writesmith.model.http.client.apple.itunes.exception.AppleItunesResponseException;
import com.writesmith.model.http.client.apple.itunes.request.verifyreceipt.VerifyReceiptRequest;
import com.writesmith.model.http.client.apple.itunes.response.verifyreceipt.VerifyReceiptResponse;
import com.writesmith.model.http.client.openaigpt.Role;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import com.writesmith.common.exceptions.PreparedStatementMissingArgumentException;
import com.writesmith.keys.Keys;
import com.writesmith.core.generation.openai.OpenAIGPTHttpsClientHelper;
import com.writesmith.model.http.client.openaigpt.exception.OpenAIGPTException;
import com.writesmith.model.http.client.openaigpt.request.prompt.OpenAIGPTChatCompletionMessageRequest;
import com.writesmith.model.http.client.openaigpt.request.prompt.OpenAIGPTChatCompletionRequest;
import com.writesmith.model.http.client.openaigpt.response.prompt.OpenAIGPTChatCompletionResponse;
import sqlcomponentizer.DBClient;
import sqlcomponentizer.dbserializer.DBSerializerException;
import sqlcomponentizer.dbserializer.DBSerializerPrimaryKeyMissingException;
import sqlcomponentizer.preparedstatement.ComponentizedPreparedStatement;
import sqlcomponentizer.preparedstatement.component.OrderByComponent;
import sqlcomponentizer.preparedstatement.component.condition.SQLOperators;
import sqlcomponentizer.preparedstatement.statement.InsertIntoComponentizedPreparedStatementBuilder;
import sqlcomponentizer.preparedstatement.statement.SelectComponentizedPreparedStatementBuilder;
import sqlcomponentizer.preparedstatement.statement.UpdateComponentizedPreparedStatementBuilder;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.sql.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class Tests {

    @BeforeAll
    static void setUp() throws SQLException {
        SQLConnectionPoolInstance.create(Constants.MYSQL_URL, Keys.MYSQL_USER, Keys.MYSQL_PASS, 10);
    }

    @Test
    @DisplayName("Try creating a SELECT Prepared Statement")
    void testSelectPreparedStatement() throws InterruptedException {
        Connection conn = SQLConnectionPoolInstance.getConnection();

        try {
            // Try complete Select PS
            ComponentizedPreparedStatement cps = SelectComponentizedPreparedStatementBuilder.forTable("Chat").select("chat_id").select("user_id").where("user_text", SQLOperators.EQUAL, 5).limit(5).orderBy(OrderByComponent.Direction.DESC, "date").build();

//            selectPSComplete.addScope("chat_id");
//            selectPSComplete.addScope("user_id");
//
//            selectPSComplete.addWhere("user_text", 5);
//            selectPSComplete.setLimit(5);
//
//            selectPSComplete.addOrderByColumn("date");
//            selectPSComplete.setOrder(SelectPreparedStatement.Order.DESC);
            PreparedStatement cpsPS = cps.connect(conn);
            System.out.println(cpsPS.toString());
            cpsPS.close();

            // Try minimal Select PS
            ComponentizedPreparedStatement selectCPSMinimal = SelectComponentizedPreparedStatementBuilder.forTable("Chat").build();

            PreparedStatement selectCPSMinimalPS = selectCPSMinimal.connect(conn);
            System.out.println(selectCPSMinimalPS.toString());
            selectCPSMinimalPS.close();

            // Try partial Select PS
            ComponentizedPreparedStatement selectCPSPartial = SelectComponentizedPreparedStatementBuilder.forTable("Chat").select("chat_id").where("user_text", SQLOperators.EQUAL, false).build();

            PreparedStatement selectCPSPartialPS = selectCPSPartial.connect(conn);
            System.out.println(selectCPSPartialPS.toString());
            selectCPSPartialPS.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            SQLConnectionPoolInstance.releaseConnection(conn);
        }
    }

    @Test
    @DisplayName("Try creating an INSERT INTO Prepared Statement")
    void testInsertIntoPreparedStatement() throws InterruptedException {
        Connection conn = SQLConnectionPoolInstance.getConnection();

        try {
            // Build the insert componentized statement
            ComponentizedPreparedStatement insertCPSComplete = InsertIntoComponentizedPreparedStatementBuilder.forTable("Chat").addColAndVal("chat_id", Types.NULL).addColAndVal("user_id", 5).addColAndVal("user_text", "hi").addColAndVal("ai_text", "hello").addColAndVal("date", LocalDateTime.now()).build(true);

            System.out.println(insertCPSComplete);

            // Do update and get generated keys
            List<Map<String, Object>> generatedKeys = DBClient.updateReturnGeneratedKeys(conn, insertCPSComplete);

            System.out.println(generatedKeys);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            SQLConnectionPoolInstance.releaseConnection(conn);
        }
    }

    @Test
    @DisplayName("Try creating an UPDATE Prepared Statement")
    void testUpdatePreparedStatement() throws InterruptedException {
        Connection conn = SQLConnectionPoolInstance.getConnection();

        try {
            ComponentizedPreparedStatement updatePSComplete = UpdateComponentizedPreparedStatementBuilder.forTable("Chat").set("user_text", "wow!").set("date", LocalDateTime.now()).where("user_id", SQLOperators.EQUAL, 5).where("chat_id", SQLOperators.EQUAL, 65842).build();

            DBClient.update(conn, updatePSComplete);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            SQLConnectionPoolInstance.releaseConnection(conn);
        }
    }

    @Test
    @DisplayName("HttpHelper Testing")
    void testBasicHttpRequest() {
        HttpClient client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).connectTimeout(Duration.ofMinutes(Constants.AI_TIMEOUT_MINUTES)).build();
//        GenerateChatRequest gcr = new GenerateChatRequest(Constants.Model_Name, "prompt", Constants.Temperature, Constants.Token_Limit_Paid);
        OpenAIGPTChatCompletionMessageRequest promptMessageRequest = new OpenAIGPTChatCompletionMessageRequest(Role.USER, "write me a short joke");
        OpenAIGPTChatCompletionRequest promptRequest = new OpenAIGPTChatCompletionRequest("gpt-3.5-turbo", 100, 0.7, Arrays.asList(promptMessageRequest));
        Consumer<HttpRequest.Builder> c = requestBuilder -> {
            requestBuilder.setHeader("Authorization", "Bearer " + Keys.openAiAPI);
        };

        OpenAIGPTHttpsClientHelper httpHelper = new OpenAIGPTHttpsClientHelper();

        try {
            OpenAIGPTChatCompletionResponse response = httpHelper.postChatCompletion(promptRequest);
            System.out.println(response.getChoices()[0].getMessage().getContent());

        } catch (OpenAIGPTException e) {
            System.out.println(e.getErrorObject().getError().getMessage());
        } catch (IOException e) {
             throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

    @Test
    @DisplayName("VerifyReceipt Testing")
    void testVerifyReceipt() {
        Integer userID = 32828;
//        DBEntity db = null;
        try {
            try {
                Receipt r = ReceiptDBManager.getMostRecentReceiptFromDB
                        (userID);

                VerifyReceiptRequest request = new VerifyReceiptRequest(r.getReceiptData(), Keys.sharedAppSecret, "false");

                VerifyReceiptResponse response = new AppleHttpVerifyReceipt().getVerifyReceiptResponse(request);

                System.out.println(response.getPending_renewal_info().get(0).getExpiration_intent());
            } catch (DBObjectNotFoundFromQueryException e) {
                System.out.println("Receipt not found when getting the most recent receipt... Could this be because there are no receipts in the database?");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (AppleItunesResponseException e) {
            throw new RuntimeException(e);
        } catch (DBSerializerException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("Test receipt validation")
    void testReceiptValidation() {
        Integer userID = 32830;
//        DBEntity db = null;
        try {
            try {
                // Ensure that the date is the same, even after getting twice
                Receipt r = ReceiptDBManager.getMostRecentReceiptFromDB(userID);
                LocalDateTime initialCheckDate = r.getCheckDate();
                r = ReceiptDBManager.getMostRecentReceiptFromDB(userID);
                LocalDateTime secondCheckDate = r.getCheckDate();

                assert (initialCheckDate.isEqual(secondCheckDate));

                // Ensure that the date is later after validating
                r = ReceiptDBManager.getMostRecentReceiptFromDB(userID);
                initialCheckDate = r.getCheckDate();
                ReceiptValidator.validateReceipt(r);
                secondCheckDate = r.getCheckDate();

                System.out.println(ChronoUnit.MILLIS.between(secondCheckDate, initialCheckDate));

                assert (secondCheckDate.isAfter(initialCheckDate));

                // Ensure that the date is later after updating
                r = ReceiptDBManager.getMostRecentReceiptFromDB(userID);
                initialCheckDate = r.getCheckDate();
                Thread.sleep(1000);
                ReceiptUpdater.updateIfNeeded(r);
                secondCheckDate = r.getCheckDate();

                assert (secondCheckDate.isAfter(initialCheckDate));
            } catch (DBObjectNotFoundFromQueryException e) {
                System.out.println("Receipt not found in \"Test receipt validation\" the most recent receipt... Could this be because there are no receipts in the database?");
            }


        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (PreparedStatementMissingArgumentException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (AppleItunesResponseException e) {
            throw new RuntimeException(e);
        } catch (DBSerializerPrimaryKeyMissingException e) {
            throw new RuntimeException(e);
        } catch (DBSerializerException e) {
            throw new RuntimeException(e);
        } catch (AutoIncrementingDBObjectExistsException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("Test Filling ChatWrapper")
    void testFillChatWrapperIfAble() {
        Integer userID = 32861;
        String userText = "test";

        try {
            ChatLegacyWrapper chatWrapper = new ChatLegacyWrapper(userID, userText, LocalDateTime.now());

            try {
                OpenAIGPTChatWrapperFiller.fillChatWrapperIfAble(chatWrapper, true);
            } catch (DBObjectNotFoundFromQueryException e) {
                System.out.println("No receipt found for id " + userID);
                // TODO: - Maybe test this more, add a receipt?
            }

            System.out.println("Remaining: " + chatWrapper.getDailyChatsRemaining());
            System.out.println("AI Text: " + chatWrapper.getAiText());

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (PreparedStatementMissingArgumentException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (OpenAIGPTException e) {
            throw new RuntimeException(e);
        } catch (CapReachedException e) {
            throw new RuntimeException(e);
        } catch (AppleItunesResponseException e) {
            throw new RuntimeException(e);
        } catch (DBSerializerException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("Misc Modifyable")
    void misc() {
//        System.out.println("Here it is: " + Table.USER_AUTHTOKEN);
    }
}
