package com.meowcode.utilityhub;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ClockFragment extends Fragment {
    private TextView tvClockTime, tvClockDate, tvStopwatch;
    private Button btnSwStart, btnSwLap, btnSwReset;
    private LinearLayout lapsContainer;

    private final Handler clockHandler = new Handler(Looper.getMainLooper());
    private final Handler swHandler = new Handler(Looper.getMainLooper());
    private final SimpleDateFormat timeFmt = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
    private final SimpleDateFormat dateFmt = new SimpleDateFormat("EEEE, dd/MM/yyyy", new Locale("vi", "VN"));

    private boolean swRunning = false;
    private long swElapsed = 0;
    private long swStartTime = 0;
    private int lapCount = 0;

    private final Runnable clockRunnable = new Runnable() {
        @Override
        public void run() {
            Date now = new Date();
            if (tvClockTime != null) tvClockTime.setText(timeFmt.format(now));
            if (tvClockDate != null) tvClockDate.setText(dateFmt.format(now));
            clockHandler.postDelayed(this, 1000);
        }
    };

    private final Runnable swRunnable = new Runnable() {
        @Override
        public void run() {
            swElapsed = System.currentTimeMillis() - swStartTime;
            if (tvStopwatch != null) tvStopwatch.setText(formatSw(swElapsed));
            if (swRunning) swHandler.postDelayed(this, 31);
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_clock, container, false);
        tvClockTime = view.findViewById(R.id.tvClockTime);
        tvClockDate = view.findViewById(R.id.tvClockDate);
        tvStopwatch = view.findViewById(R.id.tvStopwatch);
        btnSwStart = view.findViewById(R.id.btnSwStart);
        btnSwLap = view.findViewById(R.id.btnSwLap);
        btnSwReset = view.findViewById(R.id.btnSwReset);
        lapsContainer = view.findViewById(R.id.lapsContainer);

        btnSwStart.setOnClickListener(v -> toggleStopwatch());
        btnSwLap.setOnClickListener(v -> addLap());
        btnSwReset.setOnClickListener(v -> resetStopwatch());

        clockHandler.post(clockRunnable);
        updateButtons();
        return view;
    }

    private String formatSw(long ms) {
        long totalCs = ms / 10;
        long cs = totalCs % 100;
        long totalSec = totalCs / 100;
        long s = totalSec % 60;
        long m = totalSec / 60;
        return String.format(Locale.getDefault(), "%02d:%02d.%02d", m, s, cs);
    }

    private void toggleStopwatch() {
        if (!swRunning) {
            swRunning = true;
            swStartTime = System.currentTimeMillis() - swElapsed;
            swHandler.post(swRunnable);
        } else {
            swRunning = false;
            swHandler.removeCallbacks(swRunnable);
        }
        updateButtons();
    }

    private void resetStopwatch() {
        swRunning = false;
        swHandler.removeCallbacks(swRunnable);
        swElapsed = 0;
        lapCount = 0;
        if (tvStopwatch != null) tvStopwatch.setText("00:00.00");
        if (lapsContainer != null) lapsContainer.removeAllViews();
        updateButtons();
    }

    private void addLap() {
        if (!swRunning || lapsContainer == null) return;
        lapCount++;
        TextView lapView = new TextView(requireContext());
        lapView.setText("Vòng " + lapCount + "    " + formatSw(swElapsed));
        lapView.setTextColor(getResources().getColor(R.color.muted, null));
        lapView.setTextSize(14);
        lapView.setPadding(0, 8, 0, 8);
        lapsContainer.addView(lapView, 0);
    }

    private void updateButtons() {
        if (btnSwStart == null) return;
        if (swRunning) {
            btnSwStart.setText("Dừng");
            btnSwStart.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                getResources().getColor(R.color.accent, null)));
            btnSwLap.setEnabled(true);
        } else {
            btnSwStart.setText(swElapsed == 0 ? "Bắt đầu" : "Tiếp tục");
            btnSwStart.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                getResources().getColor(R.color.primary, null)));
            btnSwLap.setEnabled(false);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        clockHandler.removeCallbacks(clockRunnable);
        swHandler.removeCallbacks(swRunnable);
    }
}
