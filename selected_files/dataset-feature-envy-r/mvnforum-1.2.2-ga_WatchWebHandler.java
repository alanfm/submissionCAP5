public class WatchWebHandler {
    private final EmailSender emailSender;
    private final WatchManager watchManager;
    private final WatchPreparer watchPreparer;
    private final PermissionValidator permissionValidator;

    public WatchWebHandler() {
        this.emailSender = new EmailSender();
        this.watchManager = new WatchManager();
        this.watchPreparer = new WatchPreparer();
        this.permissionValidator = new PermissionValidator();
    }

    public void sendMail() throws Exception {
        emailSender.sendWatchNotifications();
    }

    public void prepareList(GenericRequest request) throws Exception {
        permissionValidator.ensureAuthenticated(request);
        watchPreparer.prepareWatchList(request);
    }

    public void prepareAdd(GenericRequest request, GenericResponse response) throws Exception {
        permissionValidator.validateWatchFeatureEnabled(request);
        watchManager.prepareAdd(request, response);
    }

    public void processDelete(GenericRequest request) throws Exception {
        permissionValidator.ensureAuthenticated(request);
        watchManager.processDelete(request);
    }

    public void processEdit(GenericRequest request) throws Exception {
        permissionValidator.ensureAuthenticated(request);
        watchManager.processEdit(request);
    }
}

public class EmailSender {
    public void sendWatchNotifications() throws Exception {
        if (!MVNForumConfig.getEnableWatch()) {
            System.out.println("Watch feature is disabled. Skipping email notifications.");
            return;
        }

        String forumBase = ParamUtil.getServerPath() + ParamUtil.getContextPath() + UserModuleConfig.getUrlPattern();
        Collection<WatchBean> watches = DAOFactory.getWatchDAO().getMemberBeans();

        for (WatchBean watch : watches) {
            int memberID = watch.getMemberID();
            if (!DAOFactory.getMemberDAO().getActivateCode(memberID).equals(MemberBean.MEMBER_ACTIVATECODE_ACTIVATED)) {
                continue; // Skip inactive members
            }

            Timestamp lastSent = watch.getWatchLastSentDate();
            Timestamp now = DateUtil.getCurrentGMTTimestamp();

            if (shouldSendNotification(watch, lastSent, now)) {
                sendEmailForWatch(watch, forumBase);
                DAOFactory.getWatchDAO().updateLastSentDate_forMember(memberID, now);
            }
        }
    }

    private boolean shouldSendNotification(WatchBean watch, Timestamp lastSent, Timestamp now) {
        long minimumWaitTime = getMinimumWaitTime(watch.getWatchOption());
        return now.getTime() - lastSent.getTime() >= minimumWaitTime;
    }

    private long getMinimumWaitTime(int watchOption) {
        switch (watchOption) {
            case WatchBean.WATCH_OPTION_LIVE:
                return 0;
            case WatchBean.WATCH_OPTION_HOURLY:
                return DateUtil.HOUR;
            case WatchBean.WATCH_OPTION_DAILY:
                return DateUtil.DAY;
            case WatchBean.WATCH_OPTION_WEEKLY:
                return DateUtil.WEEK;
            default:
                return DateUtil.DAY; // Default to daily
        }
    }

    private void sendEmailForWatch(WatchBean watch, String forumBase) {
        try {
            MemberBean receiver = DAOFactory.getMemberDAO().getMember(watch.getMemberID());
            WatchMail watchMail = createWatchMail(receiver, forumBase, watch);

            if (watchMail.haveAtLeastOneNewThread()) {
                MailMessageStruct mailMessage = new MailMessageStruct();
                mailMessage.setFrom(MVNForumConfig.getWebMasterEmail());
                mailMessage.setTo(receiver.getMemberEmail());
                mailMessage.setSubject("Your Watch Notifications");
                mailMessage.setBody(watchMail.generateContent());
                MailUtil.sendMessage(mailMessage);
            }
        } catch (Exception e) {
            System.err.println("Error sending email for watch ID " + watch.getWatchID() + ": " + e.getMessage());
        }
    }

