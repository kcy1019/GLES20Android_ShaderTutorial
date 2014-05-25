package sponza.OpenGL;

public class Userdata {

	// Handle to a program object
	public int Program;

	// Attribute locations
	public int VertexLoc;
	public int NormalLoc;
	public int TextureLoc;
	
	// Uniform location
	public int ModelViewMatrixLoc;
	public int NormalMatrixLoc;
	public int ModelViewProjectionMatrixLoc;
	public int IsTexturedLoc;
	public int IsHoledLoc;
	public int IsBumpedLoc;
	public int TexSamplerLoc;
	public int TexNormalLoc;
	
	public int LightPosLoc;
	public int EyeLightPosLoc;
	public int SpotLightPosLoc;
		
	public int ambientMatLoc;
	public int diffuseMatLoc;
	public int specMatLoc;
	public int specPowLoc;

	// spot light on the wall
	public int spotambientMatLoc;
	public int spotdiffuseMatLoc;
	public int spotspecularMatLoc;
	public int spotattMatLoc;
	
	// spot light in the eye of cow 
	public int eyeambientMatLoc;
	public int eyediffuseMatLoc;
	public int eyespecularMatLoc;
	public int eyeattMatLoc;
	public int eyedirLoc;
}
