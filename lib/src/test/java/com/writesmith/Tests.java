package com.writesmith;

import appletransactionclient.exception.AppStoreErrorResponseException;
import com.dbclient.DBClient;
import com.oaigptconnector.model.*;
import com.oaigptconnector.model.exception.OpenAIGPTException;
import com.oaigptconnector.model.request.chat.completion.*;
import com.oaigptconnector.model.response.chat.completion.http.OAIGPTChatCompletionResponse;
import com.writesmith.core.service.endpoints.*;
import com.writesmith.core.service.request.*;
import com.writesmith.core.service.response.*;
import com.writesmith.exceptions.AutoIncrementingDBObjectExistsException;
import com.writesmith.exceptions.DBObjectNotFoundFromQueryException;
import com.writesmith.exceptions.PreparedStatementMissingArgumentException;
import com.writesmith.connectionpool.SQLConnectionPoolInstance;
import com.writesmith.apple.iapvalidation.AppleHttpVerifyReceipt;
import com.writesmith.database.dao.pooled.TransactionDAOPooled;
import com.writesmith.database.dao.pooled.User_AuthTokenDAOPooled;
import com.writesmith.keys.Keys;
import com.writesmith.database.model.AppStoreSubscriptionStatus;
import com.writesmith.database.model.objects.Transaction;
import com.writesmith.database.model.objects.User_AuthToken;
import com.writesmith.apple.iapvalidation.networking.itunes.exception.AppleItunesResponseException;
import com.writesmith.apple.iapvalidation.networking.itunes.request.verifyreceipt.VerifyReceiptRequest;
import com.writesmith.apple.iapvalidation.networking.itunes.response.verifyreceipt.VerifyReceiptResponse;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class Tests {

    private static final HttpClient httpClient = HttpClient.newBuilder().version(HttpClient.Version.HTTP_2).connectTimeout(Duration.ofMinutes(com.oaigptconnector.Constants.AI_TIMEOUT_MINUTES)).build(); // TODO: Is this fine to create here?

    private static String authTokenRandom = "PtTBwHP4/AzHbGXfuHMpdRkWVcGbG+S/uBwKwcKKhpyMN76QaxLc49QvEpnXXgMsSS7k0kYR4+AG3QQ8a7zchOosgTCC/k7kkl4mKXEMk0ciQm++k/Nlj2pGnSojtAKjNTfD0WgsmxXAQ6hcAy08MqlhEjYVCaF83C3yXRwZd+U=";

    @BeforeAll
    static void setUp() throws SQLException {
        SQLConnectionPoolInstance.create(Constants.MYSQL_URL, Keys.MYSQL_USER, Keys.MYSQL_PASS, 10);
    }

    @Test
    @DisplayName("Try creating a SELECT Prepared Statement")
    void testSelectPreparedStatement() throws InterruptedException, SQLException {
        Connection conn = SQLConnectionPoolInstance.getConnection();

        try {
            // Try complete Select PS
            ComponentizedPreparedStatement cps = SelectComponentizedPreparedStatementBuilder.forTable("Chat").select("chat_id").select("user_id").where("user_text", SQLOperators.EQUAL, 5).limit(5).orderBy(OrderByComponent.Direction.DESC, "date").build();

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
    void testInsertIntoPreparedStatement() throws InterruptedException, SQLException {
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
    void testUpdatePreparedStatement() throws InterruptedException, SQLException {
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
        OAIChatCompletionRequestMessage promptMessageRequest = new OAIChatCompletionRequestMessageBuilder(CompletionRole.USER)
                .addText("write me a short joke")
                .build();//new OAIChatCompletionRequestMessage(CompletionRole.USER, "write me a short joke");
        OAIChatCompletionRequest promptRequest = OAIChatCompletionRequest.build(
                "gpt-3.5-turbo",
                100,
                0.7,
                new OAIChatCompletionRequestResponseFormat(ResponseFormatType.TEXT),
                Arrays.asList(promptMessageRequest));
        Consumer<HttpRequest.Builder> c = requestBuilder -> {
            requestBuilder.setHeader("Authorization", "Bearer " + Keys.openAiAPI);
        };

        try {
            OAIGPTChatCompletionResponse response = OAIClient.postChatCompletion(promptRequest, Keys.openAiAPI, httpClient);
            System.out.println(response.getChoices()[0].getMessage().getContent());

        } catch (OpenAIGPTException e) {
            System.out.println(e.getErrorObject().getError().getMessage());
        } catch (IOException e) {
             throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

//    @Test
//    @DisplayName("Test Filling ChatWrapper")
//    void testFillChatWrapperIfAble() {
//        Integer userID = 32861;
//        String userText = "test";
//
//        try {
//            ChatLegacyWrapper chatWrapper = new ChatLegacyWrapper(userID, userText, LocalDateTime.now());
//
//            try {
//                OpenAIGPTChatWrapperFiller.fillChatWrapperIfAble(chatWrapper, true);
//            } catch (DBObjectNotFoundFromQueryException e) {
//                System.out.println("No receipt found for id " + userID);
//                // TODO: - Maybe test this more, add a receipt?
//            }
//
//            System.out.println("Remaining: " + chatWrapper.getDailyChatsRemaining());
//            System.out.println("AI Text: " + chatWrapper.getAiText());
//
//        } catch (SQLException e) {
//            throw new RuntimeException(e);
//        } catch (PreparedStatementMissingArgumentException e) {
//            throw new RuntimeException(e);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        } catch (OpenAIGPTException e) {
//            throw new RuntimeException(e);
//        } catch (CapReachedException e) {
//            throw new RuntimeException(e);
//        } catch (AppleItunesResponseException e) {
//            throw new RuntimeException(e);
//        } catch (DBSerializerException e) {
//            throw new RuntimeException(e);
//        } catch (IllegalAccessException e) {
//            throw new RuntimeException(e);
//        } catch (NoSuchMethodException e) {
//            throw new RuntimeException(e);
//        } catch (InstantiationException e) {
//            throw new RuntimeException(e);
//        } catch (InvocationTargetException e) {
//            throw new RuntimeException(e);
//        }
//    }

//    @Test
//    @DisplayName("Test Registering Transaction")
//    void testTransactionValidation() throws DBSerializerPrimaryKeyMissingException, DBSerializerException, SQLException, AutoIncrementingDBObjectExistsException, InterruptedException, InvocationTargetException, IllegalAccessException, AppStoreErrorResponseException, UnrecoverableKeyException, DBObjectNotFoundFromQueryException, CertificateException, IOException, URISyntaxException, KeyStoreException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchMethodException, InstantiationException, PreparedStatementMissingArgumentException, AppleItunesResponseException {
//        /* REGISTER TRANSACTION ENDPOINT */
//        // Input
//        final Long sampleTransactionId = 2000000406355171l;
//        // Expected Results
//        final AppStoreSubscriptionStatus expectedStatus = AppStoreSubscriptionStatus.EXPIRED;
//        final Boolean expectedIsPremiumValue1 = false;
//
//        // Register user
//        BodyResponse registerUserBR = RegisterUserEndpoint.registerUser();
//        AuthResponse aResponse = (AuthResponse)registerUserBR.getBody();
//
//        // Get authToken
//        String authToken = aResponse.getAuthToken();
//
//        // Create register transaction request
//        RegisterTransactionRequest rtr = new RegisterTransactionRequest(authToken, sampleTransactionId, null);
//
//        // Register transaction
//        BodyResponse registerTransactionBR = RegisterTransactionEndpoint.registerTransaction(rtr);
//        IsPremiumResponse ipr1 = (IsPremiumResponse)registerTransactionBR.getBody();
//
//        // Get User_AuthToken
//        User_AuthToken u_aT = User_AuthTokenDAOPooled.get(authToken);
//
//        // Verify transaction registered successfully
//        Transaction transaction = TransactionDAOPooled.getMostRecent(u_aT.getUserID());
//        assert(transaction != null);
//        System.out.println(transaction.getAppstoreTransactionID() + " " + sampleTransactionId);
//        assert(transaction.getAppstoreTransactionID().equals(sampleTransactionId));
////        assert(transaction.getStatus() == expectedStatus);
//
//        // Verify registered transaction successfully got isPremium value
////        assert(ipr1.getIsPremium() == expectedIsPremiumValue1);
//
//        /* IS PREMIUM ENDPOINT */
//        // Expected Results
//        final Boolean expectedIsPremiumValue2 = false;
//
//        // Create authRequest
//        AuthRequest aRequest = new AuthRequest(authToken);
//
//        // Get Is Premium from endpoint
//        BodyResponse isPremiumBR = GetIsPremiumEndpoint.getIsPremium(aRequest);
//        IsPremiumResponse ipr2 = (IsPremiumResponse)isPremiumBR.getBody();
//
//        // Verify results
////        assert(ipr2.getIsPremium() == expectedIsPremiumValue2);
//    }

    @Test
    @DisplayName("Test Submit Feedback Endpoint")
    void testSubmitFeedbackEndpoint() throws DBSerializerPrimaryKeyMissingException, DBSerializerException, SQLException, DBObjectNotFoundFromQueryException, InterruptedException, InvocationTargetException, IllegalAccessException, NoSuchMethodException, InstantiationException {
        final FeedbackRequest feedbackRequest = new FeedbackRequest(
                authTokenRandom,
                "This is the feedback"
        );

        StatusResponse sr = SubmitFeedbackEndpoint.submitFeedback(feedbackRequest);

        assert(sr != null);
    }

    @Test
    @DisplayName("Test Generate Suggestions Endpoint")
    void testGenerateSuggestionsEndpoint() {
        List<String> conversation = List.of(
                "Hi",
                "How are you?",
                "I'm good, how are you?",
                "Good! Do you have any questions?",
                "Yes, is the earth flat?",
                "No, the earth is not flat. It is a sphere!"
        );
        Integer count = 5;

        final GenerateSuggestionsRequest generateSuggestionsRequest = new GenerateSuggestionsRequest(
                authTokenRandom,
                conversation,
                new ArrayList<String>(),
                count
        );

        GenerateSuggestionsResponse generateSuggestionsResponse;
        try {
            generateSuggestionsResponse = GenerateSuggestionsEndpoint.generateSuggestions(generateSuggestionsRequest);
        } catch (DBSerializerException | SQLException | DBObjectNotFoundFromQueryException | InterruptedException |
                 InvocationTargetException | IllegalAccessException | NoSuchMethodException | InstantiationException |
                 OAISerializerException | OpenAIGPTException | JSONSchemaDeserializerException | IOException e) {
            throw new RuntimeException(e);
        }
// Generate a simple sentence that starts with a verb like "help" or "write" with suggestions on how to use ChatGPT for ideas on unique ways to use ChatGPT
        System.out.println(String.join(", ", generateSuggestionsResponse.getSuggestions()));

        assert(generateSuggestionsResponse.getSuggestions().size() > 0);
        assert(generateSuggestionsResponse.getSuggestions().size() >= count - 1);
        assert(generateSuggestionsResponse.getSuggestions().size() <= count + 1);
    }

    @Test
    @DisplayName("Test Generate Suggestions Endpoint With Different Than")
    void testGenerateSuggestionsEndpointWithDifferentThan() {
        List<String> conversation = List.of(
                "Hi",
                "How are you?",
                "I'm good, how are you?",
                "Good! Do you have any questions?",
                "Yes, is evolution real?",
                "Yes, evolution is real. Humans are animals that have evolved over many billions of years to finally create me!"
        );
        List<String> differentThan = List.of(
                "Did humans evolve from monkeys?",
                "Where did humans come from?",
                "How many evolutions were there until modern humans?"
        );
        Integer count = 5;

        final GenerateSuggestionsRequest generateSuggestionsRequest = new GenerateSuggestionsRequest(
                authTokenRandom,
                conversation,
                differentThan,
                count
        );

        GenerateSuggestionsResponse generateSuggestionsResponse;
        try {
            generateSuggestionsResponse = GenerateSuggestionsEndpoint.generateSuggestions(generateSuggestionsRequest);
        } catch (DBSerializerException | SQLException | DBObjectNotFoundFromQueryException | InterruptedException |
                 InvocationTargetException | IllegalAccessException | NoSuchMethodException | InstantiationException |
                 OAISerializerException | OpenAIGPTException | JSONSchemaDeserializerException | IOException e) {
            throw new RuntimeException(e);
        }

        System.out.println(String.join(", ", generateSuggestionsResponse.getSuggestions()));

        assert(generateSuggestionsResponse.getSuggestions().size() > 0);
        assert(generateSuggestionsResponse.getSuggestions().size() >= count - 1);
        assert(generateSuggestionsResponse.getSuggestions().size() <= count + 1);
    }

    @Test
    @DisplayName("Test Generate Image Endpoint")
    void testGenerateImageEndpoint() throws DBSerializerException, SQLException, OpenAIGPTException, DBObjectNotFoundFromQueryException, IOException, InterruptedException, InvocationTargetException, IllegalAccessException, NoSuchMethodException, InstantiationException {
        final String prompt = "A banana holding a bear";

        // Create GenerateImageRequest
        GenerateImageRequest giRequest = new GenerateImageRequest(
                authTokenRandom,
                prompt
        );

        // Get GenerateImageResponse from GenerateImageEndpoint
        GenerateImageResponse giResponse = GenerateImageEndpoint.generateImage(giRequest);

        System.out.println(giResponse.getImageData());

        // Print and test
        assert(giResponse != null);
        assert(giResponse.getImageData() != null);
    }

    @Test
    @DisplayName("Test WebSocket Logic")
    void testWebSocket() {
        // Register user
    }

//    @Test
//    @DisplayName("Test Create Recipe Idea Endpoint")
//    void testCreateRecipeIdeaEndpoint() throws DBSerializerPrimaryKeyMissingException, DBSerializerException, SQLException, OpenAIGPTException, DBObjectNotFoundFromQueryException, IOException, InterruptedException, InvocationTargetException, IllegalAccessException, NoSuchMethodException, InstantiationException, AutoIncrementingDBObjectExistsException {
//        // Register user
//        BodyResponse registerUserBR = RegisterUserEndpoint.registerUser();
//        AuthResponse aResponse = (AuthResponse)registerUserBR.getBody();
//
//        // Get authToken
//        String authToken = aResponse.getAuthToken();
//
//        // Create create recipe idea request
//        CreateRecipeIdeaRequest request = new CreateRecipeIdeaRequest(
//                authToken,
//                List.of("onions, potatoes, peas"),
//                List.of("salad"),
//                0
//        );
//
//        System.out.println("Request:\n" + new ObjectMapper().writeValueAsString(request));
//
//        BodyResponse br = CreateRecipeIdeaEndpoint.createRecipeIdea(request);
//
//        System.out.println("Response:\n" + new ObjectMapper().writeValueAsString(br));
//    }

    @Test
    @DisplayName("Misc Modifyable")
    void misc() {
//        System.out.println("Here it is: " + Table.USER_AUTHTOKEN);
    }
}
