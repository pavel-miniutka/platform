grammar LsfLogics;

@header { 
	package platform.server; 
	import platform.server.logics.ScriptingLogicsModule; 
	import platform.server.logics.ScriptingFormEntity;
	import platform.server.data.Union;
	import platform.server.logics.linear.LP;
	import platform.interop.ClassViewType;
	import java.util.Collections;
	import java.util.Set;
	import java.util.HashSet;
	import java.util.Arrays;
}

@lexer::header { 
	package platform.server; 
}

@members { 
	public ScriptingLogicsModule self;
	public ScriptingLogicsModule.State parseState;
}



script	
	:	importDirective* statement*;


importDirective
@after {
        if (parseState == ScriptingLogicsModule.State.GROUP) {
        	self.addImportedModule($moduleName.text);
        }
}
	:	'IMPORT' moduleName=ID ';';


statement
	:	(classStatement | groupStatement | propertyStatement | tableStatement | indexStatement | formStatement) ';';


////////////////////////////////////////////////////////////////////////////////
////////////////////////////////// CLASS STATEMENT /////////////////////////////
////////////////////////////////////////////////////////////////////////////////

classStatement 
@init {
	List<String> classParents;
	String name = null; 
	String captionStr = null;
}
@after {
	if (parseState == ScriptingLogicsModule.State.CLASS) {
		self.addScriptedClass(name, captionStr, isAbstract, classParents);
	}
}
	:	isAbstract=classDeclarant className=ID 	{ name = $className.text; }
		(caption=STRING_LITERAL { captionStr = $caption.text; })?  
		':'
		parentList=nonEmptyCompoundIdList { classParents = $parentList.ids; };	  


classDeclarant returns [boolean isAbstract]
	:	'CLASS' { $isAbstract = false; } |
		'CLASS' 'ABSTRACT' { $isAbstract = true; }; 


////////////////////////////////////////////////////////////////////////////////
////////////////////////////////// GROUP STATEMENT /////////////////////////////
////////////////////////////////////////////////////////////////////////////////

groupStatement
@init {
	String parent = null;
	String name = null;
	String captionStr = null;
}
@after {
	if (parseState == ScriptingLogicsModule.State.GROUP) {
		self.addScriptedGroup(name, captionStr, parent);
	}
}
	:	'GROUP' groupName=ID { name = $groupName.text; }
			(caption=STRING_LITERAL { captionStr = $caption.text; })?  
			(':' parentName=compoundID { parent = $parentName.text; })?;


////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////// FORM STATEMENT /////////////////////////////
////////////////////////////////////////////////////////////////////////////////

formStatement
@init {
	ScriptingFormEntity form;
}
@after {
	if (parseState == ScriptingLogicsModule.State.NAVIGATOR) {
		self.addScriptedForm(form);
	}
}
	:	declaration=formDeclaration { form = $declaration.form; }
		('OBJECTS' list=formGroupObjectsList[form] |
		'PROPERTIES' list=formPropertiesList[form] |
		'FILTERS' list=formFiltersList[form])*;

	
formDeclaration returns [ScriptingFormEntity form]
@init {
	String name = null;
	String caption = null;
}
@after {
	if (parseState == ScriptingLogicsModule.State.NAVIGATOR) {
		$form = self.createScriptedForm(name, caption);
	}
}
	:	'FORM' 
		formName=ID { name = $formName.text; }
		(formCaption=STRING_LITERAL { caption = $formCaption.text; })?;


formGroupObjectsList[ScriptingFormEntity form]  // needs refactoring
@init {
	List<List<String>> names = new ArrayList<List<String>>();
	List<List<String>> classNames = new ArrayList<List<String>>(); 
	List<ClassViewType> groupViewType = new ArrayList<ClassViewType>();
	List<Boolean> isInitType = new ArrayList<Boolean>();
}
@after {
	if (parseState == ScriptingLogicsModule.State.NAVIGATOR) {
		$form.addScriptedGroupObjects(names, classNames, groupViewType, isInitType);
	}
}
	:	groupElement=formGroupObjectDeclaration { names.add($groupElement.objectNames); classNames.add($groupElement.classIds); 
							  groupViewType.add($groupElement.type); isInitType.add($groupElement.isInitType);} 
		(',' groupElement=formGroupObjectDeclaration { names.add($groupElement.objectNames); classNames.add($groupElement.classIds); 
								groupViewType.add($groupElement.type); isInitType.add($groupElement.isInitType);})*;


