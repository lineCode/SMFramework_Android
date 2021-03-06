package com.interpark.smframework.shader;

import android.opengl.GLES20;

import java.nio.FloatBuffer;

public class ProgPrimitiveSolidRect extends ProgPrimitive {
    private static final String NAME_TEXTURECOORD = "inputTextureCoordinate";

    private static final String NAME_DIMEN = "dimension";
    private static final String NAME_ROUND = "round";
    private static final String NAME_AAWIDTH = "aaWidth";

    private int attrTextureCoordinate;
    private int uniformDimen;
    private int uniformRound;
    private int uniformAAWidth;

    @Override
    public void complete() {
        super.complete();
        attrTextureCoordinate = GLES20.glGetAttribLocation(programId, NAME_TEXTURECOORD);
        uniformDimen = GLES20.glGetUniformLocation(programId, NAME_DIMEN);
        uniformRound = GLES20.glGetUniformLocation(programId, NAME_ROUND);
        uniformAAWidth = GLES20.glGetUniformLocation(programId, NAME_AAWIDTH);
    }

    @Override
    public void bind() {
        super.bind();
        GLES20.glEnableVertexAttribArray(attrTextureCoordinate);
    }

    @Override
    public void unbind() {
        super.unbind();
        GLES20.glDisableVertexAttribArray(attrTextureCoordinate);
    }

    public boolean setDrawParam(float[] modelMatrix, FloatBuffer v, FloatBuffer uv, float width, float height, float roundPixels, float aaWidthPixels) {
        if (width > 0 && height > 0 && super.setDrawParam(modelMatrix, v)) {
            GLES20.glVertexAttribPointer(attrTextureCoordinate, 2, GLES20.GL_FLOAT, false, 0, uv);

            final float r, b;
            if (width > height) {
                GLES20.glUniform2f(uniformDimen, .5f, .5f*height/width);
                r = roundPixels / width;
                b = aaWidthPixels / width;
            } else {
                GLES20.glUniform2f(uniformDimen, .5f*width/height, .5f);
                r = roundPixels / height;
                b = aaWidthPixels / height;
            }

            GLES20.glUniform1f(uniformRound, r);
            GLES20.glUniform1f(uniformAAWidth, b);
            return true;
        }
        return false;
    }
}
