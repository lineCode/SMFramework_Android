package com.interpark.smframework.view;

import android.opengl.GLES20;
import android.util.Log;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.view.SMShapeView;
import com.interpark.smframework.base.shape.PrimitiveRect;
import com.interpark.smframework.base.types.Color4F;
import com.interpark.smframework.util.Size;

public class SMSolidRectView extends SMShapeView {
    public SMSolidRectView(IDirector director) {
        super(director);

        bgShape = new PrimitiveRect(director, 1, 1, 0.0f, 0.0f, true);
    }

    public static SMSolidRectView create(IDirector director) {
        SMSolidRectView view = new SMSolidRectView(director);
        view.init();
        return view;
    }


    protected PrimitiveRect bgShape = null;

    private float lineWidth = 1.0f;

    @Override
    public void setBackgroundColor(final float r, final float g, final float b, final float a) {
        setColor(r, g, b, a);
    }

    @Override
    public void setBackgroundColor(final Color4F color) {
        setColor(color);
    }

    public SMSolidRectView(IDirector director, Color4F solidcolor) {
        this(director);
        setColor(solidcolor);
    }

    @Override
    protected void draw(float a) {
        super.draw(a);
        bgShape.drawScaleXY(0, 0, _contentSize.width, _contentSize.height);
    }
}
