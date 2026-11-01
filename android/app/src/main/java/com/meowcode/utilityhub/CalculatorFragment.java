package com.meowcode.utilityhub;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import java.text.DecimalFormat;

public class CalculatorFragment extends Fragment {
    private TextView tvResult, tvHistory;
    private String currentInput = "0";
    private Double previousValue = null;
    private String operator = null;
    private boolean waitingForOperand = false;
    private final DecimalFormat df = new DecimalFormat("#,##0.##");

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calculator, container, false);
        tvResult = view.findViewById(R.id.tvResult);
        tvHistory = view.findViewById(R.id.tvHistory);

        int[] numIds = {R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4,
                        R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9};
        for (int i = 0; i < numIds.length; i++) {
            final String digit = String.valueOf(i);
            view.findViewById(numIds[i]).setOnClickListener(v -> inputDigit(digit));
        }

        view.findViewById(R.id.btnDot).setOnClickListener(v -> inputDot());
        view.findViewById(R.id.btnC).setOnClickListener(v -> clearAll());
        view.findViewById(R.id.btnSign).setOnClickListener(v -> toggleSign());
        view.findViewById(R.id.btnPct).setOnClickListener(v -> percent());
        view.findViewById(R.id.btnAdd).setOnClickListener(v -> handleOperator("+"));
        view.findViewById(R.id.btnSub).setOnClickListener(v -> handleOperator("-"));
        view.findViewById(R.id.btnMul).setOnClickListener(v -> handleOperator("*"));
        view.findViewById(R.id.btnDiv).setOnClickListener(v -> handleOperator("/"));
        view.findViewById(R.id.btnEq).setOnClickListener(v -> handleEquals());

        render();
        return view;
    }

    private String formatNum(String raw) {
        try {
            if (raw.equals("Lỗi")) return raw;
            double d = Double.parseDouble(raw);
            return df.format(d);
        } catch (NumberFormatException e) {
            return raw;
        }
    }

    private void render() {
        tvResult.setText(formatNum(currentInput));
        if (previousValue != null && operator != null) {
            tvHistory.setText(formatNum(String.valueOf(previousValue)) + " " + opSymbol(operator));
        } else {
            tvHistory.setText("");
        }
    }

    private String opSymbol(String op) {
        switch (op) {
            case "+": return "+";
            case "-": return "−";
            case "*": return "×";
            case "/": return "÷";
            default: return op;
        }
    }

    private void inputDigit(String digit) {
        if (waitingForOperand) {
            currentInput = digit;
            waitingForOperand = false;
        } else if (currentInput.equals("0")) {
            currentInput = digit;
        } else if (!currentInput.equals("Lỗi")) {
            if (currentInput.length() < 15) currentInput += digit;
        } else {
            currentInput = digit;
        }
        render();
    }

    private void inputDot() {
        if (waitingForOperand) {
            currentInput = "0.";
            waitingForOperand = false;
        } else if (!currentInput.contains(".") && !currentInput.equals("Lỗi")) {
            currentInput += ".";
        }
        render();
    }

    private void clearAll() {
        currentInput = "0";
        previousValue = null;
        operator = null;
        waitingForOperand = false;
        render();
    }

    private void toggleSign() {
        if (currentInput.equals("0") || currentInput.equals("Lỗi")) return;
        currentInput = currentInput.startsWith("-") ? currentInput.substring(1) : "-" + currentInput;
        render();
    }

    private void percent() {
        try {
            double val = Double.parseDouble(currentInput) / 100;
            currentInput = String.valueOf(val);
            render();
        } catch (NumberFormatException ignored) {}
    }

    private double compute(double a, double b, String op) {
        switch (op) {
            case "+": return a + b;
            case "-": return a - b;
            case "*": return a * b;
            case "/": return b == 0 ? Double.NaN : a / b;
            default: return b;
        }
    }

    private void handleOperator(String nextOp) {
        double inputValue;
        try {
            inputValue = Double.parseDouble(currentInput);
        } catch (NumberFormatException e) {
            return;
        }

        if (previousValue != null && operator != null && !waitingForOperand) {
            double result = compute(previousValue, inputValue, operator);
            if (Double.isNaN(result)) {
                currentInput = "Lỗi";
                previousValue = null;
                operator = null;
                render();
                return;
            }
            previousValue = result;
            currentInput = String.valueOf(result);
        } else {
            previousValue = inputValue;
        }

        operator = nextOp;
        waitingForOperand = true;
        render();
    }

    private void handleEquals() {
        if (previousValue == null || operator == null) return;
        double inputValue;
        try {
            inputValue = Double.parseDouble(currentInput);
        } catch (NumberFormatException e) {
            return;
        }
        double result = compute(previousValue, inputValue, operator);
        tvHistory.setText(formatNum(String.valueOf(previousValue)) + " " + opSymbol(operator) + " " + formatNum(currentInput) + " =");
        if (Double.isNaN(result)) {
            currentInput = "Lỗi";
        } else {
            currentInput = String.valueOf(result);
        }
        previousValue = null;
        operator = null;
        waitingForOperand = true;
        tvResult.setText(formatNum(currentInput));
    }
}
