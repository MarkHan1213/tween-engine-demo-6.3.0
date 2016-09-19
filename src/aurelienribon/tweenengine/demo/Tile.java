package aurelienribon.tweenengine.demo;

import aurelienribon.accessors.SpriteAccessor;
import aurelienribon.tweenengine.Timeline;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenCallback;
import aurelienribon.tweenengine.TweenManager;
import aurelienribon.tweenengine.equations.Cubic;
import aurelienribon.tweenengine.equations.Quad;
import aurelienribon.tweenengine.primitives.MutableFloat;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;

/**
 * @author Aurelien Ribon | http://www.aurelienribon.com/
 */

/**
 * Tile:卡片
 * 表示Launcher界面下,一个一个的小方块
 */
public class Tile {
	private final float x, y;				// 卡片的位置
	private final Test test;				// 卡片对应的test
	private final Sprite sprite;			// 背景(一个纯蓝色的方块)
	private final Sprite interactiveIcon;	// 卡片左上角的手指
	private final Sprite veil;				// 过场动画所需的veil(面纱)
	private final OrthographicCamera camera;// 正交摄像机
	private final BitmapFont font;			// 写卡片上的文字
	private final TweenManager tweenManager;// tile所需的tweenManager
	private final MutableFloat textOpacity = new MutableFloat(1);	// 字体透明度

	public Tile(float x, float y, float w, float h, Test test, TextureAtlas atlas, OrthographicCamera camera, BitmapFont font, TweenManager tweenManager) {
		this.x = x;
		this.y = y;
		this.test = test;
		this.camera = camera;
		this.font = font;
		this.tweenManager = tweenManager;
//		System.out.println("x = " + x + " y = " + y);

		this.sprite = test.getImageName() != null ? atlas.createSprite(test.getImageName()) : atlas.createSprite("tile");
		this.interactiveIcon = atlas.createSprite("interactive");
		this.veil = atlas.createSprite("white");

		sprite.setSize(w, h);
		sprite.setOrigin(w/2, h/2);
		sprite.setPosition(x + camera.viewportWidth, y);	// sprite的x值加camera.viewportWidth,是为了将其位置右移到屏幕外面,方便做动画使用
//		System.out.println("sprite : x = " + sprite.getX() + " y = " + sprite.getY());

		interactiveIcon.setSize(w/10, w/10 * interactiveIcon.getHeight() / interactiveIcon.getWidth());
		interactiveIcon.setPosition(x+w - interactiveIcon.getWidth() - w/50, y+h - interactiveIcon.getHeight() - w/50);	// 手指的位置都放在了各自应该在的位置,并没有放在屏幕的右边
																														// 其color的a设置为了0.初始状态看不到
		interactiveIcon.setColor(1, 1, 1, 0);	// 设置opacity:a = 0. 初始状态是看不到的

		veil.setSize(w, h);
		veil.setOrigin(w/2, h/2);
		veil.setPosition(x, y);		// veil也在自己应该在的位置上,并没有放在屏幕的右边
		veil.setColor(1, 1, 1, 0);	// 设置opacity:a = 0. 初始状态是看不到的
//		System.out.println("veil : x = " + veil.getX() + " y = " + veil.getY());
	}

	public void draw(SpriteBatch batch) {
		sprite.draw(batch);
		if (test.getInput() != null) interactiveIcon.draw(batch);	// 若test界面里没有Input事件,则手指图标不绘制; 若test界面里有Input事件,则手指图标绘制.

		// 计算出 (sprite.getWidth() - sprite.getWidth()/10) 对应的实际屏幕宽度
		float wrapW = (sprite.getWidth() - sprite.getWidth()/10) * Gdx.graphics.getWidth() / camera.viewportWidth;

		font.setColor(1, 1, 1, textOpacity.floatValue());
		font.drawWrapped(batch, test.getTitle(),
			sprite.getX() + sprite.getWidth()/20,
			sprite.getY() + sprite.getHeight()*19/20,
			wrapW);

		if (veil.getColor().a > 0.1f) veil.draw(batch);
	}

