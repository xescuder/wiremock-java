import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.restassured.RestAssured;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.impl.client.CloseableHttpClient;
import org.eclipse.jetty.http.HttpStatus;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;

@Slf4j
@WireMockTest(httpPort = 8082)
class UsersRestWiremockTest {
    CloseableHttpClient client;

    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = "http://localhost:8082/api/";
    }

    @Test
    void given_user_list_when_page_1_users_are_retrieved(WireMockRuntimeInfo wmRuntimeInfo) {
        given().log().all().param("page", 1).get("/users")
                .then().log().ifError().statusCode(HttpStatus.OK_200)
                .body("total_pages", equalTo(2)).and().body("data.id", hasItems(1,2,3,4,5,6));
    }

    @Test
    void given_user_list_when_page_2_users_are_retrieved_error_is_shown(WireMockRuntimeInfo wmRuntimeInfo) {
        given().log().all().param("page", 2).get("/users")
                .then().log().ifError().statusCode(HttpStatus.NOT_FOUND_404);
    }

    @Test
    void given_user_list_when_page_2_users_are_retrieved_if_proxied(WireMockRuntimeInfo wmRuntimeInfo) {
        stubFor(get(urlPathEqualTo("/api/users"))
                .willReturn(aResponse().proxiedFrom("https://reqres.in")));

        given().log().all().param("page", 2).get("/users")
                .then().log().ifError().statusCode(HttpStatus.OK_200)
                .body("total_pages", equalTo(2)).and().body("data.id", hasItems(7,8,9,10,11,12));
    }

    @Test
    void given_user_list_when_users_request_response_less_than_2_seconds(WireMockRuntimeInfo wmRuntimeInfo) {
        stubFor(get(urlPathEqualTo("/api/users"))
                .willReturn(aResponse().withStatus(HttpStatus.OK_200).withFixedDelay(5000)));

        given().log().all().param("page", 2).get("/users")
                .then().log().ifError().statusCode(HttpStatus.OK_200).and().time(Matchers.lessThan(2000L));

    }
}

