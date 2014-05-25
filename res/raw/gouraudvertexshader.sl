precision mediump float;

uniform mat4 u_ModelViewMatrix;
uniform mat4 u_NormalMatrix;
uniform mat4 u_ModelViewProjectionMatrix;

attribute vec2 a_Texture;
attribute vec4 a_Vertex;
attribute vec3 a_Normal;
uniform vec4 u_lightPosition;

uniform vec4 u_ambientMat;
uniform vec4 u_diffuseMat;
uniform vec4 u_specMat;
uniform float u_specPow;


uniform sampler2D u_Texture;
uniform sampler2D u_Normalmap;
uniform float u_isTextured;

varying vec4 color;
varying vec2 v_Texture;
varying vec3 N;
varying vec4 v;
varying vec4 diffuse;
varying vec4 spec;

void main()
{
	vec4 diffuse;
	vec4 spec;
	vec4 ambient;

	v = u_ModelViewMatrix * a_Vertex;
	N = normalize(mat3(u_NormalMatrix) * a_Normal);
   
	gl_Position = u_ModelViewProjectionMatrix * a_Vertex;  

	vec3 L = normalize(u_lightPosition.xyz - v.xyz);
	vec3 V = normalize(-v.xyz);
	vec3 R =  normalize(reflect(-L,N));  
	
	if (u_isTextured > 0.5) {
    	v_Texture = a_Texture;
    }
    else {
    	v_Texture = vec2(0.0, 0.0);
    }
    
	ambient = u_ambientMat;
	diffuse = u_diffuseMat * max(dot(N,L), 0.0);
	spec =   u_specMat * pow(max(dot(R,V),0.0),u_specPow);

	color = ambient + diffuse + spec;
}