formGroupObjectDeclaration returns [List<String> objectNames, List<String> classIds, ClassViewType type, boolean isInitType]
@init {
	$objectNames = new ArrayList<String>();
	$classIds = new ArrayList<String>();
}
	:	(decl=formSingleGroupObjectDeclaration { $objectNames.add($decl.name); $classIds.add($decl.className); } |
		('(' 
		objDecl=formObjectDeclaration { $objectNames.add($objDecl.name); $classIds.add($objDecl.className); }	
		(',' objDecl=formObjectDeclaration { $objectNames.add($objDecl.name); $classIds.add($objDecl.className); })+	
		')'))
		(viewType=formGroupObjectViewType { $type = $viewType.type; $isInitType = $viewType.isInitType; })?; 


formGroupObjectViewType returns [ClassViewType type, boolean isInitType]
	: ('INIT' {$isInitType = true;} | 'FIXED' {$isInitType = false;})
	  ('PANEL' {$type = ClassViewType.PANEL;} | 'HIDE' {$type = ClassViewType.HIDE;} | 'GRID' {$type = ClassViewType.GRID;});


formSingleGroupObjectDeclaration returns [String name, String className] 
	:	foDecl=formObjectDeclaration { $name = $foDecl.name; $className = $foDecl.className; };


formObjectDeclaration returns [String name, String className] 
	:	(objectName=ID { $name = $objectName.text; } '=')?	
		id=classId { $className = $id.text; }; 
	
	
formPropertiesList[ScriptingFormEntity form] 
@init {
	List<String> properties = new ArrayList<String>();
	List<List<String>> mapping = new ArrayList<List<String>>();
}
@after {
	if (parseState == ScriptingLogicsModule.State.NAVIGATOR) {
		$form.addScriptedPropertyDraws(properties, mapping);
	}
}
	:	decl=formPropertyDeclaration { properties.add($decl.name); mapping.add($decl.mapping); }
		(',' decl=formPropertyDeclaration { properties.add($decl.name); mapping.add($decl.mapping); })*;


formPropertyDeclaration returns [String name, List<String> mapping]
	:	id=compoundID { $name = $id.text; }
		'(' 
		objects=idList { $mapping = $objects.ids; } 
		')';


formFiltersList[ScriptingFormEntity form] 
@init {
	List<String> propertyNames = new ArrayList<String>();
	List<List<String>> propertyMappings = new ArrayList<List<String>>();
}
@after {
	if (parseState == ScriptingLogicsModule.State.NAVIGATOR) {
		$form.addScriptedFilters(propertyNames, propertyMappings);
	}
}
	: decl=formFilterDeclaration { propertyNames.add($decl.name); propertyMappings.add($decl.mapping);}
	  (',' decl=formFilterDeclaration { propertyNames.add($decl.name); propertyMappings.add($decl.mapping);})*;

	
formFilterDeclaration returns [String name, List<String> mapping] 
	: 'NOT' 'NULL' propDecl=formPropertyDeclaration { $name = $propDecl.name; $mapping = $propDecl.mapping; };	

////////////////////////////////////////////////////////////////////////////////
//////////////////////////////// PROPERTY STATEMENT ////////////////////////////
////////////////////////////////////////////////////////////////////////////////

propertyStatement
@init {
	LP<?> property = null;
	boolean isData = false;
	List<String> context = new ArrayList<String>();
	boolean dynamic = true;
}
	:	declaration=propertyDeclaration { if ($declaration.paramNames != null) { context = $declaration.paramNames; dynamic = false; }}
		'=' 
		(def=contextIndependentPD[false] { property = $def.property; isData = $def.isData; } | 
		expr=propertyExpression[context, dynamic] { property = $expr.property; })
		settings=commonPropertySettings[property, $declaration.name, context, isData];


propertyDeclaration returns [String name, List<String> paramNames]
	:	propName=ID { $name = $propName.text; }
		('(' paramList=idList ')' { $paramNames = $paramList.ids; })? ;


propertyExpression[List<String> context, boolean dynamic] returns [LP property, List<Integer> usedParams]
	:	pe=andPE[context, dynamic] { $property = $pe.property; $usedParams = $pe.usedParams; };


