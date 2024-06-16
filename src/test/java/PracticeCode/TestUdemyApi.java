package PracticeCode;

import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class TestUdemyApi {
    public static void main(String[] args) throws IOException {
        //given - all input details
        //when - submit - Resource and request type (get,post,put,delete)
        //then - validate response

        RestAssured.baseURI="https://rahulshettyacademy.com";
        System.out.println("\n--------------------------------");
        System.out.println("POST API Call\n");
        String response=
                given().log().all()
                        .queryParam("key","qaclick123")
                        .header("Content-Type","application/json")
                        .body(new String(Files.readAllBytes(Paths.get("AddPayload.json"))))
                .when()
                    .post("/maps/api/place/add/json")
                .then().log().all()
                .assertThat()
                    .statusCode(200)
                    .body("scope",equalTo("APP"))
                    .header("server","Apache/2.4.52 (Ubuntu)")
                .extract().response().asString();

        System.out.println("\n--------------------------------");
        System.out.println("\nResponse:\n"+response);

        System.out.println("\n--------------------------------\n");
        System.out.println("Place id value:");
        JsonPath js=new JsonPath(response);
        String placeid=js.get("place_id");
        System.out.println(placeid);

        //Update API
        System.out.println("\n--------------------------------");
        System.out.println("Update API Call\n");

        given()
                .queryParam("key","qaclick123")
                .header("Content-Type","application/json")
                .body("{\n" +
                        "\"place_id\":\""+placeid+"\",\n" +
                        "\"address\":\"70 Summer walk, USA\",\n" +
                        "\"key\":\"qaclick123\"\n" +
                        "}\n")
                .when().put("/maps/api/place/update/json")
                .then().log().all().
                assertThat().statusCode(200).
                body("msg",equalTo("Address successfully updated"));

        //Get API
        System.out.println("\n--------------------------------\n");
        System.out.println("Get API Call\n");

        given().log().all()
                .queryParam("key","qaclick123")
                .queryParam("place_id",placeid)
        .when()
                .get("/maps/api/place/get/json")
        .then()
                .assertThat().log().all()
                .statusCode(200)
                .body("address",equalTo("70 Summer walk, USA"));


    }
}
