import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import user.UserCreds;
import user.UserRequest;
import user.UserResponse;

import static org.apache.http.HttpStatus.SC_OK;
import static org.apache.http.HttpStatus.SC_UNAUTHORIZED;
import static org.junit.Assert.*;

public class UserLoginTests {
    private UserClient userClient;
    private UserRequest userRequest;
    private UserResponse userCreationResponse;

    @Before
    public void setUp() {
        userRequest = UserRequest.userGenerator();
        userClient = new UserClient();
        userCreationResponse = userClient.userCreate(userRequest)
                .body()
                .as(UserResponse.class);
    }

    @Test
    @DisplayName("401 UNAUTHORIZED: login user with incorrect password")
    public void errorLoginUserIncorrectPassword() {
        userRequest.setPassword("incorrect_password");
        Response response = userClient.userLogin(userRequest);
        response.then().assertThat().statusCode(SC_UNAUTHORIZED);
        GeneralResponse generalResponse = response
                .body()
                .as(GeneralResponse.class);
        assertFalse(generalResponse.isSuccess());
        assertEquals("email or password are incorrect", generalResponse.getMessage());
    }

    @Test
    @DisplayName("401 UNAUTHORIZED: login user with incorrect email")
    public void errorLoginUserIncorrectEmail() {
        userRequest.setEmail("incorrect_email");
        Response response = userClient.userLogin(userRequest);
        response.then().assertThat().statusCode(SC_UNAUTHORIZED);
        GeneralResponse generalResponse = response
                .body()
                .as(GeneralResponse.class);
        assertFalse(generalResponse.isSuccess());
        assertEquals("email or password are incorrect", generalResponse.getMessage());
    }

    @Test
    @DisplayName("200 OK: successful login user")
    public void successfulLoginUser() {
        Response response = userClient.userLogin(userRequest);
        response.then().assertThat().statusCode(SC_OK);
        assertTrue(userClient.userLogin(userRequest)
                .body()
                .as(UserResponse.class)
                .isSuccess());
    }

    @After
    public void tearDown() {
        userClient.userDelete(userRequest, UserCreds.getCredsFrom(userCreationResponse));
    }
}
