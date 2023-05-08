import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import user.UserCreds;
import user.UserRequest;
import user.UserResponse;

import static org.apache.http.HttpStatus.SC_UNAUTHORIZED;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

@RunWith(Parameterized.class)
public class ChangeUserDataWithoutAuthorizationTests {
    private UserClient userClient;
    private static UserRequest userRequest;
    private static UserResponse userResponse;
    private final String email;
    private final String password;
    private final String name;

    public ChangeUserDataWithoutAuthorizationTests(String email, String password, String name) {
        this.email = email;
        this.password = password;
        this.name = name;
    }

    @Before
    public void setUp() {
        userRequest = UserRequest.userGenerator();
        userClient = new UserClient();
        userClient.userCreate(userRequest);
        userResponse = userClient.userLogin(userRequest).body()
                .as(UserResponse.class);
    }

    @Parameterized.Parameters()
    public static Object[][] data() {
        return new Object[][]{
                {UserRequest.userGenerator().getEmail(), null, null},
                {null, UserRequest.userGenerator().getPassword(), null},
                {null, null, UserRequest.userGenerator().getName()}
        };
    }

    @Test
    @DisplayName("401 UNAUTHORIZED: change user without authorization")
    public void errorChangeUserWithoutAuthorization() {
        userRequest = new UserRequest(email, password, name);
        Response response = userClient.userChange(userRequest);
        response.then().assertThat().statusCode(SC_UNAUTHORIZED);
        GeneralResponse generalResponse = response.body().as(GeneralResponse.class);
        assertFalse(generalResponse.isSuccess());
        assertEquals("You should be authorised", generalResponse.getMessage());
    }

    @After
    public void tearDown() {
        userClient.userDelete(userRequest, UserCreds.getCredsFrom(userResponse))
                .body()
                .as(UserResponse.class);
    }
}
