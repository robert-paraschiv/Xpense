package com.rokudo.xpense.data.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.rokudo.xpense.data.repositories.StatisticsRepo;
import com.rokudo.xpense.models.StatisticsDoc;

import java.util.Date;

public class StatisticsViewModel extends AndroidViewModel {

    private final StatisticsRepo statisticsRepo;

    public StatisticsViewModel(@NonNull Application application) {
        super(application);
        statisticsRepo = StatisticsRepo.getInstance();
    }


    public StatisticsDoc getStoredStatisticsDoc() {
        return statisticsRepo.getStoredStatisticsDoc();
    }

    public MutableLiveData<StatisticsDoc> listenForStatisticsDoc(String walletId, Date date) {
        return statisticsRepo.listenForStatisticsDoc(walletId, date);
    }

    public MutableLiveData<StatisticsDoc> loadStatisticsDoc(String walletId, Date date, boolean isYearSelected) {
        return statisticsRepo.loadStatisticsDoc(walletId, date, isYearSelected);
    }

}
