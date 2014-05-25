package Object;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import sponza.OpenGL.Userdata;
import android.opengl.GLES20;

//Y축 라인,Object의 함수,변수를 이용하므로 상속받는다.
public class AxisY extends Object {

	// Y축 초기화
	public void initShapes() {

		// (0,0,0)에서 (0,10,0)까지 뻗어나가는 선을 그릴려고 한다.
		float triangleCoords[] = {
				// X, Y, Z
				0.0f, 0.0f, 0.0f, 0.0f, 10.0f, 0.0f, };

		// initialize vertex Buffer
		ByteBuffer vbb = ByteBuffer.allocateDirect(
		// (# of coordinate values * 4 bytes per float)
				triangleCoords.length * 4);
		vbb.order(ByteOrder.nativeOrder());// use the device hardware's native
											// byte order
		mVerticesBuffer = vbb.asFloatBuffer(); // create a floating point buffer
												// from
												// the ByteBuffer
		mVerticesBuffer.put(triangleCoords); // add the coordinates to the
		// FloatBuffer
		mVerticesBuffer.position(0); // set the buffer to read the first
										// coordinate
	}

	// 화면에 선을 글려주는 함수
	// Userdata data : 어떤 쉐이더,어떤 포인터를 이용하여 그림을 그릴지 알아야하므로 넘겨받는다.
	public void draw(Userdata data) {

		// Add program to OpenGL environment
		GLES20.glUseProgram(data.Program);

		// Prepare the axis data
		GLES20.glVertexAttribPointer(data.VertexLoc, 3, GLES20.GL_FLOAT,
				false, 12, mVerticesBuffer);
		GLES20.glEnableVertexAttribArray(data.VertexLoc);

		// Draw the Y축
		GLES20.glDrawArrays(GLES20.GL_LINES, 0, 2);

	}
}
