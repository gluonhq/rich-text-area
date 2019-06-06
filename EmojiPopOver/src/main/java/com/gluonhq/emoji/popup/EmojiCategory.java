package com.gluonhq.emoji.popup;

import java.util.Locale;
import java.util.ResourceBundle;

public enum EmojiCategory {
    
    People    ("TAB.NAME.PEOPLE"       , "person"),
    Nature    ("TAB.NAME.NATURE"       , "nature"),
    FoodDrink ("TAB.NAME.FOOD_DRINK"   , "food"),
    Activity  ("TAB.NAME.ACTIVITIES"   , "activity"),
    Travel    ("TAB.NAME.TRAVEL_PLACES", "travel"),
    Objects   ("TAB.NAME.OBJECTS"      , "object"),
    Symbols   ("TAB.NAME.SYMBOLS"      , "symbol"),
    Flags     ("TAB.NAME.FLAGS"        , "flag");

    private final String name;
    private final String styleClass;

    EmojiCategory(String resourceName, String styleClass) {
        ResourceBundle resourceBundle = ResourceBundle.getBundle("com.gluonhq.emoji.popup.emoji-popover", Locale.getDefault());
        
        this.name = resourceBundle.getString(resourceName);
        this.styleClass = styleClass;
    }
    
    public String categoryName() {
        return name;
    }

    public String getStyleClass() {
        return styleClass;
    }
}
