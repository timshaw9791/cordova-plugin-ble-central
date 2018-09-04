package com.megster.cordova.ble.central;



public class Clothes{
    private String clothesId;
    private String clothesName;
    private String clothesColor;
    private String clothesGrade;
    private String clothesPrice;
    private String clothesDefect;
    private String clothesAdditional;
    private String clothesAdditionalPrice;

    public Clothes(String clothesId, String clothesName, String clothesColor, String clothesGrade, String clothesPrice, String clothesDefect, String clothesAdditional, String clothesAdditionalPrice) {
        this.clothesId = clothesId;
        this.clothesName = clothesName;
        this.clothesColor = clothesColor;
        this.clothesGrade = clothesGrade;
        this.clothesPrice = clothesPrice;
        this.clothesDefect = clothesDefect;
        this.clothesAdditional = clothesAdditional;
        this.clothesAdditionalPrice = clothesAdditionalPrice;
    }

    public String getClothesId() {
        return clothesId;
    }

    public String getClothesName() {
        return clothesName;
    }

    public String getClothesColor() {
        return clothesColor;
    }

    public String getClothesGrade() {
        return clothesGrade;
    }

    public String getClothesPrice() {
        return clothesPrice;
    }

    public String getClothesDefect() {
        return clothesDefect;
    }

    public String getClothesAdditional() {
        return clothesAdditional;
    }

    public String getClothesAdditionalPrice() {
        return clothesAdditionalPrice;
    }
}
