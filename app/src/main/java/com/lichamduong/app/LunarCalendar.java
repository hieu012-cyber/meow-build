package com.lichamduong.app;
// Lớp tính Âm lịch theo thuật toán lịch vạn niên (dựa trên bảng new moon).
import java.util.Calendar;
import java.util.TimeZone;
public class LunarCalendar {
 public static class LunarDate { public int day, month, year; public boolean leap; public LunarDate(int d,int m,int y,boolean l){day=d;month=m;year=y;leap=l;} }
 // Dữ liệu mã hóa lịch âm 1900-2100 (theo calendrical calculations)
 private static final int[] LUNAR_INFO = {0x04bd8,0x04ae0,0x0a570,0x054d5,0x0d260,0x0d950,0x16554,0x056a0,0x09ad0,0x055d2,0x04ae0,0x0a5b6,0x0a4d0,0x0d250,0x1d255,0x0b540,0x0d6a0,0x0ada2,0x095b0,0x14977,0x04970,0x0a4b0,0x0b4b5,0x06a50,0x06d40,0x1ab54,0x02b60,0x09570,0x052f2,0x04970,0x06566,0x0d4a0,0x0ea50,0x06e95,0x05ad0,0x02b60,0x186e3,0x092e0,0x1c8d7,0x0c950,0x0d4a0,0x1d8a6,0x0b550,0x056a0,0x1a5b4,0x025d0,0x092d0,0x0d2b2,0x0a950,0x0b557,0x06ca0,0x0b550,0x15355,0x04da0,0x0a5b0,0x14573,0x052b0,0x0a9a8,0x0e950,0x06aa0,0x0aea6,0x0ab50,0x04b60,0x0aae4,0x0a570,0x05260,0x0f263,0x0d950,0x05b57,0x056a0,0x096d0,0x04dd5,0x04ad0,0x0a4d0,0x0d4d4,0x0d250,0x0d558,0x0b540,0x0b6a0,0x195a6,0x095b0,0x049b0,0x0a974,0x0a4b0,0x0b27a,0x06a50,0x06d40,0x0af46,0x0ab60,0x09570,0x04af5,0x04970,0x064b0,0x074a3,0x0ea50,0x06b58,0x055c0,0x0ab60,0x096d5,0x092e0,0x0c960,0x0d954,0x0d4a0,0x0da50,0x07552,0x056a0,0x0abb7,0x025d0,0x092d0,0x0cab5,0x0a950,0x0b4a0,0x0baa4,0x0ad50,0x055d9,0x04ba0,0x0a5b0,0x15176,0x052b0,0x0a930,0x07954,0x06aa0,0x0ad50,0x05b52,0x04b60,0x0a6e6,0x0a4e0,0x0d260,0x0ea65,0x0d530,0x05aa0,0x076a3,0x096d0,0x04afb,0x04ad0,0x0a4d0,0x1d0b6,0x0d250,0x0d520,0x0dd45,0x0b5a0,0x056d0,0x055b2,0x049b0,0x0a577,0x0a4b0,0x0aa50,0x1b255,0x06d20,0x0ada0,0x14b63};
 private static int leapMonth(int y){ return LUNAR_INFO[y-1900] & 0xf; }
 private static int leapDays(int y){ if(leapMonth(y)!=0){ return ((LUNAR_INFO[y-1900] & 0x10000)!=0)?30:29; } return 0; }
 private static int monthDays(int y,int m){ return ((LUNAR_INFO[y-1900] & (0x10000>>m))!=0)?30:29; }
 private static int yearDays(int y){ int s=348; for(int i=0x8000;i>8;i>>=1) if((LUNAR_INFO[y-1900]&i)!=0) s++; return s+leapDays(y); }
 public static LunarDate solarToLunar(int y,int m,int d){
  Calendar c=Calendar.getInstance(TimeZone.getTimeZone("UTC")); c.set(y,m-1,d,0,0,0); c.set(Calendar.MILLISECOND,0);
  Calendar base=Calendar.getInstance(TimeZone.getTimeZone("UTC")); base.set(1900,0,31,0,0,0); base.set(Calendar.MILLISECOND,0);
  int offset=(int)((c.getTimeInMillis()-base.getTimeInMillis())/86400000L);
  int ly=1900, days; for(;ly<2101;ly++){ days=yearDays(ly); if(offset<days) break; offset-=days; }
  int lm=leapMonth(ly); boolean leap=false; int lmonth=1;
  for(int i=1;i<=12 && offset>=0;i++){
   int dm=monthDays(ly,i);
   if(offset<dm){ lmonth=i; break; }
   offset-=dm;
   if(i==lm){ int ld=leapDays(ly); if(offset<ld){ lmonth=i; leap=true; break; } offset-=ld; }
   lmonth=i+1;
  }
  return new LunarDate(offset+1,lmonth,ly,leap);
 }
 private static final String[] CAN={"Giáp","Ất","Bính","Đinh","Mậu","Kỷ","Canh","Tân","Nhâm","Quý"};
 private static final String[] CHI={"Tý","Sửu","Dần","Mão","Thìn","Tỵ","Ngọ","Mùi","Thân","Dậu","Tuất","Hợi"};
 public static String canChiYear(int lunarYear){ return CAN[(lunarYear+6)%10]+" "+CHI[(lunarYear+8)%12]; }
 public static String canChiDay(java.util.Calendar cal){
  long jd=cal.getTimeInMillis()/86400000L+2440588L; int can=(int)((jd+9)%10); int chi=(int)((jd+1)%12); return CAN[can]+" "+CHI[chi];
 }
 public static String tietKhi(int m,int d){
  // Rút gọn 24 tiết khí theo ngày dương gần đúng
  if((m==2&&d>=4)||(m==2&&d<19)) return "Lập Xuân"; if(m==2) return "Vũ Thủy";
  if(m==3&&d<6) return "Vũ Thủy"; if(m==3&&d<21) return "Kinh Trập"; if(m==3) return "Xuân Phân";
  if(m==4&&d<5) return "Xuân Phân"; if(m==4&&d<20) return "Thanh Minh"; if(m==4) return "Cốc Vũ";
  if(m==5&&d<6) return "Cốc Vũ"; if(m==5&&d<21) return "Lập Hạ"; if(m==5) return "Tiểu Mãn";
  if(m==6&&d<6) return "Tiểu Mãn"; if(m==6&&d<22) return "Mang Chủng"; if(m==6) return "Hạ Chí";
  if(m==7&&d<7) return "Hạ Chí"; if(m==7&&d<23) return "Tiểu Thử"; if(m==7) return "Đại Thử";
  if(m==8&&d<8) return "Đại Thử"; if(m==8&&d<23) return "Lập Thu"; if(m==8) return "Xử Thử";
  if(m==9&&d<8) return "Xử Thử"; if(m==9&&d<23) return "Bạch Lộ"; if(m==9) return "Thu Phân";
  if(m==10&&d<8) return "Thu Phân"; if(m==10&&d<24) return "Hàn Lộ"; if(m==10) return "Sương Giáng";
  if(m==11&&d<8) return "Sương Giáng"; if(m==11&&d<22) return "Lập Đông"; if(m==11) return "Tiểu Tuyết";
  if(m==12&&d<7) return "Tiểu Tuyết"; if(m==12&&d<22) return "Đại Tuyết"; if(m==12) return "Đông Chí";
  if(m==1&&d<6) return "Đông Chí"; if(m==1&&d<20) return "Tiểu Hàn"; if(m==1) return "Đại Hàn";
  if(m==2) return "Đại Hàn"; return "Lập Xuân";
 }
 public static String hoangDao(java.util.Calendar cal){
  String cc=canChiDay(cal); String chi=cc.split(" ")[1];
  // Giờ hoàng đạo theo địa chi ngày (rút gọn phổ biến)
  switch(chi){
   case "Tý": case "Ngọ": return "Tý, Sửu, Mão, Ngọ, Thân, Dậu";
   case "Sửu": case "Mùi": return "Dần, Mão, Tỵ, Thân, Tuất, Hợi";
   case "Dần": case "Thân": return "Tý, Sửu, Thìn, Tỵ, Mùi, Tuất";
   case "Mão": case "Dậu": return "Tý, Dần, Mão, Ngọ, Mùi, Dậu";
   case "Thìn": case "Tuất": return "Dần, Thìn, Tỵ, Thân, Dậu, Hợi";
   default: return "Sửu, Thìn, Ngọ, Mùi, Tuất, Hợi";
  }
 }
}
