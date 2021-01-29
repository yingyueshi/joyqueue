package org.joyqueue.handler.routing.command.migration;

import com.jd.laf.web.vertx.annotation.Body;
import com.jd.laf.web.vertx.annotation.Path;
import com.jd.laf.web.vertx.response.Response;
import com.jd.laf.web.vertx.response.Responses;
import org.joyqueue.handler.error.ErrorCode;
import org.joyqueue.handler.routing.command.CommandSupport;
import org.joyqueue.model.domain.Identity;
import org.joyqueue.model.domain.migration.MigrationTask;
import org.joyqueue.model.query.QMigrationTask;
import org.joyqueue.service.MigrationTaskService;
import org.springframework.beans.factory.annotation.Value;

import java.util.Date;
import java.util.List;

import static com.jd.laf.web.vertx.response.Response.HTTP_BAD_REQUEST;
import static org.joyqueue.model.domain.migration.MigrationTask.ScopeType.ALL;

public class MigrationTaskCommand extends CommandSupport<MigrationTask, MigrationTaskService, QMigrationTask> {

    @Path("analysis")
    public Response analysis(@Body MigrationTask model) throws Exception {

        try {
            model.setCreateBy(new Identity(session));
            return Responses.success(service.analysis(model, false));
        } catch (Exception e) {
            return Responses.success(e.getMessage());
        }
    }

    @Path("add")
    @Override
    public Response add(@Body MigrationTask model) throws Exception {
        // 校验
        List<MigrationTask> tasks = service.findUnfinishedBySrcBrokerId(model.getSrcBrokerId());
        if (tasks.size() > 0) {
            return Responses.error(HTTP_BAD_REQUEST, HTTP_BAD_REQUEST, "该Broker已经存在正在执行的迁移任务!");
        }
        // 添加
        model.setCreateBy(new Identity(session));
        return Responses.success(service.analysis(model, true));
    }

}
