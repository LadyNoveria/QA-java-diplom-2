import io.qameta.allure.Step;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import user.UserCreds;
import user.UserRequest;

import java.util.HashMap;

import static io.restassured.RestAssured.given;

public class UserClient {
    private static final String CREATE_USER_PATH = "api/auth/register";
    private static final String LOGIN_USER_PATH = "api/auth/login";
    private static final String USER_PATH = "api/auth/user";

    public UserClient() {
        RestAssured.baseURI = Constants.BASE_URI;;
    }

    @Step("Create user")
    public Response userCreate(UserRequest user) {
        return given()
                .header("Content-type", "application/json")
                .body(user)
                .post(CREATE_USER_PATH);
    }

    @Step("Login user")
    public Response userLogin(UserRequest user) {
        return given()
                .header("Content-type", "application/json")
                .body(user)
                .when()
                .post(LOGIN_USER_PATH);
    }

    @Step("Delete user")
    public Response userDelete(UserRequest user, UserCreds userCreds) {
        return given()
                .header("Content-type", "application/json")
                .header("Authorization", userCreds.getAccessToken())
                .body(user)
                .when()
                .delete(USER_PATH);
    }

    @Step("Change user")
    public Response userChange(UserRequest user, UserCreds userCreds) {
        return given()
                .header("Content-type", "application/json")
                .header("Authorization", userCreds.getAccessToken())
                .body(user)
                .when()
                .patch(USER_PATH);
    }

    @Step("Change user without authorization")
    public Response userChange(UserRequest user) {
        return given()
                .header("Content-type", "application/json")
                .body(user)
                .when()
                .patch(USER_PATH);
    }

    @Step("Get user info")
    public Response getUserInfo(UserCreds userCreds) {
        return given()
                .header("Content-type", "application/json")
                .header("Authorization", userCreds.getAccessToken())
                .when()
                .get(USER_PATH);
    }
}
