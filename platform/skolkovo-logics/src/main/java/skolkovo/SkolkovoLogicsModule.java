package skolkovo;

//import com.smartgwt.client.docs.Files;

import jasperapi.ReportGenerator;
import net.sf.jasperreports.engine.JRAbstractExporter;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import platform.base.BaseUtils;
import platform.base.IOUtils;
import platform.interop.ClassViewType;
import platform.interop.Compare;
import platform.interop.action.ClientAction;
import platform.interop.action.MessageClientAction;
import platform.interop.form.RemoteFormInterface;
import platform.interop.form.layout.DoNotIntersectSimplexConstraint;
import platform.server.classes.*;
import platform.server.data.*;
import platform.server.data.Time;
import platform.server.data.expr.query.OrderType;
import platform.server.data.query.Query;
import platform.server.form.entity.*;
import platform.server.form.entity.filter.*;
import platform.server.form.instance.PropertyObjectInterfaceInstance;
import platform.server.form.instance.remote.RemoteForm;
import platform.server.form.navigator.NavigatorElement;
import platform.server.form.view.*;
import platform.server.form.window.ToolBarNavigatorWindow;
import platform.server.form.window.TreeNavigatorWindow;
import platform.server.logics.*;
import platform.server.logics.linear.LP;
import platform.server.logics.property.ActionProperty;
import platform.server.logics.property.ClassPropertyInterface;
import platform.server.logics.property.ExecutionContext;
import platform.server.logics.property.Property;
import platform.server.logics.property.group.AbstractGroup;
import platform.server.mail.EmailActionProperty;
import skolkovo.actions.CopyProjectActionProperty;
import skolkovo.actions.ExportProjectDocumentsActionProperty;
import skolkovo.actions.ImportProjectsActionProperty;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.List;

import static platform.base.BaseUtils.nvl;

/**
 * User: DAle
 * Date: 24.05.11
 * Time: 18:21
 */


public class SkolkovoLogicsModule extends LogicsModule {
    private final SkolkovoBusinessLogics BL;

    public SkolkovoLogicsModule(BaseLogicsModule<SkolkovoBusinessLogics> baseLM, SkolkovoBusinessLogics BL) {
        super("SkolkovLogicsModule");
        setBaseLogicsModule(baseLM);
        this.BL = BL;
    }

    private LP inExpertVoteDateFromDateTo;
    private LP quantityInExpertDateFromDateTo;
    private LP emailClaimerAcceptedHeaderVote;
    private LP emailClaimerNameVote;
    private LP emailClaimerRejectedHeaderVote;
    private LP emailClaimerAcceptedHeaderProject;
    private LP emailAcceptedProjectEA;
    private LP emailAcceptedProject;
    public LP sidProject;
    public LP sidToProject;
    public LP nameNativeToCluster;
    public LP nameNativeCluster;
    public LP dateProject;
    public LP dateJoinProject;
    public LP dateDataProject;
    public LP dateStatusProject;
    public LP statusDateProject;
    public LP dateStatusDataProject;
    public LP updateDateProject;
    public LP nativeNumberToPatent;
    public LP nativeNumberSIDToPatent;

    private LP emailNoticeRejectedVoteEA;
    private LP emailNoticeRejectedVote;

    private LP emailNoticeAcceptedStatusVoteEA;
    private LP emailNoticeAcceptedStatusVote;

    private LP emailNoticeAcceptedPreliminaryVoteEA;
    private LP emailNoticeAcceptedPreliminaryVote;

    AbstractCustomClass multiLanguageNamed;

    public ConcreteCustomClass project;
    public ConcreteCustomClass expert;
    public ConcreteCustomClass cluster;
    public ConcreteCustomClass claimer;
    public ConcreteCustomClass claimerExpert;
    ConcreteCustomClass documentTemplate;
    ConcreteCustomClass documentAbstract;
    ConcreteCustomClass documentTemplateDetail;
    ConcreteCustomClass document;

    public ConcreteCustomClass nonRussianSpecialist;
    public ConcreteCustomClass academic;
    public ConcreteCustomClass patent;

    ConcreteCustomClass vote;

    ConcreteCustomClass voteR1;
    ConcreteCustomClass voteR2;

    public StaticCustomClass projectType;
    public StaticCustomClass projectAction;
    StaticCustomClass language;
    StaticCustomClass documentType;
    public StaticCustomClass ownerType;

    StaticCustomClass voteResult;
    StaticCustomClass projectStatus;

    StaticCustomClass formalControlResult;
    ConcreteCustomClass formalControl;
    StaticCustomClass legalCheckResult;
    StaticCustomClass originalDocsCheckResult;
    ConcreteCustomClass legalCheck;
    ConcreteCustomClass originalDocsCheck;
    ConcreteCustomClass currency;

    AbstractCustomClass application;
    ConcreteCustomClass applicationPreliminary;
    ConcreteCustomClass applicationStatus;

    AbstractGroup projectInformationGroup;
    AbstractGroup additionalInformationGroup;
    AbstractGroup innovationGroup;
    AbstractGroup sourcesFundingGroup;
    AbstractGroup nonReturnFundingGroup;
    AbstractGroup equipmentGroup;
    AbstractGroup projectDocumentsGroup;
    AbstractGroup executiveSummaryGroup, applicationFormGroup, techDescrGroup, roadMapGroup, resolutionIPGroup;
    AbstractGroup projectStatusGroup;
    AbstractGroup projectOptionsGroup;
    AbstractGroup translateActionGroup;
    AbstractGroup projectTranslationsGroup;
    AbstractGroup projectOtherClusterGroup;
    AbstractGroup consultingCenterGroup;
    AbstractGroup consultingCenterStatGroup;

    AbstractGroup voteResultGroup;
    AbstractGroup voteResultCheckGroup;
    AbstractGroup voteResultCommentGroup;
    AbstractGroup formalControlResultGroup;
    AbstractGroup legalCheckResultGroup;
    AbstractGroup registerGroup;
    AbstractGroup originalDoscCheckGroup;

    AbstractGroup contactGroup;
    AbstractGroup documentGroup;
    AbstractGroup legalDataGroup;
    AbstractGroup claimerInformationGroup;

    AbstractGroup expertResultGroup;
    AbstractGroup importGroup;

    @Override
    public void initClasses() {
        initBaseClassAliases();

        multiLanguageNamed = addAbstractClass("multiLanguageNamed", "Многоязычный объект", baseClass);

        projectType = addStaticClass("projectType", "Тип проекта",
                new String[]{"comparable", "surpasses", "russianbenchmark", "certainadvantages", "significantlyoutperforms", "nobenchmarks"},
                new String[]{"сопоставим с существующими российскими аналогами или уступает им", "превосходит российские аналоги, но уступает лучшим зарубежным аналогам",
                        "Является российским аналогом востребованного зарубежного продукта/технологии", "Обладает отдельными преимуществами над лучшими мировыми аналогами, но в целом сопоставим с ними",
                        "Существенно превосходит все существующие мировые аналоги", "Не имеет аналогов, удовлетворяет ранее не удовлетворенную потребность и создает новый рынок"});

        projectAction = addStaticClass("projectAction", "Тип заявки",
                new String[]{"preliminary", "status"},
                new String[]{"Предварительная экспертиза", "Статус участника"});

        ownerType = addStaticClass("ownerType", "Тип правообладателя",
                new String[]{"employee", "participant", "thirdparty"},
                new String[]{"Работником организации", "Участником организации", "Третьим лицом"});

        patent = addConcreteClass("patent", "Патент", baseClass);
        academic = addConcreteClass("academic", "Научные кадры", baseClass);
        nonRussianSpecialist = addConcreteClass("nonRussianSpecialist", "Иностранный специалист", baseClass);

        project = addConcreteClass("project", "Проект", multiLanguageNamed, baseLM.transaction);
        expert = addConcreteClass("expert", "Эксперт", baseLM.customUser);
        cluster = addConcreteClass("cluster", "Кластер", multiLanguageNamed);

        claimer = addConcreteClass("claimer", "Заявитель", multiLanguageNamed, baseLM.emailObject);
        claimer.dialogReadOnly = false;

        claimerExpert = addConcreteClass("claimerExpert", "Заявитель/эксперт", claimer, expert);

        documentTemplate = addConcreteClass("documentTemplate", "Шаблон документов", baseClass.named);

        documentAbstract = addConcreteClass("documentAbstract", "Документ (абстр.)", baseClass);

        documentTemplateDetail = addConcreteClass("documentTemplateDetail", "Документ (прототип)", documentAbstract);

        document = addConcreteClass("document", "Документ", documentAbstract);

        vote = addConcreteClass("vote", "Заседание", baseClass, baseLM.transaction);

        voteR1 = addConcreteClass("voteR1", "Заседание (регл. 1)", vote);
        voteR2 = addConcreteClass("voteR2", "Заседание (регл. 2)", vote);

        currency = addConcreteClass("currency", "Валюта", baseClass.named);

        language = addStaticClass("language", "Язык",
                new String[]{"russian", "english"},
                new String[]{"Русский", "Английский"});

        voteResult = addStaticClass("voteResult", "Результат заседания",
                new String[]{"refused", "connected", "voted"},
                new String[]{"Отказался", "Аффилирован", "Проголосовал"});

        projectStatus = addStaticClass("projectStatus", "Статус проекта",
                new String[]{"unknown", "needTranslation", "needExtraVote", "inProgress", "accepted", "rejected",
                     "notEnoughDocsForPreliminary", "notEnoughDocsForStatus", "noExperts", "noCluster", "positiveFCResult", "negativeLCResult", "positiveLCResult",
                     "registered", "repeated", "withdrawn", "overdueFC", "overdueLC",
                     "issuedVoteDocs", "applyStatus", "sentRejected", "sentPreliminaryAccepted", "sentStatusAccepted", "inProgressRepeat",
                     "haveStatus", "notEnoughOriginalDocs", "overdueOriginalDocs", "appliedOriginalDocs", "sentForSignature", "signed", "sentToFinDep",
                     "submittedToRegister", "preparedCertificate", "certified"},
                new String[]{"Неизвестный статус", "Направлена на перевод", "Требуется заседание (повторное)", "Идет заседание", "Оценен положительно", "Оценен отрицательно",
                     "Неполный перечень документов (на экспертизу)","Неполный перечень документов (на статус)",  "Отсутствует перечень экспертов", "Не соответствует направлению", "Направлена на юридическую проверку", "Не прошла юридическую проверку", "Прошла юридическую проверку",
                     "Зарегистирована", "Подана повторно", "Отозвана заявителем", "Не исправлена в срок (ФЭ)", "Не исправлена в срок (ЮП)",
                     "Оформление документов по заседанию", "Подана заявка на статус", "Отправлено отрицательное решение", "Отправлено положительное решение предв.экспертизы", "Отправлено положительное решение экспертизы на статус", "Идет заседание (повторное)",
                     "Оставлена без рассмотрения", "Неполный пакет оригиналов документов", "Пакет оригиналов документов не пополнен в срок", "Предоставлены документы в бумажном виде", "Решение передано на подпись", "Решение подписано", "Документы переданы в Финансовый департамент",
                     "Внесен в реестр участников", "Подготовлено свидетельство участника", "Выдано свидетельство участника"});

        documentType = addStaticClass("documentType", "Тип документа",
                new String[]{"application", "resume", "techdesc", "forres", "ipres", "roadmap"},
                new String[]{"Анкета", "Резюме", "Техническое описание", "Резюме иностранного специалиста", "Заявление IP", "Дорожная карта"});

        formalControlResult = addStaticClass("formalControlResult", "Решение формальной экспертизы",
                new String[]{"notEnoughDocuments", "noListOfExperts", "notSuitableCluster", "repeatedFC", "positiveFormalResult"},
                new String[]{"Неполный перечень документов", "Отсутствует перечень экспертов", "Не соответствует направлению", "Подана повторно", "Прошла формальную экспертизу"});

        legalCheckResult = addStaticClass("legalCheckResult", "Решение юридической проверки",
                new String[]{"negativeLegalCheckResult", "positiveLegalCheckResult"},
                new String[]{"Не прошла юридическую проверку", "Прошла юридическую проверку"});

        originalDocsCheckResult = addStaticClass("originalDocsCheckResult", "Проверка оригиналов документов",
                new String[]{"notCompleteOriginalDocsPacket", "completeOriginalDocsPacket"},
                new String[]{"Подан неполный пакет документов", "Подан полный пакет документов"});

        formalControl = addConcreteClass("formalControl", "Формальная экспертиза", baseClass);
        legalCheck = addConcreteClass("legalCheck", "Юридическая проверка", baseClass);
        originalDocsCheck = addConcreteClass("originalDocsCheck", "Проверка оригиналов документов", baseClass);;

        application = addAbstractClass("application", "Заявка", baseClass);
        applicationPreliminary = addConcreteClass("applicationPreliminary", "Заявка на предварительную экспертизу", application);
        applicationStatus = addConcreteClass("applicationStatus", "Заявка на статус участника", application);
    }

    @Override
    public void initTables() {
        baseLM.tableFactory.include("multiLanguageNamed", multiLanguageNamed);
        baseLM.tableFactory.include("project", project);
        baseLM.tableFactory.include("expert", expert);
        baseLM.tableFactory.include("cluster", cluster);
        baseLM.tableFactory.include("claimer", claimer);
        baseLM.tableFactory.include("vote", vote);
        baseLM.tableFactory.include("patent", patent);
        baseLM.tableFactory.include("academic", academic);
        baseLM.tableFactory.include("nonRussianSpecialist", nonRussianSpecialist);
        baseLM.tableFactory.include("documentAbstract", documentAbstract);
        baseLM.tableFactory.include("document", document);
        baseLM.tableFactory.include("expertVote", expert, vote);
        baseLM.tableFactory.include("formalControl", formalControl);
        baseLM.tableFactory.include("legalCheck", legalCheck);
        baseLM.tableFactory.include("application", application);
    }

    @Override
    public void initGroups() {
        initBaseGroupAliases();
        contactGroup = addAbstractGroup("contactGroup", "Контакты организации", publicGroup);

        documentGroup = addAbstractGroup("documentGroup", "Юридические документы", publicGroup);

        legalDataGroup = addAbstractGroup("legalDataGroup", "Юридические данные", publicGroup);

        claimerInformationGroup = addAbstractGroup("claimerInformationGroup", "Информация о заявителе", publicGroup);


        projectInformationGroup = addAbstractGroup("projectInformationGroup", "Информация по проекту", baseGroup);

        additionalInformationGroup = addAbstractGroup("additionalInformationGroup", "Доп. информация по проекту", publicGroup);

        innovationGroup = addAbstractGroup("innovationGroup", "Инновация", baseGroup);

        sourcesFundingGroup = addAbstractGroup("sourcesFundingGroup", "Источники финансирования", baseGroup);
        nonReturnFundingGroup = addAbstractGroup("nonReturnFundingGroup", "Безвозвратные", sourcesFundingGroup);

        equipmentGroup = addAbstractGroup("equipmentGroup", "Оборудование", baseGroup);

        projectDocumentsGroup = addAbstractGroup("projectDocumentsGroup", "Документы", baseGroup);
        applicationFormGroup = addAbstractGroup("applicationFormGroup", "Анкета", projectDocumentsGroup);
        executiveSummaryGroup = addAbstractGroup("executiveSummaryGroup", "Резюме проекта", projectDocumentsGroup);
        techDescrGroup = addAbstractGroup("techDescrGroup", "Техническое описание", projectDocumentsGroup);
        roadMapGroup = addAbstractGroup("roadMapGroup", "Дорожная карта", projectDocumentsGroup);
        resolutionIPGroup = addAbstractGroup("resolutionIPGroup", "Заявление IP", projectDocumentsGroup);

        projectStatusGroup = addAbstractGroup("projectStatusGroup", "Текущий статус проекта", baseGroup);

        projectOptionsGroup = addAbstractGroup("projectOptionsGroup", "Параметры проекта", baseGroup);

        translateActionGroup = addAbstractGroup("translateActionGroup", "Перевод", baseGroup);

        projectTranslationsGroup = addAbstractGroup("projectTranslationsGroup", "Переведено", baseGroup);

        projectOtherClusterGroup = addAbstractGroup("projectOtherClusterGroup", "Иной кластер", baseGroup);

        consultingCenterGroup = addAbstractGroup("consultingCenterGroup", "Консультативный центр", baseGroup);

        consultingCenterStatGroup = addAbstractGroup("consultingCenterStatGroup", "Консультативный центр", baseGroup);

        voteResultGroup = addAbstractGroup("voteResultGroup", "Результаты голосования", publicGroup);

        expertResultGroup = addAbstractGroup("expertResultGroup", "Статистика по экспертам", publicGroup);

        importGroup = addAbstractGroup("importProjectsGroup", "Импорт", actionGroup);

        voteResultCheckGroup = addAbstractGroup("voteResultCheckGroup", "Результаты голосования (выбор)", voteResultGroup, false);
        voteResultCommentGroup = addAbstractGroup("voteResultCommentGroup", "Результаты голосования (комментарии)", voteResultGroup, false);

        formalControlResultGroup = addAbstractGroup("formalControlResultGroup", "Решения формальной экспертизы", publicGroup);
        legalCheckResultGroup = addAbstractGroup("legalCheckResultGroup", "Решения юридической проверки", publicGroup);
        registerGroup = addAbstractGroup("registerGroup", "Оформление свидетельства участника", publicGroup);
        originalDoscCheckGroup = addAbstractGroup("originalDoscCheckGroup", "Проверка оригиналов документов", publicGroup);
    }

    public LP nameNative;
    public LP nameForeign;
    public LP nameNativeShort;
    public LP nameNativeShortAggregateClusterProject;
    public LP firmNameNativeClaimer;
    public LP firmNameForeignClaimer;
    public LP phoneClaimer;
    public LP addressClaimer;
    public LP siteClaimer;
    public LP emailClaimer;
    public LP emailFirmClaimer;
    public LP emailClaimerProject;
    public LP statementClaimer;
    public LP constituentClaimerProject;
    public LP extractClaimerProject;
    public LP statementClaimerProject;
    LP loadStatementClaimer, openStatementClaimer;
    public LP constituentClaimer;
    LP loadConstituentClaimer, openConstituentClaimer;
    public LP extractClaimer;
    LP loadExtractClaimer, openExtractClaimer;
    public LP OGRNClaimer;
    public LP INNClaimer;
    LP revisionVote;
    LP projectVote, claimerVote, nameNativeProjectVote, nameForeignProjectVote;
    LP quantityVoteProject;
    LP dataDocumentNameExpert, documentNameExpert;
    public LP emailExpert;
    LP clusterExpert, nameNativeClusterExpert, nameForeignClusterExpert, nameNativeShortClusterExpert;
    LP primClusterExpert, extraClusterExpert, inClusterExpert;
    LP clusterInExpertVote;
    public LP inProjectCluster;
    LP clusterVote, nameNativeClusterVote, nameForeignClusterVote;
    LP projectCluster;
    LP quantityClusterProject;
    LP isPrevVoteVote;
    LP countPrevVote;
    public LP claimerProject;
    LP nameNativeUnionManagerProject, nameForeignUnionManagerProject;
    LP nameNativeJoinClaimerProject, nameForeignJoinClaimerProject;
    LP nameNativeCorrectManagerProject, nameForeignCorrectManagerProject;
    LP nameNativeCorrectHighlightClaimerProject, nameForeignCorrectHighlightClaimerProject;
    //LP nameNativeJoinClaimerProject;
    LP nameNativeCorrectClaimer, nameForeignCorrectClaimer;
    LP nameNativeClaimer;
    LP nameNativeCorrectClaimerProject, nameForeignCorrectClaimerProject;
    public LP nameNativeClaimerProject, nameForeignClaimerProject;
    //LP nameForeignClaimerProject;
    //LP nameForeignJoinClaimerProject;
    LP emailDocuments;

    public LP emailToExpert;

    LP nameNativeJoinClaimerVote, nameForeignJoinClaimerVote;
    LP nameNativeClaimerVote, nameForeignClaimerVote;
    public LP nameNativeGenitiveManagerProject;
    LP nameGenitiveManagerProject;
    public LP nameNativeDativusManagerProject;
    LP nameDativusManagerProject;
    public LP nameNativeAblateManagerProject;
    LP nameAblateManagerProject;

    public LP nameNativeJoinClaimer, nameForeignJoinClaimer;
    public LP nameForeignClaimer;
    LP nameGenitiveClaimerProject;
    LP nameDativusClaimerProject;
    LP nameAblateClaimerProject;
    LP nameGenitiveClaimerVote;
    LP nameDativusClaimerVote;
    LP nameAblateClaimerVote;

    LP documentTemplateDocumentTemplateDetail;

    LP projectDocument, nameNativeProjectDocument;
    LP fileDocument;
    LP loadFileDocument;
    LP openFileDocument;

    LP fileDecisionVote;
    LP loadFileDecisionVote;
    LP openFileDecisionVote;

    LP fileDecisionProject;
    LP acceptedDecisionProject, rejectedDecisionProject;

    private LP decisionNoticedVote;
    private LP decisionNoticedProject;
    private LP acceptedNoticedProject;
    private LP rejectedNoticedProject;
    private LP acceptedNoticedStatusProject;
    private LP acceptedNoticedPreliminaryProject;


    LP overdueOriginalDocsCheckProject;
    LP projectOriginalDocsCheck;
    LP resultOriginalDocsCheck;
    LP commentOriginalDocsCheck;
    LP nameResultOriginalDocsCheck;
    LP dateOriginalDocsCheck;
    LP emailDateOriginalDocsCheck;
    LP overdueDateOriginalDocsCheck;
    LP [] maxOriginalDocsCheckProjectProps;
    LP currentOriginalDocsCheckProject;
    LP executeOriginalDocsCheckProject;
    LP resultExecuteOriginalDocsCheckProject;
    LP addNegativeODCResult;
    LP addPositiveODCResult;
    LP negativeOriginalDocsCheckProject;
    LP positiveOriginalDocsCheckProject;
    LP sentForSignatureProject;
    LP signedProject;
    LP sentToFinDepProject;
    LP submittedToRegisterProject;
    LP preparedCertificateProject;
    LP certifiedProject;
    LP dateAppliedOriginalDocsProject;
    LP dateSentForSignatureProject;
    LP dateSignedProject;
    LP dateSentToFinDepProject;
    LP dateSubmittedToRegisterProject;
    LP datePreparedCertificateProject;
    LP dateCertifiedProject;

    LP inDefaultDocumentLanguage;
    LP inDefaultDocumentExpert;
    LP inDocumentLanguage;
    LP inDocumentExpert;

    LP inExpertVote, oldExpertVote, inNewExpertVote, inOldExpertVote;
    LP dateStartVote, dateEndVote;
    LP aggrDateEndVote;
    public LP quantityPreliminaryVoteProject;

    LP weekStartVote, quantityNewExpertWeek;
    LP quantityNewWeek;

    LP openedVote;
    LP closedVote;
    LP voteInProgressProject;
    LP voteInProgressRepeatProject;
    LP requiredPeriod;
    LP overduePeriod;
    LP requiredQuantity;
    LP limitExperts;
    public LP projectsImportLimit;
    LP voteStartFormVote;
    LP voteProtocolFormVote;

    LP dateExpertVote;
    LP voteResultExpertVote, nameVoteResultExpertVote;
    LP voteResultNewExpertVote;
    LP inProjectExpert;
    LP voteProjectExpert;
    LP voteResultProjectExpert;
    LP doneProjectExpert;
    LP doneExpertVote, doneNewExpertVote, doneOldExpertVote;
    LP refusedExpertVote;
    LP connectedExpertVote;
    LP expertVoteConnected;

    LP inClusterExpertVote, inClusterNewExpertVote;
    LP innovativeExpertVote, innovativeNewExpertVote;
    LP innovativeCommentExpertVote;
    LP foreignExpertVote, foreignNewExpertVote;
    LP competentExpertVote;
    LP completeExpertVote;
    LP completeCommentExpertVote;

    private LP percentNeededVote;

    private LP competitiveAdvantagesExpertVote;
    private LP commercePotentialExpertVote;
    private LP canBeImplementedExpertVote;
    private LP haveExpertiseExpertVote;
    private LP internationalExperienceExpertVote;
    private LP commentCompetitiveAdvantagesExpertVote;
    private LP commentCommercePotentialExpertVote;
    private LP commentCanBeImplementedExpertVote;
    private LP commentHaveExpertiseExpertVote;
    private LP commentInternationalExperienceExpertVote;

    LP quantityInVote;
    LP quantityInOldVote;
    LP quantityRepliedVote;
    LP quantityDoneVote;
    LP quantityDoneNewVote;
    LP quantityDoneOldVote;
    LP quantityRefusedVote;
    LP quantityConnectedVote;
    LP quantityInClusterVote;
    LP quantityInnovativeVote;
    LP quantityForeignVote;
    LP acceptedInClusterVote;
    LP acceptedInnovativeVote;
    LP acceptedForeignVote;

    LP acceptedVote;
    LP succeededVote;
    LP openedSucceededVote, closedSucceededVote;
    LP closedAcceptedVote;
    LP closedRejectedVote;
    LP closedAcceptedStatusVote, closedAcceptedPreliminaryVote;
    LP doneExpertVoteDateFromDateTo;
    LP doneExpertVoteMonthYear;
    LP quantityDoneExpertDateFromDateTo;
    LP quantityDoneExpertMonthYear;
    LP voteSucceededProjectCluster;
    LP voteOpenedSucceededProject;
    LP noCurrentVoteProject;
    LP valuedProjectCluster;
    LP voteValuedProjectCluster;
    LP voteLastProject;
    LP acceptedProjectCluster;
    LP rejectedProjectCluster;
    LP clusterAcceptedProject;
    LP acceptedProject;
    LP rejectedProject;
    LP valuedProject;
    LP voteRejectedProject;
    LP needExtraVoteProject;
    LP needExtraVoteRepeatProject;

    LP emailLetterExpertVoteEA, emailLetterExpertVote;
    LP allowedEmailLetterExpertVote;

    LP emailClaimerFromAddress;
    LP emailClaimerVoteEA;
    LP claimerEmailVote;
    LP emailClaimerHeaderVote;
    LP emailClaimerVote;

    LP emailStartVoteEA, emailStartHeaderVote, emailStartVote;
    LP emailProtocolVoteEA, emailProtocolHeaderVote, emailProtocolVote;
    LP emailClosedVoteEA, emailClosedHeaderVote, emailClosedVote;
    LP emailAuthExpertEA, emailAuthExpert;
    LP authExpertSubjectLanguage, letterExpertSubjectLanguage;

    LP generateDocumentsProjectDocumentType;
    LP includeDocumentsProject, hideIncludeDocumentsProject;
    LP importProjectSidsAction, showProjectsToImportAction, showProjectsReplaceToImportAction, importProjectsAction;
    LP exportProjectDocumentsAction;
    LP copyProjectAction;
    public LP fillLangProjectAction;
    LP generateVoteProject, needNameExtraVoteProject, hideGenerateVoteProject;
    LP copyResultsVote;

    LP expertLogin;

    LP projectStatusProject, nameProjectStatusProject;
    LP logStatusProject, logNameStatusProject;

    LP valuedStatusProject;
    LP certifiedStatusProject;
    LP legalCheckStatusProject;
    LP formalCheckStatusProject;
    LP statusProject;
    public LP nameStatusProject;
    LP oficialNameProjectStatus;
    LP numberProjectStatus;
    LP statusDataProject;
    LP statusProjectVote, nameStatusProjectVote;

    LP projectSucceededClaimer;

    LP quantityTotalExpert;
    LP quantityDoneExpert;
    LP percentDoneExpert;
    LP percentInClusterExpert;
    LP percentInnovativeExpert;
    LP percentForeignExpert;

    LP prevDateStartVote, prevDateVote;
    LP prevClusterVote, nameNativePrevClusterVote;
    LP dateProjectVote;
    LP numberNewExpertVote;
    LP numberOldExpertVote;

    public LP numberCluster;
    LP clusterNumber;
    LP currentClusterProject, firstClusterProject, lastClusterProject, finalClusterProject;
    LP lastClusterProjectVote, isLastClusterVote;
    public LP nameNativeFinalClusterProject, nameForeignFinalClusterProject, nameNativeShortFinalClusterProject;

    LP finalClusterProjectVote, nameNativeFinalClusterProjectVote;

    LP nativeSubstantiationFinalClusterProject, foreignSubstantiationFinalClusterProject;

    LP languageExpert;
    LP nameLanguageExpert;
    LP languageDocument;
    LP nameLanguageDocument;
    LP englishDocument;
    LP defaultEnglishDocumentType;
    LP defaultEnglishDocument;
    LP typeDocument;
    LP nameTypeDocument;
    LP postfixDocument;
    LP hidePostfixDocument;

    LP localeLanguage;

    LP quantityMinLanguageDocumentType;
    LP quantityMaxLanguageDocumentType;
    LP quantityProjectLanguageDocumentType;
    LP translateLanguageDocumentType;
    LP notEnoughProject;
    public LP inactiveProject;
    public LP autoGenerateProject;

    LP nameDocument;

    public LP nameNativeProject, nameNativeDataProject, nameNativeJoinProject;
    public LP nameForeignProject, nameForeignDataProject, nameForeignJoinProject;
    public LP nameNativeManagerProject;
    public LP nameForeignManagerProject;
    public LP nativeProblemProject;
    public LP foreignProblemProject;
    public LP nativeInnovativeProject;
    public LP foreignInnovativeProject;
    public LP nameForeignProjectType;
    public LP projectTypeProject;
    LP nameNativeProjectTypeProject;
    LP nameForeignProjectTypeProject;
    public LP projectActionProject;
    public LP nameProjectActionProject;
    LP projectActionVote;
    LP nameProjectActionVote;
    LP isStatusVote;
    LP isPreliminaryVote;
    LP isStatusProject;
    LP isPreliminaryProject;
    public LP nativeSubstantiationProjectType;
    public LP foreignSubstantiationProjectType;
    public LP nativeSubstantiationProjectCluster;
    public LP foreignSubstantiationProjectCluster;
    public LP isOtherClusterProject;
    LP hideIsOtherClusterProject;
    public LP nativeSubstantiationOtherClusterProject;
    LP hideNativeSubstantiationOtherClusterProject;
    public LP foreignSubstantiationOtherClusterProject;
    public LP fileNativeSummaryProject;
    LP hideForeignSubstantiationOtherClusterProject;
    LP currentCluster;
    LP nameNativeCurrentCluster;
    LP inProjectCurrentCluster;
    LP loadFileNativeSummaryProject;
    LP openFileNativeSummaryProject;
    public LP fileForeignSummaryProject;
    LP loadFileForeignSummaryProject;
    LP openFileForeignSummaryProject;
    public LP fileNativeApplicationFormProject;
    LP loadFileNativeApplicationFormProject;
    LP openFileNativeApplicationFormProject;
    public LP fileForeignApplicationFormProject;
    LP loadFileForeignApplicationFormProject;
    LP openFileForeignApplicationFormProject;
    public LP fileNativeRoadMapProject;
    LP loadNativeFileRoadMapProject;
    LP openNativeFileRoadMapProject;
    public LP fileForeignRoadMapProject;
    LP loadForeignFileRoadMapProject;
    LP openForeignFileRoadMapProject;
    public LP fileResolutionIPProject;
    LP loadFileResolutionIPProject;
    LP hideLoadFileResolutionIPProject;
    LP openFileResolutionIPProject;
    public LP fileNativeTechnicalDescriptionProject;
    public LP loadFileNativeTechnicalDescriptionProject;
    LP openFileNativeTechnicalDescriptionProject;
    public LP fileForeignTechnicalDescriptionProject;
    LP loadFileForeignTechnicalDescriptionProject;
    LP openFileForeignTechnicalDescriptionProject;

    public LP isReturnInvestmentsProject;
    public LP nameReturnInvestorProject;
    public LP amountReturnFundsProject;
    public LP isNonReturnInvestmentsProject;
    public LP nameNonReturnInvestorProject;
    public LP amountNonReturnFundsProject;
    public LP isCapitalInvestmentProject;
    public LP isPropertyInvestmentProject;
    public LP isGrantsProject;
    public LP isOtherNonReturnInvestmentsProject;
    public LP commentOtherNonReturnInvestmentsProject;
    public LP isOwnFundsProject;
    public LP amountOwnFundsProject;
    public LP isPlanningSearchSourceProject;
    public LP amountFundsProject;
    public LP isOtherSoursesProject;
    public LP commentOtherSoursesProject;
    public LP needsToBeTranslatedToRussianProject;
    public LP needsToBeTranslatedToEnglishProject;
    public LP translatedToRussianProject;
    public LP hideTranslatedToRussianProject;
    public LP translatedToEnglishProject;
    public LP hideTranslatedToEnglishProject;
    public LP fillNativeProject;
    public LP fillForeignProject;
    public LP langProject;
    public LP isConsultingCenterQuestionProject;
    public LP isConsultingCenterCommentProject;
    public LP consultingCenterCommentProject;
    public LP sumPositiveConsultingCenterCommentProject;
    public LP sumNegativeConsultingCenterCommentProject;
    public LP sumTotalConsultingCenterCommentProject;
    public LP betweenDate;

    LP hideNameReturnInvestorProject;
    LP hideAmountReturnFundsProject;
    LP hideNameNonReturnInvestorProject;
    LP hideAmountNonReturnFundsProject;
    LP hideCommentOtherNonReturnInvestmentsProject;
    LP hideAmountOwnFundsProject;
    LP hideAmountFundsProject;
    LP hideCommentOtherSoursesProject;
    LP hideIsCapitalInvestmentProject;
    LP hideIsPropertyInvestmentProject;
    LP hideIsGrantsProject;
    LP hideIsOtherNonReturnInvestmentsProject;

    public LP isOwnedEquipmentProject;
    public LP isAvailableEquipmentProject;
    public LP isTransferEquipmentProject;
    public LP descriptionTransferEquipmentProject;
    public LP ownerEquipmentProject;
    public LP isPlanningEquipmentProject;
    public LP specificationEquipmentProject;
    public LP isSeekEquipmentProject;
    public LP descriptionEquipmentProject;
    public LP isOtherEquipmentProject;
    public LP commentEquipmentProject;
    LP hideDescriptionTransferEquipmentProject;
    LP hideOwnerEquipmentProject;
    LP hideSpecificationEquipmentProject;
    LP hideDescriptionEquipmentProject;
    LP hideCommentEquipmentProject;

    public LP projectPatent;
    public LP sidProjectPatent;
    public LP nativeTypePatent;
    public LP foreignTypePatent;
    public LP nativeNumberPatent;
    public LP foreignNumberPatent;
    public LP priorityDatePatent;
    public LP isOwned;
    public LP ownerPatent;
    public LP nameForeignOwnerType;
    public LP ownerTypePatent;
    LP ownerTypeToPatent;
    LP nameNativeOwnerTypePatent;
    LP nameForeignOwnerTypePatent;
    public LP ownerTypeToSID;
    public LP projectTypeToSID;
    public LP projectActionToSID;
    public LP fileIntentionOwnerPatent;
    LP loadFileIntentionOwnerPatent;
    LP openFileIntentionOwnerPatent;
    public LP isValuated;
    public LP valuatorPatent;
    public LP fileActValuationPatent;
    LP loadFileActValuationPatent;
    LP openFileActValuationPatent;
    LP hideOwnerPatent;
    LP hideFileIntentionOwnerPatent;
    LP hideLoadFileIntentionOwnerPatent;
    LP hideOpenFileIntentionOwnerPatent;
    LP hideValuatorPatent;
    LP hideFileActValuationPatent;
    LP hideLoadFileActValuationPatent;
    LP hideOpenFileActValuationPatent;
    LP hideNameNativeOwnerTypePatent;
    LP hideNameForeignOwnerTypePatent;