    private WatchMail createWatchMail(MemberBean receiver, String forumBase, WatchBean watch) {
        return new WatchMail(receiver, null, forumBase, watch.getWatchLastSentDate(), DateUtil.getCurrentGMTTimestamp());
    }
}

public class WatchManager {
    public void prepareAdd(GenericRequest request, GenericResponse response) throws Exception {
        int memberID = OnlineUserManager.getInstance().getOnlineUser(request).getMemberID();
        int categoryID = request.getParameter("category") != null ? Integer.parseInt(request.getParameter("category")) : 0;
        int forumID = request.getParameter("forum") != null ? Integer.parseInt(request.getParameter("forum")) : 0;
        int threadID = request.getParameter("thread") != null ? Integer.parseInt(request.getParameter("thread")) : 0;

        DAOFactory.getWatchDAO().create(
            memberID, categoryID, forumID, threadID,
            GenericParamUtil.getParameterInt(request, "WatchType"),
            WatchBean.WATCH_OPTION_DEFAULT,
            0, DateUtil.getCurrentGMTTimestamp(), DateUtil.getCurrentGMTTimestamp(), DateUtil.getCurrentGMTTimestamp()
        );
    }

    public void processDelete(GenericRequest request) throws Exception {
        int memberID = OnlineUserManager.getInstance().getOnlineUser(request).getMemberID();
        int watchID = GenericParamUtil.getParameterInt(request, "watch");

        WatchBean watch = DAOFactory.getWatchDAO().getWatch(watchID);
        if (watch.getMemberID() != memberID) {
            throw new BadInputException("Cannot delete watch: this watch is not owned by the current member.");
        }

        DAOFactory.getWatchDAO().delete(watchID);
    }

    public void processEdit(GenericRequest request) throws Exception {
        int memberID = OnlineUserManager.getInstance().getOnlineUser(request).getMemberID();
        int watchID = GenericParamUtil.getParameterInt(request, "watch");

        WatchBean watch = DAOFactory.getWatchDAO().getWatch(watchID);
        if (watch.getMemberID() != memberID) {
            throw new BadInputException("Cannot edit watch: this watch is not owned by the current member.");
        }

        int watchType = GenericParamUtil.getParameterInt(request, "WatchType");
        DAOFactory.getWatchDAO().updateWatchType(watchID, watchType);
    }
}

public class WatchPreparer {
    public void prepareWatchList(GenericRequest request) throws Exception {
        int memberID = OnlineUserManager.getInstance().getOnlineUser(request).getMemberID();
        Locale locale = I18nUtil.getLocaleInRequest(request);

        Collection<WatchBean> watches = DAOFactory.getWatchDAO().getWatches_forMember(memberID);
        Collection<WatchBean> globalWatches = WatchUtil.getGlobalWatchs(watches);
        Collection<WatchBean> categoryWatches = WatchUtil.getCategoryWatchs(watches);
        Collection<WatchBean> forumWatches = WatchUtil.getForumWatchs(watches);
        Collection<WatchBean> threadWatches = WatchUtil.getThreadWatchs(watches);

        request.setAttribute("GlobalWatchBeans", globalWatches);
        request.setAttribute("CategoryWatchBeans", categoryWatches);
        request.setAttribute("ForumWatchBeans", forumWatches);
        request.setAttribute("ThreadWatchBeans", threadWatches);
    }
}

public class PermissionValidator {
    public void ensureAuthenticated(GenericRequest request) throws AuthenticationException {
        OnlineUser onlineUser = OnlineUserManager.getInstance().getOnlineUser(request);
        MVNForumPermission permission = onlineUser.getPermission();
        permission.ensureIsAuthenticated();
    }

    public void validateWatchFeatureEnabled(GenericRequest request) throws IllegalStateException {
        if (!MVNForumConfig.getEnableWatch()) {
            Locale locale = I18nUtil.getLocaleInRequest(request);
            String localizedMessage = MVNForumResourceBundle.getString(locale, "java.lang.IllegalStateException.cannot_add_watch.watch_is_disabled");
            throw new IllegalStateException(localizedMessage);
        }
    }
}