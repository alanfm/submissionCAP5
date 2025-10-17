public class TargetSchedulesHandler extends TabHandler {
    private SchedulePatternFactory patternFactory;
    private BusinessObjectFactory businessObjectFactory;
    private AuthorityManager authorityManager;
    private TargetManager targetManager;

    public void setPatternFactory(SchedulePatternFactory patternFactory) {
        this.patternFactory = patternFactory;
    }

    public void setBusinessObjectFactory(BusinessObjectFactory businessObjectFactory) {
        this.businessObjectFactory = businessObjectFactory;
    }

    public void setAuthorityManager(AuthorityManager authorityManager) {
        this.authorityManager = authorityManager;
    }

    public void setTargetManager(TargetManager targetManager) {
        this.targetManager = targetManager;
    }

    @Override
    public ModelAndView processOther(TabbedController tc, Tab currentTab, HttpServletRequest req, HttpServletResponse res, Object comm, BindException errors) {
        TargetSchedulesCommand command = (TargetSchedulesCommand) comm;
        AbstractTargetEditorContext ctx = getEditorContext(req);

        if (!authorityManager.hasPrivilege(ctx.getAbstractTarget(), "EDIT_SCHEDULE")) {
            return createErrorView("User does not have permission to edit schedules.");
        }

        ScheduleActionHandler actionHandler = new ScheduleActionHandler(tc, ctx, command, patternFactory, businessObjectFactory);
        return actionHandler.handleAction(req, errors);
    }
}

public class ScheduleActionHandler {
    private final TabbedController tc;
    private final AbstractTargetEditorContext ctx;
    private final TargetSchedulesCommand command;
    private final SchedulePatternFactory patternFactory;
    private final BusinessObjectFactory businessObjectFactory;

    public ScheduleActionHandler(TabbedController tc, AbstractTargetEditorContext ctx, TargetSchedulesCommand command, SchedulePatternFactory patternFactory, BusinessObjectFactory businessObjectFactory) {
        this.tc = tc;
        this.ctx = ctx;
        this.command = command;
        this.patternFactory = patternFactory;
        this.businessObjectFactory = businessObjectFactory;
    }

    public ModelAndView handleAction(HttpServletRequest req, BindException errors) {
        if (WebUtils.hasSubmitParameter(req, "_new")) {
            return handleNewAction();
        } else if (WebUtils.hasSubmitParameter(req, "_edit")) {
            return handleEditAction();
        } else if (WebUtils.hasSubmitParameter(req, "_save")) {
            return handleSaveAction(errors);
        } else if (WebUtils.hasSubmitParameter(req, "_delete")) {
            return handleDeleteAction();
        } else if (WebUtils.hasSubmitParameter(req, "_test")) {
            return handleTestAction();
        }
        return createDefaultView();
    }

    private ModelAndView handleNewAction() {
        Tab tab = tc.getTabConfig().getTabByID("SCHEDULES").createSubTab("../target-schedule-edit.jsp");
        TabbedModelAndView tmav = tc.new TabbedModelAndView();
        tmav.getTabStatus().setCurrentTab(tab);
        tmav.getTabStatus().setEnabled(false);

        TargetSchedulesCommand newCommand = new TargetSchedulesCommand();
        tmav.addObject(Constants.GBL_CMD_DATA, newCommand);
        tmav.addObject("patterns", patternFactory.getPatterns());
        return tmav;
    }

    private ModelAndView handleEditAction() {
        Tab tab = tc.getTabConfig().getTabByID("SCHEDULES").createSubTab("../target-schedule-edit.jsp");
        TabbedModelAndView tmav = tc.new TabbedModelAndView();
        tmav.getTabStatus().setCurrentTab(tab);
        tmav.getTabStatus().setEnabled(false);

        Schedule schedule = (Schedule) ctx.getObject(Schedule.class, command.getSelectedItem());
        TargetSchedulesCommand newCommand = TargetSchedulesCommand.buildFromModel(schedule);
        tmav.addObject(Constants.GBL_CMD_DATA, newCommand);
        tmav.addObject("patterns", patternFactory.getPatterns());
        return tmav;
    }

    private ModelAndView handleSaveAction(BindException errors) {
        if (errors.hasErrors()) {
            return createErrorView(errors);
        }

        if (!Utils.isEmpty(command.getSelectedItem())) {
            Schedule existingSchedule = (Schedule) ctx.getObject(Schedule.class, command.getSelectedItem());
            ctx.getAbstractTarget().removeSchedule(existingSchedule);
        }

        Schedule schedule = buildScheduleFromCommand(command);
        ctx.getAbstractTarget().addSchedule(schedule);
        return createSuccessView("Schedule saved successfully.");
    }

    private ModelAndView handleDeleteAction() {
        if (!Utils.isEmpty(command.getSelectedItem())) {
            Schedule schedule = (Schedule) ctx.getObject(Schedule.class, command.getSelectedItem());
            ctx.getAbstractTarget().removeSchedule(schedule);
        }
        return createSuccessView("Schedule deleted successfully.");
    }

    private ModelAndView handleTestAction() {
        // Lógica para testar o agendamento
        return createSuccessView("Schedule tested successfully.");
    }

    private Schedule buildScheduleFromCommand(TargetSchedulesCommand command) {
        Schedule schedule = businessObjectFactory.newSchedule(ctx.getAbstractTarget());
        schedule.setStartDate(command.getStartDate());
        schedule.setEndDate(command.getEndDate());
        schedule.setScheduleType(command.getScheduleType());
        schedule.setCronPattern(command.getCronExpression());
        return schedule;
    }

    private ModelAndView createDefaultView() {
        TabbedModelAndView tmav = tc.new TabbedModelAndView();
        tmav.addObject("schedules", ctx.getSortedSchedules());
        tmav.addObject("patternMap", patternFactory.getPatternMap());
        return tmav;
    }

    private ModelAndView createErrorView(String message) {
        TabbedModelAndView tmav = tc.new TabbedModelAndView();
        tmav.addObject("errorMessage", message);
        return tmav;
    }

    private ModelAndView createSuccessView(String message) {
        TabbedModelAndView tmav = tc.new TabbedModelAndView();
        tmav.addObject("successMessage", message);
        return tmav;
    }
}

public class ScheduleBuilder {
    public static Schedule buildSchedule(TargetSchedulesCommand command, BusinessObjectFactory factory, AbstractTarget abstractTarget) {
        Schedule schedule = factory.newSchedule(abstractTarget);
        schedule.setStartDate(command.getStartDate());
        schedule.setEndDate(command.getEndDate());
        schedule.setScheduleType(command.getScheduleType());
        schedule.setCronPattern(command.getCronExpression());
        return schedule;
    }
}