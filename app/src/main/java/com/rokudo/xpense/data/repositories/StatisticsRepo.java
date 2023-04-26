package com.rokudo.xpense.data.repositories;

import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.ListenerRegistration;
import com.rokudo.xpense.models.StatisticsDoc;
import com.rokudo.xpense.utils.DatabaseUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

public class StatisticsRepo {
    private static StatisticsRepo instance;

    private ListenerRegistration statisticsDocListener;

    StatisticsDoc storedStatisticsDoc;
    StatisticsDoc homeStatisticsDoc;
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

    public MutableLiveData<StatisticsDoc> loadStatisticsDoc(String wallet, Date date, boolean isYearStatisticsDoc) {
        MutableLiveData<StatisticsDoc> statisticsDocMutableLiveData = new MutableLiveData<>();

        String year = new SimpleDateFormat("yyyy", Locale.getDefault()).format(date);
        String month = new SimpleDateFormat("MMMM", Locale.getDefault()).format(date);

        if (isYearStatisticsDoc) {
            DatabaseUtils.getYearReference(wallet, year)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot == null) {
                            return;
                        }
                        StatisticsDoc statisticsDoc = documentSnapshot.toObject(StatisticsDoc.class);
                        if (statisticsDoc == null) {
                            return;
                        }
                        statisticsDocMutableLiveData.setValue(statisticsDoc);
                    });

        } else {
            DatabaseUtils.getMonthsReference(wallet, year)
                    .document(month)
                    .get().addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot == null) {
                            return;
                        }
                        StatisticsDoc statisticsDoc = documentSnapshot.toObject(StatisticsDoc.class);
                        if (statisticsDoc == null) {
                            return;
                        }
                        statisticsDocMutableLiveData.setValue(statisticsDoc);
                    });
        }
        return statisticsDocMutableLiveData;
    }

    public MutableLiveData<StatisticsDoc> listenForStatisticsDoc(String wallet, Date date) {


        String year = new SimpleDateFormat("yyyy", Locale.getDefault()).format(date);
        String month = new SimpleDateFormat("MMMM", Locale.getDefault()).format(date);
        if (statisticsDocListener == null
                || (walletId == null || !walletId.equals(wallet))
                || (storedDate == null || !storedDate.equals(year + month))) {
            if (statisticsDocListener != null) {
                statisticsDocListener.remove();
                statisticsLiveData.setValue(null);
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
                        statisticsDoc.setDocPath(value.getReference().getPath());
                        statisticsLiveData.postValue(statisticsDoc);
                        storedStatisticsDoc = statisticsDoc;
                    });

        }
        return statisticsLiveData;
    }

    public StatisticsDoc getHomeStatisticsDoc() {
        return homeStatisticsDoc;
    }

    public StatisticsDoc getStoredStatisticsDoc() {
        return storedStatisticsDoc;
    }

    public void setHomeStoredStatisticsDoc(StatisticsDoc statisticsDoc) {
        homeStatisticsDoc = statisticsDoc;
    }
}
