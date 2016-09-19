package aurelienribon.tweenengine.demo;

import aurelienribon.accessors.SpriteAccessor;
import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenCallback;
import aurelienribon.tweenengine.TweenManager;
import aurelienribon.tweenengine.equations.Cubic;
import aurelienribon.tweenengine.equations.Quart;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.GLCommon;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector3;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Aurelien Ribon | http://www.aurelienribon.com/
 */
public class Launcher {
	private static final int TILES_PER_LINE = 3;		// tiles的行数
	private static final float TILES_PADDING = 0.04f;	// tiles的padding

	private final List<Tile> tiles = new ArrayList<Tile>();
	private final TweenManager tweenManager = new TweenManager();
	private final OrthographicCamera camera = new OrthographicCamera();
	private final SpriteBatch batch = new SpriteBatch();
	private final BitmapFont font;
	private final Sprite background;
	private final Sprite title;			// title的背景(黑色)
	private final Sprite titleLeft;		// "Universal Tween Engine"
	private final Sprite titleRight;	// "Aurelien Ribon www.aurelineribon.com"
	private final Sprite veil;
	private final float tileW, tileH;
	private Tile selectedTile;			// 被选中的Tile

	public Launcher(Test[] tests) {
		int w = Gdx.graphics.getWidth();
		int h = Gdx.graphics.getHeight();
		float wpw = 2;
		float wph = wpw * h / w;

		camera.viewportWidth = wpw;
		camera.viewportHeight = wph;
		camera.update();

		font = Assets.inst().get("data/arial-18.fnt", BitmapFont.class);
		font.setScale(0.0025f);
		font.setUseIntegerPositions(false);

		TextureAtlas atlas = Assets.inst().get("data/launcher/pack", TextureAtlas.class);
		background = atlas.createSprite("background");
		title = atlas.createSprite("title");
		titleLeft = atlas.createSprite("title-left");
		titleRight = atlas.createSprite("title-right");
		veil = atlas.createSprite("white");

		background.setSize(w, w * background.getHeight() / background.getWidth());
		background.setPosition(0, h/2 - background.getHeight()/2);

		float titleHmts = wph/8;				// title的高度(opengl世界坐标系下的)
		float titleHpxs = titleHmts * h / wph;	// title的高度(屏幕的实际高度)
		titleLeft.setSize(titleHpxs* titleLeft.getWidth() / titleLeft.getHeight(), titleHpxs);
		titleLeft.setPosition(0, h);	// y坐标设置成h,是为了后面做动画使用(动画:从上到下慢慢出现title)
		titleRight.setSize(titleHpxs * titleRight.getWidth() / titleRight.getHeight(), titleHpxs);
		titleRight.setPosition(w-titleRight.getWidth(), h);	// y坐标设置成h,是为了后面做动画使用(动画:从上到下慢慢出现title)
		title.setSize(w, titleHpxs);
		title.setPosition(0, h);	// y坐标设置成h,是为了后面做动画使用(动画:从上到下慢慢出现title)

		veil.setSize(w, h);
		Tween.to(veil, SpriteAccessor.OPACITY, 1f).target(0).delay(0.5f).setCallback(veilEndCallback).start(tweenManager);	// veil渐渐消失.消失完之后,执行veilEndCallback回调
																															// (该回调执行卡片向左移动等动画)

		Gdx.input.setInputProcessor(launcherInputProcessor);

		tileW = (wpw-TILES_PADDING)/TILES_PER_LINE - TILES_PADDING;	// opengl坐标系下tile的width
		tileH = tileW * 150 / 250;	// opengl坐标系下tile的height
		float tileX = -wpw/2 + TILES_PADDING;	// opengl坐标系下第一个tile的x坐标
		float tileY = wph/2 - tileH - TILES_PADDING - titleHmts;	// opengl坐标系下第一个tile的y坐标

		// 设置所有tile的位置和回调
		for (int i=0; i<tests.length; i++) {
			tiles.add(new Tile(tileX, tileY, tileW, tileH, tests[i], atlas, camera, font, tweenManager));
			tests[i].setCallback(testCallback);

			tileX += tileW + TILES_PADDING;
			if (i > 0 && i%TILES_PER_LINE == TILES_PER_LINE-1) {
				tileX = -camera.viewportWidth/2 + TILES_PADDING;
				tileY += -tileH - TILES_PADDING;
			}
		}
	}

