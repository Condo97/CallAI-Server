package com.writesmith.core.service.endpoints;

import com.writesmith.exceptions.AutoIncrementingDBObjectExistsException;
import com.writesmith.database.dao.factory.User_AuthTokenFactoryDAO;
import com.writesmith.core.service.response.factory.BodyResponseFactory;
import com.writesmith.database.model.objects.User_AuthToken;
import com.writesmith.core.service.response.AuthResponse;
import com.writesmith.core.service.response.BodyResponse;
import sqlcomponentizer.dbserializer.DBSerializerException;
import sqlcomponentizer.dbserializer.DBSerializerPrimaryKeyMissingException;

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class RegisterUserEndpoint {

    public static BodyResponse registerUser() throws DBSerializerPrimaryKeyMissingException, DBSerializerException, SQLException, AutoIncrementingDBObjectExistsException, InterruptedException, IllegalAccessException, InvocationTargetException {
        // Get AuthToken from Database by registering new user
        User_AuthToken u_aT = User_AuthTokenFactoryDAO.create();

        // Prepare and return new bodyResponse object
        AuthResponse registerUserResponse = new AuthResponse(u_aT.getAuthToken());

        // Print log
        printLog();

        return BodyResponseFactory.createSuccessBodyResponse(registerUserResponse);
    }


    private static void printLog() {
        StringBuilder sb = new StringBuilder();

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        Date date = new Date();

        sb.append("Registered User ");
        sb.append(sdf.format(date));

        System.out.println(sb);
    }

}
