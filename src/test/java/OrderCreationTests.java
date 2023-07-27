import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import ingredient.IngredientList;
import order.OrderRequest;
import order.OrderResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import user.UserCreds;
import user.UserRequest;
import user.UserResponse;

import java.util.*;

import static org.apache.http.HttpStatus.*;
import static org.junit.Assert.*;

public class OrderCreationTests {
    IngredientClient ingredientClient;
    IngredientList ingredientList;
    UserClient userClient;
    OrderClient orderClient;

    private UserRequest userRequest;
    private UserResponse userResponse;

    @Before
    public void setUp() {
        userRequest = UserRequest.userGenerator();
        userClient = new UserClient();
        ingredientClient = new IngredientClient();
        orderClient = new OrderClient();
        userClient.userCreate(userRequest);
        userResponse = userClient.userLogin(userRequest).body()
                .as(UserResponse.class);
        ingredientList = ingredientClient.getIngredientList();
    }

    @Test
    @DisplayName("200 ОК: successful order creation with token")
    public void successfulOrderCreation() {
        Response response = orderClient.orderCreate(new OrderRequest(getRandomIngredients()),
                UserCreds.getCredsFrom(userResponse));
        response.then().assertThat().statusCode(SC_OK);
        OrderResponse orderResponse = response.body().as(OrderResponse.class);
        assertTrue(orderResponse.isSuccess());
    }

    @Test
    @DisplayName("200 ОК: successful order creation without token")
    public void successfulOrderCreationWithoutAuthorization() {
        /* так как в документации не описано, должна ли быть зздесь ошибка, и если да, то какая и с каким message,
        оставлю позитивный тест */
        Response response = orderClient.orderCreateWithoutAuthorization(new OrderRequest(getRandomIngredients()));
        response.then().assertThat().statusCode(SC_OK);
        OrderResponse orderResponse = response.body().as(OrderResponse.class);
        assertTrue(orderResponse.isSuccess());
    }

    @Test
    @DisplayName("400 BAD_REQUEST: creation order without ingredients")
    public void errorOrderCreationNoIngredients() {
        Response response = orderClient.orderCreate(new OrderRequest(Collections.emptyList()),
                UserCreds.getCredsFrom(userResponse));
        response.then().assertThat().statusCode(SC_BAD_REQUEST);
        GeneralResponse generalResponse = response.body().as(GeneralResponse.class);
        assertFalse(generalResponse.isSuccess());
        assertEquals("Ingredient ids must be provided", generalResponse.getMessage());
    }

    @Test
    @DisplayName("500 INTERNAL_SERVER_ERROR: creation order with invalid ingredient")
    public void errorOrderCreationInvalidIngredientId() {
        Response response = orderClient.orderCreate(new OrderRequest(List.of("60d3b41abdacab0026a733")),
                UserCreds.getCredsFrom(userResponse));
        response.then().assertThat().statusCode(SC_INTERNAL_SERVER_ERROR);
    }

    @Step("get random ingredients list")
    private List<String> getRandomIngredients() {
        List<String> ingredients = new ArrayList<>();
        Random random = new Random();
        int count = random.nextInt(ingredientList.getData().size());
        for (int i = 0; i < count; i++) {
            int index = random.nextInt(ingredientList.getData().size());
            ingredients.add(ingredientList.getData().get(index).get_id());
        }
        return ingredients;
    }

    @After
    public void tearDown() {
        userClient.userDelete(userRequest, UserCreds.getCredsFrom(userResponse))
                .body()
                .as(UserResponse.class);
    }
}
