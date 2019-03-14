package lsfusion.server.logics.classes.utils.time;

import lsfusion.server.language.linear.LCP;
import lsfusion.server.language.ScriptingLogicsModule;
import org.antlr.runtime.RecognitionException;

import java.io.IOException;


public class TimeLogicsModule extends ScriptingLogicsModule{

    public LCP currentDateTime;
    public LCP currentDate;

    public TimeLogicsModule(BusinessLogics BL, BaseLogicsModule baseLM) throws IOException {
        super(TimeLogicsModule.class.getResourceAsStream("/system/Time.lsf"), "/system/Time.lsf", baseLM, BL);
    }

    @Override
    public void initMainLogic() throws RecognitionException {
        super.initMainLogic();

        currentDateTime = findProperty("currentDateTime[]");
        currentDate = findProperty("currentDate[]");
    }
}