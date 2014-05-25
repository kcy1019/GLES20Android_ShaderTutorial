precision mediump float;

uniform vec4 u_lightPosition;
uniform vec4 u_eyelightPosition;
uniform vec4 u_spotlightPosition;
uniform mat4 u_NormalMatrix;
uniform mat4 u_ModelViewMatrix;
uniform mat4 u_ModelViewProjectionMatrix;

uniform vec3 u_spotAtt;
uniform vec4 u_spotambientMat;
uniform vec4 u_spotdiffuseMat;
uniform vec4 u_spotspecularMat;

uniform vec3 u_eyeDirection;
uniform vec3 u_eyeAtt;
uniform vec4 u_eyeambientMat;
uniform vec4 u_eyediffuseMat;
uniform vec4 u_eyespecularMat;

uniform vec4 u_ambientMat;
uniform vec4 u_diffuseMat;
uniform vec4 u_specMat;

uniform float u_specPow;
uniform sampler2D u_Texture;
uniform sampler2D u_Normalmap;
uniform float u_isTextured;
uniform float u_isBumped;
uniform float u_isHoled;

varying vec3 N;
varying vec4 v;
varying vec2 v_Texture;
varying float pattern;

// Textrue-Supported Per-Pixel Lighting Shader
// with Bump Mapping(using Normal Map).
// It also supports multiple lights(include spot light),
// Bump normal generator using simplex noise generator.
// Coded By kcy1019(gkcy1019@gmail.com).

vec4 SpotLighting(vec3 spotAtt, vec4 spotlightPosition, vec4 spotambientMat, vec4 spotdiffuseMat, vec4 spotspecularMat, float SpecFactor, float DiffuseFactor, vec3 SpotDir)
{
	spotlightPosition = u_ModelViewMatrix * spotlightPosition;
	vec3 spotL = normalize( spotlightPosition.xyz - v.xyz );
	float dist = length( (u_ModelViewMatrix * spotlightPosition).xyz - v.xyz ); /* for attenuation */
	
	vec4 spotColor = vec4(0.0, 0.0, 0.0, 0.0);
	vec3 spotDirection = normalize(SpotDir);

	float cosInnerCone = 0.965925; // cosine 15 degree.
	float cosOuterCone = 0.939692; // cosine 20 degree.
	float cosCurAngle = dot(-spotL, spotDirection); 
	float cosGapAngle = cosInnerCone - cosOuterCone;
	float Spot = 1.0;
	if (cosCurAngle > 0.0) {
		Spot /= (spotAtt.x + dist * spotAtt.y + dist * dist * spotAtt.z);
	
		Spot *= clamp((cosCurAngle - cosOuterCone) / cosGapAngle, 0.0, 1.0);
	
		spotColor = (spotambientMat * Spot) + (spotdiffuseMat * DiffuseFactor * Spot) + (spotspecularMat * SpecFactor * Spot);
	} 
	
	return spotColor;
}

void main (void)
{
	if (u_isHoled > 0.5) {
		if ( mod(v.x * 45.0, 1.0) >= 0.25 && mod(v.y * 45.0, 1.0) >= 0.25 ) {
			discard;
		}
	}
	vec3 lN = normalize( N );
	if (u_isTextured > 1.5) {
   		lN = normalize(texture2D(u_Normalmap, v_Texture.xy).xyz * 2.0 - 1.0);
   	}
	vec3 L = normalize( u_lightPosition.xyz - v.xyz );
	vec3 V = normalize( -v.xyz); 
	vec3 R =  normalize( reflect(-L,lN) );
	
	vec4 ambient = u_ambientMat * 1.0;
	 
   	if (u_isTextured > 0.5) {
   		ambient = texture2D(u_Texture, v_Texture);
   	}
   	
  	float diffuse = max( dot(lN,L), 0.0 );
   	float spec = pow( max( dot(R,V), 0.0), u_specPow );
   	
   	if (u_isBumped > 0.5) {
		spec *= pattern;
	}
   	
    gl_FragColor = ambient + u_diffuseMat * diffuse + u_specMat * spec +
    	SpotLighting(u_spotAtt, u_spotlightPosition, u_spotambientMat, u_spotdiffuseMat, u_spotspecularMat, spec, diffuse, vec3(0.0, -1.0, -0.1)) +
    	SpotLighting(u_eyeAtt, u_eyelightPosition, u_eyeambientMat, u_eyediffuseMat, u_eyespecularMat, spec, diffuse, u_eyeDirection);
    
}