package org.example;

import io.qameta.allure.Step;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Random;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class CreateCourierWithAllureTest {

    private CreationCourier courier;

    @BeforeEach
    public void setUp() {
        RestAssured.baseURI = "https://qa-scooter.education-services.ru";

        int randomNumber = new Random().nextInt(100000);
        String login = "azamat" + randomNumber;
        String password = "azamat" + randomNumber;
        String firstName = "azamat" + randomNumber;

        courier = new CreationCourier(login, password, firstName);
    }

    @Test
    @DisplayName("Курьера можно создать, успешный запрос возвращает ok: true и код 201")
    public void courierCanBeCreated() {
        Response response = sendCreateCourierRequest(courier);
        checkCourierCreatedSuccessfully(response);
    }

    @Test
    @DisplayName("Нельзя создать двух курьеров с одинаковым логином")
    public void cannotCreateDuplicateCourier() {
        Response firstResponse = sendCreateCourierRequest(courier);
        checkCourierCreatedSuccessfully(firstResponse);

        Response secondResponse = sendCreateCourierRequest(courier);
        checkDuplicateCourierError(secondResponse);
    }

    @Test
    @DisplayName("Нельзя создать курьера без логина")
    public void cannotCreateCourierWithoutLogin() {
        File json = new File("src/test/resources/courierWithoutLogin.json");
        Response response = sendCreateCourierRequest(json);
        checkMissingFieldError(response);
    }

    @Test
    @DisplayName("Нельзя создать курьера без пароля")
    public void cannotCreateCourierWithoutPassword() {
        File json = new File("src/test/resources/courierWithoutPassword.json");
        Response response = sendCreateCourierRequest(json);
        checkMissingFieldError(response);
    }

    @Step("Отправить POST-запрос на создание курьера")
    public Response sendCreateCourierRequest(Object body) {
        return given()
                .header("Content-type", "application/json")
                .body(body)
                .when()
                .post("/api/v1/courier");
    }

    @Step("Отправить POST-запрос на создание курьера из файла")
    public Response sendCreateCourierRequest(File body) {
        return given()
                .header("Content-type", "application/json")
                .body(body)
                .when()
                .post("/api/v1/courier");
    }

    @Step("Проверить, что курьер успешно создан: ok=true, код 201")
    public void checkCourierCreatedSuccessfully(Response response) {
        response.then()
                .assertThat()
                .body("ok", equalTo(true))
                .and()
                .statusCode(201);
    }

    @Step("Проверить ошибку при создании курьера с занятым логином")
    public void checkDuplicateCourierError(Response response) {
        response.then()
                .assertThat()
                .statusCode(409)
                .body("message", equalTo("Этот логин уже используется. Попробуйте другой."));
    }

    @Step("Проверить ошибку при отсутствии обязательного поля")
    public void checkMissingFieldError(Response response) {
        response.then()
                .assertThat()
                .statusCode(400)
                .body("message", equalTo("Недостаточно данных для создания учетной записи"));
    }
}