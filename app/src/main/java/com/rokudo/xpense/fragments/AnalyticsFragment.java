package com.rokudo.xpense.fragments;

import static com.rokudo.xpense.utils.DateUtils.monthYearFormat;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.shape.ShapeAppearanceModel;
import com.google.android.material.transition.MaterialContainerTransform;
import com.rokudo.xpense.R;
import com.rokudo.xpense.data.viewmodels.TransactionViewModel;
import com.rokudo.xpense.databinding.FragmentAnalyticsBinding;

import java.util.Date;

public class AnalyticsFragment extends Fragment {

    FragmentAnalyticsBinding binding;
    TransactionViewModel transactionViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentAnalyticsBinding.inflate(inflater);


        transactionViewModel = new ViewModelProvider(requireActivity()).get(TransactionViewModel.class);

        initDateChip();
        getArgsPassed();


        return binding.getRoot();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        MaterialContainerTransform materialContainerTransform = new MaterialContainerTransform();
        materialContainerTransform.setDuration(getResources().getInteger(R.integer.transition_duration_millis));
        materialContainerTransform.setEndShapeAppearanceModel(new ShapeAppearanceModel().withCornerSize(18));
        materialContainerTransform.setStartShapeAppearanceModel(new ShapeAppearanceModel().withCornerSize(48));
        setSharedElementEnterTransition(materialContainerTransform);
        super.onCreate(savedInstanceState);
    }

    private void initDateChip() {
        binding.dateChip.setText(monthYearFormat.format(new Date()));
    }

    private void getArgsPassed() {
        AnalyticsFragmentArgs args = AnalyticsFragmentArgs.fromBundle(requireArguments());
        switch (args.getType()) {
            case "bar":
                showBarChartDefault();
                break;
            case "pie":
                showPieChartDefault();
                break;
            default:
                break;
        }
    }

    private void showPieChartDefault() {
        binding.analyticsRoot.setTransitionName("pieChartCard");
        binding.analyticsTypeImage
                .setImageDrawable(AppCompatResources.getDrawable(requireContext(), R.drawable.baseline_bar_chart_24));
    }

    private void showBarChartDefault() {
        binding.analyticsRoot.setTransitionName("barChartCard");
        binding.analyticsTypeImage
                .setImageDrawable(AppCompatResources.getDrawable(requireContext(), R.drawable.baseline_pie_chart_24));
    }
}