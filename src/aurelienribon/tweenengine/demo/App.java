package aurelienribon.tweenengine.demo;

import aurelienribon.accessors.SpriteAccessor;
import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenCallback;
import aurelienribon.tweenengine.demo.tests.Functions;
import aurelienribon.tweenengine.demo.tests.Info;
import aurelienribon.tweenengine.demo.tests.Intro;
import aurelienribon.tweenengine.demo.tests.Repetitions;
import aurelienribon.tweenengine.demo.tests.SimpleTimeline;
import aurelienribon.tweenengine.demo.tests.SimpleTween;
import aurelienribon.tweenengine.demo.tests.TimeManipulation;
import aurelienribon.tweenengine.demo.tests.Types;
import aurelienribon.tweenengine.demo.tests.Waypoints;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;

/**
 * @author Aurelien Ribon | http://www.aurelienribon.com/
 */

/**
 * add by kyuty
 * <p>
 * gdx封装的application入口类,只需要实现ApplicationListener接口,
 * 实现该接口下的create,render等方法即可
 */
public class App implements ApplicationListener {
    private SplashScreen splashScreen;
    private Launcher launcherScreen;
    private boolean isLoaded = false;

    @Override
    public void create() {
        Tween.setWaypointsLimit(10);
        Tween.setCombinedAttributesLimit(3);
        Tween.registerAccessor(Sprite.class, new SpriteAccessor());

        Assets.inst().load("data/splash/pack", TextureAtlas.class);
        Assets.inst().load("data/launcher/pack", TextureAtlas.class);
        Assets.inst().load("data/test/pack", TextureAtlas.class);
        Assets.inst().load("data/arial-16.fnt", BitmapFont.class);
        Assets.inst().load("data/arial-18.fnt", BitmapFont.class);
        Assets.inst().load("data/arial-20.fnt", BitmapFont.class);
        Assets.inst().load("data/arial-24.fnt", BitmapFont.class);
    }

    @Override
    public void dispose() {
        Assets.inst().dispose();
        if (splashScreen != null) splashScreen.dispose();
        if (launcherScreen != null) launcherScreen.dispose();
    }

    @Override
    public void render() {
//        System.out.println("render beign{");
        if (isLoaded) {
            if (splashScreen != null) splashScreen.render();
            if (launcherScreen != null) launcherScreen.render();
//            System.out.println("splashScreen = " + splashScreen);
//            System.out.println("launcherScreen = " + launcherScreen);
        } else {
            if (Assets.inst().getProgress() < 1) {
                Assets.inst().update();
//                System.out.println("Assets update");
            } else {
                launch();
                isLoaded = true;
//                System.out.println("launch()");
            }
        }
//        System.out.println("render end}");
    }

    @Override
    public void resize(int width, int height) {
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    private void launch() {
        splashScreen = new SplashScreen(new TweenCallback() {
            @Override
            /**
             * 当调到TweenCallback回调里的onEvent方法时,
             * 1.创建tests
             * 2.splashScreen dispose
             * 3.创建Launcher
             */
            public void onEvent(int type, BaseTween source) {
//                System.out.println("onEvent");
                Test[] tests = new Test[]{
                        new Intro(),
                        new Info(),
                        new SimpleTween(),
                        new SimpleTimeline(),
                        new Repetitions(),
                        new TimeManipulation(),
                        new Waypoints(),
                        new Functions(),
                        new Types()
                };

                splashScreen.dispose();
                splashScreen = null;
                launcherScreen = new Launcher(tests);
            }
        });
    }
}
