package aurelienribon.tweenengine.demo;

import aurelienribon.accessors.SpriteAccessor;
import aurelienribon.tweenengine.Timeline;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenCallback;
import aurelienribon.tweenengine.TweenManager;
import aurelienribon.tweenengine.equations.Back;
import aurelienribon.tweenengine.equations.Cubic;
import aurelienribon.tweenengine.equations.Quad;
import aurelienribon.tweenengine.equations.Quart;
import aurelienribon.tweenengine.equations.Quint;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.GLCommon;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector3;

/**
 * @author Aurelien Ribon | http://www.aurelienribon.com/
 */

/**
 * add by kyuty
 *
 * 介绍动画的界面
 */
public class SplashScreen {
	private static final int PX_PER_METER = 400;	// 转化单位

	private final OrthographicCamera camera = new OrthographicCamera();
	private final SpriteBatch batch = new SpriteBatch();
	private final TweenManager tweenManager = new TweenManager();
	private final TweenCallback callback;
	private final Sprite universal;
	private final Sprite tween;
	private final Sprite engine;
	private final Sprite logo;	// 蓝色正方体盒子
	private final Sprite strip;	// 白色背景,y轴要做缩放动画
	private final Sprite powered;	// "Render powered by" 精灵
	private final Sprite gdx;	// libgdx的logo,静态logo和模糊logo通过换图设置到该精灵上
	private final Sprite veil;	// 开场的白色背景
	private final TextureRegion gdxTex;	// libgdx的静态logo. Texture