	public void dispose() {
		tweenManager.killAll();
		batch.dispose();
		font.dispose();
	}

	public void render() {
		tweenManager.update(Gdx.graphics.getDeltaTime());

		GLCommon gl = Gdx.gl;
		gl.glClearColor(1, 1, 1, 1);
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		gl.glEnable(GL10.GL_BLEND);
		gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);

		int w = Gdx.graphics.getWidth();
		int h = Gdx.graphics.getHeight();

		if (selectedTile == null) {
			batch.getProjectionMatrix().setToOrtho2D(0, 0, w, h);
			batch.begin();
			batch.disableBlending();
			background.draw(batch);
			batch.end();

			batch.setProjectionMatrix(camera.combined);
			batch.begin();
			batch.enableBlending();
			for (int i=0; i<tiles.size(); i++) tiles.get(i).draw(batch);
			batch.end();

			batch.getProjectionMatrix().setToOrtho2D(0, 0, w, h);
			batch.begin();
			batch.disableBlending();
			title.draw(batch);
			titleLeft.draw(batch);
			titleRight.draw(batch);
			batch.enableBlending();
			if (veil.getColor().a > 0.1f) veil.draw(batch);
			batch.end();

		} else {
			selectedTile.getTest().render();
		}
	}

	/**
	 * title从上到下show出来
	 * @param delay
     */
	private void showTitle(float delay) {
		float dy = -title.getHeight();
		Tween.to(title, SpriteAccessor.POS_XY, 0.5f).targetRelative(0, dy).delay(delay).ease(Quart.OUT).start(tweenManager);
		Tween.to(titleLeft, SpriteAccessor.POS_XY, 0.5f).targetRelative(0, dy).delay(delay).ease(Quart.OUT).start(tweenManager);
		Tween.to(titleRight, SpriteAccessor.POS_XY, 0.5f).targetRelative(0, dy).delay(delay).ease(Quart.OUT).start(tweenManager);
	}

	/**
	 * title从下到上隐藏起来
	 * @param delay
     */
	private void hideTitle(float delay) {
		float dy = title.getHeight();
		Tween.to(title, SpriteAccessor.POS_XY, 0.3f).targetRelative(0, dy).delay(delay).ease(Cubic.IN).start(tweenManager);
		Tween.to(titleLeft, SpriteAccessor.POS_XY, 0.3f).targetRelative(0, dy).delay(delay).ease(Cubic.IN).start(tweenManager);
		Tween.to(titleRight, SpriteAccessor.POS_XY, 0.3f).targetRelative(0, dy).delay(delay).ease(Cubic.IN).start(tweenManager);
	}

	/**
	 * 当从test界面返回到launcher界面时,会调到这个方法
	 *
	 * 关闭test动画和title下移动画是同时进行的
	 */
	private void closeSelectedTile() {
		selectedTile.minimize(minimizeCallback);
		selectedTile = null;
		Gdx.input.setInputProcessor(null);
		showTitle(0.2f);
	}

	private final Test.Callback testCallback = new Test.Callback() {
		@Override
		public void closeRequested(Test source) {
			closeSelectedTile();
		}
	};

	// -------------------------------------------------------------------------
	// Callbacks
	// -------------------------------------------------------------------------

	/**
	 * launcher界面的veil动画走完之后,会掉这个回调方法
	 */
	private final TweenCallback veilEndCallback = new TweenCallback() {
		@Override
		public void onEvent(int type, BaseTween source) {
			showTitle(0);
			for (int i=0; i<tiles.size(); i++) {
				int row = i / TILES_PER_LINE;
				int col = i % TILES_PER_LINE;
				float delay = row * 0.07f + col * 0.15f;
				tiles.get(i).enter(delay);
			}
		}
	};

	/**
	 * 点击卡片之后(touchUp时),tile's maximize方法里的动画走完之后,会调这个回调方法.
	 */
	private final TweenCallback maximizeCallback = new TweenCallback() {
		@Override
		public void onEvent(int type, BaseTween source) {
			selectedTile = (Tile) source.getUserData();
			selectedTile.getTest().initialize();
			Gdx.input.setInputProcessor(testInputMultiplexer);	// 将inputMultiplexer设置到input里
			Gdx.input.setCatchBackKey(true);

			testInputMultiplexer.clear();	// 清空inputMultiplexer
			testInputMultiplexer.addProcessor(testInputProcessor);	// 添加inputProcessor(添加 关闭test动画 和 title下滑动画)
			if (selectedTile.getTest().getInput() != null) {
				testInputMultiplexer.addProcessor(selectedTile.getTest().getInput());	// 添加选中的test自己特有的input事件
			}
		}
	};

	/**
	 * 从test界面返回时,tile's minimize方法里的动画走完之后,会调这个回调方法.
	 */
	private final TweenCallback minimizeCallback = new TweenCallback() {
		@Override
		public void onEvent(int type, BaseTween source) {
			Tile tile = (Tile) source.getUserData();
			tile.getTest().dispose();
			Gdx.input.setInputProcessor(launcherInputProcessor);
			Gdx.input.setCatchBackKey(false);
		}
	};

	// -------------------------------------------------------------------------
	// Inputs
	// -------------------------------------------------------------------------

	private final InputProcessor launcherInputProcessor = new InputAdapter() {
		private boolean isDragged;
		private float firstY;
		private float lastY;

		@Override
		public boolean touchDown(int x, int y, int pointer, int button) {
			firstY = lastY = y;
			isDragged = false;
			return true;
		}

		@Override
		public boolean touchDragged(int x, int y, int pointer) {
			float threshold = 0.5f * Gdx.graphics.getPpcY();
			if (Math.abs(y - firstY) > threshold && !isDragged) {
				isDragged = true;
				lastY = y;
			}

			if (isDragged) {
				float dy = (y - lastY) * camera.viewportHeight / Gdx.graphics.getHeight();
				camera.translate(0, dy, 0);
				trimCamera();
				camera.update();
				lastY = y;
			}

			return true;
		}

		@Override
		public boolean touchUp(int x, int y, int pointer, int button) {
			if (!isDragged) {
				Vector3 v = new Vector3(x, y, 0);
				camera.unproject(v);

				Tile tile = getOverTile(v.x, v.y);

				if (tile != null) {
					tiles.remove(tile);
					tiles.add(tile);
					tile.maximize(maximizeCallback);
					Gdx.input.setInputProcessor(null);
					hideTitle(0.4f);
				}
			}

			return true;
		}

		@Override
		public boolean scrolled(int amount) {
			camera.position.y += amount > 0 ? -0.1f : 0.1f;
			trimCamera();
			camera.update();
			return true;
		}

		/**
		 * 若点击到tile,则返回tile 否则返回null
		 * @param x	opengl世界的坐标
		 * @param y opengl世界的坐标
         * @return
         */
		private Tile getOverTile(float x, float y) {
			for (int i=0; i<tiles.size(); i++)
				if (tiles.get(i).isOver(x, y)) return tiles.get(i);
			return null;
		}

		/**
		 * 滚轮滚动时,移动camera
		 */
		private void trimCamera() {
			int linesCntMinusOne = Math.max(tiles.size()-1, 0) / TILES_PER_LINE;
			float min = -linesCntMinusOne * (tileH + TILES_PADDING) + camera.viewportHeight/2;
			float max = 0;

			camera.position.y = Math.max(camera.position.y, min);
			camera.position.y = Math.min(camera.position.y, max);
		}
	};

	private final InputMultiplexer testInputMultiplexer = new InputMultiplexer();
	private final InputProcessor testInputProcessor = new InputAdapter() {
		/**
		 * 当在test界面按下esc键或者左击鼠标时,调用关闭test动画和title下滑的动画
		 * @param keycode
         * @return
         */
		@Override
		public boolean keyDown(int keycode) {
			if ((keycode == Keys.BACK || keycode == Keys.ESCAPE) && selectedTile != null) {
				closeSelectedTile();
			}

			return false;
		}
	};
}
