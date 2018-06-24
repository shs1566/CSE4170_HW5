uniform mat4 ModelViewProjectionMatrix;
uniform mat4 ModelViewMatrix;
uniform mat4 ModelViewMatrixInvTrans;

attribute vec3 v_position;
attribute vec3 v_normal;
attribute vec2 v_tex_coord;

varying vec3 position_eye;
varying vec3 normal_eye;
varying vec2 tex_coord;

void main(void) {
	position_eye = (ModelViewMatrix * vec4(v_position, 1.0)).xyz;
	normal_eye = normalize(ModelViewMatrixInvTrans * vec4(v_normal, 1.0)).xyz;
	tex_coord = v_tex_coord;

	gl_Position = ModelViewProjectionMatrix * vec4(v_position, 1.0);
}