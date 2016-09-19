package aurelienribon.tweenengine.demo;

import aurelienribon.accessors.SpriteAccessor;
import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenCallback;
import aurelienribon.tweenengine.TweenManager;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.GLCommon;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author Aurelien Ribon | http://www.aurelienribon.com/
 */

/**
 * add by kyuty
 *
 * Test抽象类,其他Scene都继承这个Test
 */
public abstract class Test {
	private final TweenManager tweenManager = new TweenManager();
	private final TextureAtlas atlas;	// atlas 大地图; 这个是gdx带的类.
	private final Sprite background;	// 背景精灵
	private final Sprite veil;			// veil:隐蔽;藏. 点返回的时候,出现的白色背景
	private final Sprite infoBack;		// 介绍信息的back背景
	private final List<Sprite> dots = new ArrayList<Sprite>(50);	// 点,轨迹点
	private boolean[] useDots;			// 有多少个sprite,就有多少个useDots. useDots表示该精灵当前是否有点轨迹
	private Callback callback;			// 回调.下面有该类的实现

	protected final OrthographicCamera camera = new OrthographicCamera();	// 正交摄像机
	protected final SpriteBatch batch = new SpriteBatch();	// 批处理的Sprite
	protected final Random rand = new Random();
	protected final BitmapFont font;						// bitmapFont 字体
	// wph / wpw == Gdx.graphics.getHeight() / Gdx.graphics.getWidth(); viewport的宽高比 和 实际窗口的宽高比 是相同的.
	// wph 和 wpw 表示 viewport的w,h
	protected final float wpw = 10;
	protected final float wph = 10 * Gdx.graphics.getHeight() / Gdx.graphics.getWidth();
	protected Sprite[] sprites;

	public Test() {
		atlas = Assets.inst().get("data/test/pack", TextureAtlas.class);	// ??? pack文件是怎么制作的,让gkx引擎下的TextureAtlas完美解析
		background = atlas.createSprite("background");	// 用TextureAtlas创建sprite
		veil = atlas.createSprite("white");
		infoBack = atlas.createSprite("white");

		// 根据窗口宽度选取不同的字体
		int w = Gdx.graphics.getWidth();
		if (w > 600) font = Assets.inst().get("data/arial-24.fnt", BitmapFont.class);	// ??? arial-24.fnt是怎么来的.
		else font = Assets.inst().get("data/arial-16.fnt", BitmapFont.class);
	}

	// -------------------------------------------------------------------------
	// Abstract API
	// -------------------------------------------------------------------------

	public abstract String getTitle();
	public abstract String getInfo();
	public abstract String getImageName();
	public abstract InputProcessor getInput();		// InputProcessor 输入事件
	protected abstract void initializeOverride();
	protected abstract void disposeOverride();		// dispose:部署;摆布
	protected abstract void renderOverride();

	// -------------------------------------------------------------------------
	// Public API
	// -------------------------------------------------------------------------

	public static interface Callback {
		/**
		 * 请求关闭
		 * @param source
         */
		public void closeRequested(Test source);
	}

	public void setCallback(Callback callback) {
		this.callback = callback;
	}

	public void initialize() {
		// 若当前为演示状态,则走演示状态下的逻辑和init
		if (isCustomDisplay()) {
			initializeOverride();
			return;
		}

		// gdx引擎的OrthographicCamera的viewportWidth参数,
		// 表示的并不是glViewport里的参数.
		// 而是表示真正的viewport的宽高.
		// 即在opengl里的宽高.不是指屏幕的实际宽高
		// 可以看一下在camera.update()的方法实现里,
		// setToOrtho的方法参数给的是宽高的倒数
		camera.viewportWidth = wpw;
		camera.viewportHeight = wph;
		camera.update();

//		System.out.println("wangdong wpw = " + wpw + " wph = " + wph);
		background.setSize(wpw, wpw * background.getHeight() / background.getWidth());	// 给精灵设置size,有缩放的含义
//		background.setSize(wpw, wph);	// 测试了一下,因为上面计算出来的height值比wph要大,所以背景正常覆盖界面,没有问题. 设置成wph也没有问题,也能覆盖整个界面.
//		System.out.println("wangdong wpw = " + wpw + " wpw * background.getHeight() / background.getWidth() = " + (wpw * background.getHeight() / background.getWidth()));
		background.setPosition(-wpw/2, -background.getHeight()/2);

		veil.setSize(wpw, wph);
		veil.setPosition(-wpw/2, -wph/2);

		infoBack.setColor(0, 0, 0, 0.3f);	// 设置 介绍信息背景 的alpha值
//		infoBack.setColor(1.0f, 0, 0, 0.3f);
		infoBack.setPosition(0, 0);

		initializeOverride();

		Tween.set(veil, SpriteAccessor.OPACITY).target(1).start(tweenManager);		//1.向tweenManager里放tween(动画) 2.set veil's opacity = 1
		Tween.to(veil, SpriteAccessor.OPACITY, 0.5f).target(0).start(tweenManager);	//将veil's opacity经过0,5f的时间,从原来的1变为0(target设置的0)
	}

