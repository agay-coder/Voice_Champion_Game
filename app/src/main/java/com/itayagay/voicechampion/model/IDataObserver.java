package com.itayagay.voicechampion.model;

/** לממשק זה יורשים כל מחלקה שרוצה לקבל שינויים ממחלקה שהיא Observable.
 */

public interface IDataObserver {

    void onDataChanged(IDataObservable observable);


}