    public LP projectAcademic;
    public LP sidProjectAcademic;
    public LP fullNameAcademic;
    public LP fullNameToAcademic;
    public LP fullNameSIDToAcademic;
    public LP institutionAcademic;
    public LP titleAcademic;
    public LP fileDocumentConfirmingAcademic;
    LP loadFileDocumentConfirmingAcademic;
    LP openFileDocumentConfirmingAcademic;
    public LP fileDocumentEmploymentAcademic;
    LP loadFileDocumentEmploymentAcademic;
    LP openFileDocumentEmploymentAcademic;

    public LP projectNonRussianSpecialist;
    public LP sidProjectNonRussianSpecialist;
    public LP fullNameNonRussianSpecialist;
    public LP fullNameToNonRussianSpecialist;
    public LP fullNameSIDToNonRussianSpecialist;
    public LP organizationNonRussianSpecialist;
    public LP titleNonRussianSpecialist;
    public LP fileNativeResumeNonRussianSpecialist;
    LP loadFileForeignResumeNonRussianSpecialist;
    LP openFileForeignResumeNonRussianSpecialist;
    public LP fileForeignResumeNonRussianSpecialist;
    LP loadFileNativeResumeNonRussianSpecialist;
    LP openFileNativeResumeNonRussianSpecialist;
    public LP filePassportNonRussianSpecialist;
    LP loadFilePassportNonRussianSpecialist;
    LP openFilePassportNonRussianSpecialist;
    public LP fileStatementNonRussianSpecialist;
    LP loadFileStatementNonRussianSpecialist;
    LP openFileStatementNonRussianSpecialist;

    LP isForeignExpert;
    LP localeExpert;
    LP disableExpert;

    LP editClaimer;
    public LP addProject;
    LP editProject;
    LP editClaimerProject;
    LP translateToRussianProject, translateToEnglishProject;
    LP hideTranslateToRussianProject, hideTranslateToEnglishProject;
    LP needTranslationProject;

    LP projectFormalControl;
    LP resultFormalControl;
    LP nameResultFormalControl;
    LP addNotEnoughDocumentsFCResult;
    LP addNoListOfExpertsFCResult;
    LP addNotSuitableClusterFCResult;
    LP addRepeatedFCResult;
    LP addPositiveFCResult;
    LP dateFormalControl;
    LP emailDateFormalControl;
    LP overdueDateFormalControl;
    LP overdueFormalControlProject;
    LP projectActionLegalCheck;
    LP nameProjectActionLegalCheck;
    LP commentFormalControl;
    LP[] maxFormalControlProjectProps;
    LP currentFormalControlProject;
    LP executeFormalControlProject;
    LP resultExecuteFormalControlProject;
    LP nameResultExecuteFormalControlProject;
    LP notEnoughDocumentsProject;
    LP isPreliminaryNotEnoughDocumentProject;
    LP isStatusNotEnoughDocumentProject;
    LP noListOfExpertsProject;
    LP notSuitableClusterProject;
    LP repeatedProject;
    LP positiveFormalResultProject;
    LP projectActionFormalControl;
    LP nameProjectActionFormalControl;

    LP dateLegalCheck;
    LP emailDateLegalCheck;
    LP overdueDateLegalCheck;
    LP overdueLegalCheckProject;
    LP projectLegalCheck;
    LP resultLegalCheck;
    LP nameResultLegalCheck;
    LP addNegativeLCResult;
    LP addPositiveLCResult;
    LP commentLegalCheck;
    LP[] maxLegalCheckProjectProps;
    LP currentLegalCheckProject;
    LP executeLegalCheckProject;
    LP resultExecuteLegalCheckProject;
    LP negativeLegalResultProject;
    LP positiveLegalResultProject;
    LP sentForTranslationProject;

    LP dateAgreementExpert;
    LP vone;
    LP claimerProjectVote;
    LP nameNativeJoinClaimerProjectVote;
    LP countryExpert;
    LP nameCountryExpert;
    LP caseCountry;
    LP caseCountryExpert;
    LP currencyExpert;
    LP nameCurrencyExpert;
    LP residency;
    LP residencyCountryExpert;
    LP rateExpert;
    LP emailForCertificates;
    LP moneyQuantityDoneExpertMonthYear;
    LP baseCurrency;
    LP baseCurrencyExpert;
    LP englCountry;
    LP englCountryExpert;
    LP englCurrency;
    LP englCurrencyExpert;
    LP pluralCurrency;
    LP pluralCurrencyExpert;
    LP emailLetterExpertMonthYearEA;
    LP emailLetterCertificatesExpertMonthYear;
    LP previousDate;
    LP monthInPreviousDate, yearInPreviousDate;
    LP isNewMonth;

    LP hasPreliminaryVoteProject;
    LP isPreliminaryStatusProject;
    LP isPreliminaryAndStatusProject;

    LP projectApplication;
    LP projectApplicationPreliminary, projectApplicationStatus;
    LP isPreliminaryApplication, isStatusApplication;
    LP isPreliminaryAndStatusApplication;
    LP preliminaryApplicationProject;
    LP statusApplicationProject;

    LP dateApplicationPreliminary, dateApplicationStatus;
    LP dateApplication;

    LP nameNativeProjectApplication;
    LP nameNativeClaimerApplicationPreliminary, nameNativeClaimerApplicationStatus;
    LP nameNativeClaimerApplication;

    private LP emailClaimerApplication;

    private LP langApplication;
    private LP nameNativeShortAggregateClusterApplication;

    LP statusJoinApplication;
    LP isPreliminaryAfterStatusApplication;
    LP statusApplication;
    LP nameStatusApplication;
    LP officialNameStatusApplication;

