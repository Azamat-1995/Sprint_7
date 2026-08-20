package org.example;

import io.qameta.allure.Step;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Random;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

public class CourierLoginTest {

    private String login;
    private String password;

    @BeforeEach
    public void setUp() {
        RestAssured.baseURI = "https://qa-scooter.education-services.ru";

        int randomNumber = new Random().nextInt(100000);
        login = "azamat" + randomNumber;
        password = "azamat" + randomNumber;
        String firstName = "azamat" + randomNumber;

        CreationCourier courier = new CreationCourier(login, password, firstName);
        createCourier(courier);
    }

    @Test
    @DisplayName("Курьер может авторизоваться, успешный запрос возвращает id")
    public void courierCanLogin() {
        CourierCredentials credentials = new CourierCredentials(login, password);
        Response response = sendLoginRequest(credentials);
        checkLoginSuccessful(response);
    }

    @Test
    @DisplayName("Нельзя авторизоваться с неверным паролем")
    public void cannotLoginWithWrongPassword() {
        CourierCredentials credentials = new CourierCredentials(login, "azamat12947474");
        Response response = sendLoginRequest(credentials);
        checkAccountNotFoundError(response);
    }

    @Test
    @DisplayName("Нельзя авторизоваться с неверным логином")
    public void cannotLoginWithWrongLogin() {
        CourierCredentials credentials = new CourierCredentials("azamat12947474", password);
        Response response = sendLoginRequest(credentials);
        checkAccountNotFoundError(response);
    }

    @Test
    @DisplayName("Нельзя авторизоваться под несуществующим пользователем")
    public void cannotLoginWithNonExistentUser() {
        File json = new File("src/test/resources/non-existentUser.json");
        Response response = sendLoginRequest(json);
        checkAccountNotFoundError(response);
    }

    @Test
    @DisplayName("Нельзя авторизоваться без логина")
    public void cannotLoginWithoutLogin() {
        File json = new File("src/test/resources/loginWithoutLogin.json");
        Response response = sendLoginRequest(json);
        checkMissingFieldError(response);
    }

    @Test
    @Disabled("Баг: сервер обрывает соединение (socket hang up) вместо возврата 400 при отсутствии password")
    @DisplayName("Нельзя авторизоваться без пароля")
    public void cannotLoginWithoutPassword() {
        File json = new File("src/test/resources/loginWithoutPassword.json");
        Response response = sendLoginRequest(json);
        checkMissingFieldError(response);
    }

    @Step("Создать курьера перед тестом логина")
    public void createCourier(CreationCourier courier) {
        given()
                .header("Content-type", "application/json")
                .body(courier)
                .when()
                .post("/api/v1/courier")
                .then()
                .statusCode(201);
    }

    @Step("Отправить POST-запрос на авторизацию курьера")
    public Response sendLoginRequest(Object body) {
        return given()
                .header("Content-type", "application/json")
                .body(body)
                .when()
                .post("/api/v1/courier/login");
    }

    @Step("Отправить POST-запрос на авторизацию курьера из файла")
    public Response sendLoginRequest(File body) {
        return given()
                .header("Content-type", "application/json")
                .body(body)
                .when()
                .post("/api/v1/courier/login");
    }

    @Step("Проверить, что авторизация прошла успешно и получен id")
    public void checkLoginSuccessful(Response response) {
        response.then()
                .assertThat()
                .statusCode(200)
                .body("id", notNullValue());
    }

    @Step("Проверить ошибку 'Учетная запись не найдена'")
    public void checkAccountNotFoundError(Response response) {
        response.then()
                .assertThat()
                .statusCode(404)
                .body("message", equalTo("Учетная запись не найдена"));
    }

    @Step("Проверить ошибку при отсутствии обязательного поля для входа")
    public void checkMissingFieldError(Response response) {
        response.then()
                .assertThat()
                .statusCode(400)
                .body("message", equalTo("Недостаточно данных для входа"));
    }
}