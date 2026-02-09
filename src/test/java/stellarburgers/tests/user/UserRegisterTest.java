package stellarburgers.tests.user;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import stellarburgers.data.TestData;
import stellarburgers.steps.UserSteps;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class UserRegisterTest {

    private final UserSteps userSteps = new UserSteps();
    private String accessTokenToCleanup;

    @AfterEach
    void cleanup() {
        if (accessTokenToCleanup != null) {
            userSteps.deleteUser(accessTokenToCleanup);
        }
    }

    @Test
    @DisplayName("Создать уникального пользователя")
    void shouldCreateUniqueUser() {
        String email = TestData.randomEmail();
        String password = TestData.randomPassword();
        String name = TestData.randomName();

        var resp = userSteps.register(email, password, name);

        resp.then().statusCode(200);
        assertTrue(resp.jsonPath().getBoolean("success"));

        accessTokenToCleanup = resp.jsonPath().getString("accessToken");
        assertNotNull(accessTokenToCleanup);
    }

    @Test
    @DisplayName("Создать пользователя, который уже зарегистрирован")
    void shouldNotCreateExistingUser() {
        String email = TestData.randomEmail();
        String password = TestData.randomPassword();
        String name = TestData.randomName();

        var first = userSteps.register(email, password, name);
        first.then().statusCode(200);
        accessTokenToCleanup = first.jsonPath().getString("accessToken");

        var second = userSteps.register(email, password, name);

        second.then().statusCode(403);
        assertFalse(second.jsonPath().getBoolean("success"));
        assertEquals("User already exists", second.jsonPath().getString("message"));
    }

    static Stream<Object[]> missingFieldData() {
        return Stream.of(
                new Object[]{null, TestData.randomPassword(), TestData.randomName()},
                new Object[]{TestData.randomEmail(), null, TestData.randomName()},
                new Object[]{TestData.randomEmail(), TestData.randomPassword(), null}
        );
    }

    @ParameterizedTest(name = "missing field case: email={0}, password={1}, name={2}")
    @MethodSource("missingFieldData")
    @DisplayName("Создать пользователя без обязательного поля = ошибка")
    void shouldFailRegisterWhenMissingRequiredField(String email, String password, String name) {
        var resp = userSteps.register(email, password, name);

        resp.then().statusCode(403);
        assertFalse(resp.jsonPath().getBoolean("success"));
        String msg = resp.jsonPath().getString("message");
        assertNotNull(msg);
        assertTrue(msg.toLowerCase().contains("required"), "Ожидали сообщение про обязательные поля, msg=" + msg);
    }
}
