package stellarburgers.data;

import java.util.UUID;

public class TestData {

    public static String randomEmail() {
        return "test-" + UUID.randomUUID() + "@yandex.ru";
    }

    public static String randomName() {
        return "User-" + UUID.randomUUID().toString().substring(0, 8);
    }

    public static String randomPassword() {
        return "Password-" + UUID.randomUUID().toString().substring(0, 8);
    }
}

