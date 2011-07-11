package platform.server.logics;

import net.sf.jasperreports.engine.JRException;
import org.antlr.runtime.ANTLRFileStream;
import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.apache.log4j.Logger;
import platform.base.BaseUtils;
import platform.server.LsfLogicsLexer;
import platform.server.LsfLogicsParser;
import platform.server.classes.*;
import platform.server.logics.linear.LP;
import platform.server.logics.property.group.AbstractGroup;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

/**
 * User: DAle
 * Date: 03.06.11
 * Time: 14:54
 */

public class ScriptingLogicsModule extends LogicsModule {
    private final static Logger scriptLogger = Logger.getLogger(ScriptingLogicsModule.class);
    private String scriptName;
    private String code = null;
    private String filename = null;
    private final BusinessLogics<?> BL;

    private final Map<String, List<String>> classes = new HashMap<String, List<String>>();
    private final Set<String> importedModules = new HashSet<String>();

    public enum State {GROUP, CLASS, PROP}

    private Map<String, ValueClass> primitiveTypeAliases = BaseUtils.buildMap(
            Arrays.<String>asList("Integer", "Double", "Long", "Date", "Boolean"),
            Arrays.<ValueClass>asList(IntegerClass.instance, DoubleClass.instance, LongClass.instance, DateClass.instance, LogicalClass.instance)
    );

    private ScriptingLogicsModule(String scriptName, BaseLogicsModule<?> baseModule, BusinessLogics<?> BL) {
        super(scriptName);
        setBaseLogicsModule(baseModule);
        this.scriptName = scriptName;
        this.BL = BL;
    }

    public static ScriptingLogicsModule createFromString(String scriptName, String code, BaseLogicsModule<?> baseModule, BusinessLogics<?> BL) {
        ScriptingLogicsModule module = new ScriptingLogicsModule(scriptName, baseModule, BL);
        module.code = code;
        return module;
    }

    public static ScriptingLogicsModule createFromFile(String scriptName, String filename, BaseLogicsModule<?> baseModule, BusinessLogics<?> BL) {
        ScriptingLogicsModule module = new ScriptingLogicsModule(scriptName, baseModule, BL);
        module.filename = filename;
        return module;
    }

    private CharStream createStream() throws IOException {
        if (code != null) {
            return new ANTLRStringStream(code);
        } else {
            return new ANTLRFileStream(filename);
        }
    }

    public void addImportedModule(String moduleName) {
        scriptLogger.info("import " + moduleName + ";");
        importedModules.add(moduleName);
    }

    protected LogicsModule getModule(String sid) {
        List<LogicsModule> modules = BL.getLogicModules();
        for (LogicsModule module : modules) {
            if (module.getSID().equals(sid)) {
                return module;
            }
        }
        return null;
    }

    private String transformCaptionStr(String captionStr) {
        String caption = captionStr.replace("\'", "'");
        return caption.substring(1, captionStr.length()-1);
    }

    private ValueClass getPredefinedClass(String name) {
        if (primitiveTypeAliases.containsKey(name)) {
            return primitiveTypeAliases.get(name);
        } else if (name.startsWith("String[")) {
            name = name.substring("String[".length(), name.length() - 1);
            return StringClass.get(Integer.parseInt(name));
        } else if (name.startsWith("InsensitiveString[")) {
            name = name.substring("InsensitiveString[".length(), name.length() - 1);
            return InsensitiveStringClass.get(Integer.parseInt(name));
        }
        return null;
    }

    private ValueClass getClassByName(String name) {
            ValueClass valueClass = getPredefinedClass(name);
            if (valueClass == null) {
                int dotPosition = name.indexOf('.');
                if (dotPosition > 0) {
                    LogicsModule module = getModule(name.substring(0, dotPosition));
                    valueClass = module.getClass(module.transformNameToSID(name.substring(dotPosition + 1)));
                } else {
                    valueClass = getClass(transformNameToSID(name));
                    if (valueClass == null) {
                        for (String importModuleName : importedModules) {
                            LogicsModule module = getModule(importModuleName);
                            if ((valueClass = module.getClass(module.transformNameToSID(name))) != null) {
                                break;
                            }
                        }
                    }
                }
            }
            return valueClass;
    }

    public void addScriptedClass(String className, String captionStr, boolean isAbstract, List<String> parentNames) {
        scriptLogger.info("addScriptedClass(" + className + ", " + (captionStr==null ? "" : captionStr) + ", " + isAbstract + ", " + parentNames + ");");
        String caption = (captionStr == null ? className : transformCaptionStr(captionStr));
        CustomClass[] parents = new CustomClass[parentNames.size()];
        for (int i = 0; i < parentNames.size(); i++) {
            String parentName = parentNames.get(i);
            ValueClass valueClass = getClassByName(parentName);
            assert valueClass instanceof CustomClass;
            parents[i] = (CustomClass) valueClass;
        }
        if (isAbstract) {
            addAbstractClass(className, caption, parents);
        } else {
            addConcreteClass(className, caption, parents);
        }
    }

