package com.lichamduong.app;
// View nền động: gradient đỏ chuyển động + hạt vàng bay lấp lánh.
import android.content.Context; import android.graphics.*; import android.util.AttributeSet; import android.view.View;
import java.util.Random;
public class AnimatedBackgroundView extends View {
 Paint paint = new Paint();
 Particle[] parts = new Particle[45];
 Random r = new Random();
 float t = 0; int w, h;
 static class Particle { float x,y,speed,size,alpha,phase; }
 public AnimatedBackgroundView(Context c){super(c);init();}
 public AnimatedBackgroundView(Context c, AttributeSet a){super(c,a);init();}
 void init(){
  for(int i=0;i<parts.length;i++){Particle p=new Particle();p.x=r.nextFloat();p.y=r.nextFloat();p.speed=0.0008f+r.nextFloat()*0.002f;p.size=2+r.nextFloat()*5;p.alpha=80+r.nextFloat()*150;p.phase=r.nextFloat()*6.28f;parts[i]=p;}
 }
 @Override protected void onSizeChanged(int nw,int nh,int o1,int o2){w=nw;h=nh;}
 @Override protected void onDraw(Canvas cv){
  super.onDraw(cv);
  if(w==0) w=getWidth(); if(h==0) h=getHeight();
  t+=0.008f;
  // Nền kem với ánh đỏ di chuyển
  paint.setStyle(Paint.Style.FILL);
  paint.setColor(Color.parseColor("#FFF9EE")); cv.drawRect(0,0,w,h,paint);
  // 3 quầng sáng di chuyển
  drawGlow(cv,w*0.2f+(float)Math.sin(t)*60,h*0.12f,280,Color.parseColor("#F2C14E"),60);
  drawGlow(cv,w*0.85f+(float)Math.cos(t*0.8f)*60,h*0.35f,340,Color.parseColor("#F8AFA6"),50);
  drawGlow(cv,w*0.5f+(float)Math.sin(t*0.6f)*80,h*0.75f,380,Color.parseColor("#FFE6A3"),45);
  // Hạt vàng bay lên
  for(Particle p:parts){
   p.y-=p.speed; if(p.y<0){p.y=1;p.x=r.nextFloat();}
   float tw=(float)(0.5+0.5*Math.sin(t*3+p.phase));
   paint.setColor(Color.argb((int)(p.alpha*(0.4f+0.6f*tw)),242,193,78));
   float px=p.x*w+(float)Math.sin(t*2+p.phase)*20;
   float py=p.y*h;
   cv.drawCircle(px,py,p.size*(0.6f+0.6f*tw),paint);
   // Lấp lánh 4 cánh cho hạt lớn
   if(p.size>5&&tw>0.7f){paint.setStrokeWidth(2);paint.setStyle(Paint.Style.STROKE);cv.drawLine(px-9,py,px+9,py,paint);cv.drawLine(px,py-9,px,py+9,paint);paint.setStyle(Paint.Style.FILL);}
  }
  postInvalidateOnAnimation();
 }
 void drawGlow(Canvas cv,float cx,float cy,float rad,int col,int alpha){
  paint.setColor(Color.argb(0,255,255,255));
  RadialGradient g=new RadialGradient(cx,cy,rad,Color.argb(alpha,col),Color.argb(0,col),Shader.TileMode.CLAMP);
  paint.setShader(g); cv.drawCircle(cx,cy,rad,paint); paint.setShader(null);
 }
}
