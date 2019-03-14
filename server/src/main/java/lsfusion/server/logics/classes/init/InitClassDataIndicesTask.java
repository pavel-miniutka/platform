package lsfusion.server.logics.classes.init;

import lsfusion.server.logics.init.SimpleBLTask;
import org.apache.log4j.Logger;

public class InitClassDataIndicesTask extends SimpleBLTask {

    public String getCaption() {
        return "Initializing class data indexes";
    }

    public void run(Logger logger) {
        getBL().initClassDataIndices();
    }
}