    private AbstractGroup getGroupByName(String name) {
        AbstractGroup group;
        int dotPosition = name.indexOf('.');
        if (dotPosition > 0) {
            LogicsModule module = getModule(name.substring(0, dotPosition));
            group = module.getGroup(module.transformNameToSID(name.substring(dotPosition + 1)));
        } else {
            group = getGroup(transformNameToSID(name));
            if (group == null) {
                for (String importModuleName : importedModules) {
                    LogicsModule module = getModule(importModuleName);
                    if ((group = module.getGroup(module.transformNameToSID(name))) != null) {
                        break;
                    }
                }
            }
        }
        return group;
    }

    private LP<?> getLPByName(String name) {
        LP<?> property;
        int dotPosition = name.indexOf('.');
        if (dotPosition > 0) {
            LogicsModule module = getModule(name.substring(0, dotPosition));
            property = module.getLP(module.transformNameToSID(name.substring(dotPosition + 1)));
        } else {
            property = getLP(transformNameToSID(name));
            if (property == null) {
                for (String importModuleName : importedModules) {
                    LogicsModule module = getModule(importModuleName);
                    if ((property = module.getLP(module.transformNameToSID(name))) != null) {
                        break;
                    }
                }
            }
        }
        return property;
    }

    public void addScriptedGroup(String groupName, String captionStr, String parentName) {
        scriptLogger.info("addScriptedGroup(" + groupName + ", " + (captionStr==null ? "" : captionStr) + ", " + (parentName == null ? "null" : parentName) + ");");
        String caption = (captionStr == null ? groupName : transformCaptionStr(captionStr));
        AbstractGroup parentGroup = (parentName == null ? null : getGroupByName(parentName));
        addAbstractGroup(groupName, caption, parentGroup);
    }

    public void addScriptedDProp(String propName, String caption, String parentGroup, String returnClass, List<String> paramClasses, boolean isPersistent) {
        scriptLogger.info("addScriptedDProp(" + propName + ", " + (parentGroup == null ? "" : parentGroup) + ", " + returnClass + ", " + paramClasses + ", " + isPersistent + ");");
        AbstractGroup group = (parentGroup == null ? privateGroup : getGroupByName(parentGroup));
        ValueClass value = getClassByName(returnClass);
        ValueClass[] params = new ValueClass[paramClasses.size()];
        for (int i = 0; i < paramClasses.size(); i++) {
            params[i] = getClassByName(paramClasses.get(i));
        }
        addDProp(group, propName, isPersistent, caption, value, params);
    }

    public void addScriptedJProp(String propName, String caption, String parentGroup, String mainPropName, boolean isPersistent, List<String> namedParams, List<String> paramsId, List<List<String>> propParams) {
        scriptLogger.info("addScriptedJProp(" + propName + ", " + (parentGroup == null ? "" : parentGroup) + ", " + mainPropName + ", " + isPersistent + ", " + namedParams + ", " + paramsId + ", " + propParams + ");");
        AbstractGroup group = (parentGroup == null ? privateGroup : getGroupByName(parentGroup));
        LP<?> mainProp = getLPByName(mainPropName);
        List<Object> resultParams = new ArrayList<Object>();
        for (int i = 0; i < paramsId.size(); i++) {
            if (propParams.get(i) == null) {
                resultParams.add(namedParams.indexOf(paramsId.get(i)) + 1);
            } else {
                LP<?> paramProp = getLPByName(paramsId.get(i));
                resultParams.add(paramProp);
                for (String namedParam : propParams.get(i)) {
                    resultParams.add(namedParams.indexOf(namedParam) + 1);
                }
            }
        }
        addJProp(group, false, propName, isPersistent, caption, mainProp, resultParams.toArray());
    }

    private void parseStep(State state) {
        try {
            LsfLogicsLexer lexer = new LsfLogicsLexer(createStream());
            LsfLogicsParser parser = new LsfLogicsParser(new CommonTokenStream(lexer));
            parser.self = this;
            parser.parseState = state;
            parser.script();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void initClasses() {
        parseStep(ScriptingLogicsModule.State.CLASS);
    }

    @Override
    public void initTables() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void initGroups() {
        parseStep(ScriptingLogicsModule.State.GROUP);
    }

    @Override
    public void initProperties()  {
        parseStep(ScriptingLogicsModule.State.PROP);
    }

    @Override
    public void initIndexes() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void initNavigators() throws JRException, FileNotFoundException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getNamePrefix() {
        return scriptName;
    }
}
