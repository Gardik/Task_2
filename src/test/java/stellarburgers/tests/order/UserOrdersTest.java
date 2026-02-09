package stellarburgers.tests.order;

import org.junit.jupiter.api.*;
import stellarburgers.data.TestData;
import stellarburgers.steps.IngredientSteps;
import stellarburgers.steps.OrderSteps;
import stellarburgers.steps.UserSteps;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserOrdersTest {

    private final UserSteps userSteps = new UserSteps();
    private final OrderSteps orderSteps = new OrderSteps();
    private final IngredientSteps ingredientSteps = new IngredientSteps();

    private String accessToken;
    private List<String> ingredientIds;

    @BeforeEach
    void setUp() {
        ingredientIds = ingredientSteps.getIngredientIds(2);

        var reg = userSteps.register(TestData.randomEmail(), TestData.randomPassword(), TestData.randomName());
        reg.then().statusCode(200);
        accessToken = reg.jsonPath().getString("accessToken");

        var create = orderSteps.createOrder(accessToken, ingredientIds);
        create.then().statusCode(200);
    }

    @AfterEach
    void cleanup() {
        if (accessToken != null) {
            userSteps.deleteUser(accessToken);
        }
    }

    @Test
    @DisplayName("Получение заказов авторизованного пользователя")
    void shouldGetOrdersForAuthorizedUser() {
        var resp = orderSteps.getUserOrders(accessToken);

        resp.then().statusCode(200);
        assertTrue(resp.jsonPath().getBoolean("success"));
        assertNotNull(resp.jsonPath().getList("orders"));
    }

    @Test
    @DisplayName("Получение заказов без авторизации -> ошибка")
    void shouldFailGetOrdersForUnauthorizedUser() {
        var resp = orderSteps.getUserOrders(null);

        resp.then().statusCode(401);
        assertFalse(resp.jsonPath().getBoolean("success"));
        String msg = resp.jsonPath().getString("message");
        assertNotNull(msg);
        assertTrue(msg.toLowerCase().contains("author"), "Ожидали сообщение про авторизацию, msg=" + msg);
    }
}
