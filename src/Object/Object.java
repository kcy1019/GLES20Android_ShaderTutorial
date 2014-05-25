package Object;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import android.opengl.GLES20;

import sponza.OpenGL.Userdata;

//오브젝트클래스
public class Object {

	protected short[] Indices;// 버택스의 인덱스를 저장해둔 배열
	public float[] VerticesBuffer;// 버택스를 저장한 배열

	protected int Indiceslength;// Indices배열의 길이를 저장한 변수
	protected int VerticesBufferlength;// VerticesBuffer배열의 길이를 저장한 변수
	
	protected int floatLength = 0;// Float배열의 길이를 저장한 변수

	protected ShortBuffer mIndicesBuffer;// Indices배열을 저장할 short형 버퍼
	protected FloatBuffer mVerticesBuffer;// VerticesBuffer배열을 저장한 float형 버퍼

	public String map_Ka = "";// 텍스쳐 이용시 어떤이미지를 사용하는지 저장한 변수
	public int mode;

	public int[] textureIds = new int[6];// glGenTextures,glBindTexture시 사용 텍스쳐
											// 아이디
	public int vertexCount = 0;

	// mVerticesBuffer 가져오는 함수
	public FloatBuffer getVertexBuffer() {
		return mVerticesBuffer;
	}

	// mIndicesBuffer가져오는 함수
	public ShortBuffer getIndexBuffer() {
		return mIndicesBuffer;
	}

	// 화면에 선을 글려주는 함수
	// Userdata data : 어떤 쉐이더,어떤 포인터를 이용하여 그림을 그릴지 알아야하므로 넘겨받는다.
	public void draw(Userdata data) {
		// Add program to OpenGL environment
		GLES20.glUseProgram(data.Program);

		// if 정상적으로 텍스쳐를 불러와 텍스쳐 아이디를 갖고있다면, 텍스쳐를 바인딩한다.
		// else 텍스쳐를 이용하지 않도록 모든 연결고리를 끊는다.
		if (mode == 2) {// COMPLEX OBJECT.
			getVertexBuffer().position(0);
			GLES20.glVertexAttribPointer(data.VertexLoc, 3, GLES20.GL_FLOAT,
					false, 32, getVertexBuffer());
			GLES20.glEnableVertexAttribArray(data.VertexLoc);

			getVertexBuffer().position(3);
			GLES20.glVertexAttribPointer(data.NormalLoc, 3, GLES20.GL_FLOAT, false,
					32, getVertexBuffer());
			GLES20.glEnableVertexAttribArray(data.NormalLoc);

			getIndexBuffer().position(0);
			
			
			GLES20.glUniform1f(data.IsTexturedLoc, 0.0f);
			GLES20.glUniform1f(data.IsBumpedLoc, 1.0f);
			GLES20.glUniform1f(data.IsHoledLoc, 0.0f);
			
			GLES20.glDrawElements(GLES20.GL_TRIANGLES, Indiceslength,
					GLES20.GL_UNSIGNED_SHORT, getIndexBuffer());
		} else if (mode == 1) {// SIMPLE, NON-TEXTURED OBJECT.
			getVertexBuffer().position(0);
			GLES20.glVertexAttribPointer(data.VertexLoc, 3, GLES20.GL_FLOAT,
					false, 32, getVertexBuffer());
			GLES20.glEnableVertexAttribArray(data.VertexLoc);

			getVertexBuffer().position(3);
			GLES20.glVertexAttribPointer(data.NormalLoc, 3, GLES20.GL_FLOAT,
					false, 32, getVertexBuffer());
			GLES20.glEnableVertexAttribArray(data.NormalLoc);
			
			GLES20.glUniform1f(data.IsTexturedLoc, 0.0f);
			GLES20.glUniform1f(data.IsBumpedLoc, 0.0f);
			GLES20.glUniform1f(data.IsHoledLoc, 0.0f);
			
			GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount);
		} else if (mode == 3) {// SIMPLE, TEXTURED OBJECT.
			getVertexBuffer().position(0);
			GLES20.glVertexAttribPointer(data.VertexLoc, 3, GLES20.GL_FLOAT,
					false, 32, getVertexBuffer());
			GLES20.glEnableVertexAttribArray(data.VertexLoc);

			getVertexBuffer().position(3);
			GLES20.glVertexAttribPointer(data.NormalLoc, 3, GLES20.GL_FLOAT,
					false, 32, getVertexBuffer());
			
			getVertexBuffer().position(6);
			GLES20.glVertexAttribPointer(data.TextureLoc, 2, GLES20.GL_FLOAT,
					false, 32, getVertexBuffer());
			GLES20.glEnableVertexAttribArray(data.TextureLoc);
			
			GLES20.glUniform1f(data.IsTexturedLoc, 1.0f);
			GLES20.glUniform1f(data.IsBumpedLoc, 0.0f);
			GLES20.glUniform1f(data.IsHoledLoc, 0.0f);
			
			GLES20.glUniform1i(data.TexSamplerLoc, 0);
			
			GLES20.glDisable(GLES20.GL_CULL_FACE); 
			GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount);

		} else {// SIMPLE, TEXTURED, and BUMP MAPPED OBJECT.
			getVertexBuffer().position(0);
			GLES20.glVertexAttribPointer(data.VertexLoc, 3, GLES20.GL_FLOAT,
					false, 32, getVertexBuffer());
			GLES20.glEnableVertexAttribArray(data.VertexLoc);

			getVertexBuffer().position(3);
			GLES20.glVertexAttribPointer(data.NormalLoc, 3, GLES20.GL_FLOAT,
					false, 32, getVertexBuffer());
			
			getVertexBuffer().position(6);
			GLES20.glVertexAttribPointer(data.TextureLoc, 2, GLES20.GL_FLOAT,
					false, 32, getVertexBuffer());
			GLES20.glEnableVertexAttribArray(data.TextureLoc);
			
			GLES20.glUniform1f(data.IsTexturedLoc, 1.0f);
			GLES20.glUniform1f(data.IsBumpedLoc, 0.0f);
			GLES20.glUniform1f(data.IsHoledLoc, 1.0f);
			
			GLES20.glUniform1i(data.TexSamplerLoc, 5);
			GLES20.glUniform1i(data.TexNormalLoc, 3);		
			
			GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount);
		}
	}

}