andPE[List<String> context, boolean dynamic] returns [LP property, List<Integer> usedParams]
@init {
	List<LP<?>> props = new ArrayList<LP<?>>();
	List<List<Integer>> allUsedParams = new ArrayList<List<Integer>>();
	List<Boolean> nots = new ArrayList<Boolean>();
}
@after {
	if (parseState == ScriptingLogicsModule.State.PROP) {
		ScriptingLogicsModule.LPWithParams result = self.addScriptedAndProp("", nots, props, allUsedParams);				
		$property = result.property;
		$usedParams = result.usedParams;
	}
}
	:	firstExpr=equalityPE[context, dynamic] { props.add($firstExpr.property); allUsedParams.add($firstExpr.usedParams); }
		((('AND') | ('IF')) { nots.add(false); }
		('NOT' { nots.set(nots.size()-1, true); })?
		nextExpr=equalityPE[context, dynamic] { props.add($nextExpr.property); allUsedParams.add($nextExpr.usedParams); })*;
		

equalityPE[List<String> context, boolean dynamic] returns [LP property, List<Integer> usedParams]
@init {
	LP<?> leftProp = null;
	LP<?> rightProp = null;
	List<Integer> lUsedParams = null, rUsedParams = null;
	String op = null;
}
@after {
	if (parseState == ScriptingLogicsModule.State.PROP && op != null) {
		ScriptingLogicsModule.LPWithParams result =
			self.addScriptedEqualityProp("", op, leftProp, lUsedParams, rightProp, rUsedParams);
		$property = result.property;
		$usedParams = result.usedParams;
	} else {
		$property = leftProp;
		$usedParams = lUsedParams;
	}
}
	:	lhs=relationalPE[context, dynamic] { leftProp = $lhs.property; lUsedParams = $lhs.usedParams; }
		(operand=EQ_OPERAND { op = $operand.text; }
		rhs=relationalPE[context, dynamic] { rightProp = $rhs.property; rUsedParams = $rhs.usedParams; })?;


relationalPE[List<String> context, boolean dynamic] returns [LP property, List<Integer> usedParams]
@init {
	LP<?> leftProp = null;
	LP<?> rightProp = null;
	LP<?> mainProp = null;
	List<Integer> lUsedParams = null, rUsedParams = null;
	String op = null;
}
@after {
	if (parseState == ScriptingLogicsModule.State.PROP)
	{
		if (op != null) {
			ScriptingLogicsModule.LPWithParams result =
				self.addScriptedRelationalProp("", op, leftProp, lUsedParams, rightProp, rUsedParams);
			$property = result.property;
			$usedParams = result.usedParams;
		} else if (mainProp != null) {
			$property = self.addScriptedTypeExprProp(mainProp, leftProp, lUsedParams);
			$usedParams = lUsedParams;
		} else {
			$property = leftProp;
			$usedParams = lUsedParams;
		}
	}	
}
	:	lhs=additivePE[context, dynamic] { leftProp = $lhs.property; lUsedParams = $lhs.usedParams; }
		(
			(operand=REL_OPERAND { op = $operand.text; }
			rhs=additivePE[context, dynamic] { rightProp = $rhs.property; rUsedParams = $rhs.usedParams; }) |
			
			def=typePropertyDefinition { mainProp = $def.property; }
		)?;


additivePE[List<String> context, boolean dynamic] returns [LP property, List<Integer> usedParams]
@init {
	List<LP<?>> props = new ArrayList<LP<?>>();
	List<List<Integer>> allUsedParams = new ArrayList<List<Integer>>();
	List<String> ops = new ArrayList<String>();
}
@after {
	if (parseState == ScriptingLogicsModule.State.PROP) {
		ScriptingLogicsModule.LPWithParams result = self.addScriptedAdditiveProp("", ops, props, allUsedParams);				
		$property = result.property;
		$usedParams = result.usedParams;
	}
}
	:	firstExpr=multiplicativePE[context, dynamic] { props.add($firstExpr.property); allUsedParams.add($firstExpr.usedParams); }
		( (operand=PLUS | operand=MINUS) { ops.add($operand.text); }
		nextExpr=multiplicativePE[context, dynamic] { props.add($nextExpr.property); allUsedParams.add($nextExpr.usedParams); })*;
		
	
