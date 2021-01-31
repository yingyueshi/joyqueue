package org.joyqueue.handler.routing.command.migration;

import com.jd.laf.binding.annotation.Value;
import com.jd.laf.web.vertx.Command;
import com.jd.laf.web.vertx.annotation.Body;
import com.jd.laf.web.vertx.annotation.Path;
import com.jd.laf.web.vertx.pool.Poolable;
import com.jd.laf.web.vertx.response.Response;
import com.jd.laf.web.vertx.response.Responses;
import org.joyqueue.service.MigrationSubjobService;

public class MigrationSubjobCommand implements Command<Response>, Poolable {

    @Value(nullable = false)
    private MigrationSubjobService migrationSubjobService;

    @Path("getByMigrationId")
    public Response getByMigrationId(@Body(type = Body.BodyType.TEXT) String migrationId) {
        return Responses.success(migrationSubjobService.findByMigrationId(Long.parseLong(migrationId)));
    }

    @Override
    public void clean() {
        migrationSubjobService = null;
    }
}
