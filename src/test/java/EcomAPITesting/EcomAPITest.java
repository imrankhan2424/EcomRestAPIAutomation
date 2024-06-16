package EcomAPITesting;

import Pojo.*;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.specification.RequestSpecification;
import org.testng.Assert;


import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static io.restassured.RestAssured.given;

public class EcomAPITest {
    public static void main(String[] args) {

        // 1. Login
        RequestSpecification req=
                new RequestSpecBuilder().setBaseUri("https://rahulshettyacademy.com/")
                        .setContentType(ContentType.JSON).build();

        LoginPayload loginPayload=new LoginPayload();
        loginPayload.setUserEmail("jojiri1374@noefa.com");
        loginPayload.setUserPassword("Test@123");

        RequestSpecification reqLogin=given().spec(req).body(loginPayload);

        LoginResponse loginResponse=reqLogin.when().post("api/ecom/auth/login").then().assertThat().statusCode(200)
                .extract().response().as(LoginResponse.class);
        String token=loginResponse.getToken();
        String userId=loginResponse.getUserId();

        // 2. Create Product
        RequestSpecification res2=new RequestSpecBuilder().setBaseUri("https://rahulshettyacademy.com/").addHeader("Authorization",token).build();
        String createProductResp=given().spec(res2).param("productName","Joker").param("productAddedBy",userId)
                .param("productCategory","Movies").param("productSubCategory","PG-13")
                .param("productPrice","1500").param("productDescription","DC Elseworld Project")
                .param("productFor","All").multiPart("productImage",new File("joker.png"))
                .when().post("api/ecom/product/add-product").then().extract().response().asString();
        JsonPath js=new JsonPath(createProductResp);
        String productID=js.get("productId");
        Assert.assertEquals(js.get("message"),"Product Added Successfully");

        // 3. Create Orders
        RequestSpecification res3=new RequestSpecBuilder().setBaseUri("https://rahulshettyacademy.com/").
                addHeader("Authorization",token).setContentType(ContentType.JSON).build();
        Orders order=new Orders();
        order.setCountry("India");
        order.setProductOrderedId(productID);

        List<Orders> ordersList=new ArrayList<>();
        ordersList.add(order);

        CreateOrderPayload createOrderPayload=new CreateOrderPayload();
        createOrderPayload.setOrders(ordersList);

        RequestSpecification createOrdersReq=given().spec(res3).body(createOrderPayload);
        CreateOrderResp createOrdersResp=createOrdersReq.when().post("api/ecom/order/create-order").then().extract().response().as(CreateOrderResp.class);

        Assert.assertEquals(createOrdersResp.getMessage(),"Order Placed Successfully");
        List<String> orders=createOrdersResp.getOrders();
        Assert.assertEquals(orders.size(),1);
        String orderId=orders.get(0);

        // 4. Get Orders
        RequestSpecification res4=new RequestSpecBuilder().setBaseUri("https://rahulshettyacademy.com/")
                .setContentType(ContentType.JSON).addHeader("Authorization",token).build();
        String getOrdersResp=given().spec(res4).queryParam("id",orderId).when().get("api/ecom/order/get-orders-details")
                .then().extract().response().asString();
        JsonPath js3=new JsonPath(getOrdersResp);
        Assert.assertEquals(js3.get("message"),"Orders fetched for customer Successfully");

        // 4. Delete Product
        RequestSpecification res5=new RequestSpecBuilder().setBaseUri("https://rahulshettyacademy.com/")
                .setContentType(ContentType.JSON).addHeader("Authorization",token).build();
        String deleteResp=given().pathParam("productID",productID).spec(res5).when().delete("api/ecom/product/delete-product/{productID}")
                .then().extract().response().asString();
        JsonPath js4=new JsonPath(deleteResp);
        Assert.assertEquals(js4.get("message"),"Product Deleted Successfully");

        System.out.println("API Testing Completed");
    }
}
