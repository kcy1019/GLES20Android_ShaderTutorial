uniform mat4 u_ModelViewMatrix;
uniform mat4 u_NormalMatrix;
uniform mat4 u_ModelViewProjectionMatrix;


attribute vec4 a_Vertex;
attribute vec3 a_Normal;

varying vec3 N;
varying vec4 v;

void main(void)
{
	v = u_ModelViewMatrix * a_Vertex;
	N = normalize(mat3(u_NormalMatrix) * a_Normal );
   
	gl_Position = u_ModelViewProjectionMatrix * a_Vertex;  
}

