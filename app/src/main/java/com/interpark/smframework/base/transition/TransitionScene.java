package com.interpark.smframework.base.transition;

import android.transition.Scene;
import android.util.Log;

import com.interpark.smframework.IDirector;
import com.interpark.smframework.base.SMScene;
import com.interpark.smframework.base.SMView;
import com.interpark.smframework.base.SceneParams;
import com.interpark.smframework.base.types.SEL_SCHEDULE;
import com.interpark.smframework.util.Size;
import com.interpark.smframework.util.Vec2;

public class TransitionScene extends SMScene {
    public TransitionScene(IDirector director) {
        super(director);
        Size size = new Size(getDirector().getWidth(), getDirector().getHeight());
        setAnchorPoint(new Vec2(0.5f, 0.5f));
        setPosition(new Vec2(size.width/2, size.height/2));
        setContentSize(size);

//        _rootView = new SMView(getDirector(), 0, 0, size.width, size.height);
//        super.addChild(_rootView);
//
//        _rootView.setBackgroundColor(1, 1, 0, 1);
    }

    enum Orientation {
        LEFT_OVER,
        RIGHT_OVER,
        UP_OVER,
        DOWN_OVER
    }

    public int enumToInt(Orientation orientation) {
        if (orientation==Orientation.LEFT_OVER || orientation==Orientation.UP_OVER) {
            return 0;
        } else {
            return 1;
        }
    }

    public static TransitionScene create(IDirector director, float t, SMScene scene) {
        TransitionScene tScene = new TransitionScene(director);
        if (tScene!=null) {
            tScene.initWithDuration(t, scene);
        }

        return tScene;
    }

    protected boolean initWithDuration(float t, SMScene scene) {
        if (super.init()) {
            _duration = t;

            _inScene = scene;
            _outScene = getDirector().getRunningScene();
            if (_outScene==null) {
                _outScene = SMScene.create(getDirector());
                _outScene.onEnter();
            }

            sceneOrder();

            return true;
        }

        return false;
    }

    protected void sceneOrder() {
        _isInSceneOnTop = true;
    }

    @Override
    public void render(float a) {
        super.render(a);

        if (_isInSceneOnTop) {
            _director.pushProjectionMatrix();
            {
                _outScene.transformMatrix(_director.getProjectionMatrix());
                _director.updateProjectionMatrix();
                _outScene.renderFrame(a);
            }
            _director.popProjectionMatrix();

            _director.pushProjectionMatrix();
            {
                _inScene.transformMatrix(_director.getProjectionMatrix());
                _director.updateProjectionMatrix();
                _inScene.renderFrame(a);
            }
            _director.popProjectionMatrix();

        } else {
            _director.pushProjectionMatrix();
            {
                _inScene.transformMatrix(_director.getProjectionMatrix());
                _director.updateProjectionMatrix();
                _inScene.renderFrame(a);
            }
            _director.popProjectionMatrix();

            _director.pushProjectionMatrix();
            {
                _outScene.transformMatrix(_director.getProjectionMatrix());
                _director.updateProjectionMatrix();
                _outScene.renderFrame(a);
            }
            _director.popProjectionMatrix();

        }
    }

    public void finish() {
        // clean up
        _inScene.setVisible(true);
//        _inScene.setPosition(new Vec2(0, 0));
        _inScene.setPosition(new Vec2(getDirector().getWinSize().width/2, getDirector().getWinSize().height/2));
        _inScene.setScale(1.0f);
        _inScene.setRotation(0);

        _outScene.setVisible(false);
//        _outScene.setPosition(new Vec2(0, 0));
        _outScene.setPosition(new Vec2(getDirector().getWinSize().width/2, getDirector().getWinSize().height/2));
        _outScene.setScale(1.0f);
        _outScene.setRotation(0);

        schedule(newSceneSchedule);
    }

    private SEL_SCHEDULE newSceneSchedule = new SEL_SCHEDULE() {
        @Override
        public void onFunc(float t) {
            setNewScene(t);
        }
    };

    protected void setNewScene(float dt) {
        unschedule(newSceneSchedule);

        _isSendCleanupToScene = getDirector().isSendCleanupToScene();

        getDirector().replaceScene(_inScene);
        _outScene.setVisible(true);
    }

    public void hideOutShowIn() {
        _inScene.setVisible(true);
        _outScene.setVisible(false);
    }

    public SMScene getInScene() {return _inScene;}
    public float getDuration() {return _duration;}

    public void TransitionSceneOnEnter() {
        SMSceneOnEnter();

        getDirector().setTouchEventDispatcherEnable(false);

        _outScene.onExitTransitionDidStart();
        _inScene.SMSceneOnEnter();
    }

    @Override
    public void onEnter() {
        super.onEnter();

        getDirector().setTouchEventDispatcherEnable(false);

        _outScene.onExitTransitionDidStart();
        _inScene.onEnter();
    }

    public void TransitionSceneOnExit() {
        SMSceneOnExit();
        getDirector().setTouchEventDispatcherEnable(true);

        _outScene.SMSceneOnExit();
        _inScene.onEnterTransitionDidFinish();
    }

    @Override
    public void onExit() {
        super.onExit();
        getDirector().setTouchEventDispatcherEnable(true);

        _outScene.onExit();
        _inScene.onEnterTransitionDidFinish();
    }

    @Override
    public void cleanup() {
        super.cleanup();

        if (_isSendCleanupToScene) {
            _outScene.cleanup();
        }
    }

    public float getLastProgress() {return _lastProgress;}

    protected float _lastProgress = -1;
    protected SMScene _inScene =  null;
    protected SMScene _outScene = null;
    protected float _duration = 0.0f;
    protected boolean _isInSceneOnTop = false;
    protected boolean _isSendCleanupToScene = false;


}