multiplicativePE[List<String> context, boolean dynamic] returns [LP property, List<Integer> usedParams]
@init {
	List<LP<?>> props = new ArrayList<LP<?>>();
	List<List<Integer>> allUsedParams = new ArrayList<List<Integer>>();
	List<String> ops = new ArrayList<String>();
}
@after {
	if (parseState == ScriptingLogicsModule.State.PROP) {
		ScriptingLogicsModule.LPWithParams result = self.addScriptedMultiplicativeProp("", ops, props, allUsedParams);				
		$property = result.property;
		$usedParams = result.usedParams;
	}
}
	:	firstExpr=simplePE[context, dynamic] { props.add($firstExpr.property); allUsedParams.add($firstExpr.usedParams); }
		(operand=MULT_OPERAND { ops.add($operand.text); }
		nextExpr=simplePE[context, dynamic] { props.add($nextExpr.property); allUsedParams.add($nextExpr.usedParams); })*;
	
		 
	 
simplePE[List<String> context, boolean dynamic] returns [LP property, List<Integer> usedParams]
	:
	'(' expr=propertyExpression[context, dynamic] ')' { $property = $expr.property; $usedParams = $expr.usedParams; } |
	primitive=expressionPrimitive[context, dynamic] { $property = $primitive.property; $usedParams = $primitive.usedParams; } |
	uexpr=unaryMinusPE[context, dynamic] { $property = $uexpr.property; $usedParams = $uexpr.usedParams; };

	
unaryMinusPE[List<String> context, boolean dynamic] returns [LP property, List<Integer> usedParams] 	
@after {
	if (parseState == ScriptingLogicsModule.State.PROP) { 
		$property = self.addScriptedUnaryMinusProp("", $property, $usedParams);
	}
}
	: MINUS expr=simplePE[context, dynamic] { $property = $expr.property; $usedParams = $expr.usedParams; };		 
	

expressionPrimitive[List<String> context, boolean dynamic] returns [LP property, List<Integer> usedParams]
	:	(paramName=parameter {
			if (parseState == ScriptingLogicsModule.State.PROP)
				$usedParams = Collections.singletonList(self.getParamIndex($paramName.text, $context, $dynamic));
		 })
		|
		(expr=contextDependentPD[context, dynamic] { $property = $expr.property; $usedParams = $expr.usedParams; })
	;

propertyDefinition[List<String> context, boolean dynamic] returns [LP property, List<Integer> usedParams]
	:
		propertyExpr=contextDependentPD[context, dynamic] { $property = $propertyExpr.property; $usedParams = $propertyExpr.usedParams; } |
		propertyExprI=contextIndependentPD[true] { $property = $propertyExprI.property; $usedParams = new ArrayList<Integer>(); }
	;

contextDependentPD[List<String> context, boolean dynamic] returns [LP property, List<Integer> usedParams]
	:	joinDef=joinPropertyDefinition[context, dynamic] { $property = $joinDef.property; $usedParams = $joinDef.usedParams; } |
		unionDef=unionPropertyDefinition[context, dynamic] { $property = $unionDef.property; $usedParams = $unionDef.usedParams; } |
		constDef=literal { $property = $constDef.property; $usedParams = new ArrayList<Integer>(); }
	;

contextIndependentPD[boolean innerPD] returns [LP property, boolean isData]
	: 	dataDef=dataPropertyDefinition[innerPD] { $property = $dataDef.property; $isData = true; } |
		groupDef=groupPropertyDefinition { $property = $groupDef.property; $isData = false; } |
		typeDef=typePropertyDefinition { $property = $typeDef.property; $isData = false; }
	;

joinPropertyDefinition[List<String> context, boolean dynamic] returns [LP property, List<Integer> usedParams]
@init {
	List<LP<?>> paramProps = new ArrayList<LP<?>>();
	List<List<Integer>> usedSubParams = new ArrayList<List<Integer>>();
	LP mainProp = null;
}
@after {
	if (parseState == ScriptingLogicsModule.State.PROP) {
		ScriptingLogicsModule.LPWithParams result = self.addScriptedJProp("", mainProp, paramProps, usedSubParams);
		$property = result.property;
		$usedParams = result.usedParams;
	}
}
	:	mainPropObj=propertyObject { mainProp = $mainPropObj.property; }
		'('
			(firstParam=propertyExpression[context, dynamic] { paramProps.add($firstParam.property); usedSubParams.add($firstParam.usedParams); }
			(',' nextParam=propertyExpression[context, dynamic] { paramProps.add($nextParam.property); usedSubParams.add($nextParam.usedParams);})* )?
		')';




