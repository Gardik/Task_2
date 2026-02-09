package stellarburgers.tests.user;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import stellarburgers.data.TestData;
import stellarburgers.steps.UserSteps;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class UserUpdateTest {

    private final UserSteps userSteps = new UserSteps();

    private String email;
    private String password;
    private String name;

    private String accessToken;

    @BeforeEach
    void setUp() {
        email = TestData.randomEmail();
        password = TestData.randomPassword();
        name = TestData.randomName();

        var reg = userSteps.register(email, password, name);
        reg.then().statusCode(200);
        accessToken = reg.jsonPath().getString("accessToken");
    }

    @AfterEach
    void cleanup() {
        if (accessToken != null) {
            userSteps.deleteUser(accessToken);
        }
    }

    @ParameterizedTest(name = "Update with auth: field={0}")
    @ValueSource(strings = {"email", "name", "password"})
    @DisplayName("Изменение данных пользователя с авторизацией: любое поле")
    void shouldUpdateAnyFieldWithAuth(String field) {
        Map<String, Object> update = new HashMap<>();

        if ("email".equals(field)) {
            String newEmail = TestData.randomEmail();
            update.put("email", newEmail);

            var resp = userSteps.updateUser(accessToken, update);
            resp.then().statusCode(200);
            assertTrue(resp.jsonPath().getBoolean("success"));
            assertEquals(newEmail, resp.jsonPath().getString("user.email"));
        }

        if ("name".equals(field)) {
            String newName = TestData.randomName();
            update.put("name", newName);

            var resp = userSteps.updateUser(accessToken, update);
            resp.then().statusCode(200);
            assertTrue(resp.jsonPath().getBoolean("success"));
            assertEquals(newName, resp.jsonPath().getString("user.name"));
        }

        if ("password".equals(field)) {
            String newPassword = TestData.randomPassword();
            update.put("password", newPassword);

            var resp = userSteps.updateUser(accessToken, update);
            resp.then().statusCode(200);
            assertTrue(resp.jsonPath().getBoolean("success"));

            String currentEmail = resp.jsonPath().getString("user.email");
            var login = userSteps.login(currentEmail, newPassword);
            login.then().statusCode(200);
            assertTrue(login.jsonPath().getBoolean("success"));
        }
    }

    @ParameterizedTest(name = "Update without auth: field={0}")
    @ValueSource(strings = {"email", "name", "password"})
    @DisplayName("Изменение данных пользователя без авторизации: любое поле = ошибка")
    void shouldFailUpdateAnyFieldWithoutAuth(String field) {
        Map<String, Object> update = new HashMap<>();

        if ("email".equals(field)) update.put("email", TestData.randomEmail());
        if ("name".equals(field)) update.put("name", TestData.randomName());
        if ("password".equals(field)) update.put("password", TestData.randomPassword());

        var resp = userSteps.updateUserNoAuth(update);

        resp.then().statusCode(401);
        assertFalse(resp.jsonPath().getBoolean("success"));
        String msg = resp.jsonPath().getString("message");
        assertNotNull(msg);
        assertTrue(msg.toLowerCase().contains("author"), "Ожидали сообщение про авторизацию, msg=" + msg);
    }
}

