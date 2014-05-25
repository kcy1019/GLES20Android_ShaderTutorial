package Object;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;


//bin������ �̿��Ͽ� ������Ʈ�� �׸��� ����ϴ� Ŭ����
//bin������ �������� ������Ʈ��� �����Ǿ��ִ�. 
//�׸��� �׸��� ������Ʈ ����ŭ �ݺ��Ϸ� �׸��� �׷����Ѵ�.
public class ObjectLoader {

	int ObjectStructListSize = 0;// bin������ �����ϴ� ������Ʈ�� ��ü ���� ������ ����
	public ArrayList<Object> ObjectStructList = new ArrayList<Object>();// bin������
																		// �о��
																		// �˾Ƴ�
																		// ������Ʈ����
																		// ���ʷ�
																		// ������
																		// list
	public Object tempObject = null;// �Ѱ��� ������Ʈ�� �����ϱ����� �ӽ� ����
	public int mode = 1;

	// bin������ �̿� Object�� �а�, �о���� Object ������� ���ʷ� ObjectStructList�� ����
	// Context context : � �ȵ���̵����α׷��� �̿�ǰ� �ִ��� �����ϴ� ����
	// int objectId : ��� bin������ �̿����� �Ѱ��� ��
	/*
	 * bin���� ���� :ObjectStructListSize : bin������ �����ϴ� ��ü ������Ʈ ����{ Indiceslength;
	 * ������Ʈ �Ѱ��� �ε����迭�� ���� VerticesBufferlength; ������Ʈ �Ѱ��� ���ý� �迭�� ���� protected
	 * short[] Indices; ������Ʈ �Ѱ��� �ε��� �迭 protected float[] VerticesBuffer; : ������Ʈ
	 * �Ѱ��� ���ؽ� �迭 public String map_Ka = "";// �ؽ��� �̿�� ��̹����� ����ϴ��� ������ ���� }<-
	 * ���������� ObjectStructListSize��ŭ �ݺ��ȴ�.
	 */
	public void LoadSimple(Context context, int objectId, String texFile) throws IOException {
		tempObject = new Object();
		tempObject.mode = mode;
		InputStream inputStream = context.getResources().openRawResource(objectId);// ���ҽ� ������ �о�´�.
		DataInputStream reader = new DataInputStream(inputStream);
		byte[] temp = new byte[4];// ����Ʈ�迭�� float�� ��ȯ�Ҷ� ���Ǵ� �ӽ� ����Ʈ �迭
		reader.read(temp);// FloatLength�д´�.
		tempObject.floatLength |= ((temp[0]) << 24) & 0xFF000000;
		tempObject.floatLength |= ((temp[1]) << 16) & 0xFF0000;
		tempObject.floatLength |= ((temp[2]) << 8) & 0xFF00;
		tempObject.floatLength |= (temp[3]) & 0xFF;
		tempObject.vertexCount = (int)( tempObject.floatLength / 8 );
		tempObject.VerticesBufferlength = tempObject.floatLength;
		tempObject.VerticesBuffer = new float[tempObject.floatLength];
		//reader.read(object.vnt);

		int result;
		for (int j = 0; j < tempObject.floatLength; j++) {
			reader.read(temp);
			result = 0;
			result |= ((temp[0]) << 24) & 0xFF000000;
			result |= ((temp[1]) << 16) & 0xFF0000;
			result |= ((temp[2]) << 8) & 0xFF00;
			result |= (temp[3]) & 0xFF;
			tempObject.VerticesBuffer[j] = Float.intBitsToFloat(result);
		}

		// ���ؽ� ���۸� �����.
		tempObject.mVerticesBuffer = ByteBuffer.allocateDirect(tempObject.floatLength*4).order(ByteOrder.nativeOrder()).asFloatBuffer();
		// ���ؽ����ۿ� ���ؽ� �迭�� �������ش�.
		tempObject.mVerticesBuffer.clear();
		tempObject.mVerticesBuffer.put(tempObject.VerticesBuffer);
		tempObject.mVerticesBuffer.flip();

		tempObject.mVerticesBuffer.position(0);
		if (mode > 5) {
			try {
				AssetManager as = context.getAssets();
				InputStream id = as.open("bananamap.jpg");
				loadTexture(id, 3);
				id = as.open("banana.jpg");
				loadTexture(id, 5);
			} catch (Exception e) {
				Log.e("ObjectLoader","texture file load error");
			}
		}
		else if(texFile != "") { //has texture
			try {
				AssetManager as = context.getAssets();
				InputStream id = as.open(texFile);
				loadTexture(id);
			} catch (Exception e) {
				Log.e("ObjectLoader","texture file load error");
			}
		}
	}
	
	public void Load(Context context, int objectID, String texFile) throws IOException {
		if (mode == 1 || mode == 3 || mode >= 6) {
			LoadSimple(context, objectID, texFile);
		} else if(mode == 2) {
			LoadComplex(context, objectID);
		}
	}
	
