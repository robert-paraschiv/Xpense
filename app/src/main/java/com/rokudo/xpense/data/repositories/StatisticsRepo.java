package com.rokudo.xpense.data.repositories;

import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.ListenerRegistration;
import com.rokudo.xpense.models.StatisticsDoc;
import com.rokudo.xpense.models.Transaction;
import com.rokudo.xpense.utils.DatabaseUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class StatisticsRepo {
    private static StatisticsRepo instance;

    private ListenerRegistration statisticsDocListener;

    StatisticsDoc storedStatisticsDoc;
    MutableLiveData<Map<String, Double>> categoriesByAmount;
    MutableLiveData<StatisticsDoc> statisticsLiveData;

    private String walletId;
    private String storedDate;

    public StatisticsRepo() {
        categoriesByAmount = new MutableLiveData<>();
        statisticsLiveData = new MutableLiveData<>();
    }


    public static StatisticsRepo getInstance() {
        if (instance == null) {
            instance = new StatisticsRepo();
        }
        return instance;
    }


    public MutableLiveData<StatisticsDoc> loadStatisticsDoc(String wallet, Date date) {


        String year = new SimpleDateFormat("yyyy", Locale.getDefault()).format(date);
        String month = new SimpleDateFormat("MMMM", Locale.getDefault()).format(date);
        if (statisticsDocListener == null
                || (walletId == null || !walletId.equals(wallet))
                || (storedDate == null || !storedDate.equals(year + month))
        ) {
            if (statisticsDocListener != null) {
                statisticsDocListener.remove();
            }
            walletId = wallet;
            storedDate = year + month;
            statisticsDocListener = DatabaseUtils.getMonthsReference(wallet, year)
                    .document(month)
                    .addSnapshotListener((value, error) -> {
                        if (value == null) {
                            return;
                        }
                        StatisticsDoc statisticsDoc = value.toObject(StatisticsDoc.class);
                        if (statisticsDoc == null) {
                            return;
                        }
                        statisticsLiveData.postValue(statisticsDoc);
                        storedStatisticsDoc = statisticsDoc;
                    });

        }
        return statisticsLiveData;
    }

    public StatisticsDoc getStoredStatisticsDoc() {
        return storedStatisticsDoc;
    }
}