groupPropertyDefinition returns [LP property, List<Integer> usedParams]
@init {
	List<LP<?>> paramProps = new ArrayList<LP<?>>();
	List<List<Integer>> usedParams = new ArrayList<List<Integer>>();
	LP<?> groupProp = null;
	String groupPropName;
	boolean isSGProp = true;
	List<String> groupContext = null;
}
@after {
	if (parseState == ScriptingLogicsModule.State.PROP) {
		$property = self.addScriptedGProp("", groupProp, isSGProp, paramProps, usedParams);
	}
}
	:	'GROUP' (('SUM') { isSGProp = true; } | ('MAX') { isSGProp = false; })
		prop=propertyObject { groupProp = $prop.property; groupPropName = $prop.propName; }
		'BY'
		{
			groupContext = $prop.innerContext;
			if (groupPropName != null && parseState == ScriptingLogicsModule.State.PROP)
				groupContext = self.getNamedParamsList(groupPropName);
		}
		(firstParam=propertyExpression[groupContext, false] { paramProps.add($firstParam.property); usedParams.add($firstParam.usedParams); }
		(',' nextParam=propertyExpression[groupContext, false] { paramProps.add($nextParam.property); usedParams.add($nextParam.usedParams);})* )
		;


dataPropertyDefinition[boolean innerPD] returns [LP property]
@init {
	List<String> paramClassNames;
	String returnClass = null;
}
@after {
	if (parseState == ScriptingLogicsModule.State.PROP) {
		$property = self.addScriptedDProp("", returnClass, paramClassNames, innerPD);
	}
}
	:	'DATA'
		retClass=classId { returnClass = $retClass.text; }
		'('
			classIds=classIdList { paramClassNames = $classIds.ids; }
		')';



unionPropertyDefinition[List<String> context, boolean dynamic] returns [LP property, List<Integer> usedParams]
@init {
	List<LP<?>> paramProps = new ArrayList<LP<?>>();
	List<List<Integer>> usedParams = new ArrayList<List<Integer>>();
	Union type = null;
}
@after {
	if (parseState == ScriptingLogicsModule.State.PROP) {
		$property = self.addScriptedUProp("", type, paramProps, usedParams);
	}
}
	:	'UNION'
		(('MAX' {type = Union.MAX;}) | ('SUM' {type = Union.SUM;}) | ('OVERRIDE' {type = Union.OVERRIDE;}) | ('XOR' { type = Union.XOR;}) | ('EXCLUSIVE' {type = Union.EXCLUSIVE;}))
		'('
		firstParam=propertyExpression[context, dynamic] { paramProps.add($firstParam.property); usedParams.add($firstParam.usedParams); }
		(',' nextParam=propertyExpression[context, dynamic] { paramProps.add($nextParam.property); usedParams.add($nextParam.usedParams);})*
		')';


typePropertyDefinition returns [LP property] 
@init {
	String clsId = null;
	boolean bIs = false;
}
@after {
	if (parseState == ScriptingLogicsModule.State.PROP) {
		$property = self.addScriptedTypeProp(clsId, bIs);
	}	
}
	:	('IS' { bIs = true; } | 'AS')
		id=classId { clsId = $id.text; };


propertyObject returns [LP property, String propName, List<String> innerContext]
@init {
	List<String> newContext = new ArrayList<String>(); 
}
	:	name=compoundID	{ $property = self.getLPByName($name.text); $propName = $name.text; } |
		'[' 
		(expr=propertyExpression[newContext, true] { $property = $expr.property; $innerContext = newContext; } |
		def=contextIndependentPD[true] { $property = $def.property; })
		']' ;


commonPropertySettings[LP property, String propertyName, List<String> namedParams, boolean isData] 
@init {
	String groupName = null;
	boolean isPersistent = false;	
}
@after {
	if (parseState == ScriptingLogicsModule.State.PROP) { 
		self.addSettingsToProperty(property, propertyName, namedParams, groupName, isPersistent, isData);	
	}
} 
	: 	('IN' name=compoundID { groupName = $name.text; })?
		('PERSISTENT' { isPersistent = true; })?;



////////////////////////////////////////////////////////////////////////////////
////////////////////////////////// TABLE STATEMENT /////////////////////////////
////////////////////////////////////////////////////////////////////////////////

