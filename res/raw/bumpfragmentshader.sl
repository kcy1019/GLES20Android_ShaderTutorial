precision mediump float;

uniform vec4 u_lightPosition;

uniform vec4 u_ambientMat;
uniform vec4 u_diffuseMat;
uniform vec4 u_specMat;
uniform float u_specPow;
uniform sampler2D u_Texture;
uniform float u_isTextured;

varying vec3 N;
varying vec4 v;
varying vec3 v_Texture;
varying float pattern;

// Pseudo-Bumpy Shader Coded by kcy1019 (gkcy1019@gmail.com)

float rand(float a, float b)
{
	return fract(exp( fract( dot(vec2(a, b), vec2(12.9898, 78.233)) ) ) * dot(vec2(a, b), vec2(12.9898, 78.233)));
}

void main (void)
{
	float diffuse;
	vec4 spec;
	vec4 ambient;

	vec3 lN = normalize(N);
	vec3 L = normalize(u_lightPosition.xyz - v.xyz);
	vec3 V = normalize(-v.xyz);
	vec3 R = normalize(reflect(-L, lN));

   	ambient = u_ambientMat;
  	diffuse = max(dot(lN,L), 0.0);
  	
	float spec1 = pow(max(dot(R, V), 0.0), u_specPow);
	float spec2 = pattern * spec1;

	gl_FragColor = ambient + u_diffuseMat * diffuse + u_specMat * (spec1 + spec2);
}