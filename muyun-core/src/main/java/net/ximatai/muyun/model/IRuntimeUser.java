package net.ximatai.muyun.model;

import io.vertx.core.json.JsonObject;

public interface IRuntimeUser {

    IRuntimeUser WHITE = new IRuntimeUser() {
        @Override
        public String getId() {
            return "0";
        }

        @Override
        public String getName() {
            return "白名单用户";
        }

        @Override
        public String getUsername() {
            return "white";
        }

    };

    String getId();

    String getName();

    String getUsername();


    static IRuntimeUser build(String id) {
        return new IRuntimeUser() {

            @Override
            public String getId() {
                return id;
            }

            @Override
            public String getName() {
                return "";
            }

            @Override
            public String getUsername() {
                return "";
            }

        };
    }

    static IRuntimeUser build(JsonObject object) {
        return new IRuntimeUser() {

            @Override
            public String getId() {
                return object.getString("id");
            }

            @Override
            public String getName() {
                return object.getString("name");
            }

            @Override
            public String getUsername() {
                return object.getString("username");
            }

        };
    }
}
