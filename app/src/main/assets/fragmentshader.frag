precision highp float;

struct LIGHT {
	int light_on;
	vec4 position; // assume point or direction in EC in this example shader
	vec4 ambient_color, diffuse_color, specular_color;
	vec3 spot_direction;
	float spot_exponent;
	float spot_cutoff_angle;
	vec4 light_attenuation_factors; // compute this effect only if .w != 0.0f
};

struct MATERIAL {
	vec4 ambient_color;
	vec4 diffuse_color;
	vec4 specular_color;
	vec4 emissive_color;
	float specular_exponent;
};

#define NUMBER_OF_LIGHTS_SUPPORTED 5

uniform vec4 global_ambient_color;
uniform LIGHT light[NUMBER_OF_LIGHTS_SUPPORTED];
uniform MATERIAL material;

uniform int flag_fog;
uniform int flag_blind;
uniform int flag_texture_mapping;

uniform sampler2D base_texture;

const float zero_f = 0.0;
const float one_f = 1.0;

varying vec3 position_eye;
varying vec3 normal_eye;
varying vec2 tex_coord;

vec4 lighting_equation_textured(in vec3 P_eye, in vec3 N_eye, in vec4 base_color) {
	vec4 color_sum;
	float local_scale_factor, tmp_float;
	vec3 L_eye;

//	color_sum = material.emissive_color + global_ambient_color * material.ambient_color;
	color_sum = material.emissive_color + global_ambient_color * base_color;

	for (int i = 0; i < NUMBER_OF_LIGHTS_SUPPORTED; i++) {
		if (light[i].light_on == 0) continue;

		local_scale_factor = one_f;
		if (light[i].position.w != zero_f) { // point light source
			L_eye = light[i].position.xyz - P_eye.xyz;

			if (light[i].light_attenuation_factors.w  != zero_f) {
				vec4 tmp_vec4;

				tmp_vec4.x = one_f;
				tmp_vec4.z = dot(L_eye, L_eye);
				tmp_vec4.y = sqrt(tmp_vec4.z);
				tmp_vec4.w = zero_f;
				local_scale_factor = one_f/dot(tmp_vec4, light[i].light_attenuation_factors);
			}

			L_eye = normalize(L_eye);

			if (light[i].spot_cutoff_angle < 180.0) { // [0.0f, 90.0f] or 180.0f
				float spot_cutoff_angle = clamp(light[i].spot_cutoff_angle, zero_f, 90.0);
				vec3 spot_dir = normalize(light[i].spot_direction);

				tmp_float = dot(-L_eye, spot_dir);
				if (tmp_float >= cos(radians(light[i].spot_cutoff_angle))) {
					tmp_float = pow(tmp_float, light[i].spot_exponent);
					if(i == 2 && flag_blind == 1) {
					    tmp_float = cos(tmp_float * 80.0);
					}
				}
				else {
					tmp_float = zero_f;
				}
				local_scale_factor *= tmp_float;
			}
		}
		else {  // directional light source
			L_eye = normalize(light[i].position.xyz);
		}

		if (local_scale_factor > zero_f) {
		 	vec4 local_color_sum = light[i].ambient_color * material.ambient_color;

			tmp_float = dot(N_eye, L_eye);
			if(tmp_float > zero_f) {
                local_color_sum += light[i].diffuse_color*base_color*tmp_float;

                vec3 H_eye = normalize(L_eye - normalize(P_eye));
                tmp_float = max(zero_f, dot(N_eye, H_eye));
                if (tmp_float > zero_f) {
                    local_color_sum += light[i].specular_color
                                           *material.specular_color*pow(tmp_float, material.specular_exponent);
                }
			}
			color_sum += local_scale_factor*local_color_sum;
		}
	}
 	return color_sum;
}

#define FOG_COLOR vec4(0.9, 0.9, 0.9, 1.0)
#define FOG_NEAR_DISTANCE 10.0
#define FOG_FAR_DISTANCE 1000.0

void main(void) {
	vec4 base_color, shaded_color;
	float fog_factor;

    if (flag_texture_mapping == 1)
	    base_color = texture2D(base_texture, tex_coord);
	else
	    base_color = material.diffuse_color;

	shaded_color = lighting_equation_textured(position_eye, normalize(normal_eye), base_color);

	if (flag_fog == 1) {
		fog_factor = (FOG_FAR_DISTANCE - length(position_eye.xyz))/(FOG_FAR_DISTANCE - FOG_NEAR_DISTANCE);
		fog_factor = clamp(fog_factor, 0.0, 1.0);
		gl_FragColor = mix(FOG_COLOR, shaded_color, fog_factor);
	}
	else
		gl_FragColor = shaded_color;
}
