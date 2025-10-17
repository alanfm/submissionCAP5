package org.webcurator.ui.intray.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractFormController;
import org.webcurator.core.notification.InTrayManager;
import org.webcurator.core.util.AuthUtil;
import org.webcurator.core.util.CookieUtils;

public class InTrayController extends AbstractFormController {
    private Log log = LogFactory.getLog(InTrayController.class);
    private InTrayManager inTrayManager;
    private ActionHandler actionHandler;
    private PaginationManager paginationManager;

    public InTrayController() {
        setCommandClass(InTrayCommand.class);
    }

    public void setInTrayManager(InTrayManager inTrayManager) {
        this.inTrayManager = inTrayManager;
        this.actionHandler = new ActionHandler(inTrayManager);
        this.paginationManager = new PaginationManager(inTrayManager);
    }

    @Override
    protected ModelAndView processFormSubmission(HttpServletRequest request, HttpServletResponse response, Object command, BindException errors) throws Exception {
        InTrayCommand intrayCmd = (InTrayCommand) command;

        // Gerenciamento de cookies e paginação
        String currentPageSize = CookieUtils.getPageSize(request);
        int taskPage = intrayCmd.getTaskPage();
        int notificationPage = intrayCmd.getNotificationPage();

        if (intrayCmd.getSelectedPageSize() != null && !intrayCmd.getSelectedPageSize().equals(currentPageSize)) {
            CookieUtils.setPageSize(response, intrayCmd.getSelectedPageSize());
            taskPage = 0;
            notificationPage = 0;
        }

        // Delegação de ações para o ActionHandler
        return actionHandler.handleAction(intrayCmd, taskPage, notificationPage, Integer.parseInt(currentPageSize), errors);
    }

    @Override
    protected ModelAndView showForm(HttpServletRequest request, HttpServletResponse response, BindException errors) throws Exception {
        String currentPageSize = CookieUtils.getPageSize(request);
        return paginationManager.getDefaultView(0, 0, Integer.parseInt(currentPageSize));
    }
}

public class ActionHandler {
    private final InTrayManager inTrayManager;

    public ActionHandler(InTrayManager inTrayManager) {
        this.inTrayManager = inTrayManager;
    }

    public ModelAndView handleAction(InTrayCommand intrayCmd, int taskPage, int notificationPage, int pageSize, BindException errors) {
        switch (intrayCmd.getAction()) {
            case InTrayCommand.ACTION_DELETE_NOTIFICATION:
                return deleteNotification(intrayCmd, pageSize);
            case InTrayCommand.ACTION_VIEW_NOTIFICATION:
                return viewNotification(intrayCmd, pageSize);
            case InTrayCommand.ACTION_DELETE_TASK:
                return deleteTask(intrayCmd, pageSize, errors);
            case InTrayCommand.ACTION_VIEW_TASK:
                return viewTask(intrayCmd, pageSize);
            case InTrayCommand.ACTION_CLAIM_TASK:
                return claimTask(intrayCmd, pageSize);
            case InTrayCommand.ACTION_UNCLAIM_TASK:
                return unclaimTask(intrayCmd, pageSize);
            case InTrayCommand.ACTION_DELETE_ALL_NOTIFICATIONS:
                return deleteAllNotifications(intrayCmd, pageSize);
            default:
                return paginationManager.getDefaultView(taskPage, notificationPage, pageSize);
        }
    }

    private ModelAndView deleteNotification(InTrayCommand intrayCmd, int pageSize) {
        if (intrayCmd.getNotificationOid() != null) {
            inTrayManager.deleteNotification(intrayCmd.getNotificationOid());
        }
        return paginationManager.getDefaultView(intrayCmd.getTaskPage(), intrayCmd.getNotificationPage(), pageSize);
    }

    private ModelAndView viewNotification(InTrayCommand intrayCmd, int pageSize) {
        if (intrayCmd.getNotificationOid() != null) {
            Notification notify = inTrayManager.getNotification(intrayCmd.getNotificationOid());
            ModelAndView mav = new ModelAndView("intray-notification");
            mav.addObject(InTrayCommand.MDL_NOTIFICATION, notify);
            return mav;
        }
        return paginationManager.getDefaultView(0, 0, pageSize);
    }

    private ModelAndView deleteTask(InTrayCommand intrayCmd, int pageSize, BindException errors) {
        if (intrayCmd.getTaskOid() != null) {
            try {
                inTrayManager.deleteTask(intrayCmd.getTaskOid());
            } catch (NotOwnerRuntimeException e) {
                errors.reject("task.error.delete.not.owner");
            }
        }
        ModelAndView mav = paginationManager.getDefaultView(intrayCmd.getTaskPage(), intrayCmd.getNotificationPage(), pageSize);
        if (errors.hasErrors()) {
            mav.addObject(Constants.GBL_ERRORS, errors);
        }
        return mav;
    }

    private ModelAndView viewTask(InTrayCommand intrayCmd, int pageSize) {
        if (intrayCmd.getTaskOid() != null) {
            Task task = inTrayManager.getTask(intrayCmd.getTaskOid());
            ModelAndView mav = new ModelAndView("intray-task");
            mav.addObject(InTrayCommand.MDL_TASK, task);
            return mav;
        }
        return paginationManager.getDefaultView(0, 0, pageSize);
    }

    private ModelAndView claimTask(InTrayCommand intrayCmd, int pageSize) {
        if (intrayCmd.getTaskOid() != null) {
            inTrayManager.claimTask(AuthUtil.getRemoteUserObject(), intrayCmd.getTaskOid());
        }
        return paginationManager.getDefaultView(intrayCmd.getTaskPage(), intrayCmd.getNotificationPage(), pageSize);
    }

    private ModelAndView unclaimTask(InTrayCommand intrayCmd, int pageSize) {
        if (intrayCmd.getTaskOid() != null) {
            inTrayManager.unclaimTask(AuthUtil.getRemoteUserObject(), intrayCmd.getTaskOid());
        }
        return paginationManager.getDefaultView(intrayCmd.getTaskPage(), intrayCmd.getNotificationPage(), pageSize);
    }

    private ModelAndView deleteAllNotifications(InTrayCommand intrayCmd, int pageSize) {
        inTrayManager.deleteAllNotifications(AuthUtil.getRemoteUserObject().getOid());
        return paginationManager.getDefaultView(intrayCmd.getTaskPage(), intrayCmd.getNotificationPage(), pageSize);
    }
}

public class PaginationManager {
    private final InTrayManager inTrayManager;

    public PaginationManager(InTrayManager inTrayManager) {
        this.inTrayManager = inTrayManager;
    }

    public ModelAndView getDefaultView(int taskPage, int notificationPage, int pageSize) {
        ModelAndView mav = new ModelAndView("intray");
        User loggedInUser = AuthUtil.getRemoteUserObject();

        mav.addObject(InTrayCommand.MDL_NOTIFICATIONS, inTrayManager.getNotifications(loggedInUser, notificationPage, pageSize));
        mav.addObject(InTrayCommand.MDL_TASKS, inTrayManager.getTasks(loggedInUser, taskPage, pageSize));
        mav.addObject(InTrayCommand.MDL_CURRENT_USER, loggedInUser);

        return mav;
    }
}