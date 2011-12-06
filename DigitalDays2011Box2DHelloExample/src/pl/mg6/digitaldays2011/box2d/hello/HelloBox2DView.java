package pl.mg6.digitaldays2011.box2d.hello;

import java.util.HashMap;
import java.util.Map;

import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.collision.shapes.ShapeType;
import org.jbox2d.common.MathUtils;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.Fixture;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.View;

public class HelloBox2DView extends View {
	
	private static final String TAG = HelloBox2DView.class.getSimpleName();
	
	private HelloBox2DModel model;
	
	private final Paint paint;
	
	private static final int VIEW_SIZE_FOR_IMAGES = 1280;
	public static final float VIEWPORT_SIZE = 16.0f; // meters
	
	private Map<String, Bitmap> images;
	
	public HelloBox2DView(Context context) {
		super(context);
		paint = new Paint(Paint.ANTI_ALIAS_FLAG);
	}

	public void setModel(HelloBox2DModel model) {
		this.model = model;
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		initImages();
		canvas.translate(getWidth() / 2, getHeight() / 2);
		drawBodies(canvas);
	}
	
	private void initImages() {
		if (images == null) {
			images = new HashMap<String, Bitmap>();
			images.put("a", bitmapFromResourceId(R.drawable.ball_a));
			images.put("d", bitmapFromResourceId(R.drawable.ball_d));
			images.put("g", bitmapFromResourceId(R.drawable.ball_g));
			images.put("i", bitmapFromResourceId(R.drawable.ball_i));
			images.put("l", bitmapFromResourceId(R.drawable.ball_l));
			images.put("s", bitmapFromResourceId(R.drawable.ball_s));
			images.put("t", bitmapFromResourceId(R.drawable.ball_t));
			images.put("y", bitmapFromResourceId(R.drawable.ball_y));
		}
	}
	
	private Bitmap bitmapFromResourceId(int resId) {
		Bitmap bmp = BitmapFactory.decodeResource(getResources(), resId);
		if (getWidth() != VIEW_SIZE_FOR_IMAGES) {
			Matrix matrix = new Matrix();
			float scale = ((float) getWidth()) / VIEW_SIZE_FOR_IMAGES;
			matrix.postScale(scale, scale);
			bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
		}
		return bmp;
	}
	
	private void drawBodies(Canvas canvas) {
		Body body = model.getBodyList();
		while (body != null) {
			Fixture fixture = body.getFixtureList();
			while (fixture != null) {
				Shape shape = fixture.getShape();
				drawShape(canvas, body.getPosition(), body.getAngle(), shape, (String) body.m_userData);
				fixture = fixture.getNext();
			}
			body = body.getNext();
		}
	}
	
	private void drawShape(Canvas canvas, Vec2 pos, float angle, Shape shape, String imageId) {
		float scale = getWidth() / VIEWPORT_SIZE;
		pos = new Vec2(pos).mulLocal(scale);
		canvas.save();
		canvas.rotate(180.0f * angle / MathUtils.PI, pos.x, pos.y);
		if (shape.m_type == ShapeType.CIRCLE) {
			CircleShape circle = (CircleShape) shape;
			pos.addLocal(circle.m_p.mul(scale));
			float radius = circle.m_radius * scale;
//			paint.setColor(0xFFFF6666);
//			canvas.drawCircle(pos.x, pos.y, radius, paint);
//			paint.setColor(0xFFFFFFFF);
//			canvas.drawLine(pos.x, pos.y, pos.x + radius, pos.y, paint);
			Bitmap bmp = images.get(imageId);
			canvas.drawBitmap(bmp, pos.x - radius, pos.y - radius, paint);
		}
		canvas.restore();
	}
}
