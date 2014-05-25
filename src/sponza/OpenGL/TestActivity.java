package sponza.OpenGL;

import Test.OpenGL.R;
import android.app.Activity;
import android.content.Context;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RadioButton;
import android.widget.Button;
import android.widget.Toast;

public class TestActivity extends Activity {

	public static int renderMode = 0;// 0 퐁 1고라드

	private TestSurfaceView mGLSurfaceView = null;
	private final static int programCount = 5;
	public static RadioButton leftButton;
	public static RadioButton middleButton1;
	public static RadioButton rightButton;
	public Button Button;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		final FrameLayout frameLayout = (FrameLayout) findViewById(R.id.frameLayout1);
		if (mGLSurfaceView == null) {
			mGLSurfaceView = new TestSurfaceView(this);
			frameLayout.addView(mGLSurfaceView);
		}

		leftButton = (RadioButton) findViewById(R.id.radioButton1);
		middleButton1 = (RadioButton) findViewById(R.id.radioButton2);
		rightButton = (RadioButton) findViewById(R.id.radioButton3);
		Button = (Button) findViewById(R.id.button1);
		Button.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {

				renderMode++;

				if (renderMode == programCount)
					renderMode = 0;

				if (renderMode == 0) {
					Toast.makeText(TestActivity.this, "Phong",
							Toast.LENGTH_SHORT).show();
					mGLSurfaceView.rendering(renderMode);
				} else if (renderMode == 1) {
					Toast.makeText(TestActivity.this, "Gouraud",
							Toast.LENGTH_SHORT).show();
					mGLSurfaceView.rendering(renderMode);
				} else if (renderMode == 2){
					Toast.makeText(TestActivity.this, "Net",
							Toast.LENGTH_SHORT).show();
					mGLSurfaceView.rendering(renderMode);
				} else if (renderMode == 3) {
					Toast.makeText(TestActivity.this, "Noise",
							Toast.LENGTH_SHORT).show();
					mGLSurfaceView.rendering(renderMode);
				} else if (renderMode == 4) {
					Toast.makeText(TestActivity.this, "Bumpy",
							Toast.LENGTH_SHORT).show();
					mGLSurfaceView.rendering(renderMode);
				}

			}
		});

	}

	@Override
	protected void onResume() {
		super.onResume();
		mGLSurfaceView.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		mGLSurfaceView.onPause();
	}

}

class TestSurfaceView extends GLSurfaceView {

	// member variables
	private TestRender mRenderer = null;

	// 포인트점 두개 이용 : 포인트점0,포인트점1
	// 포인트0,1 각각의 이전포인트x,y 현재 포인트 x,y
	// delta : 이전포인트와 현재 포인트간의 차이
	public float previousX[] = new float[2], currentX[] = new float[2], deltaX;
	public float previousY[] = new float[2], currentY[] = new float[2], deltaY;

	public TestSurfaceView(Context context) {
		super(context);
		// Create an OpenGL ES 2.0 context.
		setEGLContextClientVersion(2);

		// Set the Renderer for drawing on the GLSurfaceView
		mRenderer = new TestRender(context);
		setRenderer(mRenderer);
	}

	public void rendering(int mode) {
		mRenderer.renderMode = mode;
		requestRender();
	}

	@Override
	public boolean onTouchEvent(MotionEvent e) {

		final int SENSITIVITY = 5;// 이벤트를 발생시킬지 기준이 되는 값
		final float LOOSENER = 1f;

		switch (e.getAction() & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN:
			for (int i = 0; i < e.getPointerCount(); i++) {
				currentX[i] = e.getX(i);
				currentY[i] = e.getY(i);
				previousX[i] = currentX[i];
				previousY[i] = currentY[i];
			}
			break;
		case MotionEvent.ACTION_MOVE: // touch move
			for (int i = 0; i < e.getPointerCount(); i++) {
				previousX[i] = currentX[i];
				previousY[i] = currentY[i];

				currentX[i] = e.getX(i);
				currentY[i] = e.getY(i);
			}

			// delta calculation of each coord
			deltaX = currentX[0] - previousX[0];
			deltaY = currentY[0] - previousY[0];

			if (e.getPointerCount() == 1) {
				if (TestActivity.leftButton.isChecked() == true) {
					if (Math.abs(deltaY) > SENSITIVITY)
						mRenderer.mCamera.MoveUpward(deltaY);
					if (Math.abs(deltaX) > SENSITIVITY)
						mRenderer.mCamera.MoveSideward(-deltaX);
				}

				else if (TestActivity.middleButton1.isChecked() == true) {
					if (Math.abs(deltaY) > SENSITIVITY)
						mRenderer.mCamera.MoveForward(deltaY);

				} else if (TestActivity.rightButton.isChecked() == true) {
					if (Math.abs(deltaX) > SENSITIVITY) {
						mRenderer.ObjectRotateZ += deltaX / LOOSENER;
					}
					/*if (Math.abs(deltaX) > SENSITIVITY) {
						mRenderer.ObjectRotateX += deltaX;
					}*/
				}
			} else if (e.getPointerCount() == 2) {
				float pre = Math.abs(previousX[0] - previousX[1]);
				float cur = Math.abs(currentX[0] - currentX[1]);

				if (pre - cur > 0)
					mRenderer.mCamera.Zoom(30);
				else
					mRenderer.mCamera.Zoom(-30);
			}

			requestRender();
			break;
		}

		return true;
	}

}
