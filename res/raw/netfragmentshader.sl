precision mediump float;

uniform vec4 u_lightPosition;

uniform vec4 u_ambientMat;
uniform vec4 u_diffuseMat;
uniform vec4 u_specMat;
uniform float u_specPow;

varying vec3 N;
varying vec4 v;
varying vec3 temp;

void main (void)
{
	vec4 diffuse;
	vec4 spec;
	vec4 ambient;
	if ( mod(v.x * 30.0, 1.0) >= 0.25 && mod(v.y * 30.0, 1.0) >= 0.25 ) {
		discard;
	}
	vec3 lN = normalize(N);
	vec3 L = normalize(u_lightPosition.xyz - v.xyz);
	vec3 V = normalize(-v.xyz);
	vec3 R =  normalize(reflect(-L,lN));  

   	ambient = u_ambientMat;
  	diffuse = u_diffuseMat * max(dot(lN,L), 0.0);
   	spec = u_specMat * pow(max(dot(R,V),0.0),u_specPow);

   gl_FragColor = ambient + diffuse + spec;
}