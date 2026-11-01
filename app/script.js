document.addEventListener('DOMContentLoaded', () => {
    // =========================================================
    // 1. Chuyển tab
    // =========================================================
    const tabs = document.querySelectorAll('.tab');
    const panels = document.querySelectorAll('.panel');

    tabs.forEach((tab) => {
        tab.addEventListener('click', () => {
            const targetId = tab.dataset.tab;

            tabs.forEach((t) => {
                const isActive = t === tab;
                t.classList.toggle('active', isActive);
                t.setAttribute('aria-selected', String(isActive));
            });

            panels.forEach((p) => {
                p.classList.toggle('active', p.id === targetId);
            });
        });
    });

    // =========================================================
    // 2. MÁY TÍNH
    // =========================================================
    const calcValue = document.getElementById('calc-value');
    const calcHistory = document.getElementById('calc-history');
    const calcKeys = document.querySelector('.calc-keys');

    let currentInput = '0';      // Chuỗi số đang nhập
    let previousValue = null;    // Toán hạng trước
    let operator = null;         // Phép toán đang chờ
    let waitingForOperand = false; // Vừa bấm phép toán, chờ nhập số mới

    const formatNumber = (numStr) => {
        if (numStr === '' || numStr === '-') return numStr;
        const [intPart, decPart] = numStr.split('.');
        const formattedInt = Number(intPart).toLocaleString('en-US');
        return decPart !== undefined ? formattedInt + '.' + decPart : formattedInt;
    };

    const renderCalc = () => {
        if (!calcValue) return;
        calcValue.textContent = formatNumber(currentInput);
        if (calcHistory) {
            calcHistory.textContent = previousValue !== null && operator
                ? `${formatNumber(String(previousValue))} ${operatorSymbol(operator)}`
                : '';
        }
    };

    const operatorSymbol = (op) => ({ '+': '+', '-': '−', '*': '×', '/': '÷' }[op] || op);

    const inputDigit = (digit) => {
        if (waitingForOperand) {
            currentInput = digit;
            waitingForOperand = false;
        } else {
            currentInput = currentInput === '0' ? digit : currentInput + digit;
        }
        renderCalc();
    };

    const inputDot = () => {
        if (waitingForOperand) {
            currentInput = '0.';
            waitingForOperand = false;
        } else if (!currentInput.includes('.')) {
            currentInput += '.';
        }
        renderCalc();
    };

    const clearAll = () => {
        currentInput = '0';
        previousValue = null;
        operator = null;
        waitingForOperand = false;
        renderCalc();
    };

    const toggleSign = () => {
        if (currentInput === '0') return;
        currentInput = currentInput.startsWith('-')
            ? currentInput.slice(1)
            : '-' + currentInput;
        renderCalc();
    };

    const percent = () => {
        currentInput = String(parseFloat(currentInput) / 100);
        renderCalc();
    };

    const compute = (a, b, op) => {
        switch (op) {
            case '+': return a + b;
            case '-': return a - b;
            case '*': return a * b;
            case '/': return b === 0 ? NaN : a / b;
            default: return b;
        }
    };

    const handleOperator = (nextOp) => {
        const inputValue = parseFloat(currentInput);

        if (previousValue !== null && operator && !waitingForOperand) {
            const result = compute(previousValue, inputValue, operator);
            if (Number.isNaN(result)) {
                currentInput = 'Lỗi';
                previousValue = null;
                operator = null;
                renderCalc();
                return;
            }
            previousValue = result;
            currentInput = String(result);
        } else {
            previousValue = inputValue;
        }

        operator = nextOp;
        waitingForOperand = true;
        renderCalc();
    };

    const handleEquals = () => {
        if (previousValue === null || !operator) return;
        const inputValue = parseFloat(currentInput);
        const result = compute(previousValue, inputValue, operator);

        if (calcHistory) {
            calcHistory.textContent = `${formatNumber(String(previousValue))} ${operatorSymbol(operator)} ${formatNumber(String(inputValue))} =`;
        }

        currentInput = Number.isNaN(result) ? 'Lỗi' : String(parseFloat(result.toFixed(10)));
        previousValue = null;
        operator = null;
        waitingForOperand = true;
        if (calcValue) calcValue.textContent = formatNumber(currentInput);
    };

    if (calcKeys) {
        calcKeys.addEventListener('click', (e) => {
            const key = e.target.closest('.key');
            if (!key) return;

            if (key.dataset.num !== undefined) {
                inputDigit(key.dataset.num);
            } else if (key.dataset.op) {
                handleOperator(key.dataset.op);
            } else if (key.dataset.action) {
                switch (key.dataset.action) {
                    case 'clear': clearAll(); break;
                    case 'sign': toggleSign(); break;
                    case 'percent': percent(); break;
                    case 'dot': inputDot(); break;
                    case 'equals': handleEquals(); break;
                }
            }
        });
    }

    // Hỗ trợ bàn phím cho máy tính
    document.addEventListener('keydown', (e) => {
        const calcPanel = document.getElementById('calc');
        if (!calcPanel || !calcPanel.classList.contains('active')) return;

        if (/^[0-9]$/.test(e.key)) inputDigit(e.key);
        else if (e.key === '.') inputDot();
        else if (['+', '-', '*', '/'].includes(e.key)) handleOperator(e.key);
        else if (e.key === 'Enter' || e.key === '=') { e.preventDefault(); handleEquals(); }
        else if (e.key === 'Escape') clearAll();
        else if (e.key === '%') percent();
    });

    renderCalc();

    // =========================================================
    // 3. ĐỒNG HỒ
    // =========================================================
    const clockTime = document.getElementById('clock-time');
    const clockDate = document.getElementById('clock-date');

    const pad = (n) => String(n).padStart(2, '0');

    const updateClock = () => {
        const now = new Date();
        if (clockTime) {
            clockTime.textContent = `${pad(now.getHours())}:${pad(now.getMinutes())}:${pad(now.getSeconds())}`;
        }
        if (clockDate) {
            const days = ['Chủ Nhật', 'Thứ Hai', 'Thứ Ba', 'Thứ Tư', 'Thứ Năm', 'Thứ Sáu', 'Thứ Bảy'];
            clockDate.textContent = `${days[now.getDay()]}, ${pad(now.getDate())}/${pad(now.getMonth() + 1)}/${now.getFullYear()}`;
        }
    };
    updateClock();
    setInterval(updateClock, 1000);

    // =========================================================
    // 4. BẤM GIỜ (STOPWATCH)
    // =========================================================
    const swTime = document.getElementById('sw-time');
    const swStart = document.getElementById('sw-start');
    const swLap = document.getElementById('sw-lap');
    const swReset = document.getElementById('sw-reset');
    const swLaps = document.getElementById('sw-laps');

    let swRunning = false;
    let swElapsed = 0;        // ms đã trôi
    let swStartTime = 0;      // mốc bắt đầu
    let swTimer = null;
    let lapCount = 0;

    const formatSw = (ms) => {
        const totalCs = Math.floor(ms / 10);
        const cs = totalCs % 100;
        const totalSec = Math.floor(totalCs / 100);
        const s = totalSec % 60;
        const m = Math.floor(totalSec / 60);
        return `${pad(m)}:${pad(s)}.${pad(cs)}`;
    };

    const renderSw = () => {
        if (swTime) swTime.textContent = formatSw(swElapsed);
    };

    const tickSw = () => {
        swElapsed = Date.now() - swStartTime;
        renderSw();
    };

    if (swStart) {
        swStart.addEventListener('click', () => {
            if (!swRunning) {
                swRunning = true;
                swStartTime = Date.now() - swElapsed;
                swTimer = setInterval(tickSw, 31);
                swStart.textContent = 'Dừng';
                swStart.classList.remove('btn-primary');
                swStart.classList.add('btn-ghost');
                if (swLap) swLap.disabled = false;
            } else {
                swRunning = false;
                clearInterval(swTimer);
                swStart.textContent = 'Tiếp tục';
                swStart.classList.remove('btn-ghost');
                swStart.classList.add('btn-primary');
                if (swLap) swLap.disabled = true;
            }
        });
    }

    if (swReset) {
        swReset.addEventListener('click', () => {
            swRunning = false;
            clearInterval(swTimer);
            swElapsed = 0;
            lapCount = 0;
            renderSw();
            if (swStart) {
                swStart.textContent = 'Bắt đầu';
                swStart.classList.remove('btn-ghost');
                swStart.classList.add('btn-primary');
            }
            if (swLap) swLap.disabled = true;
            if (swLaps) swLaps.innerHTML = '';
        });
    }

    if (swLap) {
        swLap.addEventListener('click', () => {
            if (!swRunning || !swLaps) return;
            lapCount += 1;
            const li = document.createElement('li');
            const label = document.createElement('span');
            label.textContent = `Vòng ${lapCount}`;
            const value = document.createElement('span');
            value.textContent = formatSw(swElapsed);
            li.appendChild(label);
            li.appendChild(value);
            swLaps.prepend(li);
        });
    }

    // =========================================================
    // 5. THỜI TIẾT (dữ liệu mô phỏng, không cần API key)
    // =========================================================
    const weatherForm = document.getElementById('weather-form');
    const weatherCity = document.getElementById('weather-city');
    const weatherCard = document.getElementById('weather-card');

    // Bảng dữ liệu mô phỏng theo thành phố
    const weatherData = {
        'ha noi': { temp: 28, desc: 'Nhiều mây, có lúc nắng', humidity: 72, wind: 11, feels: 31 },
        'hà nội': { temp: 28, desc: 'Nhiều mây, có lúc nắng', humidity: 72, wind: 11, feels: 31 },
        'ho chi minh': { temp: 33, desc: 'Nắng nóng, chiều mưa rào', humidity: 65, wind: 14, feels: 37 },
        'hồ chí minh': { temp: 33, desc: 'Nắng nóng, chiều mưa rào', humidity: 65, wind: 14, feels: 37 },
        'sai gon': { temp: 33, desc: 'Nắng nóng, chiều mưa rào', humidity: 65, wind: 14, feels: 37 },
        'da nang': { temp: 30, desc: 'Có mây, gió nhẹ', humidity: 68, wind: 16, feels: 33 },
        'đà nẵng': { temp: 30, desc: 'Có mây, gió nhẹ', humidity: 68, wind: 16, feels: 33 },
        'hue': { temp: 29, desc: 'Mưa rào rải rác', humidity: 80, wind: 9, feels: 32 },
        'huế': { temp: 29, desc: 'Mưa rào rải rác', humidity: 80, wind: 9, feels: 32 },
        'hai phong': { temp: 26, desc: 'Mát mẻ, trời quang', humidity: 60, wind: 13, feels: 27 },
        'hải phòng': { temp: 26, desc: 'Mát mẻ, trời quang', humidity: 60, wind: 13, feels: 27 },
        'da lat': { temp: 20, desc: 'Se lạnh, có sương mù', humidity: 85, wind: 7, feels: 19 },
        'đà lạt': { temp: 20, desc: 'Se lạnh, có sương mù', humidity: 85, wind: 7, feels: 19 },
        'can tho': { temp: 31, desc: 'Nắng nhẹ, ẩm cao', humidity: 78, wind: 10, feels: 35 },
        'cần thơ': { temp: 31, desc: 'Nắng nhẹ, ẩm cao', humidity: 78, wind: 10, feels: 35 }
    };

    // Chọn icon theo nhiệt độ
    const pickWeatherIcon = (temp) => {
        if (temp >= 32) return 'fa-solid fa-sun';
        if (temp >= 27) return 'fa-solid fa-cloud-sun';
        if (temp >= 22) return 'fa-solid fa-cloud';
        return 'fa-solid fa-cloud-rain';
    };

    const renderWeather = (data, cityName) => {
        if (!weatherCard) return;

        const iconEl = weatherCard.querySelector('.weather-icon');
        const tempEl = weatherCard.querySelector('.weather-temp');
        const descEl = weatherCard.querySelector('.weather-desc');
        const humidityEl = weatherCard.querySelector('.w-humidity');
        const windEl = weatherCard.querySelector('.w-wind');
        const feelsEl = weatherCard.querySelector('.w-feels');

        if (iconEl) iconEl.className = pickWeatherIcon(data.temp) + ' weather-icon';
        if (tempEl) tempEl.textContent = data.temp + '°C';
        if (descEl) descEl.textContent = `${data.desc} — ${cityName}`;
        if (humidityEl) humidityEl.textContent = data.humidity + '%';
        if (windEl) windEl.textContent = data.wind + ' km/h';
        if (feelsEl) feelsEl.textContent = data.feels + '°C';
    };

    if (weatherForm) {
        weatherForm.addEventListener('submit', (e) => {
            e.preventDefault();
            const raw = (weatherCity ? weatherCity.value : '').trim().toLowerCase();
            if (!raw) return;

            const found = weatherData[raw];

            if (found) {
                renderWeather(found, weatherCity.value.trim());
            } else {
                // Thành phố không có trong dữ liệu: tạo dữ liệu ngẫu nhiên ổn định
                let hash = 0;
                for (let i = 0; i < raw.length; i += 1) {
                    hash = (hash * 31 + raw.charCodeAt(i)) % 100000;
                }
                const mock = {
                    temp: 20 + (hash % 16),
                    desc: 'Dữ liệu mô phỏng',
                    humidity: 55 + (hash % 40),
                    wind: 5 + (hash % 20),
                    feels: 0
                };
                mock.feels = mock.temp + 2;
                renderWeather(mock, weatherCity.value.trim());
            }
        });
    }
});
