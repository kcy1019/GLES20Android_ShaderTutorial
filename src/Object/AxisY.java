package Object;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import sponza.OpenGL.Userdata;
import android.opengl.GLES20;

//Y�� ����,Object�� �Լ�,������ �̿��ϹǷ� ��ӹ޴´�.
public class AxisY extends Object {

	// Y�� �ʱ�ȭ
	public void initShapes() {

		// (0,0,0)���� (0,10,0)���� ������� ���� �׸����� �Ѵ�.
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

	// ȭ�鿡 ���� �۷��ִ� �Լ�
	// Userdata data : � ���̴�,� �����͸� �̿��Ͽ� �׸��� �׸��� �˾ƾ��ϹǷ� �Ѱܹ޴´�.
	public void draw(Userdata data) {

		// Add program to OpenGL environment
		GLES20.glUseProgram(data.Program);

		// Prepare the axis data
		GLES20.glVertexAttribPointer(data.VertexLoc, 3, GLES20.GL_FLOAT,
				false, 12, mVerticesBuffer);
		GLES20.glEnableVertexAttribArray(data.VertexLoc);

		// Draw the Y��
		GLES20.glDrawArrays(GLES20.GL_LINES, 0, 2);

	}
}
