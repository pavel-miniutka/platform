package lsfusion.server.logics.controller.init;

import lsfusion.server.base.task.GroupSingleTask;
import lsfusion.server.logics.BusinessLogics;
import lsfusion.server.logics.action.session.DataSession;
import lsfusion.server.physics.dev.id.name.DBNamingPolicy;
import lsfusion.server.physics.exec.db.controller.manager.DBManager;

import java.sql.SQLException;

public abstract class BLGroupSingleTask<T> extends GroupSingleTask<T> {

    private BusinessLogics BL;

    public BusinessLogics getBL() {
        return BL;
    }

    public void setBL(BusinessLogics BL) {
        this.BL = BL;
    }

    protected DBManager getDbManager() {
        return getBL().getDbManager();
    }

    protected DBNamingPolicy getDBNamingPolicy() {
        return getDbManager().getNamingPolicy();
    }

    protected DataSession createSession() throws SQLException {
        return getDbManager().createSession();
    }
}