    @Override
    public void initProperties() {
        idGroup.add(baseLM.objectValue);

        previousDate = addJProp("previousDate", "Вчерашняя дата", baseLM.subtractDate2, baseLM.currentDate, addCProp("1", IntegerClass.instance, 1));
        monthInPreviousDate = addJProp("monthInPreviousDate", "Вчерашний месяц", baseLM.monthInDate, previousDate);
        yearInPreviousDate = addJProp("yearInPreviousDate", "Вчерашний год", baseLM.yearInDate, previousDate);

        // monthInYeasterdayDate = addJProp("monthInYeasterdayDate", "Вчерашний месяц", baseLM.monthInDate, addJProp(baseLM.addDate2, baseLM.currentDate, addCProp("1", IntegerClass.instance, 1)));
        isNewMonth = addJProp("isNewMonth", "Начало месяца", baseLM.diff2, baseLM.currentMonth, monthInPreviousDate);

        nameNative = addDProp(recognizeGroup, "nameNative", "Имя", InsensitiveStringClass.get(2000), multiLanguageNamed);
        nameNative.property.aggProp = true;
        nameNative.setMinimumWidth(10);
        nameNative.setPreferredWidth(50);
        nameForeign = addDProp(recognizeGroup, "nameForeign", "Имя (иностр.)", InsensitiveStringClass.get(2000), multiLanguageNamed);
        nameForeign.property.aggProp = true;
        nameForeign.setMinimumWidth(10);
        nameForeign.setPreferredWidth(50);

        nameNativeShort = addDProp(baseGroup, "nameNativeShort", "Имя (сокр.)", InsensitiveStringClass.get(4), cluster);
        nameNativeShort.setFixedCharWidth(5);

        nameNativeShortAggregateClusterProject = addDProp(baseGroup, "nameNativeShortAggregateClusterProject", "Кластеры", InsensitiveStringClass.get(20), project);

        baseGroup.add(baseLM.email.property); // сделано, чтобы email был не самой первой колонкой в диалогах

        LP percent = addSFProp("(prm1*100/prm2)", DoubleClass.instance, 2);

        // глобальные свойства
        requiredPeriod = addDProp(baseGroup, "votePeriod", "Срок заседания", IntegerClass.instance);
        overduePeriod = addDProp(baseGroup, "overduePeriod", "Срок просрочки для формального контроля", IntegerClass.instance);
        requiredQuantity = addDProp(baseGroup, "voteRequiredQuantity", "Кол-во экспертов", IntegerClass.instance);
        limitExperts = addDProp(baseGroup, "limitExperts", "Кол-во прогол. экспертов", IntegerClass.instance);
        projectsImportLimit = addDProp(baseGroup, "projectsImportLimit", "Максимальное кол-во импортируемых проектов", IntegerClass.instance);
        rateExpert = addDProp(baseGroup, "rateExpert", "Ставка эксперта (долларов)", DoubleClass.instance);
        emailForCertificates = addDProp(baseGroup, "emailForCertificates", "E-mail для актов", StringClass.get(50));

        //свойства заявителя
        nameNativeJoinClaimer = addJProp(claimerInformationGroup, "nameNativeJoinClaimer", "Заявитель", baseLM.and1, nameNative, 1, is(claimer), 1);
        nameNativeJoinClaimer.setMinimumWidth(10);
        nameNativeJoinClaimer.setPreferredWidth(50);
        nameForeignJoinClaimer = addJProp(claimerInformationGroup, "nameForeignJoinClaimer", "Claimer", baseLM.and1, nameForeign, 1, is(claimer), 1);
        nameForeignJoinClaimer.setMinimumWidth(10);
        nameForeignJoinClaimer.setPreferredWidth(50);
        firmNameNativeClaimer = addDProp(claimerInformationGroup, "firmNameNativeClaimer", "Фирменное название", InsensitiveStringClass.get(2000), claimer);
        firmNameNativeClaimer.setMinimumWidth(10);
        firmNameNativeClaimer.setPreferredWidth(50);
        firmNameForeignClaimer = addDProp(claimerInformationGroup, "firmNameForeignClaimer", "Brand name", InsensitiveStringClass.get(2000), claimer);
        firmNameForeignClaimer.setMinimumWidth(10);
        firmNameForeignClaimer.setPreferredWidth(50);
        phoneClaimer = addDProp(contactGroup, "phoneClaimer", "Телефон", StringClass.get(100), claimer);
        addressClaimer = addDProp(contactGroup, "addressClaimer", "Адрес", StringClass.get(2000), claimer);
        addressClaimer.setMinimumWidth(10);
        addressClaimer.setPreferredWidth(50);
        siteClaimer = addDProp(contactGroup, "siteClaimer", "Сайт", StringClass.get(100), claimer);
        emailClaimer = addJProp(contactGroup, "emailClaimer", "E-mail", baseLM.and1, baseLM.email, 1, is(claimer), 1);
        emailFirmClaimer = addDProp(contactGroup, "emailFirmClaimer", "E-mail организации", StringClass.get(50), claimer);
        emailExpert = addJProp("emailExpert", "E-mail", baseLM.and1, baseLM.email, 1, is(expert), 1);

        statementClaimer = addDProp("statementClaimer", "Заявление", CustomFileClass.instance, claimer);
        loadStatementClaimer = addLFAProp(documentGroup, "Загрузить заявление", statementClaimer);
        openStatementClaimer = addOFAProp(documentGroup, "Открыть заявление", statementClaimer);

        constituentClaimer = addDProp("constituentClaimer", "Учредительные документы", CustomFileClass.instance, claimer);
        loadConstituentClaimer = addLFAProp(documentGroup, "Загрузить учредительные документы", constituentClaimer);
        openConstituentClaimer = addOFAProp(documentGroup, "Открыть учредительные документы", constituentClaimer);

        extractClaimer = addDProp("extractClaimer", "Выписка из реестра", CustomFileClass.instance, claimer);
        loadExtractClaimer = addLFAProp(documentGroup, "Загрузить выписку из реестра", extractClaimer);
        openExtractClaimer = addOFAProp(documentGroup, "Открыть Выписку из реестра", extractClaimer);

        OGRNClaimer = addDProp(legalDataGroup, "OGRNClaimer", "ОГРН", StringClass.get(13), claimer);
        INNClaimer = addDProp(legalDataGroup, "INNClaimer", "ИНН", StringClass.get(12), claimer);

        revisionVote = addCUProp(baseGroup, "revisionVote", "Регламент", addCProp(StringClass.get(3), "R1", voteR1), addCProp(StringClass.get(3), "R2", voteR2));

        projectVote = addDProp(idGroup, "projectVote", "Проект (ИД)", project, vote);
        setNotNull(projectVote);

        inProjectCluster = addDProp(baseGroup, "inProjectCluster", "Вкл", LogicalClass.instance, project, cluster);
        projectCluster = addDProp(idGroup, "projectCluster", "Проект (ИД)", project, cluster);

        quantityClusterProject = addSGProp(baseGroup, "quantityClusterProject", true, "Кол-во кластеров",
                addJProp(baseLM.and1, addCProp(IntegerClass.instance, 1, cluster), 2,
                        addJProp(baseLM.equals2, baseLM.vtrue, inProjectCluster, 1, 2), 1, 2), 1);


        quantityVoteProject = addSGProp(baseGroup, "quantityVoteProject", true, "Кол-во заседаний", addCProp(IntegerClass.instance, 1, vote), projectVote, 1);

        nameNativeJoinProject = addJProp(baseLM.and1, nameNative, 1, is(project), 1);
        nameNativeDataProject = addDProp("nameNativeDataProject", "Название проекта", InsensitiveStringClass.get(2000), project);
        nameNativeProject = addSUProp(projectInformationGroup, "nameNativeProject", "Название проекта", Union.OVERRIDE, nameNativeJoinProject, nameNativeDataProject);
        nameNativeProject.setMinimumWidth(10);
        nameNativeProject.setPreferredWidth(120);

        nameForeignJoinProject = addJProp(baseLM.and1, nameForeign, 1, is(project), 1);

        nameForeignDataProject = addDProp("nameForeignDataProject", "Name of the project", InsensitiveStringClass.get(2000), project);

        nameForeignProject = addSUProp(projectInformationGroup, "nameForeignProject", "Name of the project", Union.OVERRIDE, nameForeignJoinProject, nameForeignDataProject);
        nameForeignProject.setMinimumWidth(10);
        nameForeignProject.setPreferredWidth(120);

        nameNativeProjectVote = addJProp(baseGroup, "nameNativeProjectVote", "Проект", nameNativeProject, projectVote, 1);
        nameForeignProjectVote = addJProp(baseGroup, "nameForeignProjectVote", "Проект (иностр.)", nameForeignProject, projectVote, 1);

        dataDocumentNameExpert = addDProp(baseGroup, "dataDocumentNameExpert", "Имя для документов", StringClass.get(70), expert);
        documentNameExpert = addSUProp("documentNameExpert", "Имя для документов", Union.OVERRIDE, addJProp(baseLM.and1, addJProp(baseLM.insensitiveString2, baseLM.userLastName, 1, baseLM.userFirstName, 1), 1, is(expert), 1), dataDocumentNameExpert);

        clusterExpert = addDProp(idGroup, "clusterExpert", "Кластер (ИД)", cluster, expert);
        nameNativeClusterExpert = addJProp(baseGroup, "nameNativeClusterExpert", "Кластер", nameNative, clusterExpert, 1);
        nameForeignClusterExpert = addJProp("nameForeignClusterExpert", "Кластер (иностр.)", nameForeign, clusterExpert, 1);
        nameNativeShortClusterExpert = addJProp(baseGroup, "nameNativeShortClusterExpert", "Кластер (сокр.)", nameNativeShort, clusterExpert, 1);

        primClusterExpert = addJProp("primClusterExpert", "Вкл (осн.)", baseLM.equals2, 1, clusterExpert, 2);
        extraClusterExpert = addDProp(baseGroup, "extraClusterExpert", "Вкл (доп.)", LogicalClass.instance, cluster, expert);
        inClusterExpert = addSUProp(baseGroup, "inClusterExpert", true, "Вкл", Union.OVERRIDE, extraClusterExpert, addJProp(baseLM.equals2, 1, clusterExpert, 2));

        // project
        claimerProject = addDProp(idGroup, "claimerProject", "Заявитель (ИД)", claimer, project);
        emailClaimerProject = addJProp("emailClaimerProject", "E-mail заявителя", emailClaimer, claimerProject, 1);
        statementClaimerProject = addJProp("statementClaimerProject", "Заявление", statementClaimer, claimerProject, 1);
        constituentClaimerProject = addJProp("constituentClaimerProject", "Учредительные документы", constituentClaimer, claimerProject, 1);
        extractClaimerProject = addJProp("extractClaimerProject", "Выписка", extractClaimer, claimerProject, 1);

        claimerVote = addJProp(idGroup, "claimerVote", "Заявитель (ИД)", claimerProject, projectVote, 1);

        sidProject = addDProp(projectInformationGroup, "sidProject", "Внешний идентификатор проекта", StringClass.get(10), project);
        sidToProject = addAGProp("sidToProject", "SID проекта", sidProject);

        nameNativeManagerProject = addDProp(projectInformationGroup, "nameNativeManagerProject", "ФИО руководителя проекта", InsensitiveStringClass.get(2000), project);
        nameNativeManagerProject.setMinimumWidth(10);
        nameNativeManagerProject.setPreferredWidth(50);

        nameForeignManagerProject = addDProp(projectInformationGroup, "nameForeignManagerProject", "Full name manager project", InsensitiveStringClass.get(2000), project);
        nameForeignManagerProject.setMinimumWidth(10);
        nameForeignManagerProject.setPreferredWidth(50);

        nameNativeCorrectManagerProject = addDProp(baseGroup, "nameNativeCorrectManagerProject", "ФИО руководителя проекта (испр.)", InsensitiveStringClass.get(2000), project);
        nameForeignCorrectManagerProject = addDProp(baseGroup, "nameForeignCorrectManagerProject", "Full name manager project (corr.)", InsensitiveStringClass.get(2000), project);

        nameNativeUnionManagerProject = addSUProp(baseGroup, "nameNativeUnionManagerProject", "ФИО руководителя проекта", Union.OVERRIDE, nameNativeManagerProject, nameNativeCorrectManagerProject);
        nameNativeUnionManagerProject.setMinimumWidth(10);
        nameNativeUnionManagerProject.setPreferredWidth(50);

        nameForeignUnionManagerProject = addSUProp(baseGroup, "nameForeignUnionManagerProject", "Full name project manager", Union.OVERRIDE, nameForeignManagerProject, nameForeignCorrectManagerProject);
        nameForeignUnionManagerProject.setMinimumWidth(10);
        nameForeignUnionManagerProject.setPreferredWidth(50);

        nameNativeGenitiveManagerProject = addDProp(projectOptionsGroup, "nameNativeGenitiveManagerProject", "ФИО руководителя проекта (Кого)", InsensitiveStringClass.get(2000), project);
        nameNativeGenitiveManagerProject.setMinimumWidth(10);
        nameNativeGenitiveManagerProject.setPreferredWidth(50);

        nameGenitiveManagerProject = addSUProp("nameGenitiveManagerProject", "Заявитель (Кого)", Union.OVERRIDE, nameNativeUnionManagerProject, nameNativeGenitiveManagerProject);
        nameGenitiveManagerProject.setMinimumWidth(10);
        nameGenitiveManagerProject.setPreferredWidth(50);

        nameNativeDativusManagerProject = addDProp(projectOptionsGroup, "nameNativeDativusManagerProject", "ФИО руководителя проекта (Кому)", InsensitiveStringClass.get(2000), project);
        nameNativeDativusManagerProject.setMinimumWidth(10);
        nameNativeDativusManagerProject.setPreferredWidth(50);

        nameDativusManagerProject = addSUProp("nameDativusManagerProject", "Заявитель (Кому)", Union.OVERRIDE, nameNativeUnionManagerProject, nameNativeDativusManagerProject);
        nameDativusManagerProject.setMinimumWidth(10);
        nameDativusManagerProject.setPreferredWidth(50);

        nameNativeAblateManagerProject = addDProp(projectOptionsGroup, "nameNativeAblateManagerProject", "ФИО руководителя проекта (Кем)", InsensitiveStringClass.get(2000), project);
        nameNativeAblateManagerProject.setMinimumWidth(10);
        nameNativeAblateManagerProject.setPreferredWidth(50);

        nameAblateManagerProject = addSUProp("nameAblateManagerProject", "Заявитель (Кем)", Union.OVERRIDE, nameNativeUnionManagerProject, nameNativeAblateManagerProject);
        nameAblateManagerProject.setMinimumWidth(10);
        nameAblateManagerProject.setPreferredWidth(50);

        projectActionProject = addDProp(idGroup, "projectActionProject", "Тип заявки (ИД)", projectAction, project);
        nameProjectActionProject = addJProp(projectInformationGroup, "nameProjectActionProject", "Тип заявки", baseLM.name, projectActionProject, 1);
        nameProjectActionProject.setPreferredCharWidth(10);

        isStatusProject = addJProp("isStatusProject", "На статус участника", baseLM.equals2, projectActionProject, 1, addCProp(projectAction, "status", project), 1);
        isPreliminaryProject = addJProp("isPreliminaryProject", "На предварительную экспертизу", baseLM.equals2, projectActionProject, 1, addCProp(projectAction, "preliminary", project), 1);

        nameNativeCorrectClaimer = addDProp("nameNativeCorrectClaimer", "Полное наименование организации", InsensitiveStringClass.get(2000), claimer);

        nameNativeClaimer = addSUProp("nameNativeClaimer", "Заявитель", Union.OVERRIDE, nameNativeJoinClaimer, nameNativeCorrectClaimer);
        nameNativeClaimer.setMinimumWidth(10);
        nameNativeClaimer.setPreferredWidth(50);

        nameNativeJoinClaimerProject = addJProp(baseGroup, true, "nameNativeJoinClaimerProject", "Заявитель", nameNativeClaimer, claimerProject, 1);

        nameNativeClaimerProject = addIfElseUProp(projectInformationGroup, "nameNativeClaimerProject", "Заявитель", nameNativeUnionManagerProject, nameNativeJoinClaimerProject, isPreliminaryProject, 1);
        nameNativeClaimerProject.setMinimumWidth(10);
        nameNativeClaimerProject.setPreferredWidth(120);

        nameNativeCorrectClaimerProject = addJProp(baseGroup, true, "nameNativeCorrectClaimerProject", "Полное наименование организации", nameNativeCorrectClaimer, claimerProject, 1);
        nameNativeCorrectHighlightClaimerProject = addIfElseUProp(baseGroup, "nameNativeCorrectHighlightClaimerProject", "Заявитель", nameNativeCorrectManagerProject, nameNativeCorrectClaimerProject, isPreliminaryProject, 1);

        nameForeignCorrectClaimer = addDProp("nameForeignCorrectClaimer", "Full name of the Organisation", InsensitiveStringClass.get(2000), claimer);

        nameForeignClaimer = addSUProp("nameForeignClaimer", "Claimer", Union.OVERRIDE, nameForeignJoinClaimer, nameForeignCorrectClaimer);
        nameForeignClaimer.setMinimumWidth(10);
        nameForeignClaimer.setPreferredWidth(50);

        nameForeignJoinClaimerProject = addJProp(baseGroup, true, "nameForeignJoinClaimerProject", "Claimer", nameForeignClaimer, claimerProject, 1);

        nameForeignClaimerProject = addIfElseUProp(projectInformationGroup, "nameForeignClaimerProject", "Claimer", nameForeignUnionManagerProject, nameForeignJoinClaimerProject, isPreliminaryProject, 1);
        nameForeignClaimerProject.setMinimumWidth(10);
        nameForeignClaimerProject.setPreferredWidth(120);

        nameForeignCorrectClaimerProject = addJProp(baseGroup, true, "nameForeignCorrectClaimerProject", "Full name of the Organisation", nameForeignCorrectClaimer, claimerProject, 1);
        nameForeignCorrectHighlightClaimerProject = addIfElseUProp(baseGroup, "nameForeignCorrectHighlightClaimerProject", "Claimer", nameForeignCorrectManagerProject, nameForeignCorrectClaimerProject, isPreliminaryProject, 1);

        nameGenitiveClaimerProject = addIfElseUProp(baseGroup, "nameGenitiveClaimerProject", "Заявитель (кого)", nameGenitiveManagerProject, nameNativeJoinClaimerProject, isPreliminaryProject, 1);
        nameGenitiveClaimerProject.setMinimumWidth(10);
        nameGenitiveClaimerProject.setPreferredWidth(50);
        nameDativusClaimerProject = addIfElseUProp(baseGroup, "nameDativusClaimerProject", "Заявитель (кому)", nameDativusManagerProject, nameNativeJoinClaimerProject, isPreliminaryProject, 1);
        nameDativusClaimerProject.setMinimumWidth(10);
        nameDativusClaimerProject.setPreferredWidth(50);
        nameAblateClaimerProject = addIfElseUProp(baseGroup, "nameAblateClaimerProject", "Заявитель (кем)", nameAblateManagerProject, nameNativeJoinClaimerProject, isPreliminaryProject, 1);
        nameAblateClaimerProject.setMinimumWidth(10);
        nameAblateClaimerProject.setPreferredWidth(50);


        nativeProblemProject = addDProp(innovationGroup, "nativeProblemProject", "Проблема, на решение которой направлен проект", InsensitiveStringClass.get(2000), project);
        nativeProblemProject.setMinimumWidth(10);
        nativeProblemProject.setPreferredWidth(50);
        foreignProblemProject = addDProp(innovationGroup, "foreignProblemProject", "The problem that the project will solve", InsensitiveStringClass.get(2000), project);
        foreignProblemProject.setMinimumWidth(10);
        foreignProblemProject.setPreferredWidth(50);

        nativeInnovativeProject = addDProp(innovationGroup, "nativeInnovativeProject", "Суть инновации", InsensitiveStringClass.get(2000), project);
        nativeInnovativeProject.setMinimumWidth(10);
        nativeInnovativeProject.setPreferredWidth(50);
        foreignInnovativeProject = addDProp(innovationGroup, "foreignInnovativeProject", "Description of the innovation", InsensitiveStringClass.get(2000), project);
        foreignInnovativeProject.setMinimumWidth(10);
        foreignInnovativeProject.setPreferredWidth(50);

        nameForeignProjectType = addDProp(baseGroup, "nameForeignProjectType", "Имя (иностр.)", InsensitiveStringClass.get(110), projectType);

        projectTypeProject = addDProp(idGroup, "projectTypeProject", "Тип проекта (ИД)", projectType, project);
        nameNativeProjectTypeProject = addJProp(innovationGroup, "nameNativeProjectTypeProject", "Тип проекта", baseLM.name, projectTypeProject, 1);
        nameForeignProjectTypeProject = addJProp(innovationGroup, "nameForeignProjectTypeProject", "Тип проекта (иностр.)", nameForeignProjectType, projectTypeProject, 1);

        projectActionVote = addDProp(idGroup, "projectActionVote", "Тип заявки (ИД)", projectAction, vote);
        nameProjectActionVote = addJProp(baseGroup, "nameProjectActionVote", "Тип заявки", baseLM.name, projectActionVote, 1);
        nameProjectActionVote.setPreferredCharWidth(20);

        isStatusVote = addJProp("isStatusVote", "На статус участника", baseLM.equals2, projectActionVote, 1, addCProp(projectAction, "status", vote), 1);
        isPreliminaryVote = addJProp("isPreliminaryVote", "На предварительную экспертизу", baseLM.equals2, projectActionVote, 1, addCProp(projectAction, "preliminary", vote), 1);

        nativeSubstantiationProjectType = addDProp(innovationGroup, "nativeSubstantiationProjectType", "Обоснование выбора", InsensitiveStringClass.get(2000), project);
        nativeSubstantiationProjectType.setMinimumWidth(10);
        nativeSubstantiationProjectType.setPreferredWidth(50);

        foreignSubstantiationProjectType = addDProp(innovationGroup, "foreignSubstantiationProjectType", "Description of choice", InsensitiveStringClass.get(2000), project);
        foreignSubstantiationProjectType.setMinimumWidth(10);
        foreignSubstantiationProjectType.setPreferredWidth(50);

        nameNativeCluster = addJProp("nameNativeCluster", "Кластер", baseLM.and1, nameNative, 1, is(cluster), 1);
        nameNativeToCluster = addAGProp(idGroup, "nameNativeToCluster", "Кластер", nameNativeCluster);
        nativeSubstantiationProjectCluster = addDProp(baseGroup, "nativeSubstantiationProjectCluster", "Обоснование выбора", InsensitiveStringClass.get(2000), project, cluster);
        nativeSubstantiationProjectCluster.setMinimumWidth(10);
        nativeSubstantiationProjectCluster.setPreferredWidth(50);
        foreignSubstantiationProjectCluster = addDProp(baseGroup, "foreignSubstantiationProjectCluster", "Description of choice", InsensitiveStringClass.get(2000), project, cluster);
        foreignSubstantiationProjectCluster.setMinimumWidth(10);
        foreignSubstantiationProjectCluster.setPreferredWidth(50);
        isOtherClusterProject = addDProp(projectOtherClusterGroup, "isOtherClusterProject", "Иной кластер", LogicalClass.instance, project);
        hideIsOtherClusterProject = addHideCaptionProp(privateGroup, "Иной кластер", isOtherClusterProject, isOtherClusterProject);
        nativeSubstantiationOtherClusterProject = addDProp(projectOtherClusterGroup, "nativeSubstantiationOtherClusterProject", "Обоснование выбора", InsensitiveStringClass.get(2000), project);
        nativeSubstantiationOtherClusterProject.setMinimumWidth(10);
        nativeSubstantiationOtherClusterProject.setPreferredWidth(50);
        hideNativeSubstantiationOtherClusterProject = addHideCaptionProp(privateGroup, "Укажите", nativeSubstantiationOtherClusterProject, isOtherClusterProject);
        foreignSubstantiationOtherClusterProject = addDProp(projectOtherClusterGroup, "foreignSubstantiationOtherClusterProject", "Description of choice", InsensitiveStringClass.get(2000), project);
        foreignSubstantiationOtherClusterProject.setMinimumWidth(10);
        foreignSubstantiationOtherClusterProject.setPreferredWidth(50);
        hideForeignSubstantiationOtherClusterProject = addHideCaptionProp(privateGroup, "Укажите", foreignSubstantiationOtherClusterProject, isOtherClusterProject);
        fileNativeSummaryProject = addDProp("fileNativeSummaryProject", "Файл резюме проекта", CustomFileClass.instance, project);
        loadFileNativeSummaryProject = addLFAProp(executiveSummaryGroup, "Загрузить файл резюме проекта", fileNativeSummaryProject);
        openFileNativeSummaryProject = addOFAProp(executiveSummaryGroup, "Открыть файл резюме проекта", fileNativeSummaryProject);

        fileForeignSummaryProject = addDProp("fileForeignSummaryProject", "Файл резюме проекта (иностр.)", CustomFileClass.instance, project);
        loadFileForeignSummaryProject = addLFAProp(executiveSummaryGroup, "Загрузить файл резюме проекта (иностр.)", fileForeignSummaryProject);
        openFileForeignSummaryProject = addOFAProp(executiveSummaryGroup, "Открыть файл резюме проекта (иностр.)", fileForeignSummaryProject);

        fileNativeApplicationFormProject = addDProp("fileNativeApplicationFormProject", "Анкета на русском", CustomFileClass.instance, project);
        loadFileNativeApplicationFormProject = addLFAProp(applicationFormGroup, "Загрузить файл анкеты", fileNativeApplicationFormProject);
        openFileNativeApplicationFormProject = addOFAProp(applicationFormGroup, "Открыть файл анкеты", fileNativeApplicationFormProject);
        fileForeignApplicationFormProject = addDProp("fileForeignApplicationFormProject", "Анкета на английском", CustomFileClass.instance, project);
        loadFileForeignApplicationFormProject = addLFAProp(applicationFormGroup, "Загрузить файл анкеты (иностр.)", fileForeignApplicationFormProject);
        openFileForeignApplicationFormProject = addOFAProp(applicationFormGroup, "Открыть файл анкеты (иностр.)", fileForeignApplicationFormProject);

        // источники финансирования
        isReturnInvestmentsProject = addDProp(sourcesFundingGroup, "isReturnInvestmentsProject", "Средства третьих лиц, привлекаемые на возвратной основе (заемные средства и т.п.)", LogicalClass.instance, project);
        nameReturnInvestorProject = addDProp(sourcesFundingGroup, "nameReturnInvestorProject", "Третьи лица для возврата средств", InsensitiveStringClass.get(2000), project);
        nameReturnInvestorProject.setMinimumWidth(10);
        nameReturnInvestorProject.setPreferredWidth(50);
        amountReturnFundsProject = addDProp(sourcesFundingGroup, "amountReturnFundsProject", "Объем средств на возвратной основе (тыс. руб.)", StringClass.get(30), project);
        hideNameReturnInvestorProject = addHideCaptionProp(privateGroup, "укажите данных лиц и их контактную информацию", nameReturnInvestorProject, isReturnInvestmentsProject);
        hideAmountReturnFundsProject = addHideCaptionProp(privateGroup, "укажите объем привлекаемых средств (тыс. руб.)", amountReturnFundsProject, isReturnInvestmentsProject);

        isNonReturnInvestmentsProject = addDProp(sourcesFundingGroup, "isNonReturnInvestmentsProject", "Средства третьих лиц, привлекаемые на безвозвратной основе (гранты и т.п.)", LogicalClass.instance, project);

        isCapitalInvestmentProject = addDProp(nonReturnFundingGroup, "isCapitalInvestmentProject", "Вклады в уставный капитал", LogicalClass.instance, project);
        isPropertyInvestmentProject = addDProp(nonReturnFundingGroup, "isPropertyInvestmentProject", "Вклады в имущество", LogicalClass.instance, project);
        isGrantsProject = addDProp(nonReturnFundingGroup, "isGrantsProject", "Гранты", LogicalClass.instance, project);

        isOtherNonReturnInvestmentsProject = addDProp(nonReturnFundingGroup, "isOtherNonReturnInvestmentsProject", "Иное", LogicalClass.instance, project);

        nameNonReturnInvestorProject = addDProp(nonReturnFundingGroup, "nameNonReturnInvestorProject", "Третьи лица для возврата средств", InsensitiveStringClass.get(2000), project);
        nameNonReturnInvestorProject.setMinimumWidth(10);
        nameNonReturnInvestorProject.setPreferredWidth(50);
        amountNonReturnFundsProject = addDProp(nonReturnFundingGroup, "amountNonReturnFundsProject", "Объем средств на безвозвратной основе (тыс. руб.)", StringClass.get(30), project);
        hideNameNonReturnInvestorProject = addHideCaptionProp(privateGroup, "укажите данных лиц и их контактную информацию", nameReturnInvestorProject, isNonReturnInvestmentsProject);
        hideAmountNonReturnFundsProject = addHideCaptionProp(privateGroup, "укажите объем привлекаемых средств (тыс. руб.)", amountNonReturnFundsProject, isNonReturnInvestmentsProject);

        commentOtherNonReturnInvestmentsProject = addDProp(nonReturnFundingGroup, "commentOtherNonReturnInvestmentsProject", "Комментарий", InsensitiveStringClass.get(2000), project);
        commentOtherNonReturnInvestmentsProject.setMinimumWidth(10);
        commentOtherNonReturnInvestmentsProject.setPreferredWidth(50);

        hideIsCapitalInvestmentProject = addHideCaptionProp(privateGroup, "Укажите", isCapitalInvestmentProject, isNonReturnInvestmentsProject);
        hideIsPropertyInvestmentProject = addHideCaptionProp(privateGroup, "Укажите", isPropertyInvestmentProject, isNonReturnInvestmentsProject);
        hideIsGrantsProject = addHideCaptionProp(privateGroup, "Укажите", isGrantsProject, isNonReturnInvestmentsProject);
        hideIsOtherNonReturnInvestmentsProject = addHideCaptionProp(privateGroup, "Укажите", isOtherNonReturnInvestmentsProject, isNonReturnInvestmentsProject);

        hideCommentOtherNonReturnInvestmentsProject = addHideCaptionProp(privateGroup, "Укажите", commentOtherNonReturnInvestmentsProject, isOtherNonReturnInvestmentsProject);

        isOwnFundsProject = addDProp(sourcesFundingGroup, "isOwnFundsProject", "Собственные средства организации", LogicalClass.instance, project);
        amountOwnFundsProject = addDProp(sourcesFundingGroup, "amountOwnFundsProject", "Объем собственных средств (тыс. руб.)", StringClass.get(30), project);
        hideAmountOwnFundsProject = addHideCaptionProp(privateGroup, "Укажите", amountOwnFundsProject, isOwnFundsProject);

        isPlanningSearchSourceProject = addDProp(sourcesFundingGroup, "isPlanningSearchSourceProject", "Планируется поиск источника финансирования проекта", LogicalClass.instance, project);
        amountFundsProject = addDProp(sourcesFundingGroup, "amountFundsProject", "Требуемый объем средств (тыс. руб.)", StringClass.get(30), project);
        hideAmountFundsProject = addHideCaptionProp(privateGroup, "Укажите", amountFundsProject, isPlanningSearchSourceProject);

        isOtherSoursesProject = addDProp(sourcesFundingGroup, "isOtherSoursesProject", "Иное", LogicalClass.instance, project);
        commentOtherSoursesProject = addDProp(sourcesFundingGroup, "commentOtherSoursesProject", "Комментарий", InsensitiveStringClass.get(2000), project);
        commentOtherSoursesProject.setMinimumWidth(10);
        commentOtherSoursesProject.setPreferredWidth(50);
        hideCommentOtherSoursesProject = addHideCaptionProp(privateGroup, "Укажите", commentOtherSoursesProject, isOtherSoursesProject);

        // оборудование
        isOwnedEquipmentProject = addDProp(equipmentGroup, "isOwnedEquipmentProject", "Оборудование имеется в собственности и/или в пользовании Вашей организации", LogicalClass.instance, project);

        isAvailableEquipmentProject = addDProp(equipmentGroup, "isAvailableEquipmentProject", "Оборудование имеется в открытом доступе на рынке, и Ваша организация планирует приобрести его в собственность и/или в пользование", LogicalClass.instance, project);

        isTransferEquipmentProject = addDProp(equipmentGroup, "isTransferEquipmentProject", "Оборудование отсутствует в открытом доступе на рынке, но достигнута договоренность с собственником оборудования о передаче данного оборудования в собственность и/или в пользование для реализации проекта", LogicalClass.instance, project);
        descriptionTransferEquipmentProject = addDProp(equipmentGroup, "descriptionTransferEquipmentProject", "Опишите данное оборудование", InsensitiveStringClass.get(2000), project);
        descriptionTransferEquipmentProject.setMinimumWidth(10);
        descriptionTransferEquipmentProject.setPreferredWidth(50);
        ownerEquipmentProject = addDProp(equipmentGroup, "ownerEquipmentProject", "Укажите собственника оборудования и его контактную информацию", InsensitiveStringClass.get(2000), project);
        ownerEquipmentProject.setMinimumWidth(10);
        ownerEquipmentProject.setPreferredWidth(50);
        hideDescriptionTransferEquipmentProject = addHideCaptionProp(privateGroup, "Укажите", descriptionTransferEquipmentProject, isTransferEquipmentProject);
        hideOwnerEquipmentProject = addHideCaptionProp(privateGroup, "Укажите", ownerEquipmentProject, isTransferEquipmentProject);

        isPlanningEquipmentProject = addDProp(equipmentGroup, "isPlanningEquipmentProject", "Ваша организация планирует использовать для реализации проекта оборудование, которое имеется в собственности или в пользовании Фонда «Сколково» (учрежденных им юридических лиц)", LogicalClass.instance, project);
        specificationEquipmentProject = addDProp(equipmentGroup, "specificationEquipmentProject", "Укажите данное оборудование", InsensitiveStringClass.get(2000), project);
        specificationEquipmentProject.setMinimumWidth(10);
        specificationEquipmentProject.setPreferredWidth(50);
        hideSpecificationEquipmentProject = addHideCaptionProp(privateGroup, "Укажите", specificationEquipmentProject, isPlanningEquipmentProject);

        isSeekEquipmentProject = addDProp(equipmentGroup, "isSeekEquipmentProject", "Оборудование имеется на рынке, но Ваша организация не имеет возможности приобрести его в собственность или в пользование и ищет возможность получить доступ к такому оборудованию", LogicalClass.instance, project);
        descriptionEquipmentProject = addDProp(equipmentGroup, "descriptionEquipmentProject", "Опишите данное оборудование", InsensitiveStringClass.get(2000), project);
        descriptionEquipmentProject.setMinimumWidth(10);
        descriptionEquipmentProject.setPreferredWidth(50);
        hideDescriptionEquipmentProject = addHideCaptionProp(privateGroup, "Укажите", descriptionEquipmentProject, isSeekEquipmentProject);

        isOtherEquipmentProject = addDProp(equipmentGroup, "isOtherEquipmentProject", "Иное", LogicalClass.instance, project);
        commentEquipmentProject = addDProp(equipmentGroup, "commentEquipmentProject", "Комментарий", InsensitiveStringClass.get(2000), project);
        commentEquipmentProject.setMinimumWidth(10);
        commentEquipmentProject.setPreferredWidth(50);
        hideCommentEquipmentProject = addHideCaptionProp(privateGroup, "Укажите", commentEquipmentProject, isOtherEquipmentProject);

        // документы
        fileNativeRoadMapProject = addDProp("fileNativeRoadMapProject", "Файл дорожной карты", CustomFileClass.instance, project);
        loadNativeFileRoadMapProject = addLFAProp(roadMapGroup, "Загрузить файл дорожной карты", fileNativeRoadMapProject);
        openNativeFileRoadMapProject = addOFAProp(roadMapGroup, "Открыть файл дорожной карты", fileNativeRoadMapProject);

        fileForeignRoadMapProject = addDProp("fileForeignRoadMapProject", "Файл дорожной карты (иностр.)", CustomFileClass.instance, project);
        loadForeignFileRoadMapProject = addLFAProp(roadMapGroup, "Загрузить файл дорожной карты (иностр.)", fileForeignRoadMapProject);
        openForeignFileRoadMapProject = addOFAProp(roadMapGroup, "Открыть файл дорожной карты (иностр.)", fileForeignRoadMapProject);

        fileResolutionIPProject = addDProp("fileResolutionIPProject", "Заявление IP", CustomFileClass.instance, project);
        loadFileResolutionIPProject = addLFAProp(resolutionIPGroup, "Загрузить файл заявление IP", fileResolutionIPProject);
        openFileResolutionIPProject = addOFAProp(resolutionIPGroup, "Открыть файл заявление IP", fileResolutionIPProject);

        fileNativeTechnicalDescriptionProject = addDProp("fileNativeTechnicalDescriptionProject", "Файл технического описания", CustomFileClass.instance, project);
        loadFileNativeTechnicalDescriptionProject = addLFAProp(techDescrGroup, "Загрузить файл технического описания", fileNativeTechnicalDescriptionProject);
        openFileNativeTechnicalDescriptionProject = addOFAProp(techDescrGroup, "Открыть файл технического описания", fileNativeTechnicalDescriptionProject);

        fileForeignTechnicalDescriptionProject = addDProp("fileForeignTechnicalDescriptionProject", "Файл технического описания (иностр.)", CustomFileClass.instance, project);
        loadFileForeignTechnicalDescriptionProject = addLFAProp(techDescrGroup, "Загрузить файл технического описания (иностр.)", fileForeignTechnicalDescriptionProject);
        openFileForeignTechnicalDescriptionProject = addOFAProp(techDescrGroup, "Открыть файл технического описания (иностр.)", fileForeignTechnicalDescriptionProject);

        // патенты
        projectPatent = addDProp(idGroup, "projectPatent", "Проект патента", project, patent);

        nativeTypePatent = addDProp(baseGroup, "nativeTypePatent", "Тип заявки/патента", InsensitiveStringClass.get(2000), patent);
        nativeTypePatent.setMinimumWidth(10);
        nativeTypePatent.setPreferredWidth(50);
        foreignTypePatent = addDProp(baseGroup, "foreignTypePatent", "Type of application/patent", InsensitiveStringClass.get(2000), patent);
        foreignTypePatent.setMinimumWidth(10);
        foreignTypePatent.setPreferredWidth(50);
        nativeNumberPatent = addDProp(baseGroup, "nativeNumberPatent", "Номер", InsensitiveStringClass.get(2000), patent);
        nativeNumberPatent.setMinimumWidth(10);
        nativeNumberPatent.setPreferredWidth(50);
        sidProjectPatent = addJProp("sidProjectPatent", sidProject, projectPatent, 1);
        nativeNumberToPatent = addAGProp("nativeNumberToPatent", "Патент (номер патента, внутренний ID проекта)", nativeNumberPatent, projectPatent);
        nativeNumberSIDToPatent = addJProp("nativeNumberSIDToPatent", "Патент (номер патента, внешний ID проекта)", nativeNumberToPatent, 1, sidToProject, 2);

        foreignNumberPatent = addDProp(baseGroup, "foreignNumberPatent", "Reference number", InsensitiveStringClass.get(2000), patent);
        foreignNumberPatent.setMinimumWidth(10);
        foreignNumberPatent.setPreferredWidth(50);
        priorityDatePatent = addDProp(baseGroup, "priorityDatePatent", "Дата приоритета", DateClass.instance, patent);

        isOwned = addDProp(baseGroup, "isOwned", "Организация не обладает исключительными правами на указанные результаты интеллектуальной деятельности", LogicalClass.instance, patent);
        ownerPatent = addDProp(baseGroup, "ownerPatent", "Укажите правообладателя и его контактную информацию", InsensitiveStringClass.get(2000), patent);
        ownerPatent.setMinimumWidth(10);
        ownerPatent.setPreferredWidth(50);

        nameForeignOwnerType = addDProp(baseGroup, "nameForeignOwnerType", "Имя (иностр.)", InsensitiveStringClass.get(110), ownerType);

        ownerTypePatent = addDProp(idGroup, "ownerTypePatent", "Кем является правообладатель (ИД)", ownerType, patent);
        nameNativeOwnerTypePatent = addJProp(baseGroup, "nameNativeOwnerTypePatent", "Кем является правообладатель", baseLM.name, ownerTypePatent, 1);
        nameForeignOwnerTypePatent = addJProp(baseGroup, "nameForeignOwnerTypePatent", "Кем является правообладатель (иностр.)", nameForeignOwnerType, ownerTypePatent, 1);

        ownerTypeToSID = addAGProp("ownerTypeToSID", "SID типа правообладателя", addJProp(baseLM.and1, baseLM.classSID, 1, is(ownerType), 1));
        projectTypeToSID = addAGProp("projectTypeToSID", "SID типа проекта", addJProp(baseLM.and1, baseLM.classSID, 1, is(projectType), 1));
        projectActionToSID = addAGProp("projectActionToSID", "SID текущего статуса", addJProp(baseLM.and1, baseLM.classSID, 1, is(projectAction), 1));
        fileIntentionOwnerPatent = addDProp("fileIntentionOwnerPatent", "Файл документа о передаче права", CustomFileClass.instance, patent);
        loadFileIntentionOwnerPatent = addLFAProp(baseGroup, "Загрузить файл документа о передаче права", fileIntentionOwnerPatent);
        openFileIntentionOwnerPatent = addOFAProp(baseGroup, "Открыть файл документа о передаче права", fileIntentionOwnerPatent);

        hideOwnerPatent = addHideCaptionProp(privateGroup, "Укажите", ownerPatent, isOwned);
        hideNameNativeOwnerTypePatent = addHideCaptionProp(privateGroup, "Укажите", nameNativeOwnerTypePatent, isOwned);
        hideNameForeignOwnerTypePatent = addHideCaptionProp(privateGroup, "Укажите", nameForeignOwnerTypePatent, isOwned);
        hideFileIntentionOwnerPatent = addHideCaptionProp(privateGroup, "Укажите", fileIntentionOwnerPatent, isOwned);
        hideLoadFileIntentionOwnerPatent = addHideCaptionProp(privateGroup, "Укажите", loadFileIntentionOwnerPatent, isOwned);
        hideOpenFileIntentionOwnerPatent = addHideCaptionProp(privateGroup, "Укажите", openFileIntentionOwnerPatent, isOwned);

        isValuated = addDProp(baseGroup, "isValuated", "Проводилась ли оценка указанных результатов интеллектальной деятельности", LogicalClass.instance, patent);
        valuatorPatent = addDProp(baseGroup, "valuatorPatent", "Укажите оценщика и его контактную информацию", InsensitiveStringClass.get(2000), patent);
        valuatorPatent.setMinimumWidth(10);
        valuatorPatent.setPreferredWidth(50);
        fileActValuationPatent = addDProp("fileActValuationPatent", "Файл акта оценки", CustomFileClass.instance, patent);
        loadFileActValuationPatent = addLFAProp(baseGroup, "Загрузить файл акта оценки", fileActValuationPatent);
        openFileActValuationPatent = addOFAProp(baseGroup, "Открыть файл акта оценки", fileActValuationPatent);
        hideValuatorPatent = addHideCaptionProp(privateGroup, "Укажите", valuatorPatent, isValuated);
        hideFileActValuationPatent = addHideCaptionProp(privateGroup, "Укажите", fileActValuationPatent, isValuated);
        hideLoadFileActValuationPatent = addHideCaptionProp(privateGroup, "Укажите", loadFileActValuationPatent, isValuated);
        hideOpenFileActValuationPatent = addHideCaptionProp(privateGroup, "Укажите", openFileActValuationPatent, isValuated);

        // учёные
        projectAcademic = addDProp(idGroup, "projectAcademic", "Проект ученого", project, academic);
        sidProjectAcademic = addJProp("sidProjectAcademic", sidProject, projectAcademic, 1);
        fullNameAcademic = addDProp(baseGroup, "fullNameAcademic", "ФИО", InsensitiveStringClass.get(2000), academic);
        fullNameAcademic.setMinimumWidth(10);
        fullNameAcademic.setPreferredWidth(50);
        fullNameToAcademic = addAGProp("fullNameToAcademic", "ФИО учёного (фамилия, внутренний ИД проекта)", fullNameAcademic, projectAcademic);
        fullNameSIDToAcademic = addJProp("fullNameSIDToAcademic", "Учёный (фамилия, внешний ИД проекта)", fullNameToAcademic, 1, sidToProject, 2);
        institutionAcademic = addDProp(baseGroup, "institutionAcademic", "Учреждение, в котором данный специалист осуществляет научную и (или) преподавательскую деятельность", InsensitiveStringClass.get(2000), academic);
        institutionAcademic.setMinimumWidth(10);
        institutionAcademic.setPreferredWidth(50);
        titleAcademic = addDProp(baseGroup, "titleAcademic", "Ученая степень, звание, должность и др.", InsensitiveStringClass.get(2000), academic);
        titleAcademic.setMinimumWidth(10);
        titleAcademic.setPreferredWidth(50);

        fileDocumentConfirmingAcademic = addDProp("fileDocumentConfirmingAcademic", "Файл трудового договора", CustomFileClass.instance, academic);
        loadFileDocumentConfirmingAcademic = addLFAProp(baseGroup, "Загрузить файл трудового договора", fileDocumentConfirmingAcademic);
        openFileDocumentConfirmingAcademic = addOFAProp(baseGroup, "Открыть файл трудового договора", fileDocumentConfirmingAcademic);

        fileDocumentEmploymentAcademic = addDProp("fileDocumentEmploymentAcademic", "Файл заявления специалиста", CustomFileClass.instance, academic);
        loadFileDocumentEmploymentAcademic = addLFAProp(baseGroup, "Загрузить файл заявления", fileDocumentEmploymentAcademic);
        openFileDocumentEmploymentAcademic = addOFAProp(baseGroup, "Открыть файл заявления", fileDocumentEmploymentAcademic);

        // иностранные специалисты
        projectNonRussianSpecialist = addDProp(idGroup, "projectNonRussianSpecialist", "Проект иностранного специалиста", project, nonRussianSpecialist);
        sidProjectNonRussianSpecialist = addJProp("sidProjectNonRussianSpecialist", sidProject, projectNonRussianSpecialist, 1);

        fullNameNonRussianSpecialist = addDProp(baseGroup, "fullNameNonRussianSpecialist", "ФИО", InsensitiveStringClass.get(2000), nonRussianSpecialist);
        fullNameNonRussianSpecialist.setMinimumWidth(10);
        fullNameNonRussianSpecialist.setPreferredWidth(50);
        fullNameToNonRussianSpecialist = addAGProp("fullNameToNonRussianSpecialist", "Иностранный специалист (фамилия, внутренний ИД проекта)", fullNameNonRussianSpecialist, projectNonRussianSpecialist);
        fullNameSIDToNonRussianSpecialist = addJProp("fullNameSIDToNonRussianSpecialist", "Иностранный специалист (фамилия, внешний ИД проекта)", fullNameToNonRussianSpecialist, 1, sidToProject, 2);
        organizationNonRussianSpecialist = addDProp(baseGroup, "organizationNonRussianSpecialist", "Место работы", InsensitiveStringClass.get(2000), nonRussianSpecialist);
        organizationNonRussianSpecialist.setMinimumWidth(10);
        organizationNonRussianSpecialist.setPreferredWidth(50);
        titleNonRussianSpecialist = addDProp(baseGroup, "titleNonRussianSpecialist", "Должность, если есть - ученая степень, звание и др.", InsensitiveStringClass.get(2000), nonRussianSpecialist);
        titleNonRussianSpecialist.setMinimumWidth(10);
        titleNonRussianSpecialist.setPreferredWidth(50);

        fileForeignResumeNonRussianSpecialist = addDProp("fileForeignResumeNonRussianSpecialist", "File Curriculum Vitae", CustomFileClass.instance, nonRussianSpecialist);
        loadFileForeignResumeNonRussianSpecialist = addLFAProp(baseGroup, "Load file Curriculum Vitae", fileForeignResumeNonRussianSpecialist);
        openFileForeignResumeNonRussianSpecialist = addOFAProp(baseGroup, "Open file Curriculum Vitae", fileForeignResumeNonRussianSpecialist);

        fileNativeResumeNonRussianSpecialist = addDProp("fileNativeResumeNonRussianSpecialist", "Файл резюме специалиста", CustomFileClass.instance, nonRussianSpecialist);
        loadFileNativeResumeNonRussianSpecialist = addLFAProp(baseGroup, "Загрузить файл резюме", fileNativeResumeNonRussianSpecialist);
        openFileNativeResumeNonRussianSpecialist = addOFAProp(baseGroup, "Открыть файл резюме", fileNativeResumeNonRussianSpecialist);

        filePassportNonRussianSpecialist = addDProp("filePassportNonRussianSpecialist", "Файл паспорта", CustomFileClass.instance, nonRussianSpecialist);
        loadFilePassportNonRussianSpecialist = addLFAProp(baseGroup, "Загрузить файл паспорта", filePassportNonRussianSpecialist);
        openFilePassportNonRussianSpecialist = addOFAProp(baseGroup, "Открыть файл паспорта", filePassportNonRussianSpecialist);

        fileStatementNonRussianSpecialist = addDProp("fileStatementNonRussianSpecialist", "Файл заявления", CustomFileClass.instance, nonRussianSpecialist);
        loadFileStatementNonRussianSpecialist = addLFAProp(baseGroup, "Загрузить файл заявления", fileStatementNonRussianSpecialist);
        openFileStatementNonRussianSpecialist = addOFAProp(baseGroup, "Открыть файл заявления", fileStatementNonRussianSpecialist);

        nameNativeJoinClaimerVote = addJProp(baseGroup, "nameNativeJoinClaimerVote", nameNativeClaimer, claimerVote, 1);
        nameForeignJoinClaimerVote = addJProp(baseGroup, "nameForeignJoinClaimerVote", nameForeign, claimerVote, 1);

        nameNativeClaimerVote = addIfElseUProp(baseGroup, "nameNativeClaimerVote", "Заявитель", addJProp(nameNativeUnionManagerProject, projectVote, 1), nameNativeJoinClaimerVote, isPreliminaryVote, 1);
        nameNativeClaimerVote.setMinimumWidth(10);
        nameNativeClaimerVote.setPreferredWidth(50);
        nameForeignClaimerVote = addIfElseUProp(baseGroup, "nameForeignClaimerVote", "Заявитель (иностр.)", addJProp(nameForeignUnionManagerProject, projectVote, 1), nameForeignJoinClaimerVote, isPreliminaryVote, 1);
        nameForeignClaimerVote.setMinimumWidth(10);
        nameForeignClaimerVote.setPreferredWidth(50);
        nameGenitiveClaimerVote = addIfElseUProp(baseGroup, "nameGenitiveClaimerVote", "Заявитель (кого)", addJProp(nameGenitiveManagerProject, projectVote, 1), nameNativeJoinClaimerVote, isPreliminaryVote, 1);
        nameGenitiveClaimerVote.setMinimumWidth(10);
        nameGenitiveClaimerVote.setPreferredWidth(50);
        nameDativusClaimerVote = addIfElseUProp(baseGroup, "nameDativusClaimerVote", "Заявитель (кому)", addJProp(nameDativusManagerProject, projectVote, 1), nameNativeJoinClaimerVote, isPreliminaryVote, 1);
        nameDativusClaimerVote.setMinimumWidth(10);
        nameDativusClaimerVote.setPreferredWidth(50);
        nameAblateClaimerVote = addIfElseUProp(baseGroup, "nameAblateClaimerVote", "Заявитель (кем)", addJProp(nameAblateManagerProject, projectVote, 1), nameNativeJoinClaimerVote, isPreliminaryVote, 1);
        nameAblateClaimerVote.setMinimumWidth(10);
        nameAblateClaimerVote.setPreferredWidth(50);

        documentTemplateDocumentTemplateDetail = addDProp(idGroup, "documentTemplateDocumentTemplateDetail", "Шаблон (ИД)", documentTemplate, documentTemplateDetail);

        projectDocument = addDProp(idGroup, "projectDocument", "Проект (ИД)", project, document);
        nameNativeProjectDocument = addJProp(baseGroup, "nameNativeProjectDocument", "Проект", nameNativeProject, projectDocument, 1);

        quantityMinLanguageDocumentType = addDProp(baseGroup, "quantityMinLanguageDocumentType", "Мин. док.", IntegerClass.instance, language, documentType);
        quantityMaxLanguageDocumentType = addDProp(baseGroup, "quantityMaxLanguageDocumentType", "Макс. док.", IntegerClass.instance, language, documentType);
        LP singleLanguageDocumentType = addJProp("Один док.", baseLM.equals2, quantityMaxLanguageDocumentType, 1, 2, addCProp(IntegerClass.instance, 1));
        LP multipleLanguageDocumentType = addJProp(baseLM.andNot1, addCProp(LogicalClass.instance, true, language, documentType), 1, 2, singleLanguageDocumentType, 1, 2);
        translateLanguageDocumentType = addDProp(baseGroup, "translateLanguageDocumentType", "Перевод", StringClass.get(50), language, documentType);

        languageExpert = addDProp(idGroup, "languageExpert", "Язык (ИД)", language, expert);
        nameLanguageExpert = addJProp(baseGroup, "nameLanguageExpert", "Язык", baseLM.name, languageExpert, 1);
        nameLanguageExpert.setFixedCharWidth(10);

        languageDocument = addDProp(idGroup, "languageDocument", "Язык (ИД)", language, documentAbstract);
        nameLanguageDocument = addJProp(baseGroup, "nameLanguageDocument", "Язык", baseLM.name, languageDocument, 1);
        englishDocument = addJProp("englishDocument", "Иностр.", baseLM.equals2, languageDocument, 1, addCProp(language, "english"));

        defaultEnglishDocumentType = addDProp(baseGroup, "defaultEnglishDocumentType", "Англ.", LogicalClass.instance, documentType);

        typeDocument = addDProp(idGroup, "typeDocument", "Тип (ИД)", documentType, documentAbstract);
        nameTypeDocument = addJProp(baseGroup, "nameTypeDocument", "Тип", baseLM.name, typeDocument, 1);
        defaultEnglishDocument = addJProp("defaultEnglishDocument", "Англ.", defaultEnglishDocumentType, typeDocument, 1);

        localeLanguage = addDProp(baseGroup, "localeLanguage", "Locale", StringClass.get(5), language);
        authExpertSubjectLanguage = addDProp(baseGroup, "authExpertSubjectLanguage", "Заголовок аутентификации эксперта", StringClass.get(100), language);
        letterExpertSubjectLanguage = addDProp(baseGroup, "letterExpertSubjectLanguage", "Заголовок письма о заседании", StringClass.get(100), language);

        LP multipleDocument = addJProp(multipleLanguageDocumentType, languageDocument, 1, typeDocument, 1);
        postfixDocument = addJProp(baseLM.and1, addDProp("postfixDocument", "Доп. описание", StringClass.get(15), document), 1, multipleDocument, 1);
        hidePostfixDocument = addJProp(baseLM.and1, addCProp(StringClass.get(40), "Постфикс", document), 1, multipleDocument, 1);

        LP translateNameDocument = addJProp("Перевод", translateLanguageDocumentType, languageDocument, 1, typeDocument, 1);
        nameDocument = addJProp("nameDocument", "Заголовок", baseLM.string2, translateNameDocument, 1, addSUProp(Union.OVERRIDE, addCProp(StringClass.get(15), "", document), postfixDocument), 1);

        quantityProjectLanguageDocumentType = addSGProp("projectLanguageDocumentType", true, "Кол-во док.", addCProp(IntegerClass.instance, 1, document), projectDocument, 1, languageDocument, 1, typeDocument, 1); // сколько экспертов высказалось
        LP notEnoughProjectLanguageDocumentType = addSUProp(Union.OVERRIDE, addJProp(baseLM.greater2, quantityProjectLanguageDocumentType, 1, 2, 3, quantityMaxLanguageDocumentType, 2, 3),
                addJProp(baseLM.less2, addSUProp(Union.OVERRIDE, addCProp(IntegerClass.instance, 0, project, language, documentType), quantityProjectLanguageDocumentType), 1, 2, 3, quantityMinLanguageDocumentType, 2, 3));
        notEnoughProject = addMGProp(projectStatusGroup, "notEnoughProject", true, "Недостаточно док.", notEnoughProjectLanguageDocumentType, 1);

        autoGenerateProject = addDProp(projectStatusGroup, "autoGenerateProject", "Авт. зас.", LogicalClass.instance, project);
        inactiveProject = addDProp(projectStatusGroup, "inactiveProject", "Не акт.", LogicalClass.instance, project);

        fileDocument = addDProp(baseGroup, "fileDocument", "Файл", PDFClass.instance, document);
        loadFileDocument = addLFAProp(baseGroup, "Загрузить", fileDocument);
        openFileDocument = addOFAProp(baseGroup, "Открыть", fileDocument);

        inDefaultDocumentLanguage = addJProp("inDefaultDocumentLanguage", "Вкл. (по умолчанию)", and(false, false, true),
                englishDocument, 1, // если документ на английском
                defaultEnglishDocument, 1, // если для типа документа можно только на английском
                is(language), 2, // второй параметр - язык
                addJProp(addMGProp(object(document), projectDocument, 1, typeDocument, 1, postfixDocument, 1, languageDocument, 1), projectDocument, 1, typeDocument, 1, postfixDocument, 1, 2), 1, 2); // нету документа на русском
        inDefaultDocumentExpert = addJProp("inDefaultDocumentExpert", "Вкл.", inDefaultDocumentLanguage, 1, languageExpert, 2);

        inDocumentLanguage = addJProp("inDocumentLanguage", "Вкл.", baseLM.equals2, languageDocument, 1, 2);
        inDocumentExpert = addJProp("inDocumentExpert", "Вкл.", inDocumentLanguage, 1, languageExpert, 2);

        inExpertVote = addDProp(baseGroup, "inExpertVote", "Вкл", LogicalClass.instance, expert, vote); // !!! нужно отослать письмо с документами и т.д
        oldExpertVote = addDProp(baseGroup, "oldExpertVote", "Из предыдущего заседания", LogicalClass.instance, expert, vote); // !!! нужно отослать письмо с документами и т.д
        inNewExpertVote = addJProp(baseGroup, "inNewExpertVote", "Вкл (нов.)", baseLM.andNot1, inExpertVote, 1, 2, oldExpertVote, 1, 2);
        inOldExpertVote = addJProp(baseGroup, "inOldExpertVote", "Вкл (стар.)", baseLM.and1, inExpertVote, 1, 2, oldExpertVote, 1, 2);

        dateStartVote = addJProp(baseGroup, "dateStartVote", true, "Дата начала", baseLM.and1, baseLM.date, 1, is(vote), 1);
//        dateEndVote = addJProp(baseGroup, "dateEndVote", "Дата окончания", addDate2, dateStartVote, 1, requiredPeriod);
        aggrDateEndVote = addJProp(baseGroup, "aggrDateEndVote", "Дата окончания (агр.)", baseLM.addDate2, dateStartVote, 1, requiredPeriod);
        dateEndVote = addDProp(baseGroup, "dateEndVote", "Дата окончания", DateClass.instance, vote);
        dateEndVote.setDerivedForcedChange(true, aggrDateEndVote, 1, dateStartVote, 1);

        weekStartVote = addJProp("weekStartVote", true, "Неделя начала", baseLM.weekInDate, dateStartVote, 1);
        quantityNewExpertWeek = addSGProp("quantityNewExpertWeek", "Кол-во заседаний", addJProp(baseLM.and1, addCProp(IntegerClass.instance, 1), inNewExpertVote, 1, 2), 1, weekStartVote, 2);
        quantityNewExpertWeek.setFixedCharWidth(2);
        quantityNewWeek = addSGProp("quantityNewWeek", "Кол-во заседаний", quantityNewExpertWeek, 2);

        quantityPreliminaryVoteProject = addSGProp("quantityPreliminaryVoteProject", true, "Количество заседаний",
                addJProp(baseLM.and1, addCProp(IntegerClass.instance, 1, vote), 1,
                        isPreliminaryVote, 1), projectVote, 1);

        openedVote = addJProp(baseGroup, "openedVote", "Открыто", baseLM.groeq2, dateEndVote, 1, baseLM.currentDate);
        closedVote = addJProp(baseGroup, "closedVote", "Закрыто", baseLM.andNot1, is(vote), 1, openedVote, 1);

        voteInProgressProject = addAGProp(idGroup, "voteInProgressProject", true, "Тек. заседание (ИД)",
                openedVote, 1, projectVote, 1); // активно только одно заседание

        voteInProgressRepeatProject = addJProp("voteInProgressRepeatProject", true, "Тек. заседание (ИД) (повт.)", baseLM.and1,
                voteInProgressProject, 1,
                addJProp(baseLM.greater2, quantityVoteProject, 1, addCProp(IntegerClass.instance, 1)), 1);

        // результаты голосования
        voteResultExpertVote = addDProp(idGroup, "voteResultExpertVote", "Результат (ИД)", voteResult, expert, vote);
        voteResultNewExpertVote = addJProp(baseGroup, "voteResultNewExpertVote", "Результат (ИД) (новый)", baseLM.and1,
                voteResultExpertVote, 1, 2, inNewExpertVote, 1, 2);

        dateExpertVote = addDProp(voteResultCheckGroup, "dateExpertVote", "Дата рез.", DateClass.instance, expert, vote);

        doneExpertVote = addJProp(baseGroup, "doneExpertVote", "Проголосовал", baseLM.equals2,
                voteResultExpertVote, 1, 2, addCProp(voteResult, "voted"));
        doneNewExpertVote = addJProp(baseGroup, "doneNewExpertVote", "Проголосовал (новый)", baseLM.and1,
                doneExpertVote, 1, 2, inNewExpertVote, 1, 2);
        doneOldExpertVote = addJProp(baseGroup, "doneOldExpertVote", "Проголосовал (старый)", baseLM.and1,
                doneExpertVote, 1, 2, inOldExpertVote, 1, 2);

        refusedExpertVote = addJProp(baseGroup, "refusedExpertVote", "Проголосовал", baseLM.equals2,
                voteResultExpertVote, 1, 2, addCProp(voteResult, "refused"));

        connectedExpertVote = addJProp(baseGroup, "connectedExpertVote", "Аффилирован", baseLM.equals2,
                voteResultExpertVote, 1, 2, addCProp(voteResult, "connected"));

        nameVoteResultExpertVote = addJProp(voteResultCheckGroup, "nameVoteResultExpertVote", "Результат", baseLM.name, voteResultExpertVote, 1, 2);

        LP incrementVote = addJProp(baseLM.greater2, dateEndVote, 1, addCProp(DateClass.instance, new java.sql.Date(2011 - 1900, 4 - 1, 26)));
        inProjectExpert = addMGProp(baseGroup, "inProjectExpert", "Вкл. в заседания", inExpertVote, projectVote, 2, 1);
        voteProjectExpert = addAGProp(baseGroup, "voteProjectExpert", "Результ. заседание", addJProp(baseLM.and1, voteResultNewExpertVote, 1, 2, incrementVote, 2), 2, projectVote, 2);
        voteResultProjectExpert = addJProp(baseGroup, "voteResultProjectExpert", "Результ. заседания", voteResultExpertVote, 2, voteProjectExpert, 1, 2);
        doneProjectExpert = addJProp(baseGroup, "doneProjectExpert", "Проголосовал", baseLM.equals2, voteResultProjectExpert, 1, 2, addCProp(voteResult, "voted"));
        LP doneProject = addSGProp(baseGroup, "doneProject", "Проголосовало", addJProp(baseLM.and1, addCProp(IntegerClass.instance, 1), doneProjectExpert, 1, 2), 2); // сколько экспертов высказалось

        // vote Revision 1
        inClusterExpertVote = addDProp(voteResultCheckGroup, "inClusterExpertVote", "Соотв-ие кластеру", LogicalClass.instance, expert, voteR1);
        inClusterNewExpertVote = addJProp(baseGroup, "inClusterNewExpertVote", "Соотв-ие кластеру (новый)", baseLM.and1,
                inClusterExpertVote, 1, 2, inNewExpertVote, 1, 2);

        innovativeExpertVote = addDProp(voteResultCheckGroup, "innovativeExpertVote", "Инновац.", LogicalClass.instance, expert, voteR1);
        innovativeNewExpertVote = addJProp(baseGroup, "innovativeNewExpertVote", "Инновац. (новый)", baseLM.and1,
                innovativeExpertVote, 1, 2, inNewExpertVote, 1, 2);

        foreignExpertVote = addDProp(voteResultCheckGroup, "foreignExpertVote", "Иностр. специалист", LogicalClass.instance, expert, voteR1);
        foreignNewExpertVote = addJProp(baseGroup, "foreignNewExpertVote", "Иностр. специалист (новый)", baseLM.and1,
                foreignExpertVote, 1, 2, inNewExpertVote, 1, 2);

        innovativeCommentExpertVote = addDProp(voteResultCommentGroup, "innovativeCommentExpertVote", "Инновационность (комм.)", TextClass.instance, expert, voteR1);
        competentExpertVote = addDProp(voteResultCheckGroup, "competentExpertVote", "Компет.", IntegerClass.instance, expert, voteR1);
        completeExpertVote = addDProp(voteResultCheckGroup, "completeExpertVote", "Полнота информ.", IntegerClass.instance, expert, voteR1);
        completeCommentExpertVote = addDProp(voteResultCommentGroup, "completeCommentExpertVote", "Полнота информации (комм.)", TextClass.instance, expert, voteR1);

        followed(doneExpertVote, inClusterExpertVote, innovativeExpertVote, foreignExpertVote, innovativeCommentExpertVote, competentExpertVote, completeExpertVote, completeCommentExpertVote);
        followed(voteResultExpertVote, dateExpertVote);

        // vote Revision 2

        percentNeededVote = addDProp(baseGroup, "percentNeededVote", "Процент голосования", DoubleClass.instance, voteR2);

        competitiveAdvantagesExpertVote = addDProp(voteResultCheckGroup, "competitiveAdvantagesExpertVote", "Конкур. преим.", LogicalClass.instance, expert, voteR2);
        commercePotentialExpertVote = addDProp(voteResultCheckGroup, "commercePotentialExpertVote", "Потенциал коммерц.", LogicalClass.instance, expert, voteR2);
        canBeImplementedExpertVote = addDProp(voteResultCheckGroup, "canBeImplementedExpertVote", "Теоретически реализуем", LogicalClass.instance, expert, voteR2);
        haveExpertiseExpertVote = addDProp(voteResultCheckGroup, "haveExpertiseExpertVote", "Наличие экспертизы", LogicalClass.instance, expert, voteR2);
        internationalExperienceExpertVote = addDProp(voteResultCheckGroup, "internationalExperienceExpertVote", "Международный опыт", LogicalClass.instance, expert, voteR2);

        commentCompetitiveAdvantagesExpertVote = addDProp(voteResultCommentGroup, "commentCompetitiveAdvantagesExpertVote", "Конкур. преим. (обоснование)", TextClass.instance, expert, voteR2);
        commentCommercePotentialExpertVote = addDProp(voteResultCommentGroup, "commentCommercePotentialExpertVote", "Потенциал коммерц. (обоснование)", TextClass.instance, expert, voteR2);
        commentCanBeImplementedExpertVote = addDProp(voteResultCommentGroup, "commentCanBeImplementedExpertVote", "Теоретически реализуем (обоснование)", TextClass.instance, expert, voteR2);
        commentHaveExpertiseExpertVote = addDProp(voteResultCommentGroup, "commentHaveExpertiseExpertVote", "Наличие экспертизы (обоснование)", TextClass.instance, expert, voteR2);
        commentInternationalExperienceExpertVote = addDProp(voteResultCommentGroup, "commentInternationalExperienceExpertVote", "Международный опыт (обоснование)", TextClass.instance, expert, voteR2);

        followed(doneExpertVote, competitiveAdvantagesExpertVote, commercePotentialExpertVote, canBeImplementedExpertVote, haveExpertiseExpertVote, internationalExperienceExpertVote,
                commentCompetitiveAdvantagesExpertVote, commentCommercePotentialExpertVote, commentCanBeImplementedExpertVote, commentHaveExpertiseExpertVote, commentInternationalExperienceExpertVote);
        followed(voteResultExpertVote, dateExpertVote);

        quantityInVote = addSGProp(voteResultGroup, "quantityInVote", true, "Участвовало",
                addJProp(baseLM.and1, addCProp(IntegerClass.instance, 1), inExpertVote, 1, 2), 2); // сколько экспертов учавстовало

        quantityInOldVote = addSGProp(voteResultGroup, "quantityInOldVote", true, "Участвовало",
                addJProp(baseLM.and1, addCProp(IntegerClass.instance, 1), oldExpertVote, 1, 2), 2); // сколько экспертов учавстовало

        quantityRepliedVote = addSGProp(voteResultGroup, "quantityRepliedVote", true, "Ответило",
                addJProp(baseLM.and1, addCProp(IntegerClass.instance, 1), voteResultExpertVote, 1, 2), 2); // сколько экспертов высказалось

        quantityDoneVote = addSGProp(voteResultGroup, "quantityDoneVote", true, "Проголосовало",
                addJProp(baseLM.and1, addCProp(IntegerClass.instance, 1), doneExpertVote, 1, 2), 2); // сколько экспертов высказалось

        quantityDoneNewVote = addSGProp(voteResultGroup, "quantityDoneNewVote", true, "Проголосовало (нов.)",
                addJProp(baseLM.and1, addCProp(IntegerClass.instance, 1), doneNewExpertVote, 1, 2), 2); // сколько новых экспертов высказалось

        quantityDoneOldVote = addSGProp(voteResultGroup, "quantityDoneOldVote", true, "Проголосовало (пред.)",
                addJProp(baseLM.and1, addCProp(IntegerClass.instance, 1), doneOldExpertVote, 1, 2), 2); // сколько старых экспертов высказалось

        quantityRefusedVote = addSGProp(voteResultGroup, "quantityRefusedVote", true, "Отказалось",
                addJProp(baseLM.and1, addCProp(IntegerClass.instance, 1), refusedExpertVote, 1, 2), 2); // сколько экспертов высказалось

        quantityConnectedVote = addSGProp(voteResultGroup, "quantityConnectedVote", true, "Аффилировано",
                addJProp(baseLM.and1, addCProp(IntegerClass.instance, 1), connectedExpertVote, 1, 2), 2); // сколько экспертов высказалось

        quantityInClusterVote = addSGProp(voteResultGroup, "quantityInClusterVote", true, "Соотв-ие кластеру (голоса)",
                addJProp(baseLM.and1, addCProp(IntegerClass.instance, 1), inClusterExpertVote, 1, 2), 2); // сколько экспертов высказалось

        quantityInnovativeVote = addSGProp(voteResultGroup, "quantityInnovativeVote", true, "Инновац. (голоса)",
                addJProp(baseLM.and1, addCProp(IntegerClass.instance, 1), innovativeExpertVote, 1, 2), 2); // сколько экспертов высказалось

        quantityForeignVote = addSGProp(voteResultGroup, "quantityForeignVote", true, "Иностр. специалист (голоса)",
                addJProp(baseLM.and1, addCProp(IntegerClass.instance, 1), foreignExpertVote, 1, 2), 2); // сколько экспертов высказалось

        acceptedInClusterVote = addJProp(voteResultGroup, "acceptedInClusterVote", "Соотв-ие кластеру", baseLM.greater2,
                addJProp(baseLM.multiplyIntegerBy2, quantityInClusterVote, 1), 1,
                quantityDoneVote, 1);

        acceptedInnovativeVote = addJProp(voteResultGroup, "acceptedInnovativeVote", "Инновац.", baseLM.greater2,
                addJProp(baseLM.multiplyIntegerBy2, quantityInnovativeVote, 1), 1,
                quantityDoneVote, 1);

        acceptedForeignVote = addJProp(voteResultGroup, "acceptedForeignVote", "Иностр. специалист", baseLM.greater2,
                addJProp(baseLM.multiplyIntegerBy2, quantityForeignVote, 1), 1,
                quantityDoneVote, 1);

        acceptedVote = addJProp(voteResultGroup, "acceptedVote", true, "Положительно", and(false, false),
                acceptedInClusterVote, 1, acceptedInnovativeVote, 1, acceptedForeignVote, 1);

        succeededVote = addJProp(voteResultGroup, "succeededVote", true, "Состоялось", baseLM.groeq2, quantityDoneVote, 1, limitExperts); // достаточно экспертов
        openedSucceededVote = addJProp("openedSucceededVote", "Открыто и состоялось", baseLM.and1, succeededVote, 1, openedVote, 1);
        closedSucceededVote = addJProp("closedSucceededVote", "Закрыто и состоялось", baseLM.andNot1, succeededVote, 1, openedVote, 1);

        closedAcceptedVote = addJProp("closedAcceptedVote", "Закрыто и положительно", baseLM.and1, closedSucceededVote, 1, acceptedVote, 1);
        closedRejectedVote = addJProp("closedRejectedVote", "Закрыто и отрицательно", baseLM.andNot1, closedSucceededVote, 1, acceptedVote, 1);

        closedAcceptedStatusVote = addJProp("closedAcceptedStatusVote", "Закрыто и положительно (статус участника)", baseLM.and1, closedAcceptedVote, 1, isStatusVote, 1);
        closedAcceptedPreliminaryVote = addJProp("closedAcceptedPreliminaryVote", "Закрыто и положительно (предварительная экспертиза)", baseLM.and1, closedAcceptedVote, 1, isPreliminaryVote, 1);
//        succeededClusterVote = addJProp("succeededClusterVote", "Состоялось в тек. кластере", baseLM.and1, succeededVote, 1, equalsClusterProjectVote, 1);

        LP betweenExpertVoteDateFromDateTo = addJProp(baseLM.betweenDates, dateExpertVote, 1, 2, 3, 4);
        doneExpertVoteDateFromDateTo = addJProp(baseLM.and1, doneNewExpertVote, 1, 2, betweenExpertVoteDateFromDateTo, 1, 2, 3, 4);
        quantityDoneExpertDateFromDateTo = addSGProp("quantityDoneExpertDateFromDateTo", "Кол-во голосов.",
                addJProp(baseLM.and1, addCProp(IntegerClass.instance, 1), doneExpertVoteDateFromDateTo, 1, 2, 3, 4), 1, 3, 4); // в скольки заседаниях поучавствовал
        inExpertVoteDateFromDateTo = addJProp(baseLM.and1, inNewExpertVote, 1, 2, betweenExpertVoteDateFromDateTo, 1, 2, 3, 4);
        quantityInExpertDateFromDateTo = addSGProp("quantityInExpertDateFromDateTo", "Кол-во участ.",
                addJProp(baseLM.and1, addCProp(IntegerClass.instance, 1), inExpertVoteDateFromDateTo, 1, 2, 3, 4), 1, 3, 4); // в скольки заседаниях поучавствовал

        LP expertVoteMonthYear = addJProp(baseLM.and1, addJProp(baseLM.equals2, 3, addJProp(baseLM.monthInDate, dateExpertVote, 1, 2), 1, 2), 1, 2, 3,
                addJProp(baseLM.equals2, 3, addJProp(baseLM.yearInDate, dateExpertVote, 1, 2), 1, 2), 1, 2, 4);
        doneExpertVoteMonthYear = addJProp("doneExpertVoteMonthYear", "Проголосовал в текущем месяце", baseLM.and1, doneNewExpertVote, 1, 2, expertVoteMonthYear, 1, 2, 3, 4);
        quantityDoneExpertMonthYear = addSGProp("quantityDoneExpertMonthYear", "Кол-во голосов.",
                addJProp(baseLM.and1, addCProp(IntegerClass.instance, 1), doneExpertVoteMonthYear, 1, 2, 3, 4), 1, 3, 4); // в скольки заседаниях поучавствовал за месяц

        clusterVote = addDProp(idGroup, "clusterVote", "Кластер (ИД)", cluster, vote);
        nameNativeClusterVote = addJProp(baseGroup, "nameNativeClusterVote", "Кластер", nameNative, clusterVote, 1);
        nameForeignClusterVote = addJProp("nameForeignClusterVote", "Кластер (иностр.)", nameForeign, clusterVote, 1);

        isPrevVoteVote = addJProp("isPrevVoteVote", "Пред.", and(false, false, false), addJProp(baseLM.equals2, projectVote, 1, projectVote, 2), 1, 2,
                addJProp(baseLM.equals2, clusterVote, 1, clusterVote, 2), 1, 2,
                addJProp(baseLM.less2, dateStartVote, 1, dateStartVote, 2), 1, 2,
                incrementVote, 1);
        countPrevVote = addSGProp("countPrevVote", "Кол-во пред. заседания", addJProp(baseLM.and1, addCProp(IntegerClass.instance, 1, vote), 2, isPrevVoteVote, 1, 2), 2);

        clusterInExpertVote = addJProp("clusterInExpertVote", "Вкл", inClusterExpert, clusterVote, 2, 1);

        voteSucceededProjectCluster = addAGProp(idGroup, "voteSucceededProjectCluster", true, "Успешное заседание (ИД)", succeededVote, 1, projectVote, 1, clusterVote, 1);
        voteOpenedSucceededProject = addAGProp(idGroup, "voteOpenedSucceededProject", true, "Успешное открытое заседание (ИД)", openedSucceededVote, 1, projectVote, 1);

        noCurrentVoteProject = addJProp(projectStatusGroup, "noCurrentVoteProject", "Нет текущих заседаний", baseLM.andNot1, is(project), 1, voteInProgressProject, 1); // нету текущих заседаний

        valuedProjectCluster = addJProp(idGroup, "valuedProjectCluster", "Заседание состоялось", closedVote, voteSucceededProjectCluster, 1, 2);
        voteValuedProjectCluster = addJProp(idGroup, "voteValuedProjectCluster", true, "Оцененное заседание (ИД)", baseLM.and1, voteSucceededProjectCluster, 1, 2, valuedProjectCluster, 1, 2);
//        voteValuedProject = addJProp(idGroup, "voteValuedProject", true, "Оцененнное заседание (ИД)", baseLM.and1, voteSucceededProject, 1, noCurrentVoteProject, 1); // нет открытого заседания и есть состояшееся заседания

        voteLastProject = addMGProp(idGroup, "voteLastProject", true, "Посл. заседание (ИД)", object(vote), projectVote, 1);

        acceptedProjectCluster = addJProp(baseGroup, "acceptedProjectCluster", true, "Оценен положительно", acceptedVote, voteValuedProjectCluster, 1, 2);
        rejectedProjectCluster = addJProp(baseGroup, "rejectedProjectCluster", true, "Оценен отрицательно", baseLM.andNot1, voteValuedProjectCluster, 1, 2, acceptedProjectCluster, 1, 2);

        clusterAcceptedProject = addAGProp(idGroup, "clusterAcceptedProject", true, "Кластер (ИД)", acceptedProjectCluster, 2);
        acceptedProject = addJProp(baseGroup, "acceptedProject", true, "Оценен положительно", baseLM.and1, addCProp(LogicalClass.instance, true, project), 1, clusterAcceptedProject, 1);

        currentCluster = addSDProp("currentCluster", "Текущий кластер", cluster);

        nameNativeCurrentCluster = addJProp("nameNativeCurrentCluster", "Кластер", nameNative, currentCluster);

        numberCluster = addDProp(baseGroup, "numberCluster", "Приоритет", IntegerClass.instance, cluster);
        clusterNumber = addAGProp("clusterName", "Кластер (ИД)", numberCluster);

        currentClusterProject = addJProp("currentClusterProject", true, "Рабочий кластер (ИД)", clusterNumber,
                addMGProp(addJProp(and(false, true), numberCluster, 2, inProjectCluster, 1, 2, rejectedProjectCluster, 1, 2), 1), 1);

        firstClusterProject = addJProp("firstClusterProject", true, "Первый кластер (ИД)", clusterNumber,
                addMGProp(addJProp(and(false), numberCluster, 2, inProjectCluster, 1, 2), 1), 1);

        lastClusterProject = addJProp("lastClusterProject", true, "Последний кластер (ИД)", clusterNumber,
                addJProp(baseLM.minusInteger,
                        addMGProp(addJProp(and(false), addJProp(baseLM.minusInteger, numberCluster, 1), 2, inProjectCluster, 1, 2), 1), 1), 1);

        finalClusterProject = addSUProp("finalClusterProject", "Тек. кластер (ИД)", Union.OVERRIDE, lastClusterProject, currentClusterProject, clusterAcceptedProject);
        nameNativeFinalClusterProject = addJProp(projectInformationGroup, "nameNativeFinalClusterProject", "Тек. кластер", nameNative, finalClusterProject, 1);
        nameForeignFinalClusterProject = addJProp(projectInformationGroup, "nameForeignFinalClusterProject", "Тек. кластер (иностр.)", nameForeign, finalClusterProject, 1);
        nameNativeShortFinalClusterProject = addJProp(projectInformationGroup, "nameShortFinalClusterProject", "Тек. кластер (сокр.)", nameNativeShort, finalClusterProject, 1);
        inProjectCurrentCluster = addJProp(baseGroup, "inProjectCurrentCluster", "Вкл", inProjectCluster, 1, currentCluster);

        finalClusterProjectVote = addJProp("finalClusterProjectVote", "Тек. кластер (ИД)", finalClusterProject, projectVote, 1);
        nameNativeFinalClusterProjectVote = addJProp("nameNativeFinalClusterProjectVote", "Тек. кластер", nameNative, finalClusterProjectVote, 1);

        nativeSubstantiationFinalClusterProject = addJProp(projectInformationGroup, "nativeSubstantiationFinalClusterProject", "Обоснование выбора (тек. кластер)", nativeSubstantiationProjectCluster, 1, finalClusterProject, 1);
        foreignSubstantiationFinalClusterProject = addJProp(projectInformationGroup, "foreignSubstantiationFinalClusterProject", "Обоснование выбора (тек. кластер) (иностр.)", foreignSubstantiationProjectCluster, 1, finalClusterProject, 1);

        lastClusterProjectVote = addJProp("lastClusterProjectVote", "Посл. кластер (ИД)", lastClusterProject, projectVote, 1);
        isLastClusterVote = addJProp("isLastClusterVote", "Посл. кластер", baseLM.equals2, lastClusterProjectVote, 1, clusterVote, 1);

        rejectedProject = addJProp("rejectedProject", true, "Оценен отрицательно", baseLM.andNot1, addCProp(LogicalClass.instance, true, project), 1, currentClusterProject, 1);

        valuedProject = addJProp("valuedProject", true, "Оценен", baseLM.and1, addSUProp(Union.OVERRIDE, acceptedProject, rejectedProject), 1, quantityClusterProject, 1);

        needExtraVoteProject = addJProp("needExtraVoteProject", true, "Треб. заседание", and(true, true, true, false),
                is(project), 1,
                notEnoughProject, 1,
                voteInProgressProject, 1,
                clusterAcceptedProject, 1,
                currentClusterProject, 1); // есть открытое заседания и есть состояшееся заседания !!! нужно создать новое заседание

        needExtraVoteRepeatProject = addJProp("needExtraVoteRepeatProject", true, "Треб. заседание (повторно)", baseLM.and1, needExtraVoteProject, 1, quantityVoteProject, 1);

//        clusterVote.setDerivedForcedChange(false, currentClusterProject, 1, is(vote), 1);
//        clusterVote = addDCProp(idGroup, "clusterVote", "Кластер (ИД)", true, currentClusterProject, true, projectVote, 1);

        addConstraint(addJProp("Эксперт не соответствует необходимому кластеру", baseLM.andNot1,
                inExpertVote, 1, 2, clusterInExpertVote, 1, 2), false);
//        addConstraint(addJProp("Эксперт не соответствует необходимому кластеру", baseLM.diff2,
//                clusterExpert, 1, addJProp(baseLM.and1, clusterVote, 2, inExpertVote, 1, 2), 1, 2), false);

//        addConstraint(addJProp("Количество экспертов не соответствует требуемому", baseLM.andNot1, is(vote), 1, addJProp(baseLM.equals2, requiredQuantity,
//                addSGProp(addJProp(baseLM.and1, addCProp(IntegerClass.instance, 1), inExpertVote, 2, 1), 1), 1), 1), false);

        generateDocumentsProjectDocumentType = addAProp(actionGroup, new GenerateDocumentsActionProperty());
        includeDocumentsProject = addAProp(actionGroup, new IncludeDocumentsActionProperty());
        importProjectSidsAction = addAProp(importGroup, new ImportProjectsActionProperty("Импортировать идентификаторы проектов", this, BL, false, false, true));
        showProjectsToImportAction = addAProp(importGroup, new ImportProjectsActionProperty("Посмотреть импортируемые проекты", this, BL, true, false, false));
        showProjectsReplaceToImportAction = addAProp(importGroup, new ImportProjectsActionProperty("Посмотреть замещаемые проекты", this, BL, true, true, false));
        importProjectsAction = addAProp(importGroup, new ImportProjectsActionProperty("Импортировать проекты", this, BL, false, false, false));
        copyProjectAction = addAProp(actionGroup, new CopyProjectActionProperty("Скопировать проект", this, project));
        exportProjectDocumentsAction = addAProp(actionGroup, new ExportProjectDocumentsActionProperty("Экспортировать документы", this, project));
        fillLangProjectAction = addAProp(baseGroup, new FillLangProjectActionProperty(this, project));

        generateVoteProject = addAProp(actionGroup, new GenerateVoteActionProperty());
        copyResultsVote = addAProp(actionGroup, new CopyResultsActionProperty());
        needNameExtraVoteProject = addJProp(and(false, false, false, false), needExtraVoteProject, 1, nameNativeProject, 1, nameForeignProject, 1, nameNativeClaimerProject, 1, nameForeignClaimerProject, 1);
        hideGenerateVoteProject = addHideCaptionProp(privateGroup, "Сгенерировать заседание", generateVoteProject, needNameExtraVoteProject);
//        generateVoteProject.setDerivedForcedChange(addCProp(ActionClass.instance, true), needExtraVoteProject, 1, autoGenerateProject, 1);

        baseLM.generateLoginPassword.setDerivedForcedChange(addCProp(ActionClass.instance, true), is(expert), 1);

        expertLogin = addAGProp(baseGroup, "expertLogin", "Эксперт (ИД)", baseLM.userLogin);
        disableExpert = addDProp(baseGroup, "disableExpert", "Не акт.", LogicalClass.instance, expert);

//        LP userRole = addCUProp("userRole", true, "Роль пользователя", baseLM.customUserSIDMainRole);
        LP userRole = addSUProp("userRole", true, "Роль пользователя", Union.OVERRIDE, baseLM.customUserSIDMainRole, addCProp(StringClass.get(30), "expert", expert));

//        voteValuedProject, 1, addIfElseUProp(addCProp(projectStatus, "accepted", project), addCProp(projectStatus, "rejected", project), acceptedProject, 1), 1,
//        voteSucceededProject, 1, addCProp(projectStatus, "succeeded", project), 1,
//        voteInProgressProject, 1, addCProp(projectStatus, "inProgress", project), 1,

        isConsultingCenterQuestionProject = addDProp(consultingCenterGroup, "isConsultingCenterQuestionProject", "Был ли задан вопрос о консультативном центре", LogicalClass.instance, project);
        isConsultingCenterCommentProject = addDProp(consultingCenterGroup, "isConsultingCenterCommentProject", "Утвердительный ответ", LogicalClass.instance, project);
        consultingCenterCommentProject = addDProp(consultingCenterGroup, "consultingCenterCommentProject", "Комментарий о консультативном центре", InsensitiveStringClass.get(2000), project);
        consultingCenterCommentProject.setMinimumWidth(50);
        consultingCenterCommentProject.setPreferredWidth(50);

        //quantitySupplierArticleBetweenDates = addSGProp("quantitySupplierArticleBetweenDates", "Проданное кол-во",
        //        addJProp(and(false,false), innerQuantity, 1, 2, 3, is(orderSale), 1, betweenDate2, 1, 4, 5), contragentOrder, 3, 2, 4, 5);

        betweenDate = addJProp(baseLM.between, baseLM.date, 1, object(DateClass.instance), 2, object(DateClass.instance), 3);

        sumPositiveConsultingCenterCommentProject = addSGProp(consultingCenterStatGroup, "sumPositiveConsultingCenterCommentProject", "Сумма положительных комментариев",
                addJProp(and(false, false, false, true), addCProp(IntegerClass.instance, 1), isConsultingCenterQuestionProject, 1, isConsultingCenterCommentProject, 1, betweenDate, 1, 2, 3, inactiveProject, 1),
                2, 3);
        sumNegativeConsultingCenterCommentProject = addSGProp(consultingCenterStatGroup, "sumNegativeConsultingCenterCommentProject", "Сумма отрицательных комментариев",
                addJProp(and(false, true, false, true), addCProp(IntegerClass.instance, 1), isConsultingCenterQuestionProject, 1, isConsultingCenterCommentProject, 1, betweenDate, 1, 2, 3, inactiveProject, 1),
                2, 3);
        sumTotalConsultingCenterCommentProject = addSGProp(consultingCenterStatGroup, "sumTotalConsultingCenterCommentProject", "Всего комментариев",
                addJProp(and(false, false, true), addCProp(IntegerClass.instance, 1), isConsultingCenterQuestionProject, 1, betweenDate, 1, 2, 3, inactiveProject, 1),
                2, 3);


        fillNativeProject = addDProp(projectOptionsGroup, "fillNativeProject", "Анкета на русском", LogicalClass.instance, project);
        fillForeignProject = addDProp(projectOptionsGroup, "fillForeignProject", "Анкета на английском", LogicalClass.instance, project);
        langProject = addDProp(projectOptionsGroup, "langProject", "Язык", InsensitiveStringClass.get(10), project);

        translatedToRussianProject = addDProp(projectTranslationsGroup, "translatedToRussianProject", "Переведено на русский", LogicalClass.instance, project);
        translatedToEnglishProject = addDProp(projectTranslationsGroup, "translatedToEnglishProject", "Переведено на английский", LogicalClass.instance, project);

        needsToBeTranslatedToRussianProject = addJProp(and(true, true), addCProp(LogicalClass.instance, true, project), 1, fillNativeProject, 1, translatedToRussianProject, 1);
        needsToBeTranslatedToEnglishProject = addJProp(and(true, true), addCProp(LogicalClass.instance, true, project), 1, fillForeignProject, 1, translatedToEnglishProject, 1);

        hideTranslatedToRussianProject = addHideCaptionProp(privateGroup, "Переведено", translatedToRussianProject, fillForeignProject);
        hideTranslatedToEnglishProject = addHideCaptionProp(privateGroup, "Переведено", translatedToEnglishProject, fillNativeProject);

        needTranslationProject = addSUProp("needTranslationProject", true, "Требуется перевод", Union.OVERRIDE, needsToBeTranslatedToRussianProject, needsToBeTranslatedToEnglishProject);

        dateJoinProject = addJProp(baseLM.and1, baseLM.date, 1, is(project), 1);
        dateDataProject = addDProp("dateDataProject", "Дата", DateClass.instance, project);
        dateProject = addSUProp("dateProject", "Дата проекта", Union.OVERRIDE, dateJoinProject, dateDataProject);
        updateDateProject = addDProp(projectInformationGroup, "updateDateProject", "Дата изменения проекта", DateTimeClass.instance, project);
        statusDateProject = addDProp(baseGroup, "statusDateProject", "Дата подачи на статус участника", DateClass.instance, project);
        dateStatusDataProject = addDProp("dateStatusDataProject", "Дата", DateClass.instance, project);
        dateStatusProject = addSUProp(projectInformationGroup, "dateStatusProject", "Дата подачи на статус участника", Union.OVERRIDE, statusDateProject, dateStatusDataProject);

        //формальная экспертиза и юридическая проверка

        projectFormalControl = addDProp("projectFormalControl", "Проект (ИД)", project, formalControl);
        resultFormalControl = addDProp("resultFormalControl", "Решение формальной экспертизы", formalControlResult, formalControl);

        addNotEnoughDocumentsFCResult = addJProp(formalControlResultGroup, true, "addNotEnoughDocumentsFCResult", "Неполный перечень документов", addAAProp(formalControl, resultFormalControl), addCProp(formalControlResult, "notEnoughDocuments"));
        addNoListOfExpertsFCResult = addJProp(formalControlResultGroup, true, "Отсутствует перечень экспертов", addAAProp(formalControl, resultFormalControl), addCProp(formalControlResult, "noListOfExperts"));
        addNotSuitableClusterFCResult = addJProp(formalControlResultGroup, true, "Не соответствует направлению", addAAProp(formalControl, resultFormalControl), addCProp(formalControlResult, "notSuitableCluster"));
        addRepeatedFCResult = addJProp(formalControlResultGroup, true, "Подана повторно", addAAProp(formalControl, resultFormalControl), addCProp(formalControlResult, "repeatedFC"));
        addPositiveFCResult = addJProp(formalControlResultGroup, true, "Прошла формальную экспертизу", addAAProp(formalControl, resultFormalControl), addCProp(formalControlResult, "positiveFormalResult"));

        nameResultFormalControl = addJProp("nameResultFormalControl", "Решение формальной экспертизы", baseLM.name, resultFormalControl, 1);
        commentFormalControl = addDProp("commentFormalControl", "Комментарий", TextClass.instance, formalControl);
        // notAvailableForStatusProjectStatus = addDProp(baseGroup, "notAvailableForStatusProjectStatus", "Не действует для заявки на статус", LogicalClass.instance, projectStatus);
        // notAvailableForPreliminaryProjectStatus = addDProp(baseGroup, "notAvailableForPreliminaryProjectStatus", "Не действует для заявки на предварительную экспертизу", LogicalClass.instance, projectStatus);

        dateFormalControl = addTCProp(Time.DATETIME, "dateFormalControl", true, "Дата экспертизы", resultFormalControl);
        emailDateFormalControl = addJProp("emailDateFormalControl", "Дата оправки уведомления", baseLM.dateInTime, dateFormalControl, 1);
        overdueDateFormalControl = addJProp("overdueDateFormalControl", "Дата просрочки формальной экспертизы", baseLM.addDate2, emailDateFormalControl, 1, overduePeriod);

        projectLegalCheck = addDProp("projectLegalCheck", "Проект (ИД)", project, legalCheck);
        resultLegalCheck = addDProp("resultLegalCheck", "Решение юридической проверки", legalCheckResult, legalCheck);
        addNegativeLCResult = addJProp(legalCheckResultGroup, true, "Не прошла юридическую проверку", addAAProp(legalCheck, resultLegalCheck), addCProp(legalCheckResult, "negativeLegalCheckResult"));
        addPositiveLCResult = addJProp(legalCheckResultGroup, true, "Прошла юридическую проверку", addAAProp(legalCheck, resultLegalCheck), addCProp(legalCheckResult, "positiveLegalCheckResult"));
        commentLegalCheck = addDProp("commentLegalCheck", "Комментарий", TextClass.instance, legalCheck);
        nameResultLegalCheck = addJProp("nameResultLegalCheck", "Решение юридической проверки", baseLM.name, resultLegalCheck, 1);

        dateLegalCheck = addTCProp(Time.DATETIME, "dateLegalCheck", true, "Дата проверки", resultLegalCheck);
        emailDateLegalCheck = addJProp("emailDateLegalCheck", "Дата оправки уведомления", baseLM.dateInTime, dateLegalCheck, 1);
        overdueDateLegalCheck = addJProp("overdueDateLegalCheck", "Дата просрочки юридической проверки", baseLM.addDate2, emailDateLegalCheck, 1, overduePeriod);

        maxFormalControlProjectProps = addMGProp((AbstractGroup) null, new String[]{"maxDateFormalControlProject", "currentFCProject"}, new String[]{"Дата посл. формальной экспертизы.", "Посл. формальная экспертиза"}, 1,
                dateFormalControl, 1, projectFormalControl, 1);
        LP currentDateFormalControlProject = maxFormalControlProjectProps[0];
        currentFormalControlProject = maxFormalControlProjectProps[1];

        executeFormalControlProject = addJProp("executeFormalControlProject", "Действующая", baseLM.and1, currentFormalControlProject, 1, addJProp(baseLM.greater2, addJProp(dateFormalControl, currentFormalControlProject, 1), 1, updateDateProject, 1), 1);

        resultExecuteFormalControlProject = addJProp("resultExecuteFormalControlProject", true, "Решение", resultFormalControl, executeFormalControlProject, 1);
        nameResultExecuteFormalControlProject = addJProp("nameResultExecuteFormalControlProject", "Решение", baseLM.name, resultExecuteFormalControlProject, 1);

        notEnoughDocumentsProject = addJProp("notEnoughDocumentsProject", true, "Неполный перечень документов", baseLM.equals2, resultExecuteFormalControlProject, 1, addCProp(formalControlResult, "notEnoughDocuments"));
        isPreliminaryNotEnoughDocumentProject = addJProp("isPreliminaryNotEnoughDocumentProject", true, "Неполный перечень документов (на экспертизу)", baseLM.and1, notEnoughDocumentsProject, 1, isPreliminaryProject, 1);
        isStatusNotEnoughDocumentProject = addJProp("isStatusNotEnoughDocumentProject", true, "Неполный перечень документов (на статус)", baseLM.and1, notEnoughDocumentsProject, 1, isStatusProject, 1);
        noListOfExpertsProject = addJProp("noListOfExpertsProject", true, "Отсутствует перечень экспертов", baseLM.equals2, resultExecuteFormalControlProject, 1, addCProp(formalControlResult, "noListOfExperts"));
        notSuitableClusterProject = addJProp("notSuitableClusterProject", true, "Не соответствует направлению", baseLM.equals2, resultExecuteFormalControlProject, 1, addCProp(formalControlResult, "notSuitableCluster"));
        repeatedProject = addJProp("repeatedProject", true, "Подана повторно", baseLM.equals2, resultExecuteFormalControlProject, 1, addCProp(formalControlResult, "repeatedFC"));
        positiveFormalResultProject = addJProp("positiveFormalResultProject", true, "Прошла формальную экспертизу", baseLM.equals2, resultExecuteFormalControlProject, 1, addCProp(formalControlResult, "positiveFormalResult"));

        projectActionFormalControl = addDProp(idGroup, "projectActionFormalControl", "Тип заявки (ИД)", projectAction, formalControl);
        nameProjectActionFormalControl = addJProp(baseGroup, "nameProjectActionFormalControl", "Тип заявки", baseLM.name, projectActionFormalControl, 1);
        nameProjectActionFormalControl.setPreferredCharWidth(20);
        projectActionFormalControl.setDerivedChange(true, addJProp(projectActionProject, projectFormalControl, 1), 1, is(formalControl), 1);

        overdueFormalControlProject = addJProp("overdueFormalControlProject", true, "Просрочена формальная экспертиза", baseLM.and1,
                addJProp(baseLM.greater2, baseLM.currentDate, addJProp(overdueDateFormalControl, executeFormalControlProject, 1), 1), 1,
                addJProp(baseLM.and1, notEnoughDocumentsProject, 1, addJProp(baseLM.equals2, addJProp(projectActionFormalControl, executeFormalControlProject, 1), 1, addCProp(projectAction, "status")), 1), 1);

        maxLegalCheckProjectProps = addMGProp((AbstractGroup) null, new String[]{"maxDateLegalCheckProject", "currentLCProject"}, new String[]{"Дата посл. юр. проверки", "Посл. юр. проверка"}, 1,
                dateLegalCheck, 1, projectLegalCheck, 1);
        LP currentDateLegalCheckProject = maxLegalCheckProjectProps[0];
        currentLegalCheckProject = maxLegalCheckProjectProps[1];

        executeLegalCheckProject = addJProp("executeLegalCheckProject", "Действующая", baseLM.and1, currentLegalCheckProject, 1, addJProp(baseLM.greater2, addJProp(dateLegalCheck, currentLegalCheckProject, 1), 1, updateDateProject, 1), 1);

        resultExecuteLegalCheckProject = addJProp("resultExecuteLegalCheckProject", true, "Решение", resultLegalCheck, executeLegalCheckProject, 1);
        negativeLegalResultProject = addJProp("negativeLegalResultProject", true, "Не прошла юридическую проверку", baseLM.equals2, resultExecuteLegalCheckProject, 1, addCProp(legalCheckResult, "negativeLegalCheckResult"));
        positiveLegalResultProject = addJProp("positiveLegalResultProject", true, "Прошла юридическую проверку", baseLM.equals2, resultExecuteLegalCheckProject, 1, addCProp(legalCheckResult, "positiveLegalCheckResult"));

        projectActionLegalCheck = addDProp(idGroup, "projectActionLegalCheck", "Тип заявки (ИД)", projectAction, legalCheck);
        nameProjectActionLegalCheck = addJProp(baseGroup, "nameProjectActionLegalCheck", "Тип заявки", baseLM.name, projectActionLegalCheck, 1);
        nameProjectActionLegalCheck.setPreferredCharWidth(20);
        projectActionLegalCheck.setDerivedChange(true, addJProp(projectActionProject, projectLegalCheck, 1), 1, is(legalCheck), 1);

        overdueLegalCheckProject = addJProp("overdueLegalCheckProject", true, "Просрочена юридическая проверка", baseLM.and1,
                addJProp(baseLM.greater2, baseLM.currentDate, addJProp(overdueDateLegalCheck, executeLegalCheckProject, 1), 1), 1,
                addJProp(baseLM.and1, negativeLegalResultProject, 1, addJProp(baseLM.equals2, addJProp(projectActionLegalCheck, executeLegalCheckProject, 1), 1, addCProp(projectAction, "status")), 1), 1);

        sentForTranslationProject = addDProp("sentForTranslationProject", "Направлена на перевод", LogicalClass.instance, project);
        oficialNameProjectStatus = addDProp(baseGroup, "oficialNameProjectStatus", "Наименование из регламента", StringClass.get(200), projectStatus);
        oficialNameProjectStatus.setMinimumWidth(10);
        oficialNameProjectStatus.setPreferredWidth(50);
        numberProjectStatus = addDProp(baseGroup, "numberProjectStatus", "Номер", StringClass.get(10), projectStatus);

        fileDecisionVote = addDProp("fileDecisionVote", "Решение по проекту", PDFClass.instance, vote);
        loadFileDecisionVote = addJProp(actionGroup, true, "Загрузить решение", baseLM.and1, addLFAProp(fileDecisionVote), 1, closedSucceededVote, 1);
        openFileDecisionVote = addJProp(actionGroup, true, "Открыть решение", baseLM.and1, addOFAProp(fileDecisionVote), 1, closedSucceededVote, 1);

        fileDecisionProject = addJProp("fileDecisionProject", "Решение по проекту", fileDecisionVote, voteLastProject, 1);

        acceptedDecisionProject = addJProp("acceptedDecisionProject", true, "Есть решение о соответствии", baseLM.and1, acceptedProject, 1, fileDecisionProject, 1);
        rejectedDecisionProject = addJProp("rejectedDecisionProject", true, "Есть решение о несоответствии", baseLM.and1, rejectedProject, 1, fileDecisionProject, 1);

        decisionNoticedVote = addDProp("decisionNoticedVote", "Отослано уведомление", LogicalClass.instance, vote);
        decisionNoticedProject = addJProp("decisionNoticedProject", "Отослано уведомление", decisionNoticedVote, voteLastProject, 1);

        acceptedNoticedProject = addJProp("acceptedNoticedProject", true, "Отослано уведомление о соответствии", baseLM.and1, acceptedProject, 1, decisionNoticedProject, 1);
        rejectedNoticedProject = addJProp("rejectedNoticedProject", true, "Отослано уведомление о несоответствии", baseLM.and1, rejectedProject, 1, decisionNoticedProject, 1);

        acceptedNoticedStatusProject = addJProp("acceptedNoticedStatusProject", true, "Отослано уведомление о соответствии (предв. экспертиза)", baseLM.and1, acceptedNoticedProject, 1, isStatusProject, 1);
        acceptedNoticedPreliminaryProject = addJProp("acceptedNoticedPreliminaryProject", true, "Отослано уведомление о соответствии (статус)", baseLM.andNot1, acceptedNoticedProject, 1, isStatusProject, 1);

        projectOriginalDocsCheck = addDProp("projectOriginalDocsCheck", "Проект (ИД)", project, originalDocsCheck);
        resultOriginalDocsCheck = addDProp("resultOriginalDocsCheck", "Проверка оригиналов документов", originalDocsCheckResult, originalDocsCheck);
        dateOriginalDocsCheck = addTCProp(Time.DATETIME, "dateOriginalDocsCheck", true, "Дата подачи документов", resultOriginalDocsCheck);
        commentOriginalDocsCheck = addDProp("commentOriginalDocsCheck", "Комментарий", TextClass.instance, originalDocsCheck);
        nameResultOriginalDocsCheck = addJProp("nameResultOriginalDocsCheck", "Проверка оригиналов документов", baseLM.name, resultOriginalDocsCheck, 1);

        emailDateOriginalDocsCheck = addJProp("emailDateOriginalDocsCheck", "Дата оправки уведомления", baseLM.dateInTime, dateOriginalDocsCheck, 1);
        overdueDateOriginalDocsCheck = addJProp("overdueDateOriginalDocsCheck", "Дата просрочки формальной экспертизы", baseLM.addDate2, emailDateOriginalDocsCheck, 1, overduePeriod);

        maxOriginalDocsCheckProjectProps = addMGProp((AbstractGroup) null, new String[]{"maxDateOriginalDocsCheckProject", "currentOCProject"}, new String[]{"Дата посл. проверки документов", "Посл. проверка документов"}, 1,
                dateOriginalDocsCheck, 1, projectOriginalDocsCheck, 1);
        LP currentDateOriginalDocsCheckProject = maxOriginalDocsCheckProjectProps[0];
        executeOriginalDocsCheckProject = maxOriginalDocsCheckProjectProps[1];

        resultExecuteOriginalDocsCheckProject = addJProp("resultExecuteOriginalDocsCheckProject", true, "Проверка", resultOriginalDocsCheck, executeOriginalDocsCheckProject, 1);
        addNegativeODCResult = addJProp(originalDoscCheckGroup, true, "Подан неполный пакет документов", addAAProp(originalDocsCheck, resultOriginalDocsCheck), addCProp(originalDocsCheckResult, "notCompleteOriginalDocsPacket"));
        addPositiveODCResult = addJProp(originalDoscCheckGroup, true, "Подан полный пакет документов", addAAProp(originalDocsCheck, resultOriginalDocsCheck), addCProp(originalDocsCheckResult, "completeOriginalDocsPacket"));
        negativeOriginalDocsCheckProject = addJProp("negativeOriginalDocsCheckProject", true, "Не полный пакет документов", baseLM.equals2, resultExecuteOriginalDocsCheckProject, 1, addCProp(originalDocsCheckResult, "notCompleteOriginalDocsPacket"));
        positiveOriginalDocsCheckProject = addJProp("positiveOriginalDocsCheckProject", true, "Полный пакет документов", baseLM.equals2, resultExecuteOriginalDocsCheckProject, 1, addCProp(originalDocsCheckResult, "completeOriginalDocsPacket"));
        overdueOriginalDocsCheckProject = addJProp("overdueOriginalDocsCheckProject", true, "Пакет оригиналов документов не пополнен в срок", baseLM.greater2, baseLM.currentDate, addJProp(overdueDateOriginalDocsCheck, executeOriginalDocsCheckProject, 1), 1);

        sentForSignatureProject = addDProp(registerGroup, "sentForSignatureProject", "Решение передано на подпись", LogicalClass.instance, project);
        dateSentForSignatureProject = addDProp(registerGroup, "dateSentForSignatureProject", "Дата передачи на подпись", DateClass.instance, project);
        signedProject = addDProp(registerGroup, "signedProject", "Решение подписано", LogicalClass.instance, project);
        dateSignedProject = addDProp(registerGroup, "dateSignedProject", "Дата подписания", DateClass.instance, project);
        sentToFinDepProject = addDProp(registerGroup, "sentToFinDepProject", "Документы переданы в Финансовый департамент", LogicalClass.instance, project);
        dateSentToFinDepProject = addDProp(registerGroup, "dateSentToFinDepProject", "Дата передачи в Финансовый департамент", DateClass.instance, project);
        submittedToRegisterProject = addDProp(registerGroup, "submittedToRegisterProject", "Внесен в реестр участников", LogicalClass.instance, project);
        dateSubmittedToRegisterProject = addDProp(registerGroup, "dateSubmittedToRegisterProject", "Дата внесения в реестр участников", DateClass.instance, project);
        preparedCertificateProject = addDProp(registerGroup, "preparedCertificateProject", "Подготовлено свидетельство участника", LogicalClass.instance, project);
        datePreparedCertificateProject = addDProp(registerGroup, "datePreparedCertificateProject", "Дата подготовки свидетельства участника", DateClass.instance, project);
        certifiedProject = addDProp(registerGroup, "certifiedProject", "Выдано свидетельство участника", LogicalClass.instance, project);
        dateCertifiedProject = addDProp(registerGroup, "dateCertifiedProject", "Дата выдачи свидетельства участника", DateClass.instance, project);

        certifiedStatusProject = addCaseUProp(idGroup, "certifiedStatusProject", true, "Статус (оформлен) (ИД)",
                certifiedProject, 1, addCProp(projectStatus, "certified", project), 1,
                preparedCertificateProject, 1, addCProp(projectStatus, "preparedCertificate", project), 1,
                submittedToRegisterProject, 1, addCProp(projectStatus, "submittedToRegister", project), 1,
                sentToFinDepProject, 1, addCProp(projectStatus, "sentToFinDep", project), 1,
                signedProject, 1, addCProp(projectStatus, "signed", project), 1,
                sentForSignatureProject, 1, addCProp(projectStatus, "sentForSignature", project), 1,
                positiveOriginalDocsCheckProject, 1, addCProp(projectStatus, "appliedOriginalDocs", project), 1,
                overdueOriginalDocsCheckProject, 1, addCProp(projectStatus, "overdueOriginalDocs", project), 1,
                negativeOriginalDocsCheckProject, 1, addCProp(projectStatus, "notEnoughOriginalDocs", project), 1,
                addCProp(projectStatus, "sentStatusAccepted", project), 1);

        valuedStatusProject = addCaseUProp(idGroup, "valuedStatusProject", true, "Статус (оценен) (ИД)",
                acceptedNoticedStatusProject, 1, addCProp(projectStatus, "sentStatusAccepted", project), 1,
                acceptedNoticedPreliminaryProject, 1, addCProp(projectStatus, "sentPreliminaryAccepted", project), 1,
                rejectedNoticedProject, 1, addCProp(projectStatus, "sentRejected", project), 1,
                acceptedDecisionProject, 1, addCProp(projectStatus, "accepted", project), 1,
                rejectedDecisionProject, 1, addCProp(projectStatus, "rejected", project), 1,
                addCProp(projectStatus, "issuedVoteDocs", project), 1);

        legalCheckStatusProject = addCaseUProp(idGroup, "legalCheckStatusProject", true, "Статус (юридич. пров.) (ИД)",
                positiveLegalResultProject, 1, addCProp(projectStatus, "positiveLCResult", project), 1,
                overdueLegalCheckProject, 1, addCProp(projectStatus, "overdueLC", project), 1,
                negativeLegalResultProject, 1, addCProp(projectStatus, "negativeLCResult", project), 1,
                addCProp(projectStatus, "unknown", project), 1);

        formalCheckStatusProject = addCaseUProp(idGroup, "formalCheckStatusProject", true, "Статус (предв. эксп.) (ИД)",
                overdueFormalControlProject, 1, addCProp(projectStatus, "overdueFC", project), 1,
                isPreliminaryNotEnoughDocumentProject, 1, addCProp(projectStatus, "notEnoughDocsForPreliminary", project), 1,
                isStatusNotEnoughDocumentProject, 1, addCProp(projectStatus, "notEnoughDocsForStatus", project), 1,
                noListOfExpertsProject, 1, addCProp(projectStatus, "noExperts", project), 1,
                notSuitableClusterProject, 1, addCProp(projectStatus, "noCluster", project), 1,
                repeatedProject, 1, addCProp(projectStatus, "repeated", project), 1,
                positiveFormalResultProject, 1, addCProp(projectStatus, "positiveFCResult", project), 1,
                addCProp(projectStatus, "unknown", project), 1);

        statusProject = addCaseUProp(idGroup, "statusProject", true, "Статус (ИД)",
                acceptedNoticedStatusProject, 1, certifiedStatusProject, 1,
                valuedProject, 1, valuedStatusProject, 1,
                voteInProgressRepeatProject, 1, addCProp(projectStatus, "inProgressRepeat", project), 1,
                voteInProgressProject, 1, addCProp(projectStatus, "inProgress", project), 1,
                needExtraVoteRepeatProject, 1, addCProp(projectStatus, "needExtraVote", project), 1,
                sentForTranslationProject, 1, addCProp(projectStatus, "needTranslation", project), 1,
                resultExecuteLegalCheckProject, 1, legalCheckStatusProject, 1,
                resultExecuteFormalControlProject, 1, formalCheckStatusProject, 1,
                addCProp(projectStatus, "registered", project), 1);

        statusDataProject = addDProp("statusDataProject", "Статус", projectStatus, project);
        projectStatusProject = addSUProp(idGroup, "projectStatusProject", true, "Статус", Union.OVERRIDE, statusProject, statusDataProject);
        // пока логи не работают, поскольку лог записывается каждый раз без проверки на изменилось ли на самом деле или нет
//        logStatusProject = addLProp(projectStatusProject);
//        logNameStatusProject = addJProp(baseGroup, "logNameStatusProject", "Статус", baseLM.name, logStatusProject, 1, 2);
//
        nameStatusProject = addJProp(projectInformationGroup, "nameStatusProject", "Статус", baseLM.name, projectStatusProject, 1);

        dateProjectVote = addJProp("dateProjectVote", "Дата проекта", dateProject, projectVote, 1);
        statusProjectVote = addJProp(idGroup, "statusProjectVote", "Статус проекта (ИД)", projectStatusProject, projectVote, 1);
        nameStatusProjectVote = addJProp(baseGroup, "nameStatusProjectVote", "Статус проекта", baseLM.name, statusProjectVote, 1);

//        сейчас в сколково реально есть заявители с двумя успешными проектами
//        projectSucceededClaimer = addAGProp(idGroup, "projectSucceededClaimer", true, "Успешный проект (ИД)", acceptedProject, 1, claimerProject, 1);


        // статистика по экспертам
        quantityTotalExpert = addSGProp(expertResultGroup, "quantityTotalExpert", "Всего заседаний",
                addJProp(baseLM.and1, addCProp(IntegerClass.instance, 1), inNewExpertVote, 1, 2), 1);

        quantityDoneExpert = addSGProp(expertResultGroup, "quantityDoneExpert", "Проголосовал",
                addJProp(baseLM.and1, addCProp(IntegerClass.instance, 1), doneNewExpertVote, 1, 2), 1);
        percentDoneExpert = addJProp(expertResultGroup, "percentDoneExpert", "Проголосовал (%)", percent, quantityDoneExpert, 1, quantityTotalExpert, 1);

        LP quantityInClusterExpert = addSGProp("quantityInClusterExpert", "Соотв-ие кластеру (голоса)",
                addJProp(baseLM.and1, addCProp(IntegerClass.instance, 1), inClusterNewExpertVote, 1, 2), 1);
        percentInClusterExpert = addJProp(expertResultGroup, "percentInClusterExpert", "Соотв-ие кластеру (%)", percent, quantityInClusterExpert, 1, quantityDoneExpert, 1);

        LP quantityInnovativeExpert = addSGProp("quantityInnovativeExpert", "Инновац. (голоса)",
                addJProp(baseLM.and1, addCProp(IntegerClass.instance, 1), innovativeNewExpertVote, 1, 2), 1);
        percentInnovativeExpert = addJProp(expertResultGroup, "percentInnovativeExpert", "Инновац. (%)", percent, quantityInnovativeExpert, 1, quantityDoneExpert, 1);

        LP quantityForeignExpert = addSGProp("quantityForeignExpert", "Иностр. специалист (голоса)",
                addJProp(baseLM.and1, addCProp(IntegerClass.instance, 1), foreignNewExpertVote, 1, 2), 1);
        percentForeignExpert = addJProp(expertResultGroup, "percentForeignExpert", "Иностр. специалист (%)", percent, quantityForeignExpert, 1, quantityDoneExpert, 1);

        prevDateStartVote = addOProp("prevDateStartVote", "Пред. засед. (старт)", OrderType.PREVIOUS, dateStartVote, true, true, 2, projectVote, 1, clusterVote, 1, baseLM.date, 1);
        prevDateVote = addOProp("prevDateVote", "Пред. засед. (окончание)", OrderType.PREVIOUS, dateEndVote, true, true, 2, projectVote, 1, clusterVote, 1, baseLM.date, 1);

        prevClusterVote = addOProp(idGroup, "prevClusterVote", "Пред. кластер. (ИД)", OrderType.PREVIOUS, clusterVote, true, true, 1, projectVote, 1, baseLM.date, 1);
        nameNativePrevClusterVote = addJProp("nameNativePrevClusterVote", "Пред. кластер", nameNative, prevClusterVote, 1);

        numberNewExpertVote = addOProp("numberNewExpertVote", "Номер (нов.)", OrderType.SUM, addJProp(baseLM.and1, addCProp(IntegerClass.instance, 1), inNewExpertVote, 1, 2), true, true, 1, 2, 1);
        numberOldExpertVote = addOProp("numberOldExpertVote", "Номер (стар.)", OrderType.SUM, addJProp(baseLM.and1, addCProp(IntegerClass.instance, 1), inOldExpertVote, 1, 2), true, true, 1, 2, 1);

        emailDocuments = addDProp(baseGroup, "emailDocuments", "E-mail для документов", StringClass.get(50));

        emailLetterExpertVoteEA = addEAProp(expert, vote);
        addEARecepient(emailLetterExpertVoteEA, baseLM.email, 1);

        emailLetterExpertVote = addJProp(baseGroup, true, "emailLetterExpertVote", "Письмо о заседании (e-mail)",
                emailLetterExpertVoteEA, 1, 2, addJProp(letterExpertSubjectLanguage, languageExpert, 1), 1);
        emailLetterExpertVote.setImage("email.png");
        emailLetterExpertVote.property.askConfirm = true;
        emailLetterExpertVote.setDerivedForcedChange(addCProp(ActionClass.instance, true), inNewExpertVote, 1, 2);

        allowedEmailLetterExpertVote = addJProp(baseGroup, "Письмо о заседании (e-mail)", "Письмо о заседании", baseLM.andNot1, emailLetterExpertVote, 1, 2, voteResultExpertVote, 1, 2);
        allowedEmailLetterExpertVote.property.askConfirm = true;

        emailClaimerFromAddress = addDProp("emailClaimerFromAddress", "Адрес отправителя (для заявителей)", StringClass.get(50));
        emailClaimerVoteEA = addEAProp(emailClaimerFromAddress, emailClaimerFromAddress, vote);

        claimerEmailVote = addJProp("claimerEmailVote", "E-mail (заявителя)", baseLM.email, claimerVote, 1);
        addEARecepient(emailClaimerVoteEA, claimerEmailVote, 1);

        emailClaimerHeaderVote = addJProp("emailClaimerHeaderVote", "Заголовок уведомления заявителю", baseLM.string2, addCProp(StringClass.get(2000), "Уведомление."), nameNativeClaimerVote, 1);
        emailClaimerVote = addJProp(actionGroup, true, "emailClaimerVote", "Письмо о рассмотрении", emailClaimerVoteEA, 1, emailClaimerHeaderVote, 1);
        emailClaimerVote.property.askConfirm = true;

        emailClaimerVote.setDerivedForcedChange(addCProp(ActionClass.instance, true), openedVote, 1);

        emailNoticeRejectedVoteEA = addEAProp(emailClaimerFromAddress, emailClaimerFromAddress, vote);
        addEARecepient(emailNoticeRejectedVoteEA, claimerEmailVote, 1);

        emailNoticeRejectedVote = addJProp(actionGroup, true, "emailNoticeRejectedVote", "Письмо о несоответствии", baseLM.and1,
                addEPAProp(EPA_DEFAULT, addJProp(true, emailNoticeRejectedVoteEA, 1, emailClaimerHeaderVote, 1), decisionNoticedVote), 1,
                closedRejectedVote, 1);
        emailNoticeRejectedVote.setImage("email.png");
        emailNoticeRejectedVote.property.askConfirm = true;

        emailNoticeAcceptedStatusVoteEA = addEAProp(emailClaimerFromAddress, emailClaimerFromAddress, vote);
        addEARecepient(emailNoticeAcceptedStatusVoteEA, claimerEmailVote, 1);

        emailNoticeAcceptedStatusVote = addJProp(actionGroup, true, "emailNoticeAcceptedStatusVote", "Письмо о соответствии (статус участника)", baseLM.and1,
                addEPAProp(EPA_DEFAULT, addJProp(true, emailNoticeAcceptedStatusVoteEA, 1, emailClaimerHeaderVote, 1), decisionNoticedVote), 1,
                closedAcceptedStatusVote, 1);
        emailNoticeAcceptedStatusVote.setImage("email.png");
        emailNoticeAcceptedStatusVote.property.askConfirm = true;

        emailNoticeAcceptedPreliminaryVoteEA = addEAProp(emailClaimerFromAddress, emailClaimerFromAddress, vote);
        addEARecepient(emailNoticeAcceptedPreliminaryVoteEA, claimerEmailVote, 1);

//        emailNoticeAcceptedPreliminaryVote = addJProp(actionGroup, true, "emailNoticeAcceptedPreliminaryVote", "Письмо о соответствии (предварительная экспертиза)", and(false, false), addJProp(emailNoticeAcceptedPreliminaryVoteEA, 1, emailClaimerHeaderVote, 1), 1,);

        emailNoticeAcceptedPreliminaryVote = addJProp(actionGroup, true, "emailNoticeAcceptedPreliminaryVote", "Письмо о соответствии (предварительная экспертиза)", and(false, false),
                addEPAProp(EPA_DEFAULT, addJProp(true, emailNoticeAcceptedPreliminaryVoteEA, 1, emailClaimerHeaderVote, 1), decisionNoticedVote), 1,
                closedAcceptedPreliminaryVote, 1, fileDecisionVote, 1);
        emailNoticeAcceptedPreliminaryVote.setImage("email.png");
        emailNoticeAcceptedPreliminaryVote.property.askConfirm = true;

        emailStartVoteEA = addEAProp(vote);
        addEARecepient(emailStartVoteEA, emailDocuments);

        emailClaimerNameVote = addJProp(addSFProp("(CAST(prm1 as text))||(CAST(prm2 as text))", StringClass.get(2000), 2), object(StringClass.get(2000)), 2, nameNativeClaimerVote, 1);

        emailStartHeaderVote = addJProp("emailStartHeaderVote", "Заголовок созыва заседания", emailClaimerNameVote, 1, addCProp(StringClass.get(2000), "Созыв заседания - "));
        emailStartVote = addJProp(baseGroup, true, "emailStartVote", "Созыв заседания (e-mail)", emailStartVoteEA, 1, emailStartHeaderVote, 1);
        emailStartVote.property.askConfirm = true;
//        emailStartVote.setDerivedForcedChange(addCProp(ActionClass.instance, true), openedVote, 1);

        emailProtocolVoteEA = addEAProp(vote);
        addEARecepient(emailProtocolVoteEA, emailDocuments);

        emailProtocolHeaderVote = addJProp("emailProtocolHeaderVote", "Заголовок протокола заседания", emailClaimerNameVote, 1, addCProp(StringClass.get(2000), "Протокол заседания - "));
        emailProtocolVote = addJProp(baseGroup, true, "emailProtocolVote", "Протокол заседания (e-mail)", emailProtocolVoteEA, 1, emailProtocolHeaderVote, 1);
        emailProtocolVote.property.askConfirm = true;
//        emailProtocolVote.setDerivedForcedChange(addCProp(ActionClass.instance, true), closedVote, 1);

        emailClosedVoteEA = addEAProp(vote);
        addEARecepient(emailClosedVoteEA, emailDocuments);

        emailClosedHeaderVote = addJProp(emailClaimerNameVote, 1, addCProp(StringClass.get(2000), "Результаты заседания - "));
        emailClosedVote = addJProp(baseGroup, true, "emailClosedVote", "Результаты заседания (e-mail)", emailClosedVoteEA, 1, emailClosedHeaderVote, 1);
        emailClosedVote.setImage("email.png");
        emailClosedVote.property.askConfirm = true;
        emailClosedVote.setDerivedForcedChange(addCProp(ActionClass.instance, true), closedVote, 1);

        isForeignExpert = addJProp("isForeignExpert", "Иностр.", baseLM.equals2, languageExpert, 1, addCProp(language, "english"));
        localeExpert = addJProp("localeExpert", "Locale", localeLanguage, languageExpert, 1);

        emailAuthExpertEA = addEAProp(expert);
        addEARecepient(emailAuthExpertEA, baseLM.email, 1);

        emailAuthExpert = addJProp(baseGroup, true, "emailAuthExpert", "Аутентификация эксперта (e-mail)",
                emailAuthExpertEA, 1, addJProp(authExpertSubjectLanguage, languageExpert, 1), 1);
        emailAuthExpert.setImage("email.png");
        emailAuthExpert.property.askConfirm = true;
//        emailAuthExpert.setDerivedChange(addCProp(ActionClass.instance, true), userLogin, 1, userPassword, 1);

        emailClaimerAcceptedHeaderVote = addJProp(emailClaimerNameVote, 1, addCProp(StringClass.get(2000), "Решение о соответствии - "));
        emailClaimerRejectedHeaderVote = addJProp(emailClaimerNameVote, 1, addCProp(StringClass.get(2000), "Решение о несоответствии - "));

        emailAcceptedProjectEA = addEAProp(project);
        addEARecepient(emailAcceptedProjectEA, emailDocuments);
        emailClaimerAcceptedHeaderProject = addJProp(addSFProp("(CAST(prm1 as text))||(CAST(prm2 as text))", StringClass.get(2000), 2), addCProp(StringClass.get(2000), "Решение о присвоении статуса участника - "), nameNativeClaimerProject, 1);
        emailAcceptedProject = addJProp(baseGroup, true, "emailAcceptedProject", "Решение о присвоении статуса участника (e-mail)", emailAcceptedProjectEA, 1, emailClaimerAcceptedHeaderProject, 1);
        emailAcceptedProject.setImage("email.png");
        emailAcceptedProject.property.askConfirm = true;
        emailAcceptedProject.setDerivedForcedChange(addCProp(ActionClass.instance, true), acceptedProject, 1);

        emailToExpert = addJProp("emailToExpert", "Эксперт по e-mail", addJProp(baseLM.and1, 1, is(expert), 1), baseLM.emailToObject, 1);

        dateAgreementExpert = addDProp("dateAgreementExpert", "Дата соглашения с экспертом", DateClass.instance, expert);
        vone = addCProp("1", IntegerClass.instance, 1);
        claimerProjectVote = addJProp("claimerProjectVote", "claimerProjectVote", claimerProject, projectVote, 1);
        nameNativeJoinClaimerProjectVote = addJProp("nameNativeJoinClaimerProjectVote", "Имя заявителя", nameNativeJoinClaimerProject, projectVote, 1);
        nameNativeJoinClaimerProjectVote.setMinimumWidth(10);
        nameNativeJoinClaimerProjectVote.setPreferredWidth(120);
        countryExpert = addDProp("countryExpert", "Страна эксперта", baseLM.country, expert);
        nameCountryExpert = addJProp("nameCountryExpert", "Страна эксперта", baseLM.name, countryExpert, 1);
        nameCountryExpert.setMinimumWidth(10);
        nameCountryExpert.setPreferredWidth(20);

        caseCountry = addDProp(baseGroup, "caseCountry", "Страна в Предложном падеже", StringClass.get(40), baseLM.country);
        caseCountryExpert = addJProp("caseCountryExpert", "Страна эксперта П.П.", caseCountry, countryExpert, 1);

        currencyExpert = addDProp("currencyExpert", "Валюта (ИД)", currency, expert);
        nameCurrencyExpert = addJProp("nameCurrencyExpert", "Валюта договора", baseLM.name, currencyExpert, 1);
        nameCurrencyExpert.setMinimumWidth(10);
        nameCurrencyExpert.setPreferredWidth(20);


        residency = addDProp(baseGroup, "residency", "Признак резидентства", LogicalClass.instance, baseLM.country);
        residencyCountryExpert = addJProp("residencyCountryExpert", "Резидент", residency, countryExpert, 1);
        moneyQuantityDoneExpertMonthYear = addJProp("moneyQuantityDoneExpertMonthYear", "ЗП эксперта за мес.", baseLM.round0, addJProp(baseLM.multiplyDouble2, quantityDoneExpertMonthYear, 1, 2, 3, rateExpert), 1, 2, 3);

        baseCurrency = addDProp(baseGroup, "baseCurrency", "Базовая валюта", LogicalClass.instance, currency);
        baseCurrencyExpert = addJProp("baseCurrencyExpert", "Базовая валюта", baseCurrency, currencyExpert, 1);

        englCountry = addDProp(baseGroup, "englCountry", "Страна на ангийском", StringClass.get(40), baseLM.country);
        englCountryExpert = addJProp("englCountryExpert", "Страна эксперта англ", englCountry, countryExpert, 1);
        englCurrency = addDProp(baseGroup, "englCurrency", "Валюта на ангийском", StringClass.get(40), currency);
        englCurrencyExpert = addJProp("englCurrencyExpert", "Валюта эксперта англ", englCurrency, currencyExpert, 1);
        pluralCurrency = addDProp(baseGroup, "pluralCurrency", "Валюта множ.числ", StringClass.get(40), currency);
        pluralCurrencyExpert = addJProp("pluralCurrencyExpert", "Валюта эксперта мн.ч.", pluralCurrency, currencyExpert, 1);

        emailLetterExpertMonthYearEA = addEAProp("Акт выполненных работ", IntegerClass.instance, IntegerClass.instance);
        addEARecepient(emailLetterExpertMonthYearEA, emailForCertificates);

        emailLetterCertificatesExpertMonthYear = addJProp(true, "emailLetterCertificatesExpertMonthYear", "Отправка актов", emailLetterExpertMonthYearEA, monthInPreviousDate, yearInPreviousDate);
        emailLetterCertificatesExpertMonthYear.setImage("email.png");
        emailLetterCertificatesExpertMonthYear.property.askConfirm = true;

        // так конечно не совсем правильно, если поменяется дата с 01 числа одного месяца на 01 число другого месяца
        emailLetterCertificatesExpertMonthYear.setDerivedForcedChange(addCProp(ActionClass.instance, true), isNewMonth);

        hasPreliminaryVoteProject = addJProp("hasPreliminaryVoteProject", "Подавался на предв. экспертизу", baseLM.and1, is(project), 1, quantityPreliminaryVoteProject, 1);
        isPreliminaryStatusProject = addSUProp("isPreliminaryStatusProject", "На предв. экспертизу", Union.OVERRIDE, hasPreliminaryVoteProject, isPreliminaryProject);

        isPreliminaryAndStatusProject = addJProp("isPreliminaryAndStatusProject", "На предв. экспертизу и статус", baseLM.and1, isStatusProject, 1, hasPreliminaryVoteProject, 1);

        projectApplication = addDProp(idGroup, "projectApplication", "Проект (ИД)", project, application);

        projectApplicationPreliminary = addJProp(idGroup, "projectApplicationPreliminary", "Проект (ИД)", baseLM.and1, projectApplication, 1, is(applicationPreliminary), 1);
        projectApplicationStatus = addJProp(idGroup, "projectApplicationStatus", "Проект (ИД)", baseLM.and1, projectApplication, 1, is(applicationStatus), 1);

        isPreliminaryApplication = addJProp(isPreliminaryStatusProject, projectApplication, 1);
        isStatusApplication = addJProp(isStatusProject, projectApplication, 1);

        isPreliminaryAndStatusApplication = addJProp(isPreliminaryAndStatusProject, projectApplication, 1);

        preliminaryApplicationProject = addAGProp(idGroup, false, "preliminaryApplicationProject", false, "Заяка на предварительную экспертизу", applicationPreliminary, projectApplication);
        follows(isPreliminaryStatusProject, preliminaryApplicationProject, 1);
        follows(addCProp("Заяка предв.", LogicalClass.instance, true, applicationPreliminary), isPreliminaryApplication, 1);

        statusApplicationProject = addAGProp(idGroup, false, "statusApplicationProject", false, "Заяка на статус участника", applicationStatus, projectApplication);
        follows(isStatusProject, statusApplicationProject, 1);
        follows(addCProp("Заяка на статус (сразу)", LogicalClass.instance, true, applicationStatus), isStatusApplication, 1);

        dateApplicationPreliminary = addJProp("dateApplicationPreliminary", "Дата (предв. экспертиза)", dateProject, projectApplicationPreliminary, 1);
        dateApplicationStatus = addJProp("dateApplicationStatus", "Дата (статус участника)", dateStatusProject, projectApplicationStatus, 1);

        dateApplication = addCUProp(baseGroup, "dateApplication", "Дата", dateApplicationPreliminary, dateApplicationStatus);

        nameNativeProjectApplication = addJProp(baseGroup, "nameNativeProjectApplication", "Проект", nameNativeProject, projectApplication, 1);

        nameNativeClaimerApplicationPreliminary = addJProp("nameNativeClaimerApplicationPreliminary", "Заявитель (предв. экспертиза)", nameNativeUnionManagerProject, projectApplicationPreliminary, 1);
        nameNativeClaimerApplicationStatus = addJProp("nameNativeClaimerApplicationPreliminary", "Заявитель (статус участника)", nameNativeJoinClaimerProject, projectApplicationStatus, 1);

        nameNativeClaimerApplication = addCUProp(baseGroup, "nameNativeClaimerApplication", "Заявитель", nameNativeClaimerApplicationPreliminary, nameNativeClaimerApplicationStatus);
        nameNativeClaimerApplication.setMinimumWidth(10);
        nameNativeClaimerApplication.setPreferredWidth(50);

        emailClaimerApplication = addJProp("emailClaimerApplication", "E-mail заявителя", emailClaimerProject, projectApplication, 1);

        statusJoinApplication = addJProp(idGroup, "statusJoinApplication", "Статус заявки (ИД)", projectStatusProject, projectApplication, 1);
        isPreliminaryAfterStatusApplication = addJProp("isPreliminaryAfterStatusApplication", "Заявка на предв. экспертизу (после подачи на статус)", baseLM.and1, is(applicationPreliminary), 1, isPreliminaryAndStatusApplication, 1);

        statusApplication = addIfElseUProp(idGroup, "statusApplication", "Статус заявки (ИД)", addCProp(projectStatus, "applyStatus", application), statusJoinApplication, isPreliminaryAfterStatusApplication, 1);
        nameStatusApplication = addJProp(baseGroup, "nameStatusApplication", "Статус заявки", baseLM.name, statusApplication, 1);
        officialNameStatusApplication = addJProp(baseGroup, "nameStatusApplication", "Статус заявки (по регламенту)", oficialNameProjectStatus, statusApplication, 1);

        langApplication = addJProp(baseGroup, "langApplication", "Язык", langProject, projectApplication, 1);
        nameNativeShortAggregateClusterApplication = addJProp(baseGroup, "nameNativeShortAggregateClusterApplication", "Кластеры", nameNativeShortAggregateClusterProject, projectApplication, 1);
    }

