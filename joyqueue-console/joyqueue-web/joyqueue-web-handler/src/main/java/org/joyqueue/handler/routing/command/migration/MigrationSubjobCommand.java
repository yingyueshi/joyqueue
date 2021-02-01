package org.joyqueue.handler.routing.command.migration;

import com.jd.laf.binding.annotation.Value;
import com.jd.laf.web.vertx.Command;
import com.jd.laf.web.vertx.annotation.Path;
import com.jd.laf.web.vertx.annotation.QueryParam;
import com.jd.laf.web.vertx.pool.Poolable;
import com.jd.laf.web.vertx.response.Response;
import com.jd.laf.web.vertx.response.Responses;
import org.joyqueue.service.MigrationSubjobService;

import static com.jd.laf.web.vertx.response.Response.HTTP_BAD_REQUEST;

public class MigrationSubjobCommand implements Command<Response>, Poolable {

    @Value(nullable = false)
    protected MigrationSubjobService migrationSubjobService;

    @Path("getByMigrationId")
    public Response getByMigrationId(@QueryParam("migrationId") Long migrationId) {
        if (migrationId == null || migrationId < 0L) {
            return Responses.error(HTTP_BAD_REQUEST, HTTP_BAD_REQUEST, "迁移任务ID不为空!");
        }
        return Responses.success(migrationSubjobService.findByMigrationId(migrationId));
    }

    @Override
    public void clean() {
        migrationSubjobService = null;
    }

}
