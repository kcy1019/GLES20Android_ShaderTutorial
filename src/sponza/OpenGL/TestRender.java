package sponza.OpenGL;

import Object.AxisX;
import Object.AxisY;
import Object.AxisZ;
import Object.ObjectLoader;
import Object.Object;
import Object.Wall;
import Test.OpenGL.*;
import android.content.Context;
import android.opengl.*;
import android.util.*;
import android.opengl.Matrix;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Stack;
import java.lang.Math;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class TestRender implements GLSurfaceView.Renderer {

	/* Constructor */
	public TestRender(Context context) {
		mContext = context;
		Matrix.setIdentityM(mMVStack, 0);
		Matrix.setIdentityM(mPStack, 0);
		mMVStackOffset += 16;
		mPStackOffset += 16;
	}

	private String ReadShader(Context context, int ShaderID) {
		StringBuffer shaderbuffer = new StringBuffer();

		// read the files
		try {
			InputStream inputStream = context.getResources().openRawResource(
					ShaderID);
			// setup Buffered reader
			BufferedReader in = new BufferedReader(new InputStreamReader(
					inputStream));

			String read = in.readLine();
			while (read != null) {
				shaderbuffer.append(read + "\n");
				read = in.readLine();
			}

			shaderbuffer.deleteCharAt(shaderbuffer.length() - 1);

		} catch (Exception e) {
			Log.d("ERROR-readingShader",
					"Could not read shader: " + e.getLocalizedMessage());
		}

		return shaderbuffer.toString();
	}

	private static int loadShader(int type, String shaderSrc) {
		int shader;
		int[] compiled = new int[1];

		// Create the shader object
		shader = GLES20.glCreateShader(type);

		if (shader == 0)
			return 0;

		// Load the shader source
		GLES20.glShaderSource(shader, shaderSrc);

		// Compile the shader
		GLES20.glCompileShader(shader);

		// Check the compile status
		GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);

		if (compiled[0] == 0) {
			Log.e("Error", GLES20.glGetShaderInfoLog(shader));
			GLES20.glDeleteShader(shader);
			return 0;
		}
		return shader;
	}

	public void onSurfaceCreated(GL10 glUnused, EGLConfig config) {

		int[] VertexShaderId = new int[programCount];
		int[] FragmentShaderId = new int[programCount];

		VertexShaderId[0] = R.raw.phongvertexshader;
		FragmentShaderId[0] = R.raw.phongfragmentshader;
		VertexShaderId[1] = R.raw.gouraudvertexshader;
		FragmentShaderId[1] = R.raw.gouraudfragmentshader;
		VertexShaderId[2] = R.raw.netvertexshader;
		FragmentShaderId[2] = R.raw.netfragmentshader;
		VertexShaderId[3] = R.raw.noisevertexshader;
		FragmentShaderId[3] = R.raw.noisefragmentshader;
		VertexShaderId[4] = R.raw.bumpvertexshader;
		FragmentShaderId[4] = R.raw.bumpfragmentshader;

		String[] vShaderStr = new String[programCount];
		String[] fShaderStr = new String[programCount];

		// Load the shader and get a linked program object
		int[] linked = new int[1];
		int[] vertexShader = new int[programCount];
		int[] fragmentShader = new int[programCount];

		for (int i = 0; i < programCount; ++i) {
			vShaderStr[i] = ReadShader(mContext, VertexShaderId[i]);
			fShaderStr[i] = ReadShader(mContext, FragmentShaderId[i]);

			// Load the shader and get a linked program object
			vertexShader[i] = loadShader(GLES20.GL_VERTEX_SHADER, vShaderStr[i]);
			fragmentShader[i] = loadShader(GLES20.GL_FRAGMENT_SHADER,
					fShaderStr[i]);
			if ((vertexShader[i] == 0) || (fragmentShader[i] == 0))
				return;

			data[i] = new Userdata();
			data[i].Program = GLES20.glCreateProgram();
			if (data[i].Program == 0)
				return;	

			GLES20.glAttachShader(data[i].Program, vertexShader[i]);
			GLES20.glAttachShader(data[i].Program, fragmentShader[i]);
			GLES20.glLinkProgram(data[i].Program);

			// Link the program
			GLES20.glGetProgramiv(data[i].Program,
					GLES20.GL_LINK_STATUS, linked, 0);

			if (linked[0] == 0) {
				Log.e("ERROR", "Error linking program:");
				Log.e("ERROR",
						GLES20.glGetProgramInfoLog(data[i].Program));
				GLES20.glDeleteProgram(data[i].Program);
				return;
			}

			// Free up no longer needed shader resources
			GLES20.glDeleteShader(vertexShader[i]);
			GLES20.glDeleteShader(fragmentShader[i]);

			// Get attribute locations
			data[i].VertexLoc = GLES20.glGetAttribLocation(data[i].Program,
					"a_Vertex");
			data[i].NormalLoc = GLES20.glGetAttribLocation(data[i].Program, "a_Normal");
			data[i].TextureLoc = GLES20.glGetAttribLocation(data[i].Program, "a_Texture");

			// Get uniform locations
			data[i].ModelViewMatrixLoc = GLES20.glGetUniformLocation(data[i].Program,
					"u_ModelViewMatrix");
			data[i].NormalMatrixLoc = GLES20.glGetUniformLocation(data[i].Program,
					"u_NormalMatrix");
			data[i].ModelViewProjectionMatrixLoc = GLES20.glGetUniformLocation(data[i].Program,
					"u_ModelViewProjectionMatrix");
			
			data[i].TexSamplerLoc = GLES20.glGetUniformLocation(data[i].Program,
					"u_Texture");
			
			data[i].TexNormalLoc = GLES20.glGetUniformLocation(data[i].Program,
					"u_Normalmap");
			
			data[i].IsTexturedLoc = GLES20.glGetUniformLocation(data[i].Program,
					"u_isTextured");
			
			data[i].IsBumpedLoc = GLES20.glGetUniformLocation(data[i].Program,
					"u_isBumped");
			
			data[i].IsHoledLoc = GLES20.glGetUniformLocation(data[i].Program,
					"u_isHoled");

			data[i].LightPosLoc = GLES20.glGetUniformLocation(data[i].Program,
					"u_lightPosition");
			data[i].EyeLightPosLoc = GLES20.glGetUniformLocation(data[i].Program,
					"u_eyelightPosition");
			data[i].SpotLightPosLoc = GLES20.glGetUniformLocation(data[i].Program,
					"u_spotlightPosition");
			
			data[i].spotambientMatLoc = GLES20.glGetUniformLocation(data[i].Program,
					"u_spotambientMat");
			data[i].spotdiffuseMatLoc = GLES20.glGetUniformLocation(data[i].Program,
					"u_spotdiffuseMat");
			data[i].spotspecularMatLoc = GLES20.glGetUniformLocation(data[i].Program,
					"u_spotspecularMat");
			data[i].spotattMatLoc = GLES20.glGetUniformLocation(data[i].Program,
					"u_spotAtt");
			
			data[i].eyedirLoc = GLES20.glGetUniformLocation(data[i].Program,
					"u_eyeDirection");
			data[i].eyeambientMatLoc = GLES20.glGetUniformLocation(data[i].Program,
					"u_eyeambientMat");
			data[i].eyediffuseMatLoc = GLES20.glGetUniformLocation(data[i].Program,
					"u_eyediffuseMat");
			data[i].eyespecularMatLoc = GLES20.glGetUniformLocation(data[i].Program,
					"u_eyespecularMat");
			data[i].eyeattMatLoc = GLES20.glGetUniformLocation(data[i].Program,
					"u_eyeAtt");

			data[i].ambientMatLoc = GLES20
					.glGetUniformLocation(data[i].Program, "u_ambientMat");
			data[i].diffuseMatLoc = GLES20
					.glGetUniformLocation(data[i].Program, "u_diffuseMat");
			data[i].specMatLoc = GLES20.glGetUniformLocation(data[i].Program,
					"u_specMat");
			data[i].specPowLoc = GLES20.glGetUniformLocation(data[i].Program,
					"u_specPow");
		}

		teapot1 = new ObjectLoader();
		cow1 = new ObjectLoader();
		banana1 = new ObjectLoader();
		tree1 = new ObjectLoader();
		//tiger1 = new ObjectLoader();
		cow1.mode = SIMPLE;
		tree1.mode = SIMPLENORMALTEX;
		teapot1.mode = COMPLEX;
		//tiger1.mode = SIMPLETEX;
		banana1.mode = SIMPLENORMALTEX;
		//Log.e("Error","d : " + GLES20.GL_MAX_VERTEX_TEXTURE_IMAGE_UNITS); 
		try {
			teapot1.Load(mContext, R.raw.teapot1, "");
			cow1.Load(mContext, R.raw.cow, "");
			//tiger1.Load(mContext, R.raw.tiger1, "tiger_tex.jpg");
			tiger = new ObjectLoader[tigerCount];
			for (int i = 0; i < tigerCount; i++) {
				tiger[i] = new ObjectLoader();
				tiger[i].mode = SIMPLETEX;
			}
			
			tiger[0].Load(mContext, R.raw.tiger1, "tiger_tex.jpg");
			tiger[1].Load(mContext, R.raw.tiger2, "tiger_tex.jpg");
			tiger[2].Load(mContext, R.raw.tiger3, "tiger_tex.jpg");
			tiger[3].Load(mContext, R.raw.tiger4, "tiger_tex.jpg");
			tiger[4].Load(mContext, R.raw.tiger5, "tiger_tex.jpg");
			tiger[5].Load(mContext, R.raw.tiger6, "tiger_tex.jpg");
			tiger[6].Load(mContext, R.raw.tiger7, "tiger_tex.jpg");
			tiger[7].Load(mContext, R.raw.tiger8, "tiger_tex.jpg");
			tiger[8].Load(mContext, R.raw.tiger9, "tiger_tex.jpg");
			tiger[9].Load(mContext, R.raw.tiger10, "tiger_tex.jpg");
			tiger[10].Load(mContext, R.raw.tiger11, "tiger_tex.jpg");
			tiger[11].Load(mContext, R.raw.tiger12, "tiger_tex.jpg");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		axisX = new AxisX();
		axisY = new AxisY();
		axisZ = new AxisZ();
		mWall = new Wall();
		sWall = new Wall();

		axisX.initShapes();
		axisY.initShapes();
		axisZ.initShapes();
		try {
			mWall.initShapes(mContext, -1);
			sWall.initShapes(mContext, -2);
			tree1.Load(mContext, R.raw.palm, "");
			banana1.Load(mContext, R.raw.banana, "");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		mCamera = new Camera();

		// depth test
		GLES20.glEnable(GLES20.GL_DEPTH_TEST);
		// cull backface
		//GLES20.glEnable(GLES20.GL_CULL_FACE);
		//GLES20.glCullFace(GLES20.GL_BACK);

	}
	
	
	// Matrix Stack Interface.
	private void PushMatrix()
	{
		float[] temp = new float[16];
		System.arraycopy( mMMatrix, 0, temp, 0, mMMatrix.length );
		matStack.push(temp);
	}
	
	private void PopMatrix()
	{
		float[] temp = new float[16];
		temp = matStack.pop();
		System.arraycopy( temp, 0, mMMatrix, 0, mMMatrix.length );
	}
	
	// Matrix Restoration(to the first state).
	private void RestoreMatrix()
	{
		Matrix.setIdentityM(mMMatrix, 0);
		/*Matrix.rotateM(mMMatrix, 0, ObjectRotateZ, 0, 0, 1.0f);
		Matrix.rotateM(mMMatrix, 0, ObjectRotateY, 0, 1.0f, 0);
		Matrix.rotateM(mMMatrix, 0, ObjectRotateX, 1.0f, 0, 0);*/
	}

	private void nextFrame()
	{
		tigerPrev = System.currentTimeMillis();
		// If-Statements are better than modulo operation
		// when performing speed-critical tasks.
		tigerAngle = tigerAngle + 1.0f;
		BananaAngle = BananaAngle + 3.0f;
		if (tigerAngle >= 360.0f) {
			tigerAngle = 0.0f;
			BananaAngle = 0.0f;
		}
		
		++tigerPhase;
		
		// Hurted Tiger! 
		if (tigerPhase >= 7/*tigerCount*/) {
			tigerPhase = 0;
		}

	}
	
	public void onDrawFrame(GL10 glUnused) {
		GLES20.glClearColor(0.11f, 0.11f, 0.11f, 1.0f);

		// Clear the color buffer
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

		// Set up projection matrix
		mCamera.Yaw(ObjectRotate = ObjectRotateZ);
		float fovy = mCamera.getFovy();
		Perspective(fovy, ratio, NEAR, FAR);
		mVMatrix = mCamera.GetViewMatrix();

		Matrix.multiplyMM(mMVStack, mMVStackOffset, mMVStack,
				mMVStackOffset - 16, mVMatrix, 0);
		mMVStackOffset += 16; // Push Matrix
		
		// BEGIN [#SCENE]
					
			Matrix.setIdentityM(mMMatrix, 0); // 바닥 
				drawObject(data[renderMode], mWall, 0, 0, 10, 0.25f, ChromeA, ChromeD, ChromeS);
				
			Matrix.setIdentityM(mMMatrix, 0); // 후방 벽
				Matrix.translateM(mMMatrix, 0, -2f, 0f, 2f);
				Matrix.rotateM(mMMatrix, 0, 90f, 0f, 1f, 0f); 
				drawObject(data[renderMode], sWall, 0, 0, 10, 0.25f, ChromeA, ChromeD, ChromeS);
			
			Matrix.setIdentityM(mMMatrix, 0); // 왼쪽 벽
				Matrix.translateM(mMMatrix, 0, 0f, 2.5f, 2f);
				Matrix.rotateM(mMMatrix, 0, 90f, 0f, 0f, 1f);
				Matrix.rotateM(mMMatrix, 0, 90f, 0f, 1f, 0f);
				drawObject(data[renderMode], mWall, 0, 0, 10, 0.25f, ChromeA, ChromeD, ChromeS);
			
			Matrix.setIdentityM(mMMatrix, 0); // 오른쪽 벽
				Matrix.translateM(mMMatrix, 0, 0f, -2.5f, 2f);
				Matrix.rotateM(mMMatrix, 0, -90f, 0f, 0f, 1f);
				Matrix.rotateM(mMMatrix, 0, 90f, 0f, 1f, 0f);
				drawObject(data[renderMode], mWall, 0, 0, 10, 0.25f, ChromeA, ChromeD, ChromeS);
			
		
			if (tigerPrev == 0) {
				tigerPrev = System.currentTimeMillis();
			}
			if (System.currentTimeMillis() - tigerPrev >= tigerTransition) {
				nextFrame();
			}
			
			RestoreMatrix();
			
			PushMatrix();
				//GLES20.glFrontFace(GLES20.GL_CW);
				//Log.i("Info", "tigerPhase = " + tigerPhase);
				Matrix.translateM(mMMatrix, 0, 1f * (float)Math.cos(tigerAngle / 180. * 3.141592),
					1f * (float)Math.sin(tigerAngle / 180. * 3.141592), 0f);
				Matrix.rotateM(mMMatrix, 0, 180f + tigerAngle, 0, 0, 1.0f);
				drawObject(data[renderMode], tiger[tigerPhase].tempObject, 0f, 0f, 0f, 0.005f, ChromeA, ChromeD, ChromeS);
				//GLES20.glFrontFace(GLES20.GL_CCW);
			PopMatrix();
			
			for (int i = 0; i < teapot1.ObjectStructList.size(); i++) {
				PushMatrix();
					Matrix.rotateM(mMMatrix, 0, 90, 1f, 0f, 0f);
					drawObject(data[renderMode],teapot1.ObjectStructList.get(i), 0, 0, 0, 0.01f, ChromeA, ChromeD, ChromeS);
				PopMatrix();
			}
			
			CowDirAngle = (90f + tigerAngle - 60f + 360f);
			CowPosAngle = (tigerAngle - 60f + 360f);
			if (CowDirAngle >= 360.0f) {
				CowDirAngle -= 360.0f;
			}
			if (CowPosAngle >= 360.0f) {
				CowPosAngle -= 360.0f;
			}
			
			PushMatrix();
				Matrix.translateM(mMMatrix, 0, -1f, -1f, 1f);
				Matrix.rotateM(mMMatrix, 0, 90f, 1f, 0, 0f);
				drawObject(data[renderMode], tree1.tempObject, 0, 0, 0, 3.01f, BronzeA, BronzeD, BronzeS);
			PopMatrix();
			
			PushMatrix();
				Matrix.translateM(mMMatrix, 0, 1f * (float)Math.cos(CowPosAngle / 180. * 3.141592),
					1f * (float)Math.sin( CowPosAngle / 180. * 3.141592), 0.3f);
				Matrix.rotateM(mMMatrix, 0, CowDirAngle, 0, 0, 1.0f);
				Matrix.rotateM(mMMatrix, 0, 90f, 1f, 0, 0f);
				drawObject(data[renderMode], cow1.tempObject, 0, 0, 0, 1.00f, RubyA, RubyD, RubyS);
				Matrix.translateM(mMMatrix, 0, 0f, 0.3f, 0f);
				drawObject(data[renderMode], cow1.tempObject, 0, 0, 0, 0.33f, BronzeA, BronzeD, BronzeS);
				Matrix.translateM(mMMatrix, 0, 0f, -0.3f, 0f);
				Matrix.rotateM(mMMatrix, 0, BananaAngle, 0f, 1f, 0f);
				drawObject(data[renderMode], banana1.tempObject, 0f, 0f, 0f, 2.00f, RubyA, RubyD, RubyS);
			PopMatrix();
			
				
		// END [#SCENE]

		// x축 : RED
		Matrix.setIdentityM(mMMatrix, 0);
		drawObject(data[renderMode],axisX, 0, 0, 0, 1.0f, ColorRed, ColorRed, ColorRed);

		// Y축 : GREEN
		Matrix.setIdentityM(mMMatrix, 0);
		drawObject(data[renderMode],axisY, 0, 0, 0, 1.0f, ColorGreen, ColorGreen, ColorGreen);

		// z축 : BLUE
		Matrix.setIdentityM(mMMatrix, 0);
		drawObject(data[renderMode],axisZ, 0, 0, 0, 1.0f, ColorBlue, ColorBlue, ColorBlue);
 
		mMVStackOffset -= 16; // Pop Matrix
		mCamera.Yaw(-ObjectRotate);
	}

	// 오브젝트를 그리는 함수
	// Object object : 그릴대상이 되는 오브젝트
	// float x : 오브젝트의 x축이동
	// float y : 오브젝트의 y축이동
	// float z : 오브젝트의 z축이동
	// float scale : 오브젝트 몇배 크기 확대
	// float[] matAmbient : 오브젝트를 칠할 색상)
	public void drawObject(Userdata data,Object object, float x, float y, float z,
			float scale, float[] ambientMat, float[] diffuseMat, float[] specularMat)
	{ 
		PushMatrix();
			Matrix.scaleM(mMMatrix, 0, scale, scale, scale);
			Matrix.translateM(mMMatrix, 0, x, y, z);//
			Matrix.multiplyMM(mMVStack, mMVStackOffset, mMVStack,
					mMVStackOffset - 16, mMMatrix, 0);
			mMVStackOffset += 16; // Push Matrix
		PopMatrix();

		Matrix.setIdentityM(tempMatrix, 0);
		Matrix.setIdentityM(ModelViewMatrix, 0);
		Matrix.multiplyMM(ModelViewMatrix, 0, tempMatrix, 0, mMVStack,
				mMVStackOffset - 16);

		Matrix.setIdentityM(NormalMatrix, 0);
		Matrix.setIdentityM(tempMatrix, 0);
		Matrix.invertM(tempMatrix, 0, ModelViewMatrix, 0);
		Matrix.transposeM(NormalMatrix, 0, tempMatrix, 0);

		Matrix.multiplyMM(ModelViewProjectionMatrix, 0, mPStack, mPStackOffset - 16, mMVStack,
				mMVStackOffset - 16);

		GLES20.glUniformMatrix4fv(data.ModelViewMatrixLoc, 1, false, ModelViewMatrix, 0);
		GLES20.glUniformMatrix4fv(data.NormalMatrixLoc, 1, false, NormalMatrix, 0);
		GLES20.glUniformMatrix4fv(data.ModelViewProjectionMatrixLoc, 1, false, ModelViewProjectionMatrix, 0);

		GLES20.glUniform4fv(data.LightPosLoc, 1, lightPosition, 0);
		GLES20.glUniform4fv(data.SpotLightPosLoc, 1, spotlightPosition, 0);
		GLES20.glUniform4f(data.EyeLightPosLoc, 1f * (float)Math.cos(CowPosAngle / 180. * 3.141592), 1f * (float)Math.sin( CowPosAngle / 180. * 3.141592), 0.3f, 1f);
		//GLES20.glUniform3f(data.eyedirLoc, 1f * (float)Math.cos( CowDirAngle / 180. * 3.141592), 1f * (float)Math.sin(CowDirAngle / 180. * 3.141592), 0f);
		if (120 < CowDirAngle && CowDirAngle < 240.0) {
			GLES20.glUniform3f(data.eyedirLoc, -1f * (float)Math.sin( (-CowDirAngle) / 180. * 3.141592), 0f, 1f * (float)Math.cos((-CowDirAngle) / 180. * 3.141592));
		} else {
			GLES20.glUniform3f(data.eyedirLoc, -1f * (float)Math.sin( CowDirAngle / 180. * 3.141592), 0f, 1f * (float)Math.cos(CowDirAngle / 180. * 3.141592));
		}

		GLES20.glUniform4fv(data.ambientMatLoc, 1, ambientMat, 0);
		GLES20.glUniform4fv(data.diffuseMatLoc, 1, diffuseMat, 0);
		GLES20.glUniform4fv(data.specMatLoc, 1, specularMat, 0);
		GLES20.glUniform1f(data.specPowLoc, 76.8f);
		
		//76.8
		GLES20.glUniform3f(data.spotattMatLoc, 1.0f, 0.5f, 0.009f);
		GLES20.glUniform4f(data.spotambientMatLoc, 0.0215f, 0.1745f, 0.0215f, 1.0f); 
		GLES20.glUniform4f(data.spotdiffuseMatLoc, 0.07568f, 0.61424f, 0.07568f, 1.0f);
		GLES20.glUniform4f(data.spotspecularMatLoc, 0.633f, 0.727811f, 0.633f, 1.0f);
		
		GLES20.glUniform3f(data.eyeattMatLoc, 1.0f, 0.5f, 0.009f);
		GLES20.glUniform4f(data.eyeambientMatLoc, 0.1f, 0.18725f, 0.1745f, 1.0f);
		GLES20.glUniform4f(data.eyediffuseMatLoc, 0.396f, 0.74151f, 0.69102f, 1.0f);
		GLES20.glUniform4f(data.eyespecularMatLoc, 0.297254f, 0.30829f, 0.306678f, 1.0f);

		object.draw(data);

		mMVStackOffset -= 16;
	}
	public void onSurfaceChanged(GL10 glUnused, int width, int height) {
		// Set the viewport
		GLES20.glViewport(0, 0, width, height);
		ratio = (float) width / (float) height;

		// Set up projection matrix
		float fovy = mCamera.getFovy();
		Perspective(fovy, ratio, NEAR, FAR);
	}

	// Perspective함수
	void Perspective(float fovy, float aspect, float nearZ, float farZ) {
		float frustumW, frustumH;
		frustumH = nearZ * FloatMath.sin(fovy / 2) / FloatMath.cos(fovy); // near * tan
		frustumW = frustumH * aspect;
		Matrix.setIdentityM(mPStack, 0);
		Matrix.frustumM(mPStack, mPStackOffset - 16, -frustumW, frustumW,
				-frustumH, frustumH, nearZ, farZ);
	}

	// Context
	private Context mContext;

	private final static int programCount = 5;

	private float[] mMVStack = new float[256];
	private float[] mPStack = new float[256];
	private int mMVStackOffset = 0;
	private int mPStackOffset = 0;

	// MVP matrix
	private float[] ModelViewMatrix = new float[16];
	private float[] NormalMatrix = new float[16];
	private float[] ModelViewProjectionMatrix = new float[16];
	private float[] tempMatrix = new float[16];

	// Model Matrix
	private float[] mMMatrix = new float[16];

	// View Matrix
	private float[] mVMatrix = new float[16];
	
	// Arbitary Matrix Stack
	private Stack<float[]> matStack = new Stack<float[]>();

	private float ratio;
	private final static float NEAR = 0.1f;
	private final static float FAR  = 100000.0f;
	
	// Shader Programs(for readability).
	private final static int PHONGSHADER  = 0;
	private final static int GOURADSHADER = 1;
	private final static int NETSHADER    = 2;
	private final static int NOISESHADER  = 3;
	private final static int BUMPSHADER   = 4;
	
	// Mesh Type(for readability).
	private final static int SIMPLE    		 = 1;
	private final static int COMPLEX  		 = 2;
	private final static int SIMPLETEX 		 = 3;
	private final static int SIMPLENORMALTEX = 6; // + Offset(4x).

	// 카메라 클래스
	public static int renderMode;//0 퐁 //1 고라드

	// 카메라 클래스
	public Camera mCamera;

	// 사용하는 쉐이더의 program,attribute등을 갖고있는 클래스
	private Userdata[] data = new Userdata[programCount];

	// Object data
	private ObjectLoader teapot1;
	private ObjectLoader cow1;
	private ObjectLoader banana1;
	//private ObjectLoader tiger1;
	private ObjectLoader[] tiger = null;
	private ObjectLoader tree1;
	
	public float ObjectRotate;
	public float ObjectRotateX;
	public float ObjectRotateY;
	public float ObjectRotateZ;
	
	public long tigerPrev   = 0;
	public float tigerAngle = 0.0f;
	public int tigerPhase   = 0;
	public static final int tigerCount      = 12;
	public static final int tigerTransition = 175;

	// 색상저장한 변수
	float[] ColorPink  = { 1.00f, 0.2f, 0.48f, 1.0f };
	float[] ColorGray  = { 111.0f/255f, 111.0f/255f, 111.0f/255f, 1.0f };
	float[] ColorBrown = { 160.0f/255f, 82.0f/255f, 45.0f/255f, 1.0f };
	float[] ColorGold  = { 255.0f/255f, 215.0f/255f, 0f/255f, 1.0f };
	float[] ColorRed   = { 1.0f, 0.0f, 0.0f, 1.0f };
	float[] ColorGreen = { 0.0f, 1.0f, 0.0f, 1.0f };
	float[] ColorBlue  = { 0.0f, 0.0f, 1.0f, 1.0f };
	
	float[] RubyA = {0.1745f, 0.01175f, 0.01175f, 1f};
	float[] RubyD = {0.61424f, 0.04136f, 0.04136f, 1f};
	float[] RubyS = {0.727811f, 0.626959f, 0.626959f, 1f};
	
	float[] ChromeA = {0.25f, 0.25f, 0.25f, 1f};
	float[] ChromeD = {0.4f, 0.4f, 0.4f, 1f};
	float[] ChromeS = {0.774597f, 0.774597f, 0.774597f, 1f};
	
	float[] BronzeA = {0.2125f, 0.1275f, 0.054f, 1f};
	float[] BronzeD = {0.714f, 0.4284f, 0.18144f, 1f};
	float[] BronzeS = {0.393548f, 0.271906f, 0.166721f, 1f};
	
	private float CowPosAngle;
	private float CowDirAngle;
	private float BananaAngle;

	// 축오브젝트
	private AxisX axisX;
	private AxisY axisY;
	private AxisZ axisZ;
	private Wall  mWall;
	private Wall  sWall;

	// Light info
	private float[] lightPosition     = { -3.0f, 3.0f, -3.0f, 1.0f };
	private float[] eyelightPosition  = { 0f, 0f, 0f, 1f };
	private float[] spotlightPosition = { -8f, 0.0f, -8.0f, 1.0f }; 

	// 조명관련 변수
	private float[] diffuseMat = { 0.5f, 0.5f, 0.5f, 1.0f };
	private float[] specMat    = { 0.508273f, 0.508273f, 0.508273f, 1.0f };
	private float specPow      = 10.0f;
}
 