    @Override
    public void initIndexes() {
        addIndex(inExpertVote);
        addIndex(projectVote);
        addIndex(projectDocument);
    }


    FormEntity languageDocumentTypeForm;
    FormEntity globalForm;
    public ProjectFullFormEntity projectFullNative;
    public ProjectFullFormEntity projectFullForeign;
    public ProjectFullFormEntity projectFullBoth;
    public ClaimerFullFormEntity claimerFull;

    private StatusLogFormEntity logNameStatusForm;
    private LP formLogNameStatusProject;

    @Override
    public void initNavigators() throws JRException, FileNotFoundException {

        projectStatus.setDialogForm(new StatusFormEntity(null, "StatusForm"));

        ToolBarNavigatorWindow mainToolbar = new ToolBarNavigatorWindow(JToolBar.VERTICAL, "mainToolbar", "Навигатор", BorderLayout.WEST);
        mainToolbar.titleShown = false;
        mainToolbar.verticalTextPosition = SwingConstants.BOTTOM;
        mainToolbar.horizontalTextPosition = SwingConstants.CENTER;
        mainToolbar.alignmentX = JToolBar.CENTER_ALIGNMENT;

        ToolBarNavigatorWindow leftToolbar = new ToolBarNavigatorWindow(JToolBar.VERTICAL, "leftToolbar", "Список", 0, 0, 20, 60);

        baseLM.baseElement.window = mainToolbar;
        baseLM.adminElement.window = leftToolbar;

        TreeNavigatorWindow objectsWindow = new TreeNavigatorWindow("objectsWindow", "Объекты", 0, 30, 20, 40);
        objectsWindow.drawRoot = true;
        baseLM.objectElement.window = objectsWindow;

        baseLM.relevantFormsWindow.visible = false;
        baseLM.relevantClassFormsWindow.visible = false;
        baseLM.logWindow.visible = false;

//        logNameStatusForm = new StatusLogFormEntity(null, "logNameStatusForm");
//        formLogNameStatusProject = addMFAProp("История", logNameStatusForm, logNameStatusForm.objProject);
//        formLogNameStatusProject.setImage("history.png");

        projectFullNative = addFormEntity(new ProjectFullFormEntity(baseLM.objectElement, "projectFullNative", "Резюме проекта для эксперта", "rus"));
        project.setEditForm(projectFullNative);
        projectFullBoth = addFormEntity(new ProjectFullFormEntity(baseLM.objectElement, "projectFullBoth", "Резюме проекта для эксперта", "both"));
        projectFullForeign = addFormEntity(new ProjectFullFormEntity(baseLM.objectElement, "projectFullForeign", "Resume project for expert", "eng"));


        claimerFull = addFormEntity(new ClaimerFullFormEntity(baseLM.objectElement, "claimerFull"));
        claimer.setEditForm(claimerFull);

        NavigatorElement print = new NavigatorElement(baseLM.baseElement, "print", "Печатные формы");
        print.window = leftToolbar;

        addFormEntity(new VoteStartFormEntity(print, "voteStart"));
        addFormEntity(new ExpertLetterFormEntity(print, "expertLetter"));
        addFormEntity(new VoteProtocolFormEntity(print, "voteProtocol"));
        addFormEntity(new ExpertProtocolFormEntity(print, "expertProtocol"));
        addFormEntity(new ExpertAuthFormEntity(print, "expertAuth"));
        addFormEntity(new ClaimerAcceptedFormEntity(print, "claimerAccepted"));
        addFormEntity(new ClaimerRejectedFormEntity(print, "claimerRejected"));
        addFormEntity(new ClaimerStatusFormEntity(print, "claimerStatus"));
        addFormEntity(new VoteClaimerFormEntity(print, "voteClaimer", "Уведомление о рассмотрении"));
        addFormEntity(new NoticeRejectedFormEntity(print, "noticeRejected", "Уведомление о несоответствии"));
        addFormEntity(new NoticeAcceptedStatusFormEntity(print, "noticeAcceptedStatus", "Уведомление о соответствии (статус участника)"));
        addFormEntity(new NoticeAcceptedPreliminaryFormEntity(print, "noticeAcceptedPreliminary", "Уведомление о соответствии (предварительная экспертиза)"));
        addFormEntity(new AcceptanceCertificateFormEntity(print, "acceptanceCertificate", "Акт оказанных услуг (резидент)", true));
        addFormEntity(new AcceptanceCertificateFormEntity(print, "acceptanceCertificateNonResident", "Акт оказанных услуг (нерезидент)", false));

        addFormEntity(new ProjectFormEntity(baseLM.baseElement, "project"));
        addFormEntity(new ApplicationFormEntity(baseLM.baseElement, "application"));
        addFormEntity(new ClaimerFormEntity(baseLM.baseElement, "claimer"));
        addFormEntity(new VoteFormEntity(baseLM.baseElement, "vote", false));
        addFormEntity(new ExpertFormEntity(baseLM.baseElement, "expert"));
        addFormEntity(new ExpertStatsFormEntity(baseLM.baseElement, "expertStats"));
        addFormEntity(new VoteExpertFormEntity(baseLM.baseElement, "voteExpert", false));
        addFormEntity(new VoteExpertFormEntity(baseLM.baseElement, "voteExpertRestricted", true));
        addFormEntity(new VoteFormEntity(baseLM.baseElement, "voterestricted", true));
        addFormEntity(new ConsultingCenterFormEntity(baseLM.baseElement, "consultingCenter"));

        baseLM.baseElement.add(print);

        NavigatorElement summaryTables = new NavigatorElement(baseLM.baseElement, "summaryTables", "Сводные таблицы");
        summaryTables.window = leftToolbar;

        addFormEntity(new ProjectClusterFormEntity(summaryTables, "projectCluster"));
        baseLM.baseElement.add(summaryTables);

        NavigatorElement options = new NavigatorElement(baseLM.baseElement, "options", "Настройки");
        options.window = leftToolbar;

        languageDocumentTypeForm = addFormEntity(new LanguageDocumentTypeFormEntity(options, "languageDocumentType"));
        addFormEntity(new DocumentTemplateFormEntity(options, "documentTemplate"));
        globalForm = addFormEntity(new GlobalFormEntity(options, "global"));

        baseLM.baseElement.add(baseLM.adminElement); // перемещаем adminElement в конец
    }

