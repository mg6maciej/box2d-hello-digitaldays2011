package pl.mg6.digitaldays2011.box2d.hello;

import java.util.ArrayList;
import java.util.List;

import org.jbox2d.callbacks.QueryCallback;
import org.jbox2d.collision.AABB;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.MathUtils;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.World;
import org.jbox2d.dynamics.joints.MouseJoint;
import org.jbox2d.dynamics.joints.MouseJointDef;

import android.util.Log;

public class HelloBox2DModel {
	
	private static final String TAG = HelloBox2DModel.class.getSimpleName();

	private World world;
	private Body worldEdge;
	private Body rightBall;
	
	private long timeAccumulator;
	
	private static final long stepInMillis = 20;
	private static final float stepInSeconds = stepInMillis / 1000.0f;
	private static final int velocityIterations = 10;
	private static final int positionIterations = 5;
	
	private List<MouseJoint> userActions = new ArrayList<MouseJoint>();
	
	public HelloBox2DModel() {
		init();
	}
	
	private void init() {
		Vec2 gravity = new Vec2(0.0f, 0.0f);
		boolean doSleep = true;
		world = new World(gravity, doSleep);
		
		BodyDef groundBodyDef = new BodyDef();
		worldEdge = world.createBody(groundBodyDef);
		PolygonShape edgeShape = new PolygonShape();
		edgeShape.setAsEdge(new Vec2(-8.0f, -4.0f), new Vec2(8.0f, -4.0f));
		worldEdge.createFixture(edgeShape, 1.0f);
		edgeShape.setAsEdge(new Vec2(-8.0f, -4.0f), new Vec2(-8.0f, 4.0f));
		worldEdge.createFixture(edgeShape, 1.0f);
		edgeShape.setAsEdge(new Vec2(8.0f, -4.0f), new Vec2(8.0f, 4.0f));
		worldEdge.createFixture(edgeShape, 1.0f);
		edgeShape.setAsEdge(new Vec2(-8.0f, 4.0f), new Vec2(8.0f, 4.0f));
		worldEdge.createFixture(edgeShape, 1.0f);
		
		float radius = 0.5f;
		rightBall = createCircleBody(4.0f, 0.1f, radius, "s");
		
		float x = -1.0f;
		float y = 0.0f;
		float subX = MathUtils.sqrt(3 * radius * radius);
		createCircleBody(x, y, radius, "y");
		createCircleBody(x - subX, y - radius, radius, "d");
		createCircleBody(x - subX, y + radius, radius, "a");
		createCircleBody(x - 2 * subX, y - 2 * radius, radius, "t");
		createCircleBody(x - 2 * subX, y, radius, "a");
		createCircleBody(x - 2 * subX, y + 2 * radius, radius, "l");
		createCircleBody(x - 3 * subX, y - 3 * radius, radius, "d");
		createCircleBody(x - 3 * subX, y - radius, radius, "i");
		createCircleBody(x - 3 * subX, y + radius, radius, "g");
		createCircleBody(x - 3 * subX, y + 3 * radius, radius, "i");
	}
	
	private Body createCircleBody(float x, float y, float radius, String tag) {
		BodyDef def = new BodyDef();
		def.type = BodyType.DYNAMIC;
		def.position.set(x, y);
		def.angularDamping = 0.05f;
		def.linearDamping = 0.3f;
		def.userData = tag;
		Body body = world.createBody(def);
		CircleShape shape = new CircleShape();
		shape.m_radius = radius;
		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.shape = shape;
		fixtureDef.density = 1.0f;
		fixtureDef.restitution = 0.95f;
		body.createFixture(fixtureDef);
		return body;
	}
		
	public void update(long dt) {
		timeAccumulator += dt;
		//int stepsDuringUpdate = 0;
		while (timeAccumulator >= stepInMillis) {
			world.step(stepInSeconds, velocityIterations, positionIterations);
			//Log.i(TAG, "right ball: " + rightBall.getPosition());
			timeAccumulator -= stepInMillis;
			//stepsDuringUpdate++;
		}
		//Log.i(TAG, "steps during update: " + stepsDuringUpdate);
	}

	public Body getBodyList() {
		return world.getBodyList();
	}

	public void userActionStart(int pointerId, final float x, final float y) {
		final List<Fixture> fixtures = new ArrayList<Fixture>();
		final Vec2 vec = new Vec2(x, y);
		world.queryAABB(new QueryCallback() {
			public boolean reportFixture(Fixture fixture) {
				Log.i(TAG, "reportFixture: " + fixture);
				//if (fixture.testPoint(vec)) {
					fixtures.add(fixture);
				//}
				return true;
			}
		}, new AABB(vec, vec));
		if (fixtures.size() > 0) {
			Fixture fixture = fixtures.get(0);
			Log.i(TAG, "creating mouse joint: " + fixture);
			Body body = fixture.getBody();
			
			MouseJointDef def = new MouseJointDef();
			def.bodyA = body;
			def.bodyB = body;
			def.maxForce = 1000.0f * body.getMass();
			def.target.set(x, y);
			
			MouseJoint joint = (MouseJoint) world.createJoint(def);
			joint.m_userData = pointerId;
			userActions.add(joint);
		} else {
			rightBall.applyForce(new Vec2(-1000.0f * rightBall.getMass(), 0.0f), rightBall.getPosition());
		}
	}

	public void userActionUpdate(int pointerId, float x, float y) {
		for (MouseJoint joint : userActions) {
			if (pointerId == (Integer) joint.m_userData) {
				joint.setTarget(new Vec2(x, y));
				break;
			}
		}
	}

	public void userActionEnd(int pointerId, float x, float y) {
		for (MouseJoint joint : userActions) {
			if (pointerId == (Integer) joint.m_userData) {
				world.destroyJoint(joint);
				userActions.remove(joint);
				break;
			}
		}
	}
}
