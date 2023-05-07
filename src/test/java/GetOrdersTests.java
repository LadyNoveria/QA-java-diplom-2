import ingredient.IngredientList;
import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import order.OrderRequest;
import order.OrderResponse;
import order.OrdersUser;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import user.UserCreds;
import user.UserRequest;
import user.UserResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.apache.http.HttpStatus.SC_OK;
import static org.apache.http.HttpStatus.SC_UNAUTHORIZED;
import static org.junit.Assert.*;

public class GetOrdersTests {
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
    @DisplayName("200 OK: successful receipt of user orders")
    public void successfulGetOrdersUser() {
        List<String> orderIngredients = getRandomIngredients();
        OrderResponse order = getOrderResponse(orderIngredients);
        Response response = orderClient.getOrdersUser(UserCreds.getCredsFrom(userResponse));
        response.then().assertThat().statusCode(SC_OK);
        OrdersUser ordersUser = response.body().as(OrdersUser.class);
        assertTrue(ordersUser.isSuccess());
        assertEquals(ordersUser.getOrders().get(0).getIngredients(), orderIngredients);
        assertEquals(order.getOrder().getNumber(), ordersUser.getOrders().get(0).getNumber());
    }

    @Test
    @DisplayName("401 UNAUTHORIZED: receipt of user orders without token")
    public void errorGetOrdersUserUnauthorized() {
        Response response = orderClient.getOrdersUserWithoutAuthorization();
        response.then().assertThat().statusCode(SC_UNAUTHORIZED);
        GeneralResponse generalResponse = response.body().as(GeneralResponse.class);
        assertFalse(generalResponse.isSuccess());
        assertEquals("You should be authorised", generalResponse.getMessage());
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

    @Step("create order")
    private OrderResponse getOrderResponse(List<String> orderIngredients) {
        return orderClient
                .orderCreate(new OrderRequest(orderIngredients), UserCreds.getCredsFrom(userResponse))
                .body()
                .as(OrderResponse.class);
    }

    @After
    public void tearDown() {
        userClient.userDelete(userRequest, UserCreds.getCredsFrom(userResponse))
                .body()
                .as(UserResponse.class);
    }
}