    @Override
    public String getNamePrefix() {
        return null;
    }

    private void addDefaultHintsIncrementTable(FormEntity form) {
        form.addHintsIncrementTable(quantityDoneVote, notEnoughProject, acceptedVote, succeededVote, voteSucceededProjectCluster,
                voteValuedProjectCluster, rejectedProjectCluster, clusterAcceptedProject, currentClusterProject, finalClusterProject, resultExecuteFormalControlProject,
                needExtraVoteProject, resultExecuteLegalCheckProject, overdueFormalControlProject, isPreliminaryNotEnoughDocumentProject, isStatusNotEnoughDocumentProject,
                acceptedProject, rejectedProject, acceptedDecisionProject, rejectedDecisionProject, valuedProject,
                rejectedNoticedProject, acceptedNoticedPreliminaryProject, acceptedNoticedStatusProject,
                formalCheckStatusProject, legalCheckStatusProject, valuedStatusProject,
                certifiedProject, preparedCertificateProject, submittedToRegisterProject, sentToFinDepProject, signedProject,
                sentForSignatureProject, positiveOriginalDocsCheckProject, overdueOriginalDocsCheckProject, negativeOriginalDocsCheckProject, certifiedStatusProject,
                statusProject, projectStatusProject);
    }

    private class ConsultingCenterFormEntity extends FormEntity<SkolkovoBusinessLogics> {
        private ObjectEntity objProject;
        private ObjectEntity objDateFrom;
        private ObjectEntity objDateTo;

