package net.ximatai.muyun.platform.config;


import io.quarkus.runtime.Startup;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import net.ximatai.muyun.core.Scaffold;
import org.eclipse.microprofile.openapi.annotations.Operation;

@Startup
@Path("Config")
public class ConfigController extends Scaffold {


    @GET
    @Path("/getAppConfig")
    @Operation(
            summary = "获取应用配置"
    )
    public String getAppConfig() {
        return "flow";
    }


    @GET
    @Path("/getRongCloudToken")
    @Operation(
            summary = "获取融云Token"
    )
    public String rongcloud() {
        return "flow";
    }

}
