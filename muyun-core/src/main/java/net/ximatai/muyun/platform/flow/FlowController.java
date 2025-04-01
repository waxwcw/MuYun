package net.ximatai.muyun.platform.flow;


import io.quarkus.runtime.Startup;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import net.ximatai.muyun.ability.curd.std.*;
import net.ximatai.muyun.core.Scaffold;
import org.eclipse.microprofile.openapi.annotations.Operation;

import java.util.Map;

@Startup
@Path("infor-flow")
public class FlowController extends Scaffold implements IUpdateAbility, ISelectAbility {


    @Override
    public String getMainTable() {
        return "flow";
    }

    @GET
    @Path("/view/comment")
    @Operation(
            summary = "分页查询评论"
    )
    public String commentView() {
        return "flow";
    }


    @POST
    @Path("/create")
    @Transactional
    @Operation(
            summary = "创建动态"
    )
    public String create(Map body) {
        return null;
    }

    @POST
    @Path("/create/comment")
    @Transactional
    @Operation(
            summary = "创建评论"
    )
    public String createComment(Map body) {
        return null;
    }

    @GET
    @Path("/delete/comment/{id}")
    @Transactional
    @Operation(
            summary = "删除评论"
    )
    public String deleteComment(Map body) {
        return null;
    }

    @GET
    @Path("/delete/{id}")
    @Transactional
    @Operation(
            summary = "删除动态"
    )
    public String delete(Map body) {
        return null;
    }


    @POST
    @Path("/like")
    @Transactional
    @Operation(
            summary = "点赞动态"
    )
    public String like(Map body) {
        return null;
    }

    @POST
    @Path("/likeComment")
    @Transactional
    @Operation(
            summary = "点赞评论"
    )
    public String likeComment(Map body) {
        return null;
    }

    @GET
    @Path("/search")
    @Operation(
            summary = "支持模糊搜索"
    )
    public String search() {
        return "flow";
    }

    @GET
    @Path("/view/hot")
    @Operation(
            summary = "热门动态推荐"
    )
    public String hot() {
        return "flow";
    }

}