        private ConsultingCenterFormEntity(NavigatorElement parent, String sID) {
            super(parent, sID, "Консультативный центр");

            GroupObjectEntity gobjDates = new GroupObjectEntity(1, "date");
            objDateFrom = new ObjectEntity(2, "dateFrom", DateClass.instance, "Дата (с)");
            objDateTo = new ObjectEntity(3, "dateTo", DateClass.instance, "Дата (по)");
            gobjDates.add(objDateFrom);
            gobjDates.add(objDateTo);

            addGroup(gobjDates);
            gobjDates.setSingleClassView(ClassViewType.PANEL);

            addPropertyDraw(objDateFrom, baseLM.objectValue);
            addPropertyDraw(objDateTo, baseLM.objectValue);

            objProject = addSingleGroupObject(4, "project", project, dateProject, nameNativeProject, nameNativeClaimerProject, isConsultingCenterCommentProject, consultingCenterCommentProject);

            addPropertyDraw(sumPositiveConsultingCenterCommentProject, objDateFrom, objDateTo);
            setForceViewType(sumPositiveConsultingCenterCommentProject, ClassViewType.PANEL);
            addPropertyDraw(sumNegativeConsultingCenterCommentProject, objDateFrom, objDateTo);
            setForceViewType(sumNegativeConsultingCenterCommentProject, ClassViewType.PANEL);
            addPropertyDraw(sumTotalConsultingCenterCommentProject, objDateFrom, objDateTo);
            setForceViewType(sumTotalConsultingCenterCommentProject, ClassViewType.PANEL);

            addFixedFilter(new CompareFilterEntity(addPropertyObject(dateProject, objProject), Compare.GREATER_EQUALS, objDateFrom));
            addFixedFilter(new CompareFilterEntity(addPropertyObject(dateProject, objProject), Compare.LESS_EQUALS, objDateTo));
            addFixedFilter(new NotFilterEntity(new NotNullFilterEntity(addPropertyObject(inactiveProject, objProject))));
            addFixedFilter(new NotNullFilterEntity(addPropertyObject(isConsultingCenterQuestionProject, objProject)));
        }

        @Override
        public FormView createDefaultRichDesign() {
            DefaultFormView design = (DefaultFormView) super.createDefaultRichDesign();
            design.defaultOrders.put(design.get(getPropertyDraw(dateProject)), true);
            return design;
        }
    }

    private class ProjectFullFormEntity extends ClassFormEntity<SkolkovoBusinessLogics> {

        private String lng;

        private ObjectEntity objProject;
        private ObjectEntity objCluster;
        private ObjectEntity objPatent;
        private ObjectEntity objAcademic;
        private ObjectEntity objNonRussianSpecialist;

        private ProjectFullFormEntity(NavigatorElement parent, String sID, String caption, String lng) {
            super(parent, sID, caption);

            this.lng = lng;

            objProject = addSingleGroupObject(1, "project", project, "Проект", projectInformationGroup, innovationGroup, projectDocumentsGroup, sourcesFundingGroup, consultingCenterGroup, equipmentGroup, projectOptionsGroup, projectStatusGroup);

            getPropertyDraw(nameReturnInvestorProject).propertyCaption = addPropertyObject(hideNameReturnInvestorProject, objProject);
            getPropertyDraw(amountReturnFundsProject).propertyCaption = addPropertyObject(hideAmountReturnFundsProject, objProject);
            getPropertyDraw(nameNonReturnInvestorProject).propertyCaption = addPropertyObject(hideNameNonReturnInvestorProject, objProject);
            getPropertyDraw(amountNonReturnFundsProject).propertyCaption = addPropertyObject(hideAmountNonReturnFundsProject, objProject);

            getPropertyDraw(isCapitalInvestmentProject).propertyCaption = addPropertyObject(hideIsCapitalInvestmentProject, objProject);
            getPropertyDraw(isPropertyInvestmentProject).propertyCaption = addPropertyObject(hideIsPropertyInvestmentProject, objProject);
            getPropertyDraw(isGrantsProject).propertyCaption = addPropertyObject(hideIsGrantsProject, objProject);
            getPropertyDraw(isOtherNonReturnInvestmentsProject).propertyCaption = addPropertyObject(hideIsOtherNonReturnInvestmentsProject, objProject);

            getPropertyDraw(commentOtherNonReturnInvestmentsProject).propertyCaption = addPropertyObject(hideCommentOtherNonReturnInvestmentsProject, objProject);
            getPropertyDraw(amountOwnFundsProject).propertyCaption = addPropertyObject(hideAmountOwnFundsProject, objProject);
            getPropertyDraw(amountFundsProject).propertyCaption = addPropertyObject(hideAmountFundsProject, objProject);
            getPropertyDraw(commentOtherSoursesProject).propertyCaption = addPropertyObject(hideCommentOtherSoursesProject, objProject);

            getPropertyDraw(descriptionTransferEquipmentProject).propertyCaption = addPropertyObject(hideDescriptionTransferEquipmentProject, objProject);
            getPropertyDraw(ownerEquipmentProject).propertyCaption = addPropertyObject(hideOwnerEquipmentProject, objProject);
            getPropertyDraw(specificationEquipmentProject).propertyCaption = addPropertyObject(hideSpecificationEquipmentProject, objProject);
            getPropertyDraw(descriptionEquipmentProject).propertyCaption = addPropertyObject(hideDescriptionEquipmentProject, objProject);
            getPropertyDraw(commentEquipmentProject).propertyCaption = addPropertyObject(hideCommentEquipmentProject, objProject);
            objProject.groupTo.setSingleClassView(ClassViewType.PANEL);

            addPropertyDraw(objProject, translatedToRussianProject, translatedToEnglishProject);

            if ("eng".equals(lng)) {
                getPropertyDraw(translatedToRussianProject).propertyCaption = addPropertyObject(hideTranslatedToRussianProject, objProject);
            }
            if ("rus".equals(lng)) {
                getPropertyDraw(translatedToEnglishProject).propertyCaption = addPropertyObject(hideTranslatedToEnglishProject, objProject);
            }

            objCluster = addSingleGroupObject(5, "cluster", cluster, "Кластер");
            addPropertyDraw(inProjectCluster, objProject, objCluster);
            addPropertyDraw(objCluster, nameNative, nameForeign);
            addPropertyDraw(new LP[]{nativeSubstantiationProjectCluster, foreignSubstantiationProjectCluster}, objProject, objCluster);
            addPropertyDraw(numberCluster, objCluster);
            addFixedFilter(new NotNullFilterEntity(addPropertyObject(inProjectCluster, objProject, objCluster)));

            objPatent = addSingleGroupObject(2, "patent", patent, "Патент", baseGroup);
            getPropertyDraw(ownerPatent).propertyCaption = addPropertyObject(hideOwnerPatent, objPatent);
            getPropertyDraw(nameNativeOwnerTypePatent).propertyCaption = addPropertyObject(hideNameNativeOwnerTypePatent, objPatent);
            getPropertyDraw(nameForeignOwnerTypePatent).propertyCaption = addPropertyObject(hideNameForeignOwnerTypePatent, objPatent);
            getPropertyDraw(loadFileIntentionOwnerPatent).propertyCaption = addPropertyObject(hideLoadFileIntentionOwnerPatent, objPatent);
            getPropertyDraw(openFileIntentionOwnerPatent).propertyCaption = addPropertyObject(hideOpenFileIntentionOwnerPatent, objPatent);

            getPropertyDraw(valuatorPatent).propertyCaption = addPropertyObject(hideValuatorPatent, objPatent);
            getPropertyDraw(loadFileActValuationPatent).propertyCaption = addPropertyObject(hideLoadFileActValuationPatent, objPatent);
            getPropertyDraw(openFileActValuationPatent).propertyCaption = addPropertyObject(hideOpenFileActValuationPatent, objPatent);
            addObjectActions(this, objPatent);

            objAcademic = addSingleGroupObject(3, "academic", academic, "Учёный", baseGroup);
            addObjectActions(this, objAcademic);
            addFixedFilter(new CompareFilterEntity(addPropertyObject(projectAcademic, objAcademic), Compare.EQUALS, objProject));

            objNonRussianSpecialist = addSingleGroupObject(4, "nonRussianSpecialist", nonRussianSpecialist, "Иностранный специалист", baseGroup);
            addObjectActions(this, objNonRussianSpecialist);
            addFixedFilter(new CompareFilterEntity(addPropertyObject(projectNonRussianSpecialist, objNonRussianSpecialist), Compare.EQUALS, objProject));

            addFixedFilter(new CompareFilterEntity(addPropertyObject(projectPatent, objPatent), Compare.EQUALS, objProject));
            addProject = addMFAProp(actionGroup, "Добавить", this, new ObjectEntity[]{}, true, addPropertyObject(getAddObjectAction(project)));

            if (lng.equals("both"))
                editProject = addMFAProp(actionGroup, "Редактировать проект", this, new ObjectEntity[]{objProject}).setImage("edit.png");
            if (lng.equals("rus")) {
                translateToRussianProject = addJProp(translateActionGroup, true, "Перевести на русский", baseLM.and1,
                        addMFAProp("Требуется перевод на русский", this, new ObjectEntity[]{objProject}), 1,
                        needsToBeTranslatedToRussianProject, 1).setImage("edit.png");
            }

            if (lng.equals("eng")) {
                translateToEnglishProject = addJProp(translateActionGroup, true, "Перевести на английский", baseLM.and1,
                        addMFAProp("Требуется перевод на английский", this, new ObjectEntity[]{objProject}), 1,
                        needsToBeTranslatedToEnglishProject, 1).setImage("edit.png");
            }

            addDefaultHintsIncrementTable(this);
        }

        @Override
        public FormView createDefaultRichDesign() {
            DefaultFormView design = (DefaultFormView) super.createDefaultRichDesign();

            ContainerView row112 = design.createContainer();
            row112.constraints.childConstraints = DoNotIntersectSimplexConstraint.TOTHE_RIGHT;
            row112.add(design.getGroupPropertyContainer(objProject.groupTo, projectOptionsGroup));
            row112.add(design.getGroupPropertyContainer(objProject.groupTo, projectTranslationsGroup));
            row112.add(design.getGroupPropertyContainer(objProject.groupTo, projectStatusGroup));

            ContainerView col11 = design.createContainer();
            col11.constraints.childConstraints = DoNotIntersectSimplexConstraint.TOTHE_BOTTOM;
            design.getGroupPropertyContainer(objProject.groupTo, innovationGroup).constraints.childConstraints = DoNotIntersectSimplexConstraint.TOTHE_BOTTOM;
            col11.add(design.getGroupPropertyContainer(objProject.groupTo, innovationGroup));
            col11.add(row112);

            ContainerView row1 = design.createContainer();
            row1.constraints.childConstraints = DoNotIntersectSimplexConstraint.TOTHE_RIGHT;
            row1.add(col11);
            design.getGroupPropertyContainer(objProject.groupTo, sourcesFundingGroup).constraints.childConstraints = DoNotIntersectSimplexConstraint.TOTHE_BOTTOM;
            design.getGroupPropertyContainer(objProject.groupTo, nonReturnFundingGroup).constraints.childConstraints = DoNotIntersectSimplexConstraint.TOTHE_BOTTOM;
            row1.add(design.getGroupPropertyContainer(objProject.groupTo, sourcesFundingGroup));

            design.getGroupPropertyContainer(objProject.groupTo, sourcesFundingGroup).addAfter(
                    design.getGroupPropertyContainer(objProject.groupTo, nonReturnFundingGroup), design.get(getPropertyDraw(isNonReturnInvestmentsProject)));

            design.getGroupPropertyContainer(objProject.groupTo, projectDocumentsGroup).constraints.childConstraints = DoNotIntersectSimplexConstraint.TOTHE_RIGHT;
            design.getGroupPropertyContainer(objProject.groupTo, applicationFormGroup).constraints.childConstraints = DoNotIntersectSimplexConstraint.TOTHE_BOTTOM;
            design.getGroupPropertyContainer(objProject.groupTo, executiveSummaryGroup).constraints.childConstraints = DoNotIntersectSimplexConstraint.TOTHE_BOTTOM;
            design.getGroupPropertyContainer(objProject.groupTo, techDescrGroup).constraints.childConstraints = DoNotIntersectSimplexConstraint.TOTHE_BOTTOM;
            design.getGroupPropertyContainer(objProject.groupTo, roadMapGroup).constraints.childConstraints = DoNotIntersectSimplexConstraint.TOTHE_BOTTOM;
            design.getGroupPropertyContainer(objProject.groupTo, resolutionIPGroup).constraints.childConstraints = DoNotIntersectSimplexConstraint.TOTHE_BOTTOM;

            ContainerView descrContainer = design.createContainer("Описание проекта");
            descrContainer.constraints.childConstraints = DoNotIntersectSimplexConstraint.TOTHE_BOTTOM;
            descrContainer.add(row1);
            descrContainer.add(design.getGroupPropertyContainer(objProject.groupTo, consultingCenterGroup));
            descrContainer.add(design.getGroupPropertyContainer(objProject.groupTo, equipmentGroup));
            descrContainer.add(design.getGroupPropertyContainer(objProject.groupTo, projectDocumentsGroup));

            ContainerView specContainer = design.createContainer();
            design.getMainContainer().addAfter(specContainer, design.getGroupObjectContainer(objProject.groupTo));
            specContainer.add(descrContainer);
            specContainer.add(design.getGroupObjectContainer(objCluster.groupTo));
            specContainer.add(design.getGroupObjectContainer(objPatent.groupTo));
            specContainer.add(design.getGroupObjectContainer(objAcademic.groupTo));
            specContainer.add(design.getGroupObjectContainer(objNonRussianSpecialist.groupTo));
            specContainer.tabbedPane = true;

            design.getMainContainer().addBefore(design.getGroupPropertyContainer(objProject.groupTo, projectInformationGroup), specContainer);

            if (!("both".equals(lng))) {
                design.getMainContainer().addAfter(design.getGroupPropertyContainer(objProject.groupTo, projectTranslationsGroup), specContainer);
            }
            design.setShowTableFirstLogical(true);

            PropertyObjectEntity sidProjectProperty = addPropertyObject(sidProject, objProject);
            if ("rus".equals(lng)) {
                getPropertyDraw(nameNativeManagerProject).setPropertyHighlight(sidProjectProperty);
                getPropertyDraw(nameNativeClaimerProject).setPropertyHighlight(sidProjectProperty);
                getPropertyDraw(nameNativeProject).setPropertyHighlight(sidProjectProperty);
                getPropertyDraw(nameNativeFinalClusterProject).setPropertyHighlight(sidProjectProperty);
                getPropertyDraw(nativeProblemProject).setPropertyHighlight(sidProjectProperty);
                getPropertyDraw(nativeInnovativeProject).setPropertyHighlight(sidProjectProperty);
                getPropertyDraw(nativeSubstantiationProjectType).setPropertyHighlight(sidProjectProperty);
                getPropertyDraw(loadFileNativeTechnicalDescriptionProject).setPropertyHighlight(sidProjectProperty);
                getPropertyDraw(openFileNativeTechnicalDescriptionProject).setPropertyHighlight(sidProjectProperty);
                getPropertyDraw(loadFileNativeSummaryProject).setPropertyHighlight(sidProjectProperty);
                getPropertyDraw(openFileNativeSummaryProject).setPropertyHighlight(sidProjectProperty);
                getPropertyDraw(nativeNumberPatent).setPropertyHighlight(sidProjectProperty);
                getPropertyDraw(nativeTypePatent).setPropertyHighlight(sidProjectProperty);
            }

            if ("eng".equals(lng)) {
                getPropertyDraw(nameForeignManagerProject).setPropertyHighlight(sidProjectProperty);
                getPropertyDraw(nameForeignClaimerProject).setPropertyHighlight(sidProjectProperty);
                getPropertyDraw(nameForeignProject).setPropertyHighlight(sidProjectProperty);
                getPropertyDraw(nameForeignFinalClusterProject).setPropertyHighlight(sidProjectProperty);
                getPropertyDraw(foreignProblemProject).setPropertyHighlight(sidProjectProperty);
                getPropertyDraw(foreignInnovativeProject).setPropertyHighlight(sidProjectProperty);
                getPropertyDraw(foreignSubstantiationProjectType).setPropertyHighlight(sidProjectProperty);
                getPropertyDraw(loadFileForeignTechnicalDescriptionProject).setPropertyHighlight(sidProjectProperty);
                getPropertyDraw(openFileForeignTechnicalDescriptionProject).setPropertyHighlight(sidProjectProperty);
                getPropertyDraw(loadFileForeignSummaryProject).setPropertyHighlight(sidProjectProperty);
                getPropertyDraw(openFileForeignSummaryProject).setPropertyHighlight(sidProjectProperty);
                getPropertyDraw(foreignNumberPatent).setPropertyHighlight(sidProjectProperty);
                getPropertyDraw(foreignTypePatent).setPropertyHighlight(sidProjectProperty);
            }
            design.setHighlightColor(new Color(255, 250, 205));
            return design;
        }

        @Override
        public ObjectEntity getObject() {
            return objProject;
        }
    }

    public class FillLangProjectActionProperty extends ActionProperty {

        private final ClassPropertyInterface projectInterface;

        public FillLangProjectActionProperty(SkolkovoLogicsModule LM, ValueClass project) {
            super(LM.genSID(), "Заполнить язык", new ValueClass[]{project});

            Iterator<ClassPropertyInterface> i = interfaces.iterator();
            projectInterface = i.next();
        }

        public void execute(Map<ClassPropertyInterface, DataObject> keys, ObjectValue value, List<ClientAction> actions, RemoteForm executeForm, Map<ClassPropertyInterface, PropertyObjectInterfaceInstance> mapObjects) throws SQLException {
            throw new RuntimeException("no need");
        }

        @Override
        public void execute(ExecutionContext context) throws SQLException {

            DataObject projectObject = context.getKeyValue(projectInterface);

            Object fillNative = fillNativeProject.read(context, projectObject);
            Object fillForeign = fillForeignProject.read(context, projectObject);

            String lang = "";
            if ((fillNative != null) && (fillForeign != null)) {
                lang = "Рус/Англ";
            } else if (fillNative != null) {
                lang = "Рус";
            } else if (fillForeign != null) {
                lang = "Англ";
            }
            langProject.execute(lang, context, projectObject);
        }
    }

    private class ProjectFormEntity extends FormEntity<SkolkovoBusinessLogics> {
        private ObjectEntity objProject;
        private ObjectEntity objCluster;
        private ObjectEntity objVote;
        private ObjectEntity objDocument;
        private ObjectEntity objExpert;
        private ObjectEntity objDocumentTemplate;
        private ObjectEntity objFormalControl;
        private ObjectEntity objLegalCheck;
        private ObjectEntity objOriginalDocsCheck;
        private ObjectEntity objNonRussianSpecialist;
        private RegularFilterGroupEntity projectFilterGroup;
        private RegularFilterGroupEntity activeProjectFilterGroup;
        private PropertyDrawEntity nameNativeEntity;
        private PropertyDrawEntity nameForeignEntity;
        private PropertyDrawEntity nameNativeClaimerEntity;
        private PropertyDrawEntity nameForeignClaimerEntity;
        private PropertyDrawEntity nameNativeTranslationEntity;
        private PropertyDrawEntity nameForeignTranslationEntity;
        private PropertyDrawEntity nameNativeClaimerTranslationEntity;
        private PropertyDrawEntity nameForeignClaimerTranslationEntity;

        private ProjectFormEntity(NavigatorElement parent, String sID) {
            super(parent, sID, "Реестр проектов");

            objProject = addSingleGroupObject(project, dateProject, dateStatusProject, nameNativeProject, nameForeignProject,
                    nameNativeShortFinalClusterProject, nameNativeClaimerProject, nameForeignClaimerProject, emailClaimerProject,
                    nameStatusProject, formLogNameStatusProject, nameProjectActionProject, updateDateProject, autoGenerateProject,
                    inactiveProject, quantityVoteProject, quantityClusterProject, generateVoteProject, editClaimerProject, editProject);

            addPropertyDraw(objProject, isOtherClusterProject, nativeSubstantiationOtherClusterProject, foreignSubstantiationOtherClusterProject);
            addPropertyDraw(objProject, registerGroup);
            setForceViewType(registerGroup, ClassViewType.PANEL);
            //   setForceViewType(dateNotEnoughOriginalDocsProject, ClassViewType.PANEL);
            getPropertyDraw(isOtherClusterProject).propertyCaption = addPropertyObject(hideIsOtherClusterProject, objProject);
            getPropertyDraw(nativeSubstantiationOtherClusterProject).propertyCaption = addPropertyObject(hideNativeSubstantiationOtherClusterProject, objProject);
            getPropertyDraw(foreignSubstantiationOtherClusterProject).propertyCaption = addPropertyObject(hideForeignSubstantiationOtherClusterProject, objProject);

            addPropertyDraw(nameNativeCurrentCluster).toDraw = objProject.groupTo;
            setForceViewType(nameNativeCurrentCluster, ClassViewType.PANEL);

            addPropertyDraw(objProject, translateToRussianProject, translateToEnglishProject);
            setForceViewType(translateToRussianProject, ClassViewType.PANEL);
            setForceViewType(translateToEnglishProject, ClassViewType.PANEL);

            nameNativeEntity = addPropertyDraw(nameNativeProject, objProject);
            nameForeignEntity = addPropertyDraw(nameForeignProject, objProject);
            nameNativeClaimerEntity = addPropertyDraw(nameNativeClaimerProject, objProject);
            nameForeignClaimerEntity = addPropertyDraw(nameForeignClaimerProject, objProject);

            setForceViewType(nameNativeEntity, ClassViewType.PANEL);
            setForceViewType(nameForeignEntity, ClassViewType.PANEL);
            setForceViewType(nameNativeClaimerEntity, ClassViewType.PANEL);
            setForceViewType(nameForeignClaimerEntity, ClassViewType.PANEL);

            nameNativeTranslationEntity = addPropertyDraw(nameNativeProject, objProject);
            nameForeignTranslationEntity = addPropertyDraw(nameForeignProject, objProject);
            nameNativeClaimerTranslationEntity = addPropertyDraw(nameNativeClaimerProject, objProject);
            nameForeignClaimerTranslationEntity = addPropertyDraw(nameForeignClaimerProject, objProject);

            setForceViewType(nameNativeTranslationEntity, ClassViewType.PANEL);
            setForceViewType(nameForeignTranslationEntity, ClassViewType.PANEL);
            setForceViewType(nameNativeClaimerTranslationEntity, ClassViewType.PANEL);
            setForceViewType(nameForeignClaimerTranslationEntity, ClassViewType.PANEL);

            addPropertyDraw(objProject, sentForTranslationProject, projectDocumentsGroup);
            setForceViewType(sentForTranslationProject, ClassViewType.PANEL);
            setForceViewType(projectDocumentsGroup, ClassViewType.PANEL);

            addPropertyDraw(importProjectsAction).toDraw = objProject.groupTo;
            setForceViewType(importProjectsAction, ClassViewType.PANEL);

            addPropertyDraw(copyProjectAction, objProject).toDraw = objProject.groupTo;
            setForceViewType(copyProjectAction, ClassViewType.PANEL);

            hideTranslateToRussianProject = addHideCaptionProp(privateGroup, "Перевести", translateToRussianProject, needsToBeTranslatedToRussianProject);
            getPropertyDraw(translateToRussianProject).propertyCaption = addPropertyObject(hideTranslateToRussianProject, objProject);

            hideTranslateToEnglishProject = addHideCaptionProp(privateGroup, "Перевести", translateToEnglishProject, needsToBeTranslatedToEnglishProject);
            getPropertyDraw(translateToEnglishProject).propertyCaption = addPropertyObject(hideTranslateToEnglishProject, objProject);

            hideLoadFileResolutionIPProject = addHideCaptionProp(privateGroup, "Загрузить", loadFileResolutionIPProject, addJProp(baseLM.andNot1, addCProp(LogicalClass.instance, true, project), 1, openFileResolutionIPProject, 1));
            getPropertyDraw(loadFileResolutionIPProject).propertyCaption = addPropertyObject(hideLoadFileResolutionIPProject, objProject);

            addObjectActions(this, objProject);

            PropertyObjectEntity statusProperty = addPropertyObject(statusDataProject, objProject);
            getPropertyDraw(nameStatusProject).setPropertyHighlight(statusProperty);

            PropertyObjectEntity dateProperty = addPropertyObject(dateDataProject, objProject);
            getPropertyDraw(dateProject).setPropertyHighlight(dateProperty);

            PropertyObjectEntity dateStatusProperty = addPropertyObject(dateStatusDataProject, objProject);
            getPropertyDraw(dateStatusProject).setPropertyHighlight(dateStatusProperty);

            PropertyObjectEntity nameNativeProperty = addPropertyObject(nameNativeDataProject, objProject);
            getPropertyDraw(nameNativeProject).setPropertyHighlight(nameNativeProperty);

            PropertyObjectEntity nameForeignProperty = addPropertyObject(nameForeignDataProject, objProject);
            getPropertyDraw(nameForeignProject).setPropertyHighlight(nameForeignProperty);

            PropertyObjectEntity nameNativeCorrectHighlightClaimerProjectProperty = addPropertyObject(nameNativeCorrectHighlightClaimerProject, objProject);
            getPropertyDraw(nameNativeClaimerProject).setPropertyHighlight(nameNativeCorrectHighlightClaimerProjectProperty);

            PropertyObjectEntity nameForeignCorrectHighlightClaimerProjectProperty = addPropertyObject(nameForeignCorrectHighlightClaimerProject, objProject);
            getPropertyDraw(nameForeignClaimerProject).setPropertyHighlight(nameForeignCorrectHighlightClaimerProjectProperty);

//            addPropertyDraw(addProject).toDraw = objProject.groupTo;
//            getPropertyDraw(addProject).forceViewType = ClassViewType.PANEL;

            objCluster = addSingleGroupObject(cluster);
            addPropertyDraw(inProjectCluster, objProject, objCluster);
            addPropertyDraw(objCluster, nameNative, nameForeign, nameNativeShort);
            addPropertyDraw(new LP[]{nativeSubstantiationProjectCluster, foreignSubstantiationProjectCluster}, objProject, objCluster);
            addPropertyDraw(numberCluster, objCluster);

            getPropertyDraw(generateVoteProject).forceViewType = ClassViewType.PANEL;
            getPropertyDraw(generateVoteProject).propertyCaption = addPropertyObject(hideGenerateVoteProject, objProject);

            objVote = addSingleGroupObject(vote, dateStartVote, dateEndVote, nameNativeClusterVote, nameProjectActionVote, openedVote, succeededVote, acceptedVote,
                    quantityDoneVote, quantityInClusterVote, quantityInnovativeVote, quantityForeignVote, loadFileDecisionVote, openFileDecisionVote,
                    emailClaimerVote, emailNoticeRejectedVote, emailNoticeAcceptedStatusVote, emailNoticeAcceptedPreliminaryVote, decisionNoticedVote, baseLM.delete);
            objVote.groupTo.banClassView.addAll(BaseUtils.toList(ClassViewType.PANEL, ClassViewType.HIDE));

//            getPropertyDraw(copyResultsVote).forceViewType = ClassViewType.PANEL;

            objDocumentTemplate = addSingleGroupObject(documentTemplate, "Шаблон документов", baseLM.name);
            objDocumentTemplate.groupTo.setSingleClassView(ClassViewType.PANEL);
            setReadOnly(objDocumentTemplate, true);
            addPropertyDraw(generateDocumentsProjectDocumentType, objProject, objDocumentTemplate);

            objDocument = addSingleGroupObject(document, nameTypeDocument, nameLanguageDocument, postfixDocument, loadFileDocument, openFileDocument);
            addObjectActions(this, objDocument);
            getPropertyDraw(postfixDocument).forceViewType = ClassViewType.PANEL;
            getPropertyDraw(postfixDocument).propertyCaption = addPropertyObject(hidePostfixDocument, objDocument);

            addPropertyDraw(exportProjectDocumentsAction, objProject).toDraw = objDocument.groupTo;
            setForceViewType(exportProjectDocumentsAction, ClassViewType.PANEL);

            addPropertyDraw(includeDocumentsProject, objProject).toDraw = objDocument.groupTo;
            setForceViewType(includeDocumentsProject, ClassViewType.PANEL);

            //hideIncludeDocumentsProject = addHideCaptionProp(privateGroup, "Подключить", includeDocumentsProject, addJProp(baseLM.andNot1, openFileResolutionIPProject, 1, needTranslationProject, 1));
            //getPropertyDraw(includeDocumentsProject).propertyCaption = addPropertyObject(hideIncludeDocumentsProject, objProject);
            hideIncludeDocumentsProject = addHideCaptionProp(privateGroup, "Подключить", includeDocumentsProject, openFileResolutionIPProject);
            getPropertyDraw(includeDocumentsProject).propertyCaption = addPropertyObject(hideIncludeDocumentsProject, objProject);


            objExpert = addSingleGroupObject(expert);
            addPropertyDraw(objExpert, objVote, inExpertVote, oldExpertVote);
            addPropertyDraw(objExpert, baseLM.name, documentNameExpert, baseLM.email);
            addPropertyDraw(voteResultGroup, true, objExpert, objVote);

            setForceViewType(voteResultCommentGroup, ClassViewType.PANEL);
            setForceViewType(projectOtherClusterGroup, ClassViewType.PANEL);

            objFormalControl = addSingleGroupObject(formalControl);
            addPropertyDraw(new LP[]{addNoListOfExpertsFCResult, addNotEnoughDocumentsFCResult, addNotSuitableClusterFCResult, addRepeatedFCResult, addPositiveFCResult});
            addPropertyDraw(objFormalControl, dateFormalControl, nameResultFormalControl, nameProjectActionFormalControl, baseLM.delete);
            addPropertyDraw(commentFormalControl, objFormalControl).forceViewType = ClassViewType.PANEL;

            objLegalCheck = addSingleGroupObject(legalCheck);
            addPropertyDraw(objLegalCheck, dateLegalCheck, nameResultLegalCheck, baseLM.delete);
            addPropertyDraw(new LP[]{addNegativeLCResult, addPositiveLCResult});
            addPropertyDraw(commentLegalCheck, objLegalCheck).forceViewType = ClassViewType.PANEL;

            objOriginalDocsCheck = addSingleGroupObject(originalDocsCheck);
            addPropertyDraw(objOriginalDocsCheck, dateOriginalDocsCheck, nameResultOriginalDocsCheck, baseLM.delete);
            addPropertyDraw(new LP[]{addNegativeODCResult, addPositiveODCResult});
            addPropertyDraw(commentOriginalDocsCheck, objOriginalDocsCheck).forceViewType = ClassViewType.PANEL;

            objNonRussianSpecialist = addSingleGroupObject(4, "nonRussianSpecialist", nonRussianSpecialist, "Иностранный специалист", baseGroup);
            addObjectActions(this, objNonRussianSpecialist);
            addFixedFilter(new CompareFilterEntity(addPropertyObject(projectNonRussianSpecialist, objNonRussianSpecialist), Compare.EQUALS, objProject));


            addFixedFilter(new CompareFilterEntity(addPropertyObject(projectVote, objVote), Compare.EQUALS, objProject));
            addFixedFilter(new CompareFilterEntity(addPropertyObject(projectDocument, objDocument), Compare.EQUALS, objProject));
            addFixedFilter(new NotNullFilterEntity(addPropertyObject(clusterInExpertVote, objExpert, objVote)));
            addFixedFilter(new CompareFilterEntity(addPropertyObject(projectFormalControl, objFormalControl), Compare.EQUALS, objProject));
            addFixedFilter(new CompareFilterEntity(addPropertyObject(projectLegalCheck, objLegalCheck), Compare.EQUALS, objProject));
            addFixedFilter(new CompareFilterEntity(addPropertyObject(projectOriginalDocsCheck, objOriginalDocsCheck), Compare.EQUALS, objProject));
            addFixedFilter(new OrFilterEntity(new NotNullFilterEntity(addPropertyObject(inProjectCurrentCluster, objProject)),
                    new NotFilterEntity(new NotNullFilterEntity(addPropertyObject(currentCluster)))));

            RegularFilterGroupEntity expertFilterGroup = new RegularFilterGroupEntity(genID());
            expertFilterGroup.addFilter(new RegularFilterEntity(genID(),
                    new NotNullFilterEntity(addPropertyObject(inExpertVote, objExpert, objVote)),
                    "В заседании",
                    KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0)), true);
            addRegularFilterGroup(expertFilterGroup);