	public SplashScreen(TweenCallback callback) {
		this.callback = callback;

		TextureAtlas atlas = Assets.inst().get("data/splash/pack", TextureAtlas.class);
		universal = atlas.createSprite("universal");
		tween = atlas.createSprite("tween");
		engine = atlas.createSprite("engine");
		logo = atlas.createSprite("logo");
		strip = atlas.createSprite("white");
		powered = atlas.createSprite("powered");
		gdx = atlas.createSprite("gdxblur");
		veil = atlas.createSprite("white");
		gdxTex = atlas.findRegion("gdx");

		float wpw = 1f;
		float wph = wpw * Gdx.graphics.getHeight() / Gdx.graphics.getWidth();

		camera.viewportWidth = wpw;
		camera.viewportHeight = wph;
		camera.update();

		Gdx.input.setInputProcessor(inputProcessor);	// 设置inputProcessor. InputProcessor接口里有关于touch的事件.

		Sprite[] sprites = new Sprite[] {universal, tween, engine, logo, powered, gdx};
		for (Sprite sp : sprites) {
			sp.setSize(sp.getWidth()/PX_PER_METER, sp.getHeight()/PX_PER_METER);
			sp.setOrigin(sp.getWidth()/2, sp.getHeight()/2);
		}

		// 设置好各自的位置. 至于动画,通过targetRelative来写参数(相对值).
		universal.setPosition(-0.325f, 0.028f);
		tween.setPosition(-0.320f, -0.066f);
		engine.setPosition(0.020f, -0.087f);
		logo.setPosition(0.238f, 0.022f);

		strip.setSize(wpw, wph);
		strip.setOrigin(wpw/2, wph/2);
		strip.setPosition(-wpw/2, -wph/2);

		powered.setPosition(-0.278f, -0.025f);
		gdx.setPosition(0.068f, -0.077f);

		veil.setSize(wpw, wph);
		veil.setPosition(-wpw/2, -wph/2);
		veil.setColor(1, 1, 1, 0);	// a初始设置为0

		// target和targetRelative的区别:target是变为多少,targetRalative是相对变为多少
		// 一条条push的tween是依次执行的,不是同时执行的.
		// 如果想同时执行,用beginParallel()和end()
		// 可以通过pushPause(-0.3f)将两个tween同时执行
		// 可以通过pushPause(0.5f)推迟动画开始执行的时间
		Timeline.createSequence()
			.push(Tween.set(tween, SpriteAccessor.POS_XY).targetRelative(-1, 0))		// 设置tween,相对于原来的位置,移动(-1,0). 在最左侧.
			.push(Tween.set(engine, SpriteAccessor.POS_XY).targetRelative(1, 0))		// 设置engine,相对于原来的位置,移动(1,0). 在最右侧.
			.push(Tween.set(universal, SpriteAccessor.POS_XY).targetRelative(0, 0.5f))	// 设置universal,相对于原来的位置,移动(0,0.5f). 在上侧.
			.push(Tween.set(logo, SpriteAccessor.SCALE_XY).target(7, 7))	// 设置logo's scale:x = 7 y = 7
			.push(Tween.set(logo, SpriteAccessor.OPACITY).target(0))		// 设置logo's opacity:a = 0
			.push(Tween.set(strip, SpriteAccessor.SCALE_XY).target(1, 0))	// 设置strip's scale:x = 1 y = 0
			.push(Tween.set(powered, SpriteAccessor.OPACITY).target(0))		// 设置powered's opacity:a = 0
			.push(Tween.set(gdx, SpriteAccessor.OPACITY).target(0))			// 设置gdx's opacity:a = 0

			.pushPause(0.5f)
			.push(Tween.to(strip, SpriteAccessor.SCALE_XY, 0.8f).target(1, 0.6f).ease(Back.OUT))				// 设置strip's scale:(x,y)经过0.8s变为(1,0.6f). 竖直拉伸的效果
			.push(Tween.to(tween, SpriteAccessor.POS_XY, 0.5f).targetRelative(1, 0).ease(Quart.OUT))			// 设置tween,相对于原来的位置,经过0.5s移动(1,0). 回到中间的效果
			.push(Tween.to(engine, SpriteAccessor.POS_XY, 0.5f).targetRelative(-1, 0).ease(Quart.OUT))			// 设置engine,相对于原来的位置,经过0.5s移动(-1,0). 回到中间的效果
			.push(Tween.to(universal, SpriteAccessor.POS_XY, 0.6f).targetRelative(0, -0.5f).ease(Quint.OUT))	// 设置universal,相对于原来的位置,经过0.6s移动(0,-0.5f). 回到中间的效果
			.pushPause(-0.3f)	// 将下面的动画提前0.3s执行
			.beginParallel()	// 平行执行
				.push(Tween.set(logo, SpriteAccessor.OPACITY).target(1))	// 设置logo's opacity:a = 1
				.push(Tween.to(logo, SpriteAccessor.SCALE_XY, 0.5f).target(1, 1).ease(Back.OUT))	// 设置logo's scale:(x,y)经过0,5s时间变为(1,1). 由大变小的效果.
			.end()
			.push(Tween.to(strip, SpriteAccessor.SCALE_XY, 0.5f).target(1, 1).ease(Back.IN))	// 设置strip's scale:(x,y)经过0.5s变为(1,1). 竖直拉伸的效果
			.pushPause(0.3f)
			.beginParallel()
				// 四个sprite向右移动. 向右消失"退场"的效果
				.push(Tween.to(tween, SpriteAccessor.POS_XY, 0.5f).targetRelative(1, 0).ease(Back.IN))
				.push(Tween.to(engine, SpriteAccessor.POS_XY, 0.5f).targetRelative(1, 0).ease(Back.IN))
				.push(Tween.to(universal, SpriteAccessor.POS_XY, 0.5f).targetRelative(1, 0).ease(Back.IN))
				.push(Tween.to(logo, SpriteAccessor.POS_XY, 0.5f).targetRelative(1, 0).ease(Back.IN))
			.end()

			.pushPause(-0.3f)
			.push(Tween.to(powered, SpriteAccessor.OPACITY, 0.3f).target(1))	// 设置powered's opacity:a经过0.3s变为1. 渐渐出现的效果
			.beginParallel()
				.push(Tween.to(gdx, SpriteAccessor.OPACITY, 1.5f).target(1).ease(Cubic.IN))	// 设置gdx's opacity:a经过1.5s变为1. 渐渐出现的效果
				.push(Tween.to(gdx, SpriteAccessor.ROTATION, 2.0f).target(360*15).ease(Quad.OUT))	// 设置gdx's rotation经过2.0s变为360*15. 旋转的效果
			.end()
			.pushPause(0.3f)
			.push(Tween.to(gdx, SpriteAccessor.SCALE_XY, 0.6f).waypoint(1.6f, 0.4f).target(1.2f, 1.2f).ease(Cubic.OUT))	// gdx's scale的(x,y) 先变为(1.6f,0.4f)再变为(1.2f,1.2f).
																														// 横向:先变宽后变窄的效果 竖向:先变窄后变宽的效果
			.pushPause(0.3f)
			.beginParallel()
				// 两个sprite向右移动. 向右消失"退场"的效果
				.push(Tween.to(powered, SpriteAccessor.POS_XY, 0.5f).targetRelative(1, 0).ease(Back.IN))
				.push(Tween.to(gdx, SpriteAccessor.POS_XY, 0.5f).targetRelative(1, 0).ease(Back.IN))
			.end()
			.pushPause(0.3f)

			.setCallback(callback)	// 设置TweenCallback,动画都走完之后,会调用TweenCallbck的onEvent方法.
			.start(tweenManager);
	}

