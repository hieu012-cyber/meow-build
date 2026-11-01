package com.meowcode.utilityhub;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class WeatherFragment extends Fragment {
    private EditText etCity;
    private Button btnSearch;
    private TextView tvCityName, tvTemp, tvDesc, tvHumidity, tvWind, tvFeels;

    static class Weather {
        int temp, humidity, wind, feels;
        String desc;
        Weather(int t, String d, int h, int w, int f) {
            temp = t; desc = d; humidity = h; wind = w; feels = f;
        }
    }

    private final Map<String, Weather> weatherData = new HashMap<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_weather, container, false);

        etCity = view.findViewById(R.id.etCity);
        btnSearch = view.findViewById(R.id.btnSearch);
        tvCityName = view.findViewById(R.id.tvCityName);
        tvTemp = view.findViewById(R.id.tvTemp);
        tvDesc = view.findViewById(R.id.tvDesc);
        tvHumidity = view.findViewById(R.id.tvHumidity);
        tvWind = view.findViewById(R.id.tvWind);
        tvFeels = view.findViewById(R.id.tvFeels);

        initData();
        btnSearch.setOnClickListener(v -> doSearch());
        return view;
    }

    private void initData() {
        weatherData.put("ha noi", new Weather(28, "Nhiều mây, có lúc nắng", 72, 11, 31));
        weatherData.put("hà nội", new Weather(28, "Nhiều mây, có lúc nắng", 72, 11, 31));
        weatherData.put("ho chi minh", new Weather(33, "Nắng nóng, chiều mưa rào", 65, 14, 37));
        weatherData.put("hồ chí minh", new Weather(33, "Nắng nóng, chiều mưa rào", 65, 14, 37));
        weatherData.put("sai gon", new Weather(33, "Nắng nóng, chiều mưa rào", 65, 14, 37));
        weatherData.put("da nang", new Weather(30, "Có mây, gió nhẹ", 68, 16, 33));
        weatherData.put("đà nẵng", new Weather(30, "Có mây, gió nhẹ", 68, 16, 33));
        weatherData.put("hue", new Weather(29, "Mưa rào rải rác", 80, 9, 32));
        weatherData.put("huế", new Weather(29, "Mưa rào rải rác", 80, 9, 32));
        weatherData.put("hai phong", new Weather(26, "Mát mẻ, trời quang", 60, 13, 27));
        weatherData.put("hải phòng", new Weather(26, "Mát mẻ, trời quang", 60, 13, 27));
        weatherData.put("da lat", new Weather(20, "Se lạnh, có sương mù", 85, 7, 19));
        weatherData.put("đà lạt", new Weather(20, "Se lạnh, có sương mù", 85, 7, 19));
        weatherData.put("can tho", new Weather(31, "Nắng nhẹ, ẩm cao", 78, 10, 35));
        weatherData.put("cần thơ", new Weather(31, "Nắng nhẹ, ẩm cao", 78, 10, 35));
    }

    private void doSearch() {
        String raw = etCity.getText().toString().trim().toLowerCase(Locale.getDefault());
        if (raw.isEmpty()) return;

        Weather w = weatherData.get(raw);
        if (w == null) {
            int hash = 0;
            for (int i = 0; i < raw.length(); i++) {
                hash = (hash * 31 + raw.charAt(i)) % 100000;
            }
            int temp = 20 + (hash % 16);
            w = new Weather(temp, "Dữ liệu mô phỏng", 55 + (hash % 40), 5 + (hash % 20), temp + 2);
        }

        tvCityName.setText(etCity.getText().toString().trim());
        tvTemp.setText(w.temp + "°C");
        tvDesc.setText(w.desc);
        tvHumidity.setText(w.humidity + "%");
        tvWind.setText(w.wind + " km/h");
        tvFeels.setText(w.feels + "°C");
    }
}
