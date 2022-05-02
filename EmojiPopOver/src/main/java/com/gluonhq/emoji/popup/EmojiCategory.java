package com.gluonhq.emoji.popup;

import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public enum EmojiCategory {
    
    People    (List.of("TAB.NAME.SMILEY", "TAB.NAME.PEOPLE"), "person"),
    Nature    (List.of("TAB.NAME.NATURE")               , "nature"),
    FoodDrink (List.of("TAB.NAME.FOOD_DRINK")           , "food"),
    Activity  (List.of("TAB.NAME.ACTIVITIES")           , "activity"),
    Travel    (List.of("TAB.NAME.TRAVEL_PLACES")        , "travel"),
    Objects   (List.of("TAB.NAME.OBJECTS")              , "object"),
    Symbols   (List.of("TAB.NAME.SYMBOLS")              , "symbol"),
    Flags     (List.of("TAB.NAME.FLAGS")                , "flag");

    private final String name;
    private final String styleClass;

    EmojiCategory(List<String> resourceNames, String styleClass) {
        ResourceBundle resourceBundle = ResourceBundle.getBundle("com.gluonhq.emoji.popup.emoji-popover", Locale.getDefault());
        
        this.name = resourceNames.stream().map(resourceBundle::getString).collect(Collectors.joining(","));
        this.styleClass = styleClass;
    }
    
    public String categoryName() {
        return name;
    }

    public String getStyleClass() {
        return styleClass;
    }
}
