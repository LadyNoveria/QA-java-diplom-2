import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import user.UserCreds;
import user.UserRequest;
import user.UserResponse;
import org.junit.Test;

import static org.apache.http.HttpStatus.SC_FORBIDDEN;
import static org.apache.http.HttpStatus.SC_OK;
import static org.junit.Assert.*;

public class UserCreationTests {

    private UserClient userClient;
    private UserRequest userRequest;
    private UserResponse userCreationResponse;

    @Before
    public void setUp() {
        userRequest = UserRequest.userGenerator();
        userClient = new UserClient();
    }

    @Test
    @DisplayName("200 ОК: create unique user")
    public void successfulCreatingUserUnique() {
        Response response = userClient.userCreate(userRequest);
        userCreationResponse = response
                .body()
                .as(UserResponse.class);
        response.then().assertThat().statusCode(SC_OK);
        assertTrue(userCreationResponse.isSuccess());
        assertTrue(userClient.userLogin(userRequest)
                .body()
                .as(UserResponse.class)
                .isSuccess());
    }

    @Test
    @DisplayName("403 FORBIDDEN: create not unique user")
    public void errorCreatingUserAlreadyExists() {
        userCreationResponse = userClient.userCreate(userRequest)
                .body()
                .as(UserResponse.class);
        Response response = userClient.userCreate(userRequest);
        GeneralResponse generalResponse = response
                .body()
                .as(GeneralResponse.class);
        response.then().assertThat().statusCode(SC_FORBIDDEN);
        assertFalse(generalResponse.isSuccess());
        assertEquals("User already exists", generalResponse.getMessage());
    }

    @After
    public void tearDown() {
        userClient.userDelete(userRequest, UserCreds.getCredsFrom(userCreationResponse));
    }
}
