package stellarburgers.steps;

import io.qameta.allure.Step;
import io.restassured.response.Response;

import java.util.List;

import static io.restassured.RestAssured.given;
import static stellarburgers.client.Specs.baseSpec;

public class IngredientSteps {

    private static final String INGREDIENTS = "/api/ingredients";

    @Step("Получение списка ингредиентов (GET /api/ingredients)")
    public Response getIngredients() {
        return given()
                .spec(baseSpec())
                .when()
                .get(INGREDIENTS);
    }

    @Step("Получить {count} id ингредиентов из /api/ingredients")
    public List<String> getIngredientIds(int count) {
        Response resp = getIngredients();
        resp.then().statusCode(200);

        List<String> ids = resp.jsonPath().getList("data._id");
        if (ids == null || ids.size() < count) {
            throw new IllegalStateException("Недостаточно ингредиентов на стенде: " + (ids == null ? 0 : ids.size()));
        }
        return ids.subList(0, count);
    }
}

