package com.rokudo.xpense.data.repositories;

import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;
import com.rokudo.xpense.models.StatisticsDoc;
import com.rokudo.xpense.models.Transaction;
import com.rokudo.xpense.utils.DatabaseUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class StatisticsRepo {
    private static StatisticsRepo instance;

    private ListenerRegistration statisticsDocListener;

    StatisticsDoc storedStatisticsDoc, homeStatisticsDoc, analyticsStoredDoc;
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
                    .collection("Months")
                    .get().addOnSuccessListener(queryDocumentSnapshots -> {
                        if (queryDocumentSnapshots.isEmpty()) {
                            return;
                        }
                        StatisticsDoc yearDoc = new StatisticsDoc();
                        yearDoc.setCategories(new HashMap<>());
                        yearDoc.setAmountByCategory(new HashMap<>());
                        yearDoc.setTransactions(new HashMap<>());
                        yearDoc.setTotalAmountSpent(0d);
                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            StatisticsDoc statisticsMonthDoc = doc.toObject(StatisticsDoc.class);
                            if (statisticsMonthDoc == null) {
                                continue;
                            }
                            yearDoc.setTotalAmountSpent(yearDoc.getTotalAmountSpent() + statisticsMonthDoc.getTotalAmountSpent());
                            yearDoc.getTransactions().putAll(statisticsMonthDoc.getTransactions());
                            for (String categoryKey : statisticsMonthDoc.getCategories().keySet()) {
                                if (yearDoc.getCategories().containsKey(categoryKey)) {
                                    yearDoc.getCategories().get(categoryKey).putAll(yearDoc.getCategories().get(categoryKey));
                                } else {
                                    yearDoc.getCategories().put(categoryKey, statisticsMonthDoc.getCategories().get(categoryKey));
                                }
                            }
                            for (String category : statisticsMonthDoc.getAmountByCategory().keySet()) {
                                if (yearDoc.getAmountByCategory().containsKey(category)) {
                                    yearDoc.getAmountByCategory().put(category, yearDoc.getAmountByCategory().get(category) + statisticsMonthDoc.getAmountByCategory().get(category));
                                } else {
                                    yearDoc.getAmountByCategory().put(category, statisticsMonthDoc.getAmountByCategory().get(category));
                                }
                            }
                        }
                        statisticsDocMutableLiveData.setValue(yearDoc);
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

    public void setAnalyticsStoredStatisticsDoc(StatisticsDoc statisticsDoc) {
        analyticsStoredDoc = statisticsDoc;
    }

    public StatisticsDoc getAnalyticsStoredDoc() {
        return analyticsStoredDoc;
    }
}
