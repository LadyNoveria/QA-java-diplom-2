import io.qameta.allure.Step;
import io.restassured.RestAssured;
import ingredient.IngredientList;

import static io.restassured.RestAssured.given;

public class IngredientClient {
    private static final String GET_INGREDIENT_INFO_PATH = "api/ingredients";

    public IngredientClient() {
        RestAssured.baseURI = Constants.BASE_URI;;
    }

    @Step("Get ingredient list")
    public IngredientList getIngredientList() {
        return given()
                .header("Content-type", "application/json")
                .when()
                .get(GET_INGREDIENT_INFO_PATH)
                .body()
                .as(IngredientList.class);
    }
}
