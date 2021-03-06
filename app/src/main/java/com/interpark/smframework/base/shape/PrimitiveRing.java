package com.interpark.smframework.base.shape;

import android.opengl.GLES20;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.base.DrawNode;
import com.interpark.smframework.shader.ShaderManager;
import com.interpark.smframework.shader.ShaderProgram;
import com.interpark.smframework.shader.ProgPrimitiveRing;
import com.interpark.smframework.util.Vec2;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class PrimitiveRing extends DrawNode {
    protected FloatBuffer uv;

    private float mRadius, mThickness;
    private Vec2 _anchor = new Vec2(Vec2.MIDDLE);

    public PrimitiveRing(IDirector director) {
        this.director = director;
        drawMode = GLES20.GL_TRIANGLE_STRIP;
        numVertices = 4;

        final float[] v = {
                -1, -1,
                1, -1,
                -1,  1,
                1,  1,
        };

        this.v = ByteBuffer.allocateDirect(v.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        this.v.put(v);
        this.v.position(0);

        this.uv = ByteBuffer.allocateDirect(v.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        this.uv.put(_texCoordConst[Quadrant_ALL]);
        this.uv.position(0);

        setProgramType(ShaderManager.ProgramType.PrimitiveRing);
    }

    protected void _draw(float[] modelMatrix) {
        ShaderProgram program = useProgram();
        if (program != null) {

            if( ((ProgPrimitiveRing)program).setDrawParam(sMatrix, v, uv, mRadius, mThickness, 1.5f, _anchor)) {;
                GLES20.glDrawArrays(drawMode, 0, numVertices);
            }
        }
    }

    public void drawRing(float x, float y, float radius, float thickness) {
        mRadius = radius;
        mThickness = thickness;
        drawScale(x, y, radius);
    }
}
