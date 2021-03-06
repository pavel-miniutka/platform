package lsfusion.server.logics.navigator.controller.init;

import lsfusion.base.col.interfaces.immutable.ImSet;
import lsfusion.server.logics.controller.init.BLGroupSingleSplitTask;
import lsfusion.server.logics.navigator.NavigatorElement;

public abstract class GroupNavigatorElementsTask extends BLGroupSingleSplitTask<NavigatorElement> {

    @Override
    protected ImSet<NavigatorElement> getObjects() {
        return getBL().getNavigatorElements();
    }
}
