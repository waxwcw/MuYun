//package net.ximatai.muyun.test.core;
//
//import io.quarkus.test.common.QuarkusTestResource;
//import io.quarkus.test.junit.QuarkusTest;
//import jakarta.ws.rs.GET;
//import jakarta.ws.rs.Path;
//import net.ximatai.muyun.test.testcontainers.PostgresTestResource;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//
//import static io.restassured.RestAssured.given;
//
//@QuarkusTest
//@QuarkusTestResource(value = PostgresTestResource.class)
//public class TestWildcardPath {
//
//    @Test
//    @DisplayName("测试通配符路径的视图接口")
//    void test() {
//        String result = given()
//            .get("/api/commondoc/wildcard/wangpan/view")
//            .then()
//            .statusCode(200)
//            .extract()
//            .asString();
//
//        System.out.println(result);
//    }
//}
//
//@Path("/commondoc/wildcard/{type}")
//class WildcardPathController {
//
//    @GET
//    @Path("view")
//    public String view() {
//        return getPath();
//    }
//
//    String getPath() {
//        Path annotation = this.getClass().getAnnotation(Path.class);
//        if (annotation != null) {
//            return annotation.value();
//        }
//        return null;
//    }
//}
