package Object;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import android.opengl.GLES20;

import sponza.OpenGL.Userdata;

//������ƮŬ����
public class Object {

	protected short[] Indices;// ���ý��� �ε����� �����ص� �迭
	public float[] VerticesBuffer;// ���ý��� ������ �迭

	protected int Indiceslength;// Indices�迭�� ���̸� ������ ����
	protected int VerticesBufferlength;// VerticesBuffer�迭�� ���̸� ������ ����
	
	protected int floatLength = 0;// Float�迭�� ���̸� ������ ����

	protected ShortBuffer mIndicesBuffer;// Indices�迭�� ������ short�� ����
	protected FloatBuffer mVerticesBuffer;// VerticesBuffer�迭�� ������ float�� ����

	public String map_Ka = "";// �ؽ��� �̿�� ��̹����� ����ϴ��� ������ ����
	public int mode;

	public int[] textureIds = new int[6];// glGenTextures,glBindTexture�� ��� �ؽ���
											// ���̵�
	public int vertexCount = 0;

	// mVerticesBuffer �������� �Լ�
	public FloatBuffer getVertexBuffer() {
		return mVerticesBuffer;
	}

	// mIndicesBuffer�������� �Լ�
	public ShortBuffer getIndexBuffer() {
		return mIndicesBuffer;
	}

	// ȭ�鿡 ���� �۷��ִ� �Լ�
	// Userdata data : � ���̴�,� �����͸� �̿��Ͽ� �׸��� �׸��� �˾ƾ��ϹǷ� �Ѱܹ޴´�.
	public void draw(Userdata data) {
		// Add program to OpenGL environment
		GLES20.glUseProgram(data.Program);

		// if ���������� �ؽ��ĸ� �ҷ��� �ؽ��� ���̵� �����ִٸ�, �ؽ��ĸ� ���ε��Ѵ�.
		// else �ؽ��ĸ� �̿����� �ʵ��� ��� ������� ���´�.
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
