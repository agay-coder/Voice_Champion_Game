package com.itayagay.voicechampion.model;

/**
 *  לממשק זה יכולים להירשם מחלקות אשר רוצות לעדכן את כל ה Observers שלהן שהיה בהן שינוי.
 */

public interface IDataObservable {

    void registerObserver(IDataObserver observer);
    void removeObserver(IDataObserver observer);

    void notifyObserversDataChanged();
}
