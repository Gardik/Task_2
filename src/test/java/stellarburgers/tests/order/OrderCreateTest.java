package stellarburgers.tests.order;

import org.junit.jupiter.api.*;
import stellarburgers.data.TestData;
import stellarburgers.steps.IngredientSteps;
import stellarburgers.steps.OrderSteps;
import stellarburgers.steps.UserSteps;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderCreateTest {

    private final IngredientSteps ingredientSteps = new IngredientSteps();
    private final OrderSteps orderSteps = new OrderSteps();
    private final UserSteps userSteps = new UserSteps();

    private List<String> ingredientIds;
    private String accessTokenToCleanup;

    @BeforeEach
    void setUp() {
        ingredientIds = ingredientSteps.getIngredientIds(2);
    }

    @AfterEach
    void cleanup() {
        if (accessTokenToCleanup != null) {
            userSteps.deleteUser(accessTokenToCleanup);
        }
    }

    @Test
    @DisplayName("Создание заказа с авторизацией и ингредиентами")
    void shouldCreateOrderWithAuthAndIngredients() {
        var reg = userSteps.register(TestData.randomEmail(), TestData.randomPassword(), TestData.randomName());
        reg.then().statusCode(200);
        accessTokenToCleanup = reg.jsonPath().getString("accessToken");

        var resp = orderSteps.createOrder(accessTokenToCleanup, ingredientIds);

        resp.then().statusCode(200);
        assertTrue(resp.jsonPath().getBoolean("success"));
        assertNotNull(resp.jsonPath().get("order.number"));
    }

    @Test
    @DisplayName("Создание заказа без авторизации, но с ингредиентами")
    void shouldCreateOrderWithoutAuthButWithIngredients() {
        var resp = orderSteps.createOrder(null, ingredientIds);

        resp.then().statusCode(200);
        assertTrue(resp.jsonPath().getBoolean("success"));
        assertNotNull(resp.jsonPath().get("order.number"));
    }

    @Test
    @DisplayName("Создание заказа без ингредиентов -> ошибка")
    void shouldFailCreateOrderWithoutIngredients() {
        var resp = orderSteps.createOrder(null, null);

        resp.then().statusCode(400);
        assertFalse(resp.jsonPath().getBoolean("success"));
        String msg = resp.jsonPath().getString("message");
        assertNotNull(msg);
        assertTrue(msg.toLowerCase().contains("ingredient"), "Ожидали ошибку про ингредиенты, msg=" + msg);
    }

    @Test
    @DisplayName("Создание заказа без ингредиентов с авторизацией -> ошибка")
    void shouldFailCreateOrderWithoutIngredientsWithAuth() {
        var reg = userSteps.register(TestData.randomEmail(), TestData.randomPassword(), TestData.randomName());
        reg.then().statusCode(200);
        accessTokenToCleanup = reg.jsonPath().getString("accessToken");

        var resp = orderSteps.createOrder(accessTokenToCleanup, null);

        resp.then().statusCode(400);
        assertFalse(resp.jsonPath().getBoolean("success"));
        String msg = resp.jsonPath().getString("message");
        assertNotNull(msg);
        assertTrue(msg.toLowerCase().contains("ingredient"), "Ожидали ошибку про ингредиенты, msg=" + msg);
    }

    @Test
    @DisplayName("Создание заказа с неверным хешем ингредиентов -> ошибка")
    void shouldFailCreateOrderWithInvalidIngredientHash() {
        var resp = orderSteps.createOrder(null, List.of("invalid_hash_123"));

        int status = resp.getStatusCode();
        assertTrue(status == 500 || status == 400,
                "Ожидали ошибку при неверном хеше ингредиентов (400 или 500), получили: " + status);
    }
}