tableStatement 
	:	't';


////////////////////////////////////////////////////////////////////////////////
////////////////////////////////// INDEX STATEMENT /////////////////////////////
////////////////////////////////////////////////////////////////////////////////

indexStatement
	:	'z';


////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////// COMMON /////////////////////////////////
////////////////////////////////////////////////////////////////////////////////

parameter 
	:	ID | NUMBERED_PARAM;

	
idList returns [List<String> ids] 
@init {
	ids = new ArrayList<String>();	
} 
	: (neIdList=nonEmptyIdList { ids = $neIdList.ids; })?;

classIdList returns [List<String> ids]
@init {
	ids = new ArrayList<String>();
}
	:	((firstClassName=classId { ids.add($firstClassName.text); })
		(',' className=classId { ids.add($className.text); })*)?;

compoundIdList returns [List<String> ids] 
@init {
	ids = new ArrayList<String>();	
} 
	: (neIdList=nonEmptyCompoundIdList { ids = $neIdList.ids; })?;

nonEmptyIdList returns [List<String> ids]
@init {
	ids = new ArrayList<String>(); 
}
	:	firstId=ID	{ $ids.add($firstId.text); }
		(',' nextId=ID	{ $ids.add($nextId.text); })*;

nonEmptyCompoundIdList returns [List<String> ids]
@init {
	ids = new ArrayList<String>();
}
	:	firstId=compoundID	{ $ids.add($firstId.text); }
		(',' nextId=compoundID	{ $ids.add($nextId.text); })*;

parameterList returns [List<String> ids]
@init {
	ids = new ArrayList<String>();
}
	:	(firstParam=parameter	 { $ids.add($firstParam.text); }
		(',' nextParam=parameter { $ids.add($nextParam.text); })* )?;


literal returns [LP property]
@init {
	ScriptingLogicsModule.ConstType cls = null;
	String text = null;
}
@after {
	if (parseState == ScriptingLogicsModule.State.PROP) { 
		$property = self.addConstantProp(cls, text);	
	}
}
	: 	strInt=intLiteral	{ cls = ScriptingLogicsModule.ConstType.INT; text = $strInt.text; } | 
		strReal=doubleLiteral	{ cls = ScriptingLogicsModule.ConstType.REAL; text = $strReal.text; } |
		str=STRING_LITERAL	{ cls = ScriptingLogicsModule.ConstType.STRING; text = $str.text; } | 
		str=LOGICAL_LITERAL	{ cls = ScriptingLogicsModule.ConstType.LOGICAL; text = $str.text; };
	
classId 
	:	compoundID | PRIMITIVE_TYPE;

compoundID
	:	(ID '.')? ID;
	
doubleLiteral 
	:	POSITIVE_DOUBLE_LITERAL; 
		

intLiteral
	:	UINT_LITERAL;		



/////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////// LEXER //////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////
	
fragment NEWLINE	:   '\r'?'\n'; 
fragment SPACE		:   (' '|'\t');
fragment STR_LITERAL_CHAR	: '\\\'' | ~('\r'|'\n'|'\'');	 // overcomplicated due to bug in ANTLR Works
fragment DIGITS		:	('0'..'9')+;
	 
PRIMITIVE_TYPE  :	'INTEGER' | 'DOUBLE' | 'LONG' | 'BOOLEAN' | 'DATE' | 'STRING[' DIGITS ']' | 'ISTRING[' DIGITS ']';		
LOGICAL_LITERAL :	'TRUE' | 'FALSE';		
ID          	:	('a'..'z'|'A'..'Z')('a'..'z'|'A'..'Z'|'_'|'0'..'9')*;
WS		:	(NEWLINE | SPACE) { $channel=HIDDEN; }; 	
STRING_LITERAL	:	'\'' STR_LITERAL_CHAR* '\'';
COMMENTS	:	('//' .* '\n') { $channel=HIDDEN; };
UINT_LITERAL 	:	DIGITS;
POSITIVE_DOUBLE_LITERAL	: 	DIGITS '.' DIGITS;	  
NUMBERED_PARAM	:	'$' DIGITS;
EQ_OPERAND	:	('==') | ('!='); 
REL_OPERAND	: 	('<') | ('>') | ('<=') | ('>=');
MINUS		:	'-';
PLUS		:	'+';
MULT_OPERAND	:	('*') | ('/');