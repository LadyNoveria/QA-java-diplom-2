import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import user.UserCreds;
import user.UserRequest;
import user.UserResponse;

import static org.apache.http.HttpStatus.SC_FORBIDDEN;
import static org.apache.http.HttpStatus.SC_OK;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

@RunWith(Parameterized.class)
public class UserCreationParameterizedTests {

    private final String email;
    private final String password;
    private final String name;

    private final UserClient userClient;

    private UserRequest userRequest;

    public UserCreationParameterizedTests(String email, String password, String name) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.userClient = new UserClient();
    }

    @Parameterized.Parameters(name = "Creating user with parameters: email: {0}, password: {1}, name: {2}")
    public static Object[][] data() {
        return new Object[][]{
                {null, UserRequest.userGenerator().getPassword(), UserRequest.userGenerator().getName()},
                {UserRequest.userGenerator().getEmail(), null, UserRequest.userGenerator().getName()},
                {UserRequest.userGenerator().getEmail(), UserRequest.userGenerator().getPassword(), null}
        };
    }

    @Test
    @DisplayName("403 FORBIDDEN: creating a user without required parameters")
    public void errorCreatingUser() {
        userRequest = new UserRequest(email, password, name);
        Response response = userClient.userCreate(userRequest);
        response.then().assertThat().statusCode(SC_FORBIDDEN);
        GeneralResponse generalResponse = response
                .body()
                .as(GeneralResponse.class);
        assertFalse(generalResponse.isSuccess());
        assertEquals("Email, password and name are required fields", generalResponse.getMessage());
    }

    @After
    public void tearDown() {
        Response userResponse = userClient.userLogin(userRequest);
        if (userResponse.getStatusCode() == 200) {
            userClient.userDelete(userRequest, UserCreds.getCredsFrom(userResponse
                    .body()
                    .as(UserResponse.class)));
        }
    }
}
