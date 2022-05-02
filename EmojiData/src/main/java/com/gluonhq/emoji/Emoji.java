package com.gluonhq.emoji;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Represents the data for each emoji as parsed from emoji.json
 *
 * {
 *     "name": "WHITE UP POINTING INDEX",
 *     "unified": "261D-FE0F",
 *     "non_qualified": "261D",
 *     "docomo": null,
 *     "au": "E4F6",
 *     "softbank": "E00F",
 *     "google": "FEB98",
 *     "image": "261d.png",
 *     "sheet_x": 1,
 *     "sheet_y": 2,
 *     "short_name": "point_up",
 *     "short_names": [
 *         "point_up"
 *     ],
 *     "text": null,
 *     "texts": null,
 *     "category": "People & Body",
 *     "subcategory": "hand-single-finger",
 *     "sort_order": 170,
 *     "added_in": "1.4",
 *     "has_img_apple": true,
 *     "has_img_google": true,
 *     "has_img_twitter": true,
 *     "has_img_facebook": false,
 *     "skin_variations": {
 *         "1F3FB": {
 *             "unified": "261D-1F3FB",
 *             "image": "261d-1f3fb.png",
 *             "sheet_x": 1,
 *             "sheet_y": 3,
 *             "added_in": "6.0",
 *             "has_img_apple": true,
 *             "has_img_google": false,
 *             "has_img_twitter": false,
 *             "has_img_facebook": false,
 *         }
 *         ...
 *         "1F3FB-1F3FC": {
 *             ...
 *         }
 *     },
 *     "obsoletes": "ABCD-1234",
 *     "obsoleted_by": "5678-90EF"
 * }
 *
 *
 *
 *
 *
 */
public class Emoji {

    String name;
    String unified;
    String non_qualified;
    List<String> variations;

    String docomo;
    String au;
    String softbank;
    String google;
    String image;
    
    int sheet_x;
    int sheet_y;
    
    String short_name;
    List<String> short_names;
    
    String text;
    List<String> texts;
      
    String category;
    String subcategory;
    int sort_order;
    String added_in;
    
    boolean has_img_apple;
    boolean has_img_google;
    boolean has_img_twitter;
    boolean has_img_emojione;
    boolean has_img_facebook;
    boolean has_img_messenger;
    
    Map<String, Emoji> skin_variations;
    String obsoletes;
    String obsoleted_by;

    public Optional<String> getName() {
        return name == null ? Optional.empty() : Optional.of(name);
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUnified() {
        return unified;
    }

    public void setUnified(String unified) {
        this.unified = unified;
    }

    public String getNon_qualified() {
        return non_qualified;
    }

    public void setNon_qualified(String non_qualified) {
        this.non_qualified = non_qualified;
    }

    public List<String> getVariations() {
        return variations;
    }

    public void setVariations(List<String> variations) {
        this.variations = variations;
    }

    public Optional<String> getDocomo() {
        return docomo == null ? Optional.empty() : Optional.of(docomo);
    }

    public void setDocomo(String docomo) {
        this.docomo = docomo;
    }

    public Optional<String> getAu() {
        return au == null ? Optional.empty() : Optional.of(au);
    }

    public void setAu(String au) {
        this.au = au;
    }

    public Optional<String> getSoftbank() {
        return softbank == null ? Optional.empty() : Optional.of(softbank);
    }

    public void setSoftbank(String softbank) {
        this.softbank = softbank;
    }

    public Optional<String> getGoogle() {
        return google == null ? Optional.empty() : Optional.of(google);
    }

    public void setGoogle(String google) {
        this.google = google;
    }

    public Optional<String> getImage() {
        return image == null ? Optional.empty() : Optional.of(image);
    }

    public void setImage(String image) {
        this.image = image;
    }

    public int getSheet_x() {
        return sheet_x;
    }

    public void setSheet_x(int sheet_x) {
        this.sheet_x = sheet_x;
    }

    public int getSheet_y() {
        return sheet_y;
    }

    public void setSheet_y(int sheet_y) {
        this.sheet_y = sheet_y;
    }

    public Optional<String> getShort_name() {
        return short_name == null? Optional.empty() : Optional.of(short_name);
    }

    public void setShort_name(String short_name) {
        this.short_name = short_name;
    }
    
    public String getCodeName() {
        return getShort_name().isPresent() ? ":" + getShort_name().get() + ":" : "";
    }

    public List<String> getShort_names() {
        return short_names;
    }

    public void setShort_names(List<String> short_names) {
        this.short_names = short_names;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public List<String> getTexts() {
        return texts;
    }

    public void setTexts(List<String> texts) {
        this.texts = texts;
    }

    public Optional<String> getCategory() {
        return category == null? Optional.empty() : Optional.of(category);
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSubcategory() {
        return subcategory;
    }

    public void setSubcategory(String subcategory) {
        this.subcategory = subcategory;
    }

    public int getSort_order() {
        return sort_order;
    }

    public void setSort_order(int sort_order) {
        this.sort_order = sort_order;
    }

    public String getAdded_in() {
        return added_in;
    }

    public void setAdded_in(String added_in) {
        this.added_in = added_in;
    }

    public boolean isHas_img_apple() {
        return has_img_apple;
    }

    public void setHas_img_apple(boolean has_img_apple) {
        this.has_img_apple = has_img_apple;
    }

    public boolean isHas_img_google() {
        return has_img_google;
    }

    public void setHas_img_google(boolean has_img_google) {
        this.has_img_google = has_img_google;
    }

    public boolean isHas_img_twitter() {
        return has_img_twitter;
    }

    public void setHas_img_twitter(boolean has_img_twitter) {
        this.has_img_twitter = has_img_twitter;
    }

    public boolean isHas_img_emojione() {
        return has_img_emojione;
    }

    public void setHas_img_emojione(boolean has_img_emojione) {
        this.has_img_emojione = has_img_emojione;
    }

    public boolean isHas_img_facebook() {
        return has_img_facebook;
    }

    public void setHas_img_facebook(boolean has_img_facebook) {
        this.has_img_facebook = has_img_facebook;
    }

    public boolean isHas_img_messenger() {
        return has_img_messenger;
    }

    public void setHas_img_messenger(boolean has_img_messenger) {
        this.has_img_messenger = has_img_messenger;
    }

    public Map<String, Emoji> getSkin_variations() {
        if (skin_variations == null) {
            skin_variations = new HashMap<>();
            // TODO: Add subtype
            // variations.forEach(variation -> skin_variations.put(variation, ));
        }
        return skin_variations;
    }

    public void setSkin_variations(Map<String, Emoji> skin_variations) {
        this.skin_variations = skin_variations;
    }

    public String getObsoletes() {
        return obsoletes;
    }

    public void setObsoletes(String obsoletes) {
        this.obsoletes = obsoletes;
    }

    public String getObsoleted_by() {
        return obsoleted_by;
    }

    public void setObsoleted_by(String obsoleted_by) {
        this.obsoleted_by = obsoleted_by;
    }

    /*************************************
     * 
     * Additional Methods
     * 
     *************************************/
    
    /**
     * `True` if emoji is coded on two or more bytes
     */
    public boolean isDoubleByte(Emoji emoji) {
        return getUnified().contains("-");
    }

    public String character() {
        return unicodeCharacter();
    }

    private String unicodeCharacter() {
        StringBuilder emojiString = new StringBuilder();
        final String unified = getUnified();
        for (String s : unified.split("-")) {
            emojiString.append(Character.toChars(Integer.parseInt(s, 16)));
        }
        return emojiString.toString();
    }
}
