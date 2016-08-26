package com.kodelabs.boilerplate.util;

import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.TextView;


public class HightLightTaskClickSpan extends ClickableSpan {

	String text;
    HightLightTaskClickListener listener;
    int color;
    boolean hasUnderline = true;

    float textSize = -1;
    boolean fakeBoldText = false;

    public HightLightTaskClickSpan(String text, int color, HightLightTaskClickListener listener) {
        super();
        this.text = text;
        this.color = color;
        this.listener = listener;
    }

    public HightLightTaskClickSpan(String text, int color, HightLightTaskClickListener listener, boolean hasUnderline) {
        super();
        this.text = text;
        this.color = color;
        this.listener = listener;
        this.hasUnderline = hasUnderline;
    }

    public HightLightTaskClickSpan(String text, int color, HightLightTaskClickListener listener, boolean hasUnderline, float textSize, boolean fakeBoldText) {
        super();
        this.text = text;
        this.color = color;
        this.listener = listener;
        this.hasUnderline = hasUnderline;
        this.textSize = textSize;
        this.fakeBoldText = fakeBoldText;
    }

    @Override
    public void updateDrawState(TextPaint ds) {
        ds.setColor(color);
        ds.setUnderlineText(hasUnderline);//去掉下划线</span>

        if(textSize != -1) {
            ds.setTextSize(textSize);
        }
        ds.setFakeBoldText(fakeBoldText);//仿“粗体”设置
    }

    @Override
    public void onClick(View widget) { 
    	if(listener != null) {
    		listener.onClick(text);
    	}
        if(widget instanceof TextView){
            ((TextView)widget).setHighlightColor(getContext().getResources().getColor(R.color.transparent));
        }
    }
    
    public interface HightLightTaskClickListener {
    	public void onClick(String text);
    }

}


