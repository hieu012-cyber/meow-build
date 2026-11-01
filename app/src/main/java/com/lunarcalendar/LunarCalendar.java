package com.lunarcalendar;
// Chuyển đổi Dương lịch sang Âm lịch (thuật toán chuẩn)
public class LunarCalendar {
    private static final int[] LUNAR_INFO = {
        0x04bd8,0x04ae0,0x0a570,0x054d5,0x0d260,0x0d950,0x16554,0x056a0,0x09ad0,0x055d2,
        0x04ae0,0x0a5b6,0x0a4d0,0x0d250,0x1d255,0x0b540,0x0d6a0,0x0ada2,0x095b0,0x14977,
        0x04970,0x0a4b0,0x0b4b5,0x06a50,0x06d40,0x1ab54,0x02b60,0x09570,0x052f2,0x04970,
        0x06566,0x0d4a0,0x0ea50,0x06e95,0x05ad0,0x02b60,0x186e3,0x092e0,0x1c8d7,0x0c950,
        0x0d4a0,0x1d8a6,0x0b550,0x056a0,0x1a5b4,0x025d0,0x092d0,0x0d2b2,0x0a950,0x0b557,
        0x06ca0,0x0b550,0x15355,0x04da0,0x0a5b0,0x14573,0x052b0,0x0a9a8,0x0e950,0x06aa0,
        0x0aea6,0x0ab50,0x04b60,0x0aae4,0x0a570,0x05260,0x0f263,0x0d950,0x05b57,0x056a0,
        0x096d0,0x04dd5,0x04ad0,0x0a4d0,0x0d4d4,0x0d250,0x0d558,0x0b540,0x0b6a0,0x195a6,
        0x095b0,0x049b0,0x0a974,0x0a4b0,0x0b27a,0x06a50,0x06d40,0x0af46,0x0ab60,0x09570,
        0x04af5,0x04970,0x064b0,0x074a3,0x0ea50,0x06b58,0x055c0,0x0ab60,0x096d5,0x092e0,
        0x0c960,0x0d954,0x0d4a0,0x0da50,0x07552,0x056a0,0x0abb7,0x025d0,0x092d0,0x0cab5,
        0x0a950,0x0b4a0,0x0baa4,0x0ad50,0x055d9,0x04ba0,0x0a5b0,0x15176,0x052b0,0x0a930,
        0x07954,0x06aa0,0x0ad50,0x05b52,0x04b60,0x0a6e6,0x0a4e0,0x0d260,0x0ea65,0x0d530,
        0x05aa0,0x076a3,0x096d0,0x04afb,0x04ad0,0x0a4d0,0x1d0b6,0x0d250,0x0d520,0x0dd45,
        0x0b5a0,0x056d0,0x055b2,0x049b0,0x0a577,0x0a4b0,0x0aa50,0x1b255,0x06d20,0x0ada0
    };
    public static class LunarDate {
        public int day, month, year; public boolean leap;
        public LunarDate(int d,int m,int y,boolean l){day=d;month=m;year=y;leap=l;}
        public String toString(){return day+"/"+month+(leap?" (nhuận)":"")+"/"+year;}
    }
    private static int leapMonth(int y){return LUNAR_INFO[y-1900]&0xf;}
    private static int leapDays(int y){
        if(leapMonth(y)!=0) return ((LUNAR_INFO[y-1900]&0x10000)!=0)?30:29;
        return 0;
    }
    private static int monthDays(int y,int m){return ((LUNAR_INFO[y-1900]>>(16-m))&0x1)==1?30:29;}
    private static int yearDays(int y){
        int s=348;
        for(int i=0x8000;i>0x8;i>>=1) s+=((LUNAR_INFO[y-1900]&i)!=0)?1:0;
        return s+leapDays(y);
    }
    // Dương -> Âm
    public static LunarDate solarToLunar(int y,int m,int d){
        long offset=(dateToDays(y,m,d)-dateToDays(1900,1,31));
        int lunarYear=1900; int days=29;
        while(lunarYear<2101 && offset>0){
            days=yearDays(lunarYear); offset-=days; lunarYear++;
        }
        if(offset<0){offset+=yearDays(--lunarYear);}
        int leap=leapMonth(lunarYear); boolean isLeap=false;
        int lunarMonth=1;
        for(lunarMonth=1;lunarMonth<=12 && offset>0;lunarMonth++){
            if(leap>0 && lunarMonth==(leap+1) && !isLeap){lunarMonth--;isLeap=true;days=leapDays(lunarYear);}
            else days=monthDays(lunarYear,lunarMonth);
            offset-=days;
            if(isLeap && lunarMonth==(leap+1)) isLeap=false;
        }
        if(offset==0 && leap>0 && lunarMonth==leap+1){if(isLeap){isLeap=false;}else{isLeap=true;lunarMonth--;}}
        if(offset<0){offset+=days; lunarMonth--;}
        return new LunarDate((int)offset+1,lunarMonth,lunarYear,isLeap);
    }
    private static long dateToDays(int y,int m,int d){
        // Số ngày từ 0000-03-01 (thuật toán Howard Hinnant)
        y-=(m<=2)?1:0;
        long era=(y>=0?y:y-399)/400;
        long yoe=y-era*400;
        long mp=(m+9)%12;
        long doy=(153*mp+2)/5+d-1;
        long doe=yoe*365+yoe/4-yoe/100+doy;
        return era*146097+doe;
    }
    // Can Chi
    private static final String[] CAN={"Giáp","Ất","Bính","Đinh","Mậu","Kỷ","Canh","Tân","Nhâm","Quý"};
    private static final String[] CHI={"Tý","Sửu","Dần","Mão","Thìn","Tỵ","Ngọ","Mùi","Thân","Dậu","Tuất","Hợi"};

    // Chuẩn hoá modulo cho số âm
    private static int mod(int a,int b){return ((a%b)+b)%b;}

    public static String getCanChiYear(int lunarYear){
        return CAN[mod(lunarYear-4,10)]+" "+CHI[mod(lunarYear-4,12)];
    }

    // Can Chi của tháng âm lịch (tháng 1 = Dần)
    public static String getCanChiMonth(int lunarYear,int lunarMonth){
        int chiIndex=mod(lunarMonth+1,12); // tháng 1 -> Dần (index 2)
        int canIndex=mod((lunarYear-4)*12+(lunarMonth-1)+2,10);
        return CAN[canIndex]+" "+CHI[chiIndex];
    }

    // Can Chi của ngày dương lịch (dựa trên số ngày Julius)
    public static String getCanChiDay(int y,int m,int d){
        long jd=dateToDays(y,m,d)+1721426L; // chuyển sang số ngày Julius
        int canIndex=mod((int)(jd+9),10);
        int chiIndex=mod((int)(jd+1),12);
        return CAN[canIndex]+" "+CHI[chiIndex];
    }
}
