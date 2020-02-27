package com.gluonhq.chat.views;

import com.gluonhq.charm.glisten.afterburner.AppView;
import com.gluonhq.charm.glisten.afterburner.AppViewRegistry;
import com.gluonhq.charm.glisten.afterburner.GluonPresenter;
import com.gluonhq.charm.glisten.afterburner.Utils;
import com.gluonhq.charm.glisten.application.MobileApplication;
import com.gluonhq.charm.glisten.control.Avatar;
import com.gluonhq.charm.glisten.control.NavigationDrawer;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;
import com.gluonhq.chat.GluonChat;
import javafx.scene.image.Image;

import java.util.Locale;

import static com.gluonhq.charm.glisten.afterburner.AppView.Flag.*;

public class AppViewManager {

    public static final AppViewRegistry REGISTRY = new AppViewRegistry();

    public static final AppView FIRST_VIEW  = view("Home View", HomePresenter.class, MaterialDesignIcon.HOME, HOME_VIEW, SKIP_VIEW_STACK);
    public static final AppView PORTRAIT_VIEW  = view("Chat View", PortraitPresenter.class, MaterialDesignIcon.CHAT, SHOW_IN_DRAWER, SKIP_VIEW_STACK);
    public static final AppView LANDSCAPE_VIEW  = view("Chat View", LandscapePresenter.class, MaterialDesignIcon.CHAT, SHOW_IN_DRAWER, SKIP_VIEW_STACK);
    public static final AppView CHAT_VIEW  = view("Chat View", ChatPresenter.class, MaterialDesignIcon.CHAT, SHOW_IN_DRAWER);
    public static final AppView USERS_VIEW  = view("Users View", UsersPresenter.class, MaterialDesignIcon.PEOPLE, SHOW_IN_DRAWER);
    public static final AppView MAPS_VIEW  = view("Maps View", MapsPresenter.class, MaterialDesignIcon.MAP, SHOW_IN_DRAWER);

    private static AppView view(String title, Class<? extends GluonPresenter<?>> presenterClass, MaterialDesignIcon menuIcon, AppView.Flag... flags ) {
        return REGISTRY.createView(name(presenterClass), title, presenterClass, menuIcon, flags);
    }

    private static String name(Class<? extends GluonPresenter<?>> presenterClass) {
        return presenterClass.getSimpleName().toUpperCase(Locale.ROOT).replace("PRESENTER", "");
    }

    public static void registerViewsAndDrawer(MobileApplication app) {
        for (AppView view : REGISTRY.getViews()) {
            view.registerView(app);
        }

        NavigationDrawer.Header header = new NavigationDrawer.Header("Gluon Mobile", "The Chat App Project",
                new Avatar(21, new Image(GluonChat.class.getResourceAsStream("/icon.png"))));

        Utils.buildDrawer(app.getDrawer(), header, REGISTRY.getViews());
    }
}
