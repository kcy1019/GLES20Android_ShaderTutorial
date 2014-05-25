precision mediump float;

uniform sampler2D u_Texture;
uniform sampler2D u_Normalmap;
uniform float u_isTextured;

varying mediump vec4 color;

void main()
{
	gl_FragColor = color;
}