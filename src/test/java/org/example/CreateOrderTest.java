package org.example;

import io.qameta.allure.Step;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;

public class CreateOrderTest {

    @BeforeEach
    public void setUp() {
        RestAssured.baseURI = "https://qa-scooter.education-services.ru";
    }

    private static Stream<Arguments> colorProvider() {
        return Stream.of(
                Arguments.of("Один цвет — BLACK", new String[]{"BLACK"}),
                Arguments.of("Один цвет — GREY", new String[]{"GREY"}),
                Arguments.of("Оба цвета", new String[]{"BLACK", "GREY"}),
                Arguments.of("Без цвета", (Object) null)
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("colorProvider")
    @DisplayName("Можно создать заказ с разными вариантами цвета")
    public void canCreateOrderWithDifferentColors(String testCaseName, String[] color) {
        Order order = createOrder(color);
        Response response = sendCreateOrderRequest(order);
        checkOrderCreatedSuccessfully(response);
    }

    @Step("Подготовить данные заказа с цветом: {color}")
    public Order createOrder(String[] color) {
        return new Order(
                "Мактыбек", "Оролбай", "Москва, ул. Космонавтов 11", "4",
                "+73872221111", 5, "2026-08-12",
                "плиз, с яйцами", color
        );
    }

    @Step("Отправить POST-запрос на создание заказа")
    public Response sendCreateOrderRequest(Order order) {
        return given()
                .header("Content-type", "application/json")
                .body(order)
                .when()
                .post("/api/v1/orders");
    }

    @Step("Проверить, что заказ создан успешно и получен track")
    public void checkOrderCreatedSuccessfully(Response response) {
        response.then()
                .assertThat()
                .statusCode(201)
                .body("track", notNullValue());
    }
}