	public void dispose() {
		tweenManager.killAll();	// 动画管理器kill掉
		batch.dispose();	// SpriteBatch dispose掉
	}

	public void render() {
		tweenManager.update(Gdx.graphics.getDeltaTime());

		if (gdx.getRotation() > 360*15-20)
			gdx.setRegion(gdxTex);	// 设置新的textrue(静态的gdx图标)

		GLCommon gl = Gdx.gl;
		gl.glClearColor(0, 0, 0, 1);
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		gl.glEnable(GL10.GL_BLEND);
		gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);

		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		strip.draw(batch);
		universal.draw(batch);
		tween.draw(batch);
		engine.draw(batch);
		logo.draw(batch);
		powered.draw(batch);
		gdx.draw(batch);
		if (veil.getColor().a > 0.1f) veil.draw(batch);	// veil最后画
		batch.end();
	}

	/**
	 * 介绍界面的Input事件
	 *
	 * 目的:点击之后,执行Tween动画.
	 * 执行完动画之后,执行回调函数里的onEvent方法.
	 */
	private final InputProcessor inputProcessor = new InputAdapter() {
		@Override
		// touchUp时,veil出现.
		public boolean touchUp(int x, int y, int pointer, int button) {
			// veil's opacity经过0.7f秒变为1,然后执行callback的onEvent方法
			Tween.to(veil, SpriteAccessor.OPACITY, 0.7f)
				.target(1)
				.setCallback(callback)
				.start(tweenManager);
			return true;
		}
	};

	private final InputProcessor placeAssetsInputProcessor = new InputAdapter() {
		private Sprite draggedSprite;
		private float lastX, lastY;

		@Override
		public boolean touchDown(int x, int y, int pointer, int button) {
			Vector3 v = new Vector3(x, y, 0);
			camera.unproject(v);

			draggedSprite = null;

			Sprite[] sprites = new Sprite[] {powered, gdx};
			for (Sprite sp : sprites) {
				if (sp.getX() <= v.x && v.x <= sp.getX() + sp.getWidth()
					&& sp.getY() <=v.y && v.y <= sp.getY() + sp.getHeight()) {
					draggedSprite = sp;
					break;
				}
			}

			lastX = x; lastY = y;
			return true;
		}

		@Override
		public boolean touchDragged(int x, int y, int pointer) {
			if (draggedSprite != null) {
				float dx = (x - lastX) * camera.viewportWidth / Gdx.graphics.getWidth();
				float dy = (lastY - y) * camera.viewportHeight / Gdx.graphics.getHeight();
				draggedSprite.translate(dx, dy);
			}

			lastX = x; lastY = y;
			return true;
		}

		@Override
		public boolean touchUp(int x, int y, int pointer, int button) {
			System.out.println("powered: " + powered.getX() + " " + powered.getY());
			System.out.println("gdx: " + gdx.getX() + " " + gdx.getY());
			System.out.println();
			return true;
		}
	};
}