	public void dispose() {
		tweenManager.killAll();
		dots.clear();
		sprites = null;
		useDots = null;

		disposeOverride();
	}

	public void render() {
		// 若当前为演示状态,则走演示状态下的逻辑和render
		if (isCustomDisplay()) {
			renderOverride();
			return;
		}

		// update

		tweenManager.update(Gdx.graphics.getDeltaTime());

		for (int i=0; i<dots.size(); i++) {
			if (dots.get(i).getScaleX() < 0.1f) {
				dots.remove(i);
			}
		}

		// render

		GLCommon gl = Gdx.gl;
		gl.glClearColor(1, 1, 1, 1);
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		gl.glEnable(GL10.GL_BLEND);
		gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
//		gl.glViewport(0,0,Gdx.graphics.getWidth(),Gdx.graphics.getHeight()); // 经过我测试:gdx引擎glViewport默认的是屏幕的宽高
//		gl.glViewport(0,0,10,10);

		int w = Gdx.graphics.getWidth();
		int h = Gdx.graphics.getHeight();
//		System.out.println("w = " + w + " h = " + h); // 800 480 这是实际的屏幕宽高

//		System.out.println("1 viewportWidth = " + camera.viewportWidth + " viewportHeight = " + camera.viewportHeight);
		// 先给batch设置投影矩阵,再begin,end
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		batch.disableBlending();
		background.draw(batch);
		batch.enableBlending();
		for (int i=0; i<dots.size(); i++) dots.get(i).draw(batch);
		for (int i=0; i<sprites.length; i++) sprites[i].draw(batch);
		batch.end();

		renderOverride(); // 编程技巧:父类有个render()方法和renderOverride()方法,
						  // 父类在render方法里调用一下renderOverride方法.同时将renderOverride方法写成protected abstract类型的.
						  // 子类通过继承父类,实现自己的renderOverride.即可以实现公共的render和自己特有的render.

		// 绘制 透明的背景 和 背景上白色的字体
		if (getInfo() != null) {
			int padding = 15;	// 上下左右的间距
			BitmapFont.TextBounds bs = font.getWrappedBounds(getInfo(), w - padding*2);	// getInfo():要显示的String. w-padding*2:字符串所占的宽度. wrap:包裹
			infoBack.setSize(w, bs.height + padding*2);	// w:屏幕的宽. bs.height+padding*2:字符串所占的高+上下两间距
			font.setColor(Color.WHITE);

			// 先给batch设置投影矩阵,再begin,end
			batch.getProjectionMatrix().setToOrtho2D(0, 0, w, h);	// 若想在屏幕上显示字体这种,平面效果,可以将setOrtho的参数设置为0,0,屏幕的宽,屏幕的高.
																	// 这样opengl里的w和h就和屏幕的w和h一样了.
			batch.begin();
			infoBack.draw(batch);
			font.drawWrapped(batch, getInfo(), padding, bs.height + padding, w - padding*2);	// 第一个参数是batch, 第二个参数是字符串,
																								// 第三个参数是指x, 第四个参数是y, 第四个参数是字符串所占的宽度
			batch.end();
		}

		// 只有在点开test的时候,才绘制veil
		if (veil.getColor().a > 0.1f) {
//			System.out.println("draw");
			// 先给batch设置投影矩阵,在begin,end
			batch.setProjectionMatrix(camera.combined);
			batch.begin();
			veil.draw(batch);
			batch.end();
		}
//		else {
//			System.out.println("no draw");
//		}
	}

