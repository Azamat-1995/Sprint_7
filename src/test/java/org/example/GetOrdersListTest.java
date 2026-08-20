package org.example;

import io.qameta.allure.Step;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;

public class GetOrdersListTest {

    @BeforeEach
    public void setUp() {
        RestAssured.baseURI = "https://qa-scooter.education-services.ru";
    }

    @Test
    @DisplayName("Список заказов возвращается в теле ответа")
    public void ordersListIsReturned() {
        Response response = sendGetOrdersRequest();
        checkOrdersListIsPresent(response);
    }

    @Step("Отправить GET-запрос на получение списка заказов")
    public Response sendGetOrdersRequest() {
        return given()
                .header("Content-type", "application/json")
                .when()
                .get("/api/v1/orders");
    }

    @Step("Проверить, что тело ответа содержит список заказов")
    public void checkOrdersListIsPresent(Response response) {
        response.then()
                .assertThat()
                .statusCode(200)
                .body("orders", notNullValue());
    }
}