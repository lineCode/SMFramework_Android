package com.interpark.smframework.base.sprite;

import android.opengl.GLES20;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.base.texture.Texture;
import com.interpark.smframework.shader.ProgSprite;
import com.interpark.smframework.shader.ShaderManager.ProgramType;
import com.interpark.smframework.shader.ShaderProgram;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

public class GridSprite extends Sprite {
    public static final int DEFAULT_GRID_SIZE = 10;


    private ShortBuffer mIndices;
    private int mNumVertices;
    private int mBufferSize;
    private int mNumFaces;

    protected float mGridSize;
    protected int mNumCol;
    protected int mNumRow;
    protected float[] vertices;

    public int getNumCols() {
        return mNumCol;
    }

    public int getNumRows() {
        return mNumRow;
    }

    public float[] getVertexBuffer() {
        return vertices;
    }

    public int getNumFaces() {
        return mNumFaces;
    }

    public ShortBuffer getIndices() {
        return mIndices;
    }

    public GridSprite(IDirector director, Texture texture, float cx, float cy, int gridSize) {
        super(director);

        final float tw = texture.getWidth();
        final float th = texture.getHeight();

        initRect(director, tw, th, cx, cy);
        setProgramType(ProgramType.Sprite);

        this.tx = 0;
        this.ty = 0;
        this.tw = tw;
        this.th = th;
        this.texture = texture;
        mGridSize = Math.max(10, gridSize);

        initTextureCoordQuard();

        texture.incRefCount();
    }

    @Override
    protected void initTextureCoordQuard() {

        drawMode = GLES20.GL_TRIANGLES;

        mNumCol = (int)Math.ceil(_w / mGridSize);
        mNumRow = (int)Math.ceil(_h / mGridSize);
        mNumVertices = (mNumCol+1) * (mNumRow+1);
        mBufferSize = mNumVertices * 2;

        // create vertex & texture buffer
        vertices = new float[mBufferSize];
        float[] texcoord = new float[mBufferSize];

        int idx = 0;
        float xx, yy, uu, vv;

        for (int y = 0; y <= mNumRow; y++) {
            if (y == mNumRow) {
                yy = _h;
                vv = _h/th;
            } else {
                yy = mGridSize * y;
                vv = (float)y * mGridSize /  _h;
            }
            for (int x = 0; x <= mNumCol; x++) {
                if (x == mNumCol) {
                    xx = _w;
                    uu = _w/tw;
                } else {
                    xx = mGridSize * x;
                    uu = (float)x * mGridSize / _w;
                }
                vertices[idx  ] = xx-cx; // x coord
                vertices[idx+1] = yy-cy; // y coord
                texcoord[idx  ] = uu;
                texcoord[idx+1] = vv;

                idx += 2;
            }
        }

        int size = mBufferSize * Float.SIZE / Byte.SIZE;
        v = ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder()).asFloatBuffer();
        v.put(vertices);
        v.position(0);

        uv = ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder()).asFloatBuffer();
        uv.put(texcoord);
        uv.position(0);

        // crete index buffer
        int numQuads = mNumCol * mNumRow;
        mNumFaces = numQuads * 2;

        size = mNumFaces * 3 * 2 * Short.SIZE / Byte.SIZE;
        mIndices = ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder()).asShortBuffer();

        int vi = 0;	// vertex index
        short ll, lr, ul, ur;

        for(int index = 0; index < numQuads; index++) {
            int rowNum = index / mNumCol;
            int colNum = index % mNumCol;
            ll = (short)(rowNum * (mNumCol+1) + colNum);
            lr = (short)(ll + 1);
            ul = (short)((rowNum + 1) * (mNumCol+1) + colNum);
            ur = (short)(ul + 1);
            QuadToTrianglesWindCCWSet(mIndices, vi, ul, ur, ll, lr);
            vi += 6;
        }
        mIndices.position(0);
    }

    @Override
    protected void _draw(float[] modelMatrix) {
        ShaderProgram program = useProgram();
        if (program != null && texture != null) {
            switch (program.getType()) {
                default:
                case Sprite:
                    if (((ProgSprite)program).setDrawParam(texture, sMatrix, v, uv)) {
                        GLES20.glDrawElements(GLES20.GL_TRIANGLES, mNumFaces*3, GLES20.GL_UNSIGNED_SHORT, mIndices);
                    }
                    break;
//                case GeineEffect:
//                    if (((ProgGeineEffect)program).setDrawParam(texture, sMatrix, v, uv)) {
//                        ((ProgGeineEffect)program).setGeineValue(mGenieMinimize, mGenieBend, mGenieSide);
//                        GLES20.glDrawElements(GLES20.GL_TRIANGLES, mNumFaces*3, GLES20.GL_UNSIGNED_SHORT, mIndices);
//                    }
//                    break;
            }
        }
    }

    static void QuadToTrianglesWindCCWSet(ShortBuffer vertex, int pos, short ul, short ur, short ll, short lr) {

        vertex.position(pos);
        vertex.put(lr);
        vertex.put(ul);
        vertex.put(ll);
        vertex.put(lr);
        vertex.put(ur);
        vertex.put(ul);
    }

    private float mGenieMinimize = 0;
    private float mGenieBend = 0;
    private float mGenieSide = 0;
    public void setGeineValue(float minimize, float bend, float side) {
        mGenieMinimize = minimize;
        mGenieBend = bend;
        mGenieSide = side;
    }

    //	private static final float GROWSTEP = 0.2f;
    // http://www.codeproject.com/Articles/182242/Transforming-Images-for-Fun-A-Local-Grid-based-Ima
    public void grow(float px, float py, float value, float step, float radius) {

        float sx, sy, dx, dy;
        float r;
        int idx;
        float growStep = Math.abs(value*step);

        idx = 0;
        for (int y = 0; y <= mNumRow; y++) {
            if (y == mNumRow) {
                sy = _h-cy;
            } else {
                sy = mGridSize * y-cy;
            }
            for (int x = 0; x <= mNumCol; x++) {
                if (x == mNumCol) {
                    sx = _w-cx;
                } else {
                    sx = mGridSize * x-cx;
                }

                dx = sx - px;
                dy = sy - py;
                r = (float)Math.sqrt(dx*dx+dy*dy)/radius;
                r = (float)Math.pow(r, growStep);

                if (value > 0 && r > .001) {
                    vertices[idx  ] = px + dx/r;
                    vertices[idx+1] = py + dy/r;
                } else if (value < 0 && r > .001) {
                    vertices[idx  ] = px + dx*(r);
                    vertices[idx+1] = py + dy*(r);
                } else {
                    vertices[idx  ] = sx;
                    vertices[idx+1] = sy;
                }
                idx += 2;
            }
        }

        v.put(vertices);
        v.position(0);
    }
}
