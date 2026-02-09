package stellarburgers.tests.user;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import stellarburgers.data.TestData;
import stellarburgers.steps.UserSteps;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class UserLoginTest {

    private final UserSteps userSteps = new UserSteps();

    private String email;
    private String password;
    private String accessTokenToCleanup;

    @BeforeEach
    void setUp() {
        email = TestData.randomEmail();
        password = TestData.randomPassword();
        String name = TestData.randomName();

        var reg = userSteps.register(email, password, name);
        reg.then().statusCode(200);

        accessTokenToCleanup = reg.jsonPath().getString("accessToken");
    }

    @AfterEach
    void cleanup() {
        if (accessTokenToCleanup != null) {
            userSteps.deleteUser(accessTokenToCleanup);
        }
    }

    @Test
    @DisplayName("Логин под существующим пользователем")
    void shouldLoginExistingUser() {
        var resp = userSteps.login(email, password);

        resp.then().statusCode(200);
        assertTrue(resp.jsonPath().getBoolean("success"));
        assertNotNull(resp.jsonPath().getString("accessToken"));
        assertNotNull(resp.jsonPath().getString("refreshToken"));
        assertEquals(email, resp.jsonPath().getString("user.email"));
    }

    @Test
    @DisplayName("Логин: существующий email + неверный пароль = ошибка")
    void shouldFailLoginWithExistingEmailAndWrongPassword() {
        var resp = userSteps.login(email, "wrong-pass");

        resp.then().statusCode(401);
        assertFalse(resp.jsonPath().getBoolean("success"));
        String msg = resp.jsonPath().getString("message");
        assertNotNull(msg);
        assertTrue(msg.toLowerCase().contains("incorrect"), "Ожидали текст про некорректные креды, msg=" + msg);
    }

    static Stream<Object[]> wrongCreds() {
        return Stream.of(
                new Object[]{"wrong-" + TestData.randomEmail(), "wrong-pass"},
                new Object[]{TestData.randomEmail(), "wrong-pass"} // несуществующий email + неверный пароль
        );
    }

    @ParameterizedTest(name = "wrong login: email={0}")
    @MethodSource("wrongCreds")
    @DisplayName("Логин с неверным логином/паролем = ошибка")
    void shouldFailLoginWithWrongCredentials(String wrongEmail, String wrongPassword) {
        var resp = userSteps.login(wrongEmail, wrongPassword);

        resp.then().statusCode(401);
        assertFalse(resp.jsonPath().getBoolean("success"));
        String msg = resp.jsonPath().getString("message");
        assertNotNull(msg);
        assertTrue(msg.toLowerCase().contains("incorrect"), "Ожидали текст про некорректные креды, msg=" + msg);
    }
}