	public void LoadComplex(Context context, int objectId) throws IOException {

		byte[] temp = new byte[4];// ����Ʈ�迭�� float�� ��ȯ�Ҷ� ���Ǵ� �ӽ� ����Ʈ �迭
		byte[] stemp = new byte[2];// ����Ʈ�迭�� short�� ��ȯ�Ҷ� ���Ǵ� �ӽ� ����Ʈ �迭

		InputStream inputStream = context.getResources().openRawResource(
				objectId);// ���ҽ� ������ �о�´�.

		DataInputStream reader = new DataInputStream(inputStream);

		reader.read(temp);// ObjectStructListSize�д´�.
		ObjectStructListSize |= ((temp[0]) << 24) & 0xFF000000;
		ObjectStructListSize |= ((temp[1]) << 16) & 0xFF0000;
		ObjectStructListSize |= ((temp[2]) << 8) & 0xFF00;
		ObjectStructListSize |= (temp[3]) & 0xFF;

		// ObjectStructListSize��ŭ �ݺ��Ͽ� �о� bin�� �����ϴ� ��� ������Ʈ���� �д´�.
		for (int i = 0; i < ObjectStructListSize; i++) {

			tempObject = new Object();
			tempObject.mode = mode;

			reader.read(temp);// Indiceslength�б�
			tempObject.Indiceslength |= ((temp[0]) << 24) & 0xFF000000;
			tempObject.Indiceslength |= ((temp[1]) << 16) & 0xFF0000;
			tempObject.Indiceslength |= ((temp[2]) << 8) & 0xFF00;
			tempObject.Indiceslength |= (temp[3]) & 0xFF;

			reader.read(temp);// VerticesBufferlength�б�
			tempObject.VerticesBufferlength |= ((temp[0]) << 24) & 0xFF000000;
			tempObject.VerticesBufferlength |= ((temp[1]) << 16) & 0xFF0000;
			tempObject.VerticesBufferlength |= ((temp[2]) << 8) & 0xFF00;
			tempObject.VerticesBufferlength |= (temp[3]) & 0xFF;

			// �ش���̸�ŭ �迭�� �����.
			tempObject.Indices = new short[tempObject.Indiceslength];
			tempObject.VerticesBuffer = new float[tempObject.VerticesBufferlength];

			// Indiceslength��ŭ �ε����迭�� �о� ����
			short sresult;
			for (int j = 0; j < tempObject.Indiceslength; j++) {
				reader.read(stemp);
				sresult = 0;
				sresult |= ((stemp[0]) << 8) & 0xFF00;
				sresult |= (stemp[1]) & 0xFF;
				tempObject.Indices[j] = sresult;
			}

			// VerticesBufferlength��ŭ ���ؽ��迭�� �о� ����
			int result;
			for (int j = 0; j < tempObject.VerticesBufferlength; j++) {
				reader.read(temp);
				result = 0;
				result |= ((temp[0]) << 24) & 0xFF000000;
				result |= ((temp[1]) << 16) & 0xFF0000;
				result |= ((temp[2]) << 8) & 0xFF00;
				result |= (temp[3]) & 0xFF;
				tempObject.VerticesBuffer[j] = Float.intBitsToFloat(result);
			}

			// �ؽ������� �̸��� ����
			tempObject.map_Ka = reader.readUTF();

			// ���ؽ� ���۸� �����.
			tempObject.mVerticesBuffer = ByteBuffer
					.allocateDirect(tempObject.VerticesBufferlength * 4)
					.order(ByteOrder.nativeOrder()).asFloatBuffer();

			// ���ؽ����ۿ� ���ؽ� �迭�� �������ش�.
			tempObject.mVerticesBuffer.clear();
			tempObject.mVerticesBuffer.put(tempObject.VerticesBuffer, 0,
					tempObject.VerticesBuffer.length);
			tempObject.mVerticesBuffer.flip();

			// �ε������۸� �����.
			tempObject.mIndicesBuffer = ByteBuffer
					.allocateDirect(tempObject.Indices.length * 2)
					.order(ByteOrder.nativeOrder()).asShortBuffer();

			// �ε��� ���ۿ� �ε����迭�� �������ش�.
			tempObject.mIndicesBuffer.put(tempObject.Indices);

			// ���ؽ�����,�ε��������� ��ó���� �����Ͱ� ����Ű�� �Ѵ�.
			tempObject.mVerticesBuffer.position(0);
			tempObject.mIndicesBuffer.position(0);

			// �ؽ��Ŀ� ����� �̹����� �о� ���ε� �����ش�.
			try {
				AssetManager as = context.getAssets();
				InputStream id = as.open(tempObject.map_Ka);
				loadTexture(id);

			} catch (Exception e) {

			}

			// �Ѱ��� ������Ʈ������ ������ ��� �о����Ƿ�, ������Ʈlist�� �����Ѵ�.
			ObjectStructList.add(tempObject);

		}
	}


	// �ؽ��ĸ� ���ε��ϴ� �Լ�
	// InputStream is : �ؽ��ĸ� ���ε��� �̹���
	public void loadTexture(InputStream is) {

		GLES20.glPixelStorei(GLES20.GL_UNPACK_ALIGNMENT, 1);
		GLES20.glGenTextures(1, tempObject.textureIds, 0);
		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, tempObject.textureIds[0]);

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
	
	public void loadTexture(InputStream is, int Offset) {

		GLES20.glPixelStorei(GLES20.GL_UNPACK_ALIGNMENT, 1);
		GLES20.glGenTextures(1, tempObject.textureIds, Offset);
		GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + Offset);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, tempObject.textureIds[Offset]);

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

	// face������ ����Ű�� ���ؽ��� �˾Ƴ��� �Լ�
	static int getIndex(String index, int size) {
		int idx = Integer.parseInt(index);
		if (idx < 0)
			return size + idx;
		else
			return idx - 1;
	}

}
