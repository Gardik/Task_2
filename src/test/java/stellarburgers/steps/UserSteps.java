package stellarburgers.steps;

import io.qameta.allure.Step;
import io.restassured.response.Response;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static stellarburgers.client.Specs.baseSpec;

public class UserSteps {

    private static final String REGISTER = "/api/auth/register";
    private static final String LOGIN = "/api/auth/login";
    private static final String USER = "/api/auth/user";

    @Step("Регистрация пользователя: email={email}, name={name}")
    public Response register(String email, String password, String name) {
        Map<String, Object> body = new HashMap<>();
        if (email != null) body.put("email", email);
        if (password != null) body.put("password", password);
        if (name != null) body.put("name", name);

        return given()
                .spec(baseSpec())
                .body(body)
                .when()
                .post(REGISTER);
    }

    @Step("Логин пользователя: email={email}")
    public Response login(String email, String password) {
        Map<String, Object> body = new HashMap<>();
        if (email != null) body.put("email", email);
        if (password != null) body.put("password", password);

        return given()
                .spec(baseSpec())
                .body(body)
                .when()
                .post(LOGIN);
    }

    @Step("Получение профиля (GET /api/auth/user)")
    public Response getUser(String accessToken) {
        return given()
                .spec(baseSpec())
                .header("Authorization", accessToken)
                .when()
                .get(USER);
    }

    @Step("Обновление профиля (PATCH /api/auth/user)")
    public Response updateUser(String accessToken, Map<String, Object> updateBody) {
        return given()
                .spec(baseSpec())
                .header("Authorization", accessToken)
                .body(updateBody)
                .when()
                .patch(USER);
    }

    @Step("Обновление профиля без авторизации (PATCH /api/auth/user)")
    public Response updateUserNoAuth(Map<String, Object> updateBody) {
        return given()
                .spec(baseSpec())
                .body(updateBody)
                .when()
                .patch(USER);
    }

    @Step("Удаление пользователя (DELETE /api/auth/user)")
    public Response deleteUser(String accessToken) {
        return given()
                .spec(baseSpec())
                .header("Authorization", accessToken)
                .when()
                .delete(USER);
    }
}
