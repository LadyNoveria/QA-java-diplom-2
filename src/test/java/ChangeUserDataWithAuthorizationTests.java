import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import user.*;

import static org.apache.http.HttpStatus.SC_OK;
import static org.apache.http.HttpStatus.SC_FORBIDDEN;
import static org.junit.Assert.*;

public class ChangeUserDataWithAuthorizationTests {

    private UserClient userClient;
    private static UserRequest userRequest;
    private static UserResponse userResponse;

    @Before
    public void setUp() {
        userRequest = UserRequest.userGenerator();
        userClient = new UserClient();
        userClient.userCreate(userRequest);
        userResponse = userClient.userLogin(userRequest).body()
                .as(UserResponse.class);
    }

    @Test
    @DisplayName("200 OK: successful change user email")
    public void successfulChangeUserEmail() {
        userRequest.setEmail(UserRequest.userGenerator().getEmail());
        userClient.userChange(userRequest, UserCreds.getCredsFrom(userResponse))
                .then()
                .assertThat()
                .statusCode(SC_OK);
        UserInfo userInfo = userClient.getUserInfo(UserCreds.getCredsFrom(userResponse))
                .body()
                .as(UserInfo.class);
        assertTrue(userInfo.isSuccess());
        assertEquals(userRequest.getEmail().toLowerCase(), userInfo.getUser().getEmail());
    }

    @Test
    @DisplayName("200 OK: successful change user name")
    public void successfulChangeUserName() {
        userRequest.setEmail(UserRequest.userGenerator().getName());
        userClient.userChange(userRequest, UserCreds.getCredsFrom(userResponse))
                .then()
                .assertThat()
                .statusCode(SC_OK);
        UserInfo userInfo = userClient.getUserInfo(UserCreds.getCredsFrom(userResponse))
                .body()
                .as(UserInfo.class);
        assertTrue(userInfo.isSuccess());
        assertEquals(userRequest.getName(), userInfo.getUser().getName());
    }

    @Test
    @DisplayName("200 OK: successful change user password")
    public void successfulChangeUserPassword() {
        userRequest.setEmail(UserRequest.userGenerator().getPassword());
        userClient.userChange(userRequest, UserCreds.getCredsFrom(userResponse))
                .then()
                .assertThat()
                .statusCode(SC_OK);
        userClient.userLogin(userRequest).then().assertThat().statusCode(SC_OK);
    }

    @Test
    @DisplayName("403 FORBIDDEN: user with such email already exists")
    public void errorChangeUserEmailExist() {
        Response response = userClient.userChange(userRequest, UserCreds.getCredsFrom(userResponse));
        response.then().assertThat().statusCode(SC_FORBIDDEN);
        GeneralResponse generalResponse = response.body().as(GeneralResponse.class);
        assertFalse(generalResponse.isSuccess());
        assertEquals("User with such email already exists", generalResponse.getMessage());
    }

    @After
    public void tearDown() {
        userClient.userDelete(userRequest, UserCreds.getCredsFrom(userResponse))
                .body()
                .as(UserResponse.class);
    }
}