	// -------------------------------------------------------------------------
	// Helpers
	// -------------------------------------------------------------------------

	/**
	 * 当前是否是普通的演示(intro).
	 * Intro类需要重写该类,将返回值设置为true.表示当前为普通的演示状态.
	 * @return
     */
	protected boolean isCustomDisplay() {
		return false;
	}

	/**
	 * 强制close
	 */
	protected void forceClose() {
		if (callback != null) callback.closeRequested(this);
	}

	/**
	 * 创建cnt个sprite
	 * sprite是4个精灵随机创建的
	 * useDots属性初始为false
	 * @param cnt
     */
	protected void createSprites(int cnt) {
		sprites = new Sprite[cnt];
		useDots = new boolean[cnt];

		for (int i=0; i<cnt; i++) {
			int idx = rand.nextInt(400)/100 + 1;	// 随机生成[1-4]之间的数
			sprites[i] = atlas.createSprite("sprite" + idx);
			sprites[i].setSize(1f, 1f * sprites[i].getHeight() / sprites[i].getWidth());
			sprites[i].setOrigin(sprites[i].getWidth()/2, sprites[i].getHeight()/2);
			useDots[i] = false;
		}
	}

	/**
	 * sprite 绕自己的中心点 translate x,y
	 * @param sp
	 * @param x
     * @param y
     */
	protected void center(Sprite sp, float x, float y) {
		sp.setPosition(x - sp.getWidth()/2, y - sp.getHeight()/2);
	}

	/**
	 * 开启sprite's dots属性,并tween call动画
	 * @param spriteId
     */
	protected void enableDots(int spriteId) {
		useDots[spriteId] = true;

		Tween.call(dotCallback)
			.delay(0.02f)
			.repeat(-1, 0.02f)
			.setUserData(spriteId)
			.start(tweenManager);	// ??? 不是很明白call函数
	}

	protected void disableDots(int spriteId) {
		useDots[spriteId] = false;
	}

	private final Vector2 v2 = new Vector2();	// 表示opengl的世界坐标
	private final Vector3 v3 = new Vector3();	// v3.x表示屏幕坐标x; v3.y表示屏幕坐标y; v3.z表示屏幕坐标(0)

	/**
	 * 屏幕坐标转世界坐标
	 * @param x	屏幕坐标x
	 * @param y	屏幕坐标y
     * @return	这里只返回了Vector2,因为这里逻辑上不需要z值,所以就返回了Vector2
     */
	protected Vector2 touch2world(int x, int y) {
		v3.set(x, y, 0);
		camera.unproject(v3);
		return v2.set(v3.x, v3.y);
	}

	// -------------------------------------------------------------------------
	// Helpers
	// -------------------------------------------------------------------------

	private final TweenCallback dotCallback = new TweenCallback() {
		@Override
		public void onEvent(int type, BaseTween source) {
			int spriteId = (Integer) source.getUserData();

			if (useDots[spriteId] == false) source.kill();
			Sprite sp = sprites[spriteId];

			Sprite dot = atlas.createSprite("dot");
			dot.setSize(0.2f, 0.2f);	// 宽高设置为0.2,0.2
			dot.setOrigin(0.1f, 0.1f);	// origin原点设置为0.1,0.1(中心点) 注意:这个setOrigin影响的只是精灵的缩放和旋转时的原点,translate的原点还是左下角.(看setOrigin方法的api)

			// 设置到sprite的中心位置
			dot.setPosition(sp.getX(), sp.getY());
			dot.translate(sp.getWidth()/2, sp.getHeight()/2);
			dot.translate(-dot.getWidth()/2, -dot.getHeight()/2);

			dots.add(dot);
			Tween.to(dot, SpriteAccessor.SCALE_XY, 1.0f).target(0, 0).start(tweenManager);	// 设置dot's scale的x,y为0,0
		}
	};

	// -------------------------------------------------------------------------
	// Dummy
	// -------------------------------------------------------------------------

	public static final Test dummy = new Test() {
		@Override public String getTitle() {return "Dummy test";}
		@Override public String getInfo() {return null;}
		@Override public String getImageName() {return null;}
		@Override public InputProcessor getInput() {return null;}
		@Override protected void initializeOverride() {}
		@Override protected void disposeOverride() {}
		@Override protected void renderOverride() {}
	};
}
