package Object;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import sponza.OpenGL.Userdata;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;

//Object Wall.
public class Wall extends Object {

	// Initialize the wall.
	
	public int texId = 0;
	
	public void initShapes(Context context, int texture) throws IOException {

		// Coordinate of the wall.
		float triangleCoords[] = {
				// X, Y, Z, nx, ny, nz, s, t
				-10.0f, -10.0f, -10.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f,
				10.0f, -10.0f, -10.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f,
				-10.0f,  10.0f, -10.0f, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f,
				-10.0f,  10.0f, -10.0f, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f,
				10.0f, -10.0f, -10.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f,
				10.0f,  10.0f, -10.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f,

		};

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
		
		AssetManager as = context.getAssets();
		InputStream id = as.open("WallTex.jpg");
		loadTexture(id, 0);
		id = as.open("WallMap.jpg");
		loadTexture(id, 1);
		texId = texture;
	}

	// 화면에 선을 글려주는 함수
	// Userdata data : 어떤 쉐이더,어떤 포인터를 이용하여 그림을 그릴지 알아야하므로 넘겨받는다.
	public void draw(Userdata data) {

		// Add program to OpenGL environment
		GLES20.glUseProgram(data.Program);

		// Prepare the triangle data
		mVerticesBuffer.position(0);
		GLES20.glVertexAttribPointer(data.VertexLoc, 3, GLES20.GL_FLOAT,
				false, 32, mVerticesBuffer);
		GLES20.glEnableVertexAttribArray(data.VertexLoc);

		mVerticesBuffer.position(3);
		GLES20.glVertexAttribPointer(data.NormalLoc, 3, GLES20.GL_FLOAT,
				false, 32, mVerticesBuffer);
		GLES20.glEnableVertexAttribArray(data.NormalLoc);

		mVerticesBuffer.position(6);
		GLES20.glVertexAttribPointer(data.TextureLoc, 2, GLES20.GL_FLOAT,
				false, 32, mVerticesBuffer);
		GLES20.glEnableVertexAttribArray(data.TextureLoc);

		//3 = 1 + 2(Texture + Normal Map for bump mapping)
		GLES20.glUniform1f(data.IsTexturedLoc, 3.0f);

		// Wall Texture image.
		GLES20.glUniform1i(data.TexSamplerLoc, 2);

		// Normal map texture.
		GLES20.glUniform1i(data.TexNormalLoc, 4);
		
		GLES20.glUniform1f(data.IsBumpedLoc, 0.0f);
		GLES20.glUniform1f(data.IsHoledLoc, 0.0f);

		// Draw the wall.
		GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6);

	}
	
	private int[] textureIds = new int[16]; 
	
	public void loadTexture(InputStream is, int Offset) {

		GLES20.glPixelStorei(GLES20.GL_UNPACK_ALIGNMENT, 1);
		GLES20.glGenTextures(1, textureIds, Offset);
		GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + 2 + 2 * Offset);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureIds[Offset]);

		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
				GLES20.GL_REPEAT);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
				GLES20.GL_REPEAT);

		GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
				GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
		GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
				GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

		Bitmap bitmap = BitmapFactory.decodeStream(is);
		GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
		bitmap.recycle();

	}
}