	/**
	 * Launcher刚刚起来时,会调到这个方法.
	 * 动画:蓝色卡片会左移,手指图标慢慢显现出来
	 * @param delay 延迟的时间
     */
	public void enter(float delay) {
		Timeline.createSequence()
			.push(Tween.to(sprite, SpriteAccessor.POS_XY, 0.7f).target(x, y).ease(Cubic.INOUT))
			.pushPause(0.1f)
			.push(Tween.to(interactiveIcon, SpriteAccessor.OPACITY, 0.4f).target(1))
			.delay(delay)	// 延迟多长时间再做动画
			.start(tweenManager);
	}

	/**
	 * 点击卡片时,会调到这个方法.
	 * @param callback
     */
	public void maximize(TweenCallback callback) {
		tweenManager.killTarget(interactiveIcon);
		tweenManager.killTarget(textOpacity);
		tweenManager.killTarget(sprite);

		// tx,ty 表示的位置是sprite/veil的中心点在整个屏幕的中心位置上.
		float tx = camera.position.x - sprite.getWidth()/2;
		float ty = camera.position.y - sprite.getHeight()/2;
		// sx,sy 表示的就是sprite/veil放大到整个屏幕上了
		float sx = camera.viewportWidth / sprite.getWidth();
		float sy = camera.viewportHeight / sprite.getHeight();

		Timeline.createSequence()
			.push(Tween.set(veil, SpriteAccessor.POS_XY).target(tx, ty))	// veil的中心点设置在屏幕的中心
			.push(Tween.set(veil, SpriteAccessor.SCALE_XY).target(sx, sy))	// veil放大至整个屏幕
			.beginParallel()
				// 文字和手指图标渐渐消失
				.push(Tween.to(textOpacity, 0, 0.2f).target(0))
				.push(Tween.to(interactiveIcon, SpriteAccessor.OPACITY, 0.2f).target(0))
			.end()
			.push(Tween.to(sprite, SpriteAccessor.SCALE_XY, 0.3f).target(0.9f, 0.9f).ease(Quad.OUT))	// sprite's scale x,y经过0.3s从原来的1.0,1.0变为0.9,0.9
			.beginParallel()
				.push(Tween.to(sprite, SpriteAccessor.SCALE_XY, 0.5f).target(sx, sy).ease(Cubic.IN))	// sprite's scale x,y经过0.5s从原来的0.9,0.9变为sx,sy
				.push(Tween.to(sprite, SpriteAccessor.POS_XY, 0.5f).target(tx, ty).ease(Quad.IN))		// sprite's position x,y经过0.5s变为tx,ty
			.end()
			.pushPause(-0.3f)
			.push(Tween.to(veil, SpriteAccessor.OPACITY, 0.7f).target(1))	// veil渐渐显现
			.setUserData(this)
			.setCallback(callback)
			.start(tweenManager);
	}

	/**
	 * 从test界面返回到launcher界面时,会调到这个方法.
	 * @param callback
     */
	public void minimize(TweenCallback callback) {
		tweenManager.killTarget(sprite);
		tweenManager.killTarget(textOpacity);

		Timeline.createSequence()
			.push(Tween.set(veil, SpriteAccessor.OPACITY).target(0))	// veil隐藏
			.beginParallel()
				.push(Tween.to(sprite, SpriteAccessor.SCALE_XY, 0.3f).target(1, 1).ease(Quad.OUT))	// sprite's scale慢慢变为1,1
				.push(Tween.to(sprite, SpriteAccessor.POS_XY, 0.5f).target(x, y).ease(Quad.OUT))	// sprite's pos慢慢变为原来的位置
			.end()
			.beginParallel()
				// 文字和手指慢慢显现
				.push(Tween.to(textOpacity, 0, 0.3f).target(1))
				.push(Tween.to(interactiveIcon, SpriteAccessor.OPACITY, 0.3f).target(1))
			.end()
			.setUserData(this)
			.setCallback(callback)
			.start(tweenManager);
	}

	/**
	 * 判断触控点是否在卡片内
	 * @param x
	 * @param y
     * @return
     */
	public boolean isOver(float x, float y) {
		return sprite.getX() <= x && x <= sprite.getX() + sprite.getWidth()
			&& sprite.getY() <= y && y <= sprite.getY() + sprite.getHeight();
	}

	public Test getTest() {
		return test;
	}
}
