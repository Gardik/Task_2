package stellarburgers.steps;

import io.qameta.allure.Step;
import io.restassured.response.Response;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static stellarburgers.client.Specs.baseSpec;

public class OrderSteps {

    private static final String ORDERS = "/api/orders";

    @Step("Создание заказа (POST /api/orders), auth={accessTokenOrNull!=null}, ingredientsCount={ingredientsOrNull==null?0:ingredientsOrNull.size()}")
    public Response createOrder(String accessTokenOrNull, List<String> ingredientsOrNull) {
        Map<String, Object> body = new HashMap<>();
        if (ingredientsOrNull != null) {
            body.put("ingredients", ingredientsOrNull);
        }

        if (accessTokenOrNull == null) {
            return given()
                    .spec(baseSpec())
                    .body(body)
                    .when()
                    .post(ORDERS);
        }

        return given()
                .spec(baseSpec())
                .header("Authorization", accessTokenOrNull)
                .body(body)
                .when()
                .post(ORDERS);
    }

    @Step("Получение заказов пользователя (GET /api/orders), auth={accessTokenOrNull!=null}")
    public Response getUserOrders(String accessTokenOrNull) {
        if (accessTokenOrNull == null) {
            return given()
                    .spec(baseSpec())
                    .when()
                    .get(ORDERS);
        }

        return given()
                .spec(baseSpec())
                .header("Authorization", accessTokenOrNull)
                .when()
                .get(ORDERS);
    }
}
