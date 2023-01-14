import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@Slf4j
class UsersRestTest {
    @BeforeAll
    public static void init() {
        RestAssured.baseURI = "https://reqres.in/api/";
    }

    @Test
    void given_user_list_when_page_1_users_are_retrieved() {
        log.debug("Executing test");
        given().log().all().param("page", 1).get("/users")
                .then().log().ifError().statusCode(HttpStatus.OK_200)
                .body("total_pages", equalTo(2)).and().body("data.id", hasItems(1,2,3,4,5,6));
    }


    @Test
    void postTest() {
        log.debug("Executing test");

        given()
                .accept(ContentType.JSON)
                .body("{'name':'Xavier', 'job':'Trainer'}")
                .post("/users")
                .then().log().ifValidationFails()
                .statusCode(HttpStatus.CREATED_201)
                .body("$", hasKey("id"))
                .and()
                .body("$", hasKey("createdAt"))
        ;
    }

    @Test
    void putTest() {
        JsonPath jsonPath  = given()
                .accept(ContentType.JSON)
                .body("{'name':'Xavier', 'job':'Trainer'}")
                .post("/users")
                .then()
                .extract().jsonPath();

        String idCreated =  jsonPath.get("id").toString() ;

        given()
                .log().all()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .body("{'job' : 'Trainer and Automation Tester'}")
                .put(String.format("/users/%s", idCreated))
                .then().log().all()
                .statusCode(HttpStatus.OK_200)
                .and()
                .body("$", hasKey("updatedAt"))
                .body("job", equalTo("Trainer and Automation Tester"));
    }

    @Test
    void delete(){
        JsonPath jsonPath  = given().log().all()
                .accept(ContentType.JSON)
                .body("{'name':'Xavier', 'job':'Trainer'")
                .post("/users")
                .then()
                .extract().jsonPath();

        String idCreated = ( jsonPath.get("id").toString() );

        given().log().all()
                .accept(ContentType.JSON)
                .param("id", idCreated)
                .delete("/users")
                .then().log().ifValidationFails()
                .statusCode(HttpStatus.NO_CONTENT_204);
    }

}
