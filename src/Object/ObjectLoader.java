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


//bin파일을 이용하여 오브젝트를 그릴때 사용하는 클래스
//bin파일은 여러개의 오브젝트들로 구성되어있다. 
//그림을 그릴때 오브젝트 수많큼 반복하려 그림을 그려야한다.
public class ObjectLoader {

	int ObjectStructListSize = 0;// bin파일을 구성하는 오브젝트의 전체 수를 저장한 변수
	public ArrayList<Object> ObjectStructList = new ArrayList<Object>();// bin파일을
																		// 읽어와
																		// 알아낸
																		// 오브젝트들을
																		// 차례로
																		// 저장한
																		// list
	public Object tempObject = null;// 한개의 오브젝트를 저장하기위한 임시 변수
	public int mode = 1;

	// bin파일을 이용 Object를 읽고, 읽어들인 Object 순서대로 차례로 ObjectStructList에 저장
	// Context context : 어떤 안드로이드프로그램이 이용되고 있는지 저장하는 변수
	// int objectId : 어던 bin파일을 이용하지 넘겨준 값
	/*
	 * bin파일 구성 :ObjectStructListSize : bin파일을 구성하는 전체 오브젝트 갯수{ Indiceslength;
	 * 오브젝트 한개의 인덱스배열의 길이 VerticesBufferlength; 오브젝트 한개의 버택스 배열의 길이 protected
	 * short[] Indices; 오브젝트 한개의 인덱스 배열 protected float[] VerticesBuffer; : 오브젝트
	 * 한개의 버텍스 배열 public String map_Ka = "";// 텍스쳐 이용시 어떤이미지를 사용하는지 저장한 변수 }<-
	 * 다음구성이 ObjectStructListSize만큼 반복된다.
	 */
	public void LoadSimple(Context context, int objectId, String texFile) throws IOException {
		tempObject = new Object();
		tempObject.mode = mode;
		InputStream inputStream = context.getResources().openRawResource(objectId);// 리소스 파일을 읽어온다.
		DataInputStream reader = new DataInputStream(inputStream);
		byte[] temp = new byte[4];// 바이트배열을 float로 변환할때 사용되는 임시 바이트 배열
		reader.read(temp);// FloatLength읽는다.
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

		// 버텍스 버퍼를 만든다.
		tempObject.mVerticesBuffer = ByteBuffer.allocateDirect(tempObject.floatLength*4).order(ByteOrder.nativeOrder()).asFloatBuffer();
		// 버텍스버퍼에 버텍스 배열을 저장해준다.
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

		byte[] temp = new byte[4];// 바이트배열을 float로 변환할때 사용되는 임시 바이트 배열
		byte[] stemp = new byte[2];// 바이트배열을 short로 변환할때 사용되는 임시 바이트 배열

		InputStream inputStream = context.getResources().openRawResource(
				objectId);// 리소스 파일을 읽어온다.

		DataInputStream reader = new DataInputStream(inputStream);

		reader.read(temp);// ObjectStructListSize읽는다.
		ObjectStructListSize |= ((temp[0]) << 24) & 0xFF000000;
		ObjectStructListSize |= ((temp[1]) << 16) & 0xFF0000;
		ObjectStructListSize |= ((temp[2]) << 8) & 0xFF00;
		ObjectStructListSize |= (temp[3]) & 0xFF;

		// ObjectStructListSize만큼 반복하여 읽어 bin을 구성하는 모든 오브젝트들을 읽는다.
		for (int i = 0; i < ObjectStructListSize; i++) {

			tempObject = new Object();
			tempObject.mode = mode;

			reader.read(temp);// Indiceslength읽기
			tempObject.Indiceslength |= ((temp[0]) << 24) & 0xFF000000;
			tempObject.Indiceslength |= ((temp[1]) << 16) & 0xFF0000;
			tempObject.Indiceslength |= ((temp[2]) << 8) & 0xFF00;
			tempObject.Indiceslength |= (temp[3]) & 0xFF;

			reader.read(temp);// VerticesBufferlength읽기
			tempObject.VerticesBufferlength |= ((temp[0]) << 24) & 0xFF000000;
			tempObject.VerticesBufferlength |= ((temp[1]) << 16) & 0xFF0000;
			tempObject.VerticesBufferlength |= ((temp[2]) << 8) & 0xFF00;
			tempObject.VerticesBufferlength |= (temp[3]) & 0xFF;

			// 해당길이만큼 배열을 만든다.
			tempObject.Indices = new short[tempObject.Indiceslength];
			tempObject.VerticesBuffer = new float[tempObject.VerticesBufferlength];

			// Indiceslength만큼 인덱스배열을 읽어 저장
			short sresult;
			for (int j = 0; j < tempObject.Indiceslength; j++) {
				reader.read(stemp);
				sresult = 0;
				sresult |= ((stemp[0]) << 8) & 0xFF00;
				sresult |= (stemp[1]) & 0xFF;
				tempObject.Indices[j] = sresult;
			}

			// VerticesBufferlength만큼 버텍스배열을 읽어 저장
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

			// 텍스쳐파일 이름을 읽음
			tempObject.map_Ka = reader.readUTF();

			// 버텍스 버퍼를 만든다.
			tempObject.mVerticesBuffer = ByteBuffer
					.allocateDirect(tempObject.VerticesBufferlength * 4)
					.order(ByteOrder.nativeOrder()).asFloatBuffer();

			// 버텍스버퍼에 버텍스 배열을 저장해준다.
			tempObject.mVerticesBuffer.clear();
			tempObject.mVerticesBuffer.put(tempObject.VerticesBuffer, 0,
					tempObject.VerticesBuffer.length);
			tempObject.mVerticesBuffer.flip();

			// 인덱스버퍼를 만든다.
			tempObject.mIndicesBuffer = ByteBuffer
					.allocateDirect(tempObject.Indices.length * 2)
					.order(ByteOrder.nativeOrder()).asShortBuffer();

			// 인덱스 버퍼에 인덱스배열을 저장해준다.
			tempObject.mIndicesBuffer.put(tempObject.Indices);

			// 버텍스버퍼,인덱스버퍼의 맨처음을 포인터가 가르키게 한다.
			tempObject.mVerticesBuffer.position(0);
			tempObject.mIndicesBuffer.position(0);

			// 텍스쳐에 사용할 이미지를 읽어 바인딩 시켜준다.
			try {
				AssetManager as = context.getAssets();
				InputStream id = as.open(tempObject.map_Ka);
				loadTexture(id);

			} catch (Exception e) {

			}

			// 한개의 오브젝트에관련 내용을 모두 읽었으므로, 오브젝트list에 저장한다.
			ObjectStructList.add(tempObject);

		}
	}


	// 텍스쳐를 바인딩하는 함수
	// InputStream is : 텍스쳐를 바인딩할 이미지
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

	// face정보가 가르키는 버텍스를 알아내는 함수
	static int getIndex(String index, int size) {
		int idx = Integer.parseInt(index);
		if (idx < 0)
			return size + idx;
		else
			return idx - 1;
	}

}
