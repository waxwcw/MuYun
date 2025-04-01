package net.ximatai.muyun.platform.topic;

import io.quarkus.runtime.Startup;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import net.ximatai.muyun.core.Scaffold;
import org.eclipse.microprofile.openapi.annotations.Operation;

@Startup
@Path("topic")
public class TopicController extends Scaffold{
    @GET
    @Path("/viewAll")
    @Transactional
    @Operation(
            summary = "获取题库"
    )
    public String viewAll() {
        return "获取题库";
    }

    @GET
    @Path("/view/list")
    @Transactional
    @Operation(
            summary = "分页获取题目"
    )
    public String view() {
        return null;
    }

    @GET
    @Path("/view/custom")
    @Transactional
    @Operation(
            summary = "获取自定义题库"
    )
    public String viewCustom() {
        return null;
    }


    @POST
    @Path("/custom/creat")
    @Transactional
    @Operation(
            summary = "创建自定义题库"
    )
    public String insert() {
        return null;
    }

    @POST
    @Path("/custom/batchCreate")
    @Transactional
    @Operation(
            summary = "批量添加题目"
    )
    public String batchCreate() {
        return null;
    }


    @GET
    @Path("/wrong/view")
    @Transactional
    @Operation(
            summary = "分页查看错题"
    )
    public String wrong() {
        return null;
    }

    @GET
    @Path("/statistics")
    @Transactional
    @Operation(
            summary = "获取刷题统计"
    )
    public String statistics() {
        return null;
    }

    @POST
    @Path("/result")
    @Transactional
    @Operation(
            summary = "答题结果"
    )
    public String result() {
        return null;
    }
}
