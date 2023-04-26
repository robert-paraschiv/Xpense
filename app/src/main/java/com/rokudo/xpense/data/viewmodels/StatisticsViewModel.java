package com.rokudo.xpense.data.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.rokudo.xpense.data.repositories.StatisticsRepo;
import com.rokudo.xpense.models.StatisticsDoc;
import com.rokudo.xpense.models.Transaction;

import java.util.Date;
import java.util.List;

public class StatisticsViewModel extends AndroidViewModel {

    private StatisticsRepo statisticsRepo;

    public StatisticsViewModel(@NonNull Application application) {
        super(application);
        statisticsRepo = StatisticsRepo.getInstance();
    }


    public StatisticsDoc getStoredStatisticsDoc() {
        return statisticsRepo.getStoredStatisticsDoc();
    }

    public MutableLiveData<StatisticsDoc> loadStatisticsMonth(String walletId, Date date) {
        return statisticsRepo.loadStatisticsDoc(walletId, date);
    }

}