            projectFilterGroup = new RegularFilterGroupEntity(genID());
            projectFilterGroup.addFilter(new RegularFilterEntity(genID(),
                    new NotNullFilterEntity(addPropertyObject(valuedProject, objProject)),
                    "Оценен",
                    KeyStroke.getKeyStroke(KeyEvent.VK_F10, 0)));
            addRegularFilterGroup(projectFilterGroup);

            activeProjectFilterGroup = new RegularFilterGroupEntity(genID());
            activeProjectFilterGroup.addFilter(new RegularFilterEntity(genID(),
                    new NotFilterEntity(new NotNullFilterEntity(addPropertyObject(inactiveProject, objProject))),
                    "Активный",
                    KeyStroke.getKeyStroke(KeyEvent.VK_F9, 0)), true);
            addRegularFilterGroup(activeProjectFilterGroup);

            addDefaultHintsIncrementTable(this);
            setPageSize(0);

            setReadOnly(true, objCluster.groupTo);
            setReadOnly(inProjectCluster, false);
            setReadOnly(nameNativeCurrentCluster, false);
        }

        @Override
        public FormView createDefaultRichDesign() {
            DefaultFormView design = (DefaultFormView) super.createDefaultRichDesign();

            design.defaultOrders.put(design.get(getPropertyDraw(dateProject, objProject)), true);
            design.defaultOrders.put(design.get(getPropertyDraw(numberCluster)), true);
//            design.get(getPropertyDraw(addProject)).drawToToolbar = true;

            design.addIntersection(design.getGroupPropertyContainer(objProject.groupTo, projectInformationGroup),
                    design.getGroupPropertyContainer(objProject.groupTo, translateActionGroup),
                    DoNotIntersectSimplexConstraint.TOTHE_RIGHT);

            design.get(getPropertyDraw(importProjectsAction)).drawToToolbar = true;
            design.get(getPropertyDraw(copyProjectAction)).drawToToolbar = true;
//            design.getPanelContainer(objProject.groupTo).add(design.getGroupPropertyContainer((GroupObjectEntity)null, importGroup));

            ContainerView specContainer = design.createContainer();
            specContainer.tabbedPane = true;

            ContainerView projectMainInformationContainer = design.createContainer();
            projectMainInformationContainer.add(design.get(nameNativeEntity));
            projectMainInformationContainer.add(design.get(nameForeignEntity));
            projectMainInformationContainer.add(design.get(nameNativeClaimerEntity));
            projectMainInformationContainer.add(design.get(nameForeignClaimerEntity));

            ContainerView projectTranslationInformationContainer = design.createContainer();
            projectTranslationInformationContainer.add(design.get(nameNativeTranslationEntity));
            projectTranslationInformationContainer.add(design.get(nameForeignTranslationEntity));
            projectTranslationInformationContainer.add(design.get(nameNativeClaimerTranslationEntity));
            projectTranslationInformationContainer.add(design.get(nameForeignClaimerTranslationEntity));

            design.getMainContainer().addAfter(specContainer, design.getGroupObjectContainer(objProject.groupTo));

            ContainerView infoContainer = design.createContainer("Информация");
            infoContainer.add(projectMainInformationContainer);
            infoContainer.add(design.getGroupObjectContainer(objCluster.groupTo));
            infoContainer.add(design.getGroupPropertyContainer(objProject.groupTo, projectOtherClusterGroup));

            ContainerView formalControlContainer = design.createContainer("Формальная экспертиза");
            formalControlContainer.add(design.get(getPropertyDraw(exportProjectDocumentsAction)));
            formalControlContainer.add(design.getGroupObjectContainer(objFormalControl.groupTo));
            formalControlContainer.add(design.getGroupPropertyContainer((GroupObjectEntity) null, formalControlResultGroup));
            PropertyDrawView commentFormalView = design.get(getPropertyDraw(commentFormalControl, objFormalControl));
            commentFormalView.constraints.fillHorizontal = 1.0;
            commentFormalView.preferredSize = new Dimension(-1, 200);
            commentFormalView.panelLabelAbove = true;

            ContainerView legalCheckContainer = design.createContainer("Юридическая проверка");
            legalCheckContainer.add(design.getGroupObjectContainer(objLegalCheck.groupTo));
            legalCheckContainer.add(design.getGroupPropertyContainer((GroupObjectEntity) null, legalCheckResultGroup));
            PropertyDrawView commentLegalView = design.get(getPropertyDraw(commentLegalCheck, objLegalCheck));
            commentLegalView.constraints.fillHorizontal = 1.0;
            commentLegalView.preferredSize = new Dimension(-1, 200);
            commentLegalView.panelLabelAbove = true;

            ContainerView projectDocumentsContainer = design.getGroupPropertyContainer(objProject.groupTo, projectDocumentsGroup);
            projectDocumentsContainer.constraints.childConstraints = DoNotIntersectSimplexConstraint.TOTHE_RIGHT;

            ContainerView translationContainer = design.createContainer("Перевод");
            translationContainer.constraints.fillHorizontal = 1.0;
            translationContainer.constraints.fillVertical = 1.0;
            translationContainer.add(design.get(getPropertyDraw(sentForTranslationProject, objProject)));
            translationContainer.add(projectTranslationInformationContainer);
            translationContainer.add(projectDocumentsContainer);
            translationContainer.add(design.getGroupObjectContainer(objNonRussianSpecialist.groupTo));
            translationContainer.add(design.getGroupPropertyContainer(objProject.groupTo, translateActionGroup));

            ContainerView docContainer = design.createContainer("Документы");
            docContainer.add(design.getGroupObjectContainer(objDocumentTemplate.groupTo));
            docContainer.add(design.getGroupObjectContainer(objDocument.groupTo));
            docContainer.add(design.getGroupPropertyContainer(objProject.groupTo, resolutionIPGroup));

            ContainerView expertContainer = design.createContainer("Экспертиза по существу");
            expertContainer.add(design.getGroupObjectContainer(objVote.groupTo));
            expertContainer.add(design.getGroupObjectContainer(objExpert.groupTo));

            ContainerView originalDocsContainer = design.createContainer("Проверка оригиналов документов");
            originalDocsContainer.add(design.getGroupObjectContainer(objOriginalDocsCheck.groupTo));
            originalDocsContainer.add(design.getGroupPropertyContainer((GroupObjectEntity) null, originalDoscCheckGroup));
            PropertyDrawView commentOriginalDocsView = design.get(getPropertyDraw(commentOriginalDocsCheck, objOriginalDocsCheck));
            commentOriginalDocsView.constraints.fillHorizontal = 1.0;
            commentOriginalDocsView.preferredSize = new Dimension(-1, 200);
            commentOriginalDocsView.panelLabelAbove = true;

            ContainerView registerContainer = design.createContainer("Оформление свидетельства");
            registerContainer.constraints.fillHorizontal = 1.0;
            registerContainer.constraints.fillVertical = 1.0;
            registerContainer.add(design.getGroupPropertyContainer(objProject.groupTo, registerGroup));

            specContainer.add(infoContainer);
            specContainer.add(formalControlContainer);
            specContainer.add(legalCheckContainer);
            specContainer.add(translationContainer);
            specContainer.add(docContainer);
            specContainer.add(expertContainer);
            specContainer.add(originalDocsContainer);
            specContainer.add(registerContainer);

            design.setHighlightColor(new Color(223, 255, 223));
//            design.get(objVoteHeader.groupTo).grid.constraints.fillHorizontal = 1.5;

            design.getPanelContainer(objVote.groupTo).add(design.get(getPropertyDraw(generateVoteProject)));

            design.get(objProject.groupTo).grid.constraints.fillVertical = 1.5;
            design.get(objExpert.groupTo).grid.constraints.fillVertical = 1.5;

            design.setPanelLabelAbove(voteResultCommentGroup, true);
            design.setConstraintsFillHorizontal(voteResultCommentGroup, 1);

            design.setPreferredSize(voteResultCheckGroup, new Dimension(60, -1));

            design.get(objVote.groupTo).grid.hideToolbarItems();

            design.addIntersection(design.get(getPropertyDraw(innovativeCommentExpertVote)), design.get(getPropertyDraw(completeCommentExpertVote)), DoNotIntersectSimplexConstraint.TOTHE_RIGHT);

            design.get(objProject.groupTo).grid.getContainer().setFixedSize(new Dimension(-1, 200));

            design.get(objFormalControl.groupTo).grid.constraints.fillHorizontal = 1;

            return design;
        }
    }

    private class StatusLogFormEntity extends FormEntity<SkolkovoBusinessLogics> {
        public ObjectEntity objProject;
        private ObjectEntity objSession;

        private StatusLogFormEntity(NavigatorElement parent, String sID) {
            super(parent, sID, "История изменений статуса");

            objProject = addSingleGroupObject(project, nameNativeProject, nameNativeClaimerProject);
            objProject.groupTo.setSingleClassView(ClassViewType.PANEL);

            objSession = addSingleGroupObject(baseLM.session, baseLM.baseGroup);

            addPropertyDraw(logNameStatusProject, objProject, objSession);

            addFixedFilter(new NotNullFilterEntity(addPropertyObject(logStatusProject, objProject, objSession)));

            setReadOnly(true);
        }
    }


    private class ApplicationFormEntity extends FormEntity<SkolkovoBusinessLogics> {
        private ObjectEntity objApplication;

        private ApplicationFormEntity(NavigatorElement parent, String sID) {
            super(parent, sID, "Реестр заявок");

            objApplication = addSingleGroupObject(application, dateApplication, nameNativeClaimerApplication, baseLM.objectClassName, nameNativeProjectApplication,
                    officialNameStatusApplication, langApplication, nameNativeShortAggregateClusterApplication, emailClaimerApplication);

            setReadOnly(false);
        }

    }

    private class ProjectClusterFormEntity extends FormEntity<SkolkovoBusinessLogics> {
        private ObjectEntity objProject;
        private ObjectEntity objCluster;

        private ProjectClusterFormEntity(NavigatorElement parent, String sID) {
            super(parent, sID, "Проект/Кластер");


            objProject = new ObjectEntity(genID(), project, "Проект");
            addPropertyDraw(objProject, dateProject, nameNativeProject, nameForeignProject, nameNativeShortFinalClusterProject, nameNativeClaimerProject, nameForeignClaimerProject, emailClaimerProject,
                    nameStatusProject, nameProjectActionProject, updateDateProject, autoGenerateProject, inactiveProject);

            objCluster = new ObjectEntity(genID(), cluster, "Кластер");
            addPropertyDraw(objCluster, nameNative, nameForeign);

            addPropertyDraw(objProject, objCluster, nativeSubstantiationProjectCluster, foreignSubstantiationProjectCluster);

            GroupObjectEntity gobjProjectCluster = new GroupObjectEntity(genID());
            gobjProjectCluster.add(objProject);
            gobjProjectCluster.add(objCluster);
            addGroup(gobjProjectCluster);
            gobjProjectCluster.setSingleClassView(ClassViewType.GRID);

            addFixedFilter(new NotNullFilterEntity(addPropertyObject(inProjectCluster, objProject, objCluster)));

            setReadOnly(true);
        }
    }

    private class GlobalFormEntity extends FormEntity<SkolkovoBusinessLogics> {

        private GlobalFormEntity(NavigatorElement parent, String sID) {
            super(parent, sID, "Глобальные параметры");

            addPropertyDraw(new LP[]{baseLM.currentDate, requiredPeriod, overduePeriod, requiredQuantity, limitExperts,
                    emailDocuments, emailClaimerFromAddress, emailForCertificates,
                    projectsImportLimit, importProjectSidsAction, showProjectsToImportAction, showProjectsReplaceToImportAction, importProjectsAction,
                    rateExpert, emailLetterCertificatesExpertMonthYear});
        }
    }

    private class VoteFormEntity extends FormEntity<SkolkovoBusinessLogics> {
        private ObjectEntity objVote;
        private ObjectEntity objExpert;

        private VoteFormEntity(NavigatorElement parent, String sID, boolean restricted) {
            super(parent, sID, (!restricted) ? "Реестр заседаний" : "Результаты заседаний");

            objVote = addSingleGroupObject(vote, baseLM.objectClassName, nameNativeProjectVote, nameNativeClaimerVote, nameProjectActionVote, nameNativeClusterVote, dateStartVote, dateEndVote, openedVote, succeededVote, acceptedVote, quantityDoneVote, quantityInClusterVote, quantityInnovativeVote, quantityForeignVote);
            if (!restricted)
                addPropertyDraw(objVote, emailClosedVote, baseLM.delete);

            objExpert = addSingleGroupObject(expert);
            if (!restricted)
                addPropertyDraw(objExpert, baseLM.userFirstName, baseLM.userLastName, documentNameExpert, baseLM.userLogin, baseLM.userPassword, baseLM.email);

            addPropertyDraw(objExpert, objVote, oldExpertVote);
            addPropertyDraw(voteResultGroup, true, objExpert, objVote);
            if (!restricted)
                addPropertyDraw(objExpert, objVote, allowedEmailLetterExpertVote);
            setForceViewType(voteResultCommentGroup, ClassViewType.PANEL);

            if (!restricted) {
                addPropertyDraw(objVote, voteStartFormVote);
                addPropertyDraw(objVote, voteProtocolFormVote);
                setForceViewType(voteStartFormVote, ClassViewType.PANEL);
                setForceViewType(voteProtocolFormVote, ClassViewType.PANEL);

            }

            addFixedFilter(new NotNullFilterEntity(addPropertyObject(inExpertVote, objExpert, objVote)));

            RegularFilterGroupEntity voteFilterGroup = new RegularFilterGroupEntity(genID());
            voteFilterGroup.addFilter(new RegularFilterEntity(genID(),
                    new NotNullFilterEntity(addPropertyObject(openedVote, objVote)),
                    "Открыто",
                    KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0)), !restricted);
            voteFilterGroup.addFilter(new RegularFilterEntity(genID(),
                    new NotNullFilterEntity(addPropertyObject(succeededVote, objVote)),
                    "Состоялось",
                    KeyStroke.getKeyStroke(KeyEvent.VK_F10, 0)), false);
            voteFilterGroup.addFilter(new RegularFilterEntity(genID(),
                    new NotNullFilterEntity(addPropertyObject(acceptedVote, objVote)),
                    "Положительно",
                    KeyStroke.getKeyStroke(KeyEvent.VK_F9, 0)), false);
            addRegularFilterGroup(voteFilterGroup);

            if (restricted) {
                addFixedFilter(new NotNullFilterEntity(addPropertyObject(voteResultExpertVote, objExpert, objVote)));
                setReadOnly(true);
            }
//            setReadOnly(true, objVote.groupTo);
//            setReadOnly(true, objExpert.groupTo);
//            setReadOnly(allowedEmailLetterExpertVote, false);
//            setReadOnly(emailClosedVote, false);

            setPageSize(0);
        }

        @Override
        public FormView createDefaultRichDesign() {
            DefaultFormView design = (DefaultFormView) super.createDefaultRichDesign();

            design.setPanelLabelAbove(voteResultCommentGroup, true);
            design.setConstraintsFillHorizontal(voteResultCommentGroup, 0.5);

            design.setPreferredSize(voteResultCheckGroup, new Dimension(60, -1));

            design.get(objVote.groupTo).grid.getContainer().setFixedSize(new Dimension(-1, 300));

            return design;
        }
    }

    private class ExpertFormEntity extends FormEntity<SkolkovoBusinessLogics> {
        private ObjectEntity objExpert;
        private ObjectEntity objVote;
        private ObjectEntity objExtraCluster;

        private ExpertFormEntity(NavigatorElement parent, String sID) {
            super(parent, sID, "Реестр экспертов");

            objExpert = addSingleGroupObject(expert, baseLM.selection, baseLM.userFirstName, baseLM.userLastName, documentNameExpert, baseLM.userLogin, baseLM.userPassword, baseLM.email, disableExpert, nameNativeClusterExpert, nameLanguageExpert, dateAgreementExpert, nameCountryExpert, nameCurrencyExpert, expertResultGroup, baseLM.generateLoginPassword, emailAuthExpert);
            addObjectActions(this, objExpert);

            objVote = addSingleGroupObject(vote, nameNativeProjectVote, dateStartVote, dateEndVote, openedVote, succeededVote, quantityDoneVote);

            addPropertyDraw(voteResultGroup, true, objExpert, objVote);
            addPropertyDraw(objExpert, objVote, allowedEmailLetterExpertVote);
            setForceViewType(voteResultCommentGroup, ClassViewType.PANEL);

            objExtraCluster = addSingleGroupObject(cluster, "Дополнительные кластеры");
            addPropertyDraw(extraClusterExpert, objExtraCluster, objExpert);
            addPropertyDraw(objExtraCluster, nameNative, nameForeign);

            RegularFilterGroupEntity inactiveFilterGroup = new RegularFilterGroupEntity(genID());
            inactiveFilterGroup.addFilter(new RegularFilterEntity(genID(),
                    new NotFilterEntity(new NotNullFilterEntity(addPropertyObject(disableExpert, objExpert))),
                    "Только активные"), true);
            addRegularFilterGroup(inactiveFilterGroup);

            addFixedFilter(new NotNullFilterEntity(addPropertyObject(inNewExpertVote, objExpert, objVote)));

            addFixedFilter(new NotFilterEntity(new NotNullFilterEntity(addPropertyObject(primClusterExpert, objExtraCluster, objExpert))));
//            setReadOnly(true, objVote.groupTo);
//            setReadOnly(allowedEmailLetterExpertVote, false);

            setPageSize(0);
        }

        @Override
        public FormView createDefaultRichDesign() {
            DefaultFormView design = (DefaultFormView) super.createDefaultRichDesign();

            design.setPanelLabelAbove(voteResultCommentGroup, true);
            design.setConstraintsFillHorizontal(voteResultCommentGroup, 0.5);

            design.setPreferredSize(voteResultCheckGroup, new Dimension(60, -1));

            design.get(objExpert.groupTo).grid.getContainer().setFixedSize(new Dimension(-1, 300));

            ContainerView specContainer = design.createContainer();
            design.getMainContainer().addAfter(specContainer, design.getGroupObjectContainer(objExpert.groupTo));
            specContainer.add(design.getGroupObjectContainer(objVote.groupTo));
            specContainer.add(design.getGroupObjectContainer(objExtraCluster.groupTo));
            specContainer.tabbedPane = true;

            return design;
        }
    }

    private class ExpertStatsFormEntity extends FormEntity<SkolkovoBusinessLogics> {

        private ObjectEntity objExpert;
        private ObjectEntity objWeek;

        private ExpertStatsFormEntity(NavigatorElement parent, String sID) {
            super(parent, sID, "Статистика экспертов");

            objWeek = addSingleGroupObject(1, "week", IntegerClass.instance, "Неделя");

            objExpert = addSingleGroupObject(2, "expert", expert, baseLM.selection, disableExpert, nameNativeShortClusterExpert, baseLM.userFirstName, baseLM.userLastName, baseLM.email);

            PropertyDrawEntity quantity = addPropertyDraw(quantityNewExpertWeek, objExpert, objWeek);
            quantity.columnGroupObjects.add(objWeek.groupTo);
            quantity.propertyCaption = addPropertyObject(baseLM.objectValue.getLP(IntegerClass.instance), objWeek);

            addPropertyDraw(objExpert, quantityTotalExpert);

            addFixedFilter(new NotNullFilterEntity(addPropertyObject(quantityNewWeek, objWeek)));

            RegularFilterGroupEntity inactiveFilterGroup = new RegularFilterGroupEntity(genID());
            inactiveFilterGroup.addFilter(new RegularFilterEntity(genID(),
                    new NotFilterEntity(new NotNullFilterEntity(addPropertyObject(disableExpert, objExpert))),
                    "Только активные"));
            addRegularFilterGroup(inactiveFilterGroup);

            setReadOnly(true);
            setPageSize(0);
        }

        @Override
        public FormView createDefaultRichDesign() {
            DefaultFormView design = (DefaultFormView) super.createDefaultRichDesign();

            design.defaultOrders.put(design.get(getPropertyDraw(nameNativeShortClusterExpert)), true);
            design.defaultOrders.put(design.get(getPropertyDraw(baseLM.userLastName)), true);

            return design;
        }
    }

    private class VoteExpertFormEntity extends FormEntity<SkolkovoBusinessLogics> {
        private ObjectEntity objExpert;
        private ObjectEntity objVote;

        private VoteExpertFormEntity(NavigatorElement parent, String sID, boolean restricted) {
            super(parent, sID, (!restricted) ? "Реестр голосований" : "Результаты голосований");

            objVote = new ObjectEntity(genID(), vote, "Заседание");
            addPropertyDraw(objVote, nameNativeProjectVote, nameNativeClaimerVote, dateStartVote, dateEndVote, openedVote, succeededVote, acceptedVote);

            objExpert = new ObjectEntity(genID(), expert, "Эксперт");
            addPropertyDraw(objExpert, nameNativeClusterExpert, nameLanguageExpert);

            if (!restricted)
                addPropertyDraw(objExpert, baseLM.userFirstName, baseLM.userLastName, documentNameExpert, baseLM.email);

            if (!restricted)
                addPropertyDraw(objExpert, objVote, allowedEmailLetterExpertVote);

            addPropertyDraw(voteResultGroup, true, objExpert, objVote);
            if (!restricted)
                addPropertyDraw(expertResultGroup, true, objExpert);
            setForceViewType(voteResultCommentGroup, ClassViewType.PANEL);

            GroupObjectEntity gobjVoteExpert = new GroupObjectEntity(genID());
            gobjVoteExpert.add(objVote);
            gobjVoteExpert.add(objExpert);
            addGroup(gobjVoteExpert);
            gobjVoteExpert.setSingleClassView(ClassViewType.GRID);

            addFixedFilter(new NotNullFilterEntity(addPropertyObject(inNewExpertVote, objExpert, objVote)));

            RegularFilterGroupEntity filterGroupOpenedVote = new RegularFilterGroupEntity(genID());
            filterGroupOpenedVote.addFilter(new RegularFilterEntity(genID(),
                    new NotNullFilterEntity(addPropertyObject(openedVote, objVote)),
                    "Только открытые заседания",
                    KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0)));
            addRegularFilterGroup(filterGroupOpenedVote);

            RegularFilterGroupEntity filterGroupDoneExpertVote = new RegularFilterGroupEntity(genID());
            filterGroupDoneExpertVote.addFilter(new RegularFilterEntity(genID(),
                    new CompareFilterEntity(addPropertyObject(doneExpertVote, objExpert, objVote), Compare.EQUALS, addPropertyObject(baseLM.vtrue)),
                    "Только проголосовавшие",
                    KeyStroke.getKeyStroke(KeyEvent.VK_F10, 0)));
            addRegularFilterGroup(filterGroupDoneExpertVote);

            RegularFilterGroupEntity filterGroupExpertVote = new RegularFilterGroupEntity(genID());
            filterGroupExpertVote.addFilter(new RegularFilterEntity(genID(),
                    new NotFilterEntity(new NotNullFilterEntity(addPropertyObject(voteResultExpertVote, objExpert, objVote))),
                    "Только не принявшие участие",
                    KeyStroke.getKeyStroke(KeyEvent.VK_F9, 0)));
            addRegularFilterGroup(filterGroupExpertVote);
        }

        @Override
        public FormView createDefaultRichDesign() {
            DefaultFormView design = (DefaultFormView) super.createDefaultRichDesign();

            design.setPanelLabelAbove(voteResultCommentGroup, true);
            design.setConstraintsFillHorizontal(voteResultCommentGroup, 0.5);

            return design;
        }
    }

    private class DocumentTemplateFormEntity extends FormEntity<SkolkovoBusinessLogics> {

        private DocumentTemplateFormEntity(NavigatorElement parent, String sID) {
            super(parent, sID, "Шаблоны документов");

            ObjectEntity objDocumentTemplate = addSingleGroupObject(documentTemplate, "Шаблон", baseLM.name);
            addObjectActions(this, objDocumentTemplate);

            ObjectEntity objDocumentTemplateDetail = addSingleGroupObject(documentTemplateDetail, nameTypeDocument, nameLanguageDocument);
            addObjectActions(this, objDocumentTemplateDetail);

            addFixedFilter(new CompareFilterEntity(addPropertyObject(documentTemplateDocumentTemplateDetail, objDocumentTemplateDetail), Compare.EQUALS, objDocumentTemplate));
        }
    }

    private class ClaimerFormEntity extends FormEntity<SkolkovoBusinessLogics> {
        public ObjectEntity objClaimer;

        private ClaimerFormEntity(NavigatorElement parent, String sID) {
            super(parent, sID, "Реестр заявителей");

            objClaimer = addSingleGroupObject(claimer, "Заявитель", claimerInformationGroup, contactGroup, editClaimer);
        }
    }


    private class ClaimerFullFormEntity extends ClassFormEntity<SkolkovoBusinessLogics> {
        public ObjectEntity objClaimer;

        private ClaimerFullFormEntity(NavigatorElement parent, String sID) {
            super(parent, sID, "Заявители");

            objClaimer = addSingleGroupObject(claimer, "Заявитель", claimerInformationGroup, contactGroup, documentGroup, legalDataGroup);
            objClaimer.groupTo.setSingleClassView(ClassViewType.PANEL);
            editClaimer = addMFAProp(actionGroup, "Редактировать", this, new ObjectEntity[]{objClaimer}).setImage("edit.png");
            editClaimerProject = addJProp(actionGroup, true, "Редактировать юр.лицо", editClaimer, claimerProject, 1);
        }

        @Override
        public ObjectEntity getObject() {
            return objClaimer;
        }
    }

    private class LanguageDocumentTypeFormEntity extends FormEntity<SkolkovoBusinessLogics> { // письмо эксперту
        private ObjectEntity objLanguage;
        private ObjectEntity objDocumentType;

        private GroupObjectEntity gobjLanguageDocumentType;

        private LanguageDocumentTypeFormEntity(NavigatorElement parent, String sID) {
            super(parent, sID, "Обязательные документы");

            gobjLanguageDocumentType = new GroupObjectEntity(genID());
            objLanguage = new ObjectEntity(genID(), language, "Язык");
            objDocumentType = new ObjectEntity(genID(), documentType, "Тип документа");
            gobjLanguageDocumentType.add(objLanguage);
            gobjLanguageDocumentType.add(objDocumentType);
            addGroup(gobjLanguageDocumentType);

            addPropertyDraw(objLanguage, baseLM.name);
            addPropertyDraw(objDocumentType, baseLM.name);
            addPropertyDraw(objLanguage, objDocumentType, quantityMinLanguageDocumentType, quantityMaxLanguageDocumentType, translateLanguageDocumentType);
        }
    }

    private class ExpertLetterFormEntity extends FormEntity<SkolkovoBusinessLogics> { // письмо эксперту
        private ObjectEntity objExpert;
        private ObjectEntity objVote;

        private ObjectEntity objDocument;

        private GroupObjectEntity gobjExpertVote;

        private ExpertLetterFormEntity(NavigatorElement parent, String sID) {
            super(parent, sID, "Письмо о заседании", true);

            gobjExpertVote = new GroupObjectEntity(2, "expertVote");
            objExpert = new ObjectEntity(2, "expert", expert, "Эксперт");
            objVote = new ObjectEntity(3, "vote", vote, "Заседание");
            gobjExpertVote.add(objExpert);
            gobjExpertVote.add(objVote);
            addGroup(gobjExpertVote);
            gobjExpertVote.initClassView = ClassViewType.PANEL;

            addPropertyDraw(baseLM.webHost, gobjExpertVote);
            addPropertyDraw(requiredPeriod, gobjExpertVote);
            addPropertyDraw(objExpert, baseLM.name, documentNameExpert, isForeignExpert, localeExpert);
            addPropertyDraw(objVote, nameNativeClaimerVote, nameForeignClaimerVote, nameNativeProjectVote, nameForeignProjectVote);
            addFixedFilter(new NotNullFilterEntity(addPropertyObject(inNewExpertVote, objExpert, objVote)));

            //!!!
            //dateStartVote = addJProp(baseGroup, "dateStartVote", "Дата начала", baseLM.and1, date, 1, is(vote), 1);
            //LP isDocumentUnique = addJProp(baseGroup,"isDocumentUnique", "уникальность док-та", baseLM.and1, languageDocument, is(), objDocument);

            objDocument = addSingleGroupObject(8, "document", document, fileDocument);
            addPropertyDraw(nameDocument, objDocument).setSID("docName");
            addFixedFilter(new CompareFilterEntity(addPropertyObject(projectDocument, objDocument), Compare.EQUALS, addPropertyObject(projectVote, objVote)));
            addFixedFilter(new OrFilterEntity(new NotNullFilterEntity(addPropertyObject(inDocumentExpert, objDocument, objExpert)),
                    new NotNullFilterEntity(addPropertyObject(inDefaultDocumentExpert, objDocument, objExpert))));

            addInlineEAForm(emailLetterExpertVoteEA, this, objExpert, 1, objVote, 2);
        }

        @Override
        public void modifyHierarchy(GroupObjectHierarchy groupHierarchy) {
            groupHierarchy.markGroupAsNonJoinable(gobjExpertVote);
        }

        @Override
        public boolean isReadOnly() {
            return true;
        }
    }

    private class ExpertAuthFormEntity extends FormEntity<SkolkovoBusinessLogics> { // письмо эксперту о логине
        private ObjectEntity objExpert;

        private ExpertAuthFormEntity(NavigatorElement parent, String sID) {
            super(parent, sID, "Аутентификация эксперта", true);

            objExpert = addSingleGroupObject(1, "expert", expert, baseLM.userLogin, baseLM.userPassword, baseLM.name, documentNameExpert, isForeignExpert);
            objExpert.groupTo.initClassView = ClassViewType.PANEL;

            addInlineEAForm(emailAuthExpertEA, this, objExpert, 1);
        }

        @Override
        public boolean isReadOnly() {
            return true;
        }
    }

    private class VoteStartFormEntity extends FormEntity<SkolkovoBusinessLogics> { // письмо эксперту

        private ObjectEntity objVote;

        private ObjectEntity objExpert;
        private ObjectEntity objOldExpert;
        private ObjectEntity objPrevVote;

        private VoteStartFormEntity(NavigatorElement parent, String sID) {
            super(parent, sID, "Созыв заседания", true);

            objVote = addSingleGroupObject(1, "vote", vote, baseLM.date, dateProjectVote, nameNativeClaimerVote, nameNativeProjectVote, nameAblateClaimerVote, prevDateStartVote, prevDateVote, quantityInVote, quantityInOldVote, countPrevVote);
            objVote.groupTo.initClassView = ClassViewType.PANEL;

            objExpert = addSingleGroupObject(2, "expert", expert, baseLM.userLastName, baseLM.userFirstName, documentNameExpert);
            addPropertyDraw(numberNewExpertVote, objExpert, objVote);
            addFixedFilter(new NotNullFilterEntity(addPropertyObject(inNewExpertVote, objExpert, objVote)));

            objOldExpert = addSingleGroupObject(3, "oldexpert", expert, baseLM.userLastName, baseLM.userFirstName, documentNameExpert);
            addPropertyDraw(numberOldExpertVote, objOldExpert, objVote);
            addFixedFilter(new NotNullFilterEntity(addPropertyObject(inOldExpertVote, objOldExpert, objVote)));

            objPrevVote = addSingleGroupObject(4, "prevVote", vote, dateEndVote);
            addFixedFilter(new NotNullFilterEntity(addPropertyObject(isPrevVoteVote, objPrevVote, objVote)));

            addAttachEAForm(emailStartVoteEA, this, EmailActionProperty.Format.PDF, objVote, 1);
            addAttachEAForm(emailClosedVoteEA, this, EmailActionProperty.Format.PDF, emailStartHeaderVote, objVote, 1);

            addDefaultHintsIncrementTable(this);

            voteStartFormVote = addFAProp("Созыв заседания", this, objVote);
        }
        @Override
        public void modifyHierarchy(GroupObjectHierarchy groupHierarchy) {
            groupHierarchy.markGroupAsNonJoinable(objVote.groupTo);
        }

        @Override
        public boolean isReadOnly() {
            return true;
        }
    }

    private class VoteProtocolFormEntity extends FormEntity<SkolkovoBusinessLogics> { // письмо эксперту

        private ObjectEntity objVote;
        private ObjectEntity objPrevVote;
        private ObjectEntity objExpert;

        private VoteProtocolFormEntity(NavigatorElement parent, String sID) {
            super(parent, sID, "Протокол заседания", true);

            objVote = addSingleGroupObject(1, "vote", vote, dateProjectVote, baseLM.date, dateEndVote, nameNativeProjectVote, nameNativeClaimerVote, nameNativeClusterVote,
                    quantityInVote, quantityRepliedVote, quantityDoneVote, quantityDoneNewVote, quantityDoneOldVote, quantityRefusedVote, quantityConnectedVote, succeededVote, acceptedVote,
                    quantityInClusterVote, acceptedInClusterVote, quantityInnovativeVote, acceptedInnovativeVote, quantityForeignVote, acceptedForeignVote, prevDateStartVote, prevDateVote, countPrevVote);
            objVote.groupTo.initClassView = ClassViewType.PANEL;

            objPrevVote = addSingleGroupObject(5, "prevVote", vote, dateStartVote);

            addFixedFilter(new NotNullFilterEntity(addPropertyObject(closedVote, objVote)));

            objExpert = addSingleGroupObject(12, "expert", expert, baseLM.userFirstName, baseLM.userLastName, documentNameExpert);

            addPropertyDraw(voteResultGroup, true, objExpert, objVote);

            addPropertyDraw(connectedExpertVote, objExpert, objVote);

            addFixedFilter(new NotNullFilterEntity(addPropertyObject(isPrevVoteVote, objPrevVote, objVote)));
            addFixedFilter(new OrFilterEntity(new NotNullFilterEntity(addPropertyObject(doneExpertVote, objExpert, objVote)),
                    new NotNullFilterEntity(addPropertyObject(connectedExpertVote, objExpert, objVote))));

            addAttachEAForm(emailProtocolVoteEA, this, EmailActionProperty.Format.PDF, objVote, 1);
            addAttachEAForm(emailClosedVoteEA, this, EmailActionProperty.Format.PDF, emailProtocolHeaderVote, objVote, 1);

            addDefaultHintsIncrementTable(this);

            voteProtocolFormVote = addFAProp("Протокол заседания", this, objVote);
        }

        @Override
        public void modifyHierarchy(GroupObjectHierarchy groupHierarchy) {
            groupHierarchy.markGroupAsNonJoinable(objVote.groupTo);
        }

        @Override
        public boolean isReadOnly() {
            return true;
        }
    }

    private class ExpertProtocolFormEntity extends FormEntity<SkolkovoBusinessLogics> { // письмо эксперту

        private ObjectEntity objDateFrom;
        private ObjectEntity objDateTo;

        private ObjectEntity objExpert;

        private ObjectEntity objVoteHeader;
        private ObjectEntity objVote;

        private ExpertProtocolFormEntity(NavigatorElement parent, String sID) {
            super(parent, sID, "Протокол голосования экспертов", true);

            GroupObjectEntity gobjDates = new GroupObjectEntity(1, "date");
            objDateFrom = new ObjectEntity(2, DateClass.instance, "Дата (с)");
            objDateTo = new ObjectEntity(3, DateClass.instance, "Дата (по)");
            gobjDates.add(objDateFrom);
            gobjDates.add(objDateTo);

            addGroup(gobjDates);
            gobjDates.setSingleClassView(ClassViewType.PANEL);

            addPropertyDraw(objDateFrom, baseLM.objectValue);
            getPropertyDraw(baseLM.objectValue, objDateFrom).setSID("dateFrom");

            // так делать неправильно в общем случае, поскольку getPropertyDraw ищет по groupObject, а не object

            addPropertyDraw(objDateTo, baseLM.objectValue);
            getPropertyDraw(baseLM.objectValue, objDateTo).setSID("dateTo");

            objExpert = addSingleGroupObject(4, "expert", expert, baseLM.userFirstName, baseLM.userLastName, documentNameExpert, nameNativeClusterExpert);
            objExpert.groupTo.initClassView = ClassViewType.PANEL;

            addPropertyDraw(objExpert, objDateFrom, objDateTo, quantityDoneExpertDateFromDateTo, quantityInExpertDateFromDateTo);

            objVoteHeader = addSingleGroupObject(5, "voteHeader", vote);

            objVote = addSingleGroupObject(6, "vote", vote, dateProjectVote, baseLM.date, dateEndVote, nameNativeProjectVote, nameNativeClaimerVote, nameNativeClusterVote);

            addPropertyDraw(nameNativeClaimerVote, objVoteHeader).setSID("nameNativeClaimerVoteHeader");
            addPropertyDraw(nameNativeProjectVote, objVoteHeader);

            addPropertyDraw(voteResultGroup, true, objExpert, objVote);

            addPropertyDraw(connectedExpertVote, objExpert, objVote);

            addFixedFilter(new CompareFilterEntity(addPropertyObject(dateExpertVote, objExpert, objVoteHeader), Compare.GREATER_EQUALS, objDateFrom));
            addFixedFilter(new CompareFilterEntity(addPropertyObject(dateExpertVote, objExpert, objVoteHeader), Compare.LESS_EQUALS, objDateTo));
            addFixedFilter(new NotNullFilterEntity(addPropertyObject(doneNewExpertVote, objExpert, objVoteHeader)));

            addFixedFilter(new CompareFilterEntity(addPropertyObject(dateExpertVote, objExpert, objVote), Compare.GREATER_EQUALS, objDateFrom));
            addFixedFilter(new CompareFilterEntity(addPropertyObject(dateExpertVote, objExpert, objVote), Compare.LESS_EQUALS, objDateTo));
            addFixedFilter(new NotNullFilterEntity(addPropertyObject(doneNewExpertVote, objExpert, objVote)));

            RegularFilterGroupEntity filterGroupExpertVote = new RegularFilterGroupEntity(genID());
            filterGroupExpertVote.addFilter(new RegularFilterEntity(genID(),
                    new NotNullFilterEntity(addPropertyObject(quantityDoneExpertDateFromDateTo, objExpert, objDateFrom, objDateTo)),
                    "Голосовавшие",
                    KeyStroke.getKeyStroke(KeyEvent.VK_F9, 0)), true);
            filterGroupExpertVote.addFilter(new RegularFilterEntity(genID(),
                    new NotNullFilterEntity(addPropertyObject(quantityInExpertDateFromDateTo, objExpert, objDateFrom, objDateTo)),
                    "Учавствовавшие",
                    KeyStroke.getKeyStroke(KeyEvent.VK_F8, 0)));
            addRegularFilterGroup(filterGroupExpertVote);

            setReadOnly(true);
            setReadOnly(false, objDateFrom.groupTo);
        }
    }

    private class VoteClaimerFormEntity extends FormEntity<SkolkovoBusinessLogics> {

        private ObjectEntity objVote;

        private VoteClaimerFormEntity(NavigatorElement parent, String sID, String caption) {
            super(parent, sID, caption, true);

            objVote = addSingleGroupObject(1, "vote", vote, "Заседание", nameNativeClusterVote, prevDateVote, nameNativePrevClusterVote);
            objVote.groupTo.initClassView = ClassViewType.PANEL;

            addInlineEAForm(emailClaimerVoteEA, this, objVote, 1);
        }
    }

    private class NoticeRejectedFormEntity extends FormEntity<SkolkovoBusinessLogics> {

        private ObjectEntity objVote;

        private NoticeRejectedFormEntity(NavigatorElement parent, String sID, String caption) {
            super(parent, sID, caption, true);

            objVote = addSingleGroupObject(1, "vote", vote, "Заседание", nameNativeClusterVote, prevDateVote, nameNativePrevClusterVote, nameNativeFinalClusterProjectVote, isLastClusterVote,
                    quantityDoneVote, quantityInClusterVote, acceptedInClusterVote, quantityInnovativeVote, acceptedInnovativeVote, quantityForeignVote, acceptedForeignVote);
            objVote.groupTo.initClassView = ClassViewType.PANEL;

            addFixedFilter(new NotNullFilterEntity(addPropertyObject(closedRejectedVote, objVote)));

            addInlineEAForm(emailNoticeRejectedVoteEA, this, objVote, 1);
        }
    }

    private class NoticeAcceptedStatusFormEntity extends FormEntity<SkolkovoBusinessLogics> {

        private ObjectEntity objVote;

        private NoticeAcceptedStatusFormEntity(NavigatorElement parent, String sID, String caption) {
            super(parent, sID, caption, true);

            objVote = addSingleGroupObject(1, "vote", vote, "Заседание", nameNativeProjectVote);
            objVote.groupTo.initClassView = ClassViewType.PANEL;

            addFixedFilter(new NotNullFilterEntity(addPropertyObject(closedAcceptedStatusVote, objVote)));

            addInlineEAForm(emailNoticeAcceptedStatusVoteEA, this, objVote, 1);
        }
    }

    private class NoticeAcceptedPreliminaryFormEntity extends FormEntity<SkolkovoBusinessLogics> {

        private ObjectEntity objVote;

        private NoticeAcceptedPreliminaryFormEntity(NavigatorElement parent, String sID, String caption) {
            super(parent, sID, caption, true);

            objVote = addSingleGroupObject(1, "vote", vote, "Заседание", nameNativeProjectVote, fileDecisionVote);
            objVote.groupTo.initClassView = ClassViewType.PANEL;

            addFixedFilter(new NotNullFilterEntity(addPropertyObject(closedAcceptedPreliminaryVote, objVote)));
            addFixedFilter(new NotNullFilterEntity(addPropertyObject(fileDecisionVote, objVote)));

            addInlineEAForm(emailNoticeAcceptedPreliminaryVoteEA, this, objVote, 1);
        }
    }

    private class AcceptanceCertificateFormEntity extends FormEntity<SkolkovoBusinessLogics> {
        private ObjectEntity objExpert;
        private ObjectEntity objVote;
        private ObjectEntity objVoteBallot;
        private ObjectEntity objYear;
        private ObjectEntity objMonth;


        private AcceptanceCertificateFormEntity(NavigatorElement parent, String sID, String caption, boolean resident) {
            super(parent, sID, caption);

            objYear = addSingleGroupObject(1, "year", IntegerClass.instance, "Год");
            objYear.groupTo.setSingleClassView(ClassViewType.PANEL);
            addPropertyDraw(objYear, baseLM.objectValue);
            getPropertyDraw(baseLM.objectValue, objYear).setSID("year");

            objMonth = addSingleGroupObject(2, "month", IntegerClass.instance, "Месяц");
            objMonth.groupTo.setSingleClassView(ClassViewType.PANEL);
            addPropertyDraw(objMonth, baseLM.objectValue);
            getPropertyDraw(baseLM.objectValue, objMonth).setSID("month");

            objExpert = addSingleGroupObject(3, "expert", expert, baseLM.selection, baseLM.userFirstName, baseLM.userLastName, documentNameExpert, dateAgreementExpert, nameCountryExpert, caseCountryExpert, englCountryExpert, nameCurrencyExpert, baseCurrencyExpert, englCurrencyExpert, pluralCurrencyExpert, nameNativeClusterExpert, nameLanguageExpert, residencyCountryExpert);
            //   objExpert.groupTo.initClassView = ClassViewType.PANEL;

            objVoteBallot = addSingleGroupObject(4, "voteBallot", vote, dateProjectVote, baseLM.date, dateEndVote, nameNativeProjectVote, nameNativeClaimerVote, nameNativeClusterVote);

            addPropertyDraw(voteResultGroup, true, objExpert, objVoteBallot);

            objVote = addSingleGroupObject(5, "vote", vote, nameNativeClaimerVote, nameForeignClaimerVote, openedVote, succeededVote, quantityDoneVote);
            addPropertyDraw(inNewExpertVote, objExpert, objVote);
            addPropertyDraw(objExpert, objVote, objMonth, objYear, doneExpertVoteMonthYear);

            //  addPropertyDraw(voteResultGroup, true, objExpert, objVote);
            addPropertyDraw(objExpert, objMonth, objYear, quantityDoneExpertMonthYear, moneyQuantityDoneExpertMonthYear);
            if (resident)
                addFixedFilter(new NotNullFilterEntity(addPropertyObject(residencyCountryExpert, objExpert)));

            if (!resident)
                addFixedFilter(new NotFilterEntity(new NotNullFilterEntity(addPropertyObject(residencyCountryExpert, objExpert))));

            addFixedFilter(new NotNullFilterEntity(addPropertyObject(quantityDoneExpertMonthYear, objExpert, objMonth, objYear)));

            addFixedFilter(new NotNullFilterEntity(addPropertyObject(doneExpertVoteMonthYear, objExpert, objVote, objMonth, objYear)));
            addFixedFilter(new NotNullFilterEntity(addPropertyObject(inNewExpertVote, objExpert, objVote)));

            addFixedFilter(new NotNullFilterEntity(addPropertyObject(doneExpertVoteMonthYear, objExpert, objVoteBallot, objMonth, objYear)));
            addFixedFilter(new NotNullFilterEntity(addPropertyObject(inNewExpertVote, objExpert, objVoteBallot)));

            addAttachEAForm(emailLetterExpertMonthYearEA, this, EmailActionProperty.Format.PDF, objMonth, 1, objYear, 2);
            //     setPageSize(0);
        }

        @Override
        public FormView createDefaultRichDesign() {
            DefaultFormView design = (DefaultFormView) super.createDefaultRichDesign();

            design.defaultOrders.put(design.get(getPropertyDraw(nameNativeClusterExpert)), true);
            design.defaultOrders.put(design.get(getPropertyDraw(documentNameExpert)), true);

            return design;
        }
    }

    private class ClaimerAcceptedFormEntity extends FormEntity<SkolkovoBusinessLogics> {

        private ObjectEntity objVote;

        private ClaimerAcceptedFormEntity(NavigatorElement parent, String sID) {
            super(parent, sID, "Решение о соответствии", true);

            objVote = addSingleGroupObject(genID(), "vote", vote, "Заседание", dateEndVote, nameNativeProjectVote, dateProjectVote, nameNativeClaimerVote, nameAblateClaimerVote, nameNativeClusterVote, isStatusVote);
            objVote.groupTo.initClassView = ClassViewType.PANEL;

            addFixedFilter(new NotNullFilterEntity(addPropertyObject(succeededVote, objVote)));
            addFixedFilter(new NotNullFilterEntity(addPropertyObject(acceptedVote, objVote)));

            addDefaultHintsIncrementTable(this);

            addAttachEAForm(emailClosedVoteEA, this, EmailActionProperty.Format.PDF, emailClaimerAcceptedHeaderVote, objVote, 1);
        }
    }

    private class ClaimerRejectedFormEntity extends FormEntity<SkolkovoBusinessLogics> {

        private ObjectEntity objVote;

        private ClaimerRejectedFormEntity(NavigatorElement parent, String sID) {
            super(parent, sID, "Решение о несоответствии", true);

            objVote = addSingleGroupObject(genID(), "vote", vote, "Заседание", dateEndVote, nameNativeProjectVote, dateProjectVote, nameNativeClaimerVote, nameAblateClaimerVote, nameDativusClaimerVote, nameGenitiveClaimerVote, isLastClusterVote, nameNativeClusterVote, isStatusVote);
            objVote.groupTo.initClassView = ClassViewType.PANEL;

            addFixedFilter(new NotNullFilterEntity(addPropertyObject(succeededVote, objVote)));
            addFixedFilter(new NotFilterEntity(new NotNullFilterEntity(addPropertyObject(acceptedVote, objVote))));

            addDefaultHintsIncrementTable(this);

            addAttachEAForm(emailClosedVoteEA, this, EmailActionProperty.Format.PDF, emailClaimerRejectedHeaderVote, objVote, 1);
        }
    }

    private class ClaimerStatusFormEntity extends FormEntity<SkolkovoBusinessLogics> {

        private ObjectEntity objProject;

        private ClaimerStatusFormEntity(NavigatorElement parent, String sID) {
            super(parent, sID, "Решение о присвоении статуса участника", true);

            objProject = addSingleGroupObject(genID(), "project", project, "Проект", dateProject, nameNativeProject, nameNativeClaimerProject, nameAblateClaimerProject, nameDativusClaimerProject, nameGenitiveClaimerProject);
            objProject.groupTo.initClassView = ClassViewType.PANEL;

            addFixedFilter(new NotNullFilterEntity(addPropertyObject(acceptedProject, objProject)));

            addAttachEAForm(emailAcceptedProjectEA, this, EmailActionProperty.Format.PDF, emailClaimerAcceptedHeaderProject, objProject, 1);
        }
    }

    private class StatusFormEntity extends ClassFormEntity<SkolkovoBusinessLogics> {

        private ObjectEntity objStatus;

        private StatusFormEntity(NavigatorElement parent, String sID) {
            super(parent, sID, "Статус", true);
            objStatus = addSingleGroupObject(genID(), "Status", projectStatus, "Статус", numberProjectStatus, baseLM.name, oficialNameProjectStatus);
        }

        @Override
        public FormView createDefaultRichDesign() {
            DefaultFormView design = (DefaultFormView) super.createDefaultRichDesign();
            design.defaultOrders.put(design.get(getPropertyDraw(numberProjectStatus, objStatus)), true);
            return design;
        }

        @Override
        public ObjectEntity getObject() {
            return objStatus;
        }
    }

    public class GenerateDocumentsActionProperty extends ActionProperty {

        private final ClassPropertyInterface projectInterface;
        private final ClassPropertyInterface documentTemplateInterface;

        public GenerateDocumentsActionProperty() {
            super(genSID(), "Сгенерировать документы", new ValueClass[]{project, documentTemplate});

            Iterator<ClassPropertyInterface> i = interfaces.iterator();
            projectInterface = i.next();
            documentTemplateInterface = i.next();
        }

        public void execute(ExecutionContext context) throws SQLException {
            DataObject projectObject = context.getKeyValue(projectInterface);
            DataObject documentTemplateObject = context.getKeyValue(documentTemplateInterface);

            Query<String, String> query = new Query<String, String>(Collections.singleton("key"));
            query.and(documentTemplateDocumentTemplateDetail.getExpr(context.getModifier(), query.mapKeys.get("key")).compare(documentTemplateObject.getExpr(), Compare.EQUALS));
            query.properties.put("documentType", typeDocument.getExpr(context.getModifier(), query.mapKeys.get("key")));
            query.properties.put("languageDocument", languageDocument.getExpr(context.getModifier(), query.mapKeys.get("key")));

            for (Map<String, Object> row : query.execute(context.getSession()).values()) {
                DataObject documentObject = context.addObject(document);
                projectDocument.execute(projectObject.getValue(), context, documentObject);
                typeDocument.execute(row.get("documentType"), context, documentObject);
                languageDocument.execute(row.get("languageDocument"), context, documentObject);
            }
        }
    }


    public class IncludeDocumentsActionProperty extends ActionProperty {

        private final ClassPropertyInterface projectInterface;

        public IncludeDocumentsActionProperty() {
            super(genSID(), "Подключить документы", new ValueClass[]{project});

            projectInterface = interfaces.iterator().next();
        }


        public void execute(Map<ClassPropertyInterface, DataObject> keys, ObjectValue value, List<ClientAction> actions, RemoteForm executeForm, Map<ClassPropertyInterface, PropertyObjectInterfaceInstance> mapObjects) throws SQLException {
            throw new RuntimeException("no need");
        }

        @Override
        public void execute(ExecutionContext context) throws SQLException {

            try {

                DataObject projectObject = context.getKeyValue(projectInterface);
                DataObject documentObject;

                Object file = fileNativeApplicationFormProject.read(context, projectObject);
                documentObject = context.addObject(document);
                projectDocument.execute(projectObject.getValue(), context, documentObject);
                typeDocument.execute(documentType.getID("application"), context, documentObject);
                languageDocument.execute(language.getID("russian"), context, documentObject);
                if (file != null)
                    fileDocument.execute(file, context, documentObject);
                else if ((fillNativeProject.read(context, projectObject)) == (Object) true || (translatedToRussianProject.read(context, projectObject)) == (Object) true)
                    fileDocument.execute(generateApplicationFile(context, projectObject, false), context, documentObject);

                file = fileForeignApplicationFormProject.read(context, projectObject);
                documentObject = context.addObject(document);
                projectDocument.execute(projectObject.getValue(), context, documentObject);
                typeDocument.execute(documentType.getID("application"), context, documentObject);
                languageDocument.execute(language.getID("english"), context, documentObject);
                if (file != null)
                    fileDocument.execute(file, context, documentObject);
                else if ((fillForeignProject.read(context, projectObject)) == (Object) true || (translatedToEnglishProject.read(context, projectObject)) == (Object) true)
                    fileDocument.execute(generateApplicationFile(context, projectObject, true), context, documentObject);

                file = fileNativeSummaryProject.read(context, projectObject);
                if (file != null) {
                    documentObject = context.addObject(document);
                    fileDocument.execute(file, context, documentObject);
                    projectDocument.execute(projectObject.getValue(), context, documentObject);
                    typeDocument.execute(documentType.getID("resume"), context, documentObject);
                    languageDocument.execute(language.getID("russian"), context, documentObject);
                }

                file = fileForeignSummaryProject.read(context, projectObject);
                if (file != null) {
                    documentObject = context.addObject(document);
                    fileDocument.execute(file, context, documentObject);
                    projectDocument.execute(projectObject.getValue(), context, documentObject);
                    typeDocument.execute(documentType.getID("resume"), context, documentObject);
                    languageDocument.execute(language.getID("english"), context, documentObject);
                }

                /*
                                Query<String, String> query = new Query<String, String>(Collections.singleton("nonRussianSpecialist"));
                                query.and(projectNonRussianSpecialist.getExpr(context.getModifier(), query.mapKeys.get("nonRussianSpecialist")).compare(projectObject.getExpr(), Compare.EQUALS));
                                query.properties.put("fullNameNonRussianSpecialist", projectNonRussianSpecialist.getExpr(context.getModifier(), query.mapKeys.get("nonRussianSpecialist")));
                                query.properties.put("fileForeignResumeNonRussianSpecialist", fileForeignResumeNonRussianSpecialist.getExpr(context.getModifier(), query.mapKeys.get("nonRussianSpecialist")));
                                int count = 1;
                                int size = query.executeClasses(context.getSession(), baseClass).entrySet().size();
                                for (Map.Entry<Map<String, DataObject>, Map<String, ObjectValue>> row : query.executeClasses(context.getSession(), baseClass).entrySet()) {
                                    row.getKey().get("nonRussianSpecialist");
                                    row.getValue().get("fullNameNonRussianSpecialist");
                                    row.getValue().get("fileForeignResumeNonRussianSpecialist");
                                    documentObject = context.addObject(document);
                                    projectDocument.execute(projectObject.getValue(), context, documentObject);
                                    typeDocument.execute(documentType.getID("forres"), context, documentObject);
                                    languageDocument.execute(language.getID("english"), context, documentObject);
                                    if (size > 1)
                                        postfixDocument.execute(String.valueOf(count), context, documentObject);
                                    fileDocument.execute(row.getValue().get("fileForeignResumeNonRussianSpecialist").getValue(), context, documentObject);
                                    count++;
                                }

                                query = new Query<String, String>(Collections.singleton("nonRussianSpecialist"));
                                query.and(projectNonRussianSpecialist.getExpr(context.getModifier(), query.mapKeys.get("nonRussianSpecialist")).compare(projectObject.getExpr(), Compare.EQUALS));
                                query.properties.put("fullNameNonRussianSpecialist", projectNonRussianSpecialist.getExpr(context.getModifier(), query.mapKeys.get("nonRussianSpecialist")));
                                query.properties.put("fileNativeResumeNonRussianSpecialist", fileNativeResumeNonRussianSpecialist.getExpr(context.getModifier(), query.mapKeys.get("nonRussianSpecialist")));
                                count = 1;
                                size = query.executeClasses(context.getSession(), baseClass).entrySet().size();
                                for (Map.Entry<Map<String, DataObject>, Map<String, ObjectValue>> row : query.executeClasses(context.getSession(), baseClass).entrySet()) {
                                    row.getKey().get("nonRussianSpecialist");
                                    row.getValue().get("fullNameNonRussianSpecialist");
                                    row.getValue().get("fileNativeResumeNonRussianSpecialist");
                                    documentObject = context.addObject(document);
                                    projectDocument.execute(projectObject.getValue(), context, documentObject);
                                    typeDocument.execute(documentType.getID("forres"), context, documentObject);
                                    languageDocument.execute(language.getID("russian"), context, documentObject);
                                    if (size > 1)
                                        postfixDocument.execute(String.valueOf(count), context, documentObject);
                                    fileDocument.execute(row.getValue().get("fileNativeResumeNonRussianSpecialist").getValue(), context, documentObject);
                                    count++;
                                }

                */
                Query<String, String> query = new Query<String, String>(Collections.singleton("nonRussianSpecialist"));
                query.and(projectNonRussianSpecialist.getExpr(context.getModifier(), query.mapKeys.get("nonRussianSpecialist")).compare(projectObject.getExpr(), Compare.EQUALS));
                query.properties.put("fullNameNonRussianSpecialist", projectNonRussianSpecialist.getExpr(context.getModifier(), query.mapKeys.get("nonRussianSpecialist")));
                query.properties.put("fileNativeResumeNonRussianSpecialist", fileNativeResumeNonRussianSpecialist.getExpr(context.getModifier(), query.mapKeys.get("nonRussianSpecialist")));
                query.properties.put("fileForeignResumeNonRussianSpecialist", fileForeignResumeNonRussianSpecialist.getExpr(context.getModifier(), query.mapKeys.get("nonRussianSpecialist")));
                int countForeign = 1;
                int countNative = 1;
                int size = query.executeClasses(context.getSession(), baseClass).entrySet().size();
                for (Map.Entry<Map<String, DataObject>, Map<String, ObjectValue>> row : query.executeClasses(context.getSession(), baseClass).entrySet()) {
                    row.getKey().get("nonRussianSpecialist");
                    row.getValue().get("fullNameNonRussianSpecialist");
                    row.getValue().get("fileForeignResumeNonRussianSpecialist");
                    row.getValue().get("fileNativeResumeNonRussianSpecialist");

                    file = row.getValue().get("fileForeignResumeNonRussianSpecialist").getValue();
                    if (file != null) {
                        documentObject = context.addObject(document);
                        projectDocument.execute(projectObject.getValue(), context, documentObject);
                        typeDocument.execute(documentType.getID("forres"), context, documentObject);
                        languageDocument.execute(language.getID("english"), context, documentObject);
                        if (size > 1)
                            postfixDocument.execute(String.valueOf(countForeign), context, documentObject);
                        fileDocument.execute(file, context, documentObject);
                        countForeign++;
                    }

                    file = row.getValue().get("fileNativeResumeNonRussianSpecialist").getValue();
                    if (file != null) {
                        documentObject = context.addObject(document);
                        projectDocument.execute(projectObject.getValue(), context, documentObject);
                        typeDocument.execute(documentType.getID("forres"), context, documentObject);
                        languageDocument.execute(language.getID("russian"), context, documentObject);
                        if (size > 1)
                            postfixDocument.execute(String.valueOf(countNative), context, documentObject);
                        fileDocument.execute(file, context, documentObject);
                        countNative++;
                    }
                }

                file = fileNativeTechnicalDescriptionProject.read(context, projectObject);
                if (file != null) {
                    documentObject = context.addObject(document);
                    projectDocument.execute(projectObject.getValue(), context, documentObject);
                    typeDocument.execute(documentType.getID("techdesc"), context, documentObject);
                    languageDocument.execute(language.getID("russian"), context, documentObject);
                    fileDocument.execute(file, context, documentObject);
                }

                file = fileForeignTechnicalDescriptionProject.read(context, projectObject);
                if (file != null) {
                    documentObject = context.addObject(document);
                    projectDocument.execute(projectObject.getValue(), context, documentObject);
                    typeDocument.execute(documentType.getID("techdesc"), context, documentObject);
                    languageDocument.execute(language.getID("english"), context, documentObject);
                    fileDocument.execute(file, context, documentObject);
                }

                file = fileNativeRoadMapProject.read(context, projectObject);
                if (file != null) {
                    documentObject = context.addObject(document);
                    projectDocument.execute(projectObject.getValue(), context, documentObject);
                    typeDocument.execute(documentType.getID("roadmap"), context, documentObject);
                    languageDocument.execute(language.getID("russian"), context, documentObject);
                    fileDocument.execute(file, context, documentObject);
                }

                file = fileForeignRoadMapProject.read(context, projectObject);
                if (file != null) {
                    documentObject = context.addObject(document);
                    projectDocument.execute(projectObject.getValue(), context, documentObject);
                    typeDocument.execute(documentType.getID("roadmap"), context, documentObject);
                    languageDocument.execute(language.getID("english"), context, documentObject);
                    fileDocument.execute(file, context, documentObject);
                }

                file = fileResolutionIPProject.read(context, projectObject);
                if (file != null) {
                    documentObject = context.addObject(document);
                    projectDocument.execute(projectObject.getValue(), context, documentObject);
                    typeDocument.execute(documentType.getID("ipres"), context, documentObject);
                    languageDocument.execute(language.getID("english"), context, documentObject);
                    fileDocument.execute(file, context, documentObject);
                }

            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (JRException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (ClassNotFoundException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
    }

    public byte[] generateApplicationFile(ExecutionContext context, DataObject project, boolean foreign) throws IOException, ClassNotFoundException, JRException {

        ProjectFullFormEntity applicationForm = foreign ? projectFullForeign : projectFullNative;

        RemoteFormInterface remoteForm = context.getRemoteForm().createForm(applicationForm, Collections.singletonMap(applicationForm.objProject, project));

        ReportGenerator report = new ReportGenerator(remoteForm);
        JasperPrint print = report.createReport(false, false, new HashMap());

        File tempFile = File.createTempFile("lsfReport", ".pdf");

        JRAbstractExporter exporter = new JRPdfExporter();
        exporter.setParameter(JRExporterParameter.OUTPUT_FILE_NAME, tempFile.getAbsolutePath());
        exporter.setParameter(JRExporterParameter.JASPER_PRINT, print);
        exporter.exportReport();

        return IOUtils.getFileBytes(tempFile);
    }

    public class GenerateVoteActionProperty extends ActionProperty {

        private final ClassPropertyInterface projectInterface;

        public GenerateVoteActionProperty() {
            super(genSID(), "Сгенерировать заседание", new ValueClass[]{project});

            Iterator<ClassPropertyInterface> i = interfaces.iterator();
            projectInterface = i.next();
        }

        public void execute(ExecutionContext context) throws SQLException {
            DataObject projectObject = context.getKeyValue(projectInterface);

            // считываем всех экспертов, которые уже голосовали по проекту
            Query<String, String> query = new Query<String, String>(Collections.singleton("key"));
            query.and(doneProjectExpert.getExpr(context.getModifier(), projectObject.getExpr(), query.mapKeys.get("key")).getWhere());
            query.and(inClusterExpert.getExpr(context.getModifier(), currentClusterProject.getExpr(context.getModifier(), projectObject.getExpr()), query.mapKeys.get("key")).getWhere());
            query.properties.put("vote", voteProjectExpert.getExpr(context.getModifier(), projectObject.getExpr(), query.mapKeys.get("key")));

            Map<DataObject, DataObject> previousResults = new HashMap<DataObject, DataObject>();
            for (Map.Entry<Map<String, DataObject>, Map<String, ObjectValue>> row : query.executeClasses(context.getSession(), baseClass).entrySet()) {
                previousResults.put(row.getKey().get("key"), (DataObject) row.getValue().get("vote"));
            }

            // считываем всех неголосовавших экспертов из этого кластера
            query = new Query<String, String>(Collections.singleton("key"));
            query.and(inClusterExpert.getExpr(context.getModifier(), currentClusterProject.getExpr(context.getModifier(), projectObject.getExpr()), query.mapKeys.get("key")).getWhere());
            query.and(disableExpert.getExpr(context.getModifier(), query.mapKeys.get("key")).getWhere().not());
            query.and(voteResultProjectExpert.getExpr(context.getModifier(), projectObject.getExpr(), query.mapKeys.get("key")).getWhere().not());

            query.properties.put("in", inProjectExpert.getExpr(context.getModifier(), projectObject.getExpr(), query.mapKeys.get("key")));

            // получаем два списка - один, которые уже назначались на проект, другой - которые нет
            java.util.List<DataObject> expertNew = new ArrayList<DataObject>();
            java.util.List<DataObject> expertVoted = new ArrayList<DataObject>();
            for (Map.Entry<Map<String, DataObject>, Map<String, ObjectValue>> row : query.executeClasses(context.getSession(), baseClass).entrySet()) {
                if (row.getValue().get("in").getValue() != null) // эксперт уже голосовал
                    expertVoted.add(row.getKey().get("key"));
                else
                    expertNew.add(row.getKey().get("key"));
            }

            Integer required = nvl((Integer) requiredQuantity.read(context), 0) - previousResults.size();
            if (required > expertVoted.size() + expertNew.size()) {
                context.addAction(new MessageClientAction("Недостаточно экспертов по кластеру", "Генерация заседания"));
                return;
            }

            // создаем новое заседание
            DataObject voteObject = context.addObject(vote);
            projectVote.execute(projectObject.object, context, voteObject);
            clusterVote.execute(currentClusterProject.read(context, projectObject), context, voteObject);
            projectActionVote.execute(projectActionProject.read(context, projectObject), context, voteObject);

            // копируем результаты старых заседаний
            for (Map.Entry<DataObject, DataObject> row : previousResults.entrySet()) {
                inExpertVote.execute(true, context, row.getKey(), voteObject);
                oldExpertVote.execute(true, context, row.getKey(), voteObject);
                LP[] copyProperties = new LP[]{dateExpertVote, voteResultExpertVote, inClusterExpertVote,
                        innovativeExpertVote, foreignExpertVote, innovativeCommentExpertVote,
                        competentExpertVote, completeExpertVote, completeCommentExpertVote};
                for (LP property : copyProperties) {
                    property.execute(property.read(context, row.getKey(), row.getValue()), context, row.getKey(), voteObject);
                }
            }

            // назначаем новых экспертов - сначала, которые не голосовали еще, а затем остальных
            Random rand = new Random();
            while (required > 0) {
                if (!expertNew.isEmpty())
                    inExpertVote.execute(true, context, expertNew.remove(rand.nextInt(expertNew.size())), voteObject);
                else
                    inExpertVote.execute(true, context, expertVoted.remove(rand.nextInt(expertVoted.size())), voteObject);
                required--;
            }
        }

        @Override
        public Set<Property> getChangeProps() {
            return BaseUtils.toSet(projectVote.property, inExpertVote.property);
        }
    }

    public class CopyResultsActionProperty extends ActionProperty {

        private final ClassPropertyInterface voteInterface;

        public CopyResultsActionProperty() {
            super(genSID(), "Скопировать результаты из предыдущего заседания", new ValueClass[]{vote});

            Iterator<ClassPropertyInterface> i = interfaces.iterator();
            voteInterface = i.next();
        }

        public void execute(Map<ClassPropertyInterface, DataObject> keys, ObjectValue value, List<ClientAction> actions, RemoteForm executeForm, Map<ClassPropertyInterface, PropertyObjectInterfaceInstance> mapObjects) throws SQLException {
            throw new RuntimeException("no need");
        }

        @Override
        public void execute(ExecutionContext context) throws SQLException {
            DataObject voteObject = context.getKeyValue(voteInterface);
            java.sql.Date dateStart = (java.sql.Date) dateStartVote.read(context, voteObject);

            DataObject projectObject = new DataObject(projectVote.read(context, voteObject), project);
            Query<String, String> voteQuery = new Query<String, String>(Collections.singleton("vote"));
            voteQuery.and(projectVote.getExpr(context.getModifier(), voteQuery.mapKeys.get("vote")).compare(projectObject.getExpr(), Compare.EQUALS));
            voteQuery.properties.put("dateStartVote", dateStartVote.getExpr(context.getModifier(), voteQuery.mapKeys.get("vote")));

            java.sql.Date datePrev = null;
            DataObject votePrevObject = null;

            for (Map.Entry<Map<String, DataObject>, Map<String, ObjectValue>> row : voteQuery.executeClasses(context.getSession(), baseClass).entrySet()) {
                java.sql.Date dateCur = (java.sql.Date) row.getValue().get("dateStartVote").getValue();
                if (dateCur != null && dateCur.getTime() < dateStart.getTime() && (datePrev == null || dateCur.getTime() > datePrev.getTime())) {
                    datePrev = dateCur;
                    votePrevObject = row.getKey().get("vote");
                }
            }
            if (votePrevObject == null) return;

            // считываем всех экспертов, которые уже голосовали по проекту
            Query<String, String> query = new Query<String, String>(Collections.singleton("key"));
            query.and(doneExpertVote.getExpr(context.getModifier(), query.mapKeys.get("key"), votePrevObject.getExpr()).getWhere());
//            query.properties.put("expert", object(expert).getExpr(session.modifier, query.mapKeys.get("key")));

            Set<DataObject> experts = new HashSet<DataObject>();
            for (Map.Entry<Map<String, DataObject>, Map<String, ObjectValue>> row : query.executeClasses(context.getSession(), baseClass).entrySet()) {
                experts.add(row.getKey().get("key"));
            }

            // копируем результаты старых заседаний
            for (DataObject expert : experts) {
                inExpertVote.execute(true, context, expert, voteObject);
                oldExpertVote.execute(true, context, expert, voteObject);
                LP[] copyProperties = new LP[]{dateExpertVote, voteResultExpertVote, inClusterExpertVote,
                        innovativeExpertVote, foreignExpertVote, innovativeCommentExpertVote,
                        competentExpertVote, completeExpertVote, completeCommentExpertVote};
                for (LP property : copyProperties) {
                    property.execute(property.read(context, expert, votePrevObject), context, expert, voteObject);
                }
            }
        }
